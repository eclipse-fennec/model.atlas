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
    public void aTaskDeRegistersOnlyItself() throws Exception {
        // #235: the running task used to do pending.remove(key) unconditionally, so a task already
        // past cancelling deleted the registration of the replacement that had superseded it. The
        // replacement was then untracked: cancel() could not reach it, and the re-register that was
        // supposed to say "that unbind was an update" no longer stopped the retirement.
        ManualScheduler manual = new ManualScheduler();
        try (RetirementQueue queue = new RetirementQueue(manual)) {
            AtomicInteger replacementRuns = new AtomicInteger();
            queue.schedule("ds", 0, () -> {
                // the superseded retirement; its body is irrelevant, its de-registration is not
            });
            // It is already running, so the replacement below cannot cancel it — the precondition
            // for the race. Its de-registration has not happened yet.
            manual.markStarted(0);
            queue.schedule("ds", WINDOW_MS, replacementRuns::incrementAndGet);

            // Only now does the superseded task reach its de-registration.
            manual.runCaptured(0);

            assertThat(queue.cancel("ds")).as("the replacement must still be cancellable").isTrue();
            manual.runAll();
            assertThat(replacementRuns.get()).as("a cancelled replacement must never run").isZero();
        }
    }

    @Test
    public void aCompletedTaskLeavesNothingPendingEvenWithoutDelay() throws Exception {
        // The other half of #235: the future used to be installed *after* it was scheduled, so a
        // zero-delay task could de-register before it was ever registered, and its finished future
        // then sat in the map for good.
        // runOnSchedule models the queue thread picking a zero-delay task up the instant it is
        // handed over — before schedule() has even returned to install its future.
        ManualScheduler manual = new ManualScheduler();
        manual.runOnSchedule();
        try (RetirementQueue queue = new RetirementQueue(manual)) {
            queue.schedule("ds", 0, () -> {
            });

            assertThat(queue.pendingCount()).as("a finished retirement must not stay pending").isZero();
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

    /**
     * A scheduler that captures tasks instead of running them, so a test can decide the exact
     * order in which they execute. Only {@code schedule(Runnable, long, TimeUnit)} is used by
     * {@link RetirementQueue}; everything else stays the inherited behaviour.
     */
    private static final class ManualScheduler extends java.util.concurrent.ScheduledThreadPoolExecutor {

        private final List<Task> captured = new CopyOnWriteArrayList<>();
        private volatile boolean runOnSchedule;

        ManualScheduler() {
            super(1);
        }

        /** Run each task inside {@code schedule}, before the caller can install its future. */
        void runOnSchedule() {
            this.runOnSchedule = true;
        }

        @Override
        public java.util.concurrent.ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            Task task = new Task(command);
            captured.add(task);
            if (runOnSchedule) {
                task.runIfLive();
            }
            return task;
        }

        /** Marks the n-th task as running, so it can no longer be cancelled. */
        void markStarted(int index) {
            captured.get(index).markStarted();
        }

        /** Runs the n-th captured task on the calling thread, unless it was cancelled. */
        void runCaptured(int index) {
            captured.get(index).runIfLive();
        }

        void runAll() {
            captured.forEach(Task::runIfLive);
        }
    }

    /** The captured task and its future in one object; cancellation is just a flag. */
    private static final class Task implements java.util.concurrent.ScheduledFuture<Object> {

        private final Runnable command;
        private volatile boolean cancelled;
        private volatile boolean started;
        private volatile boolean done;

        Task(Runnable command) {
            this.command = command;
        }

        void markStarted() {
            started = true;
        }

        void runIfLive() {
            if (cancelled || done) {
                return;
            }
            done = true;
            command.run();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            // A task that has started is past cancelling, exactly as ScheduledFuture reports it.
            if (cancelled || started || done) {
                return false;
            }
            cancelled = true;
            return true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isDone() {
            return done || cancelled;
        }

        @Override
        public Object get() {
            return null;
        }

        @Override
        public Object get(long timeout, TimeUnit unit) {
            return null;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return 0;
        }

        @Override
        public int compareTo(java.util.concurrent.Delayed other) {
            return 0;
        }
    }
}
