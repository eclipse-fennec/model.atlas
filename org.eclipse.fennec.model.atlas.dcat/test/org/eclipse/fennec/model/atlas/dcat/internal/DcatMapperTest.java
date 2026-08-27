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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.model.atlas.dcat.api.PublicationTarget;
import org.eclipse.fennec.model.atlas.scope.api.ScopeApiFactory;
import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;
import org.junit.jupiter.api.Test;

import dcat.Catalog;
import dcat.Dataset;
import dcat.Distribution;

/**
 * The mapper has one hard requirement: whatever it produces must clear the portal's write floor.
 * {@code publisher} is a lowerBound=1 containment on {@code dcat:DcatResource} and
 * {@code description} is the OCL invariant {@code HasDescription}; a Distribution additionally
 * needs {@code accessURL} and a contained {@code license}. Validate-on-write defaults to on
 * independently of SHACL, so a missing one is rejected at the portal, not here.
 */
class DcatMapperTest {

    private static final String NS = "http://test.example.com/person/1.1";
    private static final String BASE = "https://opendata.example.de/model-atlas";
    /** sha256 of nothing in particular — 64 hex digits is what matters. */
    private static final String FP = "fp1:" + "ab".repeat(32);

    private static DcatMapper mapper() {
        ConfigStub config = ConfigStub.full();
        return new DcatMapper(config, new ConfiguredMetadataSource(config));
    }

    // ---- Catalog ----------------------------------------------------------

    @Test
    void derivedCatalogClearsThePortalWriteFloor() {
        Catalog catalog = mapper().toCatalog(scope("jena", "City Jena Scope"), CatalogSettings.none());

        assertThat(catalog.getTitle()).hasSize(1);
        assertThat(catalog.getTitle().get(0).getValue()).isEqualTo("jena");
        assertThat(catalog.getTitle().get(0).getLang()).isEqualTo("de");
        assertThat(catalog.getDescription().get(0).getValue()).isEqualTo("City Jena Scope");
        assertThat(catalog.getPublisher()).isNotNull();
        assertThat(catalog.getPublisher().getName().get(0).getValue()).isEqualTo("Stadt Jena");
        assertThat(catalog.getPublisher().getAbout()).isEqualTo("https://www.jena.de");
    }

    @Test
    void fallsBackToTheTemplateWhenAScopeDeclaresNoDescription() {
        assertThat(mapper().toCatalog(scope("verkehr", null), CatalogSettings.none()).getDescription().get(0).getValue())
                .isEqualTo("Models of verkehr");
        assertThat(mapper().toCatalog(scope("verkehr", "  "), CatalogSettings.none()).getDescription().get(0).getValue())
                .isEqualTo("Models of verkehr");
    }

    @Test
    void eachEntityCarriesItsOwnAgentBecausePublisherIsContainment() {
        // Sharing one Agent instance across two entities would move it: containment re-parents.
        DcatMapper mapper = mapper();
        assertThat(mapper.toCatalog(scope("a", "d"), CatalogSettings.none()).getPublisher())
                .isNotSameAs(mapper.toCatalog(scope("b", "d"), CatalogSettings.none()).getPublisher());
        assertThat(mapper.toDataset(target("release"), pkg("Person", null)).getPublisher())
                .isNotSameAs(mapper.toCatalog(scope("a", "d"), CatalogSettings.none()).getPublisher());
    }

    @Test
    void aConfiguredCatalogPrefersItsOwnAttributes() {
        // Precedence is DcatScopeCatalog -> ScopeInfo -> publisher defaults, so a configured title
        // wins over the scope name and a configured publisher over the portal-wide one.
        CatalogSettings configured = new CatalogSettings("stadt-jena-opendata", false, false, "Verkehrsmodelle Jena",
                "Datenmodelle des Verkehrsbetriebs", "Verkehrsbetrieb Jena", "https://www.jena.de/verkehr",
                "http://dcat-ap.de/def/licenses/dl-by-de/2.0", "https://opendata.jena.de",
                List.of("http://example.org/theme/TRAN"), List.of("verkehr"), null);

        Catalog catalog = mapper().toCatalog(scope("jena", "City Jena Scope"), configured);

        assertThat(catalog.getTitle().get(0).getValue()).isEqualTo("Verkehrsmodelle Jena");
        assertThat(catalog.getDescription().get(0).getValue()).isEqualTo("Datenmodelle des Verkehrsbetriebs");
        assertThat(catalog.getPublisher().getName().get(0).getValue()).isEqualTo("Verkehrsbetrieb Jena");
        assertThat(catalog.getPublisher().getAbout()).isEqualTo("https://www.jena.de/verkehr");
        assertThat(catalog.getLicense().getAbout()).isEqualTo("http://dcat-ap.de/def/licenses/dl-by-de/2.0");
        assertThat(catalog.getHomepage()).isEqualTo("https://opendata.jena.de");
        assertThat(catalog.getTheme()).containsExactly("http://example.org/theme/TRAN");
        assertThat(catalog.getKeyword().get(0).getValue()).isEqualTo("verkehr");
    }

    @Test
    void aCatalogConfiguringNothingIsExactlyTheDerivedCatalog() {
        // What makes the configured case backward compatible: an empty configuration must not
        // change a single field, or adopting the feature would rewrite every catalogue.
        CatalogSettings empty = new CatalogSettings("some-id", false, false, null, null, null, null, null, null,
                List.of(), List.of(), null);

        Catalog configured = mapper().toCatalog(scope("jena", "City Jena Scope"), empty);
        Catalog derived = mapper().toCatalog(scope("jena", "City Jena Scope"), CatalogSettings.none());

        assertThat(configured.getTitle().get(0).getValue()).isEqualTo(derived.getTitle().get(0).getValue());
        assertThat(configured.getDescription().get(0).getValue())
                .isEqualTo(derived.getDescription().get(0).getValue());
        assertThat(configured.getPublisher().getName().get(0).getValue())
                .isEqualTo(derived.getPublisher().getName().get(0).getValue());
        assertThat(configured.getLicense()).as("the atlas licenses no catalogue it was not told about").isNull();
        assertThat(configured.getTheme()).isEmpty();
    }

    // ---- Dataset ---------------------------------------------------------

    @Test
    void datasetCarriesTheModelIdentityAndTheStageInItsTitle() {
        Dataset dataset = mapper().toDataset(target("release"), pkg("Person", null));

        assertThat(dataset.getTitle().get(0).getValue()).isEqualTo("Person (release)");
        assertThat(dataset.getIdentifier().get(0).getValue()).isEqualTo(NS);
        assertThat(dataset.getVersion()).isEqualTo("1.1.0");
        assertThat(dataset.getPublisher()).isNotNull();
        assertThat(dataset.getDescription()).hasSize(1);
    }

    @Test
    void datasetDescriptionPrefersTheModelsOwnDocumentation() {
        Dataset dataset = mapper().toDataset(target("release"), pkg("Person", "A person, as the city models one."));

        assertThat(dataset.getDescription().get(0).getValue()).isEqualTo("A person, as the city models one.");
    }

    @Test
    void datasetDescriptionFallsBackToTheTemplate() {
        Dataset dataset = mapper().toDataset(target("release"), pkg("Person", null));

        assertThat(dataset.getDescription().get(0).getValue()).isEqualTo("The Person model, release of jena");
    }

    @Test
    void datasetKeywordsMakeTheAtlasStructureSearchable() {
        Dataset dataset = mapper().toDataset(target("release"), pkg("Person", null));

        assertThat(dataset.getKeyword().stream().map(k -> k.getValue()).toList())
                .contains("scope:jena", "stage:release", "modell");
    }

    @Test
    void datasetIsNotPutInASeries() {
        // O2: an EPackage is a Dataset under a Catalog with its Distributions, and nothing above.
        assertThat(mapper().toDataset(target("release"), pkg("Person", null)).getInSeries()).isEmpty();
    }

    // ---- Distribution ----------------------------------------------------

    @Test
    void distributionCarriesBothUrlsAndTheLicence() {
        Distribution distribution = mapper().toDistribution(target("release"), "application/xmi", BASE);

        // The same URL in both, per DCAT's guidance for a distribution whose only access IS the
        // direct download — and with the media type in both, so each Distribution of a Dataset is
        // self-contained rather than sharing an accessURL that matches neither.
        // The register IRIs DCAT-AP mandates, not the served literal: a literal is valid RDF but
        // violates the controlled-vocabulary rule, which a shape reports as a node-kind violation.
        assertThat(distribution.getFormat())
                .isEqualTo("http://publications.europa.eu/resource/authority/file-type/XML");
        // No dcat:mediaType for XMI: application/xmi is not registered, and the registered
        // application/vnd.xmi+xml is not what this atlas serves. Omitting is conformant; claiming a
        // near-enough type is a falsehood a harvester could act on.
        assertThat(distribution.getMediaType()).isNull();
        // The served media type is still readable, which is what the title is for.
        assertThat(distribution.getTitle().getValue()).isEqualTo("application/xmi");
        assertThat(distribution.getAccessURL()).hasSize(1);
        assertThat(distribution.getDownloadURL().get(0)).endsWith("&mediaType=application%2Fxmi");
        assertThat(distribution.getAccessURL().get(0)).isEqualTo(distribution.getDownloadURL().get(0));
        // license is lowerBound=1 containment on a Distribution, so this is not optional.
        assertThat(distribution.getLicense()).isNotNull();
        assertThat(distribution.getLicense().getAbout()).isEqualTo("http://dcat-ap.de/def/licenses/dl-by-de/2.0");
    }

    @Test
    void distributionChecksumIsTheFingerprintAsSpdxSha256() {
        Distribution distribution = mapper().toDistribution(target("release"), "application/xmi", BASE);

        assertThat(distribution.getChecksum()).isNotNull();
        assertThat(distribution.getChecksum().getAlgorithm()).contains("sha256");
        assertThat(distribution.getChecksum().getChecksumValue()).hasSize(32);
    }

    @Test
    void anUnparseableFingerprintYieldsNoChecksumRatherThanAWrongOne() {
        // A wrong checksum in a catalogue is worse than no checksum: a harvester would report the
        // download as corrupt.
        PublicationTarget bad = new PublicationTarget("jena", "release", NS, "1.1.0", "sha1:deadbeef");
        assertThat(mapper().toDistribution(bad, "application/xmi", BASE).getChecksum()).isNull();

        PublicationTarget none = new PublicationTarget("jena", "release", NS, "1.1.0", null);
        assertThat(mapper().toDistribution(none, "application/xmi", BASE).getChecksum()).isNull();
    }

    @Test
    void refusesADistributionWithNoConfiguredLicence() {
        ConfigStub noLicence = new ConfigStub("P", "", "de", "%s", "%s %s %s", "", new String[0], new String[0]);
        DcatMapper mapper = new DcatMapper(noLicence, new ConfiguredMetadataSource(noLicence));

        // Better a named message here than the portal's constraint report, which says the same
        // thing far less legibly.
        assertThatThrownBy(() -> mapper.toDistribution(target("release"), "application/xmi", BASE))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("license.uri is required");
    }

    // ---- helpers ---------------------------------------------------------

    private static PublicationTarget target(String stage) {
        return new PublicationTarget("jena", stage, NS, "1.1.0", FP);
    }

    private static EPackage pkg(String name, String documentation) {
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setName(name);
        ePackage.setNsURI(NS);
        ePackage.setNsPrefix(name.toLowerCase());
        if (documentation != null) {
            EAnnotation annotation = EcoreFactory.eINSTANCE.createEAnnotation();
            annotation.setSource("http://www.eclipse.org/emf/2002/GenModel");
            annotation.getDetails().put("documentation", documentation);
            ePackage.getEAnnotations().add(annotation);
        }
        return ePackage;
    }

    private static ScopeInfo scope(String name, String description) {
        ScopeInfo info = ScopeApiFactory.eINSTANCE.createScopeInfo();
        info.setName(name);
        info.setDescription(description);
        return info;
    }
}
