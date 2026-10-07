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
package org.eclipse.fennec.model.atlas.rest.client.impl.spi;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.emf.ecore.EPackage;

/**
 * The map behind the client's package registries: thread-safe, and with the {@code null}
 * semantics of EMF's own registry.
 * <p>
 * EMF's {@code EPackageRegistryImpl} is a {@code HashMap}, so code written against an
 * {@link EPackage.Registry} may use {@code null} as a key or a value - EMF itself looks up
 * the {@code null} namespace while parsing, and a registry delegating to another one passes
 * such a look-up straight through. A {@link ConcurrentHashMap} rejects both with a
 * {@code NullPointerException}, which is what the client's registries used to do (#347). This
 * map accepts both, like a {@code HashMap}, while keeping the {@code ConcurrentHashMap}'s
 * thread safety; {@link #putIfAbsent} stays atomic.
 * <p>
 * Subclasses implement the look-ups ({@link #getEPackage}, {@link #getEFactory}); the entries
 * they put are what those look-ups find on a second request.
 */
public abstract class ConcurrentPackageRegistry extends AbstractMap<String, Object> implements EPackage.Registry {

	/** Stored in place of a {@code null} value, which the backing map cannot hold. */
	private static final Object NULL_VALUE = new Object();

	private final ConcurrentHashMap<String, Object> entries = new ConcurrentHashMap<>();
	/** The (masked) value of the {@code null} key, {@code null} while the key is absent. */
	private final AtomicReference<Object> nullKey = new AtomicReference<>();

	private static Object mask(Object value) {
		return value == null ? NULL_VALUE : value;
	}

	private static Object unmask(Object stored) {
		return stored == NULL_VALUE ? null : stored;
	}

	@Override
	public Object get(Object key) {
		return unmask(key == null ? nullKey.get() : entries.get(key));
	}

	@Override
	public boolean containsKey(Object key) {
		return key == null ? nullKey.get() != null : entries.containsKey(key);
	}

	@Override
	public Object put(String key, Object value) {
		return unmask(key == null ? nullKey.getAndSet(mask(value)) : entries.put(key, mask(value)));
	}

	/**
	 * Atomic, as on a {@link ConcurrentHashMap}: of two concurrent calls for one key exactly
	 * one stores its value and the other gets that value back. As {@link Map#putIfAbsent}
	 * specifies, a key mapped to {@code null} counts as absent.
	 */
	@Override
	public Object putIfAbsent(String key, Object value) {
		Object stored = mask(value);
		if (key == null) {
			return unmask(nullKey.getAndUpdate(current -> current == null || current == NULL_VALUE ? stored : current));
		}
		Object previous = entries.putIfAbsent(key, stored);
		if (previous == NULL_VALUE && entries.replace(key, NULL_VALUE, stored)) {
			return null;
		}
		return unmask(previous);
	}

	@Override
	public Object remove(Object key) {
		return unmask(key == null ? nullKey.getAndSet(null) : entries.remove(key));
	}

	@Override
	public boolean remove(Object key, Object value) {
		Object stored = mask(value);
		if (key == null) {
			Object current = nullKey.get();
			return current != null && Objects.equals(current, stored) && nullKey.compareAndSet(current, null);
		}
		return entries.remove(key, stored);
	}

	@Override
	public int size() {
		return entries.size() + (nullKey.get() != null ? 1 : 0);
	}

	@Override
	public boolean isEmpty() {
		return nullKey.get() == null && entries.isEmpty();
	}

	@Override
	public void clear() {
		nullKey.set(null);
		entries.clear();
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return new EntrySet();
	}

	/**
	 * A live, weakly consistent view, like a {@link ConcurrentHashMap}'s: the {@code null} key
	 * first, then the backing entries; removal through the iterator and
	 * {@link Entry#setValue} write through.
	 */
	private final class EntrySet extends AbstractSet<Entry<String, Object>> {

		@Override
		public int size() {
			return ConcurrentPackageRegistry.this.size();
		}

		@Override
		public void clear() {
			ConcurrentPackageRegistry.this.clear();
		}

		@Override
		public Iterator<Entry<String, Object>> iterator() {
			Iterator<Entry<String, Object>> backing = entries.entrySet().iterator();
			Object nullValue = nullKey.get();
			return new Iterator<>() {

				private boolean nullPending = nullValue != null;
				private String lastKey;
				private boolean canRemove;

				@Override
				public boolean hasNext() {
					return nullPending || backing.hasNext();
				}

				@Override
				public Entry<String, Object> next() {
					if (nullPending) {
						nullPending = false;
						lastKey = null;
						canRemove = true;
						return new WriteThroughEntry(null, unmask(nullValue));
					}
					if (!backing.hasNext()) {
						throw new NoSuchElementException();
					}
					Entry<String, Object> entry = backing.next();
					lastKey = entry.getKey();
					canRemove = true;
					return new WriteThroughEntry(lastKey, unmask(entry.getValue()));
				}

				@Override
				public void remove() {
					if (!canRemove) {
						throw new IllegalStateException();
					}
					canRemove = false;
					ConcurrentPackageRegistry.this.remove(lastKey);
				}
			};
		}
	}

	private final class WriteThroughEntry extends SimpleEntry<String, Object> {

		private static final long serialVersionUID = 1L;

		WriteThroughEntry(String key, Object value) {
			super(key, value);
		}

		@Override
		public Object setValue(Object value) {
			put(getKey(), value);
			return super.setValue(value);
		}
	}
}
