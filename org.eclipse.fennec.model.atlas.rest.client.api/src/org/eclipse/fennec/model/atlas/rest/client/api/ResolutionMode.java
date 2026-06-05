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
 * When EPackages are fetched from the Atlas. Maps to the {@code mode}
 * configuration property.
 */
public enum ResolutionMode {

	/**
	 * Pre-fetch the configured scopes/stages at start-up so the local
	 * framework mirrors the Atlas immediately. Drift detection keeps it fresh.
	 */
	EAGER,

	/**
	 * Default. Nothing is fetched up front; the first demand for an nsURI
	 * triggers its fetch.
	 */
	LAZY,

	/**
	 * EAGER-load the {@code eager.nsuri.allow.list}; everything else is LAZY.
	 */
	HYBRID
}
