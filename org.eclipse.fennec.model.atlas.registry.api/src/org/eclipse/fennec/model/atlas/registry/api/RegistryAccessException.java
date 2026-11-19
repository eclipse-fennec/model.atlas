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
 * A client request the {@link RegistryWriter} refuses. The {@link RefusalReason} says why, in terms a
 * front end can translate into its own protocol (the REST layer maps each to a status code). The
 * message is meant for the client: it names the object and the rule, never storage internals.
 */
public class RegistryAccessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final RefusalReason reason;

	/**
	 * @param reason  why the request was refused; never {@code null}
	 * @param message the client-facing message
	 */
	public RegistryAccessException(RefusalReason reason, String message) {
		super(message);
		this.reason = Objects.requireNonNull(reason, "reason");
	}

	/**
	 * @param reason  why the request was refused; never {@code null}
	 * @param message the client-facing message
	 * @param cause   the underlying failure
	 */
	public RegistryAccessException(RefusalReason reason, String message, Throwable cause) {
		super(message, cause);
		this.reason = Objects.requireNonNull(reason, "reason");
	}

	/** @return why the request was refused */
	public RefusalReason reason() {
		return reason;
	}
}
