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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.rest.uml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.rest.common.AbstractEPackageMessageBodyHandler;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.eclipse.uml2.uml.util.UMLUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.JakartarsWhiteboardConstants;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationSelect;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

@Component(name = "UMLMessageBodyReaderWriter", service = { MessageBodyReader.class,
        MessageBodyWriter.class }, enabled = true, scope = ServiceScope.PROTOTYPE)
@JakartarsExtension
@JakartarsName("UMLMessageBodyReaderWriter")
@JakartarsApplicationSelect("(|(emf=true)(" + JakartarsWhiteboardConstants.JAKARTA_RS_NAME + "=.default))")
@Provider
@Produces("application/uml")
@Consumes("application/uml")
public class UMLMessageBodyReaderWriter extends AbstractEPackageMessageBodyHandler {

    private static final MediaType UML_TYPE = new MediaType("application", "uml");

    /**
     * The URI a resource created for one request carries. It is absolute and
     * hierarchical, so references inside the document deresolve against it, and its
     * last segment is cosmetic — no document is ever written to that location.
     *
     * @param fileName the last segment of the URI
     * @return a URI under this handler's synthetic {@code atlas://response} base
     */
    private static URI newSyntheticURI(String fileName) {
        return URI.createHierarchicalURI("atlas", "response", null, new String[] { fileName }, null, null);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return EPackage.class.isAssignableFrom(type) && isMediaType(mediaType, UML_TYPE);
    }

    @Override
    public void writeTo(EPackage t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {

        ResourceSet resourceSet = getResourceSet();
        String fileName = t.getName() + "." + UMLResource.FILE_EXTENSION;
        httpHeaders.put(HttpHeaders.CONTENT_DISPOSITION, List.of("attachment; filename=" + fileName));
        Collection<Package> convertFromEcore = UMLUtil.convertFromEcore(t, null);
        // Pin the UML serialization instead of letting the ResourceSet's extension map
        // decide it. Asking the ResourceSet for a ".uml" resource made the response
        // format depend on that ResourceSet's configuration: one that does not know the
        // extension answers with the fallback factory, and one that knows no fallback
        // either returns null, failing the request with an NPE. The resource still joins
        // the ResourceSet, so pathmap references resolve through its URI converter.
        Resource resource = UMLResource.Factory.INSTANCE.createResource(newSyntheticURI(fileName));
        resourceSet.getResources().add(resource);
        try {
            resource.getContents().addAll(convertFromEcore);
            resource.save(entityStream, null);
        } finally {
            resource.getContents().clear();
            resourceSet.getResources().remove(resource);
        }
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return isWriteable(type, genericType, annotations, mediaType);
    }

    @Override
    public EPackage readFrom(Class<EPackage> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {
        ResourceSet resourceSet = getResourceSet();
        // Pin the factory here too: the payload is a UML model whichever extensions this
        // ResourceSet happens to know. It joins the ResourceSet so the model's pathmap
        // references — the UML primitive types among them — still resolve.
        Resource resource = UMLResource.Factory.INSTANCE.createResource(newSyntheticURI("temp.uml"));
        resourceSet.getResources().add(resource);
        // A payload this reader cannot turn into a model is the client's mistake: answer
        // 400 rather than null, which the endpoint would dereference into a 500. The
        // three sibling schema readers answer the same way.
        loadPayload(resource, entityStream, null, "UML");
        Package umlPackage = (Package) resource.getContents().remove(0);
        Collection<EPackage> values = UMLUtil.convertToEcore(umlPackage, null);
        return values.iterator().next();
    }
}
