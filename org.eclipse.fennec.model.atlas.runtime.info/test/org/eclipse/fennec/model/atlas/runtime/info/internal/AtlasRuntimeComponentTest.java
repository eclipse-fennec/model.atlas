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
package org.eclipse.fennec.model.atlas.runtime.info.internal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.eclipse.fennec.model.atlas.mediatypes.api.MediatypeContributor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Constants;

/**
 * What the component does with a callback that arrives outside its active window.
 *
 * <p>
 * DS binds a dynamic reference before it calls {@code activate} and unbinds it after
 * {@code deactivate} (112.5.6 and 112.5.11), so a component that registers its own service has no
 * registration to publish a change count on for the first and the last callbacks it gets. In a
 * deployed Atlas the first ones are the great majority: every scope, package and contributor is
 * registered long before this component comes up.
 * </p>
 *
 * <p>
 * Nothing here needs a framework, which is the point — the window is a property of the component,
 * not of the runtime it happens to be in.
 * </p>
 */
class AtlasRuntimeComponentTest {

    private static final MediatypeContributor CONTRIBUTOR = () -> List.of("application/vnd.test+xml");

    @Test
    @DisplayName("a service bound before activation is tracked rather than throwing")
    void boundBeforeActivation() {
        AtlasRuntimeComponent component = new AtlasRuntimeComponent();

        assertDoesNotThrow(() -> component.addContributor(CONTRIBUTOR, Map.of(Constants.SERVICE_ID, 1L)),
                "Felix logs a bind that throws and carries on, so this costs a stack trace per "
                        + "service present at start-up rather than a component — which is worse, "
                        + "because the log says the Atlas is broken when it is not");
    }

    @Test
    @DisplayName("what was bound on the way in is in the first snapshot")
    void countedBeforeActivation() {
        AtlasRuntimeComponent component = new AtlasRuntimeComponent();

        component.addContributor(CONTRIBUTOR, Map.of(Constants.SERVICE_ID, 1L));

        // The count moved although nothing could be published: activation registers with the
        // number as it stands, so the pre-activation binds are in the first value a watcher sees.
        assertEquals(1, component.snapshot().changeCount, "the count moves whether or not it can be published");
        assertEquals(1, component.snapshot().mediaTypes.size(), "and the service itself is tracked");
    }

    @Test
    @DisplayName("a service unbound after deactivation is dropped rather than throwing")
    void unboundAfterDeactivation() {
        AtlasRuntimeComponent component = new AtlasRuntimeComponent();
        component.addContributor(CONTRIBUTOR, Map.of(Constants.SERVICE_ID, 1L));

        assertDoesNotThrow(() -> component.removeContributor(CONTRIBUTOR, Map.of(Constants.SERVICE_ID, 1L)),
                "the way down is the same window as the way up");
        assertEquals(0, component.snapshot().mediaTypes.size());
    }
}
