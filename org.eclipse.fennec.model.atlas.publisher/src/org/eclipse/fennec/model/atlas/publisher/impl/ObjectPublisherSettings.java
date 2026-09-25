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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Where objects go and what may be sent, resolved from configuration once at
 * activation.
 * <p>
 * None of it is an agent parameter. The scope, the registry and the stage are
 * the deployment's choice, {@code overwrite} must not be something an agent can
 * flip, and {@code maxBodyBytes} bounds content that — unlike a published
 * package — the agent itself wrote.
 * <p>
 * There is deliberately no namespace allow-list here, the control its
 * package-publishing counterpart {@link PackagePublisherSettings} carries. The type of
 * an object is decided by the configured registry, which accepts only its own
 * root EClasses and rejects everything else server-side; a second, in-band type
 * check here would have to parse a body whose format the deployment chooses, and
 * would still not be the authority.
 *
 * @param scope          the model.atlas scope, e.g. {@code jena}
 * @param registry       the object registry within that scope, e.g. {@code default}
 * @param allowedStages  the stages this publisher may write into; empty denies everything
 * @param registriesPath the segment between scope and registry name — {@code registries}, matching
 *                       {@code ObjectRegistryResource}'s class-level {@code @Path}
 * @param contentType    the content type the object body is sent as
 * @param overwrite      whether an object already stored under the same id may be replaced
 * @param maxBodyBytes   the largest body accepted from the agent, in bytes
 *
 * @author ilenia
 * @since Sep 10, 2026
 */
public record ObjectPublisherSettings(
		String scope,
		String registry,
		List<String> allowedStages,
		String registriesPath,
		String contentType,
		boolean overwrite,
		int maxBodyBytes) {

	public ObjectPublisherSettings {
		requireText("scope", scope);
		requireText("registry", registry);
		allowedStages = allowedStages == null ? List.of() : List.copyOf(allowedStages);
		requireText("registries.path", registriesPath);
		requireText("content.type", contentType);
		if (maxBodyBytes <= 0) {
			throw new IllegalArgumentException(
					"ModelAtlasObjectPublisher: 'max.body.bytes' must be a positive number of bytes");
		}
	}

	/**
	 * Rejects a property that is present but empty, for the same reason
	 * {@link PackagePublisherSettings} does: an interpolated configuration resolves an
	 * unset environment variable to {@code ""}, a present-but-empty property
	 * overrides the annotation default rather than falling back to it, and a blank
	 * segment would build a request path that fails only at publish time — as an
	 * upstream status no operator can trace back to the configuration.
	 */
	private static void requireText(String property, String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(
					String.format("ModelAtlasObjectPublisher: '%s' must be configured and non-empty", property));
		}
	}

	/**
	 * The create-object endpoint is
	 * {@code POST {scope}/registries/{registry}/stages/{stage}/{objectId}} —
	 * {@code ObjectRegistryResource} is
	 * {@code @Path("/{scopeName}/registries/{registryName}")} with the create method
	 * at {@code /stages/{stageName}/{objectId}}.
	 * <p>
	 * The id is percent-encoded because it is the one path segment that does not
	 * come from configuration. The caller has already rejected a value that is not
	 * a single segment, so what is encoded here is only the characters — a space, a
	 * {@code #}, a {@code %} — that would otherwise end the path or start a
	 * fragment.
	 *
	 * @param objectId the object id, already checked to be a single path segment
	 * @return the path of the create-object endpoint below the base URI
	 */
	public String createObjectPath(String stage, String objectId) {
		return String.join("/", stagePath(stage), encodeSegment(objectId));
	}

	/**
	 * The stage an object goes to: the one the caller named, or the single allowed one when it named
	 * none.
	 * <p>
	 * An empty allowlist denies everything, as {@code publish.nsuri.allowlist} does on the package
	 * publisher: a deployment that installs this bundle without saying where it may write publishes
	 * nowhere, rather than defaulting into a stage nobody chose. A caller that names no stage is
	 * served only when there is exactly one to infer - guessing among several is how an object ends
	 * up in a stage that misdescribes it.
	 *
	 * @param requested the stage the caller asked for, may be {@code null} or blank
	 * @return the stage to write into, never {@code null}
	 * @throws IllegalArgumentException with a caller-facing message when there is no such stage
	 */
	public String stageFor(String requested) {
		if (allowedStages.isEmpty()) {
			throw new IllegalArgumentException(String.format(
					"This runtime is not configured to publish into any stage of registry '%s' in scope '%s'. "
							+ "Nothing was published: set 'allowed.stages' on the publisher.",
					registry, scope));
		}
		if (requested == null || requested.isBlank()) {
			if (allowedStages.size() == 1) {
				return allowedStages.get(0);
			}
			throw new IllegalArgumentException(String.format(
					"No stage was named and this runtime allows %s, so there is nothing to infer. Name the stage "
							+ "the object belongs in.",
					allowedStages));
		}
		if (!allowedStages.contains(requested)) {
			throw new IllegalArgumentException(String.format(
					"This runtime may not publish into the '%s' stage of registry '%s'; it allows %s. Nothing was "
							+ "published, and no parameter of the object fixes it.",
					requested, registry, allowedStages));
		}
		return requested;
	}

	/**
	 * @return the path that tells whether the configured scope, registry and stage
	 *         exist at all — {@code ObjectRegistryResource}'s list-in-stage endpoint
	 */
	public String stagePath(String stage) {
		return String.join("/", scope, registriesPath, registry, "stages", stage);
	}

	/**
	 * {@link URLEncoder} encodes for a query, where a space is {@code +}; in a path
	 * segment a {@code +} is a literal plus, so it is written as {@code %20}
	 * instead. Everything else it escapes is escaped the same way in both.
	 */
	private static String encodeSegment(String segment) {
		return URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20");
	}
}
