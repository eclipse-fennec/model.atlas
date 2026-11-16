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

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.codec.constants.CodecOptions;
import org.eclipse.fennec.codec.rest.annotations.EMFResourceOptions;
import org.eclipse.fennec.codec.rest.annotations.ResourceOption;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadataContainer;
import org.eclipse.fennec.model.atlas.mgmt.storage.ModelUnavailableException;
import org.eclipse.fennec.model.atlas.registry.api.ConflictPolicy;
import org.eclipse.fennec.model.atlas.registry.api.ObjectAddress;
import org.eclipse.fennec.model.atlas.registry.api.ObjectWrite;
import org.eclipse.fennec.model.atlas.registry.api.RegistryAccessException;
import org.eclipse.fennec.model.atlas.registry.api.RegistryWriter;
import org.eclipse.fennec.model.atlas.registry.api.WriteOutcome;
import org.eclipse.fennec.model.atlas.registry.api.WriteResult;
import org.eclipse.fennec.model.atlas.rest.application.exception.EndpointFailures;
import org.eclipse.fennec.model.atlas.rest.application.exception.ModelUnavailableExceptionMapper;
import org.eclipse.fennec.model.atlas.rest.application.filter.ObjectMetadataResponseFilter;
import org.eclipse.fennec.model.atlas.rest.model.StageTransitionRequest;
import org.eclipse.fennec.model.atlas.runtime.RequireRuntime;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
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
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

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
    private RegistryWriter registryWriter;

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
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
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
            return Response.status(Response.Status.OK).entity(container).header("Content-Type", ResourceSupport.resolvedMediaType(requestContext)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            throw EndpointFailures.propagate(e);
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
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
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
            return Response.status(Response.Status.OK).entity(container).header("Content-Type", ResourceSupport.resolvedMediaType(requestContext)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            throw EndpointFailures.propagate(e);
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
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
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
                    return Response.status(Response.Status.NO_CONTENT).build();
                } else {
                    Response.ResponseBuilder rb = Response.status(Response.Status.OK).entity(metadata)
                            .header("Content-Type", ResourceSupport.resolvedMediaType(requestContext));
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
                return Response.status(Response.Status.OK).entity(container).header("Content-Type", ResourceSupport.resolvedMediaType(requestContext)).build();
            } else {
                List<ObjectMetadata> objectsMetadata = scopeService.listInStageForRegistry(registryName, stageName);
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
                    @ApiResponse(responseCode = "403", description = "The existing object is read-only, or its stage is final for the registry and so its objects cannot be updated"),
                    @ApiResponse(responseCode = "409", description = "Object with same id already exists and override option not set to true"),
                    @ApiResponse(responseCode = "415", description = "Unsupported media type"),
                    @ApiResponse(responseCode = "500", description = "Internal server error") })
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
    public Response createObject(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The registry name", required = true) @PathParam("registryName") String registryName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "The object identifier", required = true) @PathParam("objectId") String objectId,
            @Parameter(description = "Human-readable name for the object") @QueryParam("name") String name,
            @Parameter(description = "Object version") @QueryParam("version") String version,
            @Parameter(description = "Override option. If set to true and the object already exists, an update will be made. If set to false and the object already exists, it will result in a 409", required = false) @QueryParam("override") boolean override,
            @RequestBody(description = "The storage object content", required = true, content = @Content(schema = @Schema(implementation = EObject.class))) EObject object) {

        try {
            WriteResult result = registryWriter.createObject(new ObjectWrite(
                    new ObjectAddress(scopeName, registryName, stageName, objectId), object, name, version,
                    override ? ConflictPolicy.OVERWRITE : ConflictPolicy.FAIL,
                    ResourceSupport.ifMatch(headers, ObjectMetadataResponseFilter.CacheTarget.CONTENT)));
            ObjectMetadata metadata = result.metadata();
            Response.ResponseBuilder rb = Response
                    .status(result.outcome() == WriteOutcome.CREATED ? Response.Status.CREATED : Response.Status.OK)
                    .location(objectLocation(scopeName, registryName, stageName, objectId))
                    .entity(metadata).header("Content-Type", ResourceSupport.resolvedMediaType(requestContext));
            ObjectMetadataResponseFilter.attach(requestContext, metadata,
                    ObjectMetadataResponseFilter.CacheTarget.METADATA);
            return rb.build();
        } catch (RegistryAccessException e) {
            return ResourceSupport.refusal(e);
        } catch (WebApplicationException e) {
            // WebApplicationException already has the correct status code, rethrow it
            throw e;
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            throw EndpointFailures.propagate(e);
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
    /*
     * AP4 of the GDPR review-document plan. The three options are pinned here rather than left to
     * the caller:
     *
     * - CODEC_ID_KEY_MODE: an EMF ID attribute is a feature, not a document key, or it comes back
     *   as '_id'.
     * - codec.tabular.referenceMode=SQL_TABLES: what turns a root's containment lists into one
     *   sheet each. Without it a spreadsheet download carries the root sheet alone - a review
     *   history renders as its title and nothing else. The key is
     *   CodecTabularOptions.OPTION_REFERENCE_MODE, written out because the tabular codec is not on
     *   this bundle's buildpath and should not be, for one constant. Non-tabular codecs ignore it.
     * - CODEC_SERIALIZE_DEFAULT: without it every value equal to its feature default is omitted,
     *   so UNCHANGED, AI_AGENT and changeCount=0 arrive as empty cells. Whether a compliance
     *   document shows its own default values is not a caller's decision, and the option is not on
     *   the ODS whitelist, so it cannot come from the Codec-Options header either.
     */
    @EMFResourceOptions(options = { //
            @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY"), //
            @ResourceOption(key = "codec.tabular.referenceMode", value = "SQL_TABLES"), //
            @ResourceOption(key = CodecOptions.CODEC_SERIALIZE_DEFAULT, value = "true", valueType = Boolean.class) })
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
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            Response.ResponseBuilder rb = Response.status(Response.Status.OK).entity(eObject)
                    .header("Content-Type", ResourceSupport.resolvedMediaType(requestContext));
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
            throw EndpointFailures.propagate(e);
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
    /*
     * AP4 of the GDPR review-document plan. The three options are pinned here rather than left to
     * the caller:
     *
     * - CODEC_ID_KEY_MODE: an EMF ID attribute is a feature, not a document key, or it comes back
     *   as '_id'.
     * - codec.tabular.referenceMode=SQL_TABLES: what turns a root's containment lists into one
     *   sheet each. Without it a spreadsheet download carries the root sheet alone - a review
     *   history renders as its title and nothing else. The key is
     *   CodecTabularOptions.OPTION_REFERENCE_MODE, written out because the tabular codec is not on
     *   this bundle's buildpath and should not be, for one constant. Non-tabular codecs ignore it.
     * - CODEC_SERIALIZE_DEFAULT: without it every value equal to its feature default is omitted,
     *   so UNCHANGED, AI_AGENT and changeCount=0 arrive as empty cells. Whether a compliance
     *   document shows its own default values is not a caller's decision, and the option is not on
     *   the ODS whitelist, so it cannot come from the Codec-Options header either.
     */
    @EMFResourceOptions(options = { //
            @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY"), //
            @ResourceOption(key = "codec.tabular.referenceMode", value = "SQL_TABLES"), //
            @ResourceOption(key = CodecOptions.CODEC_SERIALIZE_DEFAULT, value = "true", valueType = Boolean.class) })
    public Response getObjectContentFromFinalStage(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The registry name", required = true) @PathParam("registryName") String registryName,
            @Parameter(description = "The object identifier", required = true) @QueryParam("objectId") String objectId) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            ObjectMetadata contentMetadata = scopeService.getMetadataFromFinalStageForRegistry(registryName, objectId);
            Optional<?> optionalContent = scopeService.get(registryName, objectId);
            if (optionalContent.isEmpty()) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            Response.ResponseBuilder rb = Response.status(Response.Status.OK).entity(optionalContent.get())
                    .header("Content-Type", ResourceSupport.resolvedMediaType(requestContext));
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
            throw EndpointFailures.propagate(e);
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
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
    public Response updateObjectContent(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The registry name", required = true) @PathParam("registryName") String registryName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "The updated version", required = true) @QueryParam("version") String version,
            @Parameter(description = "The object identifier", required = true) @QueryParam("objectId") String objectId,
            @RequestBody(description = "The new object content", required = true, content = @Content(schema = @Schema(implementation = EObject.class))) EObject eObject) {

        try {
            WriteResult result = registryWriter.updateObject(new ObjectWrite(
                    new ObjectAddress(scopeName, registryName, stageName, objectId), eObject, null, version, null,
                    ResourceSupport.ifMatch(headers, ObjectMetadataResponseFilter.CacheTarget.CONTENT)));
            if (result.outcome() == WriteOutcome.NOT_FOUND) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            Response.ResponseBuilder rb = Response.status(Response.Status.OK)
                    .entity(result.metadata()).header("Content-Type", ResourceSupport.resolvedMediaType(requestContext));
            ObjectMetadataResponseFilter.attach(requestContext, result.metadata(),
                    ObjectMetadataResponseFilter.CacheTarget.METADATA);
            return rb.build();

        } catch (RegistryAccessException e) {
            return ResourceSupport.refusal(e);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            throw EndpointFailures.propagate(e);
        }
    }

    /**
     * Delete a storage object.
     *
     * @param scopeName    the scope name
     * @param stageName    the stage name
     * @param registryName the registry name
     * @param objectId     the object identifier
     * @param force        {@code true} to delete although a stage gate refuses it; the
     *                     gate's findings are then recorded on the dependents it named
     * @return 200 on success
     */
    @DELETE
    @Path("/stages/{stageName}")
    @Operation(summary = "Delete storage object", description = "Delete a storage object from the registry and stage. Fails if the stage is read-only. "
            + "A stage gate may refuse the delete, for example while other objects depend on this one; "
            + "the refusal is answered with 409 and its diagnostics are recorded on the object. "
            + "force=true deletes anyway and records the consequence on the dependents the gate named.", responses = {
            @ApiResponse(responseCode = "200", description = "Object deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Stage is read-only or Object is only present in a parent scope final stage and so it's read-only"),
            @ApiResponse(responseCode = "400", description = "Scope not available, registry not available for scope, stage not available for registry or not a valid stage"),
            @ApiResponse(responseCode = "204", description = "Object not found"),
            @ApiResponse(responseCode = "409", description = "A stage gate refused the delete; the object keeps its stage. The body is its unchanged metadata, whose diagnostics carry the gate's findings. Retry with force=true to override.",
                    content = @Content(schema = @Schema(implementation = ObjectMetadata.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
    public Response deleteObject(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The registry name", required = true) @PathParam("registryName") String registryName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "The object identifier", required = true) @QueryParam("objectId") String objectId,
            @Parameter(description = "Delete although a stage gate refuses it; the gate's findings are recorded on the dependents instead") @QueryParam("force") @DefaultValue("false") boolean force) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            WriteResult result = registryWriter.deleteObject(
                    new ObjectAddress(scopeName, registryName, stageName, objectId), force,
                    ResourceSupport.ifMatch(headers, ObjectMetadataResponseFilter.CacheTarget.CONTENT));
            if (result.outcome() == WriteOutcome.NOT_FOUND) {
                // Nothing to delete. 204 without a body: a 204 carries no entity, and the
                // status alone is now unambiguous because a delete that happened is 200.
                return Response.noContent().build();
            }
            // 200, as this endpoint's @ApiResponse documents and as the sibling
            // SchemaPackagesResource.deletePackage answers, so that success is
            // distinguishable from the 204 above.
            return Response.status(Response.Status.OK).build();
        } catch (RegistryAccessException e) {
            return ResourceSupport.refusal(e);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            // a gate's refusal is answered with the object's metadata as the 409 body (issue #295)
            Response refused = GateRefusals.conflict(e, scopeService, requestContext);
            if (refused != null) {
                return refused;
            }
            throw EndpointFailures.propagate(e);
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
                    @ApiResponse(responseCode = "409", description = "Either the target stage already holds a different object under this objectId (an objectId is unique per stage, so promoting onto an earlier copy of the same object is allowed; taking the id over from another object requires overwrite=true) - then the body is the plain error document - "
                            + "or a stage gate refused the transition because the object does not hold up in the target stage, e.g. a QVT source that does not compile against the target stage's view. Then the body is the object's unchanged metadata from the source stage, whose diagnostics carry the gate's findings and what to change before retrying",
                            content = @Content(schema = @Schema(implementation = ObjectMetadata.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error") })
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
    public Response transitionObject(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The registry name", required = true) @PathParam("registryName") String registryName,
            @Parameter(description = "The source stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "Overwrite option. If the target stage holds a different object under this objectId, false (the default) rejects the transition with a 409 and true replaces that object. Replacing an earlier copy of the object being promoted needs no flag - that is what a promotion is for", required = false) @QueryParam("overwrite") boolean overwrite,
            @RequestBody(description = "Transition request with objectId and targetStage", required = true, content = @Content(schema = @Schema(implementation = StageTransitionRequest.class))) StageTransitionRequest transitionRequest) {

        ScopeService<?> scopeService = getScopeServiceByScopeName(scopeName);
        try {
            WriteResult result = registryWriter.transitionObject(
                    new ObjectAddress(scopeName, registryName, stageName, transitionRequest.getObjectId()),
                    transitionRequest.getTargetStage(), overwrite,
                    // a transition changes metadata, not content
                    ResourceSupport.ifMatch(headers, ObjectMetadataResponseFilter.CacheTarget.METADATA));
            if (result.outcome() == WriteOutcome.NOT_FOUND) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            ObjectMetadataResponseFilter.attach(requestContext, result.metadata(),
                    ObjectMetadataResponseFilter.CacheTarget.METADATA);
            return Response.status(Response.Status.OK).entity(result.metadata())
                    .header("Content-Type", ResourceSupport.resolvedMediaType(requestContext)).build();
        } catch (RegistryAccessException e) {
            return ResourceSupport.refusal(e);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            // a gate's refusal is answered with the source-stage metadata as the 409 body (issue #295)
            Response refused = GateRefusals.conflict(e, scopeService, requestContext);
            if (refused != null) {
                return refused;
            }
            throw EndpointFailures.propagate(e);
        }
    }

    private ScopeService<?> getScopeServiceByScopeName(String scopeName) {
        return scopeCollector.getScopeServiceByScopeName(scopeName);
    }

    /**
     * The {@code Location} of an object in a stage, absolute and built from the request's
     * base URI. The object id is client-chosen (a file name like
     * {@code Mapping WaterQuality.xmi}) and has to go into the query encoded: an unencoded
     * blank makes the header unparseable and the upload answer a 500 (#333). A
     * {@code /scope/...} path would also lose the application's base path, as Jersey
     * resolves it against the host.
     */
    private URI objectLocation(String scopeName, String registryName, String stageName, String objectId) {
        return requestContext.getUriInfo().getBaseUriBuilder()
                .path("{scope}/registries/{registry}/stages/{stage}")
                .queryParam("objectId", "{objectId}")
                .build(scopeName, registryName, stageName, objectId);
    }
}
