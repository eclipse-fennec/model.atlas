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
package org.gecko.mac.mgmt.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.osgi.annotation.bundle.Requirement;

/**
 * Meta annotation requiring file-based storage capabilities.
 * 
 * <p>This annotation declares an OSGi requirement for file-based
 * EObject storage services with Lucene indexing and role-based
 * workspace isolation.</p>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({
    ElementType.TYPE, ElementType.PACKAGE
})
@Requirement(namespace = "mac.management", //
    name = "EObjectStorage", //
    version = "1.0")
public @interface RequireEObjectStorage {

}