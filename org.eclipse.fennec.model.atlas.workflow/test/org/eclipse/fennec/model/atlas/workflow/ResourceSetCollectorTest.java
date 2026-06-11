///**
// * Copyright (c) 2012 - 2026 Data In Motion and others.
// * All rights reserved.
// *
// * This program and the accompanying materials are made
// * available under the terms of the Eclipse Public License 2.0
// * which is available at https://www.eclipse.org/legal/epl-2.0/
// *
// * SPDX-License-Identifier: EPL-2.0
// *
// * Contributors:
// *     Data In Motion - initial API and implementation
// */
//package org.eclipse.fennec.model.atlas.workflow;
//
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.junit.jupiter.api.Assertions.assertSame;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.eclipse.emf.ecore.resource.ResourceSet;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.osgi.service.component.ComponentServiceObjects;
//
///**
// * Unit tests for {@link ResourceSetCollector}.
// *
// * @author ilenia
// * @since Apr 17, 2026
// */
//@ExtendWith(MockitoExtension.class)
//@DisplayName("ResourceSetCollector Unit Tests")
//public class ResourceSetCollectorTest {
//
//    @Mock
//    private ComponentServiceObjects<ResourceSet> csoA;
//
//    @Mock
//    private ComponentServiceObjects<ResourceSet> csoB;
//
//    private ResourceSetCollector collector;
//
//    @BeforeEach
//    void setUp() {
//        collector = new ResourceSetCollector();
//    }
//
//    private static Map<String, Object> props(String scopeName, String stageName) {
//        Map<String, Object> props = new HashMap<>();
//        if (scopeName != null) {
//            props.put(ResourceSetCollector.SCOPE_NAME_PROPERTY, scopeName);
//        }
//        if (stageName != null) {
//            props.put(ResourceSetCollector.STAGE_NAME_PROPERTY, stageName);
//        }
//        return props;
//    }
//
//    @Nested
//    @DisplayName("Lookup Tests")
//    class LookupTests {
//
//        @Test
//        @DisplayName("Should return bound CSO for matching scope/stage")
//        void shouldReturnBoundCsoForMatchingScopeStage() {
//            collector.bindResourceSet(csoA, props("billing", "draft"));
//
//            assertSame(csoA, collector.getResourceSetObjects("billing", "draft"));
//        }
//
//        @Test
//        @DisplayName("Should distinguish entries by scope and stage independently")
//        void shouldDistinguishEntriesByScopeAndStage() {
//            collector.bindResourceSet(csoA, props("billing", "draft"));
//            collector.bindResourceSet(csoB, props("billing", "release"));
//
//            assertSame(csoA, collector.getResourceSetObjects("billing", "draft"));
//            assertSame(csoB, collector.getResourceSetObjects("billing", "release"));
//        }
//
//        @Test
//        @DisplayName("Should return null when no CSO bound for scope/stage")
//        void shouldReturnNullWhenNoCsoBound() {
//            assertNull(collector.getResourceSetObjects("billing", "draft"));
//        }
//
//        @Test
//        @DisplayName("Should return null for null scope name")
//        void shouldReturnNullForNullScopeName() {
//            collector.bindResourceSet(csoA, props("billing", "draft"));
//
//            assertNull(collector.getResourceSetObjects(null, "draft"));
//        }
//
//        @Test
//        @DisplayName("Should return null for null stage name")
//        void shouldReturnNullForNullStageName() {
//            collector.bindResourceSet(csoA, props("billing", "draft"));
//
//            assertNull(collector.getResourceSetObjects("billing", null));
//        }
//
//        @Test
//        @DisplayName("Should not confuse scopes with shared suffixes")
//        void shouldNotConfuseScopesWithSharedSuffixes() {
//            collector.bindResourceSet(csoA, props("billing", "draft_release"));
//            collector.bindResourceSet(csoB, props("billing_draft", "release"));
//
//            assertSame(csoA, collector.getResourceSetObjects("billing", "draft_release"));
//            assertSame(csoB, collector.getResourceSetObjects("billing_draft", "release"));
//        }
//    }
//
//    @Nested
//    @DisplayName("Bind Validation Tests")
//    class BindValidationTests {
//
//        @Test
//        @DisplayName("Should ignore bind when scope.name is missing")
//        void shouldIgnoreBindWhenScopeMissing() {
//            collector.bindResourceSet(csoA, props(null, "draft"));
//
//            assertNull(collector.getResourceSetObjects(null, "draft"));
//        }
//
//        @Test
//        @DisplayName("Should ignore bind when stage.name is missing")
//        void shouldIgnoreBindWhenStageMissing() {
//            collector.bindResourceSet(csoA, props("billing", null));
//
//            assertNull(collector.getResourceSetObjects("billing", null));
//        }
//
//        @Test
//        @DisplayName("Should ignore bind when scope.name is blank")
//        void shouldIgnoreBindWhenScopeBlank() {
//            collector.bindResourceSet(csoA, props("  ", "draft"));
//
//            assertNull(collector.getResourceSetObjects("  ", "draft"));
//        }
//
//        @Test
//        @DisplayName("Should ignore bind when stage.name is blank")
//        void shouldIgnoreBindWhenStageBlank() {
//            collector.bindResourceSet(csoA, props("billing", "  "));
//
//            assertNull(collector.getResourceSetObjects("billing", "  "));
//        }
//
//        @Test
//        @DisplayName("Should override when rebinding same scope/stage")
//        void shouldOverrideWhenRebindingSameScopeStage() {
//            collector.bindResourceSet(csoA, props("billing", "draft"));
//            collector.bindResourceSet(csoB, props("billing", "draft"));
//
//            assertSame(csoB, collector.getResourceSetObjects("billing", "draft"));
//        }
//    }
//
//    @Nested
//    @DisplayName("Unbind Tests")
//    class UnbindTests {
//
//        @Test
//        @DisplayName("Should remove entry when unbinding")
//        void shouldRemoveEntryOnUnbind() {
//            collector.bindResourceSet(csoA, props("billing", "draft"));
//            collector.unbindResourceSet(csoA, props("billing", "draft"));
//
//            assertNull(collector.getResourceSetObjects("billing", "draft"));
//        }
//
//        @Test
//        @DisplayName("Should only remove when the CSO matches the bound one")
//        void shouldOnlyRemoveWhenCsoMatches() {
//            collector.bindResourceSet(csoA, props("billing", "draft"));
//            // GREEDY rebind: new service bound before old unbind -> old unbind must not
//            // clear the newer value.
//            collector.bindResourceSet(csoB, props("billing", "draft"));
//            collector.unbindResourceSet(csoA, props("billing", "draft"));
//
//            assertSame(csoB, collector.getResourceSetObjects("billing", "draft"));
//        }
//
//        @Test
//        @DisplayName("Should be a no-op when properties are incomplete")
//        void shouldBeNoOpWhenPropertiesIncomplete() {
//            collector.bindResourceSet(csoA, props("billing", "draft"));
//            collector.unbindResourceSet(csoA, props(null, "draft"));
//            collector.unbindResourceSet(csoA, props("billing", null));
//
//            assertSame(csoA, collector.getResourceSetObjects("billing", "draft"));
//        }
//    }
//}
