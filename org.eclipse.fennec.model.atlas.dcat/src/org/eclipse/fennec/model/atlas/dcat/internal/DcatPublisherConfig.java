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
package org.eclipse.fennec.model.atlas.dcat.internal;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * One configuration per portal this atlas publishes to.
 */
@ObjectClassDefinition(name = "Model Atlas DCAT publisher", //
        description = "Publishes this atlas's scopes and schema packages to one DCAT.Atlas portal.")
public @interface DcatPublisherConfig {

    /**
     * {@code dcat.portal.target} — which DCAT.Atlas client to use, e.g.
     * {@code (dcat.portal=jena)}. A second portal is a second configuration, not a redesign.
     */
    String dcat_portal_target();

    /**
     * {@code atlas.public.base.uri} — the entire public prefix, up to and including whatever
     * stands in for the container's own {@code /atlas/rest}. One knob that survives any rewrite
     * the gateway performs, because no id is derived from it. Required.
     */
    String atlas_public_base_uri();

    /**
     * {@code allow.local.base.uri} — tolerate a loopback base URI. A development convenience
     * that must not be the default: a {@code localhost} URL in a public catalogue is worse than
     * no catalogue entry.
     */
    boolean allow_local_base_uri() default false;

    /**
     * {@code scopes} — the scopes this portal publishes. Scopes are opt-in from configuration
     * because a package's own metadata cannot express "this deployment publishes to that
     * portal"; without this gate, uploading a package with a query parameter would be enough to
     * put a model into a public catalogue.
     */
    String[] scopes() default {};

    /** {@code language} — the language tag stamped on generated literals. */
    String language() default "de";

    /**
     * {@code publisher.name} — {@code dct:publisher}. Required by the portal's own model:
     * {@code publisher} is a lowerBound=1 containment on {@code dcat:DcatResource}, so every
     * Catalog and Dataset carries its own contained Agent and nothing is shared.
     */
    String publisher_name() default "";

    /** {@code publisher.about} — the publisher's IRI, set as the Agent's {@code about}. */
    String publisher_about() default "";

    /** {@code catalog.description.template} — used when a scope declares no description. */
    String catalog_description_template() default "Data models published by the %s scope of this Model Atlas.";

    /**
     * {@code license.uri} — {@code dct:license}, rendered as a contained LicenseDocument whose
     * {@code about} is this IRI. Required on every Distribution by the portal's model
     * ({@code license} is a lowerBound=1 containment there), so a Distribution cannot be written
     * without it.
     */
    String license_uri() default "";

    /** {@code theme} — DCAT-AP.de data-theme IRIs. */
    String[] theme() default {};

    /** {@code keywords} — added to the scope/stage keywords the publisher derives. */
    String[] keywords() default {};

    /**
     * {@code publish.stages} — which stages publish. {@code FINAL} (the default) means the
     * registry's final stages only, so a {@code dcat=true} upload to a draft records the intent
     * without putting the model in a portal, and a later promotion publishes it with nobody
     * having to do a second thing. {@code ALL}, or an explicit list of stage names.
     */
    String[] publish_stages() default { "FINAL" };

    /**
     * {@code distribution.media.types} — the allowlist, intersected with what the runtime
     * reports it can actually serve.
     */
    String[] distribution_media_types() default { "application/xmi", "application/json",
            "application/schema+json", "application/schema+xml" };

    /** {@code dataset.description.template} — used when an EPackage carries no documentation. */
    String dataset_description_template() default "The %s data model, served from the %s stage of the %s scope.";

    /**
     * {@code unpublish.mode} — what happens to a Dataset that stops being publishable: the flag
     * cleared, the package deleted, a promotion out of a permitted stage. {@code UNLINK} is the
     * default because it is reversible and cannot destroy what a portal-side editor added.
     */
    UnpublishMode unpublish_mode() default UnpublishMode.UNLINK;

    /**
     * {@code unpublish.delay.seconds} — how long an unbind stays ambiguous. A changed package is
     * republished by unregister-then-register, so a re-register inside this window cancels the
     * retirement; without it every content edit would briefly unpublish its own Dataset. Not a
     * throughput knob: it should comfortably exceed how long a re-register takes.
     */
    int unpublish_delay_seconds() default 30;

    /**
     * {@code retry.initial.delay.seconds} — how long to wait before the first retry of a transient
     * failure: a 503, a connect timeout, or a portal that is not ready yet. The delay doubles per
     * attempt from here.
     */
    int retry_initial_delay_seconds() default 5;

    /**
     * {@code retry.max.delay.seconds} — the ceiling the doubling stops at, so a portal that is down
     * for an hour is polled a handful of times rather than continuously.
     */
    int retry_max_delay_seconds() default 300;

    /**
     * {@code retry.max.attempts} — how many attempts a transient failure gets before it is recorded
     * as abandoned. Bounded so that something which only looks transient does not retry forever;
     * what it gives up on is reported by the health check rather than forgotten.
     */
    int retry_max_attempts() default 6;

    /**
     * {@code retire.on.shutdown} — whether a clean shutdown retires what this publisher put in the
     * portal. {@code false}, because it cannot deliver the guarantee it appears to (a SIGKILL, an
     * OOM kill or a dead host runs no deactivate at all) while charging every routine restart a
     * full retire plus re-publish. A DCAT entry says the model exists and where it is served from,
     * which stays true across a restart. When {@code true}, the shutdown path is forced to
     * {@link UnpublishMode#UNLINK} whatever {@code unpublish.mode} says: a restart must never
     * delete.
     */
    boolean retire_on_shutdown() default false;
}
