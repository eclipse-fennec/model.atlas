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
package org.eclipse.fennec.model.atlas.rest.jsonschema;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.codec.options.CodecModelInfoOptions;
import org.eclipse.fennec.codec.options.CodecModuleOptions;
import org.eclipse.fennec.codec.options.CodecResourceOptions;
import org.gecko.emf.osgi.constants.EMFNamespaces;
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
@JakartarsName("JSONSchemaMessagebodyReaderWriter")
@JakartarsApplicationSelect("(|(emf=true)(" + JakartarsWhiteboardConstants.JAKARTA_RS_NAME + "=.default))")
@Provider
@Produces("application/schema+json")
@Consumes("application/schema+json")
public class JsonSchemaMessageBodyReaderWriter implements MessageBodyReader<EPackage>, MessageBodyWriter<EPackage> {

  private static final Map<String, Object> OPTIONS = new HashMap<>();
  static {
    OPTIONS.put(CodecResourceOptions.CODEC_ROOT_OBJECT, EcorePackage.Literals.EPACKAGE);
    OPTIONS.put(CodecModuleOptions.CODEC_MODULE_SERIALIZE_TYPE, false);
    OPTIONS.put(CodecModuleOptions.CODEC_MODULE_SERIALIZE_EMPTY_VALUE, true);
    OPTIONS.put(CodecModuleOptions.CODEC_MODULE_SERIALIZE_NULL_VALUE, true);
    Map<String, Object> classOptions = new HashMap<>();
    classOptions.put(CodecModelInfoOptions.CODEC_EXTRAS,
        Map.of("jsonschema", "true", "jsonschema.feature.key", "definitions"));
    OPTIONS.put(CodecResourceOptions.CODEC_OPTIONS, Map.of(EcorePackage.Literals.EPACKAGE, classOptions));
  }

  @Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED, target = "(" + EMFNamespaces.EMF_CONFIGURATOR_NAME
      + "=CodecJson)")
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
    return EPackage.class.isAssignableFrom(type) && "application/schema+json".equals(mediaType.toString());
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
    ResourceSet resourceSet = resourceSetFactory.getService();
    try {
      Resource resource = resourceSet.createResource(URI.createURI(t.getNsURI()), "application/json");
      resource.getContents().add(t);
      resource.save(entityStream, OPTIONS);
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
      Resource resource = resourceSet.createResource(URI.createURI("temp.json"), "application/json");
      resource.load(entityStream, OPTIONS);
      EPackage ePackage = resource.getContents().isEmpty() ? null : (EPackage) resource.getContents().remove(0);
      return ePackage;
    } finally {
      resourceSetFactory.ungetService(resourceSet);
    }
  }

}
