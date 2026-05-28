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
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.data.atlas.jpa.tests.helper.TestAnnotations.DataFolderWatcherConfig;
import org.eclipse.fennec.data.atlas.jpa.watcher.api.WatcherConstants;
import org.eclipse.fennec.persistence.eorm.EntityMappings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.tracker.ServiceTracker;

import jakarta.persistence.EntityManagerFactory;

/**
 * Integration tests for org.eclipse.fennec.data.atlas.jpa.watcher.DataFolderWatcher.
 *
 * <p>The watcher is configured against a folder containing {@code mapping/} and
 * {@code data/} subfolders. On activation it scans {@code <basePath>/mapping/}
 * for an {@code .eorm} file: if found, it bootstraps the full pipeline
 * (DataSource, EMFFileWatcher, EormFileWatcher, CSV importer and persistence
 * unit) under a shared {@code file.context.matcher} key. We verify pipeline
 * startup by waiting for the {@link EntityMappings} service that the inner
 * org.eclipse.fennec.data.atlas.jpa.watcher.EormFileWatcher registers.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class DataFolderWatcherTests {

    private static final String PROP_NAME = "eorm.name";
    private static final String PROP_MATCHER = WatcherConstants.KEY_FILE_CONTEXT_MATCHER;
    private static final String FILTER_WATCHER_OWNED = "(" + PROP_MATCHER + "=*)";
    private static final String DEMO_NS_URI = "http://example.org/jpa/demo/1.0";

    private static final String EORM_WATCHER_TEST = """
            <?xml version="1.0" encoding="UTF-8"?>
            <eorm:EntityMappings xmi:version="2.0"
                xmlns:xmi="http://www.omg.org/XMI"
                xmlns:eorm="https://eclipse.org/fennec/persistence/eorm/1.0.0"
                package="http://example.org/jpa/watcher/1.0"
                name="watcher-test"/>
            """;

    private static final String EORM_TEMPLATE = """
            <?xml version="1.0" encoding="UTF-8"?>
            <eorm:EntityMappings xmi:version="2.0"
                xmlns:xmi="http://www.omg.org/XMI"
                xmlns:eorm="https://eclipse.org/fennec/persistence/eorm/1.0.0"
                package="http://example.org/jpa/watcher/1.0"
                name="%s"/>
            """;

    private final List<Path> createdFiles = new ArrayList<>();
    private final List<Configuration> createdConfigs = new ArrayList<>();

    @AfterEach
    void cleanUp(@InjectService ConfigurationAdmin configAdmin) throws IOException, InterruptedException {
        for (Configuration c : createdConfigs) {
            try {
                c.delete();
            } catch (IOException e) {
                // already deleted in test body
            }
        }
        createdConfigs.clear();
        for (Path p : createdFiles) {
            Files.deleteIfExists(p);
        }
        createdFiles.clear();
        Thread.sleep(2000);
       
       
    }

    @Test
    @DataFolderWatcherConfig
    public void testInitialLoad_pipelineStarted(
            @InjectService(cardinality = 0) ServiceAware<EntityMappings> aware) throws InterruptedException {
        assertNotNull(aware.waitForService(25_000));
    }

    @Test
    @DataFolderWatcherConfig
    public void testInitialLoad_eormNameIsSet(
            @InjectService(cardinality = 0, filter = "(" + PROP_NAME + "=demo-mapping)") ServiceAware<EntityMappings> aware)
            throws InterruptedException {
        assertNotNull(aware.waitForService(15_000));
        ServiceReference<EntityMappings> ref = aware.getServiceReference();
        assertNotNull(ref);
        assertEquals("demo-mapping", ref.getProperty(PROP_NAME));
    }

    @Test
    @DataFolderWatcherConfig
    public void testNonEormFile_ignored(
            @InjectService(cardinality = 0) ServiceAware<EntityMappings> aware) throws Exception {
        assertNotNull(aware.waitForService(15_000));
        int countBefore = aware.getServices().size();
        assertEquals(1, countBefore);
        Path file = dataFolder().resolve("ignored.txt");
        Files.writeString(file, "not a mapping");
        createdFiles.add(file);
        Thread.sleep(2000);
        assertEquals(countBefore, aware.getServices().size());
    }

    @Test
    public void testNoEormInFolder_pipelineNotStarted(@TempDir Path tempDir,
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0) ServiceAware<EntityMappings> aware) throws Exception {
    	System.out.print(tempDir.toAbsolutePath().toString());
        Files.createDirectories(tempDir.resolve("mapping"));
        assertEquals(0, aware.getServices().size());

        Configuration config = configAdmin.getFactoryConfiguration(
                WatcherConstants.PID_DATA_FOLDER_WATCHER, "temp-no-mapping", "?");
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("io.fs.watcher.path", tempDir.toAbsolutePath() + "/");
        config.update(props);
        createdConfigs.add(config);

        Thread.sleep(3000);
        assertEquals(0, aware.getServices().size());
    }

    @Test
    public void testEormAdded_pipelineStarted(@TempDir Path tempDir,
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0, filter = "(" + PROP_NAME + "=watcher-test)") ServiceAware<EntityMappings> aware)
            throws Exception {
    	System.out.print(tempDir.toAbsolutePath().toString());
        Files.createDirectories(tempDir.resolve("mapping"));

        Configuration config = configAdmin.getFactoryConfiguration(
                WatcherConstants.PID_DATA_FOLDER_WATCHER, "temp-add-mapping", "?");
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("io.fs.watcher.path", tempDir.toAbsolutePath() + "/");
        config.update(props);
        createdConfigs.add(config);

        assertTrue(aware.isEmpty());

        Path file = tempDir.resolve("mapping").resolve("unit.eorm");
        Files.writeString(file, EORM_WATCHER_TEST);
        createdFiles.add(file);

        assertNotNull(aware.waitForService(15_000));
        
       
    }

    @Test
    public void testDeactivation_pipelineCleanedUp(
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0, filter = "(" + PROP_NAME + "=demo-mapping)") ServiceAware<EntityMappings> aware)
            throws Exception {
        Configuration config = configAdmin.getFactoryConfiguration(
                WatcherConstants.PID_DATA_FOLDER_WATCHER, "deactivate-test", "?");
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("io.fs.watcher.path", dataFolder().toAbsolutePath() + "/");
        config.update(props);
        createdConfigs.add(config);

        assertNotNull(aware.waitForService(15_000));

        createdConfigs.remove(config);
        config.delete();

        assertTrue(waitForNoService(aware, 15_000));
    }

    @Test
    @DataFolderWatcherConfig
    public void testFullPipeline_allServicesUp(
            @InjectBundleContext BundleContext ctx,
            @InjectService(cardinality = 0, filter = FILTER_WATCHER_OWNED) ServiceAware<EntityMappings> mappingsAware,
            @InjectService(cardinality = 0, filter = FILTER_WATCHER_OWNED) ServiceAware<DataSource> dsAware,
            @InjectService(cardinality = 0, filter = "(emf.nsURI=" + DEMO_NS_URI + ")") ServiceAware<EPackage> ePackageAware)
            throws Exception {
        assertNotNull(mappingsAware.waitForService(15_000), "EntityMappings should be registered");
        String matcherKey = (String) mappingsAware.getServiceReference().getProperty(PROP_MATCHER);
        assertNotNull(matcherKey, "EntityMappings should carry file.context.matcher");

        assertNotNull(dsAware.waitForService(15_000), "H2 DataSource should be registered");
        assertEquals(matcherKey, dsAware.getServiceReference().getProperty(PROP_MATCHER),
                "DataSource and EntityMappings should share the same matcherKey");

        assertNotNull(ePackageAware.waitForService(15_000), "EPackage from model.ecore should be registered");

        EntityManagerFactory emf = waitForServiceByFilter(ctx, EntityManagerFactory.class,
                "(osgi.unit.name=data)", 55_000);
        assertNotNull(emf, "EntityManagerFactory should be registered with osgi.unit.name=data");
    }

    @Test
    public void testFullPipeline_allServicesDown(
            @InjectBundleContext BundleContext ctx,
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0, filter = FILTER_WATCHER_OWNED) ServiceAware<EntityMappings> mappingsAware,
            @InjectService(cardinality = 0, filter = FILTER_WATCHER_OWNED) ServiceAware<DataSource> dsAware,
            @InjectService(cardinality = 0, filter = "(emf.nsURI=" + DEMO_NS_URI + ")") ServiceAware<EPackage> ePackageAware)
            throws Exception {
        Configuration config = configAdmin.getFactoryConfiguration(
                WatcherConstants.PID_DATA_FOLDER_WATCHER, "full-teardown", "?");
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("io.fs.watcher.path", dataFolder().toAbsolutePath() + "/");
        config.update(props);
        createdConfigs.add(config);

        assertNotNull(mappingsAware.waitForService(15_000));
        String matcherKey = (String) mappingsAware.getServiceReference().getProperty(PROP_MATCHER);
        assertNotNull(matcherKey);
        assertNotNull(dsAware.waitForService(15_000));
        assertNotNull(ePackageAware.waitForService(15_000));
        assertNotNull(waitForServiceByFilter(ctx, EntityManagerFactory.class,
                "(osgi.unit.name=data)", 15_000));

        createdConfigs.remove(config);
        config.delete();

        assertTrue(waitForNoService(mappingsAware, 15_000), "EntityMappings should be unregistered");
        assertTrue(waitForNoService(dsAware, 15_000), "DataSource should be unregistered");
        assertTrue(waitForNoService(ePackageAware, 15_000), "EPackage should be unregistered");
        assertTrue(waitForNoServiceByFilter(ctx, EntityManagerFactory.class,
                "(osgi.unit.name=data)", 15_000),
                "EntityManagerFactory should be unregistered");
    }

    @Test
    public void testTwoFolders_isolatedPipelines(
            @TempDir Path dir1, @TempDir Path dir2,
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0, filter = FILTER_WATCHER_OWNED) ServiceAware<EntityMappings> mappingsAware,
            @InjectService(cardinality = 0, filter = FILTER_WATCHER_OWNED) ServiceAware<DataSource> dsAware)
            throws Exception {
        Files.createDirectories(dir1.resolve("mapping"));
        Files.createDirectories(dir2.resolve("mapping"));
        Path eorm1 = dir1.resolve("mapping").resolve("unit.eorm");
        Path eorm2 = dir2.resolve("mapping").resolve("unit.eorm");
        Files.writeString(eorm1, EORM_TEMPLATE.formatted("pipeline-one"));
        Files.writeString(eorm2, EORM_TEMPLATE.formatted("pipeline-two"));
        createdFiles.add(eorm1);
        createdFiles.add(eorm2);

        Configuration cfg1 = configAdmin.getFactoryConfiguration(
                WatcherConstants.PID_DATA_FOLDER_WATCHER, "folder-one", "?");
        Dictionary<String, Object> p1 = new Hashtable<>();
        p1.put("io.fs.watcher.path", dir1.toAbsolutePath() + "/");
        cfg1.update(p1);
        createdConfigs.add(cfg1);

        Configuration cfg2 = configAdmin.getFactoryConfiguration(
                WatcherConstants.PID_DATA_FOLDER_WATCHER, "folder-two", "?");
        Dictionary<String, Object> p2 = new Hashtable<>();
        p2.put("io.fs.watcher.path", dir2.toAbsolutePath() + "/");
        cfg2.update(p2);
        createdConfigs.add(cfg2);

        assertTrue(waitForServiceCount(mappingsAware, 2, 15_000),
                "Expected 2 EntityMappings services, got " + mappingsAware.getServices().size());
        assertTrue(waitForServiceCount(dsAware, 2, 15_000),
                "Expected 2 DataSource services, got " + dsAware.getServices().size());

        Set<String> mappingKeys = new HashSet<>();
        for (ServiceReference<EntityMappings> ref : mappingsAware.getServiceReferences()) {
            mappingKeys.add((String) ref.getProperty(PROP_MATCHER));
        }
        assertEquals(2, mappingKeys.size(), "Each pipeline should have a unique matcherKey");

        Set<String> dsKeys = new HashSet<>();
        for (ServiceReference<DataSource> ref : dsAware.getServiceReferences()) {
            dsKeys.add((String) ref.getProperty(PROP_MATCHER));
        }
        assertEquals(mappingKeys, dsKeys,
                "DataSource matcherKeys should align with EntityMappings matcherKeys");
    }

    private Path dataFolder() {
        return Path.of(System.getProperty("data-folder"));
    }

    private boolean waitForNoService(ServiceAware<?> aware, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (aware.isEmpty()) {
                return true;
            }
            Thread.sleep(500);
        }
        return aware.isEmpty();
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

    private <T> T waitForServiceByFilter(BundleContext ctx, Class<T> type, String filter, long timeoutMs)
            throws Exception {
        ServiceTracker<T, T> tracker = new ServiceTracker<>(ctx,
                ctx.createFilter("(&(objectClass=" + type.getName() + ")" + filter + ")"), null);
        tracker.open(true);
        try {
            return tracker.waitForService(timeoutMs);
        } finally {
            tracker.close();
        }
    }

    private <T> boolean waitForNoServiceByFilter(BundleContext ctx, Class<T> type, String filter, long timeoutMs)
            throws Exception {
        ServiceTracker<T, T> tracker = new ServiceTracker<>(ctx,
                ctx.createFilter("(&(objectClass=" + type.getName() + ")" + filter + ")"), null);
        tracker.open();
        try {
            long deadline = System.currentTimeMillis() + timeoutMs;
            while (System.currentTimeMillis() < deadline) {
                if (tracker.size() == 0) {
                    return true;
                }
                Thread.sleep(500);
            }
            return tracker.size() == 0;
        } finally {
            tracker.close();
        }
    }
}
