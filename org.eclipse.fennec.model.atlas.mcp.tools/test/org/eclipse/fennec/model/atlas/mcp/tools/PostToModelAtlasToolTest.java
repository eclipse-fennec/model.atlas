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

import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.metadata.MetadataServices;
import org.eclipse.fennec.emf.osgi.metadata.MetadataWhiteboard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.spec.McpSchema;
import tools.jackson.databind.json.JsonMapper;

/**
 * The MCP surface: one parameter in, a receipt or a sanitized message out.
 *
 * @author ilenia
 * @since Aug 27, 2026
 */
class PostToModelAtlasToolTest {

	private static final JsonMapper MAPPER = JsonMapper.builder().build();

	private MetadataWhiteboard whiteboard;

	@BeforeEach
	void setUp() {
		whiteboard = MetadataServices.createWhiteboard();
		EPackage base = TestModels.basePackage();
		whiteboard.registerPackage(base);
		whiteboard.registerPackage(TestModels.derivedPackage(base));
	}

	private PostToModelAtlasTool tool(RecordingTransport transport) {
		PublisherSettings settings = new PublisherSettings("jena", "draft", "schema", "application/xmi", false,
				List.of(TestModels.DERIVED_NS_URI));
		PostToModelAtlasTool tool = new PostToModelAtlasTool();
		tool.publisher = new ModelAtlasPublisher(whiteboard, settings, transport);
		tool.activate();
		return tool;
	}

	@Test
	void theToolAdvertisesOnlyTheNamespaceParameter() {
		PostToModelAtlasTool tool = tool(new RecordingTransport(201));

		assertThat(tool.getName()).isEqualTo("post_to_model_atlas");
		assertThat(tool.getInputSchema()).contains("nsURI");
		// Where it lands is not the agent's to choose, so it is not in the schema.
		assertThat(tool.getInputSchema()).doesNotContain("scope").doesNotContain("stage")
				.doesNotContain("overwrite");
	}

	@Test
	void aSuccessfulPublicationIsReturnedAsJson() {
		McpSchema.CallToolResult result = tool(new RecordingTransport(201))
				.execute(null, Map.of("nsURI", TestModels.DERIVED_NS_URI)).block();

		assertThat(result).isNotNull();
		assertThat(result.isError()).isNotEqualTo(Boolean.TRUE);
		@SuppressWarnings("unchecked")
		Map<String, Object> payload = MAPPER.readValue(text(result), Map.class);
		assertThat(payload)
				.containsEntry("outcome", "created")
				.containsEntry("nsURI", TestModels.DERIVED_NS_URI)
				.containsEntry("stage", "draft");
	}

	@Test
	void aMissingNamespaceIsAnErrorResultRatherThanAThrow() {
		McpSchema.CallToolResult result = tool(new RecordingTransport(201)).execute(null, Map.of()).block();

		assertThat(result).isNotNull();
		assertThat(result.isError()).isEqualTo(Boolean.TRUE);
		assertThat(text(result)).contains("nsURI");
	}

	@Test
	void aRefusedPublicationReachesTheAgentAsTheSanitizedMessage() {
		McpSchema.CallToolResult result = tool(new RecordingTransport(409, "internal detail"))
				.execute(null, Map.of("nsURI", TestModels.DERIVED_NS_URI)).block();

		assertThat(result).isNotNull();
		assertThat(result.isError()).isEqualTo(Boolean.TRUE);
		assertThat(text(result)).contains("already published").doesNotContain("internal detail");
	}

	private static String text(McpSchema.CallToolResult result) {
		return ((McpSchema.TextContent) result.content().get(0)).text();
	}
}
