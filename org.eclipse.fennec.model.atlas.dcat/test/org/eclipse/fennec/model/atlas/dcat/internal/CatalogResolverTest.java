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

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * The three cases of §7a, and the one question that matters about each: may we write it?
 */
public class CatalogResolverTest {

    @Test
    public void aScopeWithNoConfigurationIsDerivedAndOurs() {
        ResolvedCatalog resolved = CatalogResolver.resolve("jena", null);

        assertThat(resolved.owned()).isTrue();
        assertThat(resolved.id()).as("the scope name is already a URL path segment in the atlas API").isEqualTo("jena");
        assertThat(resolved.settings()).isEqualTo(CatalogSettings.none());
    }

    @Test
    public void anEmptyConfigurationIsStillTheDerivedCase() {
        // What makes §7a backward compatible: configuring a scope without saying anything about it
        // must behave exactly as before.
        ResolvedCatalog resolved = CatalogResolver.resolve("jena", CatalogSettings.of(config("jena")));

        assertThat(resolved.owned()).isTrue();
        assertThat(resolved.id()).isEqualTo("jena");
    }

    @Test
    public void aConfiguredIdReplacesTheScopeName() {
        ScopeCatalogConfigStub config = config("jena");
        config.catalogId = "stadt-jena-opendata";

        ResolvedCatalog resolved = CatalogResolver.resolve("jena", CatalogSettings.of(config));

        assertThat(resolved.owned()).as("a configured Catalog is still ours to write").isTrue();
        assertThat(resolved.id()).isEqualTo("stadt-jena-opendata");
    }

    @Test
    public void anAdoptedCatalogIsNotOurs() {
        ScopeCatalogConfigStub config = config("jena");
        config.catalogId = "stadt-jena-opendata";
        config.adopt = true;

        ResolvedCatalog resolved = CatalogResolver.resolve("jena", CatalogSettings.of(config));

        assertThat(resolved.adopted()).isTrue();
        assertThat(resolved.owned()).as("a PUT on it would drop every dataset link it holds").isFalse();
        assertThat(resolved.id()).isEqualTo("stadt-jena-opendata");
    }

    @Test
    public void adoptingWithoutAnIdRefusesTheScope() {
        // The alternative — deriving the id — answers "adopt somebody's Catalog" by writing our own
        // under a name the operator never chose. Refusing is the only safe reading.
        ScopeCatalogConfigStub config = config("jena");
        config.adopt = true;

        ResolvedCatalog resolved = CatalogResolver.resolve("jena", CatalogSettings.of(config));

        assertThat(resolved.refused()).isTrue();
        assertThat(resolved.id()).isNull();
        assertThat(resolved.refusalReason()).contains("catalog.adopt", "catalog.id");
    }

    @Test
    public void aBlankIdIsNoId() {
        ScopeCatalogConfigStub config = config("jena");
        config.catalogId = "   ";
        config.adopt = true;

        assertThat(CatalogResolver.resolve("jena", CatalogSettings.of(config)).refused()).isTrue();
    }

    @Test
    public void createIfMissingIsOffUnlessAskedFor() {
        ScopeCatalogConfigStub config = config("jena");
        config.catalogId = "foreign";
        config.adopt = true;

        assertThat(CatalogResolver.resolve("jena", CatalogSettings.of(config)).settings().createIfMissing())
                .as("minting a Catalog in somebody else's portal has no clean recovery").isFalse();
    }

    // ---- helpers ----------------------------------------------------------

    private static ScopeCatalogConfigStub config(String scope) {
        ScopeCatalogConfigStub stub = new ScopeCatalogConfigStub();
        stub.scope = scope;
        return stub;
    }

    /** A settable stand-in for the annotation-backed configuration. */
    private static final class ScopeCatalogConfigStub implements ScopeCatalogConfig {

        private String scope = "";
        private String catalogId = "";
        private boolean adopt;
        private boolean createIfMissing;

        @Override
        public Class<? extends Annotation> annotationType() {
            return ScopeCatalogConfig.class;
        }

        @Override
        public String scope() {
            return scope;
        }

        @Override
        public String catalog_id() {
            return catalogId;
        }

        @Override
        public boolean catalog_adopt() {
            return adopt;
        }

        @Override
        public boolean catalog_create_if_missing() {
            return createIfMissing;
        }

        @Override
        public String catalog_title() {
            return "";
        }

        @Override
        public String catalog_description() {
            return "";
        }

        @Override
        public String catalog_publisher_name() {
            return "";
        }

        @Override
        public String catalog_publisher_about() {
            return "";
        }

        @Override
        public String catalog_license_uri() {
            return "";
        }

        @Override
        public String catalog_homepage() {
            return "";
        }

        @Override
        public String[] catalog_theme() {
            return new String[0];
        }

        @Override
        public String[] catalog_keywords() {
            return new String[0];
        }
    }

    @Test
    public void configuredAttributesSurviveResolution() {
        ScopeCatalogConfigStub config = config("verkehr");
        ResolvedCatalog resolved = CatalogResolver.resolve("verkehr", CatalogSettings.of(config));

        assertThat(resolved.settings().themes()).isEqualTo(List.of());
        assertThat(resolved.settings().valid()).isTrue();
    }
}
