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

import org.eclipse.fennec.mcp.api.MCPTool;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;

/**
 * MCP tool storing one agent-supplied object in the model atlas.
 * <p>
 * The counterpart of {@link PostToModelAtlasTool} for instances rather than
 * schemas. An instance is not something the runtime holds until someone states
 * it, so here the agent does hand over the document — but it still names only
 * <em>what</em> to store and under which id: the scope, the object registry and
 * the stage are the deployment's, and so is whether an object already stored
 * under that id may be replaced.
 * <p>
 * The expected body format is not a parameter either. It is the content type the
 * deployment configured, and the tool says which one that is in its own
 * description, so the agent is told rather than left to guess.
 *
 * @author ilenia
 * @since Sep 10, 2026
 */
@Component(name = "PostObjectToModelAtlasTool", service = MCPTool.class, property = "tool.name=post_object_to_model_atlas")
public class PostObjectToModelAtlasTool extends AbstractAtlasTool {

	@Reference
	ObjectPublisher publisher;

	@Activate
	void activate() {
		this.name = "post_object_to_model_atlas";
		this.description = "Store one object — an instance, not a metamodel — in this runtime's model atlas, so "
				+ "data you assembled in this session is handed over as a stored, versioned object rather than "
				+ "pasted into a reply. Send the serialized object as 'content' and the id it should be stored "
				+ "under as 'objectId'. " + bodyFormatSentence() + " The object's type must already be published "
				+ "as a schema in the atlas — use post_to_model_atlas first for a metamodel you authored here — "
				+ "and it must be a type the destination registry accepts, or the atlas refuses it. The scope, "
				+ "the object registry, the stage and whether an existing object may be replaced are fixed by the "
				+ "deployment and are not parameters: if the id is already taken you are told so, and the answer "
				+ "is a free id, not a retry.";
		this.inputSchema = """
				{
					"type": "object",
					"properties": {
						"objectId": {
							"type": "string",
							"description": "The id the object is stored and read back under. A single name — no '/', and not '.' or '..'; use another separator for a qualified id, e.g. 'sensors.em310udl.device-1'."
						},
						"content": {
							"type": "string",
							"description": "The serialized object, in the content type this tool's description names. It must state its own root type the way that format does; for the codec's JSON that is a '_type' key holding '<nsURI>#//<EClass>'."
						},
						"name": {
							"type": "string",
							"description": "Optional human-readable name stored with the object."
						},
						"version": {
							"type": "string",
							"description": "Optional version string stored with the object, e.g. '1.0.0'."
						}
					},
					"required": ["objectId", "content"]
				}
				""";
	}

	/**
	 * Names the configured content type in the description, because an agent that
	 * has to guess between the codec's JSON and XMI guesses wrong. The publisher is
	 * a mandatory static reference, so it is configured before this runs; the
	 * fallback covers only a settings-less test double.
	 */
	private String bodyFormatSentence() {
		ObjectPublisherSettings settings = publisher == null ? null : publisher.settings();
		if (settings == null) {
			return "Send it in the content type this deployment configured.";
		}
		return String.format("Send it as '%s'.", settings.contentType());
	}

	@Override
	public Mono<McpSchema.CallToolResult> execute(McpAsyncServerExchange exchange, Map<String, Object> arguments) {
		return run(() -> publisher.publish(
				requireString(arguments, "objectId"),
				requireString(arguments, "content"),
				optionalString(arguments, "name"),
				optionalString(arguments, "version")));
	}
}
