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

import jakarta.ws.rs.client.Client;

/**
 * The single seam between client construction and Jakarta RS.
 * <p>
 * Everything above this seam — REST mapping, deserialization, caching, drift
 * detection, configuration handling — is identical between the plain-Java and
 * the OSGi client. The two variants differ only in how a {@link Client} is
 * obtained: the plain-Java implementation builds one via
 * {@code ClientBuilder.newBuilder()}; the OSGi implementation resolves the
 * Whiteboard {@code ClientBuilder} through DS. No code outside an implementation
 * of this SPI should call {@code ClientBuilder} directly.
 * <p>
 * Implementations are responsible for applying the connect/read timeouts and
 * any authentication ({@code bearer} / {@code mTLS}) declared on the supplied
 * {@link ClientConfiguration}.
 */
@ConsumerType
public interface JakartaRsClientProvider {

	/**
	 * Build a configured Jakarta RS {@link Client} for the given configuration.
	 * The caller owns the returned client and closes it when done.
	 *
	 * @param configuration the effective client configuration; never {@code null}
	 * @return a ready-to-use Jakarta RS client
	 */
	Client newClient(ClientConfiguration configuration);
}
