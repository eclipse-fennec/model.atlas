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
package org.eclipse.fennec.model.atlas.eobject.provider.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Configuration of an {@link AtlasEObjectProviderComponent} instance. Two standard DS
 * reference-target properties complete the wiring: {@code atlasScope.target} selects the
 * atlas scope (e.g. {@code (atlas.scope=jena)}), {@code writer.target} selects the
 * target registry by name (e.g. {@code (emf.eobject.registry.name=sensinact-mappings)}).
 * Because the registry's writer service only exists after its gated initial load, the
 * component is not satisfied - and never writes - before the registry is up.
 *
 * @since 08/2026
 */
@ObjectClassDefinition(name = "Model Atlas - EObject Registry Provider", //
		description = "Feeds a named emf.osgi EObject registry from a Model Atlas scope. The scope is "
				+ "selected via the atlasScope.target property (e.g. (atlas.scope=jena)), the target "
				+ "registry via the writer.target property (e.g. (emf.eobject.registry.name=my-registry)).")
public @interface AtlasEObjectProviderConfig {

	@AttributeDefinition(name = "Provider name", description = "This provider's source name; registry entries are "
			+ "written under the source tag <provider name>:<atlas registry>.")
	String emf_eobject_provider_name();

	@AttributeDefinition(name = "Registries", description = "Atlas registry names to sync objects from.")
	String[] registries();

	@AttributeDefinition(name = "Object ids", required = false, description = "Explicit object ids to load; empty loads every object the registries list.")
	String[] object_ids() default {};

	@AttributeDefinition(name = "Stage", required = false, description = "Atlas stage to read from; empty reads the final stage.")
	String stage() default "";

	@AttributeDefinition(name = "Key feature", required = false, description = "Attribute of the fetched objects whose "
			+ "value becomes the entry key (e.g. a domain id like 'mid'); empty keys entries by their atlas object id.")
	String key_feature() default "";

	@AttributeDefinition(name = "Required nsURIs", required = false, description = "nsURIs whose generated EPackages "
			+ "must be resolvable before a sync pass runs; guards against fetched objects materializing as dynamic "
			+ "EObjects while a model bundle is not active yet.")
	String[] required_nsuris() default {};

	@AttributeDefinition(name = "Refresh interval (ms)", required = false, description = "Interval for re-syncing from the atlas; 0 syncs once on activation.")
	long refresh_interval_ms() default 0;

	@AttributeDefinition(name = "Retry interval (ms)", required = false, description = "Back-off before retrying while the initial sync is incomplete; 0 disables retries.")
	long retry_interval_ms() default 30_000;
}
