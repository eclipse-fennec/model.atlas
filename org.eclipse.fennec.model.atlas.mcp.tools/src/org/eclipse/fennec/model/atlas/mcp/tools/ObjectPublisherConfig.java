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
 * Configuration of the model.atlas object publisher.
 * <p>
 * The connection half mirrors {@link PublisherConfig}'s property names — and
 * through it {@code AtlasClientConfig}'s — so one deployment configures the read
 * client, the package publisher and this one the same way. It is a separate
 * factory PID rather than more properties on {@code ModelAtlasPublisher} because
 * the two are independently deployable decisions: a runtime may publish schemas
 * and not instances, or instances and not schemas, and an unconfigured publisher
 * has to be an <em>absent</em> tool rather than a tool that fails on use.
 * <p>
 * Scope, registry and stage are policy, never agent parameters. The agent
 * chooses the object's id and hands over its serialized content; where that
 * content lands is the deployment's decision.
 *
 * @author ilenia
 * @since Sep 10, 2026
 */
@ObjectClassDefinition(name = "Model Atlas Object Publisher", description = "Publishes an agent-supplied object into a model.atlas object registry.")
public @interface ObjectPublisherConfig {

	@AttributeDefinition(name = "Base URI", description = "Required base URI of the model.atlas REST API, e.g. http://host:8080/atlas/rest", required = true)
	String base_uri();

	@AttributeDefinition(name = "Timeout (ms)", description = "Connect and read timeout for every request to the model atlas.", required = false)
	int timeout_ms() default 30_000;

	@AttributeDefinition(name = "Auth token env var", description = "Environment variable holding the bearer token sent as 'Authorization: Bearer'. Leave empty for an unauthenticated atlas. The token is never held in configuration and is read per request, so rotating it needs no reconfiguration.", required = false)
	String auth_token_env() default "";

	@AttributeDefinition(name = "Scope", description = "The model.atlas scope every object goes to, e.g. 'jena'. Not an agent parameter.")
	String scope();

	@AttributeDefinition(name = "Registry", description = "The object registry every object goes to, e.g. 'default'. Not an agent parameter: the registry decides which root EClasses are accepted at all, so it is the deployment's choice of what this tool may write.")
	String registry();

	@AttributeDefinition(name = "Stage", description = "The target stage. Keep this a draft stage: promotion to a released stage is a human decision made in model.atlas, not something an MCP tool should reach.", required = false)
	String stage() default "draft";

	@AttributeDefinition(name = "Registries path", description = "Path segment between the scope and the registry name in the object endpoint. Matches ObjectRegistryResource's class-level @Path; change it only if the server's resource path changes.", required = false)
	String registries_path() default "registries";

	@AttributeDefinition(name = "Content type", description = "The media type the object body is sent as, and therefore the format the agent is told to produce. 'application/json' is the Fennec codec's JSON, whose root object carries the '_type' key; 'application/xmi' and 'application/xml' are the other formats the server accepts.", required = false)
	String content_type() default "application/json";

	@AttributeDefinition(name = "Overwrite", description = "Whether an object already stored under the same id may be replaced. False keeps a 409 meaningful, which is how an agent learns the id is taken.", required = false)
	boolean overwrite() default false;

	@AttributeDefinition(name = "Maximum body bytes", description = "Largest object body accepted from the agent. Unlike a published package, this content comes from the model itself, so its size is not bounded by anything the runtime holds.", required = false)
	int max_body_bytes() default 1048576;
}
