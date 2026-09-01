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

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Configuration of the model.atlas publisher.
 * <p>
 * The connection half deliberately mirrors {@code AtlasClientConfig}'s property
 * names, so one deployment configures the read client and this publisher the
 * same way. It is re-declared rather than reused because
 * {@code org.eclipse.fennec.model.atlas.rest.client.osgi} exports no packages.
 * <p>
 * The publishing half is policy, never an agent parameter: an agent chooses
 * <em>which</em> registered package to hand over and nothing else. In particular
 * {@code publish.nsuri.allowlist} is empty by default, so a deployment that
 * installs this bundle without configuring it publishes nothing.
 *
 * @author ilenia
 * @since Aug 27, 2026
 */
@ObjectClassDefinition(name = "Model Atlas Publisher", description = "Publishes a registered EPackage to a model.atlas draft stage.")
public @interface PublisherConfig {

	@AttributeDefinition(name = "Base URI", description = "Required base URI of the model.atlas REST API, e.g. http://host:8080/atlas/rest", required = true)
	String base_uri();

	@AttributeDefinition(name = "Timeout (ms)", description = "Connect and read timeout for every request to the model atlas.", required = false)
	int timeout_ms() default 30_000;

	@AttributeDefinition(name = "Auth token env var", description = "Environment variable holding the bearer token sent as 'Authorization: Bearer'. Leave empty for an unauthenticated atlas. The token is never held in configuration and is read per request, so rotating it needs no reconfiguration.", required = false)
	String auth_token_env() default "";

	@AttributeDefinition(name = "Scope", description = "The model.atlas scope every publication goes to, e.g. 'jena'. Not an agent parameter.")
	String scope();

	@AttributeDefinition(name = "Stage", description = "The target stage. Keep this a draft stage: promotion to a released stage is a human decision made in model.atlas, not something an MCP tool should reach.", required = false)
	String stage() default "draft";

	@AttributeDefinition(name = "Packages path", description = "Path segments between the scope and 'stages' in the create-package endpoint. Matches SchemaPackagesResource's class-level @Path; change it only if the server's resource path changes.", required = false)
	String packages_path() default "schema";

	@AttributeDefinition(name = "Content type", description = "The media type the package body is sent as. The server consumes 'application/xmi' and deserializes an EPackage from it.", required = false)
	String content_type() default "application/xmi";

	@AttributeDefinition(name = "Overwrite", description = "Whether an existing package in the stage may be replaced. False keeps a 409 meaningful, which is how an agent learns the namespace is taken.", required = false)
	boolean overwrite() default false;

	@AttributeDefinition(name = "Publishable nsURIs", description = "Namespace URIs, or prefixes ending in '*', that may be published. Empty denies everything — this is the control that stops the tool publishing packages it merely happens to see.", required = false)
	String[] publish_nsuri_allowlist() default {};
}
