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
/**
 * The rules every producer of {@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic
 * diagnostics} shares: how an id is derived, how a producer's diagnostics replace their
 * predecessors on an object (issue #292).
 */
@org.osgi.annotation.bundle.Export
@org.osgi.annotation.versioning.Version("1.0.0")
package org.eclipse.fennec.model.atlas.mgmt.diagnostics;
