package org.eclipse.fennec.data.atlas.jpa.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.ColumnMapping;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig;
import org.gecko.emf.utilities.UtilitiesFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@RequireRuntime
@JakartarsResource()
@JakartarsName("JpaDataResource")
@Component(name = "JpaDataResource", service = JpaDataResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/jpa/data")
public class JpaDataResource {

    private static final String PROP_TARGET_NS_URI = "jpamapping.targetNsUri";
    private static final String PROP_UNIT_NAME = "unitName";
    private static final String CONFLICT_HINT = " Use the ?ePackageUri=... query parameter to disambiguate.";

    private sealed interface EmfResolution {
        record Error(Response response) implements EmfResolution {}
        record Success(
                JpaMappingConfig jpaMappingConfig,
                ServiceReference<EntityManagerFactory> emfRef,
                EntityManagerFactory emf) implements EmfResolution {}
    }

    private final BundleContext ctx;

    @Activate
    public JpaDataResource(BundleContext ctx) {
        this.ctx = ctx;
    }

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public Response hello() {
        return Response.ok("Hello JpaDataResource").build();
    }

    @GET
    @Path("/{eClassName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve all objects of a certain eClassName", responses = {
            @ApiResponse(responseCode = "200", description = "Retrieval was successful."),
            @ApiResponse(responseCode = "204", description = "No objects found for the given EClass."),
            @ApiResponse(responseCode = "404", description = "No JpaMappingConfig or persistence unit found."),
            @ApiResponse(responseCode = "409", description = "Multiple JpaMappingConfigs match eClassName. Provide ePackageUri to disambiguate."),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response getEObjectsByEClass(
            @PathParam("eClassName") String eClassName,
            @QueryParam("ePackageUri") String ePackageUri,
            @QueryParam("limit") @DefaultValue("100") int limit) {

        EmfResolution resolution = resolveEmf(eClassName, ePackageUri);
        if (resolution instanceof EmfResolution.Error e) {
            return e.response();
        }
        EmfResolution.Success r = (EmfResolution.Success) resolution;

        try (EntityManager em = r.emf().createEntityManager()) {
            List<?> results = em.createQuery("SELECT e FROM " + eClassName + " e")
                    .setMaxResults(limit)
                    .getResultList();

            if (results.isEmpty()) {
                return Response.noContent().build();
            }

            org.gecko.emf.utilities.Response response = UtilitiesFactory.eINSTANCE.createResponse();
            response.getData().addAll(results.stream()
                    .filter(EObject.class::isInstance)
                    .map(EObject.class::cast).toList());
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("Failed to load data for '" + eClassName + "': " + e.getMessage()).build();
        } finally {
            ctx.ungetService(r.emfRef());
        }
    }

    @GET
    @Path("/{eClassName}/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve an object of a certain eClassName by id", responses = {
            @ApiResponse(responseCode = "200", description = "Retrieval was successful."),
            @ApiResponse(responseCode = "204", description = "No object found for the given id."),
            @ApiResponse(responseCode = "400", description = "id cannot be parsed to the primary key type."),
            @ApiResponse(responseCode = "404", description = "No JpaMappingConfig or persistence unit found."),
            @ApiResponse(responseCode = "409", description = "Multiple JpaMappingConfigs match eClassName. Provide ePackageUri to disambiguate."),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response getEObjectsById(
            @PathParam("eClassName") String eClassName,
            @PathParam("id") String id,
            @QueryParam("ePackageUri") String ePackageUri) {

        EmfResolution resolution = resolveEmf(eClassName, ePackageUri);
        if (resolution instanceof EmfResolution.Error e) {
            return e.response();
        }
        EmfResolution.Success r = (EmfResolution.Success) resolution;

        ColumnMapping pkMapping = r.jpaMappingConfig().getTableMappings().stream()
                .filter(tm -> tm.getClassName().endsWith("#//" + eClassName))
                .flatMap(tm -> tm.getColumnMappings().stream())
                .filter(ColumnMapping::isPrimaryKey)
                .findFirst()
                .orElse(null);

        if (pkMapping == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("No primary key mapping found for: " + eClassName).build();
        }

        Object parsedId;
        try {
            parsedId = parseId(id, pkMapping.getColumnType());
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Cannot parse id '" + id + "' for column type " + pkMapping.getColumnType()
                            + ": " + e.getMessage()).build();
        }

        try (EntityManager em = r.emf().createEntityManager()) {
            List<?> results = em.createQuery(
                    "SELECT e FROM " + eClassName + " e WHERE e." + pkMapping.getFeatureName() + " = :id")
                    .setParameter("id", parsedId)
                    .getResultList();

            if (results.isEmpty()) {
                return Response.noContent().build();
            }

            org.gecko.emf.utilities.Response response = UtilitiesFactory.eINSTANCE.createResponse();
            response.getData().addAll(results.stream()
                    .filter(EObject.class::isInstance)
                    .map(EObject.class::cast).toList());
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("Failed to load data for '" + eClassName + "': " + e.getMessage()).build();
        } finally {
            ctx.ungetService(r.emfRef());
        }
    }

    /**
     * Resolves the JpaMappingConfig and EntityManagerFactory for the given eClassName.
     * Returns {@link EmfResolution.Error} with a ready-made Response on any failure,
     * or {@link EmfResolution.Success} with the resolved resources on success.
     */
    private EmfResolution resolveEmf(String eClassName, String ePackageUri) {
        Collection<ServiceReference<JpaMappingConfig>> jpaMappingRefs;
        try {
            jpaMappingRefs = findJpaMappingRefs(eClassName, ePackageUri);
        } catch (InvalidSyntaxException e) {
            return new EmfResolution.Error(Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Invalid filter: " + e.getMessage()).build());
        }
        if (jpaMappingRefs.isEmpty()) {
            return new EmfResolution.Error(Response.status(Status.NOT_FOUND)
                    .entity("No JpaMappingConfig found for class '" + eClassName + "'.").build());
        }
        if (jpaMappingRefs.size() > 1) {
            return new EmfResolution.Error(Response.status(Status.CONFLICT)
                    .entity("Multiple JpaMappingConfigs found for class '" + eClassName + "'." + CONFLICT_HINT).build());
        }

        ServiceReference<JpaMappingConfig> jpaMappingRef = jpaMappingRefs.iterator().next();
        String unitName = (String) jpaMappingRef.getProperty(PROP_UNIT_NAME);
        if (unitName == null) {
            return new EmfResolution.Error(Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("JpaMappingConfig without unitName property found.").build());
        }

        Collection<ServiceReference<EntityManagerFactory>> emfRefs;
        try {
            emfRefs = ctx.getServiceReferences(EntityManagerFactory.class,
                    "(osgi.unit.name=" + unitName + ")");
        } catch (InvalidSyntaxException e) {
            return new EmfResolution.Error(Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Invalid unit name filter: " + e.getMessage()).build());
        }
        if (emfRefs == null || emfRefs.isEmpty()) {
            return new EmfResolution.Error(Response.status(Status.NOT_FOUND)
                    .entity("No persistence unit found for name: " + unitName).build());
        }

        ServiceReference<EntityManagerFactory> emfRef = emfRefs.iterator().next();
        EntityManagerFactory emf = ctx.getService(emfRef);
        if (emf == null) {
            return new EmfResolution.Error(Response.status(Status.NOT_FOUND)
                    .entity("Persistence unit '" + unitName + "' is not available.").build());
        }

        JpaMappingConfig config = ctx.getService(jpaMappingRef);
        return new EmfResolution.Success(config, emfRef, emf);
    }

    /**
     * Finds JpaMappingConfig service references that have a TableMapping for the given eClassName.
     * If ePackageUri is provided, filters by it directly via the OSGi registry (fast path).
     * Otherwise, fetches all JpaMappingConfig services and inspects each one in code,
     * releasing non-matching references immediately.
     */
    private Collection<ServiceReference<JpaMappingConfig>> findJpaMappingRefs(String eClassName, String ePackageUri)
            throws InvalidSyntaxException {
        if (ePackageUri != null && !ePackageUri.isBlank()) {
            Collection<ServiceReference<JpaMappingConfig>> refs = ctx.getServiceReferences(
                    JpaMappingConfig.class, "(" + PROP_TARGET_NS_URI + "=" + ePackageUri + ")");
            return refs != null ? refs : List.of();
        }
        Collection<ServiceReference<JpaMappingConfig>> allRefs =
                ctx.getServiceReferences(JpaMappingConfig.class, null);
        if (allRefs == null || allRefs.isEmpty()) {
            return List.of();
        }
        List<ServiceReference<JpaMappingConfig>> matching = new ArrayList<>();
        for (ServiceReference<JpaMappingConfig> ref : allRefs) {
            JpaMappingConfig config = ctx.getService(ref);
            boolean matches = config != null && config.getTableMappings().stream()
                    .anyMatch(tm -> tm.getClassName().endsWith("#//" + eClassName));
            if (matches) {
                matching.add(ref);
            } else {
                ctx.ungetService(ref);
            }
        }
        return matching;
    }

    private Object parseId(String id, String columnType) {
        if (columnType == null) {
            return id;
        }
        String baseType = columnType.contains("(")
                ? columnType.substring(0, columnType.indexOf('(')).trim()
                : columnType.trim();
        return switch (baseType.toUpperCase()) {
            case "INTEGER", "INT", "SMALLINT" -> Integer.parseInt(id);
            case "BIGINT" -> Long.parseLong(id);
            case "DECIMAL", "NUMERIC", "REAL", "FLOAT", "DOUBLE" -> Double.parseDouble(id);
            case "BOOLEAN" -> Boolean.parseBoolean(id);
            default -> id;
        };
    }
}
