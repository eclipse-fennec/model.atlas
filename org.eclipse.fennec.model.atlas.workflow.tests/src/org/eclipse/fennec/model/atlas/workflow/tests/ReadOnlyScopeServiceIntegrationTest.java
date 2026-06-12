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
package org.eclipse.fennec.model.atlas.workflow.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.readonlyscope.collector.ReadOnlyScopeCollector;
import org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService;
import org.eclipse.fennec.model.atlas.scope.api.RegistryInfo;
import org.eclipse.fennec.model.atlas.scope.api.RegistryType;
import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.atlas.workflow.tests.annotations.TestAnnotations;
import org.eclipse.fennec.model.atlas.workflow.tests.annotations.TestAnnotations.ScopeServiceSetup;
import org.eclipse.fennec.model.atlas.workflow.tests.support.LuceneAwareTempDirExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * OSGi integration tests for the read-only surface of a configured scope
 * ({@code ScopeServiceImpl}): that it is published under all three service shapes,
 * that the {@link ReadOnlyScopeService} read path works against the final stage, and
 * that the {@link ReadOnlyScopeCollector} (keyed on {@code atlas.scope}) collects it.
 *
 * @author Data In Motion
 * @since Jun 11, 2026
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(LuceneAwareTempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("ReadOnlyScopeService OSGi Integration Tests")
@SuppressWarnings({ "unchecked", "rawtypes", "restriction" })
public class ReadOnlyScopeServiceIntegrationTest {

	private static final String SCHEMA_REGISTRY = CommonTestAnnotations.SCHEMA_REGISTRY_NAME;
	private static final String FINAL_STAGE = CommonTestAnnotations.STAGE_RELEASE;
	private static final String OBJECT_ID = "read-path-object";
	private static final String SCOPE_FILTER = "(scope.name=" + TestAnnotations.TEST_SCOPE_NAME + ")";

	@Nested
	@DisplayName("Dual-Shape Publication Tests")
	class DualShapePublicationTests {

		@Test
		@DisplayName("Same component resolves under ScopeService, WritableScopeService and ReadOnlyScopeService")
		@ScopeServiceSetup
		void shouldResolveUnderAllThreeShapes(
				@InjectService(cardinality = 0, filter = SCOPE_FILTER) ServiceAware<ScopeService> scopeAware,
				@InjectService(cardinality = 0, filter = SCOPE_FILTER) ServiceAware<WritableScopeService> writableAware,
				@InjectService(cardinality = 0, filter = SCOPE_FILTER) ServiceAware<ReadOnlyScopeService> readOnlyAware)
				throws InterruptedException {

			ScopeService<?> scopeService = scopeAware.waitForService(5000);
			WritableScopeService<?> writableService = writableAware.waitForService(5000);
			ReadOnlyScopeService<?> readOnlyService = readOnlyAware.waitForService(5000);

			assertNotNull(scopeService, "ScopeService shape should be registered");
			assertNotNull(writableService, "WritableScopeService shape should be registered");
			assertNotNull(readOnlyService, "ReadOnlyScopeService shape should be registered");

			// One component instance registered under all three interfaces.
			assertSame(scopeService, writableService);
			assertSame(scopeService, readOnlyService);
		}
	}

	@Nested
	@DisplayName("Read Path Tests")
	class ReadPathTests {

		@Test
		@DisplayName("getScopeInfo exposes the schema registry by type, and get() resolves final-stage content")
		@ScopeServiceSetup
		void shouldReadFinalStageContentThroughReadContract(
				@InjectService(cardinality = 0, filter = SCOPE_FILTER) ServiceAware<ScopeService> scopeAware)
				throws InterruptedException, InvocationTargetException {

			ScopeService<EPackage> scopeService = scopeAware.waitForService(5000);
			assertNotNull(scopeService);

			// Publish an EPackage into the final (release) stage via the writable surface.
			EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
			pkg.setName("read-path-pkg");
			pkg.setNsURI("http://test/read-path");
			pkg.setNsPrefix("rp");
			ObjectMetadata meta = ManagementFactory.eINSTANCE.createObjectMetadata();
			meta.setObjectId(OBJECT_ID);
			scopeService.uploadToStageForRegistry(SCHEMA_REGISTRY, FINAL_STAGE, pkg, meta).getValue();

			// Discover the registry by type through the read-only descriptor.
			ScopeInfo info = scopeService.getScopeInfo();
			assertNotNull(info);
			assertEquals(TestAnnotations.TEST_SCOPE_NAME, info.getName());
			RegistryInfo schema = info.getRegistries().stream()
					.filter(r -> RegistryType.SCHEMA == r.getType())
					.findFirst()
					.orElseThrow();
			assertEquals(SCHEMA_REGISTRY, schema.getName());

			// get() resolves the final-stage content.
			Optional<EPackage> resolved = scopeService.get(schema.getName(), OBJECT_ID);
			assertTrue(resolved.isPresent(), "Final-stage content should be resolvable via get()");
			assertEquals("http://test/read-path", resolved.get().getNsURI());

			// listObjectIds includes it.
			assertTrue(scopeService.listObjectIds(schema.getName()).contains(OBJECT_ID));
		}

		@Test
		@DisplayName("get() returns empty (not NPE) for a missing object")
		@ScopeServiceSetup
		void shouldReturnEmptyForMissingObject(
				@InjectService(cardinality = 0, filter = SCOPE_FILTER) ServiceAware<ScopeService> scopeAware)
				throws InterruptedException {

			ScopeService<EPackage> scopeService = scopeAware.waitForService(5000);
			assertNotNull(scopeService);

			Optional<EPackage> missing = scopeService.get(SCHEMA_REGISTRY, "does-not-exist");
			assertTrue(missing.isEmpty(), "Missing object must yield Optional.empty()");
		}
	}

	@Nested
	@DisplayName("Collector Tests")
	class CollectorTests {

		@Test
		@DisplayName("ReadOnlyScopeCollector collects the scope by its atlas.scope property")
		@ScopeServiceSetup
		void shouldCollectScopeByAtlasScope(
				@InjectService(cardinality = 0, filter = SCOPE_FILTER) ServiceAware<ScopeService> scopeAware,
				@InjectService ReadOnlyScopeCollector collector)
				throws InterruptedException {

			// Ensure the scope component is up before asserting the collector saw it.
			assertNotNull(scopeAware.waitForService(5000));

			ReadOnlyScopeService<?> collected = awaitCollected(collector, TestAnnotations.TEST_SCOPE_NAME, 5000);
			assertNotNull(collected, "Collector should have bound the scope under its atlas.scope key");
			assertTrue(collector.getAllScopeNames().contains(TestAnnotations.TEST_SCOPE_NAME));
		}

		/** Whiteboard binding is asynchronous; poll briefly for the collector to observe the scope. */
		private ReadOnlyScopeService<?> awaitCollected(ReadOnlyScopeCollector collector, String scopeName, long timeoutMillis)
				throws InterruptedException {
			long deadline = System.currentTimeMillis() + timeoutMillis;
			ReadOnlyScopeService<?> found = collector.getScopeServiceByScopeName(scopeName);
			while (found == null && System.currentTimeMillis() < deadline) {
				Thread.sleep(50);
				found = collector.getScopeServiceByScopeName(scopeName);
			}
			return found;
		}
	}
}
