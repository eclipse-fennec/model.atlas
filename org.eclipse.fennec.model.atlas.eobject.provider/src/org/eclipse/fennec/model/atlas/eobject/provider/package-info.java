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
/**
 * Model Atlas source for named emf.osgi EObject registries: a plain-Java sync engine
 * ({@link org.eclipse.fennec.model.atlas.eobject.provider.AtlasObjectSync}) that reads
 * the objects of configured atlas registries from a
 * {@link org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService} and pushes them
 * into an {@link org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryWriter} -
 * one {@code sync} per atlas registry, keyed per domain convention, resilient against
 * the atlas being unavailable. The OSGi layer is a config-driven DS factory component
 * (component name {@code AtlasEObjectProvider}) on top.
 */
@org.osgi.annotation.bundle.Export
@org.osgi.annotation.versioning.Version("1.0.0")
package org.eclipse.fennec.model.atlas.eobject.provider;
