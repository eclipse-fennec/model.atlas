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
package org.eclipse.fennec.model.atlas.rest.client.impl;

import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClient;
import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClientFactory;

import aQute.bnd.annotation.spi.ServiceProvider;

/**
 * {@link java.util.ServiceLoader}-discovered factory behind {@link ModelAtlasClient#builder()}.
 * <p>
 * The {@link ServiceProvider} annotation makes bnd emit the
 * {@code META-INF/services/…ModelAtlasClientFactory} descriptor (and the
 * matching {@code osgi.serviceloader} capability) so plain-Java
 * {@code ServiceLoader} look-ups resolve this provider.
 *
 * @see java.util.ServiceLoader
 */
@ServiceProvider(ModelAtlasClientFactory.class)
public class DefaultModelAtlasClientFactory implements ModelAtlasClientFactory {

	@Override
	public ModelAtlasClient.Builder builder() {
		return new ModelAtlasClientBuilderImpl();
	}
}
