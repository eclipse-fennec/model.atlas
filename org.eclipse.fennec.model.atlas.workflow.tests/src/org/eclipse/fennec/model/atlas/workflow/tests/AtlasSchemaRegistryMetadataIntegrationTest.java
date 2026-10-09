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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.emf.osgi.fingerprint.util.FingerprintHelper;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractEObjectStorageService;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations.EPackageLuceneIndexSetup;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations.RegistryConfiguration;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.workflow.EPackageVersions;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.eclipse.fennec.model.atlas.workflow.tests.support.LuceneAwareTempDirExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * The system schemas of the atlas scope carry the same metadata as an uploaded schema -
 * {@code version}, {@code contentHash}, {@code fingerprint} and {@code lastChangeTime} - and a
 * package shipped in a bundle dates from that bundle's build, so the times survive a restart
 * (issue #359).
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(LuceneAwareTempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("AtlasSchemaRegistryService - system schema metadata")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class AtlasSchemaRegistryMetadataIntegrationTest {

	private static final String ATLAS_REGISTRY_FILTER = "(registry.name=" + WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME + ")";
	private static final String DYNAMIC_NS_URI = "http://example.org/atlas-metadata/2.1";
	private static final long TIMEOUT_MS = 10_000;

	@InjectBundleContext
	BundleContext bundleContext;

	private final List<ServiceRegistration<?>> registrations = new ArrayList<>();

	@AfterEach
	void tearDown() {
		registrations.forEach(ServiceRegistration::unregister);
		registrations.clear();
	}

	@Test
	@DisplayName("a package shipped in a bundle carries the upload metadata, dated by the bundle's build")
	@RegistryConfiguration
	@EPackageLuceneIndexSetup
	void shippedPackageCarriesUploadMetadata(
			@InjectService(cardinality = 0, filter = ATLAS_REGISTRY_FILTER) ServiceAware<RegistryService> registryAware)
			throws Exception {
		RegistryService<EPackage> atlasRegistry = registryAware.waitForService(15_000);
		assertNotNull(atlasRegistry, "the atlas schema registry must be up");

		int checked = 0;
		for (ObjectMetadata metadata : atlasRegistry.listInFinalStage(WorkflowConstants.ATLAS_SCOPE_NAME)) {
			EPackage ePackage = atlasRegistry.getContentFromFinalStage(WorkflowConstants.ATLAS_SCOPE_NAME,
					metadata.getObjectId());
			if (ePackage == null || ePackage.getClass() == EPackageImpl.class) {
				continue;
			}
			Bundle shipping = FrameworkUtil.getBundle(ePackage.getClass());
			String built = shipping == null ? null : shipping.getHeaders("").get("Bnd-LastModified");
			if (built == null) {
				continue;
			}
			String what = metadata.getObjectName() + " (" + shipping.getSymbolicName() + ")";
			Instant buildTime = Instant.ofEpochMilli(Long.parseLong(built));
			assertEquals(buildTime, metadata.getUploadTime(), "uploadTime must be the build time of " + what);
			assertEquals(buildTime, metadata.getLastChangeTime(), "lastChangeTime must be the build time of " + what);
			assertEquals(expectedVersion(ePackage, shipping), metadata.getVersion(), "version of " + what);
			assertEquals(AbstractEObjectStorageService.computeContentHash(ePackage), metadata.getContentHash(),
					"contentHash of " + what);
			assertEquals(FingerprintHelper.fingerprint(ePackage), metadata.getFingerprint(), "fingerprint of " + what);
			checked++;
		}
		assertFalse(checked == 0, "the runtime must ship at least one generated package in the atlas scope");
	}

	@Test
	@DisplayName("a dynamic package carries version, contentHash, fingerprint and lastChangeTime too")
	@RegistryConfiguration
	@EPackageLuceneIndexSetup
	void dynamicPackageCarriesUploadMetadata(
			@InjectService(cardinality = 0, filter = ATLAS_REGISTRY_FILTER) ServiceAware<RegistryService> registryAware)
			throws Exception {
		RegistryService<EPackage> atlasRegistry = registryAware.waitForService(15_000);
		assertNotNull(atlasRegistry, "the atlas schema registry must be up");

		EPackage ePackage = registerStaticPackage("atlasMetadata", DYNAMIC_NS_URI);
		ObjectMetadata metadata = await(atlasRegistry, DYNAMIC_NS_URI);

		assertEquals("2.1.0", metadata.getVersion(), "the version is read from the nsURI, as for an upload");
		assertEquals(AbstractEObjectStorageService.computeContentHash(ePackage), metadata.getContentHash());
		assertEquals(FingerprintHelper.fingerprint(ePackage), metadata.getFingerprint());
		assertNotNull(metadata.getLastChangeTime());
		assertEquals(metadata.getUploadTime(), metadata.getLastChangeTime());
	}

	private static String expectedVersion(EPackage ePackage, Bundle shipping) {
		String own = EPackageVersions.of(ePackage, declared -> {
		});
		if (own != null) {
			return own;
		}
		Version version = shipping.getVersion();
		return new Version(version.getMajor(), version.getMinor(), version.getMicro()).toString();
	}

	private static ObjectMetadata await(RegistryService<EPackage> atlasRegistry, String nsUri) throws InterruptedException {
		long deadline = System.currentTimeMillis() + TIMEOUT_MS;
		while (true) {
			Optional<ObjectMetadata> found = atlasRegistry.listInFinalStage(WorkflowConstants.ATLAS_SCOPE_NAME).stream()
					.filter(md -> nsUri.equals(md.getProperties().get(WorkflowConstants.NS_URI_METADATA_PROPERTY)))
					.findFirst();
			if (found.isPresent()) {
				return found.get();
			}
			if (System.currentTimeMillis() > deadline) {
				throw new AssertionError(nsUri + " must appear in the atlas schema registry listing");
			}
			Thread.sleep(50);
		}
	}

	/** Contributes a dynamic package to the static registry, the way model bundles contribute theirs. */
	private EPackage registerStaticPackage(String name, String nsUri) {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName(name);
		ePackage.setNsPrefix(name);
		ePackage.setNsURI(nsUri);
		EClass eClass = EcoreFactory.eINSTANCE.createEClass();
		eClass.setName("Thing");
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
		registrations.add(bundleContext.registerService(EPackageConfigurator.class, configurator, properties));
		return ePackage;
	}
}
