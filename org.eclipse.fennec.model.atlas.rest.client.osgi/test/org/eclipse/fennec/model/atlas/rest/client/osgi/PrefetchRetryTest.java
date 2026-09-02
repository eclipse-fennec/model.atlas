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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the pre-fetch retry loop (issue #238): an activation-time pre-fetch that
 * could not reach the Atlas has to run again, or the client publishes nothing until it is
 * restarted.
 */
class PrefetchRetryTest {

	/** Fires scheduled tasks only when the test says so, recording the requested delays. */
	private static final class FakeScheduler implements PrefetchRetry.Scheduler {

		final List<Long> delays = new ArrayList<>();
		final Deque<Runnable> tasks = new ArrayDeque<>();
		int cancelled;

		@Override
		public AutoCloseable schedule(Runnable task, long delayMs) {
			delays.add(delayMs);
			tasks.add(task);
			return () -> cancelled++;
		}

		/** Runs the next due task, if any. */
		boolean fire() {
			Runnable task = tasks.poll();
			if (task == null) {
				return false;
			}
			task.run();
			return true;
		}
	}

	@Test
	void anIncompleteInitialAttemptIsRetriedUntilOneCompletes() {
		FakeScheduler scheduler = new FakeScheduler();
		List<Boolean> results = new ArrayList<>(List.of(false, true));
		PrefetchRetry retry = new PrefetchRetry(() -> results.remove(0), scheduler, 10_000L);

		retry.armUnless(false);

		// first retry: still unreachable -> another one is scheduled
		assertTrue(scheduler.fire());
		assertEquals(List.of(10_000L, 10_000L), scheduler.delays);
		// second retry: the pass completes -> the loop stops
		assertTrue(scheduler.fire());
		assertEquals(2, scheduler.delays.size());
		assertFalse(scheduler.fire());
		retry.close();
	}

	@Test
	void aCompleteInitialAttemptArmsNothing() {
		FakeScheduler scheduler = new FakeScheduler();
		PrefetchRetry retry = new PrefetchRetry(() -> true, scheduler, 10_000L);

		retry.armUnless(true);

		assertTrue(scheduler.delays.isEmpty());
		retry.close();
	}

	@Test
	void anAttemptThatThrowsKeepsTheLoopAlive() {
		FakeScheduler scheduler = new FakeScheduler();
		List<Integer> attempts = new ArrayList<>();
		PrefetchRetry retry = new PrefetchRetry(() -> {
			attempts.add(attempts.size());
			throw new IllegalStateException("boom");
		}, scheduler, 5_000L);

		retry.armUnless(false);
		assertTrue(scheduler.fire());
		assertTrue(scheduler.fire());

		assertEquals(2, attempts.size());
		assertEquals(3, scheduler.delays.size());
		retry.close();
	}

	@Test
	void closeCancelsAPendingRetryAndStopsTheLoop() {
		FakeScheduler scheduler = new FakeScheduler();
		List<Integer> attempts = new ArrayList<>();
		PrefetchRetry retry = new PrefetchRetry(() -> {
			attempts.add(attempts.size());
			return false;
		}, scheduler, 1_000L);

		retry.armUnless(false);
		retry.close();

		assertEquals(1, scheduler.cancelled);
		// a task the executor still fires after the close must not run the pass again
		scheduler.fire();
		assertTrue(attempts.isEmpty());
	}
}
