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
package org.eclipse.fennec.model.atlas.scope.api;

/**
 * Signals that a stage already holds a <em>different</em> object under the id an
 * operation wants to write there.
 * <p>
 * An objectId is unique per stage, not across stages: a transition copies unless
 * the registry sets {@code delete.after.transition=true}, and a new draft revision
 * of an already released model re-uploads the same id. Promoting therefore writes
 * into a target stage that may well hold that id already — and when the occupant is
 * an earlier revision of the object being promoted, replacing it is precisely what
 * the promotion is for. What this exception reports is the other case: the id is
 * held by an object the caller never named, which a promotion would otherwise
 * destroy without saying so.
 * <p>
 * Sameness is judged from the metadata both sides carry: the {@code nsUri}
 * property, the object type and the object name. A signal present on both sides and
 * disagreeing means "a different object"; a signal missing on either side decides
 * nothing, so an object whose metadata says nothing distinguishing is taken to be
 * the same object, as the convention behind the per-stage id rule assumes. The
 * caller who really does mean to replace the occupant deletes it first.
 * <p>
 * It is neither an {@link IllegalArgumentException} nor a
 * {@link StagePolicyException}: the request is well formed and breaks no stage
 * rule, it collides with state — which is what {@code 409 Conflict} is for, and
 * what the REST layer answers with.
 *
 * @author Data In Motion
 * @since Sep 6, 2026
 */
public class StageOccupiedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates an exception describing the collision.
	 *
	 * @param message the reason, naming the id, the stage holding it and how the
	 *                two objects differ
	 */
	public StageOccupiedException(String message) {
		super(message);
	}

	/**
	 * Creates an exception describing the collision.
	 *
	 * @param message the reason, naming the id, the stage holding it and how the
	 *                two objects differ
	 * @param cause   the underlying cause
	 */
	public StageOccupiedException(String message, Throwable cause) {
		super(message, cause);
	}
}
