/**
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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.schema.registry.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.schema.registry.api.SchemaRegistryService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

@Component(name = "SchemaRegistryService", configurationPid = "SchemaRegistryService", configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = SchemaRegistryServiceConfig.class, factory = true)
public class SchemaRegistryServiceImpl implements SchemaRegistryService {

    @Reference(target = "(scope=no-inject)")
    ResourceSet resourceSet;

    private SchemaRegistryServiceConfig config;
    private final EClass rootEClass;

    @Activate
    public SchemaRegistryServiceImpl(SchemaRegistryServiceConfig config) {
        requireNonNull(config.registry_name(), "registry.name property must be set");
        requireNonNull(config.root_eclass_uri(), "root.eclass.uri property must be set");
        this.config = config;

        EObject eObject = resourceSet.getEObject(URI.createURI(config.root_eclass_uri()), false);
        if (eObject != null && eObject instanceof EClass eClass) {
            rootEClass = eClass;
        } else {
            throw new IllegalArgumentException(String.format(
                    "The provided root.eclass.uri %s does not match to any known EClass", config.root_eclass_uri()));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.fennec.model.atlas.schema.registry.api.SchemaRegistryService#
     * getRegistryName()
     */
    @Override
    public String getRegistryName() {
        return config.registry_name();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.fennec.model.atlas.schema.registry.api.SchemaRegistryService#
     * getSchemaUri()
     */
    @Override
    public String getSchemaUri() {
        return rootEClass.getEPackage().getNsURI();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.fennec.model.atlas.schema.registry.api.SchemaRegistryService#
     * getRootEClass()
     */
    @Override
    public EClass getRootEClass() {
        return rootEClass;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.fennec.model.atlas.schema.registry.api.SchemaRegistryService#
     * isCompatible(org.eclipse.emf.ecore.EClass)
     */
    @Override
    public boolean isCompatible(EClass eClass) {
        return eClass.equals(rootEClass) || eClass.getEAllSuperTypes().contains(rootEClass);
    }

}
