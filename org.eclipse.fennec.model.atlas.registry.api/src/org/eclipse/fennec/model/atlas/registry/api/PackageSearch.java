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
 * Criteria for {@link RegistryReader#searchPackages(String, PackageSearch)}. Every criterion is
 * optional ({@code null} does not filter); the given ones must all match.
 *
 * @param stage               only packages in this stage
 * @param nsUri               nsURI contains
 * @param nsUriExact          nsURI equals
 * @param name                package name
 * @param prefix              namespace prefix
 * @param classifier          name of a classifier the package contains
 * @param featureName         name of a structural feature the package contains
 * @param featureType         type of a structural feature the package contains
 * @param featureNameTypePair a structural feature by name and type, as {@code name:type}
 * @param limit               the page size
 * @param offset              the number of hits to skip
 */
public record PackageSearch(String stage, String nsUri, String nsUriExact, String name, String prefix,
		String classifier, String featureName, String featureType, String featureNameTypePair, int limit,
		int offset) {
}
