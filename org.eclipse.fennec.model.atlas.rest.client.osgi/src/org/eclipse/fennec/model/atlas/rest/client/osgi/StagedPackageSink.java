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
 * Told when a stage-located bridge starts and stops holding a package (#277).
 * <p>
 * The bridge is a map plus a fetch; this keeps what is done with the packages it holds out of it,
 * and keeps the coupling to the metadata layer in one class that a deployment without that layer
 * never loads. {@link #NONE} is exactly the behaviour before this hook existed.
 * <p>
 * The two calls are paired: exactly one {@link #registered} per package the bridge starts holding,
 * exactly one {@link #evicted} when it stops. That pairing is what makes a refcounted consumer such
 * as the {@code MetadataWhiteboard} safe to drive from here.
 *
 * @author Data In Motion
 */
interface StagedPackageSink {

	/** Holds nothing, records nothing — a deployment without the metadata layer. */
	StagedPackageSink NONE = new StagedPackageSink() {

		@Override
		public void registered(EPackage ePackage) {
			// nothing to record
		}

		@Override
		public void evicted(EPackage ePackage) {
			// nothing to forget
		}
	};

	/**
	 * The bridge has taken this package into its cache — once, for this entry.
	 *
	 * @param ePackage the package now held
	 */
	void registered(EPackage ePackage);

	/**
	 * The bridge has dropped this package from its cache.
	 *
	 * @param ePackage the package that was held, never {@code null}
	 */
	void evicted(EPackage ePackage);
}
