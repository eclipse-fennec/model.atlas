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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link RegistryServiceImpl#isEClassCompatibleWithRegistry(EClass)}.
 *
 * @see <a href="https://github.com/eclipse-fennec/model.atlas/issues/188">issue #188</a>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegistryServiceImpl Unit Tests")
public class RegistryServiceImplTest {

    private static final String TEST_NS_URI = "http://example.org/test/1.0.0";

    private ResourceSet resourceSet;
    private EClass personClass;
    private EClass employeeClass;
    private EClass unrelatedClass;

    @BeforeEach
    void setUp() {
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("test");
        testPackage.setNsPrefix("test");
        testPackage.setNsURI(TEST_NS_URI);

        personClass = EcoreFactory.eINSTANCE.createEClass();
        personClass.setName("Person");
        employeeClass = EcoreFactory.eINSTANCE.createEClass();
        employeeClass.setName("Employee");
        employeeClass.getESuperTypes().add(personClass);
        unrelatedClass = EcoreFactory.eINSTANCE.createEClass();
        unrelatedClass.setName("Unrelated");
        testPackage.getEClassifiers().addAll(List.of(personClass, employeeClass, unrelatedClass));

        resourceSet = new ResourceSetImpl();
        Resource resource = new ResourceImpl(URI.createURI(TEST_NS_URI));
        resource.getContents().add(testPackage);
        resourceSet.getResources().add(resource);
    }

    private RegistryServiceImpl<EObject> createService(String rootEClassUri) {
        RegistryServiceConfig config = mock(RegistryServiceConfig.class);
        when(config.registry_name()).thenReturn("test-registry");
        when(config.registry_description()).thenReturn("");
        when(config.registry_type()).thenReturn("OTHER");
        when(config.workflow_transitions()).thenReturn(new String[] { "draft:release" });
        when(config.stage_storage_mappings()).thenReturn(new String[] { "draft:file", "release:file" });
        when(config.stages()).thenReturn(new String[] {
                "{\"name\": \"draft\", \"writable\": true, \"final\": false}",
                "{\"name\": \"release\", \"writable\": true, \"final\": true}" });
        when(config.root_eclass_uri()).thenReturn(rootEClassUri);
        return new RegistryServiceImpl<>(List.of(), resourceSet, List.of(), config);
    }

    @Nested
    @DisplayName("Registry rooted at the implicit EObject root")
    class EObjectRootTests {

        private RegistryServiceImpl<EObject> service;

        @BeforeEach
        void setUp() {
            service = createService(EcorePackage.eNS_URI + "#//EObject");
        }

        @Test
        @DisplayName("Should accept an EClass without explicit super types")
        void shouldAcceptEClassWithoutSuperTypes() {
            assertTrue(service.isEClassCompatibleWithRegistry(personClass));
        }

        @Test
        @DisplayName("Should accept an EClass with explicit super types")
        void shouldAcceptEClassWithSuperTypes() {
            assertTrue(service.isEClassCompatibleWithRegistry(employeeClass));
        }

        @Test
        @DisplayName("Should accept the EObject EClass itself")
        void shouldAcceptEObjectItself() {
            assertTrue(service.isEClassCompatibleWithRegistry(EcorePackage.Literals.EOBJECT));
        }
    }

    @Nested
    @DisplayName("Registry rooted at a specific EClass")
    class SpecificRootTests {

        private RegistryServiceImpl<EObject> service;

        @BeforeEach
        void setUp() {
            service = createService(TEST_NS_URI + "#//Person");
        }

        @Test
        @DisplayName("Should accept the root EClass itself")
        void shouldAcceptRootEClass() {
            assertTrue(service.isEClassCompatibleWithRegistry(personClass));
        }

        @Test
        @DisplayName("Should accept a sub class of the root EClass")
        void shouldAcceptSubClass() {
            assertTrue(service.isEClassCompatibleWithRegistry(employeeClass));
        }

        @Test
        @DisplayName("Should reject an unrelated EClass")
        void shouldRejectUnrelatedEClass() {
            assertFalse(service.isEClassCompatibleWithRegistry(unrelatedClass));
        }

        @Test
        @DisplayName("Should reject the EObject EClass")
        void shouldRejectEObject() {
            assertFalse(service.isEClassCompatibleWithRegistry(EcorePackage.Literals.EOBJECT));
        }
    }
}
