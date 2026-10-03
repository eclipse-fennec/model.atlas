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
package org.eclipse.fennec.model.atlas.rest.client.osgi;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;

/**
 * The framework {@code EPackage.Registry} narrowed to the packages this runtime ships
 * itself (issue #330) — what the client hands its core as the local package registry.
 * <p>
 * The framework registry also holds what this client published from the Atlas, so used
 * as-is it would answer a fetch with whichever Atlas version happens to be published: a
 * package pinned to {@code draft} would bind the released version of a dependency. So a
 * namespace counts as local by the same rule the {@link LocalFirstPublicationGate} applies
 * — a local bundle declares it, or a non-{@code atlas.remote} service provides it — and
 * the instance the framework holds for it must not be the one we published. Under
 * {@code force.remote} nothing is local, consistent with the gate publishing regardless.
 * <p>
 * A package a bundle declares but has not yet realised (#254) has no instance yet; it is
 * answered as absent, and the caller falls back to the Atlas as before.
 */
final class LocallyShippedPackages extends EPackageRegistryImpl {

	private static final long serialVersionUID = 1L;

	private final transient EPackage.Registry framework;
	private final transient Predicate<String> shippedLocally;
	private final transient Function<String, EPackage> publishedByUs;

	/**
	 * @param framework      the framework registry
	 * @param shippedLocally whether a local bundle or service provides an nsURI
	 * @param publishedByUs  the package this client published for an nsURI, or {@code null}
	 */
	LocallyShippedPackages(EPackage.Registry framework, Predicate<String> shippedLocally,
			Function<String, EPackage> publishedByUs) {
		this.framework = Objects.requireNonNull(framework, "framework");
		this.shippedLocally = Objects.requireNonNull(shippedLocally, "shippedLocally");
		this.publishedByUs = Objects.requireNonNull(publishedByUs, "publishedByUs");
	}

	@Override
	public EPackage getEPackage(String nsURI) {
		if (nsURI == null || !shippedLocally.test(nsURI)) {
			return null;
		}
		EPackage ePackage = framework.getEPackage(nsURI);
		return ePackage == null || ePackage == publishedByUs.apply(nsURI) ? null : ePackage;
	}

	@Override
	public EFactory getEFactory(String nsURI) {
		EPackage ePackage = getEPackage(nsURI);
		return ePackage == null ? null : ePackage.getEFactoryInstance();
	}
}
