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
package org.eclipse.fennec.model.atlas.healthcheck.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.apache.felix.hc.api.HealthCheck;
import org.apache.felix.hc.api.Result;
import org.apache.felix.hc.api.ResultLog;
import org.eclipse.fennec.model.atlas.scope.api.RegistryInfo;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Registry;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Integration tests for the "Scopes And Registries" health check.
 *
 * <p>
 * Scopes come and go with configuration: a {@code ScopeService} can be
 * published long after the health check component activated. These tests drive
 * that ordering — activate the health check first (by resolving its service),
 * then publish a ScopeService — because that is the ordering a readiness probe
 * has to survive. A {@code MULTIPLE} reference left on the DS defaults (STATIC
 * and RELUCTANT) never binds such a late service, so the check would keep
 * answering from the set it saw at activation time.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class ScopesHealthCheckIT {

	private static final String HEALTH_CHECK_FILTER = "(hc.name=Scopes And Registries)";
	private static final String LATE_SCOPE = "lateBoundScope";
	private static final String LATE_REGISTRY = "lateBoundRegistry";

	private ServiceRegistration<?> registration;

	@AfterEach
	public void unregisterLateScope() {
		if (registration != null) {
			registration.unregister();
			registration = null;
		}
	}

	@Test
	public void testScopeServicePublishedAfterActivationIsReported(
			@InjectService(filter = HEALTH_CHECK_FILTER, timeout = 5000) HealthCheck healthCheck,
			@InjectBundleContext BundleContext context) {

		// Resolving the service above already activated the component, so the
		// ScopeService below is by definition a late arrival.
		assertFalse(reports(healthCheck.execute(), LATE_SCOPE),
				"Scope must not be reported before its ScopeService exists");

		registration = context.registerService(ScopeService.class.getName(), lateScopeService(), scopeProperties());

		assertTrue(reports(healthCheck.execute(), LATE_SCOPE),
				"A ScopeService published after the health check activated must be reported; it is not bound at all "
						+ "when the MULTIPLE reference keeps the DS default STATIC/RELUCTANT policy");
	}

	@Test
	public void testScopeServiceWithdrawalIsReflected(
			@InjectService(filter = HEALTH_CHECK_FILTER, timeout = 5000) HealthCheck healthCheck,
			@InjectBundleContext BundleContext context) {

		registration = context.registerService(ScopeService.class.getName(), lateScopeService(), scopeProperties());
		assertTrue(reports(healthCheck.execute(), LATE_SCOPE), "Registered scope must be reported");

		registration.unregister();
		registration = null;

		assertFalse(reports(healthCheck.execute(), LATE_SCOPE),
				"A withdrawn ScopeService must stop being reported, otherwise readiness advertises a gone scope");
	}

	/**
	 * A ScopeService whose scope carries one registry, so the health check logs
	 * the scope and registry names and the result can be searched for them.
	 */
	private static ScopeService<?> lateScopeService() {
		Registry registry = mock(Registry.class);
		when(registry.getName()).thenReturn(LATE_REGISTRY);
		when(registry.getDescription()).thenReturn("registry published late in the test");
		when(registry.getStages()).thenReturn(List.of());

		Scope scope = mock(Scope.class);
		when(scope.getName()).thenReturn(LATE_SCOPE);
		when(scope.getRegistries()).thenReturn(List.<RegistryInfo>of(registry));

		ScopeService<?> scopeService = mock(ScopeService.class);
		when(scopeService.getScope()).thenReturn(scope);
		return scopeService;
	}

	/** The properties a real ScopeService carries, so collectors accept it too. */
	private static Dictionary<String, Object> scopeProperties() {
		Hashtable<String, Object> properties = new Hashtable<>();
		properties.put("scope.name", LATE_SCOPE);
		return properties;
	}

	private static boolean reports(Result result, String name) {
		for (ResultLog.Entry entry : result) {
			if (entry.getMessage() != null && entry.getMessage().contains(name)) {
				return true;
			}
		}
		return false;
	}
}
