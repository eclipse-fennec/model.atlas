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
package org.eclipse.fennec.data.atlas.jpa.datasource.api;

import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig;
import org.osgi.util.promise.Promise;

/**
 * 
 * @author ilenia
 * @since Apr 28, 2026
 */
public interface ConnectionCheckService {
	
	Promise<Boolean> checkConnection(DataSourceConfig dataSourceConfig);
	
	Promise<Boolean> checkConnection(String dataSourceName);

}
