# Model Atlas Storage API Specification

This API defines resources for storing and managing  "Storage Objects" (e.g., configurations, scripts, data models) that must conform to schemas defined in the "Model Atlas Schema API."

This API complements the Schema API and uses the same `scope` and `nsUri` concepts for validation.

## 1. Core Concepts

### Storage Registry

A "Storage Registry" is a logical "bucket" or repository within a `Scope` for storing objects. Each scope can have multiple registries.

- `default`: A special registry that exists in every scope.
- **Custom Registries:** (e.g., `configurations`, `transformation-scripts`). These can be configured on the backend, likely as OSGi services similar to Scopes.

### Storage Object

The actual data or file being stored (e.g., a JSON configuration, an XML file).

- **Identifier (`objectId`):** An object is stored and retrieved using a unique `objectId` (e.g., `production-db-settings`, `my-script.js`). This acts as a "key" in a key-value store.
- **Content:** The raw body of the object (e.g., `application/json`).

### Schema Conformance (Crucial)

This is the central rule of the Storage API.

1. **Mandatory Validation:** When creating or updating a `StorageObject` (via `PUT`), the client **must** declare the `nsUri` of the schema it conforms to.
2. **Validation Check:** The server will use the "Model Atlas Schema API" (and its "Hierarchical Visibility" rules) to find this schema.
3. **Conformance:** The server will then validate the new object's content against the found schema.
4. **Rejection:** If the schema is not found, or if the object's content does not validate against it, the `PUT` request will be rejected with a `400 Bad Request`.

This guarantees that no object can be stored in the registry unless it is a valid instance of a known schema.

## Resource Models (JSON)

### StorageRegistry

```
{
  "name": "default",
  "scope": "my-tenant",
  "description": "Default object storage for my-tenant.",
  "links": {
    "self": "/my-tenant/storage/default",
    "objects": "/my-tenant/storage/default/objects"
  }
}
```

​    

### StorageObjectMetadata

This is the metadata for a stored object.

```
{
  "id": "production-db-settings",
  "registryName": "configurations",
  "scope": "my-tenant",
  "conformingSchemaNsUri": "http://example.com/schemas/app-config/v1.2",
  "size": 2048,
  "createdAt": "2023-11-10T11:00:00Z",
  "updatedAt": "2023-11-10T11:00:00Z",
  "links": {
    "self": "/my-tenant/storage/configurations/objects/production-db-settings/metadata",
    "content": "/my-tenant/storage/configurations/objects/production-db-settings",
    "schema": "/my-tenant/schema?nsUri=http%3A%2F%2Fexample.com%2Fschemas%2Fapp-config%2Fv1.2"
  }
}
```

​      

- `links.schema`: A HATEOAS link pointing back to the "Model Atlas Schema API" endpoint for the schema this object conforms to.

## API Endpoints

### 1. Storage Registries

Resource for discovering and managing storage registries.

#### `GET /{scopeName}/storage`

- **Action:** List all available `StorageRegistry` resources within a scope.
- **Response (200 OK):** `application/json` (A list of `StorageRegistry` objects).

### 2. Storage Objects

Resources for managing objects within a registry.

#### `GET /{scopeName}/storage/{registryName}`

- **Action:** List the metadata for all `StorageObject`s within a specific registry.
- **Response (200 OK):** `application/json` (A list of `StorageObjectMetadata` objects).

#### `PUT /{scopeName}/storage/{registryName}/{objectId}`

- **Action:** Create or update a `StorageObject`. This is the primary method for adding data to the registry.

- Query Parameters:

  - `schemaNsUri` (string, required): The `nsUri` of the schema (from the Schema API) that this object must conform to.

- **Request Body:** The raw object content (e.g., `application/json`, `application/xml`).

- **Content-Type Header:** Must specify the format of the object being uploaded.

- Logic:

  1. Server uses the `schemaNsUri` to find the corresponding schema (via Schema API logic). If not found, returns `400 Bad Request`.
  2. Server validates the request body against this schema. If invalid, returns `400 Bad Request` with validation errors.
  3. If valid, the object is stored.

- Response (201 Created):

   If the object was created.

  - Body: The `StorageObjectMetadata` (JSON).

- Response (200 OK):

   If the object was updated.

  - Body: The `StorageObjectMetadata` (JSON).

- **Error Response (400 Bad Request):** If `schemaNsUri` is missing, the schema isn't found, or the object fails validation.

#### `GET /{scopeName}/storage/{registryName}/{objectId}`

- **Action:** Get the raw *content* of a `StorageObject`.
- **Accept Header:** Can be used to request content transformation (e.g., if stored as XML but `Accept: application/json` is requested).
- Response (200 OK):
  - Body: The raw object content.
  - `Content-Type` Header: Reflects the format being returned.

#### `GET /{scopeName}/storage/{registryName}/{objectId}/metadata`

- **Action:** Get *only* the metadata for a `StorageObject`.
- **Response (200 OK):** `application/json` (A single `StorageObjectMetadata` object).

#### `DELETE /{scopeName}/storage/{registryName}/{objectId}`

- **Action:** Delete a `StorageObject`.
- **Response (204 No Content):**