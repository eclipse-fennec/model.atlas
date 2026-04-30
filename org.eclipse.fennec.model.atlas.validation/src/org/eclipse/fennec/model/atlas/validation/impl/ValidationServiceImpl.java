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
package org.eclipse.fennec.model.atlas.validation.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
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
import org.eclipse.fennec.model.atlas.validation.ValidationHelper;
import org.eclipse.fennec.model.atlas.validation.ValidationService;
import org.eclipse.fennec.model.atlas.validation.model.cocl.BatchValidationRequest;
import org.eclipse.fennec.model.atlas.validation.model.cocl.COCLFactory;
import org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic;
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
import org.eclipse.fennec.model.atlas.wf.workflowapi.Registry;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryType;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Stage;
import org.eclipse.fennec.model.atlas.workflow.ScopeServiceCollector;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author ilenia
 * @since Apr 2026
 */
@Component(name = "ValidationService", service = ValidationService.class, scope = ServiceScope.PROTOTYPE)
public class ValidationServiceImpl implements ValidationService {

    @Reference
    private ScopeServiceCollector scopeCollector;

    @Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
    private OclEngine oclEngine;

    @Override
    public Diagnostic validate(EObject eObject) {
        org.eclipse.emf.common.util.Diagnostic emfDiagnostic = Diagnostician.INSTANCE.validate(eObject);
        return toDiagnostic(emfDiagnostic);
    }

    @Override
    public ValidationResponse validateWithOcl(EObject eObject, String oclId, String scopeName, ResourceSet resourceSet) {
        OclConstraintSet oclConstraintSet = resolveConstraintSet(oclId, scopeName);
        if (!ValidationHelper.canEvaluateEObject(oclConstraintSet, eObject)) {
            throw new IllegalArgumentException(String.format("OCLConstraintSet %s cannot handle EObject", oclId));
        }
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
            OclConstraintSet oclConstraintSet = resolveConstraintSet(oclId, scopeName);
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
    public ValidationResponse compute(OperationValidationRequest request) {
        EObject validatingObject = requireSingleObject(request.getValidationObjects());
        EOperation validatingOperation = request.getOperation();
        if (validatingOperation == null) {
            throw new IllegalArgumentException("No EOperation to use for validation");
        }
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
        OclConstraintSet constraintSet = resolveConstraintSet(request.getCoclId(), scopeName);
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
                            String.format("C-OCL ConstraintSet %s is not compatible with EObject", constraintSet.getName())));
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
        OclConstraintSet constraintSet = resolveConstraintSet(request.getCoclId(), scopeName);
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

    private OclConstraintSet resolveConstraintSet(String oclId, String scopeName) {
        ScopeService<?> scopeService = scopeCollector.getScopeServiceByScopeName(scopeName);
        if (scopeService == null) {
            throw new NoSuchElementException("ScopeService not available for scope: " + scopeName);
        }
        Scope scope = scopeService.getScope();
        Registry coclRegistry = scope.getRegistries().stream()
                .filter(r -> RegistryType.COCL == r.getType())
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No COCL registry found in scope: " + scopeName));
        Stage finalStage = coclRegistry.getStages().stream()
                .filter(Stage::isFinal)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No final stage in COCL registry for scope: " + scopeName));
        EObject oclObject = scopeService.getContentFromStageForRegistry(coclRegistry.getName(), finalStage.getName(), oclId);
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
            throw new IllegalStateException("Failed to parse OCL expression: " + e.getMessage(), e);
        }
    }

    // ---- guard helpers ----

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
            if (!ValidationHelper.canEvaluateEObject(constraintSet, target)) {
                throw new IllegalArgumentException(
                        String.format("OCLConstraintSet %s cannot handle EObject", constraintSet.getName()));
            }
        }
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
