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

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.mcp.api.AbstractMCPTool;
import org.eclipse.fennec.model.atlas.publisher.PublishException;

import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * What every tool in this bundle does the same way: turn a tool body into an MCP
 * result, and turn a failure into a message the agent can act on.
 * <p>
 * The split between a message written for the agent and everything else is the
 * whole point. A {@link ToolException} — this bundle's own, for an argument the
 * agent got wrong — and a {@link PublishException} — the publisher's, for a
 * publication it refused — both reach the agent verbatim; any other exception is
 * a server-side fault, so it is logged where an operator can read it and reported
 * generically, because a stack trace or an upstream body would otherwise tell
 * whoever is talking to the agent about the deployment.
 *
 * @author ilenia
 * @since Sep 10, 2026
 */
abstract class AbstractAtlasTool extends AbstractMCPTool {

	private static final Logger LOGGER = Logger.getLogger(AbstractAtlasTool.class.getName());

	private static final JsonMapper MAPPER = JsonMapper.builder()
			.enable(SerializationFeature.INDENT_OUTPUT)
			.build();

	/**
	 * Runs the tool body, mapping exceptions to sanitized MCP error results.
	 *
	 * @param body the tool body; a {@code String} result is returned as-is, anything else is written as JSON
	 * @return the result the MCP client sees, never a failed {@link Mono}
	 */
	protected Mono<McpSchema.CallToolResult> run(Callable<Object> body) {
		return Mono.fromCallable(() -> {
			try {
				Object result = body.call();
				String text = result instanceof String string ? string : MAPPER.writeValueAsString(result);
				return McpSchema.CallToolResult.builder().addTextContent(text).build();
			} catch (ToolException | PublishException e) {
				return error(e.getMessage());
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e, () -> String.format("Unexpected error executing MCP tool '%s'", getName()));
				return error("Unexpected server error while executing " + getName()
						+ " — see the server log for details");
			}
		});
	}

	/**
	 * @param arguments the tool arguments, may be {@code null}
	 * @param key       the argument name
	 * @return the argument's value
	 * @throws ToolException if the argument is absent, not a string, or blank
	 */
	protected static String requireString(Map<String, Object> arguments, String key) {
		String value = optionalString(arguments, key);
		if (value == null) {
			throw new ToolException(String.format("Parameter '%s' is required and must be a non-empty string", key));
		}
		return value;
	}

	/**
	 * @param arguments the tool arguments, may be {@code null}
	 * @param key       the argument name
	 * @return the argument's value, or {@code null} when it is absent, not a string, or blank
	 */
	protected static String optionalString(Map<String, Object> arguments, String key) {
		Object value = arguments == null ? null : arguments.get(key);
		return value instanceof String string && !string.isBlank() ? string : null;
	}

	private static McpSchema.CallToolResult error(String message) {
		return McpSchema.CallToolResult.builder().addTextContent(message).isError(true).build();
	}
}
