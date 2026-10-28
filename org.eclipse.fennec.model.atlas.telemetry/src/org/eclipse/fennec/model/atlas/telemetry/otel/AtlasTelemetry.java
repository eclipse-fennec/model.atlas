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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;
import java.util.logging.Logger;

import org.eclipse.fennec.model.atlas.runtime.info.AtlasRuntime;
import org.eclipse.fennec.model.atlas.runtime.info.AtlasRuntimeDTO;
import org.eclipse.fennec.model.atlas.runtime.info.FailureReasons;
import org.eclipse.fennec.model.atlas.runtime.info.RegistryRuntimeDTO;
import org.eclipse.fennec.model.atlas.runtime.info.SchemaPackageDTO;
import org.eclipse.fennec.model.atlas.runtime.info.ScopeRuntimeDTO;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.metrics.ObservableLongGauge;

/**
 * What this Atlas holds, as metrics.
 *
 * <p>
 * This is the bundle the runtime service was built for. It binds whatever Atlas runtime service is
 * in the framework and turns it into a handful of gauges. It reaches into nothing: everything here
 * comes from a DTO the Atlas itself decided to hand out.
 * </p>
 *
 * <p>
 * <strong>Gauges are observed, not pushed.</strong> An instrument asks the runtime for a snapshot
 * when the exporter collects, which is the OpenTelemetry idiom and the honest one: a number
 * recorded when it changed would be a number that stops being true between changes, and an Atlas
 * that goes quiet would export a stale reading forever.
 * </p>
 *
 * <p>
 * The change count is still worth having, and it is not a cache key here: it says <em>how often</em>
 * the Atlas changed, which no gauge over the current state can answer — a scope that comes and goes
 * between two collections looks unchanged to every one of them.
 * </p>
 *
 * <p>
 * <strong>It requires its configuration.</strong> Metrics cost money per series, and
 * {@link TelemetryConfig#packageSeries()} is the switch that decides whether this Atlas reports an
 * unbounded number of them — that is a deployment's decision, and a component that starts without
 * one has made it on the deployment's behalf. So no configuration, no gauges: the same stance the
 * OpenTelemetry sender below this component takes, which is why the local runtime ships
 * {@code configs/telemetry.json} for both. The cost of
 * {@link ConfigurationPolicy#REQUIRE} is that adding this bundle without that file is silent, and
 * it is the right way round — telemetry that is quiet is a missing graph, telemetry that defaults
 * itself on is a bill.
 * </p>
 */
@Component(configurationPid = "org.eclipse.fennec.model.atlas.telemetry",
        configurationPolicy = ConfigurationPolicy.REQUIRE, immediate = true)
public class AtlasTelemetry {

    private static final Logger LOG = Logger.getLogger(AtlasTelemetry.class.getName());

    private static final AttributeKey<String> ATLAS = AttributeKey.stringKey("fennec.atlas");
    private static final AttributeKey<String> SCOPE = AttributeKey.stringKey("fennec.scope");
    private static final AttributeKey<String> REGISTRY = AttributeKey.stringKey("fennec.registry");
    private static final AttributeKey<String> STAGE = AttributeKey.stringKey("fennec.stage");
    private static final AttributeKey<String> REASON = AttributeKey.stringKey("fennec.reason");
    private static final AttributeKey<String> NSURI = AttributeKey.stringKey("fennec.nsuri");
    private static final AttributeKey<String> FINGERPRINT = AttributeKey.stringKey("fennec.fingerprint");

    /**
     * Where the instruments come from, taken in the constructor.
     *
     * <p>
     * Not a field reference, and that is the lesson this class inherits: DS binds the dynamic
     * reference below while it is building the component, and a field reference may not be injected
     * yet at that moment. A constructor parameter is the one thing that is certainly there before
     * any bind method runs — which is why the configuration is taken the same way, see
     * {@link #apply}.
     * </p>
     */
    private final MeterProvider meters;

    /** What is watched, and the instruments watching it. */
    private final Map<AtlasRuntime, Watched> watched = new ConcurrentHashMap<>();

    /**
     * The instrumentation scope, configured before anything can be bound.
     *
     * <p>
     * The value here is the same default {@link TelemetryConfig#scope()} declares, so it is what
     * holds when a configuration states nothing; it is not a placeholder for an unconfigured
     * window, because there is none — see {@link #apply}.
     * </p>
     */
    private volatile String scope = "org.eclipse.fennec.model.atlas";

    /** Whether one series per package is reported; off until configured otherwise. */
    private volatile boolean packageSeries = false;

    @Activate
    public AtlasTelemetry(@Reference MeterProvider meters, TelemetryConfig config) {
        this.meters = meters;
        apply(config);
    }

    @Modified
    void modify(TelemetryConfig config) {
        apply(config);
    }

    /**
     * Takes the configuration — at construction, and again whenever it is written.
     *
     * <p>
     * <strong>One entry point on the way in, on purpose.</strong> A component can declare both an
     * {@code @Activate} constructor and an {@code @Activate} method, and then it has two of them
     * with the references bound <em>between</em> the two: DS constructs at step 4 of activation,
     * binds at step 6 and calls the activate method at step 7 (112.5.6). A runtime bound in that
     * window would have had its gauges built against whatever the configuration had not been
     * applied to yet, and would depend on the rebuild below to put it right. Taking the
     * configuration as a constructor parameter removes the window rather than coping with it.
     * </p>
     *
     * <p>
     * A write that changes neither value is a no-op: {@link Modified} is called for any write to
     * the PID, including one that sets the same values back, and rebuilding would throw away live
     * instruments for nothing.
     * </p>
     */
    private void apply(TelemetryConfig config) {
        if (config.scope().equals(scope) && config.packageSeries() == packageSeries) {
            return;
        }
        this.scope = config.scope();
        this.packageSeries = config.packageSeries();
        // A new scope is a new meter, and the switch decides which instruments there are, so
        // either way they are built again.
        for (Watched entry : watched.values()) {
            entry.rebuild();
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void setRuntime(AtlasRuntime runtime, Map<String, Object> properties) {
        AtlasRuntimeDTO first = runtime.snapshot();
        Attributes atlas = Attributes.of(ATLAS, first.name == null ? "atlas" : first.name);
        watched.put(runtime, new Watched(() -> gauges(runtime, atlas)));
        LOG.info("[Atlas] watching runtime '" + first.name + "'");
    }

    /**
     * The Atlas said something changed.
     *
     * <p>
     * Nothing to re-register — the gauges read the current snapshot on collection. What this is for
     * is the record that a change happened at all.
     * </p>
     *
     * <p>
     * Named {@code updated}, not {@code modified}: DS calls the first when a <em>bound service's</em>
     * properties change and the second when the <em>component's configuration</em> does. A watcher
     * that writes {@code modified} here compiles, binds, and is never told anything.
     * </p>
     */
    void updatedRuntime(AtlasRuntime runtime, Map<String, Object> properties) {
        LOG.fine(() -> "[Atlas] runtime changed, now at " + properties.get("service.changecount"));
    }

    void unsetRuntime(AtlasRuntime runtime) {
        Watched entry = watched.remove(runtime);
        if (entry != null) {
            entry.close();
        }
    }

    @Deactivate
    void deactivate() {
        for (AtlasRuntime runtime : List.copyOf(watched.keySet())) {
            unsetRuntime(runtime);
        }
    }

    private List<ObservableLongGauge> gauges(AtlasRuntime runtime, Attributes atlas) {
        List<ObservableLongGauge> gauges = new ArrayList<>();
        gauges.add(gauge("fennec.atlas.scopes", "{scope}", "Live scopes this Atlas holds.", atlas,
                runtime::snapshot, snapshot -> size(snapshot.scopes)));
        gauges.add(gauge("fennec.atlas.changes", "{change}",
                "How often this Atlas's state has changed since it started.", atlas, runtime::snapshot,
                snapshot -> snapshot.changeCount));
        gauges.add(gauge("fennec.atlas.mediatypes", "{mediatype}", "Media types the REST layer can serve.", atlas,
                runtime::snapshot, snapshot -> size(snapshot.mediaTypes)));

        gauges.add(series("fennec.atlas.registries", "{registry}", "Registries per scope.", runtime::snapshot,
                snapshot -> registriesPerScope(snapshot, atlas)));
        gauges.add(series("fennec.atlas.packages", "{package}",
                "Schema packages per scope, schema registry and stage.",
                runtime::snapshot, snapshot -> packagesPerStage(snapshot, atlas)));
        gauges.add(series("fennec.atlas.failures", "{failure}",
                "Configured things that are not live, by reason — the gap between the configuration and the Atlas.",
                runtime::snapshot, snapshot -> failuresPerReason(snapshot, atlas)));

        if (packageSeries) {
            gauges.add(series("fennec.atlas.package", "{package}",
                    "One per schema package: which version of which nsURI is live in which stage.",
                    runtime::snapshot, snapshot -> packageSeries(snapshot, atlas)));
        }
        return gauges;
    }

    // The point builders are package-private rather than private so they can be tested against a
    // fabricated snapshot: what they turn a DTO into is the whole behaviour of this class, and it
    // should not take a meter provider and an exporter to check it.
    static Map<Attributes, Long> registriesPerScope(AtlasRuntimeDTO snapshot, Attributes atlas) {
        Map<Attributes, Long> points = new HashMap<>();
        for (ScopeRuntimeDTO scope : nullSafe(snapshot.scopes)) {
            points.put(atlas.toBuilder().put(SCOPE, text(scope.name)).build(), (long) size(scope.registries));
        }
        return points;
    }

    static Map<Attributes, Long> packagesPerStage(AtlasRuntimeDTO snapshot, Attributes atlas) {
        Map<Attributes, Long> points = new HashMap<>();
        for (ScopeRuntimeDTO scope : nullSafe(snapshot.scopes)) {
            for (RegistryRuntimeDTO registry : nullSafe(scope.registries)) {
                // Only a schema registry can hold packages. A transformation registry reporting
                // zero of them is not a reading, it is a category error that looks like a finding
                // — and it is a series somebody pays for on every collection.
                if (!RegistryRuntimeDTO.TYPE_SCHEMA.equals(registry.type)) {
                    continue;
                }
                for (var stage : nullSafe(registry.stages)) {
                    Attributes attributes = atlas.toBuilder().put(SCOPE, text(scope.name))
                            .put(REGISTRY, text(registry.name)).put(STAGE, text(stage.name)).build();
                    points.put(attributes, (long) stage.packageCount);
                }
            }
        }
        return points;
    }

    /**
     * Failures by reason, with a zero for every reason that is currently clear.
     *
     * <p>
     * The zeros matter: a reason that is only reported when it occurs leaves a gap in the series
     * rather than a drop to zero, and a gap is indistinguishable from an exporter that stopped.
     * </p>
     */
    static Map<Attributes, Long> failuresPerReason(AtlasRuntimeDTO snapshot, Attributes atlas) {
        Map<String, Long> byReason = new HashMap<>();
        for (String reason : FailureReasons.ALL) {
            byReason.put(reason, 0L);
        }
        for (var failed : nullSafe(snapshot.failedScopes)) {
            byReason.merge(text(failed.reason), 1L, Long::sum);
        }
        for (var failed : nullSafe(snapshot.failedRegistries)) {
            byReason.merge(text(failed.reason), 1L, Long::sum);
        }
        for (var failed : nullSafe(snapshot.failedPackages)) {
            byReason.merge(text(failed.reason), 1L, Long::sum);
        }
        Map<Attributes, Long> points = new HashMap<>();
        byReason.forEach((reason, count) -> points.put(atlas.toBuilder().put(REASON, reason).build(), count));
        return points;
    }

    static Map<Attributes, Long> packageSeries(AtlasRuntimeDTO snapshot, Attributes atlas) {
        Map<Attributes, Long> points = new HashMap<>();
        for (ScopeRuntimeDTO scope : nullSafe(snapshot.scopes)) {
            for (RegistryRuntimeDTO registry : nullSafe(scope.registries)) {
                for (SchemaPackageDTO pkg : nullSafe(registry.packages)) {
                    Attributes attributes = atlas.toBuilder().put(SCOPE, text(scope.name))
                            .put(REGISTRY, text(registry.name)).put(STAGE, text(pkg.stage))
                            .put(NSURI, text(pkg.nsURI)).put(FINGERPRINT, text(pkg.fingerprint)).build();
                    points.put(attributes, 1L);
                }
            }
        }
        return points;
    }

    private <T> ObservableLongGauge gauge(String name, String unit, String description, Attributes attributes,
            Supplier<T> snapshot, ToLongFunction<T> reading) {
        return meters.get(scope).gaugeBuilder(name).ofLongs().setUnit(unit).setDescription(description)
                .buildWithCallback(measurement -> measurement.record(reading.applyAsLong(snapshot.get()), attributes));
    }

    private <T> ObservableLongGauge series(String name, String unit, String description, Supplier<T> snapshot,
            Function<T, Map<Attributes, Long>> points) {
        return meters.get(scope).gaugeBuilder(name).ofLongs().setUnit(unit).setDescription(description)
                .buildWithCallback(measurement -> points.apply(snapshot.get())
                        .forEach((attributes, value) -> measurement.record(value, attributes)));
    }

    private static <T> List<T> nullSafe(List<T> values) {
        return values == null ? List.of() : values;
    }

    private static int size(List<?> values) {
        return values == null ? 0 : values.size();
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    /** One watched runtime: how to instrument it, and what is live. */
    private final class Watched {

        private final Supplier<List<ObservableLongGauge>> instruments;

        private List<ObservableLongGauge> live;

        Watched(Supplier<List<ObservableLongGauge>> instruments) {
            this.instruments = instruments;
            this.live = instruments.get();
        }

        synchronized void rebuild() {
            close();
            this.live = instruments.get();
        }

        synchronized void close() {
            for (ObservableLongGauge gauge : live) {
                gauge.close();
            }
            live = List.of();
        }
    }
}
