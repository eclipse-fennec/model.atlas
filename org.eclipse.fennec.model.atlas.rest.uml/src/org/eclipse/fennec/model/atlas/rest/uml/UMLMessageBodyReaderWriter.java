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

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.rest.common.AbstractEPackageMessageBodyHandler;
import org.eclipse.uml2.uml.Package;
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

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return EPackage.class.isAssignableFrom(type) && "application/uml".equals(mediaType.toString());
    }

    @Override
    public void writeTo(EPackage t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {

        String fileName = t.getName() + ".uml";
        Collection<Package> convertFromEcore = UMLUtil.convertFromEcore(t, null);
        var factory = getResourceSetFactory();
        ResourceSet resourceSet = factory.getService();
        try {
            Resource resource = resourceSet.createResource(URI.createURI(fileName));
            resource.getContents().addAll(convertFromEcore);
            resource.save(entityStream, null);
            resource.getContents().clear();
        } finally {
            factory.ungetService(resourceSet);
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
        var factory = getResourceSetFactory();
        ResourceSet resourceSet = factory.getService();
        try {
            Resource resource = resourceSet.createResource(URI.createURI("temp.uml"));
            resource.load(entityStream, null);
            Package umlPackage = resource.getContents().isEmpty() ? null : (Package) resource.getContents().remove(0);
            if (umlPackage == null) {
                return null;
            }
            Collection<EPackage> values = UMLUtil.convertToEcore(umlPackage, null);
            return values.iterator().next();
        } finally {
            factory.ungetService(resourceSet);
        }
    }
}
