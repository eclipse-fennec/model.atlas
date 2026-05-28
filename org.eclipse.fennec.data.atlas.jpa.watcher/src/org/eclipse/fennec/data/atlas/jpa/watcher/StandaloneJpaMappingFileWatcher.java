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
//import java.nio.file.ClosedWatchServiceException;
//import java.nio.file.FileSystems;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.StandardWatchEventKinds;
//import java.nio.file.WatchEvent;
//import java.nio.file.WatchEvent.Kind;
//import java.nio.file.WatchKey;
//import java.nio.file.WatchService;
//import java.util.HashMap;
//import java.util.Hashtable;
//import java.util.Map;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;
//
//import org.eclipse.emf.common.util.URI;
//import org.eclipse.emf.ecore.resource.Resource;
//import org.eclipse.emf.ecore.resource.ResourceSet;
//import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig;
//import org.osgi.framework.BundleContext;
//import org.osgi.framework.ServiceRegistration;
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
///**
// * Workaround variant of {@link EormFileWatcher} that uses its own
// * dedicated {@link WatchService} instance instead of registering through the
// * daanse filesystem-watcher whiteboard.
// *
// * <p>The daanse whiteboard stores one {@code WatchKeyConfig} per OS
// * {@code WatchKey}.  Java NIO returns the <em>same</em> {@code WatchKey} when
// * the same directory is registered multiple times on the same
// * {@code WatchService}, so sharing the whiteboard with
// * {@link JpaCsvDataImporter} (which activates last and overwrites the map
// * entry) silently drops all {@code .jpamapping} events.  A separate
// * {@code WatchService} instance gives this component its own independent key.
// *
// * <p>Switch back to {@link EormFileWatcher} in
// * {@link DataFolderWatcher#JPA_MAPPING_FILE_WATCHER_PID} once the upstream
// * issue in the daanse watcher is resolved.
// *
// * @see <a href="https://github.com/eclipse-daanse/org.eclipse.daanse.io.fs.watcher/issues/">daanse watcher issue</a>
// */
//@Designate(factory = true, ocd = StandaloneJpaMappingFileWatcher.Config.class)
//@Component(name = StandaloneJpaMappingFileWatcher.PID, scope = ServiceScope.SINGLETON,
//        service = {},
//        configurationPolicy = ConfigurationPolicy.REQUIRE)
//public class StandaloneJpaMappingFileWatcher {
//
//    private static final Logger LOG = System.getLogger(StandaloneJpaMappingFileWatcher.class.getName());
//
//    public static final String PID = "StandaloneJpaMappingFileWatcher";
//
//    static final String FILE_EXTENSION = "jpamapping";
//    static final String PROP_NAME = "jpamapping.name";
//    static final String PROP_TARGET_NS_URI = "jpamapping.targetNsUri";
//    static final String PROP_FOLDER = "jpamapping.folder";
//    static final String PROP_UNIT_NAME = "unitName";
//
//    private final BundleContext bundleContext;
//
//    @Reference(target = "(emf.name=jpamapping)")
//    private ResourceSet resourceSet;
//
//    private final Lock lock = new ReentrantLock();
//    private final Map<String, ServiceRegistration<JpaMappingConfig>> registrations = new HashMap<>();
//
//    private Config config;
//    private Path watchDir;
//    private WatchService watchService;
//
//    @ObjectClassDefinition
//    public @interface Config {
//
//        @AttributeDefinition(name = "Unit Name", required = true)
//        String unitName() default "";
//
//        @AttributeDefinition(name = "Watched path", required = true)
//        String io_fs_watcher_path() default "";
//    }
//
//    @Activate
//    public StandaloneJpaMappingFileWatcher(BundleContext bundleContext, Config config) {
//        this.bundleContext = bundleContext;
//        this.config = config;
//
//        String pathStr = config.io_fs_watcher_path();
//        if (pathStr == null || pathStr.isBlank()) {
//            LOG.log(Level.WARNING, "StandaloneJpaMappingFileWatcher activated without a valid path");
//            return;
//        }
//        watchDir = Path.of(pathStr).toAbsolutePath().normalize();
//
//        initialScan();
//        startWatchThread();
//    }
//
//    @Deactivate
//    void deactivate() {
//        if (watchService != null) {
//            try {
//                watchService.close();
//            } catch (IOException e) {
//                LOG.log(Level.WARNING, "Failed to close WatchService", e);
//            }
//        }
//        lock.lock();
//        try {
//            registrations.values().forEach(ServiceRegistration::unregister);
//            registrations.clear();
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    // ── Internal watch loop ──────────────────────────────────────────────────
//
//    private void initialScan() {
//        try (var stream = Files.list(watchDir)) {
//            stream.filter(p -> !Files.isDirectory(p) && p.toString().endsWith(FILE_EXTENSION))
//                  .forEach(this::loadJpaMapping);
//        } catch (IOException e) {
//            LOG.log(Level.WARNING, "Failed to scan {0} for .jpamapping files", watchDir);
//        }
//    }
//
//    private void startWatchThread() {
//        try {
//            watchService = FileSystems.getDefault().newWatchService();
//            watchDir.register(watchService,
//                    StandardWatchEventKinds.ENTRY_CREATE,
//                    StandardWatchEventKinds.ENTRY_MODIFY,
//                    StandardWatchEventKinds.ENTRY_DELETE);
//            Thread.ofVirtual().name("jpamapping-watcher[" + config.unitName() + "]").start(this::watchLoop);
//        } catch (IOException e) {
//            LOG.log(Level.ERROR, "Failed to start watch thread for {0}", watchDir, e);
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    private void watchLoop() {
//        try {
//            while (true) {
//                WatchKey key = watchService.take();
//                for (WatchEvent<?> event : key.pollEvents()) {
//                    WatchEvent.Kind<?> kind = event.kind();
//                    if (kind == StandardWatchEventKinds.OVERFLOW) {
//                        continue;
//                    }
//                    if (!(event.context() instanceof Path)) {
//                        continue;
//                    }
//                    Path changed = watchDir.resolve((Path) event.context());
//                    if (changed.toString().endsWith(FILE_EXTENSION)) {
//                        handlePathEvent(changed, (Kind<Path>) kind);
//                    }
//                }
//                if (!key.reset()) {
//                    break;
//                }
//            }
//        } catch (ClosedWatchServiceException e) {
//            // Normal shutdown via deactivate()
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }
//
//    // ── Path event handling ──────────────────────────────────────────────────
//
//    private void handlePathEvent(Path path, Kind<Path> kind) {
//        String uri = toUri(path);
//        if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
//            loadJpaMapping(path);
//        } else if (StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)) {
//            unload(uri);
//            loadJpaMapping(path);
//        } else if (StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
//            unload(uri);
//        }
//    }
//
//    private void loadJpaMapping(Path path) {
//        if (Files.isDirectory(path) || !path.toString().endsWith(FILE_EXTENSION)) {
//            return;
//        }
//        load(toUri(path));
//    }
//
//    // ── EMF loading & OSGi service registration ──────────────────────────────
//
//    private void load(String uri) {
//        try {
//            Resource resource = resourceSet.createResource(URI.createURI(uri));
//            resource.load(null);
//            if (resource.getContents().isEmpty()) {
//                resourceSet.getResources().remove(resource);
//                LOG.log(Level.WARNING, "Empty resource loaded from {0}", uri);
//                return;
//            }
//            if (resource.getContents().get(0) instanceof JpaMappingConfig cfg) {
//                register(uri, cfg);
//            } else {
//                resourceSet.getResources().remove(resource);
//                LOG.log(Level.WARNING, "Resource at {0} does not contain a JpaMappingConfig", uri);
//            }
//        } catch (IOException e) {
//            LOG.log(Level.ERROR, "Failed to load JpaMappingConfig from {0}", uri, e);
//        }
//    }
//
//    private void register(String uri, JpaMappingConfig jpaMappingConfig) {
//        lock.lock();
//        try {
//            ServiceRegistration<JpaMappingConfig> existing = registrations.remove(uri);
//            if (existing != null) {
//                try {
//                    existing.unregister();
//                } catch (IllegalStateException ignored) {
//                }
//            }
//            Hashtable<String, Object> props = new Hashtable<>();
//            props.put(PROP_NAME, jpaMappingConfig.getName() != null ? jpaMappingConfig.getName() : "");
//            props.put(PROP_TARGET_NS_URI, jpaMappingConfig.getTargetModelNsUri() != null ? jpaMappingConfig.getTargetModelNsUri() : "");
//            props.put(PROP_FOLDER, folderOf(uri));
//            props.put(PROP_UNIT_NAME, config.unitName());
//
//            ServiceRegistration<JpaMappingConfig> reg =
//                    bundleContext.registerService(JpaMappingConfig.class, jpaMappingConfig, props);
//            registrations.put(uri, reg);
//            LOG.log(Level.INFO, "Registered JpaMappingConfig ''{0}'' from {1}", jpaMappingConfig.getName(), uri);
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    private void unload(String uri) {
//        lock.lock();
//        try {
//            ServiceRegistration<JpaMappingConfig> reg = registrations.remove(uri);
//            if (reg != null) {
//                try {
//                    reg.unregister();
//                } catch (IllegalStateException ignored) {
//                }
//            }
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    // ── Helpers ──────────────────────────────────────────────────────────────
//
//    private String toUri(Path path) {
//        return path.toAbsolutePath().normalize().toUri().toString();
//    }
//
//    private String folderOf(String uri) {
//        int last = uri.lastIndexOf('/');
//        return last > 0 ? uri.substring(0, last) : uri;
//    }
//}
