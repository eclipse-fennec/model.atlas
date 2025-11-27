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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper;

/**
 * File-based implementation of storage helper for EMF objects.
 * Extends AbstractStorageHelper to provide file system specific operations.
 */
public class FileStorageHelper extends AbstractStorageHelper {
    
    private final Path workspacePath;
    
    public FileStorageHelper(ResourceSet resourceSet, Path workspacePath) {
        super(resourceSet);
        this.workspacePath = workspacePath;
    }
    
    @Override
    protected URI createStorageURI(String path) {
        Path filePath = workspacePath.resolve(path);
        return URI.createFileURI(filePath.toString());
    }
    
    @Override
    protected void persistResource(String path, Resource resource) throws IOException {
        // For file storage, EMF handles the persistence automatically
        // The resource.save() call in the parent class writes to the file system
        // No additional action needed here
    }
    
    @Override
    protected boolean storageExists(String path) throws IOException {
        Path filePath = workspacePath.resolve(path);
        return Files.exists(filePath);
    }
    
    @Override
    protected String findObjectPath(String objectId) throws IOException {
        if (!Files.exists(workspacePath)) {
            return null;
        }
        
        try (Stream<Path> paths = Files.list(workspacePath)) {
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
    
    @Override
    public boolean deleteObject(String objectId) throws IOException {
        boolean deleted = false;
        
        // Delete the object file (with any extension)
        if (Files.exists(workspacePath)) {
            try (Stream<Path> paths = Files.list(workspacePath)) {
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
        Path metadataPath = workspacePath.resolve(objectId + METADATA_EXTENSION);
        if (Files.exists(metadataPath)) {
            Files.delete(metadataPath);
            deleted = true;
        }
        
        return deleted;
    }
    
    @Override
    public List<String> listObjectIds() throws IOException {
        if (!Files.exists(workspacePath)) {
            return Collections.emptyList();
        }
        
        try (Stream<Path> paths = Files.list(workspacePath)) {
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
}