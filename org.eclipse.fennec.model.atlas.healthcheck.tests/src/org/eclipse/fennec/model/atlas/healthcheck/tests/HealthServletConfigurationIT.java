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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.junit5.context.BundleContextExtension;

import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Checks the shipped configuration of the Felix health check servlet and of the
 * {@code ServicesCheck} instances that answer on its endpoint (issue #312).
 *
 * <p>
 * Two things can only be got wrong in the configuration files, never in Java:
 *
 * <ul>
 * <li><strong>A property name the servlet does not have is dropped in silence.</strong>
 * {@code HealthCheckExecutorServletConfiguration} is an ordinary OCD: Configuration Admin
 * stores whatever it is given and the component reads the ids it declares, so
 * {@code hc.tags} - the name the health <em>checks</em> use - sat in the configuration for
 * a long time while the servlet ran with no tag filter at all, and the documented
 * "{@code /atlas/system/health} returns the checks tagged atlas" was not true.</li>
 * <li><strong>A check without the {@code atlas} tag vanishes from the default endpoint.</strong>
 * {@code ServicesCheck} carries the Felix default {@code systemalive} unless the
 * configuration says otherwise. Now that the servlet really filters by {@code atlas}, a
 * scope check configured without that tag is not run at all - and the endpoint reports
 * OK while the scope it was meant to guard is missing. That is strictly worse than the
 * unfiltered state it replaced, so it is worth a test.</li>
 * </ul>
 *
 * <p>
 * The files are read out of this bundle, where {@code -includeresource} puts the real ones
 * from the config projects at build time - no copy is kept in git, so they cannot drift.
 * A file that fails to arrive is a failure of its own: a check that finds nothing must not
 * pass by default.
 */
@ExtendWith(BundleContextExtension.class)
public class HealthServletConfigurationIT {

	private static final String SERVLET_PID = "org.apache.felix.hc.core.impl.servlet.HealthCheckExecutorServlet";
	private static final String SERVICES_CHECK_PID = "org.apache.felix.hc.generalchecks.ServicesCheck";

	/** The {@code hc.name} every {@code ServicesCheck} carries unless configured otherwise. */
	private static final String DEFAULT_SERVICES_CHECK_NAME = "Services Ready Check";

	/** The tag the health servlet filters its default endpoint by. */
	private static final String ATLAS_TAG = "atlas";

	/**
	 * The attribute ids of {@code HealthCheckExecutorServletConfiguration} in
	 * healthcheck.core 2.3.0, read off its metatype. Anything else in the servlet's
	 * configuration is ignored at runtime. Revisit this set when the bundle is upgraded -
	 * a property that has been dropped upstream fails here rather than in production.
	 */
	private static final Set<String> SERVLET_PROPERTIES = Set.of( //
			"servletContextName", "servletPath", "httpStatusMapping", "timeout", "tags", "combineTagsWithOr", //
			"format", "allowed.formats", "disabled", "cors.accessControlAllowOrigin", //
			"disable.request.configuration", "webconsole.configurationFactory.nameHint");

	/**
	 * Every configuration file {@code -includeresource} is expected to deliver. Listed by
	 * name so a renamed or removed source file fails loudly instead of shrinking the set
	 * this test looks at.
	 */
	private static final Set<String> EXPECTED_FILES = Set.of( //
			"runtime.config.json", "local.json", "local.jena.json", //
			"docker.git.json", "docker.file.json", "jena.image.json");

	/** The configurator accepts {@code //} comments, so the parser has to as well. */
	private static final JsonMapper MAPPER = JsonMapper.builder() //
			.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS) //
			.build();

	@Test
	public void testEveryServicesCheckCarriesTheAtlasTag(@InjectBundleContext BundleContext context) throws Exception {

		Map<String, JsonNode> configs = readConfigs(context);
		List<String> checked = new ArrayList<>();

		for (Map.Entry<String, JsonNode> file : configs.entrySet()) {
			for (Map.Entry<String, JsonNode> pid : pids(file.getValue(), SERVICES_CHECK_PID)) {
				String where = file.getKey() + " -> " + pid.getKey();
				checked.add(where);

				JsonNode tags = pid.getValue().get("hc.tags");
				assertTrue(tags != null && tags.isArray(),
						where + " declares no hc.tags array. Without it the check carries only the Felix default"
								+ " 'systemalive' and is not run by /atlas/system/health, which filters by '"
								+ ATLAS_TAG + "'.");
				assertTrue(values(tags).contains(ATLAS_TAG),
						where + " declares hc.tags " + values(tags) + ", which does not contain '" + ATLAS_TAG
								+ "'. The check would not be run by /atlas/system/health.");
			}
		}

		assertEquals(configs.size(), checked.size(),
				"Every shipped configuration defines exactly one ServicesCheck; found " + checked);
	}

	/**
	 * Every {@code ServicesCheck} must be named, and two that can run side by side must not
	 * share a name.
	 *
	 * <p>
	 * {@code hc.name} defaults to "Services Ready Check" for all of them. Two checks that
	 * share a name are indistinguishable in the endpoint output - you cannot tell which
	 * scope a failing row belongs to - and the health executor collapses them into one when
	 * the endpoint is queried with an explicit {@code ?tags=} filter, so a scope that is
	 * down is hidden behind a scope that is up. Verified live: against the running runtime
	 * {@code /atlas/system/health?tags=atlas} returned six of the seven checks until the
	 * two instances were given distinct names.
	 *
	 * <p>
	 * Only checks under different PIDs are compared. The tenant configurations all use the
	 * same {@code ServicesCheck~tenant} PID, so they are alternative deployments and the
	 * Configurator only ever applies one of them - two of them never meet, and reusing a
	 * name between them is fine. What must differ is the tenant check from the
	 * {@code ~atlas} one in the shared runtime.config bundle, which every runtime carries.
	 */
	@Test
	public void testEveryServicesCheckIsNamedDistinctly(@InjectBundleContext BundleContext context) throws Exception {

		Map<String, JsonNode> configs = readConfigs(context);
		// PID -> the names that PID is given, across the configurations that define it.
		Map<String, Map<String, String>> namesByPid = new TreeMap<>();

		for (Map.Entry<String, JsonNode> file : configs.entrySet()) {
			for (Map.Entry<String, JsonNode> pid : pids(file.getValue(), SERVICES_CHECK_PID)) {
				String where = file.getKey() + " -> " + pid.getKey();

				JsonNode name = pid.getValue().get("hc.name");
				assertTrue(name != null && !name.asString().isBlank(),
						where + " declares no hc.name. It would carry the Felix default shared by every"
								+ " ServicesCheck, so its row in /atlas/system/health could not be told apart"
								+ " from the atlas scope check, and a '?tags=' query would collapse the two.");
				assertTrue(!DEFAULT_SERVICES_CHECK_NAME.equals(name.asString()),
						where + " declares hc.name '" + DEFAULT_SERVICES_CHECK_NAME + "', the Felix default"
								+ " that every other ServicesCheck would carry too.");

				namesByPid.computeIfAbsent(pid.getKey(), k -> new TreeMap<>()).put(name.asString(), where);
			}
		}

		for (Map.Entry<String, Map<String, String>> pid : namesByPid.entrySet()) {
			for (Map.Entry<String, Map<String, String>> other : namesByPid.entrySet()) {
				if (pid.getKey().equals(other.getKey())) {
					continue;
				}
				Set<String> shared = new TreeSet<>(pid.getValue().keySet());
				shared.retainAll(other.getValue().keySet());
				assertEquals(Set.of(), shared,
						pid.getKey() + " and " + other.getKey() + " both use the name(s) " + shared
								+ ". These PIDs are applied to the same runtime, so the checks would be"
								+ " indistinguishable and a '?tags=' query would report only one of them.");
			}
		}
	}

	@Test
	public void testTheServletIsConfiguredWithPropertiesItHas(@InjectBundleContext BundleContext context)
			throws Exception {

		Map<String, JsonNode> configs = readConfigs(context);
		List<Map.Entry<String, JsonNode>> servlets = new ArrayList<>();
		for (Map.Entry<String, JsonNode> file : configs.entrySet()) {
			for (Map.Entry<String, JsonNode> pid : pids(file.getValue(), SERVLET_PID)) {
				servlets.add(Map.entry(file.getKey() + " -> " + pid.getKey(), pid.getValue()));
			}
		}

		assertEquals(1, servlets.size(),
				"The health servlet is configured once, in the shared runtime.config bundle; found " + servlets.size());

		Map.Entry<String, JsonNode> servlet = servlets.get(0);
		Set<String> unknown = new TreeSet<>();
		servlet.getValue().propertyNames().forEach(name -> {
			if (!SERVLET_PROPERTIES.contains(name)) {
				unknown.add(name);
			}
		});

		assertEquals(Set.of(), unknown,
				servlet.getKey() + " sets " + unknown + ", which HealthCheckExecutorServletConfiguration does not"
						+ " declare. Such a property is stored by Configuration Admin and never read, so it looks"
						+ " configured and does nothing. Known ids: " + new TreeSet<>(SERVLET_PROPERTIES));

		JsonNode tags = servlet.getValue().get("tags");
		assertTrue(tags != null && values(tags).contains(ATLAS_TAG),
				servlet.getKey() + " must filter its default endpoint by '" + ATLAS_TAG
						+ "', the tag every Atlas health check declares.");
	}

	/** The PID entries of one configuration whose name addresses the given factory PID. */
	private static List<Map.Entry<String, JsonNode>> pids(JsonNode config, String factoryPid) {
		List<Map.Entry<String, JsonNode>> found = new ArrayList<>();
		config.propertyNames().forEach(name -> {
			// A factory configuration is addressed as "<factoryPid>~<name>"; the plain PID is
			// a singleton entry of the same component (see the DS factory configuration rules).
			if (name.equals(factoryPid) || name.startsWith(factoryPid + "~")) {
				found.add(Map.entry(name, config.get(name)));
			}
		});
		return found;
	}

	private static Set<String> values(JsonNode array) {
		Set<String> values = new TreeSet<>();
		array.forEach(value -> values.add(value.asString()));
		return values;
	}

	/**
	 * The configuration files this bundle carries, by resource name. Missing files fail
	 * here rather than quietly reducing what the tests above look at.
	 */
	private static Map<String, JsonNode> readConfigs(BundleContext context) throws Exception {
		Map<String, JsonNode> configs = new TreeMap<>();
		Enumeration<URL> entries = context.getBundle().findEntries("configs", "*.json", false);
		if (entries != null) {
			while (entries.hasMoreElements()) {
				URL entry = entries.nextElement();
				String name = entry.getPath().substring(entry.getPath().lastIndexOf('/') + 1);
				try (InputStream in = entry.openStream()) {
					configs.put(name, MAPPER.readTree(in));
				}
			}
		}
		assertEquals(new TreeSet<>(EXPECTED_FILES), new TreeSet<>(configs.keySet()),
				"The configuration files are copied into this bundle by -includeresource in bnd.bnd."
						+ " A missing one means that wiring broke, not that there is nothing to check.");
		return configs;
	}
}
