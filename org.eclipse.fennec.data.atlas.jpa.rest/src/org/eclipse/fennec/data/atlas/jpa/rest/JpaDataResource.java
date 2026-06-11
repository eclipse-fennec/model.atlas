package org.eclipse.fennec.data.atlas.jpa.rest;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.persistence.eorm.Column;
import org.eclipse.fennec.persistence.eorm.Entity;
import org.eclipse.fennec.persistence.eorm.EntityMappings;
import org.eclipse.fennec.persistence.eorm.Id;
import org.gecko.emf.utilities.UtilitiesFactory;
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
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@RequireRuntime
@JakartarsResource()
@JakartarsName("JpaDataResource")
@Component(name = "JpaDataResource", service = JpaDataResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/jpa/{rootFolderName}/data")
public class JpaDataResource {
	
	@Context
    private ContainerRequestContext requestContext;

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
            @ApiResponse(responseCode = "404", description = "No EntityMappings or persistence unit found."),
            @ApiResponse(responseCode = "409", description = "Multiple EntityMappings match eClassName. Provide ePackageUri to disambiguate."),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response getEObjectsByEClass(
            @PathParam("eClassName") String eClassName,
            @QueryParam("ePackageUri") String ePackageUri,
            @QueryParam("limit") @DefaultValue("100") int limit) {

        EntityManagerFactory emf = getResolvedEntityManagerFactory();

        try (EntityManager em = emf.createEntityManager()) {
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
        } 
    }


    @GET
    @Path("/{eClassName}/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve an object of a certain eClassName by id", responses = {
            @ApiResponse(responseCode = "200", description = "Retrieval was successful."),
            @ApiResponse(responseCode = "204", description = "No object found for the given id."),
            @ApiResponse(responseCode = "400", description = "id cannot be parsed to the primary key type."),
            @ApiResponse(responseCode = "404", description = "No EntityMappings or persistence unit found."),
            @ApiResponse(responseCode = "409", description = "Multiple EntityMappings match eClassName. Provide ePackageUri to disambiguate."),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response getEObjectsById(
            @PathParam("eClassName") String eClassName,
            @PathParam("id") String id,
            @QueryParam("ePackageUri") String ePackageUri) {
    	
    	EntityMappings entityMappings = getResolvedEntityMappings();
    	EntityManagerFactory emf = getResolvedEntityManagerFactory();

     

        Id pk = findPrimaryKey(entityMappings, eClassName);
        if (pk == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("No primary-key Id mapping found for: " + eClassName).build();
        }
        Column pkColumn = pk.getColumn();
        String pkColumnDefinition = pkColumn != null ? pkColumn.getColumnDefinition() : null;

        Object parsedId;
        try {
            parsedId = parseId(id, pkColumnDefinition);
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Cannot parse id '" + id + "' for column type " + pkColumnDefinition
                            + ": " + e.getMessage()).build();
        }

        try (EntityManager em = emf.createEntityManager()) {
            List<?> results = em.createQuery(
                    "SELECT e FROM " + eClassName + " e WHERE e." + pk.getName() + " = :id")
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
        } 
    }


    private Id findPrimaryKey(EntityMappings em, String eClassName) {
        Entity entity = em.getEntity().stream()
                .filter(e -> eClassName.equals(e.getName()))
                .findFirst()
                .orElse(null);
        if (entity == null || entity.getAttributes() == null) {
            return null;
        }
        return entity.getAttributes().getId().stream().findFirst().orElse(null);
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
    
    private EntityManagerFactory getResolvedEntityManagerFactory() {
        return (EntityManagerFactory) requestContext.getProperty("entity.manager.factory");
    }
    
    private EntityMappings getResolvedEntityMappings() {
        return (EntityMappings) requestContext.getProperty("entity.mappings");
    }

}
