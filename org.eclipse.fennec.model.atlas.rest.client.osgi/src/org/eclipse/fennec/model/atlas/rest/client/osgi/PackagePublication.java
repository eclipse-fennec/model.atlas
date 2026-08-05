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

import org.eclipse.emf.ecore.EPackage;

/**
 * The publish seam shared by the resolution-mode triggers (EAGER, LAZY, HYBRID).
 * <p>
 * Its single method matches {@link RemoteEPackagePublisher#publish}, so the
 * component wires it as {@code publisher::publish}. Triggers depend on this
 * interface rather than on the publisher (and thus on {@code BundleContext}), which
 * keeps them unit-testable with a plain recorder.
 */
@FunctionalInterface
interface PackagePublication {

	/**
	 * Publish the EPackage as the configurator/EPackage/EFactory trio.
	 *
	 * @param ePackage          the fetched package
	 * @param scope             the Atlas scope it came from (origin marker)
	 * @param stage             the stage it was fetched from
	 * @param version           the model version, or {@code null} to stamp the default
	 * @param serverFingerprint the server-reported model fingerprint, or {@code null} when
	 *                          unknown — used only as a cross-check against the locally
	 *                          computed {@code emf.fingerprint} property, never adopted
	 * @return {@code true} if it was newly published (idempotent per nsURI)
	 */
	boolean publish(EPackage ePackage, String scope, String stage, String version, String serverFingerprint);

	/** Publish without a server fingerprint cross-check. */
	default boolean publish(EPackage ePackage, String scope, String stage, String version) {
		return publish(ePackage, scope, stage, version, null);
	}
}
