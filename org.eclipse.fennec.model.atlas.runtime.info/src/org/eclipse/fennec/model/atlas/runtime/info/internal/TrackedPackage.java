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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.runtime.info.internal;

/**
 * One live EPackage service, reduced to what the snapshot needs.
 *
 * <p>
 * Everything here comes off the service properties the Atlas set when it registered the package, so
 * reading it costs nothing and reaches no storage. {@code stage} is part of the identity: the same
 * nsURI is live in several stages at once, in different versions.
 * </p>
 *
 * @param serviceId   the {@code service.id} of the EPackage service
 * @param scope       the scope the package was registered for
 * @param stage       the stage the package is live in
 * @param nsURI       the namespace URI
 * @param name        the package name
 * @param version     the declared model version
 * @param fingerprint the semantic identity, or {@code null} when it was not computed
 * @param stored      whether the Atlas registered it out of a registry, as opposed to a bundle
 *                    bringing it
 */
record TrackedPackage(long serviceId, String scope, String stage, String nsURI, String name, String version,
        String fingerprint, boolean stored) {
}
