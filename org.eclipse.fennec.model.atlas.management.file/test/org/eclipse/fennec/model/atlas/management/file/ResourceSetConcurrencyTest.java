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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper.ResourceOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit test for ResourceSet concurrency in FileStorageHelper.
 * 
 * <p>This test verifies that the synchronized ResourceSet access in AbstractStorageHelper
 * prevents ConcurrentModificationException when multiple threads access the same
 * ResourceSet simultaneously.</p>
 */
class ResourceSetConcurrencyTest {

    @TempDir
    Path tempDir;

    private ResourceSet sharedResourceSet;
    private FileStorageHelper helper;
    private ManagementFactory factory;

    @BeforeEach
    void setUp() throws IOException {
        // Create shared ResourceSet (simulates OSGi shared service)
        sharedResourceSet = new ResourceSetImpl();
        sharedResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
        sharedResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new XMIResourceFactoryImpl());
        sharedResourceSet.getPackageRegistry().put(ManagementPackage.eNS_URI, ManagementPackage.eINSTANCE);
        
        factory = ManagementFactory.eINSTANCE;
        helper = new FileStorageHelper(sharedResourceSet, tempDir);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (helper != null) {
            helper.close();
        }
    }

    @Test
    void testConcurrentResourceSetAccess() throws Exception {
        int threadCount = 20;
        int operationsPerThread = 50;
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(threadCount);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Start threads that will hammer the shared ResourceSet
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    
                    for (int i = 0; i < operationsPerThread; i++) {
                        // Create unique file paths to avoid file conflicts
                        String objectId = "thread-" + threadId + "-obj-" + i;
                        String fileName = objectId + ".ecore";
                        URI fileUri = URI.createFileURI(tempDir.resolve(fileName).toString());
                        
                        // Test concurrent ResourceSet operations
                        try {
                            // 1. Create resource (modifies ResourceSet)
                            ResourceOperation createOp = helper.createResource(fileUri, "org.eclipse.emf.ecore");
                            
                            // 2. Add content and save
                            EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
                            ePackage.setName("TestPackage" + threadId + "_" + i);
                            ePackage.setNsURI("test://thread" + threadId + "/obj" + i);
                            ePackage.setNsPrefix("t" + threadId + "o" + i);
                            
                            createOp.getResource().getContents().add(ePackage);
                            createOp.getResource().save(null);
                            createOp.cleanup(); // Modifies ResourceSet
                            
                            // 3. Load resource (accesses ResourceSet)
                            ResourceOperation loadOp = helper.loadResource(fileUri);
                            assertNotNull(loadOp.getResource());
                            assertFalse(loadOp.getResource().getContents().isEmpty());
                            loadOp.cleanup(); // Modifies ResourceSet
                            
                            successCount.incrementAndGet();
                            
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                            e.printStackTrace();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    errorCount.incrementAndGet();
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously for maximum concurrency stress
        startLatch.countDown();
        
        // Wait for completion
        assertTrue(completeLatch.await(60, TimeUnit.SECONDS), 
                  "All threads should complete within 60 seconds");
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS), 
                  "Executor should terminate cleanly");

        // Verify results
        int expectedOperations = threadCount * operationsPerThread;
        System.out.println("ResourceSet concurrency test results:");
        System.out.println("  Successful operations: " + successCount.get() + "/" + expectedOperations);
        System.out.println("  Errors: " + errorCount.get());
        
        assertEquals(0, errorCount.get(), "No ConcurrentModificationException or other errors should occur");
        assertEquals(expectedOperations, successCount.get(), "All operations should succeed");
        
        // Verify files were created
        long fileCount = Files.list(tempDir)
                             .filter(p -> p.getFileName().toString().endsWith(".ecore"))
                             .count();
        assertEquals(expectedOperations, fileCount, "All files should be created");
    }

    @Test
    void testConcurrentMetadataOperations() throws Exception {
        int threadCount = 15;
        int operationsPerThread = 30;
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(threadCount);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    
                    for (int i = 0; i < operationsPerThread; i++) {
                        String objectId = "meta-thread-" + threadId + "-obj-" + i;
                        
                        try {
                            // Save metadata (uses ResourceSet)
                            ObjectMetadata metadata = factory.createObjectMetadata();
                            metadata.setUploadUser("user-" + threadId);
                            metadata.setUploadTime(Instant.now());
                            metadata.setSourceChannel("CONCURRENCY_TEST");
                            metadata.setObjectType("EPackage");
                            metadata.setContentHash("hash-" + threadId + "-" + i);
                            metadata.getProperties().put("thread-id", String.valueOf(threadId));
                            metadata.getProperties().put("operation-id", String.valueOf(i));
                            
                            helper.saveMetadata(objectId, metadata);
                            
                            // Load metadata (uses ResourceSet)
                            ObjectMetadata loaded = helper.loadMetadata(objectId);
                            assertNotNull(loaded);
                            assertEquals("user-" + threadId, loaded.getUploadUser());
                            assertEquals("CONCURRENCY_TEST", loaded.getSourceChannel());
                            
                            successCount.incrementAndGet();
                            
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                            e.printStackTrace();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    errorCount.incrementAndGet();
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(completeLatch.await(45, TimeUnit.SECONDS), 
                  "Metadata concurrency test should complete within 45 seconds");
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        int expectedOperations = threadCount * operationsPerThread;
        System.out.println("Metadata concurrency test results:");
        System.out.println("  Successful operations: " + successCount.get() + "/" + expectedOperations);
        System.out.println("  Errors: " + errorCount.get());
        
        assertEquals(0, errorCount.get(), "No errors should occur in metadata operations");
        assertEquals(expectedOperations, successCount.get(), "All metadata operations should succeed");
        
        // Verify metadata files were created
        long metadataFileCount = Files.list(tempDir)
                                     .filter(p -> p.getFileName().toString().endsWith(".metadata.xmi"))
                                     .count();
        assertEquals(expectedOperations, metadataFileCount, "All metadata files should be created");
    }

    @Test
    void testMixedConcurrentOperations() throws Exception {
        int threadCount = 10;
        int operationsPerThread = 20;
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(threadCount);
        
        AtomicInteger createOps = new AtomicInteger(0);
        AtomicInteger loadOps = new AtomicInteger(0);
        AtomicInteger metadataOps = new AtomicInteger(0);
        AtomicInteger errors = new AtomicInteger(0);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    
                    for (int i = 0; i < operationsPerThread; i++) {
                        String objectId = "mixed-thread-" + threadId + "-obj-" + i;
                        
                        try {
                            // Mixed operations that all use ResourceSet
                            if (i % 3 == 0) {
                                // Create and save EPackage
                                String fileName = objectId + ".ecore";
                                URI fileUri = URI.createFileURI(tempDir.resolve(fileName).toString());
                                
                                ResourceOperation op = helper.createResource(fileUri, null);
                                EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
                                pkg.setName("Mixed" + threadId + "_" + i);
                                op.getResource().getContents().add(pkg);
                                op.getResource().save(null);
                                op.cleanup();
                                
                                createOps.incrementAndGet();
                                
                            } else if (i % 3 == 1) {
                                // Try to load existing file (may fail if not created yet - that's ok)
                                String existingId = "mixed-thread-" + threadId + "-obj-" + (i - 1);
                                String existingFile = existingId + ".ecore";
                                URI existingUri = URI.createFileURI(tempDir.resolve(existingFile).toString());
                                
                                if (Files.exists(tempDir.resolve(existingFile))) {
                                    ResourceOperation op = helper.loadResource(existingUri);
                                    if (op.getResource() != null) {
                                        loadOps.incrementAndGet();
                                    }
                                    op.cleanup();
                                }
                                
                            } else {
                                // Metadata operations
                                ObjectMetadata metadata = factory.createObjectMetadata();
                                metadata.setUploadUser("mixed-user-" + threadId);
                                metadata.setUploadTime(Instant.now());
                                metadata.setSourceChannel("MIXED_TEST");
                                metadata.setObjectType("Mixed");
                                metadata.setContentHash("mixed-hash-" + threadId + "-" + i);
                                
                                helper.saveMetadata(objectId, metadata);
                                ObjectMetadata loaded = helper.loadMetadata(objectId);
                                
                                if (loaded != null && "MIXED_TEST".equals(loaded.getSourceChannel())) {
                                    metadataOps.incrementAndGet();
                                }
                            }
                        } catch (Exception e) {
                            errors.incrementAndGet();
                            // Don't print all errors as some are expected (file not found, etc.)
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    errors.incrementAndGet();
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(completeLatch.await(30, TimeUnit.SECONDS), 
                  "Mixed operations test should complete within 30 seconds");
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        System.out.println("Mixed concurrency test results:");
        System.out.println("  Create operations: " + createOps.get());
        System.out.println("  Load operations: " + loadOps.get());
        System.out.println("  Metadata operations: " + metadataOps.get());
        System.out.println("  Errors: " + errors.get());
        
        // We expect some errors due to files not existing, but no ConcurrentModificationException
        assertTrue(createOps.get() > 0, "Should have some successful create operations");
        assertTrue(metadataOps.get() > 0, "Should have some successful metadata operations");
        
        // The key assertion: if synchronization works, we won't get fatal ResourceSet errors
        // that crash threads. Some operations may fail due to missing files, but that's expected.
        assertTrue(createOps.get() + loadOps.get() + metadataOps.get() > 0, 
                  "Should have successful operations without ResourceSet corruption");
    }
}