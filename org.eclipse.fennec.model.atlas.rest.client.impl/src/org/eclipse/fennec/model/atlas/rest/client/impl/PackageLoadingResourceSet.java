/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
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
package org.eclipse.fennec.model.atlas.rest.client.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

/**
 * A {@link ResourceSetImpl} for loading a single EPackage XMI document, able to
 * resolve its references <em>without any I/O</em>. Two cases the default set
 * cannot handle in a plain-Java client:
 * <ol>
 * <li><b>References into a registered package</b> (e.g. an {@code EString}
 * attribute type). {@code Ecore} has no backing resource in a plain-Java client,
 * so the default set would try to demand-load
 * {@code http://www.eclipse.org/emf/2002/Ecore} and fail. Instead the reference
 * is resolved against the registered {@link EPackage} (materializing a cached
 * holder resource for it if needed).</li>
 * <li><b>Internal self-references serialized with the server's resource name.</b>
 * EMF saves with a relative resource URI, so a same-document reference comes
 * across as e.g. {@code sample.ecore#//Person}. Loaded under a different URI it
 * would not resolve, so — while exactly one document is being loaded — such a
 * reference's fragment is resolved against that one document.</li>
 * </ol>
 * Genuinely external references (to another Atlas package not yet fetched) stay
 * unresolved here; fetching them is the Atlas-aware ResourceSet's job (P2-8).
 */
class PackageLoadingResourceSet extends ResourceSetImpl {

	private static final Map<EPackage, Resource> PACKAGE_HOLDERS = new ConcurrentHashMap<>();

	@Override
	public EObject getEObject(URI uri, boolean loadOnDemand) {
		EObject viaRegistry = resolveAgainstRegistry(uri);
		if (viaRegistry != null) {
			return viaRegistry;
		}
		EObject viaLoadedDocument = resolveAgainstLoadedDocument(uri);
		if (viaLoadedDocument != null) {
			return viaLoadedDocument;
		}
		return super.getEObject(uri, loadOnDemand);
	}

	/** Resolve a reference into a package registered in this set (e.g. Ecore). */
	private EObject resolveAgainstRegistry(URI uri) {
		if (uri == null || uri.fragment() == null) {
			return null;
		}
		EPackage ePackage = getPackageRegistry().getEPackage(uri.trimFragment().toString());
		if (ePackage == null) {
			return null;
		}
		Resource resource = ePackage.eResource();
		if (resource == null) {
			resource = PACKAGE_HOLDERS.computeIfAbsent(ePackage, PackageLoadingResourceSet::holderFor);
		}
		return resource.getEObject(uri.fragment());
	}

	/**
	 * Resolve an internal self-reference against the single document being
	 * loaded. Only active while exactly one resource is present, so it cannot
	 * mis-route once more than one document is in play.
	 */
	private EObject resolveAgainstLoadedDocument(URI uri) {
		if (uri == null || uri.fragment() == null) {
			return null;
		}
		List<Resource> resources = getResources();
		if (resources.size() != 1) {
			return null;
		}
		return resources.get(0).getEObject(uri.fragment());
	}

	private static Resource holderFor(EPackage ePackage) {
		String nsUri = ePackage.getNsURI();
		Resource holder = new XMIResourceImpl(URI.createURI(nsUri != null ? nsUri : "urn:epackage"));
		// Gives the registered package a resource so fragment resolution works.
		// Cached, so the package's resource is set once and stays stable.
		holder.getContents().add(ePackage);
		return holder;
	}
}
