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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link ConcurrentPackageRegistry} behaves like EMF's own {@link EPackageRegistryImpl} as a
 * {@link Map} - {@code null} keys and values included - while staying thread-safe (#347).
 */
class ConcurrentPackageRegistryTest {

	private static final String NS = "http://example.org/registry/1.0";

	/** A registry whose look-ups are irrelevant here; only its map is under test. */
	private static ConcurrentPackageRegistry registry() {
		return new ConcurrentPackageRegistry() {

			@Override
			public EPackage getEPackage(String nsURI) {
				return (EPackage) get(nsURI);
			}

			@Override
			public EFactory getEFactory(String nsURI) {
				return null;
			}
		};
	}

	private static EPackage pkg() {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setNsURI(NS);
		return ePackage;
	}

	/** Applies {@code operations} to this registry and to EMF's, and compares the maps after. */
	private static void sameAsEmf(Consumer<Map<String, Object>> operations) {
		ConcurrentPackageRegistry ours = registry();
		EPackageRegistryImpl emf = new EPackageRegistryImpl();
		operations.accept(ours);
		operations.accept(emf);
		assertEquals(new HashMap<>(emf), new HashMap<>(ours), "same entries as EMF's registry");
		assertEquals(emf.size(), ours.size());
		assertEquals(emf.isEmpty(), ours.isEmpty());
		assertEquals(emf, ours, "Map.equals across both implementations");
		assertEquals(emf.hashCode(), ours.hashCode());
	}

	@Test
	@DisplayName("The null key is looked up, stored and removed as in EMF's registry")
	void nullKey() {
		ConcurrentPackageRegistry registry = registry();
		assertNull(registry.get(null), "an absent null key reads as absent, it does not throw");
		assertFalse(registry.containsKey(null));
		assertNull(registry.getEPackage(null));
		assertNull(registry.remove(null));

		EPackage ePackage = pkg();
		assertNull(registry.put(null, ePackage));
		assertSame(ePackage, registry.get(null));
		assertTrue(registry.containsKey(null));
		assertEquals(1, registry.size());
		assertSame(ePackage, registry.remove(null));
		assertTrue(registry.isEmpty());

		sameAsEmf(map -> {
			map.put(null, "x");
			map.put(NS, "y");
			map.put(null, "z");
		});
	}

	@Test
	@DisplayName("A null value is stored and read back as in EMF's registry")
	void nullValue() {
		ConcurrentPackageRegistry registry = registry();
		assertNull(registry.put(NS, null));
		assertTrue(registry.containsKey(NS), "a key mapped to null is present");
		assertNull(registry.get(NS));
		assertTrue(registry.containsValue(null));
		assertEquals(1, registry.size());

		sameAsEmf(map -> {
			map.put(NS, null);
			map.put(null, null);
		});
	}

	@Test
	@DisplayName("putIfAbsent treats a key mapped to null as absent, as Map specifies")
	void putIfAbsent() {
		EPackage ePackage = pkg();
		ConcurrentPackageRegistry registry = registry();
		assertNull(registry.putIfAbsent(NS, ePackage));
		assertSame(ePackage, registry.putIfAbsent(NS, "other"), "the first value wins");

		registry.put("mappedToNull", null);
		assertNull(registry.putIfAbsent("mappedToNull", ePackage));
		assertSame(ePackage, registry.get("mappedToNull"));

		assertNull(registry.putIfAbsent(null, ePackage));
		assertSame(ePackage, registry.putIfAbsent(null, "other"));

		sameAsEmf(map -> {
			map.putIfAbsent(NS, "a");
			map.putIfAbsent(NS, "b");
			map.put("n", null);
			map.putIfAbsent("n", "c");
			map.putIfAbsent(null, "d");
			map.putIfAbsent(null, "e");
		});
	}

	@Test
	@DisplayName("Conditional removal and the views behave as in EMF's registry")
	void removalAndViews() {
		sameAsEmf(map -> {
			map.put(NS, "a");
			map.put("b", "b");
			map.put(null, "n");
			map.remove(NS, "not a");
			map.remove("b", "b");
			map.remove(null, "n");
		});
		sameAsEmf(map -> {
			map.put(NS, "a");
			map.put(null, "n");
			map.put("c", null);
			map.keySet().remove(null);
			map.values().remove(null);
		});

		ConcurrentPackageRegistry registry = registry();
		registry.put(NS, "a");
		registry.put(null, "n");
		Iterator<Map.Entry<String, Object>> iterator = registry.entrySet().iterator();
		assertThrows(IllegalStateException.class, iterator::remove, "remove before next");
		while (iterator.hasNext()) {
			Map.Entry<String, Object> entry = iterator.next();
			if (entry.getKey() == null) {
				entry.setValue("written through");
			} else {
				iterator.remove();
			}
		}
		assertEquals(Map.of(), Map.copyOf(withoutNullKey(registry)), "the removal went through");
		assertEquals("written through", registry.get(null), "setValue went through");
		registry.clear();
		assertTrue(registry.isEmpty());
		assertFalse(registry.containsKey(null));
	}

	@Test
	@DisplayName("Concurrent putIfAbsent on one key stores exactly one value")
	void putIfAbsentIsAtomic() throws Exception {
		for (String key : new String[] { NS, null }) {
			ConcurrentPackageRegistry registry = registry();
			int threads = 16;
			ExecutorService executor = Executors.newFixedThreadPool(threads);
			CountDownLatch start = new CountDownLatch(1);
			try {
				Future<?>[] futures = new Future<?>[threads];
				Object[] values = new Object[threads];
				Object[] answers = new Object[threads];
				for (int i = 0; i < threads; i++) {
					int index = i;
					values[i] = new Object();
					futures[i] = executor.submit(() -> {
						start.await();
						answers[index] = registry.putIfAbsent(key, values[index]);
						return null;
					});
				}
				start.countDown();
				for (Future<?> future : futures) {
					future.get(10, TimeUnit.SECONDS);
				}
				Object winner = registry.get(key);
				int stored = 0;
				for (int i = 0; i < threads; i++) {
					if (answers[i] == null) {
						stored++;
						assertSame(winner, values[i], "the one that stored is the value held");
					} else {
						assertSame(winner, answers[i], "every other caller gets the winner back");
					}
				}
				assertEquals(1, stored, "exactly one caller stores for key " + key);
			} finally {
				executor.shutdownNow();
			}
		}
	}

	private static Map<String, Object> withoutNullKey(Map<String, Object> map) {
		Map<String, Object> copy = new HashMap<>(map);
		copy.remove(null);
		return copy;
	}
}
