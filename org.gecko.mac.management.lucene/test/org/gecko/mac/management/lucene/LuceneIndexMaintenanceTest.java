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
package org.gecko.mac.management.lucene;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.gecko.mac.mgmt.management.ManagementFactory;
import org.gecko.mac.mgmt.management.ManagementPackage;
import org.gecko.mac.mgmt.management.ObjectMetadata;
import org.gecko.mac.management.lucene.registry.LuceneRegistryHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Integration tests for Lucene index maintenance and concurrent access.
 * 
 * <p>Tests index rebuild operations, concurrent access scenarios,
 * and error recovery mechanisms.</p>
 */
class LuceneIndexMaintenanceTest {

    @TempDir
    Path tempDir;

    private LuceneRegistryHelper helper;
    private ResourceSet resourceSet;
    private ManagementFactory factory;

    @BeforeEach
    void setUp() throws IOException {
        // Initialize EMF
        resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
        resourceSet.getPackageRegistry().put(ManagementPackage.eNS_URI, ManagementPackage.eINSTANCE);
        
        factory = ManagementFactory.eINSTANCE;
        
        // Create helper with temp directory
        helper = new LuceneRegistryHelper(tempDir);
        helper.initialize();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (helper != null) {
            helper.close();
        }
    }

    @Test
    void testIndexRebuildFromScratch() throws Exception {
        // Add objects to index first
        for (int i = 1; i <= 5; i++) {
            ObjectMetadata metadata = createTestMetadata("user" + i, "AI_GENERATOR", "EPackage");
            helper.updateIndex("rebuild-obj-" + i, metadata);
        }
        
        // Verify objects are initially indexed
        List<String> initialResults = helper.searchObjectIds("*:*", 10);
        assertEquals(5, initialResults.size(), "Should have 5 objects initially");
        
        // Manually trigger rebuild (clears index but preserves directory structure)
        helper.rebuildIndex();
        
        // After rebuild index should be empty (since we cleared it but didn't re-add metadata files)
        List<String> afterRebuild = helper.searchObjectIds("*:*", 10);
        assertEquals(0, afterRebuild.size(), "Index should be empty after rebuild with no metadata files");
        
        // Re-add objects to test rebuild functionality
        for (int i = 1; i <= 3; i++) {
            ObjectMetadata metadata = createTestMetadata("user" + i, "AI_GENERATOR", "EPackage");
            helper.updateIndex("rebuild-obj-" + i, metadata);
        }
        
        // Verify re-added objects are searchable
        List<String> finalResults = helper.searchObjectIds("*:*", 10);
        assertEquals(3, finalResults.size(), "Should have 3 objects after re-adding");
    }

    @Test
    void testManualIndexRebuild() throws Exception {
        // Add some objects normally
        for (int i = 1; i <= 6; i++) {
            ObjectMetadata metadata = createTestMetadata("user" + i, 
                i <= 3 ? "AI_GENERATOR" : "MANUAL_UPLOAD", 
                i <= 3 ? "EPackage" : "Route");
            helper.updateIndex("manual-obj-" + i, metadata);
        }
        
        // Verify all objects are indexed initially
        List<String> beforeRebuild = helper.searchObjectIds("*:*", 10);
        assertEquals(6, beforeRebuild.size());
        
        // Perform manual rebuild (clears index)
        helper.rebuildIndex();
        
        // After rebuild, index should be empty
        List<String> afterRebuild = helper.searchObjectIds("*:*", 10);
        assertEquals(0, afterRebuild.size(), "Index should be empty after rebuild");
        
        // Re-add some objects to verify rebuild works
        for (int i = 1; i <= 3; i++) {
            ObjectMetadata metadata = createTestMetadata("user" + i, "MANUAL_UPLOAD", "Route");
            helper.updateIndex("manual-obj-" + i, metadata);
        }
        
        // Verify specific searches work for re-added objects
        List<String> manualResults = helper.searchObjectIds("sourceChannel:MANUAL_UPLOAD", 10);
        assertEquals(3, manualResults.size());
    }

    @Test
    void testConcurrentWrites() throws Exception {
        int threadCount = 10;
        int objectsPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Start concurrent write operations
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    
                    for (int i = 0; i < objectsPerThread; i++) {
                        String objectId = "thread-" + threadId + "-obj-" + i;
                        ObjectMetadata metadata = createTestMetadata("user-" + threadId, "AI_GENERATOR", "EPackage");
                        
                        helper.updateIndex(objectId, metadata);
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        startLatch.countDown();
        
        // Wait for completion
        assertTrue(completeLatch.await(30, TimeUnit.SECONDS), "All threads should complete within 30 seconds");
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "Executor should terminate");

        // Verify results
        assertEquals(threadCount * objectsPerThread, successCount.get(), "All operations should succeed");
        assertEquals(0, errorCount.get(), "No errors should occur");

        // Verify all objects are searchable
        List<String> allResults = helper.searchObjectIds("*:*", 100);
        assertEquals(threadCount * objectsPerThread, allResults.size(), "All objects should be indexed");
    }

    @Test
    void testConcurrentReadWrite() throws Exception {
        // Pre-populate with some data
        for (int i = 0; i < 10; i++) {
            ObjectMetadata metadata = createTestMetadata("initialUser" + i, "AI_GENERATOR", "EPackage");
            helper.updateIndex("initial-obj-" + i, metadata);
        }

        int writerThreads = 3;
        int readerThreads = 5;
        int operationsPerThread = 10;
        
        ExecutorService executor = Executors.newFixedThreadPool(writerThreads + readerThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(writerThreads + readerThreads);
        AtomicInteger writeSuccessCount = new AtomicInteger(0);
        AtomicInteger readSuccessCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Start writer threads
        for (int t = 0; t < writerThreads; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    
                    for (int i = 0; i < operationsPerThread; i++) {
                        String objectId = "concurrent-write-" + threadId + "-" + i;
                        ObjectMetadata metadata = createTestMetadata("writer" + threadId, "AI_GENERATOR", "EPackage");
                        
                        helper.updateIndex(objectId, metadata);
                        writeSuccessCount.incrementAndGet();
                        
                        Thread.sleep(10); // Small delay to allow interleaving
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        // Start reader threads
        for (int t = 0; t < readerThreads; t++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    
                    for (int i = 0; i < operationsPerThread; i++) {
                        // Search for existing data using analyzed field for wildcard
                        List<String> results = helper.searchObjectIds("uploadUser_text:initialUser*", 20);
                        if (!results.isEmpty()) {
                            readSuccessCount.incrementAndGet();
                        }
                        
                        Thread.sleep(5); // Small delay
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        // Start all operations
        startLatch.countDown();
        
        // Wait for completion
        assertTrue(completeLatch.await(60, TimeUnit.SECONDS), "All operations should complete");
        
        executor.shutdown();

        // Verify results
        assertEquals(0, errorCount.get(), "No errors should occur during concurrent operations");
        assertEquals(writerThreads * operationsPerThread, writeSuccessCount.get(), "All writes should succeed");
        assertTrue(readSuccessCount.get() > 0, "Reads should succeed");

        // Final verification - search should find all objects
        List<String> finalResults = helper.searchObjectIds("*:*", 100);
        assertTrue(finalResults.size() >= 10 + (writerThreads * operationsPerThread), 
                  "Should find at least initial + new objects");
    }

    @Test
    void testIndexCorruptionRecovery() throws Exception {
        // Create some initial data
        for (int i = 0; i < 5; i++) {
            ObjectMetadata metadata = createTestMetadata("user" + i, "AI_GENERATOR", "EPackage");
            helper.updateIndex("corrupt-test-" + i, metadata);
        }

        // Verify initial state
        List<String> beforeCorruption = helper.searchObjectIds("*:*", 10);
        assertEquals(5, beforeCorruption.size());

        // Close helper and corrupt index files
        helper.close();
        
        Path indexPath = tempDir.resolve(".lucene-index");
        if (Files.exists(indexPath)) {
            // Delete some index files to simulate corruption
            Files.walk(indexPath)
                .filter(Files::isRegularFile)
                .findFirst()
                .ifPresent(file -> {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        // Ignore for test
                    }
                });
        }

        // Recreate helper - should handle corruption gracefully
        assertDoesNotThrow(() -> {
            helper = new LuceneRegistryHelper(tempDir);
            helper.initialize();
        }, "Helper creation should not fail even with corrupted index");

        // After corruption recovery, index starts fresh (empty)
        // This tests that corruption doesn't crash the system
        List<String> afterRecovery = helper.searchObjectIds("*:*", 10);
        assertEquals(0, afterRecovery.size(), "Index should be empty after corruption recovery");
        
        // Test that new operations work after recovery
        ObjectMetadata newMetadata = createTestMetadata("recoveryUser", "AI_GENERATOR", "EPackage");
        helper.updateIndex("recovery-test", newMetadata);
        
        List<String> afterNewData = helper.searchObjectIds("uploadUser:recoveryUser", 10);
        assertEquals(1, afterNewData.size(), "New data should be indexable after recovery");
    }

    @Test
    void testIndexMaintenanceWithDeletes() throws Exception {
        // Create initial objects
        for (int i = 0; i < 10; i++) {
            ObjectMetadata metadata = createTestMetadata("deleteTestUser" + i, "AI_GENERATOR", "EPackage");
            helper.updateIndex("delete-maint-" + i, metadata);
        }

        // Verify all are indexed using analyzed field for wildcard
        List<String> initial = helper.searchObjectIds("uploadUser_text:deleteTestUser*", 20);
        assertEquals(10, initial.size());

        // Delete every other object
        for (int i = 0; i < 10; i += 2) {
            helper.removeFromIndex("delete-maint-" + i);
        }

        // Verify deletions are reflected in index using analyzed field for wildcard
        List<String> afterDeletes = helper.searchObjectIds("uploadUser_text:deleteTestUser*", 20);
        assertEquals(5, afterDeletes.size(), "Should have 5 objects remaining after deletions");

        // Verify specific objects are gone
        for (int i = 0; i < 10; i += 2) {
            List<String> shouldBeEmpty = helper.searchObjectIds("uploadUser:deleteTestUser" + i, 5);
            assertTrue(shouldBeEmpty.isEmpty(), "Deleted object should not be found: deleteTestUser" + i);
        }

        // Verify remaining objects are still there
        for (int i = 1; i < 10; i += 2) {
            List<String> shouldExist = helper.searchObjectIds("uploadUser:deleteTestUser" + i, 5);
            assertEquals(1, shouldExist.size(), "Non-deleted object should be found: deleteTestUser" + i);
        }
    }

    @Test
    void testLargeDatasetIndexing() throws Exception {
        int objectCount = 100;
        
        // Create large dataset
        for (int i = 0; i < objectCount; i++) {
            ObjectMetadata metadata = createTestMetadata(
                "largeTestUser" + (i % 10), // 10 different users
                i % 2 == 0 ? "AI_GENERATOR" : "MANUAL_UPLOAD", // Alternate source channels
                i % 3 == 0 ? "EPackage" : (i % 3 == 1 ? "Route" : "SensorModel") // 3 different types
            );
            helper.updateIndex("large-dataset-" + i, metadata);
        }

        // Test various queries on large dataset
        List<String> allResults = helper.searchObjectIds("*:*", objectCount + 10);
        assertEquals(objectCount, allResults.size(), "Should find all objects");

        List<String> aiResults = helper.searchObjectIds("sourceChannel:AI_GENERATOR", objectCount);
        assertEquals(objectCount / 2, aiResults.size(), "Should find half the objects for AI_GENERATOR");

        List<String> epackageResults = helper.searchObjectIds("objectType:EPackage", objectCount);
        // Items where i % 3 == 0: 0,3,6,9,...,99 = 34 items (not 33)
        int expectedEPackages = (int) Math.ceil(objectCount / 3.0);
        assertEquals(expectedEPackages, epackageResults.size(), "Should find 1/3 of objects for EPackage");

        // Test user-specific queries
        List<String> user0Results = helper.searchObjectIds("uploadUser:largeTestUser0", objectCount);
        assertEquals(objectCount / 10, user0Results.size(), "Should find 1/10 of objects for user0");
    }

    /**
     * Creates test metadata with the specified parameters.
     */
    private ObjectMetadata createTestMetadata(String user, String channel, String type) {
        ObjectMetadata metadata = factory.createObjectMetadata();
        metadata.setUploadUser(user);
        metadata.setUploadTime(Instant.now());
        metadata.setSourceChannel(channel);
        metadata.setObjectType(type);
        metadata.setContentHash("test-hash-" + System.nanoTime());
        return metadata;
    }
}