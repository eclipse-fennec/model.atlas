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
package org.eclipse.fennec.model.atlas.workflow.reload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Characterization spike for the git-backend reload / referential-integrity
 * question (PLAN.md D8). This test does <em>not</em> assert a desired contract —
 * it pins down what EMF actually does today when a backing EPackage is
 * unregistered and re-registered, which is the exact situation the git backend
 * creates on every push that touches a {@code .ecore}.
 *
 * <p>
 * It models model.atlas's read path as observed in
 * {@code AbstractStorageHelper.loadEObject}: each "read" re-parses the stored
 * bytes fresh against the ResourceSet's package registry <em>as it stands at read
 * time</em>, then discards the resource (no caching). A mutable
 * {@link EPackageRegistryImpl} stands in for the per-stage EPackage registry that
 * {@code DynamicEPackageRegistrationService} mutates on register/unregister.
 * </p>
 *
 * <p>
 * The two concerns raised on the design call:
 * </p>
 * <ol>
 * <li><b>Instance {@code eClass()} after model reload</b> — does a stored instance
 * still resolve its type once its backing model has been reloaded?</li>
 * <li><b>Cross-ecore references</b> — model A references model B; B is reloaded.
 * Does a fresh read of A re-link to the new B?</li>
 * </ol>
 */
@DisplayName("Reload referential-integrity spike (D8 characterization)")
public class ReloadReferentialIntegritySpikeTest {

    private static final String B_NS = "http://spike.example.com/b";
    private static final String A_NS = "http://spike.example.com/a";

    // ---------------------------------------------------------------------
    // Concern 2 — instance eClass() resolution across a model reload
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Instance eClass() resolution")
    class InstanceEClassResolution {

        @Test
        @DisplayName("a fresh read re-parses against the LATEST registered EPackage (the catalog hypothesis)")
        void freshReadResolvesToLatestRegisteredPackage() throws IOException {
            EPackage b1 = thingPackage("v1");
            EObject instance = newThing(b1, "sensor-42");
            byte[] stored = serialize(instance, "thing.xmi");

            EPackageRegistryImpl registry = newRegistry();
            registry.put(B_NS, b1);

            // First read resolves against b1.
            EObject read1 = firstContent(load(stored, "thing.xmi", registry));
            assertSame(b1, read1.eClass().getEPackage(), "fresh read should resolve against the registered b1");

            // Simulate a push that reloads B: unregister + register a NEW instance.
            EPackage b2 = thingPackage("v2");
            registry.put(B_NS, b2); // re-register, same nsURI, different object

            // A brand-new read resolves against b2 — no stale reference.
            EObject read2 = firstContent(load(stored, "thing.xmi", registry));
            assertSame(b2, read2.eClass().getEPackage(),
                    "fresh read AFTER reload should resolve against the new b2 (hypothesis holds)");
            assertNotSame(b1, read2.eClass().getEPackage());
        }

        @Test
        @DisplayName("an already-read (retained) instance stays FROZEN on the old EPackage")
        void retainedInstanceStaysFrozenAfterReload() throws IOException {
            EPackage b1 = thingPackage("v1");
            byte[] stored = serialize(newThing(b1, "sensor-42"), "thing.xmi");

            EPackageRegistryImpl registry = newRegistry();
            registry.put(B_NS, b1);
            EObject retained = firstContent(load(stored, "thing.xmi", registry));

            EPackage b2 = thingPackage("v2");
            registry.put(B_NS, b2);

            // The retained EObject holds a hard Java reference to b1's EClass; it does
            // NOT auto-refresh. This is why model.atlas must not cache resolved reads.
            assertSame(b1, retained.eClass().getEPackage(),
                    "a retained instance keeps pointing at the OLD package (hard reference, no on-demand refresh)");
        }

        @Test
        @DisplayName("a read while the model is UNREGISTERED hard-throws (PackageNotFoundException)")
        void readWithModelRemovedThrows() throws IOException {
            EPackage b1 = thingPackage("v1");
            byte[] stored = serialize(newThing(b1, "sensor-42"), "thing.xmi");

            EPackageRegistryImpl registry = newRegistry();
            // Model NOT registered — models the reload window and a removed model.
            // (Ecore is present via the delegate; only the dynamic B is missing.)
            // FINDING: EMF does not degrade to a proxy/empty resource here; the parse
            // throws. So an instance read that races an unregistered/removed model is a
            // hard failure the git read path must anticipate (ordering + reload window).
            IOException ex = assertThrows(IOException.class,
                    () -> load(stored, "thing.xmi", registry),
                    "reading an instance whose model is not registered should throw");
            assertTrue(ex.toString().contains(B_NS) || String.valueOf(ex.getCause()).contains(B_NS),
                    "failure should point at the missing package nsURI, was: " + ex);
        }
    }

    // ---------------------------------------------------------------------
    // Concern 1 — cross-ecore references (A references B) across a B reload
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Cross-ecore references (A -> B)")
    class CrossEcoreReferences {

        @Test
        @DisplayName("a fresh read of A re-links to the reloaded B; a retained A stays frozen")
        void freshReadRelinksToReloadedPackage() throws IOException {
            EPackage b1 = thingPackage("v1");
            EPackage a = holderPackage(b1);
            // Serialize A with B present so A's cross-reference is stored as an href
            // to B_NS#//Thing (the git repo ships both .ecore files together).
            byte[] storedA = serializeWithCompanion(a, A_NS, b1, B_NS);

            // Read 1: A resolved against b1.
            EClass holder1 = readHolderAgainst(storedA, b1);
            EReference ref1 = (EReference) holder1.getEStructuralFeature("thing");
            assertSame(b1, ref1.getEReferenceType().getEPackage(),
                    "A's cross-reference should resolve to b1 on first read");

            // Read 2 after B reload: A resolved against b2.
            EPackage b2 = thingPackage("v2");
            EClass holder2 = readHolderAgainst(storedA, b2);
            EReference ref2 = (EReference) holder2.getEStructuralFeature("thing");
            assertSame(b2, ref2.getEReferenceType().getEPackage(),
                    "a fresh read of A after B is reloaded re-links to the new b2");

            // The first read stays frozen on b1.
            assertSame(b1, ref1.getEReferenceType().getEPackage(),
                    "the retained read of A still points at the OLD b1");
            assertNotSame(b2, ref1.getEReferenceType().getEPackage());
        }
    }

    // ---------------------------------------------------------------------
    // helpers — model + instance construction and the "catalog" read
    // ---------------------------------------------------------------------

    /** A B-model: one EClass "Thing" with a "name" attribute; {@code version} distinguishes reloads. */
    private static EPackage thingPackage(String version) {
        EPackage p = EcoreFactory.eINSTANCE.createEPackage();
        p.setName("bmodel");
        p.setNsPrefix("b");
        p.setNsURI(B_NS);

        EClass thing = EcoreFactory.eINSTANCE.createEClass();
        thing.setName("Thing");
        EAttribute name = EcoreFactory.eINSTANCE.createEAttribute();
        name.setName("name");
        name.setEType(EcorePackage.Literals.ESTRING);
        thing.getEStructuralFeatures().add(name);
        p.getEClassifiers().add(thing);

        // Tag the version so tests could distinguish content if needed.
        p.setNsPrefix("b_" + version);
        return p;
    }

    /** An A-model: EClass "Holder" with an EReference "thing" typed to B's Thing. */
    private static EPackage holderPackage(EPackage bModel) {
        EClass thing = (EClass) bModel.getEClassifier("Thing");

        EPackage p = EcoreFactory.eINSTANCE.createEPackage();
        p.setName("amodel");
        p.setNsPrefix("a");
        p.setNsURI(A_NS);

        EClass holder = EcoreFactory.eINSTANCE.createEClass();
        holder.setName("Holder");
        EReference ref = EcoreFactory.eINSTANCE.createEReference();
        ref.setName("thing");
        ref.setEType(thing);
        holder.getEStructuralFeatures().add(ref);
        p.getEClassifiers().add(holder);
        return p;
    }

    private static EObject newThing(EPackage bModel, String name) {
        EClass thing = (EClass) bModel.getEClassifier("Thing");
        EObject instance = bModel.getEFactoryInstance().create(thing);
        instance.eSet(thing.getEStructuralFeature("name"), name);
        return instance;
    }

    private static byte[] serialize(EObject eObject, String uriString) throws IOException {
        ResourceSet rs = newResourceSet(null);
        Resource r = rs.createResource(URI.createURI(uriString));
        r.getContents().add(eObject);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        r.save(out, Collections.emptyMap());
        return out.toByteArray();
    }

    /** Serializes {@code eObject}, with {@code companion} available at its URI so cross-resource hrefs resolve. */
    private static byte[] serializeWithCompanion(EObject eObject, String uriString, EObject companion,
            String companionUri) throws IOException {
        ResourceSet rs = newResourceSet(null);
        Resource companionRes = rs.createResource(URI.createURI(companionUri));
        companionRes.getContents().add(companion);
        Resource r = rs.createResource(URI.createURI(uriString));
        r.getContents().add(eObject);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        r.save(out, Collections.emptyMap());
        return out.toByteArray();
    }

    /**
     * Mimics {@code AbstractStorageHelper.loadEObject}: fresh parse against the
     * given package registry, resolve, then drop the resource from the set.
     */
    private static Resource load(byte[] bytes, String uriString, EPackage.Registry registry) throws IOException {
        ResourceSet rs = newResourceSet(registry);
        Resource r = rs.createResource(URI.createURI(uriString));
        r.load(new ByteArrayInputStream(bytes), Collections.emptyMap());
        EcoreUtil.resolveAll(rs);
        rs.getResources().remove(r); // cleanup(), as the storage helper does
        return r;
    }

    /**
     * Reads the stored A-model with {@code bVersion} as the currently-available B,
     * returning A's Holder EClass with its cross-reference resolved.
     */
    private static EClass readHolderAgainst(byte[] storedA, EPackage bVersion) throws IOException {
        EPackageRegistryImpl registry = newRegistry();
        registry.put(B_NS, bVersion);

        ResourceSet rs = newResourceSet(registry);
        // The current B is available as a resource at its nsURI so A's href resolves,
        // exactly as the git backend makes the same-commit .ecore available.
        Resource bRes = rs.createResource(URI.createURI(B_NS));
        bRes.getContents().add(bVersion);

        Resource aRes = rs.createResource(URI.createURI(A_NS));
        aRes.load(new ByteArrayInputStream(storedA), Collections.emptyMap());
        EcoreUtil.resolveAll(rs);

        EPackage a = (EPackage) aRes.getContents().get(0);
        return (EClass) a.getEClassifier("Holder");
    }

    private static EObject firstContent(Resource r) {
        assertFalse(r.getContents().isEmpty(), "resource should have parsed content");
        return r.getContents().get(0);
    }

    /** A registry that carries Ecore (via the global delegate) but lets tests control the dynamic packages. */
    private static EPackageRegistryImpl newRegistry() {
        return new EPackageRegistryImpl(EPackage.Registry.INSTANCE);
    }

    private static ResourceSet newResourceSet(EPackage.Registry registry) {
        ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
        if (registry != null) {
            rs.setPackageRegistry(registry);
        }
        return rs;
    }
}
