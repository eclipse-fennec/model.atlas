/*
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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.workflow.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.List;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.scope.api.StagePolicyException;
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
 * OSGi integration test for {@code RegistryService.updateDiagnostics} (issue #292): a
 * diagnostics write succeeds in a final stage where a content update is refused, changes
 * neither version nor content hash nor {@code lastChangeTime}, and the result is visible in
 * the stage listing and on a fresh metadata read.
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(LuceneAwareTempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("Diagnostics write OSGi Integration Tests")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DiagnosticsWriteIntegrationTest {

    private static final String SCOPE = "diagnostics-scope";
    private static final String REGISTRY_FILTER = "(registry.name=" + CommonTestAnnotations.SCHEMA_REGISTRY_NAME + ")";
    private static final String OBJECT_ID = "diagnosed-package";
    private static final String NS_URI = "http://test/diagnostics/a";

    @Test
    @DisplayName("Diagnostics are written where content is frozen, and change nothing but themselves")
    @SchemaRegistryServiceSetup
    void diagnosticsAreWrittenInAFinalStage(
            @InjectService(cardinality = 0, filter = REGISTRY_FILTER) ServiceAware<RegistryService> registryAware)
            throws Exception {
        RegistryService<EPackage> registryService = registryAware.waitForService(5000);
        assertNotNull(registryService);

        upload(registryService, CommonTestAnnotations.STAGE_DRAFT);
        registryService.transitionToStage(SCOPE, OBJECT_ID, CommonTestAnnotations.STAGE_DRAFT,
                CommonTestAnnotations.STAGE_APPROVED);
        registryService.transitionToStage(SCOPE, OBJECT_ID, CommonTestAnnotations.STAGE_APPROVED,
                CommonTestAnnotations.STAGE_RELEASE);
        ObjectMetadata released = registryService.getMetadataFromStage(SCOPE, CommonTestAnnotations.STAGE_RELEASE,
                OBJECT_ID);
        assertNotNull(released);
        Instant lastChange = released.getLastChangeTime();
        String contentHash = released.getContentHash();

        // the content bar of the final stage stands
        EPackage changed = EcoreFactory.eINSTANCE.createEPackage();
        changed.setName("renamed");
        changed.setNsURI(NS_URI);
        changed.setNsPrefix("renamed");
        InvocationTargetException refused = assertThrows(InvocationTargetException.class,
                () -> registryService.updateInStage(SCOPE, CommonTestAnnotations.STAGE_RELEASE, changed, OBJECT_ID, "2")
                        .getValue());
        assertTrue(refused.getCause() instanceof StagePolicyException,
                "a content update in the final stage is a policy refusal, got " + refused.getCause());

        // the diagnostics write passes the same stage
        Diagnostic finding = ManagementFactory.eINSTANCE.createDiagnostic();
        finding.setCode("unresolved-reference");
        finding.setTarget("//Person/address");
        finding.setSeverity(DiagnosticSeverity.WARNING);
        finding.setMessage("http://test/diagnostics/b is not visible in release");
        ObjectMetadata written = registryService.updateDiagnostics(SCOPE, CommonTestAnnotations.STAGE_RELEASE,
                OBJECT_ID, "reference-check", List.of(finding)).getValue();
        assertNotNull(written);
        assertEquals(1, written.getDiagnostics().size());
        assertEquals("reference-check", written.getDiagnostics().get(0).getProducer());
        assertNotNull(written.getDiagnostics().get(0).getId(), "the id was minted");

        // ... and changed nothing but the diagnostics
        ObjectMetadata reread = registryService.getMetadataFromStage(SCOPE, CommonTestAnnotations.STAGE_RELEASE,
                OBJECT_ID);
        assertEquals(1, reread.getDiagnostics().size(), "a fresh read shows the finding");
        assertEquals("//Person/address", reread.getDiagnostics().get(0).getTarget());
        assertEquals("1", reread.getVersion(), "the version is untouched");
        assertEquals(contentHash, reread.getContentHash(), "the content hash is untouched");
        assertEquals(lastChange, reread.getLastChangeTime(), "lastChangeTime is untouched");

        // ... and the listing shows it, so a 409 on read is no longer the first signal
        List<ObjectMetadata> listed = registryService.listInStage(SCOPE, CommonTestAnnotations.STAGE_RELEASE);
        ObjectMetadata inListing = listed.stream().filter(m -> OBJECT_ID.equals(m.getObjectId())).findFirst()
                .orElseThrow();
        assertEquals(1, inListing.getDiagnostics().size(), "the listing carries the diagnostics");
        assertEquals(DiagnosticSeverity.WARNING, inListing.getDiagnostics().get(0).getSeverity());

        // the draft copy of the same object is a different object as far as findings go
        ObjectMetadata draft = registryService.getMetadataFromStage(SCOPE, CommonTestAnnotations.STAGE_DRAFT,
                OBJECT_ID);
        assertTrue(draft.getDiagnostics().isEmpty(), "diagnostics are per (scope, registry, stage, objectId)");
    }

    private void upload(RegistryService<EPackage> registryService, String stage)
            throws InvocationTargetException, InterruptedException {
        EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
        pkg.setName("diagnosed");
        pkg.setNsURI(NS_URI);
        pkg.setNsPrefix("diagnosed");

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(OBJECT_ID);
        metadata.setObjectName("diagnosed");
        metadata.setVersion("1");
        metadata.getProperties().put(WorkflowConstants.NS_URI_METADATA_PROPERTY, NS_URI);

        registryService.uploadToStage(SCOPE, stage, pkg, metadata).getValue();
    }
}
