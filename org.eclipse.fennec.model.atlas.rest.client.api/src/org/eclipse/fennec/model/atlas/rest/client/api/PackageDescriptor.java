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
package org.eclipse.fennec.model.atlas.rest.client.api;

/**
 * One entry of a scope's final-stage schema listing: a package's namespace URI together
 * with the origin metadata the listing already carries — the <em>owning</em> scope, the
 * stage it lives in and its version (resolved through scope inheritance server-side, so
 * for an inherited package the scope/stage are the parent's).
 * <p>
 * Unlike {@link ResolvedEPackage} this carries <b>no</b> {@code EPackage} content: it is the
 * cheap, metadata-only view returned by {@link RemoteEPackageProvider#listPackages(String)}
 * in a single listing call. A caller that needs to publish a package with accurate
 * {@code atlas.*} provenance can therefore read the stage/version from here and fetch the
 * content separately, without the extra per-package metadata round-trip that
 * {@link RemoteEPackageProvider#resolve(String)} performs.
 *
 * @param nsUri   the package namespace URI
 * @param scope   the owning Atlas scope reported by the listing metadata (may be {@code null})
 * @param stage   the stage the package lives in (may be {@code null})
 * @param version the model version, or {@code null} if the server did not report one
 */
public record PackageDescriptor(String nsUri, String scope, String stage, String version, String fingerprint) {

	/**
	 * Compatibility constructor for callers that do not know the fingerprint
	 * (e.g. nsURI-only listings); {@link #fingerprint()} is {@code null}.
	 */
	public PackageDescriptor(String nsUri, String scope, String stage, String version) {
		this(nsUri, scope, stage, version, null);
	}
}
