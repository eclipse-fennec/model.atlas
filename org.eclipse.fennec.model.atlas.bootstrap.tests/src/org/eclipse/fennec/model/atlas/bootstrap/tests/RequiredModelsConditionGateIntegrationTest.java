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
package org.eclipse.fennec.model.atlas.bootstrap.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.condition.Condition;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * The gate {@code RequiredModelsCondition} puts in front of the {@code InitialModelLoader}
 * must not open before the deployment has said what it requires (issue #362).
 *
 * <p>
 * An unconfigured activation cannot tell "this deployment requires nothing" from "the
 * configuration has not been delivered yet". Registering {@code atlas.initial.models.ready}
 * from such an activation lets the loader read the seed folder before the model bundles have
 * registered their EPackages, which fails the start with a {@code PackageNotFoundException}
 * about a second before the configuration arrives.
 * </p>
 *
 * <p>
 * So the condition is registered only from a configured activation, and a deployment that
 * requires nothing says so with an empty {@code required.models}. Both halves are asserted
 * here: the gate stays shut while nothing has been configured, and an empty configuration is
 * enough to open it.
 * </p>
 *
 * <p>
 * The assertion is on the {@link Condition} <em>service</em> rather than on the component's
 * state, because the service is what the loader's mandatory reference binds to — a component
 * that activates but registers nothing would otherwise look like a working gate.
 * </p>
 */
@RequireConfigurationAdmin
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("InitialModelLoader Required-Models Gate Integration Tests")
public class RequiredModelsConditionGateIntegrationTest {

    private static final String REQUIRED_MODELS_PID = "InitialModelLoaderRequiredModels";
    private static final String READY_CONDITION = "atlas.initial.models.ready";
    private static final String REQUIRED_MODELS = "required.models";

    private static final long SETTLE_MS = 5_000L;

    private Configuration requiredModelsConfiguration;

    @AfterEach
    void deleteConfiguration() throws IOException {
        if (requiredModelsConfiguration != null) {
            requiredModelsConfiguration.delete();
            requiredModelsConfiguration = null;
        }
    }

    @Test
    @DisplayName("The ready condition is absent while no required-models configuration has arrived")
    public void theGateStaysShutWhileNothingIsConfigured(
            @InjectService(cardinality = 0) ServiceAware<ConfigurationAdmin> cmAware,
            @InjectService(cardinality = 0, filter = "(osgi.condition.id=" + READY_CONDITION
                    + ")") ServiceAware<Condition> readyAware)
            throws Exception {

        ConfigurationAdmin cm = cmAware.waitForService(SETTLE_MS);
        assertNotNull(cm, "ConfigurationAdmin should be available");

        // Whatever another test left behind: this deployment has said nothing about required
        // models, which is the state a runtime is in between framework start and the
        // configurator delivering its files.
        cm.getConfiguration(REQUIRED_MODELS_PID, "?").delete();

        assertNull(awaitAbsent(readyAware),
                "the loader's gate must not open from an unconfigured activation: an empty default "
                        + "cannot be told apart from a configuration that has not arrived yet");
    }

    @Test
    @DisplayName("An empty required-models configuration opens the gate")
    public void anEmptyConfigurationOpensTheGate(
            @InjectService(cardinality = 0) ServiceAware<ConfigurationAdmin> cmAware,
            @InjectService(cardinality = 0, filter = "(osgi.condition.id=" + READY_CONDITION
                    + ")") ServiceAware<Condition> readyAware)
            throws Exception {

        ConfigurationAdmin cm = cmAware.waitForService(SETTLE_MS);
        assertNotNull(cm, "ConfigurationAdmin should be available");

        requiredModelsConfiguration = cm.getConfiguration(REQUIRED_MODELS_PID, "?");
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put(REQUIRED_MODELS, new String[0]);
        requiredModelsConfiguration.update(properties);

        assertNotNull(readyAware.waitForService(SETTLE_MS),
                "a deployment that requires no bundle-shipped model says so with an empty "
                        + REQUIRED_MODELS + ", and the loader must then start");
    }

    /**
     * The condition may still be registered from an earlier state; give it a chance to go away
     * before concluding that it is absent.
     */
    private static Condition awaitAbsent(ServiceAware<Condition> aware) throws InterruptedException {
        long deadline = System.currentTimeMillis() + SETTLE_MS;
        while (System.currentTimeMillis() < deadline) {
            if (aware.getService() == null) {
                return null;
            }
            Thread.sleep(100L);
        }
        return aware.getService();
    }
}
