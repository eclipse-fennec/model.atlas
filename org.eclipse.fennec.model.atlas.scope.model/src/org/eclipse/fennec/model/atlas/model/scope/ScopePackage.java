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
package org.eclipse.fennec.model.atlas.model.scope;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import org.gecko.emf.osgi.annotation.provide.EPackage;

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
 * @see org.eclipse.fennec.model.atlas.model.scope.ScopeFactory
 * @model kind="package"
 *        annotation="Version value='1.0'"
 *        annotation="http://www.eclipse.org/emf/2002/GenModel complianceLevel='17.0' oSGiCompatible='true' basePackage='org.eclipse.fennec.model.atlas.model' resource='XMI' copyrightText='Copyright (c) 2012 - 2025 Data In Motion and others.\nAll rights reserved.\n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n     Data In Motion - initial API and implementation'"
 * @generated
 */
@ProviderType
@EPackage(uri = ScopePackage.eNS_URI, genModel = "/model/scope.genmodel", genModelSourceLocations = {"model/scope.genmodel","org.eclipse.fennec.model.atlas.scope.model/model/scope.genmodel"}, ecore="/model/scope.ecore", ecoreSourceLocations="/model/scope.ecore")
public interface ScopePackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "scope";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://eclipse.org/fennec/model/atlas/scope/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "scope";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ScopePackage eINSTANCE = org.eclipse.fennec.model.atlas.model.scope.impl.ScopePackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.model.scope.impl.ScopeImpl <em>Scope</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.model.scope.impl.ScopeImpl
	 * @see org.eclipse.fennec.model.atlas.model.scope.impl.ScopePackageImpl#getScope()
	 * @generated
	 */
	int SCOPE = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE__NAME = 0;

	/**
	 * The feature id for the '<em><b>Parent Scope</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE__PARENT_SCOPE = 1;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE__DESCRIPTION = 2;

	/**
	 * The feature id for the '<em><b>Links</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE__LINKS = 3;

	/**
	 * The feature id for the '<em><b>Stages</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE__STAGES = 4;

	/**
	 * The feature id for the '<em><b>Final Stage</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE__FINAL_STAGE = 5;

	/**
	 * The feature id for the '<em><b>Writable Stages</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE__WRITABLE_STAGES = 6;

	/**
	 * The number of structural features of the '<em>Scope</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_FEATURE_COUNT = 7;

	/**
	 * The number of operations of the '<em>Scope</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.model.scope.impl.LinksMapImpl <em>Links Map</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.model.scope.impl.LinksMapImpl
	 * @see org.eclipse.fennec.model.atlas.model.scope.impl.ScopePackageImpl#getLinksMap()
	 * @generated
	 */
	int LINKS_MAP = 1;

	/**
	 * The feature id for the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LINKS_MAP__KEY = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LINKS_MAP__VALUE = 1;

	/**
	 * The number of structural features of the '<em>Links Map</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LINKS_MAP_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Links Map</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LINKS_MAP_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.model.scope.impl.ScopeContainerImpl <em>Container</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.model.scope.impl.ScopeContainerImpl
	 * @see org.eclipse.fennec.model.atlas.model.scope.impl.ScopePackageImpl#getScopeContainer()
	 * @generated
	 */
	int SCOPE_CONTAINER = 2;

	/**
	 * The feature id for the '<em><b>Scopes</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_CONTAINER__SCOPES = 0;

	/**
	 * The number of structural features of the '<em>Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_CONTAINER_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_CONTAINER_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.model.scope.impl.StageTransitionImpl <em>Stage Transition</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.model.scope.impl.StageTransitionImpl
	 * @see org.eclipse.fennec.model.atlas.model.scope.impl.ScopePackageImpl#getStageTransition()
	 * @generated
	 */
	int STAGE_TRANSITION = 3;

	/**
	 * The feature id for the '<em><b>Object Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_TRANSITION__OBJECT_ID = 0;

	/**
	 * The feature id for the '<em><b>Target Stage</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_TRANSITION__TARGET_STAGE = 1;

	/**
	 * The number of structural features of the '<em>Stage Transition</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_TRANSITION_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Stage Transition</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_TRANSITION_OPERATION_COUNT = 0;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.model.scope.Scope <em>Scope</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Scope</em>'.
	 * @see org.eclipse.fennec.model.atlas.model.scope.Scope
	 * @generated
	 */
	EClass getScope();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.model.scope.Scope#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.model.scope.Scope#getName()
	 * @see #getScope()
	 * @generated
	 */
	EAttribute getScope_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.model.scope.Scope#getParentScope <em>Parent Scope</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Parent Scope</em>'.
	 * @see org.eclipse.fennec.model.atlas.model.scope.Scope#getParentScope()
	 * @see #getScope()
	 * @generated
	 */
	EAttribute getScope_ParentScope();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.model.scope.Scope#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.eclipse.fennec.model.atlas.model.scope.Scope#getDescription()
	 * @see #getScope()
	 * @generated
	 */
	EAttribute getScope_Description();

	/**
	 * Returns the meta object for the map '{@link org.eclipse.fennec.model.atlas.model.scope.Scope#getLinks <em>Links</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>Links</em>'.
	 * @see org.eclipse.fennec.model.atlas.model.scope.Scope#getLinks()
	 * @see #getScope()
	 * @generated
	 */
	EReference getScope_Links();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.model.atlas.model.scope.Scope#getStages <em>Stages</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Stages</em>'.
	 * @see org.eclipse.fennec.model.atlas.model.scope.Scope#getStages()
	 * @see #getScope()
	 * @generated
	 */
	EAttribute getScope_Stages();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.model.scope.Scope#getFinalStage <em>Final Stage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Final Stage</em>'.
	 * @see org.eclipse.fennec.model.atlas.model.scope.Scope#getFinalStage()
	 * @see #getScope()
	 * @generated
	 */
	EAttribute getScope_FinalStage();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.model.atlas.model.scope.Scope#getWritableStages <em>Writable Stages</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Writable Stages</em>'.
	 * @see org.eclipse.fennec.model.atlas.model.scope.Scope#getWritableStages()
	 * @see #getScope()
	 * @generated
	 */
	EAttribute getScope_WritableStages();

	/**
	 * Returns the meta object for class '{@link java.util.Map.Entry <em>Links Map</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Links Map</em>'.
	 * @see java.util.Map.Entry
	 * @model keyDataType="org.eclipse.emf.ecore.EString"
	 *        valueDataType="org.eclipse.emf.ecore.EString"
	 * @generated
	 */
	EClass getLinksMap();

	/**
	 * Returns the meta object for the attribute '{@link java.util.Map.Entry <em>Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key</em>'.
	 * @see java.util.Map.Entry
	 * @see #getLinksMap()
	 * @generated
	 */
	EAttribute getLinksMap_Key();

	/**
	 * Returns the meta object for the attribute '{@link java.util.Map.Entry <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see java.util.Map.Entry
	 * @see #getLinksMap()
	 * @generated
	 */
	EAttribute getLinksMap_Value();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.model.scope.ScopeContainer <em>Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Container</em>'.
	 * @see org.eclipse.fennec.model.atlas.model.scope.ScopeContainer
	 * @generated
	 */
	EClass getScopeContainer();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.model.scope.ScopeContainer#getScopes <em>Scopes</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Scopes</em>'.
	 * @see org.eclipse.fennec.model.atlas.model.scope.ScopeContainer#getScopes()
	 * @see #getScopeContainer()
	 * @generated
	 */
	EReference getScopeContainer_Scopes();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.model.scope.StageTransition <em>Stage Transition</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Stage Transition</em>'.
	 * @see org.eclipse.fennec.model.atlas.model.scope.StageTransition
	 * @generated
	 */
	EClass getStageTransition();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.model.scope.StageTransition#getObjectId <em>Object Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Object Id</em>'.
	 * @see org.eclipse.fennec.model.atlas.model.scope.StageTransition#getObjectId()
	 * @see #getStageTransition()
	 * @generated
	 */
	EAttribute getStageTransition_ObjectId();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.model.scope.StageTransition#getTargetStage <em>Target Stage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Target Stage</em>'.
	 * @see org.eclipse.fennec.model.atlas.model.scope.StageTransition#getTargetStage()
	 * @see #getStageTransition()
	 * @generated
	 */
	EAttribute getStageTransition_TargetStage();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ScopeFactory getScopeFactory();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.model.scope.impl.ScopeImpl <em>Scope</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.model.scope.impl.ScopeImpl
		 * @see org.eclipse.fennec.model.atlas.model.scope.impl.ScopePackageImpl#getScope()
		 * @generated
		 */
		EClass SCOPE = eINSTANCE.getScope();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SCOPE__NAME = eINSTANCE.getScope_Name();

		/**
		 * The meta object literal for the '<em><b>Parent Scope</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SCOPE__PARENT_SCOPE = eINSTANCE.getScope_ParentScope();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SCOPE__DESCRIPTION = eINSTANCE.getScope_Description();

		/**
		 * The meta object literal for the '<em><b>Links</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SCOPE__LINKS = eINSTANCE.getScope_Links();

		/**
		 * The meta object literal for the '<em><b>Stages</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SCOPE__STAGES = eINSTANCE.getScope_Stages();

		/**
		 * The meta object literal for the '<em><b>Final Stage</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SCOPE__FINAL_STAGE = eINSTANCE.getScope_FinalStage();

		/**
		 * The meta object literal for the '<em><b>Writable Stages</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SCOPE__WRITABLE_STAGES = eINSTANCE.getScope_WritableStages();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.model.scope.impl.LinksMapImpl <em>Links Map</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.model.scope.impl.LinksMapImpl
		 * @see org.eclipse.fennec.model.atlas.model.scope.impl.ScopePackageImpl#getLinksMap()
		 * @generated
		 */
		EClass LINKS_MAP = eINSTANCE.getLinksMap();

		/**
		 * The meta object literal for the '<em><b>Key</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute LINKS_MAP__KEY = eINSTANCE.getLinksMap_Key();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute LINKS_MAP__VALUE = eINSTANCE.getLinksMap_Value();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.model.scope.impl.ScopeContainerImpl <em>Container</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.model.scope.impl.ScopeContainerImpl
		 * @see org.eclipse.fennec.model.atlas.model.scope.impl.ScopePackageImpl#getScopeContainer()
		 * @generated
		 */
		EClass SCOPE_CONTAINER = eINSTANCE.getScopeContainer();

		/**
		 * The meta object literal for the '<em><b>Scopes</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SCOPE_CONTAINER__SCOPES = eINSTANCE.getScopeContainer_Scopes();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.model.scope.impl.StageTransitionImpl <em>Stage Transition</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.model.scope.impl.StageTransitionImpl
		 * @see org.eclipse.fennec.model.atlas.model.scope.impl.ScopePackageImpl#getStageTransition()
		 * @generated
		 */
		EClass STAGE_TRANSITION = eINSTANCE.getStageTransition();

		/**
		 * The meta object literal for the '<em><b>Object Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGE_TRANSITION__OBJECT_ID = eINSTANCE.getStageTransition_ObjectId();

		/**
		 * The meta object literal for the '<em><b>Target Stage</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGE_TRANSITION__TARGET_STAGE = eINSTANCE.getStageTransition_TargetStage();

	}

} //ScopePackage
