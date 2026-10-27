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
package inventory.impl;

import inventory.Aspect;
import inventory.Asset;
import inventory.AssetRelation;
import inventory.AssetRelationKind;
import inventory.InventoryFactory;
import inventory.InventoryPackage;
import inventory.SourceKind;
import inventory.SourceRef;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.ModelAtlasAssetPackage;

import org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.ModelAtlasAssetPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class InventoryPackageImpl extends EPackageImpl implements InventoryPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass assetEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass assetRelationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass sourceRefEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass aspectEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum assetRelationKindEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum sourceKindEEnum = null;

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
	 * @see inventory.InventoryPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private InventoryPackageImpl() {
		super(eNS_URI, InventoryFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link InventoryPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static InventoryPackage init() {
		if (isInited) return (InventoryPackage)EPackage.Registry.INSTANCE.getEPackage(InventoryPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredInventoryPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		InventoryPackageImpl theInventoryPackage = registeredInventoryPackage instanceof InventoryPackageImpl ? (InventoryPackageImpl)registeredInventoryPackage : new InventoryPackageImpl();

		isInited = true;

		// Obtain or create and register interdependencies
		Object registeredPackage = EPackage.Registry.INSTANCE.getEPackage(ModelAtlasAssetPackage.eNS_URI);
		ModelAtlasAssetPackageImpl theModelAtlasAssetPackage = (ModelAtlasAssetPackageImpl)(registeredPackage instanceof ModelAtlasAssetPackageImpl ? registeredPackage : ModelAtlasAssetPackage.eINSTANCE);

		// Create package meta-data objects
		theInventoryPackage.createPackageContents();
		theModelAtlasAssetPackage.createPackageContents();

		// Initialize created meta-data
		theInventoryPackage.initializePackageContents();
		theModelAtlasAssetPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theInventoryPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(InventoryPackage.eNS_URI, theInventoryPackage);
		return theInventoryPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAsset() {
		return assetEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAsset_Id() {
		return (EAttribute)assetEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAsset_Name() {
		return (EAttribute)assetEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAsset_Description() {
		return (EAttribute)assetEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAsset_Owner() {
		return (EAttribute)assetEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAsset_Relations() {
		return (EReference)assetEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAsset_Sources() {
		return (EReference)assetEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAsset_Aspects() {
		return (EReference)assetEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAssetRelation() {
		return assetRelationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAssetRelation_Id() {
		return (EAttribute)assetRelationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAssetRelation_Kind() {
		return (EAttribute)assetRelationEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAssetRelation_Target() {
		return (EReference)assetRelationEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAssetRelation_Rationale() {
		return (EAttribute)assetRelationEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAssetRelation_Source() {
		return (EReference)assetRelationEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSourceRef() {
		return sourceRefEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSourceRef_Kind() {
		return (EAttribute)sourceRefEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSourceRef_Format() {
		return (EAttribute)sourceRefEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSourceRef_Uri() {
		return (EAttribute)sourceRefEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSourceRef_SourceRevision() {
		return (EAttribute)sourceRefEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSourceRef_Name() {
		return (EAttribute)sourceRefEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSourceRef_Object() {
		return (EReference)sourceRefEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAspect() {
		return aspectEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getAssetRelationKind() {
		return assetRelationKindEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getSourceKind() {
		return sourceKindEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public InventoryFactory getInventoryFactory() {
		return (InventoryFactory)getEFactoryInstance();
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
		assetEClass = createEClass(ASSET);
		createEAttribute(assetEClass, ASSET__ID);
		createEAttribute(assetEClass, ASSET__NAME);
		createEAttribute(assetEClass, ASSET__DESCRIPTION);
		createEAttribute(assetEClass, ASSET__OWNER);
		createEReference(assetEClass, ASSET__RELATIONS);
		createEReference(assetEClass, ASSET__SOURCES);
		createEReference(assetEClass, ASSET__ASPECTS);

		assetRelationEClass = createEClass(ASSET_RELATION);
		createEAttribute(assetRelationEClass, ASSET_RELATION__ID);
		createEAttribute(assetRelationEClass, ASSET_RELATION__KIND);
		createEReference(assetRelationEClass, ASSET_RELATION__TARGET);
		createEAttribute(assetRelationEClass, ASSET_RELATION__RATIONALE);
		createEReference(assetRelationEClass, ASSET_RELATION__SOURCE);

		sourceRefEClass = createEClass(SOURCE_REF);
		createEAttribute(sourceRefEClass, SOURCE_REF__KIND);
		createEAttribute(sourceRefEClass, SOURCE_REF__FORMAT);
		createEAttribute(sourceRefEClass, SOURCE_REF__URI);
		createEAttribute(sourceRefEClass, SOURCE_REF__SOURCE_REVISION);
		createEAttribute(sourceRefEClass, SOURCE_REF__NAME);
		createEReference(sourceRefEClass, SOURCE_REF__OBJECT);

		aspectEClass = createEClass(ASPECT);

		// Create enums
		assetRelationKindEEnum = createEEnum(ASSET_RELATION_KIND);
		sourceKindEEnum = createEEnum(SOURCE_KIND);
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
		aspectEClass.getESuperTypes().add(this.getAssetRelation());

		// Initialize classes, features, and operations; add parameters
		initEClass(assetEClass, Asset.class, "Asset", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getAsset_Id(), ecorePackage.getEString(), "id", null, 1, 1, Asset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAsset_Name(), ecorePackage.getEString(), "name", null, 0, 1, Asset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAsset_Description(), ecorePackage.getEString(), "description", null, 0, 1, Asset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAsset_Owner(), ecorePackage.getEString(), "owner", null, 0, 1, Asset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAsset_Relations(), this.getAssetRelation(), this.getAssetRelation_Target(), "relations", null, 0, -1, Asset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getAsset_Relations().getEKeys().add(this.getAssetRelation_Id());
		initEReference(getAsset_Sources(), this.getSourceRef(), null, "sources", null, 0, -1, Asset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAsset_Aspects(), this.getAspect(), null, "aspects", null, 0, -1, Asset.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

		initEClass(assetRelationEClass, AssetRelation.class, "AssetRelation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getAssetRelation_Id(), ecorePackage.getEString(), "id", null, 1, 1, AssetRelation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAssetRelation_Kind(), this.getAssetRelationKind(), "kind", null, 1, 1, AssetRelation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAssetRelation_Target(), this.getAsset(), this.getAsset_Relations(), "target", null, 1, 1, AssetRelation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAssetRelation_Rationale(), ecorePackage.getEString(), "rationale", null, 0, 1, AssetRelation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAssetRelation_Source(), this.getAsset(), null, "source", null, 1, 1, AssetRelation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getAssetRelation_Source().getEKeys().add(this.getAsset_Id());

		initEClass(sourceRefEClass, SourceRef.class, "SourceRef", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSourceRef_Kind(), this.getSourceKind(), "kind", null, 1, 1, SourceRef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSourceRef_Format(), ecorePackage.getEString(), "format", null, 0, 1, SourceRef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSourceRef_Uri(), ecorePackage.getEString(), "uri", null, 1, 1, SourceRef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSourceRef_SourceRevision(), ecorePackage.getEString(), "sourceRevision", null, 0, 1, SourceRef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSourceRef_Name(), ecorePackage.getEString(), "name", null, 0, 1, SourceRef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSourceRef_Object(), ecorePackage.getEObject(), null, "object", null, 0, 1, SourceRef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(aspectEClass, Aspect.class, "Aspect", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		// Initialize enums and add enum literals
		initEEnum(assetRelationKindEEnum, AssetRelationKind.class, "AssetRelationKind");
		addEEnumLiteral(assetRelationKindEEnum, AssetRelationKind.DEPENDS_ON);
		addEEnumLiteral(assetRelationKindEEnum, AssetRelationKind.RUNS_ON);
		addEEnumLiteral(assetRelationKindEEnum, AssetRelationKind.CONNECTS_TO);
		addEEnumLiteral(assetRelationKindEEnum, AssetRelationKind.PART_OF);

		initEEnum(sourceKindEEnum, SourceKind.class, "SourceKind");
		addEEnumLiteral(sourceKindEEnum, SourceKind.SERVICE_SPEC);
		addEEnumLiteral(sourceKindEEnum, SourceKind.DEPLOYMENT);
		addEEnumLiteral(sourceKindEEnum, SourceKind.EPACKAGE);
		addEEnumLiteral(sourceKindEEnum, SourceKind.TRANSFORMATION);
		addEEnumLiteral(sourceKindEEnum, SourceKind.REPORT);
		addEEnumLiteral(sourceKindEEnum, SourceKind.DOCUMENT);
		addEEnumLiteral(sourceKindEEnum, SourceKind.DATASET);
		addEEnumLiteral(sourceKindEEnum, SourceKind.OTHER);

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
			   "documentation", "An inventory of a system, independent from the domain of the system itself."
		   });
		addAnnotation
		  (assetEClass,
		   source,
		   new String[] {
			   "documentation", "A participant in a system, as well as the system itself. This is all we have to keep track of in an infrastructure, for whatever purpose: a host, a container, a network, a volume, an application, a data set. Generated from deployment and service specifications where possible, then curated."
		   });
		addAnnotation
		  (getAsset_Id(),
		   source,
		   new String[] {
			   "documentation", "Identifier, unique across assets. EMF ID."
		   });
		addAnnotation
		  (getAsset_Name(),
		   source,
		   new String[] {
			   "documentation", "Display name."
		   });
		addAnnotation
		  (getAsset_Description(),
		   source,
		   new String[] {
			   "documentation", "Description."
		   });
		addAnnotation
		  (getAsset_Owner(),
		   source,
		   new String[] {
			   "documentation", "Responsible role or person."
		   });
		addAnnotation
		  (getAsset_Relations(),
		   source,
		   new String[] {
			   "documentation", "Relations to other assets."
		   });
		addAnnotation
		  (getAsset_Sources(),
		   source,
		   new String[] {
			   "documentation", "Where the asset is defined."
		   });
		addAnnotation
		  (getAsset_Aspects(),
		   source,
		   new String[] {
			   "documentation", "Context-specific extensions.",
			   "get", "java.util.List<Aspect> aspects = new java.util.ArrayList<>();\nfor (AssetRelation relation : getRelations()) {\n      if (relation instanceof Aspect aspect) {\n          aspects.add(aspect);\n      }\n  }\n  return new org.eclipse.emf.common.util.BasicEList.UnmodifiableEList<>(aspects.size(), aspects.toArray());"
		   });
		addAnnotation
		  (assetRelationEClass,
		   source,
		   new String[] {
			   "documentation", "A typed relation from the owning asset to another asset."
		   });
		addAnnotation
		  (getAssetRelation_Id(),
		   source,
		   new String[] {
			   "documentation", "Identifier, unique within the inventory. EMF ID: unique within the whole inventory resource, also across assets, measures and processing activities, so references are written by id and survive reordering."
		   });
		addAnnotation
		  (getAssetRelation_Kind(),
		   source,
		   new String[] {
			   "documentation", "Kind of relation."
		   });
		addAnnotation
		  (getAssetRelation_Target(),
		   source,
		   new String[] {
			   "documentation", "The related asset."
		   });
		addAnnotation
		  (getAssetRelation_Rationale(),
		   source,
		   new String[] {
			   "documentation", "Why the relation exists."
		   });
		addAnnotation
		  (sourceRefEClass,
		   source,
		   new String[] {
			   "documentation", "Loose reference to a defining or evidencing artifact, without a dependency on its model. A changed fingerprint shows that the source changed."
		   });
		addAnnotation
		  (getSourceRef_Kind(),
		   source,
		   new String[] {
			   "documentation", "What role the artifact plays."
		   });
		addAnnotation
		  (getSourceRef_Format(),
		   source,
		   new String[] {
			   "documentation", "Technical format of the artifact, free text so that the model stays independent of a runtime or tool. Common values: quadlet, compose, kubernetes, helm, terraform, xmi, ecore, json, yaml, pdf."
		   });
		addAnnotation
		  (getSourceRef_Uri(),
		   source,
		   new String[] {
			   "documentation", "URI of the artifact, e.g. an endpoint where the source is reachable, a file uri when a source is a document"
		   });
		addAnnotation
		  (getSourceRef_SourceRevision(),
		   source,
		   new String[] {
			   "documentation", "an identifier for the source reference. Whenever the source reference changes (e.g. the format changes, the uri changes, or so on), a new revision is set."
		   });
		addAnnotation
		  (getSourceRef_Name(),
		   source,
		   new String[] {
			   "documentation", "Display name."
		   });
		addAnnotation
		  (getSourceRef_Object(),
		   source,
		   new String[] {
			   "documentation", "This is an optional reference to the object the URI of the source resolves into."
		   });
		addAnnotation
		  (aspectEClass,
		   source,
		   new String[] {
			   "documentation", "An Aspect is a special kind of AssetRelation. "
		   });
		addAnnotation
		  (assetRelationKindEEnum,
		   source,
		   new String[] {
			   "documentation", "How one asset relates to another; used to inherit protection needs."
		   });
		addAnnotation
		  (assetRelationKindEEnum.getELiterals().get(0),
		   source,
		   new String[] {
			   "documentation", "The asset needs the target to work."
		   });
		addAnnotation
		  (assetRelationKindEEnum.getELiterals().get(1),
		   source,
		   new String[] {
			   "documentation", "The asset runs on the target, e.g. a container on a host."
		   });
		addAnnotation
		  (assetRelationKindEEnum.getELiterals().get(2),
		   source,
		   new String[] {
			   "documentation", "The asset communicates with the target."
		   });
		addAnnotation
		  (assetRelationKindEEnum.getELiterals().get(3),
		   source,
		   new String[] {
			   "documentation", "The asset is part of the target."
		   });
		addAnnotation
		  (sourceKindEEnum,
		   source,
		   new String[] {
			   "documentation", "What a SourceRef points at."
		   });
		addAnnotation
		  (sourceKindEEnum.getELiterals().get(0),
		   source,
		   new String[] {
			   "documentation", "A service specification."
		   });
		addAnnotation
		  (sourceKindEEnum.getELiterals().get(1),
		   source,
		   new String[] {
			   "documentation", "A deployment or service binding."
		   });
		addAnnotation
		  (sourceKindEEnum.getELiterals().get(2),
		   source,
		   new String[] {
			   "documentation", "An EPackage, by nsURI."
		   });
		addAnnotation
		  (sourceKindEEnum.getELiterals().get(3),
		   source,
		   new String[] {
			   "documentation", "A transformation unit."
		   });
		addAnnotation
		  (sourceKindEEnum.getELiterals().get(4),
		   source,
		   new String[] {
			   "documentation", "A compliance report."
		   });
		addAnnotation
		  (sourceKindEEnum.getELiterals().get(5),
		   source,
		   new String[] {
			   "documentation", "A document, e.g. a policy."
		   });
		addAnnotation
		  (sourceKindEEnum.getELiterals().get(6),
		   source,
		   new String[] {
			   "documentation", "A data set, e.g. a DCAT dataset."
		   });
		addAnnotation
		  (sourceKindEEnum.getELiterals().get(7),
		   source,
		   new String[] {
			   "documentation", "Anything else."
		   });
	}

} //InventoryPackageImpl
