/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.rest.application.resource;

import java.util.Arrays;
import java.util.Locale;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.fennec.model.atlas.registry.api.BatchItemResult;
import org.eclipse.fennec.model.atlas.registry.api.BatchResult;
import org.eclipse.fennec.model.atlas.registry.api.ConflictPolicy;
import org.eclipse.fennec.model.atlas.registry.api.Precondition;
import org.eclipse.fennec.model.atlas.registry.api.RefusalReason;
import org.eclipse.fennec.model.atlas.registry.api.RegistryAccessException;
import org.eclipse.fennec.model.atlas.rest.application.filter.ObjectMetadataResponseFilter;
import org.eclipse.fennec.model.atlas.rest.common.ModelAtlasRestConstants;
import org.eclipse.fennec.model.atlas.rest.model.BatchItem;
import org.eclipse.fennec.model.atlas.rest.model.BatchResponse;
import org.eclipse.fennec.model.atlas.rest.model.RestFactory;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response;

/**
 * Request-handling helpers shared by the resources in this package.
 *
 * <p>
 * These are the pieces every resource needs and none of them owns: reading what
 * {@code ModelAtlasRequestFilter} resolved as the media type, and checking the
 * {@code If-Match} precondition against the validator
 * {@link ObjectMetadataResponseFilter} emits as the ETag. They stay in this
 * bundle rather than moving to {@code rest.common} because
 * {@code ObjectMetadataResponseFilter} lives here — a helper in
 * {@code rest.common} would have to depend back on {@code rest.application}.
 * </p>
 */
final class ResourceSupport {

	private ResourceSupport() {
	}

	/**
	 * The media type {@code ModelAtlasRequestFilter} resolved for this request
	 * from the {@code mediaType} query parameter or the {@code Accept} header.
	 *
	 * @param requestContext the current request
	 * @return the resolved media type, or {@code null} if the filter did not run
	 */
	static String resolvedMediaType(ContainerRequestContext requestContext) {
		return (String) requestContext.getProperty(ModelAtlasRestConstants.RESOLVED_MEDIA_TYPE);
	}

	/**
	 * The {@code If-Match} header as a {@link Precondition} the {@code RegistryWriter} checks
	 * against the object it is about to change, using the same validator the response filter
	 * emits as the ETag. Without an {@code If-Match} header, or when the object has no validator
	 * yet, every state is acceptable; a mismatch makes the writer refuse with
	 * {@link RefusalReason#PRECONDITION_FAILED}, answered as {@code 412 Precondition Failed}.
	 *
	 * @param headers the request headers
	 * @param target  which validator to check against: {@link ObjectMetadataResponseFilter.CacheTarget#CONTENT}
	 *                for writes that replace the content, {@link ObjectMetadataResponseFilter.CacheTarget#METADATA}
	 *                for writes that only change metadata (e.g. a stage transition)
	 * @return the precondition; never {@code null}
	 */
	static Precondition ifMatch(HttpHeaders headers, ObjectMetadataResponseFilter.CacheTarget target) {
		String ifMatch = headers.getHeaderString("If-Match");
		if (ifMatch == null) {
			return Precondition.NONE; // No precondition — proceed normally
		}
		return current -> {
			String base = ObjectMetadataResponseFilter.baseValidator(current, target);
			// No validator yet — cannot validate, proceed
			return base == null || ObjectMetadataResponseFilter.ifMatchSatisfied(ifMatch, base);
		};
	}

	/**
	 * The answer to a request the {@code RegistryWriter} refused: the status its reason stands
	 * for, with the writer's client-facing message as a plain body — the shape these endpoints
	 * have always answered refusals in.
	 *
	 * @param refusal the writer's refusal
	 * @return the response to return
	 */
	static Response refusal(RegistryAccessException refusal) {
		return Response.status(status(refusal.reason())).entity(refusal.getMessage()).build();
	}

	/** The status a refusal of the given reason is answered with. */
	private static Status status(RefusalReason reason) {
		return switch (reason) {
		case INVALID, INCOMPATIBLE_TYPE, UNKNOWN_SCOPE, UNKNOWN_REGISTRY -> Status.BAD_REQUEST;
		case NO_SCHEMA_REGISTRY -> Status.NOT_FOUND;
		case ALREADY_EXISTS, INHERITED -> Status.CONFLICT;
		case READ_ONLY, DERIVED_CONTENT -> Status.FORBIDDEN;
		case PRECONDITION_FAILED -> Status.PRECONDITION_FAILED;
		case INTERRUPTED -> Status.SERVICE_UNAVAILABLE;
		};
	}

	/**
	 * The {@code onConflict} query parameter of a batch request.
	 *
	 * @param value the parameter, case-insensitive; {@code null} means {@link ConflictPolicy#FAIL}
	 * @return the policy
	 * @throws RegistryAccessException {@link RefusalReason#INVALID} for an unknown value
	 */
	static ConflictPolicy conflictPolicy(String value) {
		if (value == null || value.isBlank()) {
			return ConflictPolicy.FAIL;
		}
		try {
			return ConflictPolicy.valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new RegistryAccessException(RefusalReason.INVALID, String.format(
					"Query parameter onConflict '%s' must be one of %s", value, Arrays.toString(ConflictPolicy.values())));
		}
	}

	/**
	 * The objectId a root object of a batch is stored under: its EMF ID attribute, as the initial
	 * model loader derives it, or else the id the resource gives it ({@code xmi:id}).
	 *
	 * @param root the root object
	 * @return the id, or {@code null} if the object has neither
	 */
	static String batchObjectId(EObject root) {
		String id = EcoreUtil.getID(root);
		if (id != null && !id.isBlank()) {
			return id;
		}
		if (root.eResource() instanceof XMLResource xmlResource) {
			id = xmlResource.getID(root);
		}
		return id == null || id.isBlank() ? null : id;
	}

	/**
	 * The answer to a batch write: {@code 200} with one item per root when the batch was applied,
	 * otherwise the status of the first refusal, with the same body naming every refusal.
	 *
	 * @param result         what the writer did
	 * @param requestContext the current request
	 * @return the response to return
	 */
	static Response batch(BatchResult result, ContainerRequestContext requestContext) {
		BatchResponse response = RestFactory.eINSTANCE.createBatchResponse();
		response.setApplied(result.isApplied());
		for (BatchItemResult itemResult : result.getItems()) {
			BatchItem item = RestFactory.eINSTANCE.createBatchItem();
			item.setId(itemResult.getId());
			if (itemResult.isSetOutcome()) {
				item.setOutcome(itemResult.getOutcome().name());
			}
			if (itemResult.isSetRefusal()) {
				item.setRefusal(itemResult.getRefusal().name());
			}
			item.setMessage(itemResult.getMessage());
			if (itemResult.getMetadata() != null) {
				item.setMetadata(EcoreUtil.copy(itemResult.getMetadata()));
			}
			response.getItems().add(item);
		}
		Status status = result.firstRefusal().map(refused -> status(refused.getRefusal())).orElse(Status.OK);
		return Response.status(status).entity(response).header("Content-Type", resolvedMediaType(requestContext))
				.build();
	}
}
