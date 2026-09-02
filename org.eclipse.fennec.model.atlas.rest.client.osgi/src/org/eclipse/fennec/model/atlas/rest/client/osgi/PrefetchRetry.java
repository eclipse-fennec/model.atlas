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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * #238 — re-runs an activation-time pre-fetch that could not complete, until one does.
 * <p>
 * {@link EagerPrefetch} publishes whatever the Atlas holds at activation. With
 * {@code mode.strict=false} an unreachable server is logged and the pass gives up, and
 * nothing used to run it again: a client that came up before its Atlas published nothing
 * at all. The drift watcher cannot make up for that either — its first probe only
 * establishes the aggregate baseline, so packages that were already on the server are
 * never reported as a change, and the runtime needed a restart to see any model.
 * <p>
 * Once a pass completes, this stops: from there on freshness is the drift watcher's job
 * (it reports additions since #228). Retrying is therefore bounded by the Atlas becoming
 * reachable once, not by a poll interval that runs forever.
 * <p>
 * OSGi-free — the pass is a {@link BooleanSupplier} ({@code true} = it completed) and the
 * scheduling is a {@link Scheduler} seam, so the loop is unit-testable without a
 * framework or a server.
 */
final class PrefetchRetry implements AutoCloseable {

	/** Schedules one delayed retry; closing the returned handle cancels it if it has not run. */
	@FunctionalInterface
	interface Scheduler {
		AutoCloseable schedule(Runnable task, long delayMs);
	}

	private static final Logger LOGGER = Logger.getLogger(PrefetchRetry.class.getName());

	private final BooleanSupplier attempt;
	private final Scheduler scheduler;
	private final long delayMs;
	private final AtomicReference<AutoCloseable> pending = new AtomicReference<>();
	private volatile boolean closed;

	/**
	 * @param attempt   runs one pre-fetch pass; {@code true} when it completed (the server
	 *                  was reachable throughout), {@code false} when it has to be retried
	 * @param scheduler where the retries run — never the caller's thread
	 * @param delayMs   delay before each retry
	 */
	PrefetchRetry(BooleanSupplier attempt, Scheduler scheduler, long delayMs) {
		this.attempt = Objects.requireNonNull(attempt, "attempt");
		this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
		this.delayMs = Math.max(0L, delayMs);
	}

	/**
	 * Arm the loop unless the activation-time pass already completed.
	 *
	 * @param initialAttemptComplete whether the pass run during activation completed
	 */
	void armUnless(boolean initialAttemptComplete) {
		if (initialAttemptComplete) {
			return;
		}
		LOGGER.log(Level.INFO,
				() -> "Atlas pre-fetch did not complete; retrying every " + delayMs + " ms until it does");
		schedule();
	}

	private void schedule() {
		if (closed) {
			return;
		}
		pending.set(scheduler.schedule(this::retry, delayMs));
		if (closed) {
			// close() ran between the guard and the set — cancel what we just armed.
			cancelPending();
		}
	}

	private void retry() {
		if (closed) {
			return; // the component went away while the task sat in the queue
		}
		pending.set(null);
		boolean complete;
		try {
			complete = attempt.getAsBoolean();
		} catch (RuntimeException e) {
			// A retry that blows up must not end the loop — the next one may well succeed.
			LOGGER.log(Level.WARNING, e, () -> "Atlas pre-fetch retry failed; trying again in " + delayMs + " ms");
			complete = false;
		}
		if (complete) {
			LOGGER.log(Level.INFO, () -> "Atlas pre-fetch completed on a retry; drift detection takes over");
			return;
		}
		schedule();
	}

	/** Stop the loop and cancel a retry that has not run yet. */
	@Override
	public void close() {
		closed = true;
		cancelPending();
	}

	private void cancelPending() {
		AutoCloseable handle = pending.getAndSet(null);
		if (handle == null) {
			return;
		}
		try {
			handle.close();
		} catch (Exception e) {
			LOGGER.log(Level.FINE, "Cancelling a pending pre-fetch retry failed", e);
		}
	}
}
