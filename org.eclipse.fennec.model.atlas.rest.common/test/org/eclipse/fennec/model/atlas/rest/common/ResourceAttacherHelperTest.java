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
package org.eclipse.fennec.model.atlas.rest.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ResourceAttacherHelper}.
 */
class ResourceAttacherHelperTest {

    @Test
    @DisplayName("Attaching gives the object a resource in the given resource set")
    void attachesTheObject() {
        ResourceSet resourceSet = resourceSet();
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();

        ResourceAttacherHelper.attach(resourceSet, ePackage);

        assertNotNull(ePackage.eResource(), "The object must end up in a resource");
        assertSame(resourceSet, ePackage.eResource().getResourceSet(),
                "The resource must belong to the resource set it was asked for");
        assertEquals(List.of(ePackage), ePackage.eResource().getContents());
    }

    @Test
    @DisplayName("Attaching writes nothing to disk")
    void writesNothing() throws IOException {
        Set<Path> before = filesInWorkingDirectory();

        ResourceAttacherHelper.attach(resourceSet(), EcoreFactory.eINSTANCE.createEPackage());

        Set<Path> after = filesInWorkingDirectory();
        after.removeAll(before);
        // The helper used to save() the resource it had just created. Its URI is a bare
        // "<uuid>.xmi", so the save landed in whatever directory the server happens to
        // run in — a stray file per request, from a helper whose job is attachment.
        for (Path stray : after) {
            Files.deleteIfExists(stray);
        }
        assertEquals(Set.of(), after, "Attaching an object must not write files into the working directory");
    }

    private static ResourceSet resourceSet() {
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
        return resourceSet;
    }

    private Set<Path> filesInWorkingDirectory() throws IOException {
        try (Stream<Path> files = Files.list(Path.of("."))) {
            return files.filter(Files::isRegularFile).collect(Collectors.toCollection(java.util.HashSet::new));
        }
    }
}
