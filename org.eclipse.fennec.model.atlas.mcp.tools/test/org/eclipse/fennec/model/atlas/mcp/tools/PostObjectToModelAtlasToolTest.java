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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.spec.McpSchema;
import tools.jackson.databind.json.JsonMapper;

/**
 * The MCP surface: the object and its id in, a receipt or a sanitized message
 * out.
 *
 * @author ilenia
 * @since Sep 10, 2026
 */
class PostObjectToModelAtlasToolTest {

	private static final JsonMapper MAPPER = JsonMapper.builder().build();

	private static final String CONTENT = "{\"_type\":\"https://eclipse.org/fennec/test/inference/em310udl#//EM310UDLUplink\",\"distance\":42}";

	private static PostObjectToModelAtlasTool tool(RecordingTransport transport) {
		ObjectPublisherSettings settings = new ObjectPublisherSettings("jena", "default", "draft", "registries",
				"application/json", false, 1024);
		PostObjectToModelAtlasTool tool = new PostObjectToModelAtlasTool();
		tool.publisher = new ObjectPublisher(settings, transport);
		tool.activate();
		return tool;
	}

	@Test
	void theToolAdvertisesTheObjectAndItsIdAndNothingAboutWhereItLands() {
		PostObjectToModelAtlasTool tool = tool(new RecordingTransport(201));

		assertThat(tool.getName()).isEqualTo("post_object_to_model_atlas");
		assertThat(tool.getInputSchema()).contains("objectId").contains("content");
		// Where it lands is not the agent's to choose, so it is not in the schema.
		assertThat(tool.getInputSchema()).doesNotContain("scope").doesNotContain("stage")
				.doesNotContain("registry").doesNotContain("override");
	}

	@Test
	void theDescriptionNamesTheFormatTheDeploymentExpects() {
		// An agent that has to guess between the codec's JSON and XMI guesses wrong.
		assertThat(tool(new RecordingTransport(201)).getDescription()).contains("application/json");
	}

	@Test
	void aStoredObjectIsReturnedAsJson() {
		McpSchema.CallToolResult result = tool(new RecordingTransport(201))
				.execute(null, Map.of("objectId", "device-1", "content", CONTENT, "version", "1.0.0")).block();

		assertThat(result).isNotNull();
		assertThat(result.isError()).isNotEqualTo(Boolean.TRUE);
		@SuppressWarnings("unchecked")
		Map<String, Object> payload = MAPPER.readValue(text(result), Map.class);
		assertThat(payload)
				.containsEntry("outcome", "created")
				.containsEntry("objectId", "device-1")
				.containsEntry("registry", "default")
				.containsEntry("stage", "draft")
				.containsEntry("version", "1.0.0");
	}

	@Test
	void aMissingContentIsAnErrorResultRatherThanAThrow() {
		McpSchema.CallToolResult result = tool(new RecordingTransport(201))
				.execute(null, Map.of("objectId", "device-1")).block();

		assertThat(result).isNotNull();
		assertThat(result.isError()).isEqualTo(Boolean.TRUE);
		assertThat(text(result)).contains("content");
	}

	@Test
	void aRefusedObjectReachesTheAgentAsTheSanitizedMessage() {
		McpSchema.CallToolResult result = tool(new RecordingTransport(409, "internal detail"))
				.execute(null, Map.of("objectId", "device-1", "content", CONTENT)).block();

		assertThat(result).isNotNull();
		assertThat(result.isError()).isEqualTo(Boolean.TRUE);
		assertThat(text(result)).contains("already stored").doesNotContain("internal detail");
	}

	private static String text(McpSchema.CallToolResult result) {
		return ((McpSchema.TextContent) result.content().get(0)).text();
	}
}
