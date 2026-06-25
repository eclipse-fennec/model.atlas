/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.rest.client.osgi;

import org.eclipse.fennec.model.atlas.rest.client.api.AuthType;
import org.eclipse.fennec.model.atlas.rest.client.api.ClientConfiguration;
import org.eclipse.fennec.model.atlas.rest.client.api.ResolutionMode;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * ConfigAdmin-backed configuration for the OSGi Atlas client (PID
 * {@code org.eclipse.fennec.model.atlas.rest.client}, factory: one instance per
 * configured Atlas). Attribute method names map to the dotted property names of
 * the design's configuration table (metatype turns {@code _} into {@code .}), so
 * e.g. {@code base_uri()} is {@code base.uri} and {@code auth_keystore_path()} is
 * {@code auth.keystore.path}.
 * <p>
 * Defaults mirror {@link ClientConfiguration.Builder}; {@link AtlasClientComponent}
 * maps these onto a {@link ClientConfiguration}. The two OSGi-only properties
 * ({@code lazy.resolve.timeout.ms}, {@code resource.set.fallback}) live here as
 * well as on {@code ClientConfiguration}.
 */
@ObjectClassDefinition(name = "Model Atlas Client", description = "Connects to a remote Model Atlas and publishes its EPackages as OSGi services.")
public @interface AtlasClientConfig {

	@AttributeDefinition(name = "Base URI", description = "Required base URI of the Atlas REST API, e.g. http://host:8080/atlas/rest")
	String base_uri();

	@AttributeDefinition(name = "Connect timeout (ms)", required = false)
	int connect_timeout_ms() default 5_000;

	@AttributeDefinition(name = "Read timeout (ms)", required = false)
	int read_timeout_ms() default 30_000;

	@AttributeDefinition(name = "Resolution mode", description = "EAGER pre-fetches on activation; LAZY fetches on demand; HYBRID pre-fetches eager.nsuri.allow.list and lazily resolves the rest.", required = false)
	ResolutionMode mode() default ResolutionMode.LAZY;

	@AttributeDefinition(name = "Eager scopes", description = "Scopes pre-fetched in EAGER/HYBRID mode; empty + EAGER means all configured scopes.", required = false)
	String[] eager_scopes() default {};

	@AttributeDefinition(name = "Eager stages", description = "Stages pre-fetched per scope.", required = false)
	String[] eager_stages() default { "released" };

	@AttributeDefinition(name = "Eager nsURI allow-list", description = "nsURIs pre-fetched in HYBRID mode.", required = false)
	String[] eager_nsuri_allow_list() default {};

	@AttributeDefinition(name = "Strict mode", description = "If true, EAGER activation fails when the server is unreachable.", required = false)
	boolean mode_strict() default false;

	@AttributeDefinition(name = "nsURI allow-list", description = "If non-empty, only these nsURIs are publishable.", required = false)
	String[] nsuri_allow_list() default {};

	@AttributeDefinition(name = "nsURI deny-list", description = "nsURIs never publishable, even if the server returns them.", required = false)
	String[] nsuri_deny_list() default {};

	@AttributeDefinition(name = "Force remote", description = "Prefer the remote EPackage over a same-nsURI local one.", required = false)
	boolean force_remote() default false;

	@AttributeDefinition(name = "Register in global registry", description = "Mirror published EPackages into EPackage.Registry.INSTANCE.", required = false)
	boolean register_in_global_registry() default false;

	@AttributeDefinition(name = "Drift check interval (ms)", description = "0 disables the drift watcher.", required = false)
	int drift_check_interval_ms() default 300_000;

	@AttributeDefinition(name = "Scope allow-list", description = "Scopes the client looks in; empty means all scopes.", required = false)
	String[] scope_allow_list() default {};

	@AttributeDefinition(name = "Default scope", description = "Scope used for anonymous EPackage look-ups.", required = false)
	String default_scope() default "";

	@AttributeDefinition(name = "Cache max entries", description = "LRU bound; <= 0 means unbounded.", required = false)
	int cache_max_entries() default 500;

	@AttributeDefinition(name = "Cache TTL (ms)", description = "0 disables TTL expiry.", required = false)
	int cache_ttl_ms() default 0;

	@AttributeDefinition(name = "Cache disk dir", description = "Disk-cache directory; empty means in-memory only.", required = false)
	String cache_disk_dir() default "";

	@AttributeDefinition(name = "Auth type", required = false)
	AuthType auth_type() default AuthType.NONE;

	@AttributeDefinition(name = "Auth token env var", description = "Environment variable holding the bearer token (auth.type=BEARER).", required = false)
	String auth_token_env() default "";

	@AttributeDefinition(name = "Keystore path", description = "Client keystore (auth.type=MTLS).", required = false)
	String auth_keystore_path() default "";

	@AttributeDefinition(name = "Keystore password", required = false, type = org.osgi.service.metatype.annotations.AttributeType.PASSWORD)
	String auth_keystore_password() default "";

	@AttributeDefinition(name = "Keystore type", required = false)
	String auth_keystore_type() default ClientConfiguration.DEFAULT_STORE_TYPE;

	@AttributeDefinition(name = "Truststore path", description = "Truststore (auth.type=MTLS).", required = false)
	String auth_truststore_path() default "";

	@AttributeDefinition(name = "Truststore password", required = false, type = org.osgi.service.metatype.annotations.AttributeType.PASSWORD)
	String auth_truststore_password() default "";

	@AttributeDefinition(name = "Truststore type", required = false)
	String auth_truststore_type() default ClientConfiguration.DEFAULT_STORE_TYPE;

	@AttributeDefinition(name = "Lazy resolve timeout (ms)", description = "OSGi-only: how long getEPackage blocks in LAZY mode for a fetched package to become visible in EPackage.Registry.", required = false)
	int lazy_resolve_timeout_ms() default 5_000;

	@AttributeDefinition(name = "ResourceSet fallback", description = "OSGi-only: wrap framework-produced ResourceSets with the Atlas-aware delegating registry.", required = false)
	boolean resource_set_fallback() default true;
}
