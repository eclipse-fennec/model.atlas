# Issue sketch — a package's version is guessed from its nsURI, and the guess is often wrong

*Draft for a GitHub issue. Written 2026-08-07 out of quality-review finding **F134**, which was
itself found while fixing F57: pulling this logic out of `SchemaPackagesResource` made it
unit-testable for the first time, and the first test written against it went red for the wrong
reason — the code was wrong, not the test.*

---

## Summary

When a schema package is uploaded without an explicit `?version=`, its version is inferred by
parsing **every segment of its nsURI** with `Version.parseVersion` and keeping the last one that
parses. `Version.parseVersion` accepts a bare number, so **a year is read as a major version**:

| nsURI | Inferred version |
|---|---|
| `http://www.eclipse.org/emf/2002/Ecore` | `2002.0.0` |
| `http://www.w3.org/2001/XMLSchema` | `2001.0.0` |
| `http://www.omg.org/spec/UML/20131001` | `20131001.0.0` |
| `http://10.2.3.4/model/5.0` | `4.0.0` — the host beats the real version |

Two consequences, one silent and one loud:

1. The package is **stored** under that version, and the value is not confined to the response —
   it becomes the Apicurio artifact version and a query key.
2. A caller who states the package's **real** version is **rejected**:
   `400 Version parameter '1.0.0' is not compatible with URI version '2002.0.0'`. The year outranks
   the truth, so such a package cannot be uploaded with an honest version at all.

Year-in-nsURI is the convention for standard models, not an edge case.

## What happens today

`NsUriVersions.extractVersion` (moved verbatim out of `SchemaPackagesResource.java:964-1005`):

```java
URI uri = URI.createURI(nsUri);
Version lastValidVersion = null;

for (String segment : uri.segments()) {
    try {
        lastValidVersion = Version.parseVersion(segment);   // "2002" parses -> 2002.0.0
    } catch (IllegalArgumentException e) {
        // not a version, continue
    }
}

if (uri.hasAuthority()) {                                  // and then the host, which
    String[] parts = uri.authority().split("[/.]");         // OVERWRITES the segment result
    for (String part : parts) {
        try {
            lastValidVersion = Version.parseVersion(part);
        } catch (IllegalArgumentException e) {
            // not a version, continue
        }
    }
}
return lastValidVersion;
```

Two independent problems in eleven lines:

- **A bare number is a valid OSGi version.** `2002` → `2002.0.0`. Nothing distinguishes a year, a
  date, or a spec number from a major version.
- **The authority is parsed second and unconditionally overwrites.** For a numeric host, the last
  octet wins over a genuine version segment — `http://10.2.3.4/model/5.0` infers `4.0.0`. It is
  hard to see what this branch was ever meant to catch; a hostname like `www.eclipse.org` yields
  nothing, and anything it *does* yield is an IP octet.

Both are asserted as **current** behaviour in `NsUriVersionsTest`
(`testAYearSegmentIsMisreadAsAVersion`, `testANumericAuthorityOverridesTheVersionSegment`,
`testAYearSegmentRejectsAnHonestVersionParameter`), with javadoc saying the assertions should be
inverted when this is decided.

### It is documented behaviour

Both endpoints advertise the inference in their OpenAPI description:

> *"Package version. If not provided, will be extracted from the nsURI. If provided, must be
> semantically compatible with the URI version."*

So changing it is an API-visible change, not a silent bug fix — which is why this is a sketch.

## Where the inferred value goes

`resolvedVersion` is not display-only. From `SchemaPackagesResource` it reaches
`updateInStageForRegistry(...)` on both the create-overwrite path (:349) and the update path (:566),
and `metadata.setVersion(resolvedVersion)` on create (:370). From there:

| Consumer | What it does with it |
|---|---|
| `ApicurioStorageHelper.java:395-396` | sets it as the **Apicurio artifact version** — persisted in the registry |
| `BasicEObjectRegistryService.java:597-598,321,345` | keys `objectIdsByVersion`, backing `findByVersion(String)` and its regex variant |
| `management.file` | ignores it |
| `management.git` — `GitStorageHelper.java:555` | sets `version` to the **commit SHA**, i.e. the same field means something else entirely |

That last row is worth pausing on: `ObjectMetadata.version` already carries three different
meanings depending on backend (a semantic version, a commit SHA, nothing). Inferring a semantic
version from a URI is layered on top of a field whose contract is not settled.

## Why it matters

- **Stored data is wrong today.** Any package whose nsURI carries a year has been stored with that
  year as its version, in Apicurio as the artifact version. A fix changes what *new* uploads store;
  existing artifacts keep the old value unless someone migrates them.
- **A correct client is punished.** Passing the real version is exactly what a careful caller does,
  and it is the case that fails.
- **`findByVersion` answers nonsense.** Searching for `1.0.0` will not find the package a caller
  believes is at 1.0.0.

## Options

### A. Require a version-shaped segment

Accept a segment only if it looks like a version — e.g. matches `\d+\.\d+(\.\d+)?` — before handing
it to `parseVersion`.

```
http://www.eclipse.org/emf/2002/Ecore   -> null (no version)
http://www.gme.org/datagen/1.0          -> 1.0.0
```

- **for:** smallest change that fixes the reported case; keeps the convenience for the URIs that
  really do carry a version.
- **against:** an nsURI whose version is genuinely a bare major (`http://example.org/model/2`)
  stops being detected. Probably a good trade — a bare number is indistinguishable from a year.
- **effort:** minutes.

### B. Look only at the last segment

The convention the inference is really chasing is "the version is the last path element".

```
http://www.eclipse.org/emf/2002/Ecore   -> null ("Ecore" is not a version)
http://www.gme.org/datagen/1.0          -> 1.0.0
http://www.gme.org/datagen/1.0/model    -> null (regression vs today)
```

- **for:** a rule that can be stated in one sentence in the API docs.
- **against:** loses versions that sit mid-path, which today's "last that parses" does find.
- **effort:** minutes.

### C. A + B — the last segment, and it must be version-shaped

- **for:** both failure modes closed, one sentence to document, no reliance on `parseVersion`'s
  leniency.
- **against:** the same mid-path regression as B.
- **effort:** minutes.

### D. Stop inferring; require `?version=`

Make the parameter authoritative: no parameter → no version (or an explicit `0.0.0`).

- **for:** no guessing, nothing to get subtly wrong, and the caller states what they mean. Fits a
  project that is [snapshot-only for now](#related) and has not settled what `version` means across
  backends.
- **against:** the loudest API change — every existing client relying on the documented inference
  starts storing packages without a version; the OpenAPI description and any client code change.
- **effort:** small, but it needs a decision about what an absent version means downstream
  (Apicurio's artifact version, `findByVersion`).

### E. Keep inferring, but never let the guess veto the caller

Narrow fix for the loud half only: if `?version=` is given, use it and drop the compatibility check
(or log a warning on mismatch).

- **for:** unblocks correct callers immediately; smallest blast radius; the compatibility check is
  the part with no defensible purpose, since the "URI version" it compares against is a guess.
- **against:** leaves the silent half — a package uploaded without a parameter still gets `2002.0.0`.
- **effort:** minutes. **Combines well with A/B/C**, which fix the silent half.

### F. Drop the authority branch (do this regardless)

No option above needs it, and it can only produce IP octets. It should go whichever of A–E is
chosen.

## Recommendation

**C + E + F**, and they are independent enough to land together in one small change:

- **C** so a year, a date and a spec number stop being read as versions,
- **E** so an explicit `?version=` is always honoured — the compatibility check compares the
  caller's truth against a heuristic and should not be able to win,
- **F** because the authority branch has no purpose and actively corrupts the result.

**D** is the better long-term answer *if* the decision is that a package's version is something the
uploader declares rather than something Atlas derives — that is a product question, and it also
wants an answer to "what does `ObjectMetadata.version` mean when the backend is git and already
uses it for the commit SHA?"

## Questions for the decision

1. Is a package's version something Atlas **derives** from the nsURI, or something the uploader
   **declares**? (Picks A/B/C/E vs D.)
2. What should happen to packages already stored with a year as their version — leave them, or
   migrate the Apicurio artifact versions?
3. Should `ObjectMetadata.version` keep meaning three different things per backend (semantic
   version / commit SHA / unused), or does that need its own issue?
4. Is anyone relying on the documented "extracted from the nsURI" behaviour today?

## Related

- **F134** — the review finding; the report row carries the same evidence.
- **F57** — the refactor that exposed this. The version code had never been reachable from a test;
  the first unit test written against it found the defect.
- **`NsUriVersionsTest`** — the two tests documenting present behaviour, to be inverted when this
  lands.
- The project is prototype/snapshot-only for now, which is also why baselining (F14) is off — worth
  weighing when deciding how much versioning machinery a package needs at all.
- `GitStorageHelper.java:555` — the git backend's use of the same field for a commit SHA.
