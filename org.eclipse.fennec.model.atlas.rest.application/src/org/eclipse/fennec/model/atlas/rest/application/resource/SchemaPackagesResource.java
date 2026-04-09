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

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageLuceneIndex;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageLuceneIndex.SearchHit;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageLuceneIndex.SearchResult;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageSearchQuery;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadataContainer;
import org.eclipse.fennec.model.atlas.rest.application.filter.ModelAtlasRequestFilter;
import org.eclipse.fennec.model.atlas.rest.model.StageTransitionRequest;
import org.eclipse.fennec.model.atlas.runtime.RequireRuntime;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.workflow.ScopeServiceCollector;
import org.osgi.framework.Version;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
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
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * REST API for managing SchemaPackages within scopes. Provides endpoints for
 * CRUD operations on schema packages with stage-based lifecycle management.
 *
 * @author Data In Motion
 * @since 1.0
 */
@RequireRuntime
@JakartarsResource()
@JakartarsName("SchemaPackagesResource")
@Component(name = "SchemaPackagesResource", service = SchemaPackagesResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/{scopeName}/schema")
@Tag(name = "Schema Management", description = "CRUD operations for schema packages")
public class SchemaPackagesResource {

    @Reference
    private ScopeServiceCollector scopeCollector;

    @Reference
    private ManagementFactory mgmtFactory;
    
    @Reference
    EPackageLuceneIndex ePackageIndex;

    @Context
    private ContainerRequestContext requestContext;

    private static final String REGISTRY_NAME = "schema";

    @GET
    @Path("hello")
    @Produces({ MediaType.TEXT_PLAIN })
    public Response hello(@PathParam("scopeName") String scopeName) {
        return Response.ok().entity("Hello " + scopeName).build();
    }

    // ======================
    // All Schemas for Scope 
    // ======================

    /**
     * List all packages in all the stages for this scope. Respects
     * hierarchical visibility, including packages from parent scopes' released
     * stages.
     *
     * @param scopeName the scope name
     * @return List of SchemaPackage metadata objects
     */
    @GET
    @Path("/all")
    @Produces
    @Operation(summary = "List all packages in scope", description = "List all packages in all the stages for this scope, including packages from parent scopes", responses = {
            @ApiResponse(responseCode = "200", description = "Packages retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
            @ApiResponse(responseCode = "204", description = "No Package found in scope final stage, nor in the parent final stage"),
            @ApiResponse(responseCode = "400", description = "Scope not available, schema registry not available for scope, stage not available for registry or not a valid stage"),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response listAllPackages(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            List<ObjectMetadata> objectsMetadata = scopeService.listAllForRegistry(REGISTRY_NAME);
            if (objectsMetadata.isEmpty())
                return Response.status(Response.Status.NO_CONTENT).build();
            ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
            container.getMetadata().addAll(objectsMetadata);
            return Response.status(Response.Status.OK).entity(container).header("Content-Type", getResolvedMediaType()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    // ======================
    // Released Stage APIs (default)
    // ======================

    /**
     * List all packages in the final/released stage for this scope. Respects
     * hierarchical visibility, including packages from parent scopes' released
     * stages.
     *
     * @param scopeName the scope name
     * @return List of SchemaPackage metadata objects
     */
    @GET
    @Produces
    @Operation(summary = "List released packages in scope", description = "List all packages in the final stage for this scope, including packages from parent scopes", responses = {
            @ApiResponse(responseCode = "200", description = "Packages retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
            @ApiResponse(responseCode = "204", description = "No Package found in scope final stage, nor in the parent final stage"),
            @ApiResponse(responseCode = "400", description = "Scope not available, schema registry not available for scope, stage not available for registry or not a valid stage"),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response listReleasedPackages(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            List<ObjectMetadata> objectsMetadata = scopeService.listInFinalStageForRegistry(REGISTRY_NAME);
            if (objectsMetadata.isEmpty())
                return Response.status(Response.Status.NO_CONTENT).build();
            ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
            container.getMetadata().addAll(objectsMetadata);
            return Response.status(Response.Status.OK).entity(container).header("Content-Type", getResolvedMediaType()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    // ======================
    // Stage-Specific APIs
    // ======================

    /**
     * List all packages within a specific stage of a scope. Supports filtering by
     * nsUri or name.
     *
     * @param scopeName the scope name
     * @param stageName the stage name
     * @param nsUri     optional exact namespace URI filter
     * @param name      optional name filter (supports wildcards)
     * @return List of SchemaPackage metadata objects, or single object if nsUri is
     *         specified
     */
    @GET
    @Path("/stages/{stageName}")
    @Produces
    @Operation(summary = "List packages in a specific stage", description = "List all packages within a specific stage, with optional filtering by nsUri or name. "
            + "Respects hierarchical visibility when nsUri is specified. "
            + "A name filter with wildcard can be provided, but be aware that no wildcard as first character is supported nor case-sensitive searches.", responses = {
                    @ApiResponse(responseCode = "200", description = "Packages retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
                    @ApiResponse(responseCode = "204", description = "Package not found"),
                    @ApiResponse(responseCode = "400", description = "Scope not available, schema registry not available for scope, stage not available for registry or not a valid stage"),
                    @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response listPackagesInStage(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "Exact namespace URI of the package to retrieve") @QueryParam("nsUri") String nsUri,
            @Parameter(description = "Package name filter (supports wildcards, e.g., *Billing*)") @QueryParam("name") String name) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            if (nsUri != null) {
                String encodedUri = encodePackageNsURI(nsUri);
                ObjectMetadata metadata = scopeService.getMetadataFromStageForRegistry(REGISTRY_NAME, stageName,
                        encodedUri);
                if (metadata == null) {
                    return Response.status(Response.Status.NO_CONTENT).build();
                } else {
                    return Response.status(Response.Status.OK).entity(metadata).build();
                }
            } else if (name != null) {
                List<ObjectMetadata> objectsMetadata = scopeService.listInStageForRegistryByName(REGISTRY_NAME,
                        stageName, name);
                if (objectsMetadata.isEmpty()) {
                    return Response.status(Response.Status.NO_CONTENT).build();
                }
                ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
                container.getMetadata().addAll(objectsMetadata);
                return Response.status(Response.Status.OK).entity(container).header("Content-Type", getResolvedMediaType()).build();
            } else {
                List<ObjectMetadata> objectsMetadata = scopeService.listInStageForRegistry(REGISTRY_NAME, stageName);
                if (objectsMetadata.isEmpty()) {
                    return Response.status(Response.Status.NO_CONTENT).build();
                }
                ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
                container.getMetadata().addAll(objectsMetadata);
                return Response.status(Response.Status.OK).entity(container).header("Content-Type", getResolvedMediaType()).build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

    }

    /**
     * Create a new SchemaPackage in the specified stage.
     *
     * @param scopeName the scope name
     * @param stageName the stage name
     * @param nsUri     the namespace URI (required)
     * @param name      optional human-readable name
     * @param version   optional version string
     * @param ePackage  the schema package content
     * @return SchemaPackage metadata
     */
    @SuppressWarnings("unchecked")
    @POST
    @PUT
    @Path("/stages/{stageName}")
    @Consumes
    @Produces
    @Operation(summary = "Create a new schema package", description = "Create a new SchemaPackage in the specified stage. Checks for uniqueness based on nsUri. "
            + "If nsUri is not provided, the URI from the EPackage will be used. If provided, it must match the EPackage's nsURI. "
            + "If version is not provided, it will be extracted from the nsURI. If provided, it must be semantically compatible with the URI version.", responses = {
                    @ApiResponse(responseCode = "201", description = "Package created successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
                    @ApiResponse(responseCode = "400", description = "Invalid package data, missing required parameters, nsUri mismatch, or version incompatibility"),
                    @ApiResponse(responseCode = "400", description = "Scope not available, schema registry not available for scope, stage not available for registry or not a valid stage"),
                    @ApiResponse(responseCode = "409", description = "Package with nsUri already exists"),
                    @ApiResponse(responseCode = "415", description = "Unsupported media type"),
                    @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response createPackage(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "The namespace URI of the package. If not provided, uses the EPackage's nsURI. If provided, must match the EPackage's nsURI.", required = false) @QueryParam("nsUri") String nsUri,
            @Parameter(description = "Human-readable name for the package") @QueryParam("name") String name,
            @Parameter(description = "Package version. If not provided, will be extracted from the nsURI. If provided, must be semantically compatible with the URI version.", required = false) @QueryParam("version") String version,
            @Parameter(description = "Overwrite option. If true and a Package with the same uri already exists, it updates it. ", required = false) @QueryParam("overwrite") boolean overwrite,
            @RequestBody(description = "The schema package content", required = true, content = @Content(schema = @Schema(implementation = EPackage.class))) EPackage ePackage) {

        ScopeService<EObject> scopeService = (ScopeService<EObject>) getScopeServiceByScopeName(scopeName);

        try {
            String validatedNsUri = validateAndResolveNsUri(nsUri, ePackage);
            String resolvedVersion = resolveAndValidateVersion(version, validatedNsUri);
            String encodedNsURI = encodePackageNsURI(validatedNsUri);
            // Check uniqueness across visibility chain
            ObjectMetadata existingMetadata = scopeService.getMetadataFromStageForRegistry(REGISTRY_NAME, stageName,
                    encodedNsURI);
            if (existingMetadata != null) {
                if (!overwrite) {
                    return Response.status(Response.Status.CONFLICT)
                            .entity(String.format(
                                    "Schema %s already exists and overwrite flag is false. Cannot update object.",
                                    nsUri))
                            .build();
                } else {
                    if (existingMetadata.isIsReadOnly()) {
                        return Response.status(Response.Status.FORBIDDEN)
                                .entity(String.format("Schema %s is read-only. Cannot update it.", nsUri)).build();
                    }
                    ObjectMetadata metadata = scopeService
                            .updateInStageForRegistry(REGISTRY_NAME, stageName, ePackage, encodedNsURI, resolvedVersion)
                            .getValue();
                    ePackageIndex.index(metadata, ePackage);
                    return Response.status(Response.Status.OK)
                            .header("Location",
                                    "/".concat(scopeName).concat("/schemas/stages/").concat(stageName).concat("?nsUri=")
                                            .concat(encodedNsURI))
                            .entity(metadata).header("Content-Type", getResolvedMediaType()).build();
                }
            }
            // Create package and return metadata with Location header
            ObjectMetadata metadata = mgmtFactory.createObjectMetadata();
            metadata.setObjectId(encodedNsURI);
            metadata.setObjectName(name == null ? ePackage.getName() : name);
            metadata.setUploadTime(Instant.now());
            metadata.setStage(stageName);
            metadata.setScope(scopeName);
            metadata.setRegistry(REGISTRY_NAME);
            metadata.setVersion(resolvedVersion);
            metadata.setObjectType(EcoreUtil.getURI(ePackage.eClass()).toString());
            metadata.getProperties().put("nsUri", validatedNsUri);

            metadata = scopeService.uploadToStageForRegistry(REGISTRY_NAME, stageName, ePackage, metadata).getValue();
            ePackageIndex.index(metadata, ePackage);

            return Response.status(Response.Status.OK)
                    .header("Location", "/".concat(scopeName).concat("/schemas/stages/").concat(stageName)
                            .concat("?nsUri=").concat(encodedNsURI))
                    .entity(metadata).header("Content-Type", getResolvedMediaType()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (WebApplicationException e) {
            // WebApplicationException already has the correct status code, rethrow it
            throw e;
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * Get the content of a SchemaPackage in a specific format.
     *
     * @param scopeName the scope name
     * @param stageName the stage name
     * @param nsUri     the namespace URI (required)
     * @return Schema package content in requested format
     */
    @GET
    @Path("/stages/{stageName}/content")
    @Produces
    @Operation(summary = "Get package content", description = "Retrieve the content of a SchemaPackage in the requested format. "
            + "Respects hierarchical visibility.", responses = {
                    @ApiResponse(responseCode = "200", description = "Package content retrieved successfully"),
                    @ApiResponse(responseCode = "204", description = "Package not found"),
                    @ApiResponse(responseCode = "400", description = "Scope not available, schema registry not available for scope, stage not available for registry or not a valid stage"),
                    @ApiResponse(responseCode = "406", description = "Requested format not supported"),
                    @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response getPackageContent(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "The namespace URI of the package", required = true) @QueryParam("nsUri") String nsUri) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            nsUri = URI.decode(nsUri);
            String encodedNsUri = encodePackageNsURI(nsUri);
            EPackage ePackage = (EPackage) scopeService.getContentFromStageForRegistry(REGISTRY_NAME, stageName,
                    encodedNsUri);
            if (ePackage == null) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            return Response.status(Response.Status.OK).entity(ePackage).header("Content-Type", getResolvedMediaType()).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * Update (replace) the content of an existing SchemaPackage.
     *
     * @param scopeName the scope name
     * @param stageName the stage name
     * @param nsUri     the namespace URI (required)
     * @param ePackage  the new schema package content
     * @return Updated SchemaPackage metadata
     */
    @SuppressWarnings("unchecked")
    @PUT
    @POST
    @Path("/stages/{stageName}/content")
    @Consumes
    @Produces
    @Operation(summary = "Update package content", description = "Replace the content of an existing SchemaPackage. "
            + "Fails if the stage is read-only (e.g., Released). "
            + "If nsUri is not provided, the URI from the EPackage will be used. If provided, it must match the EPackage's nsURI. "
            + "If version is not provided, it will be extracted from the nsURI. If provided, it must be semantically compatible with the URI version.", responses = {
                    @ApiResponse(responseCode = "200", description = "Package updated successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
                    @ApiResponse(responseCode = "400", description = "Invalid package data, nsUri mismatch, or version incompatibility, scope not available, schema registry not available for scope, stage not available for registry or not a valid stage"),
                    @ApiResponse(responseCode = "403", description = "Stage is read-only or Package is only present in a parent scope final stage and so it's read-only"),
                    @ApiResponse(responseCode = "404", description = "Scope or stage not found"),
                    @ApiResponse(responseCode = "204", description = "Package not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response updatePackageContent(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "The updated version. If not provided, will be extracted from the nsURI. If provided, must be semantically compatible with the URI version.", required = false) @QueryParam("version") String version,
            @Parameter(description = "The namespace URI of the package. If not provided, uses the EPackage's nsURI. If provided, must match the EPackage's nsURI.", required = false) @QueryParam("nsUri") String nsUri,
            @RequestBody(description = "The new schema package content", required = true, content = @Content(schema = @Schema(implementation = EPackage.class))) EPackage ePackage) {

        ScopeService<EObject> scopeService = (ScopeService<EObject>) getScopeServiceByScopeName(scopeName);
        try {
            String validatedNsUri = validateAndResolveNsUri(nsUri, ePackage);
            String resolvedVersion = resolveAndValidateVersion(version, validatedNsUri);
            String encodedNsUri = encodePackageNsURI(validatedNsUri);
            ObjectMetadata existingMetadata = scopeService.getMetadataFromStageForRegistry(REGISTRY_NAME, stageName,
                    encodedNsUri);
            if (existingMetadata == null) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            // We might want to check if the metadata is read only (e.g. if it was retrieved
            // from a parent final stage
            if (existingMetadata.isIsReadOnly()) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(String.format("Schema %s is in read-only state", nsUri)).build();
            }

            ObjectMetadata metadata = scopeService
                    .updateInStageForRegistry(REGISTRY_NAME, stageName, ePackage, encodedNsUri, resolvedVersion)
                    .getValue();
            ePackageIndex.index(metadata, ePackage);
            return Response.status(Response.Status.OK).entity(metadata).header("Content-Type", getResolvedMediaType()).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (WebApplicationException e) {
            // WebApplicationException already has the correct status code, rethrow it
            throw e;
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/stages/{stageName}")
    @Operation(summary = "Delete a package", description = "Delete a SchemaPackage from the specified stage. "
            + "Fails if the stage is read-only.", responses = {
                    @ApiResponse(responseCode = "200", description = "Package deleted successfully"),
                    @ApiResponse(responseCode = "403", description = "Stage is read-only or Package is only present in a parent scope final stage and so it's read-only"),
                    @ApiResponse(responseCode = "400", description = "Scope not available, schema registry not available for scope, stage not available for registry or not a valid stage"),
                    @ApiResponse(responseCode = "204", description = "Package not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response deletePackage(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "The namespace URI of the package to delete", required = true) @QueryParam("nsUri") String nsUri) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            String encodedNsUri = encodePackageNsURI(nsUri);
            ObjectMetadata existingMetadata = scopeService.getMetadataFromStageForRegistry(REGISTRY_NAME, stageName,
                    encodedNsUri);
            if (existingMetadata == null) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            if (existingMetadata.isIsReadOnly()) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(String.format("Schema %s is in read-only state", nsUri)).build();
            }
            boolean deleted = scopeService.deleteFromStageForRegistry(REGISTRY_NAME, stageName, encodedNsUri)
                    .getValue();
            if (deleted) {
            	ePackageIndex.remove(encodedNsUri);
            	return Response.status(Response.Status.OK).build();
            }
               
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("Schema %s deletion failed but causes are unknown", nsUri)).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    // ======================
    // Lifecycle Actions
    // ======================

    @POST
    @Path("/stages/{stageName}/actions/transition")
    @Consumes
    @Produces
    @Operation(summary = "Transition package between stages", description = "Move a package from the current stage to a target stage. "
            + "Validates that the transition is allowed based on stage rules.", responses = {
                    @ApiResponse(responseCode = "200", description = "Package transitioned successfully", content = @Content(schema = @Schema(implementation = ObjectMetadata.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid transition, missing parameters, scope not available, schema registry not available for scope, stage not available for registry or not a valid stage"),
                    @ApiResponse(responseCode = "403", description = "Stage is read-only or Object is only present in a parent scope final stage and so it's read-only"),
                    @ApiResponse(responseCode = "204", description = "Package not found in source stage"),
                    @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response transitionPackage(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The source stage name", required = true) @PathParam("stageName") String stageName,
            @RequestBody(description = "Transition request with objectId and targetStage", required = true, content = @Content()) StageTransitionRequest transitionRequest) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            String encodedNsUri = encodePackageNsURI(transitionRequest.getObjectId());
            ObjectMetadata existingMetadata = scopeService.getMetadataFromStageForRegistry(REGISTRY_NAME, stageName,
                    encodedNsUri);
            if (existingMetadata == null) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            if (existingMetadata.isIsReadOnly()) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(String.format("Schema %s is in read-only state", transitionRequest.getObjectId()))
                        .build();
            }
            ObjectMetadata metadata = scopeService.transitionToStageForRegistry(REGISTRY_NAME, encodedNsUri, stageName,
                    transitionRequest.getTargetStage());
            return Response.status(Response.Status.OK).entity(metadata).header("Content-Type", getResolvedMediaType()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

    }
    
    @GET
    @Path("/search")
    @Produces
    @Operation(summary = "Search packages across scope chain",
        description = "Search for schema packages within the given scope and its "
            + "parent scopes. Supports filtering by nsUri, name, prefix, "
            + "classifier names, structural feature names and types. "
            + "Returns ObjectMetadata with pagination.")
    public Response searchPackages(
        @PathParam("scopeName") String scopeName,
        @QueryParam("nsUri") String nsUri,
        @QueryParam("nsUriExact") String nsUriExact,
        @QueryParam("name") String name,
        @QueryParam("prefix") String prefix,
        @QueryParam("classifier") String classifier,
        @QueryParam("featureName") String featureName,
        @QueryParam("featureType") String featureType,
        @QueryParam("featureNameTypePair") String featureNameTypePair,
        @QueryParam("stage") String stage,
        @QueryParam("limit") @DefaultValue("50") int limit,
        @QueryParam("offset") @DefaultValue("0") int offset) {

        // 1. Resolve scope chain
        Set<String> scopeChain = resolveScopeChain(scopeName);

        // 2. Build search query
        EPackageSearchQuery query = EPackageSearchQuery.create()
            .scopes(scopeChain)
            .stage(stage)
            .nsUri(nsUri)
            .nsUriExact(nsUriExact)
            .name(name)
            .nsPrefix(prefix)
            .classifier(classifier)
            .featureName(featureName)
            .featureType(featureType)
            .featureNameTypePair(featureNameTypePair)
            .limit(limit)
            .offset(offset)
            .build();

        // 3. Search EPackage index
        SearchResult result = ePackageIndex.search(query);

        if (result.hits().isEmpty()) {
            return Response.noContent().build();
        }

        // 4. Retrieve ObjectMetadata for each hit using scope/stage from the index
        List<ObjectMetadata> metadataList = resolveMetadata(result.hits(), scopeName);

        // 5. Mark results from parent scopes as read-only
        for (ObjectMetadata metadata : metadataList) {
            if (!scopeName.equals(metadata.getScope())) {
                metadata.setIsReadOnly(true);
            }
        }

        // 6. Build response
        ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
        container.getMetadata().addAll(metadataList);
        return Response.ok(container)
            .header("X-Total-Count", result.totalHits())
            .header("X-Offset", offset)
            .header("X-Limit", limit)
            .build();
    }
    

	/**
	 * @param hits
	 * @return
	 */
	private List<ObjectMetadata> resolveMetadata(List<SearchHit> hits, String scopeName) {
		List<ObjectMetadata> allMetadata = new LinkedList<>();
		hits.forEach(hit -> {
			
			ScopeService<?> scopeService = getScopeServiceByScopeName(hit.scope());
			ObjectMetadata metadata = scopeService.getMetadataFromStageForRegistry(hit.registry(), hit.stage(), hit.objectId());
			
			if(metadata != null) {
				if (!scopeName.equals(metadata.getScope())) {
	                metadata.setIsReadOnly(true);
	            }
				allMetadata.add(metadata);
			}
		});
		return allMetadata;
	}

	private Set<String> resolveScopeChain(String scopeName) {
        Set<String> chain = new LinkedHashSet<>();
        String current = scopeName;
        while (current != null) {
            chain.add(current);
            Scope scope = scopeCollector.getScopeByName(current);
            current = (scope != null) ? scope.getParentScope() : null;
        }
        return chain;
    }
    

    private String getResolvedMediaType() {
        return (String) requestContext.getProperty(ModelAtlasRequestFilter.RESOLVED_MEDIA_TYPE);
    }

    private ScopeService<?> getScopeServiceByScopeName(String scopeName) {
        ScopeService<?> scopeService = scopeCollector.getScopeServiceByScopeName(scopeName);
        if (scopeService == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Scope [" + scopeName + "] not found.").build());
        }
        return scopeService;
    }

    private String encodePackageNsURI(String nsUri) throws UnsupportedEncodingException {
        return new String(Base64.getUrlEncoder().encode(nsUri.getBytes()));
    }

    /**
     * Validates and resolves the namespace URI for a package. If nsUri parameter is
     * not provided, uses the URI from the ePackage. If nsUri is provided, validates
     * that it matches the ePackage's URI.
     *
     * @param nsUri    the namespace URI parameter (may be null)
     * @param ePackage the EPackage containing the URI
     * @return the validated namespace URI
     * @throws WebApplicationException if validation fails
     */
    private String validateAndResolveNsUri(String nsUri, EPackage ePackage) {
        String packageNsUri = ePackage.getNsURI();
        if (packageNsUri == null || packageNsUri.isBlank()) {
            throw new WebApplicationException(
                    Response.status(Status.BAD_REQUEST).entity("EPackage must have a non-empty nsURI set").build());
        }

        if (nsUri == null || nsUri.isBlank()) {
            // No parameter provided, use the ePackage's URI
            return packageNsUri;
        }
        nsUri = URI.decode(nsUri);

        // Parameter provided, validate it matches
        if (!nsUri.equals(packageNsUri)) {
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(
                    String.format("Query parameter nsUri '%s' does not match EPackage nsURI '%s'", nsUri, packageNsUri))
                    .build());
        }

        return nsUri;
    }

    /**
     * Extracts an OSGi version from a URI by parsing each segment. Returns the last
     * valid version found in the URI segments.
     *
     * @param nsUri the namespace URI to parse
     * @return the extracted Version, or null if no valid version found
     */
    private Version extractVersionFromUri(String nsUri) {
        if (nsUri == null || nsUri.isBlank()) {
            return null;
        }

        try {
            URI uri = URI.createURI(nsUri);
            Version lastValidVersion = null;

            // Parse each segment of the URI
            for (String segment : uri.segments()) {
                try {
                    Version version = Version.parseVersion(segment);
                    // Keep track of the last valid version found
                    lastValidVersion = version;
                } catch (IllegalArgumentException e) {
                    // This segment is not a valid version, continue
                }
            }

            // Also try parsing the authority and path components
            if (uri.hasAuthority()) {
                String authority = uri.authority();
                if (authority != null) {
                    String[] parts = authority.split("[/.]");
                    for (String part : parts) {
                        try {
                            Version version = Version.parseVersion(part);
                            lastValidVersion = version;
                        } catch (IllegalArgumentException e) {
                            // Not a version, continue
                        }
                    }
                }
            }

            return lastValidVersion;
        } catch (Exception e) {
            // If URI parsing fails, return null
            return null;
        }
    }

    /**
     * Validates that two versions are compatible according to semantic versioning
     * rules. Compatible means they have the same major version, and the URI version
     * is not lower than the parameter version.
     *
     * @param paramVersion the version from the parameter
     * @param uriVersion   the version extracted from the URI
     * @return true if compatible, false otherwise
     */
    private boolean areVersionsCompatible(Version paramVersion, Version uriVersion) {
        if (paramVersion == null || uriVersion == null) {
            return false;
        }

        // Major versions must match for semantic compatibility
        if (paramVersion.getMajor() != uriVersion.getMajor()) {
            return false;
        }

        // Compare minor and micro versions
        int minorCompare = Integer.compare(uriVersion.getMinor(), paramVersion.getMinor());
        if (minorCompare < 0) {
            return false; // URI version has lower minor
        }
        if (minorCompare > 0) {
            return true; // URI version has higher minor, compatible
        }

        // Minor versions are equal, check micro
        int microCompare = Integer.compare(uriVersion.getMicro(), paramVersion.getMicro());
        return microCompare >= 0; // URI version micro must be >= param version micro
    }

    /**
     * Resolves and validates the version parameter. If no version parameter is
     * given, extracts version from the URI. If both are present, validates
     * compatibility.
     *
     * @param versionParam the version parameter (may be null)
     * @param nsUri        the namespace URI
     * @return the resolved version string, or null if no version found
     * @throws WebApplicationException if version validation fails
     */
    private String resolveAndValidateVersion(String versionParam, String nsUri) {
        Version uriVersion = extractVersionFromUri(nsUri);

        if (versionParam == null || versionParam.isBlank()) {
            // No parameter provided, use the version from URI if available
            return uriVersion != null ? uriVersion.toString() : null;
        }

        // Parameter provided, validate it
        Version paramVersion;
        try {
            paramVersion = Version.parseVersion(versionParam);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
                    .entity(String.format("Invalid version parameter: '%s'", versionParam)).build());
        }

        // If URI has a version, check compatibility
        if (uriVersion != null) {
            if (!areVersionsCompatible(paramVersion, uriVersion)) {
                throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(String.format(
                        "Version parameter '%s' is not compatible with URI version '%s' (semantic versioning rules)",
                        versionParam, uriVersion.toString())).build());
            }
        }

        return versionParam;
    }
}
