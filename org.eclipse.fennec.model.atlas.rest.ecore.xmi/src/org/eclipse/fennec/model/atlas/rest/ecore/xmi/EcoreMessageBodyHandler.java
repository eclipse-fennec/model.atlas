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
 *      Data In Motion - initial API and implementation
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
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
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
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
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
@JakartarsApplicationSelect("(|(emf=true)(" + JakartarsWhiteboardConstants.JAKARTA_RS_NAME + "=.default))")
@JakartarsName("EcoreMessageBodyHandler")
@Consumes({ "application/xmi", "application/xml" })
@Produces({ "application/xmi", "application/xml" })
public class EcoreMessageBodyHandler extends AbstractEPackageMessageBodyHandler {
	
    private static final Logger logger = Logger.getLogger(EcoreMessageBodyHandler.class.getName());

    /** Scheme of the synthetic base URI the response document is serialized against. */
    private static final String RESPONSE_SCHEME = "atlas";

    /** Authority of that base URI; no other model URI shares it. */
    private static final String RESPONSE_AUTHORITY = "response";

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

        // Use ABSOLUTE URI to prevent "resolve against non-hierarchical or relative
        // base" error
        ResourceSet resourceSet = getResourceSet();
        URI absoluteURI = URI.createURI("temp://governance/" + System.currentTimeMillis() + ".xmi");
        Resource resource = resourceSet.createResource(absoluteURI);

        // Configure XMI loading options for robust parsing
        Map<Object, Object> options = new HashMap<>();
        options.put(XMLResource.OPTION_DEFER_ATTACHMENT, Boolean.TRUE);
        options.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
        options.put(XMLResource.OPTION_LAX_FEATURE_PROCESSING, Boolean.TRUE);
        options.put(XMLResource.OPTION_RECORD_UNKNOWN_FEATURE, Boolean.TRUE);

        // Load the XMI content. A payload this reader cannot turn into a model is the
        // client's mistake, so it is a 400 — not an IOException, which would blame the
        // server for the client's document. The three sibling schema readers answer the
        // same way, through this same check.
        loadPayload(resource, entityStream, options, "XMI");

        EObject rootObject = resource.getContents().get(0);
        logger.log(Level.INFO, "Successfully loaded EObject: {0}", rootObject.getClass().getSimpleName());
        return (EPackage) rootObject;
    }

    // ========== MessageBodyWriter Implementation ==========

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return isReadable(type, genericType, annotations, mediaType);
    }

    @Override
    public void writeTo(EPackage eObject, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {

        logger.log(Level.INFO, "Writing EObject to XMI: type={0}, mediaType={1}",
                new Object[] { eObject.getClass().getSimpleName(), mediaType });

        ResourceSet resourceSet = getResourceSet();
        String fileName = eObject.getName() + (isXMI(mediaType) ? ".ecore" : ".xml");
        httpHeaders.put(HttpHeaders.CONTENT_DISPOSITION, List.of("attachment; filename=" + fileName));
        // Configure XMI saving options
        Map<Object, Object> options = new HashMap<>();

        // Never re-parent the served instance into the response resource: the served
        // EPackage can be a shared/registered instance (a storage-loaded package still
        // attached to its file: resource, or a registered singleton like
        // EcorePackage.eINSTANCE reached via the parent-scope fallback). Moving it here
        // permanently changed its eResource() to a resource named "<name>.ecore", after
        // which every href computed against its elements leaked that URI — clients
        // received eType="... file:/.../ecore.ecore#//EString" instead of the canonical
        // nsURI and could no longer deserialize instances. Serializing the instance's
        // own resource in place is no alternative either: a registered singleton's
        // lazily created resource is a plain ResourceImpl whose save() throws
        // UnsupportedOperationException. So always serialize a self-contained copy —
        // cross-package references stay with the originals (resolved) or keep their
        // canonical proxy URIs (unresolved), and the live instance stays untouched.

        // The response resource must not be named after the file the download is
        // suggested to be saved as: an XMI writer only shortens a reference to a bare
        // fragment when the resource it writes has an absolute, hierarchical URI (see
        // XMLHelperImpl, which switches deresolution off otherwise). Against a bare
        // "<name>.ecore" every reference between two classifiers of this very package
        // came out as a cross-document href, "ecore:EClass <name>.ecore#//Inner"
        // instead of the stored "#//Inner", so the document only resolved as long as
        // the caller saved it under exactly that name. A synthetic absolute base fixes
        // that without becoming visible: same-document references deresolve to "#...",
        // and references into other packages keep their own absolute URIs, since they
        // share no prefix with this scheme. The package's nsURI is not usable as that
        // base — it would relativize a sibling package's URI, e.g. ".../schema/2.0" to
        // "2.0", and a non-hierarchical one (a "urn:" nsURI) would not deresolve at
        // all.
        URI responseURI = URI.createHierarchicalURI(RESPONSE_SCHEME, RESPONSE_AUTHORITY, null,
                new String[] { fileName }, null, null);
        Resource resource = resourceSet.createResource(responseURI, EcorePackage.eCONTENT_TYPE);
        try {
            resource.getContents().add(EcoreUtil.copy(eObject));
            resource.save(entityStream, options);
        } finally {
            resource.getContents().clear();
            resourceSet.getResources().remove(resource);
        }

        logger.log(Level.INFO, "Successfully serialized EObject to XMI");
    }
}