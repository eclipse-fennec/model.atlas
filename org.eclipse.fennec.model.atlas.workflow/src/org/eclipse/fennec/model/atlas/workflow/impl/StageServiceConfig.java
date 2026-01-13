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
package org.eclipse.fennec.model.atlas.workflow.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * 
 * @author ilenia
 * @since Jan 13, 2026
 */
@ObjectClassDefinition(name = "Stage Service Configuration")
public @interface StageServiceConfig {
    @AttributeDefinition(name = "Stage Name", description = "The name of this stage")
    String stage_name();

    @AttributeDefinition(name = "Writable", description = "Whether this stage allows modifications")
    boolean stage_writable() default true;

    @AttributeDefinition(name = "Final Stage", description = "Whether this is the final stage")
    boolean stage_final() default false;
}