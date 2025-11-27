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
package org.eclipse.fennec.model.atlas.management.lucene.tests.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
public class LuceneTestAnnotations {

    public static final String PROP_TEMP_DIR = "tempDir";
    
    /**
     * Shared Lucene Registry Service PID (not factory).
     */
    public static final String PID_SHARED_REGISTRY = "LuceneEObjectRegistryService";
    
    /**
     * Basic shared registry configuration.
     * 
     * <p>This annotation configures a LuceneEObjectRegistryService instance with:</p>
     * <ul>
     * <li>Storage backend tracking enabled</li>
     * <li>Debug logging enabled for troubleshooting</li>
     * <li>Workspace folder based on system property (typically temp directory)</li>
     * </ul>
     */
    @WithFactoryConfiguration(factoryPid = PID_SHARED_REGISTRY, name = "shared-registry", location = "?", properties = {
        @Property(key = "registry.workspace.folder", value = "%s/shared-registry", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.backend.tracking", value = "true"),
        @Property(key = "initial.index.capacity", value = "1000"),
        @Property(key = "enable.debug.logging", value = "true")
    })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RegistryConfiguration {}
    
    /**
     * Advanced test registry configuration with different name to avoid conflicts.
     */
    @WithFactoryConfiguration(factoryPid = PID_SHARED_REGISTRY, name = "advanced-test-registry", location = "?", properties = {
        @Property(key = "registry.workspace.folder", value = "%s/advanced-registry", templateArguments = {
            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
        }),
        @Property(key = "storage.backend.tracking", value = "true"),
        @Property(key = "initial.index.capacity", value = "1000"),
        @Property(key = "enable.debug.logging", value = "true")
    })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface AdvancedRegistryConfiguration {}

  }