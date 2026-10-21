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
package org.eclipse.fennec.model.atlas.bootstrap;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Says so when the {@link RequiredModelsCondition} configuration never arrives (issue #362).
 *
 * <p>
 * The condition requires its configuration, so a deployment that ships none registers no
 * {@code atlas.initial.models.ready} and the {@link InitialModelLoader}, whose only gate is a
 * mandatory reference to it, never activates. Nothing is seeded — and nothing says so: the scope,
 * its registries and the health checks all come up exactly as configured, only the contents are
 * missing. The condition's own {@code required.models.wait.seconds} watchdog cannot report it,
 * because it is scheduled from the activation that did not happen.
 * </p>
 *
 * <p>
 * Hence this component, which is deliberately the one thing here that needs no configuration of
 * its own: it exists to notice that a configuration is absent, so it cannot be gated on one. It
 * looks once, {@value #WAIT_SECONDS} seconds in — two orders of magnitude more than the second or
 * so the configurator actually takes — and logs an error if the PID is still not there.
 * </p>
 */
@Component(name = RequiredModelsConfigurationWatchdog.PID, immediate = true,
        configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class RequiredModelsConfigurationWatchdog {

    public static final String PID = "InitialModelLoaderConfigurationWatchdog";

    /** How long the configurator is given before its silence is taken for absence. */
    static final long WAIT_SECONDS = 60;

    private static final Logger LOG = System.getLogger(RequiredModelsConfigurationWatchdog.class.getName());

    private final ScheduledExecutorService watchdog = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "InitialModelLoader-configuration-watchdog");
        t.setDaemon(true);
        return t;
    });

    @Reference
    private ConfigurationAdmin configAdmin;

    @Activate
    void activate() {
        watchdog.schedule(this::reportIfUnconfigured, WAIT_SECONDS, TimeUnit.SECONDS);
    }

    @Deactivate
    void deactivate() {
        watchdog.shutdownNow();
    }

    /** Looks the PID up and reports it missing. Package visible so a test can drive it. */
    void reportIfUnconfigured() {
        try {
            Configuration[] configurations = configAdmin
                    .listConfigurations("(" + Constants.SERVICE_PID + "=" + RequiredModelsCondition.PID + ")");
            String message = configurationMissingMessage(configurations, WAIT_SECONDS);
            if (message != null) {
                LOG.log(Level.ERROR, message);
            }
        } catch (IOException | InvalidSyntaxException lookupFailed) {
            // Not knowing is not the same as knowing it is absent, so this is not the error above.
            LOG.log(Level.WARNING, () -> "InitialModelLoader: could not check whether "
                    + RequiredModelsCondition.PID + " is configured: " + lookupFailed);
        }
    }

    /**
     * @param configurations what {@link ConfigurationAdmin#listConfigurations(String)} answered —
     *            {@code null} when nothing matched
     * @param waitSeconds how long was waited, for the message
     * @return the error to log, or {@code null} when the configuration is there
     */
    static String configurationMissingMessage(Configuration[] configurations, long waitSeconds) {
        if (configurations != null && configurations.length > 0) {
            return null;
        }
        return "InitialModelLoader: no " + RequiredModelsCondition.PID + " configuration after " + waitSeconds
                + "s. " + InitialModelLoader.PID + " is gated on it and will not start, so nothing in the "
                + "initial models folder is seeded and no scope is filled. A deployment that needs no "
                + "bundle-shipped model must ship the configuration anyway, with an empty required.models — "
                + "an unconfigured activation cannot be told apart from one whose configuration is still "
                + "on its way, which is why the gate waits.";
    }
}
