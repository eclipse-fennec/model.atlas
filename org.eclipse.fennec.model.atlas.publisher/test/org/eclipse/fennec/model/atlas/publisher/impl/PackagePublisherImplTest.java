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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.metadata.MetadataServices;
import org.eclipse.fennec.emf.osgi.metadata.MetadataWhiteboard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.eclipse.fennec.model.atlas.publisher.PublishException;

/**
 * Everything between the agent's namespace URI and the HTTP call: policy,
 * resolution, and the shaping of every upstream status into either a receipt or
 * a message an agent can act on.
 *
 * @author ilenia
 * @since Aug 27, 2026
 */
class PackagePublisherImplTest {

	private static final String SERVER_DETAIL = "org.postgresql.util.PSQLException: relation \"schema\" missing";

	private MetadataWhiteboard whiteboard;

	@BeforeEach
	void setUp() {
		whiteboard = MetadataServices.createWhiteboard();
		EPackage base = TestModels.basePackage();
		whiteboard.registerPackage(base);
		whiteboard.registerPackage(TestModels.derivedPackage(base));
	}

	private static PackagePublisherSettings settings(boolean overwrite) {
		return new PackagePublisherSettings("jena", "draft", "schema", "application/xmi", overwrite,
				List.of(TestModels.DERIVED_NS_URI));
	}

	private PackagePublisherImpl publisher(RecordingTransport transport, boolean overwrite) {
		return new PackagePublisherImpl(whiteboard, settings(overwrite), transport);
	}

	@Test
	void aCreatedPackageComesBackAsAReceipt() {
		RecordingTransport transport = new RecordingTransport(201);

		PackagePublisherImpl.Receipt receipt = publisher(transport, false).publish(TestModels.DERIVED_NS_URI);

		assertThat(receipt.outcome()).isEqualTo("created");
		assertThat(receipt.nsURI()).isEqualTo(TestModels.DERIVED_NS_URI);
		assertThat(receipt.packageName()).isEqualTo("em310udl");
		assertThat(receipt.scope()).isEqualTo("jena");
		assertThat(receipt.stage()).isEqualTo("draft");
		assertThat(receipt.classifierCount()).isEqualTo(1);
		assertThat(receipt.byteSize()).isGreaterThan(0);
	}

	@Test
	void whatGoesOnTheWireIsTheEcoreDocumentAndTheServersQueryParameters() {
		RecordingTransport transport = new RecordingTransport(201);

		publisher(transport, true).publish(TestModels.DERIVED_NS_URI);

		assertThat(transport.path).isEqualTo("jena/schema/stages/draft");
		assertThat(transport.contentType).isEqualTo("application/xmi");
		assertThat(transport.query)
				.containsEntry("nsUri", TestModels.DERIVED_NS_URI)
				.containsEntry("name", "em310udl")
				.containsEntry("overwrite", "true");
		assertThat(transport.body).contains(TestModels.BASE_NS_URI + "#//UplinkMessage");
	}

	@Test
	void overwriteComesFromConfigurationAndNotFromTheCall() {
		RecordingTransport transport = new RecordingTransport(200);

		PackagePublisherImpl.Receipt receipt = publisher(transport, false).publish(TestModels.DERIVED_NS_URI);

		assertThat(receipt.outcome()).isEqualTo("updated");
		assertThat(transport.query).containsEntry("overwrite", "false");
	}

	@Test
	void aNamespaceOutsideTheAllowListNeverReachesTheTransport() {
		RecordingTransport transport = new RecordingTransport(201);

		assertThatThrownBy(() -> publisher(transport, false).publish(TestModels.BASE_NS_URI))
				.isInstanceOf(PublishException.class)
				.hasMessageContaining("not publishable");
		assertThat(transport.path).isNull();
	}

	@Test
	void anUnregisteredNamespaceIsRefusedWithTheRemedy() {
		RecordingTransport transport = new RecordingTransport(201);
		PackagePublisherImpl publisher = new PackagePublisherImpl(whiteboard,
				new PackagePublisherSettings("jena", "draft", "schema", "application/xmi", false, List.of("https://*")),
				transport);

		assertThatThrownBy(() -> publisher.publish("https://eclipse.org/fennec/test/absent"))
				.isInstanceOf(PublishException.class)
				.hasMessageContaining("register_package");
		assertThat(transport.path).isNull();
	}

	@Test
	void aConflictTellsTheAgentTheNamespaceIsTakenRatherThanToRetry() {
		RecordingTransport transport = new RecordingTransport(409, SERVER_DETAIL);

		assertThatThrownBy(() -> publisher(transport, false).publish(TestModels.DERIVED_NS_URI))
				.isInstanceOf(PublishException.class)
				.hasMessageContaining("already published")
				.hasMessageContaining("draft");
	}

	@Test
	void aReadOnlyEntryIsReported() {
		RecordingTransport transport = new RecordingTransport(403);

		assertThatThrownBy(() -> publisher(transport, true).publish(TestModels.DERIVED_NS_URI))
				.isInstanceOf(PublishException.class)
				.hasMessageContaining("read-only");
	}

	@Test
	void aBadRequestOnAStageTheServerHasIsReportedAsAnInvalidPackage() {
		RecordingTransport transport = new RecordingTransport(400).withStageStatus(200);

		assertThatThrownBy(() -> publisher(transport, false).publish(TestModels.DERIVED_NS_URI))
				.isInstanceOf(PublishException.class)
				.hasMessageContaining("as invalid");
		assertThat(transport.gets).containsExactly("jena/schema/stages/draft");
	}

	@Test
	void aBadRequestOnAStageTheServerLacksSaysSo() {
		RecordingTransport transport = new RecordingTransport(400).withStageStatus(400);

		assertThatThrownBy(() -> publisher(transport, false).publish(TestModels.DERIVED_NS_URI))
				.isInstanceOf(PublishException.class)
				.hasMessageContaining("does not have")
				.hasMessageContaining("no tool parameter fixes it");
	}

	@Test
	void anUnreachableServerIsNotSomethingTheAgentShouldRetry() {
		RecordingTransport transport = new RecordingTransport(0);

		assertThatThrownBy(() -> publisher(transport, false).publish(TestModels.DERIVED_NS_URI))
				.isInstanceOf(PublishException.class)
				.hasMessageContaining("could not be reached")
				.hasMessageContaining("stop retrying");
	}

	@Test
	void rejectedCredentialsAreReportedWithoutTheServerSaying() {
		RecordingTransport transport = new RecordingTransport(401, SERVER_DETAIL);

		assertThatThrownBy(() -> publisher(transport, false).publish(TestModels.DERIVED_NS_URI))
				.isInstanceOf(PublishException.class)
				.hasMessageContaining("credentials");
	}

	@Test
	void anUnsupportedMediaTypeNamesTheConfiguredTypeAsTheMismatch() {
		RecordingTransport transport = new RecordingTransport(415);

		assertThatThrownBy(() -> publisher(transport, false).publish(TestModels.DERIVED_NS_URI))
				.isInstanceOf(PublishException.class)
				.hasMessageContaining("application/xmi")
				.hasMessageContaining("deployment mismatch");
	}

	@Test
	void noUpstreamBodyEverReachesTheAgent() {
		for (int status : new int[] { 400, 401, 409, 500 }) {
			RecordingTransport transport = new RecordingTransport(status, SERVER_DETAIL).withStageStatus(200);

			assertThatThrownBy(() -> publisher(transport, false).publish(TestModels.DERIVED_NS_URI))
					.isInstanceOf(PublishException.class)
					.hasMessageNotContaining(SERVER_DETAIL)
					.hasMessageNotContaining("PSQLException");
		}
	}
}
