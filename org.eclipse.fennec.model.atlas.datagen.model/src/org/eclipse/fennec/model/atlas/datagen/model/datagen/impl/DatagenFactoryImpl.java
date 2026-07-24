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
 *       Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.datagen.model.datagen.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.fennec.model.atlas.datagen.model.datagen.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class DatagenFactoryImpl extends EFactoryImpl implements DatagenFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static DatagenFactory init() {
		try {
			DatagenFactory theDatagenFactory = (DatagenFactory)EPackage.Registry.INSTANCE.getEFactory(DatagenPackage.eNS_URI);
			if (theDatagenFactory != null) {
				return theDatagenFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new DatagenFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DatagenFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case DatagenPackage.DATA_GEN_CONFIG: return createDataGenConfig();
			case DatagenPackage.CLASS_GEN_CONFIG: return createClassGenConfig();
			case DatagenPackage.ATTRIBUTE_GEN_CONFIG: return createAttributeGenConfig();
			case DatagenPackage.REFERENCE_GEN_CONFIG: return createReferenceGenConfig();
			case DatagenPackage.CUSTOM_GENERATOR_DEF: return createCustomGeneratorDef();
			case DatagenPackage.DATA_GEN_RESULT: return createDataGenResult();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case DatagenPackage.REFERENCE_STRATEGY:
				return createReferenceStrategyFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case DatagenPackage.REFERENCE_STRATEGY:
				return convertReferenceStrategyToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DataGenConfig createDataGenConfig() {
		DataGenConfigImpl dataGenConfig = new DataGenConfigImpl();
		return dataGenConfig;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ClassGenConfig createClassGenConfig() {
		ClassGenConfigImpl classGenConfig = new ClassGenConfigImpl();
		return classGenConfig;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AttributeGenConfig createAttributeGenConfig() {
		AttributeGenConfigImpl attributeGenConfig = new AttributeGenConfigImpl();
		return attributeGenConfig;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ReferenceGenConfig createReferenceGenConfig() {
		ReferenceGenConfigImpl referenceGenConfig = new ReferenceGenConfigImpl();
		return referenceGenConfig;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CustomGeneratorDef createCustomGeneratorDef() {
		CustomGeneratorDefImpl customGeneratorDef = new CustomGeneratorDefImpl();
		return customGeneratorDef;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DataGenResult createDataGenResult() {
		DataGenResultImpl dataGenResult = new DataGenResultImpl();
		return dataGenResult;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ReferenceStrategy createReferenceStrategyFromString(EDataType eDataType, String initialValue) {
		ReferenceStrategy result = ReferenceStrategy.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertReferenceStrategyToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DatagenPackage getDatagenPackage() {
		return (DatagenPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static DatagenPackage getPackage() {
		return DatagenPackage.eINSTANCE;
	}

} //DatagenFactoryImpl
