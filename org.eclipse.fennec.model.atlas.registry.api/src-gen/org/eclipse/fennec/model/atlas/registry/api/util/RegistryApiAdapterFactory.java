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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.registry.api.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.fennec.model.atlas.registry.api.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage
 * @generated
 */
public class RegistryApiAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static RegistryApiPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RegistryApiAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = RegistryApiPackage.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
	 * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject)object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RegistryApiSwitch<Adapter> modelSwitch =
		new RegistryApiSwitch<Adapter>() {
			@Override
			public Adapter caseRegistryWriter(RegistryWriter object) {
				return createRegistryWriterAdapter();
			}
			@Override
			public Adapter caseRegistryReader(RegistryReader object) {
				return createRegistryReaderAdapter();
			}
			@Override
			public Adapter caseObjectAddress(ObjectAddress object) {
				return createObjectAddressAdapter();
			}
			@Override
			public Adapter casePackageAddress(PackageAddress object) {
				return createPackageAddressAdapter();
			}
			@Override
			public Adapter casePrecondition(Precondition object) {
				return createPreconditionAdapter();
			}
			@Override
			public Adapter caseObjectWrite(ObjectWrite object) {
				return createObjectWriteAdapter();
			}
			@Override
			public Adapter caseObjectBatch(ObjectBatch object) {
				return createObjectBatchAdapter();
			}
			@Override
			public Adapter casePackageWrite(PackageWrite object) {
				return createPackageWriteAdapter();
			}
			@Override
			public Adapter casePackageBatch(PackageBatch object) {
				return createPackageBatchAdapter();
			}
			@Override
			public Adapter casePackagePropertiesUpdate(PackagePropertiesUpdate object) {
				return createPackagePropertiesUpdateAdapter();
			}
			@Override
			public Adapter casePackageSearch(PackageSearch object) {
				return createPackageSearchAdapter();
			}
			@Override
			public Adapter casePackageSearchResult(PackageSearchResult object) {
				return createPackageSearchResultAdapter();
			}
			@Override
			public Adapter caseWriteResult(WriteResult object) {
				return createWriteResultAdapter();
			}
			@Override
			public Adapter caseBatchItemResult(BatchItemResult object) {
				return createBatchItemResultAdapter();
			}
			@Override
			public Adapter caseBatchResult(BatchResult object) {
				return createBatchResultAdapter();
			}
			@Override
			public Adapter defaultCase(EObject object) {
				return createEObjectAdapter();
			}
		};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject)target);
	}


	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryWriter <em>Registry Writer</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryWriter
	 * @generated
	 */
	public Adapter createRegistryWriterAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryReader <em>Registry Reader</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryReader
	 * @generated
	 */
	public Adapter createRegistryReaderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectAddress <em>Object Address</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.model.atlas.registry.api.ObjectAddress
	 * @generated
	 */
	public Adapter createObjectAddressAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.model.atlas.registry.api.PackageAddress <em>Package Address</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageAddress
	 * @generated
	 */
	public Adapter createPackageAddressAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.model.atlas.registry.api.Precondition <em>Precondition</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.model.atlas.registry.api.Precondition
	 * @generated
	 */
	public Adapter createPreconditionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectWrite <em>Object Write</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.model.atlas.registry.api.ObjectWrite
	 * @generated
	 */
	public Adapter createObjectWriteAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectBatch <em>Object Batch</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.model.atlas.registry.api.ObjectBatch
	 * @generated
	 */
	public Adapter createObjectBatchAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite <em>Package Write</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageWrite
	 * @generated
	 */
	public Adapter createPackageWriteAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.model.atlas.registry.api.PackageBatch <em>Package Batch</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageBatch
	 * @generated
	 */
	public Adapter createPackageBatchAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.model.atlas.registry.api.PackagePropertiesUpdate <em>Package Properties Update</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackagePropertiesUpdate
	 * @generated
	 */
	public Adapter createPackagePropertiesUpdateAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch <em>Package Search</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageSearch
	 * @generated
	 */
	public Adapter createPackageSearchAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearchResult <em>Package Search Result</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageSearchResult
	 * @generated
	 */
	public Adapter createPackageSearchResultAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.model.atlas.registry.api.WriteResult <em>Write Result</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.model.atlas.registry.api.WriteResult
	 * @generated
	 */
	public Adapter createWriteResultAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult <em>Batch Item Result</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.model.atlas.registry.api.BatchItemResult
	 * @generated
	 */
	public Adapter createBatchItemResultAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.model.atlas.registry.api.BatchResult <em>Batch Result</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.model.atlas.registry.api.BatchResult
	 * @generated
	 */
	public Adapter createBatchResultAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} //RegistryApiAdapterFactory
