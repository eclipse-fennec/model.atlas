/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
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
package org.eclipse.fennec.data.atlas.epackage.watcher;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.daanse.io.fs.watcher.api.FileSystemWatcherListener;
import org.eclipse.daanse.io.fs.watcher.api.propertytypes.FileSystemWatcherListenerProperties;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.model.atlas.emf.common.configurator.DynamicEPackageConfigurator;
import org.eclipse.fennec.model.atlas.emf.common.ecore.EClassResolvingDynamicEFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;

/**
 * Watches a folder for {@code .ecore} files and registers the contained
 * {@link EPackage}s (and their sub-packages) as OSGi services, together with
 * a paired {@link EPackageConfigurator}. Non-ecore entries delivered by the
 * underlying file-system watcher are ignored.
 *
 * @author Juergen Albert
 * @since 8 Jan 2025
 */
@Component(name = EMFFileWatcher.PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
@FileSystemWatcherListenerProperties(recursive = true, pattern = ".*\\.ecore")
public class EMFFileWatcher implements FileSystemWatcherListener {

    private static final Logger LOG = System.getLogger(EMFFileWatcher.class.getName());

    public static final String PID = "EMFFileWatcher";

    private final ResourceSet resourceSet;
    
    private final BundleContext bundleContext;
    private final Map<String, String> forwardedProperties;

    private final Lock lock = new ReentrantLock();
    private final Map<String, Metadata> originalToNsUri = new HashMap<>();
    private final Map<String, Metadata> ownedNsUris = new HashMap<>();

    private final Set<String> pendingUris = new LinkedHashSet<>();
    private final Timer timer = new Timer();
    private TimerTask task = null;

    private static class Metadata {
        String originalFileUri;
        Resource resource;
        Map<EPackage, List<ServiceRegistration<?>>> services = new HashMap<>();
    }

    @Activate
    public EMFFileWatcher(
            @Reference(name = "resourceSet", scope = ReferenceScope.PROTOTYPE_REQUIRED) ResourceSet resourceSet,
            BundleContext bundleContext,
            Map<String, Object> properties) {
        this.resourceSet = resourceSet;
        this.bundleContext = bundleContext;
        this.forwardedProperties = filterForwardedProperties(properties);
    }

    /**
     * Keeps only the configuration entries that should ride along on every
     * EPackage service this watcher publishes (e.g. a pipeline identifier such
     * as {@code file.context.matcher}). DS / OSGi framework keys and
     * file-system-watcher whiteboard keys are dropped so downstream consumers
     * see only meaningful, pipeline-level properties.
     */
    private static Map<String, String> filterForwardedProperties(Map<String, Object> props) {
        if (props == null || props.isEmpty()) {
            return Map.of();
        }
        Map<String, String> out = new HashMap<>();
        for (Entry<String, Object> e : props.entrySet()) {
            String key = e.getKey();
            if (isInternalProperty(key)) {
                continue;
            }
            Object value = e.getValue();
            if (value == null) {
                continue;
            }
            out.put(key, value.toString());
        }
        return Map.copyOf(out);
    }

    private static boolean isInternalProperty(String key) {
        return key.startsWith("io.fs.watcher.")
                || key.startsWith("service.")
                || key.startsWith("component.")
                || key.startsWith("felix.")
                || "objectClass".equals(key);
    }

    @Deactivate
    void deactivate() {
        lock.lock();
        try {
            originalToNsUri.values().forEach(md -> md.services.forEach((ePackage, registrations) -> {
                registrations.forEach(ServiceRegistration::unregister);
                
            }));
            originalToNsUri.clear();
            ownedNsUris.clear();
        } finally {
            lock.unlock();
        }
        timer.cancel();
    }

    @Override
    public void handleBasePath(Path basePath) {
    }

    @Override
    public void handleInitialPaths(List<Path> paths) {
        List<String> toAdd = paths.stream().map(this::cleanUpPath).toList();
        scheduleDelaied(toAdd);
    }

    @Override
    public void handlePathEvent(Path path, Kind<Path> kind) {
        String pathString = cleanUpPath(path);
        if (StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)
                || StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
            // CREATE includes atomic-rename saves (vim/IntelliJ/etc) — Linux's
            // IN_MOVED_TO surfaces as ENTRY_CREATE even when the file existed.
            // Always run handleRemove first so any prior registration tied to
            // this path is cleaned up before the reload.
            lock.lock();
            try {
                handleRemove(List.of(pathString));
            } finally {
                lock.unlock();
            }
            scheduleDelaied(List.of(pathString));
        } else if (StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
            lock.lock();
            try {
                handleRemove(List.of(pathString));
            } finally {
                lock.unlock();
            }
        }
    }

    private String cleanUpPath(Path path) {
        return path.toAbsolutePath().normalize().toUri().toString();
    }

    private void scheduleDelaied(List<String> toHandle) {
        lock.lock();
        try {
            pendingUris.addAll(toHandle);
            if (task != null) {
                task.cancel();
            }
            task = new DelaiedTimerTask(this::loadDelaied);
            timer.schedule(task, 1000);
        } finally {
            lock.unlock();
        }
    }

    private void loadDelaied() {
        lock.lock();
        try {
            loadResources(pendingUris);
            pendingUris.clear();
        } finally {
            lock.unlock();
        }
    }

    private void loadResources(Collection<String> uris) {
        try {
            List<Resource> toHandle = new ArrayList<>();
            createResources(uris, toHandle);
            loadResources0(toHandle);
            handleEPackages(toHandle);
        } catch (Exception e) {
            LOG.log(Level.ERROR, "Unable to handle EPackage registration", e);
        }
    }

    private void createResources(Collection<String> uris, List<Resource> toHandle) {
        for (String uri : uris) {
            LOG.log(Level.INFO, "Loading URI " + uri);
            Resource resource = resourceSet.getResource(URI.createURI(uri), false);
            if (resource == null) {
                resource = resourceSet.createResource(URI.createURI(uri));
            }
            toHandle.add(resource);
        }
    }

    private void loadResources0(List<Resource> toHandle) {
        for (Iterator<Resource> iterator = toHandle.iterator(); iterator.hasNext();) {
            Resource resource = iterator.next();
            try {
                resource.load(null);
            } catch (IOException e) {
                LOG.log(Level.ERROR, "Unable to load Resource for file " + resource.getURI().toString(), e);
            }
            if (resource.getContents().isEmpty()) {
                resourceSet.getResources().remove(resource);
                iterator.remove();
            } else {
                resource.getContents().forEach(EcoreUtil::resolveAll);
            }
        }
        for (Iterator<Resource> iterator = resourceSet.getResources().iterator(); iterator.hasNext();) {
            Resource resource = iterator.next();
            if (resource.getContents().isEmpty()) {
                iterator.remove();
            }
        }
    }

    private void handleEPackages(List<Resource> toHandle) {
        if (toHandle.isEmpty()) return;
        List<Metadata> metadataToHandle = new ArrayList<>();
        for (Resource resource : toHandle) {
            EObject eObject = resource.getContents().get(0);
            if (!(eObject instanceof EPackage ePackage)) {
                continue;
            }
            if (ownedNsUris.containsKey(ePackage.getNsURI())) {
                resource.unload();
                resourceSet.getResources().remove(resource);
                LOG.log(Level.WARNING,
                        resource.getURI().toString() + " contains EPackage with NsURI " + ePackage.getNsURI()
                                + " which is already registered from another file. It will be skipped.");
                continue;
            }
            ePackage.setEFactoryInstance(new EClassResolvingDynamicEFactory());

            Metadata metadata = new Metadata();
            metadata.originalFileUri = resource.getURI().toString();
            metadata.resource = resource;
            metadata.services.put(ePackage, new ArrayList<>());
            addSubPackages(metadata, ePackage.getESubpackages());
            originalToNsUri.put(metadata.originalFileUri, metadata);
            metadata.services.keySet().forEach(p -> ownedNsUris.put(p.getNsURI(), metadata));
            metadataToHandle.add(metadata);
        }
        for (Resource resource : toHandle) {
            if (resource.getContents().isEmpty()) continue;
            EObject eObject = resource.getContents().get(0);
            if (eObject instanceof EPackage ePackage) {
                resource.setURI(URI.createURI(ePackage.getNsURI()));
            }
        }
        metadataToHandle.forEach(this::registerConfigurators);
        metadataToHandle.forEach(this::registerEPackage);
    }

    private void registerConfigurators(Metadata data) {
        data.services.forEach(this::registerConfigurator);
    }

    private void registerConfigurator(EPackage ePackage, List<ServiceRegistration<?>> registrations) {
        DynamicEPackageConfigurator configurator = new DynamicEPackageConfigurator(ePackage);
        ServiceRegistration<EPackageConfigurator> serviceRegistration = bundleContext
                .registerService(EPackageConfigurator.class, configurator, getServiceProperties(ePackage));
        registrations.add(serviceRegistration);
    }

    private void registerEPackage(Metadata data) {
        data.services.forEach(this::registerEPackage);
    }

    private void registerEPackage(EPackage ePackage, List<ServiceRegistration<?>> registrations) {
        ServiceRegistration<EPackage> serviceRegistration = bundleContext.registerService(EPackage.class, ePackage,
                getServiceProperties(ePackage));
        registrations.add(serviceRegistration);
    }

    private Dictionary<String, String> getServiceProperties(EPackage ePackage) {
        Dictionary<String, String> serviceProperties = new Hashtable<>();
        String nsUri = ePackage.getNsURI();
        serviceProperties.put(EMFNamespaces.EMF_NAME, ePackage.getName());
        serviceProperties.put(EMFNamespaces.EMF_MODEL_NSURI, nsUri);
        serviceProperties.put(EMFNamespaces.EMF_MODEL_REGISTRATION, EMFNamespaces.MODEL_REGISTRATION_DYNAMIC);
        forwardedProperties.forEach(serviceProperties::put);
        EAnnotation eAnnotation = ePackage.getEAnnotation("properties");
        if (eAnnotation != null) {
            for (Entry<String, String> entry : eAnnotation.getDetails()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    serviceProperties.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return serviceProperties;
    }

    private void addSubPackages(Metadata metadata, EList<EPackage> eSubpackages) {
        for (EPackage ePackage : eSubpackages) {
            metadata.services.put(ePackage, new ArrayList<>());
            addSubPackages(metadata, ePackage.getESubpackages());
        }
    }

    private void handleRemove(List<String> toRemove) {
        for (String remove : toRemove) {
            Metadata metadata = originalToNsUri.remove(remove);
            if (metadata == null) {
                continue;
            }
            metadata.services.forEach((ePackage, registrations) -> {
                registrations.forEach(ServiceRegistration::unregister);
                ownedNsUris.remove(ePackage.getNsURI());
            });
            metadata.resource.unload();
            resourceSet.getResources().remove(metadata.resource);
        }
    }

    private static final class DelaiedTimerTask extends TimerTask {

        private final Runnable runnable;

        DelaiedTimerTask(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            runnable.run();
        }
    }
}
