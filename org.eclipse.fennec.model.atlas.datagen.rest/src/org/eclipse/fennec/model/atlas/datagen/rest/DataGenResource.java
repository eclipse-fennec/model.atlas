package org.eclipse.fennec.model.atlas.datagen.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.datagen.DataGenService;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenResult;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST resource for generating test data from a {@link DataGenConfig}.
 * Accepts a DataGenConfig as XMI and returns a DataGenResult containing
 * the generated EObject instances.
 */
@JakartarsResource
@JakartarsName("DataGenResource")
@Component(name = "DataGenResource", service = DataGenResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/datagen")
public class DataGenResource {

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private DataGenService dataGenService;

	@Reference
	private ResourceSet resourceSet;

	@POST
	@Consumes("application/xmi")
	@Produces({"application/xmi", MediaType.APPLICATION_JSON})
	public Response generate(DataGenConfig config) {
		try {
			List<EPackage> targetPackages = resolvePackages(config);
			Map<String, List<EObject>> generated = dataGenService.generate(config, targetPackages);

			DataGenResult result = DatagenFactory.eINSTANCE.createDataGenResult();
			generated.values().forEach(result.getResults()::addAll);

			return Response.ok(result).build();
		} catch (IllegalArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	/**
	 * Resolves the EPackages needed for the given config from the ResourceSet's package registry.
	 * Validates that all referenced EClasses can be found.
	 *
	 * @throws IllegalArgumentException if a referenced EClass cannot be found in any registered package
	 */
	private List<EPackage> resolvePackages(DataGenConfig config) {
		EPackage.Registry registry = resourceSet.getPackageRegistry();
		List<EPackage> packages = new ArrayList<>();
		List<String> missingClasses = new ArrayList<>();

		for (ClassGenConfig classConfig : config.getClassConfigs()) {
			if (!classConfig.isEnabled()) {
				continue;
			}
			String className = classConfig.getContextClass();
			boolean found = false;

			for (Object value : registry.values()) {
				EPackage pkg = value instanceof EPackage ep ? ep : ((EPackage.Descriptor) value).getEPackage();
				if (pkg == null) {
					continue;
				}
				if (findClassInPackage(pkg, className) != null) {
					if (!packages.contains(pkg)) {
						packages.add(pkg);
					}
					found = true;
					break;
				}
			}
			if (!found) {
				missingClasses.add(className);
			}
		}

		if (!missingClasses.isEmpty()) {
			throw new IllegalArgumentException(
					"EClasses not found in any registered EPackage: " + String.join(", ", missingClasses));
		}

		return packages;
	}

	private EClassifier findClassInPackage(EPackage pkg, String className) {
		// Check simple name
		EClassifier classifier = pkg.getEClassifier(className);
		if (classifier != null) {
			return classifier;
		}
		// Check qualified name (package.ClassName)
		String qualifiedName = pkg.getName() + "." + className;
		if (qualifiedName.equals(className)) {
			return null; // already checked
		}
		// Check subpackages
		for (EPackage sub : pkg.getESubpackages()) {
			classifier = findClassInPackage(sub, className);
			if (classifier != null) {
				return classifier;
			}
		}
		return null;
	}
}
