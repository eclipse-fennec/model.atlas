/*
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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.registry.api;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

/**
 * Shorthands for building the requests of {@link RegistryWriter} and {@link RegistryReader}
 * through {@link RegistryApiFactory}. The parameters are documented on the model's features.
 */
public final class RegistryRequests {

	private static final RegistryApiFactory FACTORY = RegistryApiFactory.eINSTANCE;

	private RegistryRequests() {
	}

	/** @return an {@link ObjectAddress} */
	public static ObjectAddress objectAddress(String scope, String registry, String stage, String objectId) {
		ObjectAddress address = FACTORY.createObjectAddress();
		address.setScope(scope);
		address.setRegistry(registry);
		address.setStage(stage);
		address.setObjectId(objectId);
		return address;
	}

	/** @return a {@link PackageAddress} */
	public static PackageAddress packageAddress(String scope, String stage, String nsUri) {
		PackageAddress address = FACTORY.createPackageAddress();
		address.setScope(scope);
		address.setStage(stage);
		address.setNsUri(nsUri);
		return address;
	}

	/**
	 * @param onConflict {@code null} means {@link ConflictPolicy#FAIL}
	 * @return an {@link ObjectWrite}
	 */
	public static ObjectWrite objectWrite(ObjectAddress address, EObject content, String name, String version,
			ConflictPolicy onConflict, Precondition precondition) {
		ObjectWrite write = FACTORY.createObjectWrite();
		write.setAddress(address);
		write.setContent(content);
		write.setName(name);
		write.setVersion(version);
		write.setOnConflict(onConflict == null ? ConflictPolicy.FAIL : onConflict);
		write.setPrecondition(precondition);
		return write;
	}

	/**
	 * @param onConflict {@code null} means {@link ConflictPolicy#FAIL}
	 * @return a {@link PackageWrite}
	 */
	public static PackageWrite packageWrite(String scope, String registry, String stage, EPackage content,
			String nsUri, String name, String version, Boolean dcat, ConflictPolicy onConflict,
			Precondition precondition, boolean lenientVersion) {
		PackageWrite write = FACTORY.createPackageWrite();
		write.setScope(scope);
		write.setRegistry(registry);
		write.setStage(stage);
		write.setContent(content);
		write.setNsUri(nsUri);
		write.setName(name);
		write.setVersion(version);
		write.setDcat(dcat);
		write.setOnConflict(onConflict == null ? ConflictPolicy.FAIL : onConflict);
		write.setPrecondition(precondition);
		write.setLenientVersion(lenientVersion);
		return write;
	}
}
