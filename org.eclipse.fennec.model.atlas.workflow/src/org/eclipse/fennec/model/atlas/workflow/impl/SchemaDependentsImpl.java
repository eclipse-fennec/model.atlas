/*
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
package org.eclipse.fennec.model.atlas.workflow.impl;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.scope.api.RegistryInfo;
import org.eclipse.fennec.model.atlas.scope.api.RegistryType;
import org.eclipse.fennec.model.atlas.scope.api.StageInfo;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.workflow.RegistryServiceCollector;
import org.eclipse.fennec.model.atlas.workflow.ScopeServiceCollector;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.eclipse.fennec.model.atlas.workflow.dependency.SchemaDependent;
import org.eclipse.fennec.model.atlas.workflow.dependency.SchemaDependent.Kind;
import org.eclipse.fennec.model.atlas.workflow.dependency.SchemaDependents;
import org.eclipse.fennec.model.atlas.workflow.dependency.SchemaDependentsContributor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * The dependents query of issue #250 (see {@link SchemaDependents} for the view rules).
 *
 * <p>
 * The schema edge is read off the packages registered per (scope, stage) - the
 * {@code EPackage} services the registration service publishes, tracked here by their
 * scope, stage and nsURI properties - because only the registered instance knows what its
 * references resolved to. The instance edge is read off the registries' listings: an
 * object's type is a class URI whose resource part is its package's nsURI. Everything else
 * comes from {@link SchemaDependentsContributor}s.
 * </p>
 */
@Component(name = "SchemaDependents", service = SchemaDependents.class, immediate = true)
public class SchemaDependentsImpl implements SchemaDependents {

    private static final Logger LOGGER = Logger.getLogger(SchemaDependentsImpl.class.getName());

    private record Location(String scope, String stage) {
    }

    /** (scope, stage) -> nsURI -> the registered package. */
    private final Map<Location, Map<String, EPackage>> packages = new ConcurrentHashMap<>();
    /** What a registered package references, by package identity; dropped with the package. */
    private final Map<EPackage, Set<String>> references = Collections.synchronizedMap(new IdentityHashMap<>());
    private final List<SchemaDependentsContributor> contributors = new CopyOnWriteArrayList<>();

    @Reference
    private ScopeServiceCollector scopes;

    @Reference
    private RegistryServiceCollector registries;

    @Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MULTIPLE, target = "(&("
            + EMFNamespaces.EMF_MODEL_SCOPE + "=*)(" + WorkflowConstants.ATLAS_EPACKAGE_REGISTRATION_STAGE_PROPERTY
            + "=*)(" + EMFNamespaces.EMF_MODEL_NSURI + "=*))")
    void bindEPackage(EPackage ePackage, Map<String, Object> properties) {
        Location location = locationOf(properties);
        String nsURI = String.valueOf(properties.get(EMFNamespaces.EMF_MODEL_NSURI));
        packages.computeIfAbsent(location, l -> new ConcurrentHashMap<>()).put(nsURI, ePackage);
    }

    void unbindEPackage(EPackage ePackage, Map<String, Object> properties) {
        Location location = locationOf(properties);
        String nsURI = String.valueOf(properties.get(EMFNamespaces.EMF_MODEL_NSURI));
        Map<String, EPackage> atLocation = packages.get(location);
        if (atLocation != null) {
            atLocation.remove(nsURI, ePackage);
            if (atLocation.isEmpty()) {
                packages.remove(location, atLocation);
            }
        }
        references.remove(ePackage);
    }

    @Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MULTIPLE)
    void bindContributor(SchemaDependentsContributor contributor) {
        contributors.add(contributor);
    }

    void unbindContributor(SchemaDependentsContributor contributor) {
        contributors.remove(contributor);
    }

    private static Location locationOf(Map<String, Object> properties) {
        return new Location(String.valueOf(properties.get(EMFNamespaces.EMF_MODEL_SCOPE)),
                String.valueOf(properties.get(WorkflowConstants.ATLAS_EPACKAGE_REGISTRATION_STAGE_PROPERTY)));
    }

    @Override
    public List<SchemaDependent> dependentsOf(String scope, String stage, String nsURI) {
        requireNonNull(scope, "scope");
        requireNonNull(stage, "stage");
        requireNonNull(nsURI, "nsURI");

        Scope scopeModel = scopes.getScopeByName(scope);
        List<RegistryInfo> scopeRegistries = scopeModel == null ? List.of() : scopeModel.getRegistries();
        RegistryInfo schemaRegistry = scopeRegistries.stream()
                .filter(r -> r.getType() == RegistryType.SCHEMA && hasStage(r, stage)).findFirst().orElse(null);
        if (scopeModel == null) {
            LOGGER.fine(() -> "No scope '" + scope + "' is known; the dependents of " + nsURI
                    + " cannot be looked up beyond the packages registered for stage '" + stage + "'");
        }

        Map<String, SchemaDependent> found = new LinkedHashMap<>();

        // 1. other schemas in the same stage: they resolve against their own stage only
        if (schemaRegistry != null) {
            RegistryService<?> service = registries.getRegistryServiceByRegistryName(schemaRegistry.getName());
            if (service != null) {
                for (ObjectMetadata metadata : service.listInStage(scope, stage)) {
                    String other = SchemaDependencies.nsUriOf(metadata);
                    if (other == null || other.equals(nsURI)) {
                        continue;
                    }
                    EPackage registered = registeredPackage(scope, stage, other);
                    if (registered != null && referencedNsURIs(registered).contains(nsURI)) {
                        add(found, new SchemaDependent(Kind.SCHEMA, schemaRegistry.getName(), stage,
                                metadata.getObjectId(), metadata.getObjectType()));
                    }
                }
            }
        }

        // 2. instances and contributed kinds in every stage whose view is served from here
        List<String> views = viewsServedBy(scope, schemaRegistry, scopeRegistries, stage, nsURI);
        for (RegistryInfo registry : scopeRegistries) {
            if (registry.getType() == RegistryType.SCHEMA) {
                continue;
            }
            RegistryService<?> service = registries.getRegistryServiceByRegistryName(registry.getName());
            if (service == null) {
                continue;
            }
            for (String view : views) {
                if (!hasStage(registry, view)) {
                    continue;
                }
                for (ObjectMetadata metadata : service.listInStage(scope, view)) {
                    if (nsURI.equals(packageOf(metadata.getObjectType()))) {
                        add(found, new SchemaDependent(Kind.INSTANCE, registry.getName(), view,
                                metadata.getObjectId(), metadata.getObjectType()));
                    }
                }
                for (SchemaDependentsContributor contributor : contributors) {
                    for (SchemaDependent dependent : contributor.dependentsIn(scope, service, view, nsURI)) {
                        add(found, dependent);
                    }
                }
            }
        }
        return List.copyOf(found.values());
    }

    private static void add(Map<String, SchemaDependent> found, SchemaDependent dependent) {
        found.putIfAbsent(dependent.address(), dependent);
    }

    /**
     * The stages whose view sees {@code nsURI} as served from {@code stage}: the stage
     * itself; every earlier stage of the schema registry's chain up to the first one holding
     * the package itself; and, for the final stage, the stages only other registries have.
     */
    private List<String> viewsServedBy(String scope, RegistryInfo schemaRegistry, List<RegistryInfo> scopeRegistries,
            String stage, String nsURI) {
        Set<String> views = new LinkedHashSet<>();
        views.add(stage);
        if (schemaRegistry == null) {
            return List.copyOf(views);
        }
        List<String> chain = schemaRegistry.getStages().stream().map(StageInfo::getName).toList();
        for (int i = chain.indexOf(stage) - 1; i >= 0; i--) {
            String earlier = chain.get(i);
            if (registeredPackage(scope, earlier, nsURI) != null) {
                break; // it and everything before it resolve there, not here
            }
            views.add(earlier);
        }
        boolean isFinal = schemaRegistry.getStages().stream()
                .anyMatch(s -> stage.equals(s.getName()) && s.isFinal());
        if (isFinal) {
            for (RegistryInfo registry : scopeRegistries) {
                if (registry.getType() == RegistryType.SCHEMA) {
                    continue;
                }
                for (StageInfo other : registry.getStages()) {
                    if (!chain.contains(other.getName())) {
                        views.add(other.getName());
                    }
                }
            }
        }
        return List.copyOf(views);
    }

    private static boolean hasStage(RegistryInfo registry, String stage) {
        return registry.getStages().stream().anyMatch(s -> stage.equals(s.getName()));
    }

    private EPackage registeredPackage(String scope, String stage, String nsURI) {
        Map<String, EPackage> atLocation = packages.get(new Location(scope, stage));
        return atLocation == null ? null : atLocation.get(nsURI);
    }

    /** The nsURI part of a class URI such as {@code http://x/1.0#//Person}, or {@code null}. */
    static String packageOf(String objectType) {
        if (objectType == null || objectType.isBlank()) {
            return null;
        }
        URI uri = URI.createURI(objectType);
        return uri.hasFragment() ? uri.trimFragment().toString() : null;
    }

    /**
     * The nsURIs of the other packages {@code ePackage} references: the resource of what a
     * reference resolved to, or the proxy URI of one that did not - which names the package
     * only when the reference was written by nsURI.
     */
    private Set<String> referencedNsURIs(EPackage ePackage) {
        return references.computeIfAbsent(ePackage, SchemaDependentsImpl::collectReferencedNsURIs);
    }

    static Set<String> collectReferencedNsURIs(EPackage ePackage) {
        Set<String> found = new LinkedHashSet<>();
        try {
            for (EObject external : EcoreUtil.ExternalCrossReferencer.find(ePackage).keySet()) {
                String nsURI = null;
                if (external.eIsProxy()) {
                    URI proxy = ((InternalEObject) external).eProxyURI();
                    nsURI = proxy == null ? null : proxy.trimFragment().toString();
                } else if (EcoreUtil.getRootContainer(external) instanceof EPackage other) {
                    nsURI = other.getNsURI();
                }
                if (nsURI != null && !nsURI.isBlank() && !nsURI.equals(ePackage.getNsURI())) {
                    found.add(nsURI);
                }
            }
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, e,
                    () -> "Cannot determine what package " + ePackage.getNsURI() + " references");
        }
        return Set.copyOf(found);
    }

    /** For tests: the registered packages this query currently knows for a location. */
    List<String> registeredPackages(String scope, String stage) {
        Map<String, EPackage> atLocation = packages.get(new Location(scope, stage));
        return atLocation == null ? List.of() : new ArrayList<>(atLocation.keySet());
    }
}
