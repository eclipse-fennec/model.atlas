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

/**
 * Where an instance lives: one object id in one stage of one registry of one scope.
 *
 * @param scope    the scope name
 * @param registry the registry name
 * @param stage    the stage name
 * @param objectId the object id
 */
public record ObjectAddress(String scope, String registry, String stage, String objectId) {
}
