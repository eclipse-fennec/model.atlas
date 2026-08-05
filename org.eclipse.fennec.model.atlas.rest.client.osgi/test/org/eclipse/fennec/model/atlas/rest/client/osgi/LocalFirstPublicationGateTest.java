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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the P3-7 local-first gate. Uses a mutable {@code Set} as the
 * local-presence oracle and a hand-driven {@link LocalFirstPublicationGate.Scheduler}
 * so the debounce fires (or is cancelled) deterministically, with no OSGi framework.
 */
class LocalFirstPublicationGateTest {

	private static final String NS = "urn:test:lf";

	/** Records publish/unpublish calls; publish always "succeeds". */
	private static final class RecordingPublisher implements PackagePublication {
		final List<String> published = new ArrayList<>();

		@Override
		public boolean publish(EPackage ePackage, String scope, String stage, String version,
				String serverFingerprint) {
			published.add(ePackage.getNsURI());
			return true;
		}
	}

	/** Captures scheduled tasks so the test decides when (and whether) they run. */
	private static final class FakeScheduler implements LocalFirstPublicationGate.Scheduler {
		private record Task(Runnable runnable, boolean[] cancelled) {
		}

		final List<Task> tasks = new ArrayList<>();

		@Override
		public AutoCloseable schedule(Runnable task, long delayMs) {
			boolean[] cancelled = { false };
			tasks.add(new Task(task, cancelled));
			return () -> cancelled[0] = true;
		}

		/** Run every still-pending (non-cancelled) task, as the real executor eventually would. */
		void fire() {
			for (Task task : List.copyOf(tasks)) {
				if (!task.cancelled()[0]) {
					task.runnable().run();
				}
			}
		}
	}

	private RecordingPublisher publisher;
	private List<String> unpublished;
	private Set<String> locals;
	private FakeScheduler scheduler;

	@BeforeEach
	void setUp() {
		publisher = new RecordingPublisher();
		unpublished = new ArrayList<>();
		locals = new HashSet<>();
		scheduler = new FakeScheduler();
	}

	private static EPackage ePackage(String nsUri) {
		EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setName("p");
		pkg.setNsPrefix("p");
		pkg.setNsURI(nsUri);
		return pkg;
	}

	private LocalFirstPublicationGate gate(boolean forceRemote) {
		return new LocalFirstPublicationGate(publisher, unpublished::add, locals::contains, forceRemote, scheduler, 500L);
	}

	private boolean publish(LocalFirstPublicationGate gate) {
		return gate.publish(ePackage(NS), "jena", "released", "1.0");
	}

	@Test
	void publishesWhenNoLocalExists() {
		LocalFirstPublicationGate gate = gate(false);

		assertTrue(publish(gate));

		assertEquals(List.of(NS), publisher.published);
		assertTrue(gate.isPublished(NS));
		assertFalse(gate.isParked(NS));
	}

	@Test
	void suppressesWhenLocalExists() {
		locals.add(NS); // a local EPackage already provides this nsURI
		LocalFirstPublicationGate gate = gate(false);

		assertFalse(publish(gate));

		assertTrue(publisher.published.isEmpty(), "remote must not be published while a local exists");
		assertTrue(gate.isParked(NS));
		assertFalse(gate.isPublished(NS));
	}

	@Test
	void withdrawsWhenALocalAppearsForAPackageWePublished() {
		LocalFirstPublicationGate gate = gate(false);
		publish(gate); // published (no local yet)

		locals.add(NS);
		gate.onLocalAppeared(NS);

		assertEquals(List.of(NS), unpublished, "our remote must be withdrawn");
		assertTrue(gate.isParked(NS));
		assertFalse(gate.isPublished(NS));
	}

	@Test
	void republishesAfterDebounceWhenLocalDisappears() {
		locals.add(NS);
		LocalFirstPublicationGate gate = gate(false);
		publish(gate); // suppressed → parked

		locals.remove(NS);
		gate.onLocalDisappeared(NS);
		assertTrue(publisher.published.isEmpty(), "must wait for the debounce, not publish immediately");

		scheduler.fire(); // debounce elapses, local still gone

		assertEquals(List.of(NS), publisher.published);
		assertTrue(gate.isPublished(NS));
		assertFalse(gate.isParked(NS));
	}

	@Test
	void doesNotFlapWhenLocalBrieflyDisappearsAndReturns() {
		locals.add(NS);
		LocalFirstPublicationGate gate = gate(false);
		publish(gate); // suppressed → parked

		gate.onLocalDisappeared(NS); // schedules a debounced republish
		gate.onLocalAppeared(NS); // local returns within the window → cancels it

		scheduler.fire(); // the cancelled task must not run

		assertTrue(publisher.published.isEmpty(), "no transient remote publication");
		assertTrue(gate.isParked(NS));
		assertFalse(gate.isPublished(NS));
	}

	@Test
	void debounceFiringWhileLocalIsBackKeepsSuppressed() {
		locals.add(NS);
		LocalFirstPublicationGate gate = gate(false);
		publish(gate); // parked

		gate.onLocalDisappeared(NS); // schedules
		locals.add(NS); // local is present again by the time the task fires (no appear event)
		scheduler.fire();

		assertTrue(publisher.published.isEmpty(), "re-check at fire time keeps it suppressed");
		assertTrue(gate.isParked(NS));
	}

	@Test
	void forceRemotePublishesEvenWhenLocalExistsAndIgnoresLocalEvents() {
		locals.add(NS);
		LocalFirstPublicationGate gate = gate(true);

		assertTrue(publish(gate));
		assertEquals(List.of(NS), publisher.published);
		assertTrue(gate.isPublished(NS));

		// A local appearing must not withdraw a force.remote publication.
		gate.onLocalAppeared(NS);
		assertTrue(unpublished.isEmpty());
		assertTrue(gate.isPublished(NS));
	}
}
