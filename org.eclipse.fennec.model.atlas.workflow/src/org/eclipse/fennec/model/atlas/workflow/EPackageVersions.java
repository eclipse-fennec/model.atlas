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
package org.eclipse.fennec.model.atlas.workflow;

import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EPackage;
import org.osgi.framework.Version;

/**
 * Where the version of a schema package comes from, shared by every path that registers one:
 * the upload endpoint, the bootstrap loader and the atlas schema registry (issue #359).
 * <p>
 * A model <em>declares</em> its version through a {@code Version} annotation ({@code source="Version"},
 * detail {@code value}) — the convention {@code emf.osgi}'s generator writes. Only when it declares
 * none is the version <em>inferred</em> from the last segment of its nsURI, and only when that
 * segment looks like one (issue #180): a bare number parses as an OSGi version, which read
 * {@code 2002} out of {@code http://www.eclipse.org/emf/2002/Ecore}. A year is not a version.
 * <p>
 * What an invalid declaration means is the caller's decision, not this class's: the upload
 * endpoint answers it with a bad request, while a loader must not abort start-up over a junk
 * annotation. So {@link #declared(EPackage)} returns the declaration as stated, and
 * {@link #of(EPackage, Consumer)} reports an invalid one and falls through to the nsURI.
 */
public final class EPackageVersions {

	/** Annotation source a model declares its version under; {@code emf.osgi}'s convention. */
	public static final String VERSION_ANNOTATION_SOURCE = "Version";

	/** Detail key holding the declared version, e.g. {@code <details key="value" value="1.2.0"/>}. */
	public static final String VERSION_ANNOTATION_DETAIL = "value";

	/**
	 * What a URI segment has to look like before it is read as a version: {@code major.minor},
	 * optionally {@code .micro} and an OSGi qualifier.
	 */
	private static final Pattern VERSION_SHAPED = Pattern.compile("\\d+\\.\\d+(\\.\\d+(\\.[\\p{Alnum}_-]+)?)?");

	private EPackageVersions() {
	}

	/**
	 * The version the model declares through its {@code Version} annotation, trimmed but not
	 * validated.
	 *
	 * @param ePackage the package to inspect; may be {@code null}
	 * @return the declaration, or {@code null} when the package declares none
	 */
	public static String declared(EPackage ePackage) {
		if (ePackage == null) {
			return null;
		}
		EAnnotation annotation = ePackage.getEAnnotation(VERSION_ANNOTATION_SOURCE);
		if (annotation == null) {
			return null;
		}
		String declared = annotation.getDetails().get(VERSION_ANNOTATION_DETAIL);
		return declared == null || declared.isBlank() ? null : declared.trim();
	}

	/**
	 * Whether {@code version} parses as an OSGi version.
	 *
	 * @param version the candidate; may be {@code null}
	 * @return {@code true} if it is a version
	 */
	public static boolean isVersion(String version) {
		if (version == null) {
			return false;
		}
		try {
			Version.parseVersion(version);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * The version in the last segment of {@code nsUri}, when that segment looks like one.
	 *
	 * @param nsUri the namespace URI to read; may be {@code null} or blank
	 * @return the version, or {@code null} when the last segment is not version-shaped
	 */
	public static Version fromNsUri(String nsUri) {
		if (nsUri == null || nsUri.isBlank()) {
			return null;
		}
		try {
			String[] segments = URI.createURI(nsUri).segments();
			if (segments.length == 0) {
				return null;
			}
			String last = segments[segments.length - 1];
			if (last == null || !VERSION_SHAPED.matcher(last).matches()) {
				return null;
			}
			return Version.parseVersion(last);
		} catch (RuntimeException e) {
			// An unparseable URI carries no version; the nsURI itself is validated elsewhere.
			return null;
		}
	}

	/**
	 * The version of {@code ePackage}: its valid declaration verbatim, else the one its nsURI
	 * carries.
	 *
	 * @param ePackage           the package; may be {@code null}
	 * @param invalidDeclaration told about a declaration that is not a version, which is then
	 *                           ignored
	 * @return the version, or {@code null} when the model declares none and its nsURI carries
	 *         none
	 */
	public static String of(EPackage ePackage, Consumer<String> invalidDeclaration) {
		if (ePackage == null) {
			return null;
		}
		String declared = declared(ePackage);
		if (declared != null) {
			if (isVersion(declared)) {
				return declared;
			}
			invalidDeclaration.accept(declared);
		}
		Version inferred = fromNsUri(ePackage.getNsURI());
		return inferred == null ? null : inferred.toString();
	}
}
