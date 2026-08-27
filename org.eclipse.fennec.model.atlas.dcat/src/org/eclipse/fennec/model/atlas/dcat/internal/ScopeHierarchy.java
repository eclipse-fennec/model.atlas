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
package org.eclipse.fennec.model.atlas.dcat.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The scope tree, and the one place the link fan-out is computed.
 *
 * <h2>What the tree means here</h2>
 *
 * A scope's parent is where it inherits models from, so a package defined in {@code atlas} is
 * served by {@code jena} and by {@code jena}'s children too. The catalogue says the same thing by
 * <em>linking</em> one Dataset into every descendant's Catalog rather than minting a Dataset per
 * inheriting scope: one model, one resource, one id, appearing in as many catalogues as serve it.
 *
 * <p>
 * The two questions below are duals, and both are needed. Publishing a package asks "which
 * Catalogs must list this Dataset" — its own scope's and every descendant's. Writing a Catalog
 * asks "which Datasets must this Catalog list" — its own scope's and every <em>ancestor's</em>,
 * which is also what makes a scope created later pick up the models it inherits without anything
 * happening to those models.
 * </p>
 *
 * <h2>An immutable snapshot</h2>
 *
 * Built per operation from the publisher's live scope map, which costs a walk over a handful of
 * entries and buys a stable answer: a fan-out computed against a tree that changes underneath it
 * would link into some descendants and not others, and nothing later would notice.
 */
final class ScopeHierarchy {

    /** Scope name to parent scope name. A scope with no parent is absent from the values side. */
    private final Map<String, String> parents;

    private ScopeHierarchy(Map<String, String> parents) {
        this.parents = parents;
    }

    /**
     * @param parentByScope scope name to its parent's name; a {@code null} or blank parent marks a
     *                      root. Names naming a scope this publisher has never seen are kept: the
     *                      parent may bind later, and the answer is filtered by the caller
     */
    static ScopeHierarchy of(Map<String, String> parentByScope) {
        // Roots are simply absent: a null value carries no more information than no entry, and
        // Map.copyOf rejects one anyway.
        Map<String, String> parents = new LinkedHashMap<>();
        parentByScope.forEach((scope, parent) -> {
            if (scope != null && !scope.isBlank() && parent != null && !parent.isBlank()) {
                parents.put(scope, parent);
            }
        });
        return new ScopeHierarchy(Map.copyOf(parents));
    }

    /**
     * The scopes {@code scope} inherits from, nearest parent first.
     *
     * <p>
     * A name is returned even when no scope service has been seen for it: the child told us the
     * name, and whether that scope is published to this portal is the caller's gate, not ours. The
     * walk stops at the first scope whose parent is unknown — a chain through an unbound scope
     * cannot be completed, and completing it later is what a bind re-runs the fan-out for.
     * </p>
     *
     * <p>
     * A cycle — which a hand-written configuration can express — truncates rather than loops.
     * </p>
     */
    List<String> ancestors(String scope) {
        List<String> ancestors = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        seen.add(scope);
        String current = parents.get(scope);
        while (current != null && seen.add(current)) {
            ancestors.add(current);
            current = parents.get(current);
        }
        return List.copyOf(ancestors);
    }

    /**
     * Every scope that inherits from {@code scope}, directly or through another scope.
     *
     * <p>
     * Never {@code scope} itself, even where a configuration has written a cycle: a scope does not
     * inherit from itself, and a fan-out that thought otherwise would be reasoning about a
     * catalogue tree that cannot exist.
     * </p>
     */
    Set<String> descendants(String scope) {
        Set<String> descendants = new LinkedHashSet<>();
        Set<String> visited = new LinkedHashSet<>();
        // Seeded with the start, so a cycle leading back to it neither recurses nor reports it as
        // its own descendant.
        visited.add(scope);
        collectDescendants(scope, descendants, visited);
        return descendants;
    }

    private void collectDescendants(String scope, Set<String> collected, Set<String> visited) {
        children(scope).forEach(child -> {
            if (visited.add(child)) {
                collected.add(child);
                collectDescendants(child, collected, visited);
            }
        });
    }

    /** The scopes whose parent is {@code scope}. */
    List<String> children(String scope) {
        return parents.entrySet().stream().filter(entry -> scope.equals(entry.getValue())).map(Map.Entry::getKey)
                .toList();
    }
}
