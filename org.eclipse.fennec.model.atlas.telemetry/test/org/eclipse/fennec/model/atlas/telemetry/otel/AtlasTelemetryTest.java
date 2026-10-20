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
package org.eclipse.fennec.model.atlas.telemetry.otel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.fennec.model.atlas.runtime.info.AtlasRuntimeDTO;
import org.eclipse.fennec.model.atlas.runtime.info.FailedScopeDTO;
import org.eclipse.fennec.model.atlas.runtime.info.FailureReasons;
import org.eclipse.fennec.model.atlas.runtime.info.RegistryRuntimeDTO;
import org.eclipse.fennec.model.atlas.runtime.info.SchemaPackageDTO;
import org.eclipse.fennec.model.atlas.runtime.info.ScopeRuntimeDTO;
import org.eclipse.fennec.model.atlas.runtime.info.StageRuntimeDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;

/**
 * What a snapshot becomes, as metric points.
 */
class AtlasTelemetryTest {

    private static final Attributes ATLAS = Attributes.of(AttributeKey.stringKey("fennec.atlas"), "atlas");

    private static AtlasRuntimeDTO snapshot(ScopeRuntimeDTO... scopes) {
        AtlasRuntimeDTO dto = new AtlasRuntimeDTO();
        dto.name = "atlas";
        dto.scopes = List.of(scopes);
        dto.failedScopes = new ArrayList<>();
        dto.failedRegistries = new ArrayList<>();
        dto.failedPackages = new ArrayList<>();
        return dto;
    }

    private static ScopeRuntimeDTO scope(String name, RegistryRuntimeDTO... registries) {
        ScopeRuntimeDTO dto = new ScopeRuntimeDTO();
        dto.name = name;
        dto.registries = List.of(registries);
        return dto;
    }

    private static RegistryRuntimeDTO registry(String name, String... stages) {
        return typedRegistry(name, RegistryRuntimeDTO.TYPE_SCHEMA, stages);
    }

    private static RegistryRuntimeDTO typedRegistry(String name, String type, String... stages) {
        RegistryRuntimeDTO dto = new RegistryRuntimeDTO();
        dto.name = name;
        dto.type = type;
        dto.packages = new ArrayList<>();
        dto.stages = new ArrayList<>();
        for (String stageName : stages) {
            StageRuntimeDTO stage = new StageRuntimeDTO();
            stage.name = stageName;
            dto.stages.add(stage);
        }
        return dto;
    }

    @Test
    @DisplayName("every reason reports a zero, so a fixed problem drops to zero instead of vanishing")
    void everyReasonIsReportedEvenWhenClear() {
        AtlasRuntimeDTO dto = snapshot();
        FailedScopeDTO failed = new FailedScopeDTO();
        failed.reason = FailureReasons.NO_REGISTRIES_BOUND;
        dto.failedScopes.add(failed);

        Map<Attributes, Long> points = AtlasTelemetry.failuresPerReason(dto, ATLAS);

        assertEquals(FailureReasons.ALL.size(), points.size(), "one point per reason, not per occurrence");
        assertEquals(1L, valueFor(points, FailureReasons.NO_REGISTRIES_BOUND));
        assertEquals(0L, valueFor(points, FailureReasons.STORAGE_UNAVAILABLE),
                "a reason nothing hit is a zero, not a missing series");
    }

    @Test
    @DisplayName("a stage with no packages still reports a point")
    void packagesPerStageCoversEveryStage() {
        RegistryRuntimeDTO schema = registry("schema", "draft", "release");
        schema.stages.get(0).packageCount = 2;

        Map<Attributes, Long> points = AtlasTelemetry.packagesPerStage(snapshot(scope("jena", schema)), ATLAS);

        assertEquals(2, points.size());
        assertTrue(points.containsValue(2L));
        assertTrue(points.containsValue(0L), "the empty stage is a zero, not an absence");
    }

    @Test
    @DisplayName("a registry that cannot hold packages reports no points at all")
    void onlySchemaRegistriesAreCounted() {
        RegistryRuntimeDTO schema = registry("schema", "draft");
        schema.stages.get(0).packageCount = 1;
        RegistryRuntimeDTO transformations = typedRegistry("transformations", "TRANSFORMATION", "draft", "approved");

        Map<Attributes, Long> points = AtlasTelemetry
                .packagesPerStage(snapshot(scope("civitas", schema, transformations)), ATLAS);

        assertEquals(1, points.size(),
                "a transformation registry holds no schema packages; reporting zero of them is a category error");
        assertTrue(points.containsValue(1L));
    }

    @Test
    @DisplayName("the per-package series tells two stages of one nsURI apart")
    void packageSeriesKeepsStageAndFingerprint() {
        RegistryRuntimeDTO schema = registry("schema", "draft", "release");
        schema.packages.add(pkg("http://example.org/person", "draft", "fp-draft"));
        schema.packages.add(pkg("http://example.org/person", "release", "fp-release"));

        Map<Attributes, Long> points = AtlasTelemetry.packageSeries(snapshot(scope("jena", schema)), ATLAS);

        assertEquals(2, points.size(), "one nsURI, two live versions, two series");
    }

    @Test
    @DisplayName("a null field becomes an empty attribute rather than failing the collection")
    void nullsDoNotBreakACollection() {
        RegistryRuntimeDTO schema = registry("schema", "draft");
        schema.packages.add(pkg(null, null, null));

        Map<Attributes, Long> points = AtlasTelemetry.packageSeries(snapshot(scope(null, schema)), ATLAS);

        assertEquals(1, points.size());
    }

    private static SchemaPackageDTO pkg(String nsURI, String stage, String fingerprint) {
        SchemaPackageDTO dto = new SchemaPackageDTO();
        dto.nsURI = nsURI;
        dto.stage = stage;
        dto.fingerprint = fingerprint;
        return dto;
    }

    private static long valueFor(Map<Attributes, Long> points, String reason) {
        return points.entrySet().stream()
                .filter(entry -> reason.equals(entry.getKey().get(AttributeKey.stringKey("fennec.reason"))))
                .map(Map.Entry::getValue).findFirst().orElseThrow();
    }
}
