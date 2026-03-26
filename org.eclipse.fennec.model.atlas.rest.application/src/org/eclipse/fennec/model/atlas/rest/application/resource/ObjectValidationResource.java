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
package org.eclipse.fennec.model.atlas.rest.application.resource;

import java.util.List;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.fennec.m2x.ocl.api.OclEngine;
import org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype;
import org.eclipse.fennec.model.atlas.rest.model.DiagnosticType;
import org.eclipse.fennec.model.atlas.rest.model.RestFactory;
import org.eclipse.fennec.model.atlas.runtime.RequireRuntime;
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
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * 
 * @author ilenia
 * @since Mar 16, 2026
 */
@RequireRuntime
@JakartarsResource()
@JakartarsName("ObjectValidationResource")
@Component(name = "ObjectValidationResource", service = ObjectValidationResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/validate")
@Tag(name = "Object Validation Resource", description = "CRUD operations for validating an object against a model atlas schema")
public class ObjectValidationResource {

	private final List<String> supportedMediaTypes;

	@Context
	private HttpHeaders headers;

	@QueryParam("mediaType")
	private String mediaType;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private OclEngine oclEngine; 

	@Activate
	public ObjectValidationResource(@Reference SupportedMediatype types) {
		supportedMediaTypes = types.getSupportedMediaTypes();
	}

	@POST
	@Consumes
	@Produces
	@Operation(summary = "Validates the object against its schema", description = "Validates the object against its schema. Returns the validation errors, or 200, if the validation succeded", responses = {
			@ApiResponse(responseCode = "200", description = "Object validation was performed. A Response with the list of errors/warnings is returned."
					+ " The list might be empty, if the valudation did not encounter any issue", 
					content = @Content(schema = @Schema(implementation = org.eclipse.fennec.model.atlas.rest.model.Diagnostic.class))),
			@ApiResponse(responseCode = "415", description = "Unsupported media type"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public Response validate(
			@RequestBody(description = "The object to validate", required = true, content = @Content(schema = @Schema(implementation = EObject.class))) EObject eObject) {
		try {
			checkContentType();
			Diagnostic emfDiagnostic = Diagnostician.INSTANCE.validate(eObject);			
			org.eclipse.fennec.model.atlas.rest.model.Diagnostic diagnostic = getDiagnostics(emfDiagnostic);
			return Response.status(Response.Status.OK).entity(diagnostic).header("Content-Type", mediaType).build();

		} catch (WebApplicationException e) {
			// WebApplicationException already has the correct status code, rethrow it
			throw e;
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	private org.eclipse.fennec.model.atlas.rest.model.Diagnostic getDiagnostics(Diagnostic emfDiagnostic) {
		org.eclipse.fennec.model.atlas.rest.model.Diagnostic diagnostic = RestFactory.eINSTANCE.createDiagnostic();
		diagnostic.setType(getDiagnosticType(emfDiagnostic.getSeverity()));
		diagnostic.setMessage(emfDiagnostic.getMessage());
		diagnostic.setSource(emfDiagnostic.getSource());
		diagnostic.setExceptionMsg(emfDiagnostic.getException() != null ? emfDiagnostic.getException().getMessage() : null);
		emfDiagnostic.getChildren().forEach(child -> {
			diagnostic.getChildren().add(getDiagnostics(child));
		});
		emfDiagnostic.getData().forEach(d -> diagnostic.getData().add(d.toString()));
		return diagnostic;
	}

	private DiagnosticType getDiagnosticType(int emfDiagnosticType) {
		switch(emfDiagnosticType) {
		case Diagnostic.OK:
			return DiagnosticType.OK;
		case Diagnostic.CANCEL:
			return DiagnosticType.CANCEL;
		case Diagnostic.INFO:
			return DiagnosticType.INFO;
		case Diagnostic.WARNING:
			return DiagnosticType.WARNING;
		case Diagnostic.ERROR:
			return DiagnosticType.ERROR;
		}
		return DiagnosticType.OK;
	}

	/**
	 * Check that the Accept header contains a supported media type.
	 */    
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
			// Default to JSON
			mediaType = MediaType.APPLICATION_JSON;
			return;
		}
		throw new WebApplicationException(Status.UNSUPPORTED_MEDIA_TYPE);
	}
}
