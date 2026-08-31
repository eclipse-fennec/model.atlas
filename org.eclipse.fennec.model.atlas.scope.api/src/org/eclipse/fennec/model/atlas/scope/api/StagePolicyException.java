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
 * Signals that a registry's stage policy refuses an operation the request was
 * otherwise entitled to make — updating an object in a stage declared
 * {@link StageInfo#isFinal() final}, for instance.
 * <p>
 * This is deliberately its own type rather than an {@link IllegalArgumentException}.
 * The request is well formed and names an existing object in an existing, writable
 * stage; what stops it is a rule of the registry. A caller has to be able to tell
 * those apart, because a policy refusal will never succeed on a retry while a server
 * fault might — and because they deserve different answers over HTTP, where the REST
 * layer turns this exception into a {@code 403 Forbidden} and a bad parameter into a
 * {@code 400}.
 * <p>
 * It lives beside {@link StageInfo} because that is where a stage's policy is
 * declared: the flags this exception speaks for are read from there, by whichever
 * service enforces them.
 * <p>
 * The message is written for whoever made the request — it travels to the client
 * unchanged, so it names the stage and registry that refused and carries no internal
 * detail.
 *
 * @author Data In Motion
 * @since Aug 28, 2026
 */
public class StagePolicyException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates an exception describing which policy refused the operation.
	 *
	 * @param message the reason, naming the stage and registry it applies to
	 */
	public StagePolicyException(String message) {
		super(message);
	}

	/**
	 * Creates an exception describing which policy refused the operation.
	 *
	 * @param message the reason, naming the stage and registry it applies to
	 * @param cause   the underlying cause
	 */
	public StagePolicyException(String message, Throwable cause) {
		super(message, cause);
	}
}
