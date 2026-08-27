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

import org.junit.jupiter.api.Test;

/**
 * The register IRIs, and the cases where a register has nothing to offer.
 *
 * <p>
 * Every expected value here was dereferenced when it was written down. The point of asserting them
 * is that a typo in an IRI is invisible: the RDF stays valid, the shapes stay satisfied, and the
 * catalogue quietly points at a concept that does not exist.
 * </p>
 */
public class MediaTypeVocabularyTest {

    @Test
    public void jsonHasBothRegisterTerms() {
        assertThat(MediaTypeVocabulary.formatIri("application/json"))
                .contains("http://publications.europa.eu/resource/authority/file-type/JSON");
        assertThat(MediaTypeVocabulary.mediaTypeIri("application/json"))
                .contains("http://www.iana.org/assignments/media-types/application/json");
    }

    @Test
    public void xmiGetsTheXmlFileTypeAndNoMediaTypeAtAll() {
        // The file-type list has no XMI concept — that IRI answers 200 with nothing in it, exactly
        // as a made-up code does — so the format is XML.
        assertThat(MediaTypeVocabulary.formatIri("application/xmi"))
                .contains("http://publications.europa.eu/resource/authority/file-type/XML");
        // And no dcat:mediaType: application/xmi is not registered, and OMG's registered
        // application/vnd.xmi+xml is a different type string from the one this atlas serves.
        // Advertising it would be a false statement about the wire format that a harvester could
        // act on — by sending it as an Accept header, for one.
        assertThat(MediaTypeVocabulary.mediaTypeIri("application/xmi")).isEmpty();
    }

    @Test
    public void aMediaTypeIriIsAlwaysTheIriOfTheTypeItIsFiledUnder() {
        // Derived, not written out beside the entry: a hand-written IRI can disagree with the media
        // type it belongs to, and that is exactly the mistake this shape of the table prevents.
        for (String mediaType : new String[] { "application/json", "application/xml", "application/ld+json",
                "text/csv" }) {
            assertThat(MediaTypeVocabulary.mediaTypeIri(mediaType))
                    .contains("http://www.iana.org/assignments/media-types/" + mediaType);
        }
    }

    @Test
    public void anUnregisteredMediaTypeGetsAFormatButNoMediaType() {
        // Omitting is conformant — only accessURL is mandatory on a Distribution — where a literal
        // is a violation any DCAT-AP shape reports.
        assertThat(MediaTypeVocabulary.formatIri("application/schema+json"))
                .contains("http://publications.europa.eu/resource/authority/file-type/JSON");
        assertThat(MediaTypeVocabulary.mediaTypeIri("application/schema+json")).isEmpty();
        assertThat(MediaTypeVocabulary.isMapped("application/schema+json")).isTrue();
    }

    @Test
    public void anUnknownMediaTypeIsReportedAsUnmapped() {
        assertThat(MediaTypeVocabulary.isMapped("application/vnd.acme.thing")).isFalse();
        assertThat(MediaTypeVocabulary.formatIri("application/vnd.acme.thing")).isEmpty();
        assertThat(MediaTypeVocabulary.mediaTypeIri("application/vnd.acme.thing")).isEmpty();
    }

    @Test
    public void lookupToleratesCasingAndPadding() {
        assertThat(MediaTypeVocabulary.formatIri(" APPLICATION/JSON ")).isPresent();
        assertThat(MediaTypeVocabulary.isMapped(null)).isFalse();
        assertThat(MediaTypeVocabulary.formatIri(null)).isEmpty();
    }

    @Test
    public void everyTermIsAnAbsoluteIriBecauseThatIsTheWholePoint() {
        // A relative or malformed value would serialize as a literal again, and the shape violation
        // would come back with nothing in the output to explain why.
        for (String mediaType : new String[] { "application/xmi", "application/json", "application/xml",
                "application/ld+json", "text/csv", "application/schema+json", "application/schema+xml" }) {
            MediaTypeVocabulary.formatIri(mediaType)
                    .ifPresent(iri -> assertThat(iri).startsWith("http://").doesNotContain(" "));
            MediaTypeVocabulary.mediaTypeIri(mediaType)
                    .ifPresent(iri -> assertThat(iri).startsWith("http://").doesNotContain(" ")
                            .endsWith(mediaType));
        }
    }
}
