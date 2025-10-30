package org.eclipse.fennec.model.atlas.rest.tests.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

/**
 * Helper utility for common test operations including XMI serialization and
 * test data creation.
 */
public class TestHelper {

	/**
	 * Serializes an EObject (e.g., EPackage) to XMI string format using the provided ResourceSet.
	 *
	 * @param eObject the EObject to serialize
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
	 * @param xmiContent the XMI string content
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
	 * @param nsUri the namespace URI
	 * @param name the package name
	 * @param nsPrefix the namespace prefix
	 * @return a new test EPackage
	 */
	public static EPackage createTestEPackage(String nsUri, String name, String nsPrefix) {
		EPackage ePackage = org.eclipse.emf.ecore.EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setNsURI(nsUri);
		ePackage.setName(name);
		ePackage.setNsPrefix(nsPrefix);
		return ePackage;
	}

	/**
	 * Generates a unique namespace URI for testing purposes.
	 *
	 * @param testName the test name to include in the URI
	 * @return a unique namespace URI
	 */
	public static String generateUniqueNsUri(String testName) {
		return String.format("http://test.eclipse.fennec/%s/%d",
			testName.toLowerCase().replace(" ", "_"),
			System.currentTimeMillis());
	}

	/**
	 * Registers the XMI resource factory if not already registered.
	 *
	 * @param resourceSet the ResourceSet to register the factory with
	 */
	public static void ensureXMIFactory(ResourceSet resourceSet) {
		if (!resourceSet.getResourceFactoryRegistry()
				.getExtensionToFactoryMap().containsKey("xmi")) {
			resourceSet.getResourceFactoryRegistry()
				.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		}
	}
}
