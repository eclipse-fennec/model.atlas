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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.model.atlas.rest.client.api.DriftListener;
import org.eclipse.fennec.model.atlas.rest.client.api.DriftReport;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

/**
 * P2-7 — scope-level drift watcher. The {@code HEAD /scopes/{scope}} transport is
 * mocked; the provider is mocked so the watcher's cache-invalidation / event
 * logic is exercised without a server.
 */
class DriftWatcherTest {

	private final WebTarget target = mock(WebTarget.class);
	private final Invocation.Builder request = mock(Invocation.Builder.class);
	private final RemoteEPackageProviderImpl provider = mock(RemoteEPackageProviderImpl.class);

	DriftWatcherTest() {
		when(target.path(anyString())).thenReturn(target);
		when(target.request()).thenReturn(request);
		when(request.header(anyString(), any())).thenReturn(request);
	}

	private DriftWatcher watcher() {
		return watcher(s -> null); // no read-only scope view by default (package-drift tests)
	}

	private DriftWatcher watcher(java.util.function.Function<String, RemoteReadableScopeService> scopeServiceLookup) {
		// interval 0 → no schedule; we drive check() manually.
		return new DriftWatcher(target, () -> List.of("jena"), () -> provider, scopeServiceLookup, 0);
	}

	/** A watcher that reads the feed as a discovery feed too (EAGER/HYBRID clients). */
	private DriftWatcher discoveringWatcher() {
		return new DriftWatcher(target, () -> List.of("jena"), () -> provider, s -> null, 0, true);
	}

	private static Response headResponse(int status, Response.Status statusInfo, String etag, String changedNsUris) {
		Response r = mock(Response.class);
		when(r.getStatus()).thenReturn(status);
		when(r.getStatusInfo()).thenReturn(statusInfo);
		if (etag != null) {
			when(r.getHeaderString("ETag")).thenReturn(etag);
		}
		if (changedNsUris != null) {
			when(r.getHeaderString(DriftWatcher.ATLAS_CHANGED_NSURIS)).thenReturn(changedNsUris);
		}
		return r;
	}

	private static EPackage pkg(String nsUri) {
		EPackage p = EcoreFactory.eINSTANCE.createEPackage();
		p.setNsURI(nsUri);
		return p;
	}

	private static class RecordingListener implements DriftListener {
		final java.util.List<String> added = new java.util.ArrayList<>();
		final java.util.List<String> changed = new java.util.ArrayList<>();
		final java.util.List<String> removed = new java.util.ArrayList<>();
		final java.util.List<String> objectsChanged = new java.util.ArrayList<>();
		final java.util.List<String> objectsRemoved = new java.util.ArrayList<>();

		@Override
		public void onPackageAdded(String nsUri, EPackage newPackage) {
			added.add(nsUri);
		}

		@Override
		public void onPackageChanged(String nsUri, EPackage newPackage) {
			changed.add(nsUri);
		}

		@Override
		public void onPackageRemoved(String nsUri) {
			removed.add(nsUri);
		}

		@Override
		public void onObjectChanged(String scope, String registry, String objectId) {
			objectsChanged.add(scope + "/" + registry + "/" + objectId);
		}

		@Override
		public void onObjectRemoved(String scope, String registry, String objectId) {
			objectsRemoved.add(scope + "/" + registry + "/" + objectId);
		}
	}

	@Test
	void firstCheckEstablishesBaseline_noEvents() {
		Response baseline = headResponse(200, Response.Status.OK, "\"s1\"", null);
		when(request.head()).thenReturn(baseline);
		RecordingListener listener = new RecordingListener();
		DriftWatcher watcher = watcher();
		watcher.addListener(listener);

		DriftReport report = watcher.check();

		assertFalse(report.hasChanges());
		assertTrue(listener.changed.isEmpty());
		verify(provider, never()).refresh(anyString());
	}

	@Test
	void notModified_noEvents() {
		// baseline first, then 304
		Response baseline = headResponse(200, Response.Status.OK, "\"s1\"", null);
		Response unchanged = headResponse(304, Response.Status.NOT_MODIFIED, null, null);
		when(request.head()).thenReturn(baseline, unchanged);
		RecordingListener listener = new RecordingListener();
		DriftWatcher watcher = watcher();
		watcher.addListener(listener);

		watcher.check(); // baseline
		DriftReport report = watcher.check(); // 304

		assertFalse(report.hasChanges());
		// the 304 round sends If-None-Match with the stored baseline etag
		verify(request).header("If-None-Match", "\"s1\"");
	}

	@Test
	void change_refreshesCachedEntry_andFiresOnPackageChanged() {
		Response baseline = headResponse(200, Response.Status.OK, "\"s1\"", null);
		Response changed = headResponse(200, Response.Status.OK, "\"s2\"", "ns1,ns2");
		when(request.head()).thenReturn(baseline, changed);
		when(provider.cachedNsUris()).thenReturn(Set.of("ns1")); // only ns1 is held locally
		when(provider.refresh("ns1")).thenReturn(Optional.of(pkg("ns1")));
		RecordingListener listener = new RecordingListener();
		DriftWatcher watcher = watcher();
		watcher.addListener(listener);

		watcher.check(); // baseline
		DriftReport report = watcher.check(); // change

		assertEquals(List.of("ns1"), report.getChangedNsUris());
		assertEquals(List.of("ns1"), listener.changed);
		assertTrue(report.getRemovedNsUris().isEmpty());
		verify(provider).refresh("ns1");
		verify(provider, never()).refresh("ns2"); // ns2 not cached → ignored
	}

	@Test
	void newPackage_notHeld_isDiscoveredAndAnnounced() {
		// issue #228: a package published+promoted after start-up is named by the server's
		// Atlas-Changed-NsUris, but the client holds nothing under it yet.
		Response baseline = headResponse(200, Response.Status.OK, "\"s1\"", null);
		Response changed = headResponse(200, Response.Status.OK, "\"s2\"", "nsNew");
		when(request.head()).thenReturn(baseline, changed);
		when(provider.cachedNsUris()).thenReturn(Set.of()); // nothing held
		when(provider.refresh("nsNew")).thenReturn(Optional.of(pkg("nsNew")));
		RecordingListener listener = new RecordingListener();
		DriftWatcher watcher = discoveringWatcher();
		watcher.addListener(listener);

		watcher.check(); // baseline
		DriftReport report = watcher.check(); // the new package appears

		assertEquals(List.of("nsNew"), report.getAddedNsUris());
		assertEquals(List.of("nsNew"), listener.added);
		assertTrue(report.getChangedNsUris().isEmpty());
		assertTrue(report.getRemovedNsUris().isEmpty());
		assertTrue(listener.removed.isEmpty());
		assertTrue(report.hasChanges());
	}

	@Test
	void newNsUri_notResolvableYet_isSilentlyIgnored() {
		// The server's diff spans every stage, so a draft-only publish is named here long
		// before a stage-free read can serve it. That is not an addition — and emphatically
		// not a removal: we never held it, so there is nothing for a listener to revoke.
		Response baseline = headResponse(200, Response.Status.OK, "\"s1\"", null);
		Response changed = headResponse(200, Response.Status.OK, "\"s2\"", "nsDraftOnly");
		when(request.head()).thenReturn(baseline, changed);
		when(provider.cachedNsUris()).thenReturn(Set.of());
		when(provider.refresh("nsDraftOnly")).thenReturn(Optional.empty());
		RecordingListener listener = new RecordingListener();
		DriftWatcher watcher = discoveringWatcher();
		watcher.addListener(listener);

		watcher.check();
		DriftReport report = watcher.check();

		assertFalse(report.hasChanges());
		assertTrue(listener.added.isEmpty());
		assertTrue(listener.removed.isEmpty());
	}

	@Test
	void discoveryOff_leavesUnheldNsUrisAlone() {
		// A LAZY client fetches on demand; the feed stays a pure invalidation signal.
		Response baseline = headResponse(200, Response.Status.OK, "\"s1\"", null);
		Response changed = headResponse(200, Response.Status.OK, "\"s2\"", "nsNew");
		when(request.head()).thenReturn(baseline, changed);
		when(provider.cachedNsUris()).thenReturn(Set.of());
		RecordingListener listener = new RecordingListener();
		DriftWatcher watcher = watcher();
		watcher.addListener(listener);

		watcher.check();
		DriftReport report = watcher.check();

		assertFalse(report.hasChanges());
		assertTrue(listener.added.isEmpty());
		verify(provider, never()).refresh("nsNew");
	}

	@Test
	void discovery_separatesAdditionsFromChanges() {
		Response baseline = headResponse(200, Response.Status.OK, "\"s1\"", null);
		Response changed = headResponse(200, Response.Status.OK, "\"s2\"", "nsHeld,nsNew");
		when(request.head()).thenReturn(baseline, changed);
		when(provider.cachedNsUris()).thenReturn(Set.of("nsHeld"));
		when(provider.refresh("nsHeld")).thenReturn(Optional.of(pkg("nsHeld")));
		when(provider.refresh("nsNew")).thenReturn(Optional.of(pkg("nsNew")));
		RecordingListener listener = new RecordingListener();
		DriftWatcher watcher = discoveringWatcher();
		watcher.addListener(listener);

		watcher.check();
		DriftReport report = watcher.check();

		assertEquals(List.of("nsNew"), report.getAddedNsUris());
		assertEquals(List.of("nsHeld"), report.getChangedNsUris());
		assertEquals(List.of("nsNew"), listener.added);
		assertEquals(List.of("nsHeld"), listener.changed);
	}

	@Test
	void discoveryFailure_doesNotStarveTheRemainingNsUris() {
		Response baseline = headResponse(200, Response.Status.OK, "\"s1\"", null);
		Response changed = headResponse(200, Response.Status.OK, "\"s2\"", "nsBroken,nsHeld");
		when(request.head()).thenReturn(baseline, changed);
		when(provider.cachedNsUris()).thenReturn(Set.of("nsHeld"));
		when(provider.refresh("nsBroken")).thenThrow(new IllegalStateException("boom"));
		when(provider.refresh("nsHeld")).thenReturn(Optional.of(pkg("nsHeld")));
		RecordingListener listener = new RecordingListener();
		DriftWatcher watcher = discoveringWatcher();
		watcher.addListener(listener);

		watcher.check();
		DriftReport report = watcher.check();

		assertEquals(List.of("nsHeld"), report.getChangedNsUris());
		assertTrue(report.getAddedNsUris().isEmpty());
	}

	@Test
	void removal_firesOnPackageRemoved() {
		Response baseline = headResponse(200, Response.Status.OK, "\"s1\"", null);
		Response changed = headResponse(200, Response.Status.OK, "\"s2\"", "ns1");
		when(request.head()).thenReturn(baseline, changed);
		when(provider.cachedNsUris()).thenReturn(Set.of("ns1"));
		when(provider.refresh("ns1")).thenReturn(Optional.empty()); // gone on the server
		RecordingListener listener = new RecordingListener();
		DriftWatcher watcher = watcher();
		watcher.addListener(listener);

		watcher.check();
		DriftReport report = watcher.check();

		assertEquals(List.of("ns1"), report.getRemovedNsUris());
		assertEquals(List.of("ns1"), listener.removed);
		assertTrue(report.getChangedNsUris().isEmpty());
	}

	@Test
	void removedListenerNoLongerNotified() {
		Response baseline = headResponse(200, Response.Status.OK, "\"s1\"", null);
		Response changed = headResponse(200, Response.Status.OK, "\"s2\"", "ns1");
		when(request.head()).thenReturn(baseline, changed);
		when(provider.cachedNsUris()).thenReturn(Set.of("ns1"));
		when(provider.refresh("ns1")).thenReturn(Optional.of(pkg("ns1")));
		RecordingListener listener = new RecordingListener();
		DriftWatcher watcher = watcher();
		AutoCloseable handle = watcher.addListener(listener);

		watcher.check(); // baseline
		try {
			handle.close(); // unsubscribe
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		watcher.check(); // change — listener already removed

		assertTrue(listener.changed.isEmpty(), "unsubscribed listener must not be notified");
	}

	// ---- Bug B: nsURIs held only by a listener (stage-explicit fetches bypass
	// the provider cache) must still receive drift events -------------------

	/** A listener that holds an nsURI itself, like AtlasScopedFetchOnMissRegistry. */
	private static final class HoldingListener extends RecordingListener {
		private final Set<String> held;

		HoldingListener(Set<String> held) {
			this.held = held;
		}

		@Override
		public Set<String> heldNsUris() {
			return held;
		}
	}

	@Test
	void listenerHeldNsUri_notInProviderCache_removalStillFiresOnPackageRemoved() {
		Response baseline = headResponse(200, Response.Status.OK, "\"s1\"", null);
		Response changed = headResponse(200, Response.Status.OK, "\"s2\"", "ns-staged");
		when(request.head()).thenReturn(baseline, changed);
		// The provider cache never saw this package: it was fetched stage-explicitly
		// (getEPackageAtStage deliberately bypasses the cache).
		when(provider.cachedNsUris()).thenReturn(Set.of());
		when(provider.refresh("ns-staged")).thenReturn(Optional.empty()); // gone on the server
		HoldingListener listener = new HoldingListener(Set.of("ns-staged"));
		DriftWatcher watcher = watcher();
		watcher.addListener(listener);

		watcher.check(); // baseline
		DriftReport report = watcher.check(); // change

		assertEquals(List.of("ns-staged"), listener.removed,
				"a listener-held nsURI must receive the removal event even though the provider cache never held it");
		assertEquals(List.of("ns-staged"), report.getRemovedNsUris());
	}

	@Test
	void listenerHeldNsUri_notInProviderCache_changeStillFiresOnPackageChanged() {
		Response baseline = headResponse(200, Response.Status.OK, "\"s1\"", null);
		Response changed = headResponse(200, Response.Status.OK, "\"s2\"", "ns-staged");
		when(request.head()).thenReturn(baseline, changed);
		when(provider.cachedNsUris()).thenReturn(Set.of());
		when(provider.refresh("ns-staged")).thenReturn(Optional.of(pkg("ns-staged")));
		HoldingListener listener = new HoldingListener(Set.of("ns-staged"));
		DriftWatcher watcher = watcher();
		watcher.addListener(listener);

		watcher.check();
		DriftReport report = watcher.check();

		assertEquals(List.of("ns-staged"), listener.changed,
				"a listener-held nsURI must receive the change event even though the provider cache never held it");
		assertEquals(List.of("ns-staged"), report.getChangedNsUris());
	}

	@Test
	void oneScopeFailing_doesNotStarveTheOtherScopes() {
		// First scope's HEAD blows up on every check; the second scope must still be
		// probed and its drift events delivered (previously the whole check aborted).
		Response baseline = headResponse(200, Response.Status.OK, "\"s1\"", null);
		Response changed = headResponse(200, Response.Status.OK, "\"s2\"", "ns1");
		when(request.head())
				.thenThrow(new IllegalStateException("scope 'broken' unreachable")).thenReturn(baseline)
				.thenThrow(new IllegalStateException("scope 'broken' unreachable")).thenReturn(changed);
		when(provider.cachedNsUris()).thenReturn(Set.of("ns1"));
		when(provider.refresh("ns1")).thenReturn(Optional.empty());
		RecordingListener listener = new RecordingListener();
		DriftWatcher watcher = new DriftWatcher(target, () -> List.of("broken", "jena"), () -> provider, s -> null, 0);
		watcher.addListener(listener);

		watcher.check(); // broken throws, jena baselines
		DriftReport report = watcher.check(); // broken throws again, jena reports the removal

		assertEquals(List.of("ns1"), listener.removed, "the healthy scope's events must still be delivered");
		assertEquals(List.of("ns1"), report.getRemovedNsUris());
	}

	@Test
	void nsUriHeldNowhere_staysIgnored() {
		// The gate exists to avoid refetching the world: an nsURI neither cached nor
		// held by any listener must still be skipped.
		Response baseline = headResponse(200, Response.Status.OK, "\"s1\"", null);
		Response changed = headResponse(200, Response.Status.OK, "\"s2\"", "ns-foreign");
		when(request.head()).thenReturn(baseline, changed);
		when(provider.cachedNsUris()).thenReturn(Set.of());
		RecordingListener listener = new RecordingListener(); // holds nothing
		DriftWatcher watcher = watcher();
		watcher.addListener(listener);

		watcher.check();
		DriftReport report = watcher.check();

		assertFalse(report.hasChanges());
		verify(provider, never()).refresh(anyString());
	}

	// ---- P5-2: EObject drift (Atlas-Changed-Objects) ----------------------

	private static Response headWithObjects(String etag, String changedObjects) {
		Response r = headResponse(200, Response.Status.OK, etag, null);
		when(r.getHeaderString(DriftWatcher.ATLAS_CHANGED_OBJECTS)).thenReturn(changedObjects);
		return r;
	}

	@Test
	void objectChange_refreshesEntry_andFiresOnObjectChanged() {
		Response baseline = headResponse(200, Response.Status.OK, "\"s1\"", null);
		Response changed = headWithObjects("\"s2\"", "cocl/id1,cocl/id2");
		when(request.head()).thenReturn(baseline, changed);

		RemoteReadableScopeService service = mock(RemoteReadableScopeService.class);
		when(service.cachedObjects())
				.thenReturn(Set.of(new RemoteReadableScopeService.ObjectKey("jena", "cocl", null, "id1"))); // only id1 held
		when(service.refresh("cocl", null, "id1")).thenReturn(RemoteReadableScopeService.DriftOutcome.CHANGED);
		RecordingListener listener = new RecordingListener();
		DriftWatcher watcher = watcher(s -> "jena".equals(s) ? service : null);
		watcher.addListener(listener);

		watcher.check(); // baseline
		watcher.check(); // change

		assertEquals(List.of("jena/cocl/id1"), listener.objectsChanged);
		assertTrue(listener.objectsRemoved.isEmpty());
		verify(service).refresh("cocl", null, "id1");
		verify(service, never()).refresh("cocl", null, "id2"); // id2 not cached → ignored
	}

	@Test
	void objectRemoval_firesOnObjectRemoved() {
		Response baseline = headResponse(200, Response.Status.OK, "\"s1\"", null);
		Response changed = headWithObjects("\"s2\"", "cocl/id1");
		when(request.head()).thenReturn(baseline, changed);

		RemoteReadableScopeService service = mock(RemoteReadableScopeService.class);
		when(service.cachedObjects())
				.thenReturn(Set.of(new RemoteReadableScopeService.ObjectKey("jena", "cocl", null, "id1")));
		when(service.refresh("cocl", null, "id1")).thenReturn(RemoteReadableScopeService.DriftOutcome.REMOVED); // gone
		RecordingListener listener = new RecordingListener();
		DriftWatcher watcher = watcher(s -> "jena".equals(s) ? service : null);
		watcher.addListener(listener);

		watcher.check();
		watcher.check();

		assertEquals(List.of("jena/cocl/id1"), listener.objectsRemoved);
		assertTrue(listener.objectsChanged.isEmpty());
	}

	@Test
	void objectChange_stagedView_isRefreshed() {
		// The entry was fetched through a snapshot-bound view; pre-P6-5 drift ignored it.
		Response baseline = headResponse(200, Response.Status.OK, "\"s1\"", null);
		Response changed = headWithObjects("\"s2\"", "cocl/id1");
		when(request.head()).thenReturn(baseline, changed);

		RemoteReadableScopeService service = mock(RemoteReadableScopeService.class);
		when(service.cachedObjects())
				.thenReturn(Set.of(new RemoteReadableScopeService.ObjectKey("jena", "cocl", "snapshot", "id1")));
		when(service.refresh("cocl", "snapshot", "id1")).thenReturn(RemoteReadableScopeService.DriftOutcome.CHANGED);
		RecordingListener listener = new RecordingListener();
		DriftWatcher watcher = watcher(s -> "jena".equals(s) ? service : null);
		watcher.addListener(listener);

		watcher.check();
		watcher.check();

		assertEquals(List.of("jena/cocl/id1"), listener.objectsChanged);
		verify(service).refresh("cocl", "snapshot", "id1");
	}

	@Test
	void objectChange_multipleViews_oneChanged_firesOnceAndRevalidatesAll() {
		Response baseline = headResponse(200, Response.Status.OK, "\"s1\"", null);
		Response changed = headWithObjects("\"s2\"", "cocl/id1");
		when(request.head()).thenReturn(baseline, changed);

		RemoteReadableScopeService service = mock(RemoteReadableScopeService.class);
		when(service.cachedObjects()).thenReturn(Set.of(
				new RemoteReadableScopeService.ObjectKey("jena", "cocl", null, "id1"),
				new RemoteReadableScopeService.ObjectKey("jena", "cocl", "snapshot", "id1")));
		when(service.refresh("cocl", null, "id1")).thenReturn(RemoteReadableScopeService.DriftOutcome.UNCHANGED);
		when(service.refresh("cocl", "snapshot", "id1")).thenReturn(RemoteReadableScopeService.DriftOutcome.CHANGED);
		RecordingListener listener = new RecordingListener();
		DriftWatcher watcher = watcher(s -> "jena".equals(s) ? service : null);
		watcher.addListener(listener);

		watcher.check();
		watcher.check();

		assertEquals(List.of("jena/cocl/id1"), listener.objectsChanged, "one event despite two views");
		// Every held view of the object is revalidated, regardless of which one changed.
		verify(service).refresh("cocl", null, "id1");
		verify(service).refresh("cocl", "snapshot", "id1");
	}

	@Test
	void objectChange_allViewsUnchanged_firesNothing() {
		// A sibling stage changed (object reported), but none of the held views' content did → 304s.
		Response baseline = headResponse(200, Response.Status.OK, "\"s1\"", null);
		Response changed = headWithObjects("\"s2\"", "cocl/id1");
		when(request.head()).thenReturn(baseline, changed);

		RemoteReadableScopeService service = mock(RemoteReadableScopeService.class);
		when(service.cachedObjects())
				.thenReturn(Set.of(new RemoteReadableScopeService.ObjectKey("jena", "cocl", null, "id1")));
		when(service.refresh("cocl", null, "id1")).thenReturn(RemoteReadableScopeService.DriftOutcome.UNCHANGED);
		RecordingListener listener = new RecordingListener();
		DriftWatcher watcher = watcher(s -> "jena".equals(s) ? service : null);
		watcher.addListener(listener);

		watcher.check();
		watcher.check();

		assertTrue(listener.objectsChanged.isEmpty(), "an unchanged view must not fire onObjectChanged");
		assertTrue(listener.objectsRemoved.isEmpty());
	}

	@Test
	void objectRemoval_onlyWhenAllViewsGone() {
		// One view gone, another still present (unchanged) → the object still exists → no removal.
		Response baseline = headResponse(200, Response.Status.OK, "\"s1\"", null);
		Response changed = headWithObjects("\"s2\"", "cocl/id1");
		when(request.head()).thenReturn(baseline, changed);

		RemoteReadableScopeService service = mock(RemoteReadableScopeService.class);
		when(service.cachedObjects()).thenReturn(Set.of(
				new RemoteReadableScopeService.ObjectKey("jena", "cocl", null, "id1"),
				new RemoteReadableScopeService.ObjectKey("jena", "cocl", "snapshot", "id1")));
		when(service.refresh("cocl", "snapshot", "id1")).thenReturn(RemoteReadableScopeService.DriftOutcome.REMOVED);
		when(service.refresh("cocl", null, "id1")).thenReturn(RemoteReadableScopeService.DriftOutcome.UNCHANGED);
		RecordingListener listener = new RecordingListener();
		DriftWatcher watcher = watcher(s -> "jena".equals(s) ? service : null);
		watcher.addListener(listener);

		watcher.check();
		watcher.check();

		assertTrue(listener.objectsRemoved.isEmpty(), "object still present in another view → not removed");
		assertTrue(listener.objectsChanged.isEmpty());
	}

	@Test
	void objectChange_noReadOnlyView_isSkipped() {
		Response baseline = headResponse(200, Response.Status.OK, "\"s1\"", null);
		Response changed = headWithObjects("\"s2\"", "cocl/id1");
		when(request.head()).thenReturn(baseline, changed);
		RecordingListener listener = new RecordingListener();
		DriftWatcher watcher = watcher(s -> null); // consumer never opened a read-only view
		watcher.addListener(listener);

		watcher.check();
		watcher.check();

		assertTrue(listener.objectsChanged.isEmpty(), "no cached scope view → nothing to evict or fire");
		assertTrue(listener.objectsRemoved.isEmpty());
	}
}
