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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.fennec.model.atlas.runtime.info.AtlasRuntimeDTO;
import org.eclipse.fennec.model.atlas.runtime.info.FailedRegistryDTO;
import org.eclipse.fennec.model.atlas.runtime.info.FailedScopeDTO;
import org.eclipse.fennec.model.atlas.runtime.info.FailureReasons;
import org.eclipse.fennec.model.atlas.runtime.info.InstanceDTO;
import org.eclipse.fennec.model.atlas.runtime.info.MediaTypeDTO;
import org.eclipse.fennec.model.atlas.runtime.info.RegistryRuntimeDTO;
import org.eclipse.fennec.model.atlas.runtime.info.SchemaPackageDTO;
import org.eclipse.fennec.model.atlas.runtime.info.ScopeRuntimeDTO;
import org.eclipse.fennec.model.atlas.runtime.info.StageRuntimeDTO;
import org.eclipse.fennec.model.atlas.scope.api.RegistryInfo;
import org.eclipse.fennec.model.atlas.scope.api.RegistryType;
import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;
import org.eclipse.fennec.model.atlas.scope.api.StageInfo;

/**
 * Turns what the trackers hold into one snapshot.
 *
 * <p>
 * Free of OSGi on purpose: everything it needs arrives as a record, so the assembly can be tested
 * without a framework and the component stays about tracking rather than about shape.
 * </p>
 *
 * <p>
 * It also derives the failures that are visible from the live state alone — a scope that bound no
 * registries, two schema registries in one scope, a parent that does not resolve. The failures that
 * need the component runtime come from {@link FailureScanner}.
 * </p>
 */
final class SnapshotBuilder {

    /** What a package reports as {@code origin} when the Atlas registered it out of a registry. */
    static final String ORIGIN_STORED = "STORED";

    /** What a package reports as {@code origin} when a bundle brought it into a scope. */
    static final String ORIGIN_GENERATED = "GENERATED";

    /**
     * What a package reports as {@code origin} when it is registered globally rather than into a
     * scope — the initial models' top-level files and any model bundle's own packages. These are
     * the built-in {@code atlas} scope's content.
     */
    static final String ORIGIN_GLOBAL = "GLOBAL";

    /** The built-in root scope, whose content is the globally registered packages. */
    static final String ATLAS_SCOPE = "atlas";

    /** What a media type reports as {@code origin} when the resource set announces it. */
    static final String ORIGIN_RESOURCE_SET = "RESOURCE_SET";

    /** What a media type reports as {@code origin} when a contributor declared it. */
    static final String ORIGIN_CONTRIBUTOR = "CONTRIBUTOR";

    private SnapshotBuilder() {
    }

    /**
     * Assembles the snapshot.
     *
     * @param name          how this Atlas names itself
     * @param instance      what this Atlas is
     * @param changeCount   the current change count
     * @param takenAt       the moment of the snapshot, in epoch milliseconds
     * @param scopes        the live scope services
     * @param packages      the live EPackage services that carry a scope
     * @param resourceSetTypes the media types the resource set announces
     * @param contributors  the declared media types, by contributor
     * @param scanned       the failures the component runtime knows about
     * @return the snapshot; never {@code null}
     */
    static AtlasRuntimeDTO build(String name, InstanceDTO instance, long changeCount, long takenAt,
            List<TrackedScope> scopes, List<TrackedPackage> packages, List<String> resourceSetTypes,
            List<TrackedContributor> contributors, ScannedFailures scanned) {

        AtlasRuntimeDTO dto = new AtlasRuntimeDTO();
        dto.name = name;
        dto.changeCount = changeCount;
        dto.takenAt = takenAt;
        dto.instance = instance;
        dto.mediaTypes = mediaTypes(resourceSetTypes, contributors);

        List<ScopeRuntimeDTO> scopeDtos = new ArrayList<>();
        List<FailedScopeDTO> failedScopes = new ArrayList<>(scanned.scopes());
        List<FailedRegistryDTO> failedRegistries = new ArrayList<>(scanned.registries());

        Set<String> liveScopeNames = new HashSet<>();
        for (TrackedScope scope : scopes) {
            if (scope.info() != null && scope.info().getName() != null) {
                liveScopeNames.add(scope.info().getName());
            }
        }

        List<TrackedScope> ordered = new ArrayList<>(scopes);
        ordered.sort(Comparator.comparing(s -> s.info() == null || s.info().getName() == null ? "" : s.info().getName()));

        for (TrackedScope scope : ordered) {
            ScopeInfo info = scope.info();
            if (info == null) {
                continue;
            }
            scopeDtos.add(scopeDto(scope, info, packages));
            collectScopeFailures(info, liveScopeNames, failedScopes, failedRegistries);
        }

        dto.scopes = scopeDtos;
        dto.failedScopes = failedScopes;
        dto.failedRegistries = failedRegistries;
        dto.failedPackages = new ArrayList<>(scanned.packages());
        return dto;
    }

    private static ScopeRuntimeDTO scopeDto(TrackedScope scope, ScopeInfo info, List<TrackedPackage> packages) {
        ScopeRuntimeDTO dto = new ScopeRuntimeDTO();
        dto.name = info.getName();
        dto.description = info.getDescription();
        dto.parentScope = info.getParentScope();
        dto.inheriting = scope.inheriting();
        dto.serviceId = scope.serviceId();
        dto.registries = new ArrayList<>();

        String schemaRegistry = schemaRegistryName(info);
        for (RegistryInfo registry : info.getRegistries()) {
            dto.registries.add(registryDto(registry, info.getName(), schemaRegistry, packages));
        }
        return dto;
    }

    private static RegistryRuntimeDTO registryDto(RegistryInfo registry, String scopeName, String schemaRegistry,
            List<TrackedPackage> packages) {
        RegistryRuntimeDTO dto = new RegistryRuntimeDTO();
        dto.name = registry.getName();
        dto.description = registry.getDescription();
        dto.type = registry.getType() == null ? RegistryType.OTHER.getName() : registry.getType().getName();
        dto.packages = new ArrayList<>();

        // Packages are reported under the scope's schema registry, whatever it is named: the
        // EPackage services carry a scope and a stage but no registry, and a scope has at most one
        // registry of type SCHEMA for exactly that reason.
        boolean holdsPackages = registry.getName() != null && registry.getName().equals(schemaRegistry);
        Map<String, Integer> perStage = new LinkedHashMap<>();
        if (holdsPackages) {
            // The atlas scope is the exception, and it is in the loader's contract rather than in
            // ours: its content is the set of globally registered packages, which carry no scope
            // property at all. They are reported in its final stage, because a global registration
            // has no stage either and the final stage is what a read of the registry resolves.
            boolean global = ATLAS_SCOPE.equals(scopeName);
            String fallbackStage = global ? finalStageName(registry) : null;
            for (TrackedPackage pkg : packages) {
                boolean mine = global ? pkg.scope().isBlank() : pkg.scope().equals(scopeName);
                if (!mine) {
                    continue;
                }
                String stage = pkg.stage() == null ? fallbackStage : pkg.stage();
                dto.packages.add(packageDto(pkg, stage));
                perStage.merge(stage, 1, Integer::sum);
            }
            dto.packages.sort(Comparator.comparing((SchemaPackageDTO p) -> p.stage == null ? "" : p.stage)
                    .thenComparing(p -> p.nsURI == null ? "" : p.nsURI));
        }

        dto.stages = new ArrayList<>();
        for (StageInfo stage : registry.getStages()) {
            dto.stages.add(stageDto(stage, perStage.getOrDefault(stage.getName(), 0)));
        }
        return dto;
    }

    private static StageRuntimeDTO stageDto(StageInfo stage, int packageCount) {
        StageRuntimeDTO dto = new StageRuntimeDTO();
        dto.name = stage.getName();
        dto.description = stage.getDescription();
        dto.readable = stage.isReadable();
        dto.writable = stage.isWritable();
        dto.finalStage = stage.isFinal();
        dto.packageCount = packageCount;
        return dto;
    }

    /** The stage a read of this registry resolves when none was asked for. */
    private static String finalStageName(RegistryInfo registry) {
        for (StageInfo stage : registry.getStages()) {
            if (stage.isFinal()) {
                return stage.getName();
            }
        }
        return registry.getStages().isEmpty() ? null : registry.getStages().get(0).getName();
    }

    private static SchemaPackageDTO packageDto(TrackedPackage pkg, String stage) {
        SchemaPackageDTO dto = new SchemaPackageDTO();
        dto.nsURI = pkg.nsURI();
        dto.name = pkg.name();
        dto.version = pkg.version();
        dto.fingerprint = pkg.fingerprint();
        dto.objectId = objectId(pkg.nsURI());
        dto.stage = stage;
        dto.serviceId = pkg.serviceId();
        dto.origin = origin(pkg);
        return dto;
    }

    /**
     * Where this package came from, as far as its registration says.
     *
     * <p>
     * {@code dynamic.registration} is set only by the Atlas's own registrar, and a scope property
     * only when it was registered into one. Absent both, it is a global registration.
     * </p>
     */
    private static String origin(TrackedPackage pkg) {
        if (pkg.stored()) {
            return ORIGIN_STORED;
        }
        return pkg.scope().isBlank() ? ORIGIN_GLOBAL : ORIGIN_GENERATED;
    }

    /**
     * The identifier a package has within its registry.
     *
     * <p>
     * The same encoding the schema registry uses, with UTF-8 fixed on both sides so ids stay stable
     * across platform default charsets. It is a one-line mirror of a private helper there rather
     * than a second convention.
     * </p>
     */
    static String objectId(String nsURI) {
        return nsURI == null ? null : Base64.getUrlEncoder().encodeToString(nsURI.getBytes(StandardCharsets.UTF_8));
    }

    /** The name of the scope's registry of type {@code SCHEMA}, or {@code null} if it has none. */
    static String schemaRegistryName(ScopeInfo info) {
        for (RegistryInfo registry : info.getRegistries()) {
            if (RegistryType.SCHEMA.equals(registry.getType())) {
                return registry.getName();
            }
        }
        return null;
    }

    private static void collectScopeFailures(ScopeInfo info, Set<String> liveScopeNames,
            List<FailedScopeDTO> failedScopes, List<FailedRegistryDTO> failedRegistries) {

        if (info.getRegistries().isEmpty()) {
            // The scope activated and bound nothing. Its registryService.target matches no
            // registry — most often because it was never set and kept the (scope=no-inject)
            // default, which matches nothing on purpose.
            failedScopes.add(failedScope(info.getName(), FailureReasons.NO_REGISTRIES_BOUND,
                    "the scope is live but bound no registries"));
        }

        List<String> schemaRegistries = new ArrayList<>();
        for (RegistryInfo registry : info.getRegistries()) {
            if (RegistryType.SCHEMA.equals(registry.getType())) {
                schemaRegistries.add(registry.getName());
            }
        }
        if (schemaRegistries.size() > 1) {
            for (String registry : schemaRegistries) {
                FailedRegistryDTO failed = new FailedRegistryDTO();
                failed.scope = info.getName();
                failed.name = registry;
                failed.reason = FailureReasons.DUPLICATE_SCHEMA_REGISTRY;
                failed.serviceId = -1;
                failed.detail = "a scope has at most one SCHEMA registry; this one has "
                        + schemaRegistries.size() + ": " + String.join(", ", schemaRegistries);
                failedRegistries.add(failed);
            }
        }

        String parent = info.getParentScope();
        if (parent != null && !parent.isBlank() && !liveScopeNames.contains(parent)) {
            failedScopes.add(failedScope(info.getName(), FailureReasons.PARENT_SCOPE_UNRESOLVED,
                    "parent scope '" + parent + "' is not registered, so reads do not read through to it"));
        }
    }

    private static FailedScopeDTO failedScope(String name, String reason, String detail) {
        FailedScopeDTO failed = new FailedScopeDTO();
        failed.name = name;
        failed.reason = reason;
        failed.serviceId = -1;
        failed.detail = detail;
        return failed;
    }

    private static List<MediaTypeDTO> mediaTypes(List<String> resourceSetTypes,
            List<TrackedContributor> contributors) {
        Map<String, MediaTypeDTO> byType = new LinkedHashMap<>();
        for (String type : resourceSetTypes) {
            MediaTypeDTO dto = new MediaTypeDTO();
            dto.mediaType = type;
            dto.origin = ORIGIN_RESOURCE_SET;
            dto.serviceId = -1;
            byType.put(type, dto);
        }
        // A type a contributor declares is reported as its own even when the resource set's list
        // already carries it: the contributor is the one thing here with a service behind it, so
        // naming it is strictly more than the other branch can say.
        for (TrackedContributor contributor : contributors) {
            for (String type : contributor.mediaTypes()) {
                MediaTypeDTO dto = new MediaTypeDTO();
                dto.mediaType = type;
                dto.origin = ORIGIN_CONTRIBUTOR;
                dto.serviceId = contributor.serviceId();
                byType.put(type, dto);
            }
        }
        List<MediaTypeDTO> types = new ArrayList<>(byType.values());
        types.sort(Comparator.comparing(t -> t.mediaType == null ? "" : t.mediaType));
        return types;
    }
}
