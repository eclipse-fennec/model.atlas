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
package org.eclipse.fennec.model.atlas.eobject.provider;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryEntry;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryWriter;
import org.eclipse.fennec.model.atlas.scope.api.AtlasProperties;
import org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;

/**
 * Syncs the objects of a Model Atlas scope into a named EObject registry - the atlas
 * <em>writer client</em> of emf.osgi's {@code eobject.registry}.
 * <p>
 * All atlas I/O runs on a private single-thread executor; construction never blocks on
 * the network. Scheduling: initial sync, retried every {@code retryIntervalMs} until it
 * completes, then re-synced every {@code refreshIntervalMs}. The registry's writer owns
 * the swap semantics (identity compare, update-before-remove, per-source removal); this
 * engine owns <em>what</em> it pushes and <em>when</em>:
 * <ul>
 * <li>a <b>complete</b> pass over one atlas registry results in one
 * {@link EObjectRegistryWriter#sync(String, java.util.Collection) writer.sync} scoped by
 * the source tag {@code <providerName>:<atlas-registry>} - the per-atlas-registry scope
 * is load-bearing: a transient failure of one atlas registry never touches the entries
 * of another;</li>
 * <li>a <b>partial</b> pass (listing failed, or individual objects failed to fetch)
 * pushes the successfully fetched objects via granular
 * {@link EObjectRegistryWriter#put(String, String, EObject, Map) writer.put} and removes
 * <b>nothing</b> - an object that merely failed to fetch keeps its current entry;</li>
 * <li>closing this engine leaves its content in the registry - a dying source must not
 * cost the locally held state.</li>
 * </ul>
 * Objects definitively gone from the atlas are removed by the next complete pass's
 * {@code sync}. Unchanged objects are no-ops end to end: the atlas client's ETag cache
 * returns the identical instance, which the writer's identity compare short-circuits.
 *
 * @since 08/2026
 */
public final class AtlasObjectSync implements AutoCloseable {

	/** Entry property holding the atlas object id an entry was fetched as. */
	public static final String PROP_OBJECT_ID = "atlas.object.id";
	/** Entry property holding the nsURI of the entry object's EPackage. */
	public static final String PROP_NS_URI = "emf.nsURI";

	private static final Logger logger = Logger.getLogger(AtlasObjectSync.class.getName());

	private final ReadableScopeService<EObject> scopeService;
	private final AtlasSyncSettings settings;
	private final BiFunction<String, EObject, String> keyFunction;
	private final EObjectRegistryWriter writer;
	private final ScheduledExecutorService executor;
	/** Only touched on the executor thread. */
	private final Map<String, EPackage> resolvedPackages = new HashMap<>();
	private volatile boolean active = true;

	/**
	 * Creates the engine and schedules the initial sync on a private executor.
	 *
	 * @param scopeService the atlas scope to read from; must not be {@code null}
	 * @param settings     the sync settings; must not be {@code null}
	 * @param keyFunction  derives the entry key from (atlas object id, fetched object),
	 *                     see {@link #objectIdKeys()} and {@link #featureKeys(String)};
	 *                     returning {@code null} or blank skips the object (logged);
	 *                     must not be {@code null}
	 * @param writer       the target registry's write face; must not be {@code null}
	 */
	public AtlasObjectSync(ReadableScopeService<EObject> scopeService, AtlasSyncSettings settings,
			BiFunction<String, EObject, String> keyFunction, EObjectRegistryWriter writer) {
		this(scopeService, settings, keyFunction, writer, Executors.newSingleThreadScheduledExecutor(runnable -> {
			Thread thread = new Thread(runnable, settings.threadName());
			thread.setDaemon(true);
			return thread;
		}));
	}

	/**
	 * Test constructor: the caller supplies the executor, making the scheduling
	 * deterministic. The engine still owns it - {@link #close()} shuts it down.
	 */
	AtlasObjectSync(ReadableScopeService<EObject> scopeService, AtlasSyncSettings settings,
			BiFunction<String, EObject, String> keyFunction, EObjectRegistryWriter writer,
			ScheduledExecutorService executor) {
		this.scopeService = Objects.requireNonNull(scopeService, "scopeService");
		this.settings = Objects.requireNonNull(settings, "settings");
		this.keyFunction = Objects.requireNonNull(keyFunction, "keyFunction");
		this.writer = Objects.requireNonNull(writer, "writer");
		this.executor = Objects.requireNonNull(executor, "executor");
		executor.execute(this::initialSync);
	}

	/**
	 * The default key function: the atlas object id.
	 *
	 * @return the key function
	 */
	public static BiFunction<String, EObject, String> objectIdKeys() {
		return (objectId, object) -> objectId;
	}

	/**
	 * A feature-derived key function: reads the named attribute of the fetched object
	 * (e.g. a domain id attribute like {@code mid}). Objects without the feature or with
	 * a null value are skipped by the engine (logged).
	 *
	 * @param featureName the attribute name; must not be {@code null}
	 * @return the key function
	 */
	public static BiFunction<String, EObject, String> featureKeys(String featureName) {
		Objects.requireNonNull(featureName, "featureName");
		return (objectId, object) -> {
			EStructuralFeature feature = object.eClass().getEStructuralFeature(featureName);
			if (feature == null) {
				return null;
			}
			Object value = object.eGet(feature);
			return value == null ? null : value.toString();
		};
	}

	/**
	 * Stops the sync. The engine's entries deliberately stay in the registry - only a
	 * complete pass of a living source removes content.
	 */
	@Override
	public void close() {
		active = false;
		executor.shutdownNow();
		try {
			executor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private void initialSync() {
		boolean complete;
		try {
			complete = sync();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Initial sync from atlas scope " + scopeService.getScopeName() + " failed", e);
			complete = false;
		}
		if (!active) {
			return;
		}
		if (complete) {
			if (settings.refreshIntervalMs() > 0) {
				executor.scheduleWithFixedDelay(this::refresh, settings.refreshIntervalMs(),
						settings.refreshIntervalMs(), TimeUnit.MILLISECONDS);
			}
		} else if (settings.retryIntervalMs() > 0) {
			executor.schedule(this::initialSync, settings.retryIntervalMs(), TimeUnit.MILLISECONDS);
		}
	}

	private void refresh() {
		try {
			sync();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Refresh from atlas scope " + scopeService.getScopeName()
					+ " failed - keeping the current registry entries", e);
		}
	}

	/**
	 * One full pass over all configured atlas registries.
	 *
	 * @return {@code true} if every configured registry and object was processed
	 */
	private boolean sync() {
		if (!ensureRequiredPackages()) {
			return false;
		}
		boolean complete = true;
		for (String registry : settings.registries()) {
			complete &= syncRegistry(registry);
		}
		return complete;
	}

	/**
	 * The required-nsURI gate. A listed nsURI whose generated package was never seen
	 * postpones the whole pass (no fetch runs - fetched objects would materialize as
	 * dynamic EObjects and downstream type dispatch would fail silently). Once seen, the
	 * instance is held and re-pinned per pass ({@code putIfAbsent}), healing the window
	 * a model-bundle refresh opens when its configurator removes the package on
	 * deactivate.
	 */
	private boolean ensureRequiredPackages() {
		boolean allPresent = true;
		for (String nsUri : settings.requiredNsUris()) {
			EPackage held = resolvedPackages.get(nsUri);
			if (held != null) {
				EPackage.Registry.INSTANCE.putIfAbsent(nsUri, held);
				continue;
			}
			EPackage resolved = EPackage.Registry.INSTANCE.getEPackage(nsUri);
			if (resolved == null) {
				logger.warning(String.format(
						"Required EPackage %s is not registered yet - postponing the sync pass of atlas scope %s",
						nsUri, scopeService.getScopeName()));
				allPresent = false;
			} else {
				resolvedPackages.put(nsUri, resolved);
			}
		}
		return allPresent;
	}

	private boolean syncRegistry(String registry) {
		String source = settings.providerName() + ":" + registry;
		ReadableRegistryView<EObject> view;
		List<String> ids;
		try {
			view = settings.stage().isEmpty() ? scopeService.registryView(registry)
					: scopeService.registryView(registry, settings.stage());
			ids = settings.objectIds().isEmpty() ? view.listObjectIds() : settings.objectIds();
		} catch (Exception e) {
			logger.log(Level.WARNING,
					String.format("Cannot list objects of atlas registry %s/%s - keeping the current registry entries",
							scopeService.getScopeName(), registry),
					e);
			return false;
		}
		boolean complete = true;
		Map<String, EObjectRegistryEntry> entries = new LinkedHashMap<>();
		for (String objectId : ids) {
			try {
				Optional<EObject> fetched = view.get(objectId);
				if (fetched.isEmpty()) {
					logger.warning(String.format("Object %s is not available in atlas registry %s/%s", objectId,
							scopeService.getScopeName(), registry));
					complete = false;
					continue;
				}
				EObject object = fetched.get();
				String key = keyFunction.apply(objectId, object);
				if (key == null || key.isBlank()) {
					logger.warning(String.format(
							"No key derivable for object %s (a %s) in atlas registry %s/%s - skipping it", objectId,
							object.eClass().getName(), scopeService.getScopeName(), registry));
					continue;
				}
				EObjectRegistryEntry previous = entries.put(key,
						new EObjectRegistryEntry(key, object, source, entryProperties(registry, objectId, object)));
				if (previous != null) {
					logger.warning(String.format(
							"Key collision on %s in atlas registry %s/%s: object %s replaces object %s", key,
							scopeService.getScopeName(), registry, objectId,
							previous.properties().get(PROP_OBJECT_ID)));
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, String.format(
						"Fetching object %s from atlas registry %s/%s failed - keeping its current registry entry",
						objectId, scopeService.getScopeName(), registry), e);
				complete = false;
			}
		}
		if (!active) {
			return false;
		}
		try {
			if (complete) {
				writer.sync(source, entries.values());
			} else {
				// partial pass: push what we have, remove nothing - objects that merely
				// failed to fetch must keep their current entries
				for (EObjectRegistryEntry entry : entries.values()) {
					writer.put(source, entry.key(), entry.object(), entry.properties());
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, String.format("Writing entries of atlas registry %s/%s to the registry failed",
					scopeService.getScopeName(), registry), e);
			return false;
		}
		return complete;
	}

	private Map<String, Object> entryProperties(String registry, String objectId, EObject object) {
		Map<String, Object> properties = new HashMap<>();
		properties.put(AtlasProperties.ATLAS_REMOTE, Boolean.TRUE);
		properties.put(AtlasProperties.ATLAS_SCOPE, scopeService.getScopeName());
		properties.put(AtlasProperties.ATLAS_REGISTRY, registry);
		properties.put(PROP_OBJECT_ID, objectId);
		if (!settings.stage().isEmpty()) {
			properties.put(AtlasProperties.ATLAS_STAGE, settings.stage());
		}
		EPackage ePackage = object.eClass().getEPackage();
		if (ePackage != null && ePackage.getNsURI() != null) {
			properties.put(PROP_NS_URI, ePackage.getNsURI());
		}
		return properties;
	}
}
