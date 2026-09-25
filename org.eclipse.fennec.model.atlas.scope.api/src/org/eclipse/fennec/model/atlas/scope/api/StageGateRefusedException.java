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

import java.util.List;

import org.eclipse.fennec.model.atlas.action.api.GateDiagnostic;
import org.eclipse.fennec.model.atlas.action.api.GateTrigger;

/**
 * Signals that a stage gate refused an operation before the workflow committed
 * it: a transition whose object does not hold up in the target stage, for
 * instance a QVT source that does not compile against the target stage's
 * package view (issue #248), or a delete of an object others still depend on
 * (issue #294).
 * <p>
 * It is neither a {@link StagePolicyException} nor a
 * {@link StageOccupiedException}. The request is well formed, breaks no stage
 * rule and collides with no occupant of the target address; what stops it is
 * the state of the object <em>relative to</em> the operation, and that state
 * can change: once the library it imports has been promoted, or the last
 * dependent is gone, the same request succeeds. That is a {@code 409 Conflict}
 * over HTTP, and what the REST layer answers with.
 * <p>
 * The message is the gate's reason, written for whoever made the request. It
 * travels to the client unchanged, so it names the object, the stages and what
 * failed, and carries no internal detail. Since issue #294 the exception also
 * carries the {@link #diagnostics() diagnostics} behind the refusal and the
 * {@link #objectId() address} of the object they were recorded on: the
 * workflow persists them on the object in its {@link #stage() source stage}
 * before it raises this, so a client that re-reads the object's metadata finds
 * the same findings there.
 *
 * @author Data In Motion
 * @since Sep 21, 2026
 */
public class StageGateRefusedException extends RuntimeException {

	private static final long serialVersionUID = 2L;

	private final GateTrigger trigger;
	private final String scope;
	private final String registry;
	private final String stage;
	private final String objectId;
	private final transient List<GateDiagnostic> diagnostics;

	/**
	 * Creates an exception carrying the gate's reason.
	 *
	 * @param message the reason, naming the object, the stages and what failed
	 */
	public StageGateRefusedException(String message) {
		this(message, (Throwable) null);
	}

	/**
	 * Creates an exception carrying the gate's reason.
	 *
	 * @param message the reason, naming the object, the stages and what failed
	 * @param cause   the underlying cause
	 */
	public StageGateRefusedException(String message, Throwable cause) {
		super(message, cause);
		this.trigger = null;
		this.scope = null;
		this.registry = null;
		this.stage = null;
		this.objectId = null;
		this.diagnostics = List.of();
	}

	/**
	 * Creates an exception carrying the gate's reason, the operation it refused
	 * and the diagnostics recorded on the object.
	 *
	 * @param message     the reason, naming the object, the stages and what
	 *                    failed
	 * @param trigger     the operation that was refused
	 * @param scope       the scope of the object
	 * @param registry    the registry of the object
	 * @param stage       the stage the object sits in, where the diagnostics were
	 *                    recorded
	 * @param objectId    the object's identifier
	 * @param diagnostics the findings behind the refusal, as the gates reported
	 *                    them
	 * @since 1.2
	 */
	public StageGateRefusedException(String message, GateTrigger trigger, String scope, String registry, String stage,
			String objectId, List<GateDiagnostic> diagnostics) {
		super(message);
		this.trigger = trigger;
		this.scope = scope;
		this.registry = registry;
		this.stage = stage;
		this.objectId = objectId;
		this.diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
	}

	/**
	 * @return the operation that was refused, or {@code null} when the exception
	 *         was raised without it
	 * @since 1.2
	 */
	public GateTrigger trigger() {
		return trigger;
	}

	/**
	 * @return the scope of the refused object, or {@code null}
	 * @since 1.2
	 */
	public String scope() {
		return scope;
	}

	/**
	 * @return the registry of the refused object, or {@code null}
	 * @since 1.2
	 */
	public String registry() {
		return registry;
	}

	/**
	 * @return the stage the object sits in and the diagnostics were recorded in,
	 *         or {@code null}
	 * @since 1.2
	 */
	public String stage() {
		return stage;
	}

	/**
	 * @return the identifier of the refused object, or {@code null}
	 * @since 1.2
	 */
	public String objectId() {
		return objectId;
	}

	/**
	 * @return the findings behind the refusal, as the gates reported them; empty
	 *         when the exception was raised without them
	 * @since 1.2
	 */
	public List<GateDiagnostic> diagnostics() {
		return diagnostics;
	}
}
