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

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.apache.felix.hc.api.HealthCheck;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Integration tests for the tags the Atlas health checks advertise.
 *
 * <p>
 * The Felix health check servlet turns every requested tag into its own
 * service filter — {@code (hc.tags=readiness)} — and ORs the results
 * (see {@code HealthCheckFilter#getServiceFilter}). A check therefore only
 * answers {@code /atlas/system/health?tags=readiness} when {@code hc.tags} is
 * a <em>multi-valued</em> property holding {@code readiness} as one of its
 * values: OSGi filter equality matches an element of a multi-valued property,
 * but has to match a single-valued String as a whole.
 *
 * <p>
 * These tests query with exactly the filter the servlet builds, so a check
 * declaring {@code hc.tags} as the one string {@code "atlas,readiness"} fails
 * them — that shape answers only the literal {@code ?tags=atlas,readiness} and
 * silently drops out of the Kubernetes probes documented in the README.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class HealthCheckTagsIT {

	private static final String EMF_REGISTRY = "EMF Registry";
	private static final String LIVENESS = "Liveness";
	private static final String SCOPES = "Scopes And Registries";

	/**
	 * MediaTypesHealthCheck is deliberately absent: its mandatory
	 * SupportedMediatype reference is unsatisfied in this runtime, so DS never
	 * registers it. The three checks below need no service provider.
	 */
	@Test
	public void testReadinessTagSelectsTheReadinessChecks(
			@InjectService(filter = "(hc.name=" + EMF_REGISTRY + ")", timeout = 5000) HealthCheck emfRegistry,
			@InjectService(filter = "(hc.name=" + SCOPES + ")", timeout = 5000) HealthCheck scopes,
			@InjectBundleContext BundleContext context) throws InvalidSyntaxException {

		assertEquals(Set.of(EMF_REGISTRY, SCOPES), checksTagged(context, "readiness"),
				"A readiness probe on ?tags=readiness must select the readiness checks");
	}

	@Test
	public void testLivenessTagSelectsTheLivenessCheck(
			@InjectService(filter = "(hc.name=" + LIVENESS + ")", timeout = 5000) HealthCheck liveness,
			@InjectBundleContext BundleContext context) throws InvalidSyntaxException {

		assertEquals(Set.of(LIVENESS), checksTagged(context, "liveness"),
				"A liveness probe on ?tags=liveness must select the liveness check");
	}

	@Test
	public void testAtlasTagSelectsEveryAtlasCheck(
			@InjectService(filter = "(hc.name=" + EMF_REGISTRY + ")", timeout = 5000) HealthCheck emfRegistry,
			@InjectService(filter = "(hc.name=" + LIVENESS + ")", timeout = 5000) HealthCheck liveness,
			@InjectService(filter = "(hc.name=" + SCOPES + ")", timeout = 5000) HealthCheck scopes,
			@InjectBundleContext BundleContext context) throws InvalidSyntaxException {

		assertEquals(Set.of(EMF_REGISTRY, LIVENESS, SCOPES), checksTagged(context, "atlas"),
				"Every Atlas check carries the atlas tag, the one the health servlet is configured with");
	}

	/** The names of the checks the servlet would run for a single requested tag. */
	private static Set<String> checksTagged(BundleContext context, String tag) throws InvalidSyntaxException {
		Collection<ServiceReference<HealthCheck>> references = context.getServiceReferences(HealthCheck.class,
				"(" + HealthCheck.TAGS + "=" + tag + ")");
		Set<String> names = new TreeSet<>();
		for (ServiceReference<HealthCheck> reference : references) {
			names.add(String.valueOf(reference.getProperty(HealthCheck.NAME)));
		}
		return names;
	}
}
