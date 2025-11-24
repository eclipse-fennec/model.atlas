/**
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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.mediatypes.api;

import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <p>
 * Keeps track of available MediaTypes for the Rest API
 * </p>
 * 
 * @since 1.0
 */
@ProviderType
public interface SupportedMediatype {

	/**
	 * @return a List of the supported MediaTypes
	 */
	List<String> getSupportedMediaTypes();

}
