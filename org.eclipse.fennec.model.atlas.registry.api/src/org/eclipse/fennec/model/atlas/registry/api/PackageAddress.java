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
 * Where a package lives: one nsURI in one stage of a scope's schema registry. Which registry
 * that is follows from the scope, so it is not part of the address.
 *
 * @param scope the scope name
 * @param stage the stage name
 * @param nsUri the package's namespace URI
 */
public record PackageAddress(String scope, String stage, String nsUri) {
}
