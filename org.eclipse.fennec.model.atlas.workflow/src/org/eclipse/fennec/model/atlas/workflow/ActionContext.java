/*
 * Copyright (c) 2012 - 2025 Data In Motion and others.
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
package org.eclipse.fennec.model.atlas.workflow;

import java.time.Instant;
import java.util.Map;

import org.eclipse.fennec.model.atlas.workflow.StageActionService.ExitReason;

/**
 * Context passed to a {@link StageActionService} callback.
 *
 * <p>
 * All events are delivered post-commit: by the time a callback runs, the
 * underlying storage mutation has already been applied. Implementations that
 * need the object's content can look it up via their own injected
 * {@code EObjectStorageService} using {@link #objectId()} — with the exception
 * of {@link StageActionService.ActionEvent#EXIT EXIT} events, where the object
 * is no longer retrievable and any state the action needs must have been
 * captured during a prior {@link StageActionService.ActionEvent#ENTER ENTER}
 * or {@link StageActionService.ActionEvent#UPDATE UPDATE}.
 * </p>
 *
 * @param scope        the scope the object belongs to
 * @param registry     the registry the object belongs to
 * @param objectId     the object's identifier
 * @param objectType   the object's type (for example {@code "EPackage"})
 * @param stage        the stage the event relates to; for {@code ENTER} the
 *                     stage being entered, for {@code EXIT} the stage being
 *                     left, for {@code UPDATE} the stage the object remains in
 * @param sourceStage  the stage the object came from on an {@code ENTER} via
 *                     transition; {@code null} for an initial upload and for
 *                     non-{@code ENTER} events
 * @param targetStage  the stage the object moved to on an {@code EXIT} via
 *                     transition; {@code null} on deletion and for non-
 *                     {@code EXIT} events
 * @param exitReason   the reason for an {@code EXIT} event; {@code null} for
 *                     other events
 * @param triggerUser  the user that triggered the event, or {@code "system"}
 *                     for replay
 * @param triggerTime  the time the event was raised
 * @param notes        free-form note associated with the event (release note,
 *                     deletion reason, etc.); may be {@code null}
 * @param replay       {@code true} if the workflow is replaying this event to
 *                     reconcile runtime state (for example at startup); actions
 *                     with purely external side effects typically skip work
 *                     when this is {@code true}
 * @param metadata     additional metadata carried with the event; escape hatch
 *                     for workflow- or caller-specific data that does not
 *                     belong on the typed surface of this record
 * @since 2.0.0
 */
public record ActionContext(
        String scope,
        String registry,
        String objectId,
        String objectType,
        String stage,
        String sourceStage,
        String targetStage,
        ExitReason exitReason,
        String triggerUser,
        Instant triggerTime,
        String notes,
        boolean replay,
        Map<String, Object> metadata) {
}
