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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * P3-9 — a registry of per-nsURI locks, so the publisher can serialise the
 * publish / republish / unpublish of one nsURI (e.g. a drift swap racing an initial
 * publication) while substitutions of <em>unrelated</em> nsURIs proceed in parallel.
 * <p>
 * Locks are created on demand and kept (the set of nsURIs a client touches is
 * bounded); they are never removed, which keeps {@link #run} free of the
 * create/lookup races that pruning would introduce.
 */
final class NsUriLocks {

	private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

	/** Run {@code action} while holding the lock for {@code nsUri}. */
	void run(String nsUri, Runnable action) {
		ReentrantLock lock = lockFor(nsUri);
		lock.lock();
		try {
			action.run();
		} finally {
			lock.unlock();
		}
	}

	/** The (stable) lock for {@code nsUri} — same instance for the same nsURI. */
	ReentrantLock lockFor(String nsUri) {
		return locks.computeIfAbsent(nsUri, key -> new ReentrantLock());
	}
}
