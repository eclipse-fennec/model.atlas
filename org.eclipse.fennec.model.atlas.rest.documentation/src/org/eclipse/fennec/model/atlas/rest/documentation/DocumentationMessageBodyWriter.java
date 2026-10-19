/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
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
package org.eclipse.fennec.model.atlas.rest.documentation;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.emf.util.documentation.generators.apis.EcoreToDocumentationOptions;
import org.eclipse.fennec.emf.util.documentation.generators.apis.EcoreToDocumentationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.JakartarsWhiteboardConstants;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationSelect;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

/**
 * Writes an {@link EPackage} or one of its {@link EClassifier}s as generated documentation.
 *
 * <p>
 * This makes documentation a response format rather than an endpoint of its own: every endpoint
 * that already answers with a package - {@code /{scope}/schema/stages/{stage}/content} and the
 * model converter among them - can serve it, chosen by the {@code Accept} header like any other
 * format. There is no reader half, because documentation does not parse back into a model.
 * </p>
 *
 * <p>
 * Which class diagram is embedded follows a {@code ?diagram=none|mermaid|plantuml} parameter,
 * defaulting to mermaid. A combination no generator implements - PlantUML in HTML, today - is
 * refused with a {@code 400} naming what this deployment does support, rather than answered with a
 * document that quietly has no diagram in it.
 * </p>
 *
 * <p>
 * Unlike the sibling writers for UML and JSON Schema this one needs no {@code ResourceSet}: the
 * generator takes the model element and returns text.
 * </p>
 */
@Component(name = "DocumentationMessageBodyWriter", service = MessageBodyWriter.class, scope = ServiceScope.PROTOTYPE)
@JakartarsExtension
@JakartarsName("DocumentationMessageBodyWriter")
@JakartarsApplicationSelect("(|(emf=true)(" + JakartarsWhiteboardConstants.JAKARTA_RS_NAME + "=.default))")
@Provider
@Produces({ DocumentationFormats.HTML, DocumentationFormats.MARKDOWN })
public class DocumentationMessageBodyWriter implements MessageBodyWriter<EObject> {

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    volatile List<EcoreToDocumentationService> generators;

    @Context
    jakarta.inject.Provider<ContainerRequestContext> requestContextProvider;

    /**
     * Claims every {@link EObject} in a format a bound generator produces, and not only the ones
     * that can be documented.
     *
     * <p>
     * A writer that withdrew here would leave the request with no writer at all, because nothing
     * else in the runtime serves these media types: the codec writers key on a
     * {@code Resource.Factory} and documentation has none, and the request filter has already
     * accepted the {@code Accept} header against one list that spans every endpoint. Jersey can
     * only answer that with a 500, which says nothing a caller can act on (issue #363). Being
     * chosen is what lets {@link #writeTo} answer the {@code 406} instead - the same reason this
     * method is indifferent to the {@code diagram} parameter and its {@code 400}.
     * </p>
     */
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return EObject.class.isAssignableFrom(type) && generatorFor(mediaType) != null;
    }

    @Override
    public void writeTo(EObject t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {

        EcoreToDocumentationService generator = generatorFor(mediaType);
        if (generator == null) {
            // isWriteable said yes and the generator went away between then and now.
            throw new WebApplicationException(Status.NOT_ACCEPTABLE);
        }
        requireDocumentable(t, mediaType);
        EcoreToDocumentationOptions mode = requireSupportedMode(generator, subtype(mediaType), requestedDiagram());

        CharSequence documentation = t instanceof EPackage ePackage ? generator.generateDocumentation(ePackage, mode)
                : generator.generateDocumentation((EClassifier) t, mode);
        entityStream.write(documentation.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Documentation is generated from the model's own description of itself, so only Ecore's own
     * elements can be written. A package and a classifier are what the generators accept.
     */
    private static boolean isDocumentable(Object object) {
        return object instanceof EPackage || object instanceof EClassifier;
    }

    /**
     * Refuses an object there is no documentation to generate from.
     *
     * <p>
     * The caller asked an endpoint that answers with data for a format that only describes a
     * schema. That is a content negotiation the server cannot satisfy, so it is the caller's to
     * fix and the message says what the format does apply to.
     * </p>
     */
    private static void requireDocumentable(EObject object, MediaType mediaType) {
        if (isDocumentable(object)) {
            return;
        }
        throw new WebApplicationException(Response.status(Status.NOT_ACCEPTABLE)
                .entity(subtype(mediaType) + " is generated documentation, which only an EPackage or one of its "
                        + "classifiers has. This resource answered with " + EcoreUtil.getURI(object.eClass()) + ".")
                .type(MediaType.TEXT_PLAIN).build());
    }

    /**
     * The generator that claims this media type, asked rather than assumed: which format a
     * generator produces is its own answer to give, and a deployment may install either, both or
     * neither of them.
     */
    private EcoreToDocumentationService generatorFor(MediaType mediaType) {
        String subtype = subtype(mediaType);
        if (!DocumentationFormats.isDocumentation(subtype)) {
            return null;
        }
        return generators().filter(generator -> generator.canHandleMediaType(subtype)).findFirst().orElse(null);
    }

    private Stream<EcoreToDocumentationService> generators() {
        List<EcoreToDocumentationService> bound = generators;
        return bound == null ? Stream.empty()
                : bound.stream().sorted(Comparator.comparing(generator -> generator.getClass().getName()));
    }

    /** The type and subtype, without the parameters a request may carry on them. */
    private static String subtype(MediaType mediaType) {
        return mediaType == null ? null : mediaType.getType() + "/" + mediaType.getSubtype();
    }

    private String requestedDiagram() {
        jakarta.inject.Provider<ContainerRequestContext> provider = requestContextProvider;
        if (provider == null) {
            return null;
        }
        ContainerRequestContext requestContext = provider.get();
        return requestContext == null ? null
                : requestContext.getUriInfo().getQueryParameters().getFirst(DocumentationFormats.DIAGRAM_PARAMETER);
    }

    /**
     * Resolves the requested diagram against what this generator can actually produce.
     *
     * <p>
     * Both ways of getting it wrong are the caller's to fix, so both are a 400 that says what would
     * have worked: a diagram nobody has heard of, and one this format does not implement. The
     * generator is the authority on the second - the set is its own, and it grows without this
     * bundle being touched.
     * </p>
     */
    private static EcoreToDocumentationOptions requireSupportedMode(EcoreToDocumentationService generator,
            String mediaType, String diagram) {
        EcoreToDocumentationOptions mode = DocumentationFormats.modeFor(mediaType, diagram);
        if (mode == null) {
            throw badRequest("Unknown diagram [" + diagram + "]. Supported: "
                    + String.join(", ", DocumentationFormats.diagrams()));
        }
        if (!generator.getSupportedModes().contains(mode)) {
            throw badRequest("Diagram [" + diagram + "] is not available for " + mediaType + ". Supported: "
                    + String.join(", ", supportedDiagrams(generator, mediaType)));
        }
        return mode;
    }

    private static List<String> supportedDiagrams(EcoreToDocumentationService generator, String mediaType) {
        return DocumentationFormats.diagrams().stream()
                .filter(diagram -> generator.getSupportedModes().contains(
                        DocumentationFormats.modeFor(mediaType, diagram)))
                .toList();
    }

    private static WebApplicationException badRequest(String message) {
        return new WebApplicationException(
                Response.status(Status.BAD_REQUEST).entity(message).type(MediaType.TEXT_PLAIN).build());
    }

}
