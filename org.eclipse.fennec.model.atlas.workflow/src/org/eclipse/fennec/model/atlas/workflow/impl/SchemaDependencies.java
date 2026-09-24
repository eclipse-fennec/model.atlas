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
import java.util.Map;

import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.action.api.GateDiagnostic;
import org.eclipse.fennec.model.atlas.action.api.GateSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.eclipse.fennec.model.atlas.workflow.dependency.SchemaDependent;

/**
 * What the schema dependency guard records and how (issue #250), shared by the
 * {@link SchemaDeleteGate gate} that refuses a delete and the
 * {@link SchemaDependencyStageAction stage action} that keeps the dependents' state current:
 * one producer, so the two write the same diagnostics under the same ids.
 *
 * <ul>
 * <li>{@link #CODE_HAS_DEPENDENTS} on the package whose delete was refused, with one
 * {@link #CODE_DEPENDENT} child per dependent.</li>
 * <li>{@link #CODE_DEPENDENCY_MISSING} on a dependent whose package is no longer served in
 * its view - the <em>unresolved</em> state of decision 4. Its target is the missing package's
 * nsURI, so a dependent missing two packages carries two of them, and the one for a package
 * that comes back is the one that goes.</li>
 * </ul>
 */
final class SchemaDependencies {

    /** The producer both the gate and the stage action record under. */
    static final String PRODUCER = "SchemaDependencies";

    static final String CATEGORY = "dependencies";

    /** ERROR on a package: objects depend on it, the delete was refused. */
    static final String CODE_HAS_DEPENDENTS = "schema.has-dependents";

    /** INFO child of {@link #CODE_HAS_DEPENDENTS}: one dependent. */
    static final String CODE_DEPENDENT = "schema.dependent";

    /** ERROR on a dependent: the package it references is not served in its view. */
    static final String CODE_DEPENDENCY_MISSING = "schema.dependency-missing";

    static final String EPACKAGE_TYPE = EcoreUtil.getURI(EcorePackage.Literals.EPACKAGE).toString();

    private SchemaDependencies() {
    }

    static boolean isEPackage(String objectType) {
        return EPACKAGE_TYPE.equals(objectType);
    }

    /** The {@code nsUri} the registry passed along with the object, or {@code null}. */
    static String nsUriOf(Map<String, Object> contextMetadata) {
        Object value = contextMetadata == null ? null
                : contextMetadata.get(WorkflowConstants.NS_URI_METADATA_PROPERTY);
        return value instanceof String s && !s.isBlank() ? s : null;
    }

    /** The {@code nsUri} property of a schema's metadata, or {@code null}. */
    static String nsUriOf(ObjectMetadata metadata) {
        Object value = metadata == null || metadata.getProperties() == null ? null
                : metadata.getProperties().get(WorkflowConstants.NS_URI_METADATA_PROPERTY);
        return value == null || value.toString().isBlank() ? null : value.toString();
    }

    /** The refusal finding: what depends on the package, one child per dependent. */
    static GateDiagnostic hasDependents(String nsURI, String stage, List<SchemaDependent> dependents) {
        List<GateDiagnostic> children = new ArrayList<>(dependents.size());
        for (SchemaDependent dependent : dependents) {
            children.add(GateDiagnostic.info(CODE_DEPENDENT, String.format("%s %s in stage '%s' of registry '%s'",
                    dependent.kind().name().toLowerCase(), dependent.objectId(), dependent.stage(),
                    dependent.registry())).inCategory(CATEGORY).at(dependent.address()));
        }
        return GateDiagnostic.error(CODE_HAS_DEPENDENTS, hasDependentsMessage(nsURI, stage, dependents.size()))
                .inCategory(CATEGORY).at(nsURI).withChildren(children);
    }

    static String hasDependentsMessage(String nsURI, String stage, int count) {
        return String.format(
                "%d object%s in the view of stage '%s' depend%s on package %s; delete or move them first, or force the delete",
                count, count == 1 ? "" : "s", stage, count == 1 ? "s" : "", nsURI);
    }

    /** The unresolved state, as a gate names it on a {@code Dependent}. */
    static GateDiagnostic missingFinding(String nsURI, String stage) {
        return GateDiagnostic.error(CODE_DEPENDENCY_MISSING, missingMessage(nsURI, stage)).inCategory(CATEGORY)
                .at(nsURI);
    }

    static String missingMessage(String nsURI, String stage) {
        return String.format("depends on package %s, which is no longer served from stage '%s' of the schema registry",
                nsURI, stage);
    }

    /** The unresolved state, as the stage action writes it. */
    static Diagnostic missing(String nsURI, String stage) {
        return GateDiagnostics.toModel(missingFinding(nsURI, stage));
    }

    /**
     * The roots this producer already holds on {@code metadata}, except the one about
     * {@code exceptNsURI}, as fresh copies a producer can re-emit: the write replaces the
     * producer's whole set, so what is not re-emitted is gone.
     */
    static List<Diagnostic> ownedExcept(ObjectMetadata metadata, String exceptNsURI) {
        List<Diagnostic> kept = new ArrayList<>();
        if (metadata == null) {
            return kept;
        }
        for (Diagnostic root : metadata.getDiagnostics()) {
            if (PRODUCER.equals(root.getProducer()) && !exceptNsURI.equals(root.getTarget())) {
                kept.add(EcoreUtil.copy(root));
            }
        }
        return kept;
    }

    /** Whether this producer holds a root about {@code nsURI} on {@code metadata}. */
    static boolean holdsMissing(ObjectMetadata metadata, String nsURI) {
        return metadata != null && metadata.getDiagnostics().stream().anyMatch(root -> PRODUCER
                .equals(root.getProducer()) && CODE_DEPENDENCY_MISSING.equals(root.getCode())
                && nsURI.equals(root.getTarget()));
    }

    /** A recorded root in the shape a gate verdict carries it, so a gate can re-emit it. */
    static GateDiagnostic toFinding(Diagnostic diagnostic) {
        List<GateDiagnostic> children = new ArrayList<>();
        for (Diagnostic child : diagnostic.getChildren()) {
            children.add(toFinding(child));
        }
        GateSeverity severity = diagnostic.getSeverity() == null ? GateSeverity.ERROR
                : GateSeverity.valueOf(diagnostic.getSeverity().name());
        return new GateDiagnostic(severity, diagnostic.getCode(), diagnostic.getMessage(), diagnostic.getCategory(),
                diagnostic.getTarget(), null, children);
    }
}
