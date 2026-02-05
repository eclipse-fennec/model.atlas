# ModelConverterResource REST API

## Overview

The **ModelConverterResource** provides a RESTful HTTP API for converting EMF models (EPackages) between different serialization formats on the fly. Clients can send a model in one format and receive it back in another format without any persistence.

## Key Features

- **Format Conversion**: Convert EPackages between supported formats (JSON, XML, XMI, UML, etc.)
- **Content Negotiation**: Use standard HTTP headers (`Content-Type` and `Accept`) for format specification
- **Query Parameter Override**: Optionally specify output format via `mediaType` query parameter
- **Stateless**: No persistence - pure format transformation

## Architecture

### Component Dependencies

```
┌───────────────────────────────────┐
│     ModelConverterResource        │
│     (JAX-RS REST Endpoint)        │
└─────────────┬─────────────────────┘
              │
              │
   ┌──────────▼───────────────┐
   │   SupportedMediatype     │
   │      (OSGi Service)      │
   └──────────────────────────┘
```

### Integration Points

#### **SupportedMediatype**
Provides the list of media types supported by the Model Atlas REST API. The resource uses this to validate requested output formats.

#### **EMF MessageBodyReaders/Writers**
The actual serialization/deserialization is handled by registered JAX-RS MessageBodyReader and MessageBodyWriter implementations for EMF objects.

## Resource Path

All endpoints are rooted at: `/convert`

## API Endpoints

### Convert EPackage

```http
POST /convert
Content-Type: <input-format>
Accept: <output-format>
```

**Purpose**: Convert an EPackage from one format to another.

**Request Headers**:
- `Content-Type`: The format of the input EPackage (e.g., `application/json`, `application/xmi`)
- `Accept`: The desired output format (e.g., `application/xml`, `application/json`)

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `mediaType` | String | No | Intended to override the output format (see [Implementation Notes](#open-question-mediatype-query-parameter) for current limitations) |

**Request Body**: The EPackage content in the format specified by `Content-Type`

**Response**:
- **200 OK**: Returns the converted EPackage in the requested format
- **415 Unsupported Media Type**: The requested output format is not supported

## Supported Media Types

The following formats are supported for both input and output:

| Media Type | Description |
|------------|-------------|
| `application/json` | EMF JSON format |
| `application/xml` | Standard XML format |
| `application/xmi` | XMI (XML Metadata Interchange) format |
| `application/uml` | UML model format |
| `application/schema+json` | JSON Schema format |
| `application/bson` | BSON (Binary JSON) format |

Additional formats may be available depending on registered MessageBodyReaders/Writers.

## Usage Examples

### Convert JSON to XML

```http
POST /rest/convert
Content-Type: application/json
Accept: application/xml

{
  "eClass": "http://www.eclipse.org/emf/2002/Ecore#//EPackage",
  "name": "MyPackage",
  "nsURI": "http://example.com/mypackage",
  "nsPrefix": "mp"
}
```

### Convert XMI to JSON

```http
POST /rest/convert
Content-Type: application/xmi
Accept: application/json

<?xml version="1.0" encoding="UTF-8"?>
<ecore:EPackage xmi:version="2.0"
    xmlns:xmi="http://www.omg.org/XMI"
    xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
    name="MyPackage"
    nsURI="http://example.com/mypackage"
    nsPrefix="mp"/>
```

### Using mediaType Query Parameter (see Implementation Notes)

```http
POST /rest/convert?mediaType=application/json
Content-Type: application/xmi

<EPackage content here>
```

**Note:** This feature may not work as expected. See [Implementation Notes](#open-question-mediatype-query-parameter).

## Error Handling

| Status Code | Condition |
|-------------|-----------|
| 200 OK | Conversion successful |
| 415 Unsupported Media Type | The requested output format is not supported |
| 500 Internal Server Error | Conversion failed due to internal error |

## Implementation Notes

### Open Question: `mediaType` Query Parameter

The code currently includes a `@QueryParam("mediaType")` field that is validated in `checkContentType()`, but **the actual response content type may not be affected by this parameter**.

**Current behavior:**
1. If `mediaType` query param is provided, it's validated against supported types
2. If invalid, a 415 error is thrown
3. However, the response is built without explicitly setting the content type:
   ```java
   return Response.status(Response.Status.OK).entity(ePackage).build();
   ```

**The issue:** The JAX-RS framework determines the output format via content negotiation based on the `Accept` header, not the `mediaType` field. So while the query param is validated, it may not actually override the output format.

**Options to consider:**
1. **Remove the query parameter** - Rely solely on the `Accept` header for content negotiation (standard HTTP approach)
2. **Fix the implementation** - Explicitly set the response content type using the `mediaType` value:
   ```java
   return Response.status(Response.Status.OK)
       .type(mediaType)
       .entity(ePackage)
       .build();
   ```

Until this is resolved, clients should use the `Accept` header for specifying the desired output format.
