/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
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
package org.eclipse.fennec.model.atlas.validation.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.eclipse.emf.common.util.BasicEList;
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
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.m2x.ocl.api.OclContext;
import org.eclipse.fennec.m2x.ocl.api.OclEngine;
import org.eclipse.fennec.m2x.ocl.api.OclEvaluationOptions;
import org.eclipse.fennec.m2x.ocl.api.OclParseException;
import org.eclipse.fennec.m2x.ocl.api.OclResult;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.eclipse.fennec.model.atlas.scope.api.RegistryInfo;
import org.eclipse.fennec.model.atlas.scope.api.RegistryType;
import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;
import org.eclipse.fennec.model.atlas.validation.ValidationHelper;
import org.eclipse.fennec.model.atlas.validation.ValidationService;
import org.eclipse.fennec.model.atlas.validation.model.cocl.BatchValidationRequest;
import org.eclipse.fennec.model.atlas.validation.model.cocl.COCLFactory;
import org.eclipse.fennec.model.atlas.validation.model.cocl.DerivedValidationRequest;
import org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic;
import org.eclipse.fennec.model.atlas.validation.model.cocl.EObjectValidationResult;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OperationReturnType;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OperationValidationRequest;
import org.eclipse.fennec.model.atlas.validation.model.cocl.Severity;
import org.eclipse.fennec.model.atlas.validation.model.cocl.SimpleValidationResult;
import org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResponse;
import org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResult;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.component.annotations.ServiceScope;
import org.eclipse.fennec.model.atlas.readable.scope.collector.ReadableScopeCollector;

/**
 * @author ilenia
 * @since Apr 2026
 */
@Component(name = "ValidationService", service = ValidationService.class, scope = ServiceScope.PROTOTYPE)
public class ValidationServiceImpl implements ValidationService {

	@Reference
	private ReadableScopeCollector scopeCollector;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private OclEngine oclEngine;

	@Override
	public Diagnostic validate(EObject eObject) {
		org.eclipse.emf.common.util.Diagnostic emfDiagnostic = Diagnostician.INSTANCE.validate(eObject);
		return toDiagnostic(emfDiagnostic);
	}

	@Override
	public ValidationResponse validateWithOcl(EObject eObject, String oclId, String scopeName, ResourceSet resourceSet) {
		ReadableScopeService<?> scopeService = resolveScopeService(scopeName);
		OclConstraintSet oclConstraintSet = resolveConstraintSet(oclId, scopeService);
		requireConstraintSetApplicable(oclConstraintSet, eObject);
		List<OclConstraint> constraints = ValidationHelper.filter(oclConstraintSet,
				c -> c.isActive() && OclRole.VALIDATION.equals(c.getRole()));
		List<Diagnostic> diagnostics = new ArrayList<>();
		for (OclConstraint constraint : constraints) {
			OclResult result = evaluateConstraint(constraint, eObject, resourceSet);
			if (result.isSuccess()) {
				boolean isValid = result.getValueAs(Boolean.class);
				if (!isValid) {
					diagnostics.add(toDiagnostic(constraint));
				}
			} else {
				diagnostics.addAll(toDiagnostics(result.diagnostics()));
			}
		}
		org.eclipse.emf.common.util.Diagnostic emfDiagnostic = Diagnostician.INSTANCE.validate(eObject);
		diagnostics.add(toDiagnostic(emfDiagnostic));
		ValidationResponse response = COCLFactory.eINSTANCE.createValidationResponse();
		response.getDiagnostics().addAll(diagnostics);
		response.setRole(OclRole.VALIDATION);
		return response;
	}

	@Override
	public ValidationResponse derive(DerivedValidationRequest request, String oclId, String scopeName, ResourceSet resourceSet) {
		EObject validatingObject = requireSingleObject(request.getValidationObjects());
		if (request.getDerivedFeature().isEmpty()) {
			throw new IllegalArgumentException("No Derived Feature in Request Body");
		}
		List<EStructuralFeature> notMatchingFeatures = request.getDerivedFeature().stream()
				.filter(f -> validatingObject.eClass().getEStructuralFeature(f.getName()) == null).toList();
		if (!notMatchingFeatures.isEmpty()) {
			throw new IllegalArgumentException(String.format("Feature(s) %s do not belong to EObject EClass %s",
					notMatchingFeatures, validatingObject.eClass().getName()));
		}
		ValidationResponse response = COCLFactory.eINSTANCE.createValidationResponse();
		response.setRole(OclRole.DERIVED);
		List<Diagnostic> diagnostics = new ArrayList<>();
		if (oclId != null) {
			ReadableScopeService<?> scopeService = resolveScopeService(scopeName);
			OclConstraintSet oclConstraintSet = resolveConstraintSet(oclId, scopeService);
			requireConstraintSetApplicable(oclConstraintSet, validatingObject);
			for (EStructuralFeature feature : request.getDerivedFeature()) {
				List<OclConstraint> constraints = ValidationHelper.filter(oclConstraintSet,
						c -> c.isActive() && OclRole.DERIVED.equals(c.getRole()) && c.getFeatureName() != null
						&& feature.getName().equals(c.getFeatureName()));
				if (constraints.isEmpty()) {
					diagnostics.add(toDiagnosticMissingDerived(feature, oclId));
				} else {
					OclResult result = evaluateConstraint(constraints.get(0), validatingObject, resourceSet);
					if (result.isSuccess()) {
						diagnostics.add(toDiagnostic(Severity.INFO, feature.getName(), "Succesfully computed derived feature"));
						ValidationResult vr = toValidationResult(result.getValueAs(Object.class), feature.getEType(), false);
						if (vr instanceof EObjectValidationResult eObjResult) {
							eObjResult.getDiagnostics().addAll(diagnostics);
						}
						response.getResults().add(vr);
					} else {
						diagnostics.addAll(toDiagnostics(result.diagnostics()));
					}
				}
			}
		} else {
			for (EStructuralFeature feature : request.getDerivedFeature()) {
				diagnostics.add(toDiagnostic(Severity.INFO, feature.getName(), "Succesfully computed derived feature"));
				ValidationResult vr = toValidationResult(validatingObject.eGet(feature), feature.getEType(), feature.isMany());
				if (vr instanceof EObjectValidationResult eObjResult) {
					eObjResult.getDiagnostics().addAll(diagnostics);
				}
				response.getResults().add(vr);
			}
		}
		response.getDiagnostics().addAll(diagnostics);
		return response;
	}

	@Override
	public ValidationResponse compute(OperationValidationRequest request,  String scopeName, ResourceSet resourceSet) {

		CheckedValidationRequest checkRequest = checkRequest(request);
		
		EObject validatingObject = checkRequest.validatingEObject;
		boolean withCOCL = checkRequest.withCOCL;
		if(withCOCL) {
			ReadableScopeService<?> scopeService = resolveScopeService(scopeName);
			OclConstraintSet oclConstraintSet = resolveConstraintSet(request.getCoclId(), scopeService);
			requireConstraintSetApplicable(oclConstraintSet, validatingObject);
			String operationName = request.getOperationName();
			List<String> operationParamNames = request.getParameters().stream().map(p -> p.getParameterName()).toList();
			List<OclConstraint> constraints = ValidationHelper.filter(oclConstraintSet,
					c -> c.isActive() && 
					OclRole.OPERATION.equals(c.getRole()) && 
					operationName.equals(c.getOperationName()) &&
					checkOperationParamNames(operationParamNames, c.getOperationParameterNames()));
			if(constraints.isEmpty()) {
				throw new IllegalArgumentException(String.format("No active OclConstrait of type OPERATION with operation name %s and a matching list of parameter names has been found in COCLConstraintSet %s", operationName, request.getCoclId()));
			}
			ValidationResponse response = COCLFactory.eINSTANCE.createValidationResponse();
			response.setRole(OclRole.OPERATION);
			OclConstraint oclOperation = constraints.get(0);
			Map<String, Object> variables = createOperationVariableMap(request.getParameters());
			OclResult result = evaluateConstraint(oclOperation, validatingObject, variables, resourceSet);
			
			if(result.isSuccess()) {
				ValidationResult validationResult = toValidationResult(result.value(), oclOperation.getOperationReturnType());
				response.getResults().add(validationResult);
			} else {
				response.getDiagnostics().addAll(toDiagnostics(result.diagnostics()));
			}
			return response;
		} else {
			EOperation validatingOperation = request.getOperation();
			EOperation objOperation = validatingObject.eClass().getEOperations().stream()
					.filter(o -> o.getName().equals(validatingOperation.getName())).findFirst().orElse(null);
			if (objOperation == null) {
				throw new IllegalArgumentException(String.format("EOperation %s do not belong to EObject EClass %s",
						validatingOperation.getName(), validatingObject.eClass().getName()));
			}
			checkOperationSignature(validatingOperation, objOperation);
			if (request.getParameters().size() != validatingOperation.getEParameters().size()) {
				throw new IllegalArgumentException(String.format(
						"Number of Parameters provided does not match number of EOperation parameters. Expected %d but was %d",
						validatingOperation.getEParameters().size(), request.getParameters().size()));
			}
			BasicEList<Object> arguments = new BasicEList<>();
			List<EParameter> eParams = objOperation.getEParameters();
			for (int i = 0; i < request.getParameters().size(); i++) {
				OperationRequestParameter param = request.getParameters().get(i);
				if (param.isIsNull()) {
					arguments.add(null);
				} else if (param.getEValue() != null) {
					arguments.add(param.getEValue());
				} else {
					EDataType eDataType = (EDataType) eParams.get(i).getEType();
					arguments.add(eDataType.getEPackage().getEFactoryInstance().createFromString(eDataType, param.getJavaValue()));
				}
			}
			ValidationResponse response = COCLFactory.eINSTANCE.createValidationResponse();
			response.setRole(OclRole.OPERATION);
			Object result = null;
			try {
				result = validatingObject.eInvoke(objOperation, arguments);
			} catch (InvocationTargetException e) {
				response.getDiagnostics().add(toDiagnostic(Severity.FATAL, objOperation.getName(), "EOperation threw an InvocationTargetException while executing on object"));
				return response;
			}

			response.getResults().add(toValidationResult(result, objOperation.getEType(), result instanceof EList<?>));
			return response;
		}


	}

	@Override
	public ValidationResponse validateBatch(BatchValidationRequest request, String scopeName, ResourceSet resourceSet) {
		if (request.getValidationObjects().isEmpty()) {
			throw new IllegalArgumentException("No Object to be validated");
		}
		if (request.getCoclId() == null) {
			throw new IllegalArgumentException("No C-OCL id was provided");
		}
		OclConstraint filterConstraint = request.getFilterConstraint();
		if (filterConstraint != null && !OclRole.REFERENCE_FILTER.equals(filterConstraint.getRole())) {
			throw new IllegalArgumentException(String.format(
					"Provided Filter Constraint is of type %s. Should be of type REFERENCE_FILTER",
					filterConstraint.getRole()));
		}
		ReadableScopeService<?> scopeService = resolveScopeService(scopeName);
		OclConstraintSet constraintSet = resolveConstraintSet(request.getCoclId(), scopeService);
		List<OclConstraint> constraints = ValidationHelper.filter(constraintSet,
				c -> c.isActive() && OclRole.VALIDATION.equals(c.getRole()));
		if (constraints.isEmpty()) {
			throw new IllegalArgumentException(String.format(
					"No active OCL Constraint of type VALIDATION found in OClConstraintSet %s", request.getCoclId()));
		}
		ValidationResponse response = COCLFactory.eINSTANCE.createValidationResponse();
		response.setRole(OclRole.VALIDATION);
		for (EObject target : request.getValidationObjects()) {
			Diagnostic parentDiagnostic = COCLFactory.eINSTANCE.createDiagnostic();
			parentDiagnostic.getData().add(target.toString());
			EObject filteredTarget = target;
			if (filterConstraint != null) {
				OclResult result = evaluateConstraint(filterConstraint, target, resourceSet);
				if (result.isSuccess()) {
					boolean isValid = result.getValueAs(Boolean.class);
					if (!isValid) {
						filteredTarget = null;
						parentDiagnostic.getChildren().add(toDiagnostic(Severity.INFO, filterConstraint.getName(),
								"Object was filtered out by filter constraint"));
					}
				} else {
					filteredTarget = null;
					parentDiagnostic.getChildren().addAll(toDiagnostics(result.diagnostics()));
				}
			}
			if (filteredTarget != null) {
				if (!ValidationHelper.canEvaluateEObject(constraintSet, filteredTarget)) {
					parentDiagnostic.getChildren().add(toDiagnostic(Severity.ERROR, constraintSet.getName(),
							String.format("C-OCL ConstraintSet %s is not compatible with EObject from EPackage %s", constraintSet.getName(), filteredTarget.eClass().getEPackage().getNsURI())));
					setParentDiagnosticSeverity(parentDiagnostic);
					response.getDiagnostics().add(parentDiagnostic);
					continue;
				}
				for (OclConstraint constraint : constraints) {
					OclResult result = evaluateConstraint(constraint, filteredTarget, resourceSet);
					if (result.isSuccess()) {
						boolean isValid = result.getValueAs(Boolean.class);
						if (!isValid) {
							parentDiagnostic.getChildren().add(toDiagnostic(constraint));
						}
					} else {
						parentDiagnostic.getChildren().addAll(toDiagnostics(result.diagnostics()));
					}
				}
				org.eclipse.emf.common.util.Diagnostic emfDiagnostic = Diagnostician.INSTANCE.validate(filteredTarget);
				parentDiagnostic.getChildren().add(toDiagnostic(emfDiagnostic));
			}
			setParentDiagnosticSeverity(parentDiagnostic);
			response.getDiagnostics().add(parentDiagnostic);
		}
		return response;
	}

	@Override
	public ValidationResponse filterBatch(BatchValidationRequest request, String scopeName, ResourceSet resourceSet) {
		if (request.getValidationObjects().isEmpty()) {
			throw new IllegalArgumentException("No Object to be validated");
		}
		if (request.getCoclId() == null) {
			throw new IllegalArgumentException("No C-OCL id was provided");
		}
		ReadableScopeService<?> scopeService = resolveScopeService(scopeName);
		OclConstraintSet constraintSet = resolveConstraintSet(request.getCoclId(), scopeService);
		requireConstraintSetApplicable(constraintSet, request.getValidationObjects());
		List<OclConstraint> constraints = ValidationHelper.filter(constraintSet,
				c -> c.isActive() && OclRole.REFERENCE_FILTER.equals(c.getRole()));
		if (constraints.isEmpty()) {
			return null;
		}
		ValidationResponse response = COCLFactory.eINSTANCE.createValidationResponse();
		response.setRole(OclRole.REFERENCE_FILTER);
		for (EObject target : request.getValidationObjects()) {
			EObjectValidationResult filteredResult = COCLFactory.eINSTANCE.createEObjectValidationResult();
			filteredResult.getValues().add(EcoreUtil.copy(target));
			Diagnostic parentDiagnostic = COCLFactory.eINSTANCE.createDiagnostic();
			parentDiagnostic.getData().add(target.toString());
			for (OclConstraint constraint : constraints) {
				if (!filteredResult.getValues().isEmpty()) {
					OclResult result = evaluateConstraint(constraint, target, resourceSet);
					if (result.isSuccess()) {
						boolean isValid = result.getValueAs(Boolean.class);
						if (!isValid) {
							parentDiagnostic.getChildren().add(toDiagnostic(Severity.INFO, constraint.getName(),
									"Object was filtered out by filter constraint"));
							filteredResult.getValues().clear();
						}
					} else {
						parentDiagnostic.getChildren().addAll(toDiagnostics(result.diagnostics()));
						filteredResult.getValues().clear();
					}
				}
			}
			setParentDiagnosticSeverity(parentDiagnostic);
			filteredResult.getDiagnostics().add(parentDiagnostic);
			response.getResults().add(filteredResult);
		}
		long retainedCount = response.getResults().stream()
				.map(r -> (EObjectValidationResult) r)
				.filter(r -> !r.getValues().isEmpty())
				.count();
		if (retainedCount == request.getValidationObjects().size()) {
			return null;
		}
		return response;
	}

	// ---- OCL helpers ----
	
	private ReadableScopeService<?> resolveScopeService(String scopeName) {
		ReadableScopeService<?> scopeService = scopeCollector.getScopeServiceByScopeName(scopeName);
		if (scopeService == null) {
			throw new NoSuchElementException("ReadableScopeService not available for scope: " + scopeName);
		}
		return scopeService;
	}

	private OclConstraintSet resolveConstraintSet(String oclId, ReadableScopeService<?> scopeService) {
		ScopeInfo scope = scopeService.getScopeInfo();
		RegistryInfo coclRegistry = scope.getRegistries().stream()
				.filter(r -> RegistryType.COCL == r.getType())
				.findFirst()
				.orElseThrow(() -> new NoSuchElementException("No COCL registry found in scope: " + scope.getName()));
		
		Object oclObject = scopeService.get(coclRegistry.getName(), oclId).orElse(null);
		if (!(oclObject instanceof OclConstraintSet oclConstraintSet)) {
			throw new IllegalArgumentException(String.format("No OclConstraintSet with id %s found", oclId));
		}
		return oclConstraintSet;
	}

	private OclResult evaluateConstraint(OclConstraint constraint, EObject target, ResourceSet resourceSet) {
		try {
			EClassifier source = (EClassifier) resourceSet.getEObject(URI.createURI(constraint.getContextClass()), false);
			OclExpression expr = oclEngine.parse(constraint.getExpression(), source);
			return oclEngine.evaluateWithDiagnostics(expr, OclContext.of(target), OclEvaluationOptions.lenient());
		} catch (OclParseException e) {
			throw new IllegalStateException(String.format("Failed to parse OCL expression '%s': ", constraint.getExpression(), e.getMessage()), e);
		}
	}
	
	private OclResult evaluateConstraint(OclConstraint constraint, EObject target, Map<String, Object> variables, ResourceSet resourceSet) {
		try {
			EClassifier source = (EClassifier) resourceSet.getEObject(URI.createURI(constraint.getContextClass()), false);
			OclExpression expr = oclEngine.parse(constraint.getExpression(), source);
			return oclEngine.evaluateWithDiagnostics(expr, OclContext.of(target, variables), OclEvaluationOptions.lenient());
		} catch (OclParseException e) {
			throw new IllegalStateException(String.format("Failed to parse OCL expression '%s': ", constraint.getExpression(), e.getMessage()), e);
		}
		
	}

	// ---- guard helpers ----

	private CheckedValidationRequest checkRequest(OperationValidationRequest validationRequest) {

		EObject validatingEObject = requireSingleObject(validationRequest.getValidationObjects());

		//		1. Check whether we have a C-OCL id or not
		if(validationRequest.getCoclId() != null) {
			//			1.a with COCL we need an operation name
			if(validationRequest.getOperationName() == null || validationRequest.getOperationName().isBlank()) {
				throw new IllegalArgumentException("When a COCL id is provided, an operationName must be provided as well");
			}
			return new CheckedValidationRequest(true, validatingEObject);
		} else {
			//			1.b if NO C-OCL id is provided, then we must have an operation
			if(validationRequest.getOperation() == null) {
				throw new IllegalArgumentException("When NO COCL id is provided, an operation reference must be provided as well");
			}
			return new CheckedValidationRequest(false, validatingEObject);
		}
	}

	private record CheckedValidationRequest(boolean withCOCL, EObject validatingEObject) {

	}

	private static EObject requireSingleObject(List<EObject> objects) {
		if (objects.isEmpty()) {
			throw new IllegalArgumentException("No Object to be validated");
		}
		if (objects.size() > 1) {
			throw new IllegalArgumentException("To validate more than one EObject, please use the derived/batch endpoint");
		}
		return objects.get(0);
	}

	private static void requireConstraintSetApplicable(OclConstraintSet constraintSet, List<EObject> targets) {
		for (EObject target : targets) {
			requireConstraintSetApplicable(constraintSet, target);
		}
	}
	
	private static void requireConstraintSetApplicable(OclConstraintSet constraintSet, EObject target) {
		if (!ValidationHelper.canEvaluateEObject(constraintSet, target)) {
			throw new IllegalArgumentException(String.format("OCLConstraintSet %s cannot handle EObject coming from EPackage %s", constraintSet.getName(), target.eClass().getEPackage().getNsURI()));
		}
	}

	private Map<String, Object> createOperationVariableMap(EList<OperationRequestParameter> parameters) {
		Map<String, Object> variables = new HashMap<>(parameters.size());
		parameters.forEach(p -> {
			variables.put(p.getParameterName(), p.isIsNull() ? null : p.getJavaValue() != null ? p.getJavaValue() : p.getEValue());
		});
		return variables;
	}

	private boolean checkOperationParamNames(List<String> requestParamNames, List<String> oclParamNames) {
		if(requestParamNames.size() != oclParamNames.size()) return false;
		for(int i = 0; i < requestParamNames.size(); i++) {
			if(!requestParamNames.get(i).equals(oclParamNames.get(i))) return false;
		}
		return true;
	}

	// ---- operation signature check ----

	private static void checkOperationSignature(EOperation validatingOperation, EOperation objOperation) {
		if (!Objects.equals(validatingOperation.getEType(), objOperation.getEType())) {
			throw new IllegalArgumentException(String.format(
					"EOperation %s return type mismatch: expected %s but got %s",
					objOperation.getName(),
					objOperation.getEType() != null ? objOperation.getEType().getName() : "void",
							validatingOperation.getEType() != null ? validatingOperation.getEType().getName() : "void"));
		}
		List<EParameter> objParams = objOperation.getEParameters();
		List<EParameter> validatingParams = validatingOperation.getEParameters();
		if (objParams.size() != validatingParams.size()) {
			throw new IllegalArgumentException(String.format(
					"EOperation %s parameter count mismatch: expected %d but got %d",
					objOperation.getName(), objParams.size(), validatingParams.size()));
		}
		for (int i = 0; i < objParams.size(); i++) {
			if (!Objects.equals(objParams.get(i).getEType(), validatingParams.get(i).getEType())) {
				throw new IllegalArgumentException(String.format(
						"EOperation %s parameter %d type mismatch: expected %s but got %s",
						objOperation.getName(), i,
						objParams.get(i).getEType() != null ? objParams.get(i).getEType().getName() : "null",
								validatingParams.get(i).getEType() != null ? validatingParams.get(i).getEType().getName() : "null"));
			}
		}
	}

	// ---- result-building helper ----

	@SuppressWarnings("unchecked")
	private static ValidationResult toValidationResult(Object value, EClassifier eType, boolean isMany) {
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
				r.setValue(String.valueOf(converted));
				r.setValueJavaClassName(value.getClass().getName());
			} else {
				r.setValue(String.valueOf(list));
				r.setValueJavaClassName(value.getClass().getName());
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
			r.setValue(String.valueOf(value));
		}
		if(value != null) r.setValueJavaClassName(value.getClass().getName());
		return r;
	}
	
	@SuppressWarnings("unchecked")
	private ValidationResult toValidationResult(Object value, OperationReturnType returnType) {
		if(OperationReturnType.EOBJECT.equals(returnType)) {
			if(value instanceof EList list) {
				EObjectValidationResult r = COCLFactory.eINSTANCE.createEObjectValidationResult();
				r.getValues().addAll((EList<EObject>) list);
				return r;
			} else if(value instanceof List list) {
				EObjectValidationResult r = COCLFactory.eINSTANCE.createEObjectValidationResult();
				r.getValues().addAll((List<EObject>) list);
				return r;
			}
			
			else {
				EObjectValidationResult r = COCLFactory.eINSTANCE.createEObjectValidationResult();
				r.getValues().add((EObject) value);
				return r;
			}
		} else {
			SimpleValidationResult r = COCLFactory.eINSTANCE.createSimpleValidationResult();
			r.setValue(String.valueOf(value));
			if(value != null) r.setValueJavaClassName(value.getClass().getName());
			return r;
		}	
	}

	// ---- diagnostic helpers ----

	private static void setParentDiagnosticSeverity(Diagnostic parentDiagnostic) {
		if (parentDiagnostic.getChildren().stream().anyMatch(d -> Severity.FATAL.equals(d.getType()))) {
			parentDiagnostic.setType(Severity.FATAL);
		} else if (parentDiagnostic.getChildren().stream().anyMatch(d -> Severity.ERROR.equals(d.getType()))) {
			parentDiagnostic.setType(Severity.ERROR);
		} else if (parentDiagnostic.getChildren().stream().anyMatch(d -> Severity.WARN.equals(d.getType()))) {
			parentDiagnostic.setType(Severity.WARN);
		} else if (parentDiagnostic.getChildren().stream().anyMatch(d -> Severity.TRACE.equals(d.getType()))) {
			parentDiagnostic.setType(Severity.TRACE);
		} else {
			parentDiagnostic.setType(Severity.INFO);
		}
	}

	private static Diagnostic toDiagnostic(Severity severity, String source, String msg) {
		Diagnostic diagnostic = COCLFactory.eINSTANCE.createDiagnostic();
		diagnostic.setType(severity);
		diagnostic.setSource(source);
		diagnostic.setMessage(msg);
		return diagnostic;
	}

	private static Diagnostic toDiagnostic(OclConstraint constraint) {
		Diagnostic diagnostic = COCLFactory.eINSTANCE.createDiagnostic();
		diagnostic.setType(constraint.getSeverity());
		diagnostic.setSource(constraint.getContextClass());
		diagnostic.setMessage(String.format("Constraint %s failed for EObject", constraint.getExpression()));
		return diagnostic;
	}

	private static Diagnostic toDiagnosticMissingDerived(EStructuralFeature feature, String oclId) {
		Diagnostic diagnostic = COCLFactory.eINSTANCE.createDiagnostic();
		diagnostic.setType(Severity.WARN);
		diagnostic.setSource(feature.getName());
		diagnostic.setMessage(String.format("No active DERIVED Constraint found in C-OCL %s for feature %s", oclId, feature.getName()));
		return diagnostic;
	}

	private static List<Diagnostic> toDiagnostics(List<org.eclipse.emf.common.util.Diagnostic> emfDiagnostics) {
		List<Diagnostic> diagnostics = new ArrayList<>(emfDiagnostics.size());
		emfDiagnostics.forEach(emfd -> diagnostics.add(toDiagnostic(emfd)));
		return diagnostics;
	}

	private static Diagnostic toDiagnostic(org.eclipse.emf.common.util.Diagnostic emfDiagnostic) {
		Diagnostic diagnostic = COCLFactory.eINSTANCE.createDiagnostic();
		diagnostic.setType(toDiagnosticSeverity(emfDiagnostic.getSeverity()));
		diagnostic.setMessage(emfDiagnostic.getMessage());
		diagnostic.setSource(emfDiagnostic.getSource());
		diagnostic.setExceptionMsg(emfDiagnostic.getException() != null ? emfDiagnostic.getException().getMessage() : null);
		emfDiagnostic.getChildren().forEach(child -> diagnostic.getChildren().add(toDiagnostic(child)));
		emfDiagnostic.getData().forEach(d -> diagnostic.getData().add(d.toString()));
		return diagnostic;
	}

	private static Severity toDiagnosticSeverity(int emfSeverity) {
		return switch (emfSeverity) {
		case org.eclipse.emf.common.util.Diagnostic.WARNING -> Severity.WARN;
		case org.eclipse.emf.common.util.Diagnostic.ERROR -> Severity.ERROR;
		default -> Severity.INFO;
		};
	}
}
