/*
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
package org.eclipse.fennec.model.atlas.workflow.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import org.eclipse.fennec.model.atlas.action.api.StageActionService;
import org.eclipse.fennec.model.atlas.workflow.impl.StageActionChains.ActionBinding;
import org.eclipse.fennec.model.atlas.workflow.impl.StageActionChains.Chain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link StageActionChains}: parsing, naming, selection and ordering
 * (issue #296).
 */
@DisplayName("StageActionChains")
class StageActionChainsTest {

    private static ActionBinding binding(String name, int ranking, long id) {
        return new ActionBinding(mock(StageActionService.class), name, ranking, id);
    }

    @Test
    @DisplayName("No configuration means the default chain: ranking order, continue on failure")
    void noneIsDefault() {
        assertSame(StageActionChains.DEFAULT, StageActionChains.parse(null).select("draft", "t"));
        assertSame(StageActionChains.DEFAULT, StageActionChains.parse(new String[0]).select("draft", "t"));
        assertFalse(StageActionChains.DEFAULT.stopOnFailure());
    }

    @Test
    @DisplayName("Entries parse with optional stage and objectType, and the first match wins")
    void parsesAndSelects() {
        StageActionChains chains = StageActionChains.parse(new String[] {
                "{\"stage\": \"draft\", \"objectType\": \"http://t#//A\", \"actions\": [\"x\"], \"onFailure\": \"stop\"}",
                "{\"stage\": \"draft\", \"actions\": [\"y\"]}",
                " ", "{\"actions\": [\"z\"], \"onFailure\": \"continue\"}" });

        assertEquals(3, chains.chains().size(), "a blank entry is skipped");
        Chain specific = chains.select("draft", "http://t#//A");
        assertEquals(List.of("x"), specific.actions());
        assertTrue(specific.stopOnFailure());
        assertEquals(List.of("y"), chains.select("draft", "http://t#//B").actions());
        assertEquals(List.of("z"), chains.select("release", "http://t#//A").actions());
        assertFalse(chains.select("release", "http://t#//A").stopOnFailure());
    }

    @Test
    @DisplayName("Malformed entries are refused with a reason")
    void refusesMalformedEntries() {
        assertThrows(IllegalArgumentException.class, () -> StageActionChains.parse(new String[] { "nope" }));
        assertThrows(IllegalArgumentException.class,
                () -> StageActionChains.parse(new String[] { "{\"stage\": \"draft\"}" }));
        assertThrows(IllegalArgumentException.class,
                () -> StageActionChains.parse(new String[] { "{\"actions\": []}" }));
        assertThrows(IllegalArgumentException.class,
                () -> StageActionChains.parse(new String[] { "{\"actions\": [1]}" }));
        assertThrows(IllegalArgumentException.class,
                () -> StageActionChains.parse(new String[] { "{\"actions\": [\"a\"], \"onFailure\": \"maybe\"}" }));
        assertThrows(IllegalArgumentException.class,
                () -> StageActionChains.parse(new String[] { "{\"actions\": [\"a\"], \"order\": 1}" }));
    }

    @Test
    @DisplayName("The default chain orders by ranking, highest first, then by service id")
    void defaultOrder() {
        List<ActionBinding> ordered = StageActionChains.DEFAULT
                .order(List.of(binding("c", 1, 3), binding("a", 10, 1), binding("b", 10, 2), binding("d", 0, 4)));

        assertEquals(List.of("a", "b", "c", "d"), ordered.stream().map(ActionBinding::name).toList());
    }

    @Test
    @DisplayName("A chain places the named actions first, in its order, and the rest by ranking")
    void chainOrder() {
        Chain chain = new Chain(null, null, List.of("validate", "compile", "absent"), true);
        List<ActionBinding> ordered = chain.order(List.of(binding("compile", 10, 1), binding("notify", 5, 2),
                binding("validate", 0, 3), binding("index", 7, 4)));

        assertEquals(List.of("validate", "compile", "index", "notify"),
                ordered.stream().map(ActionBinding::name).toList());
    }

    @Test
    @DisplayName("An action is named by stage.action.name, else component.name, else its class")
    void naming() {
        StageActionService service = mock(StageActionService.class);
        assertEquals("explicit", ActionBinding.nameOf(service,
                Map.of(StageActionChains.NAME_PROPERTY, "explicit", "component.name", "Component")));
        assertEquals("Component", ActionBinding.nameOf(service, Map.of("component.name", "Component")));
        assertEquals(service.getClass().getSimpleName(), ActionBinding.nameOf(service, Map.of()));

        ActionBinding fromProps = ActionBinding.of(service, Map.of("service.ranking", 7, "service.id", 42L));
        assertEquals(7, fromProps.ranking());
        assertEquals(42L, fromProps.serviceId());
        ActionBinding bare = ActionBinding.of(service, null);
        assertEquals(0, bare.ranking());
        assertTrue(bare.serviceId() > 0, "a binding without a service id still orders stably");
    }
}
