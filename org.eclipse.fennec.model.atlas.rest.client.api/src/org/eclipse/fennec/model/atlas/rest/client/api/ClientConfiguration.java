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
package org.eclipse.fennec.model.atlas.rest.client.api;

import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * Immutable configuration value type shared by the plain-Java and OSGi clients.
 * <p>
 * In plain Java it is assembled through {@link #builder()}; in OSGi (Phase 3)
 * the same fields are populated from ConfigAdmin (PID
 * {@code org.eclipse.fennec.model.atlas.rest.client}). Field names and defaults
 * mirror the Phase-2 configuration property table in the design document — the
 * dotted property name for each field is given in its accessor javadoc.
 * <p>
 * The two Phase-3-only properties ({@code lazy.resolve.timeout.ms},
 * {@code resource.set.fallback}) are modelled here too so there is a single
 * shared configuration value type, but they are consumed only by the OSGi
 * front-end and are ignored by the plain-Java client.
 */
public final class ClientConfiguration {

	/** Default key/trust store type when not configured. */
	public static final String DEFAULT_STORE_TYPE = "PKCS12";

	private final URI baseUri;
	private final int connectTimeoutMs;
	private final int readTimeoutMs;
	private final ResolutionMode mode;
	private final List<String> eagerScopes;
	private final List<String> eagerStages;
	private final List<String> eagerNsUriAllowList;
	private final boolean modeStrict;
	private final List<String> nsUriAllowList;
	private final List<String> nsUriDenyList;
	private final boolean forceRemote;
	private final boolean registerInGlobalRegistry;
	private final int driftCheckIntervalMs;
	private final List<String> scopeAllowList;
	private final String defaultScope;
	private final int cacheMaxEntries;
	private final int cacheTtlMs;
	private final String cacheDiskDir;
	private final AuthType authType;
	private final String authTokenEnv;
	private final String keystorePath;
	private final String keystorePassword;
	private final String keystoreType;
	private final String truststorePath;
	private final String truststorePassword;
	private final String truststoreType;
	private final int lazyResolveTimeoutMs;
	private final boolean resourceSetFallback;

	private ClientConfiguration(Builder b) {
		this.baseUri = b.baseUri;
		this.connectTimeoutMs = b.connectTimeoutMs;
		this.readTimeoutMs = b.readTimeoutMs;
		this.mode = b.mode;
		this.eagerScopes = b.eagerScopes;
		this.eagerStages = b.eagerStages;
		this.eagerNsUriAllowList = b.eagerNsUriAllowList;
		this.modeStrict = b.modeStrict;
		this.nsUriAllowList = b.nsUriAllowList;
		this.nsUriDenyList = b.nsUriDenyList;
		this.forceRemote = b.forceRemote;
		this.registerInGlobalRegistry = b.registerInGlobalRegistry;
		this.driftCheckIntervalMs = b.driftCheckIntervalMs;
		this.scopeAllowList = b.scopeAllowList;
		this.defaultScope = b.defaultScope;
		this.cacheMaxEntries = b.cacheMaxEntries;
		this.cacheTtlMs = b.cacheTtlMs;
		this.cacheDiskDir = b.cacheDiskDir;
		this.authType = b.authType;
		this.authTokenEnv = b.authTokenEnv;
		this.keystorePath = b.keystorePath;
		this.keystorePassword = b.keystorePassword;
		this.keystoreType = b.keystoreType;
		this.truststorePath = b.truststorePath;
		this.truststorePassword = b.truststorePassword;
		this.truststoreType = b.truststoreType;
		this.lazyResolveTimeoutMs = b.lazyResolveTimeoutMs;
		this.resourceSetFallback = b.resourceSetFallback;
	}

	/** A new builder with every property at its default; {@code base.uri} is required. */
	public static Builder builder() {
		return new Builder();
	}

	/** A builder pre-populated from an existing configuration. */
	public static Builder builder(ClientConfiguration from) {
		return new Builder(from);
	}

	/** {@code base.uri} — required server base URI. */
	public URI getBaseUri() {
		return baseUri;
	}

	/** {@code connect.timeout.ms} — default {@code 5000}. */
	public int getConnectTimeoutMs() {
		return connectTimeoutMs;
	}

	/** {@code read.timeout.ms} — default {@code 30000}. */
	public int getReadTimeoutMs() {
		return readTimeoutMs;
	}

	/** {@code mode} — default {@link ResolutionMode#LAZY}. */
	public ResolutionMode getMode() {
		return mode;
	}

	/** {@code eager.scopes} — EAGER/HYBRID scopes to pre-fetch (empty + EAGER = all configured). */
	public List<String> getEagerScopes() {
		return eagerScopes;
	}

	/** {@code eager.stages} — stages to pre-fetch per scope; default {@code ["released"]}. */
	public List<String> getEagerStages() {
		return eagerStages;
	}

	/** {@code eager.nsuri.allow.list} — HYBRID nsURIs to fetch eagerly. */
	public List<String> getEagerNsUriAllowList() {
		return eagerNsUriAllowList;
	}

	/** {@code mode.strict} — if {@code true}, EAGER fails activation when the server is unreachable. */
	public boolean isModeStrict() {
		return modeStrict;
	}

	/** {@code nsuri.allow.list} — if non-empty, only these nsURIs are publishable. */
	public List<String> getNsUriAllowList() {
		return nsUriAllowList;
	}

	/** {@code nsuri.deny.list} — nsURIs never publishable, even if returned by the server. */
	public List<String> getNsUriDenyList() {
		return nsUriDenyList;
	}

	/** {@code force.remote} — prefer remote EPackage over a same-nsURI local one. */
	public boolean isForceRemote() {
		return forceRemote;
	}

	/** {@code register.in.global.registry} — mirror published EPackages into {@code EPackage.Registry.INSTANCE}. */
	public boolean isRegisterInGlobalRegistry() {
		return registerInGlobalRegistry;
	}

	/** {@code drift.check.interval.ms} — default {@code 300000}; {@code 0} disables. */
	public int getDriftCheckIntervalMs() {
		return driftCheckIntervalMs;
	}

	/** {@code scope.allow.list} — empty = all scopes. */
	public List<String> getScopeAllowList() {
		return scopeAllowList;
	}

	/** {@code default.scope} — scope used for anonymous EPackage lookup; may be {@code null}. */
	public String getDefaultScope() {
		return defaultScope;
	}

	/** {@code cache.max.entries} — LRU bound; default {@code 500}. */
	public int getCacheMaxEntries() {
		return cacheMaxEntries;
	}

	/** {@code cache.ttl.ms} — default {@code 0} (no TTL). */
	public int getCacheTtlMs() {
		return cacheTtlMs;
	}

	/** {@code cache.disk.dir} — disk cache directory; {@code null}/empty = in-memory only. */
	public String getCacheDiskDir() {
		return cacheDiskDir;
	}

	/** {@code auth.type} — default {@link AuthType#NONE}. */
	public AuthType getAuthType() {
		return authType;
	}

	/** {@code auth.token.env} — env var holding the bearer token; may be {@code null}. */
	public String getAuthTokenEnv() {
		return authTokenEnv;
	}

	/** {@code auth.keystore.path} — client keystore (mTLS). Only used when {@code auth.type = mtls}. */
	public String getKeystorePath() {
		return keystorePath;
	}

	/** {@code auth.keystore.password} — keystore password (mTLS). Only used when {@code auth.type = mtls}. */
	public String getKeystorePassword() {
		return keystorePassword;
	}

	/** {@code auth.keystore.type} — keystore type; default {@link #DEFAULT_STORE_TYPE}. Only used when {@code auth.type = mtls}. */
	public String getKeystoreType() {
		return keystoreType;
	}

	/** {@code auth.truststore.path} — truststore (mTLS). Only used when {@code auth.type = mtls}. */
	public String getTruststorePath() {
		return truststorePath;
	}

	/** {@code auth.truststore.password} — truststore password (mTLS). Only used when {@code auth.type = mtls}. */
	public String getTruststorePassword() {
		return truststorePassword;
	}

	/** {@code auth.truststore.type} — truststore type; default {@link #DEFAULT_STORE_TYPE}. Only used when {@code auth.type = mtls}. */
	public String getTruststoreType() {
		return truststoreType;
	}

	/**
	 * {@code lazy.resolve.timeout.ms} — OSGi front-end only: how long the delegating
	 * registry blocks in LAZY mode for a fetched package to become observable in the
	 * framework {@code EPackage.Registry}; default {@code 5000}. Ignored by the
	 * plain-Java client.
	 */
	public int getLazyResolveTimeoutMs() {
		return lazyResolveTimeoutMs;
	}

	/**
	 * {@code resource.set.fallback} — OSGi front-end only: whether framework-produced
	 * {@code ResourceSet}s are wrapped with the Atlas-aware delegating registry;
	 * default {@code true}. Ignored by the plain-Java client.
	 */
	public boolean isResourceSetFallback() {
		return resourceSetFallback;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ClientConfiguration)) {
			return false;
		}
		ClientConfiguration that = (ClientConfiguration) o;
		return connectTimeoutMs == that.connectTimeoutMs
				&& readTimeoutMs == that.readTimeoutMs
				&& lazyResolveTimeoutMs == that.lazyResolveTimeoutMs
				&& resourceSetFallback == that.resourceSetFallback
				&& modeStrict == that.modeStrict
				&& forceRemote == that.forceRemote
				&& registerInGlobalRegistry == that.registerInGlobalRegistry
				&& driftCheckIntervalMs == that.driftCheckIntervalMs
				&& cacheMaxEntries == that.cacheMaxEntries
				&& cacheTtlMs == that.cacheTtlMs
				&& Objects.equals(baseUri, that.baseUri)
				&& mode == that.mode
				&& eagerScopes.equals(that.eagerScopes)
				&& eagerStages.equals(that.eagerStages)
				&& eagerNsUriAllowList.equals(that.eagerNsUriAllowList)
				&& nsUriAllowList.equals(that.nsUriAllowList)
				&& nsUriDenyList.equals(that.nsUriDenyList)
				&& scopeAllowList.equals(that.scopeAllowList)
				&& Objects.equals(defaultScope, that.defaultScope)
				&& Objects.equals(cacheDiskDir, that.cacheDiskDir)
				&& authType == that.authType
				&& Objects.equals(authTokenEnv, that.authTokenEnv)
				&& Objects.equals(keystorePath, that.keystorePath)
				&& Objects.equals(keystorePassword, that.keystorePassword)
				&& Objects.equals(keystoreType, that.keystoreType)
				&& Objects.equals(truststorePath, that.truststorePath)
				&& Objects.equals(truststorePassword, that.truststorePassword)
				&& Objects.equals(truststoreType, that.truststoreType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(baseUri, connectTimeoutMs, readTimeoutMs, mode, eagerScopes, eagerStages,
				eagerNsUriAllowList, modeStrict, nsUriAllowList, nsUriDenyList, forceRemote,
				registerInGlobalRegistry, driftCheckIntervalMs, scopeAllowList, defaultScope, cacheMaxEntries,
				cacheTtlMs, cacheDiskDir, authType, authTokenEnv, keystorePath, keystorePassword, keystoreType,
				truststorePath, truststorePassword, truststoreType, lazyResolveTimeoutMs, resourceSetFallback);
	}

	@Override
	public String toString() {
		return "ClientConfiguration[baseUri=" + baseUri + ", mode=" + mode + ", authType="
				+ authType + ", cacheMaxEntries=" + cacheMaxEntries + ", driftCheckIntervalMs=" + driftCheckIntervalMs
				+ "]";
	}

	/**
	 * Fluent builder for {@link ClientConfiguration}. Not thread-safe; build one
	 * per configuration. {@code base.uri} must be set before {@link #build()}.
	 */
	public static final class Builder {

		private URI baseUri;
		private int connectTimeoutMs = 5_000;
		private int readTimeoutMs = 30_000;
		private ResolutionMode mode = ResolutionMode.LAZY;
		private List<String> eagerScopes = List.of();
		private List<String> eagerStages = List.of("released");
		private List<String> eagerNsUriAllowList = List.of();
		private boolean modeStrict = false;
		private List<String> nsUriAllowList = List.of();
		private List<String> nsUriDenyList = List.of();
		private boolean forceRemote = false;
		private boolean registerInGlobalRegistry = false;
		private int driftCheckIntervalMs = 300_000;
		private List<String> scopeAllowList = List.of();
		private String defaultScope;
		private int cacheMaxEntries = 500;
		private int cacheTtlMs = 0;
		private String cacheDiskDir;
		private AuthType authType = AuthType.NONE;
		private String authTokenEnv;
		private String keystorePath;
		private String keystorePassword;
		private String keystoreType = DEFAULT_STORE_TYPE;
		private String truststorePath;
		private String truststorePassword;
		private String truststoreType = DEFAULT_STORE_TYPE;
		private int lazyResolveTimeoutMs = 5_000;
		private boolean resourceSetFallback = true;

		private Builder() {
			// use ClientConfiguration.builder()
		}

		private Builder(ClientConfiguration from) {
			this.baseUri = from.baseUri;
			this.connectTimeoutMs = from.connectTimeoutMs;
			this.readTimeoutMs = from.readTimeoutMs;
			this.mode = from.mode;
			this.eagerScopes = from.eagerScopes;
			this.eagerStages = from.eagerStages;
			this.eagerNsUriAllowList = from.eagerNsUriAllowList;
			this.modeStrict = from.modeStrict;
			this.nsUriAllowList = from.nsUriAllowList;
			this.nsUriDenyList = from.nsUriDenyList;
			this.forceRemote = from.forceRemote;
			this.registerInGlobalRegistry = from.registerInGlobalRegistry;
			this.driftCheckIntervalMs = from.driftCheckIntervalMs;
			this.scopeAllowList = from.scopeAllowList;
			this.defaultScope = from.defaultScope;
			this.cacheMaxEntries = from.cacheMaxEntries;
			this.cacheTtlMs = from.cacheTtlMs;
			this.cacheDiskDir = from.cacheDiskDir;
			this.authType = from.authType;
			this.authTokenEnv = from.authTokenEnv;
			this.keystorePath = from.keystorePath;
			this.keystorePassword = from.keystorePassword;
			this.keystoreType = from.keystoreType;
			this.truststorePath = from.truststorePath;
			this.truststorePassword = from.truststorePassword;
			this.truststoreType = from.truststoreType;
			this.lazyResolveTimeoutMs = from.lazyResolveTimeoutMs;
			this.resourceSetFallback = from.resourceSetFallback;
		}

		public Builder baseUri(URI baseUri) {
			this.baseUri = baseUri;
			return this;
		}

		public Builder connectTimeoutMs(int connectTimeoutMs) {
			this.connectTimeoutMs = connectTimeoutMs;
			return this;
		}

		public Builder readTimeoutMs(int readTimeoutMs) {
			this.readTimeoutMs = readTimeoutMs;
			return this;
		}

		public Builder mode(ResolutionMode mode) {
			this.mode = Objects.requireNonNull(mode, "mode");
			return this;
		}

		public Builder eagerScopes(List<String> eagerScopes) {
			this.eagerScopes = copyOf(eagerScopes);
			return this;
		}

		public Builder eagerStages(List<String> eagerStages) {
			this.eagerStages = copyOf(eagerStages);
			return this;
		}

		public Builder eagerNsUriAllowList(List<String> eagerNsUriAllowList) {
			this.eagerNsUriAllowList = copyOf(eagerNsUriAllowList);
			return this;
		}

		public Builder modeStrict(boolean modeStrict) {
			this.modeStrict = modeStrict;
			return this;
		}

		public Builder nsUriAllowList(List<String> nsUriAllowList) {
			this.nsUriAllowList = copyOf(nsUriAllowList);
			return this;
		}

		public Builder nsUriDenyList(List<String> nsUriDenyList) {
			this.nsUriDenyList = copyOf(nsUriDenyList);
			return this;
		}

		public Builder forceRemote(boolean forceRemote) {
			this.forceRemote = forceRemote;
			return this;
		}

		public Builder registerInGlobalRegistry(boolean registerInGlobalRegistry) {
			this.registerInGlobalRegistry = registerInGlobalRegistry;
			return this;
		}

		public Builder driftCheckIntervalMs(int driftCheckIntervalMs) {
			this.driftCheckIntervalMs = driftCheckIntervalMs;
			return this;
		}

		public Builder scopeAllowList(List<String> scopeAllowList) {
			this.scopeAllowList = copyOf(scopeAllowList);
			return this;
		}

		public Builder defaultScope(String defaultScope) {
			this.defaultScope = defaultScope;
			return this;
		}

		public Builder cacheMaxEntries(int cacheMaxEntries) {
			this.cacheMaxEntries = cacheMaxEntries;
			return this;
		}

		public Builder cacheTtlMs(int cacheTtlMs) {
			this.cacheTtlMs = cacheTtlMs;
			return this;
		}

		public Builder cacheDiskDir(String cacheDiskDir) {
			this.cacheDiskDir = cacheDiskDir;
			return this;
		}

		public Builder authType(AuthType authType) {
			this.authType = Objects.requireNonNull(authType, "authType");
			return this;
		}

		public Builder authTokenEnv(String authTokenEnv) {
			this.authTokenEnv = authTokenEnv;
			return this;
		}

		public Builder keystorePath(String keystorePath) {
			this.keystorePath = keystorePath;
			return this;
		}

		public Builder keystorePassword(String keystorePassword) {
			this.keystorePassword = keystorePassword;
			return this;
		}

		public Builder keystoreType(String keystoreType) {
			this.keystoreType = Objects.requireNonNull(keystoreType, "keystoreType");
			return this;
		}

		public Builder truststorePath(String truststorePath) {
			this.truststorePath = truststorePath;
			return this;
		}

		public Builder truststorePassword(String truststorePassword) {
			this.truststorePassword = truststorePassword;
			return this;
		}

		public Builder truststoreType(String truststoreType) {
			this.truststoreType = Objects.requireNonNull(truststoreType, "truststoreType");
			return this;
		}

		public Builder lazyResolveTimeoutMs(int lazyResolveTimeoutMs) {
			this.lazyResolveTimeoutMs = lazyResolveTimeoutMs;
			return this;
		}

		public Builder resourceSetFallback(boolean resourceSetFallback) {
			this.resourceSetFallback = resourceSetFallback;
			return this;
		}

		/**
		 * @return the built, immutable configuration
		 * @throws IllegalStateException if {@code base.uri} was never set
		 */
		public ClientConfiguration build() {
			if (baseUri == null) {
				throw new IllegalStateException("base.uri is required");
			}
			return new ClientConfiguration(this);
		}

		private static List<String> copyOf(List<String> values) {
			return values == null ? List.of() : List.copyOf(values);
		}
	}
}
