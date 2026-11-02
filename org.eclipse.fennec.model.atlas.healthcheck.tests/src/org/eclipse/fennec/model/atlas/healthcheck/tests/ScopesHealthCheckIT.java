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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.apache.felix.hc.api.HealthCheck;
import org.apache.felix.hc.api.Result;
import org.apache.felix.hc.api.ResultLog;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.eclipse.fennec.model.atlas.scope.api.RegistryInfo;
import org.eclipse.fennec.model.atlas.scope.api.RegistryType;
import org.eclipse.fennec.model.atlas.scope.api.ScopeApiFactory;
import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;
import org.eclipse.fennec.model.atlas.scope.api.StageInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Integration tests for the "Scopes And Registries" health check, which reads the Atlas runtime
 * snapshot.
 *
 * <p>
 * Scopes come and go with configuration, so a scope published after the check activated has to be
 * reported, a withdrawn one has to disappear, and — the case the snapshot was built for — a scope
 * that is configured but never came up has to fail readiness.
 * </p>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class ScopesHealthCheckIT {

	private static final String HEALTH_CHECK_FILTER = "(hc.name=Scopes And Registries)";
	private static final String LATE_SCOPE = "lateBoundScope";
	private static final String LATE_REGISTRY = "lateBoundRegistry";
	private static final String NO_SCOPES = "No scopes are live";

	private ServiceRegistration<?> registration;

	@AfterEach
	public void unregisterLateScope() {
		if (registration != null) {
			registration.unregister();
			registration = null;
		}
	}

	@Test
	public void testScopePublishedAfterActivationIsReported(
			@InjectService(filter = HEALTH_CHECK_FILTER, timeout = 5000) HealthCheck healthCheck,
			@InjectBundleContext BundleContext context) {

		assertFalse(reports(healthCheck.execute(), LATE_SCOPE),
				"Scope must not be reported before its service exists");

		registration = register(context, scopeInfo(LATE_SCOPE, true));

		Result result = healthCheck.execute();
		assertTrue(reports(result, LATE_SCOPE), "A scope published after the health check activated must be reported");
		assertTrue(reports(result, LATE_REGISTRY), "and so must its registry");
		assertEquals(Result.Status.OK, statusOf(result, LATE_SCOPE),
				"a live, consistent scope is healthy: " + messages(result));
		assertFalse(reports(result, NO_SCOPES), "a live scope exists: " + messages(result));
	}

	@Test
	public void testScopeWithdrawalIsReflected(
			@InjectService(filter = HEALTH_CHECK_FILTER, timeout = 5000) HealthCheck healthCheck,
			@InjectBundleContext BundleContext context) {

		registration = register(context, scopeInfo(LATE_SCOPE, true));
		assertTrue(reports(healthCheck.execute(), LATE_SCOPE), "Registered scope must be reported");

		registration.unregister();
		registration = null;

		Result result = healthCheck.execute();
		assertFalse(reports(result, LATE_SCOPE),
				"A withdrawn scope must stop being reported, otherwise readiness advertises a gone scope");
		assertEquals(Result.Status.CRITICAL, statusOf(result, NO_SCOPES), "no live scope at all is not ready");
	}

	@Test
	public void testLiveScopeWithoutRegistriesIsAWarning(
			@InjectService(filter = HEALTH_CHECK_FILTER, timeout = 5000) HealthCheck healthCheck,
			@InjectBundleContext BundleContext context) {

		registration = register(context, scopeInfo(LATE_SCOPE, false));

		Result result = healthCheck.execute();
		assertEquals(Result.Status.WARN, statusOf(result, LATE_SCOPE), messages(result));
		assertTrue(reports(result, "NO_REGISTRIES_BOUND"), messages(result));
	}

	@Test
	public void testConfiguredScopeThatNeverCameUpIsCritical(
			@InjectService(filter = HEALTH_CHECK_FILTER, timeout = 5000) HealthCheck healthCheck,
			@InjectService(timeout = 5000) ServiceComponentRuntime scr,
			@InjectBundleContext BundleContext context) throws Exception {

		String broken = UnsatisfiedScopeComponent.SCOPE;
		assertFalse(reports(healthCheck.execute(), broken), "not reported before the broken scope exists");

		ComponentDescriptionDTO description = scr.getComponentDescriptionDTO(context.getBundle(),
				UnsatisfiedScopeComponent.NAME);
		assertNotNull(description, "the test component must be declared");
		scr.enableComponent(description).getValue();
		try {
			Result result = await(healthCheck, r -> statusOf(r, broken) != null);
			assertEquals(Result.Status.CRITICAL, statusOf(result, broken),
					"a configured scope that never came up fails readiness: " + messages(result));
			assertTrue(reports(result, "NO_REGISTRIES_BOUND"), messages(result));
			assertEquals(Result.Status.CRITICAL, result.getStatus(), messages(result));
		} finally {
			scr.disableComponent(description).getValue();
		}
		Result after = await(healthCheck, r -> statusOf(r, broken) == null);
		assertFalse(reports(after, broken), "gone once the component is: " + messages(after));
	}

	/** Executes the check until the condition holds, or five seconds have passed. */
	private static Result await(HealthCheck healthCheck, Predicate<Result> condition) throws InterruptedException {
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
		Result result = healthCheck.execute();
		while (!condition.test(result) && System.nanoTime() < deadline) {
			Thread.sleep(50);
			result = healthCheck.execute();
		}
		return result;
	}

	/**
	 * The worst status among the entries mentioning the text, or {@code null} when none does.
	 *
	 * <p>
	 * The overall status is not asserted on: this runtime carries the workflow bundle without any
	 * storage, so its built-in atlas scope is itself configured and not live, and is reported as such.
	 * </p>
	 */
	private static Result.Status statusOf(Result result, String text) {
		Result.Status worst = null;
		for (ResultLog.Entry entry : result) {
			if (entry.getMessage() != null && entry.getMessage().contains(text)
					&& (worst == null || entry.getStatus().ordinal() > worst.ordinal())) {
				worst = entry.getStatus();
			}
		}
		return worst;
	}

	private static ServiceRegistration<?> register(BundleContext context, ScopeInfo info) {
		ReadableScopeService<?> scopeService = mock(ReadableScopeService.class);
		when(scopeService.getScopeName()).thenReturn(info.getName());
		when(scopeService.getScopeInfo()).thenReturn(info);
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("scope.name", info.getName());
		return context.registerService(ReadableScopeService.class.getName(), scopeService, properties);
	}

	/** A scope, with one schema registry and one final stage when asked for. */
	private static ScopeInfo scopeInfo(String name, boolean withRegistry) {
		ScopeInfo scope = ScopeApiFactory.eINSTANCE.createScopeInfo();
		scope.setName(name);
		if (withRegistry) {
			StageInfo stage = ScopeApiFactory.eINSTANCE.createStageInfo();
			stage.setName("release");
			stage.setFinal(true);
			RegistryInfo registry = ScopeApiFactory.eINSTANCE.createRegistryInfo();
			registry.setName(LATE_REGISTRY);
			registry.setDescription("registry published late in the test");
			registry.setType(RegistryType.SCHEMA);
			registry.getStages().add(stage);
			scope.getRegistries().add(registry);
		}
		return scope;
	}

	private static boolean reports(Result result, String text) {
		for (ResultLog.Entry entry : result) {
			if (entry.getMessage() != null && entry.getMessage().contains(text)) {
				return true;
			}
		}
		return false;
	}

	private static String messages(Result result) {
		StringBuilder messages = new StringBuilder();
		for (ResultLog.Entry entry : result) {
			messages.append('[').append(entry.getStatus()).append("] ").append(entry.getMessage()).append("; ");
		}
		return messages.toString();
	}
}
