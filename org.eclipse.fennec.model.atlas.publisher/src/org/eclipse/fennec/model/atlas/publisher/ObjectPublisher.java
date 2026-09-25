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
package org.eclipse.fennec.model.atlas.publisher;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Stores one object — an instance, not a metamodel — in a model.atlas object
 * registry.
 * <p>
 * The caller supplies the serialized object and nothing else that matters: the
 * scope, the registry, the stage and whether an existing object may be replaced
 * are the deployment's, resolved when the service is configured. A caller that
 * could choose them would be choosing where someone else's data lands.
 * <p>
 * The registry is what makes that safe. A model.atlas object registry accepts
 * only its own root EClasses and refuses everything else with a 400, so naming
 * the registry in configuration is at the same time naming the shape of what a
 * publisher can write. That is why there is no in-band type allow-list to go
 * with it.
 * <p>
 * <b>Deploying this bundle and configuring a publisher is the authorization
 * decision.</b> A runtime with no {@code ModelAtlasObjectPublisher}
 * configuration cannot store an object from code at all, which is why the
 * read-only {@code ModelAtlasClient} still has no write on it.
 *
 * @author ilenia
 * @since Sep 10, 2026
 */
@ProviderType
public interface ObjectPublisher {

	/**
	 * The outcome of one publication, as the caller sees it. Carries no upstream
	 * body and no server address.
	 *
	 * @param outcome     {@code created} or {@code updated}
	 * @param objectId    the id the object is stored under
	 * @param objectName  the human-readable name it was given, or {@code null}
	 * @param version     the version it was given, or {@code null}
	 * @param scope       the scope it went to
	 * @param registry    the object registry it went to
	 * @param stage       the stage it went to
	 * @param contentType the media type the body was sent as
	 * @param byteSize    the size of the sent document
	 */
	record Receipt(
			String outcome,
			String objectId,
			String objectName,
			String version,
			String scope,
			String registry,
			String stage,
			String contentType,
			int byteSize) {
	}

	/**
	 * Publishes one object under the given id.
	 *
	 * @param objectId the id the object is stored under, a single path segment
	 * @param content  the serialized object, in the configured {@link #contentType()}
	 * @param name     a human-readable name, may be {@code null}
	 * @param version  a version string, may be {@code null}
	 * @return the receipt of a successful publication
	 * @throws PublishException with a caller-facing message for every failure
	 */
	Receipt publish(String objectId, String content, String name, String version);

	/**
	 * Publishes one object into a named stage.
	 *
	 * @param objectId the id the object is stored under, a single path segment
	 * @param content  the serialized object, in the configured content type
	 * @param name     a human-readable name, may be {@code null}
	 * @param version  a version string, may be {@code null}
	 * @param stage    the stage to write into. It must be one this publisher is configured to
	 *                 allow; {@code null} or blank means "the one allowed stage", and is refused
	 *                 when the publisher permits several, because then there is nothing to infer
	 * @return the receipt of a successful publication
	 * @throws PublishException if the stage is not permitted, or cannot be inferred
	 */
	Receipt publish(String objectId, String content, String name, String version, String stage);

	/**
	 * The wire format this publisher sends, so a caller assembling an object
	 * serializes what the atlas will be told it is receiving rather than its own
	 * preference. It is the deployment's choice, never the caller's.
	 *
	 * @return the configured content type, or {@code null} while the publisher is not configured
	 */
	String contentType();
}
