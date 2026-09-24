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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.fennec.model.atlas.action.api.GateDiagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;

/**
 * Turns the findings a gate reports into the diagnostics the Atlas persists
 * (issue #294).
 *
 * <p>
 * A {@link GateDiagnostic} is the EMF-free shape of the action SPI; a
 * {@link Diagnostic} is the metadata record. What the gate does not say -
 * producer, id, status, timestamps, history - is left for
 * {@code Diagnostics.replaceOwned} to fill when the tree is written, so the
 * ids come out the same way for a gate as for every other producer.
 * </p>
 */
final class GateDiagnostics {

    private GateDiagnostics() {
    }

    static List<Diagnostic> toModel(List<GateDiagnostic> findings) {
        List<Diagnostic> model = new ArrayList<>(findings.size());
        for (GateDiagnostic finding : findings) {
            model.add(toModel(finding));
        }
        return model;
    }

    static Diagnostic toModel(GateDiagnostic finding) {
        Diagnostic diagnostic = ManagementFactory.eINSTANCE.createDiagnostic();
        diagnostic.setCode(finding.code());
        diagnostic.setMessage(finding.message());
        diagnostic.setSeverity(DiagnosticSeverity.get(finding.severity().name()));
        diagnostic.setCategory(finding.category());
        diagnostic.setTarget(finding.target());
        for (GateDiagnostic child : finding.children()) {
            diagnostic.getChildren().add(toModel(child));
        }
        return diagnostic;
    }
}
