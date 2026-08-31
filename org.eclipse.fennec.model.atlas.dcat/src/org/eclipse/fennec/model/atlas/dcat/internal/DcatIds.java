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

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * The portal id scheme.
 *
 * <p>
 * Ids appear in portal URLs and a published id is permanent, so they are URL-safe, stable across
 * restarts, and derived from nothing that moves — notably not from the public base URI, which is
 * why re-pointing the atlas at a new domain is a reconcile that rewrites URLs in place rather
 * than a rename that breaks every consumer's bookmark.
 * </p>
 */
final class DcatIds {

    private DcatIds() {
    }

    /**
     * A scope's Catalog id. Scope names are already URL path segments in the atlas's own REST
     * API, so they need no encoding.
     *
     * @param scope the scope name
     * @return the catalog id
     */
    static String catalogId(String scope) {
        return scope;
    }

    /**
     * A Dataset's id: scope, stage and the encoded namespace URI.
     *
     * <p>
     * Both scope <em>and</em> stage are in the id because the same nsURI legitimately lives in
     * several of each. The nsURI is Base64URL-encoded rather than slugged: a portal id is
     * permanent and a slug collision is not repairable after publication, so opacity wins over
     * readability — {@code dct:identifier} carries the readable nsURI and {@code dct:title} the
     * readable name, so nothing is actually lost but the look of the URL.
     * </p>
     *
     * <p>
     * {@code scope} is the scope that <em>defines</em> the package, never one that inherits it:
     * one Dataset is linked into the descendants' Catalogs rather than copied, so an id per
     * appearance would produce exactly the duplication the linking design exists to avoid.
     * </p>
     *
     * @param scope the defining scope
     * @param stage the stage
     * @param nsUri the namespace URI
     * @return the dataset id
     */
    static String datasetId(String scope, String stage, String nsUri) {
        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(nsUri.getBytes(StandardCharsets.UTF_8));
        return scope + "--" + stage + "--" + encoded;
    }

    /**
     * A Distribution's id, unique only within its Dataset — the admin path is
     * {@code …/datasets/{datasetId}/distributions/{id}}, so the id namespace is per-dataset and a
     * media-type slug suffices.
     *
     * @param mediaType e.g. {@code application/xmi}
     * @return e.g. {@code application-xmi}
     */
    static String distributionId(String mediaType) {
        return mediaType.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
