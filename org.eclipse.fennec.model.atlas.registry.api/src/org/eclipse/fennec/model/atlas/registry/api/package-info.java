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
 * Client access to registry content (issue #386).
 *
 * <p>
 * {@link org.eclipse.fennec.model.atlas.registry.api.RegistryWriter} is the single entry point
 * through which external clients — the REST layer, the initial-model bootstrap, batch imports —
 * change what a registry holds. It owns the rules a client is held to: the type gate (root types
 * only, never derived content), conflict handling, read-only and precondition checks, package
 * identity and versioning, and keeping the package search index in line. Refusals are reported
 * protocol-neutrally as {@link org.eclipse.fennec.model.atlas.registry.api.RegistryAccessException};
 * the stage machinery's own refusals ({@code StagePolicyException}, {@code StageOccupiedException},
 * {@code StageGateRefusedException}) pass through unchanged.
 * </p>
 *
 * <p>
 * Trusted internal writers — stage actions that produce derived content or diagnostics — keep
 * using the workflow API directly: they are allowed what a client is not.
 * </p>
 */
@org.osgi.annotation.bundle.Export
@org.osgi.annotation.versioning.Version("1.0")
package org.eclipse.fennec.model.atlas.registry.api;
