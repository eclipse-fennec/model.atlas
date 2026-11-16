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
package org.eclipse.fennec.model.atlas.registry.api;

/**
 * What a write did. Refusals are not outcomes — they are raised as exceptions.
 */
public enum WriteOutcome {

	/** A new object was stored. */
	CREATED,

	/** An existing object's content or metadata was replaced. */
	UPDATED,

	/** The content sent equals the stored content; nothing was written. */
	UNCHANGED,

	/** The target already held the object and {@link ConflictPolicy#SKIP} left it alone. */
	SKIPPED,

	/** The object was deleted from the stage. */
	DELETED,

	/** The object moved (or was copied) to the target stage. */
	TRANSITIONED,

	/**
	 * The object was not in the source stage but already is in the target stage — a retried
	 * transition. The result carries the target stage's metadata.
	 */
	ALREADY_IN_TARGET,

	/** The object the operation addressed does not exist; nothing was written. */
	NOT_FOUND
}
