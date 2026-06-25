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

/**
 * How the client authenticates against the Atlas. Maps to the
 * {@code auth.type} configuration property.
 */
public enum AuthType {

	/** No authentication (default). */
	NONE,

	/** {@code Authorization: Bearer <token>}, token sourced from {@code auth.token.env}. */
	BEARER,

	/** Mutual TLS — key/trust material configured on the underlying client. */
	MTLS
}
