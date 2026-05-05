package org.eclipse.fennec.data.atlas.jpa.watcher;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.daanse.io.fs.watcher.api.FileSystemWatcherListener;
import org.eclipse.daanse.io.fs.watcher.api.propertytypes.FileSystemWatcherListenerProperties;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.util.JPAMappingResourceFactoryImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;

@Component(name = JpaMappingFileWatcher.PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
@FileSystemWatcherListenerProperties(recursive = true)
public class JpaMappingFileWatcher implements FileSystemWatcherListener {

    private static final Logger LOG = System.getLogger(JpaMappingFileWatcher.class.getName());

    public static final String PID = "JpaMappingFileWatcher";

    static final String FILE_EXTENSION = "jpamapping";

    static final String PROP_NAME = "jpamapping.name";
    static final String PROP_TARGET_NS_URI = "jpamapping.targetNsUri";
    static final String PROP_FOLDER = "jpamapping.folder";
    static final String PROP_UNIT_NAME = "unitName";

    private final BundleContext bundleContext;
    private final ResourceSet resourceSet;
    private final Lock lock = new ReentrantLock();

    private final Map<String, ServiceRegistration<JpaMappingConfig>> registrations = new HashMap<>();
    private final List<String> pendingUris = new ArrayList<>();

    private Timer timer = new Timer();
    private TimerTask pendingTask;
    private String unitName;
    
    @Activate
    public JpaMappingFileWatcher(BundleContext bundleContext, Map<String, Object> properties) {
        this.bundleContext = bundleContext;
        this.resourceSet = createResourceSet();
        if(properties.containsKey("unitName")) this.unitName = (String) properties.get("unitName");
    }

    @Deactivate
    void deactivate() {
        lock.lock();
        try {
            if (pendingTask != null) {
                pendingTask.cancel();
            }
            timer.cancel();
            registrations.values().forEach(ServiceRegistration::unregister);
            registrations.clear();
            resourceSet.getResources().forEach(Resource::unload);
            resourceSet.getResources().clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void handleBasePath(Path basePath) {
    }

    @Override
    public void handleInitialPaths(List<Path> paths) {
        List<String> uris = paths.stream()
                .map(this::toUri)
                .filter(this::isJpaMappingFile)
                .toList();
        scheduleLoad(uris);
    }

    @Override
    public void handlePathEvent(Path path, Kind<Path> kind) {
        String uri = toUri(path);
        if (!isJpaMappingFile(uri)) {
            return;
        }
        if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
            scheduleLoad(List.of(uri));
        } else if (StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)) {
            unload(uri);
            scheduleLoad(List.of(uri));
        } else if (StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
            unload(uri);
        }
    }

    private void scheduleLoad(List<String> uris) {
        lock.lock();
        try {
            pendingUris.addAll(uris);
            if (pendingTask != null) {
                pendingTask.cancel();
            }
            pendingTask = new TimerTask() {
                @Override
                public void run() {
                    flushPending();
                }
            };
            timer.schedule(pendingTask, 1000);
        } finally {
            lock.unlock();
        }
    }

    private void flushPending() {
        lock.lock();
        try {
            new ArrayList<>(pendingUris).forEach(this::load);
            pendingUris.clear();
        } finally {
            lock.unlock();
        }
    }

    private void load(String uri) {
        try {
            Resource resource = resourceSet.createResource(URI.createURI(uri));
            resource.load(null);
            if (resource.getContents().isEmpty()) {
                resourceSet.getResources().remove(resource);
                LOG.log(Level.WARNING, "Empty resource loaded from {0}", uri);
                return;
            }
            if (resource.getContents().get(0) instanceof JpaMappingConfig config) {
                register(uri, config);
            } else {
                resourceSet.getResources().remove(resource);
                LOG.log(Level.WARNING, "Resource at {0} does not contain a JpaMappingConfig", uri);
            }
        } catch (IOException e) {
            LOG.log(Level.ERROR, "Failed to load JpaMappingConfig from {0}", uri, e);
        }
    }

    private void register(String uri, JpaMappingConfig config) {
        Hashtable<String, Object> props = new Hashtable<>();
        props.put(PROP_NAME, config.getName() != null ? config.getName() : "");
        props.put(PROP_TARGET_NS_URI, config.getTargetModelNsUri() != null ? config.getTargetModelNsUri() : "");
        props.put(PROP_FOLDER, folderOf(uri));
        props.put(PROP_UNIT_NAME, unitName);
        
        ServiceRegistration<JpaMappingConfig> reg =
                bundleContext.registerService(JpaMappingConfig.class, config, props);
        registrations.put(uri, reg);
        LOG.log(Level.INFO, "Registered JpaMappingConfig ''{0}'' from {1}", config.getName(), uri);
    }

    private void unload(String uri) {
        ServiceRegistration<JpaMappingConfig> reg = registrations.remove(uri);
        if (reg != null) {
            try {
                reg.unregister();
            } catch (IllegalStateException e) {
                // already unregistered
            }
        }
        resourceSet.getResources().stream()
                .filter(r -> uri.equals(r.getURI().toString()))
                .findFirst()
                .ifPresent(r -> {
                    r.unload();
                    resourceSet.getResources().remove(r);
                });
    }

    private boolean isJpaMappingFile(String uri) {
        return uri.endsWith("." + FILE_EXTENSION);
    }

    private String toUri(Path path) {
        return path.toAbsolutePath().normalize().toUri().toString();
    }

    private String folderOf(String uri) {
        int last = uri.lastIndexOf('/');
        return last > 0 ? uri.substring(0, last) : uri;
    }

    private ResourceSet createResourceSet() {
        ResourceSetImpl rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put(FILE_EXTENSION, new JPAMappingResourceFactoryImpl());
        return rs;
    }
}
