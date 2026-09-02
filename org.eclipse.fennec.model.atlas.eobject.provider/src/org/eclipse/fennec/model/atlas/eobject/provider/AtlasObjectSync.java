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
	/** Where the required-nsURI gate resolves; never {@code null}. */
	private final EPackage.Registry packageRegistry;
	/** Only touched on the executor thread. */
	private final Map<String, EPackage> resolvedPackages = new HashMap<>();
	/**
	 * The singleton entries this engine placed itself, by nsURI - the only ones it may
	 * replace or remove. Only touched on the executor thread.
	 */
	private final Map<String, EPackage> pinned = new HashMap<>();
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
		this(scopeService, settings, keyFunction, writer, (EPackage.Registry) null);
	}

	/**
	 * Same, with the registry the required-nsURI gate resolves through.
	 *
	 * @param packageRegistry the registry to resolve {@code required.nsuris} against -
	 *                        the OSGi framework {@code EPackage.Registry}, where both
	 *                        locally shipped and atlas-published packages appear;
	 *                        {@code null} falls back to {@link EPackage.Registry#INSTANCE}
	 */
	public AtlasObjectSync(ReadableScopeService<EObject> scopeService, AtlasSyncSettings settings,
			BiFunction<String, EObject, String> keyFunction, EObjectRegistryWriter writer,
			EPackage.Registry packageRegistry) {
		this(scopeService, settings, keyFunction, writer, packageRegistry,
				Executors.newSingleThreadScheduledExecutor(runnable -> {
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
		this(scopeService, settings, keyFunction, writer, (EPackage.Registry) null, executor);
	}

	/** Test constructor: caller-supplied gate registry and executor. */
	AtlasObjectSync(ReadableScopeService<EObject> scopeService, AtlasSyncSettings settings,
			BiFunction<String, EObject, String> keyFunction, EObjectRegistryWriter writer,
			EPackage.Registry packageRegistry, ScheduledExecutorService executor) {
		this.packageRegistry = packageRegistry == null ? EPackage.Registry.INSTANCE : packageRegistry;
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
	 * complete pass of a living source removes content. Its stop-gap entries in the EMF
	 * singleton do not: they exist to keep <em>this</em> engine's passes running while a
	 * package has no provider, and one left behind would occupy an nsURI its real owner
	 * can no longer take (the mirroring rule of issue #227 makes an occupied nsURI final).
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
		// After the await no pass is running, so the executor-thread-only maps are ours.
		pinned.forEach((nsUri, ePackage) -> EPackage.Registry.INSTANCE.remove(nsUri, ePackage));
		pinned.clear();
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
	 * The required-nsURI gate. A listed nsURI nothing provides postpones the whole pass
	 * (no fetch runs - fetched objects would materialize as dynamic EObjects and
	 * downstream type dispatch would fail silently).
	 * <p>
	 * Every pass re-reads the current provider, so the gate follows the package rather
	 * than the instance it first saw: a model that is deleted from the atlas and
	 * uploaded again comes back as a <em>new</em> EPackage instance, and holding on to
	 * the old one leaves everything downstream typed against a package nobody publishes
	 * any more (issue #238). Resolution order is the injected {@link #packageRegistry}
	 * (the OSGi one, where both locally shipped and atlas-published packages appear),
	 * then {@link EPackage.Registry#INSTANCE} for providers that only ever write there.
	 * <p>
	 * While no provider has it, the last known instance is pinned into the EMF singleton
	 * so the pass can still run - healing the window a model-bundle refresh opens when
	 * its configurator removes the package on deactivate. That pin is a stop-gap, not a
	 * claim: it is tracked in {@link #pinned}, given back the moment a real provider
	 * turns up, and an nsURI another bundle already holds in the singleton is never
	 * touched (the mirroring rule of issue #227 - overwriting a generated package with a
	 * dynamic one breaks the generated code that owns it).
	 */
	private boolean ensureRequiredPackages() {
		boolean allPresent = true;
		for (String nsUri : settings.requiredNsUris()) {
			allPresent &= ensureRequiredPackage(nsUri);
		}
		return allPresent;
	}

	private boolean ensureRequiredPackage(String nsUri) {
		EPackage provided = provider(nsUri);
		if (provided != null) {
			resolvedPackages.put(nsUri, provided);
			mirror(nsUri, provided);
			return true;
		}
		EPackage held = resolvedPackages.get(nsUri);
		if (held == null) {
			logger.warning(String.format(
					"Required EPackage %s is not registered yet - postponing the sync pass of atlas scope %s", nsUri,
					scopeService.getScopeName()));
			return false;
		}
		mirror(nsUri, held);
		return true;
	}

	/**
	 * Whoever currently provides {@code nsUri}, or {@code null} if that is nobody. Our own
	 * stop-gap pin does not count as a provider - it is exactly what has to make way once
	 * someone real shows up.
	 */
	private EPackage provider(String nsUri) {
		EPackage pin = pinned.get(nsUri);
		EPackage provided = packageRegistry.getEPackage(nsUri);
		if (provided != null && provided != pin) {
			return provided;
		}
		if (packageRegistry == EPackage.Registry.INSTANCE) {
			return null; // already looked there
		}
		EPackage global = EPackage.Registry.INSTANCE.getEPackage(nsUri);
		return global == pin ? null : global;
	}

	/**
	 * Keep the EMF singleton pointing at {@code ePackage} for the legacy consumers that
	 * read it - but only ever occupy an nsURI nobody else claims, and only ever replace or
	 * remove an entry this engine placed itself.
	 */
	private void mirror(String nsUri, EPackage ePackage) {
		EPackage pin = pinned.get(nsUri);
		if (pin == ePackage) {
			return; // ours already, and current
		}
		if (pin != null) {
			// Value-matching remove: if something else won the nsURI meanwhile, it stays.
			EPackage.Registry.INSTANCE.remove(nsUri, pin);
			pinned.remove(nsUri);
		}
		// containsKey is delegate-aware and, unlike getEPackage, does not force a lazily
		// registered EPackage.Descriptor to initialise just because we looked.
		if (EPackage.Registry.INSTANCE.containsKey(nsUri)) {
			return;
		}
		if (EPackage.Registry.INSTANCE.putIfAbsent(nsUri, ePackage) == null) {
			pinned.put(nsUri, ePackage);
		}
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
