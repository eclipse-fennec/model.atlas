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
 *      Ilenia Salvadori - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.management.apicurio;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.mgmt.annotations.MacCapabilityConstants;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.StorageBackendType;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractEObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper;
import org.osgi.annotation.bundle.Capability;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@RequireEMF
@RequireConfigurationAdmin
@Component(name = EObjectApicurioStorageService.PID, service = EObjectStorageService.class, property = "storage.backend=apicurio", configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = EObjectApicurioStorageService.Config.class)
@Capability(namespace = MacCapabilityConstants.NAMESPACE_MAC_MANAGEMENT, name = MacCapabilityConstants.CAP_EOBJECT_STORAGE, version = "1.0", attribute = "storage.backend=apicurio")
@ServiceDescription("Apicurio-based storage implementation for EObject workflow management")
public class EObjectApicurioStorageService extends AbstractEObjectStorageService {

    @Reference
    private EObjectRegistryService<EObject> registry;

    @Reference(target = "(emf.name=management)")
    private ResourceSet resourceSet;

    @ObjectClassDefinition(name = "Apicurio Storage Configuration", description = "Configuration for apicurio-based EObject storage service")
    public @interface Config {

        @AttributeDefinition(name = "Base Apicurio URL", description = "Base Apicurio URL for REST requests")
        String base_url() default "http://localhost:8080/apis/registry/v3/";

        @AttributeDefinition(name = "Storage Type", description = "The storage type. Can be useful when multiple storage with the same storage.backend property exist, to differentiate them.")
        String storage_type() default "apicurio";
    }

    public static final String PID = "ApicurioObjectStorage";

    private Config config;

    @Activate
    public void activate(BundleContext bundleContext, Config config) throws Exception {
        this.bctx = bundleContext;
        this.config = config;
        storageType = config.storage_type();
        // Call parent activation
        activateStorageService();
    }

    @Deactivate
    public void deactivate() {
        deactivateStorageService();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.fennec.model.atlas.mgmt.storage.AbstractEObjectStorageService#
     * createStorageHelper()
     */
    @Override
    protected AbstractStorageHelper createStorageHelper() throws Exception {
        return new ApicurioStorageHelper(resourceSet, registry, config);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.fennec.model.atlas.mgmt.storage.AbstractEObjectStorageService#
     * getBackendType()
     */
    @Override
    public StorageBackendType getBackendType() {
        return StorageBackendType.APICURIO;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.fennec.model.atlas.mgmt.storage.AbstractEObjectStorageService#
     * getRegistryService()
     */
    @Override
    protected EObjectRegistryService<EObject> getRegistryService() {
        return registry;
    }

    /**
     * Updates an existing object in Apicurio storage.
     *
     * <p>
     * Apicurio v3 doesn't support in-place content updates, so this method deletes
     * the existing artifact first, then creates a new one.
     * </p>
     *
     * @param objectId the object identifier
     * @param object   the updated EObject
     * @param metadata the updated metadata (must contain scope, registry, stage)
     * @return promise with the updated metadata
     */
    @Override
    public org.osgi.util.promise.Promise<org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata> updateObject(
            String objectId, org.eclipse.emf.ecore.EObject object,
            org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata metadata) {
        // Apicurio v3 has no replace option, so we delete + create
        // Get scope, registry, stage from metadata
        String scope = metadata.getScope();
        String registry = metadata.getRegistry();
        String stage = metadata.getStage();
        deleteObject(scope, registry, stage, objectId);
        return storeObject(scope, registry, stage, objectId, object, metadata);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#getStorageType(
     * )
     */
    @Override
    public String getStorageType() {
        return storageType;
    }
}
