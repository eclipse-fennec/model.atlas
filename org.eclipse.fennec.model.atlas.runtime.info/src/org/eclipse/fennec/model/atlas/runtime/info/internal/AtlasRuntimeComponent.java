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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

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
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

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
 * <strong>Everything it reports is tracked, not referenced.</strong> The scopes, packages and
 * contributors arrive through {@link ServiceTracker}s and the component runtime through an optional
 * reference, so this component activates whether or not any of them are there. A runtime service
 * that needed a scope in order to start would be absent in exactly the situation it exists to
 * report — an Atlas where nothing came up — and an integration test waiting for it would wait
 * forever rather than being told.
 * </p>
 */
// service = {} is load-bearing: the class implements AtlasRuntime, so without it DS would
// register a second copy of this service beside the one the publisher registers — one with a
// change count that moves and one with properties that never do, and a consumer binding the
// wrong one would be told nothing for the lifetime of the framework.
@Component(name = "AtlasRuntime", immediate = true, configurationPid = "AtlasRuntime",
        configurationPolicy = ConfigurationPolicy.OPTIONAL, service = {})
public class AtlasRuntimeComponent implements AtlasRuntime {

    private static final Logger LOG = Logger.getLogger(AtlasRuntimeComponent.class.getName());

    /**
     * Every EPackage service, scoped or not.
     *
     * <p>
     * Not filtered on {@code emf.model.scope}, and that is the whole correction: a package the
     * Atlas registered out of a scope registry carries a scope, but a <em>globally</em> registered
     * one does not — {@code InitialModelLoader} sets only name, nsURI and registration kind on the
     * top-level models. The built-in {@code atlas} scope's content is defined as exactly those
     * global registrations, so filtering on a scope property made the atlas scope permanently
     * empty however many models were loaded.
     * </p>
     */
    private static final String ALL_PACKAGES = "(objectClass=" + EPackage.class.getName() + ")";

    /** Set by the Atlas when it registers a package out of a registry, absent on a bundle's own. */
    private static final String DYNAMIC_REGISTRATION = "dynamic.registration";

    /**
     * The Jakarta RS whiteboard's runtime service, named as a string rather than imported.
     *
     * <p>
     * Its {@code osgi.jakartars.endpoint} property is where this Atlas is actually reachable, which
     * is the same thing a deployment would otherwise have to repeat in configuration and keep in
     * step by hand. Tracking it by name keeps the REST stack off this bundle's class path: an Atlas
     * with no REST layer reports no endpoints rather than failing to resolve.
     * </p>
     */
    private static final String JAKARTARS_RUNTIME = "(objectClass=org.osgi.service.jakartars.runtime.JakartarsServiceRuntime)";

    /** Where the whiteboard says it serves. */
    private static final String JAKARTARS_ENDPOINT = "osgi.jakartars.endpoint";

    private final AtomicLong changeCount = new AtomicLong();

    private BundleContext context;

    private String name;

    private List<String> endpoints = List.of();

    @SuppressWarnings("rawtypes")
	private ServiceTracker<ReadableScopeService, ReadableScopeService> scopeTracker;

    private ServiceTracker<Object, Object> packageTracker;

    private ServiceTracker<MediatypeContributor, MediatypeContributor> contributorTracker;

    private ServiceTracker<Object, Object> endpointTracker;

    private ChangeCountPublisher<AtlasRuntime> publisher;

    /**
     * Optional on purpose (see the class comment): without it the failure half of the snapshot is
     * empty, which is a weaker answer than usual but still an answer.
     */
    @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL,
            policyOption = ReferencePolicyOption.GREEDY)
    volatile ServiceComponentRuntime componentRuntime;

    private volatile SupportedMediatype supportedMediatype;

    @Activate
    void activate(BundleContext context, Map<String, Object> properties) {
        this.context = context;
        this.name = string(properties.get("atlas.name"), "atlas");
        this.endpoints = strings(properties.get(ATLAS_ENDPOINT));

        scopeTracker = new ServiceTracker<>(context, ReadableScopeService.class, counting());
        scopeTracker.open();
        try {
            packageTracker = new ServiceTracker<>(context, context.createFilter(ALL_PACKAGES),
                    countingReferences());
            packageTracker.open();
        } catch (InvalidSyntaxException impossible) {
            // The filter is a constant built from constants; if it is malformed that is a coding
            // error, and reporting no packages is better than failing to activate at all.
            LOG.severe("[Atlas] the scoped-package filter is malformed, no packages will be reported: " + impossible);
        }
        contributorTracker = new ServiceTracker<>(context, MediatypeContributor.class, counting());
        contributorTracker.open();
        try {
            endpointTracker = new ServiceTracker<>(context, context.createFilter(JAKARTARS_RUNTIME),
                    countingReferences());
            endpointTracker.open();
        } catch (org.osgi.framework.InvalidSyntaxException impossible) {
            LOG.severe("[Atlas] the REST runtime filter is malformed, no endpoints will be reported: " + impossible);
        }

        publisher = new ChangeCountPublisher<>(context, AtlasRuntime.class, this, changeCount::get,
                "Atlas runtime service");
        publisher.open();
    }

    @Deactivate
    void deactivate() {
        if (publisher != null) {
            publisher.close();
        }
        closeQuietly(endpointTracker);
        closeQuietly(contributorTracker);
        closeQuietly(packageTracker);
        closeQuietly(scopeTracker);
    }

    /**
     * The media types the resource set announces.
     *
     * <p>
     * Bound by method rather than by field so the {@code updated} callback arrives: the list is not
     * fixed, and the service refreshes its properties as codecs register content types.
     * </p>
     */
    @Reference(name = "mediatypes", policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL,
            policyOption = ReferencePolicyOption.GREEDY)
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

    @Override
    public AtlasRuntimeDTO snapshot() {
        return SnapshotBuilder.build(name, instance(), changeCount.get(), System.currentTimeMillis(), trackedScopes(),
                trackedPackages(), codecTypes(), trackedContributors(), FailureScanner.scan(componentRuntime));
    }

    private InstanceDTO instance() {
        InstanceDTO instance = new InstanceDTO();
        instance.id = name;
        instance.endpoints = endpoints.isEmpty() ? servedEndpoints() : endpoints;
        instance.implementationVersion = context == null ? null : context.getBundle().getVersion().toString();
        return instance;
    }

    /**
     * Where the REST layer says it serves, when the configuration has not been explicit.
     *
     * <p>
     * Configuration wins when it is set, because a deployment behind a proxy is reachable at an
     * address the framework cannot see. Everywhere else this is the answer and nobody has to write
     * it down twice.
     * </p>
     */
    private List<String> servedEndpoints() {
        ServiceReference<Object>[] references = endpointTracker == null ? null
                : endpointTracker.getServiceReferences();
        if (references == null) {
            return List.of();
        }
        List<String> served = new ArrayList<>();
        for (ServiceReference<Object> reference : references) {
            for (String endpoint : strings(reference.getProperty(JAKARTARS_ENDPOINT))) {
                if (!served.contains(endpoint)) {
                    served.add(endpoint);
                }
            }
        }
        return served;
    }

    @SuppressWarnings("rawtypes")
	private List<TrackedScope> trackedScopes() {
        List<TrackedScope> scopes = new ArrayList<>();
        ServiceReference<ReadableScopeService>[] references = scopeTracker == null ? null
                : scopeTracker.getServiceReferences();
        if (references == null) {
            return scopes;
        }
        for (ServiceReference<ReadableScopeService> reference : references) {
            ReadableScopeService<?> service = scopeTracker.getService(reference);
            if (service == null) {
                // Gone between the listing and the look-up. A snapshot reports what it could see.
                continue;
            }
            scopes.add(new TrackedScope(serviceId(reference), service.getScopeInfo(),
                    service.isInheritingFromParentScope()));
        }
        return scopes;
    }

    /**
     * The live packages, read from service properties alone.
     *
     * <p>
     * Deliberately never {@code getService}: asking for an EPackage service object activates the
     * component behind it, and a snapshot that activates things is not the read-only observation it
     * claims to be — the telemetry gauges call this on every collection. Nothing reported here
     * needs the object; a field that would have is not reported.
     * </p>
     */
    private List<TrackedPackage> trackedPackages() {
        List<TrackedPackage> packages = new ArrayList<>();
        ServiceReference<Object>[] references = packageTracker == null ? null
                : packageTracker.getServiceReferences();
        if (references == null) {
            return packages;
        }
        for (ServiceReference<Object> reference : references) {
            String nsURI = string(reference.getProperty(EMFNamespaces.EMF_MODEL_NSURI), null);
            boolean stored = Boolean.TRUE.equals(reference.getProperty(DYNAMIC_REGISTRATION));
            packages.add(new TrackedPackage(serviceId(reference),
                    string(reference.getProperty(EMFNamespaces.EMF_MODEL_SCOPE), ""),
                    string(reference.getProperty(WorkflowConstants.ATLAS_EPACKAGE_REGISTRATION_STAGE_PROPERTY), null),
                    nsURI, string(reference.getProperty(EMFNamespaces.EMF_NAME), null), version(reference, stored, nsURI),
                    string(reference.getProperty(EMFNamespaces.EMF_MODEL_FINGERPRINT), null), stored));
        }
        return packages;
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
        List<TrackedContributor> tracked = new ArrayList<>();
        ServiceReference<MediatypeContributor>[] references = contributorTracker == null ? null
                : contributorTracker.getServiceReferences();
        if (references == null) {
            return tracked;
        }
        for (ServiceReference<MediatypeContributor> reference : references) {
            MediatypeContributor contributor = contributorTracker.getService(reference);
            if (contributor == null) {
                continue;
            }
            List<String> types = contributor.getMediaTypes();
            tracked.add(new TrackedContributor(serviceId(reference), types == null ? List.of() : List.copyOf(types)));
        }
        return tracked;
    }

    private List<String> codecTypes() {
        SupportedMediatype types = supportedMediatype;
        if (types == null) {
            return List.of();
        }
        List<String> supported = types.getSupportedMediaTypes();
        return supported == null ? List.of() : List.copyOf(supported);
    }

    /**
     * One customizer for all three trackers: every arrival, property change and departure is a
     * change to the snapshot, and the number is all a watcher needs to know that.
     */
    private <S> ServiceTrackerCustomizer<S, S> counting() {
        return new ServiceTrackerCustomizer<S, S>() {

            @Override
            public S addingService(ServiceReference<S> reference) {
                changeCount.incrementAndGet();
                return context.getService(reference);
            }

            @Override
            public void modifiedService(ServiceReference<S> reference, S service) {
                changeCount.incrementAndGet();
            }

            @Override
            public void removedService(ServiceReference<S> reference, S service) {
                changeCount.incrementAndGet();
                context.ungetService(reference);
            }
        };
    }

    /**
     * Like {@link #counting()}, but it never asks for the service object: these are tracked for
     * their properties alone, and taking a reference to something only to read its metadata makes
     * this bundle a consumer of a stack it has no business holding open.
     */
    private ServiceTrackerCustomizer<Object, Object> countingReferences() {
        return new ServiceTrackerCustomizer<Object, Object>() {

            @Override
            public Object addingService(ServiceReference<Object> reference) {
                changeCount.incrementAndGet();
                return reference;
            }

            @Override
            public void modifiedService(ServiceReference<Object> reference, Object tracked) {
                changeCount.incrementAndGet();
            }

            @Override
            public void removedService(ServiceReference<Object> reference, Object tracked) {
                changeCount.incrementAndGet();
            }
        };
    }

    private static long serviceId(ServiceReference<?> reference) {
        Object id = reference.getProperty(Constants.SERVICE_ID);
        return id instanceof Long serviceId ? serviceId.longValue() : -1L;
    }

    private static String string(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private static List<String> strings(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof String[] array) {
            return List.of(array);
        }
        if (value instanceof Collection<?> collection) {
            List<String> values = new ArrayList<>(collection.size());
            collection.forEach(entry -> values.add(String.valueOf(entry)));
            return values;
        }
        return List.of(String.valueOf(value));
    }

    private static void closeQuietly(ServiceTracker<?, ?> tracker) {
        if (tracker != null) {
            tracker.close();
        }
    }
}
