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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageLuceneIndex;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageLuceneIndex.SearchResult;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageSearchQuery;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations.EPackageLuceneIndexSetup;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations.RegistryConfiguration;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.eclipse.fennec.model.atlas.workflow.tests.support.LuceneAwareTempDirExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * The atlas schema registry mirrors the static EPackage registry. A package that reaches
 * the static registry after the atlas schema registry activated must become listed,
 * retrievable and searchable like the packages that were there at activation, and
 * leaving again must remove that package only (issue #282).
 * <p>
 * Packages enter the static registry through {@link EPackageConfigurator} services in the
 * {@code static} model scope, the same way generated model bundles contribute theirs, so
 * that is how this test registers a package late.
 *
 * @since Sep 13, 2026
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(LuceneAwareTempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("AtlasSchemaRegistryService - a package that reaches the static registry late")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class AtlasSchemaRegistryLatePackageIntegrationTest {

	private static final String ATLAS_REGISTRY_FILTER = "(registry.name=" + WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME + ")";
	private static final String LATE_NS_URI = "http://example.org/atlas-late/1.0";
	private static final String OTHER_NS_URI = "http://example.org/atlas-other/1.0";
	private static final long TIMEOUT_MS = 10_000;

	@InjectBundleContext
	BundleContext bundleContext;

	private final List<ServiceRegistration<?>> registrations = new ArrayList<>();

	@AfterEach
	void tearDown() {
		registrations.forEach(registration -> {
			try {
				registration.unregister();
			} catch (IllegalStateException alreadyGone) {
				// unregistered by the test itself
			}
		});
		registrations.clear();
	}

	@Test
	@DisplayName("a package registered after activation is listed, retrievable, searchable - and leaves alone")
	@RegistryConfiguration
	@EPackageLuceneIndexSetup
	void latePackageIsMirroredAndUnmirroredIndividually(
			@InjectService(cardinality = 0, filter = ATLAS_REGISTRY_FILTER) ServiceAware<RegistryService> registryAware,
			@InjectService(cardinality = 0) ServiceAware<EPackageLuceneIndex> indexAware) throws Exception {

		RegistryService<EPackage> atlasRegistry = registryAware.waitForService(15_000);
		assertNotNull(atlasRegistry, "the atlas schema registry must be up before the late package arrives");
		EPackageLuceneIndex index = indexAware.waitForService(15_000);
		assertNotNull(index);

		List<ObjectMetadata> present = atlasRegistry.listInFinalStage(WorkflowConstants.ATLAS_SCOPE_NAME);
		assertTrue(findByNsUri(atlasRegistry, LATE_NS_URI).isEmpty(), "the late package must not be there yet");
		assertTrue(findByNsUri(atlasRegistry, OTHER_NS_URI).isEmpty(), "the other package must not be there yet");

		ServiceRegistration<?> late = registerStaticPackage("atlasLate", LATE_NS_URI, "LateThing");
		registerStaticPackage("atlasOther", OTHER_NS_URI, "OtherThing");

		// listed ...
		ObjectMetadata lateMetadata = await(() -> findByNsUri(atlasRegistry, LATE_NS_URI),
				"the late package must appear in the atlas schema registry listing");
		assertEquals("atlasLate", lateMetadata.getObjectName());
		assertEquals(WorkflowConstants.ATLAS_SCOPE_NAME, lateMetadata.getScope());
		assertEquals(WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME, lateMetadata.getStage());
		assertEquals("system", lateMetadata.getUploadUser());
		ObjectMetadata otherMetadata = await(() -> findByNsUri(atlasRegistry, OTHER_NS_URI),
				"the other package must appear in the atlas schema registry listing");

		// ... retrievable by its objectId ...
		assertEquals(lateMetadata, atlasRegistry.getMetadataFromFinalStage(WorkflowConstants.ATLAS_SCOPE_NAME,
				lateMetadata.getObjectId()));
		EPackage content = atlasRegistry.getContentFromFinalStage(WorkflowConstants.ATLAS_SCOPE_NAME,
				lateMetadata.getObjectId());
		assertNotNull(content, "the late package must be retrievable through the atlas schema registry");
		assertEquals(LATE_NS_URI, content.getNsURI());
		assertEquals("LateThing", content.getEClassifiers().get(0).getName());

		// ... and searchable
		SearchResult hits = index.search(searchFor(LATE_NS_URI));
		assertEquals(1, hits.totalHits(), "the late package must be found through the EPackage search index");
		assertEquals(lateMetadata.getObjectId(), hits.hits().get(0).objectId());
		assertEquals(WorkflowConstants.ATLAS_SCOPE_NAME, hits.hits().get(0).scope());

		// what was there at activation is still there
		for (ObjectMetadata before : present) {
			assertNotNull(atlasRegistry.getMetadataFromFinalStage(WorkflowConstants.ATLAS_SCOPE_NAME, before.getObjectId()),
					"a package present at activation must survive the late arrival: " + before.getObjectName());
		}

		// unregistering the late package removes that package only
		late.unregister();
		registrations.remove(late);

		await(() -> findByNsUri(atlasRegistry, LATE_NS_URI).isEmpty() ? Optional.of(Boolean.TRUE) : Optional.empty(),
				"the late package must leave the atlas schema registry listing once unregistered");
		assertNull(atlasRegistry.getMetadataFromFinalStage(WorkflowConstants.ATLAS_SCOPE_NAME, lateMetadata.getObjectId()));
		assertEquals(0, index.search(searchFor(LATE_NS_URI)).totalHits(),
				"the late package must leave the EPackage search index once unregistered");

		assertEquals(otherMetadata.getObjectId(), findByNsUri(atlasRegistry, OTHER_NS_URI).map(ObjectMetadata::getObjectId).orElse(null),
				"the other late package must stay listed");
		assertEquals(1, index.search(searchFor(OTHER_NS_URI)).totalHits(), "the other late package must stay searchable");
		for (ObjectMetadata before : present) {
			assertNotNull(atlasRegistry.getMetadataFromFinalStage(WorkflowConstants.ATLAS_SCOPE_NAME, before.getObjectId()),
					"a package present at activation must survive the late departure: " + before.getObjectName());
		}
	}

	private static Optional<ObjectMetadata> findByNsUri(RegistryService<EPackage> atlasRegistry, String nsUri) {
		return atlasRegistry.listInFinalStage(WorkflowConstants.ATLAS_SCOPE_NAME).stream()
				.filter(md -> nsUri.equals(md.getProperties().get(WorkflowConstants.NS_URI_METADATA_PROPERTY)))
				.findFirst();
	}

	private static EPackageSearchQuery searchFor(String nsUri) {
		return EPackageSearchQuery.create().scopes(Set.of(WorkflowConstants.ATLAS_SCOPE_NAME)).nsUriExact(nsUri).build();
	}

	private static <T> T await(Supplier<Optional<T>> probe, String message) throws InterruptedException {
		long deadline = System.currentTimeMillis() + TIMEOUT_MS;
		while (true) {
			Optional<T> result = probe.get();
			if (result.isPresent()) {
				return result.get();
			}
			if (System.currentTimeMillis() > deadline) {
				throw new AssertionError(message);
			}
			Thread.sleep(50);
		}
	}

	/**
	 * Contributes a package to the static registry the way model bundles do: through an
	 * {@link EPackageConfigurator} service in the {@code static} model scope.
	 */
	private ServiceRegistration<?> registerStaticPackage(String name, String nsUri, String eClassName) {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName(name);
		ePackage.setNsPrefix(name);
		ePackage.setNsURI(nsUri);
		EClass eClass = EcoreFactory.eINSTANCE.createEClass();
		eClass.setName(eClassName);
		ePackage.getEClassifiers().add(eClass);

		EPackageConfigurator configurator = new EPackageConfigurator() {
			@Override
			public void configureEPackage(EPackage.Registry registry) {
				registry.put(nsUri, ePackage);
			}

			@Override
			public void unconfigureEPackage(EPackage.Registry registry) {
				registry.remove(nsUri);
			}
		};
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put(EMFNamespaces.EMF_MODEL_SCOPE, EMFNamespaces.EMF_MODEL_SCOPE_STATIC);
		properties.put(EMFNamespaces.EMF_MODEL_NSURI, nsUri);
		properties.put(EMFNamespaces.EMF_NAME, name);
		ServiceRegistration<?> registration = bundleContext.registerService(EPackageConfigurator.class, configurator, properties);
		registrations.add(registration);
		return registration;
	}
}
