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

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.fennec.model.atlas.action.api.StageActionService;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * The order in which a registry runs its stage actions for one event, and what a failure
 * in that order means (issue #296).
 *
 * <p>
 * Stage actions are independent whiteboard services; without a word from the
 * configuration they run in {@code service.ranking} order, highest first, ties broken by
 * {@code service.id} so the order is stable, and a failing action is logged and the rest
 * still run - exactly the contract before this class existed. A registry that needs more,
 * because the QVT source has to be compiled before it is validated, says so in
 * {@code stage.action.chains}: one JSON entry per chain,
 * </p>
 *
 * <pre>
 * {"stage": "draft", "objectType": "http://…#//SourceUnit", "actions": ["QvtCompile", "QvtValidate"], "onFailure": "stop"}
 * </pre>
 *
 * <p>
 * {@code stage} and {@code objectType} are optional and narrow the chain to the events it
 * is for; the first entry that matches an event is the one that applies. {@code actions}
 * names the actions that have a place in the order, by their {@code stage.action.name}
 * service property, else their {@code component.name}, else their simple class name;
 * they run first, in that order, and every other bound action follows in ranking order.
 * A name nobody is bound under is simply not there - a registry that must not run without
 * an action says so with {@code stageActionService.cardinality.minimum}. {@code onFailure}
 * is {@code continue} (the default) or {@code stop}: whether the actions after a failing
 * one still run for this event.
 * </p>
 *
 * <p>
 * The chain is configuration, not a new kind of service: the actions know nothing of each
 * other, and the same action may sit in different places in different registries.
 * </p>
 */
final class StageActionChains {

    /** Service property under which an action names itself for a chain. */
    static final String NAME_PROPERTY = "stage.action.name";

    private static final String KEY_STAGE = "stage";
    private static final String KEY_OBJECT_TYPE = "objectType";
    private static final String KEY_ACTIONS = "actions";
    private static final String KEY_ON_FAILURE = "onFailure";
    private static final String ON_FAILURE_CONTINUE = "continue";
    private static final String ON_FAILURE_STOP = "stop";
    private static final Set<String> KNOWN_KEYS = Set.of(KEY_STAGE, KEY_OBJECT_TYPE, KEY_ACTIONS, KEY_ON_FAILURE);

    private static final AtomicLong UNKNOWN_SERVICE_IDS = new AtomicLong(Long.MAX_VALUE / 2);

    /**
     * A bound stage action with what a chain needs to place it: its name, its ranking and
     * its service id.
     */
    record ActionBinding(StageActionService service, String name, int ranking, long serviceId) {

        /** Highest ranking first; among equals the one registered first. */
        static final Comparator<ActionBinding> BY_RANKING = Comparator.comparingInt(ActionBinding::ranking).reversed()
                .thenComparingLong(ActionBinding::serviceId);

        static ActionBinding of(StageActionService service, Map<String, Object> properties) {
            requireNonNull(service, "service");
            Map<String, Object> props = properties == null ? Map.of() : properties;
            Object ranking = props.get("service.ranking");
            Object id = props.get("service.id");
            return new ActionBinding(service, nameOf(service, props),
                    ranking instanceof Number n ? n.intValue() : 0,
                    id instanceof Number n ? n.longValue() : UNKNOWN_SERVICE_IDS.getAndIncrement());
        }

        /**
         * The name a chain refers to an action by: {@code stage.action.name}, else the DS
         * {@code component.name}, else the simple class name.
         */
        static String nameOf(StageActionService service, Map<String, Object> properties) {
            Object explicit = properties.get(NAME_PROPERTY);
            if (explicit instanceof String s && !s.isBlank()) {
                return s;
            }
            Object component = properties.get("component.name");
            if (component instanceof String s && !s.isBlank()) {
                return s;
            }
            return service.getClass().getSimpleName();
        }
    }

    /** One configured chain, or the {@link #DEFAULT default} one. */
    record Chain(String stage, String objectType, List<String> actions, boolean stopOnFailure) {

        Chain {
            actions = actions == null ? List.of() : List.copyOf(actions);
        }

        boolean matches(String stage, String objectType) {
            return (this.stage == null || this.stage.equals(stage))
                    && (this.objectType == null || this.objectType.equals(objectType));
        }

        /**
         * The bound actions in the order this chain runs them: the named ones first, in
         * the configured order, then everyone else by ranking.
         */
        List<ActionBinding> order(Collection<ActionBinding> bound) {
            List<ActionBinding> byRanking = new ArrayList<>(bound);
            byRanking.sort(ActionBinding.BY_RANKING);
            if (actions.isEmpty()) {
                return byRanking;
            }
            Set<ActionBinding> placed = new LinkedHashSet<>();
            for (String name : actions) {
                for (ActionBinding binding : byRanking) {
                    if (name.equals(binding.name())) {
                        placed.add(binding);
                    }
                }
            }
            for (ActionBinding binding : byRanking) {
                placed.add(binding);
            }
            return List.copyOf(placed);
        }
    }

    /** No configured order: ranking decides, a failure does not stop the others. */
    static final Chain DEFAULT = new Chain(null, null, List.of(), false);

    private static final StageActionChains NONE = new StageActionChains(List.of());

    private final List<Chain> chains;

    private StageActionChains(List<Chain> chains) {
        this.chains = List.copyOf(chains);
    }

    /**
     * Parses the {@code stage.action.chains} configuration.
     *
     * @param entries the JSON entries, or {@code null}
     * @return the chains, in configuration order
     * @throws IllegalArgumentException for an entry that is no chain: not JSON, an unknown
     *                                  key, no actions, an unknown {@code onFailure}
     */
    static StageActionChains parse(String[] entries) {
        if (entries == null || entries.length == 0) {
            return NONE;
        }
        ObjectMapper mapper = new ObjectMapper();
        List<Chain> chains = new ArrayList<>(entries.length);
        for (String entry : entries) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            Map<String, Object> map;
            try {
                map = mapper.readValue(entry, new TypeReference<Map<String, Object>>() {
                });
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("stage.action.chains entry is no JSON object: " + entry, e);
            }
            for (String key : map.keySet()) {
                if (!KNOWN_KEYS.contains(key)) {
                    throw new IllegalArgumentException(String.format(
                            "stage.action.chains entry has an unknown key '%s'; known: %s", key, KNOWN_KEYS));
                }
            }
            Object actions = map.get(KEY_ACTIONS);
            if (!(actions instanceof List<?> list) || list.isEmpty()) {
                throw new IllegalArgumentException("stage.action.chains entry needs a non-empty 'actions' list: " + entry);
            }
            List<String> names = new ArrayList<>(list.size());
            for (Object name : list) {
                if (!(name instanceof String s) || s.isBlank()) {
                    throw new IllegalArgumentException("stage.action.chains 'actions' holds a non-name: " + entry);
                }
                names.add(s);
            }
            Object onFailure = map.getOrDefault(KEY_ON_FAILURE, ON_FAILURE_CONTINUE);
            boolean stop;
            if (ON_FAILURE_STOP.equals(onFailure)) {
                stop = true;
            } else if (ON_FAILURE_CONTINUE.equals(onFailure)) {
                stop = false;
            } else {
                throw new IllegalArgumentException(String.format(
                        "stage.action.chains 'onFailure' is '%s'; it has to be '%s' or '%s'", onFailure,
                        ON_FAILURE_CONTINUE, ON_FAILURE_STOP));
            }
            chains.add(new Chain(text(map.get(KEY_STAGE)), text(map.get(KEY_OBJECT_TYPE)), names, stop));
        }
        return new StageActionChains(chains);
    }

    private static String text(Object value) {
        return value instanceof String s && !s.isBlank() ? s : null;
    }

    /**
     * @return the first configured chain that applies to an event in {@code stage} about
     *         an object of {@code objectType}, or {@link #DEFAULT}
     */
    Chain select(String stage, String objectType) {
        for (Chain chain : chains) {
            if (chain.matches(stage, objectType)) {
                return chain;
            }
        }
        return DEFAULT;
    }

    List<Chain> chains() {
        return chains;
    }
}
