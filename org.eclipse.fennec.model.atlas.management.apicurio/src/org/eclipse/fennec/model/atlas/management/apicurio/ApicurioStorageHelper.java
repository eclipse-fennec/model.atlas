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
package org.eclipse.fennec.model.atlas.management.apicurio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.management.apicurio.EObjectApicurioStorageService.Config;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Artifact;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactType;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Content;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.CreateArtifact;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioFactory;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchResponse;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchVersionResponse;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedArtifact;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedVersion;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper;
import org.gecko.emf.json.constants.EMFJs;
import org.gecko.emf.osgi.constants.EMFUriHandlerConstants;

/**
 * 
 * Apicurio-based implementation of storage helper for EMF objects.
 * Extends AbstarctStorageHelper to provide apicurio specific operations.
 * @since Nov 18, 2025
 */
public class ApicurioStorageHelper extends AbstractStorageHelper {

	private static final Logger LOGGER = Logger.getLogger(ApicurioStorageHelper.class.getName());
	private final String apicurioURL;
	private String storageRole;
	private Config config;

	/**
	 * Creates a new instance.
	 * @param resourceSet
	 */
	public ApicurioStorageHelper(ResourceSet resourceSet, EObjectApicurioStorageService.Config config) {
		super(resourceSet);
		this.config = config;
		this.apicurioURL = constructApicurioURL(config.base_url(), config.artifact_group_id());
		this.storageRole = config.storage_role();
	}



	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#loadEObject(java.lang.String)
	 */
	@Override
	public EObject loadEObject(String objectId) throws IOException {
		String objectPath = findObjectPath(objectId);
		if (objectPath == null) {
			return null;
		}

		SearchedVersion latestVersion = getLatestVersionForArtifactId(objectId);
		if(isLatestVersionValid(latestVersion, objectId)) {
			String contentType = latestVersion.getLabels().get("contentType");
			if(contentType == null) contentType = "application/xmi";
			URI objectUri = URI.createURI(apicurioURL.concat("/").concat(objectPath).concat("/versions/").concat(latestVersion.getVersion()).concat("/content"));		
			EObject eObj = sendGETRequest(objectUri, (EClass) resourceSet.getEObject(URI.createURI(latestVersion.getLabels().get("objectEClassURI")), false), contentType);
			return eObj;
		}
		return null;
		
	}



	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#loadMetadata(java.lang.String)
	 */
	@Override
	public ObjectMetadata loadMetadata(String objectId) throws IOException {
		String objectPath = buildMetadataPath(objectId);
		if (objectPath == null) {
			return null;
		}		
		SearchedVersion latestVersion = getLatestVersionForArtifactId(objectPath);
		if(isLatestVersionValid(latestVersion, objectId)) {
			URI objectUri = URI.createURI(apicurioURL.concat("/").concat(objectPath).concat("/versions/").concat(latestVersion.getVersion()).concat("/content"));		
			ObjectMetadata eObj = (ObjectMetadata) sendGETRequest(objectUri, (EClass) resourceSet.getEObject(URI.createURI(latestVersion.getLabels().get("objectEClassURI")), false), "application/xmi");
			return eObj;
		} 
		return null;
		
	}

	

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#saveEObject(java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 */
	@Override
	public void saveEObject(String objectId, EObject object, ObjectMetadata metadata) throws IOException {
		String contentType = getContentType(metadata);
		postArtifact(objectId, object, contentType, metadata, false);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#saveMetadata(java.lang.String, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 */
	@Override
	public void saveMetadata(String objectId, ObjectMetadata metadata) throws IOException {
		validateMetadata(objectId, metadata);
		String metadataPath = buildMetadataPath(objectId);
		postArtifact(metadataPath, metadata, "application/xml", metadata, true);
	}

	

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#deleteObject(java.lang.String)
	 */
	@Override
	public boolean deleteObject(String objectId) {
		return doDelete(apicurioURL.concat("/").concat(objectId)) && doDelete(apicurioURL.concat("/").concat(objectId).concat(METADATA_EXTENSION));
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#listObjectIds()
	 */
	@Override
	public List<String> listObjectIds() throws IOException {
		URI url = URI.createURI(apicurioURL);
		SearchResponse searchResponse = (SearchResponse) sendGETRequest(url, MgmtApicurioPackage.Literals.SEARCH_RESPONSE, "application/json");
		if(searchResponse == null || searchResponse.getCount() == 0) return Collections.emptyList();
		return searchResponse.getArtifacts().stream().map(a -> a.getArtifactId()).filter(id -> !id.endsWith(".metadata.xmi")).toList();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#findObjectPath(java.lang.String)
	 */
	@Override
	protected String findObjectPath(String objectId) throws IOException {
		String url = config.base_url().concat("search/artifacts?groupId=").concat(config.artifact_group_id()).concat("&artifactId=").concat(objectId);
		SearchResponse searchResponse =  (SearchResponse) sendGETRequest(
			      URI.createURI(url),
			      MgmtApicurioPackage.Literals.SEARCH_RESPONSE,
			      "application/json"
			  );
		if(searchResponse != null && searchResponse.getCount() > 0) return objectId;
		return null;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#storageExists(java.lang.String)
	 */
	@Override
	protected boolean storageExists(String path) throws IOException {
		try {
			SearchedArtifact searchResponse = (SearchedArtifact) sendGETRequest(URI.createURI(apicurioURL.concat("/").concat(path)), MgmtApicurioPackage.Literals.SEARCHED_ARTIFACT, "application/json");
			return searchResponse != null;		
		} catch(IOException e) {
			return false;
		}

	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#createStorageURI(java.lang.String)
	 */
	@Override
	protected URI createStorageURI(String path) {
		return URI.createURI(path);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#persistResource(java.lang.String, org.eclipse.emf.ecore.resource.Resource)
	 */
	@Override
	protected void persistResource(String path, Resource resource) throws IOException {
	}


	private String constructApicurioURL(String baseURL, String groupId) {
		return baseURL.concat("groups/").concat(groupId).concat("/artifacts");
	}

	private EObject sendGETRequest(URI uri, EClass expectedResponseEClass, String mediaType) throws IOException {
		ResourceOperation searchOp = createResource(uri, mediaType);
		try {
			Map<String, Object> options = new HashMap<>();
			options.put(EMFJs.OPTION_ROOT_ELEMENT, expectedResponseEClass);
			options.put(EMFUriHandlerConstants.OPTION_HTTP_METHOD, "GET");
			searchOp.getResource().load(options);
			if(!searchOp.getResource().getContents().isEmpty()) {
				if(searchOp.getResource().getContents().get(0).eClass().getInstanceClassName().equals(expectedResponseEClass.getInstanceClassName())) {
					return searchOp.getResource().getContents().get(0);
				} else {
					LOGGER.severe(String.format("No response of type %s for request %s", expectedResponseEClass.getName(), uri));
				}
			} else {
				LOGGER.severe(String.format("No response content for request %s", uri));
			}
		} finally {
			searchOp.cleanup();
		}
		return null;
	}

	private boolean doDelete(String url) {
		ResourceOperation objectOp = createResource(URI.createURI(url), "application/json");
		try {
			Map<String, Object> options = new HashMap<>();
			options.put(EMFUriHandlerConstants.OPTION_HTTP_METHOD, "DELETE");
			objectOp.getResource().save(options);
		} catch(IOException e) {
			LOGGER.severe(String.format("Error removing artifact %s from Apicurio registry", url));
			return false;
		}
		finally {
			objectOp.cleanup();
		}
		return true;
	}


	private Artifact createArtifact(String objectId, String contentType, ObjectMetadata metadata, ByteArrayInputStream is, boolean isMetadata) {
		Content content = MgmtApicurioFactory.eINSTANCE.createContent();
		content.setContentType(contentType);
		content.setContent(new String(is.readAllBytes()));	
		ArtifactType artifactType = convertContentTypeToArtifactType(contentType);
		CreateArtifact artifact = MgmtApicurioFactory.eINSTANCE.createCreateArtifact();
		artifact.setArtifactType(artifactType);
		artifact.setArtifactId(objectId);
		artifact.setName(metadata.getObjectName());
		addLabels(artifact.getLabels(), metadata, isMetadata);
		artifact.getLabels().put("contentType", contentType);
		Version version = MgmtApicurioFactory.eINSTANCE.createVersion();
		if(metadata.getVersion() != null && !metadata.getVersion().isEmpty()) {
			version.setVersion(metadata.getVersion());
		}
		if("draft".equals(storageRole.toLowerCase().trim())) {
			version.setIsDraft(true);
		}
		addLabels(version.getLabels(), metadata, isMetadata);
		version.getLabels().put("contentType", contentType);
		version.setName(metadata.getObjectName());
		version.setContent(content);
		artifact.setFirstVersion(version);
		return artifact;

	}

	private void addLabels(EMap<String, String> labels, ObjectMetadata metadata, boolean isMetadata) {
		if(metadata.getUploadUser() != null) labels.put("uploadUser", metadata.getUploadUser());
		if(metadata.getUploadTime() != null) labels.put("uploadTime", metadata.getUploadTime().toString());
		if(metadata.getLastChangeUser() != null) labels.put("lastChangeUser", metadata.getLastChangeUser());
		if(metadata.getLastChangeTime() != null) labels.put("lastChangeTime", metadata.getLastChangeTime().toString());
		if(metadata.getReviewUser() != null) labels.put("reviewUser", metadata.getReviewUser());
		if(metadata.getReviewTime() != null) labels.put("reviewTime", metadata.getReviewTime().toString());
		if(metadata.getReviewReason() != null) labels.put("reviewReason", metadata.getReviewReason());
		if(metadata.getContentHash() != null) labels.put("contentHash", metadata.getContentHash());
		if(metadata.getGenerationTriggerFingerprint() != null) labels.put("generationTriggerFingerprint", metadata.getGenerationTriggerFingerprint());
		if(isMetadata) {
			labels.put("objectType", "ObjectMetadata");
			labels.put("objectEClassURI", EcoreUtil.getURI(metadata.eClass()).toString());
		} else {
			if(metadata.getObjectType() != null) labels.put("objectType", metadata.getObjectType());
			if(metadata.getObjectRef() != null) labels.put("objectEClassURI", EcoreUtil.getURI(metadata.getObjectRef().eClass()).toString());
		}


	}

	private ArtifactType convertContentTypeToArtifactType(String contentType) {
		Objects.requireNonNull(contentType, "Content Type cannot be null to store in Apicurio");
		switch(contentType) {
		case "application/json", "application/schema+json", "json":
			return ArtifactType.JSON;
		case "application/xml", "application/xmi":
			return ArtifactType.XML;
		case "application/xsd":
			return ArtifactType.XSD;
		default:
			throw new IllegalArgumentException(String.format("Cannot convert content type %s into proper Apicurio artifact type", contentType));
		}
	}
	
	private SearchedVersion getLatestVersionForArtifactId(String artifactId) throws IOException {
		URI searchVersionsURI = URI.createURI(config.base_url().concat("search/versions?orderBy=version&order=desc&artifactId=").concat(artifactId));
		SearchVersionResponse versionResponse = (SearchVersionResponse) sendGETRequest(searchVersionsURI, MgmtApicurioPackage.Literals.SEARCH_VERSION_RESPONSE, "application/json");
		if(versionResponse == null || versionResponse.getCount() == 0) return null;
		return versionResponse.getVersions().get(0);
	}
	
	private boolean isLatestVersionValid(SearchedVersion latestVersion, String objectId) {
		if(latestVersion == null) {
			LOGGER.severe(String.format("No version for artifact %s was found", objectId));
			return false;
		}
		String eClassURI = latestVersion.getLabels().get("objectEClassURI");
		if(eClassURI == null) {
			LOGGER.severe(String.format("Cannot retrieve object %s because it was not possible to determine its objectEClassURI from the Version", objectId));
			return false;
		}		
		return true;
	}
	
	private void postArtifact(String artifactId, EObject content, String contentType, 
			ObjectMetadata metadata, boolean isMetadata) throws IOException {
		URI contentUri = URI.createURI(artifactId);
		ResourceOperation contentRes = createResource(contentUri, contentType);
		contentRes.getResource().getContents().add(content);
		ResourceOperation objectOp = createResource(
				URI.createURI(apicurioURL.concat("?ifExists=CREATE_VERSION")), "application/json");

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			contentRes.getResource().save(baos, null);
			ByteArrayInputStream is = new ByteArrayInputStream(baos.toByteArray());
			Artifact artifact = createArtifact(artifactId, contentType, metadata, is, isMetadata);

			Resource apicurioResource = objectOp.getResource();
			apicurioResource.getContents().add(artifact);

			Map<String, Object> options = Map.of(
					EMFUriHandlerConstants.OPTION_HTTP_METHOD, "POST",
					EMFUriHandlerConstants.OPTION_HTTP_HEADERS, Map.of("Content-Type", "application/json")
					);
			apicurioResource.save(options);
		} finally {
			objectOp.cleanup();
			contentRes.cleanup();
		}
	}


}
