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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadataContainer;
import org.eclipse.fennec.model.atlas.rest.model.StageTransitionRequest;
import org.eclipse.fennec.model.atlas.runtime.RequireRuntime;
import org.eclipse.fennec.model.atlas.registry.api.ConflictPolicy;
import org.eclipse.fennec.model.atlas.registry.api.ObjectAddress;
import org.eclipse.fennec.model.atlas.registry.api.PackageAddress;
import org.eclipse.fennec.model.atlas.registry.api.PackageSearch;
import org.eclipse.fennec.model.atlas.registry.api.PackageSearchResult;
import org.eclipse.fennec.model.atlas.registry.api.PackageWrite;
import org.eclipse.fennec.model.atlas.registry.api.RegistryAccessException;
import org.eclipse.fennec.model.atlas.registry.api.RegistryReader;
import org.eclipse.fennec.model.atlas.registry.api.RegistryWriter;
import org.eclipse.fennec.model.atlas.registry.api.WriteOutcome;
import org.eclipse.fennec.model.atlas.registry.api.WriteResult;
import org.eclipse.fennec.model.atlas.rest.application.exception.EndpointFailures;
import org.eclipse.fennec.model.atlas.rest.application.filter.ObjectMetadataResponseFilter;
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
    private ManagementFactory mgmtFactory;
    
    @Reference
    private RegistryWriter registryWriter;

    @Reference
    private RegistryReader registryReader;

    @Context
    private ContainerRequestContext requestContext;

    @Context
    private HttpHeaders headers;
    

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

        try {
            List<ObjectMetadata> objectsMetadata = registryReader.listAll(scopeName, registryReader.schemaRegistry(scopeName));
            if (objectsMetadata.isEmpty())
                return Response.status(Response.Status.NO_CONTENT).build();
            ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
            container.getMetadata().addAll(objectsMetadata);
            return Response.status(Response.Status.OK).entity(container).header("Content-Type", ResourceSupport.resolvedMediaType(requestContext)).build();
        } catch (RegistryAccessException e) {
            return ResourceSupport.refusal(e);
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

        try {
            if (nsUri != null) {
                // Stage-free final-stage metadata for one package (hierarchy-aware), mirroring the
                // stage-explicit listing's nsUri branch — used by the client's resolve() (P5-7).
                ObjectMetadata metadata = registryReader.finalPackageMetadata(scopeName, nsUri).orElse(null);
                if (metadata == null) {
                    return Response.status(Response.Status.NO_CONTENT).build();
                }
                ObjectMetadataResponseFilter.attach(requestContext, metadata,
                        ObjectMetadataResponseFilter.CacheTarget.METADATA);
                return Response.status(Response.Status.OK).entity(metadata)
                        .header("Content-Type", ResourceSupport.resolvedMediaType(requestContext)).build();
            }
            List<ObjectMetadata> objectsMetadata = registryReader.listFinal(scopeName, registryReader.schemaRegistry(scopeName));
            if (objectsMetadata.isEmpty())
                return Response.status(Response.Status.NO_CONTENT).build();
            ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
            container.getMetadata().addAll(objectsMetadata);
            return Response.status(Response.Status.OK).entity(container).header("Content-Type", ResourceSupport.resolvedMediaType(requestContext)).build();
        } catch (RegistryAccessException e) {
            return ResourceSupport.refusal(e);
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

        try {
            if (nsUri != null) {
                ObjectMetadata metadata = registryReader.packageMetadata(new PackageAddress(scopeName, stageName, nsUri))
                        .orElse(null);
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
                List<ObjectMetadata> objectsMetadata = registryReader.listByName(scopeName, registryReader.schemaRegistry(scopeName), stageName,
                        name);
                if (objectsMetadata.isEmpty()) {
                    return Response.status(Response.Status.NO_CONTENT).build();
                }
                ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
                container.getMetadata().addAll(objectsMetadata);
                return Response.status(Response.Status.OK).entity(container).header("Content-Type", ResourceSupport.resolvedMediaType(requestContext)).build();
            } else {
                List<ObjectMetadata> objectsMetadata = registryReader.list(scopeName, registryReader.schemaRegistry(scopeName), stageName);
                if (objectsMetadata.isEmpty()) {
                    return Response.status(Response.Status.NO_CONTENT).build();
                }
                ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
                container.getMetadata().addAll(objectsMetadata);
                return Response.status(Response.Status.OK).entity(container).header("Content-Type", ResourceSupport.resolvedMediaType(requestContext)).build();
            }
        } catch (RegistryAccessException e) {
            return ResourceSupport.refusal(e);
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
    @POST
    @PUT
    @Path("/stages/{stageName}")
    @Consumes
    @Produces
    @Operation(summary = "Create a new schema package", description = "Create a new SchemaPackage in the specified stage. Checks for uniqueness based on nsUri. "
            + "If nsUri is not provided, the URI from the EPackage will be used. If provided, it must match the EPackage's nsURI. "
            + "The version is taken from the ?version parameter if given, otherwise from the package's own Version annotation, otherwise from the last segment of the nsURI when that segment looks like a version. An explicit ?version is always honoured.", responses = {
                    @ApiResponse(responseCode = "201", description = "Package created successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
                    @ApiResponse(responseCode = "400", description = "Invalid package data, missing required parameters, nsUri mismatch, or version incompatibility"),
                    @ApiResponse(responseCode = "400", description = "Scope not available, schema registry not available for scope, stage not available for registry or not a valid stage"),
                    @ApiResponse(responseCode = "403", description = "The existing package is read-only, or its stage is final for the registry and so its packages cannot be updated"),
                    @ApiResponse(responseCode = "409", description = "Package with nsUri already exists"),
                    @ApiResponse(responseCode = "415", description = "Unsupported media type"),
                    @ApiResponse(responseCode = "500", description = "Internal server error") })
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
    public Response createPackage(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "The namespace URI of the package. If not provided, uses the EPackage's nsURI. If provided, must match the EPackage's nsURI.", required = false) @QueryParam("nsUri") String nsUri,
            @Parameter(description = "Human-readable name for the package") @QueryParam("name") String name,
            @Parameter(description = "Package version, always honoured as given. If omitted, the version the package declares in its Version annotation is used; failing that, the last segment of the nsURI when it is version-shaped (so a year or spec number in the nsURI is not mistaken for one).", required = false) @QueryParam("version") String version,
            @Parameter(description = "Overwrite option. If true and a Package with the same uri already exists, it updates it. ", required = false) @QueryParam("overwrite") boolean overwrite,
            @Parameter(description = "Assert that this package may be published to a DCAT portal. Recorded in the metadata as the 'dcat' property. "
                    + "On create, absent means false. On an overwrite, absent leaves the stored flag untouched — only an explicit value changes it.", required = false) @QueryParam("dcat") Boolean dcat,
            @RequestBody(description = "The schema package content", required = true, content = @Content(schema = @Schema(implementation = EPackage.class))) EPackage ePackage) {

        try {
            WriteResult result = registryWriter.createPackage(new PackageWrite(scopeName, stageName, ePackage, nsUri,
                    name, version, dcat, overwrite ? ConflictPolicy.OVERWRITE : ConflictPolicy.FAIL,
                    ResourceSupport.ifMatch(headers, ObjectMetadataResponseFilter.CacheTarget.CONTENT)));
            ObjectMetadata metadata = result.metadata();
            Response.ResponseBuilder rb = Response
                    .status(result.outcome() == WriteOutcome.CREATED ? Response.Status.CREATED : Response.Status.OK)
                    .location(packageLocation(scopeName, stageName, ePackage.getNsURI()))
                    .entity(metadata).header("Content-Type", ResourceSupport.resolvedMediaType(requestContext));
            ObjectMetadataResponseFilter.attach(requestContext, metadata,
                    ObjectMetadataResponseFilter.CacheTarget.METADATA);
            return rb.build();
        } catch (RegistryAccessException e) {
            return ResourceSupport.refusal(e);
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
                    @ApiResponse(responseCode = "412", description = "A fingerprint was given and the package at this location is a different model version"),
                    @ApiResponse(responseCode = "500", description = "Internal server error") })
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
    public Response getPackageContent(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "The namespace URI of the package", required = true) @QueryParam("nsUri") String nsUri,
            @Parameter(description = "Optional precondition: the model fingerprint the caller expects. "
                    + "Omitted, the request behaves exactly as before.") @QueryParam("fingerprint") String fingerprint) {

        try {
            requireNsUri(nsUri);
            nsUri = URI.decode(nsUri);
            ObjectMetadata contentMetadata = registryReader
                    .packageMetadata(new PackageAddress(scopeName, stageName, nsUri)).orElse(null);
            if (contentMetadata == null) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            Response mismatch = refuseOnFingerprintMismatch(fingerprint, contentMetadata, nsUri);
            if (mismatch != null) {
                return mismatch;
            }
            EPackage ePackage = (EPackage) registryReader.content(new ObjectAddress(scopeName,
                    registryReader.schemaRegistry(scopeName), stageName, contentMetadata.getObjectId())).orElse(null);
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

        } catch (RegistryAccessException e) {
            return ResourceSupport.refusal(e);
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
                    @ApiResponse(responseCode = "412", description = "A fingerprint was given and the package at this location is a different model version"),
                    @ApiResponse(responseCode = "500", description = "Internal server error") })
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
    public Response getPackageContentFromFinalStage(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The namespace URI of the package", required = true) @QueryParam("nsUri") String nsUri,
            @Parameter(description = "Optional precondition: the model fingerprint the caller expects. "
                    + "Omitted, the request behaves exactly as before.") @QueryParam("fingerprint") String fingerprint) {

        try {
            requireNsUri(nsUri);
            nsUri = URI.decode(nsUri);
            ObjectMetadata contentMetadata = registryReader.finalPackageMetadata(scopeName, nsUri).orElse(null);
            if (contentMetadata == null) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            Response mismatch = refuseOnFingerprintMismatch(fingerprint, contentMetadata, nsUri);
            if (mismatch != null) {
                return mismatch;
            }
            Optional<EObject> ePackageContent = registryReader.finalContent(scopeName,
                    registryReader.schemaRegistry(scopeName), contentMetadata.getObjectId());
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

        } catch (RegistryAccessException e) {
            return ResourceSupport.refusal(e);
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
    @PUT
    @POST
    @Path("/stages/{stageName}/content")
    @Consumes
    @Produces
    @Operation(summary = "Update package content", description = "Replace the content of an existing SchemaPackage. "
            + "Fails if the stage is read-only (e.g., Released). "
            + "If nsUri is not provided, the URI from the EPackage will be used. If provided, it must match the EPackage's nsURI. "
            + "The version is taken from the ?version parameter if given, otherwise from the package's own Version annotation, otherwise from the last segment of the nsURI when that segment looks like a version. An explicit ?version is always honoured.", responses = {
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
            @Parameter(description = "The updated version, always honoured as given. If omitted, the version the package declares in its Version annotation is used; failing that, the last segment of the nsURI when it is version-shaped.", required = false) @QueryParam("version") String version,
            @Parameter(description = "The namespace URI of the package. If not provided, uses the EPackage's nsURI. If provided, must match the EPackage's nsURI.", required = false) @QueryParam("nsUri") String nsUri,
            @RequestBody(description = "The new schema package content", required = true, content = @Content(schema = @Schema(implementation = EPackage.class))) EPackage ePackage) {

        try {
            WriteResult result = registryWriter.updatePackage(new PackageWrite(scopeName, stageName, ePackage, nsUri,
                    null, version, null, null,
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
     * Only {@code dcat} for now. It stays a typed parameter rather than a generic
     * {@code property=key=value} so that a request naming an unknown key is refused instead of
     * stored: the type of a value is the business of whoever knows what the key means, and a
     * generic editor knows nothing. (It is no longer about storage - a property value is text
     * since issue #354, and the publisher parses the flag.) The per-model DCAT metadata of §6
     * joins this list the same way, one typed parameter at a time.
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
            Map.entry("lastChangeTime", "maintained by the server"),
            Map.entry("diagnostics", "written by their producer through the diagnostics API"));

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

            WriteResult result = registryWriter.updatePackageProperties(
                    new PackageAddress(scopeName, stageName, nsUri),
                    Map.of(WorkflowConstants.DCAT_PUBLISH_METADATA_PROPERTY, Boolean.toString(dcat)),
                    // metadata state, not content: the precondition belongs over the metadata validator
                    ResourceSupport.ifMatch(headers, ObjectMetadataResponseFilter.CacheTarget.METADATA));
            if (result.outcome() == WriteOutcome.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Schema %s is not in stage %s of scope %s", nsUri, stageName, scopeName))
                        .build();
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
            throw EndpointFailures.propagate(e);
        }
    }

    /**
     * Evaluate the optional {@code fingerprint} read precondition (#274).
     * <p>
     * Addressing stays {@code (scope, [stage], nsUri)}; the fingerprint only asserts <em>which
     * model version</em> the caller expects to find there. Several versions of one nsURI can be
     * live at once, one per stage, and a transition can move them between the moment a client
     * listed a version and the moment it fetches one — so a pinned read must either return the
     * version asked for or fail, never a different one.
     * <p>
     * A stored object that carries no fingerprint fails the precondition too: the caller asked for
     * a specific version and the server cannot show that this is it. Saying so beats answering
     * with content whose identity is unknown.
     *
     * @param requested the fingerprint the caller pinned, or {@code null}/blank for no precondition
     * @param metadata  the metadata of the object that was resolved
     * @param nsUri     the namespace URI, for the message
     * @return a 412 response when the precondition fails, or {@code null} when it passes or none
     *         was given
     */
    static Response refuseOnFingerprintMismatch(String requested, ObjectMetadata metadata, String nsUri) {
        if (requested == null || requested.isBlank()) {
            return null;
        }
        String actual = metadata == null ? null : metadata.getFingerprint();
        if (requested.equals(actual)) {
            return null;
        }
        String message = actual == null || actual.isBlank()
                ? String.format("%s: the stored object reports no model fingerprint, so '%s' cannot be confirmed",
                        nsUri, requested)
                : String.format("%s: expected model version '%s' but this location holds '%s'", nsUri, requested,
                        actual);
        return Response.status(Response.Status.PRECONDITION_FAILED).entity(message).build();
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
            + "Fails if the stage is read-only. A stage gate may refuse the delete, for example while instances "
            + "depend on the schema; the refusal is answered with 409 and its diagnostics are recorded on the "
            + "package. force=true deletes anyway and records the consequence on the dependents the gate named.", responses = {
                    @ApiResponse(responseCode = "200", description = "Package deleted successfully"),
                    @ApiResponse(responseCode = "403", description = "Stage is read-only or Package is only present in a parent scope final stage and so it's read-only"),
                    @ApiResponse(responseCode = "400", description = "Scope not available, schema registry not available for scope, stage not available for registry or not a valid stage"),
                    @ApiResponse(responseCode = "204", description = "Package not found"),
                    @ApiResponse(responseCode = "409", description = "A stage gate refused the delete; the package keeps its stage. The body is its unchanged metadata, whose diagnostics carry the gate's findings. Retry with force=true to override.",
                            content = @Content(schema = @Schema(implementation = ObjectMetadata.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error") })
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
    public Response deletePackage(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "The namespace URI of the package to delete", required = true) @QueryParam("nsUri") String nsUri,
            @Parameter(description = "Delete although a stage gate refuses it; the gate's findings are recorded on the dependents instead") @QueryParam("force") @DefaultValue("false") boolean force) {

        try {
            WriteResult result = registryWriter.deletePackage(new PackageAddress(scopeName, stageName, nsUri), force,
                    ResourceSupport.ifMatch(headers, ObjectMetadataResponseFilter.CacheTarget.CONTENT));
            if (result.outcome() == WriteOutcome.NOT_FOUND) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            return Response.status(Response.Status.OK).build();

        } catch (RegistryAccessException e) {
            return ResourceSupport.refusal(e);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            // a gate's refusal is answered with the package's metadata as the 409 body (issue #295)
            Response refused = GateRefusals.conflict(e, registryReader, scopeName, requestContext);
            if (refused != null) {
                return refused;
            }
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
                    @ApiResponse(responseCode = "409", description = "Either the target stage already holds a different package under this objectId (an objectId is unique per stage, so promoting onto an earlier copy of the same package is allowed; taking the id over from another package requires overwrite=true) - then the body is the plain error document - "
                            + "or a stage gate refused the transition because the package does not hold up in the target stage. Then the body is the package's unchanged metadata from the source stage, whose diagnostics carry the gate's findings and what to change before retrying",
                            content = @Content(schema = @Schema(implementation = ObjectMetadata.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error") })
    @ResourceOption(key = CodecOptions.CODEC_ID_KEY_MODE, value = "FEATURE_ONLY")
    public Response transitionPackage(
            @Parameter(description = "The scope name", required = true) @PathParam("scopeName") String scopeName,
            @Parameter(description = "The source stage name", required = true) @PathParam("stageName") String stageName,
            @Parameter(description = "Overwrite option. If the target stage holds a different package under this objectId, false (the default) rejects the transition with a 409 and true replaces that package. Replacing an earlier copy of the package being promoted needs no flag - that is what a promotion is for", required = false) @QueryParam("overwrite") boolean overwrite,
            @RequestBody(description = "Transition request with objectId and targetStage", required = true, content = @Content()) StageTransitionRequest transitionRequest) {

        try {
            WriteResult result = registryWriter.transitionPackage(scopeName, stageName,
                    transitionRequest.getObjectId(), transitionRequest.getTargetStage(), overwrite,
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
            Response refused = GateRefusals.conflict(e, registryReader, scopeName, requestContext);
            if (refused != null) {
                return refused;
            }
            throw EndpointFailures.propagate(e);
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

        PackageSearchResult result = registryReader.searchPackages(scopeName, new PackageSearch(stage, nsUri,
                nsUriExact, name, prefix, classifier, featureName, featureType, featureNameTypePair, limit, offset));
        if (result.hits().isEmpty()) {
            return Response.noContent().build();
        }
        ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
        container.getMetadata().addAll(result.hits());
        return Response.ok(container)
            .header("X-Total-Count", result.totalHits())
            .header("X-Offset", offset)
            .header("X-Limit", limit)
            .build();
    }

    /**
     * The {@code Location} of a package in a stage, absolute and built from the request's
     * base URI. A {@code /scope/...} path would lose the application's base path, as Jersey
     * resolves it against the host (#335). The nsURI goes into the query encoded.
     */
    private java.net.URI packageLocation(String scopeName, String stageName, String nsUri) {
        return requestContext.getUriInfo().getBaseUriBuilder()
                .path("{scope}/schema/stages/{stage}")
                .queryParam("nsUri", "{nsUri}")
                .build(scopeName, stageName, nsUri);
    }

    private static void requireNsUri(String nsUri) {
        if (nsUri == null || nsUri.isBlank()) {
            throw new IllegalArgumentException("Query parameter nsUri is required");
        }
    }

}
