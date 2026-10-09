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
package org.eclipse.fennec.model.atlas.mgmt.management;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.model.atlas.mgmt.storage.MetadataProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What a metadata property looks like once it has been written down (issue #354).
 * <p>
 * {@code properties} used to be {@code String -> EJavaObject}, and EMF converts an
 * {@code EJavaObject} to a string by Java-serializing it and hex-encoding the bytes. Every
 * property therefore reached the stored metadata files and the REST API as an
 * {@code ACED0005...} blob: unreadable, un-editable, undecodable by a client that is not Java,
 * and - on the way back in - Java deserialization of input that may have come from outside the
 * server.
 */
@DisplayName("A metadata property, once written down")
public class MetadataPropertySerializationTest {

	private static final String NS_URI = "https://example.org/model/1.0";

	/** The prefix of a Java serialization stream, as hex. Nothing readable starts with it. */
	private static final String JAVA_SERIAL_MAGIC = "ACED0005";

	@Test
	@DisplayName("is written as its value, not as a serialization blob")
	void isWrittenReadably() throws Exception {
		String xmi = write(metadata(Map.of("nsUri", NS_URI, "dcat", "true")));

		assertFalse(xmi.contains(JAVA_SERIAL_MAGIC),
				"a property reached the file as hex-encoded Java serialization:\n" + xmi);
		assertTrue(xmi.contains(NS_URI), "the nsUri is not readable in:\n" + xmi);
		assertTrue(xmi.contains("\"true\""), "the dcat flag is not readable in:\n" + xmi);
	}

	@Test
	@DisplayName("comes back as what was put in")
	void survivesARoundTrip() throws Exception {
		ObjectMetadata loaded = read(write(metadata(Map.of("nsUri", NS_URI, "dcat", "true"))));

		assertEquals(NS_URI, String.valueOf(loaded.getProperties().get("nsUri")));
		assertEquals("true", String.valueOf(loaded.getProperties().get("dcat")));
	}

	@Test
	@DisplayName("is still readable when it was stored in the old serialized form")
	void decodesWhatWasStoredBefore() throws Exception {
		// Every metadata file written before #354 holds its properties like this. The values are a
		// Java-serialized String and a Java-serialized Boolean, which is what EMF produced for an
		// EJavaObject. Read back as text they are the hex itself - so without a fallback, every
		// already-stored package's nsUri turns into a blob and the schema registry loses it.
		String stored = "<?xml version=\"1.0\" encoding=\"ASCII\"?>"
				+ "<mgmt:ObjectMetadata xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\""
				+ " xmlns:mgmt=\"http://eclipse.org/fennec/model/atlas/management/1.0.0\""
				+ " objectId=\"an-object\" objectName=\"a name\">"
				+ "<properties key=\"nsUri\" value=\"ACED000574001D68747470733A2F2F6578616D706C652E6F72672F6D6F64656C2F312E30\"/>"
				+ "<properties key=\"dcat\" value=\"ACED0005737200116A6176612E6C616E672E426F6F6C65616ECD207280D59CFAEE0200015A000576616C7565787001\"/>"
				+ "</mgmt:ObjectMetadata>";

		ObjectMetadata loaded = MetadataProperties.decodeLegacyValues(read(stored));

		assertEquals(NS_URI, loaded.getProperties().get("nsUri"));
		assertEquals("true", loaded.getProperties().get("dcat"));
	}

	@Test
	@DisplayName("is left alone when it only looks like the old form")
	void doesNotTouchAValueThatIsNotOne() throws Exception {
		String plain = "ACED0005 is a prefix, not this value";
		ObjectMetadata loaded = MetadataProperties
				.decodeLegacyValues(read(write(metadata(Map.of("note", plain)))));

		assertEquals(plain, loaded.getProperties().get("note"));
	}

	/* ------------------------------------------------------------------ helpers */

	private static ObjectMetadata metadata(Map<String, String> properties) {
		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setObjectId("an-object");
		metadata.setObjectName("a name");
		properties.forEach((key, value) -> metadata.getProperties().put(key, value));
		return metadata;
	}

	private static String write(ObjectMetadata metadata) throws Exception {
		Resource resource = resourceSet().createResource(URI.createURI("metadata.xmi"));
		resource.getContents().add(metadata);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		resource.save(bytes, Map.of());
		return bytes.toString(StandardCharsets.UTF_8);
	}

	private static ObjectMetadata read(String xmi) throws Exception {
		Resource resource = resourceSet().createResource(URI.createURI("metadata.xmi"));
		resource.load(new ByteArrayInputStream(xmi.getBytes(StandardCharsets.UTF_8)), Map.of());
		return (ObjectMetadata) resource.getContents().get(0);
	}

	private static ResourceSet resourceSet() {
		ResourceSet set = new ResourceSetImpl();
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		set.getPackageRegistry().put(ManagementPackage.eNS_URI, ManagementPackage.eINSTANCE);
		return set;
	}
}
