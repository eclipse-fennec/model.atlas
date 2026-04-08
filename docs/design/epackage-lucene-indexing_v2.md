# Design: EPackage Lucene Indexing & Search

## Problem Statement

When EPackages are uploaded and stored within a scope (via the REST API / Storage layer), their metadata is indexed in the Lucene-based registry. However, only generic `ObjectMetadata` fields are indexed (objectId, scope, stage, status, objectName, etc.). EPackage-specific attributes such as `nsUri`, `nsPrefix`, classifier names, and structural feature details are not available as dedicated, searchable Lucene fields.

Currently, `nsUri` is stored in the `ObjectMetadata.properties` map, but this map is serialized into a single `TextField` (analyzed, not stored), making precise queries on individual properties impossible. For example, there is no way to:

- Search for an EPackage by its exact namespace URI via Lucene
- Find all EPackages containing a specific classifier (e.g., "Customer")
- Find all EPackages that have a structural feature of a given name or type

Additionally, clients/consumers have requested a dedicated search endpoint that provides:
- Searching for `ObjectMetadata` entries within a scope **and up the parent chain of scopes**
- Filtering by: namespace URI (exact and partial), package name, namespace prefix, classifier names, structural feature names and types
- Offset and pagination support

## Goals

1. **Separate EPackage index**: Provide a standalone Lucene-based EPackage indexing component that can be reused independently of the existing `ObjectMetadata` registry index
2. Enable efficient Lucene-based search for EPackage-specific attributes
3. Support exact-match and partial-match queries on `nsUri` and `nsPrefix`
4. Support full-text search on classifier names and structural feature names/types
5. Provide a REST search endpoint with scope-chain traversal, filtering, and pagination
6. Return `ObjectMetadata` from existing storage -- no additional properties needed on `ObjectMetadata`
7. Maintain backward compatibility with existing indexed documents
8. All EPackage should be indexed, including the existing ones like ecore, qvt, etc

## Non-Goals

- Modifying the `ObjectMetadata` EMF model -- the existing fields and `properties` map already contain all information needed for search results
- Non goal for now, but future feature: index EAnnotations as well at the level of the EPackage/EClass/EStructuralFeature

## Architecture Overview

### Design Principles

Two key architectural decisions differentiate this design from earlier drafts:

1. **Separated EPackage index**: The Lucene EPackage index is implemented as a standalone, reusable component rather than being embedded into the existing `LuceneRegistryHelper`. This enables reuse in other contexts (e.g., EPackage discovery services, validation pipelines) without pulling in the full registry infrastructure.

2. **No ObjectMetadata enrichment**: The `ObjectMetadata` already contains all fields needed for search results (`objectId`, `objectName`, `scope`, `stage`, `version`, `status`, and `properties` including `nsUri`). The EPackage-specific Lucene index extracts searchable fields directly from the EPackage at index time; there is no need to push additional properties into `ObjectMetadata`.

### Component Architecture

```
REST API
  |
  v
SchemaPackagesResource
  |-- GET /{scopeName}/schema/search   <-- new search endpoint
  |
  v
ScopeServiceCollector                  <-- resolves scope + parent chain
  |
  v
EPackageLuceneIndex (new, standalone)  <-- dedicated EPackage Lucene index
  |                                        reusable outside of registry context
  v
Lucene Index (FSDirectory)             <-- separate from existing ObjectMetadata index
  |
  v
ObjectMetadata returned from           <-- existing storage, no modifications
existing registry/storage services
```

### Indexing Flow

When an EPackage is uploaded or updated, the EPackage-specific index is updated alongside the existing metadata index:

```
SchemaPackagesResource.createPackage() / updatePackageContent()
  --> ScopeService.uploadToStageForRegistry()
    --> EObjectStorageService.storeObject()
      --> existing metadata indexing (unchanged)
  --> EPackageLuceneIndex.index(objectMetadata, ePackage)           <-- new
```

### Search Flow

```
GET /{scopeName}/schema/search?nsUri=sensors&classifier=Customer&limit=20&offset=0
  |
  v
SchemaPackagesResource.searchPackages()
  |
  |-- 1. Resolve scope chain: [current-scope, parent-scope, ..., root]
  |      via ScopeServiceCollector
  |
  |-- 2. Query EPackageLuceneIndex with:
  |      - scope filter: scope IN [current-scope, parent-scope, ...]
  |      - user-provided filters (nsUri, name, prefix, classifier, feature, etc.)
  |      - pagination (limit + offset)
  |
  |-- 3. Retrieve ObjectMetadata for each SearchHit using
  |      hit.objectId, hit.scope, hit.stage from existing storage
  |
  |-- 4. Mark ObjectMetadata as read-only (setIsReadOnly(true))
  |      for hits where hit.scope != requested scope (parent scope results)
  |
  v
  Response with ObjectMetadata list + pagination headers
```

## EPackage Lucene Index (Standalone Component)

### Module

**New package:** `org.eclipse.fennec.model.atlas.management.lucene.epackage`

This package lives within the existing `management.lucene` bundle but is self-contained with no dependencies on `LuceneRegistryHelper` or `LuceneEObjectRegistryService`. It manages its own Lucene index directory.

### Indexed Fields

| Field Name | Lucene Type | Store | Source | Query Type |
|---|---|---|---|---|
| `objectId` | `StringField` | YES | Provided at index time | Exact match, result correlation |
| `scope` | `StringField` | YES | Provided at index time | Exact match (scope chain filter) |
| `stage` | `StringField` | YES | Provided at index time | Exact match |
| `epackage_nsUri` | `StringField` | YES | `EPackage.getNsURI()` | Exact match via `TermQuery` |
| `epackage_nsUri_analyzed` | `TextField` | NO | `EPackage.getNsURI()` | Partial/tokenized match |
| `epackage_name` | `StringField` | YES | `EPackage.getName()` | Exact match |
| `epackage_name_analyzed` | `TextField` | NO | `EPackage.getName()` | Partial/tokenized match |
| `epackage_nsPrefix` | `StringField` | YES | `EPackage.getNsPrefix()` | Exact match |
| `epackage_nsPrefix_analyzed` | `TextField` | NO | `EPackage.getNsPrefix()` | Partial/tokenized match |
| `epackage_classifierNames` | `TextField` | YES | All `EClassifier.getName()` (space-separated) | Full-text search |
| `epackage_featureNames` | `TextField` | YES | All `EStructuralFeature.getName()` across all EClasses (space-separated, deduplicated) | Full-text search |
| `epackage_featureTypes` | `TextField` | YES | All `EStructuralFeature.getEType().getName()` across all EClasses (space-separated, deduplicated) | Full-text search |
| `epackage_featureNameTypePairs` | `TextField` | YES | All `featureName:typeName` pairs across all EClasses (e.g., `"person:Person friend:Person age:EInt"`), space-separated | Full-text search on `name:type` pairs |

### Field Design Decisions

- **`epackage_nsUri` / `epackage_name` / `epackage_nsPrefix` dual indexing**: `StringField` for exact match, plus `TextField` (analyzed) for partial/substring search. Clients can search for parts of a namespace URI (e.g., "sensors" matching "http://example.com/sensors/1.0").
- **`epackage_classifierNames` as `TextField`**: Classifier names (EClass, EEnum, EDataType) are space-separated and individually searchable. Enables queries like `epackage_classifierNames:Customer`.
- **`epackage_featureNames` as `TextField`**: Structural feature names (EAttribute + EReference) from all EClasses, space-separated and deduplicated. Enables queries like "find all packages that have a 'temperature' feature".
- **`epackage_featureTypes` as `TextField`**: Type names of structural features (e.g., `EString`, `EInt`, `EBoolean`, or custom EClass/EDataType names). Enables queries like "find all packages with features of type 'EDate'".
- **`epackage_featureNameTypePairs` as `TextField`**: Combined `name:type` pairs for all structural features, enabling precise per-feature queries. See [Feature Name/Type Search Strategy](#feature-nametype-search-strategy) for the rationale behind this field.
- **`scope` and `stage` indexed here**: Allows scope-chain filtering entirely within the EPackage index, without a join to the metadata index.
- **No `epackage_` prefix on `objectId`/`scope`/`stage`**: These are generic correlation fields, not EPackage-specific.

### Feature Name/Type Search Strategy

Since `epackage_featureNames` and `epackage_featureTypes` are flat, deduplicated fields at the EPackage (document) level, they are inherently independent: a query combining `featureName=X` and `featureType=Y` matches any package that has *some* feature named X and *some* feature of type Y, but X and Y do not need to belong to the same structural feature.

This loose correlation has different implications for EAttributes and EReferences:

- **EAttributes**: Searching by type alone (e.g., `featureType=EInt`) is rarely useful — it would match nearly every package in the system. The natural query is by name (e.g., `featureName=temperature`), optionally combined with a type for narrowing.
- **EReferences**: Searching by type is the more interesting case because the type is a domain-specific EClass (e.g., `Person`). However, a combined name+type query would miss valid results. For example, if a package has a reference `friend: Person`, a query for `featureName=person&featureType=Person` would not find it — even though the package clearly references the `Person` type.

An alternative approach would be to use OR semantics for EReference queries and AND semantics for EAttribute queries, but this pushes complexity onto the client (which would need to know whether it is searching for attributes or references) and onto the query layer.

**Chosen approach**: In addition to the independent `featureName` and `featureType` fields, a combined `epackage_featureNameTypePairs` field indexes `name:type` pairs (e.g., `"person:Person friend:Person age:EInt temperature:EDouble"`). This gives clients three natural search patterns without requiring knowledge of the feature kind:

1. **By name only** (`featureName=temperature`): "Which packages have a temperature feature?" — best for EAttributes where the name is the primary discriminator.
2. **By type only** (`featureType=Person`): "Which packages reference Person?" — best for EReferences where the type carries domain meaning.
3. **By exact pair** (`featureNameTypePair=friend:Person`): "Which packages have a feature named 'friend' of type 'Person'?" — for precise structural queries when both name and type are known.

### API

```java
/**
 * Standalone Lucene index for EPackage-specific fields.
 * Manages its own index directory and can be reused independently
 * of the ObjectMetadata registry index.
 */
public class EPackageLuceneIndex {

    /**
     * Index or re-index an EPackage.
     * Extracts objectId, scope, and stage from the ObjectMetadata;
     * extracts EPackage-specific fields (nsUri, classifiers, features, etc.) from the EPackage.
     */
    public void index(ObjectMetadata metadata, EPackage ePackage);

    /**
     * Remove an entry from the index.
     */
    public void remove(String objectId);

    /**
     * Search with filtering and pagination.
     * Returns matching objectIds and total hit count.
     */
    public SearchResult search(EPackageSearchQuery query);

    /**
     * Result record for paginated search.
     * Each SearchHit carries the objectId, scope, and stage from the Lucene stored fields,
     * providing enough context to retrieve the corresponding ObjectMetadata from the correct
     * scope/stage in the storage layer.
     */
    public record SearchResult(List<SearchHit> hits, long totalHits) {}

    /**
     * A single search hit with correlation fields.
     */
    public record SearchHit(String objectId, String scope, String stage) {}
}
```

### EPackageSearchQuery

```java
/**
 * Builder for creating EPackage search queries.
 * Follows the same builder pattern as MetadataQueryBuilder.
 */
public class EPackageSearchQuery {
    private Set<String> scopes;            // scope chain (OR'd)
    private String stage;                  // optional stage filter
    private String nsUri;                  // partial match on namespace URI
    private String nsUriExact;             // exact match on namespace URI
    private String name;                   // partial match on package name
    private String nsPrefix;               // partial match on prefix
    private String classifier;             // full-text on classifier names
    private String featureName;            // full-text on structural feature names
    private String featureType;            // full-text on structural feature types
    private String featureNameTypePair;    // full-text on "name:type" pairs (e.g., "friend:Person")
    private int limit = 50;
    private int offset = 0;

    private EPackageSearchQuery() {}

    public static EPackageSearchQuery create() {
        return new EPackageSearchQuery();
    }

    public EPackageSearchQuery scopes(Set<String> scopes) { ... }
    public EPackageSearchQuery stage(String stage) { ... }
    public EPackageSearchQuery nsUri(String nsUri) { ... }
    public EPackageSearchQuery nsUriExact(String nsUriExact) { ... }
    public EPackageSearchQuery name(String name) { ... }
    public EPackageSearchQuery nsPrefix(String nsPrefix) { ... }
    public EPackageSearchQuery classifier(String classifier) { ... }
    public EPackageSearchQuery featureName(String featureName) { ... }
    public EPackageSearchQuery featureType(String featureType) { ... }
    public EPackageSearchQuery featureNameTypePair(String featureNameTypePair) { ... }
    public EPackageSearchQuery limit(int limit) { ... }
    public EPackageSearchQuery offset(int offset) { ... }
    public EPackageSearchQuery build() { return this; }

    // getters...
}
```

## REST Search Endpoint

### `GET /{scopeName}/schema/search`

A new endpoint on `SchemaPackagesResource` for searching EPackages within a scope and its parent chain, with filtering and pagination.

### Scope Chain Behavior

The search traverses the full scope hierarchy. For a scope `tenant-a` with parent `division-x` and grandparent `atlas`:

```
GET /tenant-a/schema/search?classifier=Customer
```

searches across scopes `[tenant-a, division-x, atlas]`, returning `ObjectMetadata` from any scope in the chain. This mirrors how `listInFinalStageForRegistry` already includes parent scope objects.

### Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| `nsUri` | String | No | Filter by namespace URI (partial match, e.g., "sensors" matches "http://example.com/sensors/1.0") |
| `nsUriExact` | String | No | Filter by exact namespace URI |
| `name` | String | No | Filter by package name (partial match) |
| `prefix` | String | No | Filter by namespace prefix (partial match) |
| `classifier` | String | No | Filter by classifier name (EClass, EEnum, EDataType). Full-text search. |
| `featureName` | String | No | Filter by structural feature name (EAttribute, EReference). Full-text search. |
| `featureType` | String | No | Filter by structural feature type name (e.g., "EString", "Customer"). Full-text search. |
| `featureNameTypePair` | String | No | Filter by combined feature name:type pair (e.g., "friend:Person"). Full-text search. See [Feature Name/Type Search Strategy](#feature-nametype-search-strategy). |
| `stage` | String | No | Filter by stage. If omitted, searches across all stages. |
| `limit` | Integer | No | Maximum number of results. Default: `50`, Max: `500` |
| `offset` | Integer | No | Number of results to skip. Default: `0` |

### Response

**200 OK** - Returns `ObjectMetadataContainer` with matching entries. Each `ObjectMetadata` is returned as-is from existing storage, containing at minimum:
- `objectId`
- `objectName`
- `stage`
- `scope`
- `version`
- `status`
- `properties` (including `nsUri` as already stored today)

**204 No Content** - No packages match the filter criteria.

**400 Bad Request** - Invalid parameters (e.g., negative offset).

### Response Headers

| Header | Description |
|---|---|
| `X-Total-Count` | Total number of matching results (before limit/offset) |
| `X-Offset` | Current offset |
| `X-Limit` | Applied limit |

### Example Request

```
GET /tenant-a/schema/search?nsUri=sensors&classifier=Customer&stage=approved&limit=20&offset=0
```

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
        "nsUri": "http://example.com/sensors/1.0"
      }
    }
  ]
}
```

### Implementation

```java
@GET
@Path("/search")
@Produces
@Operation(summary = "Search packages across scope chain",
    description = "Search for schema packages within the given scope and its "
        + "parent scopes. Supports filtering by nsUri, name, prefix, "
        + "classifier names, structural feature names and types. "
        + "Returns ObjectMetadata with pagination.")
public Response searchPackages(
    @PathParam("scopeName") String scopeName,
    @QueryParam("nsUri") String nsUri,
    @QueryParam("nsUriExact") String nsUriExact,
    @QueryParam("name") String name,
    @QueryParam("prefix") String prefix,
    @QueryParam("classifier") String classifier,
    @QueryParam("featureName") String featureName,
    @QueryParam("featureType") String featureType,
    @QueryParam("featureNameTypePair") String featureNameTypePair,
    @QueryParam("stage") String stage,
    @QueryParam("limit") @DefaultValue("50") int limit,
    @QueryParam("offset") @DefaultValue("0") int offset) {

    // 1. Resolve scope chain
    Set<String> scopeChain = resolveScopeChain(scopeName);

    // 2. Build search query
    EPackageSearchQuery query = EPackageSearchQuery.create()
        .scopes(scopeChain)
        .stage(stage)
        .nsUri(nsUri)
        .nsUriExact(nsUriExact)
        .name(name)
        .nsPrefix(prefix)
        .classifier(classifier)
        .featureName(featureName)
        .featureType(featureType)
        .featureNameTypePair(featureNameTypePair)
        .limit(limit)
        .offset(offset)
        .build();

    // 3. Search EPackage index
    SearchResult result = ePackageLuceneIndex.search(query);

    if (result.hits().isEmpty()) {
        return Response.noContent().build();
    }

    // 4. Retrieve ObjectMetadata for each hit using scope/stage from the index
    List<ObjectMetadata> metadataList = resolveMetadata(result.hits());

    // 5. Mark results from parent scopes as read-only
    for (ObjectMetadata metadata : metadataList) {
        if (!scopeName.equals(metadata.getScope())) {
            metadata.setIsReadOnly(true);
        }
    }

    // 6. Build response
    ObjectMetadataContainer container = buildContainer(metadataList);
    return Response.ok(container)
        .header("X-Total-Count", result.totalHits())
        .header("X-Offset", offset)
        .header("X-Limit", limit)
        .build();
}
```

#### Scope Chain Resolution

```java
private Set<String> resolveScopeChain(String scopeName) {
    Set<String> chain = new LinkedHashSet<>();
    String current = scopeName;
    while (current != null) {
        chain.add(current);
        Scope scope = scopeServiceCollector.getScopeByName(current);
        current = (scope != null) ? scope.getParentScope() : null;
    }
    return chain;
}
```

## Affected Files

### New Files

#### 1. EPackageLuceneIndex

**Package:** `org.eclipse.fennec.model.atlas.management.lucene.epackage`

**File:** `org.eclipse.fennec.model.atlas.management.lucene/src/org/eclipse/fennec/model/atlas/management/lucene/epackage/EPackageLuceneIndex.java`

Standalone Lucene index component:
- Manages its own `FSDirectory` and `IndexWriter` (separate index directory from the metadata index)
- `index(objectMetadata, ePackage)` -- extracts fields from ObjectMetadata and EPackage and creates Lucene document
- `remove(objectId)` -- removes entry by objectId
- `search(EPackageSearchQuery)` -- builds `BooleanQuery` from search parameters, executes with pagination, returns `SearchResult`
- Thread-safe with `ReentrantReadWriteLock` (same pattern as `LuceneRegistryHelper`)
- NRT search via `SearcherManager`

#### 2. EPackageSearchQuery

**File:** `org.eclipse.fennec.model.atlas.management.lucene/src/org/eclipse/fennec/model/atlas/management/lucene/epackage/EPackageSearchQuery.java`

Builder for EPackage search queries, following the same pattern as `MetadataQueryBuilder`: private constructor, static `create()` factory, fluent setters, and `build()`. Fields as described in the EPackageSearchQuery section above.

### Modified Files

#### 3. SchemaPackagesResource (Search Endpoint + Index Hook)

**File:** `org.eclipse.fennec.model.atlas.rest.application/src/org/eclipse/fennec/model/atlas/rest/application/resource/SchemaPackagesResource.java`

**Changes:**

- **New dependency**: Inject `EPackageLuceneIndex` via `@Reference`
- **New dependency**: Inject `ScopeServiceCollector` via `@Reference` (if not already present)
- **Hook into `createPackage()` and `updatePackageContent()`**: After successful storage, call `ePackageLuceneIndex.index(objectMetadata, ePackage)`
- **Hook into delete flow**: Call `ePackageLuceneIndex.remove(objectId)` on package deletion
- **New method `searchPackages()`**: As described in the REST Search Endpoint section

#### 4. LuceneEObjectRegistryService (No changes)

The existing registry service and its Lucene index remain unchanged. The EPackage index is fully independent.

## Query Examples

```java
// Search across scope chain for packages with "sensors" in the namespace URI
EPackageSearchQuery query = EPackageSearchQuery.create()
    .scopes(Set.of("tenant-a", "division-x", "atlas"))
    .nsUri("sensors")
    .limit(20)
    .offset(0)
    .build();
SearchResult result = ePackageLuceneIndex.search(query);

// Find packages containing a "Customer" classifier in approved stage
EPackageSearchQuery query = EPackageSearchQuery.create()
    .scopes(Set.of("tenant-a", "division-x", "atlas"))
    .classifier("Customer")
    .stage("approved")
    .limit(50)
    .offset(0)
    .build();

// Find packages with a structural feature named "temperature"
EPackageSearchQuery query = EPackageSearchQuery.create()
    .scopes(Set.of("tenant-a"))
    .featureName("temperature")
    .limit(50)
    .offset(0)
    .build();

// Find packages with features of type "EDate"
EPackageSearchQuery query = EPackageSearchQuery.create()
    .scopes(Set.of("tenant-a", "division-x", "atlas"))
    .featureType("EDate")
    .limit(50)
    .offset(0)
    .build();

// Find packages with a feature named "friend" of type "Person" (precise pair search)
EPackageSearchQuery query = EPackageSearchQuery.create()
    .scopes(Set.of("tenant-a", "division-x", "atlas"))
    .featureNameTypePair("friend:Person")
    .limit(50)
    .offset(0)
    .build();

// Combined: packages with prefix "sensors" containing a "Reading" classifier
// that has an EString feature, across all stages
EPackageSearchQuery query = EPackageSearchQuery.create()
    .scopes(Set.of("tenant-a", "division-x", "atlas"))
    .nsPrefix("sensors")
    .classifier("Reading")
    .featureType("EString")
    .limit(10)
    .offset(0)
    .build();
```

## Testing Strategy

### Unit Tests (EPackage Lucene Index)

**New test class:** `org.eclipse.fennec.model.atlas.management.lucene/test/.../epackage/EPackageLuceneIndexTest.java`

| Test Case | Validates |
|---|---|
| `testIndexAndSearchByNsUri` | Indexing an EPackage and searching by partial nsUri returns it |
| `testSearchByExactNsUri` | Exact nsUri lookup returns correct package |
| `testSearchByPackageName` | Partial package name match works |
| `testSearchByNsPrefix` | Partial prefix match works |
| `testSearchByClassifierName` | Tokenized classifier name search works |
| `testSearchByFeatureName` | Structural feature name search works |
| `testSearchByFeatureType` | Structural feature type search works |
| `testSearchByFeatureNameTypePair` | Combined name:type pair search matches the correct feature precisely |
| `testSearchByFeatureNameTypePairNoFalsePositive` | Pair search does not match when name and type exist on different features |
| `testSearchAcrossMultipleScopes` | Scope chain (OR'd scope filter) returns results from all scopes |
| `testSearchWithStageFilter` | Stage filter restricts results to matching stage |
| `testSearchPaginationLimit` | Limit parameter restricts result count |
| `testSearchPaginationOffset` | Offset parameter skips results correctly |
| `testSearchTotalHitsCount` | Total hits count is correct regardless of limit/offset |
| `testRemoveFromIndex` | Removed entry no longer appears in search results |
| `testUpdateReindexes` | Re-indexing an objectId replaces the old entry |
| `testSearchNoResults` | Query with no matches returns empty result |
| `testCombinedFilters` | Multiple filters are AND'd together |

### Integration Tests (REST Module)

**Existing test class to extend:** `org.eclipse.fennec.model.atlas.rest.tests/src/.../SchemaPackagesResourceTest.java`

| Test Case | Validates |
|---|---|
| `testSearchEndpointReturnsResults` | GET /search returns matching packages as ObjectMetadata |
| `testSearchByNsUriPartialMatch` | Partial nsUri filter works end-to-end |
| `testSearchByClassifierName` | Classifier filter returns correct packages |
| `testSearchByFeatureName` | Feature name filter works end-to-end |
| `testSearchByFeatureType` | Feature type filter works end-to-end |
| `testSearchByFeatureNameTypePair` | Feature name:type pair filter works end-to-end |
| `testSearchAcrossScopeChain` | Results include packages from parent scopes |
| `testSearchPagination` | Limit/offset/X-Total-Count headers work correctly |
| `testSearchNoResults` | Empty result returns 204 |
| `testSearchParentScopeResultsMarkedReadOnly` | Results from parent scopes have `isReadOnly=true`, results from the requested scope do not |
| `testSearchInvalidParams` | Invalid parameters return 400 |

## Additional Considerations

1. We should have a reindex mechanism. Especially for draft models it  might be possible, that a newer version is uploaded to the ModelAtlas.  We would need to reindex this EPackage then.

2. As long as the index is always up-to-date, we could keep it at disk  across restart of the model atlas, same like with the ObjectMetadata
