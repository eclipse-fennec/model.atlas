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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.eclipse.fennec.model.atlas.runtime.info.AtlasRuntimeDTO;
import org.eclipse.fennec.model.atlas.runtime.info.FailureReasons;
import org.eclipse.fennec.model.atlas.runtime.info.InstanceDTO;
import org.eclipse.fennec.model.atlas.runtime.info.RegistryRuntimeDTO;
import org.eclipse.fennec.model.atlas.runtime.info.SchemaPackageDTO;
import org.eclipse.fennec.model.atlas.runtime.info.ScopeRuntimeDTO;
import org.eclipse.fennec.model.atlas.scope.api.RegistryInfo;
import org.eclipse.fennec.model.atlas.scope.api.RegistryType;
import org.eclipse.fennec.model.atlas.scope.api.ScopeApiFactory;
import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;
import org.eclipse.fennec.model.atlas.scope.api.StageInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What the snapshot says, given what the trackers hold.
 */
class SnapshotBuilderTest {

    private static ScopeInfo scope(String name, String parent, RegistryInfo... registries) {
        ScopeInfo info = ScopeApiFactory.eINSTANCE.createScopeInfo();
        info.setName(name);
        info.setParentScope(parent);
        info.getRegistries().addAll(List.of(registries));
        return info;
    }

    private static RegistryInfo registry(String name, RegistryType type, String... stages) {
        RegistryInfo registry = ScopeApiFactory.eINSTANCE.createRegistryInfo();
        registry.setName(name);
        registry.setType(type);
        for (String stageName : stages) {
            StageInfo stage = ScopeApiFactory.eINSTANCE.createStageInfo();
            stage.setName(stageName);
            registry.getStages().add(stage);
        }
        return registry;
    }

    private static TrackedPackage pkg(String scope, String stage, String nsURI, String fingerprint) {
        return new TrackedPackage(7L, scope, stage, nsURI, "person", "1.0.0", fingerprint, true);
    }

    private static AtlasRuntimeDTO build(List<TrackedScope> scopes, List<TrackedPackage> packages) {
        return SnapshotBuilder.build("atlas", new InstanceDTO(), 3L, 1000L, scopes, packages, List.of(), List.of(),
                ScannedFailures.none());
    }

    @Test
    @DisplayName("an Atlas with nothing configured still answers, with empty lists rather than nulls")
    void emptyAtlasIsStillAnAnswer() {
        AtlasRuntimeDTO dto = build(List.of(), List.of());

        assertEquals(3L, dto.changeCount);
        assertEquals(1000L, dto.takenAt);
        assertNotNull(dto.scopes);
        assertTrue(dto.scopes.isEmpty());
        assertNotNull(dto.failedScopes);
        assertNotNull(dto.mediaTypes);
    }

    @Test
    @DisplayName("the same nsURI in two stages is two entries, told apart by stage and fingerprint")
    void oneNsUriPerStage() {
        ScopeInfo info = scope("jena", null, registry("schema", RegistryType.SCHEMA, "draft", "release"));
        AtlasRuntimeDTO dto = build(List.of(new TrackedScope(1L, info, false)),
                List.of(pkg("jena", "draft", "http://example.org/person", "fp-draft"),
                        pkg("jena", "release", "http://example.org/person", "fp-release")));

        RegistryRuntimeDTO schema = dto.scopes.get(0).registries.get(0);
        assertEquals(2, schema.packages.size(), "one entry per stage, not one per nsURI");
        assertEquals(List.of("draft", "release"), schema.packages.stream().map(p -> p.stage).toList());
        assertEquals(List.of("fp-draft", "fp-release"), schema.packages.stream().map(p -> p.fingerprint).toList());
        assertEquals(1, schema.stages.get(0).packageCount);
        assertEquals(1, schema.stages.get(1).packageCount);
    }

    @Test
    @DisplayName("packages land under the scope's SCHEMA registry whatever it is named")
    void packagesFollowTheTypeNotTheName() {
        ScopeInfo info = scope("jena", null, registry("models", RegistryType.SCHEMA, "draft"),
                registry("transformations", RegistryType.TRANSFORMATION, "draft"));
        AtlasRuntimeDTO dto = build(List.of(new TrackedScope(1L, info, false)),
                List.of(pkg("jena", "draft", "http://example.org/person", "fp")));

        ScopeRuntimeDTO scopeDto = dto.scopes.get(0);
        assertEquals(1, scopeDto.registries.get(0).packages.size(), "the SCHEMA registry holds them");
        assertTrue(scopeDto.registries.get(1).packages.isEmpty(), "the transformation registry does not");
    }

    @Test
    @DisplayName("a package's objectId is the schema registry's encoding of its nsURI")
    void objectIdMatchesTheRegistryEncoding() {
        String nsURI = "http://example.org/person";
        ScopeInfo info = scope("jena", null, registry("schema", RegistryType.SCHEMA, "draft"));
        AtlasRuntimeDTO dto = build(List.of(new TrackedScope(1L, info, false)),
                List.of(pkg("jena", "draft", nsURI, "fp")));

        SchemaPackageDTO entry = dto.scopes.get(0).registries.get(0).packages.get(0);
        assertEquals(Base64.getUrlEncoder().encodeToString(nsURI.getBytes(StandardCharsets.UTF_8)), entry.objectId);
    }

    @Test
    @DisplayName("a live scope that bound no registries is reported as a failure, not as an empty scope")
    void noRegistriesBoundIsAFailure() {
        AtlasRuntimeDTO dto = build(List.of(new TrackedScope(1L, scope("jena", null), false)), List.of());

        assertEquals(1, dto.scopes.size(), "the scope is live, so it is listed");
        assertEquals(1, dto.failedScopes.size(), "and it is also reported as broken");
        assertEquals(FailureReasons.NO_REGISTRIES_BOUND, dto.failedScopes.get(0).reason);
        assertEquals("jena", dto.failedScopes.get(0).name);
    }

    @Test
    @DisplayName("two SCHEMA registries in one scope are reported, both of them")
    void duplicateSchemaRegistry() {
        ScopeInfo info = scope("jena", null, registry("schema", RegistryType.SCHEMA, "draft"),
                registry("models", RegistryType.SCHEMA, "draft"));
        AtlasRuntimeDTO dto = build(List.of(new TrackedScope(1L, info, false)), List.of());

        assertEquals(2, dto.failedRegistries.size());
        assertTrue(dto.failedRegistries.stream()
                .allMatch(f -> FailureReasons.DUPLICATE_SCHEMA_REGISTRY.equals(f.reason)));
        assertEquals(List.of("schema", "models"), dto.failedRegistries.stream().map(f -> f.name).toList());
    }

    @Test
    @DisplayName("a parent scope nobody registered is reported; one that is live is not")
    void parentScopeResolution() {
        ScopeInfo child = scope("jena", "platform", registry("schema", RegistryType.SCHEMA, "draft"));
        AtlasRuntimeDTO missing = build(List.of(new TrackedScope(1L, child, true)), List.of());
        assertEquals(1, missing.failedScopes.size());
        assertEquals(FailureReasons.PARENT_SCOPE_UNRESOLVED, missing.failedScopes.get(0).reason);

        ScopeInfo parent = scope("platform", null, registry("schema", RegistryType.SCHEMA, "draft"));
        AtlasRuntimeDTO present = build(
                List.of(new TrackedScope(1L, child, true), new TrackedScope(2L, parent, false)), List.of());
        assertTrue(present.failedScopes.isEmpty(), "the parent is live, so there is nothing to report");
    }

    @Test
    @DisplayName("the reported type of a SCHEMA registry is the constant consumers branch on")
    void schemaTypeMatchesTheDeclaredConstant() {
        ScopeInfo info = scope("jena", null, registry("schema", RegistryType.SCHEMA, "draft"));
        AtlasRuntimeDTO dto = build(List.of(new TrackedScope(1L, info, false)), List.of());

        // The telemetry bundle decides whether a registry can hold packages by comparing against
        // this constant; the value comes from the EMF enum, so the two have to agree.
        assertEquals(RegistryRuntimeDTO.TYPE_SCHEMA, dto.scopes.get(0).registries.get(0).type);
    }

    @Test
    @DisplayName("globally registered packages are the atlas scope's content, in its final stage")
    void globalPackagesLandInTheAtlasScope() {
        RegistryInfo registry = registry("atlas-schema-registry", RegistryType.SCHEMA, "released");
        registry.getStages().get(0).setFinal(true);
        ScopeInfo atlas = scope("atlas", null, registry);

        // What InitialModelLoader registers for a top-level model: a name and an nsURI, no scope
        // and no stage. Filtering those out is what made the atlas scope permanently empty.
        TrackedPackage global = new TrackedPackage(11L, "", null, "http://example.org/system", "system", "1.0.0", "fp",
                false);
        AtlasRuntimeDTO dto = build(List.of(new TrackedScope(1L, atlas, false)), List.of(global));

        RegistryRuntimeDTO schema = dto.scopes.get(0).registries.get(0);
        assertEquals(1, schema.packages.size(), "a global registration is the atlas scope's content");
        assertEquals("released", schema.packages.get(0).stage, "reported in the stage a read resolves");
        assertEquals("GLOBAL", schema.packages.get(0).origin);
        assertEquals(1, schema.stages.get(0).packageCount);
    }

    @Test
    @DisplayName("a global package is not claimed by some other scope")
    void globalPackagesDoNotLeakIntoOtherScopes() {
        ScopeInfo jena = scope("jena", "atlas", registry("schema", RegistryType.SCHEMA, "draft"));
        TrackedPackage global = new TrackedPackage(11L, "", null, "http://example.org/system", "system", "1.0.0", "fp",
                false);
        AtlasRuntimeDTO dto = build(List.of(new TrackedScope(1L, jena, true)), List.of(global));

        assertTrue(dto.scopes.get(0).registries.get(0).packages.isEmpty());
    }

    @Test
    @DisplayName("a contributor's media type names its service; a resource-set type names none")
    void mediaTypeOriginIsReported() {
        AtlasRuntimeDTO dto = SnapshotBuilder.build("atlas", new InstanceDTO(), 0L, 0L, List.of(), List.of(),
                List.of("application/json", "text/html"), List.of(new TrackedContributor(42L, List.of("text/html"))),
                ScannedFailures.none());

        assertEquals(2, dto.mediaTypes.size());
        var json = dto.mediaTypes.stream().filter(t -> "application/json".equals(t.mediaType)).findFirst().orElseThrow();
        var html = dto.mediaTypes.stream().filter(t -> "text/html".equals(t.mediaType)).findFirst().orElseThrow();

        assertEquals(SnapshotBuilder.ORIGIN_RESOURCE_SET, json.origin);
        assertEquals(-1L, json.serviceId, "the resource set's list carries no service to name");
        assertEquals(SnapshotBuilder.ORIGIN_CONTRIBUTOR, html.origin, "a contributor's claim wins: it is why it is served");
        assertEquals(42L, html.serviceId);
    }
}
