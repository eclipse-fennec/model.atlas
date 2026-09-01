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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.model.atlas.rest.client.api.ResolvedEPackage;
import org.eclipse.fennec.model.atlas.rest.client.api.TransportException;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the P3-9 drift listener. The "is it ours", resolver, republish and
 * unpublish seams are all injected, so no OSGi framework or publisher is needed.
 */
class DriftSubstitutionTest {

	private static final String NS = "urn:test:drift";

	private final Set<String> published = new HashSet<>();
	private final List<String> republished = new ArrayList<>();
	private final List<String> unpublished = new ArrayList<>();
	private final List<String> adopted = new ArrayList<>();

	private static EPackage ePackage(String nsUri) {
		EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setName("p");
		pkg.setNsPrefix("p");
		pkg.setNsURI(nsUri);
		return pkg;
	}

	private static ResolvedEPackage resolved(String nsUri, String version) {
		return new ResolvedEPackage(ePackage(nsUri), nsUri, "atlas", "schema", "released", version);
	}

	private DriftSubstitution substitution(Function<String, Optional<ResolvedEPackage>> resolver) {
		PackagePublication republisher = (ePackage, scope, stage, version, serverFingerprint) -> {
			republished.add(ePackage.getNsURI());
			return true;
		};
		return new DriftSubstitution(published::contains, () -> published, resolver, republisher, unpublished::add);
	}

	/** A substitution that adopts additions, gated by {@code wantsAddition} (issue #228). */
	private DriftSubstitution adopting(Function<String, Optional<ResolvedEPackage>> resolver,
			java.util.function.Predicate<String> wantsAddition) {
		PackagePublication republisher = (ePackage, scope, stage, version, serverFingerprint) -> {
			republished.add(ePackage.getNsURI());
			return true;
		};
		PackagePublication adopter = (ePackage, scope, stage, version, serverFingerprint) -> {
			adopted.add(ePackage.getNsURI());
			return true;
		};
		return new DriftSubstitution(published::contains, () -> published, resolver, republisher, unpublished::add,
				wantsAddition, adopter);
	}

	@Test
	void additionIsPublishedThroughTheGate_notTheRepublishPath() {
		// #228: nothing is published under NS yet, so this must go through the adopter
		// (the local-first gate), never through republish, which bypasses suppression.
		adopting(ns -> Optional.of(resolved(ns, "1.0")), ns -> true).onPackageAdded(NS, ePackage(NS));

		assertEquals(List.of(NS), adopted);
		assertTrue(republished.isEmpty());
		assertTrue(unpublished.isEmpty());
	}

	@Test
	void additionIsIgnoredWhenTheModeDoesNotWantIt() {
		// LAZY, or HYBRID with an nsURI outside eager.nsuri.allow.list.
		adopting(ns -> Optional.of(resolved(ns, "1.0")), ns -> false).onPackageAdded(NS, ePackage(NS));

		assertTrue(adopted.isEmpty());
	}

	@Test
	void additionOfAnAlreadyPublishedNsUriIsIgnored() {
		// Raced with the EAGER pre-fetch: it is already ours, so a change (not an add).
		published.add(NS);

		adopting(ns -> Optional.of(resolved(ns, "1.0")), ns -> true).onPackageAdded(NS, ePackage(NS));

		assertTrue(adopted.isEmpty());
	}

	@Test
	void additionThatVanishesBeforeResolveIsNotPublished() {
		adopting(ns -> Optional.empty(), ns -> true).onPackageAdded(NS, ePackage(NS));

		assertTrue(adopted.isEmpty());
		assertTrue(unpublished.isEmpty());
	}

	@Test
	void defaultConstructorAdoptsNothing() {
		// The 5-arg constructor keeps the pre-#228 behaviour for existing callers.
		substitution(ns -> Optional.of(resolved(ns, "1.0"))).onPackageAdded(NS, ePackage(NS));

		assertTrue(republished.isEmpty());
		assertTrue(adopted.isEmpty());
	}

	@Test
	void heldNsUris_reportsThePublishedSet() {
		// A published service can outlive its provider-cache entry; reporting the
		// published set keeps such packages visible to the drift watcher's gate.
		published.add("ns1");
		DriftSubstitution substitution = substitution(ns -> Optional.empty());

		assertEquals(published, substitution.heldNsUris());
	}

	@Test
	void changeOfAPublishedPackageReResolvesAndRepublishes() {
		published.add(NS);

		substitution(ns -> Optional.of(resolved(ns, "2.0"))).onPackageChanged(NS, ePackage(NS));

		assertEquals(List.of(NS), republished);
		assertTrue(unpublished.isEmpty());
	}

	@Test
	void changeOfANonPublishedPackageIsIgnored() {
		// NS not in `published` (e.g. suppressed by a local) → nothing to swap.
		substitution(ns -> Optional.of(resolved(ns, "2.0"))).onPackageChanged(NS, ePackage(NS));

		assertTrue(republished.isEmpty());
		assertTrue(unpublished.isEmpty());
	}

	@Test
	void changedThenGoneUnpublishes() {
		published.add(NS);

		substitution(ns -> Optional.empty()).onPackageChanged(NS, ePackage(NS));

		assertTrue(republished.isEmpty());
		assertEquals(List.of(NS), unpublished);
	}

	@Test
	void transportErrorOnReResolveLeavesTheCurrentPublication() {
		published.add(NS);
		Function<String, Optional<ResolvedEPackage>> resolver = ns -> {
			throw new TransportException("read timed out");
		};

		substitution(resolver).onPackageChanged(NS, ePackage(NS));

		assertTrue(republished.isEmpty(), "no swap on a transport failure");
		assertTrue(unpublished.isEmpty(), "and the current publication is left in place");
	}

	@Test
	void removalUnpublishes() {
		substitution(ns -> Optional.empty()).onPackageRemoved(NS);

		assertEquals(List.of(NS), unpublished);
		assertTrue(republished.isEmpty());
	}
}
