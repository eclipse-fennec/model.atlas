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
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.eclipse.fennec.data.atlas.jpa.tests.helper.TestAnnotations.DataFolderWatcherConfig;
import org.eclipse.fennec.data.atlas.jpa.watcher.api.WatcherConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Integration tests for the CSV data import driven by the daanse
 * {@code fennec.jpa.CsvDataLoader} component inside the {@code DataFolderWatcher}
 * pipeline.
 *
 * <p>Initial-scan tests run against the static {@code data/} folder via
 * {@link DataFolderWatcherConfig}. Each test injects the watcher-owned H2
 * {@link DataSource} (filtered by {@code file.context.matcher}) and queries it
 * directly with JDBC to verify the rows that EclipseLink + the CSV importer
 * have populated.
 *
 * <p>Dynamic tests provision an isolated pipeline in a {@code @TempDir} so the
 * lifecycle of a single CSV (create / modify / delete) can be observed
 * independently from the static fixtures.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class JpaCsvDataImporterTests {

    private static final String FILTER_WATCHER_DS =
            "(" + WatcherConstants.KEY_FILE_CONTEXT_MATCHER + "=*)";

    private final List<Path> createdFiles = new ArrayList<>();
    private final List<Configuration> createdConfigs = new ArrayList<>();

    @AfterEach
    void cleanUp() throws IOException, InterruptedException {
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

    // ── Initial scan — static data/ folder ───────────────────────────────────

    @Test
    @DataFolderWatcherConfig
    public void testInitialImport_defaultSchemaTablesPopulated(
            @InjectService(cardinality = 0, filter = FILTER_WATCHER_DS) ServiceAware<DataSource> dsAware)
            throws Exception {
        DataSource ds = dsAware.waitForService(30_000);
        assertNotNull(ds, "Watcher-owned H2 DataSource should be registered");

        assertRowCount(ds, "employees", 5, 30_000);
        assertRowCount(ds, "products", 5, 30_000);
    }

    @Test
    @DataFolderWatcherConfig
    public void testInitialImport_schemaQualifiedTablesPopulated(
            @InjectService(cardinality = 0, filter = FILTER_WATCHER_DS) ServiceAware<DataSource> dsAware)
            throws Exception {
        DataSource ds = dsAware.waitForService(30_000);
        assertNotNull(ds);

        assertRowCount(ds, "finance.invoices", 5, 30_000);
        assertRowCount(ds, "finance.payments", 5, 30_000);
        assertRowCount(ds, "hr.contracts", 5, 30_000);
    }

    @Test
    @DataFolderWatcherConfig
    public void testInitialImport_specificRowValuesPresent(
            @InjectService(cardinality = 0, filter = FILTER_WATCHER_DS) ServiceAware<DataSource> dsAware)
            throws Exception {
        DataSource ds = dsAware.waitForService(30_000);
        assertNotNull(ds);
        assertRowCount(ds, "employees", 5, 30_000);

        try (Connection c = ds.getConnection();
             Statement s = c.createStatement();
             var rs = s.executeQuery(
                     "SELECT first_name, last_name, salary FROM employees WHERE id = 1")) {
            assertTrue(rs.next(), "employee with id=1 should exist");
            assertEquals("Alice", rs.getString("first_name"));
            assertEquals("Müller", rs.getString("last_name"));
            assertEquals(0, rs.getBigDecimal("salary").compareTo(new java.math.BigDecimal("72000")));
        }
    }

    // ── Dynamic CSV lifecycle — temp dir, isolated pipeline ─────────────────

    @Test
    public void testCsvAdded_rowsImported(@TempDir Path tempDir,
            @InjectBundleContext BundleContext ctx,
            @InjectService ConfigurationAdmin configAdmin) throws Exception {
        DataSource ds = setupTempPipeline(ctx, configAdmin, tempDir);

        // employees table doesn't exist yet — the daanse CSV importer creates it
        // on first CSV, and EclipseLink no longer auto-generates DDL.
        assertTableMissing(ds, "employees", 5_000);

        Path csv = tempDir.resolve("data").resolve("employees.csv");
        Files.writeString(csv, employeesCsv(
                "10,Foo,Bar,50000,99",
                "11,Baz,Qux,60000,99"));
        createdFiles.add(csv);

        assertRowCount(ds, "employees", 2, 30_000);
    }

    @Test
    public void testCsvModified_oldRowsReplacedByNew(@TempDir Path tempDir,
            @InjectBundleContext BundleContext ctx,
            @InjectService ConfigurationAdmin configAdmin) throws Exception {
        // Write CSV BEFORE the watcher activates so the initial scan picks it up.
        Files.createDirectories(tempDir.resolve("data"));
        Path csv = tempDir.resolve("data").resolve("employees.csv");
        Files.writeString(csv, employeesCsv(
                "1,Alice,Müller,72000,10",
                "2,Bob,Schmidt,65000,10",
                "3,Clara,Weber,80000,20"));
        createdFiles.add(csv);

        DataSource ds = setupTempPipeline(ctx, configAdmin, tempDir);
        assertRowCount(ds, "employees", 3, 30_000);
        
        Thread.sleep(2000);
        
        Files.writeString(csv, employeesCsv("99,Only,Remaining,1,0"));
        assertRowCount(ds, "employees", 1, 30_000);
    }

    @Test
    public void testCsvDeleted_tableDropped(@TempDir Path tempDir,
            @InjectBundleContext BundleContext ctx,
            @InjectService ConfigurationAdmin configAdmin) throws Exception {
        Files.createDirectories(tempDir.resolve("data"));
        Path csv = tempDir.resolve("data").resolve("employees.csv");
        Files.writeString(csv, employeesCsv(
                "1,Alice,Müller,72000,10",
                "2,Bob,Schmidt,65000,10"));
        createdFiles.add(csv);

        DataSource ds = setupTempPipeline(ctx, configAdmin, tempDir);
        assertRowCount(ds, "employees", 2, 30_000);

        Files.delete(csv);
        createdFiles.remove(csv);

        // The daanse CSV importer issues DROP TABLE on ENTRY_DELETE — we wait until
        // SELECT COUNT(*) fails with a "table not found" SQLException.
        assertTableMissing(ds, "employees", 30_000);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Copies {@code model.ecore} and {@code mapping.eorm} from the static fixture
     * into {@code tempDir/mapping/}, creates an empty {@code tempDir/data/},
     * configures a fresh {@code DataFolderWatcher} pointed at {@code tempDir},
     * and waits for the watcher-owned H2 DataSource to appear.
     */
    private DataSource setupTempPipeline(BundleContext ctx, ConfigurationAdmin ca, Path tempDir)
            throws Exception {
        Path mapping = tempDir.resolve("mapping");
        Path data = tempDir.resolve("data");
        Files.createDirectories(mapping);
        Files.createDirectories(data);

        Path staticData = Path.of(System.getProperty("data-folder"));
        Files.copy(staticData.resolve("mapping").resolve("model.ecore"),
                mapping.resolve("model.ecore"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(staticData.resolve("mapping").resolve("mapping.eorm"),
                mapping.resolve("mapping.eorm"), StandardCopyOption.REPLACE_EXISTING);

        String cfgName = "csv-test-" + UUID.randomUUID().toString().substring(0, 8);
        Configuration cfg = ca.getFactoryConfiguration(
                WatcherConstants.PID_DATA_FOLDER_WATCHER, cfgName, "?");
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("io.fs.watcher.path", tempDir.toAbsolutePath() + "/");
        cfg.update(props);
        createdConfigs.add(cfg);

        DataSource ds = waitForServiceByFilter(ctx, DataSource.class, FILTER_WATCHER_DS, 30_000);
        assertNotNull(ds, "DataSource for temp pipeline must appear within timeout");
        return ds;
    }

    private static String employeesCsv(String... dataRows) {
        StringBuilder sb = new StringBuilder("id,first_name,last_name,salary,department_id\n");
        sb.append("INTEGER,VARCHAR,VARCHAR,DECIMAL,BIGINT\n");
        for (String row : dataRows) {
            sb.append(row).append('\n');
        }
        return sb.toString();
    }

    /**
     * Poll {@code SELECT COUNT(*)} on the given table until it returns {@code expected}
     * rows, or fail after {@code timeoutMs}. Swallows {@link SQLException} during the
     * window — the table may not exist yet while EclipseLink is finishing DDL.
     */
    private void assertRowCount(DataSource ds, String table, int expected, long timeoutMs)
            throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        int actual = -1;
        SQLException lastEx = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                actual = rowCount(ds, table);
                if (actual == expected) {
                    return;
                }
            } catch (SQLException e) {
                lastEx = e;
            }
            Thread.sleep(300);
        }
        assertEquals(expected, actual,
                "Row count in " + table + " after " + timeoutMs + "ms"
                + (lastEx != null ? "; last SQL error: " + lastEx.getMessage() : ""));
    }

    private int rowCount(DataSource ds, String table) throws SQLException {
        try (Connection c = ds.getConnection();
             Statement s = c.createStatement();
             var rs = s.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * Wait until {@code SELECT COUNT(*) FROM <table>} fails with a "table not
     * found" {@link SQLException} — i.e. the table is absent. Covers both the
     * "never created" precondition (no CSV yet, EclipseLink not generating DDL)
     * and the "dropped after delete" case (the daanse CSV importer drops the
     * table on {@code ENTRY_DELETE}).
     */
    private void assertTableMissing(DataSource ds, String table, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            try {
                rowCount(ds, table);
            } catch (SQLException e) {
                return;
            }
            Thread.sleep(300);
        }
        throw new AssertionError("Table " + table + " should not exist within "
                + timeoutMs + "ms");
    }

    private <T> T waitForServiceByFilter(BundleContext ctx, Class<T> type, String filter, long timeoutMs)
            throws Exception {
        ServiceTracker<T, T> tracker = new ServiceTracker<>(ctx,
                ctx.createFilter("(&(objectClass=" + type.getName() + ")" + filter + ")"), null);
        tracker.open();
        try {
            return tracker.waitForService(timeoutMs);
        } finally {
            tracker.close();
        }
    }
}
