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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.dcat.atlas.client.api.RetryableException;
import org.eclipse.fennec.dcat.atlas.client.api.TransportException;

/**
 * Retries what is worth retrying, remembers what is not, and can say which is which.
 *
 * <h2>Two classes of failure, and only one of them is worth a second attempt</h2>
 *
 * A portal that refuses a payload as invalid will refuse the identical payload every time: a model
 * constraint or a SHACL violation is permanent for that entity, and retrying only turns one useful
 * report into a stream of noise. A 503, a connect timeout or a portal that is not ready yet is the
 * opposite — nothing is wrong with what we sent, and the only mistake would be to give up. Before
 * this existed both were logged and dropped, so a portal that was briefly down left the catalogue
 * permanently behind whatever the atlas actually served.
 *
 * <h2>Bounded, in both directions</h2>
 *
 * The delay doubles per attempt up to a ceiling, so a portal that is down for an hour is polled a
 * handful of times rather than continuously; and the attempts run out, so a failure that only looks
 * transient does not retry forever. Giving up is recorded rather than forgotten — that is what the
 * health check reports, and it is the difference between a stale catalogue somebody can see and one
 * nobody knows about.
 *
 * <h2>Retrying is safe because publishing is idempotent</h2>
 *
 * Every portal write is a {@code PUT} under a caller-chosen id, and every link assertion is
 * additive, so a retry is the same operation again rather than a second one. That is what lets this
 * hold a {@code Runnable} and simply run it again.
 */
final class RetryQueue implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(RetryQueue.class.getName());

    private final ScheduledExecutorService scheduler;
    private final long initialDelayMillis;
    private final long maxDelayMillis;
    private final int maxAttempts;

    /** What is failing, by key. An entry is either awaiting a retry or given up on. */
    private final Map<String, Failure> failures = new ConcurrentHashMap<>();

    /**
     * @param what     a human-readable description of the operation, for the health check
     * @param reason   the failure, as one line
     * @param attempts how many attempts have failed so far
     * @param retrying whether another attempt is scheduled
     */
    private record Failure(String what, String reason, int attempts, boolean retrying, ScheduledFuture<?> scheduled) {
    }

    /** One line per failing target, for the health check. */
    record Report(int retrying, int abandoned, List<String> lines) {
    }

    RetryQueue(String name, long initialDelayMillis, long maxDelayMillis, int maxAttempts) {
        this.initialDelayMillis = Math.max(1_000L, initialDelayMillis);
        this.maxDelayMillis = Math.max(this.initialDelayMillis, maxDelayMillis);
        this.maxAttempts = Math.max(1, maxAttempts);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, name);
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Whether {@code cause} is worth another attempt.
     *
     * <p>
     * The cause chain is walked because a failure reaches us through a {@code Promise}, which may
     * have wrapped it on the way — and a wrapped 503 is still a 503.
     * </p>
     */
    static boolean retryable(Throwable cause) {
        for (Throwable current = cause; current != null; current = current.getCause()) {
            if (current instanceof RetryableException || current instanceof TransportException
                    || current instanceof PortalNotReadyException) {
                return true;
            }
            if (current.getCause() == current) {
                break;
            }
        }
        return false;
    }

    /** The operation at {@code key} worked: forget whatever it was failing with. */
    void succeeded(String key) {
        Failure previous = failures.remove(key);
        if (previous != null) {
            if (previous.scheduled() != null) {
                previous.scheduled().cancel(false);
            }
            LOGGER.info(() -> previous.what() + " succeeded after " + previous.attempts() + " failed attempt(s)");
        }
    }

    /**
     * Records a failure and, when it is worth retrying and attempts remain, schedules {@code retry}.
     *
     * @param key   identifies the operation; one pending retry per key
     * @param what  what was being done, for the log and the health check
     * @param cause why it failed
     * @param retry the same operation again — idempotent, so this is a repeat rather than a second
     */
    void failed(String key, String what, Throwable cause, Runnable retry) {
        String reason = reasonOf(cause);
        if (!retryable(cause)) {
            // Permanent for this entity. The report is the actionable part; another attempt is not.
            abandon(key, what, reason, attemptsOf(key) + 1, cause, false);
            return;
        }
        int attempts = attemptsOf(key) + 1;
        if (attempts >= maxAttempts) {
            abandon(key, what, reason, attempts, cause, true);
            return;
        }
        long delay = delayFor(attempts);
        ScheduledFuture<?> scheduled = scheduler.schedule(() -> {
            failures.computeIfPresent(key,
                    (k, failure) -> new Failure(failure.what(), failure.reason(), failure.attempts(), false, null));
            try {
                retry.run();
            } catch (RuntimeException e) {
                LOGGER.log(Level.WARNING, "Retrying " + what + " failed to start", e);
            }
        }, delay, TimeUnit.MILLISECONDS);
        Failure previous = failures.put(key, new Failure(what, reason, attempts, true, scheduled));
        if (previous != null && previous.scheduled() != null) {
            previous.scheduled().cancel(false);
        }
        LOGGER.log(Level.INFO, () -> what + " failed (" + reason + "); attempt " + attempts + " of " + maxAttempts
                + ", retrying in " + (delay / 1000) + "s");
    }

    /**
     * Records a failure nothing will retry — a payload the portal will always refuse, or a
     * configuration only an operator can fix.
     */
    void permanent(String key, String what, String reason) {
        failures.put(key, new Failure(what, reason, attemptsOf(key) + 1, false, null));
    }

    /** What is currently failing. */
    Report report() {
        List<String> lines = new ArrayList<>();
        int retrying = 0;
        int abandoned = 0;
        for (Failure failure : failures.values()) {
            if (failure.retrying()) {
                retrying++;
                lines.add(failure.what() + ": " + failure.reason() + " (attempt " + failure.attempts() + " of "
                        + maxAttempts + ", retrying)");
            } else {
                abandoned++;
                lines.add(failure.what() + ": " + failure.reason() + " (gave up after " + failure.attempts()
                        + " attempt(s))");
            }
        }
        return new Report(retrying, abandoned, List.copyOf(lines));
    }

    /** How many retries are waiting. */
    int pending() {
        return (int) failures.values().stream().filter(Failure::retrying).count();
    }

    @Override
    public void close() {
        failures.values().forEach(failure -> {
            if (failure.scheduled() != null) {
                failure.scheduled().cancel(false);
            }
        });
        failures.clear();
        scheduler.shutdownNow();
    }

    private void abandon(String key, String what, String reason, int attempts, Throwable cause, boolean exhausted) {
        failures.put(key, new Failure(what, reason, attempts, false, null));
        if (exhausted) {
            LOGGER.log(Level.WARNING, "Giving up on " + what + " after " + attempts
                    + " attempts; the portal is behind what this atlas serves until something publishes again",
                    cause);
        } else {
            LOGGER.log(Level.WARNING, what + " failed and will not be retried: " + reason, cause);
        }
    }

    /** Doubling, from the initial delay up to the ceiling. */
    private long delayFor(int attempts) {
        long delay = initialDelayMillis;
        for (int i = 1; i < attempts && delay < maxDelayMillis; i++) {
            delay = Math.min(maxDelayMillis, delay * 2);
        }
        return delay;
    }

    private int attemptsOf(String key) {
        Failure failure = failures.get(key);
        return failure == null ? 0 : failure.attempts();
    }

    private static String reasonOf(Throwable cause) {
        Throwable root = cause;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String message = root.getMessage();
        return root.getClass().getSimpleName() + (message == null ? "" : ": " + message);
    }
}
