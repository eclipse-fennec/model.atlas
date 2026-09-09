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
import static org.mockito.Mockito.mock;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.Type;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * A registry whose schema EPackage is not there yet must wait for it, not fail
 * for good.
 *
 * <p>
 * The registry resolves {@code root.eclass.uri} while it activates. When the
 * EPackage naming that EClass has not been registered yet, the resolution used to
 * throw out of the constructor - and Declarative Services retries an activation
 * only when a reference or the configuration changes, so a package arriving
 * moments later never revived the component: the registry stayed absent, silently,
 * for the lifetime of the framework. That is issue #169, seen in the field as a
 * sensinactmapping registry whose model bundle happened to start after the
 * workflow bundle.
 * </p>
 *
 * <p>
 * The schema package is a mandatory reference now, so the decision belongs to DS:
 * no package, no activation attempt, and the attempt happens as soon as the
 * package appears.
 * </p>
 *
 * @see <a href="https://github.com/eclipse-fennec/model.atlas/issues/169">issue
 *      #169</a>
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("RegistryService - a schema package that registers late")
public class LateSchemaPackageIntegrationTest {

    private static final String REGISTRY_NAME = "late-registry";
    private static final String LATE_NS_URI = "http://example.org/late/1.0";
    private static final String LATE_ECLASS = "LateThing";

    @InjectBundleContext
    BundleContext bundleContext;

    @SuppressWarnings("rawtypes")
    private ServiceRegistration<EObjectStorageService> storageRegistration;
    @SuppressWarnings("rawtypes")
    private ServiceRegistration<EObjectRegistryService> registryRegistration;
    private ServiceRegistration<EPackage> lateSchemaPackage;

    @BeforeEach
    void setUp() {
        @SuppressWarnings("unchecked")
        EObjectStorageService<EObject> storage = mock(EObjectStorageService.class);
        Dictionary<String, Object> storageProperties = new Hashtable<>();
        storageProperties.put("storage.type", "mock-late");
        storageRegistration = bundleContext.registerService(EObjectStorageService.class, storage, storageProperties);

        @SuppressWarnings("unchecked")
        EObjectRegistryService<EObject> registry = mock(EObjectRegistryService.class);
        registryRegistration = bundleContext.registerService(EObjectRegistryService.class, registry,
                new Hashtable<>());
    }

    @AfterEach
    void tearDown() {
        if (lateSchemaPackage != null) {
            lateSchemaPackage.unregister();
        }
        storageRegistration.unregister();
        registryRegistration.unregister();
    }

    @Test
    @DisplayName("it waits for the package, then comes up with it")
    @WithFactoryConfiguration(factoryPid = "RegistryService", name = REGISTRY_NAME, location = "?", properties = {
            @Property(key = "registry.name", value = REGISTRY_NAME),
            @Property(key = "root.eclass.uri", value = LATE_NS_URI + "#//" + LATE_ECLASS),
            @Property(key = "schemaPackage.target", value = "(emf.nsURI=" + LATE_NS_URI + ")"),
            @Property(key = "stages", type = Type.Array, value = {
                    "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}" }),
            @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
            @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock-late",
                    "release:mock-late" }),
            @Property(key = "storageService.target", value = "(storage.type=mock-late)") })
    void waitsForTheSchemaPackageAndThenActivates(
            @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME
                    + ")") ServiceAware<RegistryService> registryAware) throws Exception {

        assertNull(registryAware.waitForService(1000),
                "without its schema package the registry must not be there - and must not have failed for good either");

        lateSchemaPackage = registerLateSchemaPackage();

        @SuppressWarnings("unchecked")
        RegistryService<EObject> registryService = registryAware.waitForService(10000);
        assertNotNull(registryService, "the registry must activate once its schema package registers (issue #169)");
        assertEquals(REGISTRY_NAME, registryService.getRegistryName());
        assertEquals(LATE_ECLASS, registryService.getRootEClasses().get(0).getName(),
                "the root EClass must come from the package that arrived late");
    }

    /**
     * Registers the EPackage the way the model bundles do, carrying the namespace
     * URI as {@code emf.nsURI} - the property the schema package reference filters
     * on.
     */
    private ServiceRegistration<EPackage> registerLateSchemaPackage() {
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setName("late");
        ePackage.setNsPrefix("late");
        ePackage.setNsURI(LATE_NS_URI);
        EClass lateThing = EcoreFactory.eINSTANCE.createEClass();
        lateThing.setName(LATE_ECLASS);
        ePackage.getEClassifiers().add(lateThing);

        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("emf.nsURI", LATE_NS_URI);
        properties.put("emf.name", "late");
        return bundleContext.registerService(EPackage.class, ePackage, properties);
    }
}
