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
package org.eclipse.fennec.model.atlas.scope.api;

/**
 * Service-property names of the {@code atlas.*} vocabulary shared across the Atlas
 * scope/registry services and the remote client.
 * <p>
 * This is the canonical definition. The remote client's
 * {@code org.eclipse.fennec.model.atlas.rest.client.api.AtlasProperties} mirrors these
 * names and values; the two must stay in sync (the client may later depend on this
 * bundle and drop its copy). Constant <em>names</em> and string <em>values</em> are part
 * of the public contract — never change a value, only add new ones.
 * <p>
 * The properties fall into two groups:
 * <ul>
 * <li><b>Registry identity</b> — {@link #ATLAS_SCOPE}, {@link #ATLAS_REGISTRY},
 * {@link #ATLAS_VIEW}, {@link #ATLAS_REMOTE}: stamped on a
 * {@link ReadOnlyScopeService} (and on EPackage publications) so consumers can find
 * and filter them.</li>
 * <li><b>Remote-publication provenance</b> — {@link #ATLAS_STAGE},
 * {@link #ATLAS_BASE_URI}: where a remotely fetched artifact came from; set by the
 * client when it publishes into a local framework, not by the read-only contract.</li>
 * </ul>
 */
public final class AtlasProperties {

	private AtlasProperties() {
		// constants holder
	}

	/**
	 * Boolean. Marks the artifact as fetched from a remote Atlas.
	 * Filter with {@code (atlas.remote=true)} / {@code (!(atlas.remote=true))}.
	 */
	public static final String ATLAS_REMOTE = "atlas.remote";

	/** String. Atlas scope the artifact came from, e.g. {@code jena}. */
	public static final String ATLAS_SCOPE = "atlas.scope";

	/** String. Stage on the server when fetched, e.g. {@code released}. */
	public static final String ATLAS_STAGE = "atlas.stage";

	/** String. Base URI of the originating Atlas instance. */
	public static final String ATLAS_BASE_URI = "atlas.base.uri";

	/**
	 * String. Registry name an EObject registry publication belongs to,
	 * e.g. {@code cocl}. Used by the read-only {@link ReadOnlyScopeService}
	 * services (Phase 4 / Phase 5).
	 */
	public static final String ATLAS_REGISTRY = "atlas.registry";

	/**
	 * String. Which stage view a registry publication exposes
	 * (default {@code released}). Used by the read-only
	 * {@link ReadOnlyScopeService} services (Phase 4 / Phase 5).
	 */
	public static final String ATLAS_VIEW = "atlas.view";
}
