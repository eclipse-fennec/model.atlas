package org.eclipse.fennec.data.atlas.jpa.rest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
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

    private static final java.util.logging.Logger LOG =
            java.util.logging.Logger.getLogger(JpaDataResource.class.getName());

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
    @Path("/test/{unitName}/{eClassName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve all objects of a certain eClassName associated with the provided unitName", description = "Retrieve all objects of a certain eClassName associated with the provided unitName", responses = {
            @ApiResponse(responseCode = "200", description = "Retrieval was successful."),
            @ApiResponse(responseCode = "204", description = "No objects found for the given EClass."),
            @ApiResponse(responseCode = "404", description = "No persistence unit found for the given unitName."),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response getEObjectsByEClass2(
            @PathParam("unitName") String unitName,
            @PathParam("eClassName") String eClassName,
            @QueryParam("limit") @DefaultValue("100") int limit) {

        Collection<ServiceReference<EntityManagerFactory>> refs;
        try {
            refs = ctx.getServiceReferences(EntityManagerFactory.class,
                    "(osgi.unit.name=" + unitName + ")");
        } catch (InvalidSyntaxException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Invalid unit name: " + unitName).build();
        }

        if (refs == null || refs.isEmpty()) {
            return Response.status(Status.NOT_FOUND)
                    .entity("No persistence unit found for name: " + unitName).build();
        }

        ServiceReference<EntityManagerFactory> ref = refs.iterator().next();
        EntityManagerFactory emf = ctx.getService(ref);
        if (emf == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Persistence unit '" + unitName + "' is not available").build();
        }

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
        } finally {
            ctx.ungetService(ref);
        }
    }
    
    @GET
    @Path("/{unitName}/{eClassName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve all objects of a certain eClassName associated with the provided unitName", description = "Retrieve all objects of a certain eClassName associated with the provided unitName", responses = {
            @ApiResponse(responseCode = "200", description = "Retrieval was successful."),
            @ApiResponse(responseCode = "204", description = "No objects found for the given EClass."),
            @ApiResponse(responseCode = "404", description = "No persistence unit found for the given unitName."),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response getEObjectsByEClass(
            @PathParam("unitName") String unitName,
            @PathParam("eClassName") String eClassName,
            @QueryParam("limit") @DefaultValue("100") int limit) {

        Collection<ServiceReference<DataSource>> refs;
        try {
            refs = ctx.getServiceReferences(DataSource.class,
                    "(data.source.name=" + unitName + ")");
        } catch (InvalidSyntaxException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Invalid data source name: " + unitName).build();
        }

        if (refs == null || refs.isEmpty()) {
            return Response.status(Status.NOT_FOUND)
                    .entity("No persistence unit found for name: " + unitName).build();
        }

        ServiceReference<DataSource> ref = refs.iterator().next();
        DataSource emf = ctx.getService(ref);
        if (emf == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Persistence unit '" + unitName + "' is not available").build();
        }

        try(Connection connection = emf.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + eClassName);
            statement.execute();
            ResultSet resultSet = statement.getResultSet();
            
            return Response.ok(resultSet).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("Failed to load data for '" + eClassName + "': " + e.getMessage()).build();
        } finally {
            ctx.ungetService(ref);
        }
    }
}
