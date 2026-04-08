/*
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
package org.eclipse.fennec.model.atlas.workflow.tests.support;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.eclipse.fennec.model.atlas.workflow.tests.annotations.TestAnnotations;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

/**
 * JUnit 5 extension that creates a temporary directory for tests using Lucene-backed OSGi services
 * and handles cleanup safely after asynchronous service deactivation.
 *
 * <p>
 * Lucene keeps index files open until the service is fully deactivated. When OSGi's
 * {@code ConfigurationExtension} removes configurations in its {@code afterEach}, the
 * component deactivation is asynchronous. JUnit's built-in {@code @TempDir} cleanup then
 * fails with {@code DirectoryNotEmptyException} because Lucene files are still open.
 * </p>
 *
 * <p>
 * This extension must be registered <strong>first</strong> in {@code @ExtendWith} so that
 * its {@code afterEach} runs <strong>last</strong> among user-registered extensions — after
 * {@code ConfigurationExtension} has already removed the OSGi configurations that trigger
 * service deactivation. The retry loop then waits for the Lucene index files to be released
 * before deleting the temp directory.
 * </p>
 *
 * <p>Usage:
 * <pre>
 * {@literal @}ExtendWith(LuceneAwareTempDirExtension.class)  // MUST be first
 * {@literal @}ExtendWith(BundleContextExtension.class)
 * {@literal @}ExtendWith(ServiceExtension.class)
 * {@literal @}ExtendWith(ConfigurationExtension.class)
 * class MyTest { ... }
 * </pre>
 * </p>
 */
public class LuceneAwareTempDirExtension implements BeforeEachCallback, AfterEachCallback {

    private static final Namespace NAMESPACE = Namespace.create(LuceneAwareTempDirExtension.class);
    private static final String TEMP_DIR_KEY = "tempDir";
    private static final long CLEANUP_TIMEOUT_MS = 10_000;
    private static final long RETRY_INTERVAL_MS = 200;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Path tempDir = Files.createTempDirectory("atlas-test-");
        System.setProperty(TestAnnotations.PROP_TEMP_DIR, tempDir.toString());
        context.getStore(NAMESPACE).put(TEMP_DIR_KEY, tempDir);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Path tempDir = context.getStore(NAMESPACE).get(TEMP_DIR_KEY, Path.class);
        System.clearProperty(TestAnnotations.PROP_TEMP_DIR);
        if (tempDir == null || !Files.exists(tempDir)) {
            return;
        }
        deleteWithRetry(tempDir, context);
    }

    private void deleteWithRetry(Path dir, ExtensionContext context) throws InterruptedException {
        long deadline = System.currentTimeMillis() + CLEANUP_TIMEOUT_MS;
        IOException lastException = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                deleteRecursively(dir);
                return;
            } catch (IOException e) {
                lastException = e;
                Thread.sleep(RETRY_INTERVAL_MS);
            }
        }
        System.err.printf("[LuceneAwareTempDirExtension] WARNING: Could not fully delete temp dir '%s' for test '%s': %s%n",
                dir, context.getDisplayName(), lastException != null ? lastException.getMessage() : "unknown");
    }

    private void deleteRecursively(Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path directory, IOException exc) throws IOException {
                if (exc != null) {
                    throw exc;
                }
                Files.delete(directory);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
