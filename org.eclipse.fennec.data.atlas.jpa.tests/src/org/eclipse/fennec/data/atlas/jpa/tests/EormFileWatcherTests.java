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

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.data.atlas.jpa.watcher.api.WatcherConstants;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.persistence.eorm.EntityMappings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Unit tests for EormFileWatcher
 *
 * <p>Each test sets up its own folder via {@link TempDir}, configures an
 * {@code EormFileWatcher} pointed at that folder, and verifies the
 * {@link EntityMappings} OSGi-service lifecycle in response to file events.
 *
 * <p>For tests that don't exercise the EPackage-gating path we use a minimal
 * {@code .eorm} with no {@code <entity>} elements — the watcher's load() takes
 * the early-return branch and registers immediately. {@link #testEormWithEntities_waitsForEPackage()}
 * exercises the await-the-EPackage path explicitly by including an entity reference
 * and registering a stub EPackage at the right moment.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class EormFileWatcherTests {

    private static final String PROP_NAME            = "eorm.name";
    private static final String PROP_TARGET_NS_URI   = "eorm.targetNsUri";
    private static final String PROP_FOLDER          = "eorm.folder";
    private static final String PROP_MATCHER         = WatcherConstants.KEY_FILE_CONTEXT_MATCHER;
    private static final String PROP_ORM_MAPPING_NAME = "fennec.jpa.orm.mapping.name";
    private static final String DEMO_NS_URI          = "http://example.org/eorm/test/1.0";

    private static final String EORM_NO_ENTITIES = """
            <?xml version="1.0" encoding="UTF-8"?>
            <eorm:EntityMappings xmi:version="2.0"
                xmlns:xmi="http://www.omg.org/XMI"
                xmlns:eorm="https://eclipse.org/fennec/persistence/eorm/1.0.0"
                package="%s"
                name="%s"/>
            """;

    private static final String EORM_WITH_ENTITY = """
            <?xml version="1.0" encoding="UTF-8"?>
            <eorm:EntityMappings xmi:version="2.0"
                xmlns:xmi="http://www.omg.org/XMI"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
                xmlns:eorm="https://eclipse.org/fennec/persistence/eorm/1.0.0"
                package="%s"
                name="%s">
              <entity access="FIELD" name="Foo">
                <accessibleObject xsi:type="eorm:EClassObject" name="demo.Foo">
                  <eclass href="%s#//Foo"/>
                </accessibleObject>
                <table name="foo"/>
              </entity>
            </eorm:EntityMappings>
            """;

    private final List<Path> createdFiles = new ArrayList<>();
    private final List<Configuration> createdConfigs = new ArrayList<>();
    private final List<ServiceRegistration<?>> createdRegistrations = new ArrayList<>();

    @AfterEach
    void cleanUp() throws IOException, InterruptedException {
        for (ServiceRegistration<?> reg : createdRegistrations) {
            try {
                reg.unregister();
            } catch (IllegalStateException ignored) {
                // already unregistered in the test body
            }
        }
        createdRegistrations.clear();
        for (Configuration c : createdConfigs) {
            try {
                c.delete();
            } catch (IOException ignored) {
                // already deleted in the test body
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
    public void testInitialScan_serviceRegistered(@TempDir Path tempDir,
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0, filter = "(" + PROP_NAME + "=initial)") ServiceAware<EntityMappings> aware)
            throws Exception {
        writeEorm(tempDir, "initial.eorm", EORM_NO_ENTITIES.formatted(DEMO_NS_URI, "initial"));
        startWatcher(configAdmin, tempDir, "initial-matcher");

        assertNotNull(aware.waitForService(15_000), "EntityMappings should be registered on initial scan");
    }

    @Test
    public void testInitialScan_serviceProperties(@TempDir Path tempDir,
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0, filter = "(" + PROP_NAME + "=props-test)") ServiceAware<EntityMappings> aware)
            throws Exception {
        Path eorm = writeEorm(tempDir, "props.eorm", EORM_NO_ENTITIES.formatted(DEMO_NS_URI, "props-test"));
        startWatcher(configAdmin, tempDir, "props-matcher");

        assertNotNull(aware.waitForService(15_000));
        ServiceReference<EntityMappings> ref = aware.getServiceReference();
        assertEquals("props-test", ref.getProperty(PROP_NAME));
        assertEquals(DEMO_NS_URI, ref.getProperty(PROP_TARGET_NS_URI));
        assertEquals("props-matcher", ref.getProperty(PROP_MATCHER));
        assertEquals("data", ref.getProperty(PROP_ORM_MAPPING_NAME));
        assertNotNull(ref.getProperty(PROP_FOLDER));
        assertTrue(ref.getProperty(PROP_FOLDER).toString().endsWith(tempDir.toAbsolutePath().toString()),
                "eorm.folder should point to the watched directory; got "
                        + ref.getProperty(PROP_FOLDER) + " for file " + eorm);
    }

    @Test
    public void testFileCreated_serviceRegistered(@TempDir Path tempDir,
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0, filter = "(" + PROP_NAME + "=created)") ServiceAware<EntityMappings> aware)
            throws Exception {
        startWatcher(configAdmin, tempDir, "created-matcher");
        assertTrue(aware.isEmpty(), "no EntityMappings before .eorm is written");

        writeEorm(tempDir, "created.eorm", EORM_NO_ENTITIES.formatted(DEMO_NS_URI, "created"));

        assertNotNull(aware.waitForService(15_000), "EntityMappings should appear after .eorm is created");
    }

    @Test
    public void testFileDeleted_serviceUnregistered(@TempDir Path tempDir,
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0, filter = "(" + PROP_NAME + "=deleted)") ServiceAware<EntityMappings> aware)
            throws Exception {
        Path eorm = writeEorm(tempDir, "deleted.eorm", EORM_NO_ENTITIES.formatted(DEMO_NS_URI, "deleted"));
        startWatcher(configAdmin, tempDir, "deleted-matcher");
        assertNotNull(aware.waitForService(15_000));
        
        Thread.sleep(2000);

        createdFiles.remove(eorm);
        Files.deleteIfExists(eorm);
       
        Thread.sleep(2000);
        
        assertTrue(waitForNoService(aware, 25_000), "EntityMappings should be unregistered after .eorm delete");
    }

    @Test
    public void testFileModified_servicePropertiesUpdated(@TempDir Path tempDir,
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0, filter = "(" + PROP_NAME + "=before-modify)") ServiceAware<EntityMappings> awareBefore,
            @InjectService(cardinality = 0, filter = "(" + PROP_NAME + "=after-modify)") ServiceAware<EntityMappings> awareAfter)
            throws Exception {
        Path eorm = writeEorm(tempDir, "modified.eorm", EORM_NO_ENTITIES.formatted(DEMO_NS_URI, "before-modify"));
        startWatcher(configAdmin, tempDir, "modify-matcher");
        assertNotNull(awareBefore.waitForService(15_000), "original EntityMappings should be registered");

        Thread.sleep(2000);
        
        Files.writeString(eorm, EORM_NO_ENTITIES.formatted(DEMO_NS_URI, "after-modify"));
        
        Thread.sleep(2000);

        assertTrue(waitForNoService(awareBefore, 15_000), "old EntityMappings should be unregistered");
        assertNotNull(awareAfter.waitForService(15_000), "updated EntityMappings should be registered");
    }

    @Test
    public void testNonEormFile_ignored(@TempDir Path tempDir,
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0, filter = "(" + PROP_NAME + "=baseline)") ServiceAware<EntityMappings> aware)
            throws Exception {
        writeEorm(tempDir, "baseline.eorm", EORM_NO_ENTITIES.formatted(DEMO_NS_URI, "baseline"));
        startWatcher(configAdmin, tempDir, "ignore-matcher");
        assertNotNull(aware.waitForService(15_000));
        int countBefore = aware.getServices().size();

        Path stray = tempDir.resolve("stray.txt");
        Files.writeString(stray, "not a mapping");
        createdFiles.add(stray);
        Thread.sleep(2000);

        assertEquals(countBefore, aware.getServices().size(),
                ".txt file should not produce a new EntityMappings service");
    }

    @Test
    public void testEormWithEntities_waitsForEPackage(@TempDir Path tempDir,
            @InjectBundleContext BundleContext ctx,
            @InjectService ConfigurationAdmin configAdmin,
            @InjectService(cardinality = 0, filter = "(" + PROP_NAME + "=gated)") ServiceAware<EntityMappings> aware)
            throws Exception {
        writeEorm(tempDir, "gated.eorm", EORM_WITH_ENTITY.formatted(DEMO_NS_URI, "gated", DEMO_NS_URI));
        startWatcher(configAdmin, tempDir, "gated-matcher");

        Thread.sleep(2000);
        assertTrue(aware.isEmpty(), "EntityMappings must not register until the EPackage is available");

        ServiceRegistration<EPackage> reg = registerStubEPackage(ctx, DEMO_NS_URI);

        assertNotNull(aware.waitForService(15_000),
                "EntityMappings should register once the matching EPackage appears");

        reg.unregister();
        createdRegistrations.remove(reg);

        assertTrue(waitForNoService(aware, 15_000),
                "EntityMappings should unregister when its EPackage goes away");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Path writeEorm(Path dir, String name, String content) throws IOException {
        Path eorm = dir.resolve(name);
        Files.writeString(eorm, content);
        createdFiles.add(eorm);
        return eorm;
    }

    private Configuration startWatcher(ConfigurationAdmin ca, Path dir, String matcherKey) throws IOException {
        Configuration config = ca.getFactoryConfiguration(
                WatcherConstants.PID_ENTITY_MAPPINGS_FILE_WATCHER, matcherKey, "?");
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("io.fs.watcher.path", dir.toAbsolutePath() + "/");
        props.put("io.fs.watcher.pattern", ".*\\.eorm");
        props.put("file.context.matcher", matcherKey);
        props.put("jpa.root.folder", "data");
        config.update(props);
        createdConfigs.add(config);
        return config;
    }

    private ServiceRegistration<EPackage> registerStubEPackage(BundleContext ctx, String nsUri) {
        EPackage stub = EcoreFactory.eINSTANCE.createEPackage();
        stub.setName("stub");
        stub.setNsURI(nsUri);
        stub.setNsPrefix("stub");
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(EMFNamespaces.EMF_MODEL_NSURI, nsUri);
        props.put(WatcherConstants.KEY_JPA_ROOT_FOLDER, "data");
        ServiceRegistration<EPackage> reg = ctx.registerService(EPackage.class, stub, props);
        createdRegistrations.add(reg);
        return reg;
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
