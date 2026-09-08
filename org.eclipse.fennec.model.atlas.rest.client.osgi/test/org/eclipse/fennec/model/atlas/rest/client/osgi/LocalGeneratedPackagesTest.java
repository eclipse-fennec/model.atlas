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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;

/**
 * Unit tests for {@link LocalGeneratedPackages}.
 *
 * @author Data In Motion
 */
class LocalGeneratedPackagesTest {

	private static final String NS_A = "http://example.test/a/1.0";
	private static final String NS_B = "http://example.test/b/1.0";

	private final BundleContext bundleContext = mock(BundleContext.class);

	/** Records what the scanner would tell the local-first gate. */
	private static final class RecordingListener implements LocalGeneratedPackages.DeclarationListener {
		final List<String> appeared = new ArrayList<>();
		final List<String> disappeared = new ArrayList<>();

		@Override
		public void declared(String nsUri) {
			appeared.add(nsUri);
		}

		@Override
		public void undeclared(String nsUri) {
			disappeared.add(nsUri);
		}
	}

	private static Bundle bundleDeclaring(long id, String... nsUris) {
		// The capability mocks are built first: creating a mock inside an in-progress
		// when(...) is what Mockito calls unfinished stubbing.
		List<BundleCapability> capabilities = java.util.stream.Stream.of(nsUris)
				.map(LocalGeneratedPackagesTest::capability).toList();
		Bundle bundle = mock(Bundle.class);
		when(bundle.getBundleId()).thenReturn(id);
		when(bundle.getSymbolicName()).thenReturn("test.bundle." + id);
		BundleRevision revision = mock(BundleRevision.class);
		when(bundle.adapt(BundleRevision.class)).thenReturn(revision);
		when(revision.getDeclaredCapabilities(LocalGeneratedPackages.GENERATED_PACKAGE_NAMESPACE))
				.thenReturn(capabilities);
		return bundle;
	}

	private static BundleCapability capability(String nsUri) {
		BundleCapability capability = mock(BundleCapability.class);
		when(capability.getAttributes()).thenReturn(Map.of(LocalGeneratedPackages.URI_ATTRIBUTE, nsUri));
		return capability;
	}

	@Test
	void seesTheNsUrisDeclaredByInstalledBundles() {
		Bundle bundle = bundleDeclaring(1, NS_A);
		when(bundleContext.getBundles()).thenReturn(new Bundle[] { bundle });

		LocalGeneratedPackages declared = LocalGeneratedPackages.scan(bundleContext);

		assertTrue(declared.declares(NS_A), "a bundle declaring the package must count as local");
		assertFalse(declared.declares(NS_B));
	}

	@Test
	void seesThemBeforeAnyServiceExists() throws Exception {
		// The point of the whole class: a bundle that has only been installed - not started,
		// nothing registered, no class loaded - already declares what it will provide.
		Bundle bundle = bundleDeclaring(1, NS_A);
		when(bundle.getState()).thenReturn(Bundle.INSTALLED);
		when(bundleContext.getBundles()).thenReturn(new Bundle[] { bundle });

		assertTrue(LocalGeneratedPackages.scan(bundleContext).declares(NS_A));
		verify(bundleContext, never()).getServiceReferences(anyString(), any());
	}

	@Test
	void ignoresABundleWithoutARevision() {
		Bundle uninstalled = mock(Bundle.class);
		when(uninstalled.getBundleId()).thenReturn(7L);
		when(uninstalled.adapt(BundleRevision.class)).thenReturn(null);
		when(bundleContext.getBundles()).thenReturn(new Bundle[] { uninstalled });

		assertFalse(LocalGeneratedPackages.scan(bundleContext).declares(NS_A));
	}

	@Test
	void ignoresACapabilityWithoutAUsableUriAttribute() {
		Bundle bundle = mock(Bundle.class);
		when(bundle.getBundleId()).thenReturn(1L);
		BundleRevision revision = mock(BundleRevision.class);
		when(bundle.adapt(BundleRevision.class)).thenReturn(revision);
		BundleCapability noUri = mock(BundleCapability.class);
		when(noUri.getAttributes()).thenReturn(Map.of("class", "some.Package"));
		BundleCapability blankUri = mock(BundleCapability.class);
		when(blankUri.getAttributes()).thenReturn(Map.of(LocalGeneratedPackages.URI_ATTRIBUTE, "  "));
		when(revision.getDeclaredCapabilities(LocalGeneratedPackages.GENERATED_PACKAGE_NAMESPACE))
				.thenReturn(List.of(noUri, blankUri));
		when(bundleContext.getBundles()).thenReturn(new Bundle[] { bundle });

		LocalGeneratedPackages declared = LocalGeneratedPackages.scan(bundleContext);

		assertFalse(declared.declares(""));
		assertFalse(declared.declares("  "));
	}

	@Test
	void picksUpABundleInstalledLaterAndTellsTheGate() {
		when(bundleContext.getBundles()).thenReturn(new Bundle[0]);
		RecordingListener listener = new RecordingListener();
		LocalGeneratedPackages declared = LocalGeneratedPackages.scan(bundleContext);
		declared.track(bundleContext, listener);

		Bundle later = bundleDeclaring(2, NS_A);
		declared.bundleChanged(new BundleEvent(BundleEvent.INSTALLED, later));

		assertTrue(declared.declares(NS_A), "a bundle installed later must be seen too");
		// So a remote we already published for that nsURI is withdrawn and parked.
		assertEquals(List.of(NS_A), listener.appeared);
	}

	@Test
	void forgetsTheNsUrisOfAnUninstalledBundleAndTellsTheGate() {
		Bundle bundle = bundleDeclaring(3, NS_A);
		when(bundleContext.getBundles()).thenReturn(new Bundle[] { bundle });
		RecordingListener listener = new RecordingListener();
		LocalGeneratedPackages declared = LocalGeneratedPackages.scan(bundleContext);
		declared.track(bundleContext, listener);

		declared.bundleChanged(new BundleEvent(BundleEvent.UNINSTALLED, bundle));

		assertFalse(declared.declares(NS_A), "an uninstalled bundle declares nothing");
		// The gate re-checks presence itself before it republishes, so this is a hint, not a command.
		assertEquals(List.of(NS_A), listener.disappeared);
	}

	@Test
	void keepsAnNsUriDeclaredByASecondBundle() {
		Bundle first = bundleDeclaring(1, NS_A);
		Bundle second = bundleDeclaring(2, NS_A);
		when(bundleContext.getBundles()).thenReturn(new Bundle[] { first, second });
		RecordingListener listener = new RecordingListener();
		LocalGeneratedPackages declared = LocalGeneratedPackages.scan(bundleContext);
		declared.track(bundleContext, listener);

		declared.bundleChanged(new BundleEvent(BundleEvent.UNINSTALLED, first));

		assertTrue(declared.declares(NS_A), "the other bundle still declares it");
		assertTrue(listener.disappeared.isEmpty(), "the other bundle still declares it");
	}
}
