/*
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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.action.api;

import java.time.Instant;
import java.util.Map;

/**
 * Context passed to a {@link StageGate}: the operation the workflow is about to
 * perform and has not performed yet.
 *
 * <p>
 * Unlike an {@link ActionContext}, this describes a <em>pending</em> mutation.
 * The object is still where it was: for a transition, readable in
 * {@link #sourceStage()} through the gate's own view of the registry, while the
 * target has not been written. A gate that needs the content looks it up with
 * {@link #objectId()}; the context carries what the workflow already had in
 * hand.
 * </p>
 *
 * @param scope       the scope the object belongs to
 * @param registry    the registry the object belongs to
 * @param objectId    the object's identifier
 * @param objectType  the object's type (for example the URI of the
 *                    {@code EPackage} EClass)
 * @param sourceStage the stage the object currently sits in
 * @param targetStage the stage the operation would move it to
 * @param triggerUser the user that requested the operation, or {@code "system"}
 * @param triggerTime the time the operation was requested
 * @param metadata    additional metadata carried with the operation; keys the
 *                    workflow itself sets are declared as constants on
 *                    {@link ActionContext}, for example
 *                    {@link ActionContext#FINGERPRINT}. Never {@code null}, but
 *                    an entry is present only when the workflow had a value for
 *                    it
 * @since 1.1
 */
public record GateContext(
        String scope,
        String registry,
        String objectId,
        String objectType,
        String sourceStage,
        String targetStage,
        String triggerUser,
        Instant triggerTime,
        Map<String, Object> metadata) {

    /**
     * The fingerprint of the object this operation is about, or {@code null}
     * when the object carries none.
     *
     * @return the value of {@link ActionContext#FINGERPRINT} in
     *         {@link #metadata()}, or {@code null}
     */
    public String fingerprint() {
        Object value = metadata == null ? null : metadata.get(ActionContext.FINGERPRINT);
        return value instanceof String s ? s : null;
    }
}
