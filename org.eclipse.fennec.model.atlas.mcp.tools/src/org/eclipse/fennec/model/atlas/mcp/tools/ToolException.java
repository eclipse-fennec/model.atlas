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
package org.eclipse.fennec.model.atlas.mcp.tools;

/**
 * Exception carrying a sanitized, agent-facing error message. Messages of this
 * exception are safe to return to an MCP client and should help the agent to
 * self-correct — they never carry the model.atlas base URI, an upstream response
 * body, or anything else about the deployment.
 *
 * @author ilenia
 * @since Aug 26, 2026
 */
public class ToolException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ToolException(String message) {
		super(message);
	}
}
