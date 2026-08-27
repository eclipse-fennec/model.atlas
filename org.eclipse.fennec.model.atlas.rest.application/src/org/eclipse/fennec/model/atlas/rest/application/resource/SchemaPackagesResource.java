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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractEObjectStorageService;
import org.eclipse.fennec.model.atlas.rest.model.StageTransitionRequest;
import org.eclipse.fennec.model.atlas.runtime.RequireRuntime;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.rest.application.exception.EndpointFailures;
import org.eclipse.fennec.model.atlas.rest.application.filter.ObjectMetadataResponseFilter;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.workflow.ScopeServiceCollector;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
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
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
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
import org.eclipse.fennec.codec.constants.CodecOptions;
import org.eclipse.fennec.codec.rest.annotations.ResourceOption;

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

    @Context
    private HttpHeaders headers;
    
    private static final Logger LOGGER = Logger.getLogger(SchemaPackagesResource.class.getName());

    private static final String REGISTRY_NAME = "schema";

    @GET
    @Path("hello")
    @Produces({ MediaType.TEXT_PLAIN })
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
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
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
    public Response listAllPackages(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            List<ObjectMetadata> objectsMetadata = scopeService.listAllForRegistry(REGISTRY_NAME);
            if (objectsMetadata.isEmpty())
                return Response.status(Response.Status.NO_CONTENT).build();
            ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
            container.getMetadata().addAll(objectsMetadata);
            return Response.status(Response.Status.OK).entity(container).header("Content-Type", ResourceSupport.resolvedMediaType(requestContext)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            throw EndpointFailures.propagate(e);
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
    @Operation(summary = "List released packages in scope", description = "List all packages in the final stage for this scope, including packages from parent scopes. "
            + "With an exact nsUri the single final-stage ObjectMetadata is returned (hierarchy-aware), so the stage-free path can resolve a package's origin without a stage name (P5-7).", responses = {
            @ApiResponse(responseCode = "200", description = "Packages retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
            @ApiResponse(responseCode = "204", description = "No Package found in scope final stage, nor in the parent final stage"),
            @ApiResponse(responseCode = "400", description = "Scope not available, schema registry not available for scope, stage not available for registry or not a valid stage"),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
    public Response listReleasedPackages(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "Exact namespace URI of the package to retrieve from the final stage") @QueryParam("nsUri") String nsUri) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            if (nsUri != null) {
                // Stage-free final-stage metadata for one package (hierarchy-aware), mirroring the
                // stage-explicit listing's nsUri branch — used by the client's resolve() (P5-7).
                ObjectMetadata metadata = findByNsUriInFinalStage(scopeService, nsUri);
                if (metadata == null) {
                    return Response.status(Response.Status.NO_CONTENT).build();
                }
                ObjectMetadataResponseFilter.attach(requestContext, metadata,
                        ObjectMetadataResponseFilter.CacheTarget.METADATA);
                return Response.status(Response.Status.OK).entity(metadata)
                        .header("Content-Type", ResourceSupport.resolvedMediaType(requestContext)).build();
            }
            List<ObjectMetadata> objectsMetadata = scopeService.listInFinalStageForRegistry(REGISTRY_NAME);
            if (objectsMetadata.isEmpty())
                return Response.status(Response.Status.NO_CONTENT).build();
            ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
            container.getMetadata().addAll(objectsMetadata);
            return Response.status(Response.Status.OK).entity(container).header("Content-Type", ResourceSupport.resolvedMediaType(requestContext)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            throw EndpointFailures.propagate(e);
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
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
    public Response listPackagesInStage(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "Exact namespace URI of the package to retrieve") @QueryParam("nsUri") String nsUri,
            @Parameter(description = "Package name filter (supports wildcards, e.g., *Billing*)") @QueryParam("name") String name) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            if (nsUri != null) {
                ObjectMetadata metadata = findByNsUriInStage(scopeService, stageName, nsUri);
                if (metadata == null) {
                    return Response.status(Response.Status.NO_CONTENT).build();
                } else {
                    Response.ResponseBuilder rb = Response.status(Response.Status.OK).entity(metadata)
                            .header("Content-Type", ResourceSupport.resolvedMediaType(requestContext));
                    ObjectMetadataResponseFilter.attach(requestContext, metadata,
                            ObjectMetadataResponseFilter.CacheTarget.METADATA);
                    return rb.build();
                }
            } else if (name != null) {
                List<ObjectMetadata> objectsMetadata = scopeService.listInStageForRegistryByName(REGISTRY_NAME,
                        stageName, name);
                if (objectsMetadata.isEmpty()) {
                    return Response.status(Response.Status.NO_CONTENT).build();
                }
                ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
                container.getMetadata().addAll(objectsMetadata);
                return Response.status(Response.Status.OK).entity(container).header("Content-Type", ResourceSupport.resolvedMediaType(requestContext)).build();
            } else {
                List<ObjectMetadata> objectsMetadata = scopeService.listInStageForRegistry(REGISTRY_NAME, stageName);
                if (objectsMetadata.isEmpty()) {
                    return Response.status(Response.Status.NO_CONTENT).build();
                }
                ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
                container.getMetadata().addAll(objectsMetadata);
                return Response.status(Response.Status.OK).entity(container).header("Content-Type", ResourceSupport.resolvedMediaType(requestContext)).build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            throw EndpointFailures.propagate(e);
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
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
    public Response createPackage(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "The namespace URI of the package. If not provided, uses the EPackage's nsURI. If provided, must match the EPackage's nsURI.", required = false) @QueryParam("nsUri") String nsUri,
            @Parameter(description = "Human-readable name for the package") @QueryParam("name") String name,
            @Parameter(description = "Package version. If not provided, will be extracted from the nsURI. If provided, must be semantically compatible with the URI version.", required = false) @QueryParam("version") String version,
            @Parameter(description = "Overwrite option. If true and a Package with the same uri already exists, it updates it. ", required = false) @QueryParam("overwrite") boolean overwrite,
            @Parameter(description = "Assert that this package may be published to a DCAT portal. Recorded in the metadata as the 'dcat' property. "
                    + "On create, absent means false. On an overwrite, absent leaves the stored flag untouched — only an explicit value changes it.", required = false) @QueryParam("dcat") Boolean dcat,
            @RequestBody(description = "The schema package content", required = true, content = @Content(schema = @Schema(implementation = EPackage.class))) EPackage ePackage) {

        ScopeService<EObject> scopeService = (ScopeService<EObject>) getScopeServiceByScopeName(scopeName);

        try {
            String validatedNsUri = validateAndResolveNsUri(nsUri, ePackage);
            String resolvedVersion = NsUriVersions.resolveAndValidate(version, validatedNsUri);
            // Check uniqueness across visibility chain
            ObjectMetadata existingMetadata = findByNsUriInStage(scopeService, stageName, validatedNsUri);
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
                    // If-Match validation (optimistic locking via the content ETag — overwrite replaces
                    // the content of an existing package).
                    Response preconditionResponse = ResourceSupport.checkIfMatch(headers, existingMetadata,
                            ObjectMetadataResponseFilter.CacheTarget.CONTENT);
                    if (preconditionResponse != null) {
                        return preconditionResponse;
                    }
                    ObjectMetadata metadata = scopeService
                            .updateInStageForRegistry(REGISTRY_NAME, stageName, ePackage,
                                    existingMetadata.getObjectId(), resolvedVersion)
                            .getValue();
                    // An absent ?dcat leaves the stored flag alone: an overwrite that says nothing
                    // about publication must not unpublish the model. Only an explicit value moves it.
                    if (dcat != null) {
                        metadata = scopeService.updatePropertiesInStageForRegistry(REGISTRY_NAME, stageName,
                                existingMetadata.getObjectId(),
                                Map.of(WorkflowConstants.DCAT_PUBLISH_METADATA_PROPERTY, dcat)).getValue();
                    }
                    ePackageIndex.index(metadata, ePackage);
                    Response.ResponseBuilder rb = Response.status(Response.Status.OK)
                            .header("Location", packageLocation(scopeName, stageName, validatedNsUri))
                            .entity(metadata).header("Content-Type", ResourceSupport.resolvedMediaType(requestContext));
                    ObjectMetadataResponseFilter.attach(requestContext, metadata,
                            ObjectMetadataResponseFilter.CacheTarget.METADATA);
                    return rb.build();
                }
            }
            // Create package and return metadata with Location header. The objectId is a
            // random UUID assigned once at upload and stable across stage transitions (it is
            // the lifecycle audit trail); the nsURI is carried only by the nsUri property.
            ObjectMetadata metadata = mgmtFactory.createObjectMetadata();
            metadata.setObjectId(UUID.randomUUID().toString());
            metadata.setObjectName(name == null ? ePackage.getName() : name);
            metadata.setUploadTime(Instant.now());
            metadata.setStage(stageName);
            metadata.setScope(scopeName);
            metadata.setRegistry(REGISTRY_NAME);
            metadata.setVersion(resolvedVersion);
            metadata.setObjectType(EcoreUtil.getURI(ePackage.eClass()).toString());
            metadata.getProperties().put(WorkflowConstants.NS_URI_METADATA_PROPERTY, validatedNsUri);
            // Stored as a Boolean, not a String: `properties` is String -> EJavaObject, so both
            // are storable and only one is what the publisher tests. Absent means false.
            metadata.getProperties().put(WorkflowConstants.DCAT_PUBLISH_METADATA_PROPERTY,
                    Boolean.TRUE.equals(dcat));

            metadata = scopeService.uploadToStageForRegistry(REGISTRY_NAME, stageName, ePackage, metadata).getValue();
            ePackageIndex.index(metadata, ePackage);

            Response.ResponseBuilder rb = Response.status(Response.Status.CREATED)
                    .header("Location", packageLocation(scopeName, stageName, validatedNsUri))
                    .entity(metadata).header("Content-Type", ResourceSupport.resolvedMediaType(requestContext));
            ObjectMetadataResponseFilter.attach(requestContext, metadata,
                    ObjectMetadataResponseFilter.CacheTarget.METADATA);
            return rb.build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (WebApplicationException e) {
            // WebApplicationException already has the correct status code, rethrow it
            throw e;
        } catch (Exception e) {
            throw EndpointFailures.propagate(e);
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
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
    public Response getPackageContent(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "The namespace URI of the package", required = true) @QueryParam("nsUri") String nsUri) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            requireNsUri(nsUri);
            nsUri = URI.decode(nsUri);
            ObjectMetadata contentMetadata = findByNsUriInStage(scopeService, stageName, nsUri);
            if (contentMetadata == null) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            EPackage ePackage = (EPackage) scopeService.getContentFromStageForRegistry(REGISTRY_NAME, stageName,
                    contentMetadata.getObjectId());
            if (ePackage == null) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            Response.ResponseBuilder rb = Response.status(Response.Status.OK).entity(ePackage)
                    .header("Content-Type", ResourceSupport.resolvedMediaType(requestContext));
            if (contentMetadata != null) {
                ObjectMetadataResponseFilter.attach(requestContext, contentMetadata);
                return rb.build();
            }
            return rb.build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            throw EndpointFailures.propagate(e);
        }
    }
    
    /**
     * Get the content of a SchemaPackage in a specific format.
     *
     * @param scopeName the scope name
     * @param nsUri     the namespace URI (required)
     * @return Schema package content in requested format
     */
    @GET
    @Path("/content")
    @Produces
    @Operation(summary = "Get package content from final stage, or in parent hierarchy", description = "Retrieve the content of a SchemaPackage in the requested format. "
            + "Respects hierarchical visibility.", responses = {
                    @ApiResponse(responseCode = "200", description = "Package content retrieved successfully"),
                    @ApiResponse(responseCode = "204", description = "Package not found"),
                    @ApiResponse(responseCode = "400", description = "Scope not available or schema registry not available for scope"),
                    @ApiResponse(responseCode = "406", description = "Requested format not supported"),
                    @ApiResponse(responseCode = "500", description = "Internal server error") })
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
    public Response getPackageContentFromFinalStage(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The namespace URI of the package", required = true) @QueryParam("nsUri") String nsUri) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            requireNsUri(nsUri);
            nsUri = URI.decode(nsUri);
            ObjectMetadata contentMetadata = findByNsUriInFinalStage(scopeService, nsUri);
            if (contentMetadata == null) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            Optional<?> ePackageContent = scopeService.get(REGISTRY_NAME, contentMetadata.getObjectId());
            if (ePackageContent.isEmpty()) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            Response.ResponseBuilder rb = Response.status(Response.Status.OK).entity(ePackageContent.get())
                    .header("Content-Type", ResourceSupport.resolvedMediaType(requestContext));
            if (contentMetadata != null) {
                ObjectMetadataResponseFilter.attach(requestContext, contentMetadata);
                return rb.build();
            }
            return rb.build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            throw EndpointFailures.propagate(e);
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
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
    public Response updatePackageContent(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "The updated version. If not provided, will be extracted from the nsURI. If provided, must be semantically compatible with the URI version.", required = false) @QueryParam("version") String version,
            @Parameter(description = "The namespace URI of the package. If not provided, uses the EPackage's nsURI. If provided, must match the EPackage's nsURI.", required = false) @QueryParam("nsUri") String nsUri,
            @RequestBody(description = "The new schema package content", required = true, content = @Content(schema = @Schema(implementation = EPackage.class))) EPackage ePackage) {

        ScopeService<EObject> scopeService = (ScopeService<EObject>) getScopeServiceByScopeName(scopeName);
        try {
            String validatedNsUri = validateAndResolveNsUri(nsUri, ePackage);
            String resolvedVersion = NsUriVersions.resolveAndValidate(version, validatedNsUri);
            ObjectMetadata existingMetadata = findByNsUriInStage(scopeService, stageName, validatedNsUri);
            if (existingMetadata == null) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            // We might want to check if the metadata is read only (e.g. if it was retrieved
            // from a parent final stage
            if (existingMetadata.isIsReadOnly()) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(String.format("Schema %s is in read-only state", nsUri)).build();
            }

            // If-Match validation (optimistic locking via ETag)
            Response preconditionResponse = ResourceSupport.checkIfMatch(headers, existingMetadata,
                    ObjectMetadataResponseFilter.CacheTarget.CONTENT);
            if (preconditionResponse != null) {
                return preconditionResponse;
            }

            // Content-aware skip: if content hasn't changed, skip the update
            String newContentHash = AbstractEObjectStorageService.computeContentHash(ePackage);
            if (newContentHash != null && newContentHash.equals(existingMetadata.getContentHash())) {
                Response.ResponseBuilder rb = Response.status(Response.Status.OK)
                        .entity(existingMetadata).header("Content-Type", ResourceSupport.resolvedMediaType(requestContext));
                ObjectMetadataResponseFilter.attach(requestContext, existingMetadata,
                        ObjectMetadataResponseFilter.CacheTarget.METADATA);
                return rb.build();
            }

            EcoreUtil.resolveAll(ePackage);
            if(ePackage.eResource() != null) {
        	ePackage.eResource().setURI(URI.createURI(ePackage.getNsURI()));
            }

            ObjectMetadata metadata = scopeService
                    .updateInStageForRegistry(REGISTRY_NAME, stageName, ePackage, existingMetadata.getObjectId(),
                            resolvedVersion)
                    .getValue();
            ePackageIndex.index(metadata, ePackage);
            Response.ResponseBuilder rb = Response.status(Response.Status.OK)
                    .entity(metadata).header("Content-Type", ResourceSupport.resolvedMediaType(requestContext));
            ObjectMetadataResponseFilter.attach(requestContext, metadata,
                    ObjectMetadataResponseFilter.CacheTarget.METADATA);
            return rb.build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (WebApplicationException e) {
            // WebApplicationException already has the correct status code, rethrow it
            throw e;
        } catch (Exception e) {
            throw EndpointFailures.propagate(e);
        }
    }

    /**
     * The metadata fields an operator may edit here. Everything else is refused <em>by name</em>: a
     * metadata editor that quietly drops half a request is how somebody comes to believe they
     * changed a publisher when they did not.
     *
     * <p>
     * Only {@code dcat} for now. It is deliberately a typed parameter rather than a generic
     * {@code property=key=value}: {@code properties} is {@code String -> EJavaObject}, so a
     * string-valued editor would store {@code "true"} where the publisher's service filter tests
     * {@code Boolean.TRUE}, and the flag would look set while publishing nothing. The per-model DCAT
     * metadata of §6 joins this list the same way, one typed parameter at a time.
     * </p>
     */
    private static final Set<String> EDITABLE_METADATA_PARAMS = Set.of("dcat");

    /**
     * Parameters that say <em>which</em> package to edit, and are never written to it.
     *
     * <p>
     * Kept apart from {@link #EDITABLE_METADATA_PARAMS} rather than merged into it: {@code nsUri} is
     * identity that happens to live in the {@code properties} map, and a set that called it editable
     * would say so in the 400 it hands clients — and would be the wrong thing to consult the day a
     * generic property editor is added.
     * </p>
     */
    private static final Set<String> METADATA_SELECTOR_PARAMS = Set.of("nsUri");

    /**
     * Fields this endpoint refuses, with the reason it refuses them. Identity, content-derived
     * values, provenance and workflow state are all owned by something other than a label editor.
     */
    private static final Map<String, String> REFUSED_METADATA_PARAMS = Map.ofEntries(
            Map.entry("objectId", "identity"), Map.entry("objectRef", "identity"), Map.entry("scope", "identity"),
            Map.entry("stage", "identity"), Map.entry("registry", "identity"), Map.entry("version", "identity"),
            Map.entry("contentHash", "derived from the content"),
            Map.entry("fingerprint", "derived from the content"),
            Map.entry("generationTriggerFingerprint", "derived from the content"),
            Map.entry("uploadUser", "provenance"), Map.entry("uploadTime", "provenance"),
            Map.entry("sourceChannel", "provenance"), Map.entry("objectType", "provenance"),
            Map.entry("status", "owned by the stage and review machinery"),
            Map.entry("isReadOnly", "owned by the stage and review machinery"),
            Map.entry("lastChangeUser", "maintained by the server"),
            Map.entry("lastChangeTime", "maintained by the server"));

    @PATCH
    @Path("/stages/{stageName}/metadata")
    @Consumes
    @Produces
    @Operation(summary = "Edit a schema package's editable metadata", description = "Changes metadata without "
            + "re-uploading the package. Restricted to fields that are labels rather than identity: any other "
            + "field is refused by name in a 400 rather than silently ignored. PATCH, not PUT, because a "
            + "whole-document PUT of an ObjectMetadata invites exactly the identity overwrite this endpoint "
            + "exists to forbid.", responses = {
                    @ApiResponse(responseCode = "200", description = "Metadata updated", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
                    @ApiResponse(responseCode = "400", description = "A field that cannot be edited, or a missing nsUri"),
                    @ApiResponse(responseCode = "403", description = "The package is read-only in this stage"),
                    @ApiResponse(responseCode = "404", description = "No such package in this stage"),
                    @ApiResponse(responseCode = "405", description = "This registry does not allow metadata updates"),
                    @ApiResponse(responseCode = "409", description = "The package is inherited: edit it in the scope that owns it"),
                    @ApiResponse(responseCode = "412", description = "If-Match did not match the current metadata"),
                    @ApiResponse(responseCode = "500", description = "Internal server error") })
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
    public Response patchPackageMetadata(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "The namespace URI of the package", required = true) @QueryParam("nsUri") String nsUri,
            @Parameter(description = "Whether this package may be published to a DCAT portal. Clearing it retires "
                    + "the published Dataset; setting it publishes the package if its scope and stage are "
                    + "configured for a portal.", required = false) @QueryParam("dcat") Boolean dcat) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            requireNsUri(nsUri);
            Response refusal = refuseNonEditableFields();
            if (refusal != null) {
                return refusal;
            }
            if (dcat == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Nothing to change: pass at least one editable field, e.g. ?dcat=true").build();
            }

            ObjectMetadata existingMetadata = findByNsUriInStage(scopeService, stageName, nsUri);
            if (existingMetadata == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Schema %s is not in stage %s of scope %s", nsUri, stageName, scopeName))
                        .build();
            }
            // An inherited package's record belongs to the scope that defines it. Editing it through
            // a child scope's URL would write to the parent's metadata under a name that does not
            // say so, which is worse than refusing.
            if (existingMetadata.getScope() != null && !existingMetadata.getScope().equals(scopeName)) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(String.format("Schema %s is inherited from scope %s; edit its metadata there", nsUri,
                                existingMetadata.getScope()))
                        .build();
            }
            if (existingMetadata.isIsReadOnly()) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(String.format("Schema %s is in read-only state", nsUri)).build();
            }

            // Metadata state, not content: this edit changes neither the bytes nor their hash, so
            // the precondition belongs over the metadata validator.
            Response preconditionResponse = ResourceSupport.checkIfMatch(headers, existingMetadata,
                    ObjectMetadataResponseFilter.CacheTarget.METADATA);
            if (preconditionResponse != null) {
                return preconditionResponse;
            }

            ObjectMetadata metadata = scopeService.updatePropertiesInStageForRegistry(REGISTRY_NAME, stageName,
                    existingMetadata.getObjectId(),
                    Map.of(WorkflowConstants.DCAT_PUBLISH_METADATA_PROPERTY, dcat)).getValue();
            if (metadata == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Schema %s is not in stage %s of scope %s", nsUri, stageName, scopeName))
                        .build();
            }
            ObjectMetadataResponseFilter.attach(requestContext, metadata,
                    ObjectMetadataResponseFilter.CacheTarget.METADATA);
            return Response.status(Response.Status.OK).entity(metadata)
                    .header("Content-Type", ResourceSupport.resolvedMediaType(requestContext)).build();

        } catch (UnsupportedOperationException notHere) {
            // The atlas root scope's registry, for one: its schemas are the system's own.
            return Response.status(Response.Status.METHOD_NOT_ALLOWED).entity(notHere.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            throw EndpointFailures.propagate(e);
        }
    }

    /**
     * @return a 400 naming the first field that cannot be edited, or {@code null} when every
     *         parameter sent is editable. Unknown names are refused too: a typo that silently
     *         changes nothing is the same failure as a refusal that says nothing
     */
    private Response refuseNonEditableFields() {
        for (String parameter : requestContext.getUriInfo().getQueryParameters().keySet()) {
            if (EDITABLE_METADATA_PARAMS.contains(parameter) || METADATA_SELECTOR_PARAMS.contains(parameter)) {
                continue;
            }
            String reason = REFUSED_METADATA_PARAMS.get(parameter);
            String message = reason == null
                    ? String.format("'%s' is not an editable metadata field. Editable: %s", parameter,
                            EDITABLE_METADATA_PARAMS)
                    : String.format("'%s' cannot be edited here: it is %s", parameter, reason);
            return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
        }
        return null;
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
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
    public Response deletePackage(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "The namespace URI of the package to delete", required = true) @QueryParam("nsUri") String nsUri) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            requireNsUri(nsUri);
            ObjectMetadata existingMetadata = findByNsUriInStage(scopeService, stageName, nsUri);
            if (existingMetadata == null) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            if (existingMetadata.isIsReadOnly()) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(String.format("Schema %s is in read-only state", nsUri)).build();
            }

            // If-Match validation (optimistic locking via ETag)
            Response preconditionResponse = ResourceSupport.checkIfMatch(headers, existingMetadata,
                    ObjectMetadataResponseFilter.CacheTarget.CONTENT);
            if (preconditionResponse != null) {
                return preconditionResponse;
            }

            boolean deleted = scopeService
                    .deleteFromStageForRegistry(REGISTRY_NAME, stageName, existingMetadata.getObjectId())
                    .getValue();
            if (deleted) {
            	ePackageIndex.remove(existingMetadata.getObjectId());
            	return Response.status(Response.Status.OK).build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("Schema %s deletion failed but causes are unknown", nsUri)).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            throw EndpointFailures.propagate(e);
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
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
    public Response transitionPackage(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The source stage name", required = true) @PathParam("stageName") String stageName,
            @RequestBody(description = "Transition request with objectId and targetStage", required = true, content = @Content()) StageTransitionRequest transitionRequest) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            String requestedId = transitionRequest.getObjectId();
            if (requestedId == null || requestedId.isBlank()) {
                throw new IllegalArgumentException("Transition request must carry an objectId");
            }
            String targetStage = transitionRequest.getTargetStage();
            ObjectMetadata existingMetadata = resolvePackageInStage(scopeService, stageName, requestedId);
            if (existingMetadata == null) {
                // Package not in source stage — check if already in target (idempotent retry)
                ObjectMetadata targetMetadata = resolvePackageInStage(scopeService, targetStage, requestedId);
                if (targetMetadata != null) {
                    ObjectMetadataResponseFilter.attach(requestContext, targetMetadata,
                            ObjectMetadataResponseFilter.CacheTarget.METADATA);
                    return Response.status(Response.Status.OK).entity(targetMetadata)
                            .header("Content-Type", ResourceSupport.resolvedMediaType(requestContext)).build();
                }
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            if (existingMetadata.isIsReadOnly()) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(String.format("Schema %s is in read-only state", transitionRequest.getObjectId()))
                        .build();
            }
            // If-Match validation (optimistic locking via the metadata ETag — a transition changes
            // metadata, not content).
            Response preconditionResponse = ResourceSupport.checkIfMatch(headers, existingMetadata,
                    ObjectMetadataResponseFilter.CacheTarget.METADATA);
            if (preconditionResponse != null) {
                return preconditionResponse;
            }
            ObjectMetadata metadata = scopeService.transitionToStageForRegistry(REGISTRY_NAME,
                    existingMetadata.getObjectId(), stageName, targetStage);
            reindexAfterTransition(scopeService, metadata, targetStage);
            ObjectMetadataResponseFilter.attach(requestContext, metadata,
                    ObjectMetadataResponseFilter.CacheTarget.METADATA);
            return Response.status(Response.Status.OK).entity(metadata).header("Content-Type", ResourceSupport.resolvedMediaType(requestContext)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            throw EndpointFailures.propagate(e);
        }

    }

    /**
     * Re-indexes a package after it moved to another stage. The index doc records the stage
     * a hit must be resolved against, so leaving the old one in place makes search report a
     * stage the object has left — and, once the source copy is gone, makes the hit resolve to
     * nothing and be dropped silently. Indexing is keyed by objectId, which a transition
     * preserves, so this replaces the doc rather than adding a second one.
     *
     * <p>
     * A transition that succeeded must not be reported as failed because the search index
     * could not be updated: the index is a derived view and can be rebuilt, so a failure here
     * is logged and the stale doc removed, leaving the package absent from search rather than
     * present under the wrong stage.
     * </p>
     */
    private void reindexAfterTransition(ScopeService<?> scopeService, ObjectMetadata metadata, String targetStage) {
        String objectId = metadata.getObjectId();
        try {
            Object content = scopeService.getContentFromStageForRegistry(REGISTRY_NAME, targetStage, objectId);
            if (content instanceof EPackage ePackage) {
                ePackageIndex.index(metadata, ePackage);
                return;
            }
            LOGGER.log(Level.WARNING,
                    "Could not re-index object {0} after its transition to stage {1}: no EPackage content in the target stage. Removing its stale index entry.",
                    new Object[] { objectId, targetStage });
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e,
                    () -> "Could not re-index object " + objectId + " after its transition to stage " + targetStage
                            + "; removing its stale index entry");
        }
        ePackageIndex.remove(objectId);
    }
    
    @GET
    @Path("/search")
    @Produces
    @Operation(summary = "Search packages across scope chain",
        description = "Search for schema packages within the given scope and its "
            + "parent scopes. Supports filtering by nsUri, name, prefix, "
            + "classifier names, structural feature names and types. "
            + "Returns ObjectMetadata with pagination.")
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
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

        // 5. Build response
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
    

    private ScopeService<?> getScopeServiceByScopeName(String scopeName) {
        ScopeService<?> scopeService = scopeCollector.getScopeServiceByScopeName(scopeName);
        if (scopeService == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Scope [" + scopeName + "] not found.").build());
        }
        return scopeService;
    }

    /**
     * Resolves the single package with the given nsURI in a stage via the nsUri metadata
     * property (hierarchy-aware, works for all storage backends including git). The upload
     * conflict check keeps the nsURI unique per (scope, registry, stage), so the property
     * lookup yields at most one element.
     */
    private ObjectMetadata findByNsUriInStage(ScopeService<?> scopeService, String stageName, String nsUri) {
        List<ObjectMetadata> matches = scopeService.getMetadataByPropertyFromStageForRegistry(REGISTRY_NAME, stageName,
                WorkflowConstants.NS_URI_METADATA_PROPERTY, nsUri);
        return matches.isEmpty() ? null : matches.get(0);
    }

    /**
     * Resolves a package in a stage by the identifier a transition request carries: first as a
     * real objectId, then — for legacy clients that still send the raw nsURI (the objectId used
     * to be derived from it) — via the nsUri property lookup.
     */
    private ObjectMetadata resolvePackageInStage(ScopeService<?> scopeService, String stageName, String idOrNsUri) {
        ObjectMetadata metadata = scopeService.getMetadataFromStageForRegistry(REGISTRY_NAME, stageName, idOrNsUri);
        if (metadata == null) {
            metadata = findByNsUriInStage(scopeService, stageName, idOrNsUri);
        }
        return metadata;
    }

    /** Final-stage variant of {@link #findByNsUriInStage(ScopeService, String, String)}. */
    private ObjectMetadata findByNsUriInFinalStage(ScopeService<?> scopeService, String nsUri) {
        List<ObjectMetadata> matches = scopeService.getMetadataByPropertyFromFinalStageForRegistry(REGISTRY_NAME,
                WorkflowConstants.NS_URI_METADATA_PROPERTY, nsUri);
        return matches.isEmpty() ? null : matches.get(0);
    }

    private String packageLocation(String scopeName, String stageName, String nsUri) {
        return "/".concat(scopeName).concat("/schema/stages/").concat(stageName).concat("?nsUri=")
                .concat(URLEncoder.encode(nsUri, StandardCharsets.UTF_8));
    }

    private static void requireNsUri(String nsUri) {
        if (nsUri == null || nsUri.isBlank()) {
            throw new IllegalArgumentException("Query parameter nsUri is required");
        }
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

}
