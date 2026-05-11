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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import org.eclipse.daanse.io.fs.watcher.api.FileSystemWatcherListener;
import org.eclipse.daanse.io.fs.watcher.api.FileSystemWatcherWhiteboardConstants;
import org.eclipse.daanse.io.fs.watcher.api.propertytypes.FileSystemWatcherListenerProperties;
import org.eclipse.daanse.jdbc.datasource.h2.api.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Watches a data folder and bootstraps the full pipeline for that unit:
 * an {@code EMFFileWatcher} to register .ecore models, a
 * {@code JpaMappingFileWatcher} to register .jpamapping configs (which in
 * turn trigger H2 DataSource creation via {@code DataSourceConfigHandler}),
 * and a CSV importer to load .csv data into that DataSource.
 *
 * <p>All sub-component configs are created in {@link #setupPipeline} and
 * deleted on {@link #deactivate}.
 *
 * <p>The unit name is read from the {@code name} attribute of the
 * {@code .jpamapping} file found in the watched folder. If no such file is
 * present when the folder is first registered, the pipeline is not started;
 * it will be started automatically when a {@code .jpamapping} file is
 * subsequently added to the folder.
 */
@RequireConfigurationAdmin
@Component(name = DataFolderWatcher.PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
@FileSystemWatcherListenerProperties(recursive = false)
public class DataFolderWatcher implements FileSystemWatcherListener {

    private static final Logger LOG = System.getLogger(DataFolderWatcher.class.getName());

    public static final String PID = "DataFolderWatcher";
	

    @Reference
    private ConfigurationAdmin configAdmin;

    private Path basePath;
//    private String unitName;
    private String matcherKey;
    private Configuration emfWatcherConfig;
    private Configuration entityMappingsFileWatcherConfig;
    private Configuration csvImporterConfig;
    private Configuration jpaPersistenceUnitConfig;
    private Configuration dataSourceConfig;

    @Deactivate
    void deactivate() {
        deleteConfig(emfWatcherConfig);
        deleteConfig(entityMappingsFileWatcherConfig);
        deleteConfig(csvImporterConfig);
        deleteConfig(jpaPersistenceUnitConfig);
        deleteConfig(dataSourceConfig);
        emfWatcherConfig = null;
        entityMappingsFileWatcherConfig = null;
        csvImporterConfig = null;
        jpaPersistenceUnitConfig = null;
        dataSourceConfig = null;
    }

    @Override
    public void handleBasePath(Path basePath) {
        this.basePath = basePath;
        Path eormFile = ensureEormFile(basePath.resolve("mapping"));
        if (eormFile == null) {
            LOG.log(Level.INFO, "No .eorm file in {0}/mapping — pipeline will start when one is added", basePath);
            return;
        }
        setupPipeline();
    }

    @Override
    public void handleInitialPaths(List<Path> paths) {
        // Sub-components handle their own initial scan within the configured folder.
    }

    @Override
    public void handlePathEvent(Path path, Kind<Path> kind) {
        if (!path.toString().endsWith(".eorm") || emfWatcherConfig != null) {
            return;
        }
        if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind) || StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)) {
        	Path eormFile = ensureEormFile(basePath.resolve("mapping"));
            if (eormFile == null) {
                LOG.log(Level.INFO, "No .eorm file in {0}/mapping — pipeline will start when one is added", basePath);
                return;
            }
            setupPipeline();
        }
    }

    private void setupPipeline() {
        matcherKey = UUID.randomUUID().toString();
        String mappingPath = basePath.resolve("mapping").toAbsolutePath().toString();
        String dataPath    = basePath.resolve("data").toAbsolutePath().toString();

        try {
        	dataSourceConfig = configAdmin.getFactoryConfiguration(WatcherConstants.PID_H2_DATA_SOURCE, matcherKey, "?");
    		Dictionary<String, Object> properties = new Hashtable<>();
    		properties.put(Constants.DATASOURCE_PROPERTY_IDENTIFIER, "./generated/tmp/databases/" + matcherKey);
    		properties.put(Constants.DATASOURCE_PROPERTY_PLUGABLE_FILESYSTEM, Constants.OPTION_PLUGABLE_FILESYSTEM_FILE);
    		properties.put(Constants.DATASOURCE_PROPERTY_DATABASE_TO_UPPER, false);
    		properties.put(WatcherConstants.KEY_FILE_CONTEXT_MATCHER, matcherKey);
    		dataSourceConfig.update(properties);
        	
            emfWatcherConfig = configAdmin.getFactoryConfiguration(WatcherConstants.PID_EMF_FILE_WATCHER, matcherKey, "?");
            properties = new Hashtable<>();
            properties.put(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATH, mappingPath);
            properties.put(WatcherConstants.KEY_FILE_CONTEXT_MATCHER, matcherKey);
            emfWatcherConfig.update(properties);
            
            entityMappingsFileWatcherConfig = configAdmin.getFactoryConfiguration(WatcherConstants.PID_ENTITY_MAPPINGS_FILE_WATCHER, matcherKey, "?");
            properties = new Hashtable<>();
            properties.put(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATH, mappingPath);
            properties.put(WatcherConstants.KEY_FILE_CONTEXT_MATCHER, matcherKey);
            entityMappingsFileWatcherConfig.update(properties);

            csvImporterConfig = configAdmin.getFactoryConfiguration(WatcherConstants.PID_CSV_IMPORTER, matcherKey, "?");
            properties = new Hashtable<>();
            properties.put(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATH, dataPath);
            properties.put(WatcherConstants.KEY_FILE_CONTEXT_MATCHER, matcherKey);
            properties.put(WatcherConstants.PROP_DATASOURCE_TARGET, "(" + WatcherConstants.KEY_FILE_CONTEXT_MATCHER + "=" + matcherKey + ")");
            properties.put(WatcherConstants.PROP_ENTITY_MANAGER_FACTORY_TARGET, "(osgi.unit.name=" + matcherKey + ")");
            csvImporterConfig.update(properties);                

            jpaPersistenceUnitConfig = configAdmin.getFactoryConfiguration(WatcherConstants.PID_PERSISTENCE_UNIT, matcherKey, "?");
            properties = new Hashtable<>();
            properties.put("fennec.jpa.persistenceUnitName", matcherKey);
            properties.put("fennec.jpa.dataSource.target", "(" + WatcherConstants.KEY_FILE_CONTEXT_MATCHER + "=" + matcherKey + ")");
            properties.put("fennec.jpa.mapping.target", "(" + WatcherConstants.PROP_EORM_MAPPING_NAME + "=" + matcherKey + ")");
            jpaPersistenceUnitConfig.update(properties);

            LOG.log(Level.INFO, "DataFolderWatcher activated for unit ''{0}'' at {1}", matcherKey, basePath.toAbsolutePath().toString());
        } catch (IOException e) {
            LOG.log(Level.ERROR, "Failed to create sub-component configs for folder " + basePath.toAbsolutePath().toString(), e);
        }
    }

    private Path ensureEormFile(Path folder) {
        try (var stream = Files.list(folder)) {
            return stream
                    .filter(p -> p.toString().endsWith(".eorm"))
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to scan folder {0} for .eorm file", folder);
            return null;
        }
    }



    private void deleteConfig(Configuration config) {
        if (config == null) {
            return;
        }
        try {
            config.delete();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to delete sub-component configuration", e);
        }
    }
}
