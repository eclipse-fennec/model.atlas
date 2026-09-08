/**
 * Copyright (c) 2012 - 2026 Data In Motion and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.mgmt.registry;

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;

/**
 * The full address of one stored object: its {@code objectId} within a
 * (scope, registry, stage) location.
 *
 * <p>
 * An {@code objectId} is unique per stage, not across stages: a transition
 * <em>copies</em> unless the registry sets {@code delete.after.transition=true},
 * and a new draft revision of an already released model re-uploads the same id,
 * so the same id legitimately lives in two stages of one registry at once
 * (issue #211). The storage layout honours that - the file backend writes
 * {@code workspace/<scope>/<registry>/<stage>/<objectId>.<ext>} - and so must
 * every shared piece of registry state, which would otherwise keep one entry
 * per id and let the last write win (issue #252).
 * </p>
 *
 * @since 2026-09-06
 */
public record RegistryAddress(String scope, String registry, String stage, String objectId) {

    /**
     * Separator for {@link #key()}. A unit separator cannot occur in a scope,
     * registry, stage or object id, so the flattened key stays unambiguous even
     * for backends whose ids contain path separators (the git backend uses
     * {@code <scope>/<branch>/<repo path>}).
     */
    private static final char SEPARATOR = '\u001F';

    /**
     * Returns the address of the given metadata.
     *
     * @param metadata the metadata to address, must not be {@code null}
     * @return the address the metadata describes
     */
    public static RegistryAddress of(ObjectMetadata metadata) {
        return of(metadata.getObjectId(), metadata);
    }

    /**
     * Returns the address of the given metadata, for an object id supplied by the
     * caller rather than taken from the metadata.
     *
     * @param objectId the object id to address
     * @param metadata the metadata carrying scope, registry and stage, must not be
     *                 {@code null}
     * @return the address of {@code objectId} at the metadata's location
     */
    public static RegistryAddress of(String objectId, ObjectMetadata metadata) {
        return new RegistryAddress(metadata.getScope(), metadata.getRegistry(), metadata.getStage(), objectId);
    }

    /**
     * Returns this address flattened into a single string, for use as an index
     * term or map key.
     *
     * @return the flattened address
     */
    public String key() {
        return nullSafe(scope) + SEPARATOR + nullSafe(registry) + SEPARATOR + nullSafe(stage) + SEPARATOR
                + nullSafe(objectId);
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
