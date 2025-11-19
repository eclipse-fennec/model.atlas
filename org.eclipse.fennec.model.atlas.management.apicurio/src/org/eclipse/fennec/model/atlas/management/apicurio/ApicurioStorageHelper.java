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
import java.util.UUID;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Artifact;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.ArtifactType;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Content;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.FirstVersion;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioFactory;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.VersionedArtifact;
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

	private final String apicurioURL;

	/**
	 * Creates a new instance.
	 * @param resourceSet
	 */
	public ApicurioStorageHelper(ResourceSet resourceSet, String apicurioURL) {
		super(resourceSet);
		this.apicurioURL = apicurioURL;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#createStorageURI(java.lang.String)
	 */
	@Override
	protected URI createStorageURI(String path) {
		return URI.createURI(apicurioURL);
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
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#saveEObject(java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 */
	@Override
	public void saveEObject(String objectId, EObject object, ObjectMetadata metadata) throws IOException {
		//		String fileExtension = getFileExtension(metadata);
		//        
		//        String objectPath = buildObjectPath(objectId, fileExtension);
		String contentType = getContentType(metadata);
		Resource contentResource = resourceSet.createResource(URI.createURI(UUID.randomUUID().toString()+".json"), contentType);
		contentResource.getContents().add(object);
		Map<String, Object> options = new HashMap<>();
		options.put(EMFJs.OPTION_SERIALIZE_DEFAULT_VALUE, true);
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			contentResource.save(byteArrayOutputStream, options);
			ByteArrayInputStream is = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
			Artifact artifact = createVersionedArtifact(object, metadata, is);
			URI objectUri = URI.createURI(apicurioURL);
			Resource resource = resourceSet.createResource(objectUri, "application/json");
			Resource responseResource = resourceSet.createResource(URI.createURI("temp.json"));
			resource.getContents().add(artifact);
			options.put(EMFUriHandlerConstants.OPTION_HTTP_METHOD, "POST");
			Map<String, String> headers = new HashMap<>();
			headers.put("Content-Type", "application/json");
			options.put(EMFUriHandlerConstants.OPTION_HTTP_HEADERS, headers);
			options.put(EMFUriHandlerConstants.OPTIONS_EXPECTED_RESPONSE_RESOURCE, responseResource);
			resource.save(System.out, options);
			resource.save(options);
			System.out.println("");
		};





		//        ResourceOperation objectOp = createResource(objectUri, "");

		//        try {

		//            Map<String, Object> options = new HashMap<>();

		//            options.put(XMLResource.OPTION_KEEP_DEFAULT_CONTENT, true);
		//            options.put(EMFJs.OPTION_SERIALIZE_DEFAULT_VALUE, true);


		// Let storage implementation handle the actual persistence
		//            We still need the metadata here, otherwise we cannot create our wrapper for the artifact


		////            persistResource(objectPath, objectOp.getResource());
		//        } finally {
		////            objectOp.cleanup();
		//        }
	}

	private Artifact createVersionedArtifact(EObject object, ObjectMetadata metadata, ByteArrayInputStream is) {
		VersionedArtifact artifact = MgmtApicurioFactory.eINSTANCE.createVersionedArtifact();
		String contentType = getContentType(metadata);
		artifact.setArtifactType(convertContentTypeToArtifactType(contentType));
		artifact.setArtifactId(metadata.getObjectId());
		FirstVersion version = MgmtApicurioFactory.eINSTANCE.createFirstVersion();
		version.setVersion(metadata.getVersion());
		Content content = MgmtApicurioFactory.eINSTANCE.createContent();
		content.setContentType(contentType);
		content.setContent(new String(is.readAllBytes()));
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



		return false;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#findObjectPath(java.lang.String)
	 */
	@Override
	protected String findObjectPath(String objectId) throws IOException {
		return apicurioURL.concat("/").concat(objectId);
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

}
