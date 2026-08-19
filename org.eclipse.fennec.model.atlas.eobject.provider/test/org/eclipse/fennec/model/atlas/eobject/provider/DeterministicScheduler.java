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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Single-threaded, manually driven {@link ScheduledExecutorService} stand-in: tasks run
 * only when the test calls {@link #runPending()} / {@link #runScheduledOnce()}, on the
 * test thread. Only the operations the engine uses are implemented.
 */
class DeterministicScheduler implements ScheduledExecutorService {

	record ScheduledTask(Runnable task, long delayMs, boolean periodic) {
	}

	private final Deque<Runnable> immediate = new ArrayDeque<>();
	final List<ScheduledTask> scheduled = new ArrayList<>();
	private boolean shutdown;

	/** Runs every immediately submitted task (including ones submitted while running). */
	void runPending() {
		while (!immediate.isEmpty()) {
			immediate.poll().run();
		}
	}

	/** Drains the scheduled tasks and runs each once (then their follow-up submissions). */
	void runScheduledOnce() {
		List<ScheduledTask> due = List.copyOf(scheduled);
		scheduled.clear();
		due.forEach(t -> t.task().run());
		runPending();
	}

	@Override
	public void execute(Runnable command) {
		if (!shutdown) {
			immediate.add(command);
		}
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		if (!shutdown) {
			scheduled.add(new ScheduledTask(command, unit.toMillis(delay), false));
		}
		return null;
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		if (!shutdown) {
			scheduled.add(new ScheduledTask(command, unit.toMillis(delay), true));
		}
		return null;
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return scheduleWithFixedDelay(command, initialDelay, period, unit);
	}

	@Override
	public void shutdown() {
		shutdown = true;
	}

	@Override
	public List<Runnable> shutdownNow() {
		shutdown = true;
		List<Runnable> pending = List.copyOf(immediate);
		immediate.clear();
		scheduled.clear();
		return pending;
	}

	@Override
	public boolean isShutdown() {
		return shutdown;
	}

	@Override
	public boolean isTerminated() {
		return shutdown;
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) {
		return true;
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Future<?> submit(Runnable task) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) {
		throw new UnsupportedOperationException();
	}
}
