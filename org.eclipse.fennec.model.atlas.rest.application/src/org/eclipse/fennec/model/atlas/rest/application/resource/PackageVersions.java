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
package org.eclipse.fennec.model.atlas.rest.application.resource;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.workflow.EPackageVersions;
import org.osgi.framework.Version;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Decides the version a schema package is stored under.
 *
 * <p>
 * A version is <em>declared</em> — by the caller, or by the model itself — and only
 * <em>inferred</em> from the namespace URI when nobody declared one, in decreasing order of
 * authority:
 * </p>
 * <ol>
 * <li>the {@code ?version=} parameter: what the caller asks for is what is used, never vetoed
 * against anything else;</li>
 * <li>the {@code Version} annotation on the {@link EPackage} ({@code source="Version"},
 * detail {@code value}) — the convention {@code emf.osgi}'s generator writes and reads, so a
 * model that states its own version is taken at its word;</li>
 * <li>the last segment of the nsURI, and only when it actually looks like a version.</li>
 * </ol>
 *
 * <p>
 * That last rule is narrow on purpose (issue #180). {@code Version.parseVersion} accepts a
 * bare number, so parsing every segment of every nsURI read {@code 2002} out of
 * {@code http://www.eclipse.org/emf/2002/Ecore}, {@code 20131001} out of the UML spec URI,
 * and — because the authority used to be parsed last and to overwrite the segments — the
 * final octet of {@code http://10.2.3.4/model/5.0}. Packages were stored under those numbers,
 * and a caller stating the model's real version was rejected for being "incompatible" with
 * them. A year is not a version, a host is not a version, and neither is a bare major.
 * </p>
 *
 * <p>
 * A declared version is passed through verbatim — it is someone's statement, not ours to
 * rewrite — while an inferred one is rendered from the parsed {@link Version}.
 * </p>
 *
 * <p>
 * The rule itself is {@link EPackageVersions}, shared with the bootstrap loader and the
 * atlas schema registry (issue #359); this class adds the {@code ?version=} parameter and
 * answers an invalid declaration with a bad request.
 * </p>
 *
 * <p>
 * This knows nothing about HTTP beyond how it reports a bad request, so it can be unit-tested
 * without a running container.
 * </p>
 *
 * @see <a href="https://github.com/eclipse-fennec/model.atlas/issues/180">issue #180</a>
 */
final class PackageVersions {

	private PackageVersions() {
	}

	/**
	 * The version to store the package under.
	 *
	 * @param ePackage     the package being uploaded, consulted for its declared version; may
	 *                     be {@code null}
	 * @param versionParam the {@code ?version=} parameter; may be {@code null} or blank
	 * @param nsUri        the package's namespace URI
	 * @return the resolved version, or {@code null} when no version was declared and none can
	 *         be read from the nsURI
	 * @throws WebApplicationException {@code 400} if the parameter, or the version the package
	 *                                 declares, cannot be parsed
	 */
	static String resolve(EPackage ePackage, String versionParam, String nsUri) {
		if (versionParam != null && !versionParam.isBlank()) {
			String requested = versionParam.trim();
			parseOrReject(requested, String.format("Invalid version parameter: '%s'", versionParam));
			return requested;
		}

		String declared = declaredVersion(ePackage);
		if (declared != null) {
			return declared;
		}

		Version inferred = fromNsUri(nsUri);
		return inferred == null ? null : inferred.toString();
	}

	/**
	 * The version the model declares through its {@code Version} annotation.
	 *
	 * @param ePackage the package to inspect; may be {@code null}
	 * @return the declared version verbatim, or {@code null} when the package declares none
	 * @throws WebApplicationException {@code 400} if what it declares is not a version — a
	 *                                 declaration that cannot be honoured is reported rather
	 *                                 than quietly ignored
	 */
	static String declaredVersion(EPackage ePackage) {
		String declared = EPackageVersions.declared(ePackage);
		if (declared == null) {
			return null;
		}
		parseOrReject(declared, String.format(
				"The package declares the version '%s' in its Version annotation, which is not a valid version",
				declared));
		return declared;
	}

	/**
	 * The version in the last segment of {@code nsUri}, when that segment looks like one.
	 *
	 * @param nsUri the namespace URI to read; may be {@code null} or blank
	 * @return the version, or {@code null} when the last segment is not version-shaped
	 */
	static Version fromNsUri(String nsUri) {
		return EPackageVersions.fromNsUri(nsUri);
	}

	private static void parseOrReject(String version, String message) {
		try {
			Version.parseVersion(version);
		} catch (IllegalArgumentException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(message).build());
		}
	}
}
