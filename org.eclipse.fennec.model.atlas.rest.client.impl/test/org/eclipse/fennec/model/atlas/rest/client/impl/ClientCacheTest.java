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
package org.eclipse.fennec.model.atlas.rest.client.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import org.junit.jupiter.api.Test;

/**
 * P2-5 — bounded, TTL-aware in-memory cache.
 */
class ClientCacheTest {

	@Test
	void getPutAndInvalidate() {
		ClientCache<String, String> cache = new ClientCache<>(10, 0);
		assertTrue(cache.get("a").isEmpty());

		cache.put("a", "A", "etag-a", "Mon");
		assertEquals(Optional.of("A"), cache.get("a"));

		cache.invalidate("a");
		assertTrue(cache.get("a").isEmpty());
	}

	@Test
	void storesValidatorsButNotViaApi() {
		ClientCache<String, String> cache = new ClientCache<>(10, 0);
		cache.put("a", "A", "\"v1\"", "Mon, 01 Jan 2026 00:00:00 GMT");

		ClientCache.Entry<String> entry = cache.lookup("a").orElseThrow();
		assertEquals("A", entry.value());
		assertEquals("\"v1\"", entry.etag());
		assertEquals("Mon, 01 Jan 2026 00:00:00 GMT", entry.lastModified());
		// (ClientCache/Entry are package-private to rest.client.impl; never on the API surface.)
	}

	@Test
	void evictsLeastRecentlyUsedBeyondMaxEntries() {
		ClientCache<String, String> cache = new ClientCache<>(2, 0);
		cache.put("a", "A", null, null);
		cache.put("b", "B", null, null);
		cache.get("a"); // touch 'a' so 'b' becomes least-recently-used
		cache.put("c", "C", null, null); // exceeds cap -> evict 'b'

		assertTrue(cache.get("a").isPresent());
		assertTrue(cache.get("b").isEmpty(), "least-recently-used 'b' should be evicted");
		assertTrue(cache.get("c").isPresent());
		assertEquals(2, cache.size());
	}

	@Test
	void zeroOrNegativeMaxEntriesIsUnbounded() {
		ClientCache<String, String> cache = new ClientCache<>(0, 0);
		for (int i = 0; i < 1_000; i++) {
			cache.put("k" + i, "v" + i, null, null);
		}
		assertEquals(1_000, cache.size());
	}

	@Test
	void respectsTtl() {
		AtomicLong now = new AtomicLong(0);
		LongSupplier clock = now::get;
		ClientCache<String, String> cache = new ClientCache<>(10, 1_000, clock);

		cache.put("a", "A", null, null); // expires at 1000
		assertTrue(cache.get("a").isPresent(), "fresh within TTL");

		now.set(999);
		assertTrue(cache.get("a").isPresent(), "still fresh just before TTL");

		now.set(1_000);
		assertTrue(cache.get("a").isEmpty(), "expired at TTL boundary");

		// The entry (and its validators) survive expiry for P2-6 revalidation.
		assertTrue(cache.lookup("a").isPresent(), "expired entry retained for revalidation");
	}

	@Test
	void zeroTtlNeverExpires() {
		AtomicLong now = new AtomicLong(0);
		ClientCache<String, String> cache = new ClientCache<>(10, 0, now::get);
		cache.put("a", "A", null, null);

		now.set(Long.MAX_VALUE / 2);
		assertTrue(cache.get("a").isPresent(), "ttl 0 means no expiry");
	}

	@Test
	void keysListsAllEntries() {
		ClientCache<String, String> cache = new ClientCache<>(10, 0);
		cache.put("a", "A", null, null);
		cache.put("b", "B", null, null);
		assertEquals(List.of("a", "b"), List.copyOf(cache.keys()));
	}

	@Test
	void expiredEntryIsNotReturnedButKeyRemainsForWatcher() {
		AtomicLong now = new AtomicLong(0);
		ClientCache<String, String> cache = new ClientCache<>(10, 100, now::get);
		cache.put("a", "A", "etag", null);

		now.set(200);
		assertFalse(cache.get("a").isPresent());
		assertTrue(cache.keys().contains("a"), "watcher can still see the key to revalidate it");
	}
}
