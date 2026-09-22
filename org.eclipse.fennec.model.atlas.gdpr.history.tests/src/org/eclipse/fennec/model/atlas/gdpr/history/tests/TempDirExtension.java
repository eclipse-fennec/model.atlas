/**
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
package org.eclipse.fennec.model.atlas.gdpr.history.tests;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

/**
 * Gives each test its own directory and publishes it as the {@code tempDir} system property, which
 * is what the {@code templateArguments} of the storage and registry configurations interpolate.
 * <p>
 * <b>It has to run before the other extensions</b>, because a class-level configuration would be
 * templated before the property exists. Lucene holds its index files open a moment after the
 * registry is gone, so the delete is retried rather than failing the test.
 * <p>
 * A copy of {@code workflow.tests}' extension of the same shape: that one is in a private package
 * of a test bundle, so it cannot be imported.
 */
public class TempDirExtension implements BeforeEachCallback, AfterEachCallback {

	private static final Namespace NAMESPACE = Namespace.create(TempDirExtension.class);
	private static final String TEMP_DIR_KEY = "tempDir";
	private static final long CLEANUP_TIMEOUT_MS = 10_000;
	private static final long RETRY_INTERVAL_MS = 200;

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		Path tempDir = Files.createTempDirectory("gdpr-history-test-");
		System.setProperty(CommonTestAnnotations.PROP_TEMP_DIR, tempDir.toString());
		context.getStore(NAMESPACE).put(TEMP_DIR_KEY, tempDir);
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		Path tempDir = context.getStore(NAMESPACE).get(TEMP_DIR_KEY, Path.class);
		System.clearProperty(CommonTestAnnotations.PROP_TEMP_DIR);
		if (tempDir == null || !Files.exists(tempDir)) {
			return;
		}
		deleteWithRetry(tempDir, context);
	}

	private void deleteWithRetry(Path dir, ExtensionContext context) throws InterruptedException {
		long deadline = System.currentTimeMillis() + CLEANUP_TIMEOUT_MS;
		IOException last = null;
		while (System.currentTimeMillis() < deadline) {
			try {
				deleteRecursively(dir);
				return;
			} catch (IOException e) {
				last = e;
				Thread.sleep(RETRY_INTERVAL_MS);
			}
		}
		System.err.printf("[TempDirExtension] WARNING: could not delete '%s' for test '%s': %s%n", dir,
				context.getDisplayName(), last == null ? "unknown" : last.getMessage());
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
