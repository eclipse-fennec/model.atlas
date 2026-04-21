package org.eclipse.fennec.model.atlas.validation;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole;

public class ValidationHelper {

	public static boolean canEvaluateEObject(OclConstraintSet constraintSet, EObject eObject) {
		if(constraintSet.getTargetModelNsURIs().isEmpty()) return true;
		String packageURI = eObject.eClass().getEPackage().getNsURI();
		if(constraintSet.getTargetModelNsURIs().contains(packageURI)) return true;
		return false;
	}
	
	public static boolean canHandleEClass(OclConstraint constraint, EClass eClass) {
		if(constraint.getContextClass() == null) return true;
		URI uri = EcoreUtil.getURI(eClass);
		if(constraint.getContextClass().equals(uri.toString())) return true;
		return false;
	}
	
	public static List<OclConstraint> filter(OclConstraintSet constraintSet, Predicate<OclConstraint> filter) {
		return constraintSet.getConstraints().stream().filter(filter).toList();
	}
	
	public static List<OclConstraint> filterByRole(OclConstraintSet constraintSet, OclRole role) {
		return constraintSet.getConstraints().stream().filter(c -> role.equals(c.getRole())).toList();
	}
	
	public static List<OclConstraint> filterByRoleAndClass(OclConstraintSet constraintSet, OclRole role, EClass eClass) {
		return constraintSet.getConstraints().stream().filter(c -> role.equals(c.getRole()) && canHandleEClass(c, eClass)).toList();
	}
	


}
