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
package org.eclipse.fennec.model.atlas.scope.api;

/**
 * Signals that a stage gate refused an operation before the workflow committed
 * it: a transition whose object does not hold up in the target stage, for
 * instance a QVT source that does not compile against the target stage's
 * package view (issue #248).
 * <p>
 * It is neither a {@link StagePolicyException} nor a
 * {@link StageOccupiedException}. The request is well formed, breaks no stage
 * rule and collides with no occupant of the target address; what stops it is
 * the state of the object <em>relative to</em> the target stage, and that
 * state can change: once the library it imports has been promoted, the same
 * request succeeds. That is a {@code 409 Conflict} over HTTP, and what the REST
 * layer answers with.
 * <p>
 * The message is the gate's reason, written for whoever made the request. It
 * travels to the client unchanged, so it names the object, the stages and what
 * failed, and carries no internal detail.
 *
 * @author Data In Motion
 * @since Sep 21, 2026
 */
public class StageGateRefusedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates an exception carrying the gate's reason.
	 *
	 * @param message the reason, naming the object, the stages and what failed
	 */
	public StageGateRefusedException(String message) {
		super(message);
	}

	/**
	 * Creates an exception carrying the gate's reason.
	 *
	 * @param message the reason, naming the object, the stages and what failed
	 * @param cause   the underlying cause
	 */
	public StageGateRefusedException(String message, Throwable cause) {
		super(message, cause);
	}
}
