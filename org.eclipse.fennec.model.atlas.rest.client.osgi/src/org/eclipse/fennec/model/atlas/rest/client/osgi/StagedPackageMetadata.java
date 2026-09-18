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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.fennec.emf.osgi.metadata.MetadataService;
import org.eclipse.fennec.emf.osgi.model.metadata.PackageMetadata;
import org.eclipse.fennec.model.atlas.scope.api.AtlasProperties;

/**
 * Finds the metadata tree for one nsURI <em>at one stage</em> (#277).
 * <p>
 * Registering staged packages is only half the job: a consumer must be able to ask for the version
 * at a stage. {@code MetadataService.getPackageMetadata(nsURI)} cannot answer that — an nsURI does
 * not identify a version, so it answers best-effort with the most recently registered one — and it
 * is the wrong place to teach about stages: it is a generic EMF API with no notion of them, and
 * {@code getPackageMetadataVersions} deliberately "exposes facts, not policy". Selecting among the
 * facts is the caller's job, which is what this does.
 * <p>
 * <b>Not a durable index.</b> The Atlas coordinates live in {@code PackageMetadata.getProperties()},
 * documented as transient, runtime-only build context that is neither serialized nor replicated.
 * This lookup is therefore valid in this runtime and does not survive a {@code getRegistry()} /
 * {@code loadRegistry()} round trip.
 *
 * @author Data In Motion
 */
public final class StagedPackageMetadata {

	private StagedPackageMetadata() {
	}

	/**
	 * The metadata for the version of {@code nsURI} registered from {@code scope}/{@code stage}.
	 * Where several match — the same location registered more than one version over time — the most
	 * recently registered wins, since {@code getPackageMetadataVersions} returns oldest first.
	 *
	 * @param metadata the service to query
	 * @param nsURI    the namespace URI
	 * @param scope    the Atlas scope, or {@code null} to match any
	 * @param stage    the Atlas stage, or {@code null} to match any
	 * @return the matching tree, or empty when that location registered none
	 */
	public static Optional<PackageMetadata> atStage(MetadataService metadata, String nsURI, String scope,
			String stage) {
		if (metadata == null || nsURI == null) {
			return Optional.empty();
		}
		List<PackageMetadata> versions = metadata.getPackageMetadataVersions(nsURI);
		PackageMetadata match = null;
		for (PackageMetadata candidate : versions) {
			if (matches(candidate, AtlasProperties.ATLAS_SCOPE, scope)
					&& matches(candidate, AtlasProperties.ATLAS_STAGE, stage)) {
				match = candidate; // oldest first, so the last match is the newest
			}
		}
		return Optional.ofNullable(match);
	}

	private static boolean matches(PackageMetadata candidate, String property, String expected) {
		if (expected == null) {
			return true;
		}
		Object actual = candidate.getProperties() == null ? null : candidate.getProperties().get(property);
		return actual != null && Objects.equals(expected, actual.toString());
	}
}
