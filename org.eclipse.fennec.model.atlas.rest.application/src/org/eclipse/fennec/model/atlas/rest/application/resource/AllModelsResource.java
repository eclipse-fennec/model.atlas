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
package org.eclipse.fennec.model.atlas.rest.application.resource;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationOptions;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;

import org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype;
import org.eclipse.fennec.model.atlas.model.documentation.provider.ModelDocumentationConstants;
import org.eclipse.fennec.model.atlas.model.documentation.provider.ModelDocumentationProvider;
import org.eclipse.fennec.model.atlas.runtime.RequireRuntime;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * <p>
 * This is a Demo Resource for a Jaxrs Whiteboard
 * </p>
 *
 * @since 1.0
 */
@RequireRuntime
@JakartarsResource
@Component(service = AllModelsResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/models")
public class AllModelsResource {

  private final List<String> supportedMediaType;

  @Reference
  private ResourceSet resoruceSet;

  @Activate
  public AllModelsResource(@Reference SupportedMediatype types) {
    supportedMediaType = new ArrayList<>(types.getSupportedMediaTypes());
    supportedMediaType.add("application/schema+xml");
    supportedMediaType.add("application/schema+json");
  }

  @QueryParam("mediaType")
  String mediaType = null;

  @Context
  HttpHeaders headers;

  private void checkContentType() {
    if (mediaType != null) {
      if (supportedMediaType.contains(mediaType)) {
        return;
      }
    } else {
      List<MediaType> acceptableMediaTypes = headers.getAcceptableMediaTypes();
      for (MediaType acceptedMediaType : acceptableMediaTypes) {
        String accept = acceptedMediaType.getType() + "/" + acceptedMediaType.getSubtype();
        if (supportedMediaType.contains(accept)) {
          mediaType = accept;
          return;
        }
      }
    }
    throw new WebApplicationException(Status.UNSUPPORTED_MEDIA_TYPE);
  }

  @GET
  @Path("/hello")
  public String hello() {
    StringBuilder builder = new StringBuilder("Supported MediaTypes are");
    supportedMediaType.forEach(s -> {
      builder.append(s);
      builder.append("<br>");
    });
    return builder.toString();
  }

  private static String template = """
      <!DOCTYPE html>
      	<html>
      		<body>
      		<h1>Available Models</h1>
      		<table>
                                                 <tr>
                                                   <th>Model Name</th>
                                                   <th>Model URI</th>
                                                   <th>Documentation</th>
                                                   <th>Data</th>
                                                   <th>GDPR</th>
                                                   <th>OpenApi</th>
                                                   <th>GraphQL</th>
                                                 </tr>
                                                   %s
                                                 </table>
                                               </body>
                                               </html>
      """;
  private static String hrefTemplate = "<a href=\"%s\">%s</a>";
  private static String modelHrefTemplate = "<a href=\"?nsUri=%s\">%s</a>";
  private static String gdprHrefTemplate = "<a href=\"models/gdpr?nsUri=%s\">%s</a>";
  private static String dataHrefTemplate = "<a href=\"%s/%s?mediaType=application/json&limit=100&skip=0\">%s</a>";
  private static String openApiHrefTemplate = "<a href=\"%s/openapi.json\">%s</a>";
  private static String documentationHrefTemplate = "<a href=\"models/html/mermaid?nsUri=%s\">%s</a>";

  @GET
  @Path("/")
  @Produces(MediaType.TEXT_HTML)
  public Response root(@QueryParam("nsUri") String nsUri) {
    if (nsUri != null) {
      return getEPackage(nsUri);
    }
    StringBuilder builder = new StringBuilder();
    for (Object object : resoruceSet.getPackageRegistry().values()) {
      EPackage ePackage = (EPackage) object;
      builder.append("<tr><td>");
      builder.append(ePackage.getName());
      builder.append("</td><td>");
      builder.append(modelHrefTemplate.formatted(ePackage.getNsURI(), ePackage.getNsURI()));
      builder.append("</td><td>");
      builder.append(getDocumentation(ePackage));
      builder.append("</td><td>");
      builder.append(getDataLinks(ePackage));
      builder.append("</td><td>");
      builder.append(gdprHrefTemplate.formatted(ePackage.getNsURI(), "Report for " + ePackage.getNsURI()));
      builder.append("</td><td>");
      if (isRest(ePackage)) {
        builder.append(openApiHrefTemplate.formatted(ePackage.getName(), "OpenApi.json"));
      } else {
        builder.append("N/A");
      }
      builder.append("</td><td>");
      if (isGraphQL(ePackage)) {
        builder.append(hrefTemplate.formatted("/graphql", "GraphQL Endpoint"));
      } else {
        builder.append("N/A");
      }
      builder.append("</td></tr>");
    }
    return Response.ok(template.formatted(builder.toString()), MediaType.TEXT_HTML).build();
  }

  private static String gdpr = """
      <!DOCTYPE html>
      	<html>
      		<body>
      		<h1>Report for %s</h1>
      		<table>
                                                 <tr>
                                                   <th>Class Name</th>
                                                   <th>Attribute</th>
                                                   <th>Type</th>
                                                   <th>GDPR Relevant Article</th>
                                                 </tr>
                                                   %s
                                                 </table>
                                               </body>
                                               </html>
      """;

  @GET
  @Path("/gdpr")
  @Produces(MediaType.TEXT_HTML)
  public Response gdpr(@QueryParam("nsUri") String nsUri) {
    EPackage ePackage = getEPackageObject(nsUri);
    StringBuilder builder = new StringBuilder();
    for (EClassifier eClassifier : ePackage.getEClassifiers()) {
      if (eClassifier instanceof EClass eClass) {
        for (EAttribute eAttribute : eClass.getEAllAttributes()) {
          builder.append("<tr><td>");
          builder.append(eClass.getName());
          builder.append("</td><td>");
          builder.append(eAttribute.getName());
          builder.append("</td><td>");
          builder.append(eAttribute.getEType().getName());
          builder.append("</td><td>");
          builder.append(getGdprArticle(eAttribute));
          builder.append("</td></tr>");
        }

      }
    }
    return Response.ok(gdpr.formatted(ePackage.getNsURI(), builder.toString()), MediaType.TEXT_HTML).build();
  }

  /**
   * @param eAttribute
   * @return
   */
  private Object getGdprArticle(EAttribute eAttribute) {
    EAnnotation gdpr = eAttribute.getEAnnotation("http://civitas-connect.org/gdpr/1.0.0");
    if (gdpr == null) {
      return "Not Relevant";
    }
    return hrefTemplate.formatted(gdpr.getDetails().get("link"), "Article " + gdpr.getDetails().get("article"));
  }

  /**
   * @param ePackage
   * @return
   */
  private Object getDataLinks(EPackage ePackage) {
    if (!isRest(ePackage)) {
      return "No Data Avilable";
    }
    StringBuffer buffer = new StringBuffer();
    ePackage.getEClassifiers().stream().filter(EClass.class::isInstance).forEach(eClass -> {
      buffer.append(dataHrefTemplate.formatted(ePackage.getName(), eClass.getName(),
          ePackage.getNsURI() + "#//" + eClass.getName()));
      buffer.append("<br>");
    });
    return buffer.toString();
  }

  /**
   * @param ePackage
   * @return
   */
  private boolean isRest(EPackage ePackage) {
    EAnnotation eAnnotation = ePackage.getEAnnotation("properties");
    if (eAnnotation != null) {
      return "true".equals(eAnnotation.getDetails().get("Rest"));
    }
    return false;
  }

  /**
   * @param ePackage
   * @return
   */
  private boolean isGraphQL(EPackage ePackage) {
    EAnnotation eAnnotation = ePackage.getEAnnotation("properties");
    if (eAnnotation != null) {
      return "true".equals(eAnnotation.getDetails().get("GraphQL"));
    }
    return false;
  }

  /**
   * @param ePackage
   * @return
   */
  private String getDocumentation(EPackage ePackage) {
    return documentationHrefTemplate.formatted(ePackage.getNsURI(), "Mermaid");
  }

  @GET
  @Path("/")
  public Response getEPackage(@QueryParam("nsUri") String nsUri) {
    checkContentType();
    if (nsUri == null) {
      return Response.status(404).build();
    }
    EPackage ePackage = getEPackageObject(nsUri);
    if (ePackage == null) {
      return Response.noContent().build();
    }
    return Response.ok(EcoreUtil.copy(ePackage), mediaType).build();
  }

  private EPackage getEPackageObject(String nsUri) {
    EPackage ePackage = resoruceSet.getPackageRegistry().getEPackage(nsUri);
    if (ePackage == null) {
      return null;
    }
    return ePackage;
  }

  @Reference
  ModelDocumentationProvider modelDocumentationProvider;

  Map<String, Object> properties;

  @Activate
  @Modified
  private void activate(Map<String, Object> properties) throws ConfigurationException {
    this.properties = Map.copyOf(properties);
  }

  @GET
  @Path("/html/{docType}")
  @Produces({ "text/html" })
  @Operation(description = "Returns the html model documentation for the whole package.")
  public Response getHtmlModelDocumentation(@PathParam("docType") String docType, @QueryParam("nsUri") String nsUri) {
    EPackage ePackage = getEPackageObject(nsUri);
    if (ePackage == null) {
      return Response.noContent().build();
    }
    Map<String, String> packageDocFileMap = modelDocumentationProvider.generateAllPackageDocumentation(ePackage);
    OutputStream os = null;
    String filePath = null;
    switch (docType) {
    case "mermaid":
      filePath = (String) packageDocFileMap.get(ModelDocumentationConstants.PROPERTY_HTML_MERMAID_FILE);
      break;
    case "onlyHtml":
    default:
      filePath = (String) packageDocFileMap.get(ModelDocumentationConstants.PROPERTY_HTML_FILE);
      break;
    }
    os = modelDocumentationProvider.retrieveDocumentation(filePath, true, ePackage,
        "mermaid".equals(docType) ? EcoreToDocumentationOptions.HTML_WITH_MERMAID_CLASS_DIAGRAM
            : EcoreToDocumentationOptions.ONLY_HTML_CLASS_OVERVIEW);
    if (os == null) {
      return Response.noContent().build();
    }
    return Response.ok(os.toString()).build();
  }

  @GET
  @Path("/html/{eClass}/{docType}")
  @Produces({ "text/html" })
  @Operation(description = "Returns the html model documentation for the specific requested eClass.")
  public Response getHtmlModelClassDocumentation(@PathParam("eClass") String eClassName,
      @PathParam("docType") String docType, @QueryParam("nsUri") String nsUri) {
    EPackage ePackage = getEPackageObject(nsUri);
    if (ePackage == null) {
      return Response.noContent().build();
    }
    Map<String, String> classDocFileMap = modelDocumentationProvider.generateAllClassesDocumentation(ePackage);
    EClassifier eClassifier = ePackage.getEClassifier(eClassName);
    if (eClassifier == null || !(eClassifier instanceof EClass)) {
      return Response.status(Status.BAD_REQUEST).entity("Unkwon Entity").type(MediaType.TEXT_PLAIN).build();
    }
    EClass eClass = (EClass) eClassifier;
    OutputStream os = null;
    String filePath = null;
    switch (docType) {
    case "mermaid":
      filePath = (String) classDocFileMap
          .get(ModelDocumentationConstants.PROPERTY_HTML_MERMAID_FILE + "." + eClassName);
      break;
    case "onlyHtml":
    default:
      filePath = (String) classDocFileMap.get(ModelDocumentationConstants.PROPERTY_HTML_FILE + "." + eClassName);
      break;
    }
    os = modelDocumentationProvider.retrieveDocumentation(filePath, true, eClass,
        "mermaid".equals(docType) ? EcoreToDocumentationOptions.HTML_WITH_MERMAID_CLASS_DIAGRAM
            : EcoreToDocumentationOptions.ONLY_HTML_CLASS_OVERVIEW);
    if (os == null) {
      return Response.noContent().build();
    }
    return Response.ok(os.toString()).build();
  }

  @GET
  @Path("/md/{docType}")
  @Produces({ "text/markdown" })
  @Operation(description = "Returns the markdown model documentation for the whole package.")
  public Response getMarkdownModelDocumentation(@PathParam("docType") String docType,
      @QueryParam("nsUri") String nsUri) {
    EPackage ePackage = getEPackageObject(nsUri);
    if (ePackage == null) {
      return Response.noContent().build();
    }
    Map<String, String> packageDocFileMap = modelDocumentationProvider.generateAllPackageDocumentation(ePackage);
    OutputStream os = null;
    String filePath = null;
    switch (docType) {
    case "mermaid":
      filePath = (String) packageDocFileMap.get(ModelDocumentationConstants.PROPERTY_MD_MERMAID_FILE);
      break;
    case "plantuml":
      filePath = (String) packageDocFileMap.get(ModelDocumentationConstants.PROPERTY_MD_PLANTUML_FILE);
      break;
    case "onlyMd":
    default:
      filePath = (String) properties.get(ModelDocumentationConstants.PROPERTY_MD_FILE);
      break;
    }
    os = modelDocumentationProvider.retrieveDocumentation(filePath, true, ePackage,
        "mermaid".equals(docType) ? EcoreToDocumentationOptions.MARKDOWN_WITH_MERMAID_CLASS_DIAGRAM
            : "plantuml".equals(docType) ? EcoreToDocumentationOptions.MARKDOWN_WITH_PLANTUML_CLASS_DIAGRAM
                : EcoreToDocumentationOptions.ONLY_MARKDOWN_CLASS_OVERVIEW);
    if (os == null) {
      return Response.noContent().build();
    }
    return Response.ok(os.toString()).build();
  }

  @GET
  @Path("/md/{eClass}/{docType}")
  @Produces({ "text/markdown" })
  @Operation(description = "Returns the markdown model documentation for the requested class.")
  public Response getMarkdownModelClassDocumentation(@PathParam("eClass") String eClassName,
      @PathParam("docType") String docType, @QueryParam("nsUri") String nsUri) {
    EPackage ePackage = getEPackageObject(nsUri);
    if (ePackage == null) {
      return Response.noContent().build();
    }
    Map<String, String> classDocFileMap = modelDocumentationProvider.generateAllClassesDocumentation(ePackage);
    EClassifier eClassifier = ePackage.getEClassifier(eClassName);
    if (eClassifier == null || !(eClassifier instanceof EClass)) {
      return Response.status(Status.BAD_REQUEST).entity("Unkwon Entity").type(MediaType.TEXT_PLAIN).build();
    }
    EClass eClass = (EClass) eClassifier;
    OutputStream os = null;
    String filePath = null;
    switch (docType) {
    case "mermaid":
      filePath = (String) classDocFileMap.get(ModelDocumentationConstants.PROPERTY_MD_MERMAID_FILE + "." + eClassName);
      break;
    case "plantuml":
      filePath = (String) classDocFileMap.get(ModelDocumentationConstants.PROPERTY_MD_PLANTUML_FILE + "." + eClassName);
      break;
    case "onlyMd":
    default:
      filePath = (String) classDocFileMap.get(ModelDocumentationConstants.PROPERTY_MD_FILE + "." + eClassName);
      break;
    }
    os = modelDocumentationProvider.retrieveDocumentation(filePath, true, eClass,
        "mermaid".equals(docType) ? EcoreToDocumentationOptions.MARKDOWN_WITH_MERMAID_CLASS_DIAGRAM
            : "plantuml".equals(docType) ? EcoreToDocumentationOptions.MARKDOWN_WITH_PLANTUML_CLASS_DIAGRAM
                : EcoreToDocumentationOptions.ONLY_MARKDOWN_CLASS_OVERVIEW);
    if (os == null) {
      return Response.noContent().build();
    }
    return Response.ok(os.toString()).build();
  }

}
