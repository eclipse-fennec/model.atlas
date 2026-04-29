package org.eclipse.fennec.data.atlas.jpa.rest;

import org.eclipse.fennec.data.atlas.jpa.datasource.api.ConnectionCheckService;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.SqlDialect;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.TimeoutException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@RequireRuntime
@JakartarsResource()
@JakartarsName("JpaMappingResource")
@Component(name = "JpaMappingResource", service = JpaMappingResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/jpa")
public class JpaMappingResource {
	
	@Reference
	ConnectionCheckService connectionCheckService;
	
	@GET
	@Path("/hello")
	@Produces(MediaType.TEXT_PLAIN)
	public Response hello() {
		return Response.ok("Hello").build();
	}
	
    @POST
    @Path("/test")
    @Consumes({"application/xml", "application/xmi"})
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Test a JpaMappingConfig object to check whether the connection to the DataSource would be OK",description = "Test a JpaMappingConfig object to check whether the connection to the DataSource would be OK", responses = {
            @ApiResponse(responseCode = "200", description = "Connection to DataSource successfully tested"),
            @ApiResponse(responseCode = "400", description = "Connection not testable or failed. The message should explain the reason."),
            @ApiResponse(responseCode = "408", description = "Timeout of 5 sec reached without getting any connection result."),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response testConnection(
    		@RequestBody(description = "The JpaMappingConfig to be tested", required = true, content = @Content(schema = @Schema(implementation = JpaMappingConfig.class))) 
    		JpaMappingConfig config) {
   
        DataSourceConfig ds = config.getDataSource();
        if (ds == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("JpaMappingConfig has no DataSourceConfig").build();
        }
        if (ds.getDialect() != SqlDialect.H2) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Unsupported dialect: " + ds.getDialect() + ". Only H2 is currently supported").build();
        }
        if(!"org.h2.Driver".equals(ds.getDriverClass())) {
        	return Response.status(Status.BAD_REQUEST)
                    .entity("Unsupported Driver Class: " + ds.getDriverClass() + ". Only org.h2.Driver is currently supported").build();
        }

        String jdbcUrl = ds.getJdbcUrl();
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("DataSourceConfig has no JDBC URL").build();
        }
        
        Promise<Boolean> promise = connectionCheckService.checkConnection(ds);
        try {
        	Throwable t = promise.timeout(5000).getFailure();
        	if (t instanceof TimeoutException) {
        		return Response.status(Status.REQUEST_TIMEOUT).entity("Timeout of 5 sec has been reached but no result from the connection test was returned").build();
        	} else if (t != null) {
        		return Response.status(Status.BAD_REQUEST).entity(t.getMessage()).build();
        	} else {
        		return Response.ok().build();
        	}
        } catch(InterruptedException e) {
        	return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Thread was interrupted while waiting for connection test result").build();
        }
              
    }
    
    @GET
    @Path("/test/{dataSourceName}")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Test the connection to a DataSource with the provided name", description = "Test the connection to a DataSource with the provided name", responses = {
            @ApiResponse(responseCode = "200", description = "Connection to DataSource successfully tested"),
            @ApiResponse(responseCode = "400", description = "Connection failed. The message should explain the reason."),
            @ApiResponse(responseCode = "408", description = "Timeout of 5 sec reached without getting any connection result."),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response testConnectionByName(@PathParam("dataSourceName") String dataSourceName) {
    	Promise<Boolean> promise = connectionCheckService.checkConnection(dataSourceName);
        try {
        	Throwable t = promise.timeout(5000).getFailure();
        	if (t instanceof TimeoutException) {
        		return Response.status(Status.REQUEST_TIMEOUT).entity("Timeout of 5 sec has been reached but no result from the connection test was returned").build();
        	} else if (t != null) {
        		return Response.status(Status.BAD_REQUEST).entity(t.getMessage()).build();
        	} else {
        		return Response.ok().build();
        	}
        } catch(InterruptedException e) {
        	return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Thread was interrupted while waiting for connection test result").build();
        }
              
    }
    
}
