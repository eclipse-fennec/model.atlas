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
package org.eclipse.fennec.model.atlas.rest.client.api;

import java.util.List;

/**
 * The Atlas refused an operation on an object because a stage gate vetoed it
 * (issue #295): a {@code 409 Conflict} whose body was the object's metadata
 * with the gate's findings.
 *
 * <p>
 * Unlike a plain {@link ModelAtlasClientException}, this one says what to
 * change: the {@link #diagnostics()} are the findings the gate recorded on the
 * object, the same a {@code GET} of its metadata returns afterwards. A
 * {@code 409} whose body carries no diagnostics - an id already taken in the
 * target stage, say - stays a {@link ModelAtlasClientException}.
 * </p>
 *
 * @since 1.3
 */
public class OperationRefusedException extends ModelAtlasClientException {

	private static final long serialVersionUID = 1L;

	private final transient List<Diagnostic> diagnostics;

	public OperationRefusedException(String message, List<Diagnostic> diagnostics) {
		super(message);
		this.diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
	}

	/**
	 * @return the findings behind the refusal, as recorded on the object; never
	 *         {@code null}, never empty
	 */
	public List<Diagnostic> diagnostics() {
		return diagnostics;
	}
}
