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
package org.eclipse.fennec.model.atlas.dcat.tests;

import java.util.Hashtable;
import java.util.List;
import java.util.Optional;

import org.eclipse.fennec.model.atlas.dcat.api.DcatMetadataSource;
import org.eclipse.fennec.model.atlas.dcat.api.PublicationTarget;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * What a third party actually writes: a metadata source that overrides one field and stays silent
 * about the rest, which the SPI's javadoc explicitly permits.
 */
final class TitleOnlyMetadataSource implements DcatMetadataSource {

    /** The title this source insists on, so a test can tell it apart from the derived one. */
    static final String TITLE = "Einwohnermodell";

    static ServiceRegistration<?> register(BundleContext context) {
        return context.registerService(DcatMetadataSource.class, new TitleOnlyMetadataSource(),
                new Hashtable<>());
    }

    @Override
    public Optional<String> title(PublicationTarget target) {
        return Optional.of(TITLE);
    }

    @Override
    public Optional<String> description(PublicationTarget target) {
        return Optional.empty();
    }

    @Override
    public Optional<String> publisherName(PublicationTarget target) {
        return Optional.empty();
    }

    @Override
    public Optional<String> licenseUri(PublicationTarget target) {
        return Optional.empty();
    }

    @Override
    public List<String> themes(PublicationTarget target) {
        return List.of();
    }

    @Override
    public List<String> keywords(PublicationTarget target) {
        return List.of();
    }
}
