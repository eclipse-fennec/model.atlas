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

import java.lang.annotation.Annotation;

/**
 * A hand-written stand-in for the annotation-backed configuration, so the mapper and its helpers
 * can be exercised without ConfigAdmin.
 */
record ConfigStub(String publisherName, String publisherAbout, String lang, String catalogTemplate,
        String datasetTemplate, String licenseUri, String[] themes, String[] keywords)
        implements DcatPublisherConfig {

    /** The common case: everything configured. */
    static ConfigStub full() {
        return new ConfigStub("Stadt Jena", "https://www.jena.de", "de", "Models of %s", "The %s model, %s of %s",
                "http://dcat-ap.de/def/licenses/dl-by-de/2.0", new String[] { "http://example.org/theme/TECH" },
                new String[] { "modell" });
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return DcatPublisherConfig.class;
    }

    @Override
    public String dcat_portal_target() {
        return "(dcat.portal=test)";
    }

    @Override
    public String atlas_public_base_uri() {
        return "https://example.de/model-atlas";
    }

    @Override
    public boolean allow_local_base_uri() {
        return false;
    }

    @Override
    public String[] scopes() {
        return new String[0];
    }

    @Override
    public String language() {
        return lang;
    }

    @Override
    public String publisher_name() {
        return publisherName;
    }

    @Override
    public String publisher_about() {
        return publisherAbout;
    }

    @Override
    public String catalog_description_template() {
        return catalogTemplate;
    }

    @Override
    public String license_uri() {
        return licenseUri;
    }

    @Override
    public String[] theme() {
        return themes;
    }

    @Override
    public String[] keywords() {
        return keywords;
    }

    @Override
    public String[] publish_stages() {
        return new String[] { "FINAL" };
    }

    @Override
    public String[] distribution_media_types() {
        return new String[] { "application/xmi" };
    }

    @Override
    public String dataset_description_template() {
        return datasetTemplate;
    }

    @Override
    public UnpublishMode unpublish_mode() {
        return UnpublishMode.UNLINK;
    }

    @Override
    public int unpublish_delay_seconds() {
        return 30;
    }

    @Override
    public boolean retire_on_shutdown() {
        return false;
    }
}
