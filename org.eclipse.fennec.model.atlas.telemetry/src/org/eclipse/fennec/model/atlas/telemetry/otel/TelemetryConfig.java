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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.telemetry.otel;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * What a deployment can say about the Atlas's metrics.
 *
 * <p>
 * Deliberately short. Which exporter runs, where it sends and how often is the OpenTelemetry
 * integration's configuration, not ours — this bundle only decides what to call things and how much
 * detail to report.
 * </p>
 */
@ObjectClassDefinition(name = "Model Atlas Telemetry",
        description = "Metrics for what this Atlas holds, taken from the Atlas runtime service.")
public @interface TelemetryConfig {

    /**
     * The instrumentation scope every instrument is created under. A backend groups by it, so it
     * should name this project rather than the deployment.
     */
    @AttributeDefinition(name = "Instrumentation scope",
            description = "Reported as the instrumentation scope name of every instrument.")
    String scope() default "org.eclipse.fennec.model.atlas";

    /**
     * Whether to report one series per schema package beside the counts.
     *
     * <p>
     * Off by default, and that is the opposite of the counts' default for a reason: an nsURI and a
     * fingerprint are unbounded-cardinality values, and one series per package per stage is a bill
     * that grows with the models a deployment loads. A backend that wants the detail can afford it
     * deliberately; nobody should pay for it by accident.
     * </p>
     */
    @AttributeDefinition(name = "Report packages individually",
            description = "Report one series per schema package beside the counts. Off by default: "
                    + "nsURI and fingerprint are unbounded cardinality.")
    boolean packageSeries() default false;
}
