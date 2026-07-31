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
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.codec.jsonschema.v2.constants.CodecJsonSchemaOptions;
import org.eclipse.fennec.model.atlas.rest.common.AbstractEPackageMessageBodyHandler;
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

/**
 *
 * @author Jürgen Albert
 * @since 24 Oct 2025
 */
@Component(name = "JSONSchemaMessagebodyReaderWriter", service = { MessageBodyReader.class,
		MessageBodyWriter.class }, enabled = true, scope = ServiceScope.PROTOTYPE)
@JakartarsExtension
@JakartarsName("JSONSchemaMessagebodyReaderWriter")
@JakartarsApplicationSelect("(|(emf=true)(" + JakartarsWhiteboardConstants.JAKARTA_RS_NAME + "=.default))")
@Provider
@Produces("application/schema+json")
@Consumes("application/schema+json")
public class JsonSchemaMessageBodyReaderWriter extends AbstractEPackageMessageBodyHandler {


    private static final Map<String, Object> OPTIONS = Map.ofEntries(Map.entry(CodecJsonSchemaOptions.OPTION_SCHEMA_FEATURE, "definitions"));

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return EPackage.class.isAssignableFrom(type) && "application/schema+json".equals(mediaType.toString());
	}

	@Override
	public void writeTo(EPackage t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
					throws IOException, WebApplicationException {

		ResourceSet resourceSet = getResourceSet();
		Resource resource = resourceSet.createResource(URI.createURI(t.getNsURI()), "application/schema+json");
		// Serialize a copy: adding the served instance itself would detach it from its
		// own resource (registered singletons and storage-loaded packages are shared),
		// corrupting hrefs computed against it afterwards.
		try {
			resource.getContents().add(EcoreUtil.copy(t));
			resource.save(entityStream, OPTIONS);
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
		Resource resource = resourceSet.createResource(URI.createURI("temp.jsonschema"), "application/schema+json");
		resource.load(entityStream, OPTIONS);
		return resource.getContents().isEmpty() ? null : (EPackage) resource.getContents().remove(0);
	}

}
