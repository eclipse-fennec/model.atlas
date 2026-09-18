/*
 * ******************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Data In Motion Consulting - initial implementation
 * ******************************************************************
 */
package org.eclipse.fennec.model.atlas.mcp.tools.api;

/**
 * One object a {@link PublishableObjectSource} holds, ready to store.
 * <p>
 * {@code name} and {@code version} are on the record rather than left to the agent because a source
 * usually knows them better than the agent does — for a review report, the report's own title and
 * the fingerprint of the model it reviewed. Facts the runtime already holds should not be retyped
 * by a language model.
 *
 * @param content the serialized object, in the content type that was asked for
 * @param name    a human-readable name stored with the object, or {@code null}
 * @param version a version string stored with the object, or {@code null}
 *
 * @author Data In Motion
 * @since Sep 11, 2026
 */
public record PublishableObject(String content, String name, String version) {
}
