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
package org.eclipse.fennec.model.atlas.action.api;

/**
 * How serious a {@link GateDiagnostic} is. The literals mirror the persisted
 * diagnostic model of the Atlas, so a finding keeps its weight when the
 * workflow records it on the object's metadata.
 *
 * @since 1.2
 */
public enum GateSeverity {

    /** Worth knowing, nothing to fix. */
    INFO,

    /** Something to look at; on its own it does not refuse an operation. */
    WARNING,

    /** A finding that justifies a refusal. */
    ERROR
}
