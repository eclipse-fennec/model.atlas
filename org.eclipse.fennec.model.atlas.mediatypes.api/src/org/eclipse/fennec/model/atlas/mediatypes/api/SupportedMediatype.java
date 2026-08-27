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
     * The service property carrying the current list, as a {@code String[]}.
     *
     * <p>
     * The list is not fixed: it is derived from the bound {@code ResourceSet}, and grows as codecs
     * register content types. The service properties are refreshed whenever it changes, so a
     * consumer that needs to react — rather than to ask — can declare an {@code updated} method on
     * its reference and be called when they do. The properties are a notification and a
     * convenience; {@link #getSupportedMediaTypes()} stays the answer.
     * </p>
     */
    String MEDIATYPES_PROPERTY = "mediatypes";

    /**
     * @return a List of the supported MediaTypes. You will receive a copy that can
     *         be modified to your hearts content.
     */
    List<String> getSupportedMediaTypes();

}
