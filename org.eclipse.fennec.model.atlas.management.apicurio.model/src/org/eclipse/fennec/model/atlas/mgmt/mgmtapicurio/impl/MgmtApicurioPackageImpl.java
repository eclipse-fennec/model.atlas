/**
 */
package org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl;

import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Artifact;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactReference;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactType;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Content;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.CreateArtifact;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.CreateArtifactVersion;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioFactory;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchResponse;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchVersionResponse;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedArtifact;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedVersion;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.VersionStateType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class MgmtApicurioPackageImpl extends EPackageImpl implements MgmtApicurioPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass artifactEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass createArtifactEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass createArtifactVersionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass contentEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass versionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass artifactReferenceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass searchedArtifactEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass searchResponseEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass labelsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass searchVersionResponseEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass searchedVersionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum artifactTypeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum versionStateTypeEEnum = null;

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
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private MgmtApicurioPackageImpl() {
		super(eNS_URI, MgmtApicurioFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link MgmtApicurioPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static MgmtApicurioPackage init() {
		if (isInited) return (MgmtApicurioPackage)EPackage.Registry.INSTANCE.getEPackage(MgmtApicurioPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredMgmtApicurioPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		MgmtApicurioPackageImpl theMgmtApicurioPackage = registeredMgmtApicurioPackage instanceof MgmtApicurioPackageImpl ? (MgmtApicurioPackageImpl)registeredMgmtApicurioPackage : new MgmtApicurioPackageImpl();

		isInited = true;

		// Create package meta-data objects
		theMgmtApicurioPackage.createPackageContents();

		// Initialize created meta-data
		theMgmtApicurioPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theMgmtApicurioPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(MgmtApicurioPackage.eNS_URI, theMgmtApicurioPackage);
		return theMgmtApicurioPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getArtifact() {
		return artifactEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getArtifact_ArtifactId() {
		return (EAttribute)artifactEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getArtifact_ArtifactType() {
		return (EAttribute)artifactEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getArtifact_Name() {
		return (EAttribute)artifactEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getArtifact_Description() {
		return (EAttribute)artifactEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getArtifact_Labels() {
		return (EReference)artifactEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCreateArtifact() {
		return createArtifactEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getCreateArtifact_FirstVersion() {
		return (EReference)createArtifactEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCreateArtifactVersion() {
		return createArtifactVersionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getContent() {
		return contentEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getContent_ContentType() {
		return (EAttribute)contentEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getContent_Content() {
		return (EAttribute)contentEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getContent_References() {
		return (EReference)contentEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getVersion() {
		return versionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getVersion_Version() {
		return (EAttribute)versionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getVersion_Content() {
		return (EReference)versionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getVersion_Name() {
		return (EAttribute)versionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getVersion_Description() {
		return (EAttribute)versionEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getVersion_Branches() {
		return (EAttribute)versionEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getVersion_IsDraft() {
		return (EAttribute)versionEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getVersion_Labels() {
		return (EReference)versionEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getArtifactReference() {
		return artifactReferenceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getArtifactReference_GroupId() {
		return (EAttribute)artifactReferenceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getArtifactReference_ArtifactId() {
		return (EAttribute)artifactReferenceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getArtifactReference_Name() {
		return (EAttribute)artifactReferenceEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getArtifactReference_Version() {
		return (EAttribute)artifactReferenceEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSearchedArtifact() {
		return searchedArtifactEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSearchedArtifact_CreatedOn() {
		return (EAttribute)searchedArtifactEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSearchedArtifact_Owner() {
		return (EAttribute)searchedArtifactEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSearchedArtifact_ModifiedOn() {
		return (EAttribute)searchedArtifactEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSearchedArtifact_ModifiedBy() {
		return (EAttribute)searchedArtifactEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSearchedArtifact_GroupId() {
		return (EAttribute)searchedArtifactEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSearchResponse() {
		return searchResponseEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSearchResponse_Artifacts() {
		return (EReference)searchResponseEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSearchResponse_Count() {
		return (EAttribute)searchResponseEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getLabels() {
		return labelsEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getLabels_Key() {
		return (EAttribute)labelsEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getLabels_Value() {
		return (EAttribute)labelsEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSearchVersionResponse() {
		return searchVersionResponseEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSearchVersionResponse_Versions() {
		return (EReference)searchVersionResponseEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSearchVersionResponse_Count() {
		return (EAttribute)searchVersionResponseEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSearchedVersion() {
		return searchedVersionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSearchedVersion_GlobalId() {
		return (EAttribute)searchedVersionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSearchedVersion_Version() {
		return (EAttribute)searchedVersionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSearchedVersion_ContentId() {
		return (EAttribute)searchedVersionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSearchedVersion_State() {
		return (EAttribute)searchedVersionEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getArtifactType() {
		return artifactTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getVersionStateType() {
		return versionStateTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MgmtApicurioFactory getMgmtApicurioFactory() {
		return (MgmtApicurioFactory)getEFactoryInstance();
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
		artifactEClass = createEClass(ARTIFACT);
		createEAttribute(artifactEClass, ARTIFACT__ARTIFACT_ID);
		createEAttribute(artifactEClass, ARTIFACT__ARTIFACT_TYPE);
		createEAttribute(artifactEClass, ARTIFACT__NAME);
		createEAttribute(artifactEClass, ARTIFACT__DESCRIPTION);
		createEReference(artifactEClass, ARTIFACT__LABELS);

		createArtifactEClass = createEClass(CREATE_ARTIFACT);
		createEReference(createArtifactEClass, CREATE_ARTIFACT__FIRST_VERSION);

		createArtifactVersionEClass = createEClass(CREATE_ARTIFACT_VERSION);

		contentEClass = createEClass(CONTENT);
		createEAttribute(contentEClass, CONTENT__CONTENT_TYPE);
		createEAttribute(contentEClass, CONTENT__CONTENT);
		createEReference(contentEClass, CONTENT__REFERENCES);

		versionEClass = createEClass(VERSION);
		createEAttribute(versionEClass, VERSION__VERSION);
		createEReference(versionEClass, VERSION__CONTENT);
		createEAttribute(versionEClass, VERSION__NAME);
		createEAttribute(versionEClass, VERSION__DESCRIPTION);
		createEAttribute(versionEClass, VERSION__BRANCHES);
		createEAttribute(versionEClass, VERSION__IS_DRAFT);
		createEReference(versionEClass, VERSION__LABELS);

		artifactReferenceEClass = createEClass(ARTIFACT_REFERENCE);
		createEAttribute(artifactReferenceEClass, ARTIFACT_REFERENCE__GROUP_ID);
		createEAttribute(artifactReferenceEClass, ARTIFACT_REFERENCE__ARTIFACT_ID);
		createEAttribute(artifactReferenceEClass, ARTIFACT_REFERENCE__NAME);
		createEAttribute(artifactReferenceEClass, ARTIFACT_REFERENCE__VERSION);

		searchedArtifactEClass = createEClass(SEARCHED_ARTIFACT);
		createEAttribute(searchedArtifactEClass, SEARCHED_ARTIFACT__CREATED_ON);
		createEAttribute(searchedArtifactEClass, SEARCHED_ARTIFACT__OWNER);
		createEAttribute(searchedArtifactEClass, SEARCHED_ARTIFACT__MODIFIED_ON);
		createEAttribute(searchedArtifactEClass, SEARCHED_ARTIFACT__MODIFIED_BY);
		createEAttribute(searchedArtifactEClass, SEARCHED_ARTIFACT__GROUP_ID);

		searchResponseEClass = createEClass(SEARCH_RESPONSE);
		createEReference(searchResponseEClass, SEARCH_RESPONSE__ARTIFACTS);
		createEAttribute(searchResponseEClass, SEARCH_RESPONSE__COUNT);

		labelsEClass = createEClass(LABELS);
		createEAttribute(labelsEClass, LABELS__KEY);
		createEAttribute(labelsEClass, LABELS__VALUE);

		searchVersionResponseEClass = createEClass(SEARCH_VERSION_RESPONSE);
		createEReference(searchVersionResponseEClass, SEARCH_VERSION_RESPONSE__VERSIONS);
		createEAttribute(searchVersionResponseEClass, SEARCH_VERSION_RESPONSE__COUNT);

		searchedVersionEClass = createEClass(SEARCHED_VERSION);
		createEAttribute(searchedVersionEClass, SEARCHED_VERSION__GLOBAL_ID);
		createEAttribute(searchedVersionEClass, SEARCHED_VERSION__VERSION);
		createEAttribute(searchedVersionEClass, SEARCHED_VERSION__CONTENT_ID);
		createEAttribute(searchedVersionEClass, SEARCHED_VERSION__STATE);

		// Create enums
		artifactTypeEEnum = createEEnum(ARTIFACT_TYPE);
		versionStateTypeEEnum = createEEnum(VERSION_STATE_TYPE);
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
		createArtifactEClass.getESuperTypes().add(this.getArtifact());
		createArtifactVersionEClass.getESuperTypes().add(this.getVersion());
		searchedArtifactEClass.getESuperTypes().add(this.getArtifact());
		searchedVersionEClass.getESuperTypes().add(this.getArtifact());

		// Initialize classes, features, and operations; add parameters
		initEClass(artifactEClass, Artifact.class, "Artifact", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getArtifact_ArtifactId(), ecorePackage.getEString(), "artifactId", null, 1, 1, Artifact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getArtifact_ArtifactType(), this.getArtifactType(), "artifactType", null, 0, 1, Artifact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getArtifact_Name(), ecorePackage.getEString(), "name", null, 0, 1, Artifact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getArtifact_Description(), ecorePackage.getEString(), "description", null, 0, 1, Artifact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getArtifact_Labels(), this.getLabels(), null, "labels", null, 0, -1, Artifact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(createArtifactEClass, CreateArtifact.class, "CreateArtifact", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getCreateArtifact_FirstVersion(), this.getVersion(), null, "firstVersion", null, 0, 1, CreateArtifact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(createArtifactVersionEClass, CreateArtifactVersion.class, "CreateArtifactVersion", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(contentEClass, Content.class, "Content", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getContent_ContentType(), ecorePackage.getEString(), "contentType", null, 1, 1, Content.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getContent_Content(), ecorePackage.getEString(), "content", null, 1, 1, Content.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getContent_References(), this.getArtifactReference(), null, "references", null, 0, -1, Content.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(versionEClass, Version.class, "Version", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getVersion_Version(), ecorePackage.getEString(), "version", null, 0, 1, Version.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getVersion_Content(), this.getContent(), null, "content", null, 1, 1, Version.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getVersion_Name(), ecorePackage.getEString(), "name", null, 0, 1, Version.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getVersion_Description(), ecorePackage.getEString(), "description", null, 0, 1, Version.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getVersion_Branches(), ecorePackage.getEString(), "branches", null, 0, -1, Version.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getVersion_IsDraft(), ecorePackage.getEBoolean(), "isDraft", null, 0, 1, Version.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getVersion_Labels(), this.getLabels(), null, "labels", null, 0, -1, Version.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(artifactReferenceEClass, ArtifactReference.class, "ArtifactReference", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getArtifactReference_GroupId(), ecorePackage.getEString(), "groupId", null, 1, 1, ArtifactReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getArtifactReference_ArtifactId(), ecorePackage.getEString(), "artifactId", null, 1, 1, ArtifactReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getArtifactReference_Name(), ecorePackage.getEString(), "name", null, 1, 1, ArtifactReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getArtifactReference_Version(), ecorePackage.getEString(), "version", null, 0, 1, ArtifactReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(searchedArtifactEClass, SearchedArtifact.class, "SearchedArtifact", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSearchedArtifact_CreatedOn(), ecorePackage.getEString(), "createdOn", null, 1, 1, SearchedArtifact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSearchedArtifact_Owner(), ecorePackage.getEString(), "owner", null, 1, 1, SearchedArtifact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSearchedArtifact_ModifiedOn(), ecorePackage.getEString(), "modifiedOn", null, 0, 1, SearchedArtifact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSearchedArtifact_ModifiedBy(), ecorePackage.getEString(), "modifiedBy", null, 0, 1, SearchedArtifact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSearchedArtifact_GroupId(), ecorePackage.getEString(), "groupId", null, 0, 1, SearchedArtifact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(searchResponseEClass, SearchResponse.class, "SearchResponse", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSearchResponse_Artifacts(), this.getSearchedArtifact(), null, "artifacts", null, 0, -1, SearchResponse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSearchResponse_Count(), ecorePackage.getEInt(), "count", null, 1, 1, SearchResponse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(labelsEClass, Map.Entry.class, "Labels", !IS_ABSTRACT, !IS_INTERFACE, !IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getLabels_Key(), ecorePackage.getEString(), "key", null, 0, 1, Map.Entry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getLabels_Value(), ecorePackage.getEString(), "value", null, 0, 1, Map.Entry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(searchVersionResponseEClass, SearchVersionResponse.class, "SearchVersionResponse", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSearchVersionResponse_Versions(), this.getSearchedVersion(), null, "versions", null, 0, -1, SearchVersionResponse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSearchVersionResponse_Count(), ecorePackage.getEInt(), "count", null, 1, 1, SearchVersionResponse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(searchedVersionEClass, SearchedVersion.class, "SearchedVersion", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSearchedVersion_GlobalId(), ecorePackage.getEString(), "globalId", null, 1, 1, SearchedVersion.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSearchedVersion_Version(), ecorePackage.getEString(), "version", null, 1, 1, SearchedVersion.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSearchedVersion_ContentId(), ecorePackage.getEString(), "contentId", null, 1, 1, SearchedVersion.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSearchedVersion_State(), this.getVersionStateType(), "state", null, 1, 1, SearchedVersion.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(artifactTypeEEnum, ArtifactType.class, "ArtifactType");
		addEEnumLiteral(artifactTypeEEnum, ArtifactType.XML);
		addEEnumLiteral(artifactTypeEEnum, ArtifactType.JSON);
		addEEnumLiteral(artifactTypeEEnum, ArtifactType.XSD);
		addEEnumLiteral(artifactTypeEEnum, ArtifactType.ASYNCAPI);
		addEEnumLiteral(artifactTypeEEnum, ArtifactType.AVRO);
		addEEnumLiteral(artifactTypeEEnum, ArtifactType.GRAPHQL);
		addEEnumLiteral(artifactTypeEEnum, ArtifactType.KCONNECT);
		addEEnumLiteral(artifactTypeEEnum, ArtifactType.OPENAPI);
		addEEnumLiteral(artifactTypeEEnum, ArtifactType.PROTOBUF);
		addEEnumLiteral(artifactTypeEEnum, ArtifactType.WSDL);

		initEEnum(versionStateTypeEEnum, VersionStateType.class, "VersionStateType");
		addEEnumLiteral(versionStateTypeEEnum, VersionStateType.ENABLED);
		addEEnumLiteral(versionStateTypeEEnum, VersionStateType.DISABLED);
		addEEnumLiteral(versionStateTypeEEnum, VersionStateType.DEPRECATED);
		addEEnumLiteral(versionStateTypeEEnum, VersionStateType.DRAFT);

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
			   "basePackage", "org.eclipse.fennec.model.atlas.mgmt",
			   "resource", "XMI"
		   });
		addAnnotation
		  (createArtifactEClass,
		   source,
		   new String[] {
			   "documentation", "When you first create an artifact in Apicurio, this is the object you should use."
		   });
		addAnnotation
		  (createArtifactVersionEClass,
		   source,
		   new String[] {
			   "documentation", "Creates a new version of the artifact by uploading new content."
		   });
		addAnnotation
		  (getContent_ContentType(),
		   source,
		   new String[] {
			   "documentation", "The content-type, such as application/json or text/xml."
		   });
		addAnnotation
		  (getContent_Content(),
		   source,
		   new String[] {
			   "documentation", "Raw content of the artifact version or a valid (and accessible) URL where the content can be found."
		   });
		addAnnotation
		  (getVersion_Version(),
		   source,
		   new String[] {
			   "documentation", "A single version of an artifact. Can be provided by the client when creating a new version, or it can be server-generated. The value can be any string unique to the artifact, but it is recommended to use a simple integer or a semver value."
		   });
		addAnnotation
		  (artifactTypeEEnum.getELiterals().get(4),
		   source,
		   new String[] {
			   "documentation", "Apache Avro schema"
		   });
		addAnnotation
		  (artifactTypeEEnum.getELiterals().get(6),
		   source,
		   new String[] {
			   "documentation", "Apache Kafka Connect schema"
		   });
		addAnnotation
		  (artifactTypeEEnum.getELiterals().get(8),
		   source,
		   new String[] {
			   "documentation", "Google protocol buffers schema"
		   });
		addAnnotation
		  (artifactTypeEEnum.getELiterals().get(9),
		   source,
		   new String[] {
			   "documentation", "Web Service Definition Language"
		   });
		addAnnotation
		  (getSearchResponse_Count(),
		   source,
		   new String[] {
			   "documentation", "The total number of artifacts that matched the query that produced the result set (may be more than the number of artifacts in the result set)."
		   });
		addAnnotation
		  (getSearchVersionResponse_Count(),
		   source,
		   new String[] {
			   "documentation", "The total number of artifacts that matched the query that produced the result set (may be more than the number of artifacts in the result set)."
		   });
	}

} //MgmtApicurioPackageImpl
