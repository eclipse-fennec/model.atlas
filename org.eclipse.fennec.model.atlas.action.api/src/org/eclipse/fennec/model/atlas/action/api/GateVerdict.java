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

import java.util.Objects;

/**
 * The answer of a {@link StageGate}: the operation may proceed, or it is
 * refused for a reason the caller gets to read.
 *
 * <p>
 * The reason travels to the client unchanged, so it should say what was
 * checked, what failed and, where there is one, what the caller can do about
 * it ("transition the libraries it imports first"). It must not leak internal
 * detail such as storage paths.
 * </p>
 *
 * @param refused {@code true} if the gate vetoes the operation
 * @param reason  why it was refused; {@code null} when it passed
 * @since 1.1
 */
public record GateVerdict(boolean refused, String reason) {

    private static final GateVerdict PASS = new GateVerdict(false, null);

    /**
     * @return the verdict that lets the operation proceed
     */
    public static GateVerdict pass() {
        return PASS;
    }

    /**
     * @param reason why the operation is refused, written for the caller
     * @return a refusing verdict
     */
    public static GateVerdict refuse(String reason) {
        return new GateVerdict(true, Objects.requireNonNull(reason, "a refusal needs a reason"));
    }

    /**
     * @return {@code true} if the operation may proceed
     */
    public boolean passed() {
        return !refused;
    }
}
