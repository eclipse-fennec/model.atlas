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
package org.eclipse.fennec.model.atlas.dcat.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Deferred, cancellable retirement — one pending task per key.
 *
 * <h2>Why anything is deferred at all</h2>
 *
 * A changed EPackage is republished by <em>unregistering and re-registering</em> the service, so a
 * content update reaches a whiteboard tracker as an unbind immediately followed by a bind. Acting
 * on the unbind at once would make every edit briefly unpublish the very thing it is updating —
 * two portal writes and a window in which a harvester sees the model as gone. Waiting a little and
 * letting the re-register cancel the retirement turns that sequence back into what it actually is:
 * one update.
 *
 * <p>
 * Consequently the delay is not a tuning knob for throughput. It is the width of the window in
 * which an unbind is still ambiguous, and it should comfortably exceed how long a re-register
 * takes.
 * </p>
 *
 * <h2>Own class, because it is the part with the races</h2>
 *
 * Scheduling, cancelling, re-scheduling the same key and shutting down mid-flight is the whole of
 * D5's fiddly behaviour and none of its portal behaviour, so it is worth having somewhere it can
 * be tested in milliseconds without a portal, a client or DS.
 */
final class RetirementQueue implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(RetirementQueue.class.getName());

    private final ScheduledExecutorService scheduler;

    /** Pending tasks by key. An entry is removed by whoever finishes it: the task, or a cancel. */
    private final Map<String, ScheduledFuture<?>> pending = new ConcurrentHashMap<>();

    RetirementQueue(String name) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, name);
            // A daemon: a pending retirement must never be the reason a JVM stays up, and the
            // shutdown path deliberately abandons the queue rather than draining it.
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Schedules {@code work} for {@code key}, replacing whatever was pending for it.
     *
     * @param key          identifies the thing being retired; one pending task per key
     * @param delayMillis  how long the unbind stays ambiguous; {@code <= 0} runs it promptly, still
     *                     on the queue thread rather than the caller's
     * @param work         run unless cancelled first
     */
    void schedule(String key, long delayMillis, Runnable work) {
        ScheduledFuture<?> scheduled = scheduler.schedule(() -> {
            // Remove before running, not after: a bind arriving while the work is in flight must
            // not be able to cancel a task that is already past cancelling. It sees no pending
            // entry, republishes, and the publish path is what reconciles the outcome.
            pending.remove(key);
            try {
                work.run();
            } catch (RuntimeException e) {
                LOGGER.log(Level.WARNING, "Retiring " + key + " failed", e);
            }
        }, Math.max(0, delayMillis), TimeUnit.MILLISECONDS);
        ScheduledFuture<?> previous = pending.put(key, scheduled);
        if (previous != null) {
            previous.cancel(false);
        }
    }

    /**
     * Cancels the pending retirement for {@code key}, if it has not started.
     *
     * @return {@code true} when something was pending and is now cancelled — which is exactly the
     *         "that unbind was an update" case, and worth logging as such
     */
    boolean cancel(String key) {
        ScheduledFuture<?> scheduled = pending.remove(key);
        return scheduled != null && scheduled.cancel(false);
    }

    /** Cancels everything pending, leaving the queue usable. */
    void cancelAll() {
        pending.keySet().forEach(this::cancel);
    }

    /** How many retirements are waiting out their window. For tests and for logging. */
    int pendingCount() {
        return pending.size();
    }

    /**
     * Cancels everything pending and stops the thread. Deliberately does <em>not</em> drain: our
     * own deactivate means "stop working", not "retire the catalogue".
     */
    @Override
    public void close() {
        cancelAll();
        scheduler.shutdownNow();
    }
}
