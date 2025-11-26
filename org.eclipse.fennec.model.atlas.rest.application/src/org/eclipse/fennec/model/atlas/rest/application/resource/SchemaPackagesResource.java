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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadataContainer;
import org.eclipse.fennec.model.atlas.model.scope.Scope;
import org.eclipse.fennec.model.atlas.model.scope.StageTransition;
import org.eclipse.fennec.model.atlas.runtime.RequireRuntime;
import org.eclipse.fennec.model.atlas.scope.ScopeCollector;
import org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService;
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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST API for managing SchemaPackages within scopes.
 * Provides endpoints for CRUD operations on schema packages with stage-based lifecycle management.
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

	@Context
	private HttpHeaders headers;
	
	 @Reference
	 private ScopeCollector scopeCollector;
	 
	 @Reference
	 private ManagementFactory mgmtFactory;

	// ======================
	// Released Stage APIs (default)
	// ======================

	/**
	 * List all packages in the final/released stage for this scope.
	 * Respects hierarchical visibility, including packages from parent scopes' released stages.
	 *
	 * @param scopeName the scope name
	 * @return List of SchemaPackage metadata objects
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(
		summary = "List released packages in scope",
		description = "List all packages in the final stage for this scope, including packages from parent scopes",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Packages retrieved successfully",
				content = @Content(mediaType = MediaType.APPLICATION_JSON)
			),
			@ApiResponse(responseCode = "404", description = "Scope not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
		}
	)
	public Response listReleasedPackages(
		@Parameter(description = "The scope name", required = true)
		@PathParam("scopeName") String scopeName) {

		EObjectWorkflowService<?> workflowService = getEObjectWorkflowServiceByScope(scopeName);
		if(workflowService == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		List<ObjectMetadata> objectsMetadata = workflowService.listInFinalStage();
		ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
		container.getMetadata().addAll(objectsMetadata);		
		return Response.status(Response.Status.OK).entity(container).build();
	}

	// ======================
	// Stage-Specific APIs
	// ======================

	/**
	 * List all packages within a specific stage of a scope.
	 * Supports filtering by nsUri or name.
	 *
	 * @param scopeName the scope name
	 * @param stageName the stage name
	 * @param nsUri optional exact namespace URI filter
	 * @param name optional name filter (supports wildcards)
	 * @return List of SchemaPackage metadata objects, or single object if nsUri is specified
	 */
	@GET
	@Path("/stages/{stageName}")
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(
		summary = "List packages in a specific stage",
		description = "List all packages within a specific stage, with optional filtering by nsUri or name. " +
		              "Respects hierarchical visibility when nsUri is specified.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Packages retrieved successfully",
				content = @Content(mediaType = MediaType.APPLICATION_JSON)
			),
			@ApiResponse(responseCode = "404", description = "Scope, stage, or package not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
		}
	)
	public Response listPackagesInStage(
		@Parameter(description = "The scope name", required = true)
		@PathParam("scopeName") String scopeName,
		@Parameter(description = "The stage name", required = true)
		@PathParam("stageName") String stageName,
		@Parameter(description = "Exact namespace URI of the package to retrieve")
		@QueryParam("nsUri") String nsUri,
		@Parameter(description = "Package name filter (supports wildcards, e.g., *Billing*)")
		@QueryParam("name") String name) {

		EObjectWorkflowService<?> workflowService = getEObjectWorkflowServiceByScope(scopeName);
		if(workflowService == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		List<ObjectMetadata> objectsMetadata = workflowService.listInStage(stageName);
		ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
		container.getMetadata().addAll(objectsMetadata);		
		return Response.status(Response.Status.OK).entity(container).build();
	}

	/**
	 * Create a new SchemaPackage in the specified stage.
	 *
	 * @param scopeName the scope name
	 * @param stageName the stage name
	 * @param nsUri the namespace URI (required)
	 * @param name optional human-readable name
	 * @param version optional version string
	 * @param ePackage the schema package content
	 * @return SchemaPackage metadata
	 */
	@SuppressWarnings("unchecked")
	@POST
	@Path("/stages/{stageName}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "application/ecore+xml" })
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(
		summary = "Create a new schema package",
		description = "Create a new SchemaPackage in the specified stage. Checks for uniqueness based on nsUri.",
		responses = {
			@ApiResponse(
				responseCode = "201",
				description = "Package created successfully",
				content = @Content(mediaType = MediaType.APPLICATION_JSON)
			),
			@ApiResponse(responseCode = "400", description = "Invalid package data or missing required parameters"),
			@ApiResponse(responseCode = "409", description = "Package with nsUri already exists"),
			@ApiResponse(responseCode = "415", description = "Unsupported media type"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
		}
	)
	public Response createPackage(
		@Parameter(description = "The scope name", required = true)
		@PathParam("scopeName") String scopeName,
		@Parameter(description = "The stage name", required = true)
		@PathParam("stageName") String stageName,
		@Parameter(description = "The namespace URI of the package", required = true)
		@QueryParam("nsUri") String nsUri,
		@Parameter(description = "Human-readable name for the package")
		@QueryParam("name") String name,
		@Parameter(description = "Package version")
		@QueryParam("version") String version,
		@RequestBody(description = "The schema package content", required = true,
		             content = @Content(schema = @Schema(implementation = EPackage.class)))
		EPackage ePackage) {
		//TODO: Validate nsUri parameter
		
		EObjectWorkflowService<EPackage> workflowService = (EObjectWorkflowService<EPackage>) getEObjectWorkflowServiceByScope(scopeName);
		if(workflowService == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		
		try {
			String encodedNsURI = encodePackageNsURI(nsUri);
//			Check uniqueness across visibility chain
			if(workflowService.getFromStage(stageName, encodedNsURI) != null) {
				return Response.status(Response.Status.CONFLICT).build();
			}
//			Create package and return metadata with Location header
			ObjectMetadata metadata = mgmtFactory.createObjectMetadata();
			metadata.setObjectId(encodedNsURI);
			metadata.setObjectName(name);
			metadata.setUploadTime(Instant.now());
			metadata.setRole(stageName);
			metadata.setScope(scopeName);
			metadata.setVersion(version);
			metadata.setObjectRef(ePackage);
			
			metadata = workflowService.uploadToStage(stageName, ePackage, metadata).getValue();
			
			return Response
					.status(Response.Status.NOT_IMPLEMENTED)
					.header("Location", "/".concat(scopeName).concat("/schemas/stages/").concat(stageName).concat("?nsUri=").concat(encodedNsURI))
					.entity(metadata)
					.build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Get the content of a SchemaPackage in a specific format.
	 *
	 * @param scopeName the scope name
	 * @param stageName the stage name
	 * @param nsUri the namespace URI (required)
	 * @return Schema package content in requested format
	 */
	@SuppressWarnings("unchecked")
	@GET
	@Path("/stages/{stageName}/content")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "application/ecore+xml", "application/schema+json" })
	@Operation(
		summary = "Get package content",
		description = "Retrieve the content of a SchemaPackage in the requested format. " +
		              "Respects hierarchical visibility.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Package content retrieved successfully"
			),
			@ApiResponse(responseCode = "404", description = "Package not found"),
			@ApiResponse(responseCode = "406", description = "Requested format not supported"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
		}
	)
	public Response getPackageContent(
		@Parameter(description = "The scope name", required = true)
		@PathParam("scopeName") String scopeName,
		@Parameter(description = "The stage name", required = true)
		@PathParam("stageName") String stageName,
		@Parameter(description = "The namespace URI of the package", required = true)
		@QueryParam("nsUri") String nsUri) {
		// TODO: Find package by nsUri (hierarchical visibility)
		// TODO: Transform to requested format based on Accept header
		// TODO: Return content with appropriate Content-Type
//		This should call getContentFromStage
		EObjectWorkflowService<EPackage> workflowService = (EObjectWorkflowService<EPackage>) getEObjectWorkflowServiceByScope(scopeName);
		if(workflowService == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		try {
			String encodedNsUri = encodePackageNsURI(nsUri);			
			EPackage ePackage = workflowService.getContentFromStage(stageName, encodedNsUri);
			if(ePackage == null) {
				return Response.status(Response.Status.NO_CONTENT).build();
			}
			return Response.status(Response.Status.OK).entity(ePackage).build();
			
		} catch(Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}		
	}

	/**
	 * Update (replace) the content of an existing SchemaPackage.
	 *
	 * @param scopeName the scope name
	 * @param stageName the stage name
	 * @param nsUri the namespace URI (required)
	 * @param ePackage the new schema package content
	 * @return Updated SchemaPackage metadata
	 */
	@SuppressWarnings("unchecked")
	@PUT
	@Path("/stages/{stageName}/content")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "application/ecore+xml" })
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(
		summary = "Update package content",
		description = "Replace the content of an existing SchemaPackage. " +
		              "Fails if the stage is read-only (e.g., Released).",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Package updated successfully",
				content = @Content(mediaType = MediaType.APPLICATION_JSON)
			),
			@ApiResponse(responseCode = "400", description = "Invalid package data"),
			@ApiResponse(responseCode = "403", description = "Stage is read-only or Package is only present in a parent scope final stage and so it's read-only"),
			@ApiResponse(responseCode = "404", description = "Workflow, Scope or Package not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
		}
	)
	public Response updatePackageContent(
		@Parameter(description = "The scope name", required = true)
		@PathParam("scopeName") String scopeName,
		@Parameter(description = "The stage name", required = true)
		@PathParam("stageName") String stageName,
		@Parameter(description = "The namespace URI of the package", required = true)
		@QueryParam("nsUri") String nsUri,
		@RequestBody(description = "The new schema package content", required = true,
		             content = @Content(schema = @Schema(implementation = EPackage.class)))
		EPackage ePackage) {
		EObjectWorkflowService<EPackage> workflowService = (EObjectWorkflowService<EPackage>) getEObjectWorkflowServiceByScope(scopeName);
		Scope scope = getScopeByScopeName(scopeName);
		if(workflowService == null || scope == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		try {			
			if(!scope.getWritableStages().contains(stageName)) {
				return Response.status(Response.Status.FORBIDDEN).build();
			}
			String encodedNsUri = encodePackageNsURI(nsUri);
			ObjectMetadata existingMetadata = workflowService.getFromStage(stageName, encodedNsUri);
			if(existingMetadata == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
//			We might want to check if the metadata is read only (e.g. if it was retrieved from a parent final stage
			if(existingMetadata.isIsReadOnly()) {
				return Response.status(Response.Status.FORBIDDEN).build();
			}
			
			ObjectMetadata metadata = workflowService.updateInStage(stageName, ePackage, encodedNsUri).getValue();			
			return Response.status(Response.Status.OK).entity(metadata).build();
			
		} catch(Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Delete a SchemaPackage.
	 *
	 * @param scopeName the scope name
	 * @param stageName the stage name
	 * @param nsUri the namespace URI (required)
	 * @return 204 No Content on success
	 */
	@SuppressWarnings("unchecked")
	@DELETE
	@Path("/stages/{stageName}")
	@Operation(
		summary = "Delete a package",
		description = "Delete a SchemaPackage from the specified stage. " +
		              "Fails if the stage is read-only.",
		responses = {
			@ApiResponse(
				responseCode = "204",
				description = "Package deleted successfully"
			),
			@ApiResponse(responseCode = "403", description = "Stage is read-only or Package is only present in a parent scope final stage and so it's read-only"),
			@ApiResponse(responseCode = "404", description = "Package not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
		}
	)
	public Response deletePackage(
		@Parameter(description = "The scope name", required = true)
		@PathParam("scopeName") String scopeName,
		@Parameter(description = "The stage name", required = true)
		@PathParam("stageName") String stageName,
		@Parameter(description = "The namespace URI of the package to delete", required = true)
		@QueryParam("nsUri") String nsUri) {
		
		EObjectWorkflowService<EPackage> workflowService = (EObjectWorkflowService<EPackage>) getEObjectWorkflowServiceByScope(scopeName);
		Scope scope = getScopeByScopeName(scopeName);
		if(workflowService == null || scope == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		try {			
			if(!scope.getWritableStages().contains(stageName)) {
				return Response.status(Response.Status.FORBIDDEN).build();
			}
			String encodedNsUri = encodePackageNsURI(nsUri);
			ObjectMetadata existingMetadata = workflowService.getFromStage(stageName, encodedNsUri);
			if(existingMetadata == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			} 
			if(existingMetadata.isIsReadOnly()) {
				return Response.status(Response.Status.FORBIDDEN).build();
			}
			
			boolean deleted = workflowService.deleteFromStage(stageName, encodedNsUri).getValue();	
			if(deleted) return Response.status(Response.Status.NO_CONTENT).build();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			
		} catch(Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	// ======================
	// Lifecycle Actions
	// ======================

	@SuppressWarnings("unchecked")
	/**
	 * Transition a package from one stage to another.
	 *
	 * @param scopeName the scope name
	 * @param stageName the source stage name
	 * @param transitionRequest the transition request containing nsUri and targetStage
	 * @return Updated SchemaPackage metadata
	 */
	@POST
	@Path("/stages/{stageName}/actions/transition")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(
		summary = "Transition package between stages",
		description = "Move a package from the current stage to a target stage. " +
		              "Validates that the transition is allowed based on stage rules.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Package transitioned successfully",
				content = @Content(mediaType = MediaType.APPLICATION_JSON)
			),
			@ApiResponse(responseCode = "400", description = "Invalid transition or missing parameters"),
			@ApiResponse(responseCode = "404", description = "Package not found in source stage"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
		}
	)
	public Response transitionPackage(
		@Parameter(description = "The scope name", required = true)
		@PathParam("scopeName") String scopeName,
		@Parameter(description = "The source stage name", required = true)
		@PathParam("stageName") String stageName,
		@RequestBody(
			description = "Transition request with objectId and targetStage", 
			required = true, 
			content = @Content(schema = @Schema(implementation = StageTransition.class)))
		StageTransition transitionRequest) {
		// Parse transition request (nsUri, targetStage)
		// Verify package is in source stage
		// Validate transition is allowed
		// Move package to target stage
		// Return updated metadata
		EObjectWorkflowService<EPackage> workflowService = (EObjectWorkflowService<EPackage>) getEObjectWorkflowServiceByScope(scopeName);
		if(workflowService == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		try {
			String encodedNsUri = encodePackageNsURI(transitionRequest.getObjectId());
			ObjectMetadata existingMetadata = workflowService.getFromStage(stageName, encodedNsUri);
			if(existingMetadata == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			if(existingMetadata.isIsReadOnly()) {
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
			if(workflowService.isTransitionAllowed(stageName, transitionRequest.getTargetStage())) {
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
			ObjectMetadata metadata = workflowService.transitionToStage(encodedNsUri, stageName, transitionRequest.getTargetStage());
			return Response.status(Response.Status.OK).entity(metadata).build();
		} catch(Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		
		
	}
	
	private EObjectWorkflowService<?> getEObjectWorkflowServiceByScope(String scope) {
		return scopeCollector.getWorkflowServiceByScope(scope);
	}
	
	private Scope getScopeByScopeName(String scope) {
		return scopeCollector.getScopeByName(scope);
	}
	
	private String encodePackageNsURI(String nsUri) throws UnsupportedEncodingException {
		return URLEncoder.encode(
				nsUri, 
                StandardCharsets.UTF_8.toString());
	}
}
