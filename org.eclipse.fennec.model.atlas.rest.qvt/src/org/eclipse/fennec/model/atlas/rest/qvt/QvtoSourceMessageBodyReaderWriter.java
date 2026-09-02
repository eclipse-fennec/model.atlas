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
package org.eclipse.fennec.model.atlas.rest.qvt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.URI;
import org.eclipse.fennec.m2x.model.compiled.CompiledFactory;
import org.eclipse.fennec.m2x.model.compiled.SourceUnit;
import org.eclipse.fennec.m2x.unit.fingerprint.DefaultUnitFingerprintService;
import org.eclipse.fennec.m2x.unit.store.StoredSource;
import org.eclipse.fennec.model.atlas.qvt.QvtUnits;
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
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

/**
 * Carries {@code .qvto} text through the generic registry endpoints as
 * {@link SourceUnit} documents (issue #239): an upload with
 * {@code Content-Type: text/x-qvto} arrives as plain transformation text and
 * is wrapped into a {@code SourceUnit} (the qualified name is read from the
 * source's {@code transformation}/{@code library} header, the source
 * fingerprint is computed), a read with {@code Accept: text/x-qvto} answers
 * the stored text unchanged — the editor round trip.
 */
@Component(name = "QvtoSourceMessageBodyReaderWriter", service = { MessageBodyReader.class,
        MessageBodyWriter.class }, enabled = true, scope = ServiceScope.PROTOTYPE)
@JakartarsExtension
@JakartarsName("QvtoSourceMessageBodyReaderWriter")
@JakartarsApplicationSelect("(|(emf=true)(" + JakartarsWhiteboardConstants.JAKARTA_RS_NAME + "=.default))")
@Provider
@Produces(QvtoSourceMessageBodyReaderWriter.TEXT_QVTO)
@Consumes(QvtoSourceMessageBodyReaderWriter.TEXT_QVTO)
public class QvtoSourceMessageBodyReaderWriter
        implements MessageBodyReader<SourceUnit>, MessageBodyWriter<SourceUnit> {

    public static final String TEXT_QVTO = "text/x-qvto";
    private static final MediaType TEXT_QVTO_TYPE = new MediaType("text", "x-qvto");

    /**
     * The unit name a source is imported by: the first {@code transformation} or
     * {@code library} declaration outside comments. Verified authoritatively by
     * the compile action; this extraction only names the uploaded document.
     */
    private static final Pattern UNIT_NAME = Pattern
            .compile("\\b(?:transformation|library)\\s+([A-Za-z_$][\\w$]*(?:\\.[A-Za-z_$][\\w$]*)*)");
    // QVT-O knows /* */ blocks and both line-comment forms, -- and //
    private static final Pattern COMMENTS = Pattern.compile("/\\*.*?\\*/|--[^\\r\\n]*|//[^\\r\\n]*",
            Pattern.DOTALL);

    private final DefaultUnitFingerprintService fingerprints = new DefaultUnitFingerprintService();

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.isAssignableFrom(SourceUnit.class) && TEXT_QVTO_TYPE.isCompatible(mediaType);
    }

    @Override
    public SourceUnit readFrom(Class<SourceUnit> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {
        String source = new String(entityStream.readAllBytes(), charsetOf(mediaType));
        Optional<String> qualifiedName = unitNameOf(source);
        if (qualifiedName.isEmpty()) {
            // the client's mistake: answer 400 rather than null, which the endpoint
            // would dereference into a 500 — same rule as the schema readers
            throw new WebApplicationException(
                    "The QVT-O source declares no transformation or library, so it has no unit name",
                    Response.Status.BAD_REQUEST);
        }
        SourceUnit document = CompiledFactory.eINSTANCE.createSourceUnit();
        document.setLanguage(QvtUnits.LANGUAGE_QVTO);
        document.setQualifiedName(qualifiedName.get());
        document.setUri("atlas:/" + qualifiedName.get() + ".qvto");
        document.setSource(source);
        document.setFingerprint(fingerprints.fingerprint(
                new StoredSource(qualifiedName.get(), URI.createURI(document.getUri()), source)));
        return document;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return SourceUnit.class.isAssignableFrom(type) && TEXT_QVTO_TYPE.isCompatible(mediaType);
    }

    @Override
    public void writeTo(SourceUnit document, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        httpHeaders.putSingle(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=" + document.getQualifiedName() + ".qvto");
        entityStream.write((document.getSource() != null ? document.getSource() : "")
                .getBytes(charsetOf(mediaType)));
    }

    static Optional<String> unitNameOf(String source) {
        Matcher matcher = UNIT_NAME.matcher(COMMENTS.matcher(source).replaceAll(" "));
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    private static java.nio.charset.Charset charsetOf(MediaType mediaType) {
        String charset = mediaType == null ? null : mediaType.getParameters().get(MediaType.CHARSET_PARAMETER);
        return charset != null ? java.nio.charset.Charset.forName(charset) : StandardCharsets.UTF_8;
    }
}
