# Design: EPackage Lucene Indexing

## Problem Statement

When EPackages are uploaded and stored within a scope (via the REST API / Storage layer), their metadata is indexed in the Lucene-based registry. However, only generic `ObjectMetadata` fields are indexed (objectId, scope, stage, status, objectName, etc.). EPackage-specific attributes such as `nsUri`, `nsPrefix`, classifier names, and structural statistics are not available as dedicated, searchable Lucene fields.

Currently, `nsUri` is stored in the `ObjectMetadata.properties` map, but this map is serialized into a single `TextField` (analyzed, not stored), making precise queries on individual properties impossible. For example, there is no way to:

- Search for an EPackage by its exact namespace URI via Lucene
- Find all EPackages containing a specific classifier (e.g., "Customer")
- Query EPackages by structural complexity (number of classifiers)

Additionally, clients/consumers have requested a dedicated search endpoint that provides:
- Listing all packages with at least nsUri and stage in the response
- Filtering by: classes/attributes a package contains, nsUri (including partial matching), name, stage
- Sorting, limit, and offset (pagination)

## Goals

1. Enable efficient Lucene-based search for EPackage-specific attributes
2. Support exact-match and partial-match queries on `nsUri` and `nsPrefix`
3. Support full-text search on classifier names and attribute names
4. Support numeric range queries on classifier/subpackage counts
5. Provide a REST search endpoint with filtering, sorting, and pagination
6. Maintain backward compatibility with existing indexed documents
7. Avoid changes to the EMF model (`management.ecore`) or generated code

## Non-Goals

- Indexing EPackages loaded by `EMFFileWatcher` (these are global, scope-less)
- Creating a separate Lucene index for EPackages (reuse existing infrastructure)
- Modifying the `ObjectMetadata` EMF model (use properties map for persistence)

## Architecture Overview

### Current Flow (unchanged)

```
SchemaPackagesResource.createPackage()
  --> ScopeService.uploadToStageForRegistry()
    --> EObjectStorageService.storeObject()
      --> AbstractEObjectStorageService.storeObject()
        --> storageHelper.saveEObject(...)
        --> storageHelper.saveMetadata(...)
        --> registryService.updateCache(metadata)          <-- Lucene entry point
          --> LuceneEObjectRegistryService.updateCache()
            --> luceneHelper.updateIndex(objectId, metadata)
              --> LuceneRegistryHelper.createDocument()    <-- Document creation
```

The Lucene indexing hook already exists. No new wiring is needed. The changes are:

1. **Enrich**: Populate EPackage-specific properties in `ObjectMetadata` at upload time
2. **Index**: Detect EPackage properties in `createDocument()` and create dedicated Lucene fields

### Data Flow

```
EPackage (REST upload)
  |
  v
SchemaPackagesResource        -- extracts nsUri, nsPrefix, classifiers
  |                              from EPackage, stores in metadata.properties
  v
ObjectMetadata.properties      -- persistence layer (JSON/file)
  |                              { "nsUri": "...", "nsPrefix": "...",
  |                                "classifierCount": "5", ... }
  v
LuceneRegistryHelper           -- detects nsUri property, creates
  |                              dedicated Lucene fields
  v
Lucene Index                   -- searchable via exact match, full-text,
                                  and range queries
```

## New Lucene Fields

| Field Name | Lucene Type | Store | Source | Query Type |
|---|---|---|---|---|
| `epackage_nsUri` | `StringField` | YES | `properties["nsUri"]` | Exact match via `TermQuery` |
| `epackage_nsUri_analyzed` | `TextField` | NO | `properties["nsUri"]` | Partial/tokenized match (for substring search) |
| `epackage_nsPrefix` | `StringField` | YES | `properties["nsPrefix"]` | Exact match via `TermQuery` |
| `epackage_classifierCount` | `IntPoint` + `StoredField` | YES | `properties["classifierCount"]` | Range queries |
| `epackage_subpackageCount` | `IntPoint` + `StoredField` | YES | `properties["subpackageCount"]` | Range queries |
| `epackage_classifierNames` | `TextField` | YES | `properties["classifierNames"]` | Full-text search on EClass/EEnum/EDataType names |
| `epackage_attributeNames` | `TextField` | YES | `properties["attributeNames"]` | Full-text search on EAttribute names within EClasses |

### Field Design Decisions

- **`epackage_nsUri` dual indexing**: `StringField` for exact match, plus `TextField` (analyzed) for partial/substring search. This follows the existing dual-indexing pattern for user fields. Clients can search for parts of a namespace URI (e.g., "sensors" matching "http://example.com/sensors/1.0").
- **`epackage_classifierNames` as `TextField`**: Classifier names (EClass, EEnum, EDataType) are space-separated and searchable individually. Enables queries like `epackage_classifierNames:Customer`.
- **`epackage_attributeNames` as `TextField`**: Attribute names from all EClasses in the package, space-separated. Enables queries like "find all packages that have a 'temperature' attribute".
- **`IntPoint` for counts**: Enables efficient numeric range queries (e.g., "EPackages with 5-20 classifiers") using Lucene's optimized BKD tree structure.
- **Prefix `epackage_`**: Prevents field name collisions with existing or future `ObjectMetadata` fields.

## REST Search Endpoint

### `GET /{scopeName}/schema/search`

A new endpoint on `SchemaPackagesResource` for searching EPackages across stages with filtering, sorting, and pagination.

### Request

```
GET /{scopeName}/schema/search?nsUri=sensors&classifier=Customer&stage=approved&sort=name&order=asc&limit=20&offset=0
```

### Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| `nsUri` | String | No | Filter by namespace URI. Supports partial matching (e.g., "sensors" matches "http://example.com/sensors/1.0") |
| `name` | String | No | Filter by package name. Supports wildcards (e.g., "Sensor*") |
| `classifier` | String | No | Filter by classifier name (EClass, EEnum, EDataType). Full-text search. |
| `attribute` | String | No | Filter by attribute name within EClasses. Full-text search. |
| `stage` | String | No | Filter by stage (e.g., "draft", "approved"). If omitted, searches across all stages. |
| `sort` | String | No | Sort field. Values: `name`, `nsUri`, `uploadTime`, `lastChangeTime`. Default: `name` |
| `order` | String | No | Sort order: `asc` or `desc`. Default: `asc` |
| `limit` | Integer | No | Maximum number of results. Default: `50`, Max: `500` |
| `offset` | Integer | No | Number of results to skip (for pagination). Default: `0` |

### Response

**200 OK** - Returns `ObjectMetadataContainer` with matching entries. Each `ObjectMetadata` in the response contains at minimum:
- `objectId` (encoded nsUri)
- `objectName`
- `stage`
- `scope`
- `version`
- `status`
- `properties` (including `nsUri`, `nsPrefix`, `classifierNames`, `attributeNames`, `classifierCount`)

**204 No Content** - No packages match the filter criteria.

**400 Bad Request** - Invalid parameters (e.g., unknown sort field, negative offset).

### Response Headers

| Header | Description |
|---|---|
| `X-Total-Count` | Total number of matching results (before limit/offset) |
| `X-Offset` | Current offset |
| `X-Limit` | Applied limit |

### Example Response

```json
{
  "containerId": "search-results",
  "metadata": [
    {
      "objectId": "aHR0cDovL2V4YW1wbGUuY29tL3NlbnNvcnMvMS4w",
      "objectName": "SensorModel",
      "stage": "approved",
      "scope": "tenant-a",
      "version": "1.0.0",
      "status": "APPROVED",
      "properties": {
        "nsUri": "http://example.com/sensors/1.0",
        "nsPrefix": "sensors",
        "classifierCount": "5",
        "classifierNames": "Sensor SensorType Reading Unit Location",
        "attributeNames": "name type value unit latitude longitude timestamp"
      }
    }
  ]
}
```

### Implementation Notes

- Pagination is handled at the Lucene level using `IndexSearcher.search(query, limit + offset)` and skipping the first `offset` results.
- Sorting uses Lucene `SortField`. For `name` and `nsUri`, use `SortField.Type.STRING`. For time fields, use `SortField.Type.LONG`.
- Total count for the `X-Total-Count` header comes from `TopDocs.totalHits.value`.
- The `stage` filter is optional -- if omitted, the search spans all stages within the scope. This is different from the existing `listPackagesInStage()` endpoint which requires a stage path parameter.

## Affected Files

### 1. SchemaPackagesResource (Properties Enrichment + Search Endpoint)

**File:** `org.eclipse.fennec.model.atlas.rest.application/src/org/eclipse/fennec/model/atlas/rest/application/resource/SchemaPackagesResource.java`

**Change A - Enrich properties in `createPackage()` and `updatePackageContent()`:**

After setting `metadata.getProperties().put("nsUri", validatedNsUri)`, add:

```java
metadata.getProperties().put("nsPrefix",
    ePackage.getNsPrefix() != null ? ePackage.getNsPrefix() : "");
metadata.getProperties().put("classifierCount",
    String.valueOf(ePackage.getEClassifiers().size()));
metadata.getProperties().put("subpackageCount",
    String.valueOf(ePackage.getESubpackages().size()));
metadata.getProperties().put("classifierNames",
    ePackage.getEClassifiers().stream()
        .map(EClassifier::getName)
        .filter(Objects::nonNull)
        .collect(Collectors.joining(" ")));
// Collect all attribute names from all EClasses
metadata.getProperties().put("attributeNames",
    ePackage.getEClassifiers().stream()
        .filter(EClass.class::isInstance)
        .map(EClass.class::cast)
        .flatMap(eClass -> eClass.getEAllAttributes().stream())
        .map(EAttribute::getName)
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.joining(" ")));
```

**Change B - New search endpoint `searchPackages()`:**

```java
@GET
@Path("/search")
@Produces
@Operation(summary = "Search packages across stages",
    description = "Search for schema packages with filtering by nsUri, name, "
        + "classifier names, attribute names, and stage. Supports sorting and pagination.")
public Response searchPackages(
    @PathParam("scopeName") String scopeName,
    @QueryParam("nsUri") String nsUri,
    @QueryParam("name") String name,
    @QueryParam("classifier") String classifier,
    @QueryParam("attribute") String attribute,
    @QueryParam("stage") String stage,
    @QueryParam("sort") @DefaultValue("name") String sort,
    @QueryParam("order") @DefaultValue("asc") String order,
    @QueryParam("limit") @DefaultValue("50") int limit,
    @QueryParam("offset") @DefaultValue("0") int offset) { ... }
```

### 2. LuceneRegistryHelper (Indexing + Search)

**File:** `org.eclipse.fennec.model.atlas.management.lucene/src/org/eclipse/fennec/model/atlas/management/lucene/registry/LuceneRegistryHelper.java`

**Changes:**

1. **New field constants** (after existing field declarations):
   ```java
   public static final String FIELD_EPACKAGE_NS_URI = "epackage_nsUri";
   public static final String FIELD_EPACKAGE_NS_URI_ANALYZED = "epackage_nsUri_analyzed";
   public static final String FIELD_EPACKAGE_NS_PREFIX = "epackage_nsPrefix";
   public static final String FIELD_EPACKAGE_CLASSIFIER_COUNT = "epackage_classifierCount";
   public static final String FIELD_EPACKAGE_SUBPACKAGE_COUNT = "epackage_subpackageCount";
   public static final String FIELD_EPACKAGE_CLASSIFIER_NAMES = "epackage_classifierNames";
   public static final String FIELD_EPACKAGE_ATTRIBUTE_NAMES = "epackage_attributeNames";
   ```

2. **New private method `addEPackageFields(Document, ObjectMetadata)`**: Checks if the `nsUri` property exists (indicating this metadata describes an EPackage), then adds dedicated Lucene fields. Uses dual indexing for nsUri (exact + analyzed).

3. **New private method `getPropertyValue(ObjectMetadata, String)`**: Helper to read a value from the EMF EMap (`ObjectMetadata.getProperties()`), iterating via `getKey()`/`getValue()`.

4. **Update `createDocument()`**: Call `addEPackageFields(doc, metadata)` after the existing properties block. Add `SortedDocValuesField` for sortable fields (`objectName`, `epackage_nsUri`) to enable Lucene-level sorting.

5. **Update `parseQuery()`**: Add `FIELD_EPACKAGE_NS_URI` and `FIELD_EPACKAGE_NS_PREFIX` to the `exactMatchFields` set so they use `TermQuery` instead of `QueryParser`.

6. **New search method with sorting + pagination**:
   ```java
   public SearchResult searchWithPagination(String query, String sortField,
       boolean ascending, int limit, int offset) throws IOException
   ```
   Returns a `SearchResult` record containing `List<String> objectIds`, `long totalHits`.

7. **New convenience methods**:
   - `findByEPackageNsUri(String nsUri)` - exact nsUri lookup
   - `searchByClassifierName(String name, int maxResults)` - full-text classifier search
   - `searchByAttributeName(String name, int maxResults)` - full-text attribute search
   - `findEPackagesByClassifierCountRange(int min, int max)` - range query on classifier count

8. **New imports**: `org.apache.lucene.document.IntPoint`, `org.apache.lucene.document.SortedDocValuesField`, `org.apache.lucene.search.Sort`, `org.apache.lucene.search.SortField`, `org.apache.lucene.util.BytesRef`

9. **New inner record** (or simple class):
   ```java
   public record SearchResult(List<String> objectIds, long totalHits) {}
   ```

### 3. MetadataQueryBuilder (Query API)

**File:** `org.eclipse.fennec.model.atlas.management.lucene/src/org/eclipse/fennec/model/atlas/management/lucene/registry/MetadataQueryBuilder.java`

**New builder methods:**

```java
public MetadataQueryBuilder epackageNsUri(String nsUri) { ... }          // exact match
public MetadataQueryBuilder epackageNsUriContains(String part) { ... }   // partial match (analyzed field)
public MetadataQueryBuilder epackageNsPrefix(String nsPrefix) { ... }
public MetadataQueryBuilder epackageClassifierName(String name) { ... }
public MetadataQueryBuilder epackageAttributeName(String name) { ... }
```

### 4. LuceneEObjectRegistryService (Search Delegation)

**File:** `org.eclipse.fennec.model.atlas.management.lucene/src/org/eclipse/fennec/model/atlas/management/lucene/service/LuceneEObjectRegistryService.java`

**New method:**

```java
public SearchResult searchEPackages(String queryString, String sortField,
    boolean ascending, int limit, int offset) {
    // Delegates to luceneHelper.searchWithPagination()
    // Returns objectIds + totalHits for pagination headers
}
```

## Query Examples

```java
// Find EPackage by exact namespace URI
String query = MetadataQueryBuilder.create()
    .epackageNsUri("http://example.com/sensors/1.0")
    .build();

// Find EPackages with "sensors" anywhere in the nsUri
String query = MetadataQueryBuilder.create()
    .epackageNsUriContains("sensors")
    .build();

// Find EPackages containing a "Customer" classifier in a specific scope
String query = MetadataQueryBuilder.create()
    .epackageClassifierName("Customer")
    .scope("tenant-a")
    .stage("approved")
    .build();

// Find EPackages with a "temperature" attribute
String query = MetadataQueryBuilder.create()
    .epackageAttributeName("temperature")
    .build();

// Find EPackages by classifier count range (via helper method)
List<String> ids = luceneHelper.findEPackagesByClassifierCountRange(5, 20);

// Combined REST search query translates to:
// GET /tenant-a/schema/search?nsUri=sensors&classifier=Customer&stage=approved&sort=name&limit=20&offset=0
String query = MetadataQueryBuilder.create()
    .epackageNsUriContains("sensors")
    .epackageClassifierName("Customer")
    .scope("tenant-a")
    .stage("approved")
    .build();
SearchResult result = luceneHelper.searchWithPagination(query, "objectName", true, 20, 0);
// result.objectIds()  -> first 20 matching IDs
// result.totalHits()  -> total count for X-Total-Count header
```

## Backward Compatibility

- **Existing indexed documents**: Documents without EPackage fields remain valid. Lucene queries on missing fields return no matches -- no errors or data corruption.
- **No re-indexing required**: New/updated EPackages will be indexed with the new fields. Existing entries will gain the fields on next update.
- **No model changes**: The `ObjectMetadata` EMF model is unchanged. EPackage data lives in the existing `properties` EMap.
- **No API breaking changes**: All new methods are additions, not modifications.

## Testing Strategy

### Unit Tests (Lucene Module)

**New test class:** `org.eclipse.fennec.model.atlas.management.lucene/test/.../LuceneEPackageIndexingTest.java`

| Test Case | Validates |
|---|---|
| `testEPackageFieldsIndexed` | Properties with nsUri trigger dedicated field creation |
| `testEPackageNsUriExactSearch` | Exact nsUri lookup returns correct package |
| `testEPackageNsUriPartialSearch` | Analyzed nsUri field enables partial matching |
| `testEPackageClassifierNameSearch` | Tokenized classifier name search works |
| `testEPackageAttributeNameSearch` | Tokenized attribute name search works |
| `testEPackageClassifierCountRangeQuery` | IntPoint range query filters correctly |
| `testNonEPackageMetadataUnaffected` | Metadata without nsUri property has no EPackage fields |
| `testMetadataQueryBuilderEPackageMethods` | Builder methods generate correct query strings |
| `testSearchWithPaginationLimit` | Limit parameter restricts result count |
| `testSearchWithPaginationOffset` | Offset parameter skips results correctly |
| `testSearchWithSorting` | Results are sorted by specified field and order |
| `testSearchTotalHitsCount` | Total hits count is correct regardless of limit/offset |
| `testUpdateEPackageReindexes` | Updated EPackage metadata reflects new values in index |

### Integration Tests (REST Module)

**Existing test class to extend:** `org.eclipse.fennec.model.atlas.rest.tests/src/.../SchemaPackagesResourceTest.java`

| Test Case | Validates |
|---|---|
| `testSearchEndpointReturnsResults` | GET /search returns matching packages |
| `testSearchByNsUriPartialMatch` | Partial nsUri filter works |
| `testSearchByClassifierName` | Classifier filter returns correct packages |
| `testSearchPagination` | Limit/offset/X-Total-Count headers work |
| `testSearchSorting` | Sort parameter orders results correctly |
| `testSearchNoResults` | Empty result returns 204 |
| `testSearchInvalidParams` | Invalid sort field returns 400 |

## Open Questions

None at this time. The approach reuses existing infrastructure with minimal changes.
