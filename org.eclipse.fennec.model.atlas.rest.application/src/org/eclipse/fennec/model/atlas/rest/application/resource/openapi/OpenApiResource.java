/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
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
package org.eclipse.fennec.model.atlas.rest.application.resource.openapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.workflow.ScopeServiceCollector;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.BaseOpenApiResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import io.swagger.v3.oas.integration.api.OpenApiScanner;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.servlet.ServletConfig;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/openapi.{type:json|yaml}")
@Component(name = OpenApiResource.COMPONENT_NAME, service = OpenApiResource.class, scope = ServiceScope.PROTOTYPE)
@JakartarsResource
public class OpenApiResource extends BaseOpenApiResource {

	/** OPEN_API_RESOURCE */
	public static final String COMPONENT_NAME = "OpenApiResource";

	@Context
	private Application app;

	@Context
	private ServletConfig config;

	@Reference
	ScopeServiceCollector scopeCollector;

	private final List<String> supportedMediaTypes;

	@Activate
	public OpenApiResource(@Reference SupportedMediatype types) {
	    supportedMediaTypes = types.getSupportedMediaTypes();
	}
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON, "application/yaml" })
	@Operation(hidden = true)
	public Response getOpenApi(@Context HttpHeaders headers, @Context UriInfo uriInfo, @PathParam("type") String type)
			throws Exception {

		SwaggerConfiguration openApiConfig = new SwaggerConfiguration().prettyPrint(true);

		String ctxId = app.getClass().getCanonicalName().concat("#")
				.concat(String.valueOf(System.identityHashCode(app)));

		OpenApiContext ctx = new JaxrsOpenApiContextBuilder<>().servletConfig(config).application(app)
				.openApiConfiguration(openApiConfig).ctxId(ctxId).buildContext(true);

		ctx.setOpenApiScanner(new OpenApiScanner() {

			@Override
			public void setConfiguration(OpenAPIConfiguration openApiConfiguration) {
				// TODO Auto-generated method stub
			}

			@Override
			public Set<Class<?>> classes() {
				return app.getClasses();
			}

			@Override
			public Map<String, Object> resources() {
				// TODO Auto-generated method stub
				return null;
			}

		});

		OpenAPI oas = ctx.read();

		if (oas == null) {
			return Response.status(404).build();
		}

		// Enhance OpenAPI spec with supported media types
		enhanceWithSupportedMediaTypes(oas);

		// Enhance OpenAPI spec with scope information
		enhanceWithScopeInformation(oas);

		boolean pretty = Optional.ofNullable(ctx.getOpenApiConfiguration()).map(OpenAPIConfiguration::isPrettyPrint)
				.orElse(Boolean.FALSE);

		if (Optional.ofNullable(type).map(String::trim).map("yaml"::equalsIgnoreCase).orElse(Boolean.FALSE)) {
			return Response.status(Response.Status.OK)
					.entity(pretty ? Yaml.pretty(oas) : Yaml.mapper().writeValueAsString(oas)).type("application/yaml")
					.build();
		} else {
			return Response.status(Response.Status.OK)
					.entity(pretty ? Json.pretty(oas) : Json.mapper().writeValueAsString(oas))
					.type(MediaType.APPLICATION_JSON_TYPE).build();
		}
	}

	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Operation(hidden = true)
	@Path("test")
	public Response getOpenApiTest(@Context HttpHeaders headers, @Context UriInfo uriInfo,
			@PathParam("type") String type) throws Exception {

		String ctxId = app.getClass().getCanonicalName().concat("#")
				.concat(String.valueOf(System.identityHashCode(app)));

		return Response.ok(ctxId).build();
	}

	/**
	 * Enhances the OpenAPI specification with supported media types for all endpoints
	 *
	 * @param oas the OpenAPI specification to enhance
	 */
	private void enhanceWithSupportedMediaTypes(OpenAPI oas) {
		if (oas == null || oas.getPaths() == null) {
			return;
		}

//		List<String> supportedTypes = mediatypes.getSupportedMediaTypes();
		if (supportedMediaTypes == null || supportedMediaTypes.isEmpty()) {
			return;
		}

		oas.getPaths().forEach((pathName, pathItem) -> {
			pathItem.readOperations().forEach(operation -> {
				// Add supported media types to response content
				if (operation.getResponses() != null) {
					operation.getResponses().forEach((statusCode, apiResponse) -> {
						if (apiResponse != null && apiResponse.getContent() != null && !apiResponse.getContent().isEmpty()) {
							// Only add media types if content is already defined (meaning the response returns content)
							// Add each supported media type if not already present
							supportedMediaTypes.forEach(mediaType -> {
								if (!apiResponse.getContent().containsKey(mediaType)) {
									io.swagger.v3.oas.models.media.MediaType mt = new io.swagger.v3.oas.models.media.MediaType();
									// Add a generic schema for the media type
									mt.setSchema(new Schema<>().type("object"));
									apiResponse.getContent().addMediaType(mediaType, mt);
								}
							});
						}
					});
				}

				// Add supported media types to request body content
				// (for operations that can accept request bodies)
				if (operation.getRequestBody() != null &&
					operation.getRequestBody().getContent() != null &&
					!operation.getRequestBody().getContent().isEmpty()) {
					// Only add media types if content is already defined (meaning the request expects a body)
					// Add each supported media type if not already present
					supportedMediaTypes.forEach(mediaType -> {
						if (!operation.getRequestBody().getContent().containsKey(mediaType)) {
							io.swagger.v3.oas.models.media.MediaType mt = new io.swagger.v3.oas.models.media.MediaType();
							// Add a generic schema for the media type
							mt.setSchema(new Schema<>().type("object"));
							operation.getRequestBody().getContent().addMediaType(mediaType, mt);
						}
					});
				}
			});
		});
	}

	/**
	 * Enhances the OpenAPI specification with scope information
	 *
	 * @param oas the OpenAPI specification to enhance
	 */
	private void enhanceWithScopeInformation(OpenAPI oas) {
		if (oas == null || scopeCollector == null) {
			return;
		}

		List<Scope> scopes = scopeCollector.getAllScopes();
		if (scopes == null || scopes.isEmpty()) {
			return;
		}

		// Convert scopes to a structured format for the extension
		List<Map<String, Object>> scopesData = new ArrayList<>();
		for (Scope scope : scopes) {
			Map<String, Object> scopeData = new HashMap<>();
			scopeData.put("name", scope.getName());
			scopeData.put("description", scope.getDescription());
			scopeData.put("parentScope", scope.getParentScope());

			// Convert EList to regular List for JSON serialization
			if (scope.getRegistries() != null && !scope.getRegistries().isEmpty()) {
				scopeData.put("registries", new ArrayList<>(scope.getRegistries().stream().map(r -> r.getName()).toList()));
			}

			scopesData.add(scopeData);
		}

		// Add as an extension to the OpenAPI spec
		oas.addExtension("x-scopes", scopesData);

		// Build scope description
		StringBuilder scopeDescription = new StringBuilder();
		scopeDescription.append("\n\n## Scopes\n\n");
		scopeDescription.append("### What is a Scope?\n\n");
		scopeDescription.append("A **Scope** is a \"tenant\" or logical partition within the Model Atlas registry. Scopes are hierarchical.\n\n");
		scopeDescription.append("- `atlas`: A special, read-only \"system\" scope that contains common base models (like Ecore itself). ");
		scopeDescription.append("It is the default parent for any scope that does not specify one.\n");
		scopeDescription.append("- **Custom Scopes** (e.g., `my-tenant`, `global-corporate`): These are the primary partitions for schemas.\n\n");
		scopeDescription.append("#### Hierarchical Visibility and Uniqueness\n\n");
		scopeDescription.append("The hierarchy of scopes (defined by `parentScope`) governs visibility and uniqueness:\n\n");
		scopeDescription.append("1. **Uniqueness (Write-Time):** A namespace URI must be unique within its \"visibility chain\". ");
		scopeDescription.append("When creating a new package, the system checks if that namespace URI already exists in any stage of the scope ");
		scopeDescription.append("OR in the released stage of any parent scope. If a conflict is found, the creation will be rejected.\n");
		scopeDescription.append("2. **Visibility (Read-Time):** Any query for a package by its namespace URI will search this chain. ");
		scopeDescription.append("The system will first look in the specified scope and stage. If not found, it will proceed to check the ");
		scopeDescription.append("released stage of the parent scope, and so on up to the `atlas` scope.\n\n");
		scopeDescription.append("This ensures that released packages from parent scopes are resolvable from all child scopes.\n\n");
		scopeDescription.append("### Configured Scopes\n\n");
		scopeDescription.append("The following scopes are currently configured in this API instance:\n\n");

		for (Scope scope : scopes) {
			scopeDescription.append("- **").append(scope.getName()).append("**");
			if (scope.getDescription() != null && !scope.getDescription().isEmpty()) {
				scopeDescription.append(": ").append(scope.getDescription());
			}
			scopeDescription.append("\n");

			if (scope.getParentScope() != null && !scope.getParentScope().isEmpty()) {
				scopeDescription.append("  - Parent Scope: ").append(scope.getParentScope()).append("\n");
			}
			if (scope.getRegistries() != null && !scope.getRegistries().isEmpty()) {
				scopeDescription.append("  - Registries: ").append(String.join(", ", scope.getRegistries().stream().map(r -> r.getName()).toList())).append("\n");
			}
		}

		// Create or update Info object
		if (oas.getInfo() == null) {
			Info info = new Info();
			info.setTitle("Model Atlas API");
			info.setVersion("1.0.0");
			info.setDescription(scopeDescription.toString());
			oas.setInfo(info);
		} else {
			String currentDescription = oas.getInfo().getDescription();
			if (currentDescription != null) {
				oas.getInfo().setDescription(currentDescription + scopeDescription.toString());
			} else {
				oas.getInfo().setDescription(scopeDescription.toString());
			}
		}
	}

}