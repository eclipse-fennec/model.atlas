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

import org.eclipse.emf.common.util.URI;
import org.osgi.framework.Version;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Reads OSGi versions out of EPackage namespace URIs and reconciles them with an
 * explicitly requested version.
 *
 * <p>
 * This is the versioning half of what {@code SchemaPackagesResource} used to do
 * inline. It knows nothing about HTTP beyond how it reports a bad request, so it
 * can be unit-tested without a running container.
 * </p>
 */
final class NsUriVersions {

	private NsUriVersions() {
	}

	/**
	 * Extracts an OSGi version from a URI by parsing each segment. Returns the last
	 * valid version found in the URI segments.
	 *
	 * @param nsUri the namespace URI to parse
	 * @return the extracted Version, or null if no valid version found
	 */
	static Version extractVersion(String nsUri) {
		if (nsUri == null || nsUri.isBlank()) {
			return null;
		}

		try {
			URI uri = URI.createURI(nsUri);
			Version lastValidVersion = null;

			// Parse each segment of the URI
			for (String segment : uri.segments()) {
				try {
					Version version = Version.parseVersion(segment);
					// Keep track of the last valid version found
					lastValidVersion = version;
				} catch (IllegalArgumentException e) {
					// This segment is not a valid version, continue
				}
			}

			// Also try parsing the authority and path components
			if (uri.hasAuthority()) {
				String authority = uri.authority();
				if (authority != null) {
					String[] parts = authority.split("[/.]");
					for (String part : parts) {
						try {
							Version version = Version.parseVersion(part);
							lastValidVersion = version;
						} catch (IllegalArgumentException e) {
							// Not a version, continue
						}
					}
				}
			}

			return lastValidVersion;
		} catch (Exception e) {
			// If URI parsing fails, return null
			return null;
		}
	}

	/**
	 * Validates that two versions are compatible according to semantic versioning
	 * rules. Compatible means they have the same major version, and the URI version
	 * is not lower than the parameter version.
	 *
	 * @param paramVersion the version from the parameter
	 * @param uriVersion   the version extracted from the URI
	 * @return true if compatible, false otherwise
	 */
	static boolean areCompatible(Version paramVersion, Version uriVersion) {
		if (paramVersion == null || uriVersion == null) {
			return false;
		}

		// Major versions must match for semantic compatibility
		if (paramVersion.getMajor() != uriVersion.getMajor()) {
			return false;
		}

		// Compare minor and micro versions
		int minorCompare = Integer.compare(uriVersion.getMinor(), paramVersion.getMinor());
		if (minorCompare < 0) {
			return false; // URI version has lower minor
		}
		if (minorCompare > 0) {
			return true; // URI version has higher minor, compatible
		}

		// Minor versions are equal, check micro
		int microCompare = Integer.compare(uriVersion.getMicro(), paramVersion.getMicro());
		return microCompare >= 0; // URI version micro must be >= param version micro
	}

	/**
	 * Resolves and validates the version parameter. If no version parameter is
	 * given, extracts version from the URI. If both are present, validates
	 * compatibility.
	 *
	 * @param versionParam the version parameter (may be null)
	 * @param nsUri        the namespace URI
	 * @return the resolved version string, or null if no version found
	 * @throws WebApplicationException 400 if the parameter is unparseable or
	 *                                 incompatible with the version in the URI
	 */
	static String resolveAndValidate(String versionParam, String nsUri) {
		Version uriVersion = extractVersion(nsUri);

		if (versionParam == null || versionParam.isBlank()) {
			// No parameter provided, use the version from URI if available
			return uriVersion != null ? uriVersion.toString() : null;
		}

		// Parameter provided, validate it
		Version paramVersion;
		try {
			paramVersion = Version.parseVersion(versionParam);
		} catch (IllegalArgumentException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
					.entity(String.format("Invalid version parameter: '%s'", versionParam)).build());
		}

		// If URI has a version, check compatibility
		if (uriVersion != null && !areCompatible(paramVersion, uriVersion)) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(String.format(
					"Version parameter '%s' is not compatible with URI version '%s' (semantic versioning rules)",
					versionParam, uriVersion.toString())).build());
		}

		return versionParam;
	}
}
