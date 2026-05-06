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
import java.util.List;

import org.eclipse.fennec.data.atlas.jpa.tests.helper.TestAnnotations.JpaMappingWatcherConfig;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class JpaMappingWatcherTests {

    private static final String PROP_NAME       = "jpamapping.name";
    private static final String PROP_NS_URI     = "jpamapping.targetNsUri";
    private static final String PROP_FOLDER     = "jpamapping.folder";

    private static final String DYNAMIC_XMI = """
            <?xml version="1.0" encoding="UTF-8"?>
            <jpamapping:JpaMappingConfig xmi:version="2.0"
                xmlns:xmi="http://www.omg.org/XMI"
                xmlns:jpamapping="http://eclipse.org/fennec/data/atlas/jpamapping/1.0.0"
                name="dynamic-mapping"
                targetModelNsUri="https://dg.de/1.0">
              <dataSource driverClass="org.h2.Driver" jdbcUrl="jdbc:h2:mem:testdb" username="sa" passwordRef="DB_PASSWORD" poolSize="5" dialect="H2"/>
              <tableMappings className="https://dg.de/1.0#//Address" tableName="address" schema="public">
                <columnMappings featureName="city" columnName="city" columnType="VARCHAR(255)" nullable="true"/>
              </tableMappings>
            </jpamapping:JpaMappingConfig>
            """;

    private static final String DYNAMIC_XMI_UPDATED = """
            <?xml version="1.0" encoding="UTF-8"?>
            <jpamapping:JpaMappingConfig xmi:version="2.0"
                xmlns:xmi="http://www.omg.org/XMI"
                xmlns:jpamapping="http://eclipse.org/fennec/data/atlas/jpamapping/1.0.0"
                name="dynamic-mapping-updated"
                targetModelNsUri="https://dg.de/1.0">
              <dataSource driverClass="org.h2.Driver" jdbcUrl="jdbc:h2:mem:testdb2" username="sa" passwordRef="DB_PASSWORD" poolSize="5" dialect="H2"/>
              <tableMappings className="https://dg.de/1.0#//Person" tableName="person" schema="public">
                <columnMappings featureName="email" columnName="email" columnType="VARCHAR(255)" nullable="false" primaryKey="true"/>
              </tableMappings>
            </jpamapping:JpaMappingConfig>
            """;

    private final List<Path> createdFiles = new ArrayList<>();

    @AfterEach
    void cleanUp() throws IOException, InterruptedException {
        for (Path file : createdFiles) {
            Files.deleteIfExists(file);
        }
        createdFiles.clear();
        Thread.sleep(2000); //give time to cleanup services
    }

    @Test
    @JpaMappingWatcherConfig
    public void testInitialLoad_serviceRegistered(
            @InjectService(cardinality = 0) ServiceAware<JpaMappingConfig> aware) throws InterruptedException {
        assertNotNull(aware.waitForService(5000));
    }

    @Test
    @JpaMappingWatcherConfig
    public void testInitialLoad_serviceProperties(
            @InjectService(cardinality = 0) ServiceAware<JpaMappingConfig> aware) throws InterruptedException {
        assertNotNull(aware.waitForService(15_000));
        ServiceReference<JpaMappingConfig> ref = aware.getServiceReference();
        assertNotNull(ref);
        assertEquals("demo-mapping", ref.getProperty(PROP_NAME));
        assertEquals("http://example.org/jpa/demo/1.0", ref.getProperty(PROP_NS_URI));
        assertNotNull(ref.getProperty(PROP_FOLDER));
    }

    @Test
    @JpaMappingWatcherConfig
    public void testFileCreated_serviceRegistered(
            @InjectService(cardinality = 0, filter = "(" + PROP_NAME + "=dynamic-mapping)") ServiceAware<JpaMappingConfig> aware) throws Exception {
        assertTrue(aware.isEmpty());
        Path file = writeFile("dynamic.jpamapping", DYNAMIC_XMI);
        assertNotNull(aware.waitForService(5000));
        Files.delete(file);
        createdFiles.remove(file);
    }

    @Test
    @JpaMappingWatcherConfig
    public void testFileDeleted_serviceUnregistered(
            @InjectService(cardinality = 0, filter = "(" + PROP_NAME + "=dynamic-mapping)") ServiceAware<JpaMappingConfig> aware) throws Exception {
        Path file = writeFile("dynamic.jpamapping", DYNAMIC_XMI);
        assertNotNull(aware.waitForService(5000));
        Files.delete(file);
        createdFiles.remove(file);
        assertTrue(waitForNoService(aware, 15_000));
    }

    @Test
    @JpaMappingWatcherConfig
    public void testFileModified_servicePropertiesUpdated(
            @InjectService(cardinality = 0, filter = "(" + PROP_NAME + "=dynamic-mapping)") ServiceAware<JpaMappingConfig> originalAware,
            @InjectService(cardinality = 0, filter = "(" + PROP_NAME + "=dynamic-mapping-updated)") ServiceAware<JpaMappingConfig> updatedAware) throws Exception {
        Path file = writeFile("dynamic.jpamapping", DYNAMIC_XMI);
        assertNotNull(originalAware.waitForService(15_000));
        Files.writeString(file, DYNAMIC_XMI_UPDATED);
        assertTrue(waitForNoService(originalAware, 15_000));
        assertNotNull(updatedAware.waitForService(15_000));
        Files.delete(file);
        createdFiles.remove(file);
    }

    @Test
    @JpaMappingWatcherConfig
    public void testNonJpaMappingFileIgnored(
            @InjectService(cardinality = 0) ServiceAware<JpaMappingConfig> aware) throws Exception {
        assertNotNull(aware.waitForService(5000));
        int countBefore = aware.getServices().size();
        Path file = writeFile("ignored.xml", "<test/>");
        Thread.sleep(2000);
        assertEquals(countBefore, aware.getServices().size());
        Files.delete(file);
        createdFiles.remove(file);
    }

    private Path writeFile(String name, String content) throws IOException {
        Path file = dataFolder().resolve(name);
        Files.writeString(file, content);
        createdFiles.add(file);
        return file;
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
