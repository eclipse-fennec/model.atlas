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

import java.util.List;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype;
import org.eclipse.fennec.model.atlas.runtime.RequireRuntime;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
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
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 *
 * @author ilenia
 * @since Feb 5, 2026
 */
@RequireRuntime
@JakartarsResource()
@JakartarsName("ModelConverterResource")
@Component(name = "ModelConverterResource", service = ModelConverterResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/convert")
@Tag(name = "Model Converter Resource", description = "CRUD operations for converting models on the fly from one format to another")
public class ModelConverterResource {

    private final List<String> supportedMediaTypes;

    @Context
    private HttpHeaders headers;

    @Activate
    public ModelConverterResource(@Reference SupportedMediatype types) {
        supportedMediaTypes = types.getSupportedMediaTypes();
    }

    @POST
    @Consumes
    @Produces
    @Operation(summary = "Converts the schema to another format", description = "Converts the provided schema into the same schema but in the format specified by the Accept header", responses = {
            @ApiResponse(responseCode = "200", description = "Package converted successfully", content = @Content(schema = @Schema(implementation = EPackage.class))),
            @ApiResponse(responseCode = "415", description = "Unsupported media type"),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response convertPackage(
            @RequestBody(description = "The schema package content", required = true, content = @Content(schema = @Schema(implementation = EPackage.class))) EPackage ePackage) {

        checkContentType();
        try {
            return Response.status(Response.Status.OK).entity(ePackage).build();
            
    	} catch (WebApplicationException e) {
			// WebApplicationException already has the correct status code, rethrow it
			throw e;
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * Check that the Accept header contains a supported media type.
     */
    private void checkContentType() {
        List<MediaType> acceptableMediaTypes = headers.getAcceptableMediaTypes();
        for (MediaType acceptedMediaType : acceptableMediaTypes) {
            String accept = acceptedMediaType.getType() + "/" + acceptedMediaType.getSubtype();
            if (supportedMediaTypes.contains(accept)) {
                return;
            }
        }
        // Wildcard accepts anything, default to JSON
        for (MediaType acceptedMediaType : acceptableMediaTypes) {
            if (acceptedMediaType.isWildcardType() || acceptedMediaType.isWildcardSubtype()) {
                return;
            }
        }
        throw new WebApplicationException(Status.UNSUPPORTED_MEDIA_TYPE);
    }
}
