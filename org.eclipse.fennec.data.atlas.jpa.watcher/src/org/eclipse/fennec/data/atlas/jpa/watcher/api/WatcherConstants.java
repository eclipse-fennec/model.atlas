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
 * Configuration PIDs and property names shared by the JPA folder watchers.
 *
 * <p>
 * The watchers turn folder contents into a running JPA stack: each watched data
 * folder gets its own set of downstream components, created by writing factory
 * configurations for the PIDs below and wired together with the {@code *_TARGET}
 * filter properties. The names are contract: they must match the PIDs the daanse and
 * fennec bundles actually register, so they are declared here once instead of being
 * repeated as literals.
 * </p>
 *
 * @author ilenia
 * @since May 11, 2026
 */
public interface WatcherConstants {

	/** Factory PID of the watcher that turns one data folder into a JPA stack. */
	String PID_DATA_FOLDER_WATCHER = "DataFolderWatcher";
	/** Factory PID of the watcher that scans the workspace root for data folders. */
	String PID_WORKSPACE_FOLDER_WATCHER = "WorkspaceFolderWatcher";

	// PIDs of the sub-components a DataFolderWatcher configures per folder. Each is a
	// factory PID owned by another bundle; the watcher creates and deletes instances.
	/** Watcher that registers the folder's {@code .ecore} models as EPackages. */
	String PID_EMF_FILE_WATCHER = "EMFFileWatcher";
	/** Watcher that registers the folder's {@code .eorm} JPA mapping files. */
	String PID_ENTITY_MAPPINGS_FILE_WATCHER = "JpaMappingFileWatcher";
	/** H2 {@code DataSource} for the folder's database (daanse). */
	String PID_H2_DATA_SOURCE = Constants.PID_DATASOURCE;
	/** Importer that loads the folder's {@code .csv} files into that database (daanse). */
	String PID_CSV_IMPORTER = org.eclipse.daanse.sql.jdbc.importer.csv.api.Constants.PID_CSV_DATA_IMPORTER;
	/** Persistence unit bound to the folder's DataSource and mappings. */
	String PID_PERSISTENCE_UNIT = "fennec.jpa.EMPersistenceUnit";

	/** Target filter selecting the folder's {@code DataSource} service. */
	String PROP_DATASOURCE_TARGET = "dataSource.target";
	/** Target filter selecting the folder's {@code EntityManagerFactory} service. */
	String PROP_ENTITY_MANAGER_FACTORY_TARGET = "entityManagerFactory.target";
	/** Name under which a folder's {@code .eorm} mapping is registered and referenced. */
	String PROP_EORM_MAPPING_NAME = "fennec.jpa.orm.mapping.name";

	/** Per-folder discriminator, propagated to every component of that folder's stack. */
	String KEY_FILE_CONTEXT_MATCHER = "file.context.matcher";
	/** Absolute path of the folder a stack was created for. */
	String KEY_JPA_ROOT_FOLDER = "jpa.root.folder";

}
