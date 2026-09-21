/*
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
/**
 * The stage lifecycle SPI: an implementation of {@link
 * org.eclipse.fennec.model.atlas.action.api.StageActionService} is picked up as a
 * whiteboard service and called when an object enters, is updated in, or leaves a stage;
 * an implementation of {@link org.eclipse.fennec.model.atlas.action.api.StageGate} is
 * asked <em>before</em> a stage transition and may refuse it (issue #248).
 *
 * <p>
 * This package deliberately depends on nothing but {@code java.*} and the OSGi promise
 * API, so providing an action does not require the workflow bundle or EMF.
 * </p>
 */
@org.osgi.annotation.bundle.Export
@org.osgi.annotation.versioning.Version("1.1")
package org.eclipse.fennec.model.atlas.action.api;
