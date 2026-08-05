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
package org.eclipse.fennec.model.atlas.workflow;

/**
 * Shared contract for the <em>registry resync</em> event (D8 change D).
 *
 * <p>Storage backends whose content changes at runtime <em>outside</em> the normal
 * upload/transition flow — notably the read-only git backend, whose content is pushed
 * externally and picked up by webhook/poll — cannot themselves drive the
 * ENTER/UPDATE/EXIT dispatch: the dispatcher ({@code RegistryServiceImpl}) references the
 * storage service, so a storage → dispatcher reference would be a DS activation cycle.
 *
 * <p>Instead such a backend publishes this event on the {@link #TOPIC} carrying the
 * affected {@link #KEY_SCOPE scope}; a workflow-side handler (decoupled via the event bus,
 * so there is no cycle) re-runs {@code RegistryService.activate(scope)}, which replays the
 * registration for that scope and (re)registers schemas a push added or changed.
 */
public final class RegistryResync {

	private RegistryResync() {
	}

	/** Topic a backend publishes on to request a registry replay for a scope. */
	public static final String TOPIC = "org/eclipse/fennec/model/atlas/workflow/REGISTRY_RESYNC";

	/** Event property (String): the workflow scope whose registrations should be replayed. */
	public static final String KEY_SCOPE = "scope";

	/**
	 * Event property (String, optional): the stage the change occurred in. Required to drive
	 * EXIT for {@link #KEY_REMOVED_OBJECT_IDS removed} objects; the ENTER replay
	 * ({@code activate(scope)}) covers all stages regardless.
	 */
	public static final String KEY_STAGE = "stage";

	/**
	 * Event property (String[], optional): objectIds of schemas that were <em>removed</em> in
	 * this change and whose EPackage registrations must be unregistered (EXIT). Empty/absent
	 * when nothing was removed (a pure add/change).
	 */
	public static final String KEY_REMOVED_OBJECT_IDS = "removedObjectIds";
}
