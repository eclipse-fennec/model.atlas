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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.codec.options.CodecModelInfoOptions;
import org.eclipse.fennec.codec.options.CodecModuleOptions;
import org.eclipse.fennec.codec.options.CodecResourceOptions;
import org.gecko.emf.osgi.constants.EMFNamespaces;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.JakartarsWhiteboardConstants;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationSelect;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.xsd.ecore.EcoreXMLSchemaBuilder;

/**
 * 
 * @author Jürgen Albert
 * @since 24 Oct 2025
 */
@Component(
    service = {MessageBodyReader.class, MessageBodyWriter.class},
    enabled = true,
    scope = ServiceScope.SINGLETON
  )
@JakartarsExtension
@JakartarsName("XSDSchemaMessagebodyReaderWriter")
@JakartarsApplicationSelect("(|(emf=true)("+ JakartarsWhiteboardConstants.JAKARTA_RS_NAME + "=.default))")
@Provider
@Produces("application/schema+xml")
@Consumes("application/schema+xml")
public class XSDSchemaMessageBodyReaderWriter implements MessageBodyReader<EPackage>, MessageBodyWriter<EPackage> {

  @Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED) 
  private ResourceSet resourceSet;
  
  /* 
   * (non-Javadoc)
   * @see jakarta.ws.rs.ext.MessageBodyWriter#isWriteable(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], jakarta.ws.rs.core.MediaType)
   */
  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return EPackage.class.isAssignableFrom(type) && "application/schema+xml".equals(mediaType.toString());
  }

  /* 
   * (non-Javadoc)
   * @see jakarta.ws.rs.ext.MessageBodyWriter#writeTo(java.lang.Object, java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], jakarta.ws.rs.core.MediaType, jakarta.ws.rs.core.MultivaluedMap, java.io.OutputStream)
   */
  @Override
  public void writeTo(EPackage t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    EcoreXMLSchemaBuilder schemaBuilder = new EcoreXMLSchemaBuilder();
    Node node = new Node() {
      
      @Override
      public Object setUserData(String key, Object data, UserDataHandler handler) {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public void setTextContent(String textContent) throws DOMException {
        // TODO Auto-generated method stub
        
      }
      
      @Override
      public void setPrefix(String prefix) throws DOMException {
        // TODO Auto-generated method stub
        
      }
      
      @Override
      public void setNodeValue(String nodeValue) throws DOMException {
        // TODO Auto-generated method stub
        
      }
      
      @Override
      public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public Node removeChild(Node oldChild) throws DOMException {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public void normalize() {
        // TODO Auto-generated method stub
        
      }
      
      @Override
      public String lookupPrefix(String namespaceURI) {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public String lookupNamespaceURI(String prefix) {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public boolean isSupported(String feature, String version) {
        // TODO Auto-generated method stub
        return false;
      }
      
      @Override
      public boolean isSameNode(Node other) {
        // TODO Auto-generated method stub
        return false;
      }
      
      @Override
      public boolean isEqualNode(Node arg) {
        // TODO Auto-generated method stub
        return false;
      }
      
      @Override
      public boolean isDefaultNamespace(String namespaceURI) {
        // TODO Auto-generated method stub
        return false;
      }
      
      @Override
      public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public boolean hasChildNodes() {
        // TODO Auto-generated method stub
        return false;
      }
      
      @Override
      public boolean hasAttributes() {
        // TODO Auto-generated method stub
        return false;
      }
      
      @Override
      public Object getUserData(String key) {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public String getTextContent() throws DOMException {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public Node getPreviousSibling() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public String getPrefix() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public Node getParentNode() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public Document getOwnerDocument() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public String getNodeValue() throws DOMException {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public short getNodeType() {
        // TODO Auto-generated method stub
        return 0;
      }
      
      @Override
      public String getNodeName() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public Node getNextSibling() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public String getNamespaceURI() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public String getLocalName() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public Node getLastChild() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public Node getFirstChild() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public Object getFeature(String feature, String version) {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public NodeList getChildNodes() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public String getBaseURI() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public NamedNodeMap getAttributes() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public short compareDocumentPosition(Node other) throws DOMException {
        // TODO Auto-generated method stub
        return 0;
      }
      
      @Override
      public Node cloneNode(boolean deep) {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public Node appendChild(Node newChild) throws DOMException {
        // TODO Auto-generated method stub
        return null;
      }
    };
    Collection<EObject> collection = schemaBuilder.generate(t);
    
    Resource resource = resourceSet.createResource(URI.createURI("temp.xsd"));
    resource.getContents().addAll(collection);
    resource.save(entityStream, null);
    resource.getContents().clear();
  }

  /* 
   * (non-Javadoc)
   * @see jakarta.ws.rs.ext.MessageBodyReader#isReadable(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], jakarta.ws.rs.core.MediaType)
   */
  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    
    return isWriteable(type, genericType, annotations, mediaType);
  }

  /* 
   * (non-Javadoc)
   * @see jakarta.ws.rs.ext.MessageBodyReader#readFrom(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], jakarta.ws.rs.core.MediaType, jakarta.ws.rs.core.MultivaluedMap, java.io.InputStream)
   */
  @Override
  public EPackage readFrom(Class<EPackage> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    Resource resource = resourceSet.createResource(URI.createURI("temp.json"), "application/json");
    EPackage ePackage = resource.getContents().isEmpty() ? null : (EPackage) resource.getContents().remove(0);
    return ePackage;
  }

}
