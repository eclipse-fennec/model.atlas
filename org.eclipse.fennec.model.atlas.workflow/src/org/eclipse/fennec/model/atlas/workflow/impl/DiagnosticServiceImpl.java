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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.mgmt.diagnostics.DiagnosticAddress;
import org.eclipse.fennec.model.atlas.mgmt.diagnostics.DiagnosticEdit;
import org.eclipse.fennec.model.atlas.mgmt.diagnostics.DiagnosticNotFoundException;
import org.eclipse.fennec.model.atlas.mgmt.diagnostics.DiagnosticService;
import org.eclipse.fennec.model.atlas.mgmt.diagnostics.DiagnosticVersionConflictException;
import org.eclipse.fennec.model.atlas.mgmt.diagnostics.Diagnostics;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticStatus;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.workflow.RegistryServiceCollector;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * {@link DiagnosticService} on top of the registries (issue #293).
 *
 * <p>
 * Every operation reads the object's metadata, works on a copy of the roots the addressed
 * diagnostic's producer holds, checks the version, applies the change with a history entry
 * and a version bump, and writes the producer's roots back through
 * {@code RegistryService.updateDiagnostics}. That is the one write path for diagnostics, so
 * the {@code DiagnosticsChanged} event goes out from there like for every other write, and
 * the re-validation rule of {@code Diagnostics.replaceOwned} sees the bumped version and
 * takes the change as it is.
 * </p>
 *
 * <p>
 * Read, decide and write are three steps, so two changes to the same object could
 * interleave between them. The operations on one object are serialised on a lock per
 * address, which turns the version check into what it promises within this runtime: the
 * second of two changes that decided about the same version is refused. Across runtimes,
 * or against a producer's re-validation, the version still travels with the diagnostic and
 * the conflict is still detected on the next informed change.
 * </p>
 */
@Component(name = "DiagnosticService", immediate = true)
public class DiagnosticServiceImpl implements DiagnosticService {

    @Reference
    private RegistryServiceCollector registryCollector;

    private final PromiseFactory promiseFactory = new PromiseFactory(null);
    private final Map<DiagnosticAddress, Object> locks = new ConcurrentHashMap<>();

    @Override
    public Promise<Diagnostic> add(DiagnosticAddress at, String producer, Diagnostic diagnostic, String changedBy,
            String reason) {
        requireNonNull(producer, "producer");
        requireNonNull(diagnostic, "diagnostic");
        requireNonNull(changedBy, "changedBy");
        return promiseFactory.submit(() -> locked(at, () -> {
            RegistryService<EObject> registry = registryFor(at);
            ObjectMetadata metadata = metadataOf(registry, at);
            Instant now = Instant.now();
            Diagnostic added = EcoreUtil.copy(diagnostic);
            Diagnostics.prepare(producer, List.of(added), now);
            if (Diagnostics.find(metadata, added.getId()) != null) {
                throw new IllegalArgumentException(String.format(
                        "Diagnostic %s already exists on %s; change it instead of adding it again", added.getId(),
                        at));
            }
            added.setVersion(1);
            added.setLastChangeTime(now);
            added.getHistory().add(entry(now, changedBy, null, added.getSeverity(), null, added.getStatus(),
                    reason == null ? "added" : reason));
            List<Diagnostic> roots = ownedRoots(metadata, producer);
            roots.add(added);
            ObjectMetadata written = write(registry, at, producer, roots);
            return Diagnostics.find(written, added.getId());
        }));
    }

    @Override
    public Promise<Diagnostic> update(DiagnosticAddress at, String diagnosticId, long expectedVersion,
            DiagnosticEdit edit) {
        requireNonNull(edit, "edit");
        return change(at, diagnosticId, expectedVersion, edit.changedBy(), edit.reason(), target -> {
            if (edit.severity() != null) {
                target.setSeverity(edit.severity());
            }
            if (edit.status() != null) {
                target.setStatus(edit.status());
            }
            if (edit.message() != null) {
                target.setMessage(edit.message());
            }
        });
    }

    @Override
    public Promise<Diagnostic> escalate(DiagnosticAddress at, String diagnosticId, long expectedVersion,
            DiagnosticSeverity severity, String changedBy, String reason) {
        requireNonNull(severity, "severity");
        return change(at, diagnosticId, expectedVersion, changedBy, reason, target -> {
            if (severity.getValue() <= target.getSeverity().getValue()) {
                throw new IllegalArgumentException(String.format(
                        "Escalating diagnostic %s on %s from %s to %s does not raise it; use update to lower a severity",
                        diagnosticId, at, target.getSeverity(), severity));
            }
            target.setSeverity(severity);
        });
    }

    @Override
    public Promise<Boolean> remove(DiagnosticAddress at, String diagnosticId, long expectedVersion, String changedBy,
            String reason) {
        requireNonNull(changedBy, "changedBy");
        return promiseFactory.submit(() -> locked(at, () -> {
            RegistryService<EObject> registry = registryFor(at);
            ObjectMetadata metadata = metadataOf(registry, at);
            Diagnostic target = targetOf(metadata, at, diagnosticId, expectedVersion);
            String producer = Diagnostics.rootOf(target).getProducer();
            List<Diagnostic> roots = ownedRoots(metadata, producer);
            Diagnostic copy = findIn(roots, diagnosticId);
            EcoreUtil.remove(copy);
            roots.remove(copy);
            write(registry, at, producer, roots);
            return Boolean.TRUE;
        }));
    }

    /**
     * The common shape of every by-id change: locate, check the version, apply, record,
     * write back, return the diagnostic as stored.
     */
    private Promise<Diagnostic> change(DiagnosticAddress at, String diagnosticId, long expectedVersion,
            String changedBy, String reason, java.util.function.Consumer<Diagnostic> apply) {
        requireNonNull(changedBy, "changedBy");
        return promiseFactory.submit(() -> locked(at, () -> {
            RegistryService<EObject> registry = registryFor(at);
            ObjectMetadata metadata = metadataOf(registry, at);
            Diagnostic current = targetOf(metadata, at, diagnosticId, expectedVersion);
            String producer = Diagnostics.rootOf(current).getProducer();
            List<Diagnostic> roots = ownedRoots(metadata, producer);
            Diagnostic target = findIn(roots, diagnosticId);
            DiagnosticSeverity oldSeverity = target.getSeverity();
            DiagnosticStatus oldStatus = target.getStatus();
            apply.accept(target);
            Instant now = Instant.now();
            target.getHistory().add(entry(now, changedBy, oldSeverity, target.getSeverity(), oldStatus,
                    target.getStatus(), reason));
            target.setVersion(target.getVersion() + 1);
            target.setLastChangeTime(now);
            // an edit on a descendant is a change of the root's tree: the root's version
            // is what replaceOwned compares, so it has to move too
            Diagnostic root = Diagnostics.rootOf(target);
            if (root != target) {
                root.setVersion(root.getVersion() + 1);
                root.setLastChangeTime(now);
            }
            ObjectMetadata written = write(registry, at, producer, roots);
            return Diagnostics.find(written, diagnosticId);
        }));
    }

    private static Diagnostic targetOf(ObjectMetadata metadata, DiagnosticAddress at, String diagnosticId,
            long expectedVersion) {
        Diagnostic target = Diagnostics.find(metadata, requireNonNull(diagnosticId, "diagnosticId"));
        if (target == null) {
            throw DiagnosticNotFoundException.diagnostic(at, diagnosticId);
        }
        if (target.getVersion() != expectedVersion) {
            throw new DiagnosticVersionConflictException(at, diagnosticId, expectedVersion, target.getVersion());
        }
        return target;
    }

    /** Copies of the roots a producer holds, detached from the metadata, safe to edit. */
    private static List<Diagnostic> ownedRoots(ObjectMetadata metadata, String producer) {
        List<Diagnostic> roots = new ArrayList<>();
        for (Diagnostic root : metadata.getDiagnostics()) {
            if (producer.equals(root.getProducer())) {
                roots.add(EcoreUtil.copy(root));
            }
        }
        return roots;
    }

    private static Diagnostic findIn(List<Diagnostic> roots, String id) {
        ObjectMetadata holder = ManagementFactory.eINSTANCE.createObjectMetadata();
        holder.getDiagnostics().addAll(roots);
        Diagnostic found = Diagnostics.find(holder, id);
        // the holder was only a search scope; give the roots their freedom back
        roots.clear();
        roots.addAll(new ArrayList<>(holder.getDiagnostics()));
        holder.getDiagnostics().clear();
        return found;
    }

    private static DiagnosticChange entry(Instant when, String who, DiagnosticSeverity oldSeverity,
            DiagnosticSeverity newSeverity, DiagnosticStatus oldStatus, DiagnosticStatus newStatus, String reason) {
        DiagnosticChange change = ManagementFactory.eINSTANCE.createDiagnosticChange();
        change.setChangeTime(when);
        change.setChangedBy(who);
        change.setOldSeverity(oldSeverity);
        change.setNewSeverity(newSeverity);
        change.setOldStatus(oldStatus);
        change.setNewStatus(newStatus);
        change.setReason(reason);
        return change;
    }

    private ObjectMetadata write(RegistryService<EObject> registry, DiagnosticAddress at, String producer,
            List<Diagnostic> roots) {
        ObjectMetadata written = WorkflowServiceHelper
                .getPromiseValue(registry.updateDiagnostics(at.scope(), at.stage(), at.objectId(), producer, roots));
        if (written == null) {
            throw DiagnosticNotFoundException.object(at);
        }
        return written;
    }

    private static ObjectMetadata metadataOf(RegistryService<EObject> registry, DiagnosticAddress at) {
        ObjectMetadata metadata = registry.getMetadataFromStage(at.scope(), at.stage(), at.objectId());
        if (metadata == null) {
            throw DiagnosticNotFoundException.object(at);
        }
        return metadata;
    }

    @SuppressWarnings("unchecked")
    private RegistryService<EObject> registryFor(DiagnosticAddress at) {
        RegistryService<?> registry = registryCollector.getRegistryServiceByRegistryName(at.registry());
        if (registry == null) {
            throw DiagnosticNotFoundException.object(at);
        }
        return (RegistryService<EObject>) registry;
    }

    private <R> R locked(DiagnosticAddress at, java.util.function.Supplier<R> work) {
        Object lock = locks.computeIfAbsent(at, Function.identity());
        synchronized (lock) {
            return work.get();
        }
    }
}
