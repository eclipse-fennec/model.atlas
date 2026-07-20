/*
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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.workflow.registration;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.condition.Condition;
import org.osgi.service.typedevent.TypedEventBus;

/**
 * Service for dynamic registration and unregistration of EPackages in the OSGi
 * EMF registry.
 * 
 * <p>
 * This service enables post-release actions to register released EPackages so
 * they become available to the EMF ecosystem. It follows the same pattern as
 * static model configurators but works dynamically with any EPackage instance.
 * </p>
 * 
 * <p>
 * <strong>Key Features:</strong>
 * </p>
 * <ul>
 * <li>Dynamic registration of EPackages from workflow releases</li>
 * <li>Reversible registration (can unregister EPackages)</li>
 * <li>Concurrent-safe operations for multiple EPackages</li>
 * <li>Proper OSGi service lifecycle management</li>
 * <li>Automatic cleanup on service deactivation</li>
 * </ul>
 * 
 * <p>
 * <strong>Usage Pattern:</strong>
 * </p>
 * 
 * <pre>{@code
 * // After object release in workflow
 * EPackage releasedEPackage = loadReleasedEPackage(objectId);
 * registrationService.registerEPackage(releasedEPackage, metadata);
 *
 * // Later, when EPackage should be removed
 * registrationService.unregisterEPackage(releasedEPackage.getNsURI());
 * }</pre>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
@Component(service = DynamicEPackageRegistrationService.class, immediate = true)
public class DynamicEPackageRegistrationService {

    private static final Logger logger = Logger.getLogger(DynamicEPackageRegistrationService.class.getName());

    private BundleContext bundleContext;

    @Reference
    private TypedEventBus typedEventBus;

//    @Reference
//    private ModelInitializer initializer;

    // Thread-safe storage for registered EPackages and their service registrations.
    // Keyed by scope+stage+nsURI (NOT nsURI alone): the same nsURI legitimately exists in
    // several workflow stages at once (e.g. a git schema present on multiple branches =
    // stages), each carrying its own content and its own emf.model.scope/atlas.stage
    // service properties. Keying by nsURI alone would drop every stage after the first.
    private final Map<RegistrationKey, RegisteredEPackage> registeredEPackages = new ConcurrentHashMap<>();

    // Track EPackages waiting for ResourceSet availability to send configuration
    // events (keyed by nsURI; the southbound mapping it feeds is stage-independent)
    private final Map<String, String> pendingConfigurationEvents = new ConcurrentHashMap<>();

    // Serializes register/unregister so concurrent reloads (unregister-then-register of the
    // same nsURI) do not interleave. The brief window in which the nsURI is unregistered is
    // accepted (low request frequency); the lock only prevents corruption from concurrency.
    private final ReentrantLock registrationLock = new ReentrantLock();

    /**
     * Composite registration key: an EPackage is identified by its workflow location
     * ({@code scope}, {@code stage}) <em>and</em> its {@code nsURI}, so the same nsURI can be
     * registered once per stage. {@code scope}/{@code stage} may be {@code null}.
     */
    private record RegistrationKey(String scope, String stage, String nsURI) {
    }

    /**
     * Container for all service registrations related to a single EPackage.
     */
    private static class RegisteredEPackage {
        final ServiceRegistration<EPackageConfigurator> configuratorRegistration;
        final ServiceRegistration<?> ePackageRegistration;
        final ServiceRegistration<?> eFactoryRegistration;
        final ServiceRegistration<?> conditionRegistration;
        final String modelName;

        RegisteredEPackage(ServiceRegistration<EPackageConfigurator> configuratorRegistration,
                ServiceRegistration<?> ePackageRegistration, ServiceRegistration<?> eFactoryRegistration,
                ServiceRegistration<?> conditionRegistration, String modelName) {
            this.configuratorRegistration = configuratorRegistration;
            this.ePackageRegistration = ePackageRegistration;
            this.eFactoryRegistration = eFactoryRegistration;
            this.conditionRegistration = conditionRegistration;
            this.modelName = modelName;
        }

        void unregisterAll() {
            try {
                if (conditionRegistration != null)
                    conditionRegistration.unregister();
                if (eFactoryRegistration != null)
                    eFactoryRegistration.unregister();
                if (ePackageRegistration != null)
                    ePackageRegistration.unregister();
                if (configuratorRegistration != null)
                    configuratorRegistration.unregister();
            } catch (IllegalStateException e) {
                // Service already unregistered - can happen during shutdown
                logger.log(Level.FINE, "Service already unregistered during cleanup", e);
            }
        }
    }

    @Activate
    public void activate(BundleContext context) {
        this.bundleContext = context;
        logger.info("Dynamic EPackage Registration Service activated");
    }

    @Deactivate
    public void deactivate() {
        logger.info("Deactivating Dynamic EPackage Registration Service - unregistering all EPackages");

        registrationLock.lock();
        try {
            // Unregister all registered EPackages
            registeredEPackages.values().forEach(RegisteredEPackage::unregisterAll);
            registeredEPackages.clear();

            this.bundleContext = null;
        } finally {
            registrationLock.unlock();
        }
    }

    /**
     * Registers an EPackage in the OSGi EMF registry.
     *
     * <p>
     * This method creates and registers all necessary services for the EPackage:
     * </p>
     * <ul>
     * <li>EPackageConfigurator service</li>
     * <li>EPackage service</li>
     * <li>EFactory service</li>
     * <li>Condition service</li>
     * </ul>
     *
     * <p>
     * File extension and version are read from {@code metadata} properties (keys
     * {@code file.extension} and {@code version}), with fallbacks derived from the
     * EPackage when absent. Workflow scope and stage are read directly from
     * {@code metadata} and surfaced as service properties so consumers can filter
     * registered EPackages by workflow location.
     * </p>
     *
     * <p>
     * Coupling note: this method intentionally takes the workflow-layer
     * {@link ObjectMetadata} type — it is only meant to be called from the
     * workflow stack and is not a generic EMF-OSGi utility.
     * </p>
     *
     * @param ePackage the EPackage to register (must not be null)
     * @param metadata the object metadata accompanying the EPackage (must not be
     *                 null; {@code scope} is expected to be non-null)
     * @return true if registration was successful, false if already registered or
     *         failed
     * @throws IllegalArgumentException if ePackage or metadata is null
     * @throws IllegalStateException    if service is not active
     */
    public boolean registerEPackage(EPackage ePackage, ObjectMetadata metadata) {
        if (ePackage == null) {
            throw new IllegalArgumentException("EPackage cannot be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("ObjectMetadata cannot be null");
        }

        if (bundleContext == null) {
            throw new IllegalStateException("Service is not active");
        }

        String nsURI = ePackage.getNsURI();
        if (nsURI == null || nsURI.trim().isEmpty()) {
            logger.warning("Cannot register EPackage with null or empty namespace URI");
            return false;
        }

        // Identify by scope+stage+nsURI, so the same nsURI in a different stage (e.g. a git
        // schema on another branch) is NOT treated as a duplicate.
        RegistrationKey key = new RegistrationKey(metadata.getScope(), metadata.getStage(), nsURI);

        registrationLock.lock();
        try {
            // Check if already registered for this exact scope/stage
            if (registeredEPackages.containsKey(key)) {
                logger.info("EPackage already registered for " + key);
                return false;
            }

            logger.info("Registering EPackage: " + nsURI + " (name=" + ePackage.getName() + ", scope="
                    + metadata.getScope() + ", stage=" + metadata.getStage() + ")");

            Resource eResource = ePackage.eResource();
            if(eResource.getResourceSet() != null) eResource.getResourceSet().getResources().remove(eResource);
            eResource.setURI(URI.createURI(ePackage.getNsURI()));

            String fileExtension = extractFileExtension(metadata, ePackage);
            String version = extractVersion(metadata, ePackage);

            // Create configurator
            DynamicEPackageConfigurator configurator = new DynamicEPackageConfigurator(ePackage, fileExtension, version,
                    metadata.getScope(), metadata.getStage());

            // Track for pending configuration event when ResourceSet becomes available
            String modelName = ePackage.getName();

            RegisteredEPackage registered = null;

            // Register all services
            ServiceRegistration<EPackageConfigurator> configuratorReg = registerEPackageConfigurator(configurator);
            ServiceRegistration<?> ePackageReg = registerEPackageService(ePackage, configurator);
            ServiceRegistration<?> eFactoryReg = registerEFactoryService(ePackage, configurator);
            ServiceRegistration<?> conditionReg = registerConditionService(configurator);

            // Store registration info
            registered = new RegisteredEPackage(configuratorReg, ePackageReg, eFactoryReg, conditionReg, modelName);
            registeredEPackages.put(key, registered);

            if (modelName != null) {
                pendingConfigurationEvents.put(nsURI, modelName);
                logger.fine("Added pending configuration event for model: " + modelName + " (nsURI: " + nsURI + ")");

                // Check if matching ResourceSet is already available
                checkForExistingResourceSets(nsURI, modelName);
            }

            logger.info("Successfully registered EPackage: " + nsURI);
            return true;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to register EPackage: " + nsURI, e);
            return false;
        } finally {
            registrationLock.unlock();
        }
    }

    /**
     * Unregisters an EPackage from the OSGi EMF registry for a specific workflow
     * location. Because registration is keyed by {@code scope+stage+nsURI}, the same nsURI
     * may remain registered for other stages after this call.
     *
     * @param scope        the workflow scope the EPackage was registered under (may be null)
     * @param stage        the workflow stage the EPackage was registered under (may be null)
     * @param namespaceURI the namespace URI of the EPackage to unregister
     * @return true if unregistration was successful, false if not registered or failed
     * @throws IllegalArgumentException if namespaceURI is null or empty
     */
    public boolean unregisterEPackage(String scope, String stage, String namespaceURI) {
        if (namespaceURI == null || namespaceURI.trim().isEmpty()) {
            throw new IllegalArgumentException("Namespace URI cannot be null or empty");
        }

        RegistrationKey key = new RegistrationKey(scope, stage, namespaceURI);

        registrationLock.lock();
        try {
            RegisteredEPackage registered = registeredEPackages.remove(key);
            if (registered == null) {
                logger.warning("EPackage not registered: " + key);
                return false;
            }
            try {
                logger.info("Unregistering EPackage: " + key);

                // Send REMOVE configuration event before unregistering
                sendRemoveConfigurationEvent(namespaceURI, registered.modelName);
                registered.unregisterAll();

                // Remove the pending event only if no other stage still holds this nsURI
                if (registeredEPackages.keySet().stream().noneMatch(k -> k.nsURI().equals(namespaceURI))) {
                    pendingConfigurationEvents.remove(namespaceURI);
                }

                logger.info("Successfully unregistered EPackage: " + key);
                return true;

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to unregister EPackage: " + key, e);
                return false;
            }
        } finally {
            registrationLock.unlock();
        }
    }

    /**
     * Checks if an EPackage is registered for a specific scope/stage.
     *
     * @return true if the EPackage is registered for that exact workflow location
     */
    public boolean isRegistered(String scope, String stage, String namespaceURI) {
        return namespaceURI != null
                && registeredEPackages.containsKey(new RegistrationKey(scope, stage, namespaceURI));
    }

    /**
     * Checks if an EPackage is currently registered in <em>any</em> scope/stage.
     *
     * @param namespaceURI the namespace URI to check
     * @return true if the EPackage is registered for at least one workflow location
     */
    public boolean isRegistered(String namespaceURI) {
        return namespaceURI != null
                && registeredEPackages.keySet().stream().anyMatch(k -> namespaceURI.equals(k.nsURI()));
    }

    /**
     * Returns the number of currently registered EPackages (one count per
     * scope/stage/nsURI registration).
     *
     * @return the count of registered EPackages
     */
    public int getRegisteredCount() {
        return registeredEPackages.size();
    }

    /**
     * Returns the distinct namespace URIs of all currently registered EPackages.
     *
     * @return array of namespace URIs (never null, may be empty)
     */
    public String[] getRegisteredNamespaceURIs() {
        return registeredEPackages.keySet().stream().map(RegistrationKey::nsURI).distinct().toArray(String[]::new);
    }

    @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL, updated = "modifiedResourceSet")
    public void setResourceSet(ServiceReference<ResourceSet> resourceSet) {
        checkResourceSetForConfigurationEvents(resourceSet);
    }

    public void modifiedResourceSet(ServiceReference<ResourceSet> resourceSet) {
        checkResourceSetForConfigurationEvents(resourceSet);
    }

    public void unsetResourceSet(ServiceReference<ResourceSet> resourceSet) {
        // No configuration events needed on ResourceSet removal
        logger.fine("ResourceSet service removed: " + resourceSet);
    }

    // Private helper methods for service registration

    private ServiceRegistration<EPackageConfigurator> registerEPackageConfigurator(
            DynamicEPackageConfigurator configurator) {
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.putAll(configurator.getServiceProperties());
        return bundleContext.registerService(EPackageConfigurator.class, configurator, properties);
    }

    private ServiceRegistration<?> registerEPackageService(EPackage ePackage,
            DynamicEPackageConfigurator configurator) {
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.putAll(configurator.getServiceProperties());
        String[] serviceClasses = new String[] { ePackage.getClass().getName(), EPackage.class.getName() };
        return bundleContext.registerService(serviceClasses, ePackage, properties);
    }

    private ServiceRegistration<?> registerEFactoryService(EPackage ePackage,
            DynamicEPackageConfigurator configurator) {
        EFactory eFactory = ePackage.getEFactoryInstance();
        if (eFactory == null) {
            logger.warning("No EFactory available for EPackage: " + ePackage.getNsURI());
            return null;
        }

        Hashtable<String, Object> properties = new Hashtable<>();
        properties.putAll(configurator.getServiceProperties());
        String[] serviceClasses = new String[] { eFactory.getClass().getName(), EFactory.class.getName() };
        return bundleContext.registerService(serviceClasses, eFactory, properties);
    }

    private ServiceRegistration<?> registerConditionService(DynamicEPackageConfigurator configurator) {
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.putAll(configurator.getServiceProperties());
        properties.put(Condition.CONDITION_ID, configurator.getNamespaceURI());
        return bundleContext.registerService(Condition.class, Condition.INSTANCE, properties);
    }

    /**
     * Checks if the ResourceSet service properties match any of our registered
     * EPackages and sends ADD configuration events accordingly.
     */
    private void checkResourceSetForConfigurationEvents(ServiceReference<ResourceSet> resourceSetRef) {
        if (typedEventBus == null) {
            logger.fine("TypedEventBus not available - skipping configuration events");
            return;
        }

        if (bundleContext == null) {
            logger.fine("BundleContext not available - skipping configuration events");
            return;
        }

        logger.fine(
                "Checking ResourceSet for configuration events. Pending events: " + pendingConfigurationEvents.size());

        // Check if we have any pending configuration events for registered EPackages
        for (Map.Entry<String, String> entry : pendingConfigurationEvents.entrySet()) {
            String nsURI = entry.getKey();
            String expectedModelName = entry.getValue();

            try {
                // Create filter to match ResourceSet with the expected model name
                String filterString = "(emf.name=" + expectedModelName + ")";
                Filter filter = FrameworkUtil.createFilter(filterString);

                // Test if the ResourceSet service reference matches our filter
                if (filter.match(resourceSetRef)) {
                    // Send ADD configuration event
                    sendAddConfigurationEvent(nsURI, expectedModelName);
                    // Remove from pending since event has been sent
                    pendingConfigurationEvents.remove(nsURI);
                    break;
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to create or test filter for model: " + expectedModelName, e);
            }
        }
    }

    /**
     * Sends ADD configuration event for southbound mapping.
     */
    private void sendAddConfigurationEvent(String nsURI, String modelName) {
        if (typedEventBus == null) {
            logger.warning("Cannot send configuration event - TypedEventBus not available");
            return;
        }

        try {
            String topic = "configuration/ADD/SouthboundMappingService";
            String configKey = "sthbnd.mapping.codec.typeMap." + modelName;
            String configValue = nsURI + "#//" + modelName + "Sensor";

            String[] eventData = new String[] { configKey, configValue };

            logger.fine("Sending ADD configuration event: " + configKey + " = " + configValue);
            typedEventBus.deliver(topic, eventData);

        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to send ADD configuration event for " + modelName, e);
        }
    }

    /**
     * Sends REMOVE configuration event for southbound mapping.
     */
    private void sendRemoveConfigurationEvent(String nsURI, String modelName) {
        if (typedEventBus == null) {
            logger.warning("Cannot send configuration event - TypedEventBus not available");
            return;
        }

        if (modelName == null) {
            logger.warning("Cannot send REMOVE configuration event - model name is null for nsURI: " + nsURI);
            return;
        }

        try {
            String topic = "configuration/REMOVE/SouthboundMappingService";
            String configKey = "sthbnd.mapping.codec.typeMap." + modelName;

            String[] eventData = new String[] { configKey };

            logger.fine("Sending REMOVE configuration event: " + configKey);
            typedEventBus.deliver(topic, eventData);

        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to send REMOVE configuration event for " + modelName, e);
        }
    }

    private static String extractFileExtension(ObjectMetadata metadata, EPackage ePackage) {
        if (metadata.getProperties() != null) {
            Object fileExt = metadata.getProperties().get("file.extension");
            if (fileExt instanceof String s && !s.isBlank()) {
                return s.startsWith(".") ? s.substring(1) : s;
            }
        }
        String name = ePackage.getName();
        return (name != null && !name.isEmpty()) ? name.toLowerCase() : "ecore";
    }

    private static String extractVersion(ObjectMetadata metadata, EPackage ePackage) {
        if (metadata.getProperties() != null) {
            Object version = metadata.getProperties().get("version");
            if (version instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return "1.0";
    }

    /**
     * Checks for existing ResourceSet services that match the given model name and
     * sends configuration events if found.
     */
    private void checkForExistingResourceSets(String nsURI, String modelName) {
        if (bundleContext == null) {
            logger.fine("BundleContext not available - cannot check for existing ResourceSets");
            return;
        }

        try {
            // Create filter to find ResourceSet services with matching model name
            String filterString = "(emf.name=" + modelName + ")";

            // Get all ResourceSet service references
            Collection<ServiceReference<ResourceSet>> serviceRefs = bundleContext
                    .getServiceReferences(ResourceSet.class, filterString);

            if (serviceRefs != null && serviceRefs.size() > 0) {
                // Send configuration event for the first matching ResourceSet
                sendAddConfigurationEvent(nsURI, modelName);
                // Remove from pending since event has been sent
                pendingConfigurationEvents.remove(nsURI);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to check for existing ResourceSets for model: " + modelName, e);
        }
    }
}