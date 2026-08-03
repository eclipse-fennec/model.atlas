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

import java.util.Objects;

import org.eclipse.emf.ecore.EPackage;
import org.osgi.annotation.versioning.ProviderType;

/**
 * A remotely resolved EPackage together with its authoritative origin on the
 * Atlas, as reported by the server's metadata.
 * <p>
 * Unlike {@link RemoteEPackageProvider#getEPackage(String)}, which returns just the
 * package and leaves the caller to guess where it came from,
 * {@link RemoteEPackageProvider#resolve(String)} reads the package's
 * {@code ObjectMetadata} first, so {@link #getScope()}, {@link #getRegistry()},
 * {@link #getStage()} and {@link #getVersion()} are the values the server actually
 * holds — resolved through scope inheritance, so the scope is the <em>owning</em>
 * scope (which may be a parent of the queried one), not the queried scope. The OSGi
 * front-end (Phase 3) uses this to stamp accurate {@code atlas.*} service properties
 * on lazily published packages instead of approximating them.
 */
@ProviderType
public final class ResolvedEPackage {

	private final EPackage ePackage;
	private final String nsUri;
	private final String scope;
	private final String registry;
	private final String stage;
	private final String version;
	private final String fingerprint;

	/**
	 * @param ePackage the resolved package (never {@code null})
	 * @param nsUri    its namespace URI
	 * @param scope    the owning Atlas scope reported by the metadata
	 * @param registry the registry the package lives in (e.g. {@code schema})
	 * @param stage    the stage the metadata was resolved from
	 * @param version  the model version, or {@code null} if the server did not report one
	 */
	public ResolvedEPackage(EPackage ePackage, String nsUri, String scope, String registry, String stage,
			String version) {
		this(ePackage, nsUri, scope, registry, stage, version, null);
	}

	/**
	 * @param ePackage    the resolved package (never {@code null})
	 * @param nsUri       its namespace URI
	 * @param scope       the owning Atlas scope reported by the metadata
	 * @param registry    the registry the package lives in (e.g. {@code schema})
	 * @param stage       the stage the metadata was resolved from
	 * @param version     the model version, or {@code null} if the server did not report one
	 * @param fingerprint the server-reported content-derived model fingerprint
	 *                    (scheme-prefixed, e.g. {@code fp1:<digest>}), or {@code null}
	 *                    if the server did not report one. Advisory: consumers that
	 *                    need a trustworthy value compute it locally from
	 *                    {@link #getEPackage()} and may use this one as a cross-check.
	 */
	public ResolvedEPackage(EPackage ePackage, String nsUri, String scope, String registry, String stage,
			String version, String fingerprint) {
		this.ePackage = Objects.requireNonNull(ePackage, "ePackage");
		this.nsUri = nsUri;
		this.scope = scope;
		this.registry = registry;
		this.stage = stage;
		this.version = version;
		this.fingerprint = fingerprint;
	}

	/** The resolved package. */
	public EPackage getEPackage() {
		return ePackage;
	}

	/** The package namespace URI. */
	public String getNsUri() {
		return nsUri;
	}

	/** The owning Atlas scope (resolved through inheritance; may be a parent scope). */
	public String getScope() {
		return scope;
	}

	/** The registry the package lives in (e.g. {@code schema}). */
	public String getRegistry() {
		return registry;
	}

	/** The stage the metadata was resolved from. */
	public String getStage() {
		return stage;
	}

	/** The model version, or {@code null} if the server did not report one. */
	public String getVersion() {
		return version;
	}

	/**
	 * The server-reported model fingerprint (e.g. {@code fp1:<digest>}), or
	 * {@code null} if the server did not report one. Advisory — see the
	 * constructor note.
	 */
	public String getFingerprint() {
		return fingerprint;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ResolvedEPackage)) {
			return false;
		}
		ResolvedEPackage that = (ResolvedEPackage) o;
		return ePackage == that.ePackage && Objects.equals(nsUri, that.nsUri) && Objects.equals(scope, that.scope)
				&& Objects.equals(registry, that.registry) && Objects.equals(stage, that.stage)
				&& Objects.equals(version, that.version) && Objects.equals(fingerprint, that.fingerprint);
	}

	@Override
	public int hashCode() {
		return Objects.hash(System.identityHashCode(ePackage), nsUri, scope, registry, stage, version, fingerprint);
	}

	@Override
	public String toString() {
		return "ResolvedEPackage[nsUri=" + nsUri + ", scope=" + scope + ", registry=" + registry + ", stage=" + stage
				+ ", version=" + version + ", fingerprint=" + fingerprint + "]";
	}
}
