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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.runtime.info;

import org.osgi.annotation.versioning.ProviderType;
import org.osgi.framework.Constants;

/**
 * What this Atlas holds, for anything that wants to watch it.
 *
 * <p>
 * The shape OSGi uses for this: {@code JakartarsServiceRuntime} and
 * {@code ServiceComponentRuntime} both answer with DTOs, and nothing that asks needs to know how
 * the thing inside works. Telemetry, a health check and an integration test all want the same
 * answer, and none of them should have to reach into the workflow layer to get it.
 * </p>
 *
 * <p>
 * <strong>It reports what is missing, too.</strong> Scopes and registries are configuration-driven,
 * and Declarative Services fails in silence: a target filter that matches nothing leaves a
 * component unregistered and logs nothing a client can see. A service collector can only show what
 * registered, so it can never name the scope that did not. The failure lists here can — which is
 * half of why this service exists.
 * </p>
 *
 * <p>
 * <strong>How to be told about change.</strong> The service carries
 * {@link Constants#SERVICE_CHANGECOUNT}, and its value changes whenever the snapshot would differ.
 * A consumer binds it dynamically and gets a callback:
 * </p>
 *
 * <pre>
 * &#64;Reference(name = "runtime", policy = DYNAMIC, cardinality = OPTIONAL)
 * void setRuntime(AtlasRuntime runtime, Map&lt;String, Object&gt; properties) { … }
 * void updatedRuntime(AtlasRuntime runtime, Map&lt;String, Object&gt; properties) { … }
 * void unsetRuntime(AtlasRuntime runtime) { … }
 * </pre>
 *
 * <p>
 * <strong>Two details DS will not warn you about.</strong> The callback is {@code updated<Name>},
 * not {@code modified<Name>}: DS calls the first when a <em>bound service's</em> properties change
 * and the second when the <em>component's own configuration</em> does, so a watcher that writes
 * {@code modified} compiles, binds, and is never told anything. And the properties have to be a
 * method parameter — a field-injected reference never receives them.
 * </p>
 *
 * @since 1.0
 */
@ProviderType
public interface AtlasRuntime {

    /**
     * The configuration property naming the base URL(s) this Atlas serves on, reported as
     * {@link InstanceDTO#endpoints}.
     *
     * <p>
     * A configuration property rather than a service property, which is what the Jakarta RS
     * whiteboard uses for the same thing: the change count has to be published through
     * {@code ServiceRegistration.setProperties}, and that call replaces the whole dictionary, so a
     * second property set beside it would be dropped on the first update. The endpoints travel in
     * the snapshot instead, where nothing can wipe them.
     * </p>
     */
    String ATLAS_ENDPOINT = "atlas.endpoint";

    /**
     * What this Atlas holds now.
     *
     * <p>
     * Built fresh per call, in one pass. A snapshot handed out and kept would be a snapshot someone
     * else could change, and a reader that got an older one would have no way to tell.
     * </p>
     *
     * @return the current snapshot; never {@code null}, and empty-but-valid when nothing is
     *         configured
     */
    AtlasRuntimeDTO snapshot();
}
