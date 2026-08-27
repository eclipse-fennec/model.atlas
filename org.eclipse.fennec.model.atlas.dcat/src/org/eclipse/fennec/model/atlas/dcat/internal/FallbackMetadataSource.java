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

import org.eclipse.fennec.model.atlas.dcat.api.DcatMetadataSource;
import org.eclipse.fennec.model.atlas.dcat.api.PublicationTarget;

/**
 * Two metadata sources, asked in order: whatever a whiteboard implementation answers, and the
 * configured defaults for everything it stays silent about.
 *
 * <h2>Why the composition matters</h2>
 *
 * {@link DcatMetadataSource} documents exactly this: "Every method may return an empty
 * {@link Optional} or an empty list, in which case the publisher falls back to its configuration —
 * so an implementation can answer for one field and stay silent about the rest." Substituting the
 * whiteboard source for the configured one instead of layering it broke that promise in a way that
 * only bit the first implementor: a source overriding nothing but the title made every Distribution
 * fail on a missing {@code license.uri} — recorded as a <em>permanent</em> failure, so nothing
 * retried it — while {@code license.uri} was in fact configured all along.
 *
 * <p>
 * An empty list is treated as silence, per that javadoc. A source that means "no themes at all"
 * therefore cannot express it here; that is the documented contract's choice, not this class's, and
 * the alternative (a nullable list meaning silence) would be worse.
 * </p>
 */
final class FallbackMetadataSource implements DcatMetadataSource {

    private final DcatMetadataSource primary;
    private final DcatMetadataSource fallback;

    FallbackMetadataSource(DcatMetadataSource primary, DcatMetadataSource fallback) {
        this.primary = primary;
        this.fallback = fallback;
    }

    @Override
    public Optional<String> title(PublicationTarget target) {
        return or(primary.title(target), () -> fallback.title(target));
    }

    @Override
    public Optional<String> description(PublicationTarget target) {
        return or(primary.description(target), () -> fallback.description(target));
    }

    @Override
    public Optional<String> publisherName(PublicationTarget target) {
        return or(primary.publisherName(target), () -> fallback.publisherName(target));
    }

    @Override
    public Optional<String> licenseUri(PublicationTarget target) {
        return or(primary.licenseUri(target), () -> fallback.licenseUri(target));
    }

    @Override
    public List<String> themes(PublicationTarget target) {
        List<String> themes = primary.themes(target);
        return themes == null || themes.isEmpty() ? fallback.themes(target) : themes;
    }

    @Override
    public List<String> keywords(PublicationTarget target) {
        List<String> keywords = primary.keywords(target);
        return keywords == null || keywords.isEmpty() ? fallback.keywords(target) : keywords;
    }

    /** Null-tolerant: an implementation returning {@code null} means silence rather than a crash. */
    private static Optional<String> or(Optional<String> answered, java.util.function.Supplier<Optional<String>> other) {
        return answered == null || answered.isEmpty() ? other.get() : answered;
    }
}
