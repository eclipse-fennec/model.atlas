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
 * <strong>It registers the service itself rather than letting DS do it</strong>, and that is the
 * one thing about this component that is not a preference.
 * {@link Constants#SERVICE_CHANGECOUNT} can only be moved through
 * {@link ServiceRegistration#setProperties}, and a DS-declared service hands out no registration to
 * call it on: its properties are fixed when it is registered and move only when its configuration
 * does. So the component activates, registers {@link AtlasRuntime} itself and keeps the handle,
 * which is what {@link #changed()} sets the count on.
 * </p>
 *
 * <p>
 * <strong>It takes no configuration</strong>, and that is a lifecycle decision rather than a lack
 * of knobs. This component has no {@code modified} method, so under any configuration-driven
 * policy a configuration arriving, changing or being deleted deactivates it and activates it
 * again — which here means the service is unregistered and a new one registered in its place,
 * with the change count back at zero and every bound watcher silently rebound to a number that
 * has stopped meaning what it meant. A service whose whole contract is "always there, and the
 * count only ever moves forward" cannot be restartable by a dropped config file. The cost is the
 * usual one of {@link ConfigurationPolicy#IGNORE}: a configuration for this component's name is
 * ignored without a word, so there is deliberately no PID to write one against.
 * </p>
 *
 * <p>
 * <strong>Everything it reports arrives as a reference, and none of those references is
 * mandatory.</strong> A runtime service that needed a scope in order to start would be absent in
 * exactly the situation it exists to report — an Atlas where nothing came up — and an integration
 * test waiting for it would wait forever rather than being told. They are bound by method rather
 * than by field: the bind call is where the change count moves, and a field gives no callback to
 * move it in. The component runtime is the exception on both counts — it is the framework's own
 * service, there whenever this component is, and nothing about it is reported — so it is a plain
 * mandatory field.
 * </p>
 *
 * <p>
 * <strong>Packages are bound as {@link ServiceReference}s, not as services.</strong> Nothing
 * reported about a package needs the EPackage object, and asking for one activates the component
 * behind it — a snapshot that activates things is not the read-only observation it claims to be,
 * and the telemetry gauges call it on every collection. Binding the reference gives DS's own
 * arrival, property-change and departure callbacks over the service properties without ever
 * dereferencing them.
 * </p>
 */
// service = {} is load-bearing: the class implements AtlasRuntime, so without it DS would
// register a second copy of this service beside the one the publisher registers — one with a
// change count that moves and one with properties that never do, and a consumer binding the
// wrong one would be told nothing for the lifetime of the framework.
@Component(name = "AtlasRuntime", immediate = true, configurationPolicy = ConfigurationPolicy.IGNORE,
        property = AtlasRuntimeComponent.ATLAS_NAME + "=atlas", service = {})
public class AtlasRuntimeComponent implements AtlasRuntime {

    /** Where the failure half of the snapshot comes from: what is configured and did not come up. */
    @Reference
    ServiceComponentRuntime componentRuntime;

    /**
     * The component property naming this Atlas, declared with its default on the component itself.
     *
     * <p>
     * A component property and not a configuration one, for the reason in the class comment. One
     * framework holds one Atlas, so this names the instance for a reader of the snapshot rather
     * than telling two of them apart.
     * </p>
     */
    static final String ATLAS_NAME = "atlas.name";

    /** Set by the Atlas when it registers a package out of a registry, absent on a bundle's own. */
    private static final String DYNAMIC_REGISTRATION = "dynamic.registration";

    private final AtomicLong changeCount = new AtomicLong();

    /**
     * The live scope services, by {@code service.id}.
     *
     * <p>
     * The service and not a copy of its descriptor: a snapshot asks each scope what it holds at the
     * moment it is taken, so a registry or stage that changed after the bind is still reported
     * correctly.
     * </p>
     */
    private final Map<Long, ReadableScopeService<?>> scopes = new ConcurrentHashMap<>();

    /**
     * The live EPackage services, by {@code service.id}, as references.
     *
     * <p>
     * Not filtered on {@code emf.model.scope}, and that is deliberate: a package the Atlas
     * registered out of a scope registry carries a scope, but a <em>globally</em> registered one
     * does not — {@code InitialModelLoader} sets only name, nsURI and registration kind on the
     * top-level models. The built-in {@code atlas} scope's content is defined as exactly those
     * global registrations, so filtering on a scope property would make the atlas scope
     * permanently empty however many models were loaded.
     * </p>
     */
    private final Map<Long, ServiceReference<EPackage>> packages = new ConcurrentHashMap<>();

    private final Map<Long, MediatypeContributor> contributors = new ConcurrentHashMap<>();

    private volatile SupportedMediatype supportedMediatype;

    private BundleContext context;

    private String name;

    /**
     * The handle the change count is published on.
     *
     * <p>
     * Null before activation and after deactivation, and neither is an edge case here: DS binds a
     * dynamic reference before it calls {@code activate} and unbinds it after {@code deactivate}
     * (112.5.6 and 112.5.11), so the first and the last calls to {@link #changed()} arrive with
     * nothing to publish onto.
     * </p>
     */
    private volatile ServiceRegistration<AtlasRuntime> runtimeServiceRegistration;

    /**
     * Registers the service with the count as it already stands.
     *
     * <p>
     * Not with zero: every reference bound on the way in has already raised it, and a first
     * published value that ignored them would be a number a watcher had to see move twice before
     * it meant anything. Synchronized against {@link #changed()}, so a service arriving on another
     * thread while this runs is either counted here or published there, never neither.
     * </p>
     */
    @Activate
    synchronized void activate(BundleContext context, Map<String, Object> properties) {
        this.context = context;
        this.name = string(properties.get(ATLAS_NAME), "atlas");
        runtimeServiceRegistration = context.registerService(AtlasRuntime.class, this,
                withChangeCount(changeCount.get()));
    }

    @Deactivate
    synchronized void deactivate() {
        ServiceRegistration<AtlasRuntime> live = runtimeServiceRegistration;
        runtimeServiceRegistration = null;
        if (live != null) {
            live.unregister();
        }
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

    /**
     * Every EPackage service, scoped or not, bound without being asked for.
     *
     * <p>
     * Taking the {@link ServiceReference} rather than the service is the whole point of this
     * reference — see the class comment. Its properties are read live when a snapshot is taken, so
     * the {@code updated} callback exists to move the change count rather than to re-read
     * anything.
     * </p>
     */
    @Reference(name = "package", service = EPackage.class, cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC)
    void addPackage(ServiceReference<EPackage> reference) {
        packages.put(serviceId(reference), reference);
        changed();
    }

    void updatedPackage(ServiceReference<EPackage> reference) {
        // Nothing to store: the reference held is the one whose properties changed, and a
        // reference reads them live. What a watcher needs is the number moving.
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

    /**
     * The media types the resource set announces.
     *
     * <p>
     * The {@code updated} callback is the reason this one is a method too: the list is not fixed,
     * and the service refreshes its properties as codecs register content types.
     * </p>
     */
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
     * The package's version by the Atlas's rule, not by whoever registered it.
     *
     * <p>
     * A package the Atlas registered carries a version it computed itself, so its property is the
     * answer. Any other registration's {@code emf.version} is a foreign claim made by a looser
     * rule: the Ecore package arrives announcing {@code 2002.0.0}, which is the year out of
     * {@code http://www.eclipse.org/emf/2002/Ecore} and not a model version at all.
     * </p>
     *
     * <p>
     * For those, {@link EPackageVersions} — the same rule an upload applies — is followed as far as
     * a service reference allows: the nsURI's last segment when it is version-shaped, failing that
     * the version of the bundle that registered the service, without its build qualifier. The one
     * step that cannot be taken is the {@code Version} annotation, which needs the EPackage object
     * and would activate the component behind it.
     * </p>
     *
     * @return the version, or {@code null} when nothing states one — a better answer than a number
     *         that is not a version
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

    /**
     * What the resource set announces, as a flat list of strings.
     *
     * <p>
     * Flat is all there is: {@code SupportedMediatype} derives it from the resource set's
     * content-type map and appends the formats a bundle announces without a {@code Resource.Factory}
     * behind them, and by here the two are one list. The snapshot says so rather than guessing which
     * is which.
     * </p>
     */
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

    /**
     * The registration's properties, which are the change count and nothing else.
     *
     * <p>
     * Built fresh rather than read back from the reference and amended: everything else on a
     * registration is set by the framework, and {@code setProperties} ignores those keys anyway, so
     * carrying them along would only suggest they were being maintained here.
     * </p>
     */
    private static Hashtable<String, Object> withChangeCount(long count) {
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put(Constants.SERVICE_CHANGECOUNT, count);
        return properties;
    }

    /**
     * Something the snapshot would say differently: raise the count and publish it.
     *
     * <p>
     * Called from every bind, update and unbind callback. Synchronized so the published number only
     * ever moves forward — two callbacks racing would otherwise be free to set their counts in the
     * opposite order, and a watcher reads the number, not the order it was set in. Publishing is a
     * property change, so each call also costs a synchronous callback to every bound watcher; the
     * number is deliberately dumb to keep that cheap, and what changed is another snapshot.
     * </p>
     *
     * <p>
     * With no registration yet, or none any more, the count still moves and nothing is published:
     * see {@link #runtimeServiceRegistration}. Activation publishes where it has got to, and after
     * deactivation there is no service left for the number to mean anything to.
     * </p>
     */
    synchronized void changed() {
        long count = changeCount.incrementAndGet();
        ServiceRegistration<AtlasRuntime> live = runtimeServiceRegistration;
        if (live == null) {
            return;
        }
        try {
            live.setProperties(withChangeCount(count));
        } catch (IllegalStateException alreadyUnregistered) {
            // The framework got there first, which happens once, on the way down.
            runtimeServiceRegistration = null;
        }
    }
}
