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

import java.util.Objects;

/**
 * A client request the {@link RegistryWriter} refuses. The {@link Reason} says why, in terms a
 * front end can translate into its own protocol (the REST layer maps each to a status code). The
 * message is meant for the client: it names the object and the rule, never storage internals.
 */
public class RegistryAccessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/** Why a request was refused. */
	public enum Reason {

		/** The request itself is malformed: a missing or mismatching identity, an invalid version. */
		INVALID,

		/** The content's type is not one the target registry accepts. */
		INCOMPATIBLE_TYPE,

		/** The content's type is derived content, which only the Atlas itself writes. */
		DERIVED_CONTENT,

		/** The addressed scope does not exist. */
		UNKNOWN_SCOPE,

		/** The addressed registry does not exist or is not configured. */
		UNKNOWN_REGISTRY,

		/** The addressed scope has no schema registry. */
		NO_SCHEMA_REGISTRY,

		/** A create found its target taken and the conflict policy says to fail. */
		ALREADY_EXISTS,

		/** The object is inherited from another scope and must be changed there. */
		INHERITED,

		/** The object is read-only where it was addressed. */
		READ_ONLY,

		/** The caller's {@link Precondition} does not hold for the current object. */
		PRECONDITION_FAILED,

		/** The calling thread was interrupted while the write was in flight. */
		INTERRUPTED
	}

	private final Reason reason;

	/**
	 * @param reason  why the request was refused; never {@code null}
	 * @param message the client-facing message
	 */
	public RegistryAccessException(Reason reason, String message) {
		super(message);
		this.reason = Objects.requireNonNull(reason, "reason");
	}

	/**
	 * @param reason  why the request was refused; never {@code null}
	 * @param message the client-facing message
	 * @param cause   the underlying failure
	 */
	public RegistryAccessException(Reason reason, String message, Throwable cause) {
		super(message, cause);
		this.reason = Objects.requireNonNull(reason, "reason");
	}

	/** @return why the request was refused */
	public Reason reason() {
		return reason;
	}
}
