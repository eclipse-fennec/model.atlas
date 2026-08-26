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
import java.util.Objects;
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
import org.eclipse.fennec.emf.osgi.fingerprint.FingerprintService;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
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
 * // Later, when EPackage should be removed from that workflow location
 * registrationService.unregisterEPackage(scope, stage, releasedEPackage.getNsURI());
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
    
    @Reference
    FingerprintService fingerprintService;

    // Thread-safe storage for registered EPackages and their service registrations.
    // Keyed by scope+stage+nsURI+fingerprint: the same nsURI legitimately exists in
    // several workflow stages at once (e.g. a git schema present on multiple branches =
    // stages), each carrying its own content and its own emf.model.scope/atlas.stage
    // service properties. The fingerprint component acts WITHIN one location only:
    // same content re-registered is an idempotent no-op, changed content replaces the
    // stale registration. It never creates sharing across locations — unregistration is
    // always addressed by (scope, stage, nsURI), so sibling stages holding identical
    // content (same fingerprint) are unaffected.
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
     * ({@code scope}, {@code stage}), its {@code nsURI} <em>and</em> its fingerprint.
     * {@code scope}/{@code stage} may be {@code null}.
     */
    private record RegistrationKey(String scope, String stage, String nsURI, String fingerprint) {
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
     * <p>
     * Version semantics: the content fingerprint is computed here (never taken
     * from {@code metadata} — "computed, never trusted") and published as the
     * {@code emf.fingerprint} service property. If this exact location
     * (scope, stage, nsURI) already holds the identical content, the call is an
     * idempotent no-op; if it holds a <em>different</em> content version, the
     * stale registration is replaced by this one.
     * </p>
     *
     * @param ePackage the EPackage to register (must not be null)
     * @param metadata the object metadata accompanying the EPackage (must not be
     *                 null; {@code scope} is expected to be non-null)
     * @return true if registration was successful (including replacement of a
     *         stale content version), false if the identical content is already
     *         registered for this location or registration failed
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
        // schema on another branch) is NOT treated as a duplicate. The fingerprint is
        // ALWAYS computed here, never adopted from metadata ("computed, never trusted").
        String fp = fingerprintService.fingerprint(ePackage);
        RegistrationKey key = new RegistrationKey(metadata.getScope(), metadata.getStage(), nsURI, fp);

        // Cheap drift detector: a stored metadata fingerprint that disagrees with the
        // freshly computed one means stored content and registered instance diverged.
        if (metadata.getFingerprint() != null && !fp.equals(metadata.getFingerprint())) {
            logger.warning("Fingerprint drift for " + nsURI + " (scope=" + metadata.getScope() + ", stage="
                    + metadata.getStage() + "): metadata carries " + metadata.getFingerprint()
                    + " but the loaded EPackage computes to " + fp);
        }

        registrationLock.lock();
        try {
            // Identical content already registered for this exact location: idempotent no-op
            if (registeredEPackages.containsKey(key)) {
                logger.info("EPackage already registered for " + key);
                return false;
            }

            // Same location under a DIFFERENT fingerprint: the content changed — replace the
            // stale registration instead of silently keeping outdated services alive.
            RegistrationKey staleKey = findKeyForLocation(metadata.getScope(), metadata.getStage(), nsURI);
            if (staleKey != null) {
                logger.info("Replacing EPackage registration for " + staleKey + " with new fingerprint " + fp);
                RegisteredEPackage stale = registeredEPackages.remove(staleKey);
                stale.unregisterAll();
                // No REMOVE configuration event: the same model re-registers immediately below.
            }

            logger.info("Registering EPackage: " + nsURI + " (name=" + ePackage.getName() + ", scope="
                    + metadata.getScope() + ", stage=" + metadata.getStage() + ", fingerprint=" + fp + ")");

            Resource eResource = ePackage.eResource();
            if(eResource.getResourceSet() != null) eResource.getResourceSet().getResources().remove(eResource);
            eResource.setURI(URI.createURI(ePackage.getNsURI()));

            String fileExtension = extractFileExtension(metadata, ePackage);
            String version = extractVersion(metadata, ePackage);
            boolean dcatPublish = extractDcatFlag(metadata);

            // Create configurator
            DynamicEPackageConfigurator configurator = new DynamicEPackageConfigurator(ePackage, fileExtension, version,
                    metadata.getScope(), metadata.getStage(), fp, dcatPublish);

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
     * location, regardless of which content version (fingerprint) is currently
     * registered there. Because registration is keyed per location, the same nsURI
     * may remain registered for other stages after this call — including sibling
     * stages holding identical content under the same fingerprint.
     *
     * @param scope        the workflow scope the EPackage was registered under (may be null)
     * @param stage        the workflow stage the EPackage was registered under (may be null)
     * @param namespaceURI the namespace URI of the EPackage to unregister
     * @return true if unregistration was successful, false if not registered or failed
     * @throws IllegalArgumentException if namespaceURI is null or empty
     */
    public boolean unregisterEPackage(String scope, String stage, String namespaceURI) {
        return unregisterEPackage(scope, stage, namespaceURI, null);
    }

    /**
     * Unregisters an EPackage from the OSGi EMF registry for a specific workflow
     * location and content version.
     *
     * @param scope        the workflow scope the EPackage was registered under (may be null)
     * @param stage        the workflow stage the EPackage was registered under (may be null)
     * @param namespaceURI the namespace URI of the EPackage to unregister
     * @param fingerprint  the content fingerprint the location is expected to hold;
     *                     {@code null} matches whatever version is registered there
     *                     (stage EXIT events do not know the fingerprint)
     * @return true if unregistration was successful, false if not registered or failed
     * @throws IllegalArgumentException if namespaceURI is null or empty
     */
    public boolean unregisterEPackage(String scope, String stage, String namespaceURI, String fingerprint) {
        if (namespaceURI == null || namespaceURI.trim().isEmpty()) {
            throw new IllegalArgumentException("Namespace URI cannot be null or empty");
        }

        registrationLock.lock();
        try {
            RegistrationKey key = fingerprint != null ? new RegistrationKey(scope, stage, namespaceURI, fingerprint)
                    : findKeyForLocation(scope, stage, namespaceURI);
            RegisteredEPackage registered = key != null ? registeredEPackages.remove(key) : null;
            if (registered == null) {
                logger.warning("EPackage not registered: " + (key != null ? key.toString()
                        : scope + "/" + stage + "/" + namespaceURI));
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
     * Checks if an EPackage is registered for a specific scope/stage, in whatever
     * content version (fingerprint) that location currently holds.
     *
     * @return true if the EPackage is registered for that exact workflow location
     */
    public boolean isRegistered(String scope, String stage, String namespaceURI) {
        return namespaceURI != null && findKeyForLocation(scope, stage, namespaceURI) != null;
    }

    /**
     * Finds the registration key currently held for a workflow location
     * ({@code scope}, {@code stage}, {@code nsURI}), ignoring the fingerprint
     * component. Thanks to the replace-on-changed-content semantics of
     * {@link #registerEPackage(EPackage, ObjectMetadata)} a location holds at most
     * one registration at a time.
     *
     * @return the key registered for that location, or {@code null} if none
     */
    private RegistrationKey findKeyForLocation(String scope, String stage, String namespaceURI) {
        return registeredEPackages.keySet().stream()
                .filter(k -> Objects.equals(k.scope(), scope) && Objects.equals(k.stage(), stage)
                        && Objects.equals(k.nsURI(), namespaceURI))
                .findFirst().orElse(null);
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

    /**
     * Reads the DCAT publication assertion out of the object's metadata so it can be projected
     * onto the service properties.
     *
     * <p>
     * {@code ObjectMetadata.properties} is typed {@code String -> EJavaObject}, so both
     * {@code Boolean.TRUE} and the string {@code "true"} are storable and only one of them is
     * what the schema upload path writes. Read both, and treat anything else — including a value
     * of the wrong type entirely — as "not asserted" rather than as an error: a nonsense flag
     * must not stop a package from being registered and served.
     * </p>
     *
     * @param metadata the object's metadata; never {@code null} here
     * @return {@code true} only if the metadata asserts publication
     */
    private boolean extractDcatFlag(ObjectMetadata metadata) {
        Object flag = metadata.getProperties() == null ? null
                : metadata.getProperties().get(WorkflowConstants.DCAT_PUBLISH_METADATA_PROPERTY);
        if (flag instanceof Boolean bool) {
            return bool.booleanValue();
        }
        return flag instanceof String text && Boolean.parseBoolean(text);
    }

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