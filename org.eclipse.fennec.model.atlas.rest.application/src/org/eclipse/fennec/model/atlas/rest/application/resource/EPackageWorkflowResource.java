/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.rest.application.resource;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadataContainer;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.eclipse.fennec.model.atlas.runtime.RequireRuntime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;
import org.osgi.util.promise.Promise;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST resource for EPackage workflow management operations.
 * 
 * <p>This resource provides HTTP endpoints for managing EPackage through the governance workflow,
 * including draft management, approval/rejection, and release operations.
 * All operations are synchronous from the client perspective - we wait for Promise resolution
 * before returning responses.</p>
 * 
 * <h3>Draft Management Endpoints</h3>
 * <ul>
 * <li><strong>POST /packages/workflow/drafts</strong> - Upload new EPackage draft with metadata</li>
 * <li><strong>GET /packages/workflow/drafts</strong> - List all draft EPackages</li>
 * <li><strong>GET /packages/workflow/drafts/{packageNs}</strong> - Get draft metadata by ID</li>
 * <li><strong>GET /packages/workflow/drafts/{packageID}/content</strong> - Get draft EPackage content</li>
 * <li><strong>PUT /packages/workflow/drafts/{packageID}</strong> - Update existing draft EPackage</li>
 * <li><strong>DELETE /packages/workflow/drafts/{packageID}</strong> - Delete draft EPackage</li>
 * </ul>
 * 
 * 
 * <h3>Workflow State Management Endpoints</h3>
 * <ul>
 * <li><strong>POST /epackages//workflow/state/{packageID}/approve</strong> - Approve EPackage for release</li>
 * <li><strong>POST /epackages//workflow/state/{packageID}/reject</strong> - Reject EPackage during review</li>
 * <li><strong>POST /epackages//workflow/state/{packageID}/release</strong> - Release approved EPackage to production</li>
 * </ul>
 * 
 * 
 * <h3>Object Listing Endpoints</h3>
 * <ul>
 * <li><strong>GET /epackages//workflow/approved</strong> - List all approved EPackages</li>
 * <li><strong>GET /epackages//workflow/rejected</strong> - List all rejected EPackages</li>
 * <li><strong>GET /epackages//workflow/released</strong> - List all released EPackages</li>
 * <li><strong>GET /epackages//workflow/{packageID}</strong> - Get EPackage metadata by ID</li>
 * <li><strong>GET /epackages//workflow/{packageID}/content</strong> - Get EPackage content by ID</li>
 * </ul>
 * 
 * <h3>EMF Serialization</h3>
 * <p>All EPackage responses are directly serialized using EMF's XML support.
 * The Jakarta REST infrastructure automatically handles EMF object serialization
 * via registered MessageBodyWriters.</p>
 */
@RequireRuntime
@JakartarsResource()
@JakartarsName("EPackageWorkflowResource")
@Component(name = "EPackageWorkflowResource", service = EPackageWorkflowResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/packages/workflow")
@Tag(name = "EPackage Management", description = "CRUD operations for workflow of EMF EPackages")
public class EPackageWorkflowResource {

	@Reference
	private EObjectWorkflowService<EObject> workflowService;
	
	 @Reference
	 private ManagementFactory mgmtFactory;

	// ======================
	// Draft Management APIs
	// ======================

	
	@POST
	@Path("/drafts")
	@Produces({"application/xmi", MediaType.TEXT_PLAIN})
	@Operation(
			summary = "Upload a new draft for an EPackage",
			description = "Upload an EPackage as a draft",
			responses = {
				@ApiResponse(
					responseCode = "201",
					description = "Draft stored successfully. It returns the ID of the stored draft.",
					content = @Content(mediaType = MediaType.TEXT_PLAIN)
				),
				@ApiResponse(responseCode = "400", description = "IllegalArgumentException occurs"),
				@ApiResponse(responseCode = "500", description = "Other runtime Exception occurs")
			}
		)
	public Response uploadDraft(
			@Parameter(name = "uploadUser", description = "An identifier for the user who uploads the draft", required = true) @QueryParam("uploadUser")  String uploadUser,
			@Parameter(name = "sourceChannel", description = "The source of this draft (e.g. AI generated, file, etc.)", required = true) @QueryParam("sourceChannel") String sourceChannel,
			@Parameter(name = "contentHash", description = "The optional content hash for deduplication checks", required = false) @QueryParam("contentHash")  String contentHash,
			@Parameter(name = "fileExtension", description = "Optional file extension for the draft (e.g. .ecore, .json", required = false)	 @QueryParam("fileExtension") String fileExtension, 
			@RequestBody(description = "The EPackage draft to upload", required = true, content = @Content(schema = @Schema(implementation = EPackage.class)))  EPackage ePackage) {
		try {
			// Validate required parameters
			if (isNull(uploadUser) || uploadUser.trim().isEmpty()) {
				return Response.status(Response.Status.BAD_REQUEST)
						.entity("uploadUser query parameter is required")
						.type(MediaType.TEXT_PLAIN)
						.build();
			}
			if (isNull(sourceChannel) || sourceChannel.trim().isEmpty()) {
				return Response.status(Response.Status.BAD_REQUEST)
						.entity("sourceChannel query parameter is required")
						.type(MediaType.TEXT_PLAIN)
						.build();
			}
			

			// Create metadata object
			ObjectMetadata metadata = mgmtFactory.createObjectMetadata();
			metadata.setUploadUser(uploadUser.trim());
			metadata.setSourceChannel(sourceChannel.trim());
			metadata.setObjectType("EPackage");
			metadata.setUploadTime(Instant.now());
			metadata.setObjectName(ePackage.getName());
			metadata.setStatus(ObjectStatus.DRAFT);

			if (nonNull(contentHash) && !contentHash.trim().isEmpty()) {
				metadata.setContentHash(contentHash.trim());
			}

			if (nonNull(fileExtension) && !fileExtension.trim().isEmpty()) {
				String extension = fileExtension.trim();
				if (!extension.startsWith(".")) {
					extension = "." + extension;
				}
				metadata.getProperties().put("file.extension", extension);
			}

			Promise<String> promise = workflowService.uploadDraft(ePackage, metadata);
			String objectId = promise.getValue(); // Wait for completion
			return Response.status(Response.Status.CREATED)
					.entity(objectId)
					.type(MediaType.TEXT_PLAIN)
					.build();
		} catch (IllegalArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(e.getCause().getMessage())
					.type(MediaType.TEXT_PLAIN)
					.build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Draft upload failed: " + e.getMessage())
					.type(MediaType.TEXT_PLAIN)
					.build();
		}
	}
	

    @GET
    @Path("/drafts")
    @Produces({"application/xmi", MediaType.TEXT_PLAIN})
    @Operation(
			summary = "List all EPackages in draft/review status",
			description = "List all EPackages in draft/review status",
			responses = {
				@ApiResponse(
					responseCode = "200",
					description = "Returns list of draft metadata",
					content = @Content(mediaType = "application/xmi", schema = @Schema(implementation = ObjectMetadataContainer.class))
				),
				@ApiResponse(responseCode = "204", description = "No draft was found", content = @Content(mediaType = MediaType.TEXT_PLAIN)),
				@ApiResponse(responseCode = "500", description = "Exception occurs", content = @Content(mediaType = MediaType.TEXT_PLAIN))
			}
		)
    public Response listDraftObjects() {
        try {
            List<ObjectMetadata> drafts = workflowService.listDraftObjects();
            requireNonNull(drafts, "listDraftObjects() must never return null");
            if(drafts.isEmpty()) {
            	return Response.noContent().build();
            }
            ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
            container.getMetadata().addAll(drafts);
            return Response.ok(container).type("application/xmi").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("List drafts operation failed: " + e.getMessage())
                          .type(MediaType.TEXT_PLAIN)
                          .build();
        }
    }
    
  
    @GET
    @Path("/drafts/nsuri")
    @Produces({"application/xmi", MediaType.TEXT_PLAIN})
    @Operation(
			summary = "Get draft EPackage metadata by packageNsURI",
			description = "Get draft EPackage metadata by packageNsURI",
			responses = {
				@ApiResponse(
					responseCode = "200",
					description = "Returns the metadata of the draft EPackage with the requested packageNsURI",
					content = @Content(mediaType = "application/xmi", schema = @Schema(implementation = ObjectMetadata.class))
				),
				@ApiResponse(responseCode = "404", description = "No draft corresponding to that packageNsURI was found", content = @Content(mediaType = MediaType.TEXT_PLAIN)),
				@ApiResponse(responseCode = "500", description = "Exception occurs", content = @Content(mediaType = MediaType.TEXT_PLAIN))
			}
		)
    public Response getDraft(@Parameter(name = "packageNsURI", description = "The namespace URI of the EPackage draft to look for", required = true) @QueryParam("packageNsURI") String packageNsURI) {
        try {
            ObjectMetadata metadata = workflowService.getDraft(packageNsURI);
            
            if (nonNull(metadata)) {
                return Response.ok(metadata).type("application/xmi").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Draft not found: " + packageNsURI)
                              .type(MediaType.TEXT_PLAIN)
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Get draft operation failed: " + e.getMessage())
                          .type(MediaType.TEXT_PLAIN)
                          .build();
        }
    }
    

    @GET
    @Path("/drafts/nsuri/content")
    @Produces({"application/xmi", MediaType.TEXT_PLAIN})
    @Operation(
			summary = "Get draft EPackage by packageNsURI",
			description = "Get draft EPackage by packageNsURI",
			responses = {
				@ApiResponse(
					responseCode = "200",
					description = "Returns the EPackage draft with the requested packageNsURI",
					content = @Content(mediaType = "application/xmi", schema = @Schema(implementation = EPackage.class))
				),
				@ApiResponse(responseCode = "404", description = "No draft corresponding to that packageNsURI was found", content = @Content(mediaType = MediaType.TEXT_PLAIN)),
				@ApiResponse(responseCode = "500", description = "Exception occurs", content = @Content(mediaType = MediaType.TEXT_PLAIN))
			}
		)
    public Response getDraftContent(@Parameter(name = "packageNsURI", description = "The packageNsURI of the EPackage draft to look for", required = true) @QueryParam("packageNsURI") String packageNsURI) {
        try {
            EObject content = workflowService.getDraftContent(packageNsURI);
            
            if (nonNull(content)) {
                return Response.ok(content).type("application/xmi").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Draft content not found: " + packageNsURI)
                              .type(MediaType.TEXT_PLAIN)
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Get draft content operation failed: " + e.getMessage())
                          .type(MediaType.TEXT_PLAIN)
                          .build();
        }
    }
    
    @PUT
    @Path("/drafts/nsuri")
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(
			summary = "Update the draft with the provided packageNsURI with the provided content",
			description = "Update the EPackage draft with the provided packageNsURI, with the provided new content",
			responses = {
				@ApiResponse(
					responseCode = "200",
					description = "The draft has been successfully updated",
					content = @Content(mediaType = MediaType.TEXT_PLAIN)
				),
				@ApiResponse(responseCode = "404", description = "No draft corresponding to that packageNsURI was found", content = @Content(mediaType = MediaType.TEXT_PLAIN)),
				@ApiResponse(responseCode = "500", description = "Exception occurs", content = @Content(mediaType = MediaType.TEXT_PLAIN))
			}
		)
    public Response updateDraft(
    		@Parameter(name = "packageNsURI", description = "The packageNsURI of the EPackage draft to look for", required = true) @QueryParam("packageNsURI") String packageNsURI,
    		@RequestBody(description = "The updated EPackage content", required = true, content = @Content(schema = @Schema(implementation = EPackage.class))) EPackage updatedEPackage) {
        try {
            Promise<Void> promise = workflowService.updateDraft(packageNsURI, updatedEPackage);
            promise.getValue(); // Wait for completion
            
            return Response.ok("Draft updated successfully").type(MediaType.TEXT_PLAIN).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                          .entity("Draft not found: " + packageNsURI)
                          .type(MediaType.TEXT_PLAIN)
                          .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Update draft operation failed: " + e.getMessage())
                          .type(MediaType.TEXT_PLAIN)
                          .build();
        }
    }

    /**
     * Delete a draft object.
     * 
     * @param objectId the object identifier
     * @return HTTP 204 if deleted, 404 if not found, or 500 for errors
     */
    @DELETE
    @Path("/drafts/nsuri/")
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(
			summary = "Delete the EPackage draft with the provided packageNsURI",
			description = "Delete the EPackage draft with the provided packageNsURI",
			responses = {
				@ApiResponse(
					responseCode = "204",
					description = "The draft has been successfully deleted"
				),
				@ApiResponse(responseCode = "404", description = "No draft corresponding to that packageNsURI was found", content = @Content(mediaType = MediaType.TEXT_PLAIN)),
				@ApiResponse(responseCode = "500", description = "Exception occurs", content = @Content(mediaType = MediaType.TEXT_PLAIN))
			}
		)
    public Response deleteDraft(@Parameter(name = "packageNsURI", description = "The packageNsURI of the EPackage draft to look for", required = true) @PathParam("packageNsURI") String packageNsURI) {
        try {
            Promise<Boolean> promise = workflowService.deleteDraft(packageNsURI);
            Boolean success = promise.getValue(); // Wait for completion
            
            if (success) {
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Draft not found: " + packageNsURI)
                              .type(MediaType.TEXT_PLAIN)
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Delete draft operation failed: " + e.getMessage())
                          .type(MediaType.TEXT_PLAIN)
                          .build();
        }
    }
    
    
    // ======================
 	// State Management APIs
 	// ======================

    @POST
    @Path("/state/nsuri/approve")
    @Produces({"application/xmi", MediaType.TEXT_PLAIN})
    @Operation(
			summary = "Approve the EPackage with the provided packageNsURI for release",
			description = "Approve the EPackage with the provided packageNsURI for release",
			responses = {
				@ApiResponse(
					responseCode = "200",
					description = "Approval was succesfull and updated EPackage metadata are returned",
					content = @Content(mediaType = "application/xmi", schema = @Schema(implementation = ObjectMetadata.class))
				),
				@ApiResponse(responseCode = "404", description = "No EPackage corresponding to that packageNsURI was found", content = @Content(mediaType = MediaType.TEXT_PLAIN)),
				@ApiResponse(responseCode = "500", description = "Exception occurs", content = @Content(mediaType = MediaType.TEXT_PLAIN))
			}
		)
    public Response approveObject(
    		@Parameter(name = "packageNsURI", description = "The packageNsURI of the EPackage to look for", required = true) @QueryParam("packageNsURI") String packageNsURI,
            @Parameter(name = "reviewUser", description = "An identifier for the user who made the review", required = true) @QueryParam("reviewUser") String reviewUser,
            @Parameter(name = "approvalReason", description = "A short and optional motivation for approval", required = false) @QueryParam("approvalReason") String approvalReason) {
        try {
            if (isNull(reviewUser)) {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("reviewUser query parameter is required")
                              .type(MediaType.TEXT_PLAIN)
                              .build();
            }
            
            ObjectMetadata updatedMetadata = workflowService.approveObject(packageNsURI, reviewUser, approvalReason);
            
            if (nonNull(updatedMetadata)) {
                return Response.ok(updatedMetadata).type("application/xmi").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Object not found: " + packageNsURI)
                              .type(MediaType.TEXT_PLAIN)
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Approval operation failed: " + e.getMessage())
                          .type(MediaType.TEXT_PLAIN)
                          .build();
        }
    }
    
  
    @POST
    @Path("/state/nsuri/reject")
    @Produces({"application/xmi", MediaType.TEXT_PLAIN})
    @Operation(
			summary = "Reject the EPackage with the provided packageNsURI during review",
			description = "Reject the EPackage with the provided packageNsURI during review",
			responses = {
				@ApiResponse(
					responseCode = "200",
					description = "Rejection was succesfull and updated EPackage metadata are returned",
					content = @Content(mediaType = "application/xmi", schema = @Schema(implementation = ObjectMetadata.class))
				),
				@ApiResponse(responseCode = "404", description = "No EPackage corresponding to that packageNsURI was found", content = @Content(mediaType = MediaType.TEXT_PLAIN)),
				@ApiResponse(responseCode = "500", description = "Exception occurs", content = @Content(mediaType = MediaType.TEXT_PLAIN))
			}
		)
    public Response rejectObject(
    		@Parameter(name = "packageNsURI", description = "The packageNsURI of the EPackage to look for", required = true) @QueryParam("packageNsURI") String packageNsURI,
            @Parameter(name = "reviewUser", description = "An identifier for the user who made the review", required = true) @QueryParam("reviewUser") String reviewUser,
            @Parameter(name = "rejectionReason", description = "A short and optional motivation for rejection", required = false) @QueryParam("rejectionReason") String rejectionReason)  {
        try {
            if (isNull(reviewUser) || isNull(rejectionReason)) {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("reviewUser and rejectionReason query parameters are required")
                              .type(MediaType.TEXT_PLAIN)
                              .build();
            }
            
            ObjectMetadata updatedMetadata = workflowService.rejectObject(packageNsURI, reviewUser, rejectionReason);
            
            if (nonNull(updatedMetadata)) {
                return Response.ok(updatedMetadata).type("application/xmi").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Object not found: " + packageNsURI)
                              .type(MediaType.TEXT_PLAIN)
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Rejection operation failed: " + e.getMessage())
                          .type(MediaType.TEXT_PLAIN)
                          .build();
        }
    }
    
    /**
     * Release an approved object to production storage after compliance checks.
     * 
     * @param objectId the object identifier
     * @param releaseNotes notes for the release
     * @param requireComplianceCheck whether to require compliance check before release
     * @return HTTP 200 with updated metadata, 404 if not found, or 500 for errors
     */
    @POST
    @Path("/state/nsuri/release")
    @Produces({"application/xmi", MediaType.TEXT_PLAIN})
    @Operation(
			summary = "Release the EPackage with the provided packageNsURI to production",
			description = "Release the EPackage with the provided packageNsURI to production",
			responses = {
				@ApiResponse(
					responseCode = "200",
					description = "Release was succesfull and updated EPackage metadata are returned",
					content = @Content(mediaType = "application/xmi", schema = @Schema(implementation = ObjectMetadata.class))
				),
				@ApiResponse(responseCode = "404", description = "No EPackage corresponding to that packageNsURI was found", content = @Content(mediaType = MediaType.TEXT_PLAIN)),
				@ApiResponse(responseCode = "500", description = "Exception occurs", content = @Content(mediaType = MediaType.TEXT_PLAIN))
			}
		)
    public Response releaseObject(
    		
    		@Parameter(name = "packageNsURI", description = "The packageNsURI of the EPackage to look for", required = true) @QueryParam("packageNsURI") String packageNsURI,
            @Parameter(name = "releaseNotes", description = "Optional release notes", required = false) @QueryParam("releaseNotes") String releaseNotes
             ) {
        try {
            boolean requireCheck = false;
            
            ObjectMetadata updatedMetadata = workflowService.releaseObject(packageNsURI, releaseNotes, requireCheck);
            
            if (nonNull(updatedMetadata)) {
                return Response.ok(updatedMetadata).type("application/xmi").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Object not found: " + packageNsURI)
                              .type(MediaType.TEXT_PLAIN)
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Release operation failed: " + e.getMessage())
                          .type(MediaType.TEXT_PLAIN)
                          .build();
        }
    }
    
    // ======================
    // Object Listing APIs
    // ======================

   
    @GET
    @Path("/approved")
    @Produces({"application/xmi", MediaType.TEXT_PLAIN})
    @Operation(
			summary = "List all approved EPackages ready for release.",
			description = "List all approved EPackages ready for release.",
			responses = {
				@ApiResponse(
					responseCode = "200",
					description = "Request was succesfull and approved EPackages metadata are returned",
					content = @Content(mediaType = "application/xmi", schema = @Schema(implementation = ObjectMetadataContainer.class))
				),
				@ApiResponse(responseCode = "500", description = "Exception occurs", content = @Content(mediaType = MediaType.TEXT_PLAIN))
			}
		)
    public Response listApprovedObjects() {
        try {
            List<ObjectMetadata> approved = workflowService.listApprovedObjects();
            requireNonNull(approved, "listApprovedObjects() must never return null");
            ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
            container.getMetadata().addAll(approved);
            return Response.ok(container).type("application/xmi").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("List approved objects failed: " + e.getMessage())
                          .type(MediaType.TEXT_PLAIN)
                          .build();
        }
    }

    @GET
    @Path("/all")
    @Produces({"application/xmi", MediaType.TEXT_PLAIN})
    @Operation(
			summary = "List all EPackages across all workflow states (draft, rejected, approved, released)",
			description = "List all EPackages across all workflow states (draft, rejected, approved, released)",
			responses = {
				@ApiResponse(
					responseCode = "200",
					description = "Request was succesfull and all EPackages metadata are returned",
					content = @Content(mediaType = "application/xmi", schema = @Schema(implementation = ObjectMetadataContainer.class))
				),
				@ApiResponse(responseCode = "500", description = "Exception occurs", content = @Content(mediaType = MediaType.TEXT_PLAIN))
			}
		)
    public Response listAllObjects() {
        try {
            ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
            
            // Collect objects from all workflow states with null safety checks
            List<ObjectMetadata> drafts = workflowService.listDraftObjects();
            requireNonNull(drafts, "listDraftObjects() must never return null");
            
            List<ObjectMetadata> rejected = workflowService.listRejectedObjects();
            requireNonNull(rejected, "listRejectedObjects() must never return null");
            
            List<ObjectMetadata> approved = workflowService.listApprovedObjects();
            requireNonNull(approved, "listApprovedObjects() must never return null");
            
            List<ObjectMetadata> released = workflowService.listReleasedObjects();
            requireNonNull(released, "listReleasedObjects() must never return null");
            
            // Add all objects to the container
            container.getMetadata().addAll(drafts);
            container.getMetadata().addAll(rejected);
            container.getMetadata().addAll(approved);
            container.getMetadata().addAll(released);
            
            return Response.ok(container).type("application/xmi").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("List all objects failed: " + e.getMessage())
                          .type(MediaType.TEXT_PLAIN)
                          .build();
        }
    }

 
    @GET
    @Path("/rejected")
    @Produces({"application/xmi", MediaType.TEXT_PLAIN})
    @Operation(
			summary = "List all rejected EPackages",
			description = "List all rejected EPackages",
			responses = {
				@ApiResponse(
					responseCode = "200",
					description = "Request was succesfull and rejected EPackages metadata are returned",
					content = @Content(mediaType = "application/xmi", schema = @Schema(implementation = ObjectMetadataContainer.class))
				),
				@ApiResponse(responseCode = "500", description = "Exception occurs", content = @Content(mediaType = MediaType.TEXT_PLAIN))
			}
		)
    public Response listRejectedObjects() {
        try {
            List<ObjectMetadata> rejected = workflowService.listRejectedObjects();
            requireNonNull(rejected, "listRejectedObjects() must never return null");
            ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
            container.getMetadata().addAll(rejected);
            return Response.ok(container).type("application/xmi").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("List rejected objects failed: " + e.getMessage())
                          .type(MediaType.TEXT_PLAIN)
                          .build();
        }
    }

 
    @GET
    @Path("/released")
    @Produces({"application/xmi", MediaType.TEXT_PLAIN})
    @Operation(
			summary = "List all released EPackages",
			description = "List all released EPackages",
			responses = {
				@ApiResponse(
					responseCode = "200",
					description = "Request was succesfull and released EPackages metadata are returned",
					content = @Content(mediaType = "application/xmi", schema = @Schema(implementation = ObjectMetadataContainer.class))
				),
				@ApiResponse(responseCode = "500", description = "Exception occurs", content = @Content(mediaType = MediaType.TEXT_PLAIN))
			}
		)
    public Response listReleasedObjects() {
        try {
            List<ObjectMetadata> released = workflowService.listReleasedObjects();
            requireNonNull(released, "listReleasedObjects() must never return null");
            ObjectMetadataContainer container = mgmtFactory.createObjectMetadataContainer();
            container.getMetadata().addAll(released);
            return Response.ok(container).type("application/xmi").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("List released objects failed: " + e.getMessage())
                          .type(MediaType.TEXT_PLAIN)
                          .build();
        }
    }


    @GET
    @Path("")
    @Produces({"application/xmi", MediaType.TEXT_PLAIN})
    @Operation(
			summary = "Get the EPackage metadata with the provided packageNsURI",
			description = "Get the EPackage metadata with the provided packageNsURI",
			responses = {
				@ApiResponse(
					responseCode = "200",
					description = "Request was succesfull and EPackage metadata are returned",
					content = @Content(mediaType = "application/xmi", schema = @Schema(implementation = ObjectMetadata.class))
				),
				@ApiResponse(responseCode = "404", description = "No EPackage corresponding to that packageNsURI was found", content = @Content(mediaType = MediaType.TEXT_PLAIN)),
				@ApiResponse(responseCode = "500", description = "Exception occurs", content = @Content(mediaType = MediaType.TEXT_PLAIN))
			}
		)
    public Response getObject(@Parameter(name = "packageNsURI", description = "The packageNsURI of the EPackage to look for", required = true) @QueryParam("packageNsURI") String packageNsURI) {
        try {
            ObjectMetadata metadata = workflowService.getObject(packageNsURI);
            
            if (nonNull(metadata)) {
                return Response.ok(metadata).type("application/xmi").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Object not found: " + packageNsURI)
                              .type(MediaType.TEXT_PLAIN)
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Get object operation failed: " + e.getMessage())
                          .type(MediaType.TEXT_PLAIN)
                          .build();
        }
    }

    @GET
    @Path("/content")
    @Produces({"application/xmi", MediaType.TEXT_PLAIN})
    @Operation(
			summary = "Get the EPackage with the provided packageNsURI",
			description = "Get the EPackage with the provided packageNsURI",
			responses = {
				@ApiResponse(
					responseCode = "200",
					description = "Request was succesfull and EPackage is returned",
					content = @Content(mediaType = "application/xmi", schema = @Schema(implementation = EPackage.class))
				),
				@ApiResponse(responseCode = "404", description = "No EPackage corresponding to that packageNsURI was found", content = @Content(mediaType = MediaType.TEXT_PLAIN)),
				@ApiResponse(responseCode = "500", description = "Exception occurs", content = @Content(mediaType = MediaType.TEXT_PLAIN))
			}
		)
    public Response getObjectContent(@Parameter(name = "packageNsURI", description = "The packageNsURI of the EPackage to look for", required = true) @QueryParam("packageNsURI") String packageNsURI) {
        try {
            Object content = workflowService.getObjectContent(packageNsURI);
            
            if (nonNull(content)) {
                return Response.ok(content).type("application/xmi").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Object content not found: " + packageNsURI)
                              .type(MediaType.TEXT_PLAIN)
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Get object content operation failed: " + e.getMessage())
                          .type(MediaType.TEXT_PLAIN)
                          .build();
        }
    }

 
    @PUT
    @Path("")
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(
			summary = "Update an existing EPackage with the provided packageNsURI (works across all workflow states)",
			description = "Update an existing EPackage with the provided packageNsURI (works across all workflow states)",
			responses = {
				@ApiResponse(
					responseCode = "200",
					description = "Request was succesfull",
					content = @Content(mediaType = MediaType.TEXT_PLAIN)
				),
				@ApiResponse(responseCode = "404", description = "No EPackage corresponding to that packageNsURI was found", content = @Content(mediaType = MediaType.TEXT_PLAIN)),
				@ApiResponse(responseCode = "500", description = "Exception occurs", content = @Content(mediaType = MediaType.TEXT_PLAIN))
			}
		)
    public Response updateObject(
    		@Parameter(name = "packageNsURI", description = "The packageNsURI of the EPackage to look for", required = true) @QueryParam("packageNsURI") String packageNsURI, 
    		@Parameter(schema = @Schema(implementation = EPackage.class), name = "updateEPackage", description = "The update EPackage content", required = true) @RequestBody EPackage updateEPackage) {
        try {
            Promise<Void> promise = workflowService.updateObject(packageNsURI, updateEPackage);
            promise.getValue(); // Wait for completion
            
            return Response.ok("Object updated successfully")
                          .type(MediaType.TEXT_PLAIN)
                          .build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                          .entity("Object not found: " + packageNsURI)
                          .type(MediaType.TEXT_PLAIN)
                          .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Update object operation failed: " + e.getMessage())
                          .type(MediaType.TEXT_PLAIN)
                          .build();
        }
    }

  
    @DELETE
    @Path("")
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(
			summary = "Delete the EPackage with the provided packageNsURI (works across all workflow states)",
			description = "Delete the EPackage with the provided packageNsURI (works across all workflow states)",
			responses = {
				@ApiResponse(
					responseCode = "204",
					description = "The EPackage has been successfully deleted",
					content = @Content(mediaType = MediaType.TEXT_PLAIN)
				),
				@ApiResponse(responseCode = "404", description = "No EPackage corresponding to that packageNsURI was found", content = @Content(mediaType = MediaType.TEXT_PLAIN)),
				@ApiResponse(responseCode = "500", description = "Exception occurs", content = @Content(mediaType = MediaType.TEXT_PLAIN))
			}
		)
    public Response deleteObject(@Parameter(name = "packageNsURI", description = "The packageNsURI of the EPackage to look for", required = true) @QueryParam("packageNsURI") String packageNsURI) {
        try {
            Promise<Boolean> promise = workflowService.deleteObject(packageNsURI);
            Boolean success = promise.getValue(); // Wait for completion
            
            if (success) {
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Object not found: " + packageNsURI)
                              .type(MediaType.TEXT_PLAIN)
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Delete object operation failed: " + e.getMessage())
                          .type(MediaType.TEXT_PLAIN)
                          .build();
        }
    }

}
