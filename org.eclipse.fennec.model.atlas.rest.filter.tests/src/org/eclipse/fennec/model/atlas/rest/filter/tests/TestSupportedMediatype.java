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

import java.util.List;

import org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype;

/**
 * High-priority test override of {@link SupportedMediatype}. Reports
 * {@code text/plain} and {@code application/json} as supported so the
 * {@code ModelAtlasRequestFilter}'s media-type resolution passes for the test
 * resource.
 *
 * <p>No {@code @Component} annotation — registered manually by the test
 * harness, see the rationale on {@link TestResourceSetCollector}.
 */
public class TestSupportedMediatype implements SupportedMediatype {

	@Override
	public List<String> getSupportedMediaTypes() {
		return List.of("text/plain", "application/json");
	}
}
