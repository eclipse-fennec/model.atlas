# Model spec: a metadata-properties update operation

**For:** `org.eclipse.fennec.model.atlas.workflow/model/workflow-api.ecore` (+ `.genmodel`)
**Needed by:** [`DCAT_PUBLISHING_PLAN.md`](DCAT_PUBLISHING_PLAN.md) §7 (D0, the `?dcat=` flag on an
overwrite) and §7b / O12 (the `PATCH …/metadata` endpoint)
**Status:** **implemented 2026-08-26.** The two operations are in `workflow-api.ecore` /
`.genmodel` and the code is regenerated (by Ilenia); the four implementations and the REST wiring
are in place. Kept as the record of *why* the shape is what it is, because §7b's `PATCH
…/metadata` is the second consumer of the same operation. §7 below documents the constraint that
forced it.

### As implemented

| where | what |
|---|---|
| `WritableScopeService` | `Promise<ObjectMetadata> updatePropertiesInStageForRegistry(String registry, String stage, String objectId, Map<String,Object> properties)` |
| `RegistryService` | `Promise<ObjectMetadata> updateProperties(String scope, String stage, String objectId, Map<String,Object> properties)` |
| `RegistryServiceImpl` | the real implementation — §3 below, with the two deviations noted there |
| `ScopeServiceImpl` | `validateRegistry` + delegate |
| `AtlasScopeService` | `validateRegistry` + delegate to the atlas schema registry |
| `AtlasSchemaRegistryService` | `UnsupportedOperationException`, matching its sibling mutations |

EMF generated `Map<String,Object>` (not `EMap`) for the multi-valued `StringToObjectMapEntry`
parameter, which is the nicer signature for callers — `Map.of(...)` at the call site.

---

## 1. Why the existing operation cannot do it

`WritableScopeService.updateInStageForRegistry(registry, stage, T updatedObject, objectId, version)`
is the only write the REST layer can reach that touches an existing object, and it is the wrong
tool twice over:

1. **It demands the content.** The `updatedObject` parameter is `lowerBound="1"`. Changing a
   label would mean re-sending the whole `EPackage` — which is exactly the whole-document write
   §7b exists to forbid, because it turns a label edit into an opportunity to overwrite identity.

2. **It has no channel for properties, and it discards what it is given.**
   `RegistryServiceImpl.updateInStage` (`RegistryServiceImpl.java:294`) re-reads the stored
   metadata itself and sets exactly five fields:

   ```java
   ObjectMetadata metadata = getPromiseValue(
           storageService.retrieveMetadata(scope, config.registry_name(), stage, objectId));
   metadata.setLastChangeTime(Instant.now());
   metadata.setStage(stage);
   metadata.setScope(scope);
   metadata.setRegistry(config.registry_name());
   metadata.setVersion(version);
   metadata = getPromiseValue(storageService.updateObject(objectId, updatedObject, metadata));
   ```

   Anything else the caller wants changed is unreachable. And it always calls
   `storageService.updateObject`, so a boolean edit would cost a content write, a new
   `contentHash` and `fingerprint`, a git commit on the git backend, and an
   `ActionEvent.UPDATE` dispatch — for a flag.

**The good news, and it is worth recording:** because `updateInStage` and `transitionToStage`
both *re-read* stored metadata rather than rebuilding it, `properties` already survives a
content edit and a stage promotion with no code at all. Two of the three things §7 asks us to
"get right" are already right; only the third — being able to *change* the flag — needs this
operation. §7's premise that a content edit "silently unpublishes the model" does not hold
against the current code.

The storage layer already has what is missing:
`EObjectStorageService.updateMetadata(scope, registry, stage, objectId, metadata)`
(`management/src-gen-api/…/EObjectStorageService.java:131`) writes metadata without touching
content. Nothing above it exposes that, which is the whole gap.

---

## 2. The operation

Two classifiers need it, mirroring the existing `…ForRegistry` / scope-taking split:

| classifier | operation | insert after |
|---|---|---|
| `WritableScopeService` | `updatePropertiesInStageForRegistry` | `updateInStageForRegistry` (ecore line **376**, ends 390) |
| `RegistryService` | `updateProperties` | `updateInStage` (ecore line **189**, ends 203) |

`EObjectWorkflowService` (ecore lines 17–135) carries a parallel copy of the same
`…ForRegistry` operations, but no non-generated Java in the workspace references it — so it
does **not** need the operation unless you want the two kept symmetric. Your call.

### 2a. `WritableScopeService`

```xml
    <eOperations name="updatePropertiesInStageForRegistry">
      <eAnnotations source="http://www.eclipse.org/emf/2002/GenModel">
        <details key="documentation" value="Merge entries into the properties map of an object's metadata in a certain stage and registry, without touching the object's content. Entries present in the argument are set (overwriting an existing value for the same key); keys absent from the argument are left untouched. Returns the stored ObjectMetadata."/>
      </eAnnotations>
      <eGenericType eClassifier="ecore:EDataType ../../org.eclipse.fennec.model.atlas.management/model/management.ecore#//Promise">
        <eTypeArguments eClassifier="ecore:EClass ../../org.eclipse.fennec.model.atlas.management/model/management.ecore#//ObjectMetadata"/>
      </eGenericType>
      <eParameters name="registry" eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
      <eParameters name="stage" eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
      <eParameters name="objectId" lowerBound="1" eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
      <eParameters name="properties" upperBound="-1" eType="ecore:EClass ../../org.eclipse.fennec.model.atlas.management/model/management.ecore#//StringToObjectMapEntry"/>
    </eOperations>
```

> Note the parameter order: the existing `WritableScopeService` operations declare
> `registry` before `stage` (e.g. `getMetadataFromStageForRegistry`, lines 329–336), while the
> `EObjectWorkflowService` copies declare `stage` before `registry`. I followed
> `WritableScopeService`.

### 2b. `RegistryService`

```xml
    <eOperations name="updateProperties">
      <eAnnotations source="http://www.eclipse.org/emf/2002/GenModel">
        <details key="documentation" value="Merge entries into the properties map of an object's metadata for a certain stage and scope, without touching the object's content. Entries present in the argument are set; keys absent from the argument are left untouched. Returns the stored ObjectMetadata."/>
      </eAnnotations>
      <eGenericType eClassifier="ecore:EDataType ../../org.eclipse.fennec.model.atlas.management/model/management.ecore#//Promise">
        <eTypeArguments eClassifier="ecore:EClass ../../org.eclipse.fennec.model.atlas.management/model/management.ecore#//ObjectMetadata"/>
      </eGenericType>
      <eParameters name="scope" eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
      <eParameters name="stage" eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
      <eParameters name="objectId" lowerBound="1" eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
      <eParameters name="properties" upperBound="-1" eType="ecore:EClass ../../org.eclipse.fennec.model.atlas.management/model/management.ecore#//StringToObjectMapEntry"/>
    </eOperations>
```

### 2c. Generated signature

A multi-valued `StringToObjectMapEntry` parameter generates a plain `java.util.Map` — I expected
`EMap` (the type `ObjectMetadata.getProperties()` has), and `Map` is the better outcome, since
callers can pass `Map.of(...)`:

```java
Promise<ObjectMetadata> updatePropertiesInStageForRegistry(
        String registry, String stage, String objectId, Map<String, Object> properties);
```

### 2d. genmodel

`workflow-api.genmodel` lists every operation explicitly (92 `genOperations` entries), so each
new operation needs its own entry with one `genParameters` per parameter — e.g. under
`genClasses ecoreClass="workflow-api.ecore#//WritableScopeService"` (genmodel line 165):

```xml
      <genOperations ecoreOperation="workflow-api.ecore#//WritableScopeService/updatePropertiesInStageForRegistry">
        <genParameters ecoreParameter="workflow-api.ecore#//WritableScopeService/updatePropertiesInStageForRegistry/registry"/>
        <genParameters ecoreParameter="workflow-api.ecore#//WritableScopeService/updatePropertiesInStageForRegistry/stage"/>
        <genParameters ecoreParameter="workflow-api.ecore#//WritableScopeService/updatePropertiesInStageForRegistry/objectId"/>
        <genParameters ecoreParameter="workflow-api.ecore#//WritableScopeService/updatePropertiesInStageForRegistry/properties"/>
      </genOperations>
```

The genmodel's `copyrightText` is present and must stay — regenerating without it strips the
EPL headers and the CI license gate fails.

---

## 3. What the implementation has to honour

**Two deviations from the sketch, both deliberate, both found by reading the storage layer:**

1. **The stage gate is `validateWritableStage`, not `validateUpdatableStage`.**
   `validateUpdatableStage` additionally rejects *final* stages — "Objects in the final stage
   cannot be updated" — and final stages are exactly where DCAT publishes from. Gating a
   properties edit that way would make it impossible to publish or unpublish a released model,
   which is the whole use case. The final-stage bar exists to keep released *content* immutable,
   and this operation writes no content. `writable` is still respected, so a registry that
   declares its final stage non-writable still refuses. (In the jena config every stage
   including `release` is `writable: true`.)

2. **The operation re-reads the stored metadata and returns that**, rather than returning the
   instance it mutated. `AbstractEObjectStorageService.updateMetadata` merges into an
   `EcoreUtil.copy` and stamps `lastChangeTime` itself, so the in-memory instance would carry a
   different timestamp than the persisted one — and that value becomes the REST response's
   `Last-Modified` and ETag, which a client sends back in the next `If-Match`.

Also confirmed rather than assumed: `updateMetadata` merges `properties` entry-wise
(`existing.getProperties().putAll(updates.getProperties())`), so passing the fully-merged map is
correct and same-named keys are overwritten, not duplicated.


`RegistryServiceImpl.updateProperties` — deliberately *not* a call to `updateObject`:

```
validateUpdatableStage(stage)                      // same gate as updateInStage
metadata = storageService.retrieveMetadata(scope, registry, stage, objectId)
if metadata == null -> null (the REST layer answers 204)
for each entry in properties: metadata.getProperties().put(key, value)
metadata.setLastChangeTime(Instant.now())
storageService.updateMetadata(scope, registry, stage, objectId, metadata)
return metadata
```

Must **not** change: `contentHash`, `fingerprint`, `generationTriggerFingerprint`, `version`,
`objectId`, `scope`, `stage`, `registry`, or the stored object. A properties edit is not a
content event.

**One gotcha.** `properties` is a containment list, so moving entries between two
`ObjectMetadata` instances re-parents them. Merge by `put(key, value)` on the target's `EMap` —
never `addAll` the argument's entries and never `EcoreUtil.copy` the metadata: the
suppressed-notification fennec models break `EcoreUtil.copy` with a `ClassCastException` on
`BasicInternalEList`.

---

## 4. Two decisions that are yours, not mine

**(a) Does a properties edit dispatch an `ActionEvent`? — STILL OPEN, implemented as "no".**
D0 does not need one. But O13 puts the
`dcat` flag on the `EPackage` service properties, and §7b says an edit "has to propagate" or
"the metadata says one thing, the registry says another, and the publisher believes the
registry". Dispatching `ActionEvent.UPDATE` from this operation would give that propagation for
free, since `EPackageStageActionService` re-registers on UPDATE. The cost is that an
`objectName` edit would also re-register the service — harmless, but it is a real behaviour
change and it makes a label edit visible to every `StageActionService` on the whiteboard. My
suggestion: **add the dispatch**, because §7b needs propagation anyway and a second mechanism
for it would be worse — but O13 also needs `DynamicEPackageRegistrationService` to copy the flag
onto the service properties, which does not exist yet, so dispatching today would re-register
EPackages for no benefit. Left out; it is a two-line addition when O13 lands.

**(b) Removal.** Nothing in D0 or §7b needs to *delete* a property key — `dcat=false` is a
value, not a removal — and `StringToObjectMapEntry.value` is `lowerBound="1"`, so a null value
cannot express it. If you want removal later it wants its own parameter
(`removedKeys : EString[0..*]`) rather than overloading the merge. Left out.

---

## 5. What I deliberately did **not** propose

A general patch-document operation — `patchMetadata(registry, stage, objectId, ObjectMetadata
patch)` — copying only features where `patch.eIsSet(feature)` is true. It reads well and it is a
trap: the patch arrives through a REST codec, and what a deserializer marks as "set" is not what
the caller wrote. A codec that materialises defaults would make every patch a full overwrite of
identity, provenance and content hashes — silently, and precisely in the endpoint §7b builds to
prevent that. An explicit properties parameter cannot express the mistake.

---

## 6. What landed in `SchemaPackagesResource`

The overwrite branch of `createPackage` gained, after the `updateInStageForRegistry` call:

```java
if (dcat != null) {
    metadata = scopeService.updatePropertiesInStageForRegistry(REGISTRY_NAME, stageName,
            existingMetadata.getObjectId(),
            Map.of(WorkflowConstants.DCAT_PUBLISH_METADATA_PROPERTY, dcat)).getValue();
}
```

with `dcat` declared as a nullable `Boolean` `@QueryParam` so that **absent means "leave it
alone"** and only an explicit `?dcat=false` clears the flag. A primitive `boolean` here would
make every overwrite that says nothing about DCAT silently unpublish the model — the D0 test
`testCreatePackage_OverwriteWithoutDcatParam_PreservesFlag` pins exactly that.

The same operation is then the whole write side of §7b's `PATCH …/metadata`.
