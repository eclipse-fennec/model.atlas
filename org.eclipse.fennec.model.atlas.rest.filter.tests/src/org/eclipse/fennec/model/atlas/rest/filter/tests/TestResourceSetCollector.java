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
package org.eclipse.fennec.model.atlas.rest.filter.tests;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.model.atlas.workflow.ResourceSetCollector;
import org.osgi.service.component.ComponentServiceObjects;

/**
 * High-priority test implementation of {@link ResourceSetCollector}. The test
 * harness instantiates it directly and registers it as an OSGi service via
 * {@code BundleContext.registerService} with {@code service.ranking = MAX_VALUE}
 * so the production {@code ScopedResourceSetProvider}'s DYNAMIC/GREEDY
 * reference picks it.
 *
 * <p>This class deliberately carries no {@code @Component} annotation. If it
 * did, bnd would emit an {@code osgi.service} Provide-Capability entry for
 * the {@link ResourceSetCollector} object class in this bundle's manifest,
 * which would make the workspace-aware resolver consider the test bundle as
 * a candidate provider for any production bundle that requires the
 * production {@code ResourceSetCollector} — exactly the pull-in problem the
 * runblacklist on this bundle used to work around. With manual registration,
 * the manifest stays clean.
 *
 * <p>Only entries registered through
 * {@link #register(String, String, ComponentServiceObjects)} are visible; there
 * is no service tracking behind this double.
 */
public class TestResourceSetCollector implements ResourceSetCollector {

	private final Map<String, ComponentServiceObjects<ResourceSet>> entries = new ConcurrentHashMap<>();

	public void register(String scopeName, String stageName, ComponentServiceObjects<ResourceSet> cso) {
		entries.put(key(scopeName, stageName), cso);
	}

	public void clear() {
		entries.clear();
	}

	@Override
	public ComponentServiceObjects<ResourceSet> getResourceSetObjects(String scopeName, String stageName) {
		if (scopeName == null || stageName == null) {
			return null;
		}
		return entries.get(key(scopeName, stageName));
	}

	/**
	 * The per-request injection path under test resolves only
	 * {@link ComponentServiceObjects}, so no factory is ever handed out.
	 */
	@Override
	public ResourceSetFactory getResourceSetFactory(String scopeName, String stageName) {
		return null;
	}

	private static String key(String scope, String stage) {
		return scope + "::" + stage;
	}
}
