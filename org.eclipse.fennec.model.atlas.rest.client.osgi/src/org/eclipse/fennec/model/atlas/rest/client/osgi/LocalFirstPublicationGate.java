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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EPackage;

/**
 * P3-7 — local-first suppression. A {@link PackagePublication} that sits in front of
 * the real {@link RemoteEPackagePublisher} and only publishes a remote EPackage when
 * no <em>local</em> ({@code EPackage} or {@code EPackageConfigurator}, not
 * {@code atlas.remote}) service already provides the same nsURI. A locally shipped
 * model always wins — unless {@code force.remote=true} (P3-8), in which case the gate
 * publishes regardless.
 * <p>
 * When a remote publication is suppressed, the package is <em>parked</em> as a
 * candidate. The OSGi {@link LocalServiceWatcher} feeds local service lifecycle into
 * {@link #onLocalAppeared(String)} / {@link #onLocalDisappeared(String)}:
 * <ul>
 * <li>a local appears for an nsURI we published → withdraw our service and park it;</li>
 * <li>a local disappears for an nsURI we have parked → republish it, but only after a
 * short debounce, and only if the local is still gone when the debounce fires.</li>
 * </ul>
 * The debounce plus re-check is what prevents flapping: a local that is briefly
 * unregistered and immediately re-registered cancels the pending republish
 * ({@link #onLocalAppeared(String)} cancels it), so the remote never momentarily
 * registers.
 * <p>
 * The decision logic is OSGi-free (the framework wiring is in {@link LocalServiceWatcher}),
 * so it is unit-testable with a fake {@link Scheduler} and presence {@link Predicate}.
 * All state transitions are guarded by a single lock; the gate's own publish/unpublish
 * never re-enter it because the watcher filters out {@code atlas.remote} services.
 */
final class LocalFirstPublicationGate implements PackagePublication {

	/** Schedules a debounced republish; the returned handle cancels it if it has not run. */
	@FunctionalInterface
	interface Scheduler {
		AutoCloseable schedule(Runnable task, long delayMs);
	}

	/** A remote publication we may (re)publish: the package plus its origin properties. */
	private record Candidate(EPackage ePackage, String scope, String stage, String version, String serverFingerprint) {
	}

	private static final Logger LOGGER = Logger.getLogger(LocalFirstPublicationGate.class.getName());

	private final PackagePublication publisher;
	private final Consumer<String> unpublisher;
	private final Predicate<String> localPresent;
	private final boolean forceRemote;
	private final Scheduler scheduler;
	private final long debounceMs;

	/** Suppressed-but-wanted remotes (a local provides the nsURI), keyed by nsURI. */
	private final Map<String, Candidate> parked = new ConcurrentHashMap<>();
	/** Remotes we actually published, keyed by nsURI (so we can withdraw + re-park them). */
	private final Map<String, Candidate> publishedByUs = new ConcurrentHashMap<>();
	/** Pending debounced republish tasks, keyed by nsURI. */
	private final Map<String, AutoCloseable> pendingRepublish = new ConcurrentHashMap<>();

	private final Object lock = new Object();

	LocalFirstPublicationGate(PackagePublication publisher, Consumer<String> unpublisher,
			Predicate<String> localPresent, boolean forceRemote, Scheduler scheduler, long debounceMs) {
		this.publisher = Objects.requireNonNull(publisher, "publisher");
		this.unpublisher = Objects.requireNonNull(unpublisher, "unpublisher");
		this.localPresent = Objects.requireNonNull(localPresent, "localPresent");
		this.forceRemote = forceRemote;
		this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
		this.debounceMs = Math.max(0L, debounceMs);
	}

	@Override
	public boolean publish(EPackage ePackage, String scope, String stage, String version, String serverFingerprint) {
		String nsUri = ePackage.getNsURI();
		if (nsUri == null || nsUri.isBlank()) {
			return publisher.publish(ePackage, scope, stage, version, serverFingerprint); // let the publisher warn/handle it
		}
		Candidate candidate = new Candidate(ePackage, scope, stage, version, serverFingerprint);
		synchronized (lock) {
			if (!forceRemote && localPresent.test(nsUri)) {
				parked.put(nsUri, candidate);
				LOGGER.log(Level.INFO, () -> "Local-first: a local EPackage already provides " + nsUri
						+ "; suppressing the remote publication (parked)");
				return false;
			}
			return doPublish(nsUri, candidate);
		}
	}

	/** A local service for {@code nsUri} appeared — suppress our remote if we published it. */
	void onLocalAppeared(String nsUri) {
		synchronized (lock) {
			cancelPending(nsUri); // a re-appearance cancels a scheduled republish → no flap
			if (forceRemote) {
				return; // force.remote keeps the remote regardless of locals
			}
			Candidate published = publishedByUs.remove(nsUri);
			if (published != null) {
				unpublisher.accept(nsUri);
				parked.put(nsUri, published);
				LOGGER.log(Level.INFO, () -> "Local-first: a local EPackage appeared for " + nsUri
						+ "; withdrawing the remote publication (parked)");
			}
		}
	}

	/** A local service for {@code nsUri} disappeared — republish a parked remote, debounced. */
	void onLocalDisappeared(String nsUri) {
		synchronized (lock) {
			if (forceRemote || !parked.containsKey(nsUri)) {
				return;
			}
			cancelPending(nsUri);
			AutoCloseable handle = scheduler.schedule(() -> republishIfStillAbsent(nsUri), debounceMs);
			pendingRepublish.put(nsUri, handle);
		}
	}

	private void republishIfStillAbsent(String nsUri) {
		synchronized (lock) {
			pendingRepublish.remove(nsUri);
			if (forceRemote || localPresent.test(nsUri)) {
				return; // the local came back within the debounce window — stay suppressed
			}
			Candidate candidate = parked.get(nsUri);
			if (candidate != null) {
				LOGGER.log(Level.INFO,
						() -> "Local-first: the local EPackage for " + nsUri + " is gone; publishing the remote");
				doPublish(nsUri, candidate);
			}
		}
	}

	private boolean doPublish(String nsUri, Candidate candidate) {
		boolean published = publisher.publish(candidate.ePackage(), candidate.scope(), candidate.stage(),
				candidate.version(), candidate.serverFingerprint());
		if (published) {
			publishedByUs.put(nsUri, candidate);
			parked.remove(nsUri);
		}
		return published;
	}

	private void cancelPending(String nsUri) {
		AutoCloseable handle = pendingRepublish.remove(nsUri);
		if (handle == null) {
			return;
		}
		try {
			handle.close();
		} catch (Exception e) {
			LOGGER.log(Level.FINE, "Cancelling a pending republish failed", e);
		}
	}

	// ---- test/observation accessors --------------------------------------

	boolean isParked(String nsUri) {
		return parked.containsKey(nsUri);
	}

	boolean isPublished(String nsUri) {
		return publishedByUs.containsKey(nsUri);
	}
}
