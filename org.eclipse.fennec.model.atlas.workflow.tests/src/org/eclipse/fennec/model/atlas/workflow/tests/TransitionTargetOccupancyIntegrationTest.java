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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.scope.api.StageOccupiedException;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations.SchemaRegistryServiceSetup;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
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
 * OSGi integration tests for the target-occupancy guard of
 * {@code RegistryServiceImpl#transitionToStage}.
 *
 * <p>
 * An objectId is unique per stage, not across stages (issue #211), so promoting
 * writes into a target stage that may already hold that id. When the occupant is
 * an earlier revision of the object being promoted, overwriting it is the whole
 * point of a promotion; when it is a different object, overwriting it destroys
 * something the caller never named.
 * </p>
 *
 * @see <a href="https://github.com/eclipse-fennec/model.atlas/issues/211">issue
 *      #211</a>
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(LuceneAwareTempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("Transition target-occupancy OSGi Integration Tests")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TransitionTargetOccupancyIntegrationTest {

    private static final String SCOPE = "occupancy-scope";
    private static final String REGISTRY_FILTER = "(registry.name=" + CommonTestAnnotations.SCHEMA_REGISTRY_NAME + ")";
    private static final String OBJECT_ID = "contested-id";
    private static final String NS_URI_A = "http://test/occupancy/a";
    private static final String NS_URI_B = "http://test/occupancy/b";

    @Test
    @DisplayName("Promoting onto an id another object holds in the target stage is refused")
    @SchemaRegistryServiceSetup
    void refusesPromotionOntoADifferentObject(
            @InjectService(cardinality = 0, filter = REGISTRY_FILTER) ServiceAware<RegistryService> registryAware)
            throws InterruptedException, InvocationTargetException {

        RegistryService<EPackage> registryService = registryAware.waitForService(5000);
        assertNotNull(registryService);

        upload(registryService, CommonTestAnnotations.STAGE_APPROVED, NS_URI_A, "package-a", "1");
        upload(registryService, CommonTestAnnotations.STAGE_DRAFT, NS_URI_B, "package-b", "1");

        StageOccupiedException refused = assertThrows(StageOccupiedException.class,
                () -> registryService.transitionToStage(SCOPE, OBJECT_ID, CommonTestAnnotations.STAGE_DRAFT,
                        CommonTestAnnotations.STAGE_APPROVED));
        assertNotNull(refused.getMessage());

        // the object that held the target address is untouched
        ObjectMetadata occupant = registryService.getMetadataFromStage(SCOPE, CommonTestAnnotations.STAGE_APPROVED,
                OBJECT_ID);
        assertNotNull(occupant);
        assertEquals(NS_URI_A, occupant.getProperties().get(WorkflowConstants.NS_URI_METADATA_PROPERTY));
        assertEquals("package-a", occupant.getObjectName());
    }

    @Test
    @DisplayName("Re-promoting a newer revision of the same object still overwrites the target")
    @SchemaRegistryServiceSetup
    void allowsRepromotionOfTheSameObject(
            @InjectService(cardinality = 0, filter = REGISTRY_FILTER) ServiceAware<RegistryService> registryAware)
            throws InterruptedException, InvocationTargetException {

        RegistryService<EPackage> registryService = registryAware.waitForService(5000);
        assertNotNull(registryService);

        upload(registryService, CommonTestAnnotations.STAGE_DRAFT, NS_URI_A, "package-a", "1");
        registryService.transitionToStage(SCOPE, OBJECT_ID, CommonTestAnnotations.STAGE_DRAFT,
                CommonTestAnnotations.STAGE_APPROVED);

        // a new draft revision of the very same object, promoted onto its own
        // earlier copy - the normal update flow, which the guard must not refuse
        upload(registryService, CommonTestAnnotations.STAGE_DRAFT, NS_URI_A, "package-a", "2");
        ObjectMetadata promoted = registryService.transitionToStage(SCOPE, OBJECT_ID,
                CommonTestAnnotations.STAGE_DRAFT, CommonTestAnnotations.STAGE_APPROVED);

        assertNotNull(promoted);
        assertEquals("2", registryService
                .getMetadataFromStage(SCOPE, CommonTestAnnotations.STAGE_APPROVED, OBJECT_ID).getVersion());
    }

    @Test
    @DisplayName("overwrite=true promotes onto a different object instead of refusing")
    @SchemaRegistryServiceSetup
    void overwriteReplacesTheDifferentObject(
            @InjectService(cardinality = 0, filter = REGISTRY_FILTER) ServiceAware<RegistryService> registryAware)
            throws InterruptedException, InvocationTargetException {

        RegistryService<EPackage> registryService = registryAware.waitForService(5000);
        assertNotNull(registryService);

        upload(registryService, CommonTestAnnotations.STAGE_APPROVED, NS_URI_A, "package-a", "1");
        upload(registryService, CommonTestAnnotations.STAGE_DRAFT, NS_URI_B, "package-b", "1");

        // The caller knows the target is held by another object and says so: no need to
        // delete it first (issue #211 follow-up)
        ObjectMetadata promoted = registryService.transitionToStage(SCOPE, OBJECT_ID,
                CommonTestAnnotations.STAGE_DRAFT, CommonTestAnnotations.STAGE_APPROVED, true);
        assertNotNull(promoted);

        ObjectMetadata inTarget = registryService.getMetadataFromStage(SCOPE, CommonTestAnnotations.STAGE_APPROVED,
                OBJECT_ID);
        assertNotNull(inTarget);
        assertEquals(NS_URI_B, inTarget.getProperties().get(WorkflowConstants.NS_URI_METADATA_PROPERTY));
        assertEquals("package-b", inTarget.getObjectName());
    }

    @Test
    @DisplayName("overwrite=false is the plain transition: still refused")
    @SchemaRegistryServiceSetup
    void overwriteFalseStillRefuses(
            @InjectService(cardinality = 0, filter = REGISTRY_FILTER) ServiceAware<RegistryService> registryAware)
            throws InterruptedException, InvocationTargetException {

        RegistryService<EPackage> registryService = registryAware.waitForService(5000);
        assertNotNull(registryService);

        upload(registryService, CommonTestAnnotations.STAGE_APPROVED, NS_URI_A, "package-a", "1");
        upload(registryService, CommonTestAnnotations.STAGE_DRAFT, NS_URI_B, "package-b", "1");

        assertThrows(StageOccupiedException.class,
                () -> registryService.transitionToStage(SCOPE, OBJECT_ID, CommonTestAnnotations.STAGE_DRAFT,
                        CommonTestAnnotations.STAGE_APPROVED, false));
    }

    private void upload(RegistryService<EPackage> registryService, String stage, String nsUri, String name,
            String version) throws InvocationTargetException, InterruptedException {
        EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
        pkg.setName(name);
        pkg.setNsURI(nsUri);
        pkg.setNsPrefix(name);

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(OBJECT_ID);
        metadata.setObjectName(name);
        metadata.setVersion(version);
        metadata.getProperties().put(WorkflowConstants.NS_URI_METADATA_PROPERTY, nsUri);

        registryService.uploadToStage(SCOPE, stage, pkg, metadata).getValue();
    }
}
