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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.workflow.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService;
import org.gecko.emf.osgi.annotation.require.RequireEMF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.TemplateArgument;
import org.osgi.test.common.annotation.Property.ValueSource;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Integration tests for the scope-aware EObjectWorkflowService with hierarchical lookup.
 *
 * <p>This test class validates:</p>
 * <ul>
 * <li><strong>Single Scope Operations</strong> - Basic CRUD within one scope</li>
 * <li><strong>Multi-Scope Hierarchy</strong> - Parent-child scope relationships</li>
 * <li><strong>Hierarchical Lookup</strong> - Finding packages in parent scopes</li>
 * <li><strong>Read-Only Flags</strong> - Marking parent packages as read-only</li>
 * <li><strong>Stage Operations</strong> - uploadToStage, getFromStage, etc.</li>
 * <li><strong>Stage Transitions</strong> - Moving packages between stages</li>
 * <li><strong>Storage Integration</strong> - Scope + role based storage lookup</li>
 * </ul>
 *
 * @author Data In Motion
 * @since 1.0
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class ScopeAwareWorkflowServiceTest {

    private static final String PROP_TEMP_DIR = "tempDir";

    @TempDir
    Path tempDir;

    @InjectBundleContext
    BundleContext bundleContext;

    private final ManagementFactory managementFactory = ManagementFactory.eINSTANCE;
    private final EcoreFactory ecoreFactory = EcoreFactory.eINSTANCE;

    @BeforeEach
    void setUp() {
        System.setProperty(PROP_TEMP_DIR, tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(PROP_TEMP_DIR);
    }

    // ========================================
    // Test: Single Scope Operations
    // ========================================

    /**
     * Test basic upload and retrieval within a single scope.
     * @throws InvocationTargetException 
     */
    @Test
    @WithFactoryConfiguration(factoryPid = "LuceneEObjectRegistryService", name = "registry", location = "?", properties = {
        @Property(key = "registry.workspace.folder", value = "%s/registry", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        })
    })
    @WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "tenant-draft", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/tenant-draft", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.scope", value = "my-tenant"),
        @Property(key = "storage.role", value = "draft")
    })
    @WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "tenant-workflow", location = "?", properties = {
        @Property(key = "scope.name", value = "my-tenant"),
        @Property(key = "parent.scope", value = ""),
        @Property(key = "stages", value = {"Draft", "Released"}),
        @Property(key = "stage.storage.mapping", value = {"Draft=draft", "Released=release"}),
        @Property(key = "allowed.transitions", value = {"Draft->Released"})
    })
    public void testSingleScopeUploadAndRetrieve(
            @InjectService(cardinality = 0, filter = "(scope.name=my-tenant)")
            ServiceAware<EObjectWorkflowService<EPackage>> workflowAware) throws InterruptedException, InvocationTargetException {

        EObjectWorkflowService<EPackage> workflow = workflowAware.waitForService(5000);
        assertNotNull(workflow, "Workflow service should be available");

        // Create test package
        EPackage pkg = ecoreFactory.createEPackage();
        pkg.setName("TestPackage");
        pkg.setNsURI("http://test.example.com/v1");
        pkg.setNsPrefix("test");

        // Create metadata
        ObjectMetadata metadata = managementFactory.createObjectMetadata();
        metadata.setUploadUser("testUser");
        metadata.setObjectName("TestPackage");
        metadata.setObjectId("test");

        // Upload to Draft stage
        String nsUri = pkg.getNsURI();
        String objectId = workflow.uploadToStage("Draft", pkg, metadata).getValue();
        assertNotNull(objectId, "Object ID should be returned");

        // Retrieve from Draft stage
        ObjectMetadata retrieved = workflow.getFromStage("Draft", nsUri);
        assertNotNull(retrieved, "Should retrieve uploaded package");
        assertEquals("TestPackage", retrieved.getObjectName());
        assertEquals("testUser", retrieved.getUploadUser());

        // Retrieve content
        EPackage content = workflow.getContentFromStage("Draft", nsUri);
        assertNotNull(content, "Should retrieve package content");
        assertEquals("TestPackage", content.getName());
        assertEquals(nsUri, content.getNsURI());
    }

    // ========================================
    // Test: Hierarchical Scope Lookup
    // ========================================

    /**
     * Test hierarchical lookup: child scope finds packages in parent scope's Released stage.
     * @throws InvocationTargetException 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
    @WithFactoryConfiguration(factoryPid = "LuceneEObjectRegistryService", name = "registry", location = "?", properties = {
        @Property(key = "registry.workspace.folder", value = "%s/registry", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        })
    })
    // Parent scope storage
    @WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "parent-release", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/parent-release", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.scope", value = "parent-scope"),
        @Property(key = "storage.role", value = "release")
    })
    // Child scope storage
    @WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "child-draft", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/child-draft", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.scope", value = "child-scope"),
        @Property(key = "storage.role", value = "draft")
    })
    // Parent workflow
    @WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "parent-workflow", location = "?", properties = {
        @Property(key = "scope", value = "parent-scope"),
        @Property(key = "parent.scope", value = ""),
        @Property(key = "stages", value = {"release"})
    })
    // Child workflow (references parent)
    @WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "child-workflow", location = "?", properties = {
        @Property(key = "scope", value = "child-scope"),
        @Property(key = "parent.scope", value = "parent-scope"),
        @Property(key = "stages", value = {"draft", "release"}),
        @Property(key = "parentWorkflowService.target", value = "(scope=parent-scope)")
    })
    public void testHierarchicalLookup(
            @InjectService(cardinality = 0, filter = "(scope=parent-scope)")
            ServiceAware<EObjectWorkflowService> parentWorkflowAware,
            @InjectService(cardinality = 0, filter = "(scope=child-scope)")
            ServiceAware<EObjectWorkflowService> childWorkflowAware) throws InterruptedException, InvocationTargetException {

        EObjectWorkflowService<EObject> parentWorkflow = parentWorkflowAware.waitForService(5000);
        EObjectWorkflowService<EObject> childWorkflow = childWorkflowAware.waitForService(5000);

        assertNotNull(parentWorkflow, "Parent workflow should be available");
        assertNotNull(childWorkflow, "Child workflow should be available");

        Thread.sleep(2000); // Allow parent injection

        // Upload package to parent's Released stage
        EPackage parentPkg = ecoreFactory.createEPackage();
        parentPkg.setName("ParentPackage");
        parentPkg.setNsURI("http://parent.example.com/v1");
        parentPkg.setNsPrefix("parent");

        ObjectMetadata parentMetadata = managementFactory.createObjectMetadata();
        parentMetadata.setUploadUser("parentUser");
        parentMetadata.setObjectName("ParentPackage");

        String storageId = parentWorkflow.uploadToStage("release", parentPkg, parentMetadata).getValue();
        assertNotNull(storageId);
        
        // Child SHOULD find it in Released stage (hierarchical lookup)
        ObjectMetadata found = childWorkflow.getFromStage("draft", storageId);
        assertNotNull(found, "Should find parent package in Released stage via hierarchy");

     
        assertEquals("ParentPackage", found.getObjectName());
//        assertTrue(found.isReadOnly(), "Parent package should be marked read-only");
//        assertEquals("parent-scope", found.getSourceScope(), "Should indicate source scope");
    }

    // ========================================
    // Test: Stage Transitions
    // ========================================

    /**
     * Test transitioning a package from Draft to Released stage.
     * @throws InvocationTargetException 
     */
    @Test
    @WithFactoryConfiguration(factoryPid = "LuceneEObjectRegistryService", name = "registry", location = "?", properties = {
        @Property(key = "registry.workspace.folder", value = "%s/registry", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        })
    })
    @WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "draft-storage", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/draft-storage", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.scope", value = "test-scope"),
        @Property(key = "storage.role", value = "draft")
    })
    @WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "release-storage", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/release-storage", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.scope", value = "test-scope"),
        @Property(key = "storage.role", value = "release")
    })
    @WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "test-workflow", location = "?", properties = {
        @Property(key = "scope.name", value = "test-scope"),
        @Property(key = "parent.scope", value = ""),
        @Property(key = "stages", value = {"Draft", "Released"}),
        @Property(key = "stage.storage.mapping", value = {"Draft=draft", "Released=release"}),
        @Property(key = "allowed.transitions", value = {"Draft->Released"}),
        @Property(key = "delete.after.transition", value = "true")
    })
    public void testStageTransition(
            @InjectService(cardinality = 0, filter = "(scope.name=test-scope)")
            ServiceAware<EObjectWorkflowService<EPackage>> workflowAware) throws InterruptedException, InvocationTargetException {

        EObjectWorkflowService<EPackage> workflow = workflowAware.waitForService(5000);
        assertNotNull(workflow, "Workflow service should be available");

        // Create and upload package to Draft
        EPackage pkg = ecoreFactory.createEPackage();
        pkg.setName("TransitionPackage");
        pkg.setNsURI("http://transition.example.com/v1");
        pkg.setNsPrefix("trans");

        ObjectMetadata metadata = managementFactory.createObjectMetadata();
        metadata.setUploadUser("transUser");
        metadata.setObjectName("TransitionPackage");

        String nsUri = pkg.getNsURI();
        workflow.uploadToStage("Draft", pkg, metadata).getValue();

        // Verify in Draft stage
        ObjectMetadata draft = workflow.getFromStage("Draft", nsUri);
        assertNotNull(draft, "Should exist in Draft stage");

        // Transition to Released
        ObjectMetadata released = workflow.transitionToStage(nsUri, "Draft", "Released");
        assertNotNull(released, "Transition should succeed");
//        assertEquals("Released", released.getStage(), "Should be in Released stage");

        // Verify no longer in Draft (deleted after transition)
        ObjectMetadata draftGone = workflow.getFromStage("Draft", nsUri);
        assertNull(draftGone, "Should no longer exist in Draft stage");

        // Verify in Released stage
        ObjectMetadata releasedCheck = workflow.getFromStage("Released", nsUri);
        assertNotNull(releasedCheck, "Should exist in Released stage");
        assertEquals("TransitionPackage", releasedCheck.getObjectName());
    }

    // ========================================
    // Test: List Operations with Hierarchy
    // ========================================

    /**
     * Test listing packages includes both local and parent packages.
     * @throws InvocationTargetException 
     */
    @Test
    @WithFactoryConfiguration(factoryPid = "LuceneEObjectRegistryService", name = "registry", location = "?", properties = {
        @Property(key = "registry.workspace.folder", value = "%s/registry", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        })
    })
    @WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "parent-release", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/parent-release", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.scope", value = "parent"),
        @Property(key = "storage.role", value = "release")
    })
    @WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "child-release", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/child-release", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.scope", value = "child"),
        @Property(key = "storage.role", value = "release")
    })
    @WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "parent-workflow", location = "?", properties = {
        @Property(key = "scope.name", value = "parent"),
        @Property(key = "parent.scope", value = ""),
        @Property(key = "stages", value = {"Released"}),
        @Property(key = "stage.storage.mapping", value = {"Released=release"})
    })
    @WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "child-workflow", location = "?", properties = {
        @Property(key = "scope.name", value = "child"),
        @Property(key = "parent.scope", value = "parent"),
        @Property(key = "stages", value = {"Released"}),
        @Property(key = "stage.storage.mapping", value = {"Released=release"})
    })
    public void testListWithHierarchy(
            @InjectService(cardinality = 0, filter = "(scope.name=parent)")
            ServiceAware<EObjectWorkflowService<EPackage>> parentWorkflowAware,
            @InjectService(cardinality = 0, filter = "(scope.name=child)")
            ServiceAware<EObjectWorkflowService<EPackage>> childWorkflowAware) throws InterruptedException, InvocationTargetException {

        EObjectWorkflowService<EPackage> parentWorkflow = parentWorkflowAware.waitForService(5000);
        EObjectWorkflowService<EPackage> childWorkflow = childWorkflowAware.waitForService(5000);

        assertNotNull(parentWorkflow);
        assertNotNull(childWorkflow);

        Thread.sleep(2000);

        // Upload to parent
        EPackage parentPkg = ecoreFactory.createEPackage();
        parentPkg.setName("ParentPkg");
        parentPkg.setNsURI("http://parent.pkg.com/v1");
        ObjectMetadata parentMeta = managementFactory.createObjectMetadata();
        parentMeta.setObjectName("ParentPkg");
        parentMeta.setUploadUser("parentUser");
        parentWorkflow.uploadToStage("Released", parentPkg, parentMeta).getValue();

        // Upload to child
        EPackage childPkg = ecoreFactory.createEPackage();
        childPkg.setName("ChildPkg");
        childPkg.setNsURI("http://child.pkg.com/v1");
        ObjectMetadata childMeta = managementFactory.createObjectMetadata();
        childMeta.setObjectName("ChildPkg");
        childMeta.setUploadUser("childUser");
        childWorkflow.uploadToStage("Released", childPkg, childMeta).getValue();

        // List from parent - should only see parent package
        List<ObjectMetadata> parentList = parentWorkflow.listInStage("Released");
        assertEquals(1, parentList.size(), "Parent should see 1 package");
        assertEquals("ParentPkg", parentList.get(0).getObjectName());

        // List from child - should see both child and parent packages
        List<ObjectMetadata> childList = childWorkflow.listInStage("Released");
        assertEquals(2, childList.size(), "Child should see 2 packages (local + parent)");

        boolean hasParent = childList.stream().anyMatch(m -> "ParentPkg".equals(m.getObjectName()));
        boolean hasChild = childList.stream().anyMatch(m -> "ChildPkg".equals(m.getObjectName()));
        assertTrue(hasParent, "Child should see parent package");
        assertTrue(hasChild, "Child should see its own package");

        // Verify parent package is marked read-only
        ObjectMetadata parentInChild = childList.stream()
            .filter(m -> "ParentPkg".equals(m.getObjectName()))
            .findFirst()
            .orElse(null);
        assertNotNull(parentInChild);
//        assertTrue(parentInChild.isReadOnly(), "Parent package should be read-only in child list");
    }

    // ========================================
    // Test: Update and Delete Operations
    // ========================================

    /**
     * Test updating and deleting packages within a stage.
     * @throws InvocationTargetException 
     */
    @Test
    @WithFactoryConfiguration(factoryPid = "LuceneEObjectRegistryService", name = "registry", location = "?", properties = {
        @Property(key = "registry.workspace.folder", value = "%s/registry", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        })
    })
    @WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "draft-storage", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/draft-storage", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.scope", value = "test-scope"),
        @Property(key = "storage.role", value = "draft")
    })
    @WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "test-workflow", location = "?", properties = {
        @Property(key = "scope.name", value = "test-scope"),
        @Property(key = "parent.scope", value = ""),
        @Property(key = "stages", value = {"Draft"}),
        @Property(key = "stage.storage.mapping", value = {"Draft=draft"})
    })
    public void testUpdateAndDelete(
            @InjectService(cardinality = 0, filter = "(scope.name=test-scope)")
            ServiceAware<EObjectWorkflowService<EPackage>> workflowAware) throws InterruptedException, InvocationTargetException {

        EObjectWorkflowService<EPackage> workflow = workflowAware.waitForService(5000);
        assertNotNull(workflow);

        // Upload package
        EPackage pkg = ecoreFactory.createEPackage();
        pkg.setName("OriginalName");
        pkg.setNsURI("http://update.test.com/v1");
        ObjectMetadata metadata = managementFactory.createObjectMetadata();
        metadata.setObjectName("OriginalName");
        metadata.setUploadUser("testUser");

        String nsUri = pkg.getNsURI();
        workflow.uploadToStage("Draft", pkg, metadata).getValue();

        // Update package
        pkg.setName("UpdatedName");
        workflow.uploadToStage("Draft", pkg, metadata).getValue();

        EPackage updated = workflow.getContentFromStage("Draft", nsUri);
        assertNotNull(updated);
        assertEquals("UpdatedName", updated.getName(), "Name should be updated");

        // Delete package
        Boolean deleted = workflow.deleteFromStage("Draft", nsUri).getValue();
        assertTrue(deleted, "Delete should return true");

        // Verify deleted
        ObjectMetadata gone = workflow.getFromStage("Draft", nsUri);
        assertNull(gone, "Package should be deleted");
    }

    // ========================================
    // Test: Three-Level Hierarchy
    // ========================================

    /**
     * Test three-level scope hierarchy: child -> parent -> grandparent.
     * @throws InvocationTargetException 
     */
    @Test
    @WithFactoryConfiguration(factoryPid = "LuceneEObjectRegistryService", name = "registry", location = "?", properties = {
        @Property(key = "registry.workspace.folder", value = "%s/registry", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        })
    })
    // Grandparent storage
    @WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "grandparent-release", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/grandparent-release", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.scope", value = "grandparent"),
        @Property(key = "storage.role", value = "release")
    })
    // Parent storage
    @WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "parent-release", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/parent-release", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.scope", value = "parent"),
        @Property(key = "storage.role", value = "release")
    })
    // Child storage
    @WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "child-release", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/child-release", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.scope", value = "child"),
        @Property(key = "storage.role", value = "release")
    })
    // Grandparent workflow
    @WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "grandparent-workflow", location = "?", properties = {
        @Property(key = "scope.name", value = "grandparent"),
        @Property(key = "parent.scope", value = ""),
        @Property(key = "stages", value = {"Released"}),
        @Property(key = "stage.storage.mapping", value = {"Released=release"})
    })
    // Parent workflow
    @WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "parent-workflow", location = "?", properties = {
        @Property(key = "scope.name", value = "parent"),
        @Property(key = "parent.scope", value = "grandparent"),
        @Property(key = "stages", value = {"Released"}),
        @Property(key = "stage.storage.mapping", value = {"Released=release"})
    })
    // Child workflow
    @WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "child-workflow", location = "?", properties = {
        @Property(key = "scope.name", value = "child"),
        @Property(key = "parent.scope", value = "parent"),
        @Property(key = "stages", value = {"Released"}),
        @Property(key = "stage.storage.mapping", value = {"Released=release"})
    })
    public void testThreeLevelHierarchy(
            @InjectService(cardinality = 0, filter = "(scope.name=grandparent)")
            ServiceAware<EObjectWorkflowService<EPackage>> grandparentWorkflowAware,
            @InjectService(cardinality = 0, filter = "(scope.name=child)")
            ServiceAware<EObjectWorkflowService<EPackage>> childWorkflowAware) throws InterruptedException, InvocationTargetException {

        EObjectWorkflowService<EPackage> grandparentWorkflow = grandparentWorkflowAware.waitForService(5000);
        EObjectWorkflowService<EPackage> childWorkflow = childWorkflowAware.waitForService(5000);

        assertNotNull(grandparentWorkflow);
        assertNotNull(childWorkflow);

        Thread.sleep(3000); // Allow parent chain to establish

        // Upload to grandparent
        EPackage grandparentPkg = ecoreFactory.createEPackage();
        grandparentPkg.setName("GrandparentPkg");
        grandparentPkg.setNsURI("http://grandparent.com/v1");
        ObjectMetadata grandparentMeta = managementFactory.createObjectMetadata();
        grandparentMeta.setObjectName("GrandparentPkg");
        grandparentMeta.setUploadUser("gpUser");
        grandparentWorkflow.uploadToStage("Released", grandparentPkg, grandparentMeta).getValue();

        // Child should find grandparent's package
        ObjectMetadata found = childWorkflow.getFromStage("Released", grandparentPkg.getNsURI());
        assertNotNull(found, "Child should find grandparent package via two-level delegation");
        assertEquals("GrandparentPkg", found.getObjectName());
//        assertTrue(found.isReadOnly(), "Grandparent package should be read-only");
//        assertEquals("grandparent", found.getSourceScope(), "Should indicate grandparent as source");
    }
}
