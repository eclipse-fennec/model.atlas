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

import java.util.Objects;

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.rest.client.impl.spi.ConcurrentPackageRegistry;
import org.eclipse.fennec.model.atlas.rest.client.api.DecodingRegistryProvider;

/**
 * The registry an object is decoded against when a {@link DecodingRegistryProvider}
 * supplies one for its scope and stage (issue #347): the packages this runtime ships
 * itself first (issue #330), then the supplied registry.
 * <p>
 * Holds nothing itself. Whatever it resolves comes from one of the two, so the decoded
 * object binds exactly the instances they hold, and keeping them fresh stays with their
 * owner.
 */
final class SharedDecodingRegistry extends ConcurrentPackageRegistry implements EPackage.Registry {


	private final EPackage.Registry local;
	private final EPackage.Registry shared;

	SharedDecodingRegistry(EPackage.Registry local, EPackage.Registry shared) {
		this.local = Objects.requireNonNull(local, "local");
		this.shared = Objects.requireNonNull(shared, "shared");
	}

	@Override
	public EPackage getEPackage(String nsURI) {
		EPackage ePackage = local.getEPackage(nsURI);
		// EMF asks for the null namespace while parsing; only a local package can live there.
		return ePackage != null || nsURI == null ? ePackage : shared.getEPackage(nsURI);
	}

	@Override
	public EFactory getEFactory(String nsURI) {
		EFactory eFactory = local.getEFactory(nsURI);
		return eFactory != null || nsURI == null ? eFactory : shared.getEFactory(nsURI);
	}
}
