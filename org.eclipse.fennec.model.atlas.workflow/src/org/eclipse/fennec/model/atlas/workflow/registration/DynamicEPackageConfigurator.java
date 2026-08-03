/*
 * Copyright (c) 2012 - 2025 Data In Motion and others.
 * All rights reserved.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.workflow.registration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;

/**
 * Dynamic EPackage configurator for runtime registration of released EPackages.
 * 
 * <p>
 * This configurator enables dynamic registration and unregistration of
 * EPackages in the OSGi EMF registry. It is used by the post-release actions to
 * make released EPackages available to the EMF ecosystem.
 * </p>
 * 
 * <p>
 * Unlike static configurators that are bound to specific generated models, this
 * configurator works with any EPackage instance and extracts metadata
 * dynamically from the EPackage itself.
 * </p>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
public class DynamicEPackageConfigurator implements EPackageConfigurator {

    private final EPackage ePackage;
    private final String fileExtension;
    private final String version;
    private final String scope;
    private final String stage;
    private final String fingerprint;

    /**
     * Creates a new dynamic EPackage configurator.
     *
     * @param ePackage      the EPackage to register (must not be null)
     * @param fileExtension the file extension for this model (e.g., "ecore",
     *                      "sensors")
     * @param version       the version of the model (e.g., "1.0.0")
     * @param scope         the workflow scope the EPackage belongs to
     * @param stage         the workflow stage the EPackage was registered from
     * @param fingerprint   the content-derived model fingerprint (scheme-prefixed,
     *                      e.g. {@code fp1:<digest>}) computed by the registration
     *                      service; may be null when no FingerprintService was
     *                      available
     * @throws NullPointerException if ePackage is null
     */
    public DynamicEPackageConfigurator(EPackage ePackage, String fileExtension, String version, String scope,
            String stage, String fingerprint) {
        this.ePackage = Objects.requireNonNull(ePackage, "EPackage cannot be null");
        this.fileExtension = fileExtension != null ? fileExtension : "model";
        this.version = version != null ? version : "1.0";
        this.scope = scope;
        this.stage = stage;
        this.fingerprint = fingerprint;
    }

    @Override
    public void configureEPackage(EPackage.Registry registry) {
        Objects.requireNonNull(registry, "Registry cannot be null");
        registry.put(ePackage.getNsURI(), ePackage);
    }

    @Override
    public void unconfigureEPackage(EPackage.Registry registry) {
        Objects.requireNonNull(registry, "Registry cannot be null");
        registry.remove(ePackage.getNsURI());
    }

    /**
     * Returns the service properties for OSGi service registration.
     * 
     * <p>
     * Properties include EMF model metadata extracted from the EPackage:
     * </p>
     * <ul>
     * <li>{@code emf.name} - The EPackage name</li>
     * <li>{@code emf.nsURI} - The EPackage namespace URI</li>
     * <li>{@code emf.fileExtension} - The file extension</li>
     * <li>{@code emf.version} - The model version</li>
     * <li>{@code emf.model.scope} - The workflow scope</li>
     * <li>{@code atlas.stage} - The workflow stage</li>
     * <li>{@code emf.fingerprint} - The content-derived model fingerprint
     * (omitted when unknown, so {@code (emf.fingerprint=*)} means "this service
     * knows its model version")</li>
     * <li>{@code dynamic.registration} - Marker for dynamic registration</li>
     * </ul>
     *
     * @return map of service properties
     */
    public Map<String, Object> getServiceProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(EMFNamespaces.EMF_NAME, ePackage.getName());
        properties.put(EMFNamespaces.EMF_MODEL_NSURI, ePackage.getNsURI());
        properties.put(EMFNamespaces.EMF_MODEL_FILE_EXT, fileExtension);
        properties.put(EMFNamespaces.EMF_MODEL_VERSION, version);
        properties.put(EMFNamespaces.EMF_MODEL_SCOPE, scope);
        properties.put(WorkflowConstants.ATLAS_EPACKAGE_REGISTRATION_STAGE_PROPERTY, stage);
        if (fingerprint != null) {
            properties.put(EMFNamespaces.EMF_MODEL_FINGERPRINT, fingerprint);
        }
        properties.put("dynamic.registration", Boolean.TRUE);
        return properties;
    }

    /**
     * Returns the configured EPackage.
     * 
     * @return the EPackage instance
     */
    public EPackage getEPackage() {
        return ePackage;
    }

    /**
     * Returns the namespace URI of the configured EPackage.
     * 
     * @return the namespace URI
     */
    public String getNamespaceURI() {
        return ePackage.getNsURI();
    }

    /**
     * Returns the name of the configured EPackage.
     *
     * @return the EPackage name
     */
    public String getModelName() {
        return ePackage.getName();
    }

    /**
     * Returns the content-derived model fingerprint this configurator was
     * registered with.
     *
     * @return the fingerprint, or null when it could not be computed
     */
    public String getFingerprint() {
        return fingerprint;
    }

    @Override
    public String toString() {
        return String.format("DynamicEPackageConfigurator[name=%s, nsURI=%s, version=%s, scope=%s, stage=%s, fingerprint=%s]",
                ePackage.getName(), ePackage.getNsURI(), version, scope, stage, fingerprint);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        // Identity mirrors the registration key: workflow location + nsURI + content
        // version. nsURI alone would collapse the same model registered in several
        // stages (or two content versions of one nsURI) into one.
        DynamicEPackageConfigurator other = (DynamicEPackageConfigurator) obj;
        return Objects.equals(ePackage.getNsURI(), other.ePackage.getNsURI())
                && Objects.equals(scope, other.scope)
                && Objects.equals(stage, other.stage)
                && Objects.equals(fingerprint, other.fingerprint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ePackage.getNsURI(), scope, stage, fingerprint);
    }
}