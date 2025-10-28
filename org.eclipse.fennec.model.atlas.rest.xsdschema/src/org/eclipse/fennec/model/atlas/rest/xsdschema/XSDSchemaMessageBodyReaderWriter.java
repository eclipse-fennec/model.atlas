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
package org.eclipse.fennec.model.atlas.rest.xsdschema;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.ecore.EcoreXMLSchemaBuilder;
import org.eclipse.xsd.ecore.XSDEcoreBuilder;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
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

/**
 * 
 * @author Jürgen Albert
 * @since 24 Oct 2025
 */
@Component(service = { MessageBodyReader.class,
    MessageBodyWriter.class }, enabled = true, scope = ServiceScope.SINGLETON)
@JakartarsExtension
@JakartarsName("XSDSchemaMessagebodyReaderWriter")
@JakartarsApplicationSelect("(|(emf=true)(" + JakartarsWhiteboardConstants.JAKARTA_RS_NAME + "=.default))")
@Provider
@Produces("application/schema+xml")
@Consumes("application/schema+xml")
public class XSDSchemaMessageBodyReaderWriter implements MessageBodyReader<EPackage>, MessageBodyWriter<EPackage> {

  @Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
  private ComponentServiceObjects<ResourceSet> resourceSetFactory;

  /*
   * (non-Javadoc)
   * 
   * @see jakarta.ws.rs.ext.MessageBodyWriter#isWriteable(java.lang.Class,
   * java.lang.reflect.Type, java.lang.annotation.Annotation[],
   * jakarta.ws.rs.core.MediaType)
   */
  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return EPackage.class.isAssignableFrom(type) && "application/schema+xml".equals(mediaType.toString());
  }

  /*
   * (non-Javadoc)
   * 
   * @see jakarta.ws.rs.ext.MessageBodyWriter#writeTo(java.lang.Object,
   * java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[],
   * jakarta.ws.rs.core.MediaType, jakarta.ws.rs.core.MultivaluedMap,
   * java.io.OutputStream)
   */
  @Override
  public void writeTo(EPackage t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    EcoreXMLSchemaBuilder schemaBuilder = new EcoreXMLSchemaBuilder();

    Collection<EObject> collection = schemaBuilder.generate(t);
    String fileName = t.getName() + ".xsd";
    httpHeaders.put(HttpHeaders.CONTENT_DISPOSITION, List.of("attachment; filename=" + fileName));
    ResourceSet resourceSet = resourceSetFactory.getService();
    try {
      Resource resource = resourceSet.createResource(URI.createURI(fileName));
      resource.getContents().addAll(collection);
      resource.save(entityStream, null);
      resource.getContents().clear();
    } finally {
      resourceSetFactory.ungetService(resourceSet);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see jakarta.ws.rs.ext.MessageBodyReader#isReadable(java.lang.Class,
   * java.lang.reflect.Type, java.lang.annotation.Annotation[],
   * jakarta.ws.rs.core.MediaType)
   */
  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {

    return isWriteable(type, genericType, annotations, mediaType);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jakarta.ws.rs.ext.MessageBodyReader#readFrom(java.lang.Class,
   * java.lang.reflect.Type, java.lang.annotation.Annotation[],
   * jakarta.ws.rs.core.MediaType, jakarta.ws.rs.core.MultivaluedMap,
   * java.io.InputStream)
   */
  @Override
  public EPackage readFrom(Class<EPackage> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    ResourceSet resourceSet = resourceSetFactory.getService();
    try {
      Resource resource = resourceSet.createResource(URI.createURI("temp.xsd"));
      XSDSchema schema = resource.getContents().isEmpty() ? null : (XSDSchema) resource.getContents().remove(0);
      if (schema == null) {
        return null;
      }
      XSDEcoreBuilder ecoreBuilder = new XSDEcoreBuilder(new BasicExtendedMetaData(resourceSet.getPackageRegistry()));
      ecoreBuilder.generate(schema);
      Collection<EPackage> values = ecoreBuilder.getTargetNamespaceToEPackageMap().values();
      return values.iterator().next();
    } finally {
      resourceSetFactory.ungetService(resourceSet);
    }
  }

}
