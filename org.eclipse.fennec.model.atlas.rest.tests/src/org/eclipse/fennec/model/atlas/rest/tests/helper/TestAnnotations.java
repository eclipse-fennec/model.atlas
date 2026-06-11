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
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.osgi.annotation.bundle.Requirement;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.Scalar;
import org.osgi.test.common.annotation.Property.Type;
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
public class TestAnnotations extends CommonTestAnnotations{

	public static final String PID_SCOPE_SERVICE = "ScopeService";

	public static final String OBJECT_REGISTRY_NAME = "person";

	public static final String TEST_SCOPE_NAME = "test-scope";

	public static final String TEST_PARENT_SCOPE_NAME = "test-parent-scope";


	@EPackageLuceneIndexSetup
	@StorageSetup
	@WithFactoryConfiguration(factoryPid = PID_REGISTRY_SERVICE, name = OBJECT_REGISTRY_NAME, location = "?", properties = {
			@Property(key = "registry.name", value = OBJECT_REGISTRY_NAME),
			@Property(key = "registry.type", value = "OBJECT"),
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
