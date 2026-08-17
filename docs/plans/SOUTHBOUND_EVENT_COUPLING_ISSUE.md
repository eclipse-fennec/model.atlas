# Issue sketch — the workflow bundle emits southbound-gateway configuration events

*Draft for a GitHub issue. Written 2026-08-06 from quality-review finding F45.*

---

## Summary

`DynamicEPackageRegistrationService` — the generic service that registers dynamically loaded
EPackages — also emits configuration events for one specific downstream product, a sensinact
southbound gateway. The topic, the configuration key and the shape of the configuration value are
compiled into the workflow bundle:

```java
String topic       = "configuration/ADD/SouthboundMappingService";
String configKey   = "sthbnd.mapping.codec.typeMap." + modelName;
String configValue = nsURI + "#//" + modelName + "Sensor";     // modelName = ePackage.getName()
```
*(DynamicEPackageRegistrationService.java:549-568, REMOVE variant :573-596)*

The events are really sent — this is not dead code — but **nothing in this repository, or in any
sibling repository on this machine, consumes them**. And the value they carry is derived from a
naming convention that the one real example we have does not follow.

## What actually happens at runtime

| | |
|---|---|
| Component activation | `@Component(immediate = true)`, no configuration policy — it runs in **every** runtime containing the workflow bundle |
| `TypedEventBus` reference | mandatory + static (`@Reference private TypedEventBus typedEventBus`) |
| Event bus in the runtime | yes — `org.apache.aries.typedevent.bus` in `runtime_base.bndrun:97` |
| Emission trigger | registering an EPackage queues the event (:309); it is delivered as soon as a `ResourceSet` service with `(emf.name=<ePackage name>)` exists — one already present (:639) or one appearing later (:535). Unregistration emits REMOVE (:376) |
| Consumers in model.atlas | **none** — no match for `SouthboundMappingService` or `sthbnd.mapping` outside the emitting method |
| Consumers in sibling repos | **none found**. The predecessor repo `model-atlas-cloud` contains the same emitter plus a test *sender* (`org.gecko.mac.fake.event.sender`), but no receiver either |

So on every Atlas deployment these events go onto a topic with no subscribers and are dropped.

## Why it is worth fixing anyway

**1. The value is probably wrong.** The only concrete example of these events we can find is the
fake sender in `model-atlas-cloud`:

```java
CONFIG_PROP_KEY   = "sthbnd.mapping.codec.typeMap.M5AirQ";
CONFIG_PROP_VALUE = "http://datainmotion.com/mac/sensor/airquality/1.0.0#//AirQualitySensor";
```

The key suffix (`M5AirQ`) and the EClass (`AirQualitySensor`) are *different names*. Atlas derives
both from the same `ePackage.getName()`, so it can only ever emit
`typeMap.X → …#//XSensor`. It cannot reproduce the one pairing we know is real. For any model
without a `<PackageName>Sensor` EClass — most models — the emitted value points at an EClass that
does not exist. Nothing validates it, and no one is listening, so this is invisible until a
gateway is actually wired up.

**2. The coupling runs the wrong way.** A generic model-registration service in Atlas carries
knowledge of another product's configuration PID, property namespace and model naming convention.
Whoever owns the gateway cannot change any of those without a change in this repo, and Atlas
cannot be deployed without carrying them.

**3. Atlas depends on the event bus because of it.** The `TypedEventBus` reference is mandatory and
static, so `DynamicEPackageRegistrationService` cannot activate at all without a typed-event
implementation — assemble a runtime without `aries.typedevent.bus` and dynamic EPackage
registration silently disappears rather than degrading. The code is written as if the reference
were optional: there are four `if (typedEventBus == null)` guards that can never be true.

## Options

### A. Move the emission out, into the consuming application
The review's suggestion: a separate `StageActionService` (or a listener on EPackage registration)
owned by whoever owns the gateway, in their bundle.
*For:* the naming convention lives with the people who know it; Atlas keeps no downstream
knowledge and loses its event-bus dependency. *Against:* needs an extension point Atlas is willing
to publish, and someone to own the new bundle. *Effort:* medium.

### B. Keep it here, make it configuration
Topic, key prefix and a value template (e.g. `{nsURI}#//{name}Sensor`) come from ConfigAdmin,
defaulting to *not emitting* when unconfigured.
*For:* small, immediately removes the compiled-in product knowledge and the bogus events in
deployments that do not want them. *Against:* Atlas still owns the mechanism and the event-bus
dependency. *Effort:* small.

### C. Publish a whiteboard extension point
Atlas defines something like `EPackageRegistrationListener` and notifies whoever registers one;
the gateway integration implements it.
*For:* the clean version of A, and useful beyond this one consumer. *Against:* new exported API to
design and support. *Effort:* medium.

### D. Delete the emission
If the southbound integration is no longer in use, remove the two methods, the pending-event map
and the `TypedEventBus` reference.
*For:* smallest surface, no dependency, no bogus events. *Against:* breaks anyone quietly relying
on it. *Effort:* small.

### E. Do nothing, document it
Leave as is with a comment naming the consumer and the convention.
*For:* free. *Against:* keeps a wrong value and a hidden dependency.

**Independent of all of the above:** the `TypedEventBus` reference should be `OPTIONAL` (which
makes the existing null guards meaningful and lets the registration service work without the event
bus), or the guards should go. That one is self-contained and can be fixed without deciding
anything else — say the word and I will do it on its own.

## Questions for the decision

1. Is the sensinact southbound mapping integration still live? If it is, **which repository
   consumes these events** — none of the checkouts on this machine does, so we cannot see the
   contract from here.
2. If it is live: is the `<PackageName>Sensor` convention correct, given that the only example we
   have (`M5AirQ` → `AirQualitySensor`) does not follow it?
3. Should Atlas own this integration at all, or should it publish an extension point and let the
   gateway own it?

## Related

- **F45** in `docs/reviews/quality-review-2026-08-05.md`.
- The predecessor repo `model-atlas-cloud` has the same code in
  `org.gecko.mac.governance/.../DynamicEPackageRegistrationService.java`, so a decision here
  probably wants to be reflected there (or that repo is being retired anyway).
