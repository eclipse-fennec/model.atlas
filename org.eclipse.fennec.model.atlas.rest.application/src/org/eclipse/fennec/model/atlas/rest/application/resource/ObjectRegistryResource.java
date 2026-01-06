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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadataContainer;
import org.eclipse.fennec.model.atlas.model.scope.Scope;
import org.eclipse.fennec.model.atlas.model.scope.StageTransition;
import org.eclipse.fennec.model.atlas.runtime.RequireRuntime;
import org.eclipse.fennec.model.atlas.schema.registry.api.SchemaRegistryService;
import org.eclipse.fennec.model.atlas.scope.ScopeCollector;
import org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * REST API for managing Storage Objects within scopes.
 * Provides endpoints for CRUD operations on storage objects with schema validation.
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
	ResourceSet resourceSet;

	@Reference
	private ScopeCollector scopeCollector;
	
	@Reference
	private ManagementFactory mgmtFactory;
	
	private final List<String> supportedMediaTypes;

	@Context
	private HttpHeaders headers;

	@QueryParam("mediaType")
	private String mediaType;
	
	@Activate
	public ObjectRegistryResource(@Reference SupportedMediatype types) {		
		supportedMediaTypes = new ArrayList<>(types.getSupportedMediaTypes());
		supportedMediaTypes.add(MediaType.APPLICATION_XML);
		supportedMediaTypes.add("application/xmi");
		supportedMediaTypes.add("application/uml");
	}

	// Track all SchemaRegistryService instances by registry name
    private Map<String, SchemaRegistryService> schemaRegistries = new ConcurrentHashMap<>();

    @Reference(
        cardinality = ReferenceCardinality.MULTIPLE,
        policy = ReferencePolicy.DYNAMIC
    )
    public void bindSchemaRegistry(SchemaRegistryService service, Map<String, Object> props) {
        String registryName = service.getRegistryName();
        if (registryName != null) {
            schemaRegistries.put(registryName, service);
        }
    }

    public void unbindSchemaRegistry(SchemaRegistryService service, Map<String, Object> props) {
        String registryName = service.getRegistryName();
        if (registryName != null) {
            schemaRegistries.remove(registryName);
        }
    }



	// ======================
	// Storage Objects
	// ======================
	
	/**
	 * List all objects in the final/released stage for this scope and registry.
	 * Respects hierarchical visibility, including objects from parent scopes' final stages.
	 *
	 * @param scopeName the scope name
	 * @param registryName the registry name
	 * @return List of ObjectMetadata objects
	 */
	@GET
	@Produces
	@Operation(
			summary = "List objects in the final stage for provided scope and registry",
			description = "List all objects in the final stage for this scope and registry, including objects from parent scopes",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Objects retrieved successfully",
							content = @Content(schema = @Schema( type = "array", implementation = ObjectMetadata.class))
							),
					@ApiResponse(responseCode = "404", description = "Scope not found"),
					@ApiResponse(responseCode = "500", description = "Internal server error")
			}
			)
	public Response listObjectsInFinalStage(
			@Parameter(description = "The scope name", required = true)
			@PathParam("scopeName") String scopeName,
			@Parameter(description = "The registry name", required = true)
			@PathParam("registryName") String registryName) {
		
		checkContentType();

		EObjectWorkflowService<?> workflowService = getObjectRegistryServiceByScope(scopeName);
		if(workflowService == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		try {
			List<ObjectMetadata> objectsMetadata = workflowService.listInFinalStageForRegistry(registryName);
			ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
			container.getMetadata().addAll(objectsMetadata);		
			return Response.status(Response.Status.OK).entity(container).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	/**
	 * List the metadata for all storage objects within a specific registry.
	 *
	 * @param scopeName the scope name
	 * @param stageName the stage name
	 * @param registryName the registry name
	 * @return List of StorageObjectMetadata objects
	 */
	@GET
	@Path("/stages/{stageName}")
	@Produces
	@Operation(
			summary = "List objects in the provided scope, registry and stage",
			description = "List the metadata for all storage objects within a specific registry and stage. "
					+ "Optionally accepts an objectId; if provided, the ObjectMetadata for the specific object will be returned, if found. "
					+ "Optionally accepts a name filter; if provided and no objectId has been provided, a serch by name is done. "
					+ "The name filter accepts wildcard, but no wildcard as first character and the search is case-insensitive."
					+ "When the objectId is specified, the lookup respects hierarchical visibility.",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Objects retrieved successfully",
							content = @Content(schema =  @Schema(implementation = ObjectMetadata.class))
					),
					@ApiResponse(responseCode = "204", description = "Stored object not found, or registry or stage not available for the scope"),
					@ApiResponse(responseCode = "404", description = "Scope not found"),
					@ApiResponse(responseCode = "500", description = "Internal server error")
			}
	)
	public Response listObjectsInRegistry(
			@Parameter(description = "The scope name", required = true)
			@PathParam("scopeName") String scopeName,
			@Parameter(description = "The registry name", required = true)
			@PathParam("registryName") String registryName,
			@Parameter(description = "The stage name", required = true)
			@PathParam("stageName") String stageName,
			@Parameter(description = "Exact id of the of the object to retrieve")
			@QueryParam("objectId") String objectId,
			@Parameter(description = "Object name filter (supports wildcards, e.g., Billing*)")
			@QueryParam("name") String name) {
		
		EObjectWorkflowService<?> workflowService = getObjectRegistryServiceByScope(scopeName);
		if(workflowService == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		try {
			if(objectId != null) {
				ObjectMetadata metadata = workflowService.getFromStageForRegistry(stageName, registryName, objectId);
				if(metadata == null) {
					return Response.status(Response.Status.NO_CONTENT).build();
				} else {
					return Response.status(Response.Status.OK).entity(metadata).build();
				}
			} else if(name != null) {
				List<ObjectMetadata> objectsMetadata = workflowService.listInStageForRegistryByName(stageName, registryName, name);
				if(objectsMetadata.isEmpty()) {
					return Response.status(Response.Status.NO_CONTENT).build();
				}
				ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
				container.getMetadata().addAll(objectsMetadata);		
				return Response.status(Response.Status.OK).entity(container).build();
			} else {
				List<ObjectMetadata> objectsMetadata = workflowService.listInStageForRegistry(stageName, registryName);
				if(objectsMetadata.isEmpty()) {
					return Response.status(Response.Status.NO_CONTENT).build();
				}
				ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
				container.getMetadata().addAll(objectsMetadata);		
				return Response.status(Response.Status.OK).entity(container).build();
			}

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	/**
	 * Create a storage object with schema validation.
	 *
	 * @param scopeName the scope name
	 * @param stageName the stage name
	 * @param registryName the registry name
	 * @param objectId the object identifier
	 * @param schemaNsUri the namespace URI of the schema to validate against (required)
	 * @param object the storage object content
	 * @return StorageObjectMetadata
	 */
	@SuppressWarnings("unchecked")
	@POST
	@PUT
	@Path("/stages/{stageName}/{objectId}")
	@Consumes
	@Produces
	@Operation(
			summary = "Create an object in the specified registry of the specified scope",
			description = "Create a storage object. The object must conform to a schema known to the ModelAtlas." +
					"Returns 201 Created for new objects, 200 OK for updates.",
			responses = {
					@ApiResponse(
							responseCode = "201",
							description = "Object created successfully",
							content = @Content(schema = @Schema(implementation = ObjectMetadata.class))
					),
					@ApiResponse(
							responseCode = "200",
							description = "Object updated successfully",
							content = @Content(schema = @Schema(implementation = ObjectMetadata.class))
					),
					@ApiResponse(responseCode = "400", description = "Schema not found, or validation failed"),
					@ApiResponse(responseCode = "404", description = "Scope not found"),
					@ApiResponse(responseCode = "409", description = "Object with same id already exists and override option not set to true"),
					@ApiResponse(responseCode = "415", description = "Unsupported media type"),
					@ApiResponse(responseCode = "500", description = "Internal server error")
			}
	)
	public Response createObject(
			@Parameter(description = "The scope name", required = true)
			@PathParam("scopeName") String scopeName,
			@Parameter(description = "The registry name", required = true)
			@PathParam("registryName") String registryName,
			@Parameter(description = "The stage name", required = true)
			@PathParam("stageName") String stageName,
			@Parameter(description = "The object identifier", required = true)
			@PathParam("objectId") String objectId,
			@Parameter(description = "Human-readable name for the object")
			@QueryParam("name") String name,
			@Parameter(description = "Object version")
			@QueryParam("version") String version,
			@Parameter(description = "Override option. If set to true and the object already exists, an update will be made. If set to false and the object already exists, it will result in a 409", required = false)
			@QueryParam("override") boolean override,
			@RequestBody(description = "The storage object content", required = true,
			content = @Content(schema = @Schema(implementation = EObject.class)))
			EObject object) {
		
		checkContentType();
		
		// Get schema registry for validation
        SchemaRegistryService schemaRegistry = schemaRegistries.get(registryName);
        if (schemaRegistry == null) {
            return Response.status(Status.BAD_REQUEST)
                .entity("Unknown or unconfigured registry: " + registryName)
                .build();
        }

        // Validate object type
        if (!schemaRegistry.isCompatible(object.eClass())) {
            return Response.status(Status.BAD_REQUEST)
                .entity(String.format(
                    "Object type %s not compatible with registry %s (expects %s)",
                    EcoreUtil.getURI(object.eClass()),
                    registryName,
                    EcoreUtil.getURI(schemaRegistry.getRootEClass())
                ))
                .build();
        }

		EObjectWorkflowService<EObject> workflowService = (EObjectWorkflowService<EObject>) getObjectRegistryServiceByScope(scopeName);
		if(workflowService == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		try {
			ObjectMetadata existingMetadata = workflowService.getFromStageForRegistry(stageName, registryName, objectId);
			if(existingMetadata != null) {
				if(!override) {
					return Response.status(Response.Status.CONFLICT).build();
				} else {
					ObjectMetadata metadata = workflowService.updateInStageForRegistry(stageName, registryName, object, objectId, version)
		    			    .getValue();
		    		    return Response.status(Response.Status.OK)
		    		    		.header("Location", "/".concat(scopeName).concat("/registries/").concat(registryName).concat("/stages/").concat(stageName).concat("?objectId=").concat(objectId))
		    				    .entity(metadata)
		    				    .build();
				}				
			}
			ObjectMetadata metadata = mgmtFactory.createObjectMetadata();
			metadata.setObjectId(objectId);
			metadata.setObjectName(name);
			metadata.setUploadTime(Instant.now());
			metadata.setRole(stageName);
			metadata.setScope(scopeName);
			metadata.setVersion(version);
			metadata.setObjectType(EcoreUtil.getURI(object.eClass()).toString());

			metadata = workflowService.uploadToStageForRegistry(stageName, registryName, object, metadata).getValue();
			return Response
					.status(Response.Status.CREATED)
					.header("Location", "/".concat(scopeName).concat("/registries/").concat(registryName).concat("/stages/").concat(stageName).concat("?objectId=").concat(objectId))
					.entity(metadata)
					.build();
		} catch (WebApplicationException e) {
			// WebApplicationException already has the correct status code, rethrow it
			throw e;
		} catch(Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	/**
	 * Get the raw content of a storage object.
	 *
	 * @param scopeName the scope name
	 * @param stageName the stage name
	 * @param registryName the registry name
	 * @param objectId the object identifier
	 * @return Storage object content in requested format
	 */
	@GET
	@Path("/stages/{stageName}/content")
	@Produces
	@Operation(
			summary = "Get storage object content",
			description = "Retrieve the raw content of a storage object. " +
					"The Accept header can be used to request content transformation.",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Object content retrieved successfully"
					),
					@ApiResponse(responseCode = "204", description = "Object not found"),
					@ApiResponse(responseCode = "404", description = "Scope not found"),
					@ApiResponse(responseCode = "406", description = "Requested format not supported"),
					@ApiResponse(responseCode = "500", description = "Internal server error")
			}
	)
	public Response getObjectContent(
			@Parameter(description = "The scope name", required = true)
			@PathParam("scopeName") String scopeName,
			@Parameter(description = "The registry name", required = true)
			@PathParam("registryName") String registryName,
			@Parameter(description = "The stage name", required = true)
			@PathParam("stageName") String stageName,
			@Parameter(description = "The object identifier", required = true)
			@QueryParam("objectId") String objectId) {
		checkContentType();
		
		EObjectWorkflowService<?> workflowService = getObjectRegistryServiceByScope(scopeName);
		if(workflowService == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		try {
			EObject eObject = workflowService.getContentFromStageForRegistry(stageName, registryName, objectId);
			if(eObject == null) {
				return Response.status(Response.Status.NO_CONTENT).build();
			}
			return Response.status(Response.Status.OK).entity(eObject).build();

		} catch(Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}		
	}
	
	/**
	 * Get the raw content of a storage object.
	 *
	 * @param scopeName the scope name
	 * @param stageName the stage name
	 * @param registryName the registry name
	 * @param objectId the object identifier
	 * @return Storage object content in requested format
	 */
	@SuppressWarnings("unchecked")
	@PUT
	@POST
	@Path("/stages/{stageName}/content")
	@Produces
	@Operation(
			summary = "Update storage object content",
			description = "Replace the content of an existing object. " +
					"Fails if the stage is read-only (e.g. released) or if scope, registry, stage or object cannot be found.",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Object content updated successfully"
					),
					@ApiResponse(responseCode = "204", description = "Object not found"),
					@ApiResponse(responseCode = "400", description = "If updated object is not compliant with schema"),
					@ApiResponse(responseCode = "404", description = "Scope not found"),
					@ApiResponse(responseCode = "403", description = "Stage is read-only or Package is only present in a parent scope final stage and so it's read-only"),
					@ApiResponse(responseCode = "415", description = "Requested format not supported"),
					@ApiResponse(responseCode = "500", description = "Internal server error")
			}
	)
	public Response updateObjectContent(
			@Parameter(description = "The scope name", required = true)
			@PathParam("scopeName") String scopeName,
			@Parameter(description = "The registry name", required = true)
			@PathParam("registryName") String registryName,
			@Parameter(description = "The stage name", required = true)
			@PathParam("stageName") String stageName,
			@Parameter(description = "The updated version", required = true)
			@QueryParam("version") String version,
			@Parameter(description = "The object identifier", required = true)
			@QueryParam("objectId") String objectId,
			@RequestBody(description = "The new object content", required = true,
			content = @Content(schema = @Schema(implementation = EObject.class)))
			EObject eObject) {
		
		checkContentType();
		
		// Get schema registry for validation
        SchemaRegistryService schemaRegistry = schemaRegistries.get(registryName);
        if (schemaRegistry == null) {
            return Response.status(Status.BAD_REQUEST)
                .entity("Unknown or unconfigured registry: " + registryName)
                .build();
        }

        // Validate object type
        if (!schemaRegistry.isCompatible(eObject.eClass())) {
            return Response.status(Status.BAD_REQUEST)
                .entity(String.format(
                    "Object type %s not compatible with registry %s (expects %s)",
                    EcoreUtil.getURI(eObject.eClass()),
                    registryName,
                    EcoreUtil.getURI(schemaRegistry.getRootEClass())
                ))
                .build();
        }
		
		EObjectWorkflowService<EObject> workflowService = (EObjectWorkflowService<EObject>) getObjectRegistryServiceByScope(scopeName);
		Scope scope = getScopeByScopeName(scopeName);
		if(workflowService == null || scope == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		try {			
			if(!scope.getWritableStages().contains(stageName)) {
				return Response.status(Response.Status.FORBIDDEN).build();
			}
			ObjectMetadata existingMetadata = workflowService.getFromStageForRegistry(stageName, registryName, objectId);
			if(existingMetadata == null) {
				return Response.status(Response.Status.NO_CONTENT).build();
			}
			//			We might want to check if the metadata is read only (e.g. if it was retrieved from a parent final stage
			if(existingMetadata.isIsReadOnly()) {
				return Response.status(Response.Status.FORBIDDEN).build();
			}

			ObjectMetadata metadata = workflowService.updateInStageForRegistry(stageName, registryName, eObject, objectId, version).getValue();			
			return Response.status(Response.Status.OK).entity(metadata).build();

		} catch(Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	

	/**
	 * Delete a storage object.
	 *
	 * @param scopeName the scope name
	 * @param stageName the stage name
	 * @param registryName the registry name
	 * @param objectId the object identifier
	 * @return 200 on success
	 */
	@DELETE
	@Path("/stages/{stageName}")
	@Operation(
			summary = "Delete storage object",
			description = "Delete a storage object from the registry and stage. Fails if the stage is read-only.",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "Object deleted successfully"
					),
					@ApiResponse(responseCode = "403", description = "Stage is read-only or Object is only present in a parent scope final stage and so it's read-only"),
					@ApiResponse(responseCode = "404", description = "Scope not found"),
					@ApiResponse(responseCode = "204", description = "Object not found"),
					@ApiResponse(responseCode = "500", description = "Internal server error")
			}
	)
	public Response deleteObject(
			@Parameter(description = "The scope name", required = true)
			@PathParam("scopeName") String scopeName,
			@Parameter(description = "The registry name", required = true)
			@PathParam("registryName") String registryName,
			@Parameter(description = "The stage name", required = true)
			@PathParam("stageName") String stageName,
			@Parameter(description = "The object identifier", required = true)
			@QueryParam("objectId") String objectId) {

		EObjectWorkflowService<?> workflowService =  getObjectRegistryServiceByScope(scopeName);
		Scope scope = getScopeByScopeName(scopeName);
		if(workflowService == null || scope == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		try {			
			if(!scope.getWritableStages().contains(stageName)) {
				return Response.status(Response.Status.FORBIDDEN).build();
			}
			ObjectMetadata existingMetadata = workflowService.getFromStageForRegistry(stageName, registryName, objectId);
			if(existingMetadata == null) {
				return Response.status(Response.Status.NO_CONTENT).build();
			} 
			if(existingMetadata.isIsReadOnly()) {
				return Response.status(Response.Status.FORBIDDEN).build();
			}

			boolean deleted = workflowService.deleteFromStageForRegistry(stageName, registryName, objectId).getValue();	
			if(deleted) return Response.status(Response.Status.OK).build();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

		} catch(Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}
	
	/**
	 * Transition a package from one stage to another.
	 *
	 * @param scopeName the scope name
	 * @param stageName the source stage name
	 * @param registryName the registry name
	 * @param transitionRequest the transition request containing nsUri and targetStage
	 * @return Updated SchemaPackage metadata
	 */
	@POST
	@Path("/stages/{stageName}/actions/transition")
	@Consumes
	@Produces
	@Operation(
			summary = "Transition object between stages of the same registry and scope",
			description = "Move an object from the current stage to a target stage within the same scope and registry. " +
					"Validates that the transition is allowed based on stage rules.",
					responses = {
							@ApiResponse(
									responseCode = "200",
									description = "Object transitioned successfully",
									content = @Content(schema = @Schema(implementation = ObjectMetadata.class))
									),
							@ApiResponse(responseCode = "400", description = "Invalid transition or missing parameters"),
							@ApiResponse(responseCode = "404", description = "Scope not found"),
							@ApiResponse(responseCode = "204", description = "Object not found in source stage"),
							@ApiResponse(responseCode = "500", description = "Internal server error")
			}
			)
	public Response transitionObject(
			@Parameter(description = "The scope name", required = true)
			@PathParam("scopeName") String scopeName,
			@Parameter(description = "The registry name", required = true)
			@PathParam("registryName") String registryName,
			@Parameter(description = "The source stage name", required = true)
			@PathParam("stageName") String stageName,
			@RequestBody(
					description = "Transition request with objectId and targetStage", 
					required = true, 
					content = @Content(schema = @Schema(implementation = StageTransition.class)))
			StageTransition transitionRequest) {
		
		checkContentType();
		
		EObjectWorkflowService<?> workflowService = getObjectRegistryServiceByScope(scopeName);
		if(workflowService == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		try {
			String objectId = transitionRequest.getObjectId();
			ObjectMetadata existingMetadata = workflowService.getFromStageForRegistry(stageName, registryName, objectId);
			if(existingMetadata == null) {
				return Response.status(Response.Status.NO_CONTENT).build();
			}
			if(existingMetadata.isIsReadOnly()) {
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
			if(!workflowService.isTransitionAllowed(stageName, transitionRequest.getTargetStage())) {
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
			ObjectMetadata metadata = workflowService.transitionToStageForRegistry(objectId, stageName, transitionRequest.getTargetStage(), registryName);
			return Response.status(Response.Status.OK).entity(metadata).build();
		} catch(Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}
	
	/**
	 * Check and set the content type based on Accept header or mediaType query parameter.
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

	private EObjectWorkflowService<?> getObjectRegistryServiceByScope(String scope) {
		return scopeCollector.getWorkflowServiceByScope(scope);
	}

	private Scope getScopeByScopeName(String scope) {
		return scopeCollector.getWorkflowScopeByName(scope);
	}

	
}
