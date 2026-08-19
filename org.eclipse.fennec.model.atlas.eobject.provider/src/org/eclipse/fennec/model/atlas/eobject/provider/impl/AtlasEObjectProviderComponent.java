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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryWriter;
import org.eclipse.fennec.model.atlas.eobject.provider.AtlasObjectSync;
import org.eclipse.fennec.model.atlas.eobject.provider.AtlasSyncSettings;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

/**
 * One atlas provider per factory configuration: wraps an {@link AtlasObjectSync} feeding
 * the configured registry's {@link EObjectRegistryWriter}. Publishes no service of its
 * own - it is a writer client, deliberately not an initial provider (registry
 * publication must never depend on the network).
 *
 * @since 08/2026
 */
@Component(name = AtlasEObjectProviderComponent.NAME, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = AtlasEObjectProviderConfig.class, factory = true)
public class AtlasEObjectProviderComponent {

	public static final String NAME = "AtlasEObjectProvider";

	private final AtlasObjectSync sync;

	@Activate
	public AtlasEObjectProviderComponent(
			@Reference(name = "atlasScope") ReadableScopeService<EObject> scopeService,
			@Reference(name = "writer") EObjectRegistryWriter writer, AtlasEObjectProviderConfig config) {
		AtlasSyncSettings settings = new AtlasSyncSettings(config.emf_eobject_provider_name(),
				asList(config.registries()), asList(config.object_ids()), config.stage(),
				Set.copyOf(asList(config.required_nsuris())), config.refresh_interval_ms(),
				config.retry_interval_ms(), null);
		BiFunction<String, EObject, String> keyFunction = config.key_feature() == null
				|| config.key_feature().isBlank() ? AtlasObjectSync.objectIdKeys()
						: AtlasObjectSync.featureKeys(config.key_feature());
		sync = new AtlasObjectSync(scopeService, settings, keyFunction, writer);
	}

	@Deactivate
	public void deactivate() {
		sync.close();
	}

	private static List<String> asList(String[] values) {
		return values == null ? List.of() : Arrays.asList(values);
	}
}
