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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

/**
 * The debounce, without a portal.
 *
 * <p>
 * These are the races D5 actually turns on — a re-register cancelling a retirement, the same key
 * rescheduled, a deactivate abandoning what is pending — so they are worth asserting in
 * milliseconds rather than only through a container.
 * </p>
 */
public class RetirementQueueTest {

    /** Long enough that a cancel lands first on a loaded machine, short enough to be a unit test. */
    private static final long WINDOW_MS = 500;

    @Test
    public void runsTheWorkOnceTheWindowElapses() throws Exception {
        try (RetirementQueue queue = new RetirementQueue("test")) {
            CountDownLatch ran = new CountDownLatch(1);
            queue.schedule("ds", 50, ran::countDown);

            assertThat(ran.await(5, TimeUnit.SECONDS)).as("a retirement nobody cancels must run").isTrue();
            assertThat(queue.pendingCount()).as("and must not stay pending afterwards").isZero();
        }
    }

    @Test
    public void aCancelInsideTheWindowStopsIt() throws Exception {
        try (RetirementQueue queue = new RetirementQueue("test")) {
            AtomicInteger runs = new AtomicInteger();
            queue.schedule("ds", WINDOW_MS, runs::incrementAndGet);

            // This is the whole point of the delay: a content update arrives as unbind-then-bind,
            // and the bind is what says the unbind was not a removal.
            assertThat(queue.cancel("ds")).as("cancelling a pending retirement should report that it did").isTrue();

            Thread.sleep(WINDOW_MS * 2);
            assertThat(runs.get()).as("a cancelled retirement must never run").isZero();
        }
    }

    @Test
    public void cancellingSomethingElseLeavesThePendingWorkAlone() throws Exception {
        try (RetirementQueue queue = new RetirementQueue("test")) {
            CountDownLatch ran = new CountDownLatch(1);
            queue.schedule("ds-a", 50, ran::countDown);

            assertThat(queue.cancel("ds-b")).as("nothing was pending for that key").isFalse();
            assertThat(ran.await(5, TimeUnit.SECONDS)).as("one key's re-register must not save another's").isTrue();
        }
    }

    @Test
    public void reschedulingTheSameKeyKeepsOnlyTheLatest() throws Exception {
        try (RetirementQueue queue = new RetirementQueue("test")) {
            List<String> ran = new CopyOnWriteArrayList<>();
            queue.schedule("ds", WINDOW_MS, () -> ran.add("first"));
            queue.schedule("ds", WINDOW_MS, () -> ran.add("second"));

            assertThat(queue.pendingCount()).as("one pending task per key").isEqualTo(1);
            Thread.sleep(WINDOW_MS * 3);
            // Two unbinds without a bind between them are still one removal, and the mode is read
            // when the task is built — so the newer task is the one that reflects it.
            assertThat(ran).containsExactly("second");
        }
    }

    @Test
    public void closingAbandonsWhatIsPending() throws Exception {
        AtomicInteger runs = new AtomicInteger();
        RetirementQueue queue = new RetirementQueue("test");
        queue.schedule("ds", WINDOW_MS, runs::incrementAndGet);

        // Our own deactivate means "stop working", not "retire the catalogue": a bundle refresh or
        // a configuration change must not empty a portal.
        queue.close();

        Thread.sleep(WINDOW_MS * 2);
        assertThat(runs.get()).as("a pending retirement must not survive close()").isZero();
    }

    @Test
    public void aThrowingRetirementDoesNotKillTheQueue() throws Exception {
        try (RetirementQueue queue = new RetirementQueue("test")) {
            CountDownLatch second = new CountDownLatch(1);
            queue.schedule("ds-a", 10, () -> {
                throw new IllegalStateException("the portal said no");
            });
            Thread.sleep(200);
            queue.schedule("ds-b", 10, second::countDown);

            // A single-threaded scheduler stops running a *repeating* task that throws; a one-shot
            // failure must not take the thread with it, or one unreachable portal would silently
            // end every later retirement.
            assertThat(second.await(5, TimeUnit.SECONDS)).as("a later retirement must still run").isTrue();
        }
    }
}
