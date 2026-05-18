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
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.data.atlas.jpa.watcher.api.WatcherConstants;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.persistence.eorm.EORMPackage;
import org.eclipse.fennec.persistence.eorm.EntityMappings;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
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
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

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
    
    @Reference
    EORMPackage eormPackage;

    private final Lock lock = new ReentrantLock();

    private final Map<String, ServiceRegistration<EntityMappings>> registrations = new HashMap<>();
    private final Map<String, ServiceTracker<EPackage, EPackage>> pendingTrackers = new HashMap<>();
	private Config config;

	  @ObjectClassDefinition
	    public @interface Config {

	        @AttributeDefinition(name = "File Context Matcher", required = true)
	        String file_context_matcher();
	        
	        @AttributeDefinition(name = "Jpa Root Folder", required = true)
	        String jpa_root_folder();
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
        	System.out.println("Unregistering " + registrations.size() + " EntityMappings");
        	registrations.values().forEach(reg -> {
        		System.out.println("Unregistering EntityMappings for " + reg.getReference().getProperty("file.context.matcher"));
        	});
            registrations.values().forEach(ServiceRegistration::unregister);
            registrations.clear();
            pendingTrackers.values().forEach(ServiceTracker::close);
            pendingTrackers.clear();
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
        if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind)
                || StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)) {
            // CREATE includes atomic-rename saves (vim/IntelliJ/etc) — Linux's
            // IN_MOVED_TO surfaces as ENTRY_CREATE even when the file existed.
            // Always unload first so any prior registration tied to this path
            // is torn down before the reload.
            unload(uri);
            loadJpaMapping(path);
        } else if (StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
        	System.out.println("Handling deleting event " + uri);
            unload(uri);
        }
    }

    private void load(String uri) {
        Resource resource = null;
        try {
            resource = resourceSet.createResource(URI.createURI(uri));
            resource.load(null);
            if (resource.getContents().isEmpty()) {
                LOG.log(Level.WARNING, "Empty resource loaded from {0}", uri);
                return;
            }
            if (!(resource.getContents().get(0) instanceof EntityMappings mappings)) {
                LOG.log(Level.WARNING, "Resource at {0} does not contain a EntityMappings", uri);
                return;
            }

            String nsUri = mappings.getPackage();
            boolean hasEntityRefs = !mappings.getEntity().isEmpty();
            if (nsUri == null || nsUri.isBlank() || !hasEntityRefs) {
                // No entity references to resolve — register immediately and detach.
                EcoreUtil.resolveAll(resource);
                register(uri, mappings);
                detach(resource);
                resource = null;
                return;
            }
            awaitEPackageAndRegister(uri, mappings, nsUri, resource);
            // Resource ownership is transferred — tracker callback will detach.
            resource = null;
        } catch (IOException e) {
            LOG.log(Level.ERROR, "Failed to load EntityMappings from {0}: {1}", uri, e);
        } finally {
            // If we reached here with the resource still attached (early return or failure
            // path), drop it so the next load() doesn't see an accumulated stale copy.
            if (resource != null) {
                detach(resource);
            }
        }
    }

    private void detach(Resource resource) {
        resource.getContents().clear();
        resourceSet.getResources().remove(resource);
    }

    private void awaitEPackageAndRegister(String uri, EntityMappings mappings, String nsUri, Resource resource) {
        Filter filter;
        try {
            filter = FrameworkUtil.createFilter("(&(objectClass=" + EPackage.class.getName()
                    + ")(" + EMFNamespaces.EMF_MODEL_NSURI + "=" + nsUri + "))");
        } catch (InvalidSyntaxException e) {
            LOG.log(Level.ERROR, "Invalid filter for EPackage nsUri {0}", nsUri, e);
            detach(resource);
            return;
        }

        ServiceTracker<EPackage, EPackage> tracker = new ServiceTracker<>(bundleContext, filter,
                new ServiceTrackerCustomizer<EPackage, EPackage>() {
                    @Override
                    public EPackage addingService(ServiceReference<EPackage> reference) {
                        EPackage ePackage = bundleContext.getService(reference);
                        lock.lock();
                        try {
                            if (!registrations.containsKey(uri)) {
                                // Walk the EPackage so eType proxies inside its EAttributes
                                // get resolved against the registry now that the standard
                                // packages (Ecore, etc.) are reachable.
//                                EcoreUtil.resolveAll(ePackage);
                                EcoreUtil.resolveAll(mappings);
                                register(uri, mappings);
                                detach(resource);
                            }
                        } finally {
                            lock.unlock();
                        }
                        return ePackage;
                    }

                    @Override
                    public void modifiedService(ServiceReference<EPackage> reference, EPackage service) {
                        // no-op
                    }

                    @Override
                    public void removedService(ServiceReference<EPackage> reference, EPackage service) {
                        lock.lock();
                        try {
                            unregisterMapping(uri);
                        } finally {
                            lock.unlock();
                        }
                        bundleContext.ungetService(reference);
                    }
                });

        lock.lock();
        try {
            ServiceTracker<EPackage, EPackage> previous = pendingTrackers.put(uri, tracker);
            if (previous != null) {
                previous.close();
            }
        } finally {
            lock.unlock();
        }
        tracker.open();
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
        props.put("fennec.jpa.orm.mapping.name", config.jpa_root_folder());
        props.put(WatcherConstants.KEY_FILE_CONTEXT_MATCHER, config.file_context_matcher());
        props.put(WatcherConstants.KEY_JPA_ROOT_FOLDER, config.jpa_root_folder());

        ServiceRegistration<EntityMappings> reg =
                bundleContext.registerService(EntityMappings.class, jpaMappingConfig, props);
        registrations.put(uri, reg);
        LOG.log(Level.INFO, "Registered EntityMappings ''{0}'' from {1}", jpaMappingConfig.getName(), uri);
        System.out.println("Registered EntityMappings for fileContextMatcher " + config.file_context_matcher());
    }

    private void unload(String uri) {
        lock.lock();
        try {
            ServiceTracker<EPackage, EPackage> tracker = pendingTrackers.remove(uri);
            if (tracker != null) {
                tracker.close();
            }
            System.out.println("unload uri=" + uri
                    + " registrations.keys=" + registrations.keySet());
            unregisterMapping(uri);
        } finally {
            lock.unlock();
        }
    }

    /** Caller must hold {@link #lock}. */
    private void unregisterMapping(String uri) {
        ServiceRegistration<EntityMappings> reg = registrations.remove(uri);
        if (reg != null) {
            try {
                reg.unregister();
                System.out.println("Unregistered EntityMappings for fileContextMatcher " + config.file_context_matcher());
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
