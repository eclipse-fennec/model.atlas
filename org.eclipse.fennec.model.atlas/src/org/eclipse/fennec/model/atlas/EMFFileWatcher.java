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
package org.eclipse.fennec.model.atlas;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.daanse.io.fs.watcher.api.FileSystemWatcherListener;
import org.eclipse.daanse.io.fs.watcher.api.propertytypes.FileSystemWatcherListenerProperties;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.DynamicEObjectImpl;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.codec.configurator.CodecModuleConfigurator;
import org.eclipse.fennec.codec.options.CodecModelInfoOptions;
import org.eclipse.fennec.codec.options.CodecModuleOptions;
import org.eclipse.fennec.codec.options.CodecResourceOptions;
import org.eclipse.fennec.model.atlas.emf.common.configurator.DynamicEPackageConfigurator;
import org.eclipse.fennec.model.atlas.emf.common.configurator.PrototypeEObjectServiceFactory;
import org.eclipse.fennec.model.atlas.emf.common.ecore.EClassResolvingDynamicEFactory;
import org.gecko.emf.osgi.configurator.EPackageConfigurator;
import org.gecko.emf.osgi.constants.EMFNamespaces;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * 
 * @author Juergen Albert
 * @since 8 Jan 2025
 */
@Component(name = EMFFileWatcher.PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
@FileSystemWatcherListenerProperties(recursive = true)
@SuppressWarnings("rawtypes")
public class EMFFileWatcher implements FileSystemWatcherListener {

  private static final Logger LOG = System.getLogger(EMFFileWatcher.class.getName());

  public static final String PID = "EMFFileWatcher";

  private final ResourceSet resourceSet;

  private final Lock lock = new ReentrantLock();
  private final Map<String, Metadata> originalToNsUri = new HashMap<>();
  private final Map<String, Configuration> qvtConfigs = new HashMap<>();

  private final BundleContext bundleContext;

  private final ServiceTracker eObjectTracker;

  private final ServiceTracker ePackageTracker;

  private ConfigurationAdmin configAdmin;

  private static class Metadata {
    String originalFileUri;
    Resource resource;
    Map<EObject, List<ServiceRegistration<?>>> services = new HashMap<>();
  }

  @SuppressWarnings("unchecked")
  @Activate
  public EMFFileWatcher(
      @Reference(cardinality = ReferenceCardinality.MANDATORY, target = "(test=test)") CodecModuleConfigurator codecModuleConfigurator,
      @Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED, target = "(" + EMFNamespaces.EMF_CONFIGURATOR_NAME
          + "=CodecJson)") ResourceSet resourceSet,
      @Reference ConfigurationAdmin configAdmin, BundleContext bundleContext) {
    this.resourceSet = resourceSet;
    this.configAdmin = configAdmin;
    this.bundleContext = bundleContext;
    eObjectTracker = new ServiceTracker(bundleContext, EObject.class, new ServiceTrackerCustomizer<EObject, EObject>() {

      @Override
      public EObject addingService(ServiceReference<EObject> reference) {
        EObject eObject = bundleContext.getService(reference);
        LOG.log(Level.INFO, "EObject Registered " + eObject);
        return eObject;
      }

      @Override
      public void modifiedService(ServiceReference<EObject> reference, EObject service) {
        LOG.log(Level.INFO, "EObject Modified " + service);
      }

      @Override
      public void removedService(ServiceReference<EObject> reference, EObject service) {
        LOG.log(Level.INFO, "EObject Removed " + service);
      }
    });
    eObjectTracker.open();

    ePackageTracker = new ServiceTracker(bundleContext, EPackage.class,
        new ServiceTrackerCustomizer<EPackage, EPackage>() {

          @Override
          public EPackage addingService(ServiceReference<EPackage> reference) {
            EPackage eObject = bundleContext.getService(reference);
            LOG.log(Level.INFO, "EPackage Registered " + eObject);
            return eObject;
          }

          @Override
          public void modifiedService(ServiceReference<EPackage> reference, EPackage service) {
            LOG.log(Level.INFO, "EPackage Modified " + service);
          }

          @Override
          public void removedService(ServiceReference<EPackage> reference, EPackage service) {
            LOG.log(Level.INFO, "EPackage Removed " + service);
          }
        });

    ePackageTracker.open();
  }

  @Deactivate
  void deactivate() {
    eObjectTracker.close();
    ePackageTracker.close();
    lock.lock();
    try {
      qvtConfigs.values().forEach(t -> {
        try {
          t.delete();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      });
      qvtConfigs.clear();
      originalToNsUri.values()
          .forEach(md -> md.services.values().forEach(reg -> reg.forEach(ServiceRegistration::unregister)));
      originalToNsUri.clear();
    } finally {
      lock.unlock();
    }
  }

  private void loadResources(List<String> uris) {
    try {
      List<Resource> toHandle = new ArrayList<>();
      createResource(uris, toHandle);
      loadResource(toHandle);
      handleEPackages(toHandle);
      handleQVT(uris);
      handleEObjects(toHandle);
    } catch (Exception e) {
      LOG.log(Level.ERROR, "Unable to handle EObject/EPackage registration", e);
    }
  }

  private void handleQVT(List<String> uris) throws IOException {
    for (String uri : uris) {
      if (uri.endsWith(".qvto")) {
        URI emfURI = URI.createURI(uri);
        String id = emfURI.segment(emfURI.segmentCount() - 2);
        Configuration configuration = configAdmin.getFactoryConfiguration("QVTModelTransformator", id, "?");
        Dictionary<String, String> props = new Hashtable<>();
        props.put("transformator.id", id);
        props.put("qvt.template.uri", uri);
        configuration.update(props);
        qvtConfigs.put(uri, configuration);
      }
    }
  }

  private void createResource(List<String> uris, List<Resource> toHandle) {
    for (String uri : uris) {
      System.out.println("Loading URI " + uri);
      int index = uri.lastIndexOf('.');
      if (index != -1) {
        String fileExtension = uri.substring(index + 1);
        if ("jsonschema".equals(fileExtension)) {
          Resource resource = loadJsonschema(uri);
          toHandle.add(resource);
        } else if (resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().containsKey(fileExtension)) {
          Resource resource = resourceSet.getResource(URI.createURI(uri), false);
          if (resource == null) {
            resource = resourceSet.createResource(URI.createURI(uri));
          }
          toHandle.add(resource);
        }
      }
    }
  }

  public Resource loadJsonschema(String pathToJsonschemaFile) {
    Resource resource = resourceSet.createResource(URI.createURI(pathToJsonschemaFile), "application/json");
    Map<String, Object> options = new HashMap<>();
    options.put(CodecResourceOptions.CODEC_ROOT_OBJECT, EcorePackage.Literals.EPACKAGE);
    options.put(CodecModuleOptions.CODEC_MODULE_SERIALIZE_TYPE, false);
    options.put(CodecModuleOptions.CODEC_MODULE_SERIALIZE_EMPTY_VALUE, true);
    options.put(CodecModuleOptions.CODEC_MODULE_SERIALIZE_NULL_VALUE, true);
    Map<String, Object> classOptions = new HashMap<>();
    classOptions.put(CodecModelInfoOptions.CODEC_EXTRAS,
        Map.of("jsonschema", "true", "jsonschema.feature.key", "definitions"));
    options.put(CodecResourceOptions.CODEC_OPTIONS, Map.of(EcorePackage.Literals.EPACKAGE, classOptions));
    try {
      resource.load(options);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Resource temp = resourceSet.createResource(URI.createURI("test.ecore"));
      temp.getContents().add(EcoreUtil.copy(resource.getContents().get(0)));
      temp.save(baos, null);
      System.err.println(new String(baos.toByteArray()));
    } catch (IOException e) {
      LOG.log(Level.ERROR, "Unable to load Resource for file " + resource.getURI().toString(), e);
    }
    return resource;
  }

  private void loadResource(List<Resource> toHandle) {
    for (Iterator<Resource> iterator = toHandle.iterator(); iterator.hasNext();) {
      Resource resource = iterator.next();
      try {
        resource.load(null);
      } catch (IOException e) {
        LOG.log(Level.ERROR, "Unable to load Resource for file " + resource.getURI().toString(), e);
      }
      if (resource.getContents().size() == 0) {
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
    List<Metadata> metadataToHandle = new ArrayList<>();
    for (Resource resource : toHandle) {
      EObject eObject = resource.getContents().get(0);
      if (eObject instanceof EPackage ePackage) {
        if (resourceSet.getPackageRegistry().containsKey(ePackage.getNsURI())) {
          resource.unload();
          resourceSet.getResources().remove(resource);
          LOG.log(Level.WARNING, resource.getURI().toString() + " contains EPackage with NsURI " + ePackage.getNsURI()
              + " which is already registered. It will be skipped, until the URI changes.");
          continue;
        }
        ePackage.setEFactoryInstance(new EClassResolvingDynamicEFactory());

        Metadata metadata = new Metadata();
        metadata.originalFileUri = resource.getURI().toString();
        metadata.resource = resource;
        metadata.services.put(ePackage, new ArrayList<>());
        addSubPackages(metadata, ePackage.getESubpackages());
        originalToNsUri.put(metadata.originalFileUri, metadata);
        metadataToHandle.add(metadata);
      }
    }
    for (Resource resource : toHandle) {
      EObject eObject = resource.getContents().get(0);
      if (eObject instanceof EPackage ePackage) {
        resource.setURI(URI.createURI(ePackage.getNsURI()));
      }
    }
    metadataToHandle.forEach(this::registerConfigurators);
    metadataToHandle.forEach(this::registerEPackage);
  }

  private void handleEObjects(List<Resource> toHandle) {
    List<Metadata> metadataToHandle = new ArrayList<>();
    for (Resource resource : toHandle) {
      for (EObject eObject : resource.getContents()) {
        if (eObject instanceof EPackage) {
          continue;
        }

        Metadata metadata = new Metadata();
        metadata.originalFileUri = resource.getURI().toString();
        metadata.resource = resource;
        metadata.services.put(eObject, new ArrayList<>());
        originalToNsUri.put(metadata.originalFileUri, metadata);
        metadataToHandle.add(metadata);
      }
    }
    metadataToHandle.forEach(this::registerEObject);
  }

  private void registerEObject(Metadata data) {
    data.services.forEach(this::registerEObjectService);
  }

  private void registerEObjectService(EObject eObject, List<ServiceRegistration<?>> resigtrations) {
    String id = EcoreUtil.getID(eObject);
    if (id == null) {
      return;
    }
    String idProp = eObject.eClass().getEIDAttribute().getName();
    PrototypeEObjectServiceFactory<EObject> factory = new PrototypeEObjectServiceFactory<>(eObject);
    List<String> interfaces = new ArrayList<>();
    EClass eClass = eObject.eClass();
    BundleContext theBundleContextToUse = bundleContext;
    if (eClass.getInstanceClass() != null && eClass.getInstanceClass() != DynamicEObjectImpl.class) {
      interfaces.add(eClass.getInstanceClass().getName());
      theBundleContextToUse = FrameworkUtil.getBundle(eClass.getInstanceClass()).getBundleContext();
    }
    for (EClass superType : eClass.getEAllSuperTypes()) {
      if (superType.getInstanceClass() != null) {
        interfaces.add(eClass.getInstanceClass().getName());
      }
    }

    interfaces.add(EObject.class.getName());
    List<String> eClassUris = new ArrayList<>();
    eClassUris.add(EcoreUtil.getURI(eClass).toString());
    eClass.getEAllSuperTypes().stream().map(EcoreUtil::getURI).map(Object::toString).forEach(eClassUris::add);
    ServiceRegistration<?> serviceRegistration = theBundleContextToUse
        .registerService(interfaces.toArray(new String[0]), factory, FrameworkUtil.asDictionary(Map.of(idProp, id,
            Constants.SERVICE_SCOPE, Constants.SCOPE_PROTOTYPE, "eClassUris", eClassUris, "Rest", true)));
    resigtrations.add(serviceRegistration);
  }

  private void registerConfigurators(Metadata data) {
    data.services.forEach(this::registerConfigurator);
  }

  private void registerConfigurator(EObject ePackage, List<ServiceRegistration<?>> resigtrations) {
    DynamicEPackageConfigurator configurator = new DynamicEPackageConfigurator((EPackage) ePackage);
    ServiceRegistration<EPackageConfigurator> serviceRegistration = bundleContext
        .registerService(EPackageConfigurator.class, configurator, getServiceProperties((EPackage) ePackage));
    resigtrations.add(serviceRegistration);
  }

  private void registerEPackage(Metadata data) {
    data.services.forEach(this::registerEPackage);
  }

  private void registerEPackage(EObject ePackage, List<ServiceRegistration<?>> resigtrations) {
    ServiceRegistration<EPackage> serviceRegistration = bundleContext.registerService(EPackage.class,
        (EPackage) ePackage, getServiceProperties((EPackage) ePackage));
    resigtrations.add(serviceRegistration);
  }

  private Dictionary<String, String> getServiceProperties(EPackage ePackage) {
    EPackageRegistryImpl.INSTANCE.put(ePackage.getNsURI(), ePackage);
    Dictionary<String, String> serviceProperties = new Hashtable<>();
    String nsUri = ePackage.getNsURI();
    serviceProperties.put(EMFNamespaces.EMF_MODEL_NAME, ePackage.getName());
    serviceProperties.put(EMFNamespaces.EMF_MODEL_NSURI, nsUri);
    serviceProperties.put(EMFNamespaces.EMF_MODEL_REGISTRATION, EMFNamespaces.MODEL_REGISTRATION_DYNAMIC);
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

  /**
   * @param metadata
   * @param eSubpackages
   */
  private void addSubPackages(Metadata metadata, EList<EPackage> eSubpackages) {
    for (EPackage ePackage : eSubpackages) {
      metadata.services.put(ePackage, new ArrayList<>());
      addSubPackages(metadata, ePackage.getESubpackages());
    }
  }

  private void handleRemove(List<String> toRemove) {
    for (String remove : toRemove) {
      Metadata metadata = originalToNsUri.remove(remove);
      if (metadata != null) {
        metadata.services.forEach((k, v) -> {
          v.forEach(ServiceRegistration::unregister);
          if (k instanceof EPackage ePackage) {
            EPackageRegistryImpl.INSTANCE.remove(ePackage.getNsURI());
          }
        });
        metadata.resource.unload();
        resourceSet.getResources().remove(metadata.resource);
      } else if (qvtConfigs.containsKey(remove)) {
        Configuration removed = qvtConfigs.remove(remove);
        try {
          removed.delete();
        } catch (IOException e) {
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.daanse.io.fs.watcher.api.FileSystemWatcherListener#handleBasePath
   * (java.nio.file.Path)
   */
  @Override
  public void handleBasePath(Path basePath) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.daanse.io.fs.watcher.api.FileSystemWatcherListener#
   * handleInitialPaths(java.util.List)
   */
  @Override
  public void handleInitialPaths(List<Path> paths) {
    List<String> toAdd = paths.stream().map(this::cleanUpPath).toList();
    scheduleDelaied(toAdd);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.daanse.io.fs.watcher.api.FileSystemWatcherListener#
   * handlePathEvent(java.nio.file.Path, java.nio.file.WatchEvent.Kind)
   */
  @Override
  public void handlePathEvent(Path path, Kind<Path> kind) {
    String pathString = cleanUpPath(path);
    if (StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)) {
      handleRemove(List.of(pathString));
      scheduleDelaied(pathString);
    } else if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
      scheduleDelaied(pathString);
    } else if (StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
      handleRemove(List.of(pathString));
    }
  }

  public String cleanUpPath(Path path) {
    return path.toAbsolutePath().normalize().toUri().toString();
  }

  private List<String> uris = new ArrayList<>();

  Timer timer = new Timer();
  TimerTask task = null;

  private void scheduleDelaied(List<String> toHandle) {
    lock.lock();
    try {
      uris.addAll(toHandle);
      if (task != null) {
        task.cancel();
      }
      task = new DelaiedTimerTask(this::loadDelaied);
      timer.schedule(task, 1000);
    } finally {
      lock.unlock();
    }
  }

  private void scheduleDelaied(String uri) {
    lock.lock();
    try {
      uris.add(uri);
      if (task != null) {
        task.cancel();
      }
      task = new DelaiedTimerTask(this::loadDelaied);
      timer.schedule(task, 1000);
    } finally {
      lock.unlock();
    }
  }

  private static final class DelaiedTimerTask extends TimerTask {

    private Runnable runnable;

    public DelaiedTimerTask(Runnable runnable) {
      this.runnable = runnable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.TimerTask#run()
     */
    @Override
    public void run() {
      runnable.run();
    }

  }

  private void loadDelaied() {
    lock.lock();
    try {
      loadResources(uris);
      uris.clear();
    } finally {
      lock.unlock();
    }
  }

}
