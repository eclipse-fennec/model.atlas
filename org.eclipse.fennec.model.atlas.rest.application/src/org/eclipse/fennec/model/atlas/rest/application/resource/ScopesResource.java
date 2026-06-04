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

import java.util.Date;
import java.util.List;

import org.eclipse.fennec.model.atlas.rest.application.aggregate.ScopeAggregateService;
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
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.HttpHeaders;
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

    @Reference
    private ScopeAggregateService aggregateService;

    /**
     * List all configured scopes.
     *
     * @return List of Scope objects
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
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
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML })
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

    /**
     * Aggregate change-detection probe for an entire scope. Returns a strong, order-independent
     * {@code ETag} over every package and registered EObject in the scope plus a {@code Last-Modified},
     * so a client (e.g. a drift watcher) can poll one scope instead of every cached entry.
     *
     * <p>On a stale {@code If-None-Match} whose baseline is still known, the {@code 200} response also
     * carries {@code Atlas-Changed-NsUris} and/or {@code Atlas-Changed-Objects} listing exactly which
     * entries changed; a matching {@code If-None-Match} yields {@code 304} with no diff headers.
     *
     * @param scopeName   the scope name
     * @param ifNoneMatch the previously seen aggregate ETag, if any
     * @return {@code 304} when unchanged, {@code 200} (with change hints) otherwise, {@code 404} if the
     *         scope is unknown
     */
    @HEAD
    @Path("/{scopeName}")
    @Operation(summary = "Scope aggregate validator", description = "Aggregate ETag / Last-Modified over the whole scope, with "
            + "Atlas-Changed-NsUris / Atlas-Changed-Objects change hints on a stale If-None-Match.", responses = {
                    @ApiResponse(responseCode = "200", description = "Aggregate validator returned; change hints present when a stale If-None-Match baseline is known"),
                    @ApiResponse(responseCode = "304", description = "Scope unchanged since the supplied If-None-Match"),
                    @ApiResponse(responseCode = "404", description = "Scope not found") })
    public Response scopeAggregate(
            @Parameter(description = "The name of the workflow scope", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "Previously seen aggregate ETag") @HeaderParam(HttpHeaders.IF_NONE_MATCH) String ifNoneMatch) {
        ScopeAggregateService.ScopeAggregate aggregate = aggregateService.computeAggregate(scopeName);
        if (aggregate == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(String.format("No scope with name %s was found.", scopeName)).build();
        }

        Response.ResponseBuilder rb = Response.ok().tag(new EntityTag(aggregate.etag()))
                .header(HttpHeaders.VARY, HttpHeaders.ACCEPT);
        if (aggregate.lastModified() != null) {
            rb.lastModified(Date.from(aggregate.lastModified()));
        }

        if (aggregateService.matchesIfNoneMatch(ifNoneMatch, aggregate.etag())) {
            return rb.status(Response.Status.NOT_MODIFIED).build();
        }

        ScopeAggregateService.ScopeDiff diff = aggregateService.diffSince(scopeName, ifNoneMatch, aggregate);
        if (diff.baselineKnown()) {
            if (!diff.changedNsUris().isEmpty()) {
                rb.header("Atlas-Changed-NsUris", String.join(",", diff.changedNsUris()));
            }
            if (!diff.changedObjects().isEmpty()) {
                rb.header("Atlas-Changed-Objects", String.join(",", diff.changedObjects()));
            }
        }
        return rb.build();
    }
}
