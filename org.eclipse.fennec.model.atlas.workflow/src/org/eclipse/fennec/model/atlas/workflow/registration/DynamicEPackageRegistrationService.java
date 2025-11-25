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
 *      Mark Hoffmann - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.workflow.registration;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.gecko.emf.osgi.configurator.EPackageConfigurator;
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
 * Service for dynamic registration and unregistration of EPackages in the OSGi EMF registry.
 * 
 * <p>This service enables post-release actions to register released EPackages so they become
 * available to the EMF ecosystem. It follows the same pattern as static model configurators
 * but works dynamically with any EPackage instance.</p>
 * 
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Dynamic registration of EPackages from workflow releases</li>
 * <li>Reversible registration (can unregister EPackages)</li>
 * <li>Concurrent-safe operations for multiple EPackages</li>
 * <li>Proper OSGi service lifecycle management</li>
 * <li>Automatic cleanup on service deactivation</li>
 * </ul>
 * 
 * <p><strong>Usage Pattern:</strong></p>
 * <pre>{@code
 * // After object release in workflow
 * EPackage releasedEPackage = loadReleasedEPackage(objectId);
 * registrationService.registerEPackage(releasedEPackage, "sensors", "1.0");
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
    
    // Thread-safe storage for registered EPackages and their service registrations
    private final Map<String, RegisteredEPackage> registeredEPackages = new ConcurrentHashMap<>();
    
    // Track EPackages waiting for ResourceSet availability to send configuration events
    private final Map<String, String> pendingConfigurationEvents = new ConcurrentHashMap<>();
    
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
                          ServiceRegistration<?> ePackageRegistration,
                          ServiceRegistration<?> eFactoryRegistration,
                          ServiceRegistration<?> conditionRegistration,
                          String modelName) {
            this.configuratorRegistration = configuratorRegistration;
            this.ePackageRegistration = ePackageRegistration;
            this.eFactoryRegistration = eFactoryRegistration;
            this.conditionRegistration = conditionRegistration;
            this.modelName = modelName;
        }
        
        void unregisterAll() {
            try {
                if (conditionRegistration != null) conditionRegistration.unregister();
                if (eFactoryRegistration != null) eFactoryRegistration.unregister();
                if (ePackageRegistration != null) ePackageRegistration.unregister();
                if (configuratorRegistration != null) configuratorRegistration.unregister();
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
        
        // Unregister all registered EPackages
        registeredEPackages.values().forEach(RegisteredEPackage::unregisterAll);
        registeredEPackages.clear();
        
        this.bundleContext = null;
    }
    
    /**
     * Registers an EPackage in the OSGi EMF registry.
     * 
     * <p>This method creates and registers all necessary services for the EPackage:</p>
     * <ul>
     * <li>EPackageConfigurator service</li>
     * <li>EPackage service</li>
     * <li>EFactory service</li>
     * <li>Condition service</li>
     * </ul>
     * 
     * @param ePackage the EPackage to register (must not be null)
     * @param fileExtension the file extension for this model (optional, defaults to "model")
     * @param version the version of the model (optional, defaults to "1.0")
     * @return true if registration was successful, false if already registered or failed
     * @throws IllegalArgumentException if ePackage is null
     * @throws IllegalStateException if service is not active
     */
    public boolean registerEPackage(EPackage ePackage, String fileExtension, String version) {
        if (ePackage == null) {
            throw new IllegalArgumentException("EPackage cannot be null");
        }
        
        if (bundleContext == null) {
            throw new IllegalStateException("Service is not active");
        }
        
        String nsURI = ePackage.getNsURI();
        if (nsURI == null || nsURI.trim().isEmpty()) {
            logger.warning("Cannot register EPackage with null or empty namespace URI");
            return false;
        }
        
        // Check if already registered
        if (registeredEPackages.containsKey(nsURI)) {
            logger.info("EPackage already registered: " + nsURI);
            return false;
        }
        
        try {
            logger.info("Registering EPackage: " + nsURI + " (name=" + ePackage.getName() + ")");
            
            // Create configurator
            DynamicEPackageConfigurator configurator = new DynamicEPackageConfigurator(ePackage, fileExtension, version);
            
            // Track for pending configuration event when ResourceSet becomes available
            String modelName = ePackage.getName();
            
            RegisteredEPackage registered= null;
//            if (modelName.equals("airquality")) {
//            	initializer.doRegisterModel();
//            	registered = new RegisteredEPackage(null, null, null, null, modelName);
//            } else {
            	// Register all services
            	ServiceRegistration<EPackageConfigurator> configuratorReg = registerEPackageConfigurator(configurator);
            	ServiceRegistration<?> ePackageReg = registerEPackageService(ePackage, configurator);
            	ServiceRegistration<?> eFactoryReg = registerEFactoryService(ePackage, configurator);
            	ServiceRegistration<?> conditionReg = registerConditionService(configurator);
            	
            	// Store registration info
            	registered = new RegisteredEPackage(configuratorReg, ePackageReg, eFactoryReg, conditionReg, modelName);
            	
//            }
            // Store registration info
            registeredEPackages.put(nsURI, registered);
            
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
        }
    }
    
    /**
     * Registers an EPackage with default file extension and version.
     * 
     * @param ePackage the EPackage to register
     * @return true if registration was successful, false if already registered or failed
     */
    public boolean registerEPackage(EPackage ePackage) {
        return registerEPackage(ePackage, null, null);
    }
    
    /**
     * Unregisters an EPackage from the OSGi EMF registry.
     * 
     * @param namespaceURI the namespace URI of the EPackage to unregister
     * @return true if unregistration was successful, false if not registered or failed
     * @throws IllegalArgumentException if namespaceURI is null or empty
     */
    public boolean unregisterEPackage(String namespaceURI) {
        if (namespaceURI == null || namespaceURI.trim().isEmpty()) {
            throw new IllegalArgumentException("Namespace URI cannot be null or empty");
        }
        
        RegisteredEPackage registered = registeredEPackages.remove(namespaceURI);
        if (registered == null) {
            logger.warning("EPackage not registered: " + namespaceURI);
            return false;
        }
        try {
            logger.info("Unregistering EPackage: " + namespaceURI);
            
            // Send REMOVE configuration event before unregistering
            sendRemoveConfigurationEvent(namespaceURI, registered.modelName);
//            if (registered.modelName.equals("airquality")) {
//            	initializer.doUnregisterModel();
//            }
            registered.unregisterAll();
            
            // Remove from pending events if present
            pendingConfigurationEvents.remove(namespaceURI);
            
            logger.info("Successfully unregistered EPackage: " + namespaceURI);
            return true;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to unregister EPackage: " + namespaceURI, e);
            return false;
        }
    }
    
    /**
     * Checks if an EPackage is currently registered.
     * 
     * @param namespaceURI the namespace URI to check
     * @return true if the EPackage is registered, false otherwise
     */
    public boolean isRegistered(String namespaceURI) {
        return namespaceURI != null && registeredEPackages.containsKey(namespaceURI);
    }
    
    /**
     * Returns the number of currently registered EPackages.
     * 
     * @return the count of registered EPackages
     */
    public int getRegisteredCount() {
        return registeredEPackages.size();
    }
    
    /**
     * Returns the namespace URIs of all currently registered EPackages.
     * 
     * @return array of namespace URIs (never null, may be empty)
     */
    public String[] getRegisteredNamespaceURIs() {
        return registeredEPackages.keySet().toArray(new String[0]);
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
    
    private ServiceRegistration<EPackageConfigurator> registerEPackageConfigurator(DynamicEPackageConfigurator configurator) {
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.putAll(configurator.getServiceProperties());
        return bundleContext.registerService(EPackageConfigurator.class, configurator, properties);
    }
    
    private ServiceRegistration<?> registerEPackageService(EPackage ePackage, DynamicEPackageConfigurator configurator) {
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.putAll(configurator.getServiceProperties());
        String[] serviceClasses = new String[] {ePackage.getClass().getName(), EPackage.class.getName()};
        return bundleContext.registerService(serviceClasses, ePackage, properties);
    }
    
    private ServiceRegistration<?> registerEFactoryService(EPackage ePackage, DynamicEPackageConfigurator configurator) {
        EFactory eFactory = ePackage.getEFactoryInstance();
        if (eFactory == null) {
            logger.warning("No EFactory available for EPackage: " + ePackage.getNsURI());
            return null;
        }
        
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.putAll(configurator.getServiceProperties());
        String[] serviceClasses = new String[] {eFactory.getClass().getName(), EFactory.class.getName()};
        return bundleContext.registerService(serviceClasses, eFactory, properties);
    }
    
    private ServiceRegistration<?> registerConditionService(DynamicEPackageConfigurator configurator) {
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.putAll(configurator.getServiceProperties());
        properties.put(Condition.CONDITION_ID, configurator.getNamespaceURI());
        return bundleContext.registerService(Condition.class, Condition.INSTANCE, properties);
    }
    
    /**
     * Checks if the ResourceSet service properties match any of our registered EPackages
     * and sends ADD configuration events accordingly.
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
        
        logger.fine("Checking ResourceSet for configuration events. Pending events: " + pendingConfigurationEvents.size());
        
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
            
            String[] eventData = new String[] {configKey, configValue};
            
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
            
            String[] eventData = new String[] {configKey};
            
            logger.fine("Sending REMOVE configuration event: " + configKey);
            typedEventBus.deliver(topic, eventData);
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to send REMOVE configuration event for " + modelName, e);
        }
    }
    
    /**
     * Checks for existing ResourceSet services that match the given model name
     * and sends configuration events if found.
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
            Collection<ServiceReference<ResourceSet>> serviceRefs = bundleContext.getServiceReferences(ResourceSet.class, filterString);
            
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