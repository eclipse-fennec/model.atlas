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
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#createStorageURI(java.lang.String)
	 */
	@Override
	protected URI createStorageURI(String path) {
		return URI.createURI(path);
	}
	
//	@Override
//	protected URI createStorageURI(String path) {
//		return URI.createURI(apicurioURL.concat("/").concat(path));
//	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#persistResource(java.lang.String, org.eclipse.emf.ecore.resource.Resource)
	 */
	@Override
	protected void persistResource(String path, Resource resource) throws IOException {


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

//		we should search for versions where the artifactId is = objectId
//		      we should sort the versions and take the latest one 
//			  then we shoould query the versions endpoint for the content of the artifact
		URI searchVersionsURI = URI.createURI(config.base_url().concat("search/versions?orderBy=version&order=desc&artifactId=").concat(objectId));
		SearchVersionResponse versionResponse = (SearchVersionResponse) sendGETRequest(searchVersionsURI, MgmtApicurioPackage.Literals.SEARCH_VERSION_RESPONSE, "application/json");
		if(versionResponse == null || versionResponse.getCount() == 0) return null;
		
		SearchedVersion latestVersion = versionResponse.getVersions().get(0);
		String version = latestVersion.getVersion();
		String eClassURI = latestVersion.getLabels().get("objectEClassURI");
		if(eClassURI == null) {
			LOGGER.severe(String.format("Cannot retrieve object %s because it was not possible to determine its objectEClassURI from the Version", objectId));
			return null;
		}
		String contentType = latestVersion.getLabels().get("contentType");
		if(contentType == null) contentType = "application/xmi";
		URI objectUri = URI.createURI(apicurioURL.concat("/").concat(objectPath).concat("/versions/").concat(version).concat("/content"));		
		EObject eObj = sendGETRequest(objectUri, (EClass) resourceSet.getEObject(URI.createURI(eClassURI), false), contentType);
		return eObj;
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
	
	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#buildMetadataPath(java.lang.String)
	 */
//	@Override
//	protected String buildMetadataPath(String objectId) {
//		 return objectId + METADATA_WITHOUT_EXTENSION;
//	}
	
	
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

//		TODO: we should search for versions where the artifactId is = objectId
//		      we should sort the versions and take the latest one 
//			  then we shoould query the versions endpoint for the content of the artifact
		URI searchVersionsURI = URI.createURI(config.base_url().concat("search/versions?orderBy=version&order=desc&artifactId=").concat(objectPath));
		SearchVersionResponse versionResponse = (SearchVersionResponse) sendGETRequest(searchVersionsURI, MgmtApicurioPackage.Literals.SEARCH_VERSION_RESPONSE, "application/json");
		if(versionResponse == null || versionResponse.getCount() == 0) return null;
		
		SearchedVersion latestVersion = versionResponse.getVersions().get(0);
		String version = latestVersion.getVersion();
		String eClassURI = latestVersion.getLabels().get("objectEClassURI");
		if(eClassURI == null) {
			LOGGER.severe(String.format("Cannot retrieve object %s because it was not possible to determine its objectEClassURI from the Version", objectId));
			return null;
		}
		
		URI objectUri = URI.createURI(apicurioURL.concat("/").concat(objectPath).concat("/versions/").concat(version).concat("/content"));		
		ObjectMetadata eObj = (ObjectMetadata) sendGETRequest(objectUri, (EClass) resourceSet.getEObject(URI.createURI(eClassURI), false), "application/xmi");
		return eObj;
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#buildObjectPath(java.lang.String, java.lang.String)
	 */
	@Override
	protected String buildObjectPath(String objectId, String extension) {
		return config.artifact_group_id().concat(":").concat(config.storage_role()).concat(":").concat(objectId).concat(extension);
//		return super.buildObjectPath(objectId, extension);
	}
	


	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#saveEObject(java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 */
	@Override
	public String saveEObject(String objectId, EObject object, ObjectMetadata metadata) throws IOException {
//		String fileExtension = getFileExtension(metadata);
        String contentType = getContentType(metadata);
        
        URI objectUri = URI.createURI(objectId);
        ResourceOperation contentRes = createResource(objectUri, contentType);
        contentRes.getResource().getContents().add(object);
        ResourceOperation objectOp = createResource(URI.createURI(apicurioURL.concat("?ifExists=CREATE_VERSION")), "application/json");
        String storageId = objectId;
		try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			contentRes.getResource().save(System.out, null);
			contentRes.getResource().save(byteArrayOutputStream, null);
			ByteArrayInputStream is = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());			
			Artifact artifact = createArtifact(objectId, contentType, metadata, is, false);
			storageId = artifact.getArtifactId();
			Resource apicurioResource = objectOp.getResource();
			apicurioResource.getContents().add(artifact);
			Map<String, Object> options = new HashMap<>();
			options.put(EMFUriHandlerConstants.OPTION_HTTP_METHOD, "POST");
			Map<String, String> headers = new HashMap<>();
			headers.put("Content-Type", "application/json");
			options.put(EMFUriHandlerConstants.OPTION_HTTP_HEADERS, headers);
			apicurioResource.save(System.out, options);
			apicurioResource.save(options);
		} finally {
			objectOp.cleanup();
			contentRes.cleanup();
		}
        
        return storageId;
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#saveMetadata(java.lang.String, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 */
	@Override
	public void saveMetadata(String objectId, ObjectMetadata metadata) throws IOException {
		 Objects.requireNonNull(objectId, "Cannot save metadata - objectId cannot be null");
	        Objects.requireNonNull(metadata, "Cannot save metadata - metadata cannot be null");
	        
	        if (objectId.isEmpty()) {
	            throw new IllegalArgumentException("Cannot save metadata - objectId cannot be empty");
	        }
	        
	        // CRITICAL: Ensure metadata always has objectId before persistence
	        if (Objects.isNull(metadata.getObjectId()) || metadata.getObjectId().isEmpty()) {
	            metadata.setObjectId(objectId);
	            LOGGER.fine("Set objectId in metadata before saving: " + objectId);
	        }
	        
	        // Validate that objectId matches the storage path
	        if (!Objects.equals(objectId, metadata.getObjectId())) {
	            throw new IllegalStateException("Metadata objectId (" + metadata.getObjectId() + 
	                                          ") does not match storage objectId (" + objectId + ")");
	        }
	        
	        String metadataPath = buildMetadataPath(objectId);
	        URI metadataUri = createStorageURI(metadataPath);
	        ResourceOperation contentRes = createResource(metadataUri, "application/xmi");
	        contentRes.getResource().getContents().add(metadata);
	        ResourceOperation objectOp = createResource(URI.createURI(apicurioURL.concat("?ifExists=CREATE_VERSION")), "application/json");
			try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
				contentRes.getResource().save(byteArrayOutputStream, null);
				ByteArrayInputStream is = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());			
				Artifact artifact = createArtifact(metadataPath, "application/xml", metadata, is, true);
				artifact.getArtifactId();
				Resource apicurioResource = objectOp.getResource();
				apicurioResource.getContents().add(artifact);
				Map<String, Object> options = new HashMap<>();
				options.put(EMFUriHandlerConstants.OPTION_HTTP_METHOD, "POST");
				Map<String, String> headers = new HashMap<>();
				headers.put("Content-Type", "application/json");
				options.put(EMFUriHandlerConstants.OPTION_HTTP_HEADERS, headers);
//				apicurioResource.save(System.out, options);
				apicurioResource.save(options);
			} finally {
				objectOp.cleanup();
				contentRes.cleanup();
			}
	        
	}

	private Artifact createArtifact(String objectId, String contentType, ObjectMetadata metadata, ByteArrayInputStream is, boolean isMetadata) {
		Content content = MgmtApicurioFactory.eINSTANCE.createContent();
//		String contentType = fileExtension.equals(".json") ? "application/json" : "application/xml";
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

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#storageExists(java.lang.String)
	 */
	@Override
	protected boolean storageExists(String path) throws IOException {
		try {
			SearchedArtifact searchResponse = (SearchedArtifact) sendGETRequest(URI.createURI(apicurioURL.concat("/").concat(path)), MgmtApicurioPackage.Literals.SEARCHED_ARTIFACT, "application/json");
			if(searchResponse == null) return false;
			return true;		
		} catch(IOException e) {
			return false;
		}
		
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#objectExists(java.lang.String)
	 */
	@Override
	public boolean objectExists(String objectId) throws IOException {
		if (objectId == null || objectId.isEmpty()) {
            return false;
        }
        
        // Check if metadata exists (most reliable indicator)
        String metadataPath = buildMetadataPath(objectId);
        if (storageExists(metadataPath)) {
            return true;
        }
//        We might want to check if the object exists with any extension but then we have to implement the search call in apicurio
        return false;
	}
	
	
	
	private SearchResponse searchArtifacts(String url) throws IOException {
		ResourceOperation objectOp = createResource(URI.createURI(url), "application/json");
		try {
			Map<String, Object> options = new HashMap<>();
			options.put(EMFJs.OPTION_ROOT_ELEMENT, MgmtApicurioPackage.Literals.SEARCH_RESPONSE);
			options.put(EMFUriHandlerConstants.OPTION_HTTP_METHOD, "GET");
			objectOp.getResource().load(options);
			if(!objectOp.getResource().getContents().isEmpty()) {
				if(objectOp.getResource().getContents().get(0) instanceof SearchResponse response) {
					return response;
				} else {
					LOGGER.severe(String.format("No response of type SearchResponse for request %s", url));
				}
			} else {
				LOGGER.severe(String.format("No response content for request %s", url));
			}
		} 
		finally {
			objectOp.cleanup();
		}
		return null;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#findObjectPath(java.lang.String)
	 */
	@Override
	protected String findObjectPath(String objectId) throws IOException {
		SearchResponse searchResponse = searchArtifacts(config.base_url().concat("search/artifacts?groupId=").concat(config.artifact_group_id()).concat("&artifactId=").concat(objectId));
		if(searchResponse != null && searchResponse.getCount() > 0) return objectId;
		return null;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#deleteObject(java.lang.String)
	 */
	@Override
	public boolean deleteObject(String objectId) {
		//Remove both object (all extensions and versions) and metadata
		return doDelete(apicurioURL.concat("/").concat(objectId)) && doDelete(apicurioURL.concat("/").concat(objectId).concat(METADATA_EXTENSION));
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
	
	private String constructApicurioURL(String baseURL, String gourpId) {
		return baseURL.concat("groups/").concat(gourpId).concat("/artifacts");
	}

}
