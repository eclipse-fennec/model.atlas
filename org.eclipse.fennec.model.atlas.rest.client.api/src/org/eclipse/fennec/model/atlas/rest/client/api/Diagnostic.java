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
 * One finding the Atlas holds about an object, as its metadata reports it (issue #292): a
 * stage action, a stage gate or another module found something and recorded it. Findings
 * form a tree; a compile failure, say, has one child per error.
 * <p>
 * Severity and status are the server's literals as strings ({@code INFO}, {@code WARNING},
 * {@code ERROR}; {@code OPEN}, {@code ACKNOWLEDGED}, {@code RESOLVED}), so a client built
 * against this API keeps working when the server learns a new one.
 *
 * @param id       the stable id the server derived for the finding; addresses it in later
 *                 calls
 * @param producer the action or module that recorded it
 * @param source   the validator or compiler inside the producer, or {@code null}
 * @param code     the producer's code for the kind of finding
 * @param severity {@code INFO}, {@code WARNING} or {@code ERROR}
 * @param message  the finding, written for people
 * @param category a coarse classification for grouping, or {@code null}
 * @param target   the affected element inside the object, or {@code null} for the object as
 *                 a whole
 * @param status   {@code OPEN}, {@code ACKNOWLEDGED} or {@code RESOLVED}
 * @param children findings that refine this one; never {@code null}
 * @since 1.2
 */
public record Diagnostic(String id, String producer, String source, String code, String severity, String message,
		String category, String target, String status, List<Diagnostic> children) {

	public Diagnostic {
		children = children == null ? List.of() : List.copyOf(children);
	}
}
