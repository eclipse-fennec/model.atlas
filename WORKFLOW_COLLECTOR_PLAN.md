# Plan: WorkflowCollector Refactoring (Consolidated Model)

## Overview

Replace `ScopeCollector` with a new `WorkflowCollector` that tracks `ScopeService` instances only. Consolidate all workflow-related model classes (`Scope`, `Registry`, `Stage`, `StageTransition`) into the single `workflow-api.ecore` model.

## Key Design Decisions

1. **`RegistryService.getRegistry()`** returns a pre-built `Registry` (built once at activation)
2. **`ScopeService.getScope()`** returns a pre-built `Scope` (built once at activation)
3. **WorkflowCollector is trivial** - just collects services and calls `getScope()`

Both objects are built once at activation, making the system efficient and the collector logic-free.

---

## New Model Structure in workflow-api.ecore

```
workflowapi (package)
├── Stage                    [existing]
│   ├── name: EString
│   ├── writable: EBoolean
│   └── final: EBoolean
│
├── StageTransition          [NEW]
│   ├── fromStage: EString
│   └── toStage: EString
│
├── Registry                 [NEW]
│   ├── name: EString
│   ├── description: EString
│   ├── links: EMap<String, String>
│   ├── stages: EList<Stage> [containment]
│   └── allowedTransitions: EList<StageTransition> [containment]
│
├── Scope                    [NEW]
│   ├── name: EString
│   ├── description: EString
│   ├── parentScope: EString
│   ├── links: EMap<String, String>
│   └── registries: EList<Registry> [containment]
│
├── ScopeContainer           [NEW]
│   └── scopes: EList<Scope> [containment]
│
├── RegistryService<T>       [add ONE operation]
│   └── + getRegistry(): Registry
│
├── ScopeService<T>          [add ONE operation]
│   └── + getScope(): Scope
│
└── EObjectWorkflowService<T> [unchanged]
```

---

## Implementation Phases

### Phase 1: Update workflow-api.ecore

**File:** `org.eclipse.fennec.model.atlas.workflow/model/workflow-api.ecore`

**Add new EClasses:**

```xml
<!-- LinksMap entry for EMap -->
<eClassifiers xsi:type="ecore:EClass" name="LinksMapEntry" instanceClassName="java.util.Map$Entry">
  <eStructuralFeatures xsi:type="ecore:EAttribute" name="key" eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
  <eStructuralFeatures xsi:type="ecore:EAttribute" name="value" eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
</eClassifiers>

<!-- StageTransition -->
<eClassifiers xsi:type="ecore:EClass" name="StageTransition">
  <eAnnotations source="http://www.eclipse.org/emf/2002/GenModel">
    <details key="documentation" value="Represents an allowed transition between two stages."/>
  </eAnnotations>
  <eStructuralFeatures xsi:type="ecore:EAttribute" name="fromStage" eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
  <eStructuralFeatures xsi:type="ecore:EAttribute" name="toStage" eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
</eClassifiers>

<!-- Registry -->
<eClassifiers xsi:type="ecore:EClass" name="Registry">
  <eAnnotations source="http://www.eclipse.org/emf/2002/GenModel">
    <details key="documentation" value="Data representation of a registry, built at RegistryService activation."/>
  </eAnnotations>
  <eStructuralFeatures xsi:type="ecore:EAttribute" name="name" eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
  <eStructuralFeatures xsi:type="ecore:EAttribute" name="description" eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
  <eStructuralFeatures xsi:type="ecore:EReference" name="links" upperBound="-1" eType="#//LinksMapEntry" containment="true"/>
  <eStructuralFeatures xsi:type="ecore:EReference" name="stages" upperBound="-1" eType="#//Stage" containment="true"/>
  <eStructuralFeatures xsi:type="ecore:EReference" name="allowedTransitions" upperBound="-1" eType="#//StageTransition" containment="true"/>
</eClassifiers>

<!-- Scope -->
<eClassifiers xsi:type="ecore:EClass" name="Scope">
  <eAnnotations source="http://www.eclipse.org/emf/2002/GenModel">
    <details key="documentation" value="Data representation of a scope, built at ScopeService activation."/>
  </eAnnotations>
  <eStructuralFeatures xsi:type="ecore:EAttribute" name="name" eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
  <eStructuralFeatures xsi:type="ecore:EAttribute" name="description" eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
  <eStructuralFeatures xsi:type="ecore:EAttribute" name="parentScope" eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
  <eStructuralFeatures xsi:type="ecore:EReference" name="links" upperBound="-1" eType="#//LinksMapEntry" containment="true"/>
  <eStructuralFeatures xsi:type="ecore:EReference" name="registries" upperBound="-1" eType="#//Registry" containment="true"/>
</eClassifiers>

<!-- ScopeContainer -->
<eClassifiers xsi:type="ecore:EClass" name="ScopeContainer">
  <eAnnotations source="http://www.eclipse.org/emf/2002/GenModel">
    <details key="documentation" value="Container for multiple Scope objects, used in REST list responses."/>
  </eAnnotations>
  <eStructuralFeatures xsi:type="ecore:EReference" name="scopes" upperBound="-1" eType="#//Scope" containment="true"/>
</eClassifiers>
```

**Add ONE operation to RegistryService:**

```xml
<eOperations name="getRegistry" eType="#//Registry">
  <eAnnotations source="http://www.eclipse.org/emf/2002/GenModel">
    <details key="documentation" value="Get the Registry representation, pre-built at activation."/>
  </eAnnotations>
</eOperations>
```

**Add ONE operation to ScopeService:**

```xml
<eOperations name="getScope" eType="#//Scope">
  <eAnnotations source="http://www.eclipse.org/emf/2002/GenModel">
    <details key="documentation" value="Get the Scope representation, pre-built at activation."/>
  </eAnnotations>
</eOperations>
```

---

### Phase 2: Implement RegistryServiceImpl.getRegistry()

**File:** `RegistryServiceImpl.java`

```java
public class RegistryServiceImpl<T extends EObject> implements RegistryService<T> {

    private List<Stage> stages;
    private Map<String, Set<String>> transitionsMap;
    private Registry registry;  // Pre-built at activation

    @Activate
    public RegistryServiceImpl(..., RegistryServiceConfig config) {
        this.stages = parseStages(config.stages());
        this.transitionsMap = parseTransitions(config.workflow_transitions());
        // ... existing init ...

        this.registry = buildRegistry(config);
    }

    private Registry buildRegistry(RegistryServiceConfig config) {
        Registry reg = WorkflowApiFactory.eINSTANCE.createRegistry();
        reg.setName(config.registry_name());
        reg.setDescription(config.registry_description());

        // Copy stages
        for (Stage stage : stages) {
            Stage copy = WorkflowApiFactory.eINSTANCE.createStage();
            copy.setName(stage.getName());
            copy.setWritable(stage.isWritable());
            copy.setFinal(stage.isFinal());
            reg.getStages().add(copy);
        }

        // Build transitions
        for (Map.Entry<String, Set<String>> entry : transitionsMap.entrySet()) {
            for (String toStage : entry.getValue()) {
                StageTransition t = WorkflowApiFactory.eINSTANCE.createStageTransition();
                t.setFromStage(entry.getKey());
                t.setToStage(toStage);
                reg.getAllowedTransitions().add(t);
            }
        }

        return reg;
    }

    @Override
    public Registry getRegistry() {
        return registry;
    }
}
```

---

### Phase 3: Implement ScopeServiceImpl.getScope()

**File:** `ScopeServiceImpl.java`

```java
public class ScopeServiceImpl<T extends EObject> implements ScopeService<T> {

    private List<RegistryService<T>> registryServices;
    private Scope scope;  // Pre-built at activation

    @Activate
    public ScopeServiceImpl(
            @Reference(name = "registryService") List<RegistryService<T>> registryServices,
            ScopeServiceConfig config) {
        this.registryServices = registryServices;

        this.scope = buildScope(config);
    }

    private Scope buildScope(ScopeServiceConfig config) {
        Scope s = WorkflowApiFactory.eINSTANCE.createScope();
        s.setName(config.scope_name());
        s.setDescription(config.scope_description());
        s.setParentScope(config.scope_parent());

        // Build links
        s.getLinks().put("self", "/scopes/" + config.scope_name());

        // Collect pre-built registries
        for (RegistryService<T> regService : registryServices) {
            s.getRegistries().add(regService.getRegistry());
        }

        return s;
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    // ... rest of existing methods ...
}
```

---

### Phase 4: Create WorkflowCollector (Trivial)

**File:** `WorkflowCollector.java`

```java
package org.eclipse.fennec.model.atlas.scope;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * Collects ScopeService instances. All Scope objects are pre-built by the services.
 */
@Component(name = "WorkflowCollector", immediate = true, service = WorkflowCollector.class)
public class WorkflowCollector {

    private static final Logger LOGGER = Logger.getLogger(WorkflowCollector.class.getName());

    private final Map<String, ScopeService<?>> scopeServices = new ConcurrentHashMap<>();

    // === Public API ===

    public Scope getScopeByName(String name) {
        ScopeService<?> service = scopeServices.get(name);
        return service != null ? service.getScope() : null;
    }

    public List<Scope> getAllScopes() {
        return scopeServices.values().stream()
            .map(ScopeService::getScope)
            .toList();
    }

    public ScopeService<?> getScopeService(String scopeName) {
        return scopeServices.get(scopeName);
    }

    // === ScopeService Binding ===

    @Reference(
        policy = ReferencePolicy.DYNAMIC,
        policyOption = ReferencePolicyOption.GREEDY,
        cardinality = ReferenceCardinality.MULTIPLE
    )
    public void bindScopeService(ScopeService<?> scopeService, Map<String, Object> properties) {
        String scopeName = scopeService.getScope().getName();
        scopeServices.put(scopeName, scopeService);
        LOGGER.info("Bound ScopeService: " + scopeName);
    }

    public void unbindScopeService(ScopeService<?> scopeService, Map<String, Object> properties) {
        String scopeName = scopeService.getScope().getName();
        scopeServices.remove(scopeName);
        LOGGER.info("Unbound ScopeService: " + scopeName);
    }
}
```

**That's it.** The collector has zero build logic - it just stores services and calls `getScope()`.

---

### Phase 5: Update ScopesResource

**File:** `ScopesResource.java`

```java
@Path("/scopes")
@Tag(name = "Scope Management")
public class ScopesResource {

    @Reference
    private WorkflowCollector workflowCollector;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listScopes() {
        ScopeContainer container = WorkflowApiFactory.eINSTANCE.createScopeContainer();
        container.getScopes().addAll(workflowCollector.getAllScopes());
        return Response.ok(container).build();
    }

    @GET
    @Path("/{scopeName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScope(@PathParam("scopeName") String scopeName) {
        Scope scope = workflowCollector.getScopeByName(scopeName);
        if (scope == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(scope).build();
    }

    @GET
    @Path("/{scopeName}/registries")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listRegistries(@PathParam("scopeName") String scopeName) {
        Scope scope = workflowCollector.getScopeByName(scopeName);
        if (scope == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(scope.getRegistries()).build();
    }

    @GET
    @Path("/{scopeName}/registries/{registryName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRegistry(
            @PathParam("scopeName") String scopeName,
            @PathParam("registryName") String registryName) {
        Scope scope = workflowCollector.getScopeByName(scopeName);
        if (scope == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return scope.getRegistries().stream()
            .filter(r -> registryName.equals(r.getName()))
            .findFirst()
            .map(r -> Response.ok(r).build())
            .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}
```

---

## Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         WorkflowCollector                           │
│  - Stores: Map<String, ScopeService<?>>                             │
│  - getAllScopes(): services.stream().map(getScope()).toList()       │
│  - Zero build logic!                                                │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
                                │ getScope() → Scope (pre-built)
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         ScopeServiceImpl                            │
│  - Scope built at @Activate                                         │
│  - Collects Registry from each RegistryService                      │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
                                │ getRegistry() → Registry (pre-built)
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       RegistryServiceImpl                           │
│  - Registry built at @Activate                                      │
│  - Contains stages and transitions                                  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Interface Changes Summary

```java
// RegistryService - add 1 method
Registry getRegistry();

// ScopeService - add 1 method
Scope getScope();
```

**Only 2 new methods. Both return pre-built objects.**

---

## Files Summary

### New Files
| File | Description |
|------|-------------|
| `WorkflowCollector.java` | Trivial collector - just stores services |

### Modified Files
| File | Changes |
|------|---------|
| `workflow-api.ecore` | Add Scope, Registry, StageTransition, ScopeContainer, LinksMapEntry; add getRegistry(), getScope() |
| `RegistryServiceImpl.java` | Build Registry at activation |
| `RegistryServiceConfig.java` | Add registry_description() |
| `ScopeServiceImpl.java` | Build Scope at activation |
| `ScopeServiceConfig.java` | Add scope_description() |
| `ScopesResource.java` | Use WorkflowCollector |

### Deprecated
| File | Status |
|------|--------|
| `ScopeCollector.java` | Deprecate |
| `org.eclipse.fennec.model.atlas.scope.model/` | Deprecate bundle |

---

## Implementation Order

1. Update `workflow-api.ecore` (new EClasses + 2 operations)
2. Regenerate EMF code
3. Implement `RegistryServiceImpl.getRegistry()`
4. Implement `ScopeServiceImpl.getScope()`
5. Create `WorkflowCollector`
6. Update `ScopesResource`
7. Deprecate old code
8. Test

---

## Benefits

1. **Minimal interface changes** - only 2 new methods
2. **Zero collector logic** - just stores and delegates
3. **Efficient** - objects built once at activation
4. **Clean separation** - each service builds its own representation
5. **Symmetrical design** - both services follow same pattern (getXxx())
