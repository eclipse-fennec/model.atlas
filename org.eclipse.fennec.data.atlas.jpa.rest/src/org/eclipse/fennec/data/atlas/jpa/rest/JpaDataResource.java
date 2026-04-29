package org.eclipse.fennec.data.atlas.jpa.rest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@RequireRuntime
@JakartarsResource()
@JakartarsName("JpaDataResource")
@Component(name = "JpaDataResource", service = JpaDataResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/jpa/data")
public class JpaDataResource {
	
	@GET
	@Path("/hello")
	@Produces(MediaType.TEXT_PLAIN)
	public Response hello() {
		return Response.ok("Hello JpaDataResource").build();
	}
	
    @GET
    @Path("/{unitName}/{eClassName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve all objects of a certain eClassName associated with the provided unitName",description = "Retrieve all objects of a certain eClassName associated with the provided unitName", responses = {
            @ApiResponse(responseCode = "200", description = "Retrieval was succesfull."),
            @ApiResponse(responseCode = "400", description = "Connection not testable or failed. The message should explain the reason."),
            @ApiResponse(responseCode = "408", description = "Timeout of 5 sec reached without getting any connection result."),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response getEObjectsByEClass(@PathParam("unitName") String unitName, @PathParam("eClassName") String eClassName) {
   
        
        return Response.status(Status.NOT_IMPLEMENTED).build();      
    }
    
  
    
}
