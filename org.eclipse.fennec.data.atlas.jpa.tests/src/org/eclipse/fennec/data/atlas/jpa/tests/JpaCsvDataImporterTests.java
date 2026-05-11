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

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.UUID;

import javax.sql.DataSource;

import org.eclipse.fennec.data.atlas.jpa.tests.helper.TestAnnotations;
import org.eclipse.fennec.data.atlas.jpa.tests.helper.TestAnnotations.DataFolderWatcherConfig;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig;
import org.junit.jupiter.api.BeforeEach;
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

import jakarta.persistence.EntityManagerFactory;

/**
 * Integration tests for the CSV import pipeline.
 *
 * <p>Initial-scan tests use {@link DataFolderWatcherConfig} to activate the full pipeline
 * (EMFFileWatcher → JpaMappingFileWatcher → DataSourceConfigHandler → JpaModelSetup →
 * EMPersistenceUnit → JpaCsvDataImporter) against the static {@code data/} folder.
 * EclipseLink creates schemas and tables; the CSV importer loads the data rows.
 *
 * <p>Dynamic tests (file modify / delete / bad row) spin up an isolated pipeline in a
 * {@code @TempDir} with a unique unit name and H2 database, so they do not interfere
 * with the static-folder tests or with each other.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class JpaCsvDataImporterTests {
	
	Path tempDir;
	
	@BeforeEach
	public void beforeEach(@TempDir Path tempDir) {
		System.setProperty(TestAnnotations.TEMP_DIR, tempDir.toAbsolutePath().toString());
		this.tempDir = tempDir;
	}

    // ── Initial scan (static data/ folder) ───────────────────────────────────

    @Test
    @DataFolderWatcherConfig
    void testInitialScan_populatesDefaultSchemaTables(
    		@InjectBundleContext BundleContext ctx,
			@InjectService(cardinality = 0, filter = "(jpamapping.name=" + TestAnnotations.JPA_MAPPING_NAME + ")")
			ServiceAware<JpaMappingConfig> configAware,
			@InjectService(cardinality = 0, filter = "(osgi.unit.name=" + TestAnnotations.JPA_MAPPING_NAME + ")")
			ServiceAware<EntityManagerFactory> emfAware, 
			@InjectService(cardinality = 0, filter = "(unitName=" + TestAnnotations.JPA_MAPPING_NAME + ")")
			ServiceAware<DataSource> dsAware)
            throws Exception {
    	
    	assertNotNull(configAware.waitForService(10_000), "JpaMappingConfig for " + TestAnnotations.JPA_MAPPING_NAME + " should be registered");
		assertNotNull(emfAware.waitForService(15_000), "EntityManagerFactory for data should be registered");
		assertNotNull(dsAware.waitForService(15_000), "DataSource for data should be registered");

		DataSource ds = dsAware.getService();
        assertRowCount(ds, "employees", 5, 30_000);
        assertRowCount(ds, "products",  5, 30_000);
    }

    @Test
    @DataFolderWatcherConfig
    void testInitialScan_populatesSchemaQualifiedTables(
            @InjectService(filter = "(unitName=" + TestAnnotations.JPA_MAPPING_NAME + ")", timeout = 30_000) DataSource ds)
            throws Exception {
        assertRowCount(ds, "finance.invoices",  5, 30_000);
        assertRowCount(ds, "finance.payments",  5, 30_000);
        assertRowCount(ds, "hr.contracts",      5, 30_000);
    }

    @Test
    void testCrossSchemaJoin_contractsLinkedToEmployees(
            @TempDir Path tempDir,
            @InjectBundleContext BundleContext ctx,
            @InjectService ConfigurationAdmin configAdmin) throws Exception {
        String unitName = "csv-join-" + UUID.randomUUID().toString().substring(0, 8);
        DataSource ds = startPipelineWithJoin(ctx, configAdmin, tempDir, unitName,
                employeesCsv(
                        "1,Alice,Müller,72000,10",
                        "2,Bob,Schmidt,65000,10",
                        "3,Clara,Weber,80000,20"),
                contractsCsv(
                        "1,1,2022-03-01,,PERMANENT,72000",
                        "2,2,2021-06-15,,PERMANENT,65000",
                        "3,3,2023-01-10,,PERMANENT,80000"));
        assertRowCount(ds, "employees",   3, 30_000);
        assertRowCount(ds, "hr.contracts", 3, 30_000);
    }

    // ── Dynamic tests (isolated temp dir) ────────────────────────────────────

    @Test
    void testFileModified_oldDataClearedAndNewDataImported(
            @TempDir Path tempDir,
            @InjectBundleContext BundleContext ctx,
            @InjectService ConfigurationAdmin configAdmin) throws Exception {
        DataSource ds = startPipeline(ctx, configAdmin, tempDir, employeesCsv(
                "1,Alice,Müller,72000,10",
                "2,Bob,Schmidt,65000,10",
                "3,Clara,Weber,80000,20"));
        assertRowCount(ds, "employees", 3, 30_000);

        Files.writeString(tempDir.resolve("data").resolve("employees.csv"), employeesCsv("10,Foo,Bar,50000,99"));
        assertRowCount(ds, "employees", 1, 30_000);
    }

    @Test
    void testFileDeleted_tableClearedAndRemainsEmpty(
            @TempDir Path tempDir,
            @InjectBundleContext BundleContext ctx,
            @InjectService ConfigurationAdmin configAdmin) throws Exception {
        DataSource ds = startPipeline(ctx, configAdmin, tempDir, employeesCsv(
                "1,Alice,Müller,72000,10",
                "2,Bob,Schmidt,65000,10",
                "3,Clara,Weber,80000,20"));
        assertRowCount(ds, "employees", 3, 30_000);

        Files.delete(tempDir.resolve("data").resolve("employees.csv"));
        assertRowCount(ds, "employees", 0, 30_000);
    }


    @Test
    void testBadRow_skippedAndRemainingRowsImported(
            @TempDir Path tempDir,
            @InjectBundleContext BundleContext ctx,
            @InjectService ConfigurationAdmin configAdmin) throws Exception {
        DataSource ds = startPipeline(ctx, configAdmin, tempDir, employeesCsv(
                "1,Alice,Müller,72000,10",
                "not_a_number,Bob,Schmidt,65000,10",   // bad id → INSERT fails → row skipped
                "3,Clara,Weber,80000,20"));
        assertRowCount(ds, "employees", 2, 30_000);
    }

    // ── Pipeline setup ────────────────────────────────────────────────────────

    /**
     * Copies {@code model.ecore} from the static data folder to {@code dir}, writes a
     * minimal {@code mapping.jpamapping} with a unique unit name and H2 database, writes
     * the supplied {@code employeesCsv} content, and starts a {@code DataFolderWatcher}
     * for {@code dir}.  Returns the DataSource once it appears in the registry.
     */
    private DataSource startPipeline(BundleContext ctx, ConfigurationAdmin ca,
            Path dir, String employeesCsv) throws Exception {
        return startPipeline(ctx, ca, dir,
                "csv-test-" + UUID.randomUUID().toString().substring(0, 8), employeesCsv);
    }

    private DataSource startPipeline(BundleContext ctx, ConfigurationAdmin ca,
            Path dir, String unitName, String employeesCsv) throws Exception {
        Path staticData = Path.of(System.getProperty("data-folder"));
        Files.createDirectories(dir.resolve("mapping"));
        Files.createDirectories(dir.resolve("data"));
        Files.copy(staticData.resolve("mapping").resolve("model.ecore"), dir.resolve("mapping").resolve("model.ecore"));
        Files.writeString(dir.resolve("mapping").resolve("mapping.jpamapping"), mappingXml(unitName));
        Files.writeString(dir.resolve("data").resolve("employees.csv"), employeesCsv);

        Configuration cfg = ca.getFactoryConfiguration("DataFolderWatcher", unitName, "?");
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("io.fs.watcher.path", dir.toAbsolutePath().toString());
        cfg.update(props);

        DataSource ds = waitForService(ctx, DataSource.class, "(unitName=" + unitName + ")", 30_000);
        assertNotNull(ds, "DataSource for unit '" + unitName + "' must appear within timeout");
        return ds;
    }

    private DataSource startPipelineWithJoin(BundleContext ctx, ConfigurationAdmin ca,
            Path dir, String unitName, String employeesCsv, String contractsCsv) throws Exception {
        Path staticData = Path.of(System.getProperty("data-folder"));
        Files.createDirectories(dir.resolve("mapping"));
        Files.createDirectories(dir.resolve("data").resolve("hr"));
        Files.copy(staticData.resolve("mapping").resolve("model.ecore"), dir.resolve("mapping").resolve("model.ecore"));
        Files.writeString(dir.resolve("mapping").resolve("mapping.jpamapping"), mappingXmlWithJoin(unitName));
        Files.writeString(dir.resolve("data").resolve("employees.csv"), employeesCsv);
        Files.writeString(dir.resolve("data").resolve("hr").resolve("contracts.csv"), contractsCsv);

        Configuration cfg = ca.getFactoryConfiguration("DataFolderWatcher", unitName, "?");
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("io.fs.watcher.path", dir.toAbsolutePath().toString());
        cfg.update(props);

        DataSource ds = waitForService(ctx, DataSource.class, "(unitName=" + unitName + ")", 30_000);
        assertNotNull(ds, "DataSource for unit '" + unitName + "' must appear within timeout");
        return ds;
    }

    // ── CSV builders ──────────────────────────────────────────────────────────

    private static String employeesCsv(String... dataRows) {
        StringBuilder sb = new StringBuilder("id,first_name,last_name,salary,department_id\n");
        sb.append("BIGINT,VARCHAR,VARCHAR,DECIMAL,BIGINT\n");
        for (String row : dataRows) {
            sb.append(row).append('\n');
        }
        return sb.toString();
    }

    
    private static String contractsCsv(String... dataRows) {
        StringBuilder sb = new StringBuilder("id,employee_id,start_date,end_date,contract_type,salary\n");
        sb.append("BIGINT,BIGINT,VARCHAR,VARCHAR,VARCHAR,DECIMAL\n");
        for (String row : dataRows) {
            sb.append(row).append('\n');
        }
        return sb.toString();
    }

    // ── Mapping XML ───────────────────────────────────────────────────────────

   

    private static String mappingXml(String unitName) {
        String h2DbName = unitName.replace("-", "_");
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <jpamapping:JpaMappingConfig xmi:version="2.0"
                    xmlns:xmi="http://www.omg.org/XMI"
                    xmlns:jpamapping="http://eclipse.org/fennec/data/atlas/jpamapping/1.0.0"
                    name="%s"
                    targetModelNsUri="http://example.org/jpa/demo/1.0">
                  <dataSource
                      driverClass="org.h2.Driver"
                      jdbcUrl="jdbc:h2:mem:%s;DATABASE_TO_UPPER=FALSE;DB_CLOSE_DELAY=-1"
                      username="sa"
                      passwordRef="DB_PASSWORD"
                      poolSize="5"
                      dialect="H2"/>
                  <tableMappings
                      className="http://example.org/jpa/demo/1.0#//Employee"
                      tableName="employees">
                    <columnMappings featureName="id"           columnName="id"            columnType="BIGINT"        nullable="false" primaryKey="true"/>
                    <columnMappings featureName="firstName"    columnName="first_name"    columnType="VARCHAR(255)"  nullable="false" primaryKey="false"/>
                    <columnMappings featureName="lastName"     columnName="last_name"     columnType="VARCHAR(255)"  nullable="false" primaryKey="false"/>
                    <columnMappings featureName="salary"       columnName="salary"        columnType="DECIMAL(15,2)" nullable="true"  primaryKey="false"/>
                    <columnMappings featureName="departmentId" columnName="department_id" columnType="BIGINT"        nullable="true"  primaryKey="false"/>
                  </tableMappings>
                </jpamapping:JpaMappingConfig>
                """.formatted(unitName, h2DbName);
    }

    private static String mappingXmlWithJoin(String unitName) {
        String h2DbName = unitName.replace("-", "_");
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <jpamapping:JpaMappingConfig xmi:version="2.0"
                    xmlns:xmi="http://www.omg.org/XMI"
                    xmlns:jpamapping="http://eclipse.org/fennec/data/atlas/jpamapping/1.0.0"
                    name="%s"
                    targetModelNsUri="http://example.org/jpa/demo/1.0">
                  <dataSource
                      driverClass="org.h2.Driver"
                      jdbcUrl="jdbc:h2:mem:%s;DATABASE_TO_UPPER=FALSE;DB_CLOSE_DELAY=-1"
                      username="sa"
                      passwordRef="DB_PASSWORD"
                      poolSize="5"
                      dialect="H2"/>
                  <tableMappings
                      className="http://example.org/jpa/demo/1.0#//Employee"
                      tableName="employees">
                    <columnMappings featureName="id"           columnName="id"            columnType="BIGINT"        nullable="false" primaryKey="true"/>
                    <columnMappings featureName="firstName"    columnName="first_name"    columnType="VARCHAR(255)"  nullable="false" primaryKey="false"/>
                    <columnMappings featureName="lastName"     columnName="last_name"     columnType="VARCHAR(255)"  nullable="false" primaryKey="false"/>
                    <columnMappings featureName="salary"       columnName="salary"        columnType="DECIMAL(15,2)" nullable="true"  primaryKey="false"/>
                    <columnMappings featureName="departmentId" columnName="department_id" columnType="BIGINT"        nullable="true"  primaryKey="false"/>
                    <joinMappings referenceName="contracts" joinType="FOREIGN_KEY" joinColumn="employee_id" cascadeType="ALL"/>
                  </tableMappings>
                  <tableMappings
                      className="http://example.org/jpa/demo/1.0#//Contract"
                      tableName="contracts"
                      schema="hr">
                    <columnMappings featureName="id"           columnName="id"            columnType="BIGINT"        nullable="false" primaryKey="true"/>
                    <columnMappings featureName="startDate"    columnName="start_date"    columnType="VARCHAR(20)"   nullable="false" primaryKey="false"/>
                    <columnMappings featureName="endDate"      columnName="end_date"      columnType="VARCHAR(20)"   nullable="true"  primaryKey="false"/>
                    <columnMappings featureName="contractType" columnName="contract_type" columnType="VARCHAR(50)"   nullable="false" primaryKey="false"/>
                    <columnMappings featureName="salary"       columnName="salary"        columnType="DECIMAL(15,2)" nullable="false" primaryKey="false"/>
                  </tableMappings>
                </jpamapping:JpaMappingConfig>
                """.formatted(unitName, h2DbName);
    }

    // ── Assertion helpers ─────────────────────────────────────────────────────

    private void assertRowCount(DataSource ds, String table, int expected, long timeoutMs)
            throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        int actual = 0;
        while (System.currentTimeMillis() < deadline) {
            try {
                actual = rowCount(ds, table);
                if (actual == expected) {
                    return;
                }
            } catch (SQLException ignored) {
                // Table may not exist yet (EclipseLink still starting); keep polling.
            }
            Thread.sleep(300);
        }
        assertEquals(expected, actual, "Row count in " + table + " after timeout");
    }

   
    private int rowCount(DataSource ds, String table) throws SQLException {
        try (Connection c = ds.getConnection();
             var rs = c.createStatement().executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    // ── Service wait ──────────────────────────────────────────────────────────

    private <T> T waitForService(BundleContext ctx, Class<T> type, String filter, long timeoutMs)
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
