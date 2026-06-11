# DataGen REST Resource

REST endpoint for generating fake EMF data via the [DataGen Service](../org.eclipse.fennec.model.atlas.datagen/README.md). Accepts a `DataGenConfig` as XMI and returns a `DataGenResult` containing the generated EObject instances.

## Endpoint

```
POST <host>/atlas/rest/datagen
Content-Type: application/xmi
Accept: application/xmi | application/json
```

The full URL depends on the runtime configuration. Default for local development: `http://localhost:8086/atlas/rest/datagen`.

### Content Negotiation

The response format is controlled by the `Accept` header:

| Accept Header | Response Format |
|---------------|-----------------|
| `application/xmi` | XMI (EMF standard serialization) |
| `application/json` | JSON (EMF JSON serialization) |

The request body (`Content-Type`) is always `application/xmi`.

### Request

The request body is a `DataGenConfig` serialized as XMI. The `contextClass` entries reference EClasses from EPackages that must be registered in the OSGi runtime's `ResourceSet`.

### Response

| Status | Description |
|--------|-------------|
| `200 OK` | `DataGenResult` containing all generated instances |
| `400 Bad Request` | Referenced EClasses not found in any registered EPackage |
| `500 Internal Server Error` | Unexpected generation error |

### Error Response

On `400` or `500`, the response body contains a plain text error message describing which EClasses could not be resolved or what went wrong.

## Bruno Collection

A [Bruno](https://www.usebruno.com/) API collection is included in the `bruno/` directory for interactive testing. Import the `bruno/Model Atlas Datagen` folder into Bruno to get pre-configured requests.

The collection uses the variable `{{baseUrl}}` (default: `http://localhost:8086/atlas/rest`).

## Examples

### Generate Person instances (XMI response)

```bash
curl -X POST http://localhost:8086/atlas/rest/datagen \
  -H "Content-Type: application/xmi" \
  -H "Accept: application/xmi" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<datagen:DataGenConfig xmi:version="2.0"
    xmlns:xmi="http://www.omg.org/XMI"
    xmlns:datagen="http://www.gme.org/datagen/1.0"
    name="person-gen"
    locale="de"
    seed="42">
  <classConfigs contextClass="Person" instanceCount="5">
    <attributeGens featureName="firstName" generatorKey="faker.person.firstName"/>
    <attributeGens featureName="lastName" generatorKey="faker.person.lastName"/>
  </classConfigs>
</datagen:DataGenConfig>'
```

### Generate Person instances (JSON response)

```bash
curl -X POST http://localhost:8086/atlas/rest/datagen \
  -H "Content-Type: application/xmi" \
  -H "Accept: application/json" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<datagen:DataGenConfig xmi:version="2.0"
    xmlns:xmi="http://www.omg.org/XMI"
    xmlns:datagen="http://www.gme.org/datagen/1.0"
    name="person-gen"
    locale="de"
    seed="42">
  <classConfigs contextClass="Person" instanceCount="5">
    <attributeGens featureName="firstName" generatorKey="faker.person.firstName"/>
    <attributeGens featureName="lastName" generatorKey="faker.person.lastName"/>
  </classConfigs>
</datagen:DataGenConfig>'
```

### Generate with references

```bash
curl -X POST http://localhost:8086/atlas/rest/datagen \
  -H "Content-Type: application/xmi" \
  -H "Accept: application/xmi" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<datagen:DataGenConfig xmi:version="2.0"
    xmlns:xmi="http://www.omg.org/XMI"
    xmlns:datagen="http://www.gme.org/datagen/1.0"
    name="person-address-gen"
    locale="en"
    seed="123">
  <classConfigs contextClass="Address" instanceCount="10">
    <attributeGens featureName="city" generatorKey="faker.address.city"/>
    <attributeGens featureName="zipCode" generatorKey="faker.address.zipCode"/>
  </classConfigs>
  <classConfigs contextClass="Person" instanceCount="5">
    <attributeGens featureName="firstName" generatorKey="faker.person.firstName"/>
    <attributeGens featureName="lastName" generatorKey="faker.person.lastName"/>
    <referenceGens featureName="address" strategy="RANDOM" minCount="1" maxCount="1"/>
  </classConfigs>
</datagen:DataGenConfig>'
```

### Generate with static values and templates

```bash
curl -X POST http://localhost:8086/atlas/rest/datagen \
  -H "Content-Type: application/xmi" \
  -H "Accept: application/xmi" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<datagen:DataGenConfig xmi:version="2.0"
    xmlns:xmi="http://www.omg.org/XMI"
    xmlns:datagen="http://www.gme.org/datagen/1.0"
    name="mixed-gen"
    locale="en"
    seed="42">
  <classConfigs contextClass="Person" instanceCount="3">
    <attributeGens featureName="firstName" generatorKey="faker.person.firstName"
        staticValue="FixedName"/>
    <attributeGens featureName="lastName" generatorKey="faker.person.lastName"
        template="#{Name.first_name} #{Name.last_name}"/>
  </classConfigs>
</datagen:DataGenConfig>'
```

### Generate with unique values

```bash
curl -X POST http://localhost:8086/atlas/rest/datagen \
  -H "Content-Type: application/xmi" \
  -H "Accept: application/json" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<datagen:DataGenConfig xmi:version="2.0"
    xmlns:xmi="http://www.omg.org/XMI"
    xmlns:datagen="http://www.gme.org/datagen/1.0"
    name="unique-gen"
    locale="en"
    seed="99">
  <classConfigs contextClass="Person" instanceCount="20">
    <attributeGens featureName="firstName" generatorKey="faker.person.firstName" unique="true"/>
  </classConfigs>
</datagen:DataGenConfig>'
```

### Generate with round-robin references

```bash
curl -X POST http://localhost:8086/atlas/rest/datagen \
  -H "Content-Type: application/xmi" \
  -H "Accept: application/xmi" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<datagen:DataGenConfig xmi:version="2.0"
    xmlns:xmi="http://www.omg.org/XMI"
    xmlns:datagen="http://www.gme.org/datagen/1.0"
    name="roundrobin-gen"
    locale="de"
    seed="42">
  <classConfigs contextClass="Address" instanceCount="3">
    <attributeGens featureName="city" generatorKey="faker.address.city"/>
  </classConfigs>
  <classConfigs contextClass="Person" instanceCount="9">
    <attributeGens featureName="firstName" generatorKey="faker.person.firstName"/>
    <referenceGens featureName="address" strategy="ROUND_ROBIN" minCount="1" maxCount="1"/>
  </classConfigs>
</datagen:DataGenConfig>'
```

## Response Format

### XMI Response

```xml
<?xml version="1.0" encoding="UTF-8"?>
<datagen:DataGenResult xmi:version="2.0"
    xmlns:xmi="http://www.omg.org/XMI"
    xmlns:datagen="http://www.gme.org/datagen/1.0"
    xmlns:dge="https://dg.de/1.0">
  <results xsi:type="dge:Person" firstName="Hans" lastName="Mueller"/>
  <results xsi:type="dge:Person" firstName="Anna" lastName="Schmidt"/>
  <results xsi:type="dge:Address" city="Berlin" zipCode="10115"/>
</datagen:DataGenResult>
```

### JSON Response

```json
{
  "eClass": "http://www.gme.org/datagen/1.0#//DataGenResult",
  "results": [
    {
      "eClass": "https://dg.de/1.0#//Person",
      "firstName": "Hans",
      "lastName": "Mueller"
    },
    {
      "eClass": "https://dg.de/1.0#//Address",
      "city": "Berlin",
      "zipCode": "10115"
    }
  ]
}
```

## Prerequisites

The target EPackages referenced by the `contextClass` entries in the config must be registered in the OSGi runtime (e.g. loaded via the EMFFileWatcher from `workspace/models/` or via an EPackage configurator component). If a referenced EClass cannot be found, the endpoint returns `400 Bad Request` with the list of missing class names.

The `org.eclipse.fennec.model.atlas.datagen.example.model` bundle provides a sample EPackage (`dge`) with Person, Address, and Company classes for testing.

## OSGi Dependencies

- `DataGenService` (prototype scope) — the generation engine
- `ResourceSet` — provides the EPackage registry with all loaded models

## License

Eclipse Public License 2.0 (EPL-2.0)
