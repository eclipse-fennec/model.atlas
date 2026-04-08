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
package org.eclipse.fennec.model.atlas.management.lucene.epackage;

import java.util.Set;

/**
 * Builder for creating EPackage search queries for the EPackage Lucene index.
 *
 * <p>
 * This utility class provides a fluent API for building search queries
 * against EPackage-specific indexed fields. All filter conditions are AND'd
 * together in the resulting query.
 * </p>
 *
 * <h3>Usage Examples</h3>
 *
 * <pre>{@code
 * // Search by namespace URI across a scope chain
 * EPackageSearchQuery query = EPackageSearchQuery.create()
 *         .scopes(Set.of("tenant-a", "division-x", "atlas"))
 *         .nsUri("sensors")
 *         .limit(20)
 *         .build();
 *
 * // Find packages containing a "Customer" classifier in approved stage
 * EPackageSearchQuery query = EPackageSearchQuery.create()
 *         .scopes(Set.of("tenant-a", "division-x", "atlas"))
 *         .classifier("Customer")
 *         .stage("approved")
 *         .build();
 *
 * // Find packages with a feature named "friend" of type "Person"
 * EPackageSearchQuery query = EPackageSearchQuery.create()
 *         .scopes(Set.of("tenant-a"))
 *         .featureNameTypePair("friend:Person")
 *         .build();
 * }</pre>
 *
 * @author ilenia
 * @since Apr 8, 2026
 * @see EPackageLuceneIndex
 */
public class EPackageSearchQuery {

    private Set<String> scopes;
    private String stage;
    private String nsUri;
    private String nsUriExact;
    private String name;
    private String nsPrefix;
    private String classifier;
    private String featureName;
    private String featureType;
    private String featureNameTypePair;
    private int limit = 50;
    private int offset = 0;

    private EPackageSearchQuery() {
        // Private constructor for builder pattern
    }

    /**
     * Creates a new query builder instance.
     *
     * @return new EPackageSearchQuery builder
     */
    public static EPackageSearchQuery create() {
        return new EPackageSearchQuery();
    }

    /**
     * Sets the scope chain to search across (OR'd).
     *
     * @param scopes the set of scope names (typically the requested scope and its parents)
     * @return this builder for method chaining
     */
    public EPackageSearchQuery scopes(Set<String> scopes) {
        this.scopes = scopes;
        return this;
    }

    /**
     * Sets the stage filter (exact match).
     *
     * @param stage the stage to filter by (e.g., "approved", "draft")
     * @return this builder for method chaining
     */
    public EPackageSearchQuery stage(String stage) {
        this.stage = stage;
        return this;
    }

    /**
     * Sets a partial match filter on namespace URI.
     *
     * @param nsUri the partial namespace URI (e.g., "sensors" matches "http://example.com/sensors/1.0")
     * @return this builder for method chaining
     */
    public EPackageSearchQuery nsUri(String nsUri) {
        this.nsUri = nsUri;
        return this;
    }

    /**
     * Sets an exact match filter on namespace URI.
     *
     * @param nsUriExact the exact namespace URI
     * @return this builder for method chaining
     */
    public EPackageSearchQuery nsUriExact(String nsUriExact) {
        this.nsUriExact = nsUriExact;
        return this;
    }

    /**
     * Sets a partial match filter on package name.
     *
     * @param name the partial package name
     * @return this builder for method chaining
     */
    public EPackageSearchQuery name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets a partial match filter on namespace prefix.
     *
     * @param nsPrefix the partial namespace prefix
     * @return this builder for method chaining
     */
    public EPackageSearchQuery nsPrefix(String nsPrefix) {
        this.nsPrefix = nsPrefix;
        return this;
    }

    /**
     * Sets a full-text search filter on classifier names (EClass, EEnum, EDataType).
     *
     * @param classifier the classifier name to search for
     * @return this builder for method chaining
     */
    public EPackageSearchQuery classifier(String classifier) {
        this.classifier = classifier;
        return this;
    }

    /**
     * Sets a full-text search filter on structural feature names.
     *
     * @param featureName the feature name to search for
     * @return this builder for method chaining
     */
    public EPackageSearchQuery featureName(String featureName) {
        this.featureName = featureName;
        return this;
    }

    /**
     * Sets a full-text search filter on structural feature type names.
     *
     * @param featureType the feature type name to search for (e.g., "EString", "Person")
     * @return this builder for method chaining
     */
    public EPackageSearchQuery featureType(String featureType) {
        this.featureType = featureType;
        return this;
    }

    /**
     * Sets a full-text search filter on combined feature name:type pairs.
     * This enables precise per-feature queries where both the name and type
     * must belong to the same structural feature.
     *
     * @param featureNameTypePair the name:type pair to search for (e.g., "friend:Person")
     * @return this builder for method chaining
     */
    public EPackageSearchQuery featureNameTypePair(String featureNameTypePair) {
        this.featureNameTypePair = featureNameTypePair;
        return this;
    }

    /**
     * Sets the maximum number of results to return.
     *
     * @param limit the result limit (default: 50, max: 500)
     * @return this builder for method chaining
     */
    public EPackageSearchQuery limit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Sets the number of results to skip for pagination.
     *
     * @param offset the result offset (default: 0)
     * @return this builder for method chaining
     */
    public EPackageSearchQuery offset(int offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Builds and returns this query instance.
     *
     * @return the configured EPackageSearchQuery
     */
    public EPackageSearchQuery build() {
        return this;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public String getStage() {
        return stage;
    }

    public String getNsUri() {
        return nsUri;
    }

    public String getNsUriExact() {
        return nsUriExact;
    }

    public String getName() {
        return name;
    }

    public String getNsPrefix() {
        return nsPrefix;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getFeatureName() {
        return featureName;
    }

    public String getFeatureType() {
        return featureType;
    }

    public String getFeatureNameTypePair() {
        return featureNameTypePair;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }
}
