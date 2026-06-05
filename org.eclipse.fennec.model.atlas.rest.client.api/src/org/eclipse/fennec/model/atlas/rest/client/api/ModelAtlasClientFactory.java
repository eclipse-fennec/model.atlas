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
package org.eclipse.fennec.model.atlas.rest.client.api;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * Bootstrap SPI that lets {@link ModelAtlasClient#builder()} obtain a builder
 * without this API bundle depending on any implementation.
 * <p>
 * The plain-Java implementation registers exactly one provider via
 * {@link java.util.ServiceLoader} ({@code META-INF/services}); the static
 * {@link ModelAtlasClient#builder()} factory loads it. This mirrors how
 * {@code jakarta.ws.rs.client.ClientBuilder.newBuilder()} locates its own
 * implementation.
 */
@ConsumerType
public interface ModelAtlasClientFactory {

	/**
	 * @return a fresh client builder
	 */
	ModelAtlasClient.Builder builder();
}
