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
package org.eclipse.fennec.model.atlas.healthcheck.tests;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * A scope component that can never come up: its registry reference targets nothing.
 *
 * <p>
 * Disabled by default, so it only exists while a test enables it — exactly the silent DS failure a
 * readiness probe has to catch: configured, unsatisfied, and absent from every service collector.
 * </p>
 */
@Component(name = UnsatisfiedScopeComponent.NAME, enabled = false, service = ReadableScopeService.class,
        property = "scope.name=" + UnsatisfiedScopeComponent.SCOPE)
public class UnsatisfiedScopeComponent implements ReadableScopeService<EObject> {

    static final String NAME = "UnsatisfiedScopeComponent";

    static final String SCOPE = "neverLiveScope";

    @Reference(name = "registryService", target = "(scope=no-inject)")
    Object registryService;

    @Override
    public String getScopeName() {
        return SCOPE;
    }

    @Override
    public boolean isInheritingFromParentScope() {
        return false;
    }

    @Override
    public Optional<EObject> get(String registry, String objectId) {
        return Optional.empty();
    }

    @Override
    public List<String> listObjectIds(String registry) {
        return List.of();
    }

    @Override
    public List<EObject> listAll(String registry) {
        return List.of();
    }

    @Override
    public Stream<EObject> stream(String registry) {
        return Stream.empty();
    }

    @Override
    public ScopeInfo getScopeInfo() {
        return null;
    }

    @Override
    public ReadableRegistryView<EObject> registryView(String registry) {
        return null;
    }

    @Override
    public ReadableRegistryView<EObject> registryView(String registry, String stage) {
        return null;
    }
}
