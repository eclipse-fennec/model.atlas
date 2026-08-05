/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
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
package org.eclipse.fennec.model.atlas.mgmt.registry;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;

/**
 * Basic in-memory registry helper implementation.
 * 
 * <p>
 * This implementation provides simple in-memory indexing for ObjectMetadata
 * without external dependencies. It's suitable for smaller datasets or when
 * advanced indexing features are not required.
 * </p>
 * 
 * <h3>Features</h3>
 * <ul>
 * <li><strong>In-Memory Storage</strong> - All metadata stored in memory
 * maps</li>
 * <li><strong>Thread-Safe</strong> - Uses ConcurrentHashMap for safe concurrent
 * access</li>
 * <li><strong>Fast Access</strong> - O(1) lookups for direct queries</li>
 * <li><strong>Simple Filtering</strong> - Stream-based filtering; the string query
 * syntax of {@link #searchObjectIds(String, int)} is a documented subset (exact
 * field:value terms), and anything outside it is rejected rather than ignored</li>
 * <li><strong>No Dependencies</strong> - Uses only standard Java
 * collections</li>
 * </ul>
 * 
 * <h3>Performance Characteristics</h3>
 * <ul>
 * <li><strong>updateIndex()</strong> - O(1)</li>
 * <li><strong>removeFromIndex()</strong> - O(1)</li>
 * <li><strong>findByStatus()</strong> - O(n) with stream filtering</li>
 * <li><strong>findByObjectName()</strong> - O(n) with stream filtering</li>
 * <li><strong>findByRole()</strong> - O(n) with stream filtering</li>
 * </ul>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
public class BasicRegistryHelper extends AbstractRegistryHelper {

    /** Matches every indexed object, mirroring Lucene's {@code *:*}. */
    private static final String MATCH_ALL_QUERY = "*:*";

    /**
     * The attributes {@link #searchObjectIds(String, int)} can filter on, under the same
     * names the sibling Lucene index uses for them.
     */
    private static final Map<String, Function<ObjectMetadata, String>> SEARCHABLE_FIELDS = Map.ofEntries(
            Map.entry("objectId", ObjectMetadata::getObjectId),
            Map.entry("objectName", ObjectMetadata::getObjectName),
            Map.entry("stage", ObjectMetadata::getStage),
            Map.entry("scope", ObjectMetadata::getScope),
            Map.entry("registry", ObjectMetadata::getRegistry),
            Map.entry("objectType", ObjectMetadata::getObjectType),
            Map.entry("version", ObjectMetadata::getVersion),
            Map.entry("contentHash", ObjectMetadata::getContentHash),
            Map.entry("fingerprint", ObjectMetadata::getFingerprint),
            Map.entry("uploadUser", ObjectMetadata::getUploadUser),
            Map.entry("reviewUser", ObjectMetadata::getReviewUser),
            Map.entry("lastChangeUser", ObjectMetadata::getLastChangeUser),
            Map.entry("sourceChannel", ObjectMetadata::getSourceChannel),
            Map.entry("complianceStatus", ObjectMetadata::getComplianceStatus),
            Map.entry("governanceDocumentationId", ObjectMetadata::getGovernanceDocumentationId),
            Map.entry("generationTriggerFingerprint", ObjectMetadata::getGenerationTriggerFingerprint),
            Map.entry("status", metadata -> metadata.getStatus() == null ? null : metadata.getStatus().getLiteral()));

    private final Map<String, ObjectMetadata> metadataById = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;

    @Override
    public void initialize() throws IOException {
        // No special initialization needed for in-memory registry
        initialized = true;
    }

    @Override
    public void updateIndex(String objectId, ObjectMetadata metadata) throws IOException {
        Objects.requireNonNull(objectId, "Object ID cannot be null");
        Objects.requireNonNull(metadata, "Metadata cannot be null");

        metadataById.put(objectId, metadata);
    }

    @Override
    public void removeFromIndex(String objectId) throws IOException {
        Objects.requireNonNull(objectId, "Object ID cannot be null");

        metadataById.remove(objectId);
    }

    /**
     * Searches the indexed metadata.
     *
     * <p>
     * The supported query syntax is the subset the sibling {@code LuceneRegistryHelper}
     * emits from its own finders: {@code field:value} terms, values optionally quoted,
     * combined with {@code AND} or {@code OR} (not both in one query) and optionally
     * wrapped in parentheses — for example {@code stage:draft} or
     * {@code (objectName:"My Package" AND stage:draft)}. A {@code null}, blank or
     * {@code *:*} query matches everything. Values are compared for exact equality:
     * this index does no analysis, so there are no wildcards, ranges or fuzzy terms.
     * </p>
     *
     * <p>
     * Anything outside that subset — an unknown field, or a term that is not
     * {@code field:value} — is rejected with an {@link IllegalArgumentException}. A
     * search that cannot honour its filter must not answer with every object it holds;
     * a caller cannot tell that apart from a genuine match-all.
     * </p>
     *
     * @param query      the search query, or {@code null}/blank/{@code *:*} for all
     * @param maxResults maximum number of results, unlimited if not positive
     * @return the ids of the matching objects
     * @throws IllegalArgumentException if the query is outside the supported subset
     */
    @Override
    public List<String> searchObjectIds(String query, int maxResults) throws IOException {
        Predicate<ObjectMetadata> filter = parseQuery(query);
        return metadataById.entrySet().stream().filter(entry -> filter.test(entry.getValue()))
                .map(Map.Entry::getKey).limit(maxResults > 0 ? maxResults : Integer.MAX_VALUE)
                .collect(Collectors.toList());
    }

    private static Predicate<ObjectMetadata> parseQuery(String query) {
        if (query == null || query.isBlank() || MATCH_ALL_QUERY.equals(query.trim())) {
            return metadata -> true;
        }
        boolean anyOf = query.contains(" OR ");
        List<Predicate<ObjectMetadata>> terms = new LinkedList<>();
        for (String term : query.split(anyOf ? " OR " : " AND ")) {
            terms.add(parseTerm(term));
        }
        return anyOf ? metadata -> terms.stream().anyMatch(term -> term.test(metadata))
                : metadata -> terms.stream().allMatch(term -> term.test(metadata));
    }

    private static Predicate<ObjectMetadata> parseTerm(String term) {
        String cleaned = term.trim().replaceAll("^\\(+|\\)+$", "").trim();
        int separator = cleaned.indexOf(':');
        if (separator < 1) {
            throw new IllegalArgumentException(String.format(
                    "Unsupported search term '%s': expected field:value, one of %s", term, SEARCHABLE_FIELDS.keySet()));
        }
        String field = cleaned.substring(0, separator).trim();
        Function<ObjectMetadata, String> reader = SEARCHABLE_FIELDS.get(field);
        if (reader == null) {
            throw new IllegalArgumentException(
                    String.format("Unknown search field '%s': searchable fields are %s", field,
                            SEARCHABLE_FIELDS.keySet()));
        }
        String value = unquote(cleaned.substring(separator + 1).trim());
        return metadata -> value.equals(reader.apply(metadata));
    }

    private static String unquote(String value) {
        if (value.length() > 1 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    @Override
    public List<String> findByStatus(ObjectStatus status) throws IOException {
        Objects.requireNonNull(status, "Status cannot be null");

        return metadataById.entrySet().stream().filter(entry -> status.equals(entry.getValue().getStatus()))
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    @Override
    public List<String> findByObjectName(String objectName) throws IOException {
        Objects.requireNonNull(objectName, "Object name cannot be null");

        return metadataById.entrySet().stream().filter(entry -> objectName.equals(entry.getValue().getObjectName()))
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    @Override
    public List<String> findByStage(String stage) throws IOException {
        Objects.requireNonNull(stage, "Stage cannot be null");

        return metadataById.entrySet().stream().filter(entry -> stage.equals(entry.getValue().getStage()))
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    @Override
    public Optional<String> findByObjectNameAndStage(String objectName, String stage) throws IOException {
        Objects.requireNonNull(objectName, "Object name cannot be null");
        Objects.requireNonNull(stage, "stage cannot be null");

        return metadataById.entrySet().stream().filter(entry -> objectName.equals(entry.getValue().getObjectName())
                && stage.equals(entry.getValue().getStage())).map(Map.Entry::getKey).findFirst();
    }

    @Override
    public List<String> getAllObjectIds() throws IOException {
        return List.copyOf(metadataById.keySet());
    }

    @Override
    public long getObjectCount() throws IOException {
        return metadataById.size();
    }

    @Override
    public boolean exists(String objectId) throws IOException {
        Objects.requireNonNull(objectId, "Object ID cannot be null");

        return metadataById.containsKey(objectId);
    }

    @Override
    public void rebuildIndex() throws IOException {
        // For in-memory registry, we keep existing data
        // This could be enhanced to reload from a data source if needed
    }

    @Override
    public Object getRegistryStatistics() throws IOException {
        Map<String, Object> stats = new HashMap<>();

        stats.put("registryType", getRegistryType());
        stats.put("totalObjects", (long) metadataById.size());
        stats.put("initialized", initialized);

        // Status distribution
        Map<String, Long> statusCounts = metadataById.values().stream()
                .collect(Collectors.groupingBy(
                        metadata -> metadata.getStatus() != null ? metadata.getStatus().getLiteral() : "unknown",
                        Collectors.counting()));
        stats.put("statusDistribution", statusCounts);

        // Stage distribution
        Map<String, Long> stageCounts = metadataById.values().stream().collect(Collectors.groupingBy(
                metadata -> metadata.getStage() != null ? metadata.getStage() : "unknown", Collectors.counting()));
        stats.put("stageDistribution", stageCounts);

        return stats;
    }

    @Override
    public String getRegistryType() {
        return "basic-memory";
    }

    @Override
    public void close() throws Exception {
        super.close();
        metadataById.clear();
        initialized = false;
    }

    /**
     * Get all metadata objects (for testing and debugging).
     * 
     * @return set of all metadata objects
     */
    public Set<ObjectMetadata> getAllMetadata() {
        return Set.copyOf(metadataById.values());
    }
}