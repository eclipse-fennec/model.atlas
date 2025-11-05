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
package org.gecko.mac.management.file.tests.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.gecko.mac.management.lucene.tests.annotations.LuceneTestAnnotations;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.TemplateArgument;
import org.osgi.test.common.annotation.Property.ValueSource;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;

/**
 * Test configuration annotations for Model Atlas Cloud management file storage tests.
 * 
 * <p>These annotations provide predefined OSGi configurations for integration tests,
 * reducing boilerplate code and ensuring consistent test setups.</p>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
public class FileTestAnnotations extends LuceneTestAnnotations {

    public static final String PROP_TEMP_DIR = "tempDir";
    
    /**
     * File Object Storage Service factory PID.
     */
    public static final String PID_FILE_STORAGE = "FileObjectStorage";

    /**
     * File storage configuration with specified role.
     * 
     * <p>This annotation configures a FileObjectStorage instance with:</p>
     * <ul>
     * <li>Workspace folder based on system property and test method name</li>
     * <li>Lucene indexing enabled</li>
     * <li>Storage role set to "test-role"</li>
     * </ul>
     */
    @WithFactoryConfiguration(factoryPid = PID_FILE_STORAGE, name = "test", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
    })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FileStorageConfiguration {
    }

    /**
     * Complete test setup with both shared registry and file storage.
     * 
     * <p>This meta-annotation combines:</p>
     * <ul>
     * <li>{@link SharedRegistryConfiguration} - Shared registry service</li>
     * <li>{@link FileStorageConfiguration} - File storage service</li>
     * </ul>
     * 
     * <p>Use this annotation for tests that need both services configured and working together.</p>
     */
    @RegistryConfiguration
    @FileStorageConfiguration
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DefaultFileStorageSetup {}

    /**
     * File storage configuration with custom role.
     * 
     * <p>Creates a FileObjectStorage instance with a custom storage role.
     * Useful for testing role-specific functionality.</p>
     */
    @WithFactoryConfiguration(factoryPid = PID_FILE_STORAGE, name = "custom-role", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.role", value = "custom-role")
    })
    @RegistryConfiguration
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomRoleFileStorageSetup {}

    /**
     * Multiple file storage instances with different roles.
     * 
     * <p>This setup creates three file storage instances:</p>
     * <ul>
     * <li>Draft storage with role "draft"</li>
     * <li>Approved storage with role "approved"</li>
     * <li>Documentation storage with role "documentation"</li>
     * </ul>
     * 
     * <p>Useful for testing multi-role scenarios and cross-storage queries.</p>
     */
    @RegistryConfiguration
    @WithFactoryConfiguration(factoryPid = PID_FILE_STORAGE, name = "draft", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/draft-storage", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.role", value = "draft")
    })
    @WithFactoryConfiguration(factoryPid = PID_FILE_STORAGE, name = "approved", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/approved-storage", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR),
            @TemplateArgument(source = ValueSource.TestMethod)
        }),
        @Property(key = "storage.role", value = "approved")
    })
    @WithFactoryConfiguration(factoryPid = PID_FILE_STORAGE, name = "documentation", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/documentation-storage", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR),
            @TemplateArgument(source = ValueSource.TestMethod)
        }),
        @Property(key = "storage.role", value = "documentation")
    })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MultiRoleFileStorageSetup {}
}