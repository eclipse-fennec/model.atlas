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

import java.util.Set;

import org.osgi.util.promise.Promise;

/**
 * Service interface for executing actions tied to the lifecycle of an object
 * within a stage.
 *
 * <p>
 * An implementation declares which stages and which lifecycle events it cares
 * about; the workflow fires the matching callback after the storage mutation
 * has committed. All callbacks are expected to be idempotent, because the
 * workflow may replay {@link ActionEvent#ENTER ENTER} events at startup to
 * reconcile runtime state that does not survive a restart (for example OSGi
 * service registrations).
 * </p>
 *
 * <p>
 * Typical use cases:
 * </p>
 * <ul>
 * <li>Registering / unregistering EPackages as OSGi services when they enter
 * or leave a given stage</li>
 * <li>Updating service properties on a re-upload to the same stage</li>
 * <li>Notifying external systems on stage transitions</li>
 * <li>Index maintenance, cache invalidation, audit trails</li>
 * </ul>
 *
 * @since 2.0.0
 */
public interface StageActionService {

    /**
     * Lifecycle events an action can subscribe to.
     */
    enum ActionEvent {
        /**
         * Fired after an object has been written into a stage for the first time,
         * either by an initial upload or by a transition from another stage.
         */
        ENTER,
        /**
         * Fired after an object's content has been updated in place while it remains
         * in the same stage.
         */
        UPDATE,
        /**
         * Fired after an object has left a stage, either because it was deleted or
         * because it was transitioned to another stage. By the time this fires, the
         * object is no longer retrievable from storage under that stage.
         */
        EXIT
    }

    /**
     * Reason an {@link ActionEvent#EXIT EXIT} event was raised.
     */
    enum ExitReason {
        /** The object was removed from the stage without moving elsewhere. */
        DELETED,
        /** The object moved to another stage; see {@link ActionContext#getTargetStage()}. */
        TRANSITIONED
    }

    /**
     * @param objectType the object type (for example {@code "EPackage"})
     * @return {@code true} if this action applies to objects of the given type
     */
    boolean supportsObjectType(String objectType);

    /**
     * @return the stages this action cares about. An empty set means "all stages".
     */
    Set<String> getTriggerStages();

    /**
     * @return the lifecycle events this action subscribes to. An empty set means
     *         "all events".
     */
    Set<ActionEvent> getTriggerEvents();

    /**
     * Invoked after an object has entered {@link ActionContext#stage()}.
     *
     * @param ctx context describing the event
     * @return promise that resolves when the action has finished
     */
    Promise<Void> onEnter(ActionContext ctx);

    /**
     * Invoked after an object already in {@link ActionContext#stage()} has had
     * its content updated in place.
     *
     * @param ctx context describing the event
     * @return promise that resolves when the action has finished
     */
    Promise<Void> onUpdate(ActionContext ctx);

    /**
     * Invoked after an object has left {@link ActionContext#stage()}. The
     * {@link ActionContext#exitReason() exit reason} distinguishes deletion from
     * a transition; on a transition, {@link ActionContext#targetStage()} carries
     * the destination.
     *
     * @param ctx context describing the event
     * @return promise that resolves when the action has finished
     */
    Promise<Void> onExit(ActionContext ctx);

    /**
     * Indicates whether the workflow should replay {@link ActionEvent#ENTER ENTER}
     * events for every object currently in a trigger stage when the action service
     * starts. Useful for actions whose effect lives only in memory (for example
     * OSGi service registrations).
     *
     * @return {@code true} if startup replay is required
     */
    boolean requiresReplayOnStartup();

    /**
     * Indicates whether the workflow should fire synthetic {@link ActionEvent#EXIT
     * EXIT} events ({@link ExitReason#DELETED DELETED}) for every object currently
     * in a trigger stage when the action service is going away. Mirrors
     * {@link #requiresReplayOnStartup()} for shutdown / registry-gone scenarios.
     *
     * @return {@code true} if shutdown replay is required
     */
    boolean requiresReplayOnShutdown();
}
