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
package org.eclipse.fennec.data.atlas.jpa.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.fennec.data.atlas.jpa.watcher.api.WatcherConstants;
import org.eclipse.fennec.persistence.eorm.EntityMappings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Integration tests for WorkspaceFolderWatcher.
 *
 * <p>The watcher is configured against a workspace root and, for every direct
 * sub-directory it finds (initial scan or {@code ENTRY_CREATE}), it creates a
 * DataFolderWatcher factory configuration. We verify pipeline activation by waiting for the
 * {@link EntityMappings} services that the inner {@code EormFileWatcher}
 * registers, since their count maps one-to-one onto the active DataFolderWatchers.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class WorkspaceFolderWatcherTests {

    private static final String PROP_NAME = "eorm.name";
    private static final String PROP_MATCHER = WatcherConstants.KEY_FILE_CONTEXT_MATCHER;
    private static final String FILTER_WATCHER_OWNED = "(" + PROP_MATCHER + "=*)";

    private static final String EORM_TEMPLATE = """
            <?xml version="1.0" encoding="UTF-8"?>
            <eorm:EntityMappings xmi:version="2.0"
                xmlns:xmi="http://www.omg.org/XMI"
                xmlns:eorm="https://eclipse.org/fennec/persistence/eorm/1.0.0"
                package="http://example.org/jpa/workspace/1.0"
                name="%s"/>
            """;

    private final List<Configuration> createdConfigs = new ArrayList<>();

    @AfterEach
    void cleanUp() throws IOException, InterruptedException {
        for (Configuration c : createdConfigs) {
            try {
                c.delete();
            } catch (IOException ignored) {
                // already deleted in test body
            }
        }
        createdConfigs.clear();
        Thread.sleep(2000);
    }

    @Test
    public void testEmptyWorkspace_noPipelines(@TempDir Path workspace,
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0, filter = FILTER_WATCHER_OWNED) ServiceAware<EntityMappings> mappingsAware)
            throws Exception {
        registerWorkspaceWatcher(configAdmin, workspace, "empty-workspace");

        Thread.sleep(3000);
        assertEquals(0, mappingsAware.getServices().size(),
                "No EntityMappings should be registered for an empty workspace");
    }

    @Test
    public void testInitialScan_subfoldersBecomePipelines(@TempDir Path workspace,
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0, filter = FILTER_WATCHER_OWNED) ServiceAware<EntityMappings> mappingsAware)
            throws Exception {
        createDataFolder(workspace, "alpha", "unit-alpha");
        createDataFolder(workspace, "beta", "unit-beta");

        registerWorkspaceWatcher(configAdmin, workspace, "initial-scan");

        assertTrue(waitForServiceCount(mappingsAware, 2, 30_000),
                "Expected 2 EntityMappings (one per subfolder), got " + mappingsAware.getServices().size());

        Set<String> matcherKeys = new HashSet<>();
        for (ServiceReference<EntityMappings> ref : mappingsAware.getServiceReferences()) {
            matcherKeys.add((String) ref.getProperty(PROP_MATCHER));
        }
        assertEquals(2, matcherKeys.size(), "Each pipeline should have a unique matcherKey");
    }

    @Test
    public void testFolderCreated_pipelineStarts(@TempDir Path workspace,
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0, filter = FILTER_WATCHER_OWNED) ServiceAware<EntityMappings> mappingsAware)
            throws Exception {
        registerWorkspaceWatcher(configAdmin, workspace, "folder-created");

        Thread.sleep(2000);
        assertEquals(0, mappingsAware.getServices().size());

        createDataFolder(workspace, "late-comer", "unit-late");

        assertTrue(waitForServiceCount(mappingsAware, 1, 30_000),
                "Pipeline should be started after subfolder is created");
    }

    @Test
    public void testFolderDeleted_pipelineStops(@TempDir Path workspace,
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0, filter = FILTER_WATCHER_OWNED) ServiceAware<EntityMappings> mappingsAware)
            throws Exception {
        Path subfolder = createDataFolder(workspace, "transient", "unit-transient");

        registerWorkspaceWatcher(configAdmin, workspace, "folder-deleted");

        assertTrue(waitForServiceCount(mappingsAware, 1, 30_000),
                "Initial pipeline should be running");

        deleteRecursively(subfolder);

        assertTrue(waitForServiceCount(mappingsAware, 0, 30_000),
                "Pipeline should be torn down when its subfolder is deleted");
    }

    @Test
    public void testFileInWorkspace_ignored(@TempDir Path workspace,
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0, filter = FILTER_WATCHER_OWNED) ServiceAware<EntityMappings> mappingsAware)
            throws Exception {
        createDataFolder(workspace, "real", "unit-real");
        Files.writeString(workspace.resolve("stray.txt"), "not a data folder");

        registerWorkspaceWatcher(configAdmin, workspace, "file-ignored");

        assertTrue(waitForServiceCount(mappingsAware, 1, 30_000));
        Thread.sleep(2000);
        assertEquals(1, mappingsAware.getServices().size(),
                "Only the directory entry should produce a pipeline; the file must be ignored");

        Files.writeString(workspace.resolve("stray2.txt"), "still not a data folder");
        Thread.sleep(3000);
        assertEquals(1, mappingsAware.getServices().size(),
                "Files added at runtime must not start pipelines");
    }

    @Test
    public void testNestedFolders_onlyDirectChildrenRegistered(@TempDir Path workspace,
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0, filter = FILTER_WATCHER_OWNED) ServiceAware<EntityMappings> mappingsAware)
            throws Exception {
        // Direct child – should become a pipeline.
        createDataFolder(workspace, "direct", "unit-direct");
        // Nested child two levels deep – should be ignored by the non-recursive watcher.
        Path nested = workspace.resolve("outer").resolve("nested");
        Files.createDirectories(nested.resolve("mapping"));
        Files.writeString(nested.resolve("mapping").resolve("unit.eorm"),
                EORM_TEMPLATE.formatted("unit-nested"));

        registerWorkspaceWatcher(configAdmin, workspace, "non-recursive");

        // We expect exactly 2 pipelines: "direct" and "outer" (since "outer" itself
        // is a direct child – but it has no mapping/ folder, so DataFolderWatcher
        // won't start a pipeline for it). So only "direct" should produce a service.
        assertTrue(waitForServiceCount(mappingsAware, 1, 30_000),
                "Only direct-child subfolders with mappings should produce pipelines");
        Thread.sleep(2000);
        assertEquals(1, mappingsAware.getServices().size(),
                "Nested grand-child folder must not be registered");
    }

    @Test
    public void testDeactivation_allPipelinesCleanedUp(@TempDir Path workspace,
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0, filter = FILTER_WATCHER_OWNED) ServiceAware<EntityMappings> mappingsAware)
            throws Exception {
        createDataFolder(workspace, "one", "unit-one");
        createDataFolder(workspace, "two", "unit-two");

        Configuration workspaceCfg = registerWorkspaceWatcher(configAdmin, workspace, "deactivate-all");

        assertTrue(waitForServiceCount(mappingsAware, 2, 30_000));

        createdConfigs.remove(workspaceCfg);
        workspaceCfg.delete();

        assertTrue(waitForServiceCount(mappingsAware, 0, 30_000),
                "All child pipelines should be torn down on deactivation");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Configuration registerWorkspaceWatcher(ConfigurationAdmin ca, Path workspace, String name)
            throws IOException {
        Configuration cfg = ca.getFactoryConfiguration(
                WatcherConstants.PID_WORKSPACE_FOLDER_WATCHER, name, "?");
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("io.fs.watcher.path", workspace.toAbsolutePath() + "/");
        cfg.update(props);
        createdConfigs.add(cfg);
        return cfg;
    }

    private Path createDataFolder(Path workspace, String subName, String eormName) throws IOException {
        Path sub = workspace.resolve(subName);
        Files.createDirectories(sub.resolve("mapping"));
        Files.writeString(sub.resolve("mapping").resolve("unit.eorm"),
                EORM_TEMPLATE.formatted(eormName));
        return sub;
    }

    private void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(root)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException ignored) {
                }
            });
        }
    }

    private boolean waitForServiceCount(ServiceAware<?> aware, int expected, long timeoutMs)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (aware.getServices().size() == expected) {
                return true;
            }
            Thread.sleep(500);
        }
        return aware.getServices().size() == expected;
    }

    @Test
    public void testEormNamePropagated(@TempDir Path workspace,
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0, filter = "(" + PROP_NAME + "=unit-named)") ServiceAware<EntityMappings> named)
            throws Exception {
        createDataFolder(workspace, "named-folder", "unit-named");
        registerWorkspaceWatcher(configAdmin, workspace, "name-propagation");

        assertNotNull(named.waitForService(30_000),
                "EntityMappings with the eorm name from the subfolder should appear");
    }
}
