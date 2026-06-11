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
package org.eclipse.fennec.model.atlas.rest.filter.tests;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentServiceObjects;

/**
 * Test-only {@link ComponentServiceObjects} that always returns the same
 * {@link ResourceSet} instance and counts get/unget invocations. Used by
 * {@code ScopedResourceSetIntegrationTest} to verify that the HK2 Factory's
 * {@code dispose()} hook is actually called at the end of every request.
 */
public final class CountingCso implements ComponentServiceObjects<ResourceSet> {

	private final ResourceSet resourceSet;
	private final AtomicInteger getCount = new AtomicInteger();
	private final AtomicInteger ungetCount = new AtomicInteger();

	public CountingCso(ResourceSet resourceSet) {
		this.resourceSet = resourceSet;
	}

	@Override
	public ResourceSet getService() {
		getCount.incrementAndGet();
		return resourceSet;
	}

	@Override
	public void ungetService(ResourceSet service) {
		ungetCount.incrementAndGet();
	}

	@Override
	public ServiceReference<ResourceSet> getServiceReference() {
		return null;
	}

	public int getServiceCalls() {
		return getCount.get();
	}

	public int ungetServiceCalls() {
		return ungetCount.get();
	}

	public void reset() {
		getCount.set(0);
		ungetCount.set(0);
	}
}
