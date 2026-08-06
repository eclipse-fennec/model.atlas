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
package org.eclipse.fennec.model.atlas.rest.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Map;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import jakarta.inject.Provider;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;

/**
 * Base class for {@link EPackage} {@link MessageBodyReader}/
 * {@link MessageBodyWriter} implementations that need a {@link ResourceSet}
 * for de/serialization.
 *
 * <p>The scope/stage-specific {@link ResourceSet} is resolved per request by
 * the codec's {@code CodecResourceSetFeature}, which binds {@code ResourceSet}
 * in the JAX-RS request scope via the highest-ranked {@code ResourceSetProvider}
 * (the Model Atlas {@code ScopedResourceSetProvider}). The codec's
 * {@code CodecResourceSetCleanupFilter} releases it back to its OSGi prototype
 * CSO after the response has been written.</p>
 *
 * <p>Because MBR/MBW components are JAX-RS providers (effectively singletons),
 * a request-scoped {@link ResourceSet} cannot be injected into a plain field —
 * it would be {@code null}. Instead a {@link Provider} is injected and
 * {@link #getResourceSet()} resolves the current request's instance on every
 * call. This is safe under concurrent requests.</p>
 */
public abstract class AbstractEPackageMessageBodyHandler
        implements MessageBodyReader<EPackage>, MessageBodyWriter<EPackage> {

    @Context
    private Provider<ResourceSet> resourceSetProvider;

    /**
     * Resolves the {@link ResourceSet} for the current request. Within a single
     * request the same instance is returned on repeated calls (request scope).
     *
     * @return the per-request {@link ResourceSet}; never {@code null} in a
     *         correctly wired runtime
     */
    protected ResourceSet getResourceSet() {
        return resourceSetProvider.get();
    }

    /**
     * Compares type and subtype only, case-insensitively, so a request carrying
     * parameters still matches: {@code application/uml;charset=UTF-8} is the
     * {@code application/uml} handler's media type.
     *
     * @param mediaType the media type of the request or response, may be
     *                  {@code null}
     * @param expected  the media type this handler serves
     * @return {@code true} if both name the same type and subtype
     */
    protected static boolean isMediaType(MediaType mediaType, MediaType expected) {
        return mediaType != null && expected.getType().equalsIgnoreCase(mediaType.getType())
                && expected.getSubtype().equalsIgnoreCase(mediaType.getSubtype());
    }

    /**
     * Loads a request payload into {@code resource}, rejecting anything that is not a
     * readable model of this handler's format.
     *
     * <p>
     * A body this handler cannot parse is the client's mistake, so every handler answers
     * it with {@code 400}. The four schema handlers used to disagree about that for the
     * very same condition: the XMI one threw {@link java.io.IOException} (a 500, blaming
     * the server for the client's document) while the JSON-schema, UML and XSD ones
     * returned {@code null}, which the endpoint then dereferenced into a 500 from an NPE
     * further in. Those three also never inspected {@link Resource#getErrors()}, so a
     * partially parsed document passed for a good one.
     * </p>
     *
     * <p>
     * A failure raised while parsing is treated the same as one recorded on the resource:
     * the formats differ in which they use — EMF wraps a parse error in an
     * {@code IOWrappedException}, the JSON codec throws its own unchecked read exception,
     * and the XMI reader records diagnostics instead — but all three describe the bytes
     * the client sent. Their message is passed on, because it names where the document
     * broke and is what makes the 400 actionable.
     * </p>
     *
     * @param resource     the resource to load into
     * @param entityStream the request payload
     * @param options      load options, may be {@code null}
     * @param format       the payload's format, named in error messages (e.g. {@code XMI})
     * @throws BadRequestException if the payload is empty, unparseable, or yields nothing
     * @throws IOException         if the stream itself cannot be read
     */
    protected static void loadPayload(Resource resource, InputStream entityStream, Map<?, ?> options, String format)
            throws IOException {
        // An empty body never becomes a model, and not every format's parser says so:
        // the JSON-schema codec answers an empty stream with an empty EPackage.
        PushbackInputStream stream = new PushbackInputStream(entityStream);
        int firstByte = stream.read();
        if (firstByte == -1) {
            throw new BadRequestException("No content found in the " + format + " payload");
        }
        stream.unread(firstByte);

        try {
            resource.load(stream, options);
        } catch (IOException | RuntimeException e) {
            throw new BadRequestException("The " + format + " payload could not be read: " + e.getMessage(), e);
        }

        if (!resource.getErrors().isEmpty()) {
            StringBuilder message = new StringBuilder("The ").append(format).append(" payload could not be read: ");
            for (Resource.Diagnostic error : resource.getErrors()) {
                message.append(error.getMessage()).append("; ");
            }
            throw new BadRequestException(message.toString());
        }
        if (resource.getContents().isEmpty()) {
            throw new BadRequestException("No content found in the " + format + " payload");
        }
    }
}
