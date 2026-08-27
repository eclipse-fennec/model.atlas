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
package org.eclipse.fennec.model.atlas.dcat.api;

/**
 * One publishable thing: an EPackage in one stage of one scope, as the DCAT publisher sees it.
 *
 * <p>
 * Every field is read straight off the registered {@code EPackage} service's properties, so a
 * target can be built without touching storage or the workflow API. {@code scope} is the scope
 * that <em>defines</em> the package, never one that merely inherits it — the Dataset id derives
 * from it and one Dataset is linked into the descendants' Catalogs rather than copied.
 * </p>
 *
 * @param scope       the defining scope, from {@code emf.model.scope}
 * @param stage       the stage the package is registered in, from {@code atlas.stage}
 * @param nsUri       the namespace URI, from {@code emf.nsURI}
 * @param version     the package version, from {@code emf.version}
 * @param fingerprint the content fingerprint, from {@code emf.fingerprint}; drives change detection
 */
public record PublicationTarget(String scope, String stage, String nsUri, String version, String fingerprint) {
}
