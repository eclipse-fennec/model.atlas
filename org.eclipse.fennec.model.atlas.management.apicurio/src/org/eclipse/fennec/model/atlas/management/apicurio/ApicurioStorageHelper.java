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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.management.apicurio.EObjectApicurioStorageService.Config;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Artifact;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactType;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Content;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.CreateArtifact;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioFactory;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper;
import org.gecko.emf.osgi.constants.EMFUriHandlerConstants;

/**
 * 
 * Apicurio-based implementation of storage helper for EMF objects.
 * Extends AbstarctStorageHelper to provide apicurio specific operations.
 * @since Nov 18, 2025
 */
public class ApicurioStorageHelper extends AbstractStorageHelper {

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

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#persistResource(java.lang.String, org.eclipse.emf.ecore.resource.Resource)
	 */
	@Override
	protected void persistResource(String path, Resource resource) throws IOException {


	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#persistResource(java.lang.String, org.eclipse.emf.ecore.resource.Resource, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 */
	@Override
	protected void persistResource(String path, Resource resource, ObjectMetadata metadata) throws IOException {
		ResourceOperation objectOp = createResource(URI.createURI(apicurioURL), "application/json");
		try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			resource.save(byteArrayOutputStream, null);
			ByteArrayInputStream is = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());			
			Artifact artifact = createArtifact(path, resource.getURI().fileExtension(), metadata, is);
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
		}
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

		URI objectUri = createStorageURI(objectPath);
		ResourceOperation operation = loadResource(objectUri);
		try {
			if (operation.getResource().getContents().isEmpty()) {
				return null;
			}
			return operation.getResource().getContents().get(0);
		} finally {
			operation.cleanup();
		}
	}


	private Artifact createArtifact(String objectId, String fileExtension, ObjectMetadata metadata, ByteArrayInputStream is) {
		if(objectId.endsWith(fileExtension)) {
			objectId = objectId.replaceFirst("."+fileExtension, "");
		}
		Content content = MgmtApicurioFactory.eINSTANCE.createContent();
		String contentType = fileExtension.equals("json") ? "application/json" : "application/xml";
		content.setContentType(contentType);
		content.setContent(new String(is.readAllBytes()));	
		ArtifactType artifactType = convertContentTypeToArtifactType(contentType);
		CreateArtifact artifact = MgmtApicurioFactory.eINSTANCE.createCreateArtifact();
		artifact.setArtifactType(artifactType);
		artifact.setArtifactId(objectId);
		artifact.setName(metadata.getObjectName());
		Version version = MgmtApicurioFactory.eINSTANCE.createVersion();
		if(metadata.getVersion() != null && !metadata.getVersion().isEmpty()) {
			version.setVersion(metadata.getVersion());
		}
		if("draft".equals(storageRole.toLowerCase().trim())) {
			version.setIsDraft(true);
		}
		version.setName(metadata.getObjectName());
		version.setContent(content);
		artifact.setFirstVersion(version);
		return artifact;

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
		//TODO: REMOVE THE EXTENSION !
		return doStorageExists(apicurioURL.concat("/").concat(path));
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
	
	private boolean doStorageExists(String url) {
		ResourceOperation objectOp = createResource(URI.createURI(url), "application/json");
		try {
			Map<String, Object> options = new HashMap<>();
			options.put(EMFUriHandlerConstants.OPTION_HTTP_METHOD, "GET");
			objectOp.getResource().load(options);
		} catch(Exception e) {			
			return false;
		}		
		finally {
			objectOp.cleanup();
		}
		return true;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#findObjectPath(java.lang.String)
	 */
	@Override
	protected String findObjectPath(String objectId) throws IOException {
//		TODO: here we need to extract the result of the search. Because the doStorageExissts return true even if the count is 0 for search!
		if(doStorageExists(config.base_url().concat("search/artifacts?groupId=").concat(config.artifact_group_id()).concat("&artifactId=").concat(objectId))) {
			return objectId;
		}
		return null;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#deleteObject(java.lang.String)
	 */
	@Override
	public boolean deleteObject(String objectId) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#listObjectIds()
	 */
	@Override
	public List<String> listObjectIds() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	private String constructApicurioURL(String baseURL, String gourpId) {
		return baseURL.concat("groups/").concat(gourpId).concat("/artifacts");
	}

}
