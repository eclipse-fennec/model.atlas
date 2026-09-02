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
package org.eclipse.fennec.model.atlas.rest.qvt;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.m2x.unit.api.Unit;
import org.eclipse.fennec.m2x.unit.api.UnitKey;
import org.eclipse.fennec.m2x.unit.api.UnitKind;
import org.eclipse.fennec.m2x.unit.api.UnitStoreException;
import org.eclipse.fennec.m2x.unit.store.PackagedUnit;
import org.eclipse.fennec.model.atlas.qvt.AtlasUnitStore;
import org.eclipse.fennec.model.atlas.qvt.QvtUnits;
import org.eclipse.fennec.model.atlas.runtime.RequireRuntime;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.workflow.RegistryServiceCollector;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Serves compiled QVT units by their consumer address — qualified name plus
 * fingerprint, within a (scope, registry, stage) view (issue #239). The stage
 * is part of the address deliberately: a unit fingerprint does not fold in
 * package fingerprints, so name + fingerprint is unique only within one
 * stage's package view.
 *
 * <p>
 * Rooted at {@code …/registries/{registry}/units/{stage}/…} — beside, not
 * inside, the generic {@code …/stages/{stage}/{objectId}} routes: a sub-path
 * under {@code /stages} would shadow the generic endpoints for the literal
 * object id {@code units}.
 * </p>
 */
@RequireRuntime
@JakartarsResource
@JakartarsName("QvtUnitsResource")
@Component(name = "QvtUnitsResource", service = QvtUnitsResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/{scopeName}/registries/{registryName}/units/{stageName}")
@Tag(name = "QVT Units", description = "Compiled QVT-O units addressed by qualified name + fingerprint")
public class QvtUnitsResource {

    @Reference
    private RegistryServiceCollector registryCollector;

    @GET
    @Path("/{qualifiedName}")
    @Consumes
    @Produces
    @Operation(summary = "Resolve a compiled unit by qualified name", //
            description = "Answers the newest compiled unit of that name in the stage view, or exactly the"
                    + " pinned fingerprint when the 'fingerprint' query parameter is given. A pinned"
                    + " fingerprint the stage does not hold is answered 404 naming the versions it has —"
                    + " never a silent substitute.", responses = {
                            @ApiResponse(responseCode = "200", description = "The CompiledUnit document"),
                            @ApiResponse(responseCode = "404", description = "Unknown name, or pinned fingerprint not present (body names the existing versions)"),
                            @ApiResponse(responseCode = "400", description = "Unknown registry or stage"),
                            @ApiResponse(responseCode = "500", description = "The registry's storage failed") })
    public Response getUnit(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The registry name", required = true) @PathParam("registryName") String registryName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "The unit's qualified name", required = true) @PathParam("qualifiedName") String qualifiedName,
            @Parameter(description = "Pin exactly this unit fingerprint (m2x1:…)") @QueryParam("fingerprint") String fingerprint) {
        return withStore(scopeName, registryName, stageName, store -> {
            // resolve through versions first: a storage failure there is a 500, and
            // a pinned miss can then be answered naming what actually exists
            List<UnitKey> versions = store.versions(QvtUnits.LANGUAGE_QVTO, qualifiedName, UnitKind.COMPILED);
            UnitKey key;
            if (fingerprint == null || fingerprint.isBlank()) {
                if (versions.isEmpty()) {
                    return notFound(scopeName, registryName, stageName, qualifiedName, null);
                }
                key = versions.get(0);
            } else {
                key = versions.stream().filter(v -> fingerprint.equals(v.fingerprint().orElse(null))).findFirst()
                        .orElse(null);
                if (key == null) {
                    return notFound(scopeName, registryName, stageName, qualifiedName,
                            versions.stream().map(v -> v.fingerprint().orElse("?"))
                                    .collect(Collectors.joining(", ")));
                }
            }
            Optional<Unit> unit = store.get(key);
            if (unit.isEmpty() || !(unit.get() instanceof PackagedUnit packaged)) {
                return notFound(scopeName, registryName, stageName, qualifiedName, null);
            }
            return Response.ok(packaged.document()).build();
        });
    }

    @GET
    @Path("/{qualifiedName}/versions")
    @Consumes
    @Produces("text/plain")
    @Operation(summary = "List the stored unit fingerprints of a qualified name, newest first", responses = {
            @ApiResponse(responseCode = "200", description = "One fingerprint per line, newest first (empty for an unknown name)"),
            @ApiResponse(responseCode = "400", description = "Unknown registry or stage"),
            @ApiResponse(responseCode = "500", description = "The registry's storage failed") })
    public Response getVersions(@PathParam("scopeName") String scopeName,
            @PathParam("registryName") String registryName, @PathParam("stageName") String stageName,
            @PathParam("qualifiedName") String qualifiedName) {
        return withStore(scopeName, registryName, stageName,
                store -> Response.ok(store.versions(QvtUnits.LANGUAGE_QVTO, qualifiedName, UnitKind.COMPILED)
                        .stream().map(v -> v.fingerprint().orElse("?")).collect(Collectors.joining("\n"))).build());
    }

    @GET
    @Path("/{qualifiedName}/diagnostics")
    @Consumes
    @Produces
    @Operation(summary = "The compile outcome of a source: status and positioned findings", responses = {
            @ApiResponse(responseCode = "200", description = "The SourceDiagnostics document"),
            @ApiResponse(responseCode = "404", description = "No diagnostics for that name (never uploaded, or not compiled yet)"),
            @ApiResponse(responseCode = "400", description = "Unknown registry or stage"),
            @ApiResponse(responseCode = "500", description = "The registry's storage failed") })
    public Response getDiagnostics(@PathParam("scopeName") String scopeName,
            @PathParam("registryName") String registryName, @PathParam("stageName") String stageName,
            @PathParam("qualifiedName") String qualifiedName) {
        RegistryService<EObject> registryService = registryFor(registryName);
        if (registryService == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Unknown or unconfigured registry: " + registryName).build();
        }
        if (!registryService.isValidStage(stageName)) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Unknown stage " + stageName + " for registry " + registryName).build();
        }
        EObject diagnostics;
        try {
            diagnostics = registryService.getContentFromStage(scopeName, stageName,
                    QvtUnits.diagnosticsObjectId(QvtUnits.LANGUAGE_QVTO, qualifiedName));
        } catch (RuntimeException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
        if (diagnostics == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("No diagnostics for '" + qualifiedName + "' in (" + scopeName + ", " + registryName
                            + ", " + stageName + ")")
                    .build();
        }
        return Response.ok(diagnostics).build();
    }

    private interface StoreCall {
        Response call(AtlasUnitStore store) throws UnitStoreException;
    }

    private Response withStore(String scopeName, String registryName, String stageName, StoreCall call) {
        RegistryService<EObject> registryService = registryFor(registryName);
        if (registryService == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Unknown or unconfigured registry: " + registryName).build();
        }
        if (!registryService.isValidStage(stageName)) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Unknown stage " + stageName + " for registry " + registryName).build();
        }
        try {
            return call.call(new AtlasUnitStore(registryService, scopeName, stageName));
        } catch (UnitStoreException e) {
            // a broken storage must not masquerade as "not found"
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    private static Response notFound(String scopeName, String registryName, String stageName, String qualifiedName,
            String existingVersions) {
        String message = "No compiled unit '" + qualifiedName + "' in (" + scopeName + ", " + registryName + ", "
                + stageName + ")";
        if (existingVersions != null && !existingVersions.isEmpty()) {
            message += " with that fingerprint; it has: " + existingVersions;
        }
        return Response.status(Status.NOT_FOUND).entity(message).build();
    }

    @SuppressWarnings("unchecked")
    private RegistryService<EObject> registryFor(String registryName) {
        return (RegistryService<EObject>) registryCollector.getRegistryServiceByRegistryName(registryName);
    }
}
