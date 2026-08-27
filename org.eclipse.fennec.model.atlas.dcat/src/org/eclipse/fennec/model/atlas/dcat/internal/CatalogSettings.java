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
package org.eclipse.fennec.model.atlas.dcat.internal;

import java.util.List;
import java.util.Optional;

/**
 * What an operator said about one scope's Catalog, as configuration.
 *
 * <p>
 * A value object rather than a live component reference: resolution and mapping both read it, and
 * neither should be able to see it change halfway through.
 * </p>
 *
 * @param id              the portal id to use, or {@code null} to derive it from the scope name
 * @param adopt           the Catalog belongs to somebody else: link into it, never write it
 * @param createIfMissing create an adopted Catalog that is not there. Off by default, because
 *                        minting a Catalog under an id in somebody else's portal is the one
 *                        failure mode with no clean recovery
 * @param title           overrides the scope name
 * @param description     overrides {@code ScopeInfo.description}
 * @param publisherName   overrides the publisher's {@code publisher.name}
 * @param publisherAbout  overrides the publisher's {@code publisher.about}
 * @param licenseUri      {@code dct:license} for the Catalog itself
 * @param homepage        {@code foaf:homepage}
 * @param themes          {@code dcat:theme} IRIs
 * @param keywords        {@code dcat:keyword}s
 * @param invalidReason   why this configuration cannot be used, or {@code null} when it can. A
 *                        broken configuration must refuse its scope rather than fall back to the
 *                        derived case, which would publish a Catalog the operator never asked for
 */
record CatalogSettings(String id, boolean adopt, boolean createIfMissing, String title, String description,
        String publisherName, String publisherAbout, String licenseUri, String homepage, List<String> themes,
        List<String> keywords, String invalidReason) {

    private static final CatalogSettings NONE = new CatalogSettings(null, false, false, null, null, null, null, null,
            null, List.of(), List.of(), null);

    /** No configuration at all: the derived case, and what every scope had before D1a. */
    static CatalogSettings none() {
        return NONE;
    }

    /**
     * @return the settings as configured, carrying an {@link #invalidReason()} when the
     *         combination cannot be honoured
     */
    static CatalogSettings of(ScopeCatalogConfig config) {
        String id = blankToNull(config.catalog_id());
        // An adopted Catalog is identified by the id the operator asserts exists. Without one there
        // is nothing to adopt, and deriving the id instead would quietly make this the configured
        // case — writing a Catalog under a name the operator did not choose.
        String invalid = config.catalog_adopt() && id == null
                ? "catalog.adopt is true but catalog.id is not set, so there is no Catalog to adopt"
                : null;
        return new CatalogSettings(id, config.catalog_adopt(), config.catalog_create_if_missing(),
                blankToNull(config.catalog_title()), blankToNull(config.catalog_description()),
                blankToNull(config.catalog_publisher_name()), blankToNull(config.catalog_publisher_about()),
                blankToNull(config.catalog_license_uri()), blankToNull(config.catalog_homepage()),
                List.of(config.catalog_theme()), List.of(config.catalog_keywords()), invalid);
    }

    boolean valid() {
        return invalidReason == null;
    }

    Optional<String> titleOrEmpty() {
        return Optional.ofNullable(title);
    }

    Optional<String> descriptionOrEmpty() {
        return Optional.ofNullable(description);
    }

    Optional<String> publisherNameOrEmpty() {
        return Optional.ofNullable(publisherName);
    }

    Optional<String> publisherAboutOrEmpty() {
        return Optional.ofNullable(publisherAbout);
    }

    Optional<String> licenseUriOrEmpty() {
        return Optional.ofNullable(licenseUri);
    }

    Optional<String> homepageOrEmpty() {
        return Optional.ofNullable(homepage);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
