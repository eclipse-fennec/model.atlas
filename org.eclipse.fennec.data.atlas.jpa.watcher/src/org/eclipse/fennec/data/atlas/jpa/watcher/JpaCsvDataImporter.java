///**
// * Copyright (c) 2012 - 2026 Data In Motion and others.
// * All rights reserved.
// *
// * This program and the accompanying materials are made
// * available under the terms of the Eclipse Public License 2.0
// * which is available at https://www.eclipse.org/legal/epl-2.0/
// *
// * SPDX-License-Identifier: EPL-2.0
// *
// * Contributors:
// *     Data In Motion - initial API and implementation
// */
//package org.eclipse.fennec.data.atlas.jpa.watcher;
//
//import java.io.IOException;
//import java.lang.System.Logger;
//import java.lang.System.Logger.Level;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.StandardWatchEventKinds;
//import java.nio.file.WatchEvent.Kind;
//import java.sql.Connection;
//import java.sql.JDBCType;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import javax.sql.DataSource;
//
//import org.eclipse.daanse.io.fs.watcher.api.FileSystemWatcherListener;
//import org.eclipse.daanse.io.fs.watcher.api.propertytypes.FileSystemWatcherListenerProperties;
//import org.osgi.service.component.annotations.Activate;
//import org.osgi.service.component.annotations.Component;
//import org.osgi.service.component.annotations.ConfigurationPolicy;
//import org.osgi.service.component.annotations.Deactivate;
//import org.osgi.service.component.annotations.Reference;
//import org.osgi.service.component.annotations.ServiceScope;
//import org.osgi.service.metatype.annotations.AttributeDefinition;
//import org.osgi.service.metatype.annotations.Designate;
//import org.osgi.service.metatype.annotations.ObjectClassDefinition;
//
//import de.siegmar.fastcsv.reader.CloseableIterator;
//import de.siegmar.fastcsv.reader.CsvReader;
//import de.siegmar.fastcsv.reader.NamedCsvRecord;
//import jakarta.persistence.EntityManagerFactory;
//
///**
// * Data-only CSV importer for JPA-managed tables.
// *
// * <p>Expects the same two-row-header CSV format used by the daanse CsvDataImporter:
// * row 1 = column names, row 2 = JDBC type specs (e.g. {@code BIGINT}, {@code VARCHAR(255)}),
// * remaining rows = data.
// *
// * <p>On each CSV file event this component:
// * <ol>
// *   <li>Issues {@code DELETE FROM <table>} to clear existing rows (the table structure is
// *       owned by EclipseLink and is left untouched).
// *   <li>Inserts every data row via a {@link PreparedStatement}.  A failure on an individual
// *       row is logged at WARNING level and skipped; the remaining rows are still imported.
// * </ol>
// *
// * <p>Schema is derived from the CSV file's parent folder name (same convention as the daanse
// * importer): files in the root watched folder have no schema qualifier; files in a sub-folder
// * use the sub-folder name as the schema.
// */
//@Designate(factory = true, ocd = JpaCsvDataImporter.Config.class)
//@Component(name = JpaCsvDataImporter.PID, scope = ServiceScope.SINGLETON,
//           service = FileSystemWatcherListener.class,
//           configurationPolicy = ConfigurationPolicy.REQUIRE)
//@FileSystemWatcherListenerProperties(pattern = ".*.csv", recursive = true)
//public class JpaCsvDataImporter implements FileSystemWatcherListener {
//
//    public static final String PID = "fennec.jpa.CsvDataLoader";
//
//    private static final Logger LOG = System.getLogger(JpaCsvDataImporter.class.getName());
//
//    @ObjectClassDefinition
//    public @interface Config {
//        @AttributeDefinition(name = "Field separator", required = false)
//        char fieldSeparator() default ',';
//
//        @AttributeDefinition(name = "Quote character", required = false)
//        char quoteCharacter() default '"';
//
//        @AttributeDefinition(name = "Skip empty lines", required = false)
//        boolean skipEmptyLines() default true;
//
//        @AttributeDefinition(name = "Null value token", required = false)
//        String nullValue() default "";
//    }
//
//    // Target overridden at runtime via dataSource.target config property.                                                          
//    // Acts as a readiness gate: DS activates this component only after the DataSource                                                                  
//    // specific for that folder has been activated
//    @Reference(name = "dataSource", target = "(scope=no-inject)")
//    private DataSource dataSource;
//    
//    // Target overridden at runtime via entityManagerFactory.target config property.                                                          
//    // Acts as a readiness gate: DS activates this component only after EclipseLink                                                                  
//    // has created the tables and registered the EntityManagerFactory service. 
//    @Reference(name = "entityManagerFactory", target = "(scope=no-inject)")
//    EntityManagerFactory entityManagerFactory;
//
//    private Path basePath;
//    private Config config;
//
//    @Activate
//    void activate(Config config) {
//        this.config = config;
//        System.out.println("Activated CsvImporter");
//    }
//    
//    @Deactivate
//    void deactivate() {
//    	System.out.println("Deactivated CsvImporter");
//    }
//
//    @Override
//    public void handleBasePath(Path basePath) {
//        this.basePath = basePath;
//    }
//
//    @Override
//    public void handleInitialPaths(List<Path> paths) {
//        paths.forEach(this::loadIfCsv);
//    }
//
//    @Override
//    public void handlePathEvent(Path path, Kind<Path> kind) {
//        if (Files.isDirectory(path)) {
//            return;
//        }
//        if (StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)
//                || StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
//            loadIfCsv(path);
//        }
//        if (StandardWatchEventKinds.ENTRY_DELETE.equals(kind) && path.toString().endsWith(".csv")) {
//            clearTable(path);
//        }
//    }
//
//    private void loadIfCsv(Path path) {
//        if (Files.isDirectory(path) || !path.toString().endsWith(".csv")) {
//            return;
//        }
//        try (Connection conn = dataSource.getConnection()) {
//            load(conn, path);
//        } catch (SQLException e) {
//            LOG.log(Level.WARNING, "Cannot acquire DB connection for " + path + ": " + e.getMessage());
//        }
//    }
//
//    private void load(Connection conn, Path path) {
//        String qualified = qualifiedTableName(path);
//        // Disable FK checks before the transaction: SET REFERENTIAL_INTEGRITY is a DDL-level
//        // statement in H2 that causes an implicit commit, so it must run outside the transaction
//        // boundary or it would commit the DELETE before insertRows runs.
//        boolean riDisabled = tryDisableReferentialIntegrity(conn);
//        try {
//            conn.setAutoCommit(false);
//        } catch (SQLException e) {
//            LOG.log(Level.WARNING, "Cannot disable auto-commit for " + qualified + ": " + e.getMessage());
//            if (riDisabled) tryEnableReferentialIntegrity(conn);
//            return;
//        }
//        try {
//            deleteRows(conn, qualified);
//            insertRows(conn, path, qualified);
//            conn.commit();
//        } catch (IOException | SQLException e) {
//            LOG.log(Level.WARNING, "Load of " + qualified + " failed — rolling back: " + e.getMessage());
//            safeRollback(conn);
//        } finally {
//            safeSetAutoCommit(conn, true);
//            if (riDisabled) tryEnableReferentialIntegrity(conn);
//        }
//    }
//
//    private void deleteRows(Connection conn, String qualified) throws SQLException {
//        try (Statement st = conn.createStatement()) {
//            st.execute("DELETE FROM " + qualified);
//        }
//    }
//
//    private void insertRows(Connection conn, Path path, String qualified) throws IOException, SQLException {
//        CsvReader.CsvReaderBuilder builder = CsvReader.builder()
//                .fieldSeparator(config.fieldSeparator())
//                .quoteCharacter(config.quoteCharacter())
//                .skipEmptyLines(config.skipEmptyLines());
//
//        try (CloseableIterator<NamedCsvRecord> it = builder.ofNamedCsvRecord(path).iterator()) {
//            if (!it.hasNext()) {
//                return;
//            }
//            List<Column> columns = parseColumns(it.next());
//            if (columns.isEmpty() || !it.hasNext()) {
//                return;
//            }
//            String sql = buildInsert(qualified, columns);
//            try (PreparedStatement ps = conn.prepareStatement(sql)) {
//                while (it.hasNext()) {
//                    NamedCsvRecord row = it.next();
//                    try {
//                        bind(ps, row, columns);
//                        ps.executeUpdate();
//                    } catch (Exception e) {
//                        LOG.log(Level.WARNING, "Row skipped in " + qualified + ": " + e.getMessage());
//                    }
//                }
//            }
//        }
//    }
//
//    private void safeRollback(Connection conn) {
//        try {
//            conn.rollback();
//        } catch (SQLException e) {
//            LOG.log(Level.WARNING, "Rollback failed: " + e.getMessage());
//        }
//    }
//
//    private void safeSetAutoCommit(Connection conn, boolean autoCommit) {
//        try {
//            conn.setAutoCommit(autoCommit);
//        } catch (SQLException e) {
//            LOG.log(Level.WARNING, "Cannot restore auto-commit: " + e.getMessage());
//        }
//    }
//
//    private void clearTable(Path path) {
//        String qualified = qualifiedTableName(path);
//        try (Connection conn = dataSource.getConnection()) {
//            boolean riDisabled = tryDisableReferentialIntegrity(conn);
//            try {
//                deleteRows(conn, qualified);
//            } finally {
//                if (riDisabled) tryEnableReferentialIntegrity(conn);
//            }
//        } catch (SQLException e) {
//            LOG.log(Level.WARNING, "Cannot clear table " + qualified + " on CSV delete: " + e.getMessage());
//        }
//    }
//
//    private boolean tryDisableReferentialIntegrity(Connection conn) {
//        try (Statement st = conn.createStatement()) {
//            st.execute("SET REFERENTIAL_INTEGRITY FALSE");
//            return true;
//        } catch (SQLException ignored) {
//            return false;
//        }
//    }
//
//    private void tryEnableReferentialIntegrity(Connection conn) {
//        try (Statement st = conn.createStatement()) {
//            st.execute("SET REFERENTIAL_INTEGRITY TRUE");
//        } catch (SQLException e) {
//            LOG.log(Level.WARNING, "Failed to re-enable referential integrity: " + e.getMessage());
//        }
//    }
//
//    // ── Helpers ─────────────────────────────────────────────────────────────
//
//    private String qualifiedTableName(Path path) {
//        String table = nameWithoutExtension(path.getFileName().toString());
//        String schema = schemaOf(path);
//        return schema != null ? schema + "." + table : table;
//    }
//
//    private String schemaOf(Path path) {
//        Path parent = path.getParent();
//        if (parent == null || (basePath != null && basePath.equals(parent))) {
//            return null;
//        }
//        return parent.getFileName().toString();
//    }
//
//    private List<Column> parseColumns(NamedCsvRecord typeRow) {
//        List<Column> columns = new ArrayList<>();
//        for (String name : typeRow.getHeader()) {
//            JDBCType type = parseType(typeRow.getField(name));
//            columns.add(new Column(name, type));
//        }
//        return columns;
//    }
//
//    private JDBCType parseType(String spec) {
//        if (spec == null || spec.isBlank()) {
//            return JDBCType.VARCHAR;
//        }
//        String typeName = spec.contains("(") ? spec.substring(0, spec.indexOf('(')).trim() : spec.trim();
//        try {
//            return JDBCType.valueOf(typeName.toUpperCase());
//        } catch (IllegalArgumentException e) {
//            return JDBCType.VARCHAR;
//        }
//    }
//
//    private String buildInsert(String qualifiedTable, List<Column> columns) {
//        String cols = columns.stream().map(Column::name).collect(Collectors.joining(", "));
//        String placeholders = columns.stream().map(c -> "?").collect(Collectors.joining(", "));
//        return "INSERT INTO " + qualifiedTable + " (" + cols + ") VALUES (" + placeholders + ")";
//    }
//
//    private void bind(PreparedStatement ps, NamedCsvRecord row, List<Column> columns) throws SQLException {
//        int idx = 1;
//        for (Column col : columns) {
//            String val = row.getField(col.name());
//            if (val == null || val.equals(config.nullValue())) {
//                ps.setNull(idx++, col.type().getVendorTypeNumber());
//            } else {
//                ps.setObject(idx++, val, col.type().getVendorTypeNumber());
//            }
//        }
//    }
//
//    private static String nameWithoutExtension(String name) {
//        int dot = name.lastIndexOf('.');
//        return dot > 0 ? name.substring(0, dot) : name;
//    }
//
//    private record Column(String name, JDBCType type) {}
//}
