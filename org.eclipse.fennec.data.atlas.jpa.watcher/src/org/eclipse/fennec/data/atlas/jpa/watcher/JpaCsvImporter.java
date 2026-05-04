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
package org.eclipse.fennec.data.atlas.jpa.watcher;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.eclipse.daanse.io.fs.watcher.api.EventKind;
import org.eclipse.daanse.io.fs.watcher.api.FileSystemWatcherListener;
import org.eclipse.daanse.io.fs.watcher.api.propertytypes.FileSystemWatcherListenerProperties;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.ColumnMapping;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import de.siegmar.fastcsv.reader.CloseableIterator;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRecord;

/**
 * Watches CSV files in the configured folder and imports each row into the
 * database directly via JDBC, using the {@link JpaMappingConfig} to resolve
 * which table/schema each file belongs to and how feature names map to column
 * names.
 *
 * <p>CSV files must use EClass feature names as column headers. Row 2 (the SQL
 * types row used by the daanse CSV format) is skipped — types are read from the
 * {@link ColumnMapping} instead.
 *
 * <p>The component creates the target schema and table if they do not already
 * exist, then replaces all rows on every file event.
 */
@Designate(factory = true, ocd = JpaCsvImporter.Config.class)
@Component(name = JpaCsvImporter.PID, configurationPolicy = ConfigurationPolicy.REQUIRE,
        scope = ServiceScope.SINGLETON, service = FileSystemWatcherListener.class)
@FileSystemWatcherListenerProperties(kinds = EventKind.ENTRY_MODIFY, pattern = ".*.csv", recursive = true)
public class JpaCsvImporter implements FileSystemWatcherListener {

    public static final String PID = "JpaCsvImporter";

    private static final Logger LOG = System.getLogger(JpaCsvImporter.class.getName());

    private static final int BATCH_SIZE = 500;

    @ObjectClassDefinition
    public @interface Config {
        @AttributeDefinition(name = "Unit name", description = "Persistence unit name — must match JpaMappingConfig name")
        String unitName();
    }

    private DataSource dataSource;
    private JpaMappingConfig jpaMappingConfig;

    private Path basePath;

    @Activate
    public JpaCsvImporter(Config config, @Reference(name = "dataSource", target = "(scope=no-inject)") DataSource dataSource,
    		 @Reference(name = "jpaMappingConfig", target = "(scope=no-inject)") JpaMappingConfig jpaMappingConfig) {
        this.dataSource = dataSource;
        this.jpaMappingConfig = jpaMappingConfig;
        
    }

    @Override
    public void handleBasePath(Path basePath) {
        this.basePath = basePath;
    }

    @Override
    public void handleInitialPaths(List<Path> initialPaths) {
        initialPaths.forEach(this::importCsv);
    }

    @Override
    public void handlePathEvent(Path path, Kind<Path> kind) {
        importCsv(path);
    }

    private void importCsv(Path path) {
        if (Files.isDirectory(path) || !path.toString().endsWith(".csv")) {
            return;
        }

        String fileName = fileNameWithoutExtension(path);
        String schema = schemaFromPath(path).orElse(null);

        TableMapping tableMapping = findTableMapping(fileName, schema);
        if (tableMapping == null) {
            LOG.log(Level.WARNING, "No TableMapping found for file ''{0}'' (schema={1}) — skipping",
                    fileName, schema);
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            ensureSchema(conn, tableMapping.getSchema());
            ensureTable(conn, tableMapping);
            deleteExisting(conn, tableMapping);
            int count = insertRows(conn, path, tableMapping);
            conn.commit();
            LOG.log(Level.INFO, "Imported {0} rows into {1} from {2}",
                    count, qualifiedTableName(tableMapping), path);
        } catch (Exception e) {
            LOG.log(Level.ERROR, "Failed to import CSV " + path, e);
        }
    }

    private void ensureSchema(Connection conn, String schema) throws SQLException {
        if (schema == null || schema.isBlank()) {
            return;
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schema.toUpperCase());
        }
    }

    private void ensureTable(Connection conn, TableMapping tm) throws SQLException {
        List<ColumnMapping> cols = tm.getColumnMappings();
        StringJoiner columnDefs = new StringJoiner(", ");
        for (ColumnMapping col : cols) {
            StringBuilder def = new StringBuilder(col.getColumnName()).append(' ').append(col.getColumnType());
            if (!col.isNullable()) {
                def.append(" NOT NULL");
            }
            columnDefs.add(def.toString());
        }
        String pkCols = cols.stream()
                .filter(ColumnMapping::isPrimaryKey)
                .map(ColumnMapping::getColumnName)
                .collect(Collectors.joining(", "));
        if (!pkCols.isEmpty()) {
            columnDefs.add("PRIMARY KEY (" + pkCols + ")");
        }
        String sql = "CREATE TABLE IF NOT EXISTS " + qualifiedTableName(tm) + " (" + columnDefs + ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void deleteExisting(Connection conn, TableMapping tm) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM " + qualifiedTableName(tm));
        }
    }

    private int insertRows(Connection conn, Path path, TableMapping tm) throws IOException, SQLException {
        List<ColumnMapping> cols = tm.getColumnMappings();
        String sql = buildInsertSql(tm, cols);
        int count = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             CloseableIterator<NamedCsvRecord> it = CsvReader.builder()
                     .skipEmptyLines(true)
                     .ofNamedCsvRecord(path)
                     .iterator()) {
            if (it.hasNext()) {
                it.next(); // row 2 is the SQL-types row (daanse CSV format) — types come from JpaMappingConfig
            }
            while (it.hasNext()) {
                NamedCsvRecord record = it.next();
                for (int i = 0; i < cols.size(); i++) {
                    ColumnMapping col = cols.get(i);
                    String raw = record.getField(col.getFeatureName());
                    setParameter(ps, i + 1, raw, col.getColumnType());
                }
                ps.addBatch();
                count++;
                if (count % BATCH_SIZE == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();
        }
        return count;
    }

    private String buildInsertSql(TableMapping tm, List<ColumnMapping> cols) {
        String columnNames = cols.stream().map(ColumnMapping::getColumnName).collect(Collectors.joining(", "));
        String placeholders = cols.stream().map(c -> "?").collect(Collectors.joining(", "));
        return "INSERT INTO " + qualifiedTableName(tm) + " (" + columnNames + ") VALUES (" + placeholders + ")";
    }

    private void setParameter(PreparedStatement ps, int index, String value, String columnType) throws SQLException {
        if (value == null || value.isBlank()) {
            ps.setNull(index, Types.VARCHAR);
            return;
        }
        String baseType = columnType.toUpperCase();
        int paren = baseType.indexOf('(');
        if (paren > 0) {
            baseType = baseType.substring(0, paren).trim();
        }
        try {
            switch (baseType) {
                case "BIGINT"            -> ps.setLong(index, Long.parseLong(value.trim()));
                case "INTEGER", "INT"    -> ps.setInt(index, Integer.parseInt(value.trim()));
                case "SMALLINT"          -> ps.setShort(index, Short.parseShort(value.trim()));
                case "DECIMAL", "NUMERIC"-> ps.setBigDecimal(index, new BigDecimal(value.trim()));
                case "DOUBLE", "FLOAT",
                     "REAL"              -> ps.setDouble(index, Double.parseDouble(value.trim()));
                case "BOOLEAN"           -> ps.setBoolean(index, Boolean.parseBoolean(value.trim()));
                default                  -> ps.setString(index, value);
            }
        } catch (NumberFormatException e) {
            LOG.log(Level.WARNING, "Cannot convert ''{0}'' for type {1} — storing as NULL", value, columnType);
            ps.setNull(index, Types.VARCHAR);
        }
    }

    private String qualifiedTableName(TableMapping tm) {
        String schema = tm.getSchema();
        String table = tm.getTableName().toUpperCase();
        if (schema != null && !schema.isBlank()) {
            return schema.toUpperCase() + "." + table;
        }
        return table;
    }

    private TableMapping findTableMapping(String fileName, String schema) {
        return jpaMappingConfig.getTableMappings().stream()
                .filter(tm -> {
                    String eClassName = tm.getClassName().substring(tm.getClassName().lastIndexOf('/') + 1);
                    return fileName.equalsIgnoreCase(tm.getTableName())
                            || fileName.equalsIgnoreCase(eClassName)
                            || fileName.equalsIgnoreCase(eClassName + "s");
                })
                .filter(tm -> {
                    if (schema == null) {
                        return tm.getSchema() == null || tm.getSchema().isBlank();
                    }
                    return schema.equalsIgnoreCase(tm.getSchema());
                })
                .findFirst()
                .orElse(null);
    }

    private Optional<String> schemaFromPath(Path path) {
        Path parent = path.getParent();
        if (basePath == null || basePath.equals(parent)) {
            return Optional.empty();
        }
        return Optional.of(parent.getFileName().toString());
    }

    private String fileNameWithoutExtension(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}
