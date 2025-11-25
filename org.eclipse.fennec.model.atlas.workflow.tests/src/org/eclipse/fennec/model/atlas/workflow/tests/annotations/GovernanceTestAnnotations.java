/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Mark Hoffmann - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.workflow.tests.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.fennec.model.atlas.mgmt.annotations.MacCapabilityConstants;
import org.gecko.emf.osgi.annotation.require.RequireEMF;
import org.osgi.annotation.bundle.Requirement;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.typedevent.annotations.RequireTypedEvent;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.Scalar;
import org.osgi.test.common.annotation.Property.TemplateArgument;
import org.osgi.test.common.annotation.Property.ValueSource;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;

/**
 * Test configuration annotations for Governance workflow tests.
 * 
 * <p>These annotations provide predefined OSGi configurations for governance workflow
 * integration tests, setting up the complete stack including storage services,
 * registry service, and workflow service.</p>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
@RequireEMF
@RequireConfigurationAdmin
@Requirement(namespace = MacCapabilityConstants.NAMESPACE_MAC_MANAGEMENT, name = MacCapabilityConstants.CAP_EOBJECT_STORAGE, filter = "(storage.backend=file)")
public class GovernanceTestAnnotations {
	
	public static final String PROP_TEMP_DIR = "tempDir";

    /**
     * EObject Workflow Service factory PID.
     */
    public static final String PID_WORKFLOW_SERVICE = "EObjectWorkflowService";

    /**
     * Governance Compliance Service PID.
     */
    public static final String PID_COMPLIANCE_SERVICE = "GovernanceComplianceService";
    
    /**
     * Shared Lucene Registry Service PID (not factory).
     */
    public static final String PID_SHARED_REGISTRY = "LuceneEObjectRegistryService";
    
    /**
     * File Object Storage Service factory PID.
     */
    public static final String PID_FILE_STORAGE = "FileObjectStorage";
    
    /**
     * File Object Storage Registry Service factory PID.
     */
    public static final String PID_STORAGE_REGISTRY = "BasicStorageRegistry";
    
    /**
     * Initializer Service factory PID.
     */
    public static final String PID_INITIALIZER = "GovernanceInitializerComponent";
    
    @WithFactoryConfiguration(factoryPid = PID_INITIALIZER, name = "initializer", location = "?", properties = {
            @Property(key = "draftStorage.target", value = "(storage.role=draft)"),
            @Property(key = "releaseStorage.target", value = "(storage.role=release)")
        })
    @FullWorkflowWithDocumentationSetup
    public @interface InitializerConfigurationSetup{}
    
    /**
     * Basic shared registry configuration.
     * 
     * <p>This annotation configures a LuceneEObjectRegistryService instance with:</p>
     * <ul>
     * <li>Storage backend tracking enabled</li>
     * <li>Debug logging enabled for troubleshooting</li>
     * <li>Workspace folder based on system property (typically temp directory)</li>
     * </ul>
     */
    @WithFactoryConfiguration(factoryPid = PID_SHARED_REGISTRY, name = "shared-registry", location = "?", properties = {
        @Property(key = "registry.workspace.folder", value = "%s/shared-registry", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.backend.tracking", value = "true"),
        @Property(key = "initial.index.capacity", value = "1000"),
        @Property(key = "enable.debug.logging", value = "true")
    })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RegistryConfiguration {}
    
    /**
     * Documentation-specific registry configuration.
     * 
     * <p>This annotation configures a separate LuceneEObjectRegistryService instance for governance documentation:</p>
     * <ul>
     * <li>Isolated workspace folder for documentation metadata</li>
     * <li>Separate registry property "documentation" for service targeting</li>
     * <li>Prevents governance documentation from appearing in main object queries</li>
     * <li>Maintains clean separation between managed objects and governance artifacts</li>
     * <li>Enables independent scaling and optimization for documentation storage</li>
     * </ul>
     * 
     * <p><strong>Architecture Benefits:</strong></p>
     * <ul>
     * <li><code>listApprovedObjects()</code> only returns actual managed objects (EPackages, Routes, etc.)</li>
     * <li>Governance documentation queries are isolated to documentation-specific operations</li>
     * <li>Registry performance optimized for each domain (objects vs documentation)</li>
     * <li>Clear separation of concerns in the storage architecture</li>
     * <li>Documentation storage targets this registry via <code>registry.target=(registry=documentation)</code></li>
     * </ul>
     */
    @WithFactoryConfiguration(factoryPid = PID_SHARED_REGISTRY, name = "doc-registry", location = "?", properties = {
    		@Property(key = "registry.workspace.folder", value = "%s/doc-registry", templateArguments = {
    				@TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
    		}),
    		@Property(key = "registry", value = "documentation"),
    		@Property(key = "storage.backend.tracking", value = "true"),
    		@Property(key = "initial.index.capacity", value = "1000"),
    		@Property(key = "enable.debug.logging", value = "true")
    })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DocumentationRegistryConfiguration {}

    /**
     * Role-based storage setup for governance workflows.
     * 
     * <p>This setup creates the required storage infrastructure:</p>
     * <ul>
     * <li>Draft storage with role "draft" (uses main registry)</li>
     * <li>Release storage with role "release" (uses main registry)</li>
     * <li>Documentation storage with role "documentation" (uses separate documentation registry)</li>
     * <li>Main registry for managed objects (EPackages, Routes, etc.)</li>
     * <li>Separate documentation registry for governance documentation objects</li>
     * </ul>
     */
    @RegistryConfiguration
    @DocumentationRegistryConfiguration
    @WithFactoryConfiguration(factoryPid = PID_FILE_STORAGE, name = "draft", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/draft-storage", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.role", value = "draft")
    })
    @WithFactoryConfiguration(factoryPid = PID_FILE_STORAGE, name = "release", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/release-storage", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.role", value = "release")
    })
    @WithFactoryConfiguration(factoryPid = PID_FILE_STORAGE, name = "documentation", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/documentation-storage", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.role", value = "documentation"),
        @Property(key = "registry.target", value = "(registry=documentation)")
    })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface WorkflowStorageSetup {}
    
    @WorkflowStorageSetup
    @WithFactoryConfiguration(factoryPid = PID_STORAGE_REGISTRY, name = "workflow", location = "?", properties = {
            @Property(key = "storage.registry.name", value = "basic"),
            @Property(key = "storage.target", value = "(|(storage.role=documentation)(storage.role=release)(storage.role=draft))"),
            @Property(key = "storage.cardinality.minimum", scalar = Scalar.Integer, value = "3")
        })
    @Retention(RetentionPolicy.RUNTIME)
    @RequireTypedEvent
    public @interface StorageRegistrySetup {}
    
    @RegistryConfiguration
    @WithFactoryConfiguration(factoryPid = PID_FILE_STORAGE, name = "release", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/release-storage", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.role", value = "release")
    })
    @Retention(RetentionPolicy.RUNTIME)
    @RequireTypedEvent
    public @interface PostActionStorageSetup {}
    
    /**
     * Creates a documentation-specific file storage service for testing.
     * This storage service is configured with the "documentation" role.
     */
	@WithFactoryConfiguration(factoryPid = PID_FILE_STORAGE, name = "documentationStorage", location = "?", properties = {
	        @Property(key = "workspace.folder", value = "%s", templateArguments = {
	            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
	        }),
	        @Property(key = "storage.role", value = "documentation"),
	        @Property(key = "registry.target", value = "(registry=documentation)")
	    })
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DocumentationStorageSetup {
    }

    /**
     * Creates a comprehensive test setup for documentation service tests including
     * documentation storage and governance factory requirements.
     */
	
    @DocumentationStorageSetup
    @DocumentationRegistryConfiguration
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DocumentationServiceTestSetup {
    }
    
    /**
     * Creates a documentation-specific file storage service for testing.
     * This storage service is configured with the "documentation" role.
     */
    @WithFactoryConfiguration(factoryPid = PID_FILE_STORAGE, name = "complianceStorage", location = "?", properties = {
    		@Property(key = "workspace.folder", value = "%s/draft-storage", templateArguments = {
    				@TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
    		}),
    		@Property(key = "storage.role", value = "draft")
    })
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ComplianceStorageSetup {
    }
    
    /**
     * Creates a comprehensive test setup for documentation service tests including
     * documentation storage and governance factory requirements.
     */
    
    @ComplianceStorageSetup
    @DocumentationStorageSetup
    @DocumentationRegistryConfiguration
    @RegistryConfiguration
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ComplianceServiceTestSetup {
    }
    
    @WorkflowStorageSetup
    @RegistryConfiguration
    @BasicWorkflowConfiguration
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FullWorkflowWithDocumentationSetup {
    }

    /**
     * Basic workflow service configuration.
     * 
     * <p>Configures an EObjectWorkflowService instance with:</p>
     * <ul>
     * <li>Standard transaction timeout (30 seconds)</li>
     * <li>Auto-rollback enabled</li>
     * <li>Archive drafts on approval (instead of deletion)</li>
     * <li>No mandatory compliance checks</li>
     * </ul>
     */
    @WithFactoryConfiguration(factoryPid = PID_WORKFLOW_SERVICE, name = "basic-workflow", location = "?", properties = {
        @Property(key = "workflow.id", value = "basic-test-workflow"),
        @Property(key = "archive.drafts.on.approval", value = "true"),
        @Property(key = "enable.auto.rollback", value = "true"),
        @Property(key = "transaction.timeout.ms", value = "30000"),
        @Property(key = "require.compliance.checks", value = "false"),
        @Property(key = "max.concurrent.approvals", value = "10"),
        @Property(key = "enable.detailed.logging", value = "true"),
        @Property(key = "enable.auto.compliance.check", value = "false"),
        @Property(key = "require.approved.governance.documentation", value = "false")
    })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface BasicWorkflowConfiguration {}

    /**
     * Complete governance workflow test setup.
     * 
     * <p>This meta-annotation combines:</p>
     * <ul>
     * <li>{@link WorkflowStorageSetup} - All required storage services</li>
     * <li>{@link BasicWorkflowConfiguration} - Basic workflow service</li>
     * </ul>
     * 
     * <p>Use this for testing the complete workflow lifecycle.</p>
     */
    @WorkflowStorageSetup
    @BasicWorkflowConfiguration
    @Retention(RetentionPolicy.RUNTIME)
    public @interface BasicWorkflowSetup {}

    
    /**
     * Governance-gated workflow configuration with mandatory compliance.
     * 
     * <p>Enforces governance documentation approval before object approval.</p>
     */
    @WithFactoryConfiguration(factoryPid = PID_WORKFLOW_SERVICE, name = "governance-gated-workflow", location = "?", properties = {
        @Property(key = "workflow.id", value = "governance-gated-test-workflow"),
        @Property(key = "draft.storage.filter", value = "(storage.role=draft)"),
        @Property(key = "approved.storage.filter", value = "(storage.role=release)"),
        @Property(key = "archive.drafts.on.approval", value = "true"),
        @Property(key = "enable.auto.rollback", value = "true"),
        @Property(key = "transaction.timeout.ms", value = "30000"),
        @Property(key = "require.compliance.checks", value = "false"),
        @Property(key = "max.concurrent.approvals", value = "10"),
        @Property(key = "enable.detailed.logging", value = "true"),
        @Property(key = "enable.auto.compliance.check", value = "true"),
        @Property(key = "require.approved.governance.documentation", value = "true")
    })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface GovernanceGatedWorkflowConfiguration {}



    
    /**
     * Complete governance-gated workflow test setup.
     * 
     * <p>This meta-annotation combines:</p>
     * <ul>
     * <li>{@link WorkflowStorageSetup} - All required storage services</li>
     * <li>{@link GovernanceGatedWorkflowConfiguration} - Governance-gated workflow service</li>
     * <li>Mock compliance service for testing governance integration</li>
     * </ul>
     * 
     * <p>Use this for testing the complete governance-gated workflow lifecycle.</p>
     */
    @WorkflowStorageSetup
    @GovernanceGatedWorkflowConfiguration
    @WithFactoryConfiguration(factoryPid = PID_COMPLIANCE_SERVICE, name = "mock-governance-compliance", location = "?", properties = {
        @Property(key = "service.ranking", value = "200"),
        @Property(key = "mock.mode", value = "true"),
        @Property(key = "mock.compliance_result", value = "COMPLIANT")
    })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface GovernanceGatedWorkflowSetup {}
    
    /**
     * State transition testing workflow configuration with **disabled** automatic compliance checks.
     * 
     * <h3>Purpose</h3>
     * <p>This annotation is specifically designed for testing governance documentation state machine 
     * transitions without interference from automatic background processes. It provides the same 
     * infrastructure as {@link GovernanceGatedWorkflowSetup} but with a critical difference:</p>
     * 
     * <h3>Key Difference from {@link GovernanceGatedWorkflowSetup}</h3>
     * <table border="1">
     * <tr><th>Feature</th><th>GovernanceGatedWorkflowSetup</th><th>StateTransitionWorkflowSetup</th></tr>
     * <tr><td><strong>Auto Compliance Check</strong></td><td>✅ Enabled (production-like)</td><td>❌ Disabled (test isolation)</td></tr>
     * <tr><td><strong>Use Case</strong></td><td>End-to-end workflow testing</td><td>State machine transition testing</td></tr>
     * <tr><td><strong>Concurrency</strong></td><td>Automatic async processes</td><td>Synchronous manual control</td></tr>
     * </table>
     * 
     * <h3>When to Use This Annotation</h3>
     * <ul>
     * <li><strong>✅ State Machine Testing</strong> - Testing individual governance documentation state transitions (DRAFT → IN_REVIEW → APPROVED)</li>
     * <li><strong>✅ Race Condition Avoidance</strong> - When you need deterministic, synchronous state control</li>
     * <li><strong>✅ Manual State Management</strong> - Tests that explicitly call setGovernanceDocumentationDraft/InReview/Approved</li>
     * <li><strong>✅ Transition Validation</strong> - Testing invalid transitions and state machine enforcement</li>
     * </ul>
     * 
     * <h3>When NOT to Use This Annotation</h3>
     * <ul>
     * <li><strong>❌ End-to-End Testing</strong> - Use {@link GovernanceGatedWorkflowSetup} for realistic production scenarios</li>
     * <li><strong>❌ Automatic Compliance Flow</strong> - Testing the automatic compliance check behavior itself</li>
     * <li><strong>❌ Performance Testing</strong> - Real-world async behavior testing</li>
     * </ul>
     * 
     * <h3>Race Condition Problem This Solves</h3>
     * <p>Without this annotation, tests experience this problematic sequence:</p>
     * <pre><code>
     * 1. Test uploads draft → automatic compliance check starts (async)
     * 2. Test manually sets state to DRAFT
     * 3. Test manually sets state to IN_REVIEW  
     * 4. Automatic compliance check overwrites state back to DRAFT (race condition!)
     * 5. Test tries DRAFT → APPROVED transition → FAILS (invalid transition)
     * </code></pre>
     * 
     * <p>With this annotation, the automatic compliance check is disabled, allowing clean manual state transitions.</p>
     * 
     * <h3>Configuration Details</h3>
     * <p>Key configuration differences from {@link GovernanceGatedWorkflowSetup}:</p>
     * <ul>
     * <li><code>enable.auto.compliance.check = false</code> (vs. true)</li>
     * <li><code>workflow.id = state-transition-test-workflow</code> (unique identifier)</li>
     * <li>All other governance infrastructure remains identical</li>
     * </ul>
     * 
     * <h3>Example Usage</h3>
     * <pre><code>
     * &#64;Test
     * &#64;StateTransitionWorkflowSetup
     * public void testGovernanceStateTransitions(
     *     &#64;InjectService(filter = "(workflow.id=state-transition-test-workflow)")
     *     EObjectWorkflowService workflowService
     * ) {
     *     // Upload draft (no automatic compliance check interference)
     *     String objectId = workflowService.uploadDraft(object, metadata).getValue();
     *     
     *     // Clean, deterministic state transitions
     *     workflowService.setGovernanceDocumentationDraft(objectId, user, reason);
     *     workflowService.setGovernanceDocumentationInReview(objectId, user, reason);  
     *     workflowService.setGovernanceDocumentationApproved(objectId, user, reason); // ✅ Works!
     * }
     * </code></pre>
     * 
     * @see GovernanceGatedWorkflowSetup for production-like end-to-end testing
     * @since 1.0.0
     * @author Documentation for AI and Developer Understanding
     */
    @WithFactoryConfiguration(factoryPid = PID_WORKFLOW_SERVICE, name = "state-transition-workflow", location = "?", properties = {
        @Property(key = "workflow.id", value = "state-transition-test-workflow"),
        @Property(key = "draft.storage.filter", value = "(storage.role=draft)"),
        @Property(key = "approved.storage.filter", value = "(storage.role=release)"),
        @Property(key = "archive.drafts.on.approval", value = "true"),
        @Property(key = "enable.auto.rollback", value = "true"),
        @Property(key = "transaction.timeout.ms", value = "30000"),
        @Property(key = "require.compliance.checks", value = "false"),
        @Property(key = "max.concurrent.approvals", value = "10"),
        @Property(key = "enable.detailed.logging", value = "true"),
        @Property(key = "enable.auto.compliance.check", value = "false"),  // 🔑 KEY DIFFERENCE
        @Property(key = "require.approved.governance.documentation", value = "true")
    })
    @WithFactoryConfiguration(factoryPid = PID_COMPLIANCE_SERVICE, name = "mock-state-transition-compliance", location = "?", properties = {
        @Property(key = "service.ranking", value = "200"),
        @Property(key = "mock.mode", value = "true"),
        @Property(key = "mock.compliance_result", value = "COMPLIANT")
    })
    @WorkflowStorageSetup
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface StateTransitionWorkflowSetup {}
}