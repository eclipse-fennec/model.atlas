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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.metadata.MetadataWhiteboard;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.eclipse.fennec.model.atlas.scope.api.AtlasProperties;

/**
 * Registers the packages a stage-located bridge holds with the {@link MetadataWhiteboard} (#277).
 * <p>
 * A package that exists only at a non-final stage reaches the runtime through the fetch-on-miss
 * bridge and nowhere else: it is resolvable by asking the right registry service for its nsURI, and
 * invisible to {@code MetadataService} — no metadata tree, no aspects, and
 * {@code getPackageMetadataByFingerprint} never finds it.
 * <p>
 * The whiteboard is the right target where a second {@code EPackage} service would not be. It keys
 * by <b>fingerprint</b>, so two stage variants of one nsURI become two coexisting trees rather than
 * bind-order ambiguity, and unregistration is a per-version refcount, so dropping the draft tree
 * never collaterally removes the released one.
 * <p>
 * The Atlas coordinates ride along as registration properties and reach consumers through
 * {@code PackageMetadata.getProperties()}. They are transient build context by design — not
 * serialized, not replicated — so they are good for asking "which stage is this tree from" in this
 * runtime, and not a durable index.
 *
 * @author Data In Motion
 */
class MetadataWhiteboardSink implements StagedPackageSink {

	/**
	 * Publishes the sink factory whenever a {@link MetadataWhiteboard} is around. A separate
	 * component on purpose: it is the only class here that names a metadata type, so a deployment
	 * without that layer simply never activates it and everything else is untouched.
	 */
	@Component(service = StagedPackageSinkFactory.class)
	public static class Factory implements StagedPackageSinkFactory {

		@Reference
		MetadataWhiteboard whiteboard;

		@Override
		public StagedPackageSink sinkFor(String scope, String stage) {
			return new MetadataWhiteboardSink(whiteboard, scope, stage);
		}
	}


	private static final Logger LOGGER = Logger.getLogger(MetadataWhiteboardSink.class.getName());

	private final MetadataWhiteboard whiteboard;
	private final String scope;
	private final String stage;

	MetadataWhiteboardSink(MetadataWhiteboard whiteboard, String scope, String stage) {
		this.whiteboard = Objects.requireNonNull(whiteboard, "whiteboard");
		this.scope = scope;
		this.stage = stage;
	}

	@Override
	public void registered(EPackage ePackage) {
		Map<String, Object> properties = new HashMap<>();
		properties.put(AtlasProperties.ATLAS_REMOTE, Boolean.TRUE);
		if (scope != null) {
			properties.put(AtlasProperties.ATLAS_SCOPE, scope);
		}
		if (stage != null) {
			properties.put(AtlasProperties.ATLAS_STAGE, stage);
		}
		try {
			// The whiteboard computes the fingerprint key itself; we supply only context.
			whiteboard.registerPackage(ePackage, properties);
		} catch (RuntimeException e) {
			// Metadata is an observer of resolution, never a gate on it: a package that resolved
			// must stay usable even if describing it failed.
			LOGGER.log(Level.WARNING, e,
					() -> "Registering " + ePackage.getNsURI() + " with the metadata whiteboard failed");
		}
	}

	@Override
	public void evicted(EPackage ePackage) {
		try {
			whiteboard.unregisterPackage(ePackage);
		} catch (RuntimeException e) {
			LOGGER.log(Level.WARNING, e,
					() -> "Unregistering " + ePackage.getNsURI() + " from the metadata whiteboard failed");
		}
	}
}
