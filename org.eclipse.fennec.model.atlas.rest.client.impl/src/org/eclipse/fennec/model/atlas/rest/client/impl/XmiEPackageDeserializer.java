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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClientException;

/**
 * Default {@link EPackageDeserializer}: parses the {@code application/xmi}
 * (Ecore XMI) body the server emits for a package content GET into a fully
 * resolved {@link EPackage} (P2-4).
 * <p>
 * This mirrors the server side ({@code EcoreMessageBodyHandler.readFrom}) with
 * stock EMF — no codec — into a self-contained {@link ResourceSet}: an
 * {@link EcoreResourceFactoryImpl} for any URI and {@code Ecore} pre-registered
 * so cross-references into {@code Ecore} (e.g. {@code EString} attribute types)
 * resolve. The returned package carries a dynamic {@code EFactory}, so
 * {@code pkg.getEFactoryInstance().create(eClass)} works.
 * <p>
 * The package is freshly parsed and is <em>not</em> added to any shared
 * registry ({@code EPackage.Registry.INSTANCE} or a framework registry) — that
 * publication is the caller's / Phase-3's concern.
 * <p>
 * The parse set's package registry delegates to the registry of the packages
 * the runtime ships itself, so a reference into one of their namespaces
 * resolves to the local instance rather than to an Atlas copy (issue #330).
 */
public class XmiEPackageDeserializer implements EPackageDeserializer {

	private final EPackage.Registry localPackages;

	/** Parses against {@code EPackage.Registry.INSTANCE} as the local packages. */
	public XmiEPackageDeserializer() {
		this(EPackage.Registry.INSTANCE);
	}

	/**
	 * @param localPackages the packages the runtime ships itself; consulted for
	 *                      every namespace this document references
	 */
	public XmiEPackageDeserializer(EPackage.Registry localPackages) {
		this.localPackages = Objects.requireNonNull(localPackages, "localPackages");
	}

	@Override
	public EPackage deserialize(InputStream content, String nsUri, String mediaType) {
		ResourceSet resourceSet = createResourceSet();
		// Absolute URI to avoid "resolve against non-hierarchical or relative base".
		Resource resource = resourceSet.createResource(URI.createURI("atlas-client://epackage.ecore"));
		try {
			resource.load(content, loadOptions());
		} catch (IOException e) {
			throw new ModelAtlasClientException("Failed to read EPackage XMI for " + nsUri, e);
		}
		if (!resource.getErrors().isEmpty()) {
			throw new ModelAtlasClientException(
					"EPackage XMI for " + nsUri + " had load errors: " + resource.getErrors());
		}
		if (resource.getContents().isEmpty()) {
			throw new ModelAtlasClientException("EPackage XMI for " + nsUri + " contained no content");
		}
		EObject root = resource.getContents().get(0);
		if (!(root instanceof EPackage)) {
			throw new ModelAtlasClientException("EPackage XMI for " + nsUri + " did not contain an EPackage but "
					+ root.eClass().getName());
		}
		return (EPackage) root;
	}

	/**
	 * A standalone {@link ResourceSet} for loading Ecore XMI: any URI is handled
	 * by an {@link EcoreResourceFactoryImpl}, {@code Ecore} is registered so
	 * references into it resolve, and the local packages sit underneath.
	 */
	protected ResourceSet createResourceSet() {
		ResourceSet resourceSet = new PackageLoadingResourceSet();
		resourceSet.setPackageRegistry(new EPackageRegistryImpl(localPackages));
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new EcoreResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
		return resourceSet;
	}

	/** Robust XMI load options, matching the server's {@code EcoreMessageBodyHandler}. */
	private static Map<Object, Object> loadOptions() {
		Map<Object, Object> options = new HashMap<>();
		options.put(XMLResource.OPTION_DEFER_ATTACHMENT, Boolean.TRUE);
		options.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
		options.put(XMLResource.OPTION_LAX_FEATURE_PROCESSING, Boolean.TRUE);
		options.put(XMLResource.OPTION_RECORD_UNKNOWN_FEATURE, Boolean.TRUE);
		return options;
	}
}
