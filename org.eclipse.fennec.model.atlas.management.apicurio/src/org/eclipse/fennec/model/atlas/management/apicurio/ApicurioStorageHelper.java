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

import java.io.IOException;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper;

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
		return URI.createURI(path);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#persistResource(java.lang.String, org.eclipse.emf.ecore.resource.Resource)
	 */
	@Override
	protected void persistResource(String path, Resource resource) throws IOException {
		// TODO Auto-generated method stub
		
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
