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

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentServiceObjects;

/**
 * Test-only {@link ComponentServiceObjects} that creates a fresh
 * {@link ResourceSet} per {@link #getService()} call (matching the
 * prototype-scope semantics of real OSGi DS prototype services). Tracks the
 * set of instances that have been handed out but not yet returned, so tests
 * can assert that every {@code getService()} is balanced by an
 * {@code ungetService()} &mdash; even under concurrent load.
 */
public final class PrototypeCountingCso implements ComponentServiceObjects<ResourceSet> {

	private final Set<ResourceSet> outstanding = Collections.synchronizedSet(
			Collections.newSetFromMap(new IdentityHashMap<>()));
	private final AtomicInteger getCount = new AtomicInteger();
	private final AtomicInteger ungetCount = new AtomicInteger();

	@Override
	public ResourceSet getService() {
		getCount.incrementAndGet();
		ResourceSet rs = new ResourceSetImpl();
		outstanding.add(rs);
		return rs;
	}

	@Override
	public void ungetService(ResourceSet service) {
		ungetCount.incrementAndGet();
		outstanding.remove(service);
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

	public int outstandingCount() {
		return outstanding.size();
	}
}
