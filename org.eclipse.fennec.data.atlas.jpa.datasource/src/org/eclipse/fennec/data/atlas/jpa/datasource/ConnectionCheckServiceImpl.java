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
package org.eclipse.fennec.data.atlas.jpa.datasource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Dictionary;
import java.util.UUID;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.eclipse.fennec.data.atlas.jpa.datasource.api.ConnectionCheckException;
import org.eclipse.fennec.data.atlas.jpa.datasource.api.ConnectionCheckService;
import org.eclipse.fennec.data.atlas.jpa.datasource.helper.DataSourceConfigHelper;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.osgi.util.tracker.ServiceTracker;

/**
 * 
 * @author ilenia
 * @since Apr 28, 2026
 */
@Component(name = "ConnectionCheckService", scope = ServiceScope.PROTOTYPE)
@RequireConfigurationAdmin
public class ConnectionCheckServiceImpl implements ConnectionCheckService {

	@Reference
	ConfigurationAdmin configAdmin;


	private PromiseFactory pf = new PromiseFactory(Executors.newCachedThreadPool());
	private UUID uuid;
	private Configuration config;
	private ServiceTracker<DataSource, DataSource> tracker;

	private BundleContext bundleContext;

	@Activate
	public ConnectionCheckServiceImpl(BundleContext bundleContext) {
		this.bundleContext = bundleContext;

	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.data.atlas.jpa.datasource.api.ConnectionCheckService#checkConnection(java.util.Map)
	 */
	@Override
	public Promise<Boolean> checkConnection(DataSourceConfig dataSourceConfig) {
		
		Dictionary<String, Object> properties = DataSourceConfigHelper.buildProperties(dataSourceConfig, null);
		uuid = UUID.randomUUID();
		properties.put("uuid", uuid.toString());
		Deferred<Boolean> deferred = pf.deferred();
		
		Filter filter = null;
		try {
			filter = createFilter("(uuid=" + uuid.toString() + ")");
		} catch(InvalidSyntaxException e) {
			deferred.fail(new ConnectionCheckException(String.format("Filter %s to look for DataSource has wrong syntax", "(uuid=" + uuid.toString() + ")"), e));
		}
		if(filter != null) {
			startTrackerForFilter(filter, deferred);
			try {
				config = DataSourceConfigHelper.createH2Config(configAdmin, uuid.toString(), properties); //daanse.jdbc.datasource.h2.DataSource~uuid
			} catch(IOException e) {
				deferred.fail(new ConnectionCheckException("IOException while trying to create DataSource Configuration via ConfigAdmin", e));
			}
		}

		return deferred.getPromise().onResolve(this::cleanup);
	}


	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.data.atlas.jpa.datasource.api.ConnectionCheckService#checkConnection(java.lang.String)
	 */
	@Override
	public Promise<Boolean> checkConnection(String dataSourceName) {
		Deferred<Boolean> deferred = pf.deferred();
		Filter filter = null;
		try {
			filter = createFilter("(" + DataSourceConfigHelper.DATA_SOURCE_NAME + "=" + dataSourceName + ")");
		} catch(InvalidSyntaxException e) {
			deferred.fail(new ConnectionCheckException(String.format("Filter %s to look for DataSource has wrong syntax", "(" + DataSourceConfigHelper.DATA_SOURCE_NAME + "=" + dataSourceName + ")"), e));
		}
		if(filter != null) {
			startTrackerForFilter(filter, deferred);
		}
		return deferred.getPromise();
	}

	private Filter createFilter(String filterStr) throws InvalidSyntaxException {
		return bundleContext.createFilter(filterStr);
	}

	private void startTrackerForFilter(Filter filter, Deferred<Boolean> deferred) {

		tracker = new ServiceTracker<DataSource, DataSource>(bundleContext, filter, null) {
			/* 
			 * (non-Javadoc)
			 * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
			 */
			@Override
			public DataSource addingService(ServiceReference<DataSource> reference) {
				DataSource ds = bundleContext.getService(reference);
				try {
					doConnectionCheck(ds);
					deferred.resolve(true);
				} catch (Exception e) {
					deferred.fail(new ConnectionCheckException("Exception while trying to test connection", e));
				}
				return ds;
			}
		};

		tracker.open(true);

	}


	private void doConnectionCheck(DataSource dataSource) throws SQLException {
		dataSource.getConnection();
	}

	private void cleanup() {
		if(tracker != null) tracker.close();
		if(config != null) {
			try {
				config.delete();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}


}
