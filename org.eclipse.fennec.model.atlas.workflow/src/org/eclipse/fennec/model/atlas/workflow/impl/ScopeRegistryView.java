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
package org.eclipse.fennec.model.atlas.workflow.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView;

/**
 * A {@link ReadableRegistryView} bound to one {@code (registry, stage)} of the scope
 * service that created it.
 *
 * <p>
 * It owns no state and holds no copy of the registry: every read delegates to the two
 * functions it was built from, so a view answers exactly what the owning scope service
 * answers, including that service's scope-inheritance rules. This mirrors what the
 * remote client does — {@code RemoteReadableScopeService.RemoteReadableRegistryView}
 * delegates to the same service's stage-aware core — so a consumer holding a
 * {@code ReadableScopeService} gets the same behaviour whether the scope is in-process
 * or on the other end of the REST API.
 * </p>
 *
 * <p>
 * A view is a handle, not a snapshot: it is cheap to create and it sees writes that
 * happen after it was created.
 * </p>
 *
 * @param <T> the type of object the registry holds
 * @author Data In Motion
 * @since Aug 6, 2026
 */
final class ScopeRegistryView<T extends EObject> implements ReadableRegistryView<T> {

    private final String scopeName;
    private final String registryName;
    private final String stageName;
    private final Supplier<List<String>> objectIds;
    private final Function<String, T> content;

    /**
     * @param scopeName    the scope this view reads from; never {@code null}
     * @param registryName the registry within that scope; never {@code null}
     * @param stageName    the bound stage, or {@code null} for a final-stage view — the
     *                     contract {@link ReadableRegistryView#getStageName()} documents
     * @param objectIds    supplies the ids visible in this view
     * @param content      resolves one id to its object, or {@code null} if not visible
     */
    ScopeRegistryView(String scopeName, String registryName, String stageName, Supplier<List<String>> objectIds,
            Function<String, T> content) {
        this.scopeName = Objects.requireNonNull(scopeName, "scopeName");
        this.registryName = Objects.requireNonNull(registryName, "registryName");
        this.stageName = stageName;
        this.objectIds = Objects.requireNonNull(objectIds, "objectIds");
        this.content = Objects.requireNonNull(content, "content");
    }

    @Override
    public String getScopeName() {
        return scopeName;
    }

    @Override
    public String getRegistryName() {
        return registryName;
    }

    @Override
    public String getStageName() {
        return stageName;
    }

    @Override
    public Optional<T> get(String objectId) {
        Objects.requireNonNull(objectId, "objectId");
        return Optional.ofNullable(content.apply(objectId));
    }

    @Override
    public List<String> listObjectIds() {
        List<String> ids = objectIds.get();
        return ids == null ? List.of() : List.copyOf(ids);
    }

    @Override
    public List<T> listAll() {
        return stream().toList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Lazy, as the contract asks: content is resolved as the stream is consumed, so a
     * caller that stops early does not pay for the rest of the registry. Ids that no
     * longer resolve — removed between listing and reading — are skipped rather than
     * surfacing as {@code null} elements.
     * </p>
     */
    @Override
    public Stream<T> stream() {
        return listObjectIds().stream().map(content).filter(Objects::nonNull);
    }
}
