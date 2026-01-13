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
package org.eclipse.fennec.model.atlas.wf.workflowapi.impl;

import java.lang.reflect.InvocationTargetException;

import java.util.List;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;

import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage;

import org.osgi.util.promise.Promise;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Scope Service</b></em>'.
 * <!-- end-user-doc -->
 *
 * @generated
 */
public class ScopeServiceImpl<T extends EObject> extends MinimalEObjectImpl.Container implements ScopeService<T> {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ScopeServiceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return WorkflowApiPackage.Literals.SCOPE_SERVICE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Promise<ObjectMetadata> uploadToStageForRegistry(String registry, String stage, T object, ObjectMetadata metadata) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ObjectMetadata getMetadataFromStageForRegistry(String registry, String stage, String objectId) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ObjectMetadata getMetadataFromFinalStageForRegistry(String registry, String objectId) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public T getContentFromStageForRegistry(String registry, String stage, String objectId) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Promise<ObjectMetadata> updateInStageForRegistry(String registry, String stage, T updatedObject, String objectId, String version) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Promise<Boolean> deleteFromStageForRegistry(String registry, String stage, String objectId) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<ObjectMetadata> listInStageForRegistry(String registry, String stage) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<ObjectMetadata> listInStageForRegistryByName(String registry, String stage, String name) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<ObjectMetadata> listInFinalStageForRegistry(String registry) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ObjectMetadata transitionToStageForRegistry(String registry, String objectId, String fromStage, String toStage) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isValidRegistry(String registryName) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<String> getAllRegistries() {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
			case WorkflowApiPackage.SCOPE_SERVICE___UPLOAD_TO_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_OBJECTMETADATA:
				return uploadToStageForRegistry((String)arguments.get(0), (String)arguments.get(1), (T)arguments.get(2), (ObjectMetadata)arguments.get(3));
			case WorkflowApiPackage.SCOPE_SERVICE___GET_METADATA_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING:
				return getMetadataFromStageForRegistry((String)arguments.get(0), (String)arguments.get(1), (String)arguments.get(2));
			case WorkflowApiPackage.SCOPE_SERVICE___GET_METADATA_FROM_FINAL_STAGE_FOR_REGISTRY__STRING_STRING:
				return getMetadataFromFinalStageForRegistry((String)arguments.get(0), (String)arguments.get(1));
			case WorkflowApiPackage.SCOPE_SERVICE___GET_CONTENT_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING:
				return getContentFromStageForRegistry((String)arguments.get(0), (String)arguments.get(1), (String)arguments.get(2));
			case WorkflowApiPackage.SCOPE_SERVICE___UPDATE_IN_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_STRING_STRING:
				return updateInStageForRegistry((String)arguments.get(0), (String)arguments.get(1), (T)arguments.get(2), (String)arguments.get(3), (String)arguments.get(4));
			case WorkflowApiPackage.SCOPE_SERVICE___DELETE_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING:
				return deleteFromStageForRegistry((String)arguments.get(0), (String)arguments.get(1), (String)arguments.get(2));
			case WorkflowApiPackage.SCOPE_SERVICE___LIST_IN_STAGE_FOR_REGISTRY__STRING_STRING:
				return listInStageForRegistry((String)arguments.get(0), (String)arguments.get(1));
			case WorkflowApiPackage.SCOPE_SERVICE___LIST_IN_STAGE_FOR_REGISTRY_BY_NAME__STRING_STRING_STRING:
				return listInStageForRegistryByName((String)arguments.get(0), (String)arguments.get(1), (String)arguments.get(2));
			case WorkflowApiPackage.SCOPE_SERVICE___LIST_IN_FINAL_STAGE_FOR_REGISTRY__STRING:
				return listInFinalStageForRegistry((String)arguments.get(0));
			case WorkflowApiPackage.SCOPE_SERVICE___TRANSITION_TO_STAGE_FOR_REGISTRY__STRING_STRING_STRING_STRING:
				return transitionToStageForRegistry((String)arguments.get(0), (String)arguments.get(1), (String)arguments.get(2), (String)arguments.get(3));
			case WorkflowApiPackage.SCOPE_SERVICE___IS_VALID_REGISTRY__STRING:
				return isValidRegistry((String)arguments.get(0));
			case WorkflowApiPackage.SCOPE_SERVICE___GET_ALL_REGISTRIES:
				return getAllRegistries();
		}
		return super.eInvoke(operationID, arguments);
	}

} //ScopeServiceImpl
