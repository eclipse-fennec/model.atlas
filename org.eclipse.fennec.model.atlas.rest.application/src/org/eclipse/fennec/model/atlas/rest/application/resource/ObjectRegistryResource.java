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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadataContainer;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractEObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.storage.ModelUnavailableException;
import org.eclipse.fennec.model.atlas.rest.application.exception.ModelUnavailableExceptionMapper;
import org.eclipse.fennec.model.atlas.rest.application.filter.ObjectMetadataResponseFilter;
import org.eclipse.fennec.model.atlas.rest.common.ModelAtlasRestConstants;
import org.eclipse.fennec.model.atlas.rest.model.StageTransitionRequest;
import org.eclipse.fennec.model.atlas.runtime.RequireRuntime;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.workflow.RegistryServiceCollector;
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
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * REST API for managing Storage Objects within scopes. Provides endpoints for
 * CRUD operations on storage objects with schema validation.
 *
 * @author Data In Motion
 * @since 1.0
 */
@RequireRuntime
@JakartarsResource()
@JakartarsName("ObjectRegistryResource")
@Component(name = "ObjectRegistryResource", service = ObjectRegistryResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/{scopeName}/registries/{registryName}")
@Tag(name = "Storage Management", description = "CRUD operations for storage objects with schema conformance")
public class ObjectRegistryResource {

    @Reference
    private ScopeServiceCollector scopeCollector;

    @Reference
    private RegistryServiceCollector registryCollector;

    @Reference
    private ManagementFactory mgmtFactory;

    @Context
    private HttpHeaders headers;
    
    @Context
    private ContainerRequestContext requestContext;

    // ======================
    // Storage Objects
    // ======================
    
    /**
     * List all objects in all the stages for this scope and registry.
     * Respects hierarchical visibility, including objects from parent scopes' final
     * stages.
     *
     * @param scopeName    the scope name
     * @param registryName the registry name
     * @return List of ObjectMetadata objects
     */
    @GET
    @Path("/all")
    @Produces
    @Operation(summary = "List objects in all the stages for provided scope and registry", description = "List all objects in all the stages for this scope and registry, including objects from parent scopes", responses = {
            @ApiResponse(responseCode = "200", description = "Objects retrieved successfully", content = @Content(schema = @Schema(type = "array", implementation = ObjectMetadata.class))),
            @ApiResponse(responseCode = "400", description = "Scope not available, registry not available for scope, stage not available for registry or not a valid stage"),
            @ApiResponse(responseCode = "204", description = "No object found in scope final stage, nor in the parent final stage"),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response listAll(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The registry name", required = true) @PathParam("registryName") String registryName) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            List<ObjectMetadata> objectsMetadata = scopeService.listAllForRegistry(registryName);
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

    /**
     * List all objects in the final/released stage for this scope and registry.
     * Respects hierarchical visibility, including objects from parent scopes' final
     * stages.
     *
     * @param scopeName    the scope name
     * @param registryName the registry name
     * @return List of ObjectMetadata objects
     */
    @GET
    @Produces
    @Operation(summary = "List objects in the final stage for provided scope and registry", description = "List all objects in the final stage for this scope and registry, including objects from parent scopes", responses = {
            @ApiResponse(responseCode = "200", description = "Objects retrieved successfully", content = @Content(schema = @Schema(type = "array", implementation = ObjectMetadata.class))),
            @ApiResponse(responseCode = "400", description = "Scope not available, registry not available for scope, stage not available for registry or not a valid stage"),
            @ApiResponse(responseCode = "204", description = "No object found in scope final stage, nor in the parent final stage"),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response listObjectsInFinalStage(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The registry name", required = true) @PathParam("registryName") String registryName) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            List<ObjectMetadata> objectsMetadata = scopeService.listInFinalStageForRegistry(registryName);
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

    /**
     * List the metadata for all storage objects within a specific registry.
     *
     * @param scopeName    the scope name
     * @param stageName    the stage name
     * @param registryName the registry name
     * @return List of StorageObjectMetadata objects
     */
    @GET
    @Path("/stages/{stageName}")
    @Produces
    @Operation(summary = "List objects in the provided scope, registry and stage", description = "List the metadata for all storage objects within a specific registry and stage. "
            + "Optionally accepts an objectId; if provided, the ObjectMetadata for the specific object will be returned, if found. "
            + "Optionally accepts a name filter; if provided and no objectId has been provided, a serch by name is done. "
            + "The name filter accepts wildcard, but no wildcard as first character and the search is case-insensitive."
            + "When the objectId is specified, the lookup respects hierarchical visibility.", responses = {
                    @ApiResponse(responseCode = "200", description = "Objects retrieved successfully", content = @Content(schema = @Schema(implementation = ObjectMetadata.class))),
                    @ApiResponse(responseCode = "204", description = "Stored object not found, or registry or stage not available for the scope"),
                    @ApiResponse(responseCode = "400", description = "Scope not available, registry not available for scope, stage not available for registry or not a valid stage"),
                    @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response listObjectsInRegistry(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The registry name", required = true) @PathParam("registryName") String registryName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "Exact id of the of the object to retrieve") @QueryParam("objectId") String objectId,
            @Parameter(description = "Object name filter (supports wildcards, e.g., Billing*)") @QueryParam("name") String name) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            if (objectId != null) {
                ObjectMetadata metadata = scopeService.getMetadataFromStageForRegistry(registryName, stageName,
                        objectId);
                if (metadata == null) {
                    return Response.status(Response.Status.NO_CONTENT).entity(String.format(
                            "Obejct %s not found neither scope '%s', registry '%s' and stage '%s' nor in parent hierarchy",
                            objectId, scopeName, registryName, stageName)).build();
                } else {
                    Response.ResponseBuilder rb = Response.status(Response.Status.OK).entity(metadata)
                            .header("Content-Type", getResolvedMediaType());
                    ObjectMetadataResponseFilter.attach(requestContext, metadata,
                            ObjectMetadataResponseFilter.CacheTarget.METADATA);
                    return rb.build();
                }
            } else if (name != null) {
                List<ObjectMetadata> objectsMetadata = scopeService.listInStageForRegistryByName(registryName,
                        stageName, name);
                if (objectsMetadata.isEmpty()) {
                    return Response.status(Response.Status.NO_CONTENT).build();
                }
                ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
                container.getMetadata().addAll(objectsMetadata);
                return Response.status(Response.Status.OK).entity(container).header("Content-Type", getResolvedMediaType()).build();
            } else {
                List<ObjectMetadata> objectsMetadata = scopeService.listInStageForRegistry(registryName, stageName);
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
     * Create a storage object with schema validation.
     *
     * @param scopeName    the scope name
     * @param stageName    the stage name
     * @param registryName the registry name
     * @param objectId     the object identifier
     * @param schemaNsUri  the namespace URI of the schema to validate against
     *                     (required)
     * @param object       the storage object content
     * @return StorageObjectMetadata
     */
    @SuppressWarnings("unchecked")
    @POST
    @PUT
    @Path("/stages/{stageName}/{objectId}")
    @Consumes
    @Produces
    @Operation(summary = "Create an object in the specified registry of the specified scope", description = "Create a storage object. The object must conform to a schema known to the ModelAtlas."
            + "Returns 201 Created for new objects, 200 OK for updates.", responses = {
                    @ApiResponse(responseCode = "201", description = "Object created successfully", content = @Content(schema = @Schema(implementation = ObjectMetadata.class))),
                    @ApiResponse(responseCode = "200", description = "Object updated successfully", content = @Content(schema = @Schema(implementation = ObjectMetadata.class))),
                    @ApiResponse(responseCode = "400", description = "Schema not found, validation failed, scope not available, registry not available for scope, stage not available for registry or not a valid stage "),
                    @ApiResponse(responseCode = "409", description = "Object with same id already exists and override option not set to true"),
                    @ApiResponse(responseCode = "415", description = "Unsupported media type"),
                    @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response createObject(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The registry name", required = true) @PathParam("registryName") String registryName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "The object identifier", required = true) @PathParam("objectId") String objectId,
            @Parameter(description = "Human-readable name for the object") @QueryParam("name") String name,
            @Parameter(description = "Object version") @QueryParam("version") String version,
            @Parameter(description = "Override option. If set to true and the object already exists, an update will be made. If set to false and the object already exists, it will result in a 409", required = false) @QueryParam("override") boolean override,
            @RequestBody(description = "The storage object content", required = true, content = @Content(schema = @Schema(implementation = EObject.class))) EObject object) {

        // Get schema registry for validation
        RegistryService<?> registryService = getRegistryServiceByRegistryName(registryName);
        if (registryService == null) {
            return Response.status(Status.BAD_REQUEST).entity("Unknown or unconfigured registry: " + registryName)
                    .build();
        }

        // Validate object type
        if (!registryService.isEClassCompatibleWithRegistry(object.eClass())) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(String.format("Object type %s not compatible with registry %s (expects %s)",
                            EcoreUtil.getURI(object.eClass()), registryName,
                            EcoreUtil.getURI(registryService.getRootEClass())))
                    .build();
        }

        ScopeService<EObject> scopeService = (ScopeService<EObject>) getScopeServiceByScopeName(scopeName);
        try {
            ObjectMetadata existingMetadata = scopeService.getMetadataFromStageForRegistry(registryName, stageName,
                    objectId);
            if (existingMetadata != null) {
                if (!override) {
                    return Response.status(Response.Status.CONFLICT)
                            .entity(String.format(
                                    "Object %s already exists and override flag is false. Cannot update object.",
                                    objectId))
                            .build();
                } else {
                    if (existingMetadata.isIsReadOnly()) {
                        return Response.status(Response.Status.FORBIDDEN)
                                .entity(String.format("Object %s is read-only. Cannot update it.", objectId)).build();
                    }
                    // If-Match validation (optimistic locking via the content ETag — override replaces
                    // the content of an existing object).
                    Response preconditionResponse = checkIfMatch(existingMetadata,
                            ObjectMetadataResponseFilter.CacheTarget.CONTENT);
                    if (preconditionResponse != null) {
                        return preconditionResponse;
                    }
                    ObjectMetadata metadata = scopeService
                            .updateInStageForRegistry(registryName, stageName, object, objectId, version).getValue();
                    Response.ResponseBuilder rb = Response.status(Response.Status.OK)
                            .header("Location",
                                    "/".concat(scopeName).concat("/registries/").concat(registryName).concat("/stages/")
                                            .concat(stageName).concat("?objectId=").concat(objectId))
                            .entity(metadata).header("Content-Type", getResolvedMediaType());
                    ObjectMetadataResponseFilter.attach(requestContext, metadata,
                            ObjectMetadataResponseFilter.CacheTarget.METADATA);
                    return rb.build();
                }
            }
            ObjectMetadata metadata = mgmtFactory.createObjectMetadata();
            metadata.setObjectId(objectId);
            metadata.setObjectName(name);
            metadata.setUploadTime(Instant.now());
            metadata.setStage(stageName);
            metadata.setScope(scopeName);
            metadata.setRegistry(registryName);
            metadata.setVersion(version);
            metadata.setObjectType(EcoreUtil.getURI(object.eClass()).toString());

            metadata = scopeService.uploadToStageForRegistry(registryName, stageName, object, metadata).getValue();
            Response.ResponseBuilder rb = Response.status(Response.Status.CREATED)
                    .header("Location",
                            "/".concat(scopeName).concat("/registries/").concat(registryName).concat("/stages/")
                                    .concat(stageName).concat("?objectId=").concat(objectId))
                    .entity(metadata).header("Content-Type", getResolvedMediaType());
            ObjectMetadataResponseFilter.attach(requestContext, metadata,
                    ObjectMetadataResponseFilter.CacheTarget.METADATA);
            return rb.build();
        } catch (WebApplicationException e) {
            // WebApplicationException already has the correct status code, rethrow it
            throw e;
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * Get the raw content of a storage object.
     *
     * @param scopeName    the scope name
     * @param stageName    the stage name
     * @param registryName the registry name
     * @param objectId     the object identifier
     * @return Storage object content in requested format
     */
    @GET
    @Path("/stages/{stageName}/content")
    @Produces
    @Operation(summary = "Get storage object content", description = "Retrieve the raw content of a storage object. "
            + "The Accept header can be used to request content transformation.", responses = {
                    @ApiResponse(responseCode = "200", description = "Object content retrieved successfully"),
                    @ApiResponse(responseCode = "204", description = "Object not found"),
                    @ApiResponse(responseCode = "400", description = "Scope not available, registry not available for scope, stage not available for registry or not a valid stage"),
                    @ApiResponse(responseCode = "406", description = "Requested format not supported"),
                    @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response getObjectContent(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The registry name", required = true) @PathParam("registryName") String registryName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "The object identifier", required = true) @QueryParam("objectId") String objectId) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            ObjectMetadata contentMetadata = scopeService.getMetadataFromStageForRegistry(registryName, stageName, objectId);
            EObject eObject = scopeService.getContentFromStageForRegistry(registryName, stageName, objectId);
            if (eObject == null) {
                return Response.status(Response.Status.NO_CONTENT).entity(String.format(
                        "Obejct %s not found neither scope '%s', registry '%s' and stage '%s' nor in parent hierarchy",
                        objectId, scopeName, registryName, stageName)).build();
            }
            Response.ResponseBuilder rb = Response.status(Response.Status.OK).entity(eObject)
                    .header("Content-Type", getResolvedMediaType());
            if (contentMetadata != null) {
                ObjectMetadataResponseFilter.attach(requestContext, contentMetadata);
                return rb.build();
            }
            return rb.build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            // The instance's backing model may have been removed (e.g. a git branch push) — the
            // storage read surfaces that as a (wrapped) ModelUnavailableException. Map it to 409
            // Conflict rather than an opaque 500.
            ModelUnavailableException mue = ModelUnavailableExceptionMapper.findInChain(e);
            if (mue != null) {
                return ModelUnavailableExceptionMapper.conflict(mue);
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    /**
     * Get the raw content of a storage object.
     *
     * @param scopeName    the scope name
     * @param registryName the registry name
     * @param objectId     the object identifier
     * @return Storage object content in requested format
     */
    @GET
    @Path("/content")
    @Produces
    @Operation(summary = "Get object content from final stage", description = "Retrieve the raw content of a storage object from the final stage. "
            + "The Accept header can be used to request content transformation.", responses = {
                    @ApiResponse(responseCode = "200", description = "Object content retrieved successfully"),
                    @ApiResponse(responseCode = "204", description = "Object not found"),
                    @ApiResponse(responseCode = "400", description = "Scope not available or registry not available for scope"),
                    @ApiResponse(responseCode = "406", description = "Requested format not supported"),
                    @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response getObjectContentFromFinalStage(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The registry name", required = true) @PathParam("registryName") String registryName,
            @Parameter(description = "The object identifier", required = true) @QueryParam("objectId") String objectId) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            ObjectMetadata contentMetadata = scopeService.getMetadataFromFinalStageForRegistry(registryName, objectId);
            Optional<?> optionalContent = scopeService.get(registryName, objectId);
            if (optionalContent.isEmpty()) {
                return Response.status(Response.Status.NO_CONTENT).entity(String.format(
                        "Obejct %s not found neither in (scope,registry)=('%s','%s') final stage nor in parent hierarchy",
                        objectId, scopeName, registryName)).build();
            }
            Response.ResponseBuilder rb = Response.status(Response.Status.OK).entity(optionalContent.get())
                    .header("Content-Type", getResolvedMediaType());
            if (contentMetadata != null) {
                ObjectMetadataResponseFilter.attach(requestContext, contentMetadata);
                return rb.build();
            }
            return rb.build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            // The instance's backing model may have been removed (e.g. a git branch push) — the
            // storage read surfaces that as a (wrapped) ModelUnavailableException. Map it to 409
            // Conflict rather than an opaque 500.
            ModelUnavailableException mue = ModelUnavailableExceptionMapper.findInChain(e);
            if (mue != null) {
                return ModelUnavailableExceptionMapper.conflict(mue);
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * Get the raw content of a storage object.
     *
     * @param scopeName    the scope name
     * @param stageName    the stage name
     * @param registryName the registry name
     * @param objectId     the object identifier
     * @return Storage object content in requested format
     */
    @SuppressWarnings("unchecked")
    @PUT
    @POST
    @Path("/stages/{stageName}/content")
    @Produces
    @Operation(summary = "Update storage object content", description = "Replace the content of an existing object. "
            + "Fails if the stage is read-only (e.g. released) or if scope, registry, stage or object cannot be found.", responses = {
                    @ApiResponse(responseCode = "200", description = "Object content updated successfully"),
                    @ApiResponse(responseCode = "204", description = "Object not found"),
                    @ApiResponse(responseCode = "400", description = "If updated object is not compliant with schema, scope not available, registry not available for scope, stage not available for registry or not a valid writable stage"),
                    @ApiResponse(responseCode = "403", description = "The requested object is read-only (e.g. if it is only available in a parent scope)"),
                    @ApiResponse(responseCode = "415", description = "Requested format not supported"),
                    @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response updateObjectContent(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The registry name", required = true) @PathParam("registryName") String registryName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "The updated version", required = true) @QueryParam("version") String version,
            @Parameter(description = "The object identifier", required = true) @QueryParam("objectId") String objectId,
            @RequestBody(description = "The new object content", required = true, content = @Content(schema = @Schema(implementation = EObject.class))) EObject eObject) {

        // Get schema registry for validation
        RegistryService<?> registryService = getRegistryServiceByRegistryName(registryName);
        if (registryService == null) {
            return Response.status(Status.BAD_REQUEST).entity("Unknown or unconfigured registry: " + registryName)
                    .build();
        }

        // Validate object type
        if (!registryService.isEClassCompatibleWithRegistry(eObject.eClass())) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(String.format("Object type %s not compatible with registry %s (expects %s)",
                            EcoreUtil.getURI(eObject.eClass()), registryName,
                            EcoreUtil.getURI(registryService.getRootEClass())))
                    .build();
        }

        ScopeService<EObject> scopeService = (ScopeService<EObject>) getScopeServiceByScopeName(scopeName);
        try {
            ObjectMetadata existingMetadata = scopeService.getMetadataFromStageForRegistry(registryName, stageName,
                    objectId);
            if (existingMetadata == null) {
                return Response.status(Response.Status.NO_CONTENT).entity(String.format(
                        "Obejct %s not found neither scope '%s', registry '%s' and stage '%s' nor in parent hierarchy",
                        objectId, scopeName, registryName, stageName)).build();
            }
            // We might want to check if the metadata is read only (e.g. if it was retrieved
            // from a parent final stage
            if (existingMetadata.isIsReadOnly()) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(String.format("Object %s is in read-only state", objectId)).build();
            }

            // If-Match validation (optimistic locking via ETag)
            Response preconditionResponse = checkIfMatch(existingMetadata,
                    ObjectMetadataResponseFilter.CacheTarget.CONTENT);
            if (preconditionResponse != null) {
                return preconditionResponse;
            }

            // Content-aware skip: if content hasn't changed, skip the update
            String newContentHash = AbstractEObjectStorageService.computeContentHash(eObject);
            if (newContentHash != null && newContentHash.equals(existingMetadata.getContentHash())) {
                Response.ResponseBuilder rb = Response.status(Response.Status.OK)
                        .entity(existingMetadata).header("Content-Type", getResolvedMediaType());
                ObjectMetadataResponseFilter.attach(requestContext, existingMetadata,
                        ObjectMetadataResponseFilter.CacheTarget.METADATA);
                return rb.build();
            }

            ObjectMetadata metadata = scopeService
                    .updateInStageForRegistry(registryName, stageName, eObject, objectId, version).getValue();
            Response.ResponseBuilder rb = Response.status(Response.Status.OK)
                    .entity(metadata).header("Content-Type", getResolvedMediaType());
            ObjectMetadataResponseFilter.attach(requestContext, metadata,
                    ObjectMetadataResponseFilter.CacheTarget.METADATA);
            return rb.build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * Delete a storage object.
     *
     * @param scopeName    the scope name
     * @param stageName    the stage name
     * @param registryName the registry name
     * @param objectId     the object identifier
     * @return 200 on success
     */
    @DELETE
    @Path("/stages/{stageName}")
    @Operation(summary = "Delete storage object", description = "Delete a storage object from the registry and stage. Fails if the stage is read-only.", responses = {
            @ApiResponse(responseCode = "200", description = "Object deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Stage is read-only or Object is only present in a parent scope final stage and so it's read-only"),
            @ApiResponse(responseCode = "400", description = "Scope not available, registry not available for scope, stage not available for registry or not a valid stage"),
            @ApiResponse(responseCode = "204", description = "Object not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response deleteObject(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The registry name", required = true) @PathParam("registryName") String registryName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "The object identifier", required = true) @QueryParam("objectId") String objectId) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            ObjectMetadata existingMetadata = scopeService.getMetadataFromStageForRegistry(registryName, stageName,
                    objectId);
            if (existingMetadata == null) {
                return Response.status(Response.Status.NO_CONTENT).entity(String.format(
                        "Obejct %s not found neither scope '%s', registry '%s' and stage '%s' nor in parent hierarchy",
                        objectId, scopeName, registryName, stageName)).build();
            }
            if (existingMetadata.isIsReadOnly()) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(String.format("Object %s is in read-only state", objectId)).build();
            }

            // If-Match validation (optimistic locking via ETag)
            Response preconditionResponse = checkIfMatch(existingMetadata,
                    ObjectMetadataResponseFilter.CacheTarget.CONTENT);
            if (preconditionResponse != null) {
                return preconditionResponse;
            }

            boolean deleted = scopeService.deleteFromStageForRegistry(registryName, stageName, objectId).getValue();
            if (deleted)
                return Response.noContent().build();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("Object %s deletion failed but causes are unknown", objectId)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * Transition a package from one stage to another.
     *
     * @param scopeName         the scope name
     * @param stageName         the source stage name
     * @param registryName      the registry name
     * @param transitionRequest the transition request containing nsUri and
     *                          targetStage
     * @return Updated SchemaPackage metadata
     */
    @POST
    @Path("/stages/{stageName}/actions/transition")
    @Consumes
    @Produces
    @Operation(summary = "Transition object between stages of the same registry and scope", description = "Move an object from the current stage to a target stage within the same scope and registry. "
            + "Validates that the transition is allowed based on stage rules.", responses = {
                    @ApiResponse(responseCode = "200", description = "Object transitioned successfully", content = @Content(schema = @Schema(implementation = ObjectMetadata.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid transition, missing parameters, scope not available, registry not available for scope, stage not available for registry or not a valid stage"),
                    @ApiResponse(responseCode = "403", description = "Stage is read-only or Object is only present in a parent scope final stage and so it's read-only"),
                    @ApiResponse(responseCode = "204", description = "Object not found in source stage"),
                    @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response transitionObject(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The registry name", required = true) @PathParam("registryName") String registryName,
            @Parameter(description = "The source stage name", required = true) @PathParam("stageName") String stageName,
            @RequestBody(description = "Transition request with objectId and targetStage", required = true, content = @Content(schema = @Schema(implementation = StageTransitionRequest.class))) StageTransitionRequest transitionRequest) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            String objectId = transitionRequest.getObjectId();
            String targetStage = transitionRequest.getTargetStage();
            ObjectMetadata existingMetadata = scopeService.getMetadataFromStageForRegistry(registryName, stageName,
                    objectId);
            if (existingMetadata == null) {
                // Object not in source stage — check if already in target (idempotent retry)
                ObjectMetadata targetMetadata = scopeService.getMetadataFromStageForRegistry(registryName, targetStage,
                        objectId);
                if (targetMetadata != null) {
                    ObjectMetadataResponseFilter.attach(requestContext, targetMetadata,
                            ObjectMetadataResponseFilter.CacheTarget.METADATA);
                    return Response.status(Response.Status.OK).entity(targetMetadata)
                            .header("Content-Type", getResolvedMediaType()).build();
                }
                return Response.status(Response.Status.NO_CONTENT).entity(String.format(
                        "Obejct %s not found neither scope '%s', registry '%s' and stage '%s' nor in parent hierarchy",
                        objectId, scopeName, registryName, stageName)).build();
            }
            if (existingMetadata.isIsReadOnly()) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(String.format("Object %s is in read-only state", objectId)).build();
            }
            // If-Match validation (optimistic locking via the metadata ETag — a transition changes
            // metadata, not content).
            Response preconditionResponse = checkIfMatch(existingMetadata,
                    ObjectMetadataResponseFilter.CacheTarget.METADATA);
            if (preconditionResponse != null) {
                return preconditionResponse;
            }
            ObjectMetadata metadata = scopeService.transitionToStageForRegistry(registryName, objectId, stageName,
                    targetStage);
            ObjectMetadataResponseFilter.attach(requestContext, metadata,
                    ObjectMetadataResponseFilter.CacheTarget.METADATA);
            return Response.status(Response.Status.OK).entity(metadata).header("Content-Type", getResolvedMediaType()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * Checks the {@code If-Match} header for an optimistic-concurrency precondition against the current
     * state of {@code metadata}, using the same validator the response filter emits as the ETag.
     * Returns a {@code 412 Precondition Failed} response if the precondition is not satisfied, or
     * {@code null} if it is satisfied, if no {@code If-Match} header was sent, or if there is no
     * validator to compare against.
     *
     * @param metadata the current metadata of the object being written
     * @param target   which validator to check against: {@link ObjectMetadataResponseFilter.CacheTarget#CONTENT}
     *                 for writes that replace the content, {@link ObjectMetadataResponseFilter.CacheTarget#METADATA}
     *                 for writes that only change metadata (e.g. a stage transition)
     */
    private Response checkIfMatch(ObjectMetadata metadata, ObjectMetadataResponseFilter.CacheTarget target) {
        String ifMatch = headers.getHeaderString("If-Match");
        if (ifMatch == null) {
            return null; // No precondition — proceed normally
        }
        String base = ObjectMetadataResponseFilter.baseValidator(metadata, target);
        if (base == null) {
            return null; // No validator yet — cannot validate, proceed
        }
        if (!ObjectMetadataResponseFilter.ifMatchSatisfied(ifMatch, base)) {
            return Response.status(Response.Status.PRECONDITION_FAILED)
                    .entity("Resource has been modified. ETag mismatch.").build();
        }
        return null;
    }

    private ScopeService<?> getScopeServiceByScopeName(String scopeName) {
        return scopeCollector.getScopeServiceByScopeName(scopeName);
    }

    private RegistryService<?> getRegistryServiceByRegistryName(String registryName) {
        return registryCollector.getRegistryServiceByRegistryName(registryName);
    }

    private String getResolvedMediaType() {
        return (String) requestContext.getProperty(ModelAtlasRestConstants.RESOLVED_MEDIA_TYPE);
    }
}
