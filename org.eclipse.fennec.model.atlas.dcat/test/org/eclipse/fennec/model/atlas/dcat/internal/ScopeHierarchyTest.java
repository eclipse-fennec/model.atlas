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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * The scope tree, and the two questions the fan-out asks of it.
 *
 * <p>
 * Worth testing on plain strings: the link bookkeeping of O4 is where inheritance is either right
 * or silently half-right, and none of it needs a portal.
 * </p>
 */
public class ScopeHierarchyTest {

    /** The plan's own example: {@code atlas → jena → nawerker}. */
    private static ScopeHierarchy chain() {
        Map<String, String> parents = new LinkedHashMap<>();
        parents.put("atlas", null);
        parents.put("jena", "atlas");
        parents.put("nawerker", "jena");
        return ScopeHierarchy.of(parents);
    }

    @Test
    public void ancestorsRunFromTheNearestParentUpwards() {
        assertThat(chain().ancestors("nawerker")).containsExactly("jena", "atlas");
        assertThat(chain().ancestors("jena")).containsExactly("atlas");
        assertThat(chain().ancestors("atlas")).as("a root inherits from nothing").isEmpty();
    }

    @Test
    public void descendantsAreTransitive() {
        // The whole point of O4: an atlas package's Dataset belongs in three Catalogs, so this
        // answer has to reach past the direct children.
        assertThat(chain().descendants("atlas")).containsExactlyInAnyOrder("jena", "nawerker");
        assertThat(chain().descendants("jena")).containsExactly("nawerker");
        assertThat(chain().descendants("nawerker")).isEmpty();
    }

    @Test
    public void aParentNobodyHasSeenIsStillAnAncestor() {
        // The child told us the name. Whether that scope is bound, or published to this portal, is
        // the caller's gate — dropping the name here would silently narrow the fan-out instead.
        ScopeHierarchy orphan = ScopeHierarchy.of(Map.of("nawerker", "jena"));
        assertThat(orphan.ancestors("nawerker")).containsExactly("jena");
        assertThat(orphan.descendants("jena")).containsExactly("nawerker");
    }

    @Test
    public void aChainThroughAnUnknownScopeStopsThere() {
        // nawerker's parent is jena, but nothing here knows jena's parent, so atlas is not
        // reachable yet. This is exactly why a scope bind re-runs the fan-out for what is below it.
        ScopeHierarchy partial = ScopeHierarchy.of(Map.of("nawerker", "jena", "atlas", "root"));
        assertThat(partial.ancestors("nawerker")).containsExactly("jena");
        assertThat(partial.descendants("atlas")).isEmpty();
    }

    @Test
    public void anUnknownScopeAnswersEmpty() {
        assertThat(chain().ancestors("nobody")).isEmpty();
        assertThat(chain().descendants("nobody")).isEmpty();
        assertThat(chain().parentOf("nobody")).isNull();
    }

    @Test
    public void aCycleTruncatesInsteadOfLooping() {
        // A hand-written configuration can express this, and neither walk may hang on it.
        ScopeHierarchy cyclic = ScopeHierarchy.of(Map.of("a", "b", "b", "c", "c", "a"));

        assertThat(cyclic.ancestors("a")).containsExactly("b", "c");
        assertThat(cyclic.descendants("a")).containsExactlyInAnyOrder("c", "b");
    }

    @Test
    public void aScopeThatIsItsOwnParentIsNotAnAncestorOfItself() {
        ScopeHierarchy selfParent = ScopeHierarchy.of(Map.of("a", "a"));

        assertThat(selfParent.ancestors("a")).isEmpty();
        assertThat(selfParent.descendants("a")).isEmpty();
    }

    @Test
    public void blankAndMissingParentsBothMeanRoot() {
        Map<String, String> parents = new LinkedHashMap<>();
        parents.put("blank", "   ");
        parents.put("nulled", null);
        parents.put("child", "blank");
        ScopeHierarchy hierarchy = ScopeHierarchy.of(parents);

        assertThat(hierarchy.parentOf("blank")).isNull();
        assertThat(hierarchy.parentOf("nulled")).isNull();
        assertThat(hierarchy.children("blank")).containsExactly("child");
    }

    @Test
    public void childrenAreDirectOnly() {
        assertThat(chain().children("atlas")).containsExactly("jena");
        assertThat(chain().parentOf("jena")).isEqualTo("atlas");
    }
}
