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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Reports configuration properties a component does not know.
 *
 * <p>
 * Declarative Services drops a property no attribute claims without a word, so
 * {@code parent.scope} sat in six shipped configurations while the component read
 * {@code scope.parent}, and the mismatch stayed invisible for as long as the value
 * happened to equal the default (issue #261). A silently dropped property cannot be
 * told apart from one that was rejected, overridden or never delivered, so a
 * component is better off naming what it ignored.
 * </p>
 *
 * <h2>Use</h2>
 *
 * <p>
 * Take the delivered properties as an activation object and hand them over with the
 * configuration annotation:
 * </p>
 *
 * <pre>
 * &#64;Activate
 * void activate(Map&lt;String, Object&gt; properties) {
 *     UnrecognisedProperties.warn(LOGGER, "MyService[" + name + "]", MyServiceConfig.class, properties);
 * }
 * </pre>
 *
 * <p>
 * {@link #report(String, Class, Map, Set)} returns the same findings without
 * logging them, for a component whose logging is not {@code java.util.logging}.
 * </p>
 *
 * <h2>What counts as unrecognised</h2>
 *
 * <p>
 * The recognised ids are derived from the configuration annotation itself, by the
 * mapping Declarative Services applies to a component property type's method names
 * (OSGi Compendium 707.4.4.4.1): a single {@code _} becomes {@code .}, {@code __}
 * an {@code _}, {@code $$} a {@code $}, {@code $_$} a {@code -}, and a lone
 * {@code $} is removed - which is why {@code scope_parent()} reads
 * {@code scope.parent}, and why the {@code @AttributeDefinition} name, being a
 * label, has no say in it.
 * </p>
 *
 * <p>
 * Everything the framework owns is left alone: the {@code service.} and
 * {@code component.} properties Configuration Admin and DS add themselves,
 * {@code objectClass}, the {@code felix.} and {@code osgi.} namespaces, keys
 * private by convention ({@code .}-prefixed) or internal to the configurator
 * ({@code :}-prefixed).
 * </p>
 *
 * <p>
 * Reference properties are left alone too - anything shaped like
 * {@code <reference>.target} or {@code <reference>.cardinality.minimum}, the two
 * the specification defines - because a component cannot enumerate its own
 * reference names at runtime: the DS annotations are compiled with {@code CLASS} retention and are gone
 * by then. So a misspelled <em>reference</em> name is not caught here, only a
 * misspelled configuration attribute; catching the former would mean maintaining a
 * second copy of every {@code @Reference} name by hand, which is its own drift.
 * </p>
 *
 * <h2>Properties a component publishes on purpose</h2>
 *
 * <p>
 * A configuration may carry a key the component never reads, because Declarative
 * Services propagates it onto the registered service for others to target - as
 * {@code "registry": "main"} is set on a Lucene registry and matched by
 * {@code registry.target=(registry=main)}. Such a key is not a mistake, so name it
 * in {@code alsoRecognised}. The alternative, often the better one, is to declare it
 * in the configuration annotation: then it is recognised here, appears in the
 * metatype, and the component can read the value rather than rely on propagation.
 * </p>
 */
public final class UnrecognisedProperties {

    /** Namespaces owned by the framework, Configuration Admin, DS or an implementation. */
    private static final List<String> FRAMEWORK_PREFIXES = List.of("service.", "component.", "felix.", "osgi.", ".",
            ":");

    /**
     * Suffixes of the reference properties Declarative Services reads: the target
     * filter and the minimum cardinality, and those two only (112.6.2). There is no
     * {@code .cardinality.maximum} - a reference's multiplicity is fixed by its
     * declaration and no configuration can raise it - so a key by that name is
     * reported like any other that does nothing.
     */
    private static final List<String> REFERENCE_SUFFIXES = List.of(".target", ".cardinality.minimum");

    /** How far a key may be from a declared id and still be reported as a likely near miss. */
    private static final int SUGGESTION_MAX_DISTANCE = 3;

    private UnrecognisedProperties() {
        // static use only
    }

    /**
     * Logs a warning per unrecognised property, at {@link Level#WARNING}.
     *
     * @param logger     the component's logger
     * @param subject    how the component identifies itself in the message, e.g.
     *                   {@code ScopeService[jena]}
     * @param ocd        the configuration annotation whose attributes are the
     *                   recognised ids
     * @param properties the component properties as delivered by Declarative
     *                   Services
     */
    public static void warn(Logger logger, String subject, Class<? extends Annotation> ocd,
            Map<String, Object> properties) {
        warn(logger, subject, ocd, properties, Set.of());
    }

    /**
     * Logs a warning per unrecognised property, at {@link Level#WARNING}, accepting
     * {@code alsoRecognised} as known.
     *
     * @param logger          the component's logger
     * @param subject         how the component identifies itself in the message
     * @param ocd             the configuration annotation whose attributes are the
     *                        recognised ids
     * @param properties      the component properties as delivered by Declarative
     *                        Services
     * @param alsoRecognised  keys the component accepts without declaring them, such
     *                        as service properties it publishes for others to target
     */
    public static void warn(Logger logger, String subject, Class<? extends Annotation> ocd,
            Map<String, Object> properties, Set<String> alsoRecognised) {
        if (!logger.isLoggable(Level.WARNING)) {
            return;
        }
        report(subject, ocd, properties, alsoRecognised).forEach(finding -> logger.log(Level.WARNING, finding));
    }

    /**
     * The findings, one message per unrecognised property, for a caller that logs
     * them itself.
     *
     * @param subject    how the component identifies itself in the message
     * @param ocd        the configuration annotation whose attributes are the
     *                   recognised ids
     * @param properties the component properties as delivered by Declarative
     *                   Services
     * @return the messages, in key order; empty when every property is recognised
     */
    public static List<String> report(String subject, Class<? extends Annotation> ocd,
            Map<String, Object> properties) {
        return report(subject, ocd, properties, Set.of());
    }

    /**
     * The findings, one message per unrecognised property, for a caller that logs
     * them itself.
     *
     * @param subject        how the component identifies itself in the message
     * @param ocd            the configuration annotation whose attributes are the
     *                       recognised ids
     * @param properties     the component properties as delivered by Declarative
     *                       Services
     * @param alsoRecognised keys the component accepts without declaring them
     * @return the messages, in key order; empty when every property is recognised
     */
    public static List<String> report(String subject, Class<? extends Annotation> ocd, Map<String, Object> properties,
            Set<String> alsoRecognised) {
        if (properties == null || properties.isEmpty()) {
            return List.of();
        }
        Set<String> declared = declaredIds(ocd);
        Set<String> accepted = alsoRecognised == null ? Set.of() : alsoRecognised;
        return properties.keySet().stream()
                .filter(key -> key != null && !declared.contains(key) && !accepted.contains(key))
                .filter(UnrecognisedProperties::isConfigurable)
                .sorted()
                .map(key -> message(subject, key, declared))
                .toList();
    }

    /**
     * The property ids {@code ocd} declares: one per attribute method, mangled the
     * way DS maps a component property type's method names, and prefixed with the
     * value of a {@code PREFIX_} constant when the annotation declares one.
     *
     * @param ocd a configuration annotation
     * @return the recognised property ids
     */
    public static Set<String> declaredIds(Class<? extends Annotation> ocd) {
        String prefix = prefix(ocd);
        return Arrays.stream(ocd.getDeclaredMethods())
                .filter(method -> method.getParameterCount() == 0)
                .map(Method::getName)
                .map(UnrecognisedProperties::idOf)
                .map(id -> prefix + id)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * The property id for an attribute method name, per OSGi Compendium
     * 707.4.4.4.1.
     */
    static String idOf(String methodName) {
        StringBuilder id = new StringBuilder(methodName.length());
        for (int i = 0; i < methodName.length(); i++) {
            char character = methodName.charAt(i);
            if (character == '$') {
                if (isNext(methodName, i, "_$")) {
                    id.append('-');
                    i += 2;
                } else if (isNext(methodName, i, "$")) {
                    id.append('$');
                    i++;
                }
                // a lone dollar sign is removed
            } else if (character == '_') {
                if (isNext(methodName, i, "_")) {
                    id.append('_');
                    i++;
                } else {
                    id.append('.');
                }
            } else {
                id.append(character);
            }
        }
        return id.toString();
    }

    private static boolean isNext(String name, int index, String expected) {
        return name.regionMatches(index + 1, expected, 0, expected.length());
    }

    /** The value of a {@code public static final String PREFIX_} constant, or {@code ""}. */
    private static String prefix(Class<? extends Annotation> ocd) {
        for (Field field : ocd.getDeclaredFields()) {
            if ("PREFIX_".equals(field.getName()) && field.getType() == String.class
                    && Modifier.isStatic(field.getModifiers())) {
                try {
                    return String.valueOf(field.get(null));
                } catch (IllegalAccessException e) {
                    return "";
                }
            }
        }
        return "";
    }

    /**
     * Whether {@code key} is one a configuration author is answerable for, as opposed
     * to one the framework adds or Declarative Services reads for a reference.
     */
    private static boolean isConfigurable(String key) {
        return FRAMEWORK_PREFIXES.stream().noneMatch(key::startsWith)
                && REFERENCE_SUFFIXES.stream().noneMatch(key::endsWith)
                && !"objectClass".equals(key);
    }

    private static String message(String subject, String key, Set<String> declared) {
        String message = String.format("%s: unrecognised configuration property '%s' - ignored.", subject, key);
        return suggestionFor(key, declared).map(hint -> message + String.format(" Did you mean '%s'?", hint))
                .orElse(message);
    }

    /**
     * The declared id {@code key} most likely meant: one built from the same
     * dot-separated segments in another order - the shape of the {@code parent.scope}
     * mistake - or failing that the nearest one within
     * {@link #SUGGESTION_MAX_DISTANCE} edits, which is where a plain typo lands.
     */
    static Optional<String> suggestionFor(String key, Set<String> declared) {
        Optional<String> reordered = declared.stream().filter(id -> segments(id).equals(segments(key))).findFirst();
        if (reordered.isPresent()) {
            return reordered;
        }
        return declared.stream()
                .map(id -> Map.entry(id, distance(key, id)))
                .filter(candidate -> candidate.getValue() <= Math.min(SUGGESTION_MAX_DISTANCE, key.length() / 2))
                .min(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey);
    }

    private static List<String> segments(String key) {
        return Arrays.stream(key.split("\\.")).sorted().toList();
    }

    /** Levenshtein distance, two rows at a time. */
    private static int distance(String left, String right) {
        int[] previous = new int[right.length() + 1];
        int[] current = new int[right.length() + 1];
        for (int j = 0; j <= right.length(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= left.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= right.length(); j++) {
                int substitution = previous[j - 1] + (left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1);
                current[j] = Math.min(substitution, Math.min(previous[j] + 1, current[j - 1] + 1));
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[right.length()];
    }
}
