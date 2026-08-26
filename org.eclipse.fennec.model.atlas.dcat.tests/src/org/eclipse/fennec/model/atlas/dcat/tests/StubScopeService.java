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
package org.eclipse.fennec.model.atlas.dcat.tests;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.eclipse.fennec.model.atlas.scope.api.RegistryInfo;
import org.eclipse.fennec.model.atlas.scope.api.ScopeApiFactory;
import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;
import org.eclipse.fennec.model.atlas.scope.api.StageInfo;
import org.osgi.service.component.annotations.Component;

/**
 * A scope for the publisher to see, without dragging the whole workflow stack into the test
 * runtime.
 *
 * <p>
 * The publisher's contract with the scope side is narrow — a service carrying {@code atlas.scope}
 * that can answer {@link #getScopeInfo()} — so standing in for it here keeps the IT about the
 * thing under test: the publisher, the real client and a real portal. Everything the atlas does
 * to <em>produce</em> a scope is covered by the workflow tests.
 * </p>
 */
@Component(service = ReadableScopeService.class, property = { "atlas.scope=" + StubScopeService.SCOPE })
public class StubScopeService implements ReadableScopeService<EObject> {

    /** The scope name the IT publishes. */
    public static final String SCOPE = "itscope";

    /** The description the portal should end up storing. */
    public static final String DESCRIPTION = "Integration test scope";

    @Override
    public String getScopeName() {
        return SCOPE;
    }

    @Override
    public ScopeInfo getScopeInfo() {
        ScopeInfo info = ScopeApiFactory.eINSTANCE.createScopeInfo();
        info.setName(SCOPE);
        info.setDescription(DESCRIPTION);
        // The publisher resolves publish.stages=FINAL against these, never against a hardcoded
        // stage name: a scope may call its final stage anything it likes.
        RegistryInfo registry = ScopeApiFactory.eINSTANCE.createRegistryInfo();
        registry.setName("schema");
        registry.getStages().add(stage("draft", false));
        registry.getStages().add(stage("release", true));
        info.getRegistries().add(registry);
        return info;
    }

    private static StageInfo stage(String name, boolean isFinal) {
        StageInfo stage = ScopeApiFactory.eINSTANCE.createStageInfo();
        stage.setName(name);
        stage.setFinal(isFinal);
        stage.setReadable(true);
        stage.setWritable(true);
        return stage;
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
    public ReadableRegistryView<EObject> registryView(String registry) {
        return null;
    }

    @Override
    public ReadableRegistryView<EObject> registryView(String registry, String stage) {
        return null;
    }
}
