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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.daanse.io.fs.watcher.api.EventKind;
import org.eclipse.daanse.io.fs.watcher.api.FileSystemWatcherListener;
import org.eclipse.daanse.io.fs.watcher.api.FileSystemWatcherWhiteboardConstants;
import org.eclipse.daanse.io.fs.watcher.api.propertytypes.FileSystemWatcherListenerProperties;
import org.eclipse.fennec.data.atlas.jpa.watcher.api.WatcherConstants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Watches a workspace root and registers/unregisters a {@link DataFolderWatcher}
 * factory configuration for every direct sub-directory it discovers.
 *
 * <p>This removes the need to pre-configure each individual data folder: the
 * user configures a single {@code WorkspaceFolderWatcher} pointing at a parent
 * directory, and any folder created (or already present) underneath is
 * automatically turned into a full pipeline. When a sub-directory is deleted,
 * the corresponding {@code DataFolderWatcher} configuration is removed and its
 * pipeline is torn down.
 *
 * <p>Non-directory entries in the workspace root are ignored.
 */
@RequireConfigurationAdmin
@Component(name = WatcherConstants.PID_WORKSPACE_FOLDER_WATCHER, configurationPolicy = ConfigurationPolicy.REQUIRE)
@FileSystemWatcherListenerProperties(recursive = false, kinds = { EventKind.ENTRY_CREATE, EventKind.ENTRY_DELETE })
public class WorkspaceFolderWatcher implements FileSystemWatcherListener {

    private static final Logger LOG = System.getLogger(WorkspaceFolderWatcher.class.getName());

    @Reference
    private ConfigurationAdmin configAdmin;

    private final Map<Path, Configuration> dataFolderConfigs = new ConcurrentHashMap<>();

    @Deactivate
    void deactivate() {
        dataFolderConfigs.values().forEach(this::deleteConfig);
        dataFolderConfigs.clear();
    }

    @Override
    public void handleBasePath(Path basePath) {
        LOG.log(Level.INFO, "WorkspaceFolderWatcher activated for {0}", basePath.toAbsolutePath());
    }

    @Override
    public void handleInitialPaths(List<Path> paths) {
        for (Path p : paths) {
            if (Files.isDirectory(p)) {
                registerDataFolder(p);
            }
        }
    }

    @Override
    public void handlePathEvent(Path path, Kind<Path> kind) {
        if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
            if (Files.isDirectory(path)) {
                registerDataFolder(path);
            }
        } else if (StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
            unregisterDataFolder(path);
        }
    }

    private void registerDataFolder(Path folder) {
        Path key = folder.toAbsolutePath();
        if (dataFolderConfigs.containsKey(key)) {
            return;
        }
        try {
            Configuration cfg = configAdmin.getFactoryConfiguration(
                    WatcherConstants.PID_DATA_FOLDER_WATCHER, UUID.randomUUID().toString(), "?");
            Dictionary<String, Object> props = new Hashtable<>();
            props.put(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATH, key.toString() + "/");
            cfg.update(props);
            dataFolderConfigs.put(key, cfg);
            LOG.log(Level.INFO, "Registered DataFolderWatcher for {0}", key);
        } catch (IOException e) {
            LOG.log(Level.ERROR, "Failed to create DataFolderWatcher config for " + key, e);
        }
    }

    private void unregisterDataFolder(Path folder) {
        Configuration cfg = dataFolderConfigs.remove(folder.toAbsolutePath());
        if (cfg != null) {
            deleteConfig(cfg);
            LOG.log(Level.INFO, "Unregistered DataFolderWatcher for {0}", folder.toAbsolutePath());
        }
    }

    private void deleteConfig(Configuration config) {
        try {
            config.delete();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to delete DataFolderWatcher configuration", e);
        }
    }
}
