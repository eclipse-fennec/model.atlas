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

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

/**
 * Publishes one agent-supplied object into a model.atlas object registry.
 * <p>
 * Where {@link ModelAtlasPublisher} hands over a schema the <em>runtime</em>
 * holds, this hands over an instance the <em>agent</em> wrote: the content
 * cannot come from the metadata layer, because an instance is not something the
 * runtime has until someone states it. So the agent supplies the serialized
 * object and nothing else that matters — the scope, the registry, the stage and
 * whether an existing object may be replaced are all resolved here at
 * activation.
 * <p>
 * The registry is what makes that safe. A model.atlas object registry accepts
 * only its own root EClasses and refuses everything else with a 400, so naming
 * the registry in configuration is at the same time naming the shape of what
 * this tool can write. That is why there is no in-band type allow-list to go
 * with it.
 * <p>
 * This is a bundle-private service for the same reason its package-publishing
 * counterpart is: deploying the bundle and configuring it <em>is</em> the
 * authorization decision, and nothing here is exported.
 *
 * @author ilenia
 * @since Sep 10, 2026
 */
@Designate(ocd = ObjectPublisherConfig.class, factory = true)
@Component(name = "ModelAtlasObjectPublisher", service = ObjectPublisher.class, configurationPid = "ModelAtlasObjectPublisher")
public class ObjectPublisher {

	private static final Logger LOGGER = Logger.getLogger(ObjectPublisher.class.getName());

	private static final String PARAM_NAME = "name";
	private static final String PARAM_VERSION = "version";
	/** The server spells the replace flag 'override'; the configuration property is 'overwrite', as for packages. */
	private static final String PARAM_OVERRIDE = "override";

	/**
	 * Only the {@code URIConverter} of the ResourceSet it makes is used, for the
	 * RESTful URI handler registered on it — see {@link UriHandlerAtlasTransport}
	 * for why publishing goes that way rather than through a JAX-RS client.
	 */
	@Reference
	ResourceSetFactory resourceSetFactory;

	private volatile ObjectPublisherSettings settings;
	private volatile AtlasTransport transport;
	private volatile boolean ownsTransport;

	/**
	 * The outcome of one publication, as the agent sees it. Carries no upstream
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
	public record Receipt(
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

	/** DS constructor. */
	public ObjectPublisher() {
	}

	/** Test constructor: the transport is supplied, so no client is built and none is closed. */
	ObjectPublisher(ObjectPublisherSettings settings, AtlasTransport transport) {
		this.settings = settings;
		this.transport = transport;
		this.ownsTransport = false;
	}

	@Activate
	void activate(ObjectPublisherConfig config) {
		// Not folded into ObjectPublisherSettings: the base URI belongs to the
		// transport, not to the publishing policy. Checked here for the same reason as
		// the policy's own properties — an unset environment variable interpolates to
		// "", which would otherwise surface as an unreachable atlas.
		if (config.base_uri() == null || config.base_uri().isBlank()) {
			throw new IllegalArgumentException("ModelAtlasObjectPublisher: 'base.uri' must be configured and non-empty");
		}
		this.settings = new ObjectPublisherSettings(
				config.scope(),
				config.registry(),
				config.stage(),
				config.registries_path(),
				config.content_type(),
				config.overwrite(),
				config.max_body_bytes());
		this.transport = new UriHandlerAtlasTransport(resourceSetFactory.createResourceSet().getURIConverter(),
				config.base_uri(),
				config.auth_token_env(), config.timeout_ms());
		this.ownsTransport = true;
	}

	@Deactivate
	void deactivate() {
		if (ownsTransport && transport instanceof AutoCloseable closeable) {
			try {
				closeable.close();
			} catch (Exception e) {
				LOGGER.log(Level.FINE, e, () -> "Closing the model.atlas transport failed");
			}
		}
	}

	/**
	 * Publishes one object under the given id.
	 *
	 * @param objectId the id the object is stored under, a single path segment
	 * @param content  the serialized object, in the configured content type
	 * @param name     a human-readable name, may be {@code null}
	 * @param version  a version string, may be {@code null}
	 * @return the receipt of a successful publication
	 * @throws ToolException with an agent-facing message for every failure
	 */
	public Receipt publish(String objectId, String content, String name, String version) {
		ObjectPublisherSettings current = settings;
		requireSingleSegment(objectId);
		if (content == null || content.isBlank()) {
			throw new ToolException("The object content is empty. Send the serialized object as the 'content' "
					+ "parameter — an object with no content is not something the atlas can store.");
		}
		int byteSize = content.getBytes(StandardCharsets.UTF_8).length;
		if (byteSize > current.maxBodyBytes()) {
			throw new ToolException(String.format(
					"The object is %d bytes and this runtime accepts at most %d. Nothing was published. Send a "
							+ "smaller object — splitting one oversized document into several stored objects is the "
							+ "way out, not a retry.",
					byteSize, current.maxBodyBytes()));
		}

		Map<String, String> query = new LinkedHashMap<>();
		if (name != null && !name.isBlank()) {
			query.put(PARAM_NAME, name);
		}
		if (version != null && !version.isBlank()) {
			query.put(PARAM_VERSION, version);
		}
		query.put(PARAM_OVERRIDE, Boolean.toString(current.overwrite()));

		AtlasTransport.Result response = transport.post(current.createObjectPath(objectId), query,
				current.contentType(), content);
		return receiptOf(response, current, objectId, name, version, byteSize);
	}

	/**
	 * An object id is one path segment. A value carrying a separator would either
	 * address a different endpoint or, percent-encoded, be stored under an id
	 * nobody can read back — so it is refused here, where the message can say what
	 * a usable id looks like.
	 */
	private static void requireSingleSegment(String objectId) {
		if (objectId == null || objectId.isBlank()) {
			throw new ToolException("An object id is required: it is the name the object is stored and read back under.");
		}
		if (objectId.indexOf('/') >= 0 || ".".equals(objectId) || "..".equals(objectId)) {
			throw new ToolException(String.format(
					"'%s' is not a usable object id. An id is a single name — no '/', and not '.' or '..'. "
							+ "Use a qualified name with another separator if you need structure, e.g. 'sensors.em310udl.device-1'.",
					objectId));
		}
	}

	/**
	 * Turns an upstream status into either a receipt or a message the agent can act
	 * on. The upstream body never crosses this method — it goes to the log, where
	 * an operator can read it — because it is written for whoever runs the server,
	 * not for whoever is talking to the agent.
	 */
	private Receipt receiptOf(AtlasTransport.Result response, ObjectPublisherSettings current, String objectId,
			String name, String version, int byteSize) {
		if (!response.reached()) {
			throw new ToolException(
					"The model atlas could not be reached. Nothing was published; this is not something you can "
							+ "correct by changing the object — report it and stop retrying.");
		}
		logUpstream(response, objectId);
		return switch (response.status()) {
		case 201 -> receipt("created", current, objectId, name, version, byteSize);
		case 200 -> receipt("updated", current, objectId, name, version, byteSize);
		case 409 -> throw new ToolException(String.format(
				"An object is already stored as '%s' in the '%s' stage of registry '%s', and this runtime does not "
						+ "replace what is there. Store it under an id that is still free, or ask for the existing "
						+ "object to be replaced by hand.",
				objectId, current.stage(), current.registry()));
		case 403 -> throw new ToolException(String.format(
				"The object stored as '%s' cannot be written: either it is read-only, or its type is content the "
						+ "atlas produces itself and will not accept from a client.",
				objectId));
		case 400 -> throw new ToolException(badRequestMessage(current));
		case 401, 407 -> throw new ToolException(
				"The model atlas rejected this runtime's credentials. Nothing was published, and no tool "
						+ "parameter fixes it.");
		case 415 -> throw new ToolException(String.format(
				"The model atlas does not accept '%s' for an object body. This is a deployment mismatch, not "
						+ "something the object can be changed to satisfy.", current.contentType()));
		default -> throw new ToolException(String.format(
				"The model atlas refused the object with status %d. Nothing was published.", response.status()));
		};
	}

	private static Receipt receipt(String outcome, ObjectPublisherSettings current, String objectId, String name,
			String version, int byteSize) {
		return new Receipt(outcome, objectId, name, version, current.scope(), current.registry(), current.stage(),
				current.contentType(), byteSize);
	}

	/**
	 * A 400 conflates two very different things: a scope, registry or stage the
	 * server does not have, and an object it will not accept. One GET separates
	 * them, and the difference is the difference between an agent retrying
	 * pointlessly and an agent fixing its document.
	 */
	private String badRequestMessage(ObjectPublisherSettings current) {
		AtlasTransport.Result stage = transport.get(current.stagePath());
		if (stage.reached() && stage.status() >= 400) {
			return String.format(
					"This runtime is configured to write into the '%s' stage of registry '%s' in scope '%s', which "
							+ "the model atlas does not have. Nothing was published, and no tool parameter fixes it.",
					current.stage(), current.registry(), current.scope());
		}
		return String.format(
				"The model atlas rejected the object as invalid for registry '%s'. Either its type is not one the "
						+ "registry accepts, or the document does not say which type it is: a '%s' body has to name "
						+ "its root type the way that format does — for the codec's JSON that is the '_type' key "
						+ "holding '<nsURI>#//<EClass>'. The schema itself must already be published in this scope.",
				current.registry(), current.contentType());
	}

	private static void logUpstream(AtlasTransport.Result response, String objectId) {
		if (response.status() >= 400) {
			LOGGER.log(Level.WARNING, () -> String.format("model.atlas refused object '%s' with status %d: %s",
					objectId, response.status(), response.body()));
		}
	}

	/** @return the resolved settings, for the tool's description of what it will do */
	ObjectPublisherSettings settings() {
		return settings;
	}
}
