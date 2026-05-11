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
//import java.util.Dictionary;
//import java.util.Hashtable;
//import java.util.List;
//import java.util.Objects;
//import java.util.UUID;
//
//import javax.xml.parsers.DocumentBuilderFactory;
//
//import org.eclipse.daanse.io.fs.watcher.api.FileSystemWatcherListener;
//import org.eclipse.daanse.io.fs.watcher.api.FileSystemWatcherWhiteboardConstants;
//import org.eclipse.daanse.io.fs.watcher.api.propertytypes.FileSystemWatcherListenerProperties;
//import org.osgi.service.cm.Configuration;
//import org.osgi.service.cm.ConfigurationAdmin;
//import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
//import org.osgi.service.component.annotations.Component;
//import org.osgi.service.component.annotations.ConfigurationPolicy;
//import org.osgi.service.component.annotations.Deactivate;
//import org.osgi.service.component.annotations.Reference;
//
///**
// * Watches a data folder and bootstraps the full pipeline for that unit:
// * an {@code EMFFileWatcher} to register .ecore models, a
// * {@code JpaMappingFileWatcher} to register .jpamapping configs (which in
// * turn trigger H2 DataSource creation via {@code DataSourceConfigHandler}),
// * and a CSV importer to load .csv data into that DataSource.
// *
// * <p>All sub-component configs are created in {@link #setupPipeline} and
// * deleted on {@link #deactivate}.
// *
// * <p>The unit name is read from the {@code name} attribute of the
// * {@code .jpamapping} file found in the watched folder. If no such file is
// * present when the folder is first registered, the pipeline is not started;
// * it will be started automatically when a {@code .jpamapping} file is
// * subsequently added to the folder.
// */
//@RequireConfigurationAdmin
//@Component(name = DataFolderWatcherOld.PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
//@FileSystemWatcherListenerProperties(recursive = false)
//public class DataFolderWatcherOld implements FileSystemWatcherListener {
//
//    private static final Logger LOG = System.getLogger(DataFolderWatcherOld.class.getName());
//
//    public static final String PID = "DataFolderWatcher";
//
//    // PIDs of the sub-components we configure dynamically
//    private static final String EMF_FILE_WATCHER_PID = "EMFFileWatcher";
//    private static final String JPA_MAPPING_FILE_WATCHER_PID = "JpaMappingFileWatcher";
//    private static final String CSV_IMPORTER_PID = "org.eclipse.daanse.jdbc.db.importer.csv.CsvDataImporter";
//
//    private static final String PROP_DATASOURCE_TARGET = "dataSource.target";
//
//    @Reference
//    private ConfigurationAdmin configAdmin;
//
//    private Path basePath;
//    private String unitName;
//    private String matcherKey;
//    private Configuration emfWatcherConfig;
//    private Configuration jpaMappingWatcherConfig;
//    private Configuration csvImporterConfig;
//    private Configuration jpaModelSetupConfig;
//
//    @Deactivate
//    void deactivate() {
//        deleteConfig(emfWatcherConfig);
//        deleteConfig(jpaMappingWatcherConfig);
//        deleteConfig(csvImporterConfig);
//        deleteConfig(jpaModelSetupConfig);
//        emfWatcherConfig = null;
//        jpaMappingWatcherConfig = null;
//        csvImporterConfig = null;
//        jpaModelSetupConfig = null;
//    }
//
//    @Override
//    public void handleBasePath(Path basePath) {
//        this.basePath = basePath;
//        String name = readUnitNameFromFolder(basePath);
//        if (name == null) {
//            LOG.log(Level.INFO, "No .jpamapping file in {0} — pipeline will start when one is added", basePath);
//            return;
//        }
//        setupPipeline(name);
//    }
//
//    @Override
//    public void handleInitialPaths(List<Path> paths) {
//        // Sub-components handle their own initial scan within the configured folder.
//    }
//
//    @Override
//    public void handlePathEvent(Path path, Kind<Path> kind) {
//        if (!path.toString().endsWith(".jpamapping") || emfWatcherConfig != null) {
//            return;
//        }
//        if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind) || StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)) {
//            String name = readNameAttribute(path);
//            if (name != null && !name.isBlank()) {
//                setupPipeline(name);
//            }
//        }
//    }
//
//    private void setupPipeline(String name) {
//        unitName = name;
//        matcherKey = UUID.randomUUID().toString();
//        String pathStr = basePath.toAbsolutePath().toString();
//        try {
//            emfWatcherConfig = configAdmin.getFactoryConfiguration(EMF_FILE_WATCHER_PID, matcherKey, "?");
//            Dictionary<String, Object> emfProps = new Hashtable<>();
//            emfProps.put(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATH, pathStr);
//            emfWatcherConfig.update(emfProps);
//
//            jpaMappingWatcherConfig = configAdmin.getFactoryConfiguration(JPA_MAPPING_FILE_WATCHER_PID, matcherKey, "?");
//            Dictionary<String, Object> jpaProps = new Hashtable<>();
//            jpaProps.put(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATH, pathStr);
//            jpaProps.put("unitName", unitName);
//            jpaMappingWatcherConfig.update(jpaProps);
//
//            csvImporterConfig = configAdmin.getFactoryConfiguration(CSV_IMPORTER_PID, matcherKey, "?");
//            Dictionary<String, Object> csvProps = new Hashtable<>();
//            csvProps.put(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATH, pathStr);
//            csvProps.put(PROP_DATASOURCE_TARGET, "(unitName=" + unitName + ")");
//            csvImporterConfig.update(csvProps);
//
//            jpaModelSetupConfig = configAdmin.getFactoryConfiguration(JpaModelSetup.PID, matcherKey, "?");
//            Dictionary<String, Object> setupProps = new Hashtable<>();
//            setupProps.put("unitName", unitName);
//            setupProps.put("jpaMappingConfig.target", "(unitName=" + unitName + ")");
//            jpaModelSetupConfig.update(setupProps);
//
//            LOG.log(Level.INFO, "DataFolderWatcher activated for unit ''{0}'' at {1}", unitName, pathStr);
//        } catch (IOException e) {
//            LOG.log(Level.ERROR, "Failed to create sub-component configs for folder " + basePath, e);
//        }
//    }
//
//    private String readUnitNameFromFolder(Path folder) {
//        try (var stream = Files.list(folder)) {
//            return stream
//                    .filter(p -> p.toString().endsWith(".jpamapping"))
//                    .map(this::readNameAttribute)
//                    .filter(Objects::nonNull)
//                    .filter(n -> !n.isBlank())
//                    .findFirst()
//                    .orElse(null);
//        } catch (IOException e) {
//            LOG.log(Level.WARNING, "Failed to scan folder {0} for .jpamapping file", folder);
//            return null;
//        }
//    }
//
//    private String readNameAttribute(Path file) {
//        try {
//            return DocumentBuilderFactory.newInstance()
//                    .newDocumentBuilder()
//                    .parse(file.toFile())
//                    .getDocumentElement()
//                    .getAttribute("name");
//        } catch (Exception e) {
//            LOG.log(Level.WARNING, "Failed to read name attribute from {0}: {1}", file, e.getMessage());
//            return null;
//        }
//    }
//
//    private void deleteConfig(Configuration config) {
//        if (config == null) {
//            return;
//        }
//        try {
//            config.delete();
//        } catch (IOException e) {
//            LOG.log(Level.WARNING, "Failed to delete sub-component configuration", e);
//        }
//    }
//}
