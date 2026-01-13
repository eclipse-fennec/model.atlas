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

import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage;

import org.osgi.util.promise.Promise;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Registry Service</b></em>'.
 * <!-- end-user-doc -->
 *
 * @generated
 */
public class RegistryServiceImpl<T extends EObject> extends MinimalEObjectImpl.Container implements RegistryService<T> {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RegistryServiceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return WorkflowApiPackage.Literals.REGISTRY_SERVICE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Promise<ObjectMetadata> uploadToStage(String scope, String stage, T object, ObjectMetadata metadata) {
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
	public ObjectMetadata getMetadataFromStage(String scope, String stage, String objectId) {
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
	public ObjectMetadata getMetadataFromFinalStage(String scope, String objectId) {
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
	public T getContentFromStage(String scope, String stage, String objectId) {
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
	public Promise<ObjectMetadata> updateInStage(String scope, String stage, T updatedObject, String objectId, String version) {
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
	public Promise<Boolean> deleteFromStage(String scope, String stage, String objectId) {
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
	public List<ObjectMetadata> listInStage(String scope, String stage) {
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
	public List<ObjectMetadata> listInStageByName(String scope, String stage, String name) {
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
	public List<ObjectMetadata> listInFinalStage(String scope) {
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
	public ObjectMetadata transitionToStage(String scope, String objectId, String fromStage, String toStage) {
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
	public String getRegistryName() {
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
	public boolean isValidStage(String stageName) {
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
	public boolean isWritableStage(String stageName) {
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
	public boolean isFinalStageWritable() {
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
	public boolean isTransitionAllowed(String fromStage, String toStage) {
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
			case WorkflowApiPackage.REGISTRY_SERVICE___UPLOAD_TO_STAGE__STRING_STRING_EOBJECT_OBJECTMETADATA:
				return uploadToStage((String)arguments.get(0), (String)arguments.get(1), (T)arguments.get(2), (ObjectMetadata)arguments.get(3));
			case WorkflowApiPackage.REGISTRY_SERVICE___GET_METADATA_FROM_STAGE__STRING_STRING_STRING:
				return getMetadataFromStage((String)arguments.get(0), (String)arguments.get(1), (String)arguments.get(2));
			case WorkflowApiPackage.REGISTRY_SERVICE___GET_METADATA_FROM_FINAL_STAGE__STRING_STRING:
				return getMetadataFromFinalStage((String)arguments.get(0), (String)arguments.get(1));
			case WorkflowApiPackage.REGISTRY_SERVICE___GET_CONTENT_FROM_STAGE__STRING_STRING_STRING:
				return getContentFromStage((String)arguments.get(0), (String)arguments.get(1), (String)arguments.get(2));
			case WorkflowApiPackage.REGISTRY_SERVICE___UPDATE_IN_STAGE__STRING_STRING_EOBJECT_STRING_STRING:
				return updateInStage((String)arguments.get(0), (String)arguments.get(1), (T)arguments.get(2), (String)arguments.get(3), (String)arguments.get(4));
			case WorkflowApiPackage.REGISTRY_SERVICE___DELETE_FROM_STAGE__STRING_STRING_STRING:
				return deleteFromStage((String)arguments.get(0), (String)arguments.get(1), (String)arguments.get(2));
			case WorkflowApiPackage.REGISTRY_SERVICE___LIST_IN_STAGE__STRING_STRING:
				return listInStage((String)arguments.get(0), (String)arguments.get(1));
			case WorkflowApiPackage.REGISTRY_SERVICE___LIST_IN_STAGE_BY_NAME__STRING_STRING_STRING:
				return listInStageByName((String)arguments.get(0), (String)arguments.get(1), (String)arguments.get(2));
			case WorkflowApiPackage.REGISTRY_SERVICE___LIST_IN_FINAL_STAGE__STRING:
				return listInFinalStage((String)arguments.get(0));
			case WorkflowApiPackage.REGISTRY_SERVICE___TRANSITION_TO_STAGE__STRING_STRING_STRING_STRING:
				return transitionToStage((String)arguments.get(0), (String)arguments.get(1), (String)arguments.get(2), (String)arguments.get(3));
			case WorkflowApiPackage.REGISTRY_SERVICE___GET_REGISTRY_NAME:
				return getRegistryName();
			case WorkflowApiPackage.REGISTRY_SERVICE___IS_VALID_STAGE__STRING:
				return isValidStage((String)arguments.get(0));
			case WorkflowApiPackage.REGISTRY_SERVICE___IS_WRITABLE_STAGE__STRING:
				return isWritableStage((String)arguments.get(0));
			case WorkflowApiPackage.REGISTRY_SERVICE___IS_FINAL_STAGE_WRITABLE:
				return isFinalStageWritable();
			case WorkflowApiPackage.REGISTRY_SERVICE___IS_TRANSITION_ALLOWED__STRING_STRING:
				return isTransitionAllowed((String)arguments.get(0), (String)arguments.get(1));
		}
		return super.eInvoke(operationID, arguments);
	}

} //RegistryServiceImpl
