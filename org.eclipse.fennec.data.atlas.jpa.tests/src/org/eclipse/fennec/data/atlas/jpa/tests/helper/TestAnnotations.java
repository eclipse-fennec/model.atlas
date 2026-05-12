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
package org.eclipse.fennec.data.atlas.jpa.tests.helper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.eclipse.fennec.data.atlas.jpa.watcher.api.WatcherConstants;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.TemplateArgument;
import org.osgi.test.common.annotation.Property.ValueSource;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;

/**
 * 
 * @author ilenia
 * @since Apr 28, 2026
 */
@RequireEMF
@RequireConfigurationAdmin
public class TestAnnotations {
	

	private static final String PROP_DATA_FOLDER = "data-folder";
	private static final String PROP_MAPPING_FOLDER = "mapping-folder";
	public static final String JPA_MAPPING_NAME = "demo-mapping";
	public static final String TEMP_DIR = "tempDir";

	@WithFactoryConfiguration(factoryPid = WatcherConstants.PID_ENTITY_MAPPINGS_FILE_WATCHER, name = "test", location = "?", properties = {
			@Property(key = "io.fs.watcher.path", value = "%s/", templateArguments = {
					@TemplateArgument(source = ValueSource.SystemProperty, value = PROP_MAPPING_FOLDER) }),
			@Property(key = "io.fs.watcher.pattern", value = ".*\\.jpamapping"),
			@Property(key = "unitName", value = "demo-mapping")})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface JpaMappingWatcherConfig {
	}
	
	@WithFactoryConfiguration(factoryPid = WatcherConstants.PID_DATA_FOLDER_WATCHER, name = "test", location = "?", properties = {
			@Property(key = "io.fs.watcher.path", value = "%s/", templateArguments = {
					@TemplateArgument(source = ValueSource.SystemProperty, value = PROP_DATA_FOLDER) })})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface DataFolderWatcherConfig {
	}
	
	@WithFactoryConfiguration(factoryPid = WatcherConstants.PID_DATA_FOLDER_WATCHER, name = "temp", location = "?", properties = {
			@Property(key = "io.fs.watcher.path", value = "%s/", templateArguments = {
					@TemplateArgument(source = ValueSource.SystemProperty, value = TEMP_DIR) })})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface TempDataFolderWatcherConfig {
	}
	


}
