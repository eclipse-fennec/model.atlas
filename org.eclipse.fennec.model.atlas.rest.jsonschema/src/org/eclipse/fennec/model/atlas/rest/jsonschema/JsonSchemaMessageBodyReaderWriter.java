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
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.fennec.codec.jsonschema.resource.JsonSchemaResourceFactory;
import org.eclipse.fennec.codec.options.CodecOptionsBuilder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
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
@Component(name = "JSONSchemaMessagebodyReaderWriter", service = { MessageBodyReader.class,
		MessageBodyWriter.class }, enabled = true, scope = ServiceScope.SINGLETON)
@JakartarsExtension
@JakartarsName("JSONSchemaMessagebodyReaderWriter")
@JakartarsApplicationSelect("(|(emf=true)(" + JakartarsWhiteboardConstants.JAKARTA_RS_NAME + "=.default))")
@Provider
@Produces("application/schema+json")
@Consumes("application/schema+json")
public class JsonSchemaMessageBodyReaderWriter implements MessageBodyReader<EPackage>, MessageBodyWriter<EPackage> {

	private static final Map<String, Object> OPTIONS = CodecOptionsBuilder.create().
			rootObject(EcorePackage.Literals.EPACKAGE).
			serializeType(false).
			serializeEmptyValue(true).
			serializeNullValue(true).
			forClass(EcorePackage.Literals.EPACKAGE).
			withExtraProperties(Map.of("jsonschema", "true", "jsonschema.feature.key", "definitions")).
			build();


	//  We need to inject this and not the resource factory because we might have conflicts with the json resource. (see codec documentation!)
	@Reference
	private JsonSchemaResourceFactory jsonSchemaResourceFactory;

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

		Resource resource = jsonSchemaResourceFactory.createResource(URI.createURI(t.getNsURI()));
		resource.getContents().add(t);
		resource.save(entityStream, OPTIONS);
		resource.getContents().clear();

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

		Resource resource = jsonSchemaResourceFactory.createResource(URI.createURI("temp.json"));
		resource.load(entityStream, OPTIONS);
		EPackage ePackage = resource.getContents().isEmpty() ? null : (EPackage) resource.getContents().remove(0);
		return ePackage;

	}

}
