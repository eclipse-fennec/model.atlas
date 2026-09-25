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
package org.eclipse.fennec.model.atlas.workflow.dependency;

import java.util.List;

import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * A bundle that knows how its objects reference schema packages contributes that edge type
 * to the {@link SchemaDependents dependents query} by registering one of these as an OSGi
 * service. The query does the view arithmetic (which stages see a package served from a
 * given stage) and asks the contributor per registry and stage; the contributor only has to
 * look at what is stored there.
 */
@ConsumerType
public interface SchemaDependentsContributor {

    /**
     * The objects stored in {@code stage} of {@code registry} that reference the package
     * {@code nsURI}.
     *
     * @param scope    the scope
     * @param registry the registry to look in; a contributor that does not know the registry's
     *                 kind of objects answers an empty list
     * @param stage    the stage of that registry to look in
     * @param nsURI    the package's namespace URI
     * @return the dependents found there; never {@code null}
     */
    List<SchemaDependent> dependentsIn(String scope, RegistryService<?> registry, String stage, String nsURI);
}
