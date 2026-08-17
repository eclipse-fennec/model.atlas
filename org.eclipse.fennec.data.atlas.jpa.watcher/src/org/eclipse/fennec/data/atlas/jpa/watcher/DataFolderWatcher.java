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

import org.eclipse.daanse.io.fs.watcher.api.EventKind;
import org.eclipse.daanse.io.fs.watcher.api.FileSystemWatcherListener;
import org.eclipse.daanse.io.fs.watcher.api.FileSystemWatcherWhiteboardConstants;
import org.eclipse.daanse.io.fs.watcher.api.propertytypes.FileSystemWatcherListenerProperties;
import org.eclipse.daanse.jdbc.datasource.h2.api.Constants;
import org.eclipse.fennec.data.atlas.jpa.watcher.api.WatcherConstants;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
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
 * {@code .eorm} file found in {@code <basePath>/mapping}. If no such file is
 * present when the folder is first registered, the pipeline is not started;
 * it will be started automatically when an {@code .eorm} file is
 * subsequently added to that folder.
 */
@RequireConfigurationAdmin
@Component(name = WatcherConstants.PID_DATA_FOLDER_WATCHER, configurationPolicy = ConfigurationPolicy.REQUIRE)
// Recursive: the mapping this watcher waits for lives in the <basePath>/mapping
// subfolder, and a non-recursive watch only reports entries directly inside the
// watched folder — so the deferred start below could never be triggered by a file
// event, only by the initial scan happening to run late.
@FileSystemWatcherListenerProperties(recursive = true)
public class DataFolderWatcher implements FileSystemWatcherListener {

    private static final Logger LOG = System.getLogger(DataFolderWatcher.class.getName());

	

    @Reference
    private ConfigurationAdmin configAdmin;

    private Path basePath;
    private Path rootFolder;
    
//    private String unitName;
    private String matcherKey;
    private Configuration emfWatcherConfig;
    private Configuration entityMappingsFileWatcherConfig;
    private Configuration csvImporterConfig;
    private Configuration jpaPersistenceUnitConfig;
    private Configuration dataSourceConfig;
    private Configuration ePackageRegistryConfig;
    private Configuration resourceSetFactoryConfig;

    @Deactivate
    void deactivate() {
        deletePipelineConfigs();
    }

    /**
     * Deletes every configuration this watcher created, and forgets them.
     *
     * <p>
     * Used both on deactivation and to roll back a pipeline that could not be built:
     * ConfigAdmin configurations outlive the call that created them, so a half-built
     * pipeline leaves live components bound to configs that nothing will complete — and
     * nothing would delete either, since the fields tracking them are overwritten by the
     * next attempt.
     * </p>
     */
    private void deletePipelineConfigs() {
        deleteConfig(emfWatcherConfig);
        deleteConfig(entityMappingsFileWatcherConfig);
        deleteConfig(csvImporterConfig);
        deleteConfig(jpaPersistenceUnitConfig);
        deleteConfig(dataSourceConfig);
        deleteConfig(ePackageRegistryConfig);
        deleteConfig(resourceSetFactoryConfig);
        emfWatcherConfig = null;
        entityMappingsFileWatcherConfig = null;
        csvImporterConfig = null;
        jpaPersistenceUnitConfig = null;
        dataSourceConfig = null;
        ePackageRegistryConfig = null;
        resourceSetFactoryConfig = null;
    }

    @Override
    public void handleBasePath(Path basePath) {
        this.basePath = basePath;
        this.rootFolder = basePath.getFileName();
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
        matcherKey = matcherKeyFor(basePath);
        String mappingPath = basePath.resolve("mapping").toAbsolutePath().toString();
        String dataPath    = basePath.resolve("data").toAbsolutePath().toString();

        try {
        	dataSourceConfig = configAdmin.getFactoryConfiguration(WatcherConstants.PID_H2_DATA_SOURCE, matcherKey, "?");
    		Dictionary<String, Object> properties = new Hashtable<>();
    		properties.put(Constants.DATASOURCE_PROPERTY_IDENTIFIER, "./generated/tmp/databases/" + matcherKey);
    		properties.put(WatcherConstants.KEY_JPA_ROOT_FOLDER, rootFolder.toString());
    		properties.put(Constants.DATASOURCE_PROPERTY_PLUGABLE_FILESYSTEM, Constants.OPTION_PLUGABLE_FILESYSTEM_FILE);
    		properties.put(Constants.DATASOURCE_PROPERTY_DATABASE_TO_UPPER, false);
    		properties.put(WatcherConstants.KEY_FILE_CONTEXT_MATCHER, matcherKey);
    		dataSourceConfig.update(properties);
    		
    		ePackageRegistryConfig = createEPackageRegistryConfig();
    		resourceSetFactoryConfig = createResourceSetFactoryConfig();
        	
            emfWatcherConfig = configAdmin.getFactoryConfiguration(WatcherConstants.PID_EMF_FILE_WATCHER, matcherKey, "?");
            properties = new Hashtable<>();
            properties.put(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATH, mappingPath);
            properties.put(WatcherConstants.KEY_FILE_CONTEXT_MATCHER, matcherKey);
            properties.put("resourceSet.target", "(" + WatcherConstants.KEY_FILE_CONTEXT_MATCHER + "=" + matcherKey + ")");
            properties.put(WatcherConstants.KEY_JPA_ROOT_FOLDER, rootFolder.toString());
//            properties.put("ePackageRegistry.target", "(" + EMFNamespaces.PROP_RESOURCE_SET_FACTORY_NAME + "=" + matcherKey + ")");
            emfWatcherConfig.update(properties);
            
            entityMappingsFileWatcherConfig = configAdmin.getFactoryConfiguration(WatcherConstants.PID_ENTITY_MAPPINGS_FILE_WATCHER, matcherKey, "?");
            properties = new Hashtable<>();
            properties.put(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATH, mappingPath);
            properties.put(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATTERN, ".*\\.eorm");
            properties.put(WatcherConstants.KEY_FILE_CONTEXT_MATCHER, matcherKey);
            properties.put(WatcherConstants.KEY_JPA_ROOT_FOLDER, rootFolder.toString());
            entityMappingsFileWatcherConfig.update(properties);

            csvImporterConfig = configAdmin.getFactoryConfiguration(WatcherConstants.PID_CSV_IMPORTER, matcherKey, "?");
            properties = new Hashtable<>();
            properties.put(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATH, dataPath);
            // The CSV importer component declares kinds=ENTRY_MODIFY by default; override here
            // so CSV file creation and deletion also reach its handler.
            properties.put(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_KINDS,
                    new String[] { EventKind.ENTRY_CREATE.name(), EventKind.ENTRY_DELETE.name(), EventKind.ENTRY_MODIFY.name() });
            properties.put(WatcherConstants.KEY_FILE_CONTEXT_MATCHER, matcherKey);
            properties.put(WatcherConstants.PROP_DATASOURCE_TARGET, "(" + WatcherConstants.KEY_JPA_ROOT_FOLDER + "=" + rootFolder.toString() + ")");
            properties.put(WatcherConstants.PROP_ENTITY_MANAGER_FACTORY_TARGET, "(osgi.unit.name=" + rootFolder.toString() + ")");
            properties.put(WatcherConstants.KEY_JPA_ROOT_FOLDER, rootFolder.toString());
            csvImporterConfig.update(properties);                

            jpaPersistenceUnitConfig = configAdmin.getFactoryConfiguration(WatcherConstants.PID_PERSISTENCE_UNIT, matcherKey, "?");
            properties = new Hashtable<>();
            properties.put("fennec.jpa.persistenceUnitName", rootFolder.toString());
            properties.put("fennec.jpa.dataSource.target", "(" + WatcherConstants.KEY_JPA_ROOT_FOLDER + "=" + rootFolder.toString() + ")");
            properties.put("fennec.jpa.mapping.target", "(" + WatcherConstants.PROP_EORM_MAPPING_NAME + "=" + rootFolder.toString() + ")");
            properties.put(WatcherConstants.KEY_JPA_ROOT_FOLDER, rootFolder.toString());
            jpaPersistenceUnitConfig.update(properties);

            LOG.log(Level.INFO, "DataFolderWatcher activated for unit ''{0}'' at {1}", matcherKey, basePath.toAbsolutePath().toString());
        } catch (IOException e) {
            LOG.log(Level.ERROR, "Failed to create sub-component configs for folder "
                    + basePath.toAbsolutePath().toString() + " — rolling back the partial pipeline", e);
            deletePipelineConfigs();
        }
    }

    /**
     * A key identifying this folder's pipeline, stable across attempts.
     *
     * <p>
     * It used to be a fresh {@link UUID} per {@code setupPipeline()} call. A second
     * attempt — after a failed one — therefore addressed a whole new set of ConfigAdmin
     * configurations, leaving the first set behind with nothing tracking it. Deriving the
     * key from the folder means a retry updates the same configurations instead of
     * orphaning them. It is also filter- and filename-safe, which the raw path is not:
     * the key ends up in LDAP target filters and in the H2 database directory name.
     * </p>
     */
    private static String matcherKeyFor(Path basePath) {
        Path absolute = basePath.toAbsolutePath().normalize();
        String name = absolute.getFileName() == null ? "unit" : absolute.getFileName().toString();
        return name.replaceAll("[^A-Za-z0-9._-]", "_") + "-"
                + Integer.toHexString(absolute.toString().hashCode());
    }
    
    private Configuration createEPackageRegistryConfig() throws IOException {
        Configuration config = configAdmin.getFactoryConfiguration(EMFNamespaces.EPACKAGE_REGISTRY_CONFIG_NAME,
                matcherKey, "?");
        Hashtable<String, Object> props = new Hashtable<>();
        props.put(EMFNamespaces.PROP_RESOURCE_SET_FACTORY_NAME, matcherKey);
        props.put(EMFNamespaces.EPACKAGE_TARGET, "(" + WatcherConstants.KEY_JPA_ROOT_FOLDER + "=" + rootFolder.toString() + ")");
        props.put(WatcherConstants.KEY_JPA_ROOT_FOLDER, rootFolder.toString());
        config.update(props);
        return config;
    }

    private Configuration createResourceSetFactoryConfig() throws IOException {
        Configuration config = configAdmin.getFactoryConfiguration(EMFNamespaces.RESOURCE_SET_FACTORY_CONFIG_NAME,
                matcherKey, "?");
        Hashtable<String, Object> props = new Hashtable<>();
        props.put(EMFNamespaces.EPACKAGE_REGISTRY_TARGET, "(" + EMFNamespaces.PROP_RESOURCE_SET_FACTORY_NAME + "=" + matcherKey + ")");
        props.put(WatcherConstants.KEY_FILE_CONTEXT_MATCHER, matcherKey);
        props.put(WatcherConstants.KEY_JPA_ROOT_FOLDER, rootFolder.toString());
        config.update(props);
        return config;
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
        	LOG.log(Level.DEBUG, "Deleted config " + config.getPid());
            config.delete();
            
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to delete sub-component configuration", e);
        }
    }
}
