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
import org.eclipse.fennec.services.runtime.ChangeCountPublisher;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
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
 * {@code ServiceRegistration.setProperties}, and a DS-declared service hands out no registration to
 * call it on: its properties are fixed when it is registered and move only when its configuration
 * does. So the component activates, registers, and keeps the handle — through
 * {@link ChangeCountPublisher}, which already does this for the broker and client runtimes and
 * coalesces the updates onto a daemon thread.
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
 * <strong>Everything it reports arrives as a reference, and every reference is optional.</strong>
 * A runtime service that needed a scope in order to start would be absent in exactly the situation
 * it exists to report — an Atlas where nothing came up — and an integration test waiting for it
 * would wait forever rather than being told. The references are bound by method rather than by
 * field: the bind call is where the change count moves, and a field gives no callback to move it
 * in.
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

    /**
     * Optional on purpose (see the class comment): without it the failure half of the snapshot is
     * empty, which is a weaker answer than usual but still an answer.
     */
    private volatile ServiceComponentRuntime componentRuntime;

    private BundleContext context;

    private String name;

    private ChangeCountPublisher<AtlasRuntime> publisher;

    @Activate
    void activate(BundleContext context, Map<String, Object> properties) {
        this.context = context;
        this.name = string(properties.get(ATLAS_NAME), "atlas");

        publisher = new ChangeCountPublisher<>(context, AtlasRuntime.class, this, changeCount::get,
                "Atlas runtime service");
        publisher.open();
    }

    @Deactivate
    void deactivate() {
        if (publisher != null) {
            publisher.close();
        }
    }

    @Reference(name = "scope", service = ReadableScopeService.class, cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC)
    void addScope(ReadableScopeService<?> scope, Map<String, Object> properties) {
        scopes.put(serviceId(properties), scope);
        changeCount.incrementAndGet();
    }

    void updatedScope(ReadableScopeService<?> scope, Map<String, Object> properties) {
        scopes.put(serviceId(properties), scope);
        changeCount.incrementAndGet();
    }

    void removeScope(ReadableScopeService<?> scope, Map<String, Object> properties) {
        scopes.remove(serviceId(properties), scope);
        changeCount.incrementAndGet();
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
        changeCount.incrementAndGet();
    }

    void updatedPackage(ServiceReference<EPackage> reference) {
        // Nothing to store: the reference held is the one whose properties changed, and a
        // reference reads them live. What a watcher needs is the number moving.
        changeCount.incrementAndGet();
    }

    void removePackage(ServiceReference<EPackage> reference) {
        packages.remove(serviceId(reference), reference);
        changeCount.incrementAndGet();
    }

    @Reference(name = "contributor", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void addContributor(MediatypeContributor contributor, Map<String, Object> properties) {
        contributors.put(serviceId(properties), contributor);
        changeCount.incrementAndGet();
    }

    void updatedContributor(MediatypeContributor contributor, Map<String, Object> properties) {
        contributors.put(serviceId(properties), contributor);
        changeCount.incrementAndGet();
    }

    void removeContributor(MediatypeContributor contributor, Map<String, Object> properties) {
        contributors.remove(serviceId(properties), contributor);
        changeCount.incrementAndGet();
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
        changeCount.incrementAndGet();
    }

    void updatedMediatypes(SupportedMediatype supportedMediatype, Map<String, Object> properties) {
        this.supportedMediatype = supportedMediatype;
        changeCount.incrementAndGet();
    }

    void unsetMediatypes(SupportedMediatype supportedMediatype) {
        if (this.supportedMediatype == supportedMediatype) {
            this.supportedMediatype = null;
        }
        changeCount.incrementAndGet();
    }

    @Reference(name = "componentRuntime", cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC)
    void setComponentRuntime(ServiceComponentRuntime componentRuntime) {
        this.componentRuntime = componentRuntime;
        changeCount.incrementAndGet();
    }

    void unsetComponentRuntime(ServiceComponentRuntime componentRuntime) {
        if (this.componentRuntime == componentRuntime) {
            this.componentRuntime = null;
        }
        changeCount.incrementAndGet();
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
}
