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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl;

import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class MgmtApicurioFactoryImpl extends EFactoryImpl implements MgmtApicurioFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static MgmtApicurioFactory init() {
		try {
			MgmtApicurioFactory theMgmtApicurioFactory = (MgmtApicurioFactory)EPackage.Registry.INSTANCE.getEFactory(MgmtApicurioPackage.eNS_URI);
			if (theMgmtApicurioFactory != null) {
				return theMgmtApicurioFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new MgmtApicurioFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MgmtApicurioFactoryImpl() {
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
			case MgmtApicurioPackage.ARTIFACT: return createArtifact();
			case MgmtApicurioPackage.CREATE_ARTIFACT: return createCreateArtifact();
			case MgmtApicurioPackage.CREATE_ARTIFACT_VERSION: return createCreateArtifactVersion();
			case MgmtApicurioPackage.CONTENT: return createContent();
			case MgmtApicurioPackage.VERSION: return createVersion();
			case MgmtApicurioPackage.ARTIFACT_REFERENCE: return createArtifactReference();
			case MgmtApicurioPackage.SEARCHED_ARTIFACT: return createSearchedArtifact();
			case MgmtApicurioPackage.SEARCH_RESPONSE: return createSearchResponse();
			case MgmtApicurioPackage.LABELS: return (EObject)createLabels();
			case MgmtApicurioPackage.SEARCH_VERSION_RESPONSE: return createSearchVersionResponse();
			case MgmtApicurioPackage.SEARCHED_VERSION: return createSearchedVersion();
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
			case MgmtApicurioPackage.ARTIFACT_TYPE:
				return createArtifactTypeFromString(eDataType, initialValue);
			case MgmtApicurioPackage.VERSION_STATE_TYPE:
				return createVersionStateTypeFromString(eDataType, initialValue);
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
			case MgmtApicurioPackage.ARTIFACT_TYPE:
				return convertArtifactTypeToString(eDataType, instanceValue);
			case MgmtApicurioPackage.VERSION_STATE_TYPE:
				return convertVersionStateTypeToString(eDataType, instanceValue);
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
	public Artifact createArtifact() {
		ArtifactImpl artifact = new ArtifactImpl();
		return artifact;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CreateArtifact createCreateArtifact() {
		CreateArtifactImpl createArtifact = new CreateArtifactImpl();
		return createArtifact;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CreateArtifactVersion createCreateArtifactVersion() {
		CreateArtifactVersionImpl createArtifactVersion = new CreateArtifactVersionImpl();
		return createArtifactVersion;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Content createContent() {
		ContentImpl content = new ContentImpl();
		return content;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Version createVersion() {
		VersionImpl version = new VersionImpl();
		return version;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ArtifactReference createArtifactReference() {
		ArtifactReferenceImpl artifactReference = new ArtifactReferenceImpl();
		return artifactReference;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SearchedArtifact createSearchedArtifact() {
		SearchedArtifactImpl searchedArtifact = new SearchedArtifactImpl();
		return searchedArtifact;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SearchResponse createSearchResponse() {
		SearchResponseImpl searchResponse = new SearchResponseImpl();
		return searchResponse;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Map.Entry<String, String> createLabels() {
		LabelsImpl labels = new LabelsImpl();
		return labels;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SearchVersionResponse createSearchVersionResponse() {
		SearchVersionResponseImpl searchVersionResponse = new SearchVersionResponseImpl();
		return searchVersionResponse;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SearchedVersion createSearchedVersion() {
		SearchedVersionImpl searchedVersion = new SearchedVersionImpl();
		return searchedVersion;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ArtifactType createArtifactTypeFromString(EDataType eDataType, String initialValue) {
		ArtifactType result = ArtifactType.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertArtifactTypeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public VersionStateType createVersionStateTypeFromString(EDataType eDataType, String initialValue) {
		VersionStateType result = VersionStateType.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertVersionStateTypeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MgmtApicurioPackage getMgmtApicurioPackage() {
		return (MgmtApicurioPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static MgmtApicurioPackage getPackage() {
		return MgmtApicurioPackage.eINSTANCE;
	}

} //MgmtApicurioFactoryImpl
