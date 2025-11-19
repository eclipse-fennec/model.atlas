/*
 */
package org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
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
 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioFactory
 * @model kind="package"
 *        annotation="Version value='1.0'"
 *        annotation="http://www.eclipse.org/emf/2002/GenModel complianceLevel='17.0' oSGiCompatible='true' basePackage='org.eclipse.fennec.model.atlas.mgmt' resource='XMI'"
 * @generated
 */
@ProviderType
@EPackage(uri = MgmtApicurioPackage.eNS_URI, genModel = "/model/mgmt-apicurio.genmodel", genModelSourceLocations = {"model/mgmt-apicurio.genmodel","org.eclipse.fennec.model.atlas.management.apicurio.model/model/mgmt-apicurio.genmodel"}, ecore="/model/mgmt-apicurio.ecore", ecoreSourceLocations="/model/mgmt-apicurio.ecore")
public interface MgmtApicurioPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "mgmtapicurio";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://eclipse.org/fennec/model/atlas/management/apicurio/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "mgmtapicurio";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	MgmtApicurioPackage eINSTANCE = org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.ArtifactImpl <em>Artifact</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.ArtifactImpl
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl#getArtifact()
	 * @generated
	 */
	int ARTIFACT = 0;

	/**
	 * The feature id for the '<em><b>Artifact Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARTIFACT__ARTIFACT_ID = 0;

	/**
	 * The feature id for the '<em><b>Artifact Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARTIFACT__ARTIFACT_TYPE = 1;

	/**
	 * The number of structural features of the '<em>Artifact</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARTIFACT_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Artifact</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARTIFACT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.SimpleArtifactImpl <em>Simple Artifact</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.SimpleArtifactImpl
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl#getSimpleArtifact()
	 * @generated
	 */
	int SIMPLE_ARTIFACT = 1;

	/**
	 * The feature id for the '<em><b>Artifact Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_ARTIFACT__ARTIFACT_ID = ARTIFACT__ARTIFACT_ID;

	/**
	 * The feature id for the '<em><b>Artifact Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_ARTIFACT__ARTIFACT_TYPE = ARTIFACT__ARTIFACT_TYPE;

	/**
	 * The feature id for the '<em><b>Content</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_ARTIFACT__CONTENT = ARTIFACT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Simple Artifact</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_ARTIFACT_FEATURE_COUNT = ARTIFACT_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Simple Artifact</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_ARTIFACT_OPERATION_COUNT = ARTIFACT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.VersionedArtifactImpl <em>Versioned Artifact</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.VersionedArtifactImpl
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl#getVersionedArtifact()
	 * @generated
	 */
	int VERSIONED_ARTIFACT = 2;

	/**
	 * The feature id for the '<em><b>Artifact Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VERSIONED_ARTIFACT__ARTIFACT_ID = ARTIFACT__ARTIFACT_ID;

	/**
	 * The feature id for the '<em><b>Artifact Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VERSIONED_ARTIFACT__ARTIFACT_TYPE = ARTIFACT__ARTIFACT_TYPE;

	/**
	 * The feature id for the '<em><b>First Version</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VERSIONED_ARTIFACT__FIRST_VERSION = ARTIFACT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Versioned Artifact</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VERSIONED_ARTIFACT_FEATURE_COUNT = ARTIFACT_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Versioned Artifact</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VERSIONED_ARTIFACT_OPERATION_COUNT = ARTIFACT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.ContentImpl <em>Content</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.ContentImpl
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl#getContent()
	 * @generated
	 */
	int CONTENT = 3;

	/**
	 * The feature id for the '<em><b>Content Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTENT__CONTENT_TYPE = 0;

	/**
	 * The feature id for the '<em><b>Content</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTENT__CONTENT = 1;

	/**
	 * The number of structural features of the '<em>Content</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTENT_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Content</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTENT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.FirstVersionImpl <em>First Version</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.FirstVersionImpl
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl#getFirstVersion()
	 * @generated
	 */
	int FIRST_VERSION = 4;

	/**
	 * The feature id for the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIRST_VERSION__VERSION = 0;

	/**
	 * The feature id for the '<em><b>Content</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIRST_VERSION__CONTENT = 1;

	/**
	 * The number of structural features of the '<em>First Version</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIRST_VERSION_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>First Version</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIRST_VERSION_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactType <em>Artifact Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactType
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl#getArtifactType()
	 * @generated
	 */
	int ARTIFACT_TYPE = 5;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Artifact <em>Artifact</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Artifact</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Artifact
	 * @generated
	 */
	EClass getArtifact();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Artifact#getArtifactId <em>Artifact Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Artifact Id</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Artifact#getArtifactId()
	 * @see #getArtifact()
	 * @generated
	 */
	EAttribute getArtifact_ArtifactId();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Artifact#getArtifactType <em>Artifact Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Artifact Type</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Artifact#getArtifactType()
	 * @see #getArtifact()
	 * @generated
	 */
	EAttribute getArtifact_ArtifactType();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SimpleArtifact <em>Simple Artifact</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Simple Artifact</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SimpleArtifact
	 * @generated
	 */
	EClass getSimpleArtifact();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SimpleArtifact#getContent <em>Content</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Content</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SimpleArtifact#getContent()
	 * @see #getSimpleArtifact()
	 * @generated
	 */
	EReference getSimpleArtifact_Content();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.VersionedArtifact <em>Versioned Artifact</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Versioned Artifact</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.VersionedArtifact
	 * @generated
	 */
	EClass getVersionedArtifact();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.VersionedArtifact#getFirstVersion <em>First Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>First Version</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.VersionedArtifact#getFirstVersion()
	 * @see #getVersionedArtifact()
	 * @generated
	 */
	EReference getVersionedArtifact_FirstVersion();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Content <em>Content</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Content</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Content
	 * @generated
	 */
	EClass getContent();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Content#getContentType <em>Content Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Content Type</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Content#getContentType()
	 * @see #getContent()
	 * @generated
	 */
	EAttribute getContent_ContentType();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Content#getContent <em>Content</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Content</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Content#getContent()
	 * @see #getContent()
	 * @generated
	 */
	EAttribute getContent_Content();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.FirstVersion <em>First Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>First Version</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.FirstVersion
	 * @generated
	 */
	EClass getFirstVersion();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.FirstVersion#getVersion <em>Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Version</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.FirstVersion#getVersion()
	 * @see #getFirstVersion()
	 * @generated
	 */
	EAttribute getFirstVersion_Version();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.FirstVersion#getContent <em>Content</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Content</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.FirstVersion#getContent()
	 * @see #getFirstVersion()
	 * @generated
	 */
	EReference getFirstVersion_Content();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactType <em>Artifact Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Artifact Type</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactType
	 * @generated
	 */
	EEnum getArtifactType();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	MgmtApicurioFactory getMgmtApicurioFactory();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.ArtifactImpl <em>Artifact</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.ArtifactImpl
		 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl#getArtifact()
		 * @generated
		 */
		EClass ARTIFACT = eINSTANCE.getArtifact();

		/**
		 * The meta object literal for the '<em><b>Artifact Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ARTIFACT__ARTIFACT_ID = eINSTANCE.getArtifact_ArtifactId();

		/**
		 * The meta object literal for the '<em><b>Artifact Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ARTIFACT__ARTIFACT_TYPE = eINSTANCE.getArtifact_ArtifactType();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.SimpleArtifactImpl <em>Simple Artifact</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.SimpleArtifactImpl
		 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl#getSimpleArtifact()
		 * @generated
		 */
		EClass SIMPLE_ARTIFACT = eINSTANCE.getSimpleArtifact();

		/**
		 * The meta object literal for the '<em><b>Content</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SIMPLE_ARTIFACT__CONTENT = eINSTANCE.getSimpleArtifact_Content();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.VersionedArtifactImpl <em>Versioned Artifact</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.VersionedArtifactImpl
		 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl#getVersionedArtifact()
		 * @generated
		 */
		EClass VERSIONED_ARTIFACT = eINSTANCE.getVersionedArtifact();

		/**
		 * The meta object literal for the '<em><b>First Version</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VERSIONED_ARTIFACT__FIRST_VERSION = eINSTANCE.getVersionedArtifact_FirstVersion();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.ContentImpl <em>Content</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.ContentImpl
		 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl#getContent()
		 * @generated
		 */
		EClass CONTENT = eINSTANCE.getContent();

		/**
		 * The meta object literal for the '<em><b>Content Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CONTENT__CONTENT_TYPE = eINSTANCE.getContent_ContentType();

		/**
		 * The meta object literal for the '<em><b>Content</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CONTENT__CONTENT = eINSTANCE.getContent_Content();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.FirstVersionImpl <em>First Version</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.FirstVersionImpl
		 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl#getFirstVersion()
		 * @generated
		 */
		EClass FIRST_VERSION = eINSTANCE.getFirstVersion();

		/**
		 * The meta object literal for the '<em><b>Version</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FIRST_VERSION__VERSION = eINSTANCE.getFirstVersion_Version();

		/**
		 * The meta object literal for the '<em><b>Content</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FIRST_VERSION__CONTENT = eINSTANCE.getFirstVersion_Content();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactType <em>Artifact Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactType
		 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl#getArtifactType()
		 * @generated
		 */
		EEnum ARTIFACT_TYPE = eINSTANCE.getArtifactType();

	}

} //MgmtApicurioPackage
