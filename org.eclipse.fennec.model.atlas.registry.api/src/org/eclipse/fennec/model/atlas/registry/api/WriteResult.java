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
 * The result of a {@link RegistryWriter} operation.
 *
 * @param outcome  what the operation did; never {@code null}
 * @param metadata the metadata of the object as it is now stored, or {@code null} for
 *                 {@link WriteOutcome#NOT_FOUND} and {@link WriteOutcome#DELETED}
 */
public record WriteResult(WriteOutcome outcome, ObjectMetadata metadata) {

	/**
	 * @param outcome what the operation did
	 * @return a result without metadata
	 */
	public static WriteResult of(WriteOutcome outcome) {
		return new WriteResult(outcome, null);
	}

	/**
	 * @param outcome  what the operation did
	 * @param metadata the stored metadata
	 * @return a result carrying the stored metadata
	 */
	public static WriteResult of(WriteOutcome outcome, ObjectMetadata metadata) {
		return new WriteResult(outcome, metadata);
	}
}
