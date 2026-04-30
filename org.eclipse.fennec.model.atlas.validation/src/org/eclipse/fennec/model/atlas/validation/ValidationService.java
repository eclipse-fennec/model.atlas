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
package org.eclipse.fennec.model.atlas.validation;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.validation.model.cocl.BatchValidationRequest;
import org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic;
import org.eclipse.fennec.model.atlas.validation.model.cocl.DerivedValidationRequest;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OperationValidationRequest;
import org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResponse;

/**
 * Service for validating EObjects using EMF and C-OCL constraints.
 *
 * <p>Throws {@link IllegalArgumentException} for invalid input (mapped to HTTP 400 by callers)
 * and {@link java.util.NoSuchElementException} for not-found cases (mapped to HTTP 404).
 *
 * @author ilenia
 * @since Apr 2026
 */
public interface ValidationService {

    Diagnostic validate(EObject eObject);

    ValidationResponse validateWithOcl(EObject eObject, String oclId, String scopeName, ResourceSet resourceSet);

    ValidationResponse derive(DerivedValidationRequest request, String oclId, String scopeName, ResourceSet resourceSet);

    ValidationResponse compute(OperationValidationRequest request);

    ValidationResponse validateBatch(BatchValidationRequest request, String scopeName, ResourceSet resourceSet);

    ValidationResponse filterBatch(BatchValidationRequest request, String scopeName, ResourceSet resourceSet);
}
