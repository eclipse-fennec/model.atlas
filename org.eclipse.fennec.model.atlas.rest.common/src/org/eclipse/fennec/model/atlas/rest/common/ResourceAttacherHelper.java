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
package org.eclipse.fennec.model.atlas.rest.common;

import java.util.UUID;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

/**
 * Puts a deserialized object into a resource of the request's {@link ResourceSet}.
 *
 * <p>
 * A request body arrives as a detached {@link EObject}. Anything that has to resolve a
 * URI against it — proxy resolution, an OCL expression naming a context classifier —
 * needs it to have an {@link EObject#eResource() eResource} in the same resource set as
 * the models it is validated against. Attaching it is what these endpoints need, and all
 * this class does.
 *
 * @author ilenia
 * @since May 20, 2026
 */
public final class ResourceAttacherHelper {

	private ResourceAttacherHelper() {
	}

	/**
	 * Attaches {@code eObject} to a new, in-memory resource of {@code resourceSet}.
	 *
	 * <p>
	 * The resource is never written anywhere. It used to be {@code save(null)}d right
	 * after creation, and because its URI is a bare {@code <uuid>.xmi} — relative, with
	 * no base — that wrote a file into whatever directory the server happened to be
	 * started from, one per request, with the {@link java.io.IOException} printed to
	 * stderr and otherwise ignored. Nothing read those files.
	 * </p>
	 *
	 * <p>
	 * The object stays attached: the caller is expected to be done with the resource set
	 * when the request ends, which is when the per-request {@code ResourceSet} is
	 * released.
	 * </p>
	 *
	 * @param resourceSet the request's resource set; never {@code null}
	 * @param eObject     the object to attach; never {@code null}
	 */
	public static void attach(ResourceSet resourceSet, EObject eObject) {
		Resource resource = resourceSet.createResource(URI.createURI(UUID.randomUUID().toString().concat(".xmi")));
		resource.getContents().add(eObject);
	}

}
