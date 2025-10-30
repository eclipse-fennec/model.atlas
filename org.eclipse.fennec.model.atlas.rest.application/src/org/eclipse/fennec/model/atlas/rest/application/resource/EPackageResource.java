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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype;
import org.eclipse.fennec.model.atlas.rest.model.EPackageInfo;
import org.eclipse.fennec.model.atlas.rest.model.EPackageListResponse;
import org.eclipse.fennec.model.atlas.rest.model.RestFactory;
import org.eclipse.fennec.model.atlas.runtime.RequireRuntime;
import org.eclipse.fennec.model.atlas.service.EPackageService;
import org.osgi.service.component.annotations.Activate;
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
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * REST API for CRUD operations on EPackages.
 * Provides endpoints to create, read, update, and delete EMF EPackages at runtime.
 *
 * @author Jürgen Albert
 * @since 1.0
 */
@RequireRuntime
@JakartarsResource()
@JakartarsName("EPackageResource")
@Component(service = EPackageResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/epackages")
@Tag(name = "EPackage Management", description = "CRUD operations for EMF EPackages")
public class EPackageResource {

	@Reference
	private EPackageService ePackageService;

	@Reference
	private RestFactory restFactory;

	private final List<String> supportedMediaTypes;

	@Context
	private HttpHeaders headers;

	@QueryParam("mediaType")
	private String mediaType;

	@Activate
	public EPackageResource(@Reference SupportedMediatype types) {
		supportedMediaTypes = new ArrayList<>(types.getSupportedMediaTypes());
		supportedMediaTypes.add(MediaType.APPLICATION_XML);
		supportedMediaTypes.add("application/xmi");
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

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "application/xmi" })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "application/xmi" })
	@Operation(
		summary = "Create a new EPackage",
		description = "Register a new EPackage in the system",
		responses = {
			@ApiResponse(
				responseCode = "201",
				description = "EPackage created successfully",
				content = @Content(schema = @Schema(implementation = EPackage.class))
			),
			@ApiResponse(responseCode = "400", description = "Invalid EPackage data"),
			@ApiResponse(responseCode = "409", description = "EPackage with nsUri already exists"),
			@ApiResponse(responseCode = "415", description = "Unsupported media type")
		}
	)
	public Response createEPackage(
		@Parameter(description = "The EPackage to register", required = true)
		EPackage ePackage) {

		checkContentType();

		try {
			Optional<EPackage> registered = ePackageService.registerEPackage(ePackage);

			if (registered.isEmpty()) {
				return Response.status(Status.CONFLICT)
					.entity("EPackage with nsUri '" + ePackage.getNsURI() + "' already exists")
					.type(MediaType.TEXT_PLAIN)
					.build();
			}

			return Response.status(Status.CREATED)
				.header("Location", "/epackages?nsUri=" + ePackage.getNsURI())
				.entity(registered.get())
				.type(mediaType)
				.build();

		} catch (IllegalArgumentException e) {
			return Response.status(Status.BAD_REQUEST)
				.entity(e.getMessage())
				.type(MediaType.TEXT_PLAIN)
				.build();
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "application/xmi" })
	@Operation(
		summary = "Get an EPackage or list all EPackages",
		description = "Get a specific EPackage by nsUri, or list all if no nsUri provided",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "EPackage found or list returned",
				content = @Content(schema = @Schema(implementation = EPackage.class))
			),
			@ApiResponse(responseCode = "204", description = "No content - EPackage not found or no packages registered"),
			@ApiResponse(responseCode = "415", description = "Unsupported media type")
		}
	)
	
	public Response getEPackage(
		@Parameter(description = "The namespace URI of the EPackage to retrieve")
		@QueryParam("nsUri") String nsUri,
		@Parameter(description = "Number of items to skip for pagination")
		@QueryParam("skip") @DefaultValue("0") int skip,
		@Parameter(description = "Maximum number of items to return")
		@QueryParam("limit") @DefaultValue("100") int limit) {

		checkContentType();

		// If nsUri is provided, get specific EPackage
		if (nsUri != null && !nsUri.isEmpty()) {
			Optional<EPackage> ePackage = ePackageService.getEPackage(nsUri);

			if (ePackage.isEmpty()) {
				return Response.status(Status.NO_CONTENT).build();
			}

			return Response.ok(ePackage.get(), mediaType).build();
		}

		// Otherwise, list all EPackages
		List<EPackage> allPackages = ePackageService.listEPackages();

		if (allPackages.isEmpty()) {
			return Response.status(Status.NO_CONTENT).build();
		}

		EPackageListResponse response = createListResponse(allPackages, skip, limit);
		return Response.ok(response, MediaType.APPLICATION_JSON).build();
	}

	@PUT
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "application/xmi" })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "application/xmi" })
	@Operation(
		summary = "Update an existing EPackage",
		description = "Update an EPackage identified by nsUri",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "EPackage updated successfully",
				content = @Content(schema = @Schema(implementation = EPackage.class))
			),
			@ApiResponse(responseCode = "204", description = "No content - EPackage not found"),
			@ApiResponse(responseCode = "400", description = "Invalid EPackage data or nsUri mismatch"),
			@ApiResponse(responseCode = "415", description = "Unsupported media type")
		}
	)
	public Response updateEPackage(
		@Parameter(description = "The namespace URI of the EPackage to update", required = true)
		@QueryParam("nsUri") String nsUri,
		@Parameter(description = "The updated EPackage data", required = true)
		EPackage ePackage) {

		checkContentType();

		if (nsUri == null || nsUri.isEmpty()) {
			return Response.status(Status.BAD_REQUEST)
				.entity("nsUri query parameter is required")
				.type(MediaType.TEXT_PLAIN)
				.build();
		}

		try {
			Optional<EPackage> updated = ePackageService.updateEPackage(nsUri, ePackage);

			if (updated.isEmpty()) {
				return Response.status(Status.NO_CONTENT).build();
			}

			return Response.ok(updated.get(), mediaType).build();

		} catch (IllegalArgumentException e) {
			return Response.status(Status.BAD_REQUEST)
				.entity(e.getMessage())
				.type(MediaType.TEXT_PLAIN)
				.build();
		}
	}

	@DELETE
	@Operation(
		summary = "Delete an EPackage",
		description = "Unregister an EPackage identified by nsUri",
		responses = {
			@ApiResponse(responseCode = "204", description = "EPackage deleted successfully (or didn't exist)")
		}
	)
	public Response deleteEPackage(
		@Parameter(description = "The namespace URI of the EPackage to delete", required = true)
		@QueryParam("nsUri") String nsUri) {

		if (nsUri != null && !nsUri.isEmpty()) {
			ePackageService.unregisterEPackage(nsUri);
		}

		// Always return 204 for idempotent DELETE
		return Response.status(Status.NO_CONTENT).build();
	}

	/**
	 * Create a paginated list response from a list of EPackages.
	 */
	private EPackageListResponse createListResponse(List<EPackage> packages, int skip, int limit) {
		EPackageListResponse response = restFactory.createEPackageListResponse();
		response.setTotal(packages.size());
		response.setSkip(skip);
		response.setLimit(limit);

		packages.stream()
			.skip(skip)
			.limit(limit)
			.forEach(pkg -> {
				EPackageInfo info = restFactory.createEPackageInfo();
				info.setName(pkg.getName());
				info.setNsURI(pkg.getNsURI());
				info.setNsPrefix(pkg.getNsPrefix());
				info.setClassifierCount(pkg.getEClassifiers().size());
				info.setSubPackageCount(pkg.getESubpackages().size());
				response.getPackages().add(info);
			});

		return response;
	}
}
