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
package org.eclipse.fennec.model.atlas.mcp.tools.api;

import java.util.Optional;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * Where an object the agent did not send comes from.
 * <p>
 * Packages already have this: {@code post_to_model_atlas} takes an nsURI and resolves it through
 * {@code MetadataService} — a whiteboard, so the bundle that holds a package and the bundle that
 * publishes it need not know about each other. Instances had no equivalent. A bundle assembling one
 * — an MCP tool set building it call by call, a dataset the agent filled — keeps it in its own
 * state, and the only route across a bundle boundary was through the agent: the tool took the
 * serialized object as an argument, so the model generated the whole document as output tokens.
 * <p>
 * A source is that missing whiteboard. Register one and the tool can be handed an id and fetch the
 * bytes itself, exactly as the package tool fetches an EPackage.
 * <p>
 * Sources are asked in service-ranking order until one claims the id. A source that does not hold
 * an id returns empty rather than throwing — not holding it is the normal case for every source but
 * one.
 *
 * @author Data In Motion
 * @since Sep 11, 2026
 */
@ConsumerType
public interface PublishableObjectSource {

	/**
	 * The object stored under {@code objectId}, if this source holds it.
	 * <p>
	 * <b>A pure read: asking for an object must not change it.</b> In particular a source that
	 * closes an object once it is stored must not close it here — a fetch is not a store, and the
	 * publish that follows can still be refused. {@link #published(String)} is where that belongs.
	 * <p>
	 * A source may refuse an object it holds but that is not fit to store, by throwing; that fails
	 * the publish and the agent is told why.
	 *
	 * @param objectId    the id the agent named, which is also the id it will be stored under
	 * @param contentType the media type the publisher is configured to send, so a source serializes
	 *                    what the atlas will be told it is receiving rather than its own preference
	 * @return the object, or empty if this source does not hold that id
	 */
	Optional<PublishableObject> find(String objectId, String contentType);

	/**
	 * The object this source supplied was stored.
	 * <p>
	 * Called after the atlas returned a receipt and never when it did not, so a source that closes
	 * an object once it is safely stored knows it actually got there. Called only on the source the
	 * content came from. Anything thrown here is logged and swallowed: the object is already
	 * stored, and a notification must not turn a successful publish into a failed call.
	 *
	 * @param objectId the id that was stored
	 */
	default void published(String objectId) {
		// most sources hold nothing that being stored changes
	}
}
