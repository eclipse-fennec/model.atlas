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
package org.eclipse.fennec.model.atlas.rest.client.osgi;

/**
 * Supplies the {@link StagedPackageSink} for one stage-located bridge (#277).
 * <p>
 * This indirection is what keeps the metadata layer genuinely optional. It is a type of this
 * bundle, so {@link AtlasClientComponent} can hold an optional reference to it and still load when
 * the metadata layer is absent — which it could not do if it referenced {@code MetadataWhiteboard}
 * itself, because a DS reference of a type from an optionally-imported package fails the whole
 * component at class load.
 *
 * @author Data In Motion
 */
interface StagedPackageSinkFactory {

	/**
	 * @param scope the bridge's scope
	 * @param stage the bridge's stage, or {@code null} for a stage-free bridge
	 * @return the sink for that bridge, never {@code null}
	 */
	StagedPackageSink sinkFor(String scope, String stage);
}
