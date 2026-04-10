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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.rest.tests.helper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.mgmt.annotations.MacCapabilityConstants;
import org.osgi.annotation.bundle.Requirement;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.Scalar;
import org.osgi.test.common.annotation.Property.TemplateArgument;
import org.osgi.test.common.annotation.Property.Type;
import org.osgi.test.common.annotation.Property.ValueSource;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;

/**
 * Test configuration annotations for Governance workflow tests.
 *
 * <p>
 * These annotations provide predefined OSGi configurations for governance
 * workflow integration tests, setting up the complete stack including storage
 * services, registry service, and workflow service.
 * </p>
 *
 * @author Mark Hoffmann
 * @since 1.0.0
 */
@RequireEMF
@RequireConfigurationAdmin
@Requirement(namespace = MacCapabilityConstants.NAMESPACE_MAC_MANAGEMENT, name = MacCapabilityConstants.CAP_EOBJECT_STORAGE, filter = "(storage.backend=file)")
public class TestAnnotations {

	public static final String PROP_TEMP_DIR = "tempDir";

	/**
	 * Shared Lucene Registry Service PID (not factory).
	 */
	public static final String PID_SHARED_REGISTRY = "LuceneEObjectRegistryService";

	/**
	 * File Object Storage Service factory PID.
	 */
	public static final String PID_FILE_STORAGE = "FileObjectStorage";

	public static final String PID_REGISTRY_SERVICE = "RegistryService";

	public static final String PID_SCOPE_SERVICE = "ScopeService";

	public static final String PID_EPACKAGE_INDEX_SERVICE = "EPackageLuceneIndex";

	public static final String SCHEMA_REGISTRY_NAME = "schema";

	public static final String OBJECT_REGISTRY_NAME = "person";

	public static final String TEST_SCOPE_NAME = "test-scope";

	public static final String TEST_PARENT_SCOPE_NAME = "test-parent-scope";

	public static final String STAGE_DRAFT = "draft";

	public static final String STAGE_APPROVED = "approved";

	public static final String STAGE_RELEASE = "release";


	@WithFactoryConfiguration(factoryPid = PID_SHARED_REGISTRY, name = "shared-registry", location = "?", properties = {
			@Property(key = "registry.workspace.folder", value = "%s/shared-registry", templateArguments = {
					@TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR) }),
			@Property(key = "registry", value = "main"),
			@Property(key = "storage.backend.tracking", value = "true"),
			@Property(key = "initial.index.capacity", value = "1000"),
			@Property(key = "enable.debug.logging", value = "true") })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface RegistryConfiguration {
	}


	@RegistryConfiguration
	@WithFactoryConfiguration(factoryPid = PID_FILE_STORAGE, name = "file-storage", location = "?", properties = {
			@Property(key = "workspace.folder", value = "%s/file-storage", templateArguments = {
					@TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR) }),
			@Property(key = "storage.type", value = "file"),
			@Property(key = "registry.target", value = "(registry=main)")})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface StorageSetup {
	}

	@WithFactoryConfiguration(factoryPid = PID_EPACKAGE_INDEX_SERVICE, name = "epackage-index", location = "?", properties = {
			@Property(key = "index.folder", value = "%s/epackage-index", templateArguments = {
					@TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR) })})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface EPackageLuceneIndexSetup{

	}

	@EPackageLuceneIndexSetup
	@StorageSetup
	@WithFactoryConfiguration(factoryPid = PID_REGISTRY_SERVICE, name = SCHEMA_REGISTRY_NAME, location = "?", properties = {
			@Property(key = "registry.name", value = SCHEMA_REGISTRY_NAME),
			@Property(key = "schema.registry", value = "true", scalar = Scalar.Boolean),
			@Property(key = "schema.uri", value = "http://www.eclipse.org/emf/2002/Ecore"),
			@Property(key = "root.eclass.uri", value = "http://www.eclipse.org/emf/2002/Ecore#//EPackage"),
			@Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
			@Property(key = "storageService.target", value = "(storage.type=file)" ),
			@Property(key = "registry.target", value = "(registry=main)"),
			@Property(key = "stages", type = Type.Array, value = {
					"{ \"name\" : \"" + STAGE_DRAFT    + "\", \"writable\" : true, \"final\": false}",
					"{ \"name\" : \"" + STAGE_APPROVED + "\", \"writable\" : true, \"final\": false}",                                                                                                                                                                                
					"{ \"name\" : \"" + STAGE_RELEASE  + "\", \"writable\" : true, \"final\": true}",
			}),                                                                                                                                                                                                                                                               
			@Property(key = "workflow.transitions", type = Type.Array,                                                                                                                                                                                                            
			value = { STAGE_DRAFT + ":" + STAGE_APPROVED, STAGE_APPROVED + ":" + STAGE_RELEASE }),
			@Property(key = "stage.storage.mappings", type = Type.Array, value = { STAGE_DRAFT +":file", STAGE_APPROVED+":file",
			STAGE_RELEASE+":file" })})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface SchemaRegistryServiceSetup {

	}

	@EPackageLuceneIndexSetup
	@StorageSetup
	@WithFactoryConfiguration(factoryPid = PID_REGISTRY_SERVICE, name = OBJECT_REGISTRY_NAME, location = "?", properties = {
			@Property(key = "registry.name", value = OBJECT_REGISTRY_NAME),
			@Property(key = "schema.registry", value = "false", scalar = Scalar.Boolean),
			@Property(key = "schema.uri", value = "https://dg.de/1.0"),
			@Property(key = "root.eclass.uri", value = "https://dg.de/1.0#//Person"),
			@Property(key = "resourceSet.target", value = "(emf.name=dge)"),
			@Property(key = "storageService.target", value = "(storage.type=file)" ),
			@Property(key = "registry.target", value = "(registry=main)"),
			@Property(key = "stages", type = Type.Array, value = {
					"{ \"name\" : \"" + STAGE_DRAFT    + "\", \"writable\" : true, \"final\": false}",
					"{ \"name\" : \"" + STAGE_APPROVED + "\", \"writable\" : true, \"final\": false}",                                                                                                                                                                                
					"{ \"name\" : \"" + STAGE_RELEASE  + "\", \"writable\" : true, \"final\": true}",
			}),                                                                                                                                                                                                                                                               
			@Property(key = "workflow.transitions", type = Type.Array,                                                                                                                                                                                                            
			value = { STAGE_DRAFT + ":" + STAGE_APPROVED, STAGE_APPROVED + ":" + STAGE_RELEASE }),
			@Property(key = "stage.storage.mappings", type = Type.Array, value = { STAGE_DRAFT +":file", STAGE_APPROVED+":file",
			STAGE_RELEASE+":file" })})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ObjectRegistryServiceSetup {

	}

	@SchemaRegistryServiceSetup
	@ObjectRegistryServiceSetup
	@WithFactoryConfiguration(factoryPid = PID_SCOPE_SERVICE, name = TEST_SCOPE_NAME, location = "?", properties = {
			@Property(key = "scope.name", value = TEST_SCOPE_NAME),
			@Property(key = "scope.parent", value = TEST_PARENT_SCOPE_NAME),
			@Property(key = "registryService.target", value = "(|(registry.name="+SCHEMA_REGISTRY_NAME+")(registry.name="+OBJECT_REGISTRY_NAME+"))"),
			@Property(key = "registryService.cardinality.minimum", value = "2", scalar = Scalar.Integer)})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ScopeServiceSetup {
	}

	@ScopeServiceSetup
	@WithFactoryConfiguration(factoryPid = PID_SCOPE_SERVICE, name = TEST_PARENT_SCOPE_NAME, location = "?", properties = {
			@Property(key = "scope.name", value = TEST_PARENT_SCOPE_NAME),
			@Property(key = "registryService.target", value = "(|(registry.name="+SCHEMA_REGISTRY_NAME+")(registry.name="+OBJECT_REGISTRY_NAME+"))"),
			@Property(key = "registryService.cardinality.minimum", value = "2", scalar = Scalar.Integer)})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ParentScopeServiceSetup {
	}


}
