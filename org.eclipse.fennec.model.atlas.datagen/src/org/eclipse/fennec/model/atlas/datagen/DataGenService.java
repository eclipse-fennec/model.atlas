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
	 * @return map of EClass name to list of generated EObject instances
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
