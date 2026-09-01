/*
 * ******************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Data In Motion Consulting - initial implementation
 * ******************************************************************
 */
package org.eclipse.fennec.model.atlas.mcp.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.URIHandlerImpl;

/**
 * Serializes one {@link EPackage} to the {@code application/xmi} body the
 * model.atlas create-package endpoint consumes.
 * <p>
 * This duplicates {@code emf.tools}' {@code Exports.toEcore} on purpose:
 * {@code org.eclipse.fennec.mcp.emf.tools.core} is a private package, and making
 * it API only so that a second bundle can serialize a package would widen the
 * EMF tool bundle's contract for a reason that has nothing to do with it.
 * <p>
 * What matters here is the same thing that matters there — the package and every
 * foreign package it references are copied in a single {@link EcoreUtil.Copier}
 * pass and each copy is put in a resource keyed by its own namespace URI, so a
 * supertype from another package leaves as {@code <nsURI>#//<Name>} rather than
 * as a dangling reference or a local file path. Foreign packages are referenced,
 * never inlined: the server receives one package, and resolves the rest itself.
 *
 * @author ilenia
 * @since Aug 27, 2026
 */
final class EcoreXmi {

	private static final EcoreResourceFactoryImpl ECORE_RESOURCE_FACTORY = new EcoreResourceFactoryImpl();

	private EcoreXmi() {
	}

	/**
	 * @param ePackage the package to serialize
	 * @return the {@code .ecore} XMI document as a UTF-8 string
	 * @throws ToolException if the package has no namespace URI or cannot be serialized
	 */
	static String toXmi(EPackage ePackage) {
		String nsURI = ePackage.getNsURI();
		if (nsURI == null || nsURI.isBlank()) {
			throw new ToolException("The package has no namespace URI and cannot be published");
		}
		EcoreUtil.Copier copier = new EcoreUtil.Copier();
		EPackage copy = (EPackage) copier.copy(ePackage);
		Map<String, EPackage> foreignCopies = new LinkedHashMap<>();
		for (EPackage foreign : foreignPackages(ePackage)) {
			foreignCopies.put(foreign.getNsURI(), (EPackage) copier.copy(foreign));
		}
		copier.copyReferences();

		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = packageResource(resourceSet, nsURI, copy);
		foreignCopies.forEach((foreignNsURI, foreignCopy) -> packageResource(resourceSet, foreignNsURI, foreignCopy));

		URI documentURI = URI.createURI(nsURI);
		Map<String, Object> options = new LinkedHashMap<>();
		options.put(XMLResource.OPTION_ENCODING, StandardCharsets.UTF_8.name());
		// Deresolve references into this document to their '#//Name' fragment form and
		// leave every other reference absolute. EMF's default handler deresolves both,
		// which turns a foreign package sharing a host into a bare relative segment
		// that the server cannot resolve back to a namespace.
		options.put(XMLResource.OPTION_URI_HANDLER, new URIHandlerImpl() {
			@Override
			public URI deresolve(URI uri) {
				return documentURI.equals(uri.trimFragment())
						? URI.createURI("#" + uri.fragment())
						: uri;
			}
		});
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			resource.save(out, options);
		} catch (IOException | RuntimeException e) {
			throw new ToolException(String.format(
					"Serialization of '%s' failed: %s. This usually means the package references a classifier "
							+ "that belongs to no registered package.", nsURI, e.getMessage()));
		}
		return out.toString(StandardCharsets.UTF_8);
	}

	/**
	 * The distinct root packages referenced from outside the given package.
	 * {@link EcorePackage} is left out: it already sits in a resource keyed by its
	 * own namespace URI, so its built-ins serialize correctly untouched.
	 */
	private static Collection<EPackage> foreignPackages(EPackage ePackage) {
		Map<String, EPackage> foreign = new LinkedHashMap<>();
		collectForeign(ePackage, ePackage, foreign);
		for (Iterator<EObject> contents = ePackage.eAllContents(); contents.hasNext();) {
			collectForeign(ePackage, contents.next(), foreign);
		}
		return foreign.values();
	}

	private static void collectForeign(EPackage root, EObject eObject, Map<String, EPackage> foreign) {
		for (EObject referenced : eObject.eCrossReferences()) {
			EPackage owner = rootPackageOf(referenced);
			if (owner == null || owner == root || owner == EcorePackage.eINSTANCE) {
				continue;
			}
			String nsURI = owner.getNsURI();
			if (nsURI != null && !nsURI.isBlank()) {
				foreign.putIfAbsent(nsURI, owner);
			}
		}
	}

	private static EPackage rootPackageOf(EObject eObject) {
		for (EObject current = eObject; current != null; current = current.eContainer()) {
			if (current instanceof EPackage candidate && candidate.getESuperPackage() == null) {
				return candidate;
			}
		}
		return null;
	}

	/**
	 * Places a package in a resource keyed by its own namespace URI, which is what
	 * makes a reference into it serialize as {@code <nsURI>#//<Name>}. The resource
	 * comes from {@link EcoreResourceFactoryImpl} — the factory every {@code .ecore}
	 * is written with — so the qualified {@code eType="ecore:EClass <uri>"} form the
	 * server's deserializer expects is what goes on the wire.
	 */
	private static Resource packageResource(ResourceSet resourceSet, String nsURI, EPackage ePackage) {
		Resource resource = ECORE_RESOURCE_FACTORY.createResource(URI.createURI(nsURI));
		resourceSet.getResources().add(resource);
		resource.getContents().add(ePackage);
		resourceSet.getPackageRegistry().put(nsURI, ePackage);
		return resource;
	}
}
