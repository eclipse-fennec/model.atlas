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

import java.util.List;

/**
 * The publishing policy, resolved from configuration once at activation.
 * <p>
 * None of it is an agent parameter. The scope and stage are the deployment's
 * choice, {@code overwrite} must not be something an agent can flip, and
 * {@code publishNsUriAllowList} is the control that stops the tool from
 * publishing packages it merely happens to be able to see.
 *
 * @param scope                 the model.atlas scope, e.g. {@code jena}
 * @param stage                 the target stage, e.g. {@code draft} — never a released stage
 * @param packagesPath          path segments between scope and {@code stages} — {@code schema}, matching
 *                              {@code SchemaPackagesResource}'s class-level {@code @Path}
 * @param contentType           the content type the package body is sent as
 * @param overwrite             whether an existing draft may be replaced; {@code false} keeps 409 meaningful
 * @param publishNsUriAllowList namespace prefixes that may be published; blank entries are dropped, and an
 *                              empty list denies everything
 *
 * @author ilenia
 * @since Aug 26, 2026
 */
public record PublisherSettings(
		String scope,
		String stage,
		String packagesPath,
		String contentType,
		boolean overwrite,
		List<String> publishNsUriAllowList) {

	public PublisherSettings {
		requireText("scope", scope);
		requireText("stage", stage);
		requireText("packages.path", packagesPath);
		requireText("content.type", contentType);
		publishNsUriAllowList = publishNsUriAllowList.stream()
				.filter(rule -> rule != null && !rule.isBlank())
				.toList();
	}

	/**
	 * Rejects a property that is present but empty.
	 * <p>
	 * An interpolated configuration resolves an unset environment variable to
	 * {@code ""}, and a present-but-empty property overrides the annotation
	 * default rather than falling back to it. A blank {@code scope} or
	 * {@code stage} would then build a request path with an empty segment and fail
	 * only at publish time, as an upstream status no operator can trace back to
	 * the configuration — so it fails at activation instead, where the message can
	 * name the property.
	 */
	private static void requireText(String property, String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(
					String.format("ModelAtlasPublisher: '%s' must be configured and non-empty", property));
		}
	}

	/**
	 * Whether a namespace may be published.
	 * <p>
	 * Prefix-anchored on the whole URI, never a substring match: a rule for
	 * {@code https://eclipse.org/fennec/inference/} must not admit
	 * {@code https://evil.example/inference/x}. An empty list denies everything, so
	 * an unconfigured deployment publishes nothing.
	 *
	 * @param nsURI the namespace URI
	 * @return {@code true} if some rule admits it
	 */
	public boolean isPublishable(String nsURI) {
		if (nsURI == null || nsURI.isBlank()) {
			return false;
		}
		for (String rule : publishNsUriAllowList) {
			if (rule.endsWith("*")) {
				if (nsURI.startsWith(rule.substring(0, rule.length() - 1))) {
					return true;
				}
			} else if (rule.equals(nsURI)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The create-package endpoint is {@code POST {scope}/schema/stages/{stage}} —
	 * {@code SchemaPackagesResource} is {@code @Path("/{scopeName}/schema")} with the
	 * create method at {@code /stages/{stageName}}.
	 *
	 * @return the path of the create-package endpoint below the base URI
	 */
	public String createPackagePath() {
		return String.join("/", scope, packagesPath, "stages", stage);
	}

	/** @return the path that tells whether the configured stage exists at all */
	public String stagePath() {
		return String.join("/", scope, "schema", "stages", stage);
	}
}
