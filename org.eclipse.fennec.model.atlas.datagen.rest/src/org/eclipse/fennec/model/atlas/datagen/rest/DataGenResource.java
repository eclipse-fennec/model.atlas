package org.eclipse.fennec.model.atlas.datagen.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.datagen.DataGenService;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenResult;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenFactory;
import org.eclipse.fennec.model.atlas.readable.scope.collector.ReadableScopeCollector;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
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
	
	private static final String JENA_SCOPE_NAME = "jena";
	private static final String DATA_GEN_REGISTRY_NAME = "DataGen";

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private DataGenService dataGenService;

	@Reference
	private ResourceSet resourceSet;
	
	@Reference
    private ReadableScopeCollector scopeCollector;

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
	
	@GET
	@Path("/{objectId}")
	@Produces({"application/xmi", MediaType.APPLICATION_JSON})
	public Response generateByObjectId(@PathParam("objectId") String objectId) {
		try {
			ReadableScopeService<?> scopeService = getScopeService();

			Optional<?> content = scopeService.get(DATA_GEN_REGISTRY_NAME, objectId);
            if (content.isEmpty() || !(content.get() instanceof DataGenConfig config)) {
            	return Response.status(Response.Status.NO_CONTENT).build();
            }
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
	/**
	 * Resolves the EPackages needed for the given config by parsing the EClass URIs
	 * from contextClass attributes. The URI format is {@code nsURI#//ClassName}.
	 *
	 * @throws IllegalArgumentException if a referenced EClass cannot be found
	 */
	private List<EPackage> resolvePackages(DataGenConfig config) {
		EPackage.Registry registry = resourceSet.getPackageRegistry();
		List<EPackage> packages = new ArrayList<>();
		List<String> missingClasses = new ArrayList<>();

		for (ClassGenConfig classConfig : config.getClassConfigs()) {
			if (!classConfig.isEnabled()) {
				continue;
			}
			String eClassUri = classConfig.getContextClass();
			int fragmentIndex = eClassUri.indexOf('#');
			if (fragmentIndex < 0) {
				missingClasses.add(eClassUri);
				continue;
			}
			String nsUri = eClassUri.substring(0, fragmentIndex);
			String fragment = eClassUri.substring(fragmentIndex + 3); // skip #//

			EPackage pkg = registry.getEPackage(nsUri);
			if (pkg == null) {
				missingClasses.add(eClassUri);
				continue;
			}

			EClassifier classifier = findClassByFragment(pkg, fragment);
			if (classifier == null) {
				missingClasses.add(eClassUri);
				continue;
			}

			if (!packages.contains(pkg)) {
				packages.add(pkg);
			}
		}

		if (!missingClasses.isEmpty()) {
			throw new IllegalArgumentException(
					"EClasses not found in any registered EPackage: " + String.join(", ", missingClasses));
		}

		return packages;
	}

	private EClassifier findClassByFragment(EPackage pkg, String fragment) {
		int slashIndex = fragment.indexOf('/');
		if (slashIndex < 0) {
			return pkg.getEClassifier(fragment);
		}
		String subPkgName = fragment.substring(0, slashIndex);
		String rest = fragment.substring(slashIndex + 1);
		for (EPackage sub : pkg.getESubpackages()) {
			if (sub.getName().equals(subPkgName)) {
				return findClassByFragment(sub, rest);
			}
		}
		return null;
	}
	
	private ReadableScopeService<?> getScopeService() {
        return scopeCollector.getScopeServiceByScopeName(JENA_SCOPE_NAME);
    }
}
