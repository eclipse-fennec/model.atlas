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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;

/** Unit tests for the P3-9 per-nsURI lock registry. */
class NsUriLocksTest {

	@Test
	void sameNsUriYieldsSameLockDistinctNsUrisDiffer() {
		NsUriLocks locks = new NsUriLocks();
		assertSame(locks.lockFor("urn:a"), locks.lockFor("urn:a"));
		assertNotSame(locks.lockFor("urn:a"), locks.lockFor("urn:b"));
	}

	@Test
	void runHoldsTheLockDuringTheActionAndReleasesAfter() {
		NsUriLocks locks = new NsUriLocks();
		ReentrantLock lock = locks.lockFor("urn:a");
		boolean[] ran = { false };

		locks.run("urn:a", () -> {
			ran[0] = true;
			assertTrue(lock.isHeldByCurrentThread(), "the action runs while holding the nsURI lock");
		});

		assertTrue(ran[0]);
		assertFalse(lock.isLocked(), "the lock is released after the action");
	}

	@Test
	void distinctNsUrisDoNotBlockEachOther() throws InterruptedException {
		NsUriLocks locks = new NsUriLocks();
		CountDownLatch aHeld = new CountDownLatch(1);
		CountDownLatch releaseA = new CountDownLatch(1);

		Thread holder = new Thread(() -> locks.run("urn:a", () -> {
			aHeld.countDown();
			try {
				releaseA.await(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}));
		holder.setDaemon(true);
		holder.start();

		assertTrue(aHeld.await(5, TimeUnit.SECONDS), "the holder grabbed urn:a");
		boolean[] ranB = { false };
		// Must not block on urn:a's lock — different nsURI.
		locks.run("urn:b", () -> ranB[0] = true);
		assertTrue(ranB[0]);

		releaseA.countDown();
		holder.join(5_000);
	}
}
