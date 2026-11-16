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

import org.eclipse.emf.ecore.EPackage;

/**
 * A request to store a package in a scope's schema registry. The package's identity is its
 * nsURI.
 *
 * @param scope        the scope name
 * @param stage        the stage name
 * @param content      the package
 * @param nsUri        the nsURI the caller expects the package to carry, or {@code null} to take
 *                     it from the package; when given it must match
 * @param name         a human-readable name; {@code null} uses the package name
 * @param version      the version to record; {@code null} resolves it from the package's Version
 *                     annotation, then from a version-shaped last nsURI segment
 * @param dcat         whether the package may be published to a DCAT portal; {@code null} means
 *                     false on create and leaves the stored flag untouched on overwrite
 * @param onConflict   what a create does when the nsURI is taken in the stage; ignored by
 *                     updates. {@code null} means {@link ConflictPolicy#FAIL}
 * @param precondition checked against the existing package before it is replaced; may be
 *                     {@code null}
 */
public record PackageWrite(String scope, String stage, EPackage content, String nsUri, String name,
		String version, Boolean dcat, ConflictPolicy onConflict, Precondition precondition) {
}
