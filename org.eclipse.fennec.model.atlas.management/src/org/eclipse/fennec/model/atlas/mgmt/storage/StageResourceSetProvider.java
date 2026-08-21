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
package org.eclipse.fennec.model.atlas.mgmt.storage;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.osgi.annotation.versioning.ProviderType;
import org.osgi.service.component.ComponentServiceObjects;

/**
 * Resolves the {@link ResourceSet} that knows the models of a given (scope,
 * stage) location, so storage backends can re-load stored <em>instances</em> of
 * dynamically registered EPackages (issue #190).
 *
 * <p>
 * The management ResourceSet the storage services own only aggregates the
 * statically registered models; instances of schemas uploaded at runtime need
 * the per-(scope, stage) ResourceSet whose package registry tracks the dynamic
 * registrations. This interface lives in the management bundle so storage
 * backends can consume it without depending on the workflow bundle — the
 * workflow bundle (which owns the scope/stage ResourceSet chain) provides the
 * implementation.
 * </p>
 *
 * <p>
 * Callers own the {@link ComponentServiceObjects#getService() getService()} /
 * {@link ComponentServiceObjects#ungetService(Object) ungetService()} lease
 * lifecycle.
 * </p>
 */
@ProviderType
public interface StageResourceSetProvider {

    /**
     * Returns a lease for the {@link ResourceSet} of the given (scope, stage)
     * pair, or {@code null} if none is currently known.
     */
    ComponentServiceObjects<ResourceSet> getResourceSetObjects(String scope, String stage);
}
