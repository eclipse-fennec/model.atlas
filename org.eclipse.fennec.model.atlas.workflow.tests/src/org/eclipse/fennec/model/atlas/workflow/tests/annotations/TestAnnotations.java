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
package org.eclipse.fennec.model.atlas.workflow.tests.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.mgmt.annotations.MacCapabilityConstants;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.osgi.annotation.bundle.Requirement;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.typedevent.annotations.RequireTypedEvent;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.Scalar;
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

	public static final String PID_EPACKAGE_POST_RELEASE_SERVICE = "EPackagePostReleaseActionService";

    public static final String PID_STORAGE_REGISTRY = "BasicStorageRegistry";
    
    @StorageSetup
    @WithFactoryConfiguration(factoryPid = PID_STORAGE_REGISTRY, name = "workflow", location = "?", properties = {
            @Property(key = "storage.registry.name", value = "basic"),
            @Property(key = "storage.target", value = "(storage.type=file)"),
            @Property(key = "storage.cardinality.minimum", scalar = Scalar.Integer, value = "1") })
    @Retention(RetentionPolicy.RUNTIME)
    @RequireTypedEvent
    public @interface StorageRegistrySetup {
    }
   
    @StorageSetup
    @WithFactoryConfiguration(factoryPid = PID_EPACKAGE_POST_RELEASE_SERVICE, name = "post-release-action-service", location = "?", properties = {
            @Property(key = "releaseStorage.target", value = "(storage.type=file)") })
    @Retention(RetentionPolicy.RUNTIME)
    @RequireTypedEvent
    public @interface PostActionStorageSetup {
    }
    

}
