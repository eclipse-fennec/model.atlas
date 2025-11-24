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
 *      Mark Hoffmann - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.rest.ecore.xmi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
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
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;

/**
 * Custom EMF MessageBodyReader/Writer for the governance application that fixes
 * URI resolution issues.
 * 
 * <p>
 * This implementation addresses the "resolve against non-hierarchical or
 * relative base" error by using absolute URIs instead of relative ones for XMI
 * resource creation and XML URI handling.
 * </p>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
@Component(service = { MessageBodyReader.class, MessageBodyWriter.class }, scope = ServiceScope.PROTOTYPE)
@JakartarsExtension
@JakartarsApplicationSelect("(|(emf=true)("+ JakartarsWhiteboardConstants.JAKARTA_RS_NAME + "=.default))")
@JakartarsName("EcoreMessageBodyHandler")
@Consumes({ "application/xmi", "application/xml" })
@Produces({ "application/xmi", "application/xml" })
public class EcoreMessageBodyHandler implements MessageBodyReader<EPackage>, MessageBodyWriter<EPackage> {

  private static final Logger logger = Logger.getLogger(EcoreMessageBodyHandler.class.getName());

  @Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
  private ComponentServiceObjects<ResourceSet> resourceSetFactory;

  // ========== MessageBodyReader Implementation ==========

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    // Support EObject and its subclasses for XMI content types
    boolean isEObject = EPackage.class.isAssignableFrom(type);
    boolean isXMI = isXMI(mediaType);

    logger.log(Level.FINE, "isReadable check: type={0}, mediaType={1}, result={2}",
        new Object[] { type.getSimpleName(), mediaType, isEObject && isXMI });

    return isEObject && isXMI;
  }

  /**
   * @param mediaType
   * @return
   */
  private boolean isXMI(MediaType mediaType) {
    return "application".equals(mediaType.getType())
        && ("xmi".equals(mediaType.getSubtype()) || "xml".equals(mediaType.getSubtype()));
  }

  @Override
  public EPackage readFrom(Class<EPackage> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {

    logger.log(Level.INFO, "Reading EObject from XMI: type={0}, mediaType={1}",
        new Object[] { type.getSimpleName(), mediaType });

    ResourceSet resourceSet = resourceSetFactory.getService();
    try {

      // Use ABSOLUTE URI to prevent "resolve against non-hierarchical or relative
      // base" error
      URI absoluteURI = URI.createURI("temp://governance/" + System.currentTimeMillis() + ".xmi");
      Resource resource = resourceSet.createResource(absoluteURI);

      // Configure XMI loading options for robust parsing
      Map<Object, Object> options = new HashMap<>();

      // Additional XMI loading options for robust parsing
      options.put(XMLResource.OPTION_DEFER_ATTACHMENT, Boolean.TRUE);
      options.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
      options.put(XMLResource.OPTION_LAX_FEATURE_PROCESSING, Boolean.TRUE);
      options.put(XMLResource.OPTION_RECORD_UNKNOWN_FEATURE, Boolean.TRUE);

      // Load the XMI content
      resource.load(entityStream, options);

      // Check for loading errors
      if (!resource.getErrors().isEmpty()) {
        StringBuilder errorMsg = new StringBuilder("XMI loading errors: ");
        for (Resource.Diagnostic error : resource.getErrors()) {
          errorMsg.append(error.getMessage()).append("; ");
        }
        logger.log(Level.SEVERE, errorMsg.toString());
        throw new IOException("XMI loading failed: " + errorMsg.toString());
      }

      // Get the root EObject
      if (resource.getContents().isEmpty()) {
        throw new IOException("No content found in XMI resource");
      }

      EObject rootObject = resource.getContents().get(0);
      logger.log(Level.INFO, "Successfully loaded EObject: {0}", rootObject.getClass().getSimpleName());

      return (EPackage) rootObject;

    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error deserializing XMI content", e);

      String errorText = String.format("[%s] Error de-serializing incoming data: %s", genericType.getTypeName(),
          e.getMessage());
      Response errorResponse = Response.serverError().entity(errorText).type(MediaType.TEXT_PLAIN).build();
      throw new WebApplicationException(e, errorResponse);
    } finally {
      resourceSetFactory.ungetService(resourceSet);
    }
  }

  // ========== MessageBodyWriter Implementation ==========

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return isReadable(type, genericType, annotations, mediaType);
  }

  @Override
  public void writeTo(EPackage eObject, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {

    logger.log(Level.INFO, "Writing EObject to XMI: type={0}, mediaType={1}",
        new Object[] { eObject.getClass().getSimpleName(), mediaType });

    ResourceSet resourceSet = resourceSetFactory.getService();
    try {

      // Use ABSOLUTE URI for consistent behavior
      String fileName = eObject.getName() + (isXMI(mediaType) ? ".xmi" : ".xml");
      httpHeaders.put(HttpHeaders.CONTENT_DISPOSITION, List.of("attachment; filename=" + fileName));
      URI absoluteURI = URI.createURI(fileName);
      Resource resource = resourceSet.createResource(absoluteURI);
      resource.getContents().add(eObject);

      // Configure XMI saving options
      Map<Object, Object> options = new HashMap<>();
      options.put(XMLResource.OPTION_ENCODING, "UTF-8");
      options.put(XMLResource.OPTION_XML_VERSION, "1.0");
      options.put(XMLResource.OPTION_DECLARE_XML, Boolean.TRUE);

      // Save to output stream
      resource.save(entityStream, options);

      logger.log(Level.INFO, "Successfully serialized EObject to XMI");

    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error serializing EObject to XMI", e);

      String errorText = String.format("Error serializing [%s] to XMI: %s", eObject.getClass().getSimpleName(),
          e.getMessage());
      Response errorResponse = Response.serverError().entity(errorText).type(MediaType.TEXT_PLAIN).build();
      throw new WebApplicationException(e, errorResponse);
    } finally {
      resourceSetFactory.ungetService(resourceSet);
    }
  }
}