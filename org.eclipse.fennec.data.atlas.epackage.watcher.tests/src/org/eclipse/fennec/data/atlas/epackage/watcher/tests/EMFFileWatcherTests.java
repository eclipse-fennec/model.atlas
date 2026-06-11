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
package org.eclipse.fennec.data.atlas.epackage.watcher.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.function.BooleanSupplier;

import org.eclipse.daanse.io.fs.watcher.api.FileSystemWatcherListener;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Integration tests for EMFFileWatcher.
 *
 * <p>Each test creates its own {@code @TempDir}, drops one or more
 * {@code .ecore} files in it, then configures an {@code EMFFileWatcher}
 * factory configuration pointed at that directory. Pipeline behaviour is
 * verified by observing the {@link EPackage} OSGi services the watcher
 * publishes.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class EMFFileWatcherTests {

    private static final String PID = "EMFFileWatcher";
    private static final String NS_INITIAL  = "http://test.atlas/epackage-watcher/initial/1.0";
    private static final String NS_CREATED  = "http://test.atlas/epackage-watcher/created/1.0";
    private static final String NS_DELETED  = "http://test.atlas/epackage-watcher/deleted/1.0";
    private static final String NS_MODIFIED = "http://test.atlas/epackage-watcher/modified/1.0";
    private static final String NS_IGNORED  = "http://test.atlas/epackage-watcher/ignored/1.0";
    private static final String NS_FORWARD  = "http://test.atlas/epackage-watcher/forward/1.0";
    private static final String NS_DUP      = "http://test.atlas/epackage-watcher/dup/1.0";
    private static final String NS_DEACT    = "http://test.atlas/epackage-watcher/deact/1.0";
    private static final String NS_DUP_MOD  = "http://test.atlas/epackage-watcher/dupmod/1.0";
    private static final String NS_RECREATE = "http://test.atlas/epackage-watcher/recreate/1.0";

    private static final String ECORE_TEMPLATE = """
            <?xml version="1.0" encoding="UTF-8"?>
            <ecore:EPackage xmi:version="2.0"
                xmlns:xmi="http://www.omg.org/XMI"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
                name="%s"
                nsURI="%s"
                nsPrefix="%s">
              <eClassifiers xsi:type="ecore:EClass" name="Entity">
                <eStructuralFeatures xsi:type="ecore:EAttribute" name="id"
                    eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//ELong"/>
              </eClassifiers>
            </ecore:EPackage>
            """;

    private final List<Configuration> createdConfigs = new ArrayList<>();

    @AfterEach
    void cleanUp() throws InterruptedException {
        for (Configuration c : createdConfigs) {
            try {
                c.delete();
            } catch (IOException ignored) {
                // already deleted by the test
            }
        }
        createdConfigs.clear();
        Thread.sleep(1500);
    }

    @Test
    public void testInitialScan_ecoreRegistered(@TempDir Path dir,
            @InjectService ConfigurationAdmin ca,
            @InjectService(cardinality = 0, filter = "(emf.nsURI=" + NS_INITIAL + ")") ServiceAware<EPackage> aware)
            throws Exception {
        writeEcore(dir, "model.ecore", "initial", NS_INITIAL);

        registerWatcher(ca, dir, "initial-scan", null, null);

        EPackage ep = aware.waitForService(15_000);
        assertNotNull(ep, "EPackage should be registered after the initial scan");
        assertEquals(NS_INITIAL, ep.getNsURI());
    }

    @Test
    public void testEcoreCreated_serviceAppears(@TempDir Path dir,
            @InjectService ConfigurationAdmin ca,
            @InjectService(cardinality = 0, filter = "(emf.nsURI=" + NS_CREATED + ")") ServiceAware<EPackage> aware)
            throws Exception {
        registerWatcher(ca, dir, "ecore-created", null, null);

        Thread.sleep(1500);
        assertTrue(aware.isEmpty(), "No EPackage expected before the file is created");

        writeEcore(dir, "model.ecore", "created", NS_CREATED);

        assertNotNull(aware.waitForService(15_000),
                "EPackage should be registered after the .ecore file is created");
    }

    @Test
    public void testEcoreDeleted_serviceUnregisters(@TempDir Path dir,
            @InjectService ConfigurationAdmin ca,
            @InjectService(cardinality = 0, filter = "(emf.nsURI=" + NS_DELETED + ")") ServiceAware<EPackage> aware)
            throws Exception {
        Path file = writeEcore(dir, "model.ecore", "deleted", NS_DELETED);
        registerWatcher(ca, dir, "ecore-deleted", null, null);
        assertNotNull(aware.waitForService(15_000));

        Files.delete(file);

        assertTrue(waitUntil(aware::isEmpty, 15_000),
                "EPackage should be unregistered after the .ecore file is deleted");
    }

    /**
     * Guards the same-nsURI-modify regression: previously the dedup check
     * tripped on the freshly-loaded EPackage and skipped re-registration.
     */
    @Test
    public void testEcoreModified_sameNsUriReRegisters(@TempDir Path dir,
            @InjectService ConfigurationAdmin ca,
            @InjectService(cardinality = 0, filter = "(emf.nsURI=" + NS_MODIFIED + ")") ServiceAware<EPackage> aware)
            throws Exception {
        Path file = writeEcore(dir, "model.ecore", "modified", NS_MODIFIED);
        registerWatcher(ca, dir, "ecore-modified", null, null);
        assertNotNull(aware.waitForService(15_000));

        ServiceReference<EPackage> initialRef = aware.getServiceReference();
        Long initialSid = (Long) initialRef.getProperty(Constants.SERVICE_ID);
        assertNotNull(initialSid);

        // Same nsURI, different content (extra EClass).
        Files.writeString(file, """
                <?xml version="1.0" encoding="UTF-8"?>
                <ecore:EPackage xmi:version="2.0"
                    xmlns:xmi="http://www.omg.org/XMI"
                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
                    name="modified"
                    nsURI="%s"
                    nsPrefix="modified">
                  <eClassifiers xsi:type="ecore:EClass" name="Entity"/>
                  <eClassifiers xsi:type="ecore:EClass" name="OtherEntity"/>
                </ecore:EPackage>
                """.formatted(NS_MODIFIED));

        assertTrue(waitUntil(() -> {
            ServiceReference<EPackage> ref = aware.getServiceReference();
            if (ref == null) return false;
            Long sid = (Long) ref.getProperty(Constants.SERVICE_ID);
            return sid != null && !sid.equals(initialSid);
        }, 30_000), "EPackage should be re-registered with a new service.id after MODIFY with unchanged nsURI");
    }

    @Test
    public void testNonEcoreFile_ignored(@TempDir Path dir,
            @InjectService ConfigurationAdmin ca,
            @InjectService(cardinality = 0, filter = "(emf.nsURI=" + NS_IGNORED + ")") ServiceAware<EPackage> aware)
            throws Exception {
        // Drop a non-.ecore file that contains ecore-like content. The pattern filter must reject it.
        Files.writeString(dir.resolve("notes.txt"),
                ECORE_TEMPLATE.formatted("ignored", NS_IGNORED, "ignored"));
        registerWatcher(ca, dir, "non-ecore", null, null);

        Thread.sleep(3000);
        assertTrue(aware.isEmpty(), "Non-.ecore files must not produce EPackage services");
    }

    @Test
    public void testForwardedProperties_propagatedAndInternalSuppressed(@TempDir Path dir,
            @InjectService ConfigurationAdmin ca,
            @InjectService(cardinality = 0, filter = "(emf.nsURI=" + NS_FORWARD + ")") ServiceAware<EPackage> aware)
            throws Exception {
        writeEcore(dir, "model.ecore", "forward", NS_FORWARD);

        registerWatcher(ca, dir, "forward", "file.context.matcher", "matcher-abc");

        assertNotNull(aware.waitForService(15_000));
        ServiceReference<EPackage> ref = aware.getServiceReference();

        assertEquals("matcher-abc", ref.getProperty("file.context.matcher"),
                "Pipeline-level config keys must ride along on the EPackage service");
        assertNull(ref.getProperty("io.fs.watcher.path"),
                "io.fs.watcher.* keys must be stripped from forwarded properties");
    }

    @Test
    public void testTwoFilesSameNsUri_secondSkipped(@TempDir Path dir,
            @InjectService ConfigurationAdmin ca,
            @InjectService(cardinality = 0, filter = "(emf.nsURI=" + NS_DUP + ")") ServiceAware<EPackage> aware)
            throws Exception {
        writeEcore(dir, "first.ecore", "first", NS_DUP);
        writeEcore(dir, "second.ecore", "second", NS_DUP);

        registerWatcher(ca, dir, "dup", null, null);

        assertNotNull(aware.waitForService(15_000));
        Thread.sleep(2500);
        assertEquals(1, aware.getServices().size(),
                "Two .ecore files sharing one nsURI must produce exactly one EPackage service");
    }

    @Test
    public void testDeactivation_servicesUnregistered(@TempDir Path dir,
            @InjectService ConfigurationAdmin ca,
            @InjectService(cardinality = 0, filter = "(emf.nsURI=" + NS_DEACT + ")") ServiceAware<EPackage> aware)
            throws Exception {
        writeEcore(dir, "model.ecore", "deact", NS_DEACT);
        Configuration cfg = registerWatcher(ca, dir, "deactivate", null, null);

        assertNotNull(aware.waitForService(15_000));

        createdConfigs.remove(cfg);
        cfg.delete();

        assertTrue(waitUntil(aware::isEmpty, 15_000),
                "Deleting the watcher configuration must unregister its EPackage services");
    }

    /**
     * Regression guard: two MODIFY events landing in the same debounce window
     * (editors firing truncate+write or atomic-rename) must not leave the
     * EPackage proxified. Bypasses the underlying WatchService — which may
     * coalesce rapid writes — by calling the listener directly so the
     * duplicate-URI race is exercised deterministically.
     */
    @Test
    public void testDuplicateModifyEvents_ePackageReloadedIntact(@TempDir Path dir,
            @InjectBundleContext BundleContext ctx,
            @InjectService ConfigurationAdmin ca,
            @InjectService(cardinality = 0, filter = "(emf.nsURI=" + NS_DUP_MOD + ")") ServiceAware<EPackage> aware)
            throws Exception {
        Path file = writeEcore(dir, "model.ecore", "dupmod", NS_DUP_MOD);
        registerWatcher(ca, dir, "dup-modify", null, null);
        assertNotNull(aware.waitForService(15_000));

        FileSystemWatcherListener listener = findListenerForDir(ctx, dir);
        assertNotNull(listener, "EMFFileWatcher should expose a FileSystemWatcherListener for the temp dir");

        // Two MODIFY events in quick succession — both must land in the
        // 1-second debounce window of the watcher.
        listener.handlePathEvent(file, StandardWatchEventKinds.ENTRY_MODIFY);
        listener.handlePathEvent(file, StandardWatchEventKinds.ENTRY_MODIFY);

        // Wait for the watcher's timer to flush the pending URIs and for the
        // new EPackage service to be re-registered. Without dedup, the service
        // count still settles at 1, but the registered EPackage is a proxy.
        assertTrue(waitUntil(() -> aware.getServices().size() == 1
                && !aware.getServices().get(0).eIsProxy(), 15_000),
                "Re-registered EPackage must be healthy (non-proxy)");

        EPackage ep = aware.getServices().get(0);
        EClass entity = (EClass) ep.getEClassifier("Entity");
        assertNotNull(entity, "Entity EClass should still be present after reload");
        assertFalse(entity.eIsProxy(), "Entity EClass must not be a proxy");
        assertEquals(1, entity.getEStructuralFeatures().size(),
                "Entity must retain its EAttribute after reload");
        EAttribute idAttr = (EAttribute) entity.getEStructuralFeature("id");
        assertNotNull(idAttr, "id EAttribute must be present after reload");
        assertNotNull(idAttr.getEAttributeType(),
                "EAttribute must have a resolved type — guards the EclipseLink NPE");
    }

    /**
     * Regression guard for atomic-rename saves (vim, IntelliJ, etc). On Linux
     * the inotify {@code IN_MOVED_TO} from the rename surfaces as
     * {@code ENTRY_CREATE} — there is no preceding MODIFY/DELETE. The watcher
     * must still tear down the prior registration before reloading, otherwise
     * the dedup-by-owned-nsURI check rejects the fresh load.
     */
    @Test
    public void testCreateOnAlreadyOwnedFile_ePackageReloaded(@TempDir Path dir,
            @InjectBundleContext BundleContext ctx,
            @InjectService ConfigurationAdmin ca,
            @InjectService(cardinality = 0, filter = "(emf.nsURI=" + NS_RECREATE + ")") ServiceAware<EPackage> aware)
            throws Exception {
        Path file = writeEcore(dir, "model.ecore", "recreate", NS_RECREATE);
        registerWatcher(ca, dir, "recreate", null, null);
        assertNotNull(aware.waitForService(15_000));

        FileSystemWatcherListener listener = findListenerForDir(ctx, dir);
        assertNotNull(listener);

        // Simulate an atomic-rename save: the watcher sees ENTRY_CREATE for an
        // already-owned file (without a preceding MODIFY/DELETE).
        listener.handlePathEvent(file, StandardWatchEventKinds.ENTRY_CREATE);

        assertTrue(waitUntil(() -> aware.getServices().size() == 1
                && !aware.getServices().get(0).eIsProxy(), 15_000),
                "Re-registered EPackage must be healthy after CREATE-on-rename");

        EPackage ep = aware.getServices().get(0);
        EClass entity = (EClass) ep.getEClassifier("Entity");
        assertNotNull(entity);
        assertFalse(entity.eIsProxy());
        EAttribute idAttr = (EAttribute) entity.getEStructuralFeature("id");
        assertNotNull(idAttr);
        assertNotNull(idAttr.getEAttributeType());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Configuration registerWatcher(ConfigurationAdmin ca, Path dir, String name,
            String extraKey, String extraValue) throws IOException {
        Configuration cfg = ca.getFactoryConfiguration(PID, name, "?");
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("io.fs.watcher.path", dir.toAbsolutePath() + "/");
        if (extraKey != null) {
            props.put(extraKey, extraValue);
        }
        cfg.update(props);
        createdConfigs.add(cfg);
        return cfg;
    }

    private Path writeEcore(Path dir, String filename, String pkgName, String nsUri) throws IOException {
        Path file = dir.resolve(filename);
        Files.writeString(file, ECORE_TEMPLATE.formatted(pkgName, nsUri, pkgName));
        return file;
    }

    private boolean waitUntil(BooleanSupplier condition, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            Thread.sleep(250);
        }
        return condition.getAsBoolean();
    }

    @SuppressWarnings("unchecked")
    private FileSystemWatcherListener findListenerForDir(BundleContext ctx, Path dir) throws InvalidSyntaxException {
        String dirPath = dir.toAbsolutePath() + "/";
        String filter = "(&(objectClass=" + FileSystemWatcherListener.class.getName() + ")"
                + "(io.fs.watcher.path=" + dirPath + "))";
        ServiceReference<?>[] refs = ctx.getServiceReferences((String) null, filter);
        if (refs == null || refs.length == 0) {
            return null;
        }
        return (FileSystemWatcherListener) ctx.getService((ServiceReference<Object>) refs[0]);
    }
}
