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
package org.eclipse.fennec.model.atlas.publisher.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.eclipse.fennec.model.atlas.publisher.PublishException;

/**
 * What the agent hands over, what actually goes on the wire, and what it is told
 * when the atlas says no.
 *
 * @author ilenia
 * @since Sep 10, 2026
 */
class ObjectPublisherImplTest {

	private static final String CONTENT = "{\"_type\":\"https://eclipse.org/fennec/test/inference/em310udl#//EM310UDLUplink\",\"distance\":42}";

	private static ObjectPublisherImpl publisher(RecordingTransport transport) {
		return publisher(transport, false, 1024);
	}

	private static ObjectPublisherImpl publisher(RecordingTransport transport, boolean overwrite, int maxBodyBytes) {
		return new ObjectPublisherImpl(new ObjectPublisherSettings("jena", "default", java.util.List.of("draft"),
				"registries", "application/json", overwrite, maxBodyBytes), transport);
	}

	@Test
	void aStoredObjectIsPostedToTheConfiguredRegistryWithTheConfiguredReplacePolicy() {
		RecordingTransport transport = new RecordingTransport(201);

		ObjectPublisherImpl.Receipt receipt = publisher(transport).publish("device-1", CONTENT, "Device 1", "1.0.0");

		assertThat(transport.path).isEqualTo("jena/registries/default/stages/draft/device-1");
		assertThat(transport.contentType).isEqualTo("application/json");
		assertThat(transport.body).isEqualTo(CONTENT);
		// The server spells the replace flag 'override', and it is always sent so the
		// deployment's answer is explicit rather than the endpoint's default.
		assertThat(transport.query).containsEntry("name", "Device 1").containsEntry("version", "1.0.0")
				.containsEntry("override", "false");
		assertThat(receipt.outcome()).isEqualTo("created");
		assertThat(receipt.objectId()).isEqualTo("device-1");
		assertThat(receipt.registry()).isEqualTo("default");
		assertThat(receipt.byteSize()).isEqualTo(CONTENT.length());
	}

	@Test
	void theOptionalParametersAreOmittedRatherThanSentEmpty() {
		RecordingTransport transport = new RecordingTransport(200);

		ObjectPublisherImpl.Receipt receipt = publisher(transport).publish("device-1", CONTENT, null, "   ");

		assertThat(transport.query).doesNotContainKey("name").doesNotContainKey("version");
		assertThat(receipt.outcome()).isEqualTo("updated");
	}

	@Test
	void aDeploymentThatOverwritesSaysSoOnEveryRequest() {
		RecordingTransport transport = new RecordingTransport(200);

		publisher(transport, true, 1024).publish("device-1", CONTENT, null, null);

		assertThat(transport.query).containsEntry("override", "true");
	}

	@Test
	void anIdThatIsNotASinglePathSegmentIsRefusedBeforeAnythingIsSent() {
		RecordingTransport transport = new RecordingTransport(201);

		assertThatExceptionOfType(PublishException.class)
				.isThrownBy(() -> publisher(transport).publish("sensors/device-1", CONTENT, null, null))
				.withMessageContaining("single name");

		assertThat(transport.path).isNull();
	}

	@Test
	void anObjectOverTheConfiguredCapIsRefusedBeforeAnythingIsSent() {
		RecordingTransport transport = new RecordingTransport(201);

		assertThatExceptionOfType(PublishException.class)
				.isThrownBy(() -> publisher(transport, false, 8).publish("device-1", CONTENT, null, null))
				.withMessageContaining("smaller object");

		assertThat(transport.path).isNull();
	}

	@Test
	void emptyContentIsRefusedBeforeAnythingIsSent() {
		RecordingTransport transport = new RecordingTransport(201);

		assertThatExceptionOfType(PublishException.class)
				.isThrownBy(() -> publisher(transport).publish("device-1", "   ", null, null))
				.withMessageContaining("empty");

		assertThat(transport.path).isNull();
	}

	@Test
	void aTakenIdIsReportedAsATakenIdRatherThanAsTheUpstreamBody() {
		assertThatExceptionOfType(PublishException.class)
				.isThrownBy(() -> publisher(new RecordingTransport(409, "internal detail"))
						.publish("device-1", CONTENT, null, null))
				.withMessageContaining("already stored as 'device-1'")
				.withMessageNotContaining("internal detail");
	}

	@Test
	void aStageTheAtlasDoesNotHaveIsSeparatedFromAnObjectItRejects() {
		// The stage probe answering 400 means the destination itself is wrong, which
		// is an operator's problem — no rewrite of the document fixes it.
		RecordingTransport missingStage = new RecordingTransport(400).withStageStatus(400);

		assertThatExceptionOfType(PublishException.class)
				.isThrownBy(() -> publisher(missingStage).publish("device-1", CONTENT, null, null))
				.withMessageContaining("which the model atlas does not have");
		assertThat(missingStage.gets).containsExactly("jena/registries/default/stages/draft");
	}

	@Test
	void anEmptyStageIsNotMistakenForAMissingOne() {
		// The list-in-stage endpoint answers 204 when the stage exists and holds
		// nothing, so only a 4xx there may be read as a destination that is not there.
		RecordingTransport emptyStage = new RecordingTransport(400).withStageStatus(204);

		assertThatExceptionOfType(PublishException.class)
				.isThrownBy(() -> publisher(emptyStage).publish("device-1", CONTENT, null, null))
				.withMessageContaining("rejected the object as invalid")
				.withMessageContaining("_type");
	}

	@Test
	void anUnreachableAtlasTellsTheAgentToStop() {
		assertThatExceptionOfType(PublishException.class)
				.isThrownBy(() -> publisher(new RecordingTransport(0)).publish("device-1", CONTENT, null, null))
				.withMessageContaining("stop retrying");
	}

	@Test
	void aReadOnlyOrAtlasOwnedTypeIsReportedAsSuch() {
		assertThatExceptionOfType(PublishException.class)
				.isThrownBy(() -> publisher(new RecordingTransport(403)).publish("device-1", CONTENT, null, null))
				.withMessageContaining("read-only");
	}

	@Test
	void anUnexpectedStatusStillSaysNothingWasPublished() {
		assertThatExceptionOfType(PublishException.class)
				.isThrownBy(() -> publisher(new RecordingTransport(503)).publish("device-1", CONTENT, null, null))
				.withMessageContaining("status 503")
				.withMessageContaining("Nothing was published");
	}
}
