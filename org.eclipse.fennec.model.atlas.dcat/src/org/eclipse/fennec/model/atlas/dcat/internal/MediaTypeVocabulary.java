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
 * <li>{@code application/json}, {@code application/xml}, {@code text/csv},
 * {@code application/ld+json} are registered with IANA, as is
 * {@code application/vnd.xmi+xml} — the vendor-tree registration OMG filed for XMI in 2008, and the
 * only registered name for that format. {@code application/xmi}, which is what this atlas actually
 * puts on the wire, is <strong>not</strong> registered, and neither is
 * {@code application/schema+json}.</li>
 * <li>The file-type NAL has {@code XML}, {@code JSON}, {@code JSON_LD} and {@code CSV} — verified by
 * fetching each concept and reading its {@code skos:prefLabel}. It has <strong>no</strong> XMI
 * entry: that IRI answers 200 with no concept in it, exactly as a made-up code does.</li>
 * </ul>
 *
 * <h2>Where a register has nothing to offer</h2>
 *
 * A media type with no IANA registration gets <em>no</em> {@code dcat:mediaType} rather than a
 * literal one. Omitting it is conformant — DCAT-AP makes only {@code dcat:accessURL} mandatory on a
 * Distribution — where a literal is a documented violation, and the served type is still visible in
 * the Distribution's title and in the {@code ?mediaType=} of its download URL. An unmapped media
 * type keeps the literal for both, which is the pre-existing behaviour, and is logged once by the
 * mapper so it is visible rather than silently non-conformant.
 */
final class MediaTypeVocabulary {

    private static final String FILE_TYPE = "http://publications.europa.eu/resource/authority/file-type/";
    private static final String IANA = "http://www.iana.org/assignments/media-types/";

    /** Served media type to (file-type concept, IANA media type), either of which may be absent. */
    private static final Map<String, String[]> TERMS = terms();

    private MediaTypeVocabulary() {
    }

    private static Map<String, String[]> terms() {
        Map<String, String[]> terms = new LinkedHashMap<>();
        // XMI is XML as far as the file-type list is concerned — it has no XMI concept — and its
        // only registered media type is OMG's vendor-tree one. That name describes the format
        // correctly even though the wire header is the shorter unregistered alias.
        terms.put("application/xmi", new String[] { FILE_TYPE + "XML", IANA + "application/vnd.xmi+xml" });
        terms.put("application/json", new String[] { FILE_TYPE + "JSON", IANA + "application/json" });
        terms.put("application/xml", new String[] { FILE_TYPE + "XML", IANA + "application/xml" });
        terms.put("application/ld+json", new String[] { FILE_TYPE + "JSON_LD", IANA + "application/ld+json" });
        terms.put("text/csv", new String[] { FILE_TYPE + "CSV", IANA + "text/csv" });
        // A JSON Schema document is a JSON file; the media type itself is not registered, so it
        // gets a format and no mediaType rather than a literal one.
        terms.put("application/schema+json", new String[] { FILE_TYPE + "JSON", null });
        terms.put("application/schema+xml", new String[] { FILE_TYPE + "XML", null });
        return Map.copyOf(terms);
    }

    /** @return the EU file-type IRI for {@code mediaType}, or empty when the list has no entry */
    static Optional<String> formatIri(String mediaType) {
        return term(mediaType, 0);
    }

    /** @return the IANA IRI for {@code mediaType}, or empty when it is not registered */
    static Optional<String> mediaTypeIri(String mediaType) {
        return term(mediaType, 1);
    }

    /** Whether anything at all is known about this media type. */
    static boolean isMapped(String mediaType) {
        return mediaType != null && TERMS.containsKey(mediaType.trim().toLowerCase());
    }

    private static Optional<String> term(String mediaType, int index) {
        if (mediaType == null) {
            return Optional.empty();
        }
        String[] terms = TERMS.get(mediaType.trim().toLowerCase());
        return terms == null ? Optional.empty() : Optional.ofNullable(terms[index]);
    }
}
