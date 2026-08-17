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

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.rest.application.filter.ObjectMetadataResponseFilter;
import org.eclipse.fennec.model.atlas.rest.common.ModelAtlasRestConstants;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
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
	 * Checks the {@code If-Match} header for an optimistic-concurrency precondition against the current
	 * state of {@code metadata}, using the same validator the response filter emits as the ETag.
	 * Returns a {@code 412 Precondition Failed} response if the precondition is not satisfied, or
	 * {@code null} if it is satisfied, if no {@code If-Match} header was sent, or if there is no
	 * validator to compare against.
	 *
	 * @param headers  the request headers
	 * @param metadata the current metadata of the object being written
	 * @param target   which validator to check against: {@link ObjectMetadataResponseFilter.CacheTarget#CONTENT}
	 *                 for writes that replace the content, {@link ObjectMetadataResponseFilter.CacheTarget#METADATA}
	 *                 for writes that only change metadata (e.g. a stage transition)
	 */
	static Response checkIfMatch(HttpHeaders headers, ObjectMetadata metadata,
			ObjectMetadataResponseFilter.CacheTarget target) {
		String ifMatch = headers.getHeaderString("If-Match");
		if (ifMatch == null) {
			return null; // No precondition — proceed normally
		}
		String base = ObjectMetadataResponseFilter.baseValidator(metadata, target);
		if (base == null) {
			return null; // No validator yet — cannot validate, proceed
		}
		if (!ObjectMetadataResponseFilter.ifMatchSatisfied(ifMatch, base)) {
			return Response.status(Response.Status.PRECONDITION_FAILED)
					.entity("Resource has been modified. ETag mismatch.").build();
		}
		return null;
	}
}
