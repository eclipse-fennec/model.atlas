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
package org.eclipse.fennec.model.atlas.emf.common.ecore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Map;

import org.eclipse.emf.common.util.BasicEMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.DynamicEObjectImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests that {@link EClassResolvingDynamicEFactory} produces instances that survive a
 * reload of their EPackage — including the map entries.
 *
 * <p>
 * A reload leaves existing instances holding an {@link EClass} that has become a proxy.
 * The whole point of this factory is that {@code eClass()} heals that on access. Map
 * entries used to be left out: the factory asked for
 * {@code EClassResolvingDynamicEObject.BasicEMapEntry}, which compiles, but resolves to
 * the <em>inherited</em> {@code DynamicEObjectImpl.BasicEMapEntry} — the stock EMF class,
 * without the resolving {@code eClass()}.
 * </p>
 */
class EClassResolvingDynamicEFactoryTest {

    private static final String NS_URI = "http://example.com/reload/1.0";

    @Test
    @DisplayName("A plain object resolves its EClass after the package was reloaded")
    void plainObjectResolvesProxiedEClass() {
        Fixture fixture = new Fixture("Thing", false);

        EObject instance = fixture.createInstance();
        fixture.proxifyEClassOf(instance);

        assertFalse(instance.eClass().eIsProxy(), "The proxied EClass must be resolved on access");
        assertSame(fixture.eClass, instance.eClass());
    }

    @Test
    @DisplayName("A map entry resolves its EClass after the package was reloaded")
    void mapEntryResolvesProxiedEClass() {
        Fixture fixture = new Fixture("Entry", true);

        EObject entry = fixture.createInstance();
        assertInstanceOf(Map.Entry.class, entry, "A map-entry EClass must still produce a map entry");

        fixture.proxifyEClassOf(entry);

        assertFalse(entry.eClass().eIsProxy(),
                "A map entry must resolve its proxied EClass too — it is created by the same factory");
        assertSame(fixture.eClass, entry.eClass());
    }

    @Test
    @DisplayName("A map entry is still usable after the package was reloaded")
    void mapEntryStillReadsItsKeyAndValueAfterReload() {
        Fixture fixture = new Fixture("Entry", true);

        @SuppressWarnings("unchecked")
        Map.Entry<String, String> entry = (Map.Entry<String, String>) fixture.createInstance();
        ((BasicEMap.Entry<String, String>) entry).setKey("k");
        entry.setValue("v");

        fixture.proxifyEClassOf((EObject) entry);

        // Resolving eClass() is only half of it: the key and value features have to come
        // from the resolved EClass too. Held from before the reload they belong to an
        // EClass this entry no longer has.
        assertEquals("k", entry.getKey(), "The key must still be readable after a reload");
        assertEquals("v", entry.getValue(), "The value must still be readable after a reload");
    }

    /** An EPackage in a ResourceSet, so a proxy pointing into it can actually resolve. */
    private static final class Fixture {

        private final ResourceSet resourceSet = newResourceSet();
        private final EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        private final Resource contentResource;
        final EClass eClass;

        Fixture(String className, boolean mapEntry) {
            ePackage.setName("reload");
            ePackage.setNsURI(NS_URI);
            ePackage.setNsPrefix("rl");
            ePackage.setEFactoryInstance(new EClassResolvingDynamicEFactory());

            eClass = EcoreFactory.eINSTANCE.createEClass();
            eClass.setName(className);
            if (mapEntry) {
                eClass.setInstanceClassName("java.util.Map$Entry");
                eClass.getEStructuralFeatures().add(stringAttribute("key"));
                eClass.getEStructuralFeatures().add(stringAttribute("value"));
            }
            ePackage.getEClassifiers().add(eClass);

            // The package must live in a resource of this ResourceSet, otherwise a proxy
            // URI into it has nothing to resolve against.
            Resource packageResource = resourceSet.createResource(URI.createURI(NS_URI));
            packageResource.getContents().add(ePackage);
            resourceSet.getPackageRegistry().put(NS_URI, ePackage);

            contentResource = resourceSet.createResource(URI.createURI("http://example.com/content"));
        }

        EObject createInstance() {
            EObject instance = ePackage.getEFactoryInstance().create(eClass);
            contentResource.getContents().add(instance);
            return instance;
        }

        /** Reproduces what an EPackage reload does to an instance already in memory. */
        void proxifyEClassOf(EObject instance) {
            EClass proxy = EcoreFactory.eINSTANCE.createEClass();
            ((InternalEObject) proxy).eSetProxyURI(URI.createURI(NS_URI + "#//" + eClass.getName()));
            ((DynamicEObjectImpl) instance).eSetClass(proxy);
        }

        private static ResourceSet newResourceSet() {
            ResourceSet resourceSet = new ResourceSetImpl();
            resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap()
                    .put("http", new XMIResourceFactoryImpl());
            return resourceSet;
        }

        private static EAttribute stringAttribute(String name) {
            EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
            attribute.setName(name);
            attribute.setEType(EcorePackage.eINSTANCE.getEString());
            return attribute;
        }
    }
}
