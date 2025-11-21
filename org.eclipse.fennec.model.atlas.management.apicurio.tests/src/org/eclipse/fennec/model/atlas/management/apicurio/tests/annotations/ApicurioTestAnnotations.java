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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.management.apicurio.tests.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.eclipse.fennec.model.atlas.management.lucene.tests.annotations.LuceneTestAnnotations.RegistryConfiguration;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;

/**
 * 
 * @author ilenia
 * @since Nov 19, 2025
 */
public class ApicurioTestAnnotations {
	
	/**
     * Apicurio Object Storage Service factory PID.
     */
    public static final String PID_APICURIO_STORAGE = "ApicurioObjectStorage";
    
    public static final String PROP_TEMP_DIR = "tempDir";
        
    /**
     * Apicurio default storage configuration.
     * 
     * <p>This annotation configures a ApicurioObjectStorage instance with:</p>
     * <ul>
     * <li>base_url as default</li>
     * <li>artifact_group_id as default</li>
     * <li>storage_role as default</li>
     * </ul>
     */
    @WithFactoryConfiguration(factoryPid = PID_APICURIO_STORAGE, name = "test", location = "?")
    @RegistryConfiguration
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DefaultApicurioStorageConfiguration {
    }
    
    @WithFactoryConfiguration(factoryPid = PID_APICURIO_STORAGE, name = "test", location = "?", properties = {
    		@Property(key = "storage.role", value = "custom-role")})
    @RegistryConfiguration
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomRoleApicurioStorageConfiguration {
    }

}
