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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.rest.application.aggregate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.workflow.ScopeServiceCollector;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Computes a scope-level aggregate validator over every package and every registered EObject in a
 * scope, and the diff between the current state and a previously emitted aggregate ETag.
 *
 * <p>The aggregate {@code ETag} is a strong, order-independent SHA-256 over the identity and content
 * hash of every entry in the scope (across all registries and stages, including inherited
 * parent-final-stage entries). Because a bare hash cannot be reversed into the state it stood for, the
 * service keeps a small, bounded per-scope cache of the recent {@code etag -> manifest} snapshots so it
 * can reconstruct the baseline an old ETag represented and list exactly what changed. This cache is a
 * pure in-memory accelerator: a miss (evicted / never seen / after a restart) simply yields a diff with
 * {@link ScopeDiff#baselineKnown()} {@code == false}, i.e. "re-sync fully" — never a wrong answer.
 *
 * @author Data In Motion
 * @since 1.0
 */
@Component(service = ScopeAggregateService.class)
public class ScopeAggregateService {

    /** Registry under which schema packages live; their changes are reported as nsURIs. */
    static final String SCHEMA_REGISTRY = "schema";

    /** Number of recent manifests retained per scope for diffing against a stale {@code If-None-Match}. */
    static final int MAX_SNAPSHOTS_PER_SCOPE = 16;

    /** Field separator within a manifest key / row (ASCII unit separator). */
    private static final char FIELD_SEP = '';

    /** Row separator between manifest rows when hashing (ASCII record separator). */
    private static final String ROW_SEP = "";

    @Reference
    private ScopeServiceCollector scopeCollector;

    /** scope -&gt; (etag -&gt; manifest). The inner map is a bounded, access-ordered LRU. */
    private final Map<String, Map<String, Map<String, ManifestEntry>>> snapshots = new ConcurrentHashMap<>();

    /** A single scope entry: its identity (registry/objectId, plus nsURI for packages) and content hash. */
    public record ManifestEntry(String registry, String objectId, String nsUri, String contentHash) {
    }

    /** The current aggregate for a scope: its ETag, {@code Last-Modified}, and backing manifest. */
    public record ScopeAggregate(String etag, Instant lastModified, Map<String, ManifestEntry> manifest) {
    }

    /**
     * The diff between a prior aggregate and the current one. {@code baselineKnown} is {@code false}
     * when the prior aggregate could not be reconstructed (so no exact diff is available).
     */
    public record ScopeDiff(List<String> changedNsUris, List<String> changedObjects, boolean baselineKnown) {
    }

    /**
     * Builds the current aggregate for a scope, caching its manifest so a later request citing this
     * ETag can be diffed.
     *
     * @param scopeName the scope name
     * @return the aggregate, or {@code null} if the scope is unknown
     */
    public ScopeAggregate computeAggregate(String scopeName) {
        ScopeService<?> scopeService = scopeCollector.getScopeServiceByScopeName(scopeName);
        if (scopeService == null) {
            return null;
        }
        Map<String, ManifestEntry> manifest = new LinkedHashMap<>();
        Instant lastModified = null;
        for (String registry : scopeService.getAllRegistries()) {
            for (ObjectMetadata md : scopeService.listAllForRegistry(registry)) {
                manifest.put(manifestKey(md), toEntry(md));
                Instant t = md.getLastChangeTime();
                if (t != null && (lastModified == null || t.isAfter(lastModified))) {
                    lastModified = t;
                }
            }
        }
        String etag = aggregateEtag(manifest);
        cache(scopeName, etag, manifest);
        return new ScopeAggregate(etag, lastModified, manifest);
    }

    /**
     * Computes the diff between the baseline an {@code If-None-Match} ETag stood for and the current
     * aggregate. Returns an empty, {@code baselineKnown == false} diff when the baseline is unknown.
     *
     * @param scopeName    the scope name
     * @param ifNoneMatch  the request's {@code If-None-Match} header value (may be {@code null})
     * @param current      the current aggregate (from {@link #computeAggregate(String)})
     */
    public ScopeDiff diffSince(String scopeName, String ifNoneMatch, ScopeAggregate current) {
        String baselineTag = firstTag(ifNoneMatch);
        Map<String, ManifestEntry> baseline = null;
        if (baselineTag != null) {
            Map<String, Map<String, ManifestEntry>> perScope = snapshots.get(scopeName);
            if (perScope != null) {
                baseline = perScope.get(baselineTag);
            }
        }
        if (baseline == null) {
            return new ScopeDiff(List.of(), List.of(), false);
        }
        SortedSet<String> nsUris = new TreeSet<>();
        SortedSet<String> objects = new TreeSet<>();
        Map<String, ManifestEntry> cur = current.manifest();
        // Added or changed: present in current, absent or content-different in the baseline.
        for (Map.Entry<String, ManifestEntry> e : cur.entrySet()) {
            ManifestEntry old = baseline.get(e.getKey());
            if (old == null || !Objects.equals(old.contentHash(), e.getValue().contentHash())) {
                record(e.getValue(), nsUris, objects);
            }
        }
        // Removed: present in the baseline, absent in current.
        for (Map.Entry<String, ManifestEntry> e : baseline.entrySet()) {
            if (!cur.containsKey(e.getKey())) {
                record(e.getValue(), nsUris, objects);
            }
        }
        return new ScopeDiff(List.copyOf(nsUris), List.copyOf(objects), true);
    }

    /**
     * Whether an {@code If-None-Match} header matches the current aggregate ETag (handles {@code *},
     * comma-separated lists, weak prefixes and quotes).
     */
    public boolean matchesIfNoneMatch(String ifNoneMatch, String etag) {
        if (ifNoneMatch == null || etag == null) {
            return false;
        }
        String header = ifNoneMatch.trim();
        if ("*".equals(header)) {
            return true;
        }
        for (String token : header.split(",")) {
            if (etag.equals(normalize(token))) {
                return true;
            }
        }
        return false;
    }

    private static void record(ManifestEntry entry, Set<String> nsUris, Set<String> objects) {
        if (SCHEMA_REGISTRY.equals(entry.registry()) && entry.nsUri() != null) {
            nsUris.add(entry.nsUri());
        } else {
            objects.add(entry.registry() + "/" + entry.objectId());
        }
    }

    private static ManifestEntry toEntry(ObjectMetadata md) {
        Object nsUri = md.getProperties() == null ? null : md.getProperties().get("nsUri");
        return new ManifestEntry(md.getRegistry(), md.getObjectId(), nsUri == null ? null : nsUri.toString(),
                md.getContentHash());
    }

    /** Unique key for an entry across the whole scope: registry + stage + objectId. */
    private static String manifestKey(ObjectMetadata md) {
        return md.getRegistry() + FIELD_SEP + md.getStage() + FIELD_SEP + md.getObjectId();
    }

    /**
     * The strong aggregate ETag: a SHA-256 over each entry's {@code key + contentHash}, sorted, so it is
     * deterministic and independent of the order the entries were discovered in. Package-private and
     * pure for direct unit testing.
     */
    static String aggregateEtag(Map<String, ManifestEntry> manifest) {
        List<String> rows = new ArrayList<>(manifest.size());
        for (Map.Entry<String, ManifestEntry> e : manifest.entrySet()) {
            String contentHash = e.getValue().contentHash();
            rows.add(e.getKey() + FIELD_SEP + (contentHash == null ? "" : contentHash));
        }
        Collections.sort(rows);
        String joined = String.join(ROW_SEP, rows);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(joined.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is a required JCE algorithm; fall back to a stable string hash if it is absent.
            return Integer.toHexString(joined.hashCode());
        }
    }

    private void cache(String scopeName, String etag, Map<String, ManifestEntry> manifest) {
        Map<String, Map<String, ManifestEntry>> perScope = snapshots.computeIfAbsent(scopeName,
                s -> Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected boolean removeEldestEntry(Map.Entry<String, Map<String, ManifestEntry>> eldest) {
                        return size() > MAX_SNAPSHOTS_PER_SCOPE;
                    }
                }));
        perScope.put(etag, manifest);
    }

    /** Normalizes a single tag: strips a weak {@code W/} prefix and surrounding quotes. */
    private static String normalize(String tag) {
        String t = tag.trim();
        if (t.startsWith("W/")) {
            t = t.substring(2).trim();
        }
        return t.replace("\"", "");
    }

    /** The first usable baseline tag from an {@code If-None-Match} value, or {@code null} for none/{@code *}. */
    private static String firstTag(String ifNoneMatch) {
        if (ifNoneMatch == null) {
            return null;
        }
        String header = ifNoneMatch.trim();
        if (header.isEmpty() || "*".equals(header)) {
            return null;
        }
        return normalize(header.split(",")[0]);
    }
}
