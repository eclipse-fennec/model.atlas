package org.eclipse.fennec.data.atlas.jpa.watcher;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.daanse.io.fs.watcher.api.FileSystemWatcherListener;
import org.eclipse.daanse.io.fs.watcher.api.propertytypes.FileSystemWatcherListenerProperties;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.persistence.eorm.EntityMappings;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Designate(factory = true, ocd = EormFileWatcher.Config.class)
@Component(name = EormFileWatcher.PID, scope = ServiceScope.SINGLETON,
service = FileSystemWatcherListener.class,
configurationPolicy = ConfigurationPolicy.REQUIRE)
@FileSystemWatcherListenerProperties(pattern = ".*.eorm", recursive = true)
public class EormFileWatcher implements FileSystemWatcherListener {

    private static final Logger LOG = System.getLogger(EormFileWatcher.class.getName());

    public static final String PID = WatcherConstants.PID_ENTITY_MAPPINGS_FILE_WATCHER;

    static final String FILE_EXTENSION = "eorm";
    static final String PROP_NAME = "eorm.name";
    static final String PROP_TARGET_NS_URI = "eorm.targetNsUri";
    static final String PROP_FOLDER = "eorm.folder";

    private final BundleContext bundleContext;
    
    @Reference(target = "(emf.name=eorm)")
    private ResourceSet resourceSet;
    
    private final Lock lock = new ReentrantLock();

    private final Map<String, ServiceRegistration<EntityMappings>> registrations = new HashMap<>();
	private Config config;

	  @ObjectClassDefinition
	    public @interface Config {

	        @AttributeDefinition(name = "File Context Matcher", required = true)
	        String fileContextMatcher() default "";
	    }

    @Activate
    public EormFileWatcher(BundleContext bundleContext, Config config) {
        this.bundleContext = bundleContext;
		this.config = config;
    }

    @Deactivate
    void deactivate() {
        lock.lock();
        try {
            registrations.values().forEach(ServiceRegistration::unregister);
            registrations.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void handleBasePath(Path basePath) {
    }

    @Override
    public void handleInitialPaths(List<Path> paths) {
        paths.forEach(this::loadJpaMapping);
    }
    
    private void loadJpaMapping(Path path) {
    	if (Files.isDirectory(path) || !path.toString().endsWith(FILE_EXTENSION)) {
            return;
        }
    	String uri = toUri(path);
    	load(uri);
    }

    @Override
    public void handlePathEvent(Path path, Kind<Path> kind) {
        String uri = toUri(path);
        if (!isJpaMappingFile(uri)) {
            return;
        }
        if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
        	loadJpaMapping(path);
        } else if (StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)) {
            unload(uri);
            loadJpaMapping(path);
        } else if (StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
            unload(uri);
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
            if (resource.getContents().get(0) instanceof EntityMappings config) {
                register(uri, config);
            } else {
                resourceSet.getResources().remove(resource);
                LOG.log(Level.WARNING, "Resource at {0} does not contain a EntityMappings", uri);
            }
        } catch (IOException e) {
            LOG.log(Level.ERROR, "Failed to load EntityMappings from {0}", uri, e);
        }
    }

    private void register(String uri, EntityMappings jpaMappingConfig) {
        ServiceRegistration<EntityMappings> existing = registrations.remove(uri);
        if (existing != null) {
            try {
                existing.unregister();
            } catch (IllegalStateException ignored) {
            }
        }
        Hashtable<String, Object> props = new Hashtable<>();
        props.put(PROP_NAME, jpaMappingConfig.getName() != null ? jpaMappingConfig.getName() : "");
        props.put(PROP_TARGET_NS_URI, jpaMappingConfig.getPackage() != null ? jpaMappingConfig.getPackage() : "");
        props.put(PROP_FOLDER, folderOf(uri));
        props.put(WatcherConstants.KEY_FILE_CONTEXT_MATCHER, config.fileContextMatcher());

        ServiceRegistration<EntityMappings> reg =
                bundleContext.registerService(EntityMappings.class, jpaMappingConfig, props);
        registrations.put(uri, reg);
        LOG.log(Level.INFO, "Registered EntityMappings ''{0}'' from {1}", jpaMappingConfig.getName(), uri);
        System.out.println("Registered EntityMappings for fileContextMatcher " + config.fileContextMatcher());
    }

    private void unload(String uri) {
        ServiceRegistration<EntityMappings> reg = registrations.remove(uri);
        if (reg != null) {
            try {
                reg.unregister();
                System.out.println("Unregistered EntityMappings for fileContextMatcher " + config.fileContextMatcher());
            } catch (IllegalStateException e) {
                // already unregistered
            }
        }
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
}
