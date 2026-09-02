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
package org.eclipse.fennec.model.atlas.qvt;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.m2x.model.compiled.CompiledPackage;
import org.eclipse.fennec.m2x.model.compiled.CompiledUnit;
import org.eclipse.fennec.m2x.model.compiled.SourceUnit;
import org.eclipse.fennec.m2x.unit.api.Unit;
import org.eclipse.fennec.m2x.unit.api.UnitFingerprintService;
import org.eclipse.fennec.m2x.unit.api.UnitKey;
import org.eclipse.fennec.m2x.unit.api.UnitKind;
import org.eclipse.fennec.m2x.unit.api.UnitStore;
import org.eclipse.fennec.m2x.unit.api.UnitStoreException;
import org.eclipse.fennec.m2x.unit.fingerprint.DefaultUnitFingerprintService;
import org.eclipse.fennec.m2x.unit.store.PackagedUnit;
import org.eclipse.fennec.m2x.unit.store.StoredSource;
import org.eclipse.fennec.m2x.unit.store.UnitDocuments;
import org.eclipse.fennec.m2x.unit.store.UnitXmi;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.osgi.util.promise.Promise;

/**
 * The m2x {@link UnitStore} contract over one (scope, stage) view of the Atlas
 * transformation registry: dumb key ↔ document, exactly like the emf.m2x
 * object medium ({@code RegistryUnitStore}, #213), but carried by the Atlas
 * workflow registry instead of an emf.osgi {@code EObjectRegistry} — the Atlas
 * is the provider side that feeds client registries.
 *
 * <p>
 * Two id shapes share the registry:
 * </p>
 * <ul>
 * <li><b>Canonical entries</b> — compiled units (and sources stored through
 * {@link #put(String, Unit.Source)}): objectId = URL-encoded
 * {@code <language>/<kind>/<qualifiedName>/<fingerprint>}
 * ({@link QvtUnits#objectId(UnitKey)}). Written by the Atlas itself.</li>
 * <li><b>Working-copy sources</b> — {@code SourceUnit} documents uploaded by a
 * client under an id of its own choosing (by convention the qualified name):
 * recognized by their object type, addressed by the qualified name and
 * fingerprint <em>inside</em> the document. This is what an editor round-trips;
 * a client cannot know a fingerprint before uploading.</li>
 * </ul>
 *
 * <p>
 * Documents are sealed and normalized into transport state with the shared m2x
 * helpers ({@link UnitDocuments}, {@link UnitXmi}) on the way in; what comes
 * out is a fresh instance loaded from storage, so a caller's mutation never
 * reaches the stored form. Diagnostics documents use a pseudo-kind outside the
 * {@code UnitKey} scheme and are filtered out of every answer.
 * </p>
 */
public class AtlasUnitStore implements UnitStore {

    private static final String SOURCE_UNIT_TYPE = EcoreUtil.getURI(CompiledPackage.Literals.SOURCE_UNIT).toString();

    private final RegistryService<EObject> registryService;
    private final String scope;
    private final String stage;
    private final UnitFingerprintService fingerprints = new DefaultUnitFingerprintService();

    public AtlasUnitStore(RegistryService<EObject> registryService, String scope, String stage) {
        this.registryService = Objects.requireNonNull(registryService, "registryService must not be null");
        this.scope = Objects.requireNonNull(scope, "scope must not be null");
        this.stage = Objects.requireNonNull(stage, "stage must not be null");
    }

    public String scope() {
        return scope;
    }

    public String stage() {
        return stage;
    }

    @Override
    public UnitKey put(CompiledUnit document) throws UnitStoreException {
        UnitDocuments.Sealed sealed = UnitDocuments.seal(document, fingerprints);
        // one XMI round trip: a document whose references were bound in the
        // producer's context arrives in storage with them unresolved, exactly as
        // the byte medium delivers (see RegistryUnitStore)
        EObject detached = UnitXmi.read(UnitXmi.write(sealed.document(), sealed.key()), sealed.key());
        store(sealed.key(), detached);
        return sealed.key();
    }

    @Override
    public UnitKey put(String language, Unit.Source source) throws UnitStoreException {
        UnitDocuments.Sealed sealed = UnitDocuments.sourceForm(language, source, fingerprints);
        store(sealed.key(), sealed.document());
        return sealed.key();
    }

    @Override
    public Optional<Unit> get(UnitKey key) throws UnitStoreException {
        Optional<Entry> entry = resolve(key);
        if (entry.isEmpty()) {
            if (key.fingerprint().isPresent()) {
                List<UnitKey> versions = versions(key.language(), key.qualifiedName(), key.kind());
                if (!versions.isEmpty()) {
                    throw new UnitStoreException("the store has '" + key.qualifiedName() + "' (" + key.kind().tag()
                            + ") but not with fingerprint " + key.fingerprint().get() + "; it has: "
                            + versions.stream().map(v -> v.fingerprint().orElse("?"))
                                    .collect(Collectors.joining(", ")));
                }
            }
            return Optional.empty();
        }
        EObject content = contentOfId(entry.get().objectId());
        if (content == null) {
            return Optional.empty();
        }
        return Optional.of(asUnit(entry.get().key(), content));
    }

    @Override
    public boolean contains(UnitKey key) throws UnitStoreException {
        return resolve(key).isPresent();
    }

    @Override
    public List<UnitKey> versions(String language, String qualifiedName, UnitKind kind) throws UnitStoreException {
        return entries().stream()
                .map(Entry::key)
                .filter(key -> key.kind() == kind && key.language().equals(language)
                        && key.qualifiedName().equals(qualifiedName))
                .collect(Collectors.toList());
    }

    /**
     * Every unit key in this (scope, stage) view — sources and compiled units,
     * diagnostics and foreign entries filtered out — newest first. Atlas-side
     * extension used by the dependent-recompile cascade; not part of the m2x
     * {@link UnitStore} contract.
     */
    public List<UnitKey> allKeysNewestFirst() throws UnitStoreException {
        return entries().stream().map(Entry::key).collect(Collectors.toList());
    }

    @Override
    public boolean remove(UnitKey key) throws UnitStoreException {
        if (key.fingerprint().isPresent()) {
            Optional<Entry> entry = resolve(key);
            if (entry.isEmpty()) {
                return false;
            }
            return await(registryService.deleteFromStage(scope, stage, entry.get().objectId()),
                    "remove " + QvtUnits.entryKey(key)) == Boolean.TRUE;
        }
        boolean removed = false;
        for (UnitKey version : versions(key.language(), key.qualifiedName(), key.kind())) {
            removed |= remove(version);
        }
        return removed;
    }

    private record Entry(String objectId, UnitKey key, Instant at) {
    }

    /** The entry a key denotes: pinned → exact match, unpinned → the newest version. */
    private Optional<Entry> resolve(UnitKey key) throws UnitStoreException {
        // fast path: canonical compiled entries are addressable without a scan
        if (key.kind() == UnitKind.COMPILED && key.fingerprint().isPresent()) {
            String objectId = QvtUnits.objectId(key);
            if (metadataOfId(objectId) != null) {
                return Optional.of(new Entry(objectId, key, Instant.EPOCH));
            }
        }
        return entries().stream()
                .filter(entry -> entry.key().kind() == key.kind()
                        && entry.key().language().equals(key.language())
                        && entry.key().qualifiedName().equals(key.qualifiedName())
                        && key.fingerprint().map(f -> f.equals(entry.key().fingerprint().orElse(null))).orElse(true))
                .findFirst();
    }

    /**
     * All unit entries of this view, newest first: canonical ids decoded
     * directly; working-copy sources recognized by object type and keyed by the
     * qualified name and fingerprint inside the document (computing the
     * fingerprint when the document carries none). Loads every working-copy
     * source once per call — acceptable for registry-sized content; revisit
     * with indexed metadata properties if it ever isn't.
     */
    private List<Entry> entries() throws UnitStoreException {
        List<Entry> found = new ArrayList<>();
        for (ObjectMetadata metadata : listSafely()) {
            Instant at = metadata.getLastChangeTime() != null ? metadata.getLastChangeTime()
                    : metadata.getUploadTime() != null ? metadata.getUploadTime() : Instant.EPOCH;
            Optional<UnitKey> canonical = QvtUnits.parseObjectId(metadata.getObjectId());
            if (canonical.isPresent()) {
                found.add(new Entry(metadata.getObjectId(), canonical.get(), at));
                continue;
            }
            if (!SOURCE_UNIT_TYPE.equals(metadata.getObjectType())) {
                continue;
            }
            EObject content = contentOfId(metadata.getObjectId());
            if (content instanceof SourceUnit source && source.getQualifiedName() != null
                    && source.getSource() != null) {
                String language = source.getLanguage() == null || source.getLanguage().isBlank()
                        ? QvtUnits.LANGUAGE_QVTO
                        : source.getLanguage();
                String fingerprint = source.getFingerprint() != null && !source.getFingerprint().isBlank()
                        ? source.getFingerprint()
                        : fingerprints.fingerprint(asSource(source));
                found.add(new Entry(metadata.getObjectId(),
                        UnitKey.pinned(language, source.getQualifiedName(), UnitKind.SOURCE, fingerprint), at));
            }
        }
        found.sort(Comparator.comparing(Entry::at).reversed());
        return found;
    }

    private void store(UnitKey key, EObject document) throws UnitStoreException {
        // same key, new content is possible when the same source recompiles against
        // a changed package view (the unit fingerprint does not fold in package
        // fingerprints) — the newer manifest replaces the older one
        upsert(registryService, scope, stage, QvtUnits.objectId(key), key.qualifiedName(), document);
    }

    /**
     * Create-or-replace of one Atlas-written document (a unit, a diagnostics
     * document) in a (scope, stage) view — the shared write path of the store
     * and the compile action.
     */
    public static void upsert(RegistryService<EObject> registryService, String scope, String stage, String objectId,
            String objectName, EObject document) throws UnitStoreException {
        String operation = "store " + objectId + " in (" + scope + ", " + registryService.getRegistryName() + ", "
                + stage + ")";
        ObjectMetadata existing;
        try {
            existing = registryService.getMetadataFromStage(scope, stage, objectId);
        } catch (RuntimeException e) {
            throw new UnitStoreException("cannot reach the transformation registry to " + operation, e);
        }
        try {
            if (existing != null) {
                registryService.updateInStage(scope, stage, document, objectId, existing.getVersion()).getValue();
                return;
            }
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId(objectId);
            metadata.setObjectName(objectName);
            metadata.setUploadTime(Instant.now());
            metadata.setObjectType(EcoreUtil.getURI(document.eClass()).toString());
            registryService.uploadToStage(scope, stage, document, metadata).getValue();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UnitStoreException("interrupted while trying to " + operation, e);
        } catch (Exception e) {
            throw new UnitStoreException("failed to " + operation, e);
        }
    }

    private ObjectMetadata metadataOfId(String objectId) throws UnitStoreException {
        try {
            return registryService.getMetadataFromStage(scope, stage, objectId);
        } catch (RuntimeException e) {
            throw new UnitStoreException("cannot reach the transformation registry for " + describe(), e);
        }
    }

    private EObject contentOfId(String objectId) throws UnitStoreException {
        try {
            return registryService.getContentFromStage(scope, stage, objectId);
        } catch (RuntimeException e) {
            throw new UnitStoreException("cannot load " + objectId + " from " + describe(), e);
        }
    }

    private Unit asUnit(UnitKey key, EObject content) throws UnitStoreException {
        if (key.kind() == UnitKind.COMPILED) {
            if (!(content instanceof CompiledUnit compiled)) {
                throw new UnitStoreException("the entry " + QvtUnits.entryKey(key) + " holds a "
                        + content.eClass().getName() + ", not a CompiledUnit document");
            }
            return new PackagedUnit(compiled);
        }
        if (!(content instanceof SourceUnit source)) {
            throw new UnitStoreException("the entry " + QvtUnits.entryKey(key) + " holds a "
                    + content.eClass().getName() + ", not a SourceUnit document");
        }
        return asSource(source);
    }

    private static StoredSource asSource(SourceUnit source) {
        return new StoredSource(source.getQualifiedName(),
                URI.createURI(source.getUri() != null ? source.getUri() : "atlas:/" + source.getQualifiedName()),
                source.getSource());
    }

    private List<ObjectMetadata> listSafely() throws UnitStoreException {
        try {
            return registryService.listInStage(scope, stage);
        } catch (RuntimeException e) {
            throw new UnitStoreException("cannot list the transformation registry for " + describe(), e);
        }
    }

    private <R> R await(Promise<R> promise, String operation) throws UnitStoreException {
        try {
            return promise.getValue();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UnitStoreException("interrupted while trying to " + operation + " in " + describe(), e);
        } catch (Exception e) {
            throw new UnitStoreException("failed to " + operation + " in " + describe(), e);
        }
    }

    private String describe() {
        return "(" + scope + ", " + registryService.getRegistryName() + ", " + stage + ")";
    }
}
