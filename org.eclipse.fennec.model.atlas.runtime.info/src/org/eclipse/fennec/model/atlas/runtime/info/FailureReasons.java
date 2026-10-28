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
package org.eclipse.fennec.model.atlas.runtime.info;

import java.util.List;

/**
 * Why something the configuration asked for is not there.
 *
 * <p>
 * Strings rather than an enum, because a DTO travels and an enum constant that a reader does not
 * know is worse than a string it can still log. A reader that does not recognise a reason reports
 * it verbatim; a reader that does can branch on these constants.
 * </p>
 *
 * @since 1.0
 */
public final class FailureReasons {

    private FailureReasons() {
    }

    /**
     * A scope service is configured but its component is not satisfied — an unsatisfied mandatory
     * reference, most often a schema package or a registry that never arrived.
     */
    public static final String SCOPE_SERVICE_UNSATISFIED = "SCOPE_SERVICE_UNSATISFIED";

    /**
     * The scope activated but bound no registries at all: its {@code registryService.target} matches
     * nothing. A scope that never had the target set keeps the {@code (scope=no-inject)} default,
     * which matches nothing on purpose and is easy to leave behind.
     */
    public static final String NO_REGISTRIES_BOUND = "NO_REGISTRIES_BOUND";

    /** A registry has no storage service bound. Which backend it wanted is not reported. */
    public static final String STORAGE_UNAVAILABLE = "STORAGE_UNAVAILABLE";

    /**
     * More than one registry of type {@code SCHEMA} in a scope. A scope has at most one, because
     * that is what resolving schemas by type rather than by name depends on.
     */
    public static final String DUPLICATE_SCHEMA_REGISTRY = "DUPLICATE_SCHEMA_REGISTRY";

    /** The scope names a parent scope that is not registered. */
    public static final String PARENT_SCOPE_UNRESOLVED = "PARENT_SCOPE_UNRESOLVED";

    /** A {@code *.target} property that is not a valid LDAP filter. */
    public static final String INVALID_FILTER = "INVALID_FILTER";

    /**
     * A configuration carries a key no component declares. DS drops an unrecognised property in
     * silence, so a misspelling sits in a shipped configuration doing nothing.
     */
    public static final String UNRECOGNISED_PROPERTY = "UNRECOGNISED_PROPERTY";

    /** A stored schema that did not become a live EPackage service. */
    public static final String PACKAGE_REGISTRATION_FAILED = "PACKAGE_REGISTRATION_FAILED";

    /**
     * Every reason, so a reader can account for the ones that are currently clear.
     *
     * <p>
     * A metric that reports only the failures it found leaves a gap in the series where a reason
     * stopped occurring, and a gap reads the same as an exporter that stopped. A reader that
     * iterates this reports a zero instead.
     * </p>
     */
    public static final List<String> ALL = List.of(SCOPE_SERVICE_UNSATISFIED, NO_REGISTRIES_BOUND,
            STORAGE_UNAVAILABLE, DUPLICATE_SCHEMA_REGISTRY, PARENT_SCOPE_UNRESOLVED, INVALID_FILTER,
            UNRECOGNISED_PROPERTY, PACKAGE_REGISTRATION_FAILED);
}
