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

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.runtime.RequireRuntime;
import org.osgi.service.component.annotations.Component;
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
@Component(service = SchemaPackagesResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/{scopeName}/schema")
@Tag(name = "Schema Management", description = "CRUD operations for schema packages")
public class SchemaPackagesResource {

	@Context
	private HttpHeaders headers;

	// TODO: Inject schema package service
	// @Reference
	// private SchemaPackageService schemaPackageService;

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
		description = "List all packages in the final/released stage for this scope, including packages from parent scopes",
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
		// TODO: Implement released package listing with hierarchical visibility
		return Response.status(Response.Status.NOT_IMPLEMENTED).build();
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
		// TODO: Implement package listing with optional nsUri/name filtering
		// TODO: Handle hierarchical visibility for nsUri lookup
		return Response.status(Response.Status.NOT_IMPLEMENTED).build();
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
		// TODO: Validate nsUri parameter
		// TODO: Check uniqueness across visibility chain
		// TODO: Create package and return metadata with Location header
		return Response.status(Response.Status.NOT_IMPLEMENTED).build();
	}

	/**
	 * Get the content of a SchemaPackage in a specific format.
	 *
	 * @param scopeName the scope name
	 * @param stageName the stage name
	 * @param nsUri the namespace URI (required)
	 * @return Schema package content in requested format
	 */
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
		return Response.status(Response.Status.NOT_IMPLEMENTED).build();
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
			@ApiResponse(responseCode = "403", description = "Stage is read-only"),
			@ApiResponse(responseCode = "404", description = "Package not found"),
			@ApiResponse(responseCode = "405", description = "Method not allowed for this stage"),
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
		// TODO: Check if stage is writable
		// TODO: Update package content
		// TODO: Return updated metadata
		return Response.status(Response.Status.NOT_IMPLEMENTED).build();
	}

	/**
	 * Delete a SchemaPackage.
	 *
	 * @param scopeName the scope name
	 * @param stageName the stage name
	 * @param nsUri the namespace URI (required)
	 * @return 204 No Content on success
	 */
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
			@ApiResponse(responseCode = "403", description = "Stage is read-only"),
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
		// TODO: Check if stage is writable
		// TODO: Delete package
		return Response.status(Response.Status.NOT_IMPLEMENTED).build();
	}

	// ======================
	// Lifecycle Actions
	// ======================

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
		@RequestBody(description = "Transition request with nsUri and targetStage", required = true)
		Object transitionRequest) {
		// TODO: Parse transition request (nsUri, targetStage)
		// TODO: Verify package is in source stage
		// TODO: Validate transition is allowed
		// TODO: Move package to target stage
		// TODO: Return updated metadata
		return Response.status(Response.Status.NOT_IMPLEMENTED).build();
	}
}
