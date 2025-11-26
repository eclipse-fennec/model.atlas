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
 *      Mark Hoffmann - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.management.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.mgmt.annotations.MacCapabilityConstants;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.StorageBackendType;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractEObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper;
import org.gecko.emf.osgi.annotation.require.RequireEMF;
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

/**
 * File-based implementation of EObjectStorageService using AbstractEObjectStorageService.
 * 
 * <p>This service provides persistent storage for EMF EObjects and their metadata
 * using the local file system. It leverages the {@link AbstractEObjectStorageService}
 * framework to provide a complete Promise-based API while delegating actual storage
 * operations to {@link FileStorageHelper}.</p>
 * 
 * <h3>Storage Structure</h3>
 * <p>Objects and metadata are stored in the configured workspace folder with the following structure:</p>
 * <ul>
 * <li><strong>Objects</strong>: {@code {objectId}.{extension}} (e.g., "model123.xmi", "schema456.ecore")</li>
 * <li><strong>Metadata</strong>: {@code {objectId}.metadata.xmi} (e.g., "model123.metadata.xmi")</li>
 * </ul>
 * 
 * <h3>Features</h3>
 * <ul>
 * <li><strong>Automatic Workspace Creation</strong> - Creates workspace directory if it doesn't exist</li>
 * <li><strong>Format Support</strong> - Supports any EMF resource format (XMI, Ecore, JSON, etc.)</li>
 * <li><strong>Metadata Management</strong> - Automatically manages object metadata alongside content</li>
 * <li><strong>Promise-based API</strong> - All operations return OSGi Promises for async processing</li>
 * <li><strong>Error Recovery</strong> - Robust error handling with detailed logging</li>
 * </ul>
 * 
 * <h3>Configuration</h3>
 * <p>This service requires OSGi Configuration Admin with the following properties:</p>
 * <ul>
 * <li><strong>workspace_folder</strong> - Directory path for storing files (default: "/tmp/epackage-storage")</li>
 * </ul>
 * 
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Configuration via OSGi Configuration Admin:
 * // PID: FileObjectStorage
 * // workspace_folder=/opt/storage/objects
 * 
 * // Service automatically available as EObjectStorageService
 * EObjectStorageService<EObject> storage = ...; // Injected via OSGi DS
 * 
 * // Store an EObject with metadata
 * ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
 * metadata.setName("MyModel");
 * storage.storeObject("model-123", myEObject, metadata)
 *     .onSuccess(id -> logger.info("Stored: " + id))
 *     .onFailure(error -> logger.severe("Failed: " + error));
 * }</pre>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 * @see AbstractEObjectStorageService
 * @see FileStorageHelper
 */
@RequireEMF
@RequireConfigurationAdmin
@Component(
	name = EObjectFileStorageService.PID,
	service = EObjectStorageService.class,
    property = "storage.backend=file",
    configurationPolicy = ConfigurationPolicy.REQUIRE
)
@Designate(ocd = EObjectFileStorageService.Config.class)
@Capability(namespace = MacCapabilityConstants.NAMESPACE_MAC_MANAGEMENT, name = MacCapabilityConstants.CAP_EOBJECT_STORAGE, version = "1.0", attribute = "storage.backend=file")
@ServiceDescription("File-based storage implementation for EObject workflow management")
public class EObjectFileStorageService extends AbstractEObjectStorageService {

    @ObjectClassDefinition(
        name = "File Storage Configuration",
        description = "Configuration for file-based EObject storage service"
    )
    public @interface Config {
        
        @AttributeDefinition(
            name = "Workspace Folder",
            description = "Folder path for storing objects and metadata"
        )
        String workspace_folder() default "/tmp/epackage-storage";
        
        @AttributeDefinition(
            name = "Storage Role",
            description = "Role of this storage service (draft, approved, documentation, etc.)"
        )
        String storage_role() default "draft";
    }

    public static final String PID = "FileObjectStorage";
    
    @Reference(target = "(emf.name=management)")
    private ResourceSet resourceSet;
    
    @Reference
    private EObjectRegistryService<EObject> registry;
    
    private Path workspacePath;
    private String storageRole;
    
    @Activate
    public void activate(BundleContext bundleContext, Config config) throws Exception {
        this.bctx = bundleContext;
        this.workspacePath = Paths.get(config.workspace_folder());
        this.storageRole = config.storage_role();
        
        // Create workspace directory
        try {
            Files.createDirectories(workspacePath);
            LOGGER.info("File storage service activated with workspace: " + workspacePath  + " and role: " + storageRole);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create workspace directory", e);
            throw new RuntimeException("Cannot initialize file storage workspace", e);
        }
        
        // Call parent activation
        activateStorageService();
        
    }
    
    @Deactivate
    public void deactivate() {
        deactivateStorageService();
    }
    
    /* 
	 * (non-Javadoc)
	 * @see org.gecko.mac.mgmt.storage.AbstractEObjectStorageService#getBackendType()
	 */
	@Override
	public StorageBackendType getBackendType() {
	    return StorageBackendType.FILE;
	}

	/* 
     * (non-Javadoc)
     * @see org.gecko.mac.mgmt.storage.AbstractEObjectStorageService#getRegistryService()
     */
    @Override
    protected EObjectRegistryService<EObject> getRegistryService() {
    	return registry;
    }
    
    /* 
     * (non-Javadoc)
     * @see org.gecko.mac.mgmt.storage.AbstractEObjectStorageService#getStorageRole()
     */
    @Override
    protected String getStorageRole() {
        return storageRole;
    }

    /* 
     * (non-Javadoc)
     * @see org.gecko.mac.mgmt.storage.AbstractEObjectStorageService#createStorageHelper()
     */
    @Override
    protected AbstractStorageHelper createStorageHelper() throws Exception {
        LOGGER.info("Creating file storage helper");
        return new FileStorageHelper(resourceSet, workspacePath);
    }

}