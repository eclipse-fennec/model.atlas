/*
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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.validation.rest;

import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype;
import org.eclipse.fennec.model.atlas.rest.common.ResourceAttacherHelper;
import org.eclipse.fennec.model.atlas.runtime.RequireRuntime;
import org.eclipse.fennec.model.atlas.validation.ValidationService;
import org.eclipse.fennec.model.atlas.validation.model.cocl.BatchValidationRequest;
import org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResponse;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * @author ilenia
 * @since Mar 16, 2026
 */
@RequireRuntime
@JakartarsResource()
@JakartarsName("ObjectBatchValidationResource")
@Path("/{scopeName}/{stageName}/validate/batch")
@Component(name = "ObjectBatchValidationResource", service = ObjectBatchValidationResource.class, scope = ServiceScope.PROTOTYPE)
@Tag(name = "Object Batch Validation Resource", description = "CRUD operations for validating an object against a model atlas schema")
public class ObjectBatchValidationResource {

	private final List<String> supportedMediaTypes;

	@PathParam("scopeName")
	private String scopeName;

	@PathParam("stageName")
	private String stageName;

	@Context
	private HttpHeaders headers;

	@QueryParam("mediaType")
	private String mediaType;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ValidationService validationService;

	@Context
	private ResourceSet resourceSet;

	@Activate
	public ObjectBatchValidationResource(@Reference SupportedMediatype types) {
		supportedMediaTypes = types.getSupportedMediaTypes();
	}

	@POST
	@Consumes({"application/xmi", "application/xml", MediaType.APPLICATION_JSON})
	@Produces({"application/xmi", MediaType.APPLICATION_JSON})
	@Operation(summary = "Validates the provided objects against their schema and an additional OCLConstraintSet", description = "Validates the provided objects against their schema and an additional OCLConstraintSet, whose id has to be provided.",
	responses = {
			@ApiResponse(responseCode = "200", description = "Object validation was performed. A Response with the ValidationResponse is returned.",
					content = @Content(schema = @Schema(implementation = ValidationResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input: no objects, no C-OCL id, wrong filter role, or no active VALIDATION constraints"),
			@ApiResponse(responseCode = "404", description = "Scope or COCL registry not found"),
			@ApiResponse(responseCode = "415", description = "Unsupported media type"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public Response validate(
			@RequestBody(description = "The batch validation request", required = true, content = @Content(schema = @Schema(implementation = BatchValidationRequest.class))) BatchValidationRequest validationRequest) {
		try {
			checkContentType();
			ResourceAttacherHelper.attach(resourceSet, validationRequest);
			ValidationResponse response = validationService.validateBatch(validationRequest, scopeName, resourceSet);
			return Response.status(Response.Status.OK).entity(response).header("Content-Type", mediaType).build();
		} catch (IllegalArgumentException e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		} catch (NoSuchElementException e) {
			return Response.status(Status.NOT_FOUND).entity(e.getMessage()).build();
		} catch (WebApplicationException e) {
			throw e;
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path("/filter")
	@Consumes({"application/xmi", "application/xml", MediaType.APPLICATION_JSON})
	@Produces({"application/xmi", MediaType.APPLICATION_JSON})
	@Operation(summary = "Filter the provided objects based on a C-OCL ConstraintSet", description = "Filter the provided objects based on a C-OCL ConstraintSet.",
	responses = {
			@ApiResponse(responseCode = "200", description = "Filtering was performed successfully. A ValidationResponse with the corresponding results and diagnostics is returned.",
					content = @Content(schema = @Schema(implementation = ValidationResponse.class))),
			@ApiResponse(responseCode = "204", description = "If no filter constraint in the C-OCL Constraint Set has been found, or if after performing the filter the original data remains unchanged"),
			@ApiResponse(responseCode = "400", description = "Invalid input"),
			@ApiResponse(responseCode = "404", description = "Scope or COCL registry not found"),
			@ApiResponse(responseCode = "415", description = "Unsupported media type"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public Response filter(
			@RequestBody(description = "The BatchValidationRequest", required = true,
			content = @Content(schema = @Schema(implementation = BatchValidationRequest.class))) BatchValidationRequest validationRequest) {
		try {
			checkContentType();
			ResourceAttacherHelper.attach(resourceSet, validationRequest);
			ValidationResponse response = validationService.filterBatch(validationRequest, scopeName, resourceSet);
			if (response == null) {
				return Response.status(Status.NO_CONTENT).build();
			}
			return Response.status(Response.Status.OK).entity(response).header("Content-Type", mediaType).build();
		} catch (IllegalArgumentException e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		} catch (NoSuchElementException e) {
			return Response.status(Status.NOT_FOUND).entity(e.getMessage()).build();
		} catch (WebApplicationException e) {
			throw e;
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	// ---- infrastructure helpers ----

	private void checkContentType() {
		if (mediaType != null) {
			if (supportedMediaTypes.contains(mediaType)) {
				return;
			}
		} else {
			List<MediaType> acceptableMediaTypes = headers.getAcceptableMediaTypes();
			for (MediaType acceptedMediaType : acceptableMediaTypes) {
				String accept = acceptedMediaType.getType() + "/" + acceptedMediaType.getSubtype();
				if (supportedMediaTypes.contains(accept)) {
					mediaType = accept;
					return;
				}
			}
			mediaType = MediaType.APPLICATION_JSON;
			return;
		}
		throw new WebApplicationException(Status.UNSUPPORTED_MEDIA_TYPE);
	}
}
