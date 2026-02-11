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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.management.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper;

/**
 * File-based implementation of storage helper for EMF objects.
 * Extends AbstractStorageHelper to provide file system specific operations.
 */
public class FileStorageHelper extends AbstractStorageHelper {

    private static final Logger LOGGER = Logger.getLogger(FileStorageHelper.class.getName());

    private final Path workspacePath;

	private EObjectRegistryService<EObject> objectRegistryService;
    
    public FileStorageHelper(ResourceSet resourceSet, Path workspacePath, EObjectRegistryService<EObject> objectRegistryService) {
        super(resourceSet);
        this.workspacePath = workspacePath;
		this.objectRegistryService = objectRegistryService;
        try {
			updateRegistryCache();
		} catch (IOException e) {
			throw new IllegalStateException("IOException while updating registry with existing objects");
		}
    }
    
    @Override
    protected URI createStorageURI(String scope, String registry, String stage, String path) {
        Path filePath = workspacePath.resolve(scope).resolve(registry).resolve(stage).resolve(path);
        return URI.createFileURI(filePath.toString());
    }
    
    @Override
    protected void persistResource(String path, Resource resource) throws IOException {
        // For file storage, EMF handles the persistence automatically
        // The resource.save() call in the parent class writes to the file system
        // No additional action needed here
    }
    
    @Override
    protected boolean storageExists(String scope, String registry, String stage, String path) throws IOException {
    	Path filePath = workspacePath.resolve(scope).resolve(registry).resolve(stage).resolve(path);
        return Files.exists(filePath);
    }
    
    @Override
    protected String findObjectPath(String scope, String registry, String stage, String objectId) throws IOException {
    	Path basePath = workspacePath.resolve(scope).resolve(registry).resolve(stage);
        if (!Files.exists(basePath)) {
            return null;
        }
        
        try (Stream<Path> paths = Files.list(basePath)) {
            return paths
                .filter(path -> {
                    String fileName = path.getFileName().toString();
                    return fileName.startsWith(objectId + ".") && 
                           !fileName.endsWith(METADATA_EXTENSION);
                })
                .map(path -> path.getFileName().toString())
                .findFirst()
                .orElse(null);
        }
    }
    
    /*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper#loadAllStoredMetadata()
	 */
	@Override
	protected List<ObjectMetadata> loadAllStoredMetadata() throws IOException {
		if (!Files.exists(workspacePath)) {
            return Collections.emptyList();
        }

		List<ObjectMetadata> allMetadata = new ArrayList<>();

		// Walk the directory tree to find all scope/registry/stage directories
		// The structure is: workspacePath/scope/registry/stage/objectId.metadata.xmi
		try (Stream<Path> scopePaths = Files.list(workspacePath)) {
			for (Path scopePath : scopePaths.filter(Files::isDirectory).collect(Collectors.toList())) {
				String scope = scopePath.getFileName().toString();

				try (Stream<Path> registryPaths = Files.list(scopePath)) {
					for (Path registryPath : registryPaths.filter(Files::isDirectory).collect(Collectors.toList())) {
						String registry = registryPath.getFileName().toString();

						try (Stream<Path> stagePaths = Files.list(registryPath)) {
							for (Path stagePath : stagePaths.filter(Files::isDirectory).collect(Collectors.toList())) {
								String stage = stagePath.getFileName().toString();

								// Now we're at the scope/registry/stage level - list metadata files
								try (Stream<Path> files = Files.list(stagePath)) {
									List<String> metadataFiles = files
										.filter(Files::isRegularFile)
										.map(path -> path.getFileName().toString())
										.filter(fileName -> fileName.endsWith(METADATA_EXTENSION))
										.collect(Collectors.toList());

									// For each metadata file, extract objectId and load metadata
									for (String metadataFileName : metadataFiles) {
										// Remove METADATA_EXTENSION to get objectId
										String objectId = metadataFileName.substring(0,
											metadataFileName.length() - METADATA_EXTENSION.length());

										try {
											ObjectMetadata metadata = loadMetadata(scope, registry, stage, objectId);
											if (metadata != null) {
												allMetadata.add(metadata);
											}
										} catch (Exception e) {
											LOGGER.warning("Failed to load metadata for objectId: " + objectId +
												" in scope=" + scope + ", registry=" + registry + ", stage=" + stage +
												": " + e.getMessage());
											// Continue with next metadata file
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return allMetadata;
	}
    
    @Override
    public boolean deleteObject(String scope, String registry, String stage, String objectId) throws IOException {
        boolean deleted = false;
        
        // Delete the object file (with any extension)
        Path basePath = workspacePath.resolve(scope).resolve(registry).resolve(stage);
        if (Files.exists(basePath)) {
            try (Stream<Path> paths = Files.list(basePath)) {
                List<Path> objectFiles = paths
                    .filter(path -> {
                        String fileName = path.getFileName().toString();
                        return fileName.startsWith(objectId + ".") && 
                               !fileName.endsWith(METADATA_EXTENSION);
                    })
                    .collect(Collectors.toList());
                
                for (Path objectFile : objectFiles) {
                    Files.delete(objectFile);
                    deleted = true;
                }
            }
        }
        
        // Delete the metadata file
        Path metadataPath = basePath.resolve(objectId + METADATA_EXTENSION);
        if (Files.exists(metadataPath)) {
            Files.delete(metadataPath);
            deleted = true;
        }
        
        return deleted;
    }
    
    @Override
    public List<String> listObjectIds(String scope, String registry, String stage) throws IOException {
    	Path basePath = workspacePath.resolve(scope).resolve(registry).resolve(stage);
        if (!Files.exists(basePath)) {
            return Collections.emptyList();
        }
        
        try (Stream<Path> paths = Files.list(basePath)) {
            return paths
                .filter(path -> {
                    String fileName = path.getFileName().toString();
                    // Include all files except metadata files
                    return !fileName.endsWith(METADATA_EXTENSION) && fileName.contains(".");
                })
                .map(path -> {
                    String fileName = path.getFileName().toString();
                    int lastDot = fileName.lastIndexOf('.');
                    return fileName.substring(0, lastDot);
                })
                .distinct() // In case there are multiple files with same ID
                .collect(Collectors.toList());
        }
    }

    /**
	 * When the service comes up it should retrieve from apicurio the existing
	 * metadata and cache them in the registry
	 * 
	 * @throws IOException
	 */
	private void updateRegistryCache() throws IOException {
		List<ObjectMetadata> existingMetadata = loadAllStoredMetadata();
		existingMetadata.forEach(m -> objectRegistryService.updateCache(m));
	}
	
}