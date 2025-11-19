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
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARTIFACT__NAME = 2;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARTIFACT__DESCRIPTION = 3;

	/**
	 * The number of structural features of the '<em>Artifact</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARTIFACT_FEATURE_COUNT = 4;

	/**
	 * The number of operations of the '<em>Artifact</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARTIFACT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.CreateArtifactImpl <em>Create Artifact</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.CreateArtifactImpl
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl#getCreateArtifact()
	 * @generated
	 */
	int CREATE_ARTIFACT = 1;

	/**
	 * The feature id for the '<em><b>Artifact Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CREATE_ARTIFACT__ARTIFACT_ID = ARTIFACT__ARTIFACT_ID;

	/**
	 * The feature id for the '<em><b>Artifact Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CREATE_ARTIFACT__ARTIFACT_TYPE = ARTIFACT__ARTIFACT_TYPE;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CREATE_ARTIFACT__NAME = ARTIFACT__NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CREATE_ARTIFACT__DESCRIPTION = ARTIFACT__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>First Version</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CREATE_ARTIFACT__FIRST_VERSION = ARTIFACT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Create Artifact</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CREATE_ARTIFACT_FEATURE_COUNT = ARTIFACT_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Create Artifact</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CREATE_ARTIFACT_OPERATION_COUNT = ARTIFACT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.VersionImpl <em>Version</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.VersionImpl
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl#getVersion()
	 * @generated
	 */
	int VERSION = 4;

	/**
	 * The feature id for the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VERSION__VERSION = 0;

	/**
	 * The feature id for the '<em><b>Content</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VERSION__CONTENT = 1;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VERSION__NAME = 2;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VERSION__DESCRIPTION = 3;

	/**
	 * The feature id for the '<em><b>Branches</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VERSION__BRANCHES = 4;

	/**
	 * The feature id for the '<em><b>Is Draft</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VERSION__IS_DRAFT = 5;

	/**
	 * The number of structural features of the '<em>Version</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VERSION_FEATURE_COUNT = 6;

	/**
	 * The number of operations of the '<em>Version</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VERSION_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.CreateArtifactVersionImpl <em>Create Artifact Version</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.CreateArtifactVersionImpl
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl#getCreateArtifactVersion()
	 * @generated
	 */
	int CREATE_ARTIFACT_VERSION = 2;

	/**
	 * The feature id for the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CREATE_ARTIFACT_VERSION__VERSION = VERSION__VERSION;

	/**
	 * The feature id for the '<em><b>Content</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CREATE_ARTIFACT_VERSION__CONTENT = VERSION__CONTENT;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CREATE_ARTIFACT_VERSION__NAME = VERSION__NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CREATE_ARTIFACT_VERSION__DESCRIPTION = VERSION__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Branches</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CREATE_ARTIFACT_VERSION__BRANCHES = VERSION__BRANCHES;

	/**
	 * The feature id for the '<em><b>Is Draft</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CREATE_ARTIFACT_VERSION__IS_DRAFT = VERSION__IS_DRAFT;

	/**
	 * The number of structural features of the '<em>Create Artifact Version</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CREATE_ARTIFACT_VERSION_FEATURE_COUNT = VERSION_FEATURE_COUNT + 0;

	/**
	 * The number of operations of the '<em>Create Artifact Version</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CREATE_ARTIFACT_VERSION_OPERATION_COUNT = VERSION_OPERATION_COUNT + 0;

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
	 * The feature id for the '<em><b>References</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTENT__REFERENCES = 2;

	/**
	 * The number of structural features of the '<em>Content</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTENT_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Content</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTENT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.ArtifactReferenceImpl <em>Artifact Reference</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.ArtifactReferenceImpl
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl#getArtifactReference()
	 * @generated
	 */
	int ARTIFACT_REFERENCE = 5;

	/**
	 * The feature id for the '<em><b>Group Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARTIFACT_REFERENCE__GROUP_ID = 0;

	/**
	 * The feature id for the '<em><b>Artifact Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARTIFACT_REFERENCE__ARTIFACT_ID = 1;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARTIFACT_REFERENCE__NAME = 2;

	/**
	 * The feature id for the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARTIFACT_REFERENCE__VERSION = 3;

	/**
	 * The number of structural features of the '<em>Artifact Reference</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARTIFACT_REFERENCE_FEATURE_COUNT = 4;

	/**
	 * The number of operations of the '<em>Artifact Reference</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARTIFACT_REFERENCE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactType <em>Artifact Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactType
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl#getArtifactType()
	 * @generated
	 */
	int ARTIFACT_TYPE = 6;


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
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Artifact#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Artifact#getName()
	 * @see #getArtifact()
	 * @generated
	 */
	EAttribute getArtifact_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Artifact#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Artifact#getDescription()
	 * @see #getArtifact()
	 * @generated
	 */
	EAttribute getArtifact_Description();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.CreateArtifact <em>Create Artifact</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Create Artifact</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.CreateArtifact
	 * @generated
	 */
	EClass getCreateArtifact();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.CreateArtifact#getFirstVersion <em>First Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>First Version</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.CreateArtifact#getFirstVersion()
	 * @see #getCreateArtifact()
	 * @generated
	 */
	EReference getCreateArtifact_FirstVersion();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.CreateArtifactVersion <em>Create Artifact Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Create Artifact Version</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.CreateArtifactVersion
	 * @generated
	 */
	EClass getCreateArtifactVersion();

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
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Content#getReferences <em>References</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>References</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Content#getReferences()
	 * @see #getContent()
	 * @generated
	 */
	EReference getContent_References();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version <em>Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Version</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version
	 * @generated
	 */
	EClass getVersion();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#getVersion <em>Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Version</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#getVersion()
	 * @see #getVersion()
	 * @generated
	 */
	EAttribute getVersion_Version();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#getContent <em>Content</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Content</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#getContent()
	 * @see #getVersion()
	 * @generated
	 */
	EReference getVersion_Content();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#getName()
	 * @see #getVersion()
	 * @generated
	 */
	EAttribute getVersion_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#getDescription()
	 * @see #getVersion()
	 * @generated
	 */
	EAttribute getVersion_Description();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#getBranches <em>Branches</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Branches</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#getBranches()
	 * @see #getVersion()
	 * @generated
	 */
	EAttribute getVersion_Branches();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#isIsDraft <em>Is Draft</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Is Draft</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#isIsDraft()
	 * @see #getVersion()
	 * @generated
	 */
	EAttribute getVersion_IsDraft();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactReference <em>Artifact Reference</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Artifact Reference</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactReference
	 * @generated
	 */
	EClass getArtifactReference();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactReference#getGroupId <em>Group Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Group Id</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactReference#getGroupId()
	 * @see #getArtifactReference()
	 * @generated
	 */
	EAttribute getArtifactReference_GroupId();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactReference#getArtifactId <em>Artifact Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Artifact Id</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactReference#getArtifactId()
	 * @see #getArtifactReference()
	 * @generated
	 */
	EAttribute getArtifactReference_ArtifactId();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactReference#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactReference#getName()
	 * @see #getArtifactReference()
	 * @generated
	 */
	EAttribute getArtifactReference_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactReference#getVersion <em>Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Version</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactReference#getVersion()
	 * @see #getArtifactReference()
	 * @generated
	 */
	EAttribute getArtifactReference_Version();

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
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ARTIFACT__NAME = eINSTANCE.getArtifact_Name();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ARTIFACT__DESCRIPTION = eINSTANCE.getArtifact_Description();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.CreateArtifactImpl <em>Create Artifact</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.CreateArtifactImpl
		 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl#getCreateArtifact()
		 * @generated
		 */
		EClass CREATE_ARTIFACT = eINSTANCE.getCreateArtifact();

		/**
		 * The meta object literal for the '<em><b>First Version</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CREATE_ARTIFACT__FIRST_VERSION = eINSTANCE.getCreateArtifact_FirstVersion();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.CreateArtifactVersionImpl <em>Create Artifact Version</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.CreateArtifactVersionImpl
		 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl#getCreateArtifactVersion()
		 * @generated
		 */
		EClass CREATE_ARTIFACT_VERSION = eINSTANCE.getCreateArtifactVersion();

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
		 * The meta object literal for the '<em><b>References</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CONTENT__REFERENCES = eINSTANCE.getContent_References();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.VersionImpl <em>Version</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.VersionImpl
		 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl#getVersion()
		 * @generated
		 */
		EClass VERSION = eINSTANCE.getVersion();

		/**
		 * The meta object literal for the '<em><b>Version</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VERSION__VERSION = eINSTANCE.getVersion_Version();

		/**
		 * The meta object literal for the '<em><b>Content</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VERSION__CONTENT = eINSTANCE.getVersion_Content();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VERSION__NAME = eINSTANCE.getVersion_Name();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VERSION__DESCRIPTION = eINSTANCE.getVersion_Description();

		/**
		 * The meta object literal for the '<em><b>Branches</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VERSION__BRANCHES = eINSTANCE.getVersion_Branches();

		/**
		 * The meta object literal for the '<em><b>Is Draft</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VERSION__IS_DRAFT = eINSTANCE.getVersion_IsDraft();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.ArtifactReferenceImpl <em>Artifact Reference</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.ArtifactReferenceImpl
		 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.MgmtApicurioPackageImpl#getArtifactReference()
		 * @generated
		 */
		EClass ARTIFACT_REFERENCE = eINSTANCE.getArtifactReference();

		/**
		 * The meta object literal for the '<em><b>Group Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ARTIFACT_REFERENCE__GROUP_ID = eINSTANCE.getArtifactReference_GroupId();

		/**
		 * The meta object literal for the '<em><b>Artifact Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ARTIFACT_REFERENCE__ARTIFACT_ID = eINSTANCE.getArtifactReference_ArtifactId();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ARTIFACT_REFERENCE__NAME = eINSTANCE.getArtifactReference_Name();

		/**
		 * The meta object literal for the '<em><b>Version</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ARTIFACT_REFERENCE__VERSION = eINSTANCE.getArtifactReference_Version();

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
