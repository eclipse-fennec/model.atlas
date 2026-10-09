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
package org.eclipse.fennec.model.atlas.mgmt.storage;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.fingerprint.util.FingerprintHelper;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.osgi.framework.Version;

/**
 * What can be read off a schema package itself, for the metadata it is recorded under.
 * <p>
 * The version it declares, the hash of its bytes and the fingerprint of its shape are all
 * properties of the package and of nothing else, so a package registered at boot and the same
 * package uploaded over REST have to come out with the same three. They did not: a registry that
 * serves the packages of the running framework had no storage service to pass them through, and
 * the fields were simply never filled - so a client could see a version, a change and an identity
 * for every schema of a tenant scope and none of them for a system schema (issue #359).
 *
 * @see <a href="https://github.com/eclipse-fennec/model.atlas/issues/359">issue #359</a>
 */
public final class PackageMetadata {

	private static final Logger LOGGER = Logger.getLogger(PackageMetadata.class.getName());

	/** Annotation source a model declares its version under; {@code emf.osgi}'s convention. */
	private static final String VERSION_ANNOTATION_SOURCE = "Version";

	/** Detail key holding the declared version, e.g. {@code <details key="value" value="1.2.0"/>}. */
	private static final String VERSION_ANNOTATION_DETAIL = "value";

	/**
	 * What a URI segment has to look like before it is read as a version:
	 * {@code major.minor}, optionally {@code .micro} and an OSGi qualifier. Being
	 * <em>parseable</em> is not enough - a bare number parses, which is how a year was once read
	 * out of {@code http://www.eclipse.org/emf/2002/Ecore} (issue #180).
	 */
	private static final Pattern VERSION_SHAPED = Pattern.compile("\\d+\\.\\d+(\\.\\d+(\\.[\\p{Alnum}_-]+)?)?");

	private PackageMetadata() {
	}

	/**
	 * Writes onto the metadata what the package says about itself: its version, the hash of its
	 * serialised form and its fingerprint.
	 * <p>
	 * The timestamps are not among them. When a schema was first seen and when it last changed are
	 * facts about this registry's history with it, not about the package, and only a caller that
	 * can see what was recorded before knows them.
	 *
	 * @param metadata the metadata to fill, never {@code null}
	 * @param ePackage the package it is about, never {@code null}
	 */
	public static void describe(ObjectMetadata metadata, EPackage ePackage) {
		metadata.setVersion(version(ePackage));
		metadata.setContentHash(AbstractEObjectStorageService.computeContentHash(ePackage));
		metadata.setFingerprint(FingerprintHelper.fingerprint(ePackage));
	}

	/**
	 * The version a package states: the one it declares in its {@code Version} annotation, or else
	 * the last segment of its nsURI when that segment looks like a version.
	 * <p>
	 * The same rule the REST upload applies, and lenient in the same way the bootstrap loader is:
	 * a declaration that is not a version is logged and ignored rather than raised. Nothing here
	 * has a caller to report a bad request to - the package is already in the running framework,
	 * and refusing to describe it would hide it rather than correct it.
	 *
	 * @param ePackage the package, never {@code null}
	 * @return the version, or {@code null} when it declares none and its nsURI carries none
	 */
	public static String version(EPackage ePackage) {
		String declared = declaredVersion(ePackage);
		if (declared != null) {
			return declared;
		}
		if (ePackage.getNsURI() == null) {
			return null;
		}
		String[] segments = URI.createURI(ePackage.getNsURI()).segments();
		if (segments.length == 0) {
			return null;
		}
		String last = segments[segments.length - 1];
		if (last == null || !VERSION_SHAPED.matcher(last).matches()) {
			return null;
		}
		try {
			return Version.parseVersion(last).toString();
		} catch (IllegalArgumentException notAVersion) {
			return null;
		}
	}

	/**
	 * The version the model states in its {@code Version} annotation, or {@code null} when it
	 * states none - or states something that is not a version, which is logged and ignored.
	 */
	private static String declaredVersion(EPackage ePackage) {
		EAnnotation annotation = ePackage.getEAnnotation(VERSION_ANNOTATION_SOURCE);
		if (annotation == null) {
			return null;
		}
		String declared = annotation.getDetails().get(VERSION_ANNOTATION_DETAIL);
		if (declared == null || declared.isBlank()) {
			return null;
		}
		String trimmed = declared.trim();
		try {
			Version.parseVersion(trimmed);
			return trimmed;
		} catch (IllegalArgumentException notAVersion) {
			LOGGER.log(Level.WARNING, () -> ePackage.getNsURI() + " declares the version '" + declared
					+ "' in its Version annotation, which is not a valid version - ignoring it");
			return null;
		}
	}
}
