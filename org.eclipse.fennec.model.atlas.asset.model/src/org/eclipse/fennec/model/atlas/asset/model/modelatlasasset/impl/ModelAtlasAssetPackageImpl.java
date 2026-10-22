/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
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
package org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl;

import inventory.InventoryPackage;

import inventory.impl.InventoryPackageImpl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.FingerprintableAsset;
import org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.ModelAtlasAssetFactory;
import org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.ModelAtlasAssetPackage;
import org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.SchemaAsset;
import org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ModelAtlasAssetPackageImpl extends EPackageImpl implements ModelAtlasAssetPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass stageableAssetEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass fingerprintableAssetEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass schemaAssetEClass = null;

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
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.ModelAtlasAssetPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ModelAtlasAssetPackageImpl() {
		super(eNS_URI, ModelAtlasAssetFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link ModelAtlasAssetPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ModelAtlasAssetPackage init() {
		if (isInited) return (ModelAtlasAssetPackage)EPackage.Registry.INSTANCE.getEPackage(ModelAtlasAssetPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredModelAtlasAssetPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		ModelAtlasAssetPackageImpl theModelAtlasAssetPackage = registeredModelAtlasAssetPackage instanceof ModelAtlasAssetPackageImpl ? (ModelAtlasAssetPackageImpl)registeredModelAtlasAssetPackage : new ModelAtlasAssetPackageImpl();

		isInited = true;

		// Obtain or create and register interdependencies
		Object registeredPackage = EPackage.Registry.INSTANCE.getEPackage(InventoryPackage.eNS_URI);
		InventoryPackageImpl theInventoryPackage = (InventoryPackageImpl)(registeredPackage instanceof InventoryPackageImpl ? registeredPackage : InventoryPackage.eINSTANCE);

		// Create package meta-data objects
		theModelAtlasAssetPackage.createPackageContents();
		theInventoryPackage.createPackageContents();

		// Initialize created meta-data
		theModelAtlasAssetPackage.initializePackageContents();
		theInventoryPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theModelAtlasAssetPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ModelAtlasAssetPackage.eNS_URI, theModelAtlasAssetPackage);
		return theModelAtlasAssetPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getStageableAsset() {
		return stageableAssetEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStageableAsset_Scope() {
		return (EAttribute)stageableAssetEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStageableAsset_Registry() {
		return (EAttribute)stageableAssetEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStageableAsset_Stage() {
		return (EAttribute)stageableAssetEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStageableAsset_AssetType() {
		return (EAttribute)stageableAssetEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getFingerprintableAsset() {
		return fingerprintableAssetEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFingerprintableAsset_Fingerprint() {
		return (EAttribute)fingerprintableAssetEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSchemaAsset() {
		return schemaAssetEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSchemaAsset_NsURI() {
		return (EAttribute)schemaAssetEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSchemaAsset_NsPrefix() {
		return (EAttribute)schemaAssetEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ModelAtlasAssetFactory getModelAtlasAssetFactory() {
		return (ModelAtlasAssetFactory)getEFactoryInstance();
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
		stageableAssetEClass = createEClass(STAGEABLE_ASSET);
		createEAttribute(stageableAssetEClass, STAGEABLE_ASSET__SCOPE);
		createEAttribute(stageableAssetEClass, STAGEABLE_ASSET__REGISTRY);
		createEAttribute(stageableAssetEClass, STAGEABLE_ASSET__STAGE);
		createEAttribute(stageableAssetEClass, STAGEABLE_ASSET__ASSET_TYPE);

		fingerprintableAssetEClass = createEClass(FINGERPRINTABLE_ASSET);
		createEAttribute(fingerprintableAssetEClass, FINGERPRINTABLE_ASSET__FINGERPRINT);

		schemaAssetEClass = createEClass(SCHEMA_ASSET);
		createEAttribute(schemaAssetEClass, SCHEMA_ASSET__NS_URI);
		createEAttribute(schemaAssetEClass, SCHEMA_ASSET__NS_PREFIX);
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

		// Obtain other dependent packages
		InventoryPackage theInventoryPackage = (InventoryPackage)EPackage.Registry.INSTANCE.getEPackage(InventoryPackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		stageableAssetEClass.getESuperTypes().add(theInventoryPackage.getAsset());
		fingerprintableAssetEClass.getESuperTypes().add(this.getStageableAsset());
		schemaAssetEClass.getESuperTypes().add(this.getFingerprintableAsset());

		// Initialize classes, features, and operations; add parameters
		initEClass(stageableAssetEClass, StageableAsset.class, "StageableAsset", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getStageableAsset_Scope(), ecorePackage.getEString(), "scope", null, 0, 1, StageableAsset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStageableAsset_Registry(), ecorePackage.getEString(), "registry", null, 0, 1, StageableAsset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStageableAsset_Stage(), ecorePackage.getEString(), "stage", null, 0, 1, StageableAsset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStageableAsset_AssetType(), ecorePackage.getEString(), "assetType", null, 0, 1, StageableAsset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(fingerprintableAssetEClass, FingerprintableAsset.class, "FingerprintableAsset", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getFingerprintableAsset_Fingerprint(), ecorePackage.getEString(), "fingerprint", null, 0, 1, FingerprintableAsset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(schemaAssetEClass, SchemaAsset.class, "SchemaAsset", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSchemaAsset_NsURI(), ecorePackage.getEString(), "nsURI", null, 0, 1, SchemaAsset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSchemaAsset_NsPrefix(), ecorePackage.getEString(), "nsPrefix", null, 0, 1, SchemaAsset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// Version
		createVersionAnnotations();
		// http://www.eclipse.org/emf/2002/GenModel
		createGenModelAnnotations();
	}

	/**
	 * Initializes the annotations for <b>Version</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createVersionAnnotations() {
		String source = "Version";
		addAnnotation
		  (this,
		   source,
		   new String[] {
			   "value", "1.0"
		   });
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
			   "complianceLevel", "17.0",
			   "oSGiCompatible", "true",
			   "basePackage", "org.eclipse.fennec.model.atlas.asset.model",
			   "resource", "XMI",
			   "copyrightText", "Copyright (c) 2012 - 2025 Data In Motion and others.\nAll rights reserved.\n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n     Data In Motion - initial API and implementation"
		   });
		addAnnotation
		  (stageableAssetEClass,
		   source,
		   new String[] {
			   "documentation", "An Asset that has the concpets of stage, registry and scope typical of the model.atlas"
		   });
		addAnnotation
		  (getStageableAsset_AssetType(),
		   source,
		   new String[] {
			   "documentation", "The object type of the asset, as it is saved in the corresponding ObjectMetadata#objectType"
		   });
		addAnnotation
		  (fingerprintableAssetEClass,
		   source,
		   new String[] {
			   "documentation", "An asset which has a fingerprint, in the model.atlas sense of the term. An EPackage has a fingerprint, but also a CompiledUnit"
		   });
		addAnnotation
		  (schemaAssetEClass,
		   source,
		   new String[] {
			   "documentation", "An Asset for an EPackage object"
		   });
	}

} //ModelAtlasAssetPackageImpl
