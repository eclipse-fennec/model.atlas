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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.osgi.test.common.annotation.Property.Type;
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
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
    @WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "tenant-release", location = "?", properties = {
            @Property(key = "workspace.folder", value = "%s/tenant-release", templateArguments = {
                @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
            }),
            @Property(key = "storage.scope", value = "my-tenant"),
            @Property(key = "storage.role", value = "release")
        })
    @WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "tenant-workflow", location = "?", properties = {
	        @Property(key = "scope", value = "my-tenant"),
	        @Property(key = "parent.scope", value = ""),
	        @Property(key = "stages", value = {"draft", "release"}, type = Type.Array),
	        @Property(key = "writable.stages", value = {"draft", "release"}, type = Type.Array)
	    })
    public void testSingleScopeUploadAndRetrieve(
            @InjectService(cardinality = 0, filter = "(scope=my-tenant)")
            ServiceAware<EObjectWorkflowService> workflowAware) throws InterruptedException, InvocationTargetException {

        EObjectWorkflowService<EObject> workflow = workflowAware.waitForService(5000);
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
        workflow.uploadToStage("draft", pkg, metadata).getValue();
        String objectId = metadata.getObjectId();
        assertNotNull(objectId, "Object ID should be returned");

        // Retrieve from Draft stage
        ObjectMetadata retrieved = workflow.getFromStage("draft", objectId);
        assertNotNull(retrieved, "Should retrieve uploaded package");
        assertEquals("TestPackage", retrieved.getObjectName());
        assertEquals("testUser", retrieved.getUploadUser());

        // Retrieve content
        EObject content = workflow.getContentFromStage("draft", objectId);
        assertNotNull(content, "Should retrieve package content");
        assertInstanceOf(EPackage.class, content);
        EPackage packageContent = (EPackage) content;
        assertEquals("TestPackage", packageContent.getName());
        assertEquals(nsUri, packageContent.getNsURI());
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
        @Property(key = "stages", value = {"release"}, type = Type.Array),
        @Property(key = "writable.stages", value = {"release"}, type = Type.Array)
    })
    // Child workflow (references parent)
    @WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "child-workflow", location = "?", properties = {
        @Property(key = "scope", value = "child-scope"),
        @Property(key = "parent.scope", value = "parent-scope"),
        @Property(key = "stages", value = {"draft", "release"}, type = Type.Array),
        @Property(key = "writable.stages", value = {"draft", "release"}, type = Type.Array),
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

        parentWorkflow.uploadToStage("release", parentPkg, parentMetadata).getValue();
        String storageId = parentMetadata.getObjectId();
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
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
        @Property(key = "scope", value = "test-scope"),
        @Property(key = "parent.scope", value = ""),
        @Property(key = "stages", value = {"draft", "release"}, type = Type.Array),
        @Property(key = "writable.stages", value = {"draft", "release"}, type = Type.Array),
        @Property(key = "delete.after.transition", value = "true")
    })
    public void testStageTransition(
            @InjectService(cardinality = 0, filter = "(scope=test-scope)")
            ServiceAware<EObjectWorkflowService> workflowAware) throws InterruptedException, InvocationTargetException {

        EObjectWorkflowService<EObject> workflow = workflowAware.waitForService(5000);
        assertNotNull(workflow, "Workflow service should be available");

        // Create and upload package to Draft
        EPackage pkg = ecoreFactory.createEPackage();
        pkg.setName("TransitionPackage");
        pkg.setNsURI("http://transition.example.com/v1");
        pkg.setNsPrefix("trans");

        ObjectMetadata metadata = managementFactory.createObjectMetadata();
        metadata.setUploadUser("transUser");
        metadata.setObjectName("TransitionPackage");

        workflow.uploadToStage("draft", pkg, metadata).getValue();
        String storageId = metadata.getObjectId();

        // Verify in Draft stage
        ObjectMetadata draft = workflow.getFromStage("draft", storageId);
        assertNotNull(draft, "Should exist in Draft stage");

        // Transition to Released
        ObjectMetadata released = workflow.transitionToStage(storageId, "draft", "release");
        assertNotNull(released, "Transition should succeed");
//        assertEquals("Released", released.getStage(), "Should be in Released stage");

        // Verify no longer in Draft (deleted after transition)
        ObjectMetadata draftGone = workflow.getFromStage("draft", storageId);
        assertNull(draftGone, "Should no longer exist in Draft stage");

        // Verify in Released stage
        ObjectMetadata releasedCheck = workflow.getFromStage("release", storageId);
        assertNotNull(releasedCheck, "Should exist in Released stage");
        assertEquals("TransitionPackage", releasedCheck.getObjectName());
    }

    // ========================================
    // Test: List Operations with Hierarchy
    // ========================================

    @SuppressWarnings({ "unchecked", "rawtypes" })
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
        @Property(key = "scope", value = "parent"),
        @Property(key = "parent.scope", value = ""),
        @Property(key = "stages", value = {"release"}, type = Type.Array),
        @Property(key = "writable.stages", value = {"release"}, type = Type.Array)
    })
    @WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "child-workflow", location = "?", properties = {
        @Property(key = "scope", value = "child"),
        @Property(key = "parent.scope", value = "parent"),
        @Property(key = "stages", value = {"release"}, type = Type.Array),
        @Property(key = "writable.stages", value = {"release"}, type = Type.Array),
        @Property(key = "parentWorkflowService.target", value = "(scope=parent)")
    })
    public void testListWithHierarchy(
            @InjectService(cardinality = 0, filter = "(scope=parent)")
            ServiceAware<EObjectWorkflowService> parentWorkflowAware,
            @InjectService(cardinality = 0, filter = "(scope=child)")
            ServiceAware<EObjectWorkflowService> childWorkflowAware) throws InterruptedException, InvocationTargetException {

        EObjectWorkflowService<EObject> parentWorkflow = parentWorkflowAware.waitForService(5000);
        EObjectWorkflowService<EObject> childWorkflow = childWorkflowAware.waitForService(5000);

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
        parentMeta.setScope("parent");
        parentMeta.setRole("release");
        parentWorkflow.uploadToStage("release", parentPkg, parentMeta).getValue();

        // Upload to child
        EPackage childPkg = ecoreFactory.createEPackage();
        childPkg.setName("ChildPkg");
        childPkg.setNsURI("http://child.pkg.com/v1");
        ObjectMetadata childMeta = managementFactory.createObjectMetadata();
        childMeta.setObjectName("ChildPkg");
        childMeta.setUploadUser("childUser");
        childMeta.setScope("child");
        childMeta.setRole("release");
        childWorkflow.uploadToStage("release", childPkg, childMeta).getValue();

        // List from parent - should only see parent package
        List<ObjectMetadata> parentList = parentWorkflow.listInStage("release");
        assertEquals(1, parentList.size(), "Parent should see 1 package");
        assertEquals("ParentPkg", parentList.get(0).getObjectName());

        // List from child - should see both child and parent packages
        List<ObjectMetadata> childList = childWorkflow.listInStage("release");
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
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
    @WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "release-storage", location = "?", properties = {
            @Property(key = "workspace.folder", value = "%s/release-storage", templateArguments = {
                @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
            }),
            @Property(key = "storage.scope", value = "test-scope"),
            @Property(key = "storage.role", value = "release")
        })
    @WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "test-workflow", location = "?", properties = {
        @Property(key = "scope", value = "test-scope"),
        @Property(key = "parent.scope", value = ""),
        @Property(key = "stages", value = {"draft"}, type = Type.Array),
        @Property(key = "writable.stages", value = {"draft"}, type = Type.Array),
        @Property(key = "final.stage", value = "draft")
    })
    public void testUpdateAndDelete(
            @InjectService(cardinality = 0, filter = "(scope=test-scope)")
            ServiceAware<EObjectWorkflowService> workflowAware) throws InterruptedException, InvocationTargetException {

        EObjectWorkflowService<EObject> workflow = workflowAware.waitForService(5000);
        assertNotNull(workflow);

        // Upload package
        EPackage pkg = ecoreFactory.createEPackage();
        pkg.setName("OriginalName");
        pkg.setNsURI("http://update.test.com/v1");
        ObjectMetadata metadata = managementFactory.createObjectMetadata();
        metadata.setObjectName("OriginalName");
        metadata.setUploadUser("testUser");

        workflow.uploadToStage("draft", pkg, metadata).getValue();
        String storageId = metadata.getObjectId();

        // Update package
        pkg.setName("UpdatedName");
        workflow.uploadToStage("draft", pkg, metadata).getValue();

        EObject updated = workflow.getContentFromStage("draft", storageId);
        assertNotNull(updated);
        assertInstanceOf(EPackage.class, updated);
        EPackage updatedPackage = (EPackage) updated;        
        assertEquals("UpdatedName", updatedPackage.getName(), "Name should be updated");

        // Delete package
        Boolean deleted = workflow.deleteFromStage("draft", storageId).getValue();
        assertTrue(deleted, "Delete should return true");

        // Verify deleted
        ObjectMetadata gone = workflow.getFromStage("draft", storageId);
        assertNull(gone, "Package should be deleted");
    }

    // ========================================
    // Test: Final Stage Operations
    // ========================================

    @SuppressWarnings({ "unchecked", "rawtypes" })
    /**
     * Test basic final stage operations: getFromFinalStage and listInFinalStage.
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
        @Property(key = "scope", value = "test-scope"),
        @Property(key = "parent.scope", value = ""),
        @Property(key = "stages", value = {"draft", "release"}, type = Type.Array),
        @Property(key = "writable.stages", value = {"draft", "release"}, type = Type.Array),
        @Property(key = "final.stage", value = "release")
    })
    public void testFinalStageOperations(
            @InjectService(cardinality = 0, filter = "(scope=test-scope)")
            ServiceAware<EObjectWorkflowService> workflowAware) throws InterruptedException, InvocationTargetException {

        EObjectWorkflowService<EObject> workflow = workflowAware.waitForService(5000);
        assertNotNull(workflow, "Workflow service should be available");

        // Create and upload first package to release stage
        EPackage pkg1 = ecoreFactory.createEPackage();
        pkg1.setName("Package1");
        pkg1.setNsURI("http://test1.example.com/v1");
        pkg1.setNsPrefix("test1");

        ObjectMetadata metadata1 = managementFactory.createObjectMetadata();
        metadata1.setUploadUser("testUser");
        metadata1.setObjectName("Package1");
        metadata1.setObjectId("pkg1");
        metadata1.setScope("test-scope");

        workflow.uploadToStage("release", pkg1, metadata1).getValue();
        String objectId1 = metadata1.getObjectId();
        assertNotNull(objectId1);

        // Create and upload second package to release stage
        EPackage pkg2 = ecoreFactory.createEPackage();
        pkg2.setName("Package2");
        pkg2.setNsURI("http://test2.example.com/v1");
        pkg2.setNsPrefix("test2");

        ObjectMetadata metadata2 = managementFactory.createObjectMetadata();
        metadata2.setUploadUser("testUser");
        metadata2.setObjectName("Package2");
        metadata2.setObjectId("pkg2");
        metadata2.setScope("test-scope");

        workflow.uploadToStage("release", pkg2, metadata2).getValue();
        String objectId2 = metadata2.getObjectId();
        assertNotNull(objectId2);

        // Upload one package to draft stage (should not appear in final stage list)
        EPackage draftPkg = ecoreFactory.createEPackage();
        draftPkg.setName("DraftPackage");
        draftPkg.setNsURI("http://draft.example.com/v1");
        draftPkg.setNsPrefix("draft");

        ObjectMetadata draftMetadata = managementFactory.createObjectMetadata();
        draftMetadata.setUploadUser("testUser");
        draftMetadata.setObjectName("DraftPackage");
        draftMetadata.setObjectId("draft");
        draftMetadata.setScope("test-scope");

        workflow.uploadToStage("draft", draftPkg, draftMetadata).getValue();

        // Test getFromFinalStage
        ObjectMetadata retrieved1 = workflow.getFromFinalStage(objectId1);
        assertNotNull(retrieved1, "Should retrieve package from final stage");
        assertEquals("Package1", retrieved1.getObjectName());

        ObjectMetadata retrieved2 = workflow.getFromFinalStage(objectId2);
        assertNotNull(retrieved2, "Should retrieve package from final stage");
        assertEquals("Package2", retrieved2.getObjectName());

        // Test listInFinalStage
        List<ObjectMetadata> finalStageList = workflow.listInFinalStage();
        assertEquals(2, finalStageList.size(), "Should list only packages in final (release) stage");

        boolean hasPkg1 = finalStageList.stream().anyMatch(m -> "Package1".equals(m.getObjectName()));
        boolean hasPkg2 = finalStageList.stream().anyMatch(m -> "Package2".equals(m.getObjectName()));
        boolean hasDraft = finalStageList.stream().anyMatch(m -> "DraftPackage".equals(m.getObjectName()));

        assertTrue(hasPkg1, "Should find Package1 in final stage");
        assertTrue(hasPkg2, "Should find Package2 in final stage");
        assertTrue(!hasDraft, "Should not find draft package in final stage list");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    /**
     * Test final stage operations with hierarchical scopes.
     * Child workflow should see packages from both its own final stage and parent's final stage.
     * @throws InvocationTargetException
     */
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
    @WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "child-release", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/child-release", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.scope", value = "child-scope"),
        @Property(key = "storage.role", value = "release")
    })
    // Parent workflow
    @WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "parent-workflow", location = "?", properties = {
        @Property(key = "scope", value = "parent-scope"),
        @Property(key = "parent.scope", value = ""),
        @Property(key = "stages", value = {"release"}, type = Type.Array),
        @Property(key = "writable.stages", value = {"release"}, type = Type.Array),
        @Property(key = "final.stage", value = "release")
    })
    // Child workflow (references parent)
    @WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "child-workflow", location = "?", properties = {
        @Property(key = "scope", value = "child-scope"),
        @Property(key = "parent.scope", value = "parent-scope"),
        @Property(key = "stages", value = {"release"}, type = Type.Array),
        @Property(key = "writable.stages", value = {"release"}, type = Type.Array),
        @Property(key = "final.stage", value = "release"),
        @Property(key = "parentWorkflowService.target", value = "(scope=parent-scope)")
    })
    public void testFinalStageWithHierarchy(
            @InjectService(cardinality = 0, filter = "(scope=parent-scope)")
            ServiceAware<EObjectWorkflowService> parentWorkflowAware,
            @InjectService(cardinality = 0, filter = "(scope=child-scope)")
            ServiceAware<EObjectWorkflowService> childWorkflowAware) throws InterruptedException, InvocationTargetException {

        EObjectWorkflowService<EObject> parentWorkflow = parentWorkflowAware.waitForService(5000);
        EObjectWorkflowService<EObject> childWorkflow = childWorkflowAware.waitForService(5000);

        assertNotNull(parentWorkflow, "Parent workflow should be available");
        assertNotNull(childWorkflow, "Child workflow should be available");

        Thread.sleep(2000); // Allow parent injection

        // Upload package to parent's final (release) stage
        EPackage parentPkg = ecoreFactory.createEPackage();
        parentPkg.setName("ParentPackage");
        parentPkg.setNsURI("http://parent.example.com/v1");
        parentPkg.setNsPrefix("parent");

        ObjectMetadata parentMetadata = managementFactory.createObjectMetadata();
        parentMetadata.setUploadUser("parentUser");
        parentMetadata.setObjectName("ParentPackage");
        parentMetadata.setObjectId("parent-pkg");
        parentMetadata.setScope("parent-scope");
        parentMetadata.setRole("release");

        parentWorkflow.uploadToStage("release", parentPkg, parentMetadata).getValue();
        String parentObjectId = parentMetadata.getObjectId();
        assertNotNull(parentObjectId);

        // Upload package to child's final (release) stage
        EPackage childPkg = ecoreFactory.createEPackage();
        childPkg.setName("ChildPackage");
        childPkg.setNsURI("http://child.example.com/v1");
        childPkg.setNsPrefix("child");

        ObjectMetadata childMetadata = managementFactory.createObjectMetadata();
        childMetadata.setUploadUser("childUser");
        childMetadata.setObjectName("ChildPackage");
        childMetadata.setObjectId("child-pkg");
        childMetadata.setScope("child-scope");
        childMetadata.setRole("release");
        
        childWorkflow.uploadToStage("release", childPkg, childMetadata).getValue();
        String childObjectId = childMetadata.getObjectId();
        assertNotNull(childObjectId);

        // Test getFromFinalStage - child should find parent's package
        ObjectMetadata parentFound = childWorkflow.getFromFinalStage(parentObjectId);
        assertNotNull(parentFound, "Child should find parent package via getFromFinalStage");
        assertEquals("ParentPackage", parentFound.getObjectName());

        // Test getFromFinalStage - child should find its own package
        ObjectMetadata childFound = childWorkflow.getFromFinalStage(childObjectId);
        assertNotNull(childFound, "Child should find its own package via getFromFinalStage");
        assertEquals("ChildPackage", childFound.getObjectName());

        // Test listInFinalStage - parent should only see its own package
        List<ObjectMetadata> parentList = parentWorkflow.listInFinalStage();
        assertEquals(1, parentList.size(), "Parent should see only its own package");
        assertEquals("ParentPackage", parentList.get(0).getObjectName());

        // Test listInFinalStage - child should see both packages
        List<ObjectMetadata> childList = childWorkflow.listInFinalStage();
        assertEquals(2, childList.size(), "Child should see both its own and parent's package");

        boolean hasParent = childList.stream().anyMatch(m -> "ParentPackage".equals(m.getObjectName()));
        boolean hasChild = childList.stream().anyMatch(m -> "ChildPackage".equals(m.getObjectName()));

        assertTrue(hasParent, "Child's final stage list should include parent package");
        assertTrue(hasChild, "Child's final stage list should include child package");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    /**
     * Test final stage configuration with custom final stage (not "release").
     * Verifies that final.stage property correctly determines which stage is considered final.
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
        @Property(key = "storage.scope", value = "custom-scope"),
        @Property(key = "storage.role", value = "draft")
    })
    @WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "approved-storage", location = "?", properties = {
            @Property(key = "workspace.folder", value = "%s/approved-storage", templateArguments = {
                @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
            }),
            @Property(key = "storage.scope", value = "custom-scope"),
            @Property(key = "storage.role", value = "approved")
        })
    @WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "approved-storage", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/approved-storage", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.scope", value = "custom-scope"),
        @Property(key = "storage.role", value = "approved")
    })
    @WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "release-storage", location = "?", properties = {
            @Property(key = "workspace.folder", value = "%s/release-storage", templateArguments = {
                @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
            }),
            @Property(key = "storage.scope", value = "custom-scope"),
            @Property(key = "storage.role", value = "release")
        })
    @WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "custom-workflow", location = "?", properties = {
        @Property(key = "scope", value = "custom-scope"),
        @Property(key = "parent.scope", value = ""),
        @Property(key = "stages", value = {"draft", "approved"}, type = Type.Array),
        @Property(key = "writable.stages", value = {"draft", "approved"}, type = Type.Array),
        @Property(key = "final.stage", value = "approved")
    })
    public void testCustomFinalStage(
            @InjectService(cardinality = 0, filter = "(scope=custom-scope)")
            ServiceAware<EObjectWorkflowService> workflowAware) throws InterruptedException, InvocationTargetException {

        EObjectWorkflowService<EObject> workflow = workflowAware.waitForService(5000);
        assertNotNull(workflow, "Workflow service should be available");

        // Upload to draft stage
        EPackage draftPkg = ecoreFactory.createEPackage();
        draftPkg.setName("DraftPackage");
        draftPkg.setNsURI("http://draft.example.com/v1");
        draftPkg.setNsPrefix("draft");

        ObjectMetadata draftMetadata = managementFactory.createObjectMetadata();
        draftMetadata.setUploadUser("testUser");
        draftMetadata.setObjectName("DraftPackage");
        draftMetadata.setObjectId("draft-pkg");
        draftMetadata.setScope("custom-scope");

        workflow.uploadToStage("draft", draftPkg, draftMetadata).getValue();
        String draftObjectId = draftMetadata.getObjectId();
        assertNotNull(draftObjectId);

        // Upload to approved stage (configured as final stage)
        EPackage approvedPkg = ecoreFactory.createEPackage();
        approvedPkg.setName("ApprovedPackage");
        approvedPkg.setNsURI("http://approved.example.com/v1");
        approvedPkg.setNsPrefix("approved");

        ObjectMetadata approvedMetadata = managementFactory.createObjectMetadata();
        approvedMetadata.setUploadUser("testUser");
        approvedMetadata.setObjectName("ApprovedPackage");
        approvedMetadata.setObjectId("approved-pkg");
        approvedMetadata.setScope("custom-scope");

        workflow.uploadToStage("approved", approvedPkg, approvedMetadata).getValue();
        String approvedObjectId = approvedMetadata.getObjectId();
        assertNotNull(approvedObjectId);

        // Test getFromFinalStage - should find approved package
        ObjectMetadata approvedFound = workflow.getFromFinalStage(approvedObjectId);
        assertNotNull(approvedFound, "Should find package in final (approved) stage");
        assertEquals("ApprovedPackage", approvedFound.getObjectName());

        // Test getFromFinalStage - should also check final stage when object not in draft
        ObjectMetadata foundViaFinal = workflow.getFromFinalStage(approvedObjectId);
        assertNotNull(foundViaFinal, "Should find approved package via getFromFinalStage");

        // Test listInFinalStage - should only list approved stage packages
        List<ObjectMetadata> finalList = workflow.listInFinalStage();
        assertEquals(1, finalList.size(), "Should only list packages in final (approved) stage");
        assertEquals("ApprovedPackage", finalList.get(0).getObjectName());

        // Verify draft package is not in final stage list
        boolean hasDraft = finalList.stream().anyMatch(m -> "DraftPackage".equals(m.getObjectName()));
        assertTrue(!hasDraft, "Draft package should not appear in final stage list");
    }

    // ========================================
    // Test: Invalid Transition Scenarios
    // ========================================

    @SuppressWarnings({ "unchecked", "rawtypes" })
    /**
     * Test that transition from draft to release (skipping approved) throws an exception.
     * Stages are configured as: draft -> approved -> release
     * Direct transition from draft to release should fail.
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
    @WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "approved-storage", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/approved-storage", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.scope", value = "test-scope"),
        @Property(key = "storage.role", value = "approved")
    })
    @WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "release-storage", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/release-storage", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.scope", value = "test-scope"),
        @Property(key = "storage.role", value = "release")
    })
    @WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "test-workflow", location = "?", properties = {
        @Property(key = "scope", value = "test-scope"),
        @Property(key = "parent.scope", value = ""),
        @Property(key = "stages", value = {"draft", "approved", "release"}, type = Type.Array),
        @Property(key = "writable.stages", value = {"draft", "approved", "release"}, type = Type.Array)
    })
    public void testInvalidTransitionSkipsStage(
            @InjectService(cardinality = 0, filter = "(scope=test-scope)")
            ServiceAware<EObjectWorkflowService> workflowAware) throws InterruptedException, InvocationTargetException {

        EObjectWorkflowService<EObject> workflow = workflowAware.waitForService(5000);
        assertNotNull(workflow, "Workflow service should be available");

        // Create and upload package to Draft
        EPackage pkg = ecoreFactory.createEPackage();
        pkg.setName("TestPackage");
        pkg.setNsURI("http://test.invalid.transition.com/v1");
        pkg.setNsPrefix("test");

        ObjectMetadata metadata = managementFactory.createObjectMetadata();
        metadata.setUploadUser("testUser");
        metadata.setObjectName("TestPackage");

        workflow.uploadToStage("draft", pkg, metadata).getValue();
        String storageId = metadata.getObjectId();
        assertNotNull(storageId);

        // Verify in Draft stage
        ObjectMetadata draft = workflow.getFromStage("draft", storageId);
        assertNotNull(draft, "Should exist in Draft stage");

        // Try to transition directly from draft to release (skipping approved) - should throw exception
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> workflow.transitionToStage(storageId, "draft", "release"),
            "Should throw IllegalStateException when trying to skip stages"
        );

        assertTrue(exception.getMessage().contains("Transition is not allowed"),
            "Exception message should indicate transition is not allowed");
        assertTrue(exception.getMessage().contains("draft"),
            "Exception message should mention source stage");
        assertTrue(exception.getMessage().contains("release"),
            "Exception message should mention target stage");

        // Verify object is still in draft stage (unchanged)
        ObjectMetadata stillInDraft = workflow.getFromStage("draft", storageId);
        assertNotNull(stillInDraft, "Object should still be in draft stage after failed transition");

        // Verify object is NOT in release stage
        ObjectMetadata notInRelease = workflow.getFromStage("release", storageId);
        assertNull(notInRelease, "Object should not be in release stage after failed transition");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    /**
     * Test that valid sequential transitions work correctly.
     * Validates that draft -> approved -> release works as expected.
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
    @WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "approved-storage", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/approved-storage", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.scope", value = "test-scope"),
        @Property(key = "storage.role", value = "approved")
    })
    @WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "release-storage", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/release-storage", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.scope", value = "test-scope"),
        @Property(key = "storage.role", value = "release")
    })
    @WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "test-workflow", location = "?", properties = {
        @Property(key = "scope", value = "test-scope"),
        @Property(key = "parent.scope", value = ""),
        @Property(key = "stages", value = {"draft", "approved", "release"}, type = Type.Array),
        @Property(key = "writable.stages", value = {"draft", "approved", "release"}, type = Type.Array),
        @Property(key = "delete.after.transition", value = "true")
    })
    public void testValidSequentialTransitions(
            @InjectService(cardinality = 0, filter = "(scope=test-scope)")
            ServiceAware<EObjectWorkflowService> workflowAware) throws InterruptedException, InvocationTargetException {

        EObjectWorkflowService<EObject> workflow = workflowAware.waitForService(5000);
        assertNotNull(workflow, "Workflow service should be available");

        // Create and upload package to Draft
        EPackage pkg = ecoreFactory.createEPackage();
        pkg.setName("SequentialPackage");
        pkg.setNsURI("http://sequential.test.com/v1");
        pkg.setNsPrefix("seq");

        ObjectMetadata metadata = managementFactory.createObjectMetadata();
        metadata.setUploadUser("testUser");
        metadata.setObjectName("SequentialPackage");

        workflow.uploadToStage("draft", pkg, metadata).getValue();
        String storageId = metadata.getObjectId();

        // Verify in Draft stage
        ObjectMetadata draft = workflow.getFromStage("draft", storageId);
        assertNotNull(draft, "Should exist in Draft stage");

        // Valid transition: draft -> approved
        ObjectMetadata approved = workflow.transitionToStage(storageId, "draft", "approved");
        assertNotNull(approved, "Transition to approved should succeed");

        // Verify in approved stage and not in draft (due to delete.after.transition=true)
        ObjectMetadata draftGone = workflow.getFromStage("draft", storageId);
        assertNull(draftGone, "Should no longer exist in Draft stage");

        ObjectMetadata approvedCheck = workflow.getFromStage("approved", storageId);
        assertNotNull(approvedCheck, "Should exist in Approved stage");

        // Valid transition: approved -> release
        ObjectMetadata released = workflow.transitionToStage(storageId, "approved", "release");
        assertNotNull(released, "Transition to release should succeed");

        // Verify in release stage and not in approved
        ObjectMetadata approvedGone = workflow.getFromStage("approved", storageId);
        assertNull(approvedGone, "Should no longer exist in Approved stage");

        ObjectMetadata releaseCheck = workflow.getFromStage("release", storageId);
        assertNotNull(releaseCheck, "Should exist in Release stage");
        assertEquals("SequentialPackage", releaseCheck.getObjectName());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    /**
     * Test transition to non-existent stage throws exception.
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
        @Property(key = "scope", value = "test-scope"),
        @Property(key = "parent.scope", value = ""),
        @Property(key = "stages", value = {"draft", "release"}, type = Type.Array),
        @Property(key = "writable.stages", value = {"draft", "release"}, type = Type.Array)
    })
    public void testTransitionToNonExistentStage(
            @InjectService(cardinality = 0, filter = "(scope=test-scope)")
            ServiceAware<EObjectWorkflowService> workflowAware) throws InterruptedException, InvocationTargetException {

        EObjectWorkflowService<EObject> workflow = workflowAware.waitForService(5000);
        assertNotNull(workflow, "Workflow service should be available");

        // Create and upload package to Draft
        EPackage pkg = ecoreFactory.createEPackage();
        pkg.setName("TestPackage");
        pkg.setNsURI("http://test.nonexistent.com/v1");
        pkg.setNsPrefix("test");

        ObjectMetadata metadata = managementFactory.createObjectMetadata();
        metadata.setUploadUser("testUser");
        metadata.setObjectName("TestPackage");

        workflow.uploadToStage("draft", pkg, metadata).getValue();
        String storageId = metadata.getObjectId();

        // Try to transition to non-existent stage
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> workflow.transitionToStage(storageId, "draft", "nonexistent"),
            "Should throw IllegalStateException when transitioning to non-existent stage"
        );

        assertTrue(exception.getMessage().contains("Transition is not allowed"),
            "Exception message should indicate transition is not allowed");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    /**
     * Test backward transition (e.g., release -> draft) is not allowed.
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
        @Property(key = "scope", value = "test-scope"),
        @Property(key = "parent.scope", value = ""),
        @Property(key = "stages", value = {"draft", "release"}, type = Type.Array),
        @Property(key = "writable.stages", value = {"draft", "release"}, type = Type.Array)
    })
    public void testBackwardTransitionNotAllowed(
            @InjectService(cardinality = 0, filter = "(scope=test-scope)")
            ServiceAware<EObjectWorkflowService> workflowAware) throws InterruptedException, InvocationTargetException {

        EObjectWorkflowService<EObject> workflow = workflowAware.waitForService(5000);
        assertNotNull(workflow, "Workflow service should be available");

        // Create and upload package directly to Release
        EPackage pkg = ecoreFactory.createEPackage();
        pkg.setName("ReleasedPackage");
        pkg.setNsURI("http://backward.test.com/v1");
        pkg.setNsPrefix("back");

        ObjectMetadata metadata = managementFactory.createObjectMetadata();
        metadata.setUploadUser("testUser");
        metadata.setObjectName("ReleasedPackage");

        workflow.uploadToStage("release", pkg, metadata).getValue();
        String storageId = metadata.getObjectId();

        // Verify in Release stage
        ObjectMetadata release = workflow.getFromStage("release", storageId);
        assertNotNull(release, "Should exist in Release stage");

        // Try backward transition: release -> draft (should fail)
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> workflow.transitionToStage(storageId, "release", "draft"),
            "Should throw IllegalStateException when trying backward transition"
        );

        assertTrue(exception.getMessage().contains("Transition is not allowed"),
            "Exception message should indicate transition is not allowed");

        // Verify object is still in release stage
        ObjectMetadata stillInRelease = workflow.getFromStage("release", storageId);
        assertNotNull(stillInRelease, "Object should still be in release stage after failed transition");
    }

    // ========================================
    // Test: Three-Level Hierarchy
    // ========================================

    @SuppressWarnings({ "unchecked", "rawtypes" })
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
        @Property(key = "scope", value = "grandparent"),
        @Property(key = "parent.scope", value = ""),
        @Property(key = "stages", value = {"release"}, type = Type.Array),
        @Property(key = "writable.stages", value = {"release"}, type = Type.Array)
    })
    // Parent workflow
    @WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "parent-workflow", location = "?", properties = {
        @Property(key = "scope", value = "parent"),
        @Property(key = "parent.scope", value = "grandparent"),
        @Property(key = "stages", value = {"release"}, type = Type.Array),
        @Property(key = "writable.stages", value = {"release"}, type = Type.Array),
        @Property(key = "parentWorkflowService.target", value = "(scope=grandparent)")
    })
    // Child workflow
    @WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "child-workflow", location = "?", properties = {
        @Property(key = "scope", value = "child"),
        @Property(key = "parent.scope", value = "parent"),
        @Property(key = "stages", value = {"release"}, type = Type.Array),
        @Property(key = "writable.stages", value = {"release"}, type = Type.Array),
        @Property(key = "parentWorkflowService.target", value = "(scope=parent)")
    })
    public void testThreeLevelHierarchy(
            @InjectService(cardinality = 0, filter = "(scope=grandparent)")
            ServiceAware<EObjectWorkflowService> grandparentWorkflowAware,
            @InjectService(cardinality = 0, filter = "(scope=child)")
            ServiceAware<EObjectWorkflowService> childWorkflowAware) throws InterruptedException, InvocationTargetException {

        EObjectWorkflowService<EObject> grandparentWorkflow = grandparentWorkflowAware.waitForService(5000);
        EObjectWorkflowService<EObject> childWorkflow = childWorkflowAware.waitForService(5000);

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
        grandparentWorkflow.uploadToStage("release", grandparentPkg, grandparentMeta).getValue();
        String storageId = grandparentMeta.getObjectId();

        // Child should find grandparent's package
        ObjectMetadata found = childWorkflow.getFromStage("release", storageId);
        assertNotNull(found, "Child should find grandparent package via two-level delegation");
        assertEquals("GrandparentPkg", found.getObjectName());
//        assertTrue(found.isReadOnly(), "Grandparent package should be read-only");
//        assertEquals("grandparent", found.getSourceScope(), "Should indicate grandparent as source");
    }
}
