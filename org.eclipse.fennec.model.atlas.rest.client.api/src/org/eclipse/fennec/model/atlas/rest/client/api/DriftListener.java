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

import org.eclipse.emf.ecore.EPackage;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Callback for drift events — fired when the client observes that an artifact
 * it had cached has changed or disappeared on the server, whether via a
 * per-request conditional GET or the background scope watcher.
 * <p>
 * In OSGi (Phase 3) the registrar additionally re-publishes the affected
 * service after notifying listeners.
 *
 * @see ModelAtlasClient#addDriftListener(DriftListener)
 */
@ConsumerType
public interface DriftListener {

	/**
	 * An EPackage changed on the server and was re-fetched.
	 *
	 * @param nsUri      the affected nsURI
	 * @param newPackage the freshly fetched replacement package
	 */
	void onPackageChanged(String nsUri, EPackage newPackage);

	/**
	 * An EPackage is no longer available on the server; its cache entry was
	 * dropped.
	 *
	 * @param nsUri the removed nsURI
	 */
	void onPackageRemoved(String nsUri);

	/**
	 * An EObject changed on the server; its cache entry was evicted/refreshed (the
	 * next read reflects the change). Default no-op so EPackage-only listeners need
	 * not implement it.
	 *
	 * @param scope    the scope the object lives in
	 * @param registry the registry within the scope
	 * @param objectId the object identifier
	 */
	default void onObjectChanged(String scope, String registry, String objectId) {
		// no-op by default
	}

	/**
	 * An EObject is no longer available on the server; its cache entry was dropped.
	 * Default no-op so EPackage-only listeners need not implement it.
	 *
	 * @param scope    the scope the object lived in
	 * @param registry the registry within the scope
	 * @param objectId the object identifier
	 */
	default void onObjectRemoved(String scope, String registry, String objectId) {
		// no-op by default
	}
}
