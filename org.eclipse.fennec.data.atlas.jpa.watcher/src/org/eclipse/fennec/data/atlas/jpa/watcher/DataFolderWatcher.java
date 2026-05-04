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
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import org.eclipse.daanse.io.fs.watcher.api.FileSystemWatcherListener;
import org.eclipse.daanse.io.fs.watcher.api.FileSystemWatcherWhiteboardConstants;
import org.eclipse.daanse.io.fs.watcher.api.propertytypes.FileSystemWatcherListenerProperties;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Watches a data folder and bootstraps the full JPA pipeline for that unit:
 * <ol>
 *   <li>{@code EMFFileWatcher} — loads the .ecore model and registers the EPackage</li>
 *   <li>{@code JpaMappingFileWatcher} — loads the .jpamapping and creates the DataSource</li>
 *   <li>{@code JpaModelSetup} — builds the EntityMappings and starts the EclipseLink persistence unit</li>
 *   <li>{@code JpaCsvImporter} — activates only once the EntityManagerFactory is ready, then
 *       reads CSV files (using EClass feature names as column headers) and persists each row
 *       via JPA so EclipseLink applies the jpamapping column-name translation automatically</li>
 * </ol>
 *
 * <p>The unit name is derived from the last segment of the watched folder path and must match
 * the {@code name} attribute in the .jpamapping file.
 */
@RequireConfigurationAdmin
@Component(name = DataFolderWatcher.PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
@FileSystemWatcherListenerProperties(recursive = false)
public class DataFolderWatcher implements FileSystemWatcherListener {

    private static final Logger LOG = System.getLogger(DataFolderWatcher.class.getName());

    public static final String PID = "DataFolderWatcher";

    // PIDs of the sub-components we configure dynamically
    private static final String EMF_FILE_WATCHER_PID = "EMFFileWatcher";
    private static final String JPA_MAPPING_FILE_WATCHER_PID = "JpaMappingFileWatcher";

    @Reference
    private ConfigurationAdmin configAdmin;

    private String unitName;
    private String matcherKey;
    private Configuration emfWatcherConfig;
    private Configuration jpaMappingWatcherConfig;
    private Configuration jpaCsvImporterConfig;
    private Configuration jpaModelSetupConfig;

    @Deactivate
    void deactivate() {
        deleteConfig(emfWatcherConfig);
        deleteConfig(jpaMappingWatcherConfig);
        deleteConfig(jpaCsvImporterConfig);
        deleteConfig(jpaModelSetupConfig);
        emfWatcherConfig = null;
        jpaMappingWatcherConfig = null;
        jpaCsvImporterConfig = null;
        jpaModelSetupConfig = null;
    }

    @Override
    public void handleBasePath(Path basePath) {
        unitName = basePath.getFileName().toString();
        matcherKey = UUID.randomUUID().toString();
        String pathStr = basePath.toAbsolutePath().toString();
        try {
            emfWatcherConfig = configAdmin.getFactoryConfiguration(EMF_FILE_WATCHER_PID, matcherKey, "?");
            Dictionary<String, Object> emfProps = new Hashtable<>();
            emfProps.put(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATH, pathStr);
            emfWatcherConfig.update(emfProps);

            jpaMappingWatcherConfig = configAdmin.getFactoryConfiguration(JPA_MAPPING_FILE_WATCHER_PID, matcherKey, "?");
            Dictionary<String, Object> jpaProps = new Hashtable<>();
            jpaProps.put(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATH, pathStr);
            jpaProps.put("unitName", unitName);
            jpaMappingWatcherConfig.update(jpaProps);

            jpaCsvImporterConfig = configAdmin.getFactoryConfiguration(JpaCsvImporter.PID, matcherKey, "?");
            Dictionary<String, Object> csvProps = new Hashtable<>();
            csvProps.put(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATH, pathStr);
            csvProps.put("dataSource.target", "(unitName=" + unitName + ")");
            csvProps.put("jpaMappingConfig.target", "(unitName=" + unitName + ")");
            csvProps.put("unitName", unitName);
            jpaCsvImporterConfig.update(csvProps);

            jpaModelSetupConfig = configAdmin.getFactoryConfiguration(JpaModelSetup.PID, matcherKey, "?");
            Dictionary<String, Object> setupProps = new Hashtable<>();
            setupProps.put("unitName", unitName);
            setupProps.put("jpaMappingConfig.target", "(unitName=" + unitName + ")");
            setupProps.put("dataSource.target", "(unitName=" + unitName + ")");
            jpaModelSetupConfig.update(setupProps);

            LOG.log(Level.INFO, "DataFolderWatcher activated for unit ''{0}'' at {1}", unitName, pathStr);
        } catch (IOException e) {
            LOG.log(Level.ERROR, "Failed to create sub-component configs for folder " + basePath, e);
        }
    }

    @Override
    public void handleInitialPaths(List<Path> paths) {
        // Sub-components handle their own initial scan within the configured folder.
    }

    @Override
    public void handlePathEvent(Path path, Kind<Path> kind) {
        // Sub-components handle their own file events.
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
