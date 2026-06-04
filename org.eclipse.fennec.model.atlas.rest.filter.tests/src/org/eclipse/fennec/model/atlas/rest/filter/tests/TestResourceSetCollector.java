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
import org.eclipse.fennec.model.atlas.workflow.ResourceSetCollector;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Component;

/**
 * High-priority test override of {@link ResourceSetCollector}. Tests register
 * scope/stage-specific {@link CountingCso} entries via
 * {@link #register(String, String, CountingCso)}. The
 * {@code ScopedResourceSetFeature}'s DYNAMIC/GREEDY reference will pick this
 * instance because of the {@code service.ranking = Integer.MAX_VALUE}.
 *
 * <p>This subclass intentionally does not re-declare the parent's
 * {@code @Reference} bindings: DS only processes annotations on the declared
 * component class, so the inherited tracking maps stay empty and only entries
 * registered through {@link #register(String, String, CountingCso)} are
 * visible.
 */
@Component(
		service = { ResourceSetCollector.class, TestResourceSetCollector.class },
		property = "service.ranking:Integer=2147483647")
public class TestResourceSetCollector extends ResourceSetCollector {

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

	private static String key(String scope, String stage) {
		return scope + "::" + stage;
	}
}
