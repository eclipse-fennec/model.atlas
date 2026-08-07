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

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.rest.application.exception.EndpointFailures;
import org.eclipse.fennec.model.atlas.runtime.RequireRuntime;
import org.osgi.service.component.annotations.Component;
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
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.eclipse.fennec.codec.constants.CodecOptions;
import org.eclipse.fennec.codec.rest.annotations.ResourceOption;

/**
 *
 * @author ilenia
 * @since Feb 5, 2026
 */
@RequireRuntime
@JakartarsResource()
@JakartarsName("ModelConverterResource")
@Component(name = "ModelConverterResource", service = ModelConverterResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/{scopeName}/stages/{stageName}/convert")
@Tag(name = "Model Converter Resource", description = "CRUD operations for converting models on the fly from one format to another")
public class ModelConverterResource {

    @Context
    private ContainerRequestContext requestContext;

    @POST
    @Consumes
    @Produces
    @Operation(summary = "Converts the schema to another format", description = "Converts the provided schema into the same schema but in the format specified by the Accept header", responses = {
            @ApiResponse(responseCode = "200", description = "Package converted successfully", content = @Content(schema = @Schema(implementation = EPackage.class))),
            @ApiResponse(responseCode = "415", description = "Unsupported media type"),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
    public Response convertPackage(
            @RequestBody(description = "The schema package content", required = true, content = @Content(schema = @Schema(implementation = EPackage.class))) EPackage ePackage) {

        try {
            return Response.status(Response.Status.OK).entity(ePackage).header("Content-Type", ResourceSupport.resolvedMediaType(requestContext)).build();
            
    	} catch (WebApplicationException e) {
			// WebApplicationException already has the correct status code, rethrow it
			throw e;
        } catch (Exception e) {
            throw EndpointFailures.propagate(e);
        }
    }
}
