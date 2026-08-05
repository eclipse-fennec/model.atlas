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

/**
 * Applicability checks used to narrow an {@link OclConstraintSet} down to the
 * constraints that may run against a given object.
 *
 * <p>
 * Both predicates are deliberately permissive: an unrestricted constraint set (no
 * target nsURIs) applies to every object, and a constraint without a context class
 * applies to every EClass. Callers therefore filter, they do not validate — an empty
 * result means "nothing to evaluate", not "invalid input".
 * </p>
 */
public final class ValidationHelper {

	private ValidationHelper() {
		// static utilities only
	}

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
