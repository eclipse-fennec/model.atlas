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

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;

/**
 * An optimistic-concurrency check on the object a write is about to change, evaluated against
 * its metadata as the writer has just read it. The REST layer expresses {@code If-Match} as one.
 */
@FunctionalInterface
public interface Precondition {

	/** No precondition: every state is acceptable. */
	Precondition NONE = metadata -> true;

	/**
	 * @param current the current metadata of the object the write addresses; never {@code null}
	 * @return {@code true} if the write may proceed; {@code false} refuses it with
	 *         {@link RegistryAccessException.Reason#PRECONDITION_FAILED}
	 */
	boolean test(ObjectMetadata current);

	/**
	 * @param precondition a precondition, may be {@code null}
	 * @return {@code precondition}, or {@link #NONE} for {@code null}
	 */
	static Precondition orNone(Precondition precondition) {
		return precondition == null ? NONE : precondition;
	}
}
