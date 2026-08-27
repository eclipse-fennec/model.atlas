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

import java.util.Optional;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.dcat.api.DcatMetadataSource;
import org.eclipse.fennec.model.atlas.dcat.api.PublicationTarget;
import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;

import dcat.Catalog;
import dcat.Dataset;
import dcat.DcatFactory;
import dcat.Distribution;
import foaf.Agent;
import foaf.FoafFactory;
import spdx.Checksum;
import spdx.SpdxFactory;
import terms.LicenseDocument;
import terms.TermsFactory;
import rdf.PlainLiteral;
import rdf.RdfFactory;

/**
 * Turns atlas facts plus configured defaults into DCAT entities.
 *
 * <p>
 * The write floor is set by the portal's own model rather than by SHACL: {@code publisher} is a
 * lowerBound=1 <em>containment</em> on {@code dcat:DcatResource} and {@code description} is the
 * OCL invariant {@code HasDescription}, with validate-on-write defaulting to on. So every
 * Catalog needs a title, a description and a contained Agent that has its own name — and because
 * the publisher is containment, each entity carries its own copy of that Agent.
 * </p>
 */
final class DcatMapper {

    /** {@code fp1:} fingerprints are sha256, and this is SPDX's IRI for that algorithm. */
    private static final String SPDX_SHA256 = "http://spdx.org/rdf/terms#checksumAlgorithm_sha256";

    private final DcatPublisherConfig config;
    private final DcatMetadataSource metadata;

    DcatMapper(DcatPublisherConfig config, DcatMetadataSource metadata) {
        this.config = config;
        this.metadata = metadata;
    }

    /**
     * The Catalog for one scope.
     *
     * <p>
     * Metadata precedence is {@code DcatScopeCatalog} attributes, then {@link ScopeInfo}, then the
     * publisher's own defaults — so a scope that configures nothing produces exactly the derived
     * Catalog, which is what makes the configured case backward compatible.
     * </p>
     *
     * <p>
     * Only ever called for a Catalog this atlas owns. An adopted one is never written, so it is
     * never mapped.
     * </p>
     *
     * @param scope    the scope to describe
     * @param settings its Catalog configuration, or {@link CatalogSettings#none()}
     * @return a Catalog that satisfies the portal's write floor
     */
    Catalog toCatalog(ScopeInfo scope, CatalogSettings settings) {
        Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
        catalog.getTitle().add(literal(settings.titleOrEmpty().orElseGet(scope::getName)));
        catalog.getDescription().add(literal(settings.descriptionOrEmpty().orElseGet(() -> description(scope))));
        catalog.setPublisher(publisher(settings.publisherNameOrEmpty().orElseGet(config::publisher_name),
                settings.publisherAboutOrEmpty().orElseGet(config::publisher_about)));
        // A Catalog's own licence and themes are configuration only: the atlas has no opinion about
        // either, and inventing one from the publisher's Dataset defaults would state a licence
        // over a catalogue nobody licensed.
        settings.licenseUriOrEmpty().ifPresent(uri -> catalog.setLicense(license(uri)));
        settings.homepageOrEmpty().ifPresent(catalog::setHomepage);
        catalog.getTheme().addAll(settings.themes());
        settings.keywords().forEach(keyword -> catalog.getKeyword().add(literal(keyword)));
        return catalog;
    }

    private String description(ScopeInfo scope) {
        String described = scope.getDescription();
        if (described != null && !described.isBlank()) {
            return described;
        }
        return String.format(config.catalog_description_template(), scope.getName());
    }


    /**
     * A Dataset for one EPackage in one stage of one scope.
     *
     * <p>
     * No {@code dcat:inSeries}: an EPackage is a Dataset under a Catalog with its Distributions,
     * and nothing above it. A consumer wanting every stage of one nsURI searches
     * {@code dct:identifier}, which carries the nsURI verbatim.
     * </p>
     *
     * @param target   the package-in-a-stage being described
     * @param ePackage the package itself, for its name and documentation
     * @return a Dataset that satisfies the portal's write floor
     */
    Dataset toDataset(PublicationTarget target, EPackage ePackage) {
        Dataset dataset = DcatFactory.eINSTANCE.createDataset();
        dataset.getTitle().add(literal(metadata.title(target).orElseGet(() -> defaultTitle(target, ePackage))));
        dataset.getDescription()
                .add(literal(metadata.description(target).orElseGet(() -> defaultDescription(target, ePackage))));
        dataset.setPublisher(publisher(target));
        dataset.getIdentifier().add(literal(target.nsUri()));
        if (target.version() != null && !target.version().isBlank()) {
            dataset.setVersion(target.version());
        }
        metadata.licenseUri(target).ifPresent(uri -> dataset.setLicense(license(uri)));
        dataset.getTheme().addAll(metadata.themes(target));
        // Derived keywords make the atlas's own structure searchable in the portal; configured
        // ones are added on top rather than replacing them.
        dataset.getKeyword().add(literal("scope:" + target.scope()));
        dataset.getKeyword().add(literal("stage:" + target.stage()));
        metadata.keywords(target).forEach(k -> dataset.getKeyword().add(literal(k)));
        return dataset;
    }

    /**
     * One representation of a Dataset.
     *
     * @param target    the package-in-a-stage
     * @param mediaType the representation
     * @param baseUri   the validated public prefix
     * @return a Distribution that satisfies the portal's write floor
     * @throws IllegalStateException if no licence is configured — {@code license} is a
     *                               lowerBound=1 containment on a Distribution, so the portal
     *                               would refuse the write with a constraint report that is much
     *                               harder to read than this message
     */
    Distribution toDistribution(PublicationTarget target, String mediaType, String baseUri) {
        String licenseUri = metadata.licenseUri(target).orElseThrow(() -> new IllegalStateException(
                "license.uri is required to publish a Distribution: dcat:Distribution#license is a "
                        + "lowerBound=1 containment, so the portal refuses a Distribution without one"));

        Distribution distribution = DcatFactory.eINSTANCE.createDistribution();
        distribution.setTitle(literal(mediaType));
        distribution.setMediaType(mediaType);
        distribution.setFormat(mediaType);
        distribution.setLicense(license(licenseUri));
        distribution.getAccessURL()
                .add(AtlasContentUrls.accessUrl(baseUri, target.scope(), target.stage(), target.nsUri()));
        distribution.getDownloadURL().add(
                AtlasContentUrls.downloadUrl(baseUri, target.scope(), target.stage(), target.nsUri(), mediaType));
        checksum(target.fingerprint()).ifPresent(distribution::setChecksum);
        return distribution;
    }

    private String defaultTitle(PublicationTarget target, EPackage ePackage) {
        String name = ePackage == null || ePackage.getName() == null ? target.nsUri() : ePackage.getName();
        return name + " (" + target.stage() + ")";
    }

    private String defaultDescription(PublicationTarget target, EPackage ePackage) {
        String documented = ePackage == null ? null : EcoreUtil.getDocumentation(ePackage);
        if (documented != null && !documented.isBlank()) {
            return documented;
        }
        String name = ePackage == null || ePackage.getName() == null ? target.nsUri() : ePackage.getName();
        return String.format(config.dataset_description_template(), name, target.stage(), target.scope());
    }

    /**
     * Turns an {@code fp1:<sha256 hex>} fingerprint into an SPDX checksum. Anything that is not
     * that shape is skipped rather than guessed at: a wrong checksum in a catalogue is worse than
     * no checksum.
     */
    private Optional<Checksum> checksum(String fingerprint) {
        if (fingerprint == null || !fingerprint.startsWith("fp1:")) {
            return Optional.empty();
        }
        String hex = fingerprint.substring("fp1:".length());
        if (hex.length() != 64 || !hex.chars().allMatch(c -> Character.digit(c, 16) >= 0)) {
            return Optional.empty();
        }
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        Checksum checksum = SpdxFactory.eINSTANCE.createChecksum();
        checksum.setAlgorithm(SPDX_SHA256);
        checksum.setChecksumValue(bytes);
        return Optional.of(checksum);
    }

    private LicenseDocument license(String uri) {
        LicenseDocument document = TermsFactory.eINSTANCE.createLicenseDocument();
        document.setAbout(uri);
        return document;
    }

    private Agent publisher(PublicationTarget target) {
        return publisher(metadata.publisherName(target).orElse(config.publisher_name()), config.publisher_about());
    }

    private Agent publisher(String name, String about) {
        Agent agent = FoafFactory.eINSTANCE.createAgent();
        if (about != null && !about.isBlank()) {
            agent.setAbout(about);
        }
        agent.getName().add(literal(name));
        return agent;
    }

    private PlainLiteral literal(String value) {
        PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
        literal.setValue(value);
        literal.setLang(config.language());
        return literal;
    }
}
