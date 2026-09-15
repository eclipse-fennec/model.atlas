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
import org.eclipse.fennec.model.atlas.publisher.PackagePublisher;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;

/**
 * MCP tool handing one registered EPackage to the model atlas.
 * <p>
 * The agent names a namespace URI and gets back a receipt. It never sees the
 * XMI, never sees the server address, and cannot choose the scope, the stage or
 * whether an existing draft is replaced — so a model inferred in this session can
 * leave the runtime without its serialization ever passing through the LLM, and
 * without the agent being in a position to decide where it lands.
 *
 * @author ilenia
 * @since Aug 27, 2026
 */
@Component(name = "PostToModelAtlasTool", service = MCPTool.class, property = "tool.name=post_to_model_atlas")
public class PostToModelAtlasTool extends AbstractAtlasTool {

	@Reference
	PackagePublisher publisher;

	@Activate
	void activate() {
		this.name = "post_to_model_atlas";
		this.description = "Publish one registered EPackage to this runtime's model atlas, so a metamodel you "
				+ "authored or inferred in this session is handed over as a stored schema rather than pasted "
				+ "into a reply. Name the namespace URI of a package that is already registered — use "
				+ "list_registry for the ones this session registered, or the discovery tools for the ones the "
				+ "runtime always had — and the package is serialized and sent server-side; you never handle "
				+ "the document. The destination scope and stage, and whether an existing entry may be "
				+ "replaced, are fixed by the deployment and are not parameters: if the namespace is already "
				+ "taken you are told so, and the answer is a free namespace, not a retry. Only namespaces the "
				+ "deployment allow-lists can be published at all.";
		this.inputSchema = """
				{
					"type": "object",
					"properties": {
						"nsURI": {
							"type": "string",
							"description": "The namespace URI of the registered package to publish, e.g. 'https://eclipse.org/fennec/inference/em310udl'. It must be the URI the package itself carries."
						}
					},
					"required": ["nsURI"]
				}
				""";
	}

	@Override
	public Mono<McpSchema.CallToolResult> execute(McpAsyncServerExchange exchange, Map<String, Object> arguments) {
		return run(() -> publisher.publish(requireString(arguments, "nsURI")));
	}
}
