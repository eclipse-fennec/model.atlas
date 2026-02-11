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

## Error Handling

| Status Code | Condition |
|-------------|-----------|
| 200 OK | Conversion successful |
| 415 Unsupported Media Type | The requested output format is not supported |
| 500 Internal Server Error | Conversion failed due to internal error |
