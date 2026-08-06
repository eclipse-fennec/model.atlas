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
package org.eclipse.fennec.model.atlas.rest.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.MessageBodyReader;

/**
 * The four schema {@code MessageBodyReader}s answer a payload they cannot turn into a
 * model the same way: {@link BadRequestException}.
 *
 * <p>
 * They used to disagree. The XMI reader threw {@code IOException} (a 500, blaming the
 * server for the client's document), while the JSON-schema, UML and XSD readers returned
 * {@code null} — which the endpoint then dereferenced, so the client got a 500 from an
 * NPE somewhere further in. Those three also never inspected {@code resource.getErrors()},
 * so a partially parsed document passed for a good one.
 * </p>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class MessageBodyReaderBadPayloadTest {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private MessageBodyReader<EPackage> reader(BundleContext context, String componentName) throws Exception {
        Collection<ServiceReference<MessageBodyReader>> references = context
                .getServiceReferences(MessageBodyReader.class, "(component.name=" + componentName + ")");
        assertNotNull(references, "No MessageBodyReader for " + componentName);
        MessageBodyReader<EPackage> reader = (MessageBodyReader<EPackage>) context
                .getService(references.iterator().next());
        assertNotNull(reader, componentName + " should be available");

        // @Context fields are only injected by Jersey; give the reader a real ResourceSet
        // so readFrom works outside a JAX-RS request scope.
        ResourceSet resourceSet = context.getService(context.getServiceReference(ResourceSet.class));
        TestHelper.injectResourceSet(reader, resourceSet);
        return reader;
    }

    @ParameterizedTest(name = "{0} rejects an empty payload with 400")
    @CsvSource({
            "org.eclipse.fennec.model.atlas.rest.ecore.xmi.EcoreMessageBodyHandler, application/xmi",
            "JSONSchemaMessagebodyReaderWriter, application/schema+json",
            "UMLMessageBodyReaderWriter, application/uml",
            "org.eclipse.fennec.model.atlas.rest.xsdschema.XSDSchemaMessageBodyReaderWriter, application/schema+xml" })
    @DisplayName("An empty payload is the client's mistake, not the server's")
    void shouldRejectEmptyPayload(String componentName, String mediaType,
            @InjectBundleContext BundleContext context) throws Exception {

        MessageBodyReader<EPackage> reader = reader(context, componentName);
        InputStream empty = new ByteArrayInputStream(new byte[0]);

        assertThrows(BadRequestException.class,
                () -> reader.readFrom(EPackage.class, EPackage.class, null, MediaType.valueOf(mediaType), null, empty),
                componentName + " must answer an empty payload with 400");
    }

    @ParameterizedTest(name = "{0} rejects an unparseable payload with 400")
    @CsvSource({
            "org.eclipse.fennec.model.atlas.rest.ecore.xmi.EcoreMessageBodyHandler, application/xmi",
            "JSONSchemaMessagebodyReaderWriter, application/schema+json",
            "UMLMessageBodyReaderWriter, application/uml",
            "org.eclipse.fennec.model.atlas.rest.xsdschema.XSDSchemaMessageBodyReaderWriter, application/schema+xml" })
    @DisplayName("An unparseable payload is the client's mistake, not the server's")
    void shouldRejectUnparseablePayload(String componentName, String mediaType,
            @InjectBundleContext BundleContext context) throws Exception {

        MessageBodyReader<EPackage> reader = reader(context, componentName);
        InputStream garbage = new ByteArrayInputStream(
                "this is not a model in any of the four formats".getBytes(StandardCharsets.UTF_8));

        assertThrows(BadRequestException.class,
                () -> reader.readFrom(EPackage.class, EPackage.class, null, MediaType.valueOf(mediaType), null, garbage),
                componentName + " must answer an unparseable payload with 400");
    }
}
