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
package org.eclipse.fennec.model.atlas.datagen;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig;

/**
 * Service for generating EObject instances based on a {@link DataGenConfig}
 * and target EPackages. Uses Datafaker expressions for attribute value generation.
 */
public interface DataGenService {

	/**
	 * Generates EObject instances based on the given configuration and target packages.
	 *
	 * @param config the data generation configuration
	 * @param targetPackages the EPackages containing the target EClasses
	 * @return map of contextClass URI — the {@code nsURI#//ClassName} value configured
	 *         per class, not a bare EClass name — to the instances generated for it
	 */
	Map<String, List<EObject>> generate(DataGenConfig config, List<EPackage> targetPackages);

	/**
	 * Convenience method that returns all generated instances as a flat list.
	 *
	 * @param config the data generation configuration
	 * @param targetPackages the EPackages containing the target EClasses
	 * @return flat list of all generated EObject instances
	 */
	List<EObject> generateFlat(DataGenConfig config, List<EPackage> targetPackages);
}
