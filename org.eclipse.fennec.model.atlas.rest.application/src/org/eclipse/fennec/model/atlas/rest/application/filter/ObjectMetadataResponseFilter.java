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
package org.eclipse.fennec.model.atlas.rest.application.filter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Centralised response filter that stamps HTTP caching validators
 * ({@code ETag}, {@code Last-Modified}) onto cacheable responses and handles
 * conditional GETs ({@code If-None-Match} / {@code If-Modified-Since}).
 *
 * <p>Resources do not build these headers, nor evaluate preconditions, themselves.
 * Instead they attach the {@link ObjectMetadata} backing a response as a request
 * property via {@link #attach(ContainerRequestContext, ObjectMetadata)}; this filter
 * reads it back and derives everything from it. Keeping the logic here means every
 * resource opts in the same way and the caching semantics live in a single place.
 *
 * <ul>
 *   <li>{@code ETag} is a strong validator over the response's underlying state and the negotiated
 *       representation (response {@code Content-Type}). What the state is depends on what the response
 *       carries, signalled by the {@link CacheTarget} a resource attaches:
 *       <ul>
 *         <li>{@link CacheTarget#CONTENT} (default): the validator is {@link ObjectMetadata#getContentHash()}.</li>
 *         <li>{@link CacheTarget#METADATA}: the validator is a strong hash over
 *             {@code (contentHash, version, status, lastChangeTime)}, so a metadata-only change such as a
 *             stage transition (which leaves the bytes untouched) invalidates the metadata cache without
 *             invalidating the content cache, and vice-versa.</li>
 *       </ul>
 *       Folding the media type in on top of that means the same object served as XMI and as JSON gets
 *       distinct ETags, so a shared cache cannot return the wrong representation.</li>
 *   <li>{@code Last-Modified} is derived from {@link ObjectMetadata#getLastChangeTime()}.</li>
 *   <li>{@code Vary: Accept} is set on every cacheable response (including {@code 304}) so intermediaries
 *       key their cache entries on the requested representation.</li>
 *   <li>On a safe ({@code GET}/{@code HEAD}) request whose preconditions match, the
 *       {@code 200} response is rewritten to {@code 304 Not Modified} with no body,
 *       retaining the validators.</li>
 * </ul>
 *
 * <p>The validators are only set when absent, so a resource that already emitted one is
 * never overwritten. Conditional evaluation follows RFC 7232: {@code If-None-Match} takes
 * precedence over {@code If-Modified-Since}; when {@code If-None-Match} is present,
 * {@code If-Modified-Since} is ignored.
 *
 * @author Data In Motion
 * @since 1.0
 */
@Component
@JakartarsExtension
@JakartarsName("ObjectMetadataResponseFilter")
public class ObjectMetadataResponseFilter implements ContainerResponseFilter {

    /**
     * Request-property key under which a resource stashes the {@link ObjectMetadata}
     * that should drive the caching headers for the current response.
     */
    public static final String PROP_OBJECT_METADATA = "org.eclipse.fennec.model.atlas.rest.objectMetadata";

    /**
     * Request-property key under which a resource records which {@link CacheTarget} the response
     * represents, so the filter derives the right ETag validator.
     */
    public static final String PROP_CACHE_TARGET = "org.eclipse.fennec.model.atlas.rest.cacheTarget";

    /**
     * What a cacheable response actually carries, which decides the ETag validator the filter derives.
     */
    public enum CacheTarget {
        /** The response body is the object's content (e.g. an {@code EPackage}); ETag tracks the content hash. */
        CONTENT,
        /** The response body is the object's {@link ObjectMetadata}; ETag tracks the metadata state. */
        METADATA
    }

    /**
     * Attach the metadata that backs the current <em>content</em> response so the filter can derive
     * caching headers and evaluate preconditions from it. Equivalent to
     * {@link #attach(ContainerRequestContext, ObjectMetadata, CacheTarget)} with
     * {@link CacheTarget#CONTENT}.
     *
     * @param requestContext the current request context
     * @param metadata       the metadata describing the response entity
     */
    public static void attach(ContainerRequestContext requestContext, ObjectMetadata metadata) {
        attach(requestContext, metadata, CacheTarget.CONTENT);
    }

    /**
     * Attach the metadata that backs the current response so the filter can derive caching headers and
     * evaluate preconditions from it. No-op when either {@code requestContext} or {@code metadata} is
     * {@code null}.
     *
     * @param requestContext the current request context
     * @param metadata       the metadata describing the response entity
     * @param target         what the response carries (content vs. metadata); a {@code null} is treated
     *                       as {@link CacheTarget#CONTENT}
     */
    public static void attach(ContainerRequestContext requestContext, ObjectMetadata metadata, CacheTarget target) {
        if (requestContext != null && metadata != null) {
            requestContext.setProperty(PROP_OBJECT_METADATA, metadata);
            requestContext.setProperty(PROP_CACHE_TARGET, target == null ? CacheTarget.CONTENT : target);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Object attached = requestContext.getProperty(PROP_OBJECT_METADATA);
        if (!(attached instanceof ObjectMetadata metadata)) {
            return;
        }
        CacheTarget target = requestContext.getProperty(PROP_CACHE_TARGET) instanceof CacheTarget t ? t
                : CacheTarget.CONTENT;

        String etagValue = etagValue(metadata, responseContext.getMediaType(), target);

        if (responseContext.getEntityTag() == null && etagValue != null) {
            responseContext.getHeaders().putSingle(HttpHeaders.ETAG, new EntityTag(etagValue));
        }
        if (responseContext.getLastModified() == null && metadata.getLastChangeTime() != null) {
            responseContext.getHeaders().putSingle(HttpHeaders.LAST_MODIFIED, Date.from(metadata.getLastChangeTime()));
        }
        // Representations differ by Accept (XMI vs JSON vs …). Mark the response so caches key on it.
        // Set before any 304 rewrite so the 304 carries it too.
        addVaryAccept(responseContext);

        if (isConditionalGet(requestContext, responseContext) && isNotModified(requestContext, metadata, etagValue)) {
            // Validators are already on the response; drop the body and switch to 304.
            responseContext.setStatus(Response.Status.NOT_MODIFIED.getStatusCode());
            responseContext.setEntity(null);
        }
    }

    /**
     * The strong ETag value for a response: the target-specific validator folded together with the
     * negotiated representation, so the same object served under different {@code Accept} types yields
     * distinct ETags. Falls back to the bare validator when the media type is unknown, and to
     * {@code null} (no ETag) when no validator can be derived.
     */
    private static String etagValue(ObjectMetadata metadata, MediaType mediaType, CacheTarget target) {
        String validator = baseValidator(metadata, target);
        if (validator == null) {
            return null;
        }
        if (mediaType == null) {
            return validator;
        }
        return validator + "." + mediaTypeToken(mediaType);
    }

    /**
     * The <em>unfolded</em> ETag base validator for the given target — the representation-independent
     * token a write should use for an optimistic-concurrency ({@code If-Match}) check. This is the same
     * value the emitted ETag is derived from, before the media-type fold is applied, so the precondition
     * path and the GET/cache path cannot drift.
     *
     * <ul>
     *   <li>{@link CacheTarget#CONTENT}: {@link ObjectMetadata#getContentHash()}.</li>
     *   <li>{@link CacheTarget#METADATA}: {@link #metadataValidator(ObjectMetadata)}.</li>
     * </ul>
     *
     * @return the base validator, or {@code null} when none can be derived (or {@code metadata} is null)
     */
    public static String baseValidator(ObjectMetadata metadata, CacheTarget target) {
        if (metadata == null) {
            return null;
        }
        return target == CacheTarget.METADATA ? metadataValidator(metadata) : metadata.getContentHash();
    }

    /**
     * Evaluates an {@code If-Match} header value against a base validator for an optimistic-concurrency
     * check. The header may be {@code *} (matches any existing representation), a single tag, or a
     * comma-separated list; weak ({@code W/}) prefixes and quotes are stripped. A tag matches when it
     * equals the {@code base} <em>or</em> is a representation-folded form of it ({@code base + "." + …}),
     * so a client may echo back the (folded) ETag it received from any representation and still satisfy
     * the precondition. A {@code null} header (no precondition) or {@code null} base (nothing to validate
     * against) is treated as satisfied.
     *
     * @param headerValue the raw {@code If-Match} header value, or {@code null}
     * @param base        the base validator from {@link #baseValidator(ObjectMetadata, CacheTarget)}
     * @return {@code true} if the write may proceed; {@code false} → caller should answer {@code 412}
     */
    public static boolean ifMatchSatisfied(String headerValue, String base) {
        if (headerValue == null || base == null) {
            return true;
        }
        String header = headerValue.trim();
        if ("*".equals(header)) {
            return true;
        }
        for (String token : header.split(",")) {
            String tag = token.trim();
            if (tag.startsWith("W/")) {
                tag = tag.substring(2).trim();
            }
            tag = tag.replace("\"", "");
            if (tag.equals(base) || tag.startsWith(base + ".")) {
                return true;
            }
        }
        return false;
    }

    /**
     * The metadata validator: a strong (SHA-256) hash over
     * {@code (contentHash, version, status, lastChangeTime)}. This is independent of the content hash
     * alone, so a metadata-only change (e.g. a stage transition that doesn't touch the bytes) changes
     * the metadata ETag while leaving the content ETag untouched. Returns {@code null} only when none
     * of the inputs is set.
     */
    private static String metadataValidator(ObjectMetadata metadata) {
        String contentHash = metadata.getContentHash();
        String version = metadata.getVersion();
        Object status = metadata.getStatus();
        Object lastChange = metadata.getLastChangeTime();
        if (contentHash == null && version == null && status == null && lastChange == null) {
            return null;
        }
        String raw = contentHash + "|" + version + "|" + status + "|" + lastChange;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is a required JCE algorithm; if it is somehow absent, fall back to the raw form.
            return raw;
        }
    }

    /**
     * A filesystem/header-safe token for a media type's {@code type/subtype} (parameters such as
     * {@code charset} are ignored), e.g. {@code application/json} → {@code application_json}.
     */
    private static String mediaTypeToken(MediaType mediaType) {
        return (mediaType.getType() + "_" + mediaType.getSubtype()).replaceAll("[^A-Za-z0-9_.-]", "_");
    }

    /**
     * Adds {@code Vary: Accept} unless the response already varies on {@code Accept}.
     */
    private void addVaryAccept(ContainerResponseContext responseContext) {
        List<Object> vary = responseContext.getHeaders().get(HttpHeaders.VARY);
        if (vary != null) {
            for (Object value : vary) {
                if (value != null && value.toString().toLowerCase().contains("accept")) {
                    return;
                }
            }
        }
        responseContext.getHeaders().add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
    }

    /**
     * A conditional GET is a safe ({@code GET}/{@code HEAD}) request that produced a
     * {@code 200 OK} and carries at least one of the supported precondition headers.
     */
    private boolean isConditionalGet(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        String method = requestContext.getMethod();
        if (!HttpMethod.GET.equals(method) && !HttpMethod.HEAD.equals(method)) {
            return false;
        }
        if (responseContext.getStatus() != Response.Status.OK.getStatusCode()) {
            return false;
        }
        return requestContext.getHeaderString(HttpHeaders.IF_NONE_MATCH) != null
                || requestContext.getHeaderString(HttpHeaders.IF_MODIFIED_SINCE) != null;
    }

    /**
     * Evaluates {@code If-None-Match} / {@code If-Modified-Since} against the metadata.
     * Returns {@code true} when the client's cached copy is still current (→ {@code 304}).
     * {@code etagValue} is the representation-specific ETag computed for this response, so the
     * comparison honours the same media-type folding the emitted ETag uses.
     */
    private boolean isNotModified(ContainerRequestContext requestContext, ObjectMetadata metadata, String etagValue) {
        String ifNoneMatch = requestContext.getHeaderString(HttpHeaders.IF_NONE_MATCH);
        if (ifNoneMatch != null) {
            // RFC 7232: when If-None-Match is present, If-Modified-Since is ignored.
            return ifNoneMatchMatches(ifNoneMatch, etagValue);
        }
        String ifModifiedSince = requestContext.getHeaderString(HttpHeaders.IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && metadata.getLastChangeTime() != null) {
            Long since = parseHttpDateEpochSecond(ifModifiedSince);
            if (since != null) {
                // HTTP dates have second precision; compare truncated to seconds.
                return metadata.getLastChangeTime().getEpochSecond() <= since;
            }
        }
        return false;
    }

    /**
     * Matches an {@code If-None-Match} header value (which may be {@code *}, a single tag,
     * or a comma-separated list of strong/weak tags) against the current ETag value.
     */
    private boolean ifNoneMatchMatches(String headerValue, String etagValue) {
        if (etagValue == null) {
            return false;
        }
        String header = headerValue.trim();
        if ("*".equals(header)) {
            return true;
        }
        for (String token : header.split(",")) {
            String tag = token.trim();
            if (tag.startsWith("W/")) {
                tag = tag.substring(2).trim();
            }
            tag = tag.replace("\"", "");
            if (tag.equals(etagValue)) {
                return true;
            }
        }
        return false;
    }

    private Long parseHttpDateEpochSecond(String httpDate) {
        try {
            return ZonedDateTime.parse(httpDate, DateTimeFormatter.RFC_1123_DATE_TIME).toEpochSecond();
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
