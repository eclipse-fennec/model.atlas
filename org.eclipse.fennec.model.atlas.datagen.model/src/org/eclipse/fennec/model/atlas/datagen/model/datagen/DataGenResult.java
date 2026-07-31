/*
 */
package org.eclipse.fennec.model.atlas.datagen.model.datagen;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Data Gen Result</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenResult#getResults <em>Results</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getDataGenResult()
 * @model
 * @generated
 */
@ProviderType
public interface DataGenResult extends EObject {
	/**
	 * Returns the value of the '<em><b>Results</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.emf.ecore.EObject}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Results</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getDataGenResult_Results()
	 * @model containment="true"
	 * @generated
	 */
	EList<EObject> getResults();

} // DataGenResult
