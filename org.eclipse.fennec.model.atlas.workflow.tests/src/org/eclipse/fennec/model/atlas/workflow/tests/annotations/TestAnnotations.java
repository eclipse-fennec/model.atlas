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
import org.osgi.annotation.bundle.Requirement;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.typedevent.annotations.RequireTypedEvent;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.Scalar;
import org.osgi.test.common.annotation.Property.TemplateArgument;
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

    /**
     * File Object Storage Registry Service factory PID.
     */
    public static final String PID_STORAGE_REGISTRY = "BasicStorageRegistry";

    /**
     * Basic shared registry configuration.
     *
     * <p>
     * This annotation configures a LuceneEObjectRegistryService instance with:
     * </p>
     * <ul>
     * <li>Storage backend tracking enabled</li>
     * <li>Debug logging enabled for troubleshooting</li>
     * <li>Workspace folder based on system property (typically temp directory)</li>
     * </ul>
     */
    @WithFactoryConfiguration(factoryPid = PID_SHARED_REGISTRY, name = "shared-registry", location = "?", properties = {
            @Property(key = "registry.workspace.folder", value = "%s/shared-registry", templateArguments = {
                    @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR) }),
            @Property(key = "storage.backend.tracking", value = "true"),
            @Property(key = "initial.index.capacity", value = "1000"),
            @Property(key = "enable.debug.logging", value = "true") })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RegistryConfiguration {
    }

    /**
     * Documentation-specific registry configuration.
     *
     * <p>
     * This annotation configures a separate LuceneEObjectRegistryService instance
     * for governance documentation:
     * </p>
     * <ul>
     * <li>Isolated workspace folder for documentation metadata</li>
     * <li>Separate registry property "documentation" for service targeting</li>
     * <li>Prevents governance documentation from appearing in main object
     * queries</li>
     * <li>Maintains clean separation between managed objects and governance
     * artifacts</li>
     * <li>Enables independent scaling and optimization for documentation
     * storage</li>
     * </ul>
     *
     * <p>
     * <strong>Architecture Benefits:</strong>
     * </p>
     * <ul>
     * <li><code>listApprovedObjects()</code> only returns actual managed objects
     * (EPackages, Routes, etc.)</li>
     * <li>Governance documentation queries are isolated to documentation-specific
     * operations</li>
     * <li>Registry performance optimized for each domain (objects vs
     * documentation)</li>
     * <li>Clear separation of concerns in the storage architecture</li>
     * <li>Documentation storage targets this registry via
     * <code>registry.target=(registry=documentation)</code></li>
     * </ul>
     */
    @WithFactoryConfiguration(factoryPid = PID_SHARED_REGISTRY, name = "doc-registry", location = "?", properties = {
            @Property(key = "registry.workspace.folder", value = "%s/doc-registry", templateArguments = {
                    @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR) }),
            @Property(key = "registry", value = "documentation"),
            @Property(key = "storage.backend.tracking", value = "true"),
            @Property(key = "initial.index.capacity", value = "1000"),
            @Property(key = "enable.debug.logging", value = "true") })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DocumentationRegistryConfiguration {
    }

    /**
     * Type-based storage setup for governance workflows.
     *
     * <p>
     * This setup creates the required storage infrastructure:
     * </p>
     * <ul>
     * <li>File storage with type "file" (uses main registry)</li>
     * <li>Main registry for managed objects (EPackages, Routes, etc.)</li>
     * <li>Separate documentation registry for governance documentation objects</li>
     * </ul>
     */
    @RegistryConfiguration
    @DocumentationRegistryConfiguration
    @WithFactoryConfiguration(factoryPid = PID_FILE_STORAGE, name = "file-storage", location = "?", properties = {
            @Property(key = "workspace.folder", value = "%s/file-storage", templateArguments = {
                    @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR) }),
            @Property(key = "storage.type", value = "file") })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface WorkflowStorageSetup {
    }

    /**
     * Storage registry setup for workflow tests.
     *
     * <p>
     * Combines WorkflowStorageSetup with a BasicStorageRegistry configuration.
     * </p>
     */
    @WorkflowStorageSetup
    @WithFactoryConfiguration(factoryPid = PID_STORAGE_REGISTRY, name = "workflow", location = "?", properties = {
            @Property(key = "storage.registry.name", value = "basic"),
            @Property(key = "storage.target", value = "(storage.type=file)"),
            @Property(key = "storage.cardinality.minimum", scalar = Scalar.Integer, value = "1") })
    @Retention(RetentionPolicy.RUNTIME)
    @RequireTypedEvent
    public @interface StorageRegistrySetup {
    }

    /**
     * Post-release action storage setup for testing EPackage registration.
     *
     * <p>
     * Configures file storage and the EPackagePostReleaseActionService for testing
     * post-release actions like EPackage registration in the OSGi EMF registry.
     * </p>
     */
    @RegistryConfiguration
    @WithFactoryConfiguration(factoryPid = PID_FILE_STORAGE, name = "file-storage", location = "?", properties = {
            @Property(key = "workspace.folder", value = "%s/file-storage", templateArguments = {
                    @TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR) }),
            @Property(key = "storage.type", value = "file") })
    @WithFactoryConfiguration(factoryPid = "EPackagePostReleaseActionService", name = "post-release-action-service", location = "?", properties = {
            @Property(key = "releaseStorage.target", value = "(storage.type=file)") })
    @Retention(RetentionPolicy.RUNTIME)
    @RequireTypedEvent
    public @interface PostActionStorageSetup {
    }
    
    @WorkflowStorageSetup
    @WithFactoryConfiguration(factoryPid = "ScopeService", name = "jena-scope", location = "?", properties = {
            @Property(key = "scope.name", value = "jena"),
            @Property(key = "registryService.target", value = "(registry.name=jena-schema)"),
            @Property(key = "registryService.cardinality.minimum", value = "1", scalar = Scalar.Integer)})
    public @interface ScopeServiceSetup {
    }
    
    @WithFactoryConfiguration(factoryPid = "RegistryService", name = "jena-schema-registry", location = "?", properties = {
            @Property(key = "registry.name", value = "jena-schema"),
            @Property(key = "schema.registry", value = "true", scalar = Scalar.Boolean),
            @Property(key = "schema.uri", value = "http://www.eclipse.org/emf/2002/Ecore"),
            @Property(key = "root.eclass.uri", value = "http://www.eclipse.org/emf/2002/Ecore#//EPackage"),
            @Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
            @Property(key = "storageService.target", value = "(storage.type=file)" )})
    public @interface SchemaRegistryServiceSetup {
    	
    }
}
