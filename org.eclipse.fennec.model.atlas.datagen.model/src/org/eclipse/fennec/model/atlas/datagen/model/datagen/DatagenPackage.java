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
 *       Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.datagen.model.datagen;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.fennec.emf.osgi.annotation.provide.EPackage;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Data Generator Metamodell zur Konfiguration automatischer Testdaten-Generierung fuer bestehende Metamodelle. Nutzt FakerJS als Datenquelle.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenFactory
 * @model kind="package"
 *        annotation="http://www.eclipse.org/emf/2002/GenModel oSGiCompatible='true' basePackage='org.eclipse.fennec.model.atlas.datagen.model' copyrightText='Copyright (c) 2026 Contributors to the Eclipse Foundation.\n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n  Data In Motion Consulting - initial implementation' resource='XMI' fileExtensions='datagen'"
 * @generated
 */
@ProviderType
@EPackage(uri = DatagenPackage.eNS_URI, genModel = "/model/datagen.genmodel", genModelSourceLocations = {"model/datagen.genmodel","org.eclipse.fennec.model.atlas.datagen.model/model/datagen.genmodel"}, ecore = "/model/datagen.ecore", ecoreSourceLocations = "/model/datagen.ecore")
public interface DatagenPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "datagen";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.gme.org/datagen/1.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "datagen";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	DatagenPackage eINSTANCE = org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DatagenPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DataGenConfigImpl <em>Data Gen Config</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DataGenConfigImpl
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DatagenPackageImpl#getDataGenConfig()
	 * @generated
	 */
	int DATA_GEN_CONFIG = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_GEN_CONFIG__NAME = 0;

	/**
	 * The feature id for the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_GEN_CONFIG__VERSION = 1;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_GEN_CONFIG__DESCRIPTION = 2;

	/**
	 * The feature id for the '<em><b>Seed</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_GEN_CONFIG__SEED = 3;

	/**
	 * The feature id for the '<em><b>Locale</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_GEN_CONFIG__LOCALE = 4;

	/**
	 * The feature id for the '<em><b>Target Model Ns UR Is</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_GEN_CONFIG__TARGET_MODEL_NS_UR_IS = 5;

	/**
	 * The feature id for the '<em><b>Class Configs</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_GEN_CONFIG__CLASS_CONFIGS = 6;

	/**
	 * The feature id for the '<em><b>Custom Generators</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_GEN_CONFIG__CUSTOM_GENERATORS = 7;

	/**
	 * The number of structural features of the '<em>Data Gen Config</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_GEN_CONFIG_FEATURE_COUNT = 8;

	/**
	 * The number of operations of the '<em>Data Gen Config</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_GEN_CONFIG_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.ClassGenConfigImpl <em>Class Gen Config</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.ClassGenConfigImpl
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DatagenPackageImpl#getClassGenConfig()
	 * @generated
	 */
	int CLASS_GEN_CONFIG = 1;

	/**
	 * The feature id for the '<em><b>Context Class</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_GEN_CONFIG__CONTEXT_CLASS = 0;

	/**
	 * The feature id for the '<em><b>Instance Count</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_GEN_CONFIG__INSTANCE_COUNT = 1;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_GEN_CONFIG__ENABLED = 2;

	/**
	 * The feature id for the '<em><b>Attribute Gens</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_GEN_CONFIG__ATTRIBUTE_GENS = 3;

	/**
	 * The feature id for the '<em><b>Reference Gens</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_GEN_CONFIG__REFERENCE_GENS = 4;

	/**
	 * The number of structural features of the '<em>Class Gen Config</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_GEN_CONFIG_FEATURE_COUNT = 5;

	/**
	 * The number of operations of the '<em>Class Gen Config</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_GEN_CONFIG_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.AttributeGenConfigImpl <em>Attribute Gen Config</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.AttributeGenConfigImpl
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DatagenPackageImpl#getAttributeGenConfig()
	 * @generated
	 */
	int ATTRIBUTE_GEN_CONFIG = 2;

	/**
	 * The feature id for the '<em><b>Feature Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATTRIBUTE_GEN_CONFIG__FEATURE_NAME = 0;

	/**
	 * The feature id for the '<em><b>Generator Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATTRIBUTE_GEN_CONFIG__GENERATOR_KEY = 1;

	/**
	 * The feature id for the '<em><b>Generator Args</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATTRIBUTE_GEN_CONFIG__GENERATOR_ARGS = 2;

	/**
	 * The feature id for the '<em><b>Unique</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATTRIBUTE_GEN_CONFIG__UNIQUE = 3;

	/**
	 * The feature id for the '<em><b>Static Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATTRIBUTE_GEN_CONFIG__STATIC_VALUE = 4;

	/**
	 * The feature id for the '<em><b>Template</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATTRIBUTE_GEN_CONFIG__TEMPLATE = 5;

	/**
	 * The number of structural features of the '<em>Attribute Gen Config</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATTRIBUTE_GEN_CONFIG_FEATURE_COUNT = 6;

	/**
	 * The number of operations of the '<em>Attribute Gen Config</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATTRIBUTE_GEN_CONFIG_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.ReferenceGenConfigImpl <em>Reference Gen Config</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.ReferenceGenConfigImpl
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DatagenPackageImpl#getReferenceGenConfig()
	 * @generated
	 */
	int REFERENCE_GEN_CONFIG = 3;

	/**
	 * The feature id for the '<em><b>Feature Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_GEN_CONFIG__FEATURE_NAME = 0;

	/**
	 * The feature id for the '<em><b>Strategy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_GEN_CONFIG__STRATEGY = 1;

	/**
	 * The feature id for the '<em><b>Target Class Filter</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_GEN_CONFIG__TARGET_CLASS_FILTER = 2;

	/**
	 * The feature id for the '<em><b>Min Count</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_GEN_CONFIG__MIN_COUNT = 3;

	/**
	 * The feature id for the '<em><b>Max Count</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_GEN_CONFIG__MAX_COUNT = 4;

	/**
	 * The number of structural features of the '<em>Reference Gen Config</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_GEN_CONFIG_FEATURE_COUNT = 5;

	/**
	 * The number of operations of the '<em>Reference Gen Config</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_GEN_CONFIG_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.CustomGeneratorDefImpl <em>Custom Generator Def</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.CustomGeneratorDefImpl
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DatagenPackageImpl#getCustomGeneratorDef()
	 * @generated
	 */
	int CUSTOM_GENERATOR_DEF = 4;

	/**
	 * The feature id for the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUSTOM_GENERATOR_DEF__KEY = 0;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUSTOM_GENERATOR_DEF__LABEL = 1;

	/**
	 * The feature id for the '<em><b>Expression</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUSTOM_GENERATOR_DEF__EXPRESSION = 2;

	/**
	 * The feature id for the '<em><b>Category</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUSTOM_GENERATOR_DEF__CATEGORY = 3;

	/**
	 * The number of structural features of the '<em>Custom Generator Def</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUSTOM_GENERATOR_DEF_FEATURE_COUNT = 4;

	/**
	 * The number of operations of the '<em>Custom Generator Def</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUSTOM_GENERATOR_DEF_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DataGenResultImpl <em>Data Gen Result</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DataGenResultImpl
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DatagenPackageImpl#getDataGenResult()
	 * @generated
	 */
	int DATA_GEN_RESULT = 5;

	/**
	 * The feature id for the '<em><b>Results</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_GEN_RESULT__RESULTS = 0;

	/**
	 * The number of structural features of the '<em>Data Gen Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_GEN_RESULT_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Data Gen Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_GEN_RESULT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceStrategy <em>Reference Strategy</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceStrategy
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DatagenPackageImpl#getReferenceStrategy()
	 * @generated
	 */
	int REFERENCE_STRATEGY = 6;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig <em>Data Gen Config</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Data Gen Config</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig
	 * @generated
	 */
	EClass getDataGenConfig();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getName()
	 * @see #getDataGenConfig()
	 * @generated
	 */
	EAttribute getDataGenConfig_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getVersion <em>Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Version</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getVersion()
	 * @see #getDataGenConfig()
	 * @generated
	 */
	EAttribute getDataGenConfig_Version();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getDescription()
	 * @see #getDataGenConfig()
	 * @generated
	 */
	EAttribute getDataGenConfig_Description();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getSeed <em>Seed</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Seed</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getSeed()
	 * @see #getDataGenConfig()
	 * @generated
	 */
	EAttribute getDataGenConfig_Seed();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getLocale <em>Locale</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Locale</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getLocale()
	 * @see #getDataGenConfig()
	 * @generated
	 */
	EAttribute getDataGenConfig_Locale();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getTargetModelNsURIs <em>Target Model Ns UR Is</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Target Model Ns UR Is</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getTargetModelNsURIs()
	 * @see #getDataGenConfig()
	 * @generated
	 */
	EAttribute getDataGenConfig_TargetModelNsURIs();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getClassConfigs <em>Class Configs</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Class Configs</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getClassConfigs()
	 * @see #getDataGenConfig()
	 * @generated
	 */
	EReference getDataGenConfig_ClassConfigs();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getCustomGenerators <em>Custom Generators</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Custom Generators</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getCustomGenerators()
	 * @see #getDataGenConfig()
	 * @generated
	 */
	EReference getDataGenConfig_CustomGenerators();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig <em>Class Gen Config</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Class Gen Config</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig
	 * @generated
	 */
	EClass getClassGenConfig();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig#getContextClass <em>Context Class</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Context Class</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig#getContextClass()
	 * @see #getClassGenConfig()
	 * @generated
	 */
	EAttribute getClassGenConfig_ContextClass();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig#getInstanceCount <em>Instance Count</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Instance Count</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig#getInstanceCount()
	 * @see #getClassGenConfig()
	 * @generated
	 */
	EAttribute getClassGenConfig_InstanceCount();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig#isEnabled <em>Enabled</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Enabled</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig#isEnabled()
	 * @see #getClassGenConfig()
	 * @generated
	 */
	EAttribute getClassGenConfig_Enabled();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig#getAttributeGens <em>Attribute Gens</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Attribute Gens</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig#getAttributeGens()
	 * @see #getClassGenConfig()
	 * @generated
	 */
	EReference getClassGenConfig_AttributeGens();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig#getReferenceGens <em>Reference Gens</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Reference Gens</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig#getReferenceGens()
	 * @see #getClassGenConfig()
	 * @generated
	 */
	EReference getClassGenConfig_ReferenceGens();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig <em>Attribute Gen Config</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Attribute Gen Config</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig
	 * @generated
	 */
	EClass getAttributeGenConfig();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#getFeatureName <em>Feature Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Feature Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#getFeatureName()
	 * @see #getAttributeGenConfig()
	 * @generated
	 */
	EAttribute getAttributeGenConfig_FeatureName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#getGeneratorKey <em>Generator Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Generator Key</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#getGeneratorKey()
	 * @see #getAttributeGenConfig()
	 * @generated
	 */
	EAttribute getAttributeGenConfig_GeneratorKey();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#getGeneratorArgs <em>Generator Args</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Generator Args</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#getGeneratorArgs()
	 * @see #getAttributeGenConfig()
	 * @generated
	 */
	EAttribute getAttributeGenConfig_GeneratorArgs();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#isUnique <em>Unique</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Unique</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#isUnique()
	 * @see #getAttributeGenConfig()
	 * @generated
	 */
	EAttribute getAttributeGenConfig_Unique();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#getStaticValue <em>Static Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Static Value</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#getStaticValue()
	 * @see #getAttributeGenConfig()
	 * @generated
	 */
	EAttribute getAttributeGenConfig_StaticValue();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#getTemplate <em>Template</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Template</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig#getTemplate()
	 * @see #getAttributeGenConfig()
	 * @generated
	 */
	EAttribute getAttributeGenConfig_Template();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig <em>Reference Gen Config</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Reference Gen Config</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig
	 * @generated
	 */
	EClass getReferenceGenConfig();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig#getFeatureName <em>Feature Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Feature Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig#getFeatureName()
	 * @see #getReferenceGenConfig()
	 * @generated
	 */
	EAttribute getReferenceGenConfig_FeatureName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig#getStrategy <em>Strategy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Strategy</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig#getStrategy()
	 * @see #getReferenceGenConfig()
	 * @generated
	 */
	EAttribute getReferenceGenConfig_Strategy();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig#getTargetClassFilter <em>Target Class Filter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Target Class Filter</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig#getTargetClassFilter()
	 * @see #getReferenceGenConfig()
	 * @generated
	 */
	EAttribute getReferenceGenConfig_TargetClassFilter();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig#getMinCount <em>Min Count</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Min Count</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig#getMinCount()
	 * @see #getReferenceGenConfig()
	 * @generated
	 */
	EAttribute getReferenceGenConfig_MinCount();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig#getMaxCount <em>Max Count</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Max Count</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig#getMaxCount()
	 * @see #getReferenceGenConfig()
	 * @generated
	 */
	EAttribute getReferenceGenConfig_MaxCount();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.CustomGeneratorDef <em>Custom Generator Def</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Custom Generator Def</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.CustomGeneratorDef
	 * @generated
	 */
	EClass getCustomGeneratorDef();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.CustomGeneratorDef#getKey <em>Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.CustomGeneratorDef#getKey()
	 * @see #getCustomGeneratorDef()
	 * @generated
	 */
	EAttribute getCustomGeneratorDef_Key();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.CustomGeneratorDef#getLabel <em>Label</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Label</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.CustomGeneratorDef#getLabel()
	 * @see #getCustomGeneratorDef()
	 * @generated
	 */
	EAttribute getCustomGeneratorDef_Label();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.CustomGeneratorDef#getExpression <em>Expression</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Expression</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.CustomGeneratorDef#getExpression()
	 * @see #getCustomGeneratorDef()
	 * @generated
	 */
	EAttribute getCustomGeneratorDef_Expression();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.CustomGeneratorDef#getCategory <em>Category</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Category</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.CustomGeneratorDef#getCategory()
	 * @see #getCustomGeneratorDef()
	 * @generated
	 */
	EAttribute getCustomGeneratorDef_Category();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenResult <em>Data Gen Result</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Data Gen Result</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenResult
	 * @generated
	 */
	EClass getDataGenResult();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenResult#getResults <em>Results</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Results</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenResult#getResults()
	 * @see #getDataGenResult()
	 * @generated
	 */
	EReference getDataGenResult_Results();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceStrategy <em>Reference Strategy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Reference Strategy</em>'.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceStrategy
	 * @generated
	 */
	EEnum getReferenceStrategy();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	DatagenFactory getDatagenFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DataGenConfigImpl <em>Data Gen Config</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DataGenConfigImpl
		 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DatagenPackageImpl#getDataGenConfig()
		 * @generated
		 */
		EClass DATA_GEN_CONFIG = eINSTANCE.getDataGenConfig();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_GEN_CONFIG__NAME = eINSTANCE.getDataGenConfig_Name();

		/**
		 * The meta object literal for the '<em><b>Version</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_GEN_CONFIG__VERSION = eINSTANCE.getDataGenConfig_Version();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_GEN_CONFIG__DESCRIPTION = eINSTANCE.getDataGenConfig_Description();

		/**
		 * The meta object literal for the '<em><b>Seed</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_GEN_CONFIG__SEED = eINSTANCE.getDataGenConfig_Seed();

		/**
		 * The meta object literal for the '<em><b>Locale</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_GEN_CONFIG__LOCALE = eINSTANCE.getDataGenConfig_Locale();

		/**
		 * The meta object literal for the '<em><b>Target Model Ns UR Is</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_GEN_CONFIG__TARGET_MODEL_NS_UR_IS = eINSTANCE.getDataGenConfig_TargetModelNsURIs();

		/**
		 * The meta object literal for the '<em><b>Class Configs</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DATA_GEN_CONFIG__CLASS_CONFIGS = eINSTANCE.getDataGenConfig_ClassConfigs();

		/**
		 * The meta object literal for the '<em><b>Custom Generators</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DATA_GEN_CONFIG__CUSTOM_GENERATORS = eINSTANCE.getDataGenConfig_CustomGenerators();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.ClassGenConfigImpl <em>Class Gen Config</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.ClassGenConfigImpl
		 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DatagenPackageImpl#getClassGenConfig()
		 * @generated
		 */
		EClass CLASS_GEN_CONFIG = eINSTANCE.getClassGenConfig();

		/**
		 * The meta object literal for the '<em><b>Context Class</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CLASS_GEN_CONFIG__CONTEXT_CLASS = eINSTANCE.getClassGenConfig_ContextClass();

		/**
		 * The meta object literal for the '<em><b>Instance Count</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CLASS_GEN_CONFIG__INSTANCE_COUNT = eINSTANCE.getClassGenConfig_InstanceCount();

		/**
		 * The meta object literal for the '<em><b>Enabled</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CLASS_GEN_CONFIG__ENABLED = eINSTANCE.getClassGenConfig_Enabled();

		/**
		 * The meta object literal for the '<em><b>Attribute Gens</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CLASS_GEN_CONFIG__ATTRIBUTE_GENS = eINSTANCE.getClassGenConfig_AttributeGens();

		/**
		 * The meta object literal for the '<em><b>Reference Gens</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CLASS_GEN_CONFIG__REFERENCE_GENS = eINSTANCE.getClassGenConfig_ReferenceGens();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.AttributeGenConfigImpl <em>Attribute Gen Config</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.AttributeGenConfigImpl
		 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DatagenPackageImpl#getAttributeGenConfig()
		 * @generated
		 */
		EClass ATTRIBUTE_GEN_CONFIG = eINSTANCE.getAttributeGenConfig();

		/**
		 * The meta object literal for the '<em><b>Feature Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ATTRIBUTE_GEN_CONFIG__FEATURE_NAME = eINSTANCE.getAttributeGenConfig_FeatureName();

		/**
		 * The meta object literal for the '<em><b>Generator Key</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ATTRIBUTE_GEN_CONFIG__GENERATOR_KEY = eINSTANCE.getAttributeGenConfig_GeneratorKey();

		/**
		 * The meta object literal for the '<em><b>Generator Args</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ATTRIBUTE_GEN_CONFIG__GENERATOR_ARGS = eINSTANCE.getAttributeGenConfig_GeneratorArgs();

		/**
		 * The meta object literal for the '<em><b>Unique</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ATTRIBUTE_GEN_CONFIG__UNIQUE = eINSTANCE.getAttributeGenConfig_Unique();

		/**
		 * The meta object literal for the '<em><b>Static Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ATTRIBUTE_GEN_CONFIG__STATIC_VALUE = eINSTANCE.getAttributeGenConfig_StaticValue();

		/**
		 * The meta object literal for the '<em><b>Template</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ATTRIBUTE_GEN_CONFIG__TEMPLATE = eINSTANCE.getAttributeGenConfig_Template();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.ReferenceGenConfigImpl <em>Reference Gen Config</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.ReferenceGenConfigImpl
		 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DatagenPackageImpl#getReferenceGenConfig()
		 * @generated
		 */
		EClass REFERENCE_GEN_CONFIG = eINSTANCE.getReferenceGenConfig();

		/**
		 * The meta object literal for the '<em><b>Feature Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REFERENCE_GEN_CONFIG__FEATURE_NAME = eINSTANCE.getReferenceGenConfig_FeatureName();

		/**
		 * The meta object literal for the '<em><b>Strategy</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REFERENCE_GEN_CONFIG__STRATEGY = eINSTANCE.getReferenceGenConfig_Strategy();

		/**
		 * The meta object literal for the '<em><b>Target Class Filter</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REFERENCE_GEN_CONFIG__TARGET_CLASS_FILTER = eINSTANCE.getReferenceGenConfig_TargetClassFilter();

		/**
		 * The meta object literal for the '<em><b>Min Count</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REFERENCE_GEN_CONFIG__MIN_COUNT = eINSTANCE.getReferenceGenConfig_MinCount();

		/**
		 * The meta object literal for the '<em><b>Max Count</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REFERENCE_GEN_CONFIG__MAX_COUNT = eINSTANCE.getReferenceGenConfig_MaxCount();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.CustomGeneratorDefImpl <em>Custom Generator Def</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.CustomGeneratorDefImpl
		 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DatagenPackageImpl#getCustomGeneratorDef()
		 * @generated
		 */
		EClass CUSTOM_GENERATOR_DEF = eINSTANCE.getCustomGeneratorDef();

		/**
		 * The meta object literal for the '<em><b>Key</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CUSTOM_GENERATOR_DEF__KEY = eINSTANCE.getCustomGeneratorDef_Key();

		/**
		 * The meta object literal for the '<em><b>Label</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CUSTOM_GENERATOR_DEF__LABEL = eINSTANCE.getCustomGeneratorDef_Label();

		/**
		 * The meta object literal for the '<em><b>Expression</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CUSTOM_GENERATOR_DEF__EXPRESSION = eINSTANCE.getCustomGeneratorDef_Expression();

		/**
		 * The meta object literal for the '<em><b>Category</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CUSTOM_GENERATOR_DEF__CATEGORY = eINSTANCE.getCustomGeneratorDef_Category();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DataGenResultImpl <em>Data Gen Result</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DataGenResultImpl
		 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DatagenPackageImpl#getDataGenResult()
		 * @generated
		 */
		EClass DATA_GEN_RESULT = eINSTANCE.getDataGenResult();

		/**
		 * The meta object literal for the '<em><b>Results</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DATA_GEN_RESULT__RESULTS = eINSTANCE.getDataGenResult_Results();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceStrategy <em>Reference Strategy</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceStrategy
		 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DatagenPackageImpl#getReferenceStrategy()
		 * @generated
		 */
		EEnum REFERENCE_STRATEGY = eINSTANCE.getReferenceStrategy();

	}

} //DatagenPackage
