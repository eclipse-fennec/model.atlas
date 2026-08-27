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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The register IRIs DCAT-AP requires for a Distribution's format, per media type the atlas serves.
 *
 * <h2>Why a table and not the media type itself</h2>
 *
 * DCAT-AP mandates a controlled vocabulary for both properties: {@code dct:format} from the EU
 * file-type Named Authority List, {@code dcat:mediaType} from the IANA media-type register. A plain
 * literal like {@code "application/xmi"} is perfectly valid RDF — nothing fails to parse — but it
 * violates the vocabulary rule, which a DCAT-AP SHACL shape reports as
 * {@code sh:NodeKindConstraintComponent}: the value must be an IRI. The portal serializes a value
 * that <em>is</em> an absolute IRI as an IRI node, so emitting the register IRI is all it takes.
 *
 * <h2>Every IRI below was dereferenced before being written down</h2>
 *
 * <ul>
 * <li>{@code application/json}, {@code application/xml}, {@code text/csv} and
 * {@code application/ld+json} are registered with IANA. {@code application/xmi} — what this atlas
 * actually puts on the wire — is <strong>not</strong>, and neither is
 * {@code application/schema+json}. ({@code application/vnd.xmi+xml} is registered, but it is a
 * different type string from the one served, so it is not used: see below.)</li>
 * <li>The file-type NAL has {@code XML}, {@code JSON}, {@code JSON_LD} and {@code CSV} — verified by
 * fetching each concept and reading its {@code skos:prefLabel}. It has <strong>no</strong> XMI
 * entry: that IRI answers 200 with no concept in it, exactly as a made-up code does.</li>
 * </ul>
 *
 * <h2>Where a register has nothing to offer</h2>
 *
 * A media type with no IANA registration gets <em>no</em> {@code dcat:mediaType} rather than a
 * literal one, and — the part worth stating — rather than a registered name for a <em>related</em>
 * type. {@code application/xmi} is served here and is not registered; OMG's
 * {@code application/vnd.xmi+xml} is registered but is not what this atlas sends, so advertising it
 * would be a false statement about the wire format that a harvester could act on. Omitting is
 * conformant (DCAT-AP makes only {@code dcat:accessURL} mandatory on a Distribution), it loses no
 * information — {@code dct:format} already names the format family, and the served type stays in the
 * Distribution's title and in the {@code ?mediaType=} of its download URL — and it keeps the rule
 * simple enough to check: <strong>{@code dcat:mediaType} appears exactly when the media type this
 * atlas serves is itself registered.</strong>
 *
 * <p>
 * That rule is why the IANA IRI is <em>derived</em> from the served media type rather than written
 * out beside it. A hand-written IRI can disagree with the type it is filed under; a derived one
 * cannot.
 * </p>
 */
final class MediaTypeVocabulary {

    private static final String FILE_TYPE = "http://publications.europa.eu/resource/authority/file-type/";
    private static final String IANA = "http://www.iana.org/assignments/media-types/";

    /** Served media type to what the two registers offer for it. */
    private static final Map<String, Terms> TERMS = terms();

    /**
     * @param fileTypeConcept the code of the EU file-type concept, or {@code null} when the list has
     *                        no entry for this format
     * @param ianaRegistered  whether the served media type <em>itself</em> is registered with IANA,
     *                        which is the only condition under which a {@code dcat:mediaType} IRI is
     *                        emitted — and it is derived from the served type, never written out
     */
    private record Terms(String fileTypeConcept, boolean ianaRegistered) {
    }

    private MediaTypeVocabulary() {
    }

    private static Map<String, Terms> terms() {
        Map<String, Terms> terms = new LinkedHashMap<>();
        // XMI is XML as far as the file-type list is concerned — it has no XMI concept — and
        // application/xmi is not registered with IANA, so it gets no dcat:mediaType. OMG's
        // application/vnd.xmi+xml is registered, but this atlas does not serve it.
        terms.put("application/xmi", new Terms("XML", false));
        terms.put("application/json", new Terms("JSON", true));
        terms.put("application/xml", new Terms("XML", true));
        terms.put("application/ld+json", new Terms("JSON_LD", true));
        terms.put("text/csv", new Terms("CSV", true));
        // A JSON Schema document is a JSON file; the media type itself is not registered.
        terms.put("application/schema+json", new Terms("JSON", false));
        terms.put("application/schema+xml", new Terms("XML", false));
        return Map.copyOf(terms);
    }

    /** @return the EU file-type IRI for {@code mediaType}, or empty when the list has no entry */
    static Optional<String> formatIri(String mediaType) {
        return terms(mediaType).map(Terms::fileTypeConcept).map(concept -> FILE_TYPE + concept);
    }

    /**
     * @return the IANA IRI of the media type this atlas serves, or empty when that type is not
     *         registered. Never the IRI of a merely similar type
     */
    static Optional<String> mediaTypeIri(String mediaType) {
        return terms(mediaType).filter(Terms::ianaRegistered).map(unused -> IANA + normalise(mediaType));
    }

    /** Whether anything at all is known about this media type. */
    static boolean isMapped(String mediaType) {
        return terms(mediaType).isPresent();
    }

    private static Optional<Terms> terms(String mediaType) {
        return mediaType == null ? Optional.empty() : Optional.ofNullable(TERMS.get(normalise(mediaType)));
    }

    private static String normalise(String mediaType) {
        return mediaType.trim().toLowerCase();
    }
}
