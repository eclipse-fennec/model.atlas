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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.runtime.info.internal;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.model.atlas.emf.common.ecore.EPackageVersions;
import org.eclipse.fennec.model.atlas.mediatypes.api.MediatypeContributor;
import org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype;
import org.eclipse.fennec.model.atlas.runtime.info.AtlasRuntime;
import org.eclipse.fennec.model.atlas.runtime.info.AtlasRuntimeDTO;
import org.eclipse.fennec.model.atlas.runtime.info.InstanceDTO;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.runtime.ServiceComponentRuntime;

/**
 * Registers the Atlas runtime service and keeps its change count current.
 *
 * <p>
 * The service is registered by hand rather than by DS, because {@link Constants#SERVICE_CHANGECOUNT}
 * can only be moved through {@link ServiceRegistration#setProperties} and a DS-registered service
 * hands out no registration. Hence {@code service = {}}: without it DS would register a second copy
 * whose change count never moves.
 * </p>
 *
 * <p>
 * The component takes no configuration ({@link ConfigurationPolicy#IGNORE}), so no configuration
 * event can restart it and reset the count. None of the reported references is mandatory: the
 * service has to be there precisely when nothing came up.
 * </p>
 *
 * <p>
 * The count moves on every bind, update and unbind of a tracked service, and whenever the
 * component runtime's own {@code service.changecount} moves — which is how a component that fails
 * to come up, the failure half of the snapshot, reaches a watcher.
 * </p>
 *
 * <p>
 * Packages are bound as {@link ServiceReference}s: nothing reported needs the EPackage object, and
 * getting it would activate the component behind it.
 * </p>
 */
@Component(name = "AtlasRuntime", immediate = true, configurationPolicy = ConfigurationPolicy.IGNORE,
        property = AtlasRuntimeComponent.ATLAS_NAME + "=atlas", service = {})
public class AtlasRuntimeComponent implements AtlasRuntime {

    /** The component property naming this Atlas. */
    static final String ATLAS_NAME = "atlas.name";

    /** Set by the Atlas when it registers a package out of a registry, absent on a bundle's own. */
    private static final String DYNAMIC_REGISTRATION = "dynamic.registration";

    private final AtomicLong changeCount = new AtomicLong();

    /** Held by whichever thread is currently publishing; no monitor is held across the call out. */
    private final AtomicBoolean publishing = new AtomicBoolean();

    /** The last count handed to {@code setProperties}. */
    private volatile long published = -1;

    /** The live scope services, by {@code service.id}; asked for their state when a snapshot is taken. */
    private final Map<Long, ReadableScopeService<?>> scopes = new ConcurrentHashMap<>();

    /**
     * Every live EPackage service, by {@code service.id}. Not filtered on a scope: the globally
     * registered packages carry none and are the built-in {@code atlas} scope's content.
     */
    private final Map<Long, ServiceReference<EPackage>> packages = new ConcurrentHashMap<>();

    private final Map<Long, MediatypeContributor> contributors = new ConcurrentHashMap<>();

    private volatile SupportedMediatype supportedMediatype;

    private ServiceComponentRuntime componentRuntime;

    private BundleContext context;

    private String name;

    /**
     * Null before activation and after deactivation: DS binds dynamic references before
     * {@code activate} and unbinds them after {@code deactivate}, so those calls count but publish
     * nothing.
     */
    private volatile ServiceRegistration<AtlasRuntime> registration;

    @Activate
    void activate(BundleContext context, Map<String, Object> properties) {
        this.context = context;
        this.name = string(properties.get(ATLAS_NAME), "atlas");
        long count = changeCount.get();
        registration = context.registerService(AtlasRuntime.class, this, withChangeCount(count));
        published = count;
        // Anything bound while registering has moved the count past what was registered.
        publish();
    }

    @Deactivate
    void deactivate() {
        ServiceRegistration<AtlasRuntime> live = registration;
        registration = null;
        if (live != null) {
            try {
                live.unregister();
            } catch (IllegalStateException alreadyUnregistered) {
                // the framework got there first
            }
        }
    }

    /** Where the failure half of the snapshot comes from. */
    @Reference(name = "componentRuntime", updated = "updatedComponentRuntime")
    void setComponentRuntime(ServiceComponentRuntime componentRuntime) {
        this.componentRuntime = componentRuntime;
    }

    /** The component runtime moved its change count: some component changed state. */
    void updatedComponentRuntime(ServiceComponentRuntime componentRuntime, Map<String, Object> properties) {
        changed();
    }

    @Reference(name = "scope", service = ReadableScopeService.class, cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC)
    void addScope(ReadableScopeService<?> scope, Map<String, Object> properties) {
        scopes.put(serviceId(properties), scope);
        changed();
    }

    void updatedScope(ReadableScopeService<?> scope, Map<String, Object> properties) {
        scopes.put(serviceId(properties), scope);
        changed();
    }

    void removeScope(ReadableScopeService<?> scope, Map<String, Object> properties) {
        scopes.remove(serviceId(properties), scope);
        changed();
    }

    @Reference(name = "package", service = EPackage.class, cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC)
    void addPackage(ServiceReference<EPackage> reference) {
        packages.put(serviceId(reference), reference);
        changed();
    }

    void updatedPackage(ServiceReference<EPackage> reference) {
        // The held reference reads its properties live; only the count has to move.
        changed();
    }

    void removePackage(ServiceReference<EPackage> reference) {
        packages.remove(serviceId(reference), reference);
        changed();
    }

    @Reference(name = "contributor", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void addContributor(MediatypeContributor contributor, Map<String, Object> properties) {
        contributors.put(serviceId(properties), contributor);
        changed();
    }

    void updatedContributor(MediatypeContributor contributor, Map<String, Object> properties) {
        contributors.put(serviceId(properties), contributor);
        changed();
    }

    void removeContributor(MediatypeContributor contributor, Map<String, Object> properties) {
        contributors.remove(serviceId(properties), contributor);
        changed();
    }

    /** The media types the resource set announces; refreshed as codecs register content types. */
    @Reference(name = "mediatypes", cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    void setMediatypes(SupportedMediatype supportedMediatype, Map<String, Object> properties) {
        this.supportedMediatype = supportedMediatype;
        changed();
    }

    void updatedMediatypes(SupportedMediatype supportedMediatype, Map<String, Object> properties) {
        this.supportedMediatype = supportedMediatype;
        changed();
    }

    void unsetMediatypes(SupportedMediatype supportedMediatype) {
        if (this.supportedMediatype == supportedMediatype) {
            this.supportedMediatype = null;
        }
        changed();
    }

    @Override
    public AtlasRuntimeDTO snapshot() {
        return SnapshotBuilder.build(name, instance(), changeCount.get(), System.currentTimeMillis(), trackedScopes(),
                trackedPackages(), resourceSetTypes(), trackedContributors(),
                FailureScanner.scan(componentRuntime));
    }

    private InstanceDTO instance() {
        InstanceDTO instance = new InstanceDTO();
        instance.id = name;
        instance.implementationVersion = context == null ? null : context.getBundle().getVersion().toString();
        return instance;
    }

    private List<TrackedScope> trackedScopes() {
        List<TrackedScope> tracked = new ArrayList<>(scopes.size());
        scopes.forEach((serviceId, scope) -> tracked
                .add(new TrackedScope(serviceId, scope.getScopeInfo(), scope.isInheritingFromParentScope())));
        return tracked;
    }

    /** The live packages, read from service properties alone. */
    private List<TrackedPackage> trackedPackages() {
        List<TrackedPackage> tracked = new ArrayList<>(packages.size());
        for (ServiceReference<EPackage> reference : packages.values()) {
            String nsURI = string(reference.getProperty(EMFNamespaces.EMF_MODEL_NSURI), null);
            boolean stored = Boolean.TRUE.equals(reference.getProperty(DYNAMIC_REGISTRATION));
            tracked.add(new TrackedPackage(serviceId(reference),
                    string(reference.getProperty(EMFNamespaces.EMF_MODEL_SCOPE), ""),
                    string(reference.getProperty(WorkflowConstants.ATLAS_EPACKAGE_REGISTRATION_STAGE_PROPERTY), null),
                    nsURI, string(reference.getProperty(EMFNamespaces.EMF_NAME), null),
                    version(reference, stored, nsURI),
                    string(reference.getProperty(EMFNamespaces.EMF_MODEL_FINGERPRINT), null), stored));
        }
        return tracked;
    }

    /**
     * The package's version by the Atlas's rule. A package the Atlas registered carries the version
     * it computed. For any other, {@link EPackageVersions} is followed as far as a service reference
     * allows — a version-shaped nsURI segment, else the registering bundle's version without
     * qualifier. A foreign {@code emf.version} is not trusted: the Ecore package announces
     * {@code 2002.0.0}, the year out of its nsURI.
     *
     * @return the version, or {@code null} when nothing states one
     */
    private static String version(ServiceReference<?> reference, boolean stored, String nsURI) {
        if (stored) {
            return string(reference.getProperty(EMFNamespaces.EMF_MODEL_VERSION), null);
        }
        Version fromNsUri = EPackageVersions.fromNsUri(nsURI);
        if (fromNsUri != null) {
            return fromNsUri.toString();
        }
        Bundle shipping = reference.getBundle();
        if (shipping == null) {
            return null;
        }
        Version shipped = shipping.getVersion();
        return new Version(shipped.getMajor(), shipped.getMinor(), shipped.getMicro()).toString();
    }

    private List<TrackedContributor> trackedContributors() {
        List<TrackedContributor> tracked = new ArrayList<>(contributors.size());
        contributors.forEach((serviceId, contributor) -> {
            List<String> types = contributor.getMediaTypes();
            tracked.add(new TrackedContributor(serviceId, types == null ? List.of() : List.copyOf(types)));
        });
        return tracked;
    }

    private List<String> resourceSetTypes() {
        SupportedMediatype types = supportedMediatype;
        if (types == null) {
            return List.of();
        }
        List<String> supported = types.getSupportedMediaTypes();
        return supported == null ? List.of() : List.copyOf(supported);
    }

    private static long serviceId(Map<String, Object> properties) {
        return properties.get(Constants.SERVICE_ID) instanceof Long serviceId ? serviceId.longValue() : -1L;
    }

    private static long serviceId(ServiceReference<?> reference) {
        return reference.getProperty(Constants.SERVICE_ID) instanceof Long serviceId ? serviceId.longValue() : -1L;
    }

    private static String string(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private static Hashtable<String, Object> withChangeCount(long count) {
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put(Constants.SERVICE_CHANGECOUNT, count);
        return properties;
    }

    /** Something the snapshot would say differently: raise the count and publish it. */
    void changed() {
        changeCount.incrementAndGet();
        publish();
    }

    /**
     * Publishes the current count, without holding a monitor across {@code setProperties}.
     *
     * <p>
     * {@code setProperties} delivers its service event synchronously to every listener, and the
     * callbacks that call this arrive on SCR threads; holding a lock across that call is the classic
     * OSGi deadlock. Instead one thread at a time publishes, always the latest count, and a thread
     * that finds another one publishing leaves its increment to it: the publisher re-checks after
     * releasing, so no increment is left unpublished and the published number only moves forward.
     * </p>
     */
    private void publish() {
        do {
            if (!publishing.compareAndSet(false, true)) {
                return;
            }
            try {
                ServiceRegistration<AtlasRuntime> live = registration;
                if (live == null) {
                    return;
                }
                long count = changeCount.get();
                if (count != published) {
                    live.setProperties(withChangeCount(count));
                    published = count;
                }
            } catch (IllegalStateException alreadyUnregistered) {
                registration = null;
                return;
            } finally {
                publishing.set(false);
            }
        } while (registration != null && changeCount.get() != published);
    }
}
