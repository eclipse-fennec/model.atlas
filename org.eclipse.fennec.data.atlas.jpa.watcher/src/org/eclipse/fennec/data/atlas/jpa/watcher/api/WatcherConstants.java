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
package org.eclipse.fennec.data.atlas.jpa.watcher.api;

import org.eclipse.daanse.jdbc.datasource.h2.api.Constants;

/**
 * 
 * @author ilenia
 * @since May 11, 2026
 */
public interface WatcherConstants {

	String PID_DATA_FOLDER_WATCHER = "DataFolderWatcher";
	String PID_WORKSPACE_FOLDER_WATCHER = "WorkspaceFolderWatcher";

	// PIDs of the sub-components we configure dynamically
	String PID_EMF_FILE_WATCHER = "EMFFileWatcher";  //--> ecore
	String PID_ENTITY_MAPPINGS_FILE_WATCHER = "JpaMappingFileWatcher"; //--> eorm
	String PID_H2_DATA_SOURCE = Constants.PID_DATASOURCE; // --> DataSource
	String PID_CSV_IMPORTER = org.eclipse.daanse.sql.jdbc.importer.csv.api.Constants.PID_CSV_DATA_IMPORTER; //--> csv
	String PID_PERSISTENCE_UNIT = "fennec.jpa.EMPersistenceUnit"; //--> persistence unit



	String PROP_DATASOURCE_TARGET = "dataSource.target";
	String PROP_ENTITY_MANAGER_FACTORY_TARGET = "entityManagerFactory.target";
	String PROP_EORM_MAPPING_NAME = "fennec.jpa.orm.mapping.name";

	String KEY_FILE_CONTEXT_MATCHER = "file.context.matcher";
	String KEY_JPA_ROOT_FOLDER = "jpa.root.folder";

}
