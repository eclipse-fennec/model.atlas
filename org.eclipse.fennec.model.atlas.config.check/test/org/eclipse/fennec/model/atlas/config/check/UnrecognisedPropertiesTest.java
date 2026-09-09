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
package org.eclipse.fennec.model.atlas.config.check;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the check itself, independent of any component that uses it.
 *
 * @see <a href="https://github.com/eclipse-fennec/model.atlas/issues/261">issue
 *      #261</a>
 */
@DisplayName("UnrecognisedProperties")
public class UnrecognisedPropertiesTest {

    /** Stands in for a configuration annotation, with the mangling cases in it. */
    @interface ExampleConfig {

        String scope_name();

        String scope_parent() default "atlas";

        String registryService_target() default "(scope=no-inject)";

        String delete_after_transition() default "false";

        String single__low_line();

        String with$_$hyphen();

        String dollar$sign();

        String two$$dollars();
    }

    /** A configuration annotation that prefixes every one of its ids. */
    @interface PrefixedConfig {

        String PREFIX_ = "atlas.";

        String scope_name();
    }

    @Nested
    @DisplayName("the recognised ids come from the annotation, per OSGi 707.4.4.4.1")
    class DeclaredIds {

        @Test
        @DisplayName("a single low line reads as a full stop, which is the #261 mistake")
        void singleLowLineBecomesAFullStop() {
            assertTrue(UnrecognisedProperties.declaredIds(ExampleConfig.class).contains("scope.parent"),
                    "scope_parent() is the property scope.parent");
        }

        @Test
        @DisplayName("the remaining escapes are honoured")
        void theEscapesAreHonoured() {
            assertEquals("single_low.line", UnrecognisedProperties.idOf("single__low_line"));
            assertEquals("with-hyphen", UnrecognisedProperties.idOf("with$_$hyphen"));
            assertEquals("dollarsign", UnrecognisedProperties.idOf("dollar$sign"));
            assertEquals("two$dollars", UnrecognisedProperties.idOf("two$$dollars"));
        }

        @Test
        @DisplayName("a PREFIX_ constant prefixes every id")
        void aPrefixConstantIsApplied() {
            assertEquals(Set.of("atlas.scope.name"), UnrecognisedProperties.declaredIds(PrefixedConfig.class));
        }
    }

    @Nested
    @DisplayName("what is reported")
    class Reported {

        @Test
        @DisplayName("an unrecognised key is named, with the declared id it resembles")
        void anUnrecognisedKeyIsNamed() {
            List<String> findings = UnrecognisedProperties.report("ScopeService[jena]", ExampleConfig.class,
                    props("scope.name", "jena", "parent.scope", "platform"));

            assertEquals(1, findings.size(), () -> "one finding expected, got " + findings);
            assertEquals("ScopeService[jena]: unrecognised configuration property 'parent.scope' - ignored."
                    + " Did you mean 'scope.parent'?", findings.get(0));
        }

        @Test
        @DisplayName("a typo is measured against the declared ids")
        void aTypoIsMeasured() {
            List<String> findings = UnrecognisedProperties.report("RegistryService[schema]", ExampleConfig.class,
                    props("delete.after.transitions", true));

            assertTrue(findings.get(0).endsWith("Did you mean 'delete.after.transition'?"),
                    () -> "the nearest declared id must be offered: " + findings);
        }

        @Test
        @DisplayName("a key that resembles nothing is reported without a guess")
        void anUnrelatedKeyGetsNoGuess() {
            List<String> findings = UnrecognisedProperties.report("ScopeService[jena]", ExampleConfig.class,
                    props("colour.of.the.bikeshed", "green"));

            assertEquals(List.of("ScopeService[jena]: unrecognised configuration property"
                    + " 'colour.of.the.bikeshed' - ignored."), findings);
        }

        @Test
        @DisplayName("findings come in key order, so a diff of them is stable")
        void findingsAreOrdered() {
            List<String> findings = UnrecognisedProperties.report("X", ExampleConfig.class,
                    props("zebra", 1, "aardvark", 2));

            assertTrue(findings.get(0).contains("'aardvark'"), () -> "sorted by key: " + findings);
            assertTrue(findings.get(1).contains("'zebra'"), () -> "sorted by key: " + findings);
        }
    }

    @Nested
    @DisplayName("what is left alone")
    class LeftAlone {

        @Test
        @DisplayName("the declared attributes, however many are set")
        void theDeclaredAttributes() {
            assertEquals(List.of(), UnrecognisedProperties.report("X", ExampleConfig.class,
                    props("scope.name", "jena", "scope.parent", "atlas", "registryService.target", "(a=b)")));
        }

        @Test
        @DisplayName("the properties DS, Configuration Admin and the framework add")
        void theFrameworkOwnedProperties() {
            assertEquals(List.of(), UnrecognisedProperties.report("X", ExampleConfig.class,
                    props("component.name", "X", "component.id", 3L, "service.pid", "X~a", "service.factoryPid", "X",
                            "service.ranking", 10, "objectClass", new String[] { "java.lang.Object" }, ".private", "p",
                            ":configurator:resource-version", 1, "felix.fileinstall.filename", "file:/x",
                            "osgi.ds.satisfying.condition.target", "(a=b)")));
        }

        @Test
        @DisplayName("the reference properties, whose names this cannot know")
        void theReferenceProperties() {
            assertEquals(List.of(), UnrecognisedProperties.report("X", ExampleConfig.class,
                    props("stageActionService.target", "(a=b)", "storageService.cardinality.minimum", 1)));
        }

        @Test
        @DisplayName("but not a cardinality maximum, which Declarative Services does not have")
        void aCardinalityMaximumIsNotAReferenceProperty() {
            List<String> findings = UnrecognisedProperties.report("X", ExampleConfig.class,
                    props("storageService.cardinality.maximum", 4));

            assertEquals(1, findings.size(), () -> "a reference's multiplicity cannot be configured: " + findings);
            assertTrue(findings.get(0).contains("'storageService.cardinality.maximum'"),
                    () -> "the useless key must be named: " + findings);
        }

        @Test
        @DisplayName("a service property the component publishes on purpose, once named")
        void anAcceptedServiceProperty() {
            Map<String, Object> properties = props("scope.name", "jena", "registry", "main");

            assertTrue(UnrecognisedProperties.report("X", ExampleConfig.class, properties).size() == 1,
                    "unnamed, it is a finding like any other");
            assertEquals(List.of(),
                    UnrecognisedProperties.report("X", ExampleConfig.class, properties, Set.of("registry")),
                    "named in alsoRecognised, it is accepted");
        }

        @Test
        @DisplayName("no properties at all")
        void noProperties() {
            assertEquals(List.of(), UnrecognisedProperties.report("X", ExampleConfig.class, Map.of()));
            assertEquals(List.of(), UnrecognisedProperties.report("X", ExampleConfig.class, null));
        }
    }

    @Test
    @DisplayName("a reordering of the same segments is preferred over a near-enough edit")
    void reorderingWinsOverEditDistance() {
        Optional<String> suggestion = UnrecognisedProperties.suggestionFor("parent.scope",
                Set.of("scope.parent", "scope.parents"));

        assertEquals(Optional.of("scope.parent"), suggestion);
    }

    private static Map<String, Object> props(Object... keysAndValues) {
        Map<String, Object> properties = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            properties.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return properties;
    }
}
