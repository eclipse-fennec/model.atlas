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
package org.eclipse.fennec.model.atlas.workflow.tests.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.gecko.emf.osgi.annotation.require.RequireEMF;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.TemplateArgument;
import org.osgi.test.common.annotation.Property.Type;
import org.osgi.test.common.annotation.Property.ValueSource;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;

/**
 * 
 * @author ilenia
 * @since Nov 26, 2025
 */
@RequireEMF
@RequireConfigurationAdmin
public class ScopeAwareWorkflowTestAnnotations {
	
	public static final String PROP_TEMP_DIR = "tempDir";
	
	@WithFactoryConfiguration(factoryPid = "LuceneEObjectRegistryService", name = "registry", location = "?", properties = {
	        @Property(key = "registry.workspace.folder", value = "%s/registry", templateArguments = {
	            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
	        })
	    })
	@Retention(RetentionPolicy.RUNTIME)
    public @interface RegistryConfiguration {}
	
	@WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "tenant-draft", location = "?", properties = {
	        @Property(key = "workspace.folder", value = "%s/tenant-draft", templateArguments = {
	            @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
	        }),
	        @Property(key = "storage.scope", value = "my-tenant"),
	        @Property(key = "storage.role", value = "draft")
	    })
	@Retention(RetentionPolicy.RUNTIME)
    public @interface DraftStorageConfiguration {}
	
	@WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "tenant-release", location = "?", properties = {
            @Property(key = "workspace.folder", value = "%s/tenant-release", templateArguments = {
                @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR)
            }),
            @Property(key = "storage.scope", value = "my-tenant"),
            @Property(key = "storage.role", value = "release")
        })
	@Retention(RetentionPolicy.RUNTIME)
    public @interface ReleaseStorageConfiguration {}
	
	@WithFactoryConfiguration(factoryPid = "EObjectWorkflowService", name = "tenant-workflow", location = "?", properties = {
	        @Property(key = "scope", value = "my-tenant"),
	        @Property(key = "parent.scope", value = ""),
	        @Property(key = "stages", value = {"draft", "release"}, type = Type.Array),
	        @Property(key = "writable.stages", value = {"draft", "release"}, type = Type.Array)
	    })
	@Retention(RetentionPolicy.RUNTIME)
    public @interface SimpleWorkflowConfiguration {}
}
