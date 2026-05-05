# Idempotency Plan for Model Atlas REST API

## Implementation Status

> **All phases have been implemented.** The sections below document both the original plan and what was delivered.

| Phase | Description | Status |
|-------|-------------|--------|
| Phase 1 | Content Hashing & ETag Generation | **Implemented** |
| Phase 2 | Optimistic Locking (If-Match) + Content-Aware Skip | **Implemented** (Phase 2a — If-Match optional) |
| Phase 3 | Idempotent Transitions | **Implemented** |
| Phase 4 | DELETE Normalization | **Implemented** |
| Phase 5 | Conditional GET (If-None-Match) | **Implemented** |

### What Was Implemented

**Phase 1 — Content Hashing & ETags:**
- `computeContentHash(EObject)` added to `AbstractEObjectStorageService` — serializes a copy of the EObject to XMI, computes SHA-256, returns hex string.
- `storeObject()` now calls `computeContentHash()` and sets `contentHash` on `ObjectMetadata` before persisting.
- `ObjectRegistryResource` and `SchemaPackagesResource` return `ETag` headers (from `contentHash`) on create, update, get content, and single-object list responses.
- Lazy population: objects written before this change will get their `contentHash` set on the next write. Until then, ETag is omitted and conditional requests are not enforced.

**Phase 2 — Optimistic Locking & Content-Aware Skip:**
- `If-Match` header support added to update (`PUT`) and delete (`DELETE`) endpoints in both resource classes.
- If `If-Match` is provided and does not match the current `contentHash`, the server returns `412 Precondition Failed`.
- `If-Match` is **optional** (Phase 2a) — clients that omit it get last-write-wins behavior as before.
- Content-aware skip: on update, if the new content hash equals the existing hash, the write is skipped entirely — no timestamp change, no storage call. Returns `200 OK` with existing metadata and ETag.

**Phase 3 — Idempotent Transitions:**
- Implemented in the REST resource layer (`ObjectRegistryResource.transitionObject()` and `SchemaPackagesResource.transitionPackage()`).
- When an object is not found in the source stage, the code checks the target stage. If the object is already there, it returns `200 OK` with the target stage metadata (safe retry).
- If the object is in neither source nor target stage, returns `204 No Content`.

**Phase 4 — DELETE Normalization:**
- DELETE endpoints in both resource classes now always return `204 No Content`, whether the object was actually deleted or was already absent.
- `403 Forbidden` is still returned for read-only (parent scope) objects.

**Phase 5 — Conditional GET (If-None-Match):**
- GET content endpoints and single-object list endpoints support `If-None-Match`.
- If the provided ETag matches the current `contentHash`, returns `304 Not Modified` with no body.

### What Was NOT Implemented (Deferred)

- **Phase 2b** (mandatory `If-Match`): Not implemented. `If-Match` remains optional for backward compatibility.
- **Idempotency-Key header** for transitions (Section 3.8): Deferred. The target-stage check covers the common retry scenario without requiring a cache layer.
- **Batch migration** of existing objects' content hashes: Deferred in favor of lazy computation on next write.
- **Concurrency tests** with parallel threads: Not added to the test suite. The `If-Match` mechanism is tested with sequential stale-ETag scenarios.

### Files Modified

| File | Changes |
|------|---------|
| `AbstractEObjectStorageService.java` | Added `computeContentHash()` static method; integrated hash computation into `storeObject()` |
| `ObjectRegistryResource.java` | Added ETag responses, `If-Match` validation, `If-None-Match` conditional GET, content-aware skip, idempotent DELETE (204), idempotent transition retry; added `addETagHeader()`, `checkIfMatch()`, `evaluateConditionalGet()` helpers |
| `SchemaPackagesResource.java` | Same changes as ObjectRegistryResource; also changed create response from 200 to 201 for new packages |
| `MockTestHelper.java` | Updated mocks to compute and set `contentHash` on metadata; added Base64-encoded nsUri to recognized objectId lists |
| `ObjectRegistryResourceTest.java` | Updated DELETE test to expect 204; added 12 new tests (ETag, If-None-Match, If-Match, content-aware skip, DELETE idempotency, transition idempotency) |
| `SchemaPackagesResourceTest.java` | Updated DELETE test to expect 204; updated create tests to expect 201; added 10 new tests (ETag, If-None-Match, If-Match, DELETE idempotency) |
| `AbstractEObjectStorageServiceTest.java` | Added 5 unit tests for content hash computation (deterministic, different content, null, store integration, valid SHA-256 hex) |

### Documentation Updated

- `docs/user-guide.md` — Added "ETags and Conditional Requests" section with curl examples; updated HTTP status codes table with 304 and 412.
- `README-ObjectStorage.md` — Updated all endpoint sections with ETag/If-Match/If-None-Match headers, updated response codes (DELETE→204, added 304/412), added examples.
- `README-SchemaPackages.md` — Same updates; also documented create returning 201.

---

## 1. Current Idempotency Status

### Overview

The Model Atlas REST API had **partial idempotency** through `override`/`overwrite` flags on create endpoints and safe DELETE behavior, but lacked proper HTTP-level idempotency mechanisms. The `version` and `contentHash` fields existed in `ObjectMetadata` but were **not used for conflict detection**.

### Endpoint-by-Endpoint Status (Before Implementation)

#### Object Endpoints (`ObjectRegistryResource.java`)

| Endpoint | Method | Path | Idempotent? | Details |
|----------|--------|------|-------------|---------|
| Create Object | POST/PUT | `/{scopeName}/registries/{registryName}/stages/{stageName}/{objectId}` | **No** | Returns 409 on duplicate unless `override=true`; with override, updates timestamps on every call |
| Update Object Content | PUT/POST | `/{scopeName}/registries/{registryName}/stages/{stageName}/content` | **Partial** | Returns 200 on repeats but modifies `lastChangeTime`/`lastChangeUser` each time |
| Delete Object | DELETE | `/{scopeName}/registries/{registryName}/stages/{stageName}` | **Yes** | Returns 200 on first call, 204 on subsequent calls (object not found) |
| Transition Object | POST | `/{scopeName}/registries/{registryName}/stages/{stageName}/actions/transition` | **No** | Object leaves source stage after first call; retries return 204 |

#### Schema Package Endpoints (`SchemaPackagesResource.java`)

| Endpoint | Method | Path | Idempotent? | Details |
|----------|--------|------|-------------|---------|
| Create Package | POST/PUT | `/{scopeName}/schema/stages/{stageName}` | **No** | Returns 409 on duplicate unless `overwrite=true`; with overwrite, updates timestamps |
| Update Package Content | PUT/POST | `/{scopeName}/schema/stages/{stageName}/content` | **Partial** | Returns 200 on repeats but modifies timestamps |
| Delete Package | DELETE | `/{scopeName}/schema/stages/{stageName}` | **Yes** | Returns 200 on first call, 204 on subsequent |
| Transition Package | POST | `/{scopeName}/schema/stages/{stageName}/actions/transition` | **No** | Package leaves source stage; retries return 204 |

### Gaps (Before Implementation)

1. **`version` field** — Client-managed versioning metadata. Accepted as a query parameter and stored as-is. The field's Ecore documentation says "Object version for optimistic locking" but the version is owned by the client, not the server, so it is not suitable as an optimistic locking mechanism. Conflict detection should use `contentHash`/ETags instead.

2. **`contentHash` field** — Defined as "SHA-256 hash of XMI content" in `management.ecore`. Marked as updatable in `AbstractEObjectStorageService` but **never computed or verified** anywhere in the codebase.

3. **No HTTP conditional headers** — `HttpHeaders` is injected in resource classes but only used for Accept/Content-Type negotiation. No `ETag`, `If-Match`, `If-None-Match`, or `If-Unmodified-Since` headers are generated or consumed.

4. **No 304 Not Modified** — GET endpoints always return full content regardless of client cache state.

5. **Last-write-wins** — Concurrent updates silently overwrite each other. No lost-update detection.

6. **Timestamp side effects** — `lastChangeTime` is updated on every write operation in `RegistryServiceImpl.updateInStage()`, making repeated calls observably non-idempotent even when the content is identical.

---

## 2. Required Changes Per Endpoint

### 2.1 Create Object / Create Package (POST/PUT)

**Previous behavior:** Returns 201 on first call, 409 on duplicate (unless override/overwrite=true).

**Changes made:**
- Content hash is computed and stored on creation.
- `ETag` header returned in 201/200 responses.
- With `override=true` + identical content: returns 200 with ETag, no timestamp change (content-aware skip).
- Schema package create now correctly returns 201 (was returning 200).

### 2.2 Update Object Content / Update Package Content (PUT/POST)

**Previous behavior:** Always updates, always modifies timestamps.

**Changes made:**
- Optional `If-Match` header support — returns 412 Precondition Failed on mismatch.
- Content-aware skip: identical content produces no write and no timestamp change.
- New `ETag` returned on successful update.
- `version` remains client-managed and is stored as-is.

### 2.3 Delete Object / Delete Package (DELETE)

**Previous behavior:** Returns 200 on first call, 204 when not found.

**Changes made:**
- Normalized to always return 204 No Content (both successful deletion and already-deleted).
- Optional `If-Match` support — validates ETag before deletion, returns 412 on mismatch.
- 403 Forbidden still returned for read-only resources.

### 2.4 Stage Transitions (POST .../actions/transition)

**Previous behavior:** Object moves from source to target stage. Retries fail because object is no longer in source.

**Changes made:**
- When object not in source stage, checks target stage. If found there, returns 200 OK with target metadata (idempotent retry).
- If object in neither stage, returns 204 No Content.

### 2.5 GET Endpoints (all)

**Previous behavior:** Always return full content.

**Changes made:**
- `ETag` header returned on GET content responses and single-object list responses.
- `If-None-Match` support — returns 304 Not Modified if ETag matches.

---

## 3. Implementation Approach

### Phase 1: Content Hashing & ETag Generation

**Goal:** Compute and store content hashes; return ETags on responses.

#### 3.1 Content Hash Computation

Added `computeContentHash()` in `AbstractEObjectStorageService`:

```java
public static String computeContentHash(EObject object) {
    if (object == null) {
        return null;
    }
    try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Resource resource = new XMIResourceImpl();
        resource.getContents().add(EcoreUtil.copy(object));
        resource.save(baos, Collections.emptyMap());
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(baos.toByteArray());
        return HexFormat.of().formatHex(digest);
    } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Failed to compute content hash", e);
        return null;
    }
}
```

Integrated into `storeObject()` so `contentHash` is always populated in `ObjectMetadata` before persisting.

#### 3.2 ETag Response Headers

Added directly in resource methods using helper:

```java
private void addETagHeader(Response.ResponseBuilder rb, ObjectMetadata metadata) {
    if (metadata != null && metadata.getContentHash() != null) {
        rb.tag(new EntityTag(metadata.getContentHash()));
    }
}
```

#### 3.3 Migration

Lazy computation: the hash is populated on the next write to any object. GET responses omit ETag for objects that have not been written since the change.

### Phase 2: Optimistic Locking (If-Match)

**Goal:** Prevent lost updates via conditional writes using content-hash-based ETags. Note that `version` is **client-managed** (set via query parameter or in the object itself) and is not owned or incremented by the backend — it is purely descriptive metadata. Conflict detection relies on `contentHash`/ETags, not version comparison.

#### 3.4 If-Match Validation

Added in both resource classes:

```java
private Response checkIfMatch(ObjectMetadata metadata) {
    String ifMatch = headers.getHeaderString("If-Match");
    if (ifMatch == null) return null;
    String currentHash = metadata.getContentHash();
    if (currentHash == null) return null;
    String cleanedIfMatch = ifMatch.replace("\"", "");
    if (!cleanedIfMatch.equals(currentHash)) {
        return Response.status(Response.Status.PRECONDITION_FAILED)
                .entity("Resource has been modified. ETag mismatch.").build();
    }
    return null;
}
```

**Rollout:** Phase 2a only — `If-Match` is optional. Clients that don't provide it get last-write-wins.

#### 3.5 Content-Aware Skip

In update methods, after computing the new content hash:

```java
String newHash = AbstractEObjectStorageService.computeContentHash(object);
if (newHash != null && newHash.equals(metadata.getContentHash())) {
    Response.ResponseBuilder rb = Response.ok(metadata);
    addETagHeader(rb, metadata);
    return rb.build();
}
```

Repeated calls with identical content produce no observable side effects.

### Phase 3: Idempotent Transitions

**Goal:** Make stage transitions safe to retry.

#### 3.7 Transition Idempotency Check

Implemented in the REST resource layer. When object is not in source stage:

```java
// Check if already in target stage (idempotent retry)
ObjectMetadata targetMetadata = scopeService.getMetadataFromStageForRegistry(...)
    .getValue();
if (targetMetadata != null) {
    return Response.ok(targetMetadata).build();
}
```

#### 3.8 Idempotency Key for Transitions

**Deferred.** The target-stage check covers the common retry scenario without requiring a cache layer.

### Phase 4: Idempotent DELETE Normalization

#### 3.9 Consistent DELETE Responses

DELETE always returns `204 No Content`:

```java
// Object not found — already gone
return Response.noContent().build(); // 204

// Object deleted successfully
deleteFromStage(...);
return Response.noContent().build(); // 204
```

### Phase 5: Conditional GET (If-None-Match)

#### 3.10 Cache Validation

Added in GET methods using helper:

```java
private Response evaluateConditionalGet(Response.ResponseBuilder rb, ObjectMetadata metadata) {
    String ifNoneMatch = headers.getHeaderString("If-None-Match");
    if (ifNoneMatch != null && metadata.getContentHash() != null) {
        String cleanedIfNoneMatch = ifNoneMatch.replace("\"", "");
        if (cleanedIfNoneMatch.equals(metadata.getContentHash())) {
            return Response.notModified(new EntityTag(metadata.getContentHash())).build();
        }
    }
    return rb.build();
}
```

---

## 4. Implementation Priority & Dependencies

```
Phase 1: Content Hashing & ETags          (foundation — no breaking changes)       ✅ Done
   ↓
Phase 2: Optimistic Locking (If-Match)     (depends on Phase 1 for ETags)          ✅ Done (Phase 2a)
   ↓
Phase 3: Idempotent Transitions           (independent of Phase 1/2)               ✅ Done
   ↓
Phase 4: DELETE Normalization             (independent, trivial)                    ✅ Done
   ↓
Phase 5: Conditional GET                  (depends on Phase 1 for ETags)            ✅ Done
```

### Files Modified

| Phase | File | Change |
|-------|------|--------|
| 1 | `AbstractEObjectStorageService.java` | Added `computeContentHash()`, call it in `storeObject()` |
| 1 | `ObjectRegistryResource.java` | Added `ETag` header to responses via `addETagHeader()` helper |
| 1 | `SchemaPackagesResource.java` | Added `ETag` header to responses via `addETagHeader()` helper |
| 2 | `ObjectRegistryResource.java` | Added `If-Match` validation via `checkIfMatch()` helper in update/delete methods |
| 2 | `SchemaPackagesResource.java` | Added `If-Match` validation via `checkIfMatch()` helper in update/delete methods |
| 2 | `ObjectRegistryResource.java` | Added content-aware skip (hash comparison before write) |
| 2 | `SchemaPackagesResource.java` | Added content-aware skip (hash comparison before write) |
| 3 | `ObjectRegistryResource.java` | Added target-stage check in `transitionObject()` |
| 3 | `SchemaPackagesResource.java` | Added target-stage check in `transitionPackage()` |
| 4 | `ObjectRegistryResource.java` | Normalized DELETE response to 204 |
| 4 | `SchemaPackagesResource.java` | Normalized DELETE response to 204 |
| 5 | `ObjectRegistryResource.java` | Added `If-None-Match` handling via `evaluateConditionalGet()` helper |
| 5 | `SchemaPackagesResource.java` | Added `If-None-Match` handling via `evaluateConditionalGet()` helper |

---

## 5. Test Strategy

### 5.1 Unit Tests — ✅ Implemented

**Content Hash Tests** (in `AbstractEObjectStorageServiceTest.java`):
- `testComputeContentHash_ReturnsDeterministicHash` — identical content produces identical hash.
- `testComputeContentHash_DifferentContentProducesDifferentHash` — different content produces different hash.
- `testComputeContentHash_NullReturnsNull` — null input returns null.
- `testStoreObject_SetsContentHash` — `storeObject()` populates `contentHash` in metadata.
- `testComputeContentHash_IsValidSha256Hex` — hash is valid 64-character hex string.

### 5.2 Integration Tests (REST Layer) — ✅ Implemented

**ETag Tests** (in `ObjectRegistryResourceTest.java` and `SchemaPackagesResourceTest.java`):
- Create returns ETag header.
- Get content returns ETag header.
- ETag value is a valid non-empty string.

**If-Match Tests:**
- `PUT` with correct `If-Match` → 200 OK.
- `PUT` with stale `If-Match` → 412 Precondition Failed.
- `PUT` without `If-Match` → 200 OK (backward compatible).

**If-None-Match Tests:**
- `GET` with matching `If-None-Match` → 304 Not Modified.
- `GET` with non-matching `If-None-Match` → 200 OK with body.

**Idempotency Tests:**
- Update with identical content → 200, no timestamp change (content-aware skip).
- Delete already-deleted resource → 204.
- Delete non-existent resource → 204.
- Transition already-transitioned object → 200 with target metadata.

### 5.3 Backward Compatibility

- Existing clients that don't send `If-Match` continue to work unchanged (Phase 2a).
- Objects without `contentHash` (pre-migration) don't return `ETag`; conditional requests are not enforced.
- DELETE response code change from 200→204 is semantically equivalent.
- Schema package create response change from 200→201 for new packages is a correction (was a bug).

### 5.4 Not Yet Tested

- Concurrency tests with parallel threads (sequential stale-ETag scenarios are tested instead).
- Storage backend-specific tests (Apicurio round-trip of `contentHash`).

### 5.5 Test Location

- `org.eclipse.fennec.model.atlas.rest.tests` — REST-level integration tests for ETag, If-Match, If-None-Match, idempotency scenarios.
- `org.eclipse.fennec.model.atlas.management/test` — Unit tests for content hash computation.
