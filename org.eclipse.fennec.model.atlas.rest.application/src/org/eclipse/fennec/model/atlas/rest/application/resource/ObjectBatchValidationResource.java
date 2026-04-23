/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.m2x.ocl.api.OclContext;
import org.eclipse.fennec.m2x.ocl.api.OclEngine;
import org.eclipse.fennec.m2x.ocl.api.OclEvaluationOptions;
import org.eclipse.fennec.m2x.ocl.api.OclParseException;
import org.eclipse.fennec.m2x.ocl.api.OclResult;
import org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype;
import org.eclipse.fennec.model.atlas.runtime.RequireRuntime;
import org.eclipse.fennec.model.atlas.validation.ValidationHelper;
import org.eclipse.fennec.model.atlas.validation.model.cocl.BatchValidationRequest;
import org.eclipse.fennec.model.atlas.validation.model.cocl.COCLFactory;
import org.eclipse.fennec.model.atlas.validation.model.cocl.EObjectValidationResult;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole;
import org.eclipse.fennec.model.atlas.validation.model.cocl.Severity;
import org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResponse;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.workflow.ScopeServiceCollector;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
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
 *
 * @author ilenia
 * @since Mar 16, 2026
 */
@RequireRuntime
@JakartarsResource()
@JakartarsName("ObjectBatchValidationResource")
@Path("/validate/batch")
@Component(name = "ObjectBatchValidationResource", service = ObjectBatchValidationResource.class, scope = ServiceScope.PROTOTYPE)
@Tag(name = "Object Batch Validation Resource", description = "CRUD operations for validating an object against a model atlas schema")
public class ObjectBatchValidationResource {

	private final List<String> supportedMediaTypes;

	private static final String JENA_SCOPE_NAME = "jena";
	private static final String JENA_OCL_REGISTRY_NAME = "cocl";
	private static final String JENA_OCL_STAGE_NAME = "release";

	@Reference
	private ScopeServiceCollector scopeCollector;

	@Context
	private HttpHeaders headers;

	@QueryParam("mediaType")
	private String mediaType;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private OclEngine oclEngine;

	@Reference
	ResourceSet resourceSet;

	@Activate
	public ObjectBatchValidationResource(@Reference SupportedMediatype types) {
		supportedMediaTypes = types.getSupportedMediaTypes();
	}

	@POST
	@Consumes({"application/xmi", "application/xml", MediaType.APPLICATION_JSON})
	@Produces({"application/xmi", MediaType.APPLICATION_JSON})
	@Operation(summary = "Validates the provided objects against their schema and an additional OCLConstraintSet", description = "Validates the provided objects against their schema and an additional OCLConstraintSet, whose id has to be provided.",
	responses = {
			@ApiResponse(responseCode = "200", description = "Object validation was performed. A Response with the ValidationResponse is returned.",
					content = @Content(schema = @Schema(implementation = ValidationResponse.class))),
			@ApiResponse(responseCode = "415", description = "Unsupported media type"),
			@ApiResponse(responseCode = "404", description = "OCLConstraintSet id was not found in C-OCL registry"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public Response validate(
			@RequestBody(description = "The batch validation request", required = true, content = @Content(schema = @Schema(implementation = BatchValidationRequest.class))) BatchValidationRequest validationRequest) {
		try {
			checkContentType();
			if(validationRequest.getValidationObjects().isEmpty()) {
				throw badRequest("No Object to be validated");
			}
//			Check if C-OCL id is there
			if(validationRequest.getCoclId() == null) {
				throw badRequest("No C-OCL id was provided");
			}
			
//			Check if we have a filter
			OclConstraint filterConstraint = validationRequest.getFilterConstraint();
			if(filterConstraint != null) {
				if(!OclRole.REFERENCE_FILTER.equals(filterConstraint.getRole())) {
					throw badRequest(String.format("Provided Filter Constraint is of type %s. Should be of type REFERENCE_FILTER", filterConstraint.getRole()));
				}				
			}
			
//			Retrieve the C-OCL 
			OclConstraintSet constraintSet = resolveConstraintSet(validationRequest.getCoclId());
			List<OclConstraint> constraints = ValidationHelper.filter(constraintSet, c -> c.isActive() && OclRole.VALIDATION.equals(c.getRole()));
			if(constraints.isEmpty()) {
				throw badRequest(String.format("No active OCL Constraint of type VALIDATION found in OClConstraintSet %s", validationRequest.getCoclId()));
			}
			
//			Start processing all the objects
			ValidationResponse response = COCLFactory.eINSTANCE.createValidationResponse();
			response.setRole(OclRole.VALIDATION);
			for(EObject target : validationRequest.getValidationObjects()) {
				org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic parentDiagnostic = COCLFactory.eINSTANCE.createDiagnostic();
				parentDiagnostic.getData().add(target.toString());
				EObject filteredTarget = target;
				if(filterConstraint != null) {
					OclResult result = evaluateConstraint(filterConstraint, target);
					if(result.isSuccess()) {
						boolean isValid = result.getValueAs(Boolean.class);
						if (!isValid) {
							filteredTarget = null;
							parentDiagnostic.getChildren().add(getDiagnostic(Severity.INFO, filterConstraint.getName(), "Object was filtered out by filter constraint"));
						} 
					} else {
						filteredTarget = null;
						parentDiagnostic.getChildren().addAll(getDiagnostics(result.diagnostics()));
					}
				} 
				if(filteredTarget != null) {
					if(!isConstraintSetApplicable(constraintSet, filteredTarget)) {
						parentDiagnostic.getChildren().add(getDiagnostic(Severity.ERROR, constraintSet.getName(), String.format("C-OCL ConstraintSet %s is not compatible with EObject", constraintSet.getName())));
						setParentDiagnosticSeverity(parentDiagnostic);
						response.getDiagnostics().add(parentDiagnostic);
						continue;
					}
					for(OclConstraint constraint : constraints) {
						OclResult result = evaluateConstraint(constraint, filteredTarget);
						if (result.isSuccess()) {
							boolean isValid = result.getValueAs(Boolean.class);
							if (!isValid) {
								parentDiagnostic.getChildren().add(getDiagnostic(constraint));
							}
						} else {
							parentDiagnostic.getChildren().addAll(getDiagnostics(result.diagnostics()));
						}
					}
					Diagnostic emfDiagnostic = Diagnostician.INSTANCE.validate(filteredTarget);
					parentDiagnostic.getChildren().add(getDiagnostics(emfDiagnostic));
				}
				setParentDiagnosticSeverity(parentDiagnostic);
				response.getDiagnostics().add(parentDiagnostic);
			}
			return Response.status(Response.Status.OK).entity(response).header("Content-Type", mediaType).build();
		} catch (WebApplicationException e) {
			throw e;
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	
	

	@POST
	@Path("/filter")
	@Consumes({"application/xmi", "application/xml", MediaType.APPLICATION_JSON})
	@Produces({"application/xmi", MediaType.APPLICATION_JSON})
	@Operation(summary = "Filter the provided objects based on a C-OCL ConstraintSet", description = "Filter the provided objects based on a C-OCL ConstraintSet.",
	responses = {
			@ApiResponse(responseCode = "200", description = "Filtering was performed successfully. A ValidationResponse with the corresponding results and diagnostics is returned.",
					content = @Content(schema = @Schema(implementation = ValidationResponse.class))),
			@ApiResponse(responseCode = "204", description = "If no filter constraint in the C-OCL Constraint Set has been found, or if after performing the filter the original data remains unchanged"),
			@ApiResponse(responseCode = "415", description = "Unsupported media type"),
			@ApiResponse(responseCode = "404", description = "If no EObject has to be validated or if the provided OCLConstraintSet cannot handle the provided EObjects"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public Response filter(
			@RequestBody(description = "The BatchValidationRequest", required = true,
			content = @Content(schema = @Schema(implementation = BatchValidationRequest.class))) BatchValidationRequest validationRequest) {
		try {
			checkContentType();
			if(validationRequest.getValidationObjects().isEmpty()) {
				throw badRequest("No Object to be validated");
			}
//			Check if C-OCL id is there
			if(validationRequest.getCoclId() == null) {
				throw badRequest("No C-OCL id was provided");
			}
//			Retrieve the C-OCL 
			OclConstraintSet constraintSet = resolveConstraintSet(validationRequest.getCoclId());
			requireConstraintSetApplicable(constraintSet, validationRequest.getValidationObjects());
			List<OclConstraint> constraints = ValidationHelper.filter(constraintSet, c -> c.isActive() && OclRole.REFERENCE_FILTER.equals(c.getRole()));
			if(constraints.isEmpty()) {
				return Response.status(Status.NO_CONTENT).build();
			}
			
//			Start processing all the objects
			ValidationResponse response = COCLFactory.eINSTANCE.createValidationResponse();
			response.setRole(OclRole.REFERENCE_FILTER);
			for(EObject target : validationRequest.getValidationObjects()) {
				EObjectValidationResult filteredResult = COCLFactory.eINSTANCE.createEObjectValidationResult();
				filteredResult.getValues().add(EcoreUtil.copy(target));
				org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic parentDiagnostic = COCLFactory.eINSTANCE.createDiagnostic();
				parentDiagnostic.getData().add(target.toString());
				for(OclConstraint constraint : constraints) {
					if(!filteredResult.getValues().isEmpty()) {
						OclResult result = evaluateConstraint(constraint, target);
						if (result.isSuccess()) {
							boolean isValid = result.getValueAs(Boolean.class);
							if (!isValid) {
								parentDiagnostic.getChildren().add(getDiagnostic(Severity.INFO, constraint.getName(), "Object was filtered out by filter constraint"));
								filteredResult.getValues().clear();
							} 
						} else {
							parentDiagnostic.getChildren().addAll(getDiagnostics(result.diagnostics()));
							filteredResult.getValues().clear();
						}
					}					
				}
				setParentDiagnosticSeverity(parentDiagnostic);
				filteredResult.getDiagnostics().add(parentDiagnostic);
				response.getResults().add(filteredResult);
			}			
			long retainedCount = response.getResults().stream().map(r -> (EObjectValidationResult) r).filter(r -> !r.getValues().isEmpty()).count();
			if(retainedCount == validationRequest.getValidationObjects().size()) {
				return Response.status(Status.NO_CONTENT).build();
			}
			return Response.status(Response.Status.OK).entity(response).header("Content-Type", mediaType).build();
		} catch (WebApplicationException e) {
			throw e;
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	

	// ---- guard helpers ----

	private static WebApplicationException badRequest(String message) {
		return new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(message).build());
	}
	
	private static WebApplicationException notFound(String message) {
		return new WebApplicationException(Response.status(Status.NOT_FOUND).entity(message).build());
	}

	// ---- OCL helpers ----
	
	private OclConstraintSet resolveConstraintSet(String oclId) {
		ScopeService<?> scopeService = getScopeService();
		if (scopeService == null) {
			throw notFound("ScopeService not available");
		}
		EObject oclObject = scopeService.getContentFromStageForRegistry(JENA_OCL_REGISTRY_NAME, JENA_OCL_STAGE_NAME, oclId);
		if (!(oclObject instanceof OclConstraintSet oclConstraintSet)) {
			throw badRequest(String.format("No OClConstraintSet with id %s found", oclId));
		}
		return oclConstraintSet;
	}
	
	private void requireConstraintSetApplicable(OclConstraintSet oclConstraintSet, List<EObject> targets) {
		for(EObject target : targets) {
			if (!ValidationHelper.canEvaluateEObject(oclConstraintSet, target)) {
				throw badRequest(String.format("OCLConstraintSet %s cannot handle EObject", oclConstraintSet.getName()));
			}
		}
	}

	private boolean isConstraintSetApplicable(OclConstraintSet oclConstraintSet, EObject target) {
		return ValidationHelper.canEvaluateEObject(oclConstraintSet, target);
	}

	private OclResult evaluateConstraint(OclConstraint constraint, EObject target) throws OclParseException {
		EClassifier source = (EClassifier) resourceSet.getEObject(URI.createURI(constraint.getContextClass()), false);
		OclExpression expr = oclEngine.parse(constraint.getExpression(), source);
		return oclEngine.evaluateWithDiagnostics(expr, OclContext.of(target), OclEvaluationOptions.lenient());
	}

	// ---- result-building helper ----

	

	// ---- diagnostic helpers ----
	
	private void setParentDiagnosticSeverity(
			org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic parentDiagnostic) {
		if(parentDiagnostic.getChildren().stream().filter(d -> Severity.FATAL.equals(d.getType())).findFirst().isPresent()) {
			parentDiagnostic.setType(Severity.FATAL);
		} else if(parentDiagnostic.getChildren().stream().filter(d -> Severity.ERROR.equals(d.getType())).findFirst().isPresent()) {
			parentDiagnostic.setType(Severity.ERROR);
		} else if(parentDiagnostic.getChildren().stream().filter(d -> Severity.WARN.equals(d.getType())).findFirst().isPresent()) {
			parentDiagnostic.setType(Severity.WARN);
		} else if(parentDiagnostic.getChildren().stream().filter(d -> Severity.TRACE.equals(d.getType())).findFirst().isPresent()) {
			parentDiagnostic.setType(Severity.TRACE);
		} else  {
			parentDiagnostic.setType(Severity.INFO);
		} 
	}

	private org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic getDiagnostic(Severity severity, String source, String msg) {
		org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic diagnostic = COCLFactory.eINSTANCE.createDiagnostic();
		diagnostic.setType(severity);
		diagnostic.setSource(source);
		diagnostic.setMessage(msg);
		return diagnostic;
	}

	private org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic getDiagnostic(OclConstraint constraint) {
		org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic diagnostic = COCLFactory.eINSTANCE.createDiagnostic();
		diagnostic.setType(constraint.getSeverity());
		diagnostic.setSource(constraint.getContextClass());
		diagnostic.setMessage(String.format("Constraint %s failed for EObject", constraint.getName()));
		return diagnostic;
	}

	private List<org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic> getDiagnostics(List<Diagnostic> emfDiagnostics) {
		List<org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic> diagnostics = new ArrayList<>(emfDiagnostics.size());
		emfDiagnostics.forEach(emfd -> diagnostics.add(getDiagnostics(emfd)));
		return diagnostics;
	}

	private org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic getDiagnostics(Diagnostic emfDiagnostic) {
		org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic diagnostic = COCLFactory.eINSTANCE.createDiagnostic();
		diagnostic.setType(getDiagnosticSeverity(emfDiagnostic.getSeverity()));
		diagnostic.setMessage(emfDiagnostic.getMessage());
		diagnostic.setSource(emfDiagnostic.getSource());
		diagnostic.setExceptionMsg(emfDiagnostic.getException() != null ? emfDiagnostic.getException().getMessage() : null);
		emfDiagnostic.getChildren().forEach(child -> diagnostic.getChildren().add(getDiagnostics(child)));
		emfDiagnostic.getData().forEach(d -> diagnostic.getData().add(d.toString()));
		return diagnostic;
	}

	private Severity getDiagnosticSeverity(int emfDiagnosticType) {
		return switch (emfDiagnosticType) {
			case Diagnostic.WARNING -> Severity.WARN;
			case Diagnostic.ERROR -> Severity.ERROR;
			default -> Severity.INFO;
		};
	}

	// ---- infrastructure helpers ----

	/**
	 * Check that the Accept header contains a supported media type.
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

	private ScopeService<?> getScopeService() {
		return scopeCollector.getScopeServiceByScopeName(JENA_SCOPE_NAME);
	}
}
