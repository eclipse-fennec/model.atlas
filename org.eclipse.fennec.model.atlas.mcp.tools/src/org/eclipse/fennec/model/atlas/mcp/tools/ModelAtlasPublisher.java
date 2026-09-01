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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.emf.osgi.metadata.MetadataService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

/**
 * Publishes a registered {@link EPackage} to a model.atlas stage.
 * <p>
 * Everything the deployment decides — where to publish, whether an existing draft
 * may be replaced, which namespaces are publishable at all — is resolved here at
 * activation. The tool above it contributes one thing: which registered package
 * the agent named.
 * <p>
 * The package is reached through {@link MetadataService} rather than through the
 * EMF tool bundle's session registry, which is private to that bundle. That is
 * not a workaround: the metadata layer sees an OSGi-registered package and a
 * package the EMF tools registered in this session alike, so a model the agent
 * just inferred is publishable by the same path as one that was always there.
 * <p>
 * This is a bundle-private service on purpose. Deploying the bundle <em>is</em>
 * the authorization decision — a runtime without it cannot publish — so there is
 * no write method on the widely consumed read-only {@code ModelAtlasClient} and
 * nothing here is exported.
 *
 * @author ilenia
 * @since Aug 27, 2026
 */
@Designate(ocd = PublisherConfig.class, factory = true)
@Component(name = "ModelAtlasPublisher", service = ModelAtlasPublisher.class, configurationPid = "ModelAtlasPublisher")
public class ModelAtlasPublisher {

	private static final Logger LOGGER = Logger.getLogger(ModelAtlasPublisher.class.getName());

	/** Query parameter carrying the namespace URI; the server cross-checks it against the body. */
	private static final String PARAM_NS_URI = "nsUri";
	private static final String PARAM_NAME = "name";
	private static final String PARAM_OVERWRITE = "overwrite";

	@Reference
	MetadataService metadata;

	/**
	 * Only the {@code URIConverter} of the ResourceSet it makes is used, for the
	 * RESTful URI handler registered on it. Going through the runtime's factory
	 * rather than building a bare ResourceSet means the deployment's own URI
	 * handlers — a proxy handler, a test double — apply to publishing too.
	 */
	@Reference
	ResourceSetFactory resourceSetFactory;

	private volatile PublisherSettings settings;
	private volatile AtlasTransport transport;
	private volatile boolean ownsTransport;

	/**
	 * The outcome of one publication, as the agent sees it. Carries no upstream
	 * body and no server address.
	 *
	 * @param outcome         {@code created} or {@code updated}
	 * @param nsURI           the published namespace URI
	 * @param packageName     the EPackage's name
	 * @param scope           the scope it went to
	 * @param stage           the stage it went to
	 * @param classifierCount how many classifiers the published package holds
	 * @param byteSize        the size of the serialized document
	 */
	public record Receipt(
			String outcome,
			String nsURI,
			String packageName,
			String scope,
			String stage,
			int classifierCount,
			int byteSize) {
	}

	/** DS constructor. */
	public ModelAtlasPublisher() {
	}

	/** Test constructor: the transport is supplied, so no client is built and none is closed. */
	ModelAtlasPublisher(MetadataService metadata, PublisherSettings settings, AtlasTransport transport) {
		this.metadata = metadata;
		this.settings = settings;
		this.transport = transport;
		this.ownsTransport = false;
	}

	@Activate
	void activate(PublisherConfig config) {
		// Not folded into PublisherSettings: the base URI belongs to the transport,
		// not to the publishing policy. Checked here for the same reason as the
		// policy's own properties — an unset environment variable interpolates to
		// "", which would otherwise surface as an unreachable atlas.
		if (config.base_uri() == null || config.base_uri().isBlank()) {
			throw new IllegalArgumentException("ModelAtlasPublisher: 'base.uri' must be configured and non-empty");
		}
		this.settings = new PublisherSettings(
				config.scope(),
				config.stage(),
				config.packages_path(),
				config.content_type(),
				config.overwrite(),
				List.of(config.publish_nsuri_allowlist()));
		this.transport = new UriHandlerAtlasTransport(resourceSetFactory.createResourceSet().getURIConverter(),
				config.base_uri(),
				config.auth_token_env(), config.timeout_ms());
		this.ownsTransport = true;
		if (settings.publishNsUriAllowList().isEmpty()) {
			LOGGER.log(Level.WARNING,
					"ModelAtlasPublisher activated with no usable publish.nsuri.allowlist rule (blank entries are dropped) — post_to_model_atlas will refuse every package");
		}
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
	 * Publishes the registered package under the given namespace URI.
	 *
	 * @param nsURI the namespace URI of a package the metadata layer knows
	 * @return the receipt of a successful publication
	 * @throws ToolException with an agent-facing message for every failure
	 */
	public Receipt publish(String nsURI) {
		PublisherSettings current = settings;
		if (!current.isPublishable(nsURI)) {
			throw new ToolException(String.format(
					"Namespace '%s' is not publishable. This runtime publishes only the namespaces its "
							+ "publish.nsuri.allowlist names, and that is a deployment decision — no tool "
							+ "parameter changes it.", nsURI));
		}
		EPackage ePackage = metadata.getPackageMetadata(nsURI)
				.map(packageMetadata -> packageMetadata.getEPackage())
				.orElseThrow(() -> new ToolException(String.format(
						"No package is registered under '%s'. Register the package first — an authored package "
								+ "becomes visible here only once register_package has accepted it.", nsURI)));

		String body = EcoreXmi.toXmi(ePackage);
		Map<String, String> query = new LinkedHashMap<>();
		query.put(PARAM_NS_URI, nsURI);
		if (ePackage.getName() != null && !ePackage.getName().isBlank()) {
			query.put(PARAM_NAME, ePackage.getName());
		}
		query.put(PARAM_OVERWRITE, Boolean.toString(current.overwrite()));

		AtlasTransport.Result response = transport.post(current.createPackagePath(), query, current.contentType(),
				body);
		return receiptOf(response, current, nsURI, ePackage, body);
	}

	/**
	 * Turns an upstream status into either a receipt or a message the agent can act
	 * on. The upstream body never crosses this method — it goes to the log, where
	 * an operator can read it — because it is written for whoever runs the server,
	 * not for whoever is talking to the agent.
	 */
	private Receipt receiptOf(AtlasTransport.Result response, PublisherSettings current, String nsURI,
			EPackage ePackage, String body) {
		int byteSize = body.getBytes(StandardCharsets.UTF_8).length;
		if (!response.reached()) {
			throw new ToolException(
					"The model atlas could not be reached. Nothing was published; this is not something you can "
							+ "correct by changing the package — report it and stop retrying.");
		}
		logUpstream(response, nsURI);
		return switch (response.status()) {
		case 201 -> new Receipt("created", nsURI, ePackage.getName(), current.scope(), current.stage(),
				ePackage.getEClassifiers().size(), byteSize);
		case 200 -> new Receipt("updated", nsURI, ePackage.getName(), current.scope(), current.stage(),
				ePackage.getEClassifiers().size(), byteSize);
		case 409 -> throw new ToolException(String.format(
				"A package is already published under '%s' in the '%s' stage, and this runtime does not overwrite. "
						+ "Publish under a namespace that is still free — check first with the discovery tools — "
						+ "or ask for the existing draft to be replaced by hand.",
				nsURI, current.stage()));
		case 403 -> throw new ToolException(String.format(
				"The package published under '%s' is read-only and cannot be replaced.", nsURI));
		case 400 -> throw new ToolException(badRequestMessage(current, nsURI));
		case 401, 407 -> throw new ToolException(
				"The model atlas rejected this runtime's credentials. Nothing was published, and no tool "
						+ "parameter fixes it.");
		case 415 -> throw new ToolException(String.format(
				"The model atlas does not accept '%s' for a package body. This is a deployment mismatch, not "
						+ "something the package can be changed to satisfy.", current.contentType()));
		default -> throw new ToolException(String.format(
				"The model atlas refused the publication with status %d. Nothing was published.",
				response.status()));
		};
	}

	/**
	 * A 400 conflates two very different things: a stage the server does not have,
	 * and a package it will not accept. One GET separates them, and the difference
	 * is the difference between an agent retrying pointlessly and an agent stopping.
	 */
	private String badRequestMessage(PublisherSettings current, String nsURI) {
		AtlasTransport.Result stage = transport.get(current.stagePath());
		if (stage.reached() && stage.status() >= 400) {
			return String.format(
					"This runtime is configured to publish into the '%s' stage of scope '%s', which the model "
							+ "atlas does not have. Nothing was published, and no tool parameter fixes it.",
					current.stage(), current.scope());
		}
		return String.format(
				"The model atlas rejected the package published under '%s' as invalid. Check that the namespace "
						+ "URI you named is the one the package itself carries, and that every classifier it "
						+ "references belongs to a package the atlas can resolve.", nsURI);
	}

	private static void logUpstream(AtlasTransport.Result response, String nsURI) {
		if (response.status() >= 400) {
			LOGGER.log(Level.WARNING, () -> String.format("model.atlas refused '%s' with status %d: %s", nsURI,
					response.status(), response.body()));
		}
	}

	/** @return the resolved settings, for the tool's description of what it will do */
	PublisherSettings settings() {
		return settings;
	}
}
