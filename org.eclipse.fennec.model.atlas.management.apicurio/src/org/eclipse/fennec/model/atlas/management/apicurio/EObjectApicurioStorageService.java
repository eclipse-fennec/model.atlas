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
 *      Ilenia Salvadori - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.management.apicurio;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.mgmt.annotations.MacCapabilityConstants;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.StorageBackendType;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractEObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper;
import org.gecko.emf.osgi.annotation.require.RequireEMF;
import org.osgi.annotation.bundle.Capability;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@RequireEMF
@RequireConfigurationAdmin
@Component(
		name = EObjectApicurioStorageService.PID,
		service = EObjectStorageService.class,
		property = "storage.backend=apicurio",
		configurationPolicy = ConfigurationPolicy.REQUIRE
		)
@Designate(ocd = EObjectApicurioStorageService.Config.class)
@Capability(namespace = MacCapabilityConstants.NAMESPACE_MAC_MANAGEMENT, name = MacCapabilityConstants.CAP_EOBJECT_STORAGE, version = "1.0", attribute = "storage.backend=apicurio")
@ServiceDescription("Apicurio-based storage implementation for EObject workflow management")
public class EObjectApicurioStorageService extends AbstractEObjectStorageService {

	@Reference
	private EObjectRegistryService<EObject> registry;

	@Reference(target = "(emf.name=management)")
	private ResourceSet resourceSet;

	@ObjectClassDefinition(
			name = "Apicurio Storage Configuration",
			description = "Configuration for apicurio-based EObject storage service"
			)
	public @interface Config {

		@AttributeDefinition(
				name = "Base Apicurio URL",
				description = "Base Apicurio URL for REST requests"
				)
		String base_url() default "http://localhost:8080/apis/registry/v3/";

		@AttributeDefinition(
				name = "Artifact Group Id",
				description = "Artifact Group Id to identify under which group the artifacts should be stored in the Apicurio Registry"
				)
		String artifact_group_id() default "default";

		@AttributeDefinition(
				name = "Storage Role",
				description = "Role of this storage service (draft, approved, documentation, etc.)"
				)
		String storage_role() default "draft";
	}

	public static final String PID = "ApicurioObjectStorage";
	private String apicurioURL;
	private String storageRole;

	@Activate
	public void activate(BundleContext bundleContext, Config config) throws Exception {
		this.bctx = bundleContext;
		this.apicurioURL = constructApicurioURL(config.base_url(), config.artifact_group_id());
		this.storageRole = config.storage_role();    

		// Call parent activation
		activateStorageService();
	}

	@Deactivate
	public void deactivate() {
		deactivateStorageService();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractEObjectStorageService#createStorageHelper()
	 */
	@Override
	protected AbstractStorageHelper createStorageHelper() throws Exception {
		return new ApicurioStorageHelper(resourceSet, apicurioURL);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractEObjectStorageService#getBackendType()
	 */
	@Override
	public StorageBackendType getBackendType() {
		return StorageBackendType.APICURIO;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractEObjectStorageService#getStorageRole()
	 */
	@Override
	protected String getStorageRole() {
		return storageRole;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.storage.AbstractEObjectStorageService#getRegistryService()
	 */
	@Override
	protected EObjectRegistryService<EObject> getRegistryService() {
		return registry;
	}

	private String constructApicurioURL(String baseURL, String gourpId) {
		return baseURL.concat("groups/").concat(gourpId).concat("/artifacts");
	}
}
