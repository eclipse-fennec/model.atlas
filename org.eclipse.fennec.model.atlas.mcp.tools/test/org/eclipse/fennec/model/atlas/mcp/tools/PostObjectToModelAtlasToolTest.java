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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.fennec.model.atlas.mcp.tools.api.PublishableObject;
import org.eclipse.fennec.model.atlas.mcp.tools.api.PublishableObjectSource;
import org.eclipse.fennec.model.atlas.publisher.ObjectPublisher;
import org.eclipse.fennec.model.atlas.publisher.PublishException;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Constants;

import io.modelcontextprotocol.spec.McpSchema;
import tools.jackson.databind.json.JsonMapper;

/**
 * The MCP surface: the object and its id in, a receipt or a sanitized message
 * out.
 * <p>
 * What the publisher does with the object — the destination, the size cap, the
 * upstream status — is tested where it lives, in
 * {@code org.eclipse.fennec.model.atlas.publisher}. Here it is a stand-in, so
 * that what is asserted is the tool: which source is asked, what reaches the
 * publisher, who is notified afterwards.
 *
 * @author ilenia
 * @since Sep 10, 2026
 */
class PostObjectToModelAtlasToolTest {

	private static final JsonMapper MAPPER = JsonMapper.builder().build();

	private static final String CONTENT = "{\"_type\":\"https://eclipse.org/fennec/test/inference/em310udl#//EM310UDLUplink\",\"distance\":42}";

	/** A publisher that records what it was handed and answers with a canned receipt — or refuses. */
	private static final class FakePublisher implements ObjectPublisher {
		String objectId;
		String content;
		String name;
		String version;
		String stage;
		RuntimeException failure;

		/**
		 * The tool names no stage: which stage an agent may write into is the deployment's choice,
		 * expressed as the publisher's allowed.stages, not a tool parameter.
		 */
		@Override
		public Receipt publish(String objectId, String content, String name, String version, String stage) {
			this.stage = stage;
			return publish(objectId, content, name, version);
		}

		@Override
		public Receipt publish(String objectId, String content, String name, String version) {
			this.objectId = objectId;
			this.content = content;
			this.name = name;
			this.version = version;
			if (failure != null) {
				throw failure;
			}
			return new Receipt("created", objectId, name, version, "jena", "default", "draft", contentType(),
					content.length());
		}

		@Override
		public String contentType() {
			return "application/json";
		}
	}

	private static PostObjectToModelAtlasTool tool(FakePublisher publisher, FakeSource... sources) {
		PostObjectToModelAtlasTool tool = new PostObjectToModelAtlasTool();
		tool.publisher = publisher;
		long serviceId = 1;
		for (FakeSource source : sources) {
			tool.addSource(source, Map.of(Constants.SERVICE_RANKING, source.ranking, Constants.SERVICE_ID,
					serviceId++));
		}
		tool.activate();
		return tool;
	}

	/** A source holding at most one id, recording what it was asked and what it was told. */
	private static final class FakeSource implements PublishableObjectSource {
		private final String heldId;
		private final PublishableObject held;
		private final int ranking;
		final List<String> asked = new ArrayList<>();
		final List<String> notified = new ArrayList<>();
		RuntimeException findFailure;
		RuntimeException publishedFailure;
		String lastContentType;

		FakeSource(String heldId, PublishableObject held, int ranking) {
			this.heldId = heldId;
			this.held = held;
			this.ranking = ranking;
		}

		static FakeSource holding(String id, String content) {
			return new FakeSource(id, new PublishableObject(content, null, null), 0);
		}

		static FakeSource holdingNothing() {
			return new FakeSource(null, null, 0);
		}

		@Override
		public Optional<PublishableObject> find(String objectId, String contentType) {
			asked.add(objectId);
			lastContentType = contentType;
			if (findFailure != null) {
				throw findFailure;
			}
			return objectId.equals(heldId) ? Optional.of(held) : Optional.empty();
		}

		@Override
		public void published(String objectId) {
			notified.add(objectId);
			if (publishedFailure != null) {
				throw publishedFailure;
			}
		}
	}

	@Test
	void theToolAdvertisesTheObjectAndItsIdAndNothingAboutWhereItLands() {
		PostObjectToModelAtlasTool tool = tool(new FakePublisher());

		assertThat(tool.getName()).isEqualTo("post_object_to_model_atlas");
		assertThat(tool.getInputSchema()).contains("objectId");
		// The agent names the object; it never carries it.
		assertThat(tool.getInputSchema()).doesNotContain("content");
		// Where it lands is not the agent's to choose, so it is not in the schema.
		assertThat(tool.getInputSchema()).doesNotContain("scope").doesNotContain("stage")
				.doesNotContain("registry").doesNotContain("override");
	}

	@Test
	void theDescriptionNoLongerNamesAWireFormat() {
		// Nothing the agent writes is in that format any more, so telling it one would only invite
		// it to serialize something.
		assertThat(tool(new FakePublisher()).getDescription()).doesNotContain("application/json");
	}

	@Test
	void aStoredObjectIsReturnedAsJson() {
		McpSchema.CallToolResult result = tool(new FakePublisher(), FakeSource.holding("device-1", CONTENT))
				.execute(null, Map.of("objectId", "device-1", "version", "1.0.0")).block();

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
	void anIdNothingHoldsIsAnErrorResultNamingTheId() {
		FakePublisher publisher = new FakePublisher();

		McpSchema.CallToolResult result = tool(publisher, FakeSource.holdingNothing())
				.execute(null, Map.of("objectId", "device-1")).block();

		assertThat(result).isNotNull();
		assertThat(result.isError()).isEqualTo(Boolean.TRUE);
		assertThat(text(result)).contains("device-1").contains("the id is the one that tool gave you");
		assertThat(publisher.content).as("nothing was published").isNull();
	}

	/**
	 * The publisher writes for the agent and the tool adds nothing: the message arrives verbatim.
	 * That it says nothing about the deployment is the publisher's doing, and is tested there.
	 */
	@Test
	void aRefusedObjectReachesTheAgentAsTheSanitizedMessage() {
		FakePublisher publisher = refusing("An object is already stored as 'device-1'");

		McpSchema.CallToolResult result = tool(publisher, FakeSource.holding("device-1", CONTENT))
				.execute(null, Map.of("objectId", "device-1")).block();

		assertThat(result).isNotNull();
		assertThat(result.isError()).isEqualTo(Boolean.TRUE);
		assertThat(text(result)).isEqualTo("An object is already stored as 'device-1'");
	}


	// ---- publish by id: the source whiteboard -----------------------------

	@Test
	void theObjectIsFetchedFromTheSourceThatHoldsIt() {
		FakeSource empty = FakeSource.holdingNothing();
		FakeSource holder = FakeSource.holding("device-1", CONTENT);
		FakePublisher publisher = new FakePublisher();

		McpSchema.CallToolResult result = tool(publisher, empty, holder)
				.execute(null, Map.of("objectId", "device-1")).block();

		assertThat(result.isError()).isNotEqualTo(Boolean.TRUE);
		assertThat(publisher.content).isEqualTo(CONTENT);
		assertThat(empty.asked).containsExactly("device-1");
		assertThat(holder.asked).containsExactly("device-1");
	}

	@Test
	void sourcesAreAskedHighestRankingFirst() {
		FakeSource low = new FakeSource("device-1", new PublishableObject("low", null, null), 0);
		FakeSource high = new FakeSource("device-1", new PublishableObject("high", null, null), 10);
		FakePublisher publisher = new FakePublisher();

		tool(publisher, low, high).execute(null, Map.of("objectId", "device-1")).block();

		assertThat(publisher.content).isEqualTo("high");
	}

	@Test
	void theSourcesNameAndVersionReachThePublisher() {
		FakeSource holder = new FakeSource("report-1",
				new PublishableObject(CONTENT, "GDPR review of clinic 1.0.0", "fp1:abc"), 0);
		FakePublisher publisher = new FakePublisher();

		tool(publisher, holder).execute(null, Map.of("objectId", "report-1")).block();

		assertThat(publisher.name).isEqualTo("GDPR review of clinic 1.0.0");
		assertThat(publisher.version).isEqualTo("fp1:abc");
	}

	/** Facts the runtime holds should not be retyped by a model — but an explicit argument wins. */
	@Test
	void anExplicitNameOverridesTheSources() {
		FakeSource holder = new FakeSource("report-1", new PublishableObject(CONTENT, "from the source", null), 0);
		FakePublisher publisher = new FakePublisher();

		tool(publisher, holder).execute(null, Map.of("objectId", "report-1", "name", "from the agent")).block();

		assertThat(publisher.name).isEqualTo("from the agent");
	}

	@Test
	void theSourceIsToldTheContentTypeTheAtlasWillBeTold() {
		FakeSource holder = FakeSource.holding("device-1", CONTENT);

		tool(new FakePublisher(), holder).execute(null, Map.of("objectId", "device-1")).block();

		assertThat(holder.lastContentType).isEqualTo("application/json");
	}

	/**
	 * Source-supplied content goes through the publisher rather than around it, so the publisher's
	 * own refusals — the size cap among them, tested in the publisher bundle — reach the agent and
	 * leave the source un-notified.
	 */
	@Test
	void aPublisherRefusalOfSourceContentIsNotSwallowed() {
		FakeSource holder = FakeSource.holding("device-1", "x".repeat(2048));
		FakePublisher publisher = refusing("The object is 2048 bytes and this runtime accepts at most 1024");

		McpSchema.CallToolResult result = tool(publisher, holder)
				.execute(null, Map.of("objectId", "device-1")).block();

		assertThat(result.isError()).isEqualTo(Boolean.TRUE);
		assertThat(publisher.content).as("the source's body was handed over, not the agent's").isEqualTo(
				"x".repeat(2048));
		assertThat(holder.notified).as("nothing was stored, so nothing is notified").isEmpty();
	}

	/**
	 * A source that breaks while holding the id must not look like an id nothing holds — the agent
	 * would be sent to re-check an id it got right.
	 */
	@Test
	void aSourceThatThrowsFailsTheCallRatherThanBeingSkipped() {
		FakeSource broken = FakeSource.holding("device-1", CONTENT);
		broken.findFailure = new IllegalStateException("the report has no classifier evaluations");

		McpSchema.CallToolResult result = tool(new FakePublisher(), broken)
				.execute(null, Map.of("objectId", "device-1")).block();

		assertThat(result.isError()).isEqualTo(Boolean.TRUE);
		assertThat(text(result)).contains("classifier evaluations");
	}

	// ---- published(): only after a receipt, only the source it came from ---

	@Test
	void onlyTheSourceTheContentCameFromIsNotified() {
		FakeSource empty = FakeSource.holdingNothing();
		FakeSource holder = FakeSource.holding("device-1", CONTENT);

		tool(new FakePublisher(), empty, holder).execute(null, Map.of("objectId", "device-1")).block();

		assertThat(holder.notified).containsExactly("device-1");
		assertThat(empty.notified).isEmpty();
	}

	/**
	 * The reason this is a separate call and not something the fetch implies: a refused publish
	 * leaves the object open, and a retry can carry whatever was recorded in between.
	 */
	@Test
	void aRefusedPublishNotifiesNothing() {
		FakeSource holder = FakeSource.holding("device-1", CONTENT);

		McpSchema.CallToolResult result = tool(refusing("that id is taken"), holder)
				.execute(null, Map.of("objectId", "device-1")).block();

		assertThat(result.isError()).isEqualTo(Boolean.TRUE);
		assertThat(holder.notified).isEmpty();
	}

	@Test
	void aNotificationThatThrowsDoesNotFailAStoredObject() {
		FakeSource holder = FakeSource.holding("device-1", CONTENT);
		holder.publishedFailure = new IllegalStateException("sealing blew up");

		McpSchema.CallToolResult result = tool(new FakePublisher(), holder)
				.execute(null, Map.of("objectId", "device-1")).block();

		assertThat(result.isError()).as("the object is stored; the notification is not the publish")
				.isNotEqualTo(Boolean.TRUE);
	}

	private static FakePublisher refusing(String message) {
		FakePublisher publisher = new FakePublisher();
		publisher.failure = new PublishException(message);
		return publisher;
	}

	private static String text(McpSchema.CallToolResult result) {
		return ((McpSchema.TextContent) result.content().get(0)).text();
	}
}
