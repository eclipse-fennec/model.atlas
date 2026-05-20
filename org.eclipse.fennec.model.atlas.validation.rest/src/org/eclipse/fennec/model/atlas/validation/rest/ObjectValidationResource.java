/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.validation.rest;

import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.codec.rest.jakartas.JakartaRestConstants;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype;
import org.eclipse.fennec.model.atlas.runtime.RequireRuntime;
import org.eclipse.fennec.model.atlas.validation.ValidationService;
import org.eclipse.fennec.model.atlas.validation.model.cocl.DerivedValidationRequest;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OperationValidationRequest;
import org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResponse;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import jakarta.ws.rs.container.ContainerRequestContext;
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
@JakartarsName("ObjectValidationResource")
@Path("/{scopeName}/{stageName}/validate")
@Component(name = "ObjectValidationResource", service = ObjectValidationResource.class, scope = ServiceScope.PROTOTYPE)
@Tag(name = "Object Validation Resource", description = "CRUD operations for validating an object against a model atlas schema")
public class ObjectValidationResource {

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
    private ContainerRequestContext requestContext;


	@Activate
	public ObjectValidationResource(@Reference SupportedMediatype types) {
		supportedMediaTypes = types.getSupportedMediaTypes();
	}

	@POST
	@Consumes("application/xmi")
	@Produces({"application/xmi", MediaType.APPLICATION_JSON})
	@Operation(summary = "Validates the object against its schema", description = "Validates the object against its schema. Returns the validation errors, or 200, if the validation succeeded", responses = {
			@ApiResponse(responseCode = "200", description = "Object validation was performed. A Response with the list of errors/warnings is returned."
					+ " The list might be empty, if the validation did not encounter any issue",
					content = @Content(schema = @Schema(implementation = org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic.class))),
			@ApiResponse(responseCode = "415", description = "Unsupported media type"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public Response validate(
			@RequestBody(description = "The object to validate", required = true, content = @Content(schema = @Schema(implementation = EObject.class))) EObject eObject) {
		try {
			checkContentType();
			org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic diagnostic = validationService.validate(eObject);
			return Response.status(Response.Status.OK).entity(diagnostic).header("Content-Type", mediaType).build();
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
	@Path("/{oclId}")
	@Consumes({"application/xmi", "application/xml", MediaType.APPLICATION_JSON})
	@Produces({"application/xmi", MediaType.APPLICATION_JSON})
	@Operation(summary = "Validates the object against its schema and an additional OCLConstraintSet", description = "Validates the object against its schema and an additional OCLConstraintSet, whose id has to be provided.",
	responses = {
			@ApiResponse(responseCode = "200", description = "Object validation was performed. A Response with the list of errors/warnings is returned."
					+ " The list might be empty, if the validation did not encounter any issue",
					content = @Content(schema = @Schema(implementation = org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic.class))),
			@ApiResponse(responseCode = "415", description = "Unsupported media type"),
			@ApiResponse(responseCode = "404", description = "OCLConstraintSet id was not found in C-OCL registry or OCLConstraintSet cannot handle the provided EObject"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public Response validateByOCLId(
			@PathParam("oclId") String oclId,
			@RequestBody(description = "The object to validate", required = true, content = @Content(schema = @Schema(implementation = EObject.class))) EObject eObject) {
		try {
			checkContentType();
			ValidationResponse response = validationService.validateWithOcl(eObject, oclId, scopeName, getResolvedResourceSetFactory().createResourceSet());
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
	@Path("/derive")
	@Consumes({"application/xmi", "application/xml", MediaType.APPLICATION_JSON})
	@Produces({"application/xmi", MediaType.APPLICATION_JSON})
	@Operation(summary = "Computes derived features for the provided EObject using either its model or the provided C-OCL id", description = "Computes derived features for the provided EObject using either its model or the provided C-OCL id.",
	responses = {
			@ApiResponse(responseCode = "200", description = "Derived feature performed. A ValidationResponse with the corresponding results and diagnostics is returned.",
					content = @Content(schema = @Schema(implementation = ValidationResponse.class))),
			@ApiResponse(responseCode = "415", description = "Unsupported media type"),
			@ApiResponse(responseCode = "400", description = "If no EObject or more than one EObject has to be validated or if the provided OCLConstraintSet cannot handle the provided EObject or if one or more feature in the request are not in the EObject EClass"),
			@ApiResponse(responseCode = "404", description = "If the scope or COCL registry is not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public Response derive(
			@Parameter(description = "The C-OCL id where to compute the derived expression from", required = false)
			@QueryParam("oclId") String oclId,
			@RequestBody(description = "The DerivedValidationRequest", required = true,
			content = @Content(schema = @Schema(implementation = DerivedValidationRequest.class))) DerivedValidationRequest validationRequest) {
		try {
			checkContentType();
			ValidationResponse response = validationService.derive(validationRequest, oclId, scopeName, getResolvedResourceSetFactory().createResourceSet());
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
	@Path("/compute")
	@Consumes({"application/xmi", "application/xml", MediaType.APPLICATION_JSON})
	@Produces({"application/xmi", MediaType.APPLICATION_JSON})
	@Operation(summary = "Computes EOperation for the provided EObjects", description = "Computes EOperation for the provided EObjects.",
	responses = {
			@ApiResponse(responseCode = "200", description = "EOperation performed. A ValidationResponse with the corresponding results and diagnostics is returned.",
					content = @Content(schema = @Schema(implementation = ValidationResponse.class))),
			@ApiResponse(responseCode = "400", description = "No object to validate is provided or no matching EOperation in the object EClass is found"),
			@ApiResponse(responseCode = "415", description = "Unsupported media type"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public Response compute(
			@RequestBody(description = "The OperationValidationRequest", required = true,
			content = @Content(schema = @Schema(implementation = OperationValidationRequest.class))) OperationValidationRequest validationRequest) {
		try {
			checkContentType();
			ValidationResponse response = validationService.compute(validationRequest, scopeName, getResolvedResourceSetFactory().createResourceSet());
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
	
	private ResourceSetFactory getResolvedResourceSetFactory() {
        return (ResourceSetFactory) requestContext.getProperty(JakartaRestConstants.RESOLVED_RESOURCE_SET_FACTORY);
    }
}
