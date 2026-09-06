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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations.SchemaRegistryServiceSetup;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.workflow.tests.support.LuceneAwareTempDirExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * OSGi integration tests for the lifecycle that leaves one {@code objectId} in
 * two stages of the same registry: a transition <em>copies</em> unless the
 * registry sets {@code delete.after.transition=true} (the default, and what the
 * schema registry of this test setup uses), so after a promotion the object is
 * stored in both stages under one id (issue #211).
 *
 * <p>
 * Both stages must keep listing their own copy, and deleting one of them must
 * leave the other alone: the storage layout has the stage in its key, and the
 * shared registry the listings resolve through must address its entries the same
 * way (issue #252).
 * </p>
 *
 * @see <a href="https://github.com/eclipse-fennec/model.atlas/issues/252">issue
 *      #252</a>
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(LuceneAwareTempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("Copy-transition stage aliasing OSGi Integration Tests")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class RegistryStageAliasingIntegrationTest {

    private static final String SCOPE = "aliasing-scope";
    private static final String REGISTRY_FILTER = "(registry.name=" + CommonTestAnnotations.SCHEMA_REGISTRY_NAME + ")";
    private static final String OBJECT_ID = "aliased-object";

    @Test
    @DisplayName("A copy-transition leaves the object listed in both stages")
    @SchemaRegistryServiceSetup
    void copyTransitionKeepsSourceStageListed(
            @InjectService(cardinality = 0, filter = REGISTRY_FILTER) ServiceAware<RegistryService> registryAware)
            throws InterruptedException, InvocationTargetException {

        RegistryService<EPackage> registryService = registryAware.waitForService(5000);
        assertNotNull(registryService);

        upload(registryService);

        registryService.transitionToStage(SCOPE, OBJECT_ID, CommonTestAnnotations.STAGE_DRAFT,
                CommonTestAnnotations.STAGE_APPROVED);

        assertListed(registryService, CommonTestAnnotations.STAGE_APPROVED);
        assertListed(registryService, CommonTestAnnotations.STAGE_DRAFT);
    }

    @Test
    @DisplayName("Deleting the promoted copy leaves the source stage listed")
    @SchemaRegistryServiceSetup
    void deletingPromotedCopyKeepsSourceStageListed(
            @InjectService(cardinality = 0, filter = REGISTRY_FILTER) ServiceAware<RegistryService> registryAware)
            throws InterruptedException, InvocationTargetException {

        RegistryService<EPackage> registryService = registryAware.waitForService(5000);
        assertNotNull(registryService);

        upload(registryService);
        registryService.transitionToStage(SCOPE, OBJECT_ID, CommonTestAnnotations.STAGE_DRAFT,
                CommonTestAnnotations.STAGE_APPROVED);

        assertTrue(registryService.deleteFromStage(SCOPE, CommonTestAnnotations.STAGE_APPROVED, OBJECT_ID).getValue(),
                "the promoted copy should be deletable");

        assertTrue(registryService.listInStage(SCOPE, CommonTestAnnotations.STAGE_APPROVED).isEmpty(),
                "the deleted copy should be gone from its own stage");
        assertListed(registryService, CommonTestAnnotations.STAGE_DRAFT);
    }

    private void upload(RegistryService<EPackage> registryService) throws InvocationTargetException,
            InterruptedException {
        EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
        pkg.setName("aliasing-pkg");
        pkg.setNsURI("http://test/aliasing");
        pkg.setNsPrefix("al");

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(OBJECT_ID);
        metadata.setObjectName(pkg.getName());

        registryService.uploadToStage(SCOPE, CommonTestAnnotations.STAGE_DRAFT, pkg, metadata).getValue();
    }

    private void assertListed(RegistryService<EPackage> registryService, String stage) {
        List<ObjectMetadata> listed = registryService.listInStage(SCOPE, stage);
        assertEquals(1, listed.size(), "stage " + stage + " should list its own copy of " + OBJECT_ID);
        assertEquals(OBJECT_ID, listed.get(0).getObjectId());
        assertEquals(stage, listed.get(0).getStage(), "the listed metadata should be the one of stage " + stage);
    }
}
