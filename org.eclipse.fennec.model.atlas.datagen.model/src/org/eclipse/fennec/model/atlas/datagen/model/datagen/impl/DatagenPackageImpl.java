/**
 */
package org.eclipse.fennec.model.atlas.datagen.model.datagen.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.CustomGeneratorDef;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenResult;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenFactory;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceStrategy;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class DatagenPackageImpl extends EPackageImpl implements DatagenPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dataGenConfigEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass classGenConfigEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass attributeGenConfigEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass referenceGenConfigEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass customGeneratorDefEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dataGenResultEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum referenceStrategyEEnum = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private DatagenPackageImpl() {
		super(eNS_URI, DatagenFactory.eINSTANCE);
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link DatagenPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static DatagenPackage init() {
		if (isInited) return (DatagenPackage)EPackage.Registry.INSTANCE.getEPackage(DatagenPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredDatagenPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		DatagenPackageImpl theDatagenPackage = registeredDatagenPackage instanceof DatagenPackageImpl ? (DatagenPackageImpl)registeredDatagenPackage : new DatagenPackageImpl();

		isInited = true;

		// Create package meta-data objects
		theDatagenPackage.createPackageContents();

		// Initialize created meta-data
		theDatagenPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theDatagenPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(DatagenPackage.eNS_URI, theDatagenPackage);
		return theDatagenPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDataGenConfig() {
		return dataGenConfigEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataGenConfig_Name() {
		return (EAttribute)dataGenConfigEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataGenConfig_Version() {
		return (EAttribute)dataGenConfigEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataGenConfig_Description() {
		return (EAttribute)dataGenConfigEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataGenConfig_Seed() {
		return (EAttribute)dataGenConfigEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataGenConfig_Locale() {
		return (EAttribute)dataGenConfigEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataGenConfig_TargetModelNsURIs() {
		return (EAttribute)dataGenConfigEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDataGenConfig_ClassConfigs() {
		return (EReference)dataGenConfigEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDataGenConfig_CustomGenerators() {
		return (EReference)dataGenConfigEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getClassGenConfig() {
		return classGenConfigEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getClassGenConfig_ContextClass() {
		return (EAttribute)classGenConfigEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getClassGenConfig_InstanceCount() {
		return (EAttribute)classGenConfigEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getClassGenConfig_Enabled() {
		return (EAttribute)classGenConfigEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getClassGenConfig_AttributeGens() {
		return (EReference)classGenConfigEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getClassGenConfig_ReferenceGens() {
		return (EReference)classGenConfigEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAttributeGenConfig() {
		return attributeGenConfigEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttributeGenConfig_FeatureName() {
		return (EAttribute)attributeGenConfigEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttributeGenConfig_GeneratorKey() {
		return (EAttribute)attributeGenConfigEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttributeGenConfig_GeneratorArgs() {
		return (EAttribute)attributeGenConfigEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttributeGenConfig_Unique() {
		return (EAttribute)attributeGenConfigEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttributeGenConfig_StaticValue() {
		return (EAttribute)attributeGenConfigEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttributeGenConfig_Template() {
		return (EAttribute)attributeGenConfigEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getReferenceGenConfig() {
		return referenceGenConfigEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getReferenceGenConfig_FeatureName() {
		return (EAttribute)referenceGenConfigEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getReferenceGenConfig_Strategy() {
		return (EAttribute)referenceGenConfigEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getReferenceGenConfig_TargetClassFilter() {
		return (EAttribute)referenceGenConfigEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getReferenceGenConfig_MinCount() {
		return (EAttribute)referenceGenConfigEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getReferenceGenConfig_MaxCount() {
		return (EAttribute)referenceGenConfigEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCustomGeneratorDef() {
		return customGeneratorDefEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCustomGeneratorDef_Key() {
		return (EAttribute)customGeneratorDefEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCustomGeneratorDef_Label() {
		return (EAttribute)customGeneratorDefEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCustomGeneratorDef_Expression() {
		return (EAttribute)customGeneratorDefEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCustomGeneratorDef_Category() {
		return (EAttribute)customGeneratorDefEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDataGenResult() {
		return dataGenResultEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDataGenResult_Results() {
		return (EReference)dataGenResultEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getReferenceStrategy() {
		return referenceStrategyEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DatagenFactory getDatagenFactory() {
		return (DatagenFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		dataGenConfigEClass = createEClass(DATA_GEN_CONFIG);
		createEAttribute(dataGenConfigEClass, DATA_GEN_CONFIG__NAME);
		createEAttribute(dataGenConfigEClass, DATA_GEN_CONFIG__VERSION);
		createEAttribute(dataGenConfigEClass, DATA_GEN_CONFIG__DESCRIPTION);
		createEAttribute(dataGenConfigEClass, DATA_GEN_CONFIG__SEED);
		createEAttribute(dataGenConfigEClass, DATA_GEN_CONFIG__LOCALE);
		createEAttribute(dataGenConfigEClass, DATA_GEN_CONFIG__TARGET_MODEL_NS_UR_IS);
		createEReference(dataGenConfigEClass, DATA_GEN_CONFIG__CLASS_CONFIGS);
		createEReference(dataGenConfigEClass, DATA_GEN_CONFIG__CUSTOM_GENERATORS);

		classGenConfigEClass = createEClass(CLASS_GEN_CONFIG);
		createEAttribute(classGenConfigEClass, CLASS_GEN_CONFIG__CONTEXT_CLASS);
		createEAttribute(classGenConfigEClass, CLASS_GEN_CONFIG__INSTANCE_COUNT);
		createEAttribute(classGenConfigEClass, CLASS_GEN_CONFIG__ENABLED);
		createEReference(classGenConfigEClass, CLASS_GEN_CONFIG__ATTRIBUTE_GENS);
		createEReference(classGenConfigEClass, CLASS_GEN_CONFIG__REFERENCE_GENS);

		attributeGenConfigEClass = createEClass(ATTRIBUTE_GEN_CONFIG);
		createEAttribute(attributeGenConfigEClass, ATTRIBUTE_GEN_CONFIG__FEATURE_NAME);
		createEAttribute(attributeGenConfigEClass, ATTRIBUTE_GEN_CONFIG__GENERATOR_KEY);
		createEAttribute(attributeGenConfigEClass, ATTRIBUTE_GEN_CONFIG__GENERATOR_ARGS);
		createEAttribute(attributeGenConfigEClass, ATTRIBUTE_GEN_CONFIG__UNIQUE);
		createEAttribute(attributeGenConfigEClass, ATTRIBUTE_GEN_CONFIG__STATIC_VALUE);
		createEAttribute(attributeGenConfigEClass, ATTRIBUTE_GEN_CONFIG__TEMPLATE);

		referenceGenConfigEClass = createEClass(REFERENCE_GEN_CONFIG);
		createEAttribute(referenceGenConfigEClass, REFERENCE_GEN_CONFIG__FEATURE_NAME);
		createEAttribute(referenceGenConfigEClass, REFERENCE_GEN_CONFIG__STRATEGY);
		createEAttribute(referenceGenConfigEClass, REFERENCE_GEN_CONFIG__TARGET_CLASS_FILTER);
		createEAttribute(referenceGenConfigEClass, REFERENCE_GEN_CONFIG__MIN_COUNT);
		createEAttribute(referenceGenConfigEClass, REFERENCE_GEN_CONFIG__MAX_COUNT);

		customGeneratorDefEClass = createEClass(CUSTOM_GENERATOR_DEF);
		createEAttribute(customGeneratorDefEClass, CUSTOM_GENERATOR_DEF__KEY);
		createEAttribute(customGeneratorDefEClass, CUSTOM_GENERATOR_DEF__LABEL);
		createEAttribute(customGeneratorDefEClass, CUSTOM_GENERATOR_DEF__EXPRESSION);
		createEAttribute(customGeneratorDefEClass, CUSTOM_GENERATOR_DEF__CATEGORY);

		dataGenResultEClass = createEClass(DATA_GEN_RESULT);
		createEReference(dataGenResultEClass, DATA_GEN_RESULT__RESULTS);

		// Create enums
		referenceStrategyEEnum = createEEnum(REFERENCE_STRATEGY);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes

		// Initialize classes, features, and operations; add parameters
		initEClass(dataGenConfigEClass, DataGenConfig.class, "DataGenConfig", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDataGenConfig_Name(), ecorePackage.getEString(), "name", null, 1, 1, DataGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataGenConfig_Version(), ecorePackage.getEString(), "version", "1.0", 0, 1, DataGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataGenConfig_Description(), ecorePackage.getEString(), "description", null, 0, 1, DataGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataGenConfig_Seed(), ecorePackage.getEInt(), "seed", "0", 0, 1, DataGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataGenConfig_Locale(), ecorePackage.getEString(), "locale", "de", 0, 1, DataGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataGenConfig_TargetModelNsURIs(), ecorePackage.getEString(), "targetModelNsURIs", null, 0, -1, DataGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDataGenConfig_ClassConfigs(), this.getClassGenConfig(), null, "classConfigs", null, 0, -1, DataGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDataGenConfig_CustomGenerators(), this.getCustomGeneratorDef(), null, "customGenerators", null, 0, -1, DataGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(classGenConfigEClass, ClassGenConfig.class, "ClassGenConfig", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getClassGenConfig_ContextClass(), ecorePackage.getEString(), "contextClass", null, 1, 1, ClassGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getClassGenConfig_InstanceCount(), ecorePackage.getEInt(), "instanceCount", "10", 1, 1, ClassGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getClassGenConfig_Enabled(), ecorePackage.getEBoolean(), "enabled", "true", 0, 1, ClassGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getClassGenConfig_AttributeGens(), this.getAttributeGenConfig(), null, "attributeGens", null, 0, -1, ClassGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getClassGenConfig_ReferenceGens(), this.getReferenceGenConfig(), null, "referenceGens", null, 0, -1, ClassGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(attributeGenConfigEClass, AttributeGenConfig.class, "AttributeGenConfig", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getAttributeGenConfig_FeatureName(), ecorePackage.getEString(), "featureName", null, 1, 1, AttributeGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAttributeGenConfig_GeneratorKey(), ecorePackage.getEString(), "generatorKey", null, 1, 1, AttributeGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAttributeGenConfig_GeneratorArgs(), ecorePackage.getEString(), "generatorArgs", null, 0, 1, AttributeGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAttributeGenConfig_Unique(), ecorePackage.getEBoolean(), "unique", "false", 0, 1, AttributeGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAttributeGenConfig_StaticValue(), ecorePackage.getEString(), "staticValue", null, 0, 1, AttributeGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAttributeGenConfig_Template(), ecorePackage.getEString(), "template", null, 0, 1, AttributeGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(referenceGenConfigEClass, ReferenceGenConfig.class, "ReferenceGenConfig", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getReferenceGenConfig_FeatureName(), ecorePackage.getEString(), "featureName", null, 1, 1, ReferenceGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getReferenceGenConfig_Strategy(), this.getReferenceStrategy(), "strategy", "RANDOM", 1, 1, ReferenceGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getReferenceGenConfig_TargetClassFilter(), ecorePackage.getEString(), "targetClassFilter", null, 0, 1, ReferenceGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getReferenceGenConfig_MinCount(), ecorePackage.getEInt(), "minCount", "0", 0, 1, ReferenceGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getReferenceGenConfig_MaxCount(), ecorePackage.getEInt(), "maxCount", "1", 0, 1, ReferenceGenConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(customGeneratorDefEClass, CustomGeneratorDef.class, "CustomGeneratorDef", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getCustomGeneratorDef_Key(), ecorePackage.getEString(), "key", null, 1, 1, CustomGeneratorDef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCustomGeneratorDef_Label(), ecorePackage.getEString(), "label", null, 1, 1, CustomGeneratorDef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCustomGeneratorDef_Expression(), ecorePackage.getEString(), "expression", null, 1, 1, CustomGeneratorDef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCustomGeneratorDef_Category(), ecorePackage.getEString(), "category", "Custom", 0, 1, CustomGeneratorDef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(dataGenResultEClass, DataGenResult.class, "DataGenResult", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getDataGenResult_Results(), ecorePackage.getEObject(), null, "results", null, 0, -1, DataGenResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(referenceStrategyEEnum, ReferenceStrategy.class, "ReferenceStrategy");
		addEEnumLiteral(referenceStrategyEEnum, ReferenceStrategy.RANDOM);
		addEEnumLiteral(referenceStrategyEEnum, ReferenceStrategy.ROUND_ROBIN);
		addEEnumLiteral(referenceStrategyEEnum, ReferenceStrategy.FIRST);
		addEEnumLiteral(referenceStrategyEEnum, ReferenceStrategy.NONE);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http://www.eclipse.org/emf/2002/GenModel
		createGenModelAnnotations();
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/emf/2002/GenModel</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createGenModelAnnotations() {
		String source = "http://www.eclipse.org/emf/2002/GenModel";
		addAnnotation
		  (this,
		   source,
		   new String[] {
			   "documentation", "Data Generator Metamodell zur Konfiguration automatischer Testdaten-Generierung fuer bestehende Metamodelle. Nutzt FakerJS als Datenquelle.",
			   "oSGiCompatible", "true",
			   "basePackage", "org.eclipse.fennec.model.atlas.datagen.model",
			   "copyrightText", "Copyright (c) 2026 Contributors to the Eclipse Foundation.\n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n  Data In Motion Consulting - initial implementation",
			   "resource", "XMI",
			   "fileExtensions", "datagen"
		   });
		addAnnotation
		  (referenceStrategyEEnum,
		   source,
		   new String[] {
			   "documentation", "Strategie fuer die Zuweisung von Referenz-Zielen bei der Generierung."
		   });
		addAnnotation
		  (referenceStrategyEEnum.getELiterals().get(0),
		   source,
		   new String[] {
			   "documentation", "Zufaellige Auswahl aus dem Pool passender Instanzen."
		   });
		addAnnotation
		  (referenceStrategyEEnum.getELiterals().get(1),
		   source,
		   new String[] {
			   "documentation", "Zyklische Zuweisung aus dem Pool passender Instanzen."
		   });
		addAnnotation
		  (referenceStrategyEEnum.getELiterals().get(2),
		   source,
		   new String[] {
			   "documentation", "Immer die erste passende Instanz zuweisen."
		   });
		addAnnotation
		  (referenceStrategyEEnum.getELiterals().get(3),
		   source,
		   new String[] {
			   "documentation", "Keine Referenz-Zuweisung (Referenz bleibt leer)."
		   });
		addAnnotation
		  (dataGenConfigEClass,
		   source,
		   new String[] {
			   "documentation", "Root-Container fuer eine Data-Generator-Konfiguration. Entspricht einer .datagen Datei."
		   });
		addAnnotation
		  (getDataGenConfig_Name(),
		   source,
		   new String[] {
			   "documentation", "Name der Generierungs-Konfiguration."
		   });
		addAnnotation
		  (getDataGenConfig_Version(),
		   source,
		   new String[] {
			   "documentation", "Versionsnummer der Konfiguration."
		   });
		addAnnotation
		  (getDataGenConfig_Description(),
		   source,
		   new String[] {
			   "documentation", "Optionale Beschreibung der Konfiguration."
		   });
		addAnnotation
		  (getDataGenConfig_Seed(),
		   source,
		   new String[] {
			   "documentation", "Seed fuer den Zufallsgenerator. 0 = zufaelliger Seed."
		   });
		addAnnotation
		  (getDataGenConfig_Locale(),
		   source,
		   new String[] {
			   "documentation", "Locale fuer FakerJS (z.B. de, en, fr)."
		   });
		addAnnotation
		  (getDataGenConfig_TargetModelNsURIs(),
		   source,
		   new String[] {
			   "documentation", "Namespace URIs der Ziel-Metamodelle fuer die Generierung."
		   });
		addAnnotation
		  (getDataGenConfig_ClassConfigs(),
		   source,
		   new String[] {
			   "documentation", "Generierungs-Konfigurationen pro EClass."
		   });
		addAnnotation
		  (getDataGenConfig_CustomGenerators(),
		   source,
		   new String[] {
			   "documentation", "Benutzerdefinierte Generator-Definitionen."
		   });
		addAnnotation
		  (classGenConfigEClass,
		   source,
		   new String[] {
			   "documentation", "Konfiguration fuer die Generierung von Instanzen einer bestimmten EClass."
		   });
		addAnnotation
		  (getClassGenConfig_ContextClass(),
		   source,
		   new String[] {
			   "documentation", "Vollqualifizierter Name der EClass, also die EClass-URI."
		   });
		addAnnotation
		  (getClassGenConfig_InstanceCount(),
		   source,
		   new String[] {
			   "documentation", "Anzahl der zu generierenden Instanzen."
		   });
		addAnnotation
		  (getClassGenConfig_Enabled(),
		   source,
		   new String[] {
			   "documentation", "Ob die Generierung fuer diese Klasse aktiviert ist."
		   });
		addAnnotation
		  (getClassGenConfig_AttributeGens(),
		   source,
		   new String[] {
			   "documentation", "Generierungs-Konfigurationen fuer Attribute."
		   });
		addAnnotation
		  (getClassGenConfig_ReferenceGens(),
		   source,
		   new String[] {
			   "documentation", "Generierungs-Konfigurationen fuer Referenzen."
		   });
		addAnnotation
		  (attributeGenConfigEClass,
		   source,
		   new String[] {
			   "documentation", "Konfiguration fuer die Generierung eines einzelnen Attributwerts."
		   });
		addAnnotation
		  (getAttributeGenConfig_FeatureName(),
		   source,
		   new String[] {
			   "documentation", "Name des Attributs in der EClass."
		   });
		addAnnotation
		  (getAttributeGenConfig_GeneratorKey(),
		   source,
		   new String[] {
			   "documentation", "Schluessel des Generators (z.B. faker.person.firstName)."
		   });
		addAnnotation
		  (getAttributeGenConfig_GeneratorArgs(),
		   source,
		   new String[] {
			   "documentation", "JSON-String mit Argumenten fuer den Generator."
		   });
		addAnnotation
		  (getAttributeGenConfig_Unique(),
		   source,
		   new String[] {
			   "documentation", "Ob generierte Werte eindeutig sein muessen."
		   });
		addAnnotation
		  (getAttributeGenConfig_StaticValue(),
		   source,
		   new String[] {
			   "documentation", "Statischer Wert (ueberschreibt generatorKey wenn gesetzt)."
		   });
		addAnnotation
		  (getAttributeGenConfig_Template(),
		   source,
		   new String[] {
			   "documentation", "Template mit #{key} Platzhaltern fuer zusammengesetzte Werte."
		   });
		addAnnotation
		  (referenceGenConfigEClass,
		   source,
		   new String[] {
			   "documentation", "Konfiguration fuer die Generierung von Referenz-Zuweisungen."
		   });
		addAnnotation
		  (getReferenceGenConfig_FeatureName(),
		   source,
		   new String[] {
			   "documentation", "Name der Referenz in der EClass."
		   });
		addAnnotation
		  (getReferenceGenConfig_Strategy(),
		   source,
		   new String[] {
			   "documentation", "Strategie fuer die Auswahl der Referenz-Ziele."
		   });
		addAnnotation
		  (getReferenceGenConfig_TargetClassFilter(),
		   source,
		   new String[] {
			   "documentation", "Optional: Filter auf eine bestimmte Ziel-Klasse."
		   });
		addAnnotation
		  (getReferenceGenConfig_MinCount(),
		   source,
		   new String[] {
			   "documentation", "Minimale Anzahl der zugewiesenen Referenzen."
		   });
		addAnnotation
		  (getReferenceGenConfig_MaxCount(),
		   source,
		   new String[] {
			   "documentation", "Maximale Anzahl der zugewiesenen Referenzen."
		   });
		addAnnotation
		  (customGeneratorDefEClass,
		   source,
		   new String[] {
			   "documentation", "Benutzerdefinierte Generator-Definition mit JS-Expression."
		   });
		addAnnotation
		  (getCustomGeneratorDef_Key(),
		   source,
		   new String[] {
			   "documentation", "Eindeutiger Schluessel des Generators (z.B. custom.fullAddress)."
		   });
		addAnnotation
		  (getCustomGeneratorDef_Label(),
		   source,
		   new String[] {
			   "documentation", "Anzeigename des Generators."
		   });
		addAnnotation
		  (getCustomGeneratorDef_Expression(),
		   source,
		   new String[] {
			   "documentation", "JavaScript-Expression. Erhaelt faker und index als Variablen."
		   });
		addAnnotation
		  (getCustomGeneratorDef_Category(),
		   source,
		   new String[] {
			   "documentation", "Kategorie fuer die Anzeige im Generator-Picker."
		   });
	}

} //DatagenPackageImpl
