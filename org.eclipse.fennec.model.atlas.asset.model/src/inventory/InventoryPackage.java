/*
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
package inventory;


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
 * An inventory of a system, independent from the domain of the system itself.
 * <!-- end-model-doc -->
 * @see inventory.InventoryFactory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = InventoryPackage.eNS_URI, fingerprint = "fp1:aa049fa58b5886015829e96de0689ae9e2171c8cea65f3a84d59da0c2d065ab8", genModel = "/model/model-atlas-asset.genmodel", genModelSourceLocations = {"model/model-atlas-asset.genmodel","org.eclipse.fennec.model.atlas.asset.model/model/model-atlas-asset.genmodel"}, ecore = "/model/inventory.ecore", ecoreSourceLocations = "/model/inventory.ecore")
public interface InventoryPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "inventory";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "https://org.eclipse/fennec/validation/inventory/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "inventory";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	InventoryPackage eINSTANCE = inventory.impl.InventoryPackageImpl.init();

	/**
	 * The meta object id for the '{@link inventory.impl.AssetImpl <em>Asset</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see inventory.impl.AssetImpl
	 * @see inventory.impl.InventoryPackageImpl#getAsset()
	 * @generated
	 */
	int ASSET = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASSET__ID = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASSET__NAME = 1;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASSET__DESCRIPTION = 2;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASSET__OWNER = 3;

	/**
	 * The feature id for the '<em><b>Relations</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASSET__RELATIONS = 4;

	/**
	 * The feature id for the '<em><b>Sources</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASSET__SOURCES = 5;

	/**
	 * The feature id for the '<em><b>Aspects</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASSET__ASPECTS = 6;

	/**
	 * The number of structural features of the '<em>Asset</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASSET_FEATURE_COUNT = 7;

	/**
	 * The number of operations of the '<em>Asset</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASSET_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link inventory.impl.AssetRelationImpl <em>Asset Relation</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see inventory.impl.AssetRelationImpl
	 * @see inventory.impl.InventoryPackageImpl#getAssetRelation()
	 * @generated
	 */
	int ASSET_RELATION = 1;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASSET_RELATION__ID = 0;

	/**
	 * The feature id for the '<em><b>Kind</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASSET_RELATION__KIND = 1;

	/**
	 * The feature id for the '<em><b>Rationale</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASSET_RELATION__RATIONALE = 2;

	/**
	 * The feature id for the '<em><b>Source Asset Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASSET_RELATION__SOURCE_ASSET_ID = 3;

	/**
	 * The feature id for the '<em><b>Target Asset Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASSET_RELATION__TARGET_ASSET_ID = 4;

	/**
	 * The number of structural features of the '<em>Asset Relation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASSET_RELATION_FEATURE_COUNT = 5;

	/**
	 * The number of operations of the '<em>Asset Relation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASSET_RELATION_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link inventory.impl.SourceRefImpl <em>Source Ref</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see inventory.impl.SourceRefImpl
	 * @see inventory.impl.InventoryPackageImpl#getSourceRef()
	 * @generated
	 */
	int SOURCE_REF = 2;

	/**
	 * The feature id for the '<em><b>Kind</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE_REF__KIND = 0;

	/**
	 * The feature id for the '<em><b>Format</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE_REF__FORMAT = 1;

	/**
	 * The feature id for the '<em><b>Uri</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE_REF__URI = 2;

	/**
	 * The feature id for the '<em><b>Source Revision</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE_REF__SOURCE_REVISION = 3;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE_REF__NAME = 4;

	/**
	 * The feature id for the '<em><b>Object</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE_REF__OBJECT = 5;

	/**
	 * The number of structural features of the '<em>Source Ref</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE_REF_FEATURE_COUNT = 6;

	/**
	 * The number of operations of the '<em>Source Ref</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE_REF_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link inventory.impl.AspectImpl <em>Aspect</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see inventory.impl.AspectImpl
	 * @see inventory.impl.InventoryPackageImpl#getAspect()
	 * @generated
	 */
	int ASPECT = 3;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASPECT__ID = ASSET_RELATION__ID;

	/**
	 * The feature id for the '<em><b>Kind</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASPECT__KIND = ASSET_RELATION__KIND;

	/**
	 * The feature id for the '<em><b>Rationale</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASPECT__RATIONALE = ASSET_RELATION__RATIONALE;

	/**
	 * The feature id for the '<em><b>Source Asset Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASPECT__SOURCE_ASSET_ID = ASSET_RELATION__SOURCE_ASSET_ID;

	/**
	 * The feature id for the '<em><b>Target Asset Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASPECT__TARGET_ASSET_ID = ASSET_RELATION__TARGET_ASSET_ID;

	/**
	 * The number of structural features of the '<em>Aspect</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASPECT_FEATURE_COUNT = ASSET_RELATION_FEATURE_COUNT + 0;

	/**
	 * The number of operations of the '<em>Aspect</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ASPECT_OPERATION_COUNT = ASSET_RELATION_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link inventory.AssetRelationKind <em>Asset Relation Kind</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see inventory.AssetRelationKind
	 * @see inventory.impl.InventoryPackageImpl#getAssetRelationKind()
	 * @generated
	 */
	int ASSET_RELATION_KIND = 4;

	/**
	 * The meta object id for the '{@link inventory.SourceKind <em>Source Kind</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see inventory.SourceKind
	 * @see inventory.impl.InventoryPackageImpl#getSourceKind()
	 * @generated
	 */
	int SOURCE_KIND = 5;


	/**
	 * Returns the meta object for class '{@link inventory.Asset <em>Asset</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Asset</em>'.
	 * @see inventory.Asset
	 * @generated
	 */
	EClass getAsset();

	/**
	 * Returns the meta object for the attribute '{@link inventory.Asset#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see inventory.Asset#getId()
	 * @see #getAsset()
	 * @generated
	 */
	EAttribute getAsset_Id();

	/**
	 * Returns the meta object for the attribute '{@link inventory.Asset#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see inventory.Asset#getName()
	 * @see #getAsset()
	 * @generated
	 */
	EAttribute getAsset_Name();

	/**
	 * Returns the meta object for the attribute '{@link inventory.Asset#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see inventory.Asset#getDescription()
	 * @see #getAsset()
	 * @generated
	 */
	EAttribute getAsset_Description();

	/**
	 * Returns the meta object for the attribute '{@link inventory.Asset#getOwner <em>Owner</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Owner</em>'.
	 * @see inventory.Asset#getOwner()
	 * @see #getAsset()
	 * @generated
	 */
	EAttribute getAsset_Owner();

	/**
	 * Returns the meta object for the containment reference list '{@link inventory.Asset#getRelations <em>Relations</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Relations</em>'.
	 * @see inventory.Asset#getRelations()
	 * @see #getAsset()
	 * @generated
	 */
	EReference getAsset_Relations();

	/**
	 * Returns the meta object for the containment reference list '{@link inventory.Asset#getSources <em>Sources</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Sources</em>'.
	 * @see inventory.Asset#getSources()
	 * @see #getAsset()
	 * @generated
	 */
	EReference getAsset_Sources();

	/**
	 * Returns the meta object for the reference list '{@link inventory.Asset#getAspects <em>Aspects</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Aspects</em>'.
	 * @see inventory.Asset#getAspects()
	 * @see #getAsset()
	 * @generated
	 */
	EReference getAsset_Aspects();

	/**
	 * Returns the meta object for class '{@link inventory.AssetRelation <em>Asset Relation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Asset Relation</em>'.
	 * @see inventory.AssetRelation
	 * @generated
	 */
	EClass getAssetRelation();

	/**
	 * Returns the meta object for the attribute '{@link inventory.AssetRelation#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see inventory.AssetRelation#getId()
	 * @see #getAssetRelation()
	 * @generated
	 */
	EAttribute getAssetRelation_Id();

	/**
	 * Returns the meta object for the attribute '{@link inventory.AssetRelation#getKind <em>Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Kind</em>'.
	 * @see inventory.AssetRelation#getKind()
	 * @see #getAssetRelation()
	 * @generated
	 */
	EAttribute getAssetRelation_Kind();

	/**
	 * Returns the meta object for the attribute '{@link inventory.AssetRelation#getRationale <em>Rationale</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Rationale</em>'.
	 * @see inventory.AssetRelation#getRationale()
	 * @see #getAssetRelation()
	 * @generated
	 */
	EAttribute getAssetRelation_Rationale();

	/**
	 * Returns the meta object for the attribute '{@link inventory.AssetRelation#getSourceAssetId <em>Source Asset Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Source Asset Id</em>'.
	 * @see inventory.AssetRelation#getSourceAssetId()
	 * @see #getAssetRelation()
	 * @generated
	 */
	EAttribute getAssetRelation_SourceAssetId();

	/**
	 * Returns the meta object for the attribute '{@link inventory.AssetRelation#getTargetAssetId <em>Target Asset Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Target Asset Id</em>'.
	 * @see inventory.AssetRelation#getTargetAssetId()
	 * @see #getAssetRelation()
	 * @generated
	 */
	EAttribute getAssetRelation_TargetAssetId();

	/**
	 * Returns the meta object for class '{@link inventory.SourceRef <em>Source Ref</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Source Ref</em>'.
	 * @see inventory.SourceRef
	 * @generated
	 */
	EClass getSourceRef();

	/**
	 * Returns the meta object for the attribute '{@link inventory.SourceRef#getKind <em>Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Kind</em>'.
	 * @see inventory.SourceRef#getKind()
	 * @see #getSourceRef()
	 * @generated
	 */
	EAttribute getSourceRef_Kind();

	/**
	 * Returns the meta object for the attribute '{@link inventory.SourceRef#getFormat <em>Format</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Format</em>'.
	 * @see inventory.SourceRef#getFormat()
	 * @see #getSourceRef()
	 * @generated
	 */
	EAttribute getSourceRef_Format();

	/**
	 * Returns the meta object for the attribute '{@link inventory.SourceRef#getUri <em>Uri</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Uri</em>'.
	 * @see inventory.SourceRef#getUri()
	 * @see #getSourceRef()
	 * @generated
	 */
	EAttribute getSourceRef_Uri();

	/**
	 * Returns the meta object for the attribute '{@link inventory.SourceRef#getSourceRevision <em>Source Revision</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Source Revision</em>'.
	 * @see inventory.SourceRef#getSourceRevision()
	 * @see #getSourceRef()
	 * @generated
	 */
	EAttribute getSourceRef_SourceRevision();

	/**
	 * Returns the meta object for the attribute '{@link inventory.SourceRef#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see inventory.SourceRef#getName()
	 * @see #getSourceRef()
	 * @generated
	 */
	EAttribute getSourceRef_Name();

	/**
	 * Returns the meta object for the reference '{@link inventory.SourceRef#getObject <em>Object</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Object</em>'.
	 * @see inventory.SourceRef#getObject()
	 * @see #getSourceRef()
	 * @generated
	 */
	EReference getSourceRef_Object();

	/**
	 * Returns the meta object for class '{@link inventory.Aspect <em>Aspect</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Aspect</em>'.
	 * @see inventory.Aspect
	 * @generated
	 */
	EClass getAspect();

	/**
	 * Returns the meta object for enum '{@link inventory.AssetRelationKind <em>Asset Relation Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Asset Relation Kind</em>'.
	 * @see inventory.AssetRelationKind
	 * @generated
	 */
	EEnum getAssetRelationKind();

	/**
	 * Returns the meta object for enum '{@link inventory.SourceKind <em>Source Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Source Kind</em>'.
	 * @see inventory.SourceKind
	 * @generated
	 */
	EEnum getSourceKind();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	InventoryFactory getInventoryFactory();

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
		 * The meta object literal for the '{@link inventory.impl.AssetImpl <em>Asset</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see inventory.impl.AssetImpl
		 * @see inventory.impl.InventoryPackageImpl#getAsset()
		 * @generated
		 */
		EClass ASSET = eINSTANCE.getAsset();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ASSET__ID = eINSTANCE.getAsset_Id();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ASSET__NAME = eINSTANCE.getAsset_Name();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ASSET__DESCRIPTION = eINSTANCE.getAsset_Description();

		/**
		 * The meta object literal for the '<em><b>Owner</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ASSET__OWNER = eINSTANCE.getAsset_Owner();

		/**
		 * The meta object literal for the '<em><b>Relations</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ASSET__RELATIONS = eINSTANCE.getAsset_Relations();

		/**
		 * The meta object literal for the '<em><b>Sources</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ASSET__SOURCES = eINSTANCE.getAsset_Sources();

		/**
		 * The meta object literal for the '<em><b>Aspects</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ASSET__ASPECTS = eINSTANCE.getAsset_Aspects();

		/**
		 * The meta object literal for the '{@link inventory.impl.AssetRelationImpl <em>Asset Relation</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see inventory.impl.AssetRelationImpl
		 * @see inventory.impl.InventoryPackageImpl#getAssetRelation()
		 * @generated
		 */
		EClass ASSET_RELATION = eINSTANCE.getAssetRelation();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ASSET_RELATION__ID = eINSTANCE.getAssetRelation_Id();

		/**
		 * The meta object literal for the '<em><b>Kind</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ASSET_RELATION__KIND = eINSTANCE.getAssetRelation_Kind();

		/**
		 * The meta object literal for the '<em><b>Rationale</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ASSET_RELATION__RATIONALE = eINSTANCE.getAssetRelation_Rationale();

		/**
		 * The meta object literal for the '<em><b>Source Asset Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ASSET_RELATION__SOURCE_ASSET_ID = eINSTANCE.getAssetRelation_SourceAssetId();

		/**
		 * The meta object literal for the '<em><b>Target Asset Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ASSET_RELATION__TARGET_ASSET_ID = eINSTANCE.getAssetRelation_TargetAssetId();

		/**
		 * The meta object literal for the '{@link inventory.impl.SourceRefImpl <em>Source Ref</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see inventory.impl.SourceRefImpl
		 * @see inventory.impl.InventoryPackageImpl#getSourceRef()
		 * @generated
		 */
		EClass SOURCE_REF = eINSTANCE.getSourceRef();

		/**
		 * The meta object literal for the '<em><b>Kind</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SOURCE_REF__KIND = eINSTANCE.getSourceRef_Kind();

		/**
		 * The meta object literal for the '<em><b>Format</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SOURCE_REF__FORMAT = eINSTANCE.getSourceRef_Format();

		/**
		 * The meta object literal for the '<em><b>Uri</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SOURCE_REF__URI = eINSTANCE.getSourceRef_Uri();

		/**
		 * The meta object literal for the '<em><b>Source Revision</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SOURCE_REF__SOURCE_REVISION = eINSTANCE.getSourceRef_SourceRevision();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SOURCE_REF__NAME = eINSTANCE.getSourceRef_Name();

		/**
		 * The meta object literal for the '<em><b>Object</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SOURCE_REF__OBJECT = eINSTANCE.getSourceRef_Object();

		/**
		 * The meta object literal for the '{@link inventory.impl.AspectImpl <em>Aspect</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see inventory.impl.AspectImpl
		 * @see inventory.impl.InventoryPackageImpl#getAspect()
		 * @generated
		 */
		EClass ASPECT = eINSTANCE.getAspect();

		/**
		 * The meta object literal for the '{@link inventory.AssetRelationKind <em>Asset Relation Kind</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see inventory.AssetRelationKind
		 * @see inventory.impl.InventoryPackageImpl#getAssetRelationKind()
		 * @generated
		 */
		EEnum ASSET_RELATION_KIND = eINSTANCE.getAssetRelationKind();

		/**
		 * The meta object literal for the '{@link inventory.SourceKind <em>Source Kind</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see inventory.SourceKind
		 * @see inventory.impl.InventoryPackageImpl#getSourceKind()
		 * @generated
		 */
		EEnum SOURCE_KIND = eINSTANCE.getSourceKind();

	}

} //InventoryPackage
