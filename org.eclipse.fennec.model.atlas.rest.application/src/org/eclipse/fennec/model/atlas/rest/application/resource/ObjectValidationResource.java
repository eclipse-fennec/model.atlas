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
import java.util.Objects;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.m2x.ocl.api.OclContext;
import org.eclipse.fennec.m2x.ocl.api.OclEngine;
import org.eclipse.fennec.m2x.ocl.api.OclEvaluationOptions;
import org.eclipse.fennec.m2x.ocl.api.OclParseException;
import org.eclipse.fennec.m2x.ocl.api.OclResult;
import org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype;
import org.eclipse.fennec.model.atlas.runtime.RequireRuntime;
import org.eclipse.fennec.model.atlas.validation.ValidationHelper;
import org.eclipse.fennec.model.atlas.validation.model.cocl.COCLFactory;
import org.eclipse.fennec.model.atlas.validation.model.cocl.DerivedValidationRequest;
import org.eclipse.fennec.model.atlas.validation.model.cocl.EObjectValidationResult;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OperationValidationRequest;
import org.eclipse.fennec.model.atlas.validation.model.cocl.Severity;
import org.eclipse.fennec.model.atlas.validation.model.cocl.SimpleValidationResult;
import org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResponse;
import org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResult;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
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
 *
 * @author ilenia
 * @since Mar 16, 2026
 */
@RequireRuntime
@JakartarsResource()
@JakartarsName("ObjectValidationResource")
@Path("/validate")
@Component(name = "ObjectValidationResource", service = ObjectValidationResource.class, scope = ServiceScope.PROTOTYPE)
@Tag(name = "Object Validation Resource", description = "CRUD operations for validating an object against a model atlas schema")
public class ObjectValidationResource {

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
	public ObjectValidationResource(@Reference SupportedMediatype types) {
		supportedMediaTypes = types.getSupportedMediaTypes();
	}

	@POST
	@Consumes("application/xmi")
	@Produces({"application/xmi", MediaType.APPLICATION_JSON})
	@Operation(summary = "Validates the object against its schema", description = "Validates the object against its schema. Returns the validation errors, or 200, if the validation succeded", responses = {
			@ApiResponse(responseCode = "200", description = "Object validation was performed. A Response with the list of errors/warnings is returned."
					+ " The list might be empty, if the valudation did not encounter any issue",
					content = @Content(schema = @Schema(implementation = org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic.class))),
			@ApiResponse(responseCode = "415", description = "Unsupported media type"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public Response validate(
			@RequestBody(description = "The object to validate", required = true, content = @Content(schema = @Schema(implementation = EObject.class))) EObject eObject) {
		try {
			checkContentType();
			Diagnostic emfDiagnostic = Diagnostician.INSTANCE.validate(eObject);
			org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic diagnostic = getDiagnostics(emfDiagnostic);
			return Response.status(Response.Status.OK).entity(diagnostic).header("Content-Type", mediaType).build();
		} catch (WebApplicationException e) {
			throw e;
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path("/{oclId}")
	@Consumes({"application/xmi", "application/xml", MediaType.APPLICATION_JSON})
	@Produces({"application/xmi", MediaType.APPLICATION_JSON})
	@Operation(summary = "Validates the object against its schema and an additional OCLConstraintSet", description = "Validates the object against its schema and an additional OCLConstraintSet, whose id has to be provided.",
	responses = {
			@ApiResponse(responseCode = "200", description = "Object validation was performed. A Response with the list of errors/warnings is returned."
					+ " The list might be empty, if the valudation did not encounter any issue",
					content = @Content(schema = @Schema(implementation = org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic.class))),
			@ApiResponse(responseCode = "415", description = "Unsupported media type"),
			@ApiResponse(responseCode = "404", description = "OCLConstraintSet id was not found in C-OCL registry or OCLConstraintSet cannot handle the provided EObject"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public Response validateByOCLId(
			@PathParam("oclId") String oclId,
			@RequestBody(description = "The object to validate", required = true, content = @Content(schema = @Schema(implementation = EObject.class))) EObject eObject) {
		try {
			checkContentType();
			OclConstraintSet oclConstraintSet = resolveConstraintSet(oclId, eObject);
			List<OclConstraint> constraints = ValidationHelper.filter(oclConstraintSet, c -> c.isActive() && OclRole.VALIDATION.equals(c.getRole()));
			List<org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic> diagnostics = new ArrayList<>();
			for (OclConstraint constraint : constraints) {
				OclResult result = evaluateConstraint(constraint, eObject);
				if (result.isSuccess()) {
					boolean isValid = result.getValueAs(Boolean.class);
					if (!isValid) {
						diagnostics.add(getDiagnostic(constraint));
					}
				} else {
					diagnostics.addAll(getDiagnostics(result.diagnostics()));
				}
			}
			Diagnostic emfDiagnostic = Diagnostician.INSTANCE.validate(eObject);
			diagnostics.add(getDiagnostics(emfDiagnostic));
			ValidationResponse response = COCLFactory.eINSTANCE.createValidationResponse();
			response.getDiagnostics().addAll(diagnostics);
			response.setRole(OclRole.VALIDATION);
			return Response.status(Response.Status.OK).entity(response).header("Content-Type", mediaType).build();
		} catch (WebApplicationException e) {
			throw e;
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path("/derive")
	@Consumes({"application/xmi", "application/xml", MediaType.APPLICATION_JSON})
	@Produces({"application/xmi", MediaType.APPLICATION_JSON})
	@Operation(summary = "Computes derived features for the provided EObject using either its model or the provided C-OCL id", description = "Computes derived features for the provided EObject using either its model or the provided C-OCL id.",
	responses = {
			@ApiResponse(responseCode = "200", description = "Derived feature performed. A ValidationResponse with the corresponding results and diagnostics is returned.",
					content = @Content(schema = @Schema(implementation = org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResponse.class))),
			@ApiResponse(responseCode = "415", description = "Unsupported media type"),
			@ApiResponse(responseCode = "404", description = "If no EObject or more than one EObject has to be validated or if the provided OCLConstraintSet cannot handle the provided EObject or if one or more feature in the request are not in the EObject EClass"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public Response derive(
			@Parameter(description = "The C-OCL id where to compute the derived expression from", required = false)
			@QueryParam("oclId") String oclId,
			@RequestBody(description = "The DerivedValidationRequest", required = true,
			content = @Content(schema = @Schema(implementation = org.eclipse.fennec.model.atlas.validation.model.cocl.DerivedValidationRequest.class))) DerivedValidationRequest validationRequest) {
		try {
			checkContentType();
			EObject validatingObject = requireSingleObject(validationRequest.getValidationObjects());
			if (validationRequest.getDerivedFeature().isEmpty()) {
				throw badRequest("No Derived Feature in Request Body");
			}
			List<EStructuralFeature> notMatchingFeatures = validationRequest.getDerivedFeature().stream()
					.filter(f -> validatingObject.eClass().getEStructuralFeature(f.getName()) == null).toList();
			if (!notMatchingFeatures.isEmpty()) {
				throw badRequest(String.format("Feature(s) %s do not belong to EObject EClass %s", notMatchingFeatures, validatingObject.eClass().getName()));
			}
			ValidationResponse response = COCLFactory.eINSTANCE.createValidationResponse();
			response.setRole(OclRole.DERIVED);
			List<org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic> diagnostics = new ArrayList<>();
			if (oclId != null) {
				OclConstraintSet oclConstraintSet = resolveConstraintSet(oclId, validatingObject);
				for (EStructuralFeature feature : validationRequest.getDerivedFeature()) {
					List<OclConstraint> constraints = ValidationHelper.filter(oclConstraintSet,
							c -> c.isActive() && OclRole.DERIVED.equals(c.getRole()) && c.getFeatureName() != null &&
							feature.getName().equals(c.getFeatureName()));
					if (constraints.isEmpty()) {
						diagnostics.add(getDiagnostic(feature, oclId));
					} else {
						OclResult result = evaluateConstraint(constraints.get(0), validatingObject);
						if (result.isSuccess()) {
							diagnostics.add(getDiagnostic(Severity.INFO, feature.getName(), "Succesfully computed derived feature"));
							ValidationResult vr = toValidationResult(result.getValueAs(Object.class), feature.getEType(), false);
							if (vr instanceof EObjectValidationResult eObjResult) {
								eObjResult.getDiagnostics().addAll(diagnostics);
							}
							response.getResults().add(vr);
						} else {
							diagnostics.addAll(getDiagnostics(result.diagnostics()));
						}
					}
				}
			} else {
				for (EStructuralFeature feature : validationRequest.getDerivedFeature()) {
					diagnostics.add(getDiagnostic(Severity.INFO, feature.getName(), "Succesfully computed derived feature"));
					ValidationResult vr = toValidationResult(validatingObject.eGet(feature), feature.getEType(), feature.isMany());
					if (vr instanceof EObjectValidationResult eObjResult) {
						eObjResult.getDiagnostics().addAll(diagnostics);
					}
					response.getResults().add(vr);
				}
			}
			response.getDiagnostics().addAll(diagnostics);
			return Response.status(Response.Status.OK).entity(response).header("Content-Type", mediaType).build();
		} catch (WebApplicationException e) {
			throw e;
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path("/compute")
	@Consumes({"application/xmi", "application/xml", MediaType.APPLICATION_JSON})
	@Produces({"application/xmi", MediaType.APPLICATION_JSON})
	@Operation(summary = "Computes EOperation for the provided EObjects", description = "Computes EOperation for the provided EObjects.",
	responses = {
			@ApiResponse(responseCode = "200", description = "EOperation performed. A ValidationResponse with the corresponding results and diagnostics is returned.",
					content = @Content(schema = @Schema(implementation = ValidationResponse.class))),
			@ApiResponse(responseCode = "415", description = "Unsupported media type"),
			@ApiResponse(responseCode = "404", description = "No object to validate is provided or no matching EOperation in the object EClass is found"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public Response compute(
			@RequestBody(description = "The OperationValidationRequest", required = true,
			content = @Content(schema = @Schema(implementation = OperationValidationRequest.class))) OperationValidationRequest validationRequest) {
		try {
			checkContentType();
			EObject validatingObject = requireSingleObject(validationRequest.getValidationObjects());
			EOperation validatingOperation = validationRequest.getOperation();
			if (validatingOperation == null) {
				throw badRequest("No EOperation to use for validation");
			}
			EOperation objOperation = validatingObject.eClass().getEOperations().stream()
					.filter(o -> o.getName().equals(validatingOperation.getName())).findFirst().orElse(null);
			if (objOperation == null) {
				throw badRequest(String.format("EOperation %s do not belong to EObject EClass %s", validatingOperation.getName(), validatingObject.eClass().getName()));
			}
			checkOperationSignature(validatingOperation, objOperation);
			if (validationRequest.getParameters().size() != validatingOperation.getEParameters().size()) {
				throw badRequest(String.format("Number of Parameters provided does not match number of EOperation parameters. Expected %d but was %d",
						validatingOperation.getEParameters().size(), validationRequest.getParameters().size()));
			}
			BasicEList<Object> arguments = new BasicEList<>();
			List<EParameter> eParams = objOperation.getEParameters();
			for (int i = 0; i < validationRequest.getParameters().size(); i++) {
				OperationRequestParameter param = validationRequest.getParameters().get(i);
				if (param.isIsNull()) {
					arguments.add(null);
				} else if (param.getEValue() != null) {
					arguments.add(param.getEValue());
				} else {
					EDataType eDataType = (EDataType) eParams.get(i).getEType();
					arguments.add(eDataType.getEPackage().getEFactoryInstance().createFromString(eDataType, param.getJavaValue()));
				}
			}
			Object result = validatingObject.eInvoke(objOperation, arguments);
			ValidationResponse response = COCLFactory.eINSTANCE.createValidationResponse();
			response.setRole(OclRole.OPERATION);
			response.getResults().add(toValidationResult(result, objOperation.getEType(), result instanceof EList<?>));
			return Response.status(Response.Status.OK).entity(response).header("Content-Type", mediaType).build();
		} catch (WebApplicationException e) {
			throw e;
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	// ---- guard helpers ----

	private EObject requireSingleObject(List<EObject> objects) {
		if (objects.isEmpty()) {
			throw badRequest("No Object to be validated");
		}
		if (objects.size() > 1) {
			throw badRequest("To validate more than one EObject, please use the derived/batch endpoint");
		}
		return objects.get(0);
	}

	private static WebApplicationException badRequest(String message) {
		return new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(message).build());
	}

	private static WebApplicationException notFound(String message) {
		return new WebApplicationException(Response.status(Status.NOT_FOUND).entity(message).build());
	}

	// ---- OCL helpers ----

	private OclConstraintSet resolveConstraintSet(String oclId, EObject target) {
		ScopeService<?> scopeService = getScopeService();
		if (scopeService == null) {
			throw notFound("ScopeService not available");
		}
		EObject oclObject = scopeService.getContentFromStageForRegistry(JENA_OCL_REGISTRY_NAME, JENA_OCL_STAGE_NAME, oclId);
		if (!(oclObject instanceof OclConstraintSet oclConstraintSet)) {
			throw badRequest(String.format("No OClConstraintSet with id %s found", oclId));
		}
		if (!ValidationHelper.canEvaluateEObject(oclConstraintSet, target)) {
			throw badRequest(String.format("OCLConstraintSet %s cannot handle EObject", oclId));
		}
		return oclConstraintSet;
	}

	private OclResult evaluateConstraint(OclConstraint constraint, EObject target) throws OclParseException {
		EClassifier source = (EClassifier) resourceSet.getEObject(URI.createURI(constraint.getContextClass()), false);
		OclExpression expr = oclEngine.parse(constraint.getExpression(), source);
		return oclEngine.evaluateWithDiagnostics(expr, OclContext.of(target), OclEvaluationOptions.lenient());
	}

	// ---- result-building helper ----

	@SuppressWarnings("unchecked")
	private ValidationResult toValidationResult(Object value, EClassifier eType, boolean isMany) {
		if (isMany) {
			EList<?> list = (EList<?>) value;
			if (eType instanceof EClass) {
				EObjectValidationResult r = COCLFactory.eINSTANCE.createEObjectValidationResult();
				r.getValues().addAll((EList<EObject>) list);
				return r;
			}
			SimpleValidationResult r = COCLFactory.eINSTANCE.createSimpleValidationResult();
			if (eType instanceof EDataType eDataType) {
				List<String> converted = new ArrayList<>(list.size());
				for (Object v : list) {
					converted.add(eDataType.getEPackage().getEFactoryInstance().convertToString(eDataType, v));
				}
				r.setValue(converted);
			} else {
				r.setValue(new ArrayList<>(list));
			}
			return r;
		}
		if (value instanceof EObject eObj) {
			EObjectValidationResult r = COCLFactory.eINSTANCE.createEObjectValidationResult();
			r.getValues().add(eObj);
			return r;
		}
		SimpleValidationResult r = COCLFactory.eINSTANCE.createSimpleValidationResult();
		if (eType instanceof EDataType eDataType) {
			r.setValue(eDataType.getEPackage().getEFactoryInstance().convertToString(eDataType, value));
		} else {
			r.setValue(value);
		}
		return r;
	}

	// ---- operation signature check ----

	private void checkOperationSignature(EOperation validatingOperation, EOperation objOperation) {
		if (!Objects.equals(validatingOperation.getEType(), objOperation.getEType())) {
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST)
					.entity(String.format("EOperation %s return type mismatch: expected %s but got %s",
							objOperation.getName(),
							objOperation.getEType() != null ? objOperation.getEType().getName() : "void",
							validatingOperation.getEType() != null ? validatingOperation.getEType().getName() : "void"))
					.build());
		}
		List<org.eclipse.emf.ecore.EParameter> objParams = objOperation.getEParameters();
		List<org.eclipse.emf.ecore.EParameter> validatingParams = validatingOperation.getEParameters();
		if (objParams.size() != validatingParams.size()) {
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST)
					.entity(String.format("EOperation %s parameter count mismatch: expected %d but got %d",
							objOperation.getName(), objParams.size(), validatingParams.size()))
					.build());
		}
		for (int i = 0; i < objParams.size(); i++) {
			if (!Objects.equals(objParams.get(i).getEType(), validatingParams.get(i).getEType())) {
				throw new WebApplicationException(
						Response.status(Status.BAD_REQUEST)
						.entity(String.format("EOperation %s parameter %d type mismatch: expected %s but got %s",
								objOperation.getName(), i,
								objParams.get(i).getEType() != null ? objParams.get(i).getEType().getName() : "null",
								validatingParams.get(i).getEType() != null ? validatingParams.get(i).getEType().getName() : "null"))
						.build());
			}
		}
	}

	// ---- diagnostic helpers ----

	private org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic getDiagnostic(Severity severity, String source, String msg) {
		org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic diagnostic = COCLFactory.eINSTANCE.createDiagnostic();
		diagnostic.setType(severity);
		diagnostic.setSource(source);
		diagnostic.setMessage(msg);
		return diagnostic;
	}

	private org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic getDiagnostic(EStructuralFeature feature, String oclId) {
		org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic diagnostic = COCLFactory.eINSTANCE.createDiagnostic();
		diagnostic.setType(Severity.WARN);
		diagnostic.setSource(feature.getName());
		diagnostic.setMessage(String.format("No active DERIVED Constraint found in C-OCL %s for feature %s", oclId, feature.getName()));
		return diagnostic;
	}

	private org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic getDiagnostic(OclConstraint constraint) {
		org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic diagnostic = COCLFactory.eINSTANCE.createDiagnostic();
		diagnostic.setType(constraint.getSeverity());
		diagnostic.setSource(constraint.getContextClass());
		diagnostic.setMessage(String.format("Constraint %s failed for EObject", constraint.getExpression()));
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
