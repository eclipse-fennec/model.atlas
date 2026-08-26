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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.fennec.model.atlas.dcat.api.DcatMetadataSource;
import org.eclipse.fennec.model.atlas.dcat.api.PublicationTarget;

/**
 * The default {@link DcatMetadataSource}: everything comes from this portal's configuration.
 *
 * <p>
 * Layer 1 of the metadata design. It answers nothing it was not configured for, so the caller
 * falls back to what it can derive from the model itself — which is how a per-model
 * {@code EAnnotation}-driven source can later override one field without having to answer for the
 * rest.
 * </p>
 */
final class ConfiguredMetadataSource implements DcatMetadataSource {

    private final DcatPublisherConfig config;

    ConfiguredMetadataSource(DcatPublisherConfig config) {
        this.config = config;
    }

    @Override
    public Optional<String> title(PublicationTarget target) {
        return Optional.empty();
    }

    @Override
    public Optional<String> description(PublicationTarget target) {
        return Optional.empty();
    }

    @Override
    public Optional<String> publisherName(PublicationTarget target) {
        return nonBlank(config.publisher_name());
    }

    @Override
    public Optional<String> licenseUri(PublicationTarget target) {
        return nonBlank(config.license_uri());
    }

    @Override
    public List<String> themes(PublicationTarget target) {
        return config.theme() == null ? List.of() : Arrays.asList(config.theme());
    }

    @Override
    public List<String> keywords(PublicationTarget target) {
        return config.keywords() == null ? List.of() : Arrays.asList(config.keywords());
    }

    private static Optional<String> nonBlank(String value) {
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
    }
}
