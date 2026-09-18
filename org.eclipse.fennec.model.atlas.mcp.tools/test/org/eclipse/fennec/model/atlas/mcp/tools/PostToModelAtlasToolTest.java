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

import org.eclipse.fennec.model.atlas.publisher.PackagePublisher;
import org.eclipse.fennec.model.atlas.publisher.PublishException;
import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.spec.McpSchema;
import tools.jackson.databind.json.JsonMapper;

/**
 * The MCP surface: one parameter in, a receipt or a sanitized message out.
 * <p>
 * What the publisher does with the namespace — the allow-list, the wire format,
 * the upstream status — is tested where it lives, in
 * {@code org.eclipse.fennec.model.atlas.publisher}. Here the publisher is a
 * stand-in, so that what is asserted is the tool: what it advertises, what it
 * passes on, and that a refusal reaches the agent as the publisher wrote it.
 *
 * @author ilenia
 * @since Aug 27, 2026
 */
class PostToModelAtlasToolTest {

	private static final JsonMapper MAPPER = JsonMapper.builder().build();

	private static final String NS_URI = "https://eclipse.org/fennec/test/inference/em310udl";

	/** A publisher that records the namespace it was given and answers with a canned receipt — or refuses. */
	private static final class FakePublisher implements PackagePublisher {
		String published;
		RuntimeException failure;

		@Override
		public Receipt publish(String nsURI) {
			published = nsURI;
			if (failure != null) {
				throw failure;
			}
			return new Receipt("created", nsURI, "em310udl", "jena", "draft", 3, 1024);
		}
	}

	private static PostToModelAtlasTool tool(FakePublisher publisher) {
		PostToModelAtlasTool tool = new PostToModelAtlasTool();
		tool.publisher = publisher;
		tool.activate();
		return tool;
	}

	@Test
	void theToolAdvertisesOnlyTheNamespaceParameter() {
		PostToModelAtlasTool tool = tool(new FakePublisher());

		assertThat(tool.getName()).isEqualTo("post_to_model_atlas");
		assertThat(tool.getInputSchema()).contains("nsURI");
		// Where it lands is not the agent's to choose, so it is not in the schema.
		assertThat(tool.getInputSchema()).doesNotContain("scope").doesNotContain("stage")
				.doesNotContain("overwrite");
	}

	@Test
	void aSuccessfulPublicationIsReturnedAsJson() {
		FakePublisher publisher = new FakePublisher();

		McpSchema.CallToolResult result = tool(publisher).execute(null, Map.of("nsURI", NS_URI)).block();

		assertThat(result).isNotNull();
		assertThat(result.isError()).isNotEqualTo(Boolean.TRUE);
		assertThat(publisher.published).isEqualTo(NS_URI);
		@SuppressWarnings("unchecked")
		Map<String, Object> payload = MAPPER.readValue(text(result), Map.class);
		assertThat(payload)
				.containsEntry("outcome", "created")
				.containsEntry("nsURI", NS_URI)
				.containsEntry("stage", "draft");
	}

	@Test
	void aMissingNamespaceIsAnErrorResultRatherThanAThrow() {
		FakePublisher publisher = new FakePublisher();

		McpSchema.CallToolResult result = tool(publisher).execute(null, Map.of()).block();

		assertThat(result).isNotNull();
		assertThat(result.isError()).isEqualTo(Boolean.TRUE);
		assertThat(text(result)).contains("nsURI");
		assertThat(publisher.published).as("nothing was published").isNull();
	}

	/**
	 * The publisher writes for the agent and the tool adds nothing: the message arrives verbatim,
	 * which is also what keeps the deployment out of it — sanitizing is the publisher's job, and it
	 * is tested there.
	 */
	@Test
	void aRefusedPublicationReachesTheAgentAsTheSanitizedMessage() {
		FakePublisher publisher = new FakePublisher();
		publisher.failure = new PublishException("'" + NS_URI + "' is already published in the 'draft' stage");

		McpSchema.CallToolResult result = tool(publisher).execute(null, Map.of("nsURI", NS_URI)).block();

		assertThat(result).isNotNull();
		assertThat(result.isError()).isEqualTo(Boolean.TRUE);
		assertThat(text(result)).isEqualTo("'" + NS_URI + "' is already published in the 'draft' stage");
	}

	/** A fault is not something the agent can act on, so it is reported without saying what broke. */
	@Test
	void aFaultIsReportedGenerically() {
		FakePublisher publisher = new FakePublisher();
		publisher.failure = new IllegalStateException("connection pool exhausted on host atlas-7");

		McpSchema.CallToolResult result = tool(publisher).execute(null, Map.of("nsURI", NS_URI)).block();

		assertThat(result).isNotNull();
		assertThat(result.isError()).isEqualTo(Boolean.TRUE);
		assertThat(text(result)).doesNotContain("atlas-7").contains("post_to_model_atlas");
	}

	private static String text(McpSchema.CallToolResult result) {
		return ((McpSchema.TextContent) result.content().get(0)).text();
	}
}
