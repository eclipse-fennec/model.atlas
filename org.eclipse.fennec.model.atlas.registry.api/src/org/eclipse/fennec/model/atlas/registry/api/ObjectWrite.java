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

import org.eclipse.emf.ecore.EObject;

/**
 * A request to store an instance.
 *
 * @param address      where to store it
 * @param content      the object; its type must be a root type of the registry
 * @param name         a human-readable name, may be {@code null}
 * @param version      the version to record, may be {@code null}
 * @param onConflict   what a create does when the address is taken; ignored by updates.
 *                     {@code null} means {@link ConflictPolicy#FAIL}
 * @param precondition checked against the existing object before it is replaced; may be
 *                     {@code null}
 */
public record ObjectWrite(ObjectAddress address, EObject content, String name, String version,
		ConflictPolicy onConflict, Precondition precondition) {
}
