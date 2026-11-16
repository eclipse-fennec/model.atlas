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

/**
 * What a create does when its target already holds an object under the same identity
 * (the objectId of an instance, the nsURI of a package).
 */
public enum ConflictPolicy {

	/** Refuse with {@link RegistryAccessException.Reason#ALREADY_EXISTS}. */
	FAIL,

	/** Replace the existing object, subject to its read-only state and the precondition. */
	OVERWRITE,

	/** Leave the existing object alone and report {@link WriteOutcome#SKIPPED}. */
	SKIP
}
