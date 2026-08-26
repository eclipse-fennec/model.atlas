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
import java.util.Objects;
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
 * would not resolve, so such a reference's fragment is resolved against the
 * document being loaded.
 * <p>
 * That reference arrives resolved against the URI this document is parsed under
 * — {@code atlas-client://epackage.ecore/sample.ecore#//Person} — which is how
 * it is told apart from a genuine reference into another package: the latter
 * carries the target namespace's own scheme and authority, and must keep
 * falling through to be fetched rather than being answered from this document.
 * The test is that base, not "only one resource is loaded": EMF demand-creates a
 * placeholder resource for every cross-document href it reads, so any package
 * that references another has more than one resource in the set before a single
 * proxy is resolved — and gating on that count silently disabled this case for
 * exactly the layered models that need it.</li>
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
	 * Resolve an internal self-reference against the document being loaded —
	 * only for a reference that arrived document-relative, i.e. under the URI
	 * this document is parsed under. A reference into another namespace carries
	 * that namespace's own scheme and authority and is left alone, so this
	 * cannot answer a genuine cross-package reference from the wrong document.
	 */
	private EObject resolveAgainstLoadedDocument(URI uri) {
		if (uri == null || uri.fragment() == null) {
			return null;
		}
		Resource document = document();
		if (document == null || !sameBase(uri, document.getURI())) {
			return null;
		}
		return document.getEObject(uri.fragment());
	}

	/**
	 * The one document this set exists to load. A set is created per parse and
	 * the document's resource is the first created in it; anything after it is a
	 * placeholder EMF demand-created for a cross-document href.
	 */
	private Resource document() {
		List<Resource> resources = getResources();
		return resources.isEmpty() ? null : resources.get(0);
	}

	/** Whether {@code uri} sits under the same scheme and authority as the document. */
	private static boolean sameBase(URI uri, URI documentUri) {
		return documentUri != null && uri.scheme() != null && uri.scheme().equals(documentUri.scheme())
				&& Objects.equals(uri.authority(), documentUri.authority());
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
