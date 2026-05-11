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
import java.util.Hashtable;
import java.util.List;

import org.eclipse.fennec.data.atlas.jpa.tests.helper.TestAnnotations;
import org.eclipse.fennec.data.atlas.jpa.tests.helper.TestAnnotations.DataFolderWatcherConfig;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig;
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

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class DataFolderWatcherTests {

    private static final String PROP_NAME = "jpamapping.name";

    private static final String JPAMAPPING_WATCHER_TEST = """
            <?xml version="1.0" encoding="UTF-8"?>
            <jpamapping:JpaMappingConfig xmi:version="2.0"
                xmlns:xmi="http://www.omg.org/XMI"
                xmlns:jpamapping="http://eclipse.org/fennec/data/atlas/jpamapping/1.0.0"
                name="watcher-test"
                targetModelNsUri="http://example.org/jpa/watcher/1.0">
              <dataSource driverClass="org.h2.Driver" jdbcUrl="jdbc:h2:mem:watchertest"
                  username="sa" passwordRef="DB_PASSWORD" poolSize="5" dialect="H2"/>
            </jpamapping:JpaMappingConfig>
            """;

    private final List<Path> createdFiles = new ArrayList<>();
    private final List<Configuration> createdConfigs = new ArrayList<>();

    @AfterEach
    void cleanUp() throws IOException, InterruptedException {
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
            @InjectService(cardinality = 0) ServiceAware<JpaMappingConfig> aware) throws InterruptedException {
        assertNotNull(aware.waitForService(15_000));
    }

    @Test
    @DataFolderWatcherConfig
    public void testInitialLoad_unitNameIsSet(
            @InjectService(cardinality = 0, filter = "(" + PROP_NAME + "=demo-mapping)") ServiceAware<JpaMappingConfig> aware)
            throws InterruptedException {
        assertNotNull(aware.waitForService(15_000));
        ServiceReference<JpaMappingConfig> ref = aware.getServiceReference();
        assertNotNull(ref);
        assertEquals("demo-mapping", ref.getProperty(PROP_NAME));
    }

    @Test
    @DataFolderWatcherConfig
    public void testNonJpamappingFile_ignored(
            @InjectService(cardinality = 0) ServiceAware<JpaMappingConfig> aware) throws Exception {
        assertNotNull(aware.waitForService(15_000));
        int countBefore = aware.getServices().size();
        Path file = dataFolder().resolve("ignored.txt");
        Files.writeString(file, "not a mapping");
        createdFiles.add(file);
        Thread.sleep(2000);
        assertEquals(countBefore, aware.getServices().size());
    }
   

    @Test
    public void testNoJpamappingInFolder_pipelineNotStarted(@TempDir Path tempDir,
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0) ServiceAware<JpaMappingConfig> aware) throws Exception {
        assertTrue(aware.isEmpty());

        Configuration config = configAdmin.getFactoryConfiguration(
                TestAnnotations.DATA_FOLDER_WATCHER_PID, "temp-no-mapping", "?");
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("io.fs.watcher.path", tempDir.toAbsolutePath() + "/");
        config.update(props);
        createdConfigs.add(config);

        Thread.sleep(3000);
        assertTrue(aware.isEmpty());
    }

    @Test
    public void testJpamappingAdded_pipelineStarted(@TempDir Path tempDir,
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0, filter = "(" + PROP_NAME + "=watcher-test)") ServiceAware<JpaMappingConfig> aware)
            throws Exception {
        Configuration config = configAdmin.getFactoryConfiguration(
                TestAnnotations.DATA_FOLDER_WATCHER_PID, "temp-add-mapping", "?");
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("io.fs.watcher.path", tempDir.toAbsolutePath() + "/");
        config.update(props);
        createdConfigs.add(config);

        assertTrue(aware.isEmpty());

        Files.createDirectories(tempDir.resolve("mapping"));
        Path file = tempDir.resolve("mapping").resolve("unit.jpamapping");
        Files.writeString(file, JPAMAPPING_WATCHER_TEST);
        createdFiles.add(file);

        assertNotNull(aware.waitForService(15_000));
    }

    @Test
    public void testDeactivation_pipelineCleanedUp(
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0, filter = "(" + PROP_NAME + "=demo-mapping)") ServiceAware<JpaMappingConfig> aware)
            throws Exception {
        Configuration config = configAdmin.getFactoryConfiguration(
                TestAnnotations.DATA_FOLDER_WATCHER_PID, "deactivate-test", "?");
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("io.fs.watcher.path", dataFolder().toAbsolutePath() + "/");
        config.update(props);
        createdConfigs.add(config);

        assertNotNull(aware.waitForService(15_000));

        createdConfigs.remove(config);
        config.delete();
        
        assertTrue(waitForNoService(aware, 15_000));
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
}
