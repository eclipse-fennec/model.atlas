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
package org.eclipse.fennec.data.atlas.jpa.datasource.helper;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * 
 * @author ilenia
 * @since Apr 28, 2026
 */
public class DataSourceConfigHelper {

	private static final String H2_PID = "daanse.jdbc.datasource.h2.DataSource";
	private static final String PROP_IDENTIFIER = "identifier";
	private static final String PROP_USERNAME = "username";
	private static final String PROP_PASSWORD = ".password";
	private static final String JDBC_H2_PREFIX = "jdbc:h2:";
	public static final String DATA_SOURCE_NAME = "data.source.name";

	public static Configuration createH2Config(ConfigurationAdmin configAdmin, String name, String unitName, DataSourceConfig ds) throws IOException {
		Configuration cfg = configAdmin.getFactoryConfiguration(H2_PID, name, "?");
		Dictionary<String, Object> properties = buildProperties(ds, name);
		properties.put("unitName", unitName);
		cfg.update(properties);
		return cfg;
	}
	
	public static Configuration createH2Config(ConfigurationAdmin configAdmin, String name, Dictionary<String, Object> properties) throws IOException {
		Configuration cfg = configAdmin.getFactoryConfiguration(H2_PID, name, "?");
		cfg.update(properties);
		return cfg;
	}

	public static Dictionary<String, Object> buildProperties(DataSourceConfig ds, String dataSourceName) {
		Dictionary<String, Object> properties = new Hashtable<>();
		if(dataSourceName != null) properties.put(DATA_SOURCE_NAME, dataSourceName);
		String jdbcUrl = ds.getJdbcUrl();
		if (jdbcUrl != null) {
			String identifier = jdbcUrl.startsWith(JDBC_H2_PREFIX)
					? jdbcUrl.substring(JDBC_H2_PREFIX.length())
							: jdbcUrl;
			properties.put(PROP_IDENTIFIER, identifier);
		}
		if (ds.getUsername() != null) {
			properties.put(PROP_USERNAME, ds.getUsername());
		}
		if (ds.getPasswordRef() != null) {
			properties.put(PROP_PASSWORD, ds.getPasswordRef());
		}
		return properties;
	}

}
