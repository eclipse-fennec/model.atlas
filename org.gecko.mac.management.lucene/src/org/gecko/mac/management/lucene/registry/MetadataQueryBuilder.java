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
 *      Mark Hoffmann - initial API and implementation
 */
package org.gecko.mac.management.lucene.registry;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating Lucene query strings from metadata search criteria.
 * 
 * <p>This utility class provides a fluent API for building complex Lucene queries
 * to search ObjectMetadata fields efficiently. It handles proper escaping and
 * formatting of different field types.</p>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * // Simple user search
 * String query = MetadataQueryBuilder.create()
 *     .uploadUser("john")
 *     .build();
 * 
 * // Complex query with multiple criteria
 * String query = MetadataQueryBuilder.create()
 *     .objectType("EPackage")
 *     .sourceChannel("AI_GENERATOR")
 *     .uploadTimeRange(start, end)
 *     .property("version", "1.0")
 *     .build();
 * 
 * // Text search across multiple fields
 * String query = MetadataQueryBuilder.create()
 *     .anyField("sensor")
 *     .build();
 * }</pre>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 * @see LuceneRegistryHelper
 */
public class MetadataQueryBuilder {

    private final List<String> conditions = new ArrayList<>();
    private boolean useOr = false;

    private MetadataQueryBuilder() {
        // Private constructor for builder pattern
    }

    /**
     * Creates a new query builder instance.
     * 
     * @return new MetadataQueryBuilder
     */
    public static MetadataQueryBuilder create() {
        return new MetadataQueryBuilder();
    }

    /**
     * Sets the query to use OR logic instead of default AND logic.
     * 
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder or() {
        this.useOr = true;
        return this;
    }

    /**
     * Adds a condition for upload user.
     * Note: This field uses exact match (StringField), so no escaping is applied.
     * 
     * @param user the upload user to search for
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder uploadUser(String user) {
        if (user != null && !user.isEmpty()) {
            conditions.add(LuceneRegistryHelper.FIELD_UPLOAD_USER + ":" + user);
        }
        return this;
    }

    /**
     * Adds a condition for review user.
     * Note: This field uses exact match (StringField), so no escaping is applied.
     * 
     * @param user the review user to search for
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder reviewUser(String user) {
        if (user != null && !user.isEmpty()) {
            conditions.add(LuceneRegistryHelper.FIELD_REVIEW_USER + ":" + user);
        }
        return this;
    }

    /**
     * Adds a condition for review reason.
     * 
     * @param reason the review reason to search for
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder reviewReason(String reason) {
        if (reason != null && !reason.isEmpty()) {
            conditions.add(LuceneRegistryHelper.FIELD_REVIEW_REASON + ":" + escapeValue(reason));
        }
        return this;
    }

    /**
     * Adds a condition for any user (upload, review, or last change).
     * Note: User fields use exact match (StringField), so no escaping is applied.
     * 
     * @param user the user to search for
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder anyUser(String user) {
        if (user != null && !user.isEmpty()) {
            conditions.add("(" + LuceneRegistryHelper.FIELD_UPLOAD_USER + ":" + user + 
                          " OR " + LuceneRegistryHelper.FIELD_REVIEW_USER + ":" + user +
                          " OR " + LuceneRegistryHelper.FIELD_LAST_CHANGE_USER + ":" + user + ")");
        }
        return this;
    }

    /**
     * Adds a condition for source channel.
     * 
     * @param channel the source channel (e.g., AI_GENERATOR, MANUAL_UPLOAD)
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder sourceChannel(String channel) {
        if (channel != null && !channel.isEmpty()) {
            conditions.add(LuceneRegistryHelper.FIELD_SOURCE_CHANNEL + ":" + escapeValue(channel));
        }
        return this;
    }

    /**
     * Adds a condition for object type.
     * 
     * @param type the object type (e.g., EPackage, Route, SensorModel)
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder objectType(String type) {
        if (type != null && !type.isEmpty()) {
            conditions.add(LuceneRegistryHelper.FIELD_OBJECT_TYPE + ":" + escapeValue(type));
        }
        return this;
    }

    /**
     * Adds a condition for content hash (exact match).
     * 
     * @param hash the content hash
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder contentHash(String hash) {
        if (hash != null && !hash.isEmpty()) {
            conditions.add(LuceneRegistryHelper.FIELD_CONTENT_HASH + ":" + hash);
        }
        return this;
    }

    /**
     * Adds a condition for upload time range.
     * Note: For optimal performance, use LuceneRegistryHelper.searchByUploadTimeRange() directly
     * instead of building string queries for time ranges.
     * 
     * @param start the start time (inclusive)
     * @param end the end time (inclusive)
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder uploadTimeRange(Instant start, Instant end) {
        if (start != null && end != null) {
            long startMillis = start.toEpochMilli();
            long endMillis = end.toEpochMilli();
            conditions.add(LuceneRegistryHelper.FIELD_UPLOAD_TIME + ":[" + startMillis + " TO " + endMillis + "]");
        }
        return this;
    }

    /**
     * Adds a condition for upload time after the specified time.
     * 
     * @param after the time after which to search
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder uploadTimeAfter(Instant after) {
        if (after != null) {
            long afterMillis = after.toEpochMilli();
            conditions.add(LuceneRegistryHelper.FIELD_UPLOAD_TIME + ":[" + afterMillis + " TO *]");
        }
        return this;
    }

    /**
     * Adds a condition for upload time before the specified time.
     * 
     * @param before the time before which to search
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder uploadTimeBefore(Instant before) {
        if (before != null) {
            long beforeMillis = before.toEpochMilli();
            conditions.add(LuceneRegistryHelper.FIELD_UPLOAD_TIME + ":[* TO " + beforeMillis + "]");
        }
        return this;
    }

    /**
     * Adds a condition for review time range.
     * 
     * @param start the start time (inclusive)
     * @param end the end time (inclusive)
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder reviewTimeRange(Instant start, Instant end) {
        if (start != null && end != null) {
            long startMillis = start.toEpochMilli();
            long endMillis = end.toEpochMilli();
            conditions.add(LuceneRegistryHelper.FIELD_REVIEW_TIME + ":[" + startMillis + " TO " + endMillis + "]");
        }
        return this;
    }

    /**
     * Adds a condition for compliance check time range.
     * 
     * @param start the start time (inclusive)
     * @param end the end time (inclusive)
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder complianceCheckTimeRange(Instant start, Instant end) {
        if (start != null && end != null) {
            long startMillis = start.toEpochMilli();
            long endMillis = end.toEpochMilli();
            conditions.add(LuceneRegistryHelper.FIELD_COMPLIANCE_CHECK_TIME + ":[" + startMillis + " TO " + endMillis + "]");
        }
        return this;
    }

    /**
     * Adds a condition for last change time range.
     * 
     * @param start the start time (inclusive)
     * @param end the end time (inclusive)
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder lastChangeTimeRange(Instant start, Instant end) {
        if (start != null && end != null) {
            long startMillis = start.toEpochMilli();
            long endMillis = end.toEpochMilli();
            conditions.add(LuceneRegistryHelper.FIELD_LAST_CHANGE_TIME + ":[" + startMillis + " TO " + endMillis + "]");
        }
        return this;
    }

    /**
     * Adds a condition for generation trigger fingerprint.
     * Note: This field uses exact match (StringField), so no escaping is applied.
     * 
     * @param fingerprint the JSON fingerprint that triggered generation
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder generationTriggerFingerprint(String fingerprint) {
        if (fingerprint != null && !fingerprint.isEmpty()) {
            // Use quotes for exact match on StringField
            conditions.add(LuceneRegistryHelper.FIELD_GENERATION_TRIGGER_FINGERPRINT + ":\"" + fingerprint + "\"");
        }
        return this;
    }

    /**
     * Adds a condition for compliance status.
     * 
     * @param status the compliance status (e.g., COMPLIANT, NON_COMPLIANT, PENDING)
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder complianceStatus(String status) {
        if (status != null && !status.isEmpty()) {
            conditions.add(LuceneRegistryHelper.FIELD_COMPLIANCE_STATUS + ":" + escapeValue(status));
        }
        return this;
    }

    /**
     * Adds a condition for governance documentation ID.
     * Note: This field uses exact match (StringField), so no escaping is applied.
     * 
     * @param documentationId the governance documentation reference
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder governanceDocumentationId(String documentationId) {
        if (documentationId != null && !documentationId.isEmpty()) {
            // Use quotes for exact match on StringField
            conditions.add(LuceneRegistryHelper.FIELD_GOVERNANCE_DOCUMENTATION_ID + ":\"" + documentationId + "\"");
        }
        return this;
    }

    /**
     * Adds a condition for last change user.
     * Note: This field uses exact match (StringField), so no escaping is applied.
     * 
     * @param user the user who last modified the object
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder lastChangeUser(String user) {
        if (user != null && !user.isEmpty()) {
            conditions.add(LuceneRegistryHelper.FIELD_LAST_CHANGE_USER + ":" + user);
        }
        return this;
    }

    /**
     * Adds a condition for custom property.
     * 
     * @param key the property key
     * @param value the property value
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder property(String key, String value) {
        if (key != null && !key.isEmpty() && value != null && !value.isEmpty()) {
            conditions.add(LuceneRegistryHelper.FIELD_PROPERTIES + ":" + 
                          escapeValue(key + ":" + value));
        }
        return this;
    }

    /**
     * Adds a condition for property key existence.
     * 
     * @param key the property key to check for existence
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder hasProperty(String key) {
        if (key != null && !key.isEmpty()) {
            conditions.add(LuceneRegistryHelper.FIELD_PROPERTIES + ":" + escapeValue(key + ":*"));
        }
        return this;
    }

    /**
     * Adds a text search condition that searches across multiple fields.
     * Note: User fields use exact match, while other fields use analyzed search.
     * 
     * @param text the text to search for
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder anyField(String text) {
        if (text != null && !text.isEmpty()) {
            String escapedText = escapeValue(text);
            conditions.add("(" +
                LuceneRegistryHelper.FIELD_UPLOAD_USER + ":" + text + " OR " + // Exact match for user fields
                LuceneRegistryHelper.FIELD_SOURCE_CHANNEL + ":" + escapedText + " OR " +
                LuceneRegistryHelper.FIELD_OBJECT_TYPE + ":" + escapedText + " OR " +
                LuceneRegistryHelper.FIELD_OBJECT_NAME + ":" + escapedText + " OR " +
                LuceneRegistryHelper.FIELD_REVIEW_USER + ":" + text + " OR " + // Exact match for user fields
                LuceneRegistryHelper.FIELD_REVIEW_REASON + ":" + escapedText + " OR " +
                LuceneRegistryHelper.FIELD_COMPLIANCE_STATUS + ":" + escapedText + " OR " +
                LuceneRegistryHelper.FIELD_LAST_CHANGE_USER + ":" + text + " OR " + // Exact match for user fields
                LuceneRegistryHelper.FIELD_PROPERTIES + ":" + escapedText +
                ")");
        }
        return this;
    }

    /**
     * Adds a custom Lucene condition.
     * 
     * @param condition the raw Lucene query condition
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder custom(String condition) {
        if (condition != null && !condition.isEmpty()) {
            conditions.add(condition);
        }
        return this;
    }

    /**
     * Adds a wildcard search condition for upload user.
     * Uses the analyzed field for wildcard matching.
     * 
     * @param pattern the wildcard pattern (e.g., "user*", "*admin")
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder uploadUserWildcard(String pattern) {
        if (pattern != null && !pattern.isEmpty()) {
            conditions.add(LuceneRegistryHelper.FIELD_UPLOAD_USER_TEXT + ":" + pattern);
        }
        return this;
    }

    /**
     * Adds a wildcard search condition for review user.
     * Uses the analyzed field for wildcard matching.
     * 
     * @param pattern the wildcard pattern (e.g., "user*", "*admin")
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder reviewUserWildcard(String pattern) {
        if (pattern != null && !pattern.isEmpty()) {
            conditions.add(LuceneRegistryHelper.FIELD_REVIEW_USER_TEXT + ":" + pattern);
        }
        return this;
    }

    /**
     * Adds a wildcard search condition for last change user.
     * Uses the analyzed field for wildcard matching.
     * 
     * @param pattern the wildcard pattern (e.g., "user*", "*admin")
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder lastChangeUserWildcard(String pattern) {
        if (pattern != null && !pattern.isEmpty()) {
            conditions.add(LuceneRegistryHelper.FIELD_LAST_CHANGE_USER_TEXT + ":" + pattern);
        }
        return this;
    }

    /**
     * Adds a fuzzy search condition for upload user.
     * Uses the analyzed field for fuzzy matching.
     * 
     * @param user the user name for fuzzy search
     * @param maxEdits the maximum edit distance (1 or 2)
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder uploadUserFuzzy(String user, int maxEdits) {
        if (user != null && !user.isEmpty() && (maxEdits == 1 || maxEdits == 2)) {
            conditions.add(LuceneRegistryHelper.FIELD_UPLOAD_USER_TEXT + ":" + user + "~" + maxEdits);
        }
        return this;
    }

    /**
     * Adds a fuzzy search condition for review user.
     * Uses the analyzed field for fuzzy matching.
     * 
     * @param user the user name for fuzzy search
     * @param maxEdits the maximum edit distance (1 or 2)
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder reviewUserFuzzy(String user, int maxEdits) {
        if (user != null && !user.isEmpty() && (maxEdits == 1 || maxEdits == 2)) {
            conditions.add(LuceneRegistryHelper.FIELD_REVIEW_USER_TEXT + ":" + user + "~" + maxEdits);
        }
        return this;
    }

    /**
     * Adds a fuzzy search condition for last change user.
     * Uses the analyzed field for fuzzy matching.
     * 
     * @param user the user name for fuzzy search
     * @param maxEdits the maximum edit distance (1 or 2)
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder lastChangeUserFuzzy(String user, int maxEdits) {
        if (user != null && !user.isEmpty() && (maxEdits == 1 || maxEdits == 2)) {
            conditions.add(LuceneRegistryHelper.FIELD_LAST_CHANGE_USER_TEXT + ":" + user + "~" + maxEdits);
        }
        return this;
    }

    /**
     * Adds a condition for object lifecycle status.
     * 
     * @param status the ObjectStatus value (e.g., DRAFT, APPROVED, DEPLOYED)
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder status(String status) {
        if (status != null && !status.isEmpty()) {
            conditions.add(LuceneRegistryHelper.FIELD_STATUS + ":" + status);
        }
        return this;
    }

    /**
     * Adds a condition for object version (exact match).
     * 
     * @param version the version string (e.g., "1.0.0")
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder version(String version) {
        if (version != null && !version.isEmpty()) {
            conditions.add(LuceneRegistryHelper.FIELD_VERSION + ":" + escapeValue(version));
        }
        return this;
    }

    /**
     * Adds a wildcard condition for object version (e.g., "1.*" for all 1.x versions).
     * 
     * @param pattern the version pattern with wildcards (e.g., "1.*", "2.0.*")
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder versionWildcard(String pattern) {
        if (pattern != null && !pattern.isEmpty()) {
            conditions.add(LuceneRegistryHelper.FIELD_VERSION + ":" + pattern);
        }
        return this;
    }

    /**
     * Adds a condition for object reference URI.
     * 
     * @param objectRefUri the EObject URI string
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder objectRef(String objectRefUri) {
        if (objectRefUri != null && !objectRefUri.isEmpty()) {
            // Don't escape objectRef URIs since they use StringField for exact matching
            conditions.add(LuceneRegistryHelper.FIELD_OBJECT_REF + ":" + objectRefUri);
        }
        return this;
    }

    /**
     * Adds a condition for object metadata ID.
     * 
     * @param objectMetadataId the unique metadata identifier
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder objectMetadataId(String objectMetadataId) {
        if (objectMetadataId != null && !objectMetadataId.isEmpty()) {
            // Don't escape metadata IDs since they use StringField for exact matching
            conditions.add(LuceneRegistryHelper.FIELD_OBJECT_METADATA_ID + ":" + objectMetadataId);
        }
        return this;
    }

    /**
     * Adds a condition for object name.
     * 
     * @param objectName the human-readable object name
     * @return this builder for method chaining
     */
    public MetadataQueryBuilder objectName(String objectName) {
        if (objectName != null && !objectName.isEmpty()) {
            conditions.add(LuceneRegistryHelper.FIELD_OBJECT_NAME + ":" + escapeValue(objectName));
        }
        return this;
    }

    /**
     * Builds the final Lucene query string.
     * 
     * @return the Lucene query string, or "*:*" if no conditions were added
     */
    public String build() {
        if (conditions.isEmpty()) {
            return "*:*"; // Match all documents
        }
        
        String operator = useOr ? " OR " : " AND ";
        return String.join(operator, conditions);
    }

    /**
     * Escapes special Lucene characters in field values.
     * 
     * @param value the value to escape
     * @return the escaped value
     */
    private String escapeValue(String value) {
        if (value == null) {
            return "";
        }
        
        // Escape special Lucene characters
        String escaped = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace("~", "\\~")
            .replace("*", "\\*")
            .replace("?", "\\?")
            .replace(":", "\\:")
            .replace("^", "\\^")
            .replace("-", "\\-")
            .replace("+", "\\+");
        
        // Quote if contains spaces
        if (escaped.contains(" ")) {
            return "\"" + escaped + "\"";
        }
        
        return escaped;
    }
}