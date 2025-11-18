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
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.util.UMLUtil;
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

@Component(name = "UMLMessageBodyReaderWriter", service = { MessageBodyReader.class,
		MessageBodyWriter.class }, enabled = true, scope = ServiceScope.SINGLETON)
@JakartarsExtension
@JakartarsName("UMLMessageBodyReaderWriter")
@JakartarsApplicationSelect("(|(emf=true)(" + JakartarsWhiteboardConstants.JAKARTA_RS_NAME + "=.default))")
@Provider
@Produces("application/uml")
@Consumes("application/uml")
public class UMLMessageBodyReaderWriter implements MessageBodyReader<EPackage>, MessageBodyWriter<EPackage>{


	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<ResourceSet> resourceSetFactory;

	/* 
	 * (non-Javadoc)
	 * @see jakarta.ws.rs.ext.MessageBodyWriter#isWriteable(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], jakarta.ws.rs.core.MediaType)
	 */
	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return EPackage.class.isAssignableFrom(type) && "application/uml".equals(mediaType.toString());
	}

	/* 
	 * (non-Javadoc)
	 * @see jakarta.ws.rs.ext.MessageBodyWriter#writeTo(java.lang.Object, java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], jakarta.ws.rs.core.MediaType, jakarta.ws.rs.core.MultivaluedMap, java.io.OutputStream)
	 */
	@Override
	public void writeTo(EPackage t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
					throws IOException, WebApplicationException {

		String fileName = t.getName() + ".uml";
		Collection<Package> convertFromEcore = UMLUtil.convertFromEcore(t, null);
		ResourceSet resourceSet = resourceSetFactory.getService();
		try {
			Resource resource = resourceSet.createResource(URI.createURI(fileName));
			resource.getContents().addAll(convertFromEcore);
			resource.save(entityStream, null);
			resource.getContents().clear();
		} finally {
			resourceSetFactory.ungetService(resourceSet);
		}
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
		ResourceSet resourceSet = resourceSetFactory.getService();
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
			resourceSetFactory.ungetService(resourceSet);
		}
	}
}
