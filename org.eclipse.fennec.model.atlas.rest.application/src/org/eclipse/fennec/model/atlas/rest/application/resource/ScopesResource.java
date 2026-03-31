/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
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

import org.eclipse.fennec.model.atlas.rest.model.RestFactory;
import org.eclipse.fennec.model.atlas.rest.model.ScopeListResponse;
import org.eclipse.fennec.model.atlas.runtime.RequireRuntime;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.workflow.ScopeServiceCollector;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST API for Scope discovery and management. Provides endpoints to list and
 * retrieve scope metadata.
 *
 * @author Data In Motion
 * @since 1.0
 */
@RequireRuntime
@JakartarsResource()
@JakartarsName("ScopesResource")
@Component(name = "ScopeResource", service = ScopesResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/scopes")
@Tag(name = "Scope Management", description = "Discovery and management of Model Atlas scopes")
public class ScopesResource {

    @Reference
    private ScopeServiceCollector scopeCollector;

    /**
     * List all configured scopes.
     *
     * @return List of Scope objects
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List all scopes", description = "Get a list of all configured scopes in the Model Atlas", responses = {
            @ApiResponse(responseCode = "200", description = "Scopes retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ScopeListResponse.class))), })
    public Response listScopes() {
        List<Scope> scopes = scopeCollector.getAllScopes();
        ScopeListResponse container = RestFactory.eINSTANCE.createScopeListResponse();
        container.getScopes().addAll(scopes);
        return Response.status(Response.Status.OK).entity(container).build();
    }

    /**
     * Get metadata for a specific scope.
     *
     * @param scopeName the name of the scope
     * @return Scope object
     */
    @GET
    @Path("/{scopeName}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    @Operation(summary = "Get workflow scope metadata", description = "Retrieve metadata for a specific workflow scope by name", responses = {
            @ApiResponse(responseCode = "200", description = "Scope found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Scope.class))),
            @ApiResponse(responseCode = "404", description = "Scope not found"), })
    public Response getScopeByName(
            @Parameter(description = "The name of the workflow scope", required = true) @PathParam("scopeName") String scopeName) {
        Scope scope = scopeCollector.getScopeByName(scopeName);
        if (scope == null)
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(String.format("No scope with name %s was found.", scopeName)).build();
        return Response.status(Response.Status.OK).entity(scope).build();
    }
}
