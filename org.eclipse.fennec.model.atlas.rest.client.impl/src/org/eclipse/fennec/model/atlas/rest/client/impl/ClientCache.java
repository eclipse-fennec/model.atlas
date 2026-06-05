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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.LongSupplier;

/**
 * Bounded, TTL-aware in-memory cache (P2-5), internal to {@code rest.client.impl}.
 * <p>
 * Keyed generically — by {@code nsUri} for EPackages, and (Phase 5) by a
 * {@code (scope, registry, objectId)} key for EObjects. Each entry stores the
 * deserialized value plus the last-seen {@code ETag} / {@code Last-Modified}
 * (used by the conditional-GET revalidation in P2-6); these validators are kept
 * here and are never exposed through {@code rest.client.api}.
 * <p>
 * Eviction: least-recently-used beyond {@code cache.max.entries}
 * ({@code <= 0} = unbounded) and time-based via {@code cache.ttl.ms}
 * ({@code 0} = no TTL). The clock is injectable for testing. Thread-safe.
 *
 * @param <K> the key type
 * @param <V> the cached value type
 */
final class ClientCache<K, V> {

	/** A cached value with its HTTP validators and expiry. */
	static final class Entry<V> {
		private final V value;
		private final String etag;
		private final String lastModified;
		private final long expiresAtMillis;

		Entry(V value, String etag, String lastModified, long expiresAtMillis) {
			this.value = value;
			this.etag = etag;
			this.lastModified = lastModified;
			this.expiresAtMillis = expiresAtMillis;
		}

		V value() {
			return value;
		}

		String etag() {
			return etag;
		}

		String lastModified() {
			return lastModified;
		}

		boolean isExpired(long nowMillis) {
			return expiresAtMillis != 0L && nowMillis >= expiresAtMillis;
		}
	}

	private final long ttlMillis;
	private final LongSupplier clock;
	private final LruMap<K, Entry<V>> map;

	ClientCache(int maxEntries, long ttlMillis) {
		this(maxEntries, ttlMillis, System::currentTimeMillis);
	}

	ClientCache(int maxEntries, long ttlMillis, LongSupplier clock) {
		this.ttlMillis = ttlMillis;
		this.clock = clock;
		this.map = new LruMap<>(maxEntries);
	}

	/** The live value, or empty if absent or past its TTL. */
	synchronized Optional<V> get(K key) {
		Entry<V> entry = map.get(key);
		if (entry == null || entry.isExpired(clock.getAsLong())) {
			return Optional.empty();
		}
		return Optional.of(entry.value);
	}

	/**
	 * The raw entry regardless of expiry — including its validators — for
	 * conditional-GET revalidation (P2-6). Empty only if the key is absent.
	 */
	synchronized Optional<Entry<V>> lookup(K key) {
		return Optional.ofNullable(map.get(key));
	}

	/** Store/replace a value with its validators, (re)starting its TTL. */
	synchronized void put(K key, V value, String etag, String lastModified) {
		long expiresAt = ttlMillis > 0L ? clock.getAsLong() + ttlMillis : 0L;
		map.put(key, new Entry<>(value, etag, lastModified, expiresAt));
	}

	synchronized void invalidate(K key) {
		map.remove(key);
	}

	/** A snapshot of the current keys (most-recently-used last), for the drift watcher. */
	synchronized Set<K> keys() {
		return new LinkedHashSet<>(map.keySet());
	}

	synchronized int size() {
		return map.size();
	}

	synchronized void clear() {
		map.clear();
	}

	/** Access-ordered map that evicts the least-recently-used entry beyond the cap. */
	private static final class LruMap<K, V> extends LinkedHashMap<K, V> {

		private static final long serialVersionUID = 1L;

		private final int maxEntries;

		LruMap(int maxEntries) {
			super(16, 0.75f, true);
			this.maxEntries = maxEntries;
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
			return maxEntries > 0 && size() > maxEntries;
		}
	}
}
