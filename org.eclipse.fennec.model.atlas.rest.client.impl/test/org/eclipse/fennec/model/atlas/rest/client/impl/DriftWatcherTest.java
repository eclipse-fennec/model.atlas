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
		// interval 0 → no schedule; we drive check() manually.
		return new DriftWatcher(target, () -> List.of("jena"), () -> provider, 0);
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

	private static final class RecordingListener implements DriftListener {
		final java.util.List<String> changed = new java.util.ArrayList<>();
		final java.util.List<String> removed = new java.util.ArrayList<>();

		@Override
		public void onPackageChanged(String nsUri, EPackage newPackage) {
			changed.add(nsUri);
		}

		@Override
		public void onPackageRemoved(String nsUri) {
			removed.add(nsUri);
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
}
