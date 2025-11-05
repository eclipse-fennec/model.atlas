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
 *      Mark Hoffmann - initial API and implementation
 */
package org.gecko.mac.governance.registration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.ecore.EPackage;
import org.gecko.emf.osgi.configurator.EPackageConfigurator;
import org.gecko.emf.osgi.constants.EMFNamespaces;

/**
 * Dynamic EPackage configurator for runtime registration of released EPackages.
 * 
 * <p>This configurator enables dynamic registration and unregistration of EPackages
 * in the OSGi EMF registry. It is used by the post-release actions to make
 * released EPackages available to the EMF ecosystem.</p>
 * 
 * <p>Unlike static configurators that are bound to specific generated models,
 * this configurator works with any EPackage instance and extracts metadata
 * dynamically from the EPackage itself.</p>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
public class DynamicEPackageConfigurator implements EPackageConfigurator {
    
    private final EPackage ePackage;
    private final String fileExtension;
    private final String version;
    
    /**
     * Creates a new dynamic EPackage configurator.
     * 
     * @param ePackage the EPackage to register (must not be null)
     * @param fileExtension the file extension for this model (e.g., "ecore", "sensors")
     * @param version the version of the model (e.g., "1.0.0")
     * @throws NullPointerException if ePackage is null
     */
    public DynamicEPackageConfigurator(EPackage ePackage, String fileExtension, String version) {
        this.ePackage = Objects.requireNonNull(ePackage, "EPackage cannot be null");
        this.fileExtension = fileExtension != null ? fileExtension : "model";
        this.version = version != null ? version : "1.0";
    }
    
    /**
     * Creates a new dynamic EPackage configurator with default file extension and version.
     * 
     * @param ePackage the EPackage to register (must not be null)
     */
    public DynamicEPackageConfigurator(EPackage ePackage) {
        this(ePackage, null, null);
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
     * <p>Properties include EMF model metadata extracted from the EPackage:</p>
     * <ul>
     * <li>{@code emf.model.name} - The EPackage name</li>
     * <li>{@code emf.model.nsuri} - The EPackage namespace URI</li>
     * <li>{@code emf.model.file.ext} - The file extension</li>
     * <li>{@code emf.model.version} - The model version</li>
     * <li>{@code dynamic.registration} - Marker for dynamic registration</li>
     * </ul>
     * 
     * @return map of service properties
     */
    public Map<String, Object> getServiceProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(EMFNamespaces.EMF_MODEL_NAME, ePackage.getName());
        properties.put(EMFNamespaces.EMF_MODEL_NSURI, ePackage.getNsURI());
        properties.put(EMFNamespaces.EMF_MODEL_FILE_EXT, fileExtension);
        properties.put(EMFNamespaces.EMF_MODEL_VERSION, version);
        properties.put("dynamic.registration", Boolean.TRUE);
        properties.put("epackage.source", "workflow.release");
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
    
    @Override
    public String toString() {
        return String.format("DynamicEPackageConfigurator[name=%s, nsURI=%s, version=%s]", 
                           ePackage.getName(), ePackage.getNsURI(), version);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        DynamicEPackageConfigurator other = (DynamicEPackageConfigurator) obj;
        return Objects.equals(ePackage.getNsURI(), other.ePackage.getNsURI());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(ePackage.getNsURI());
    }
}