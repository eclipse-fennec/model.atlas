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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.eclipse.fennec.model.atlas.dcat.api.DcatMetadataSource;
import org.eclipse.fennec.model.atlas.dcat.api.PublicationTarget;
import org.junit.jupiter.api.Test;

/**
 * What the SPI's javadoc promises: an implementation may answer for one field and stay silent about
 * the rest.
 */
public class FallbackMetadataSourceTest {

    private static final PublicationTarget TARGET = new PublicationTarget("jena", "release",
            "http://test.example.com/person/1.1", "1.1.0", null);

    /** Answers a title and nothing else — the case that used to break every Distribution. */
    private static final class TitleOnly implements DcatMetadataSource {

        @Override
        public Optional<String> title(PublicationTarget target) {
            return Optional.of("Einwohner");
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

    /** A source that answers nothing at all, including nulls where the API allows empties. */
    private static final class Silent implements DcatMetadataSource {

        @Override
        public Optional<String> title(PublicationTarget target) {
            return null;
        }

        @Override
        public Optional<String> description(PublicationTarget target) {
            return null;
        }

        @Override
        public Optional<String> publisherName(PublicationTarget target) {
            return null;
        }

        @Override
        public Optional<String> licenseUri(PublicationTarget target) {
            return null;
        }

        @Override
        public List<String> themes(PublicationTarget target) {
            return null;
        }

        @Override
        public List<String> keywords(PublicationTarget target) {
            return null;
        }
    }

    private static DcatMetadataSource configured() {
        return new ConfiguredMetadataSource(ConfigStub.full());
    }

    @Test
    public void theRegisteredSourceWinsWhereItAnswers() {
        DcatMetadataSource composed = new FallbackMetadataSource(new TitleOnly(), configured());

        assertThat(composed.title(TARGET)).contains("Einwohner");
    }

    @Test
    public void theConfigurationFillsInEverythingItStaysSilentAbout() {
        // The regression this class exists for: a source overriding only the title made every
        // Distribution fail on a missing license.uri that was configured all along — and the failure
        // was recorded as permanent, so nothing retried it.
        DcatMetadataSource composed = new FallbackMetadataSource(new TitleOnly(), configured());

        assertThat(composed.licenseUri(TARGET)).contains("http://dcat-ap.de/def/licenses/dl-by-de/2.0");
        assertThat(composed.publisherName(TARGET)).contains("Stadt Jena");
        assertThat(composed.themes(TARGET)).containsExactly("http://example.org/theme/TECH");
        assertThat(composed.keywords(TARGET)).containsExactly("modell");
    }

    @Test
    public void aSourceReturningNullIsTreatedAsSilenceRatherThanACrash() {
        DcatMetadataSource composed = new FallbackMetadataSource(new Silent(), configured());

        assertThat(composed.licenseUri(TARGET)).isNotEmpty();
        assertThat(composed.keywords(TARGET)).isNotEmpty();
        assertThat(composed.title(TARGET)).isEmpty();
    }

    @Test
    public void withNoRegisteredSourceTheConfiguredAnswersStand() {
        DcatMetadataSource composed = new FallbackMetadataSource(configured(), configured());

        assertThat(composed.licenseUri(TARGET)).contains("http://dcat-ap.de/def/licenses/dl-by-de/2.0");
    }
}
