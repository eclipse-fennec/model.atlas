/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
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
package org.eclipse.fennec.model.atlas.rest.tests.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGFactory;
import org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person;
import org.eclipse.fennec.model.atlas.rest.common.AbstractEPackageMessageBodyHandler;
import org.eclipse.fennec.model.atlas.rest.common.ModelAtlasRestConstants;
import org.mockito.Mockito;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentServiceObjects;

import jakarta.ws.rs.container.ContainerRequestContext;

/**
 * Helper utility for common test operations including XMI serialization and
 * test data creation.
 */
public class TestHelper {

    /**
     * Serializes an EObject (e.g., EPackage) to XMI string format using the
     * provided ResourceSet.
     *
     * @param eObject     the EObject to serialize
     * @param resourceSet the ResourceSet to use for serialization
     * @return the XMI string representation
     * @throws IOException if serialization fails
     */
    public static String serializeToXMI(EObject eObject, ResourceSet resourceSet) throws IOException {
        // Create a temporary resource
        Resource resource = resourceSet.createResource(URI.createURI("temp://test.xmi"));
        resource.getContents().add(eObject);

        // Serialize to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        resource.save(baos, Collections.emptyMap());

        // Clean up
        resource.getContents().clear();
        resourceSet.getResources().remove(resource);

        return baos.toString("UTF-8");
    }

    /**
     * Deserializes an XMI string to an EObject using the provided ResourceSet.
     *
     * @param xmiContent  the XMI string content
     * @param resourceSet the ResourceSet to use for deserialization
     * @return the deserialized EObject
     * @throws IOException if deserialization fails
     */
    public static EObject deserializeFromXMI(String xmiContent, ResourceSet resourceSet) throws IOException {
        // Create a temporary resource
        Resource resource = resourceSet.createResource(URI.createURI("temp://test.xmi"));

        // Load from byte array
        ByteArrayInputStream bais = new ByteArrayInputStream(xmiContent.getBytes("UTF-8"));
        resource.load(bais, Collections.emptyMap());

        if (resource.getContents().isEmpty()) {
            throw new IOException("No content found in XMI");
        }

        EObject result = resource.getContents().get(0);

        // Clean up
        resource.getContents().clear();
        resourceSet.getResources().remove(resource);

        return result;
    }

    /**
     * Creates a test EPackage with the specified namespace URI and name.
     *
     * @param nsUri    the namespace URI
     * @param name     the package name
     * @param nsPrefix the namespace prefix
     * @return a new test EPackage
     */
    public static EPackage createTestEPackage(String nsUri, String name, String nsPrefix) {
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setNsURI(nsUri);
        ePackage.setName(name);
        ePackage.setNsPrefix(nsPrefix);
        return ePackage;
    }

    public static EClass createTestEClass(String name) {
    	EClass eClass = EcoreFactory.eINSTANCE.createEClass();
        eClass.setName(name);
        return eClass;
    }

    public static EAttribute createTestEAttribute(String name) {
    	EAttribute eAtt = EcoreFactory.eINSTANCE.createEAttribute();
    	eAtt.setName(name);
    	eAtt.setEType(EcorePackage.Literals.EINT);
    	return eAtt;
    }

    public static Person createTestObject() {
		Person person = DGFactory.eINSTANCE.createPerson();
		person.setFirstName("John");
		person.setLastName("Doe");
		person.setEmail("john.doe@gmail.com");
		person.setJobTitle("Software Developer");
		return person;
	}

    /**
     * Wraps a {@link ResourceSet} as a {@link ComponentServiceObjects} for use in
     * tests that invoke MBR/W methods directly (outside a JAX-RS request scope),
     * where {@code @Context} injection does not happen.
     *
     * <p>{@code ungetService} is a no-op; {@code getServiceReference} returns
     * {@code null}. This is intentional — tests do not manage OSGi service
     * lifecycles directly.</p>
     */
    public static ComponentServiceObjects<ResourceSet> wrapAsComponentServiceObjects(ResourceSet resourceSet) {
        return new ComponentServiceObjects<>() {
            @Override
            public ResourceSet getService() {
                return resourceSet;
            }

            @Override
            public void ungetService(ResourceSet service) {
                // no-op in tests
            }

            @Override
            public ServiceReference<ResourceSet> getServiceReference() {
                return null;
            }
        };
    }

    /**
     * Injects a {@link ComponentServiceObjects} into the base-class
     * {@code requestContextProvider} field so that {@code getResourceSetFactory()}
     * works in unit tests where no JAX-RS request scope exists.
     */
    public static void injectResourceSetFactory(Object service, ComponentServiceObjects<ResourceSet> cso)
            throws Exception {
        ContainerRequestContext mockCtx = Mockito.mock(ContainerRequestContext.class);
        Mockito.when(mockCtx.getProperty(ModelAtlasRestConstants.RESOLVED_RESOURCE_SET_CSO)).thenReturn(cso);
        Field field = AbstractEPackageMessageBodyHandler.class.getDeclaredField("requestContextProvider");
        field.setAccessible(true);
        field.set(service, (jakarta.inject.Provider<ContainerRequestContext>) () -> mockCtx);
    }

    /**
     * Generates a unique namespace URI for testing purposes.
     *
     * @param testName the test name to include in the URI
     * @return a unique namespace URI
     */
    public static String generateUniqueNsUri(String testName) {
        return String.format("http://test.eclipse.fennec/%s/%d", testName.toLowerCase().replace(" ", "_"),
                System.currentTimeMillis());
    }

    /**
     * Registers the XMI resource factory if not already registered.
     *
     * @param resourceSet the ResourceSet to register the factory with
     */
    public static void ensureXMIFactory(ResourceSet resourceSet) {
        if (!resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().containsKey("xmi")) {
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi",
                    new XMIResourceFactoryImpl());
        }
    }
}
