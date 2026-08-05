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

import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.workflow.ScopeServiceCollector;

/**
 * High-priority test implementation of {@link ScopeServiceCollector}. Hands back a
 * stub {@link ScopeService} for any scope registered via
 * {@link #register(String)} so that the side-by-side
 * {@code ModelAtlasRequestFilter} (which validates scope names ahead of the
 * binder) lets requests through to the test resource.
 *
 * <p>No {@code @Component} annotation — registered manually by the test
 * harness, see the rationale on {@link TestResourceSetCollector}.
 */
public class TestScopeServiceCollector implements ScopeServiceCollector {

	@SuppressWarnings("rawtypes")
	private final Map<String, ScopeService> scopes = new ConcurrentHashMap<>();

	public void register(String scopeName) {
		ScopeService<? extends EObject> mock = mock(ScopeService.class);
		scopes.put(scopeName, mock);
	}

	public void clear() {
		scopes.clear();
	}

	@Override
	public ScopeService<?> getScopeServiceByScopeName(String scopeName) {
		return scopes.get(scopeName);
	}

	/**
	 * The stubbed scope services carry no {@link Scope} model object; the filter
	 * path under test only resolves the service itself.
	 */
	@Override
	public Scope getScopeByName(String scopeName) {
		return null;
	}

	@Override
	public List<Scope> getAllScopes() {
		return List.of();
	}
}
