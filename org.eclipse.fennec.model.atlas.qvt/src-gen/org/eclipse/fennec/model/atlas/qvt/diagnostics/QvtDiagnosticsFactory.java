/*
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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.qvt.diagnostics;

import org.eclipse.emf.ecore.EFactory;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.QvtDiagnosticsPackage
 * @generated
 */
@ProviderType
public interface QvtDiagnosticsFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	QvtDiagnosticsFactory eINSTANCE = org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.QvtDiagnosticsFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Source Diagnostics</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Source Diagnostics</em>'.
	 * @generated
	 */
	SourceDiagnostics createSourceDiagnostics();

	/**
	 * Returns a new object of class '<em>Diagnostic Entry</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Diagnostic Entry</em>'.
	 * @generated
	 */
	DiagnosticEntry createDiagnosticEntry();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	QvtDiagnosticsPackage getQvtDiagnosticsPackage();

} //QvtDiagnosticsFactory
