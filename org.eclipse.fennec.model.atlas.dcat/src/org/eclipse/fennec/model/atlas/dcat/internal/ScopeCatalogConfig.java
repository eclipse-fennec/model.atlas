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
 * One configuration per scope whose Catalog is adopted from, or described beyond, what the scope
 * itself says. A scope with no such configuration is the derived case and needs none.
 */
@ObjectClassDefinition(name = "Model Atlas DCAT scope catalog", //
        description = "Adopts an existing Catalog for a scope, or describes the one this atlas owns.")
public @interface ScopeCatalogConfig {

    /** {@code scope} — the scope this configuration is about. Required. */
    String scope();

    /**
     * {@code catalog.id} — the portal id. Required when adopting; otherwise optional, and the scope
     * name is used. A published id is permanent, so changing it later leaves the old Catalog behind
     * rather than renaming it.
     */
    String catalog_id() default "";

    /**
     * {@code catalog.adopt} — this Catalog belongs to somebody else. Datasets are linked into it
     * and unlinked from it; it is never written and never deleted, because {@code registerCatalog}
     * is a {@code PUT} and a {@code PUT} would drop every dataset link it holds, other publishers'
     * included.
     */
    boolean catalog_adopt() default false;

    /**
     * {@code catalog.create.if.missing} — create an adopted Catalog the portal does not have.
     * Default {@code false}: the operator asserted the id exists, and minting a Catalog under an id
     * in somebody else's portal is the one failure mode with no clean recovery. When it is missing
     * and this is off, the scope is refused and reported.
     */
    boolean catalog_create_if_missing() default false;

    /** {@code catalog.title} — overrides the scope name. */
    String catalog_title() default "";

    /** {@code catalog.description} — overrides the scope's own description. */
    String catalog_description() default "";

    /** {@code catalog.publisher.name} — overrides the publisher's {@code publisher.name}. */
    String catalog_publisher_name() default "";

    /** {@code catalog.publisher.about} — overrides the publisher's {@code publisher.about}. */
    String catalog_publisher_about() default "";

    /** {@code catalog.license.uri} — {@code dct:license} for the Catalog itself. */
    String catalog_license_uri() default "";

    /** {@code catalog.homepage} — {@code foaf:homepage}. */
    String catalog_homepage() default "";

    /** {@code catalog.theme} — DCAT-AP.de data-theme IRIs for the Catalog. */
    String[] catalog_theme() default {};

    /** {@code catalog.keywords} — keywords for the Catalog. */
    String[] catalog_keywords() default {};
}
