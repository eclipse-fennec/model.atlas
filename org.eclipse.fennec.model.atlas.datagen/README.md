# DataGen Service

OSGi service for generating fake EMF EObject instances based on a `DataGenConfig` and target EPackages. Uses [Datafaker](https://www.datafaker.net/) expressions for realistic attribute value generation.

## Overview

The DataGen Service takes a configuration model (`DataGenConfig`) that describes which EClasses to instantiate, how many instances to create, and how to populate attributes and references. It generates dynamic EObject instances at runtime without requiring generated Java code for the target models.

## Architecture

```
DataGenService (Interface)
  └── DataGenServiceImpl (@Component, scope=PROTOTYPE)
        └── GeneratorKeyMapper (internal)
```

- **`DataGenService`** — exported API interface
- **`DataGenServiceImpl`** — prototype-scoped OSGi DS component
- **`GeneratorKeyMapper`** — maps `faker.*` keys to Datafaker `#{...}` expressions

## Usage

### Programmatic (OSGi)

```java
@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
private DataGenService dataGenService;

// Build config
DataGenConfig config = DatagenFactory.eINSTANCE.createDataGenConfig();
config.setName("test-data");
config.setLocale("de");
config.setSeed(42);

ClassGenConfig personConfig = DatagenFactory.eINSTANCE.createClassGenConfig();
personConfig.setContextClass("Person");
personConfig.setInstanceCount(100);

AttributeGenConfig nameGen = DatagenFactory.eINSTANCE.createAttributeGenConfig();
nameGen.setFeatureName("firstName");
nameGen.setGeneratorKey("faker.person.firstName");
personConfig.getAttributeGens().add(nameGen);

config.getClassConfigs().add(personConfig);

// Generate
Map<String, List<EObject>> result = dataGenService.generate(config, targetPackages);
List<EObject> persons = result.get("Person"); // 100 generated Person instances
```

### Via REST

See [datagen.rest README](../org.eclipse.fennec.model.atlas.datagen.rest/README.md) for the HTTP API.

## Configuration Model

The configuration is defined by the `datagen.ecore` model in `org.eclipse.fennec.model.atlas.datagen.model`.

### DataGenConfig (Root)

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `name` | String | required | Name of the configuration |
| `locale` | String | `"de"` | Locale for Datafaker (e.g. `de`, `en`, `fr`) |
| `seed` | int | `0` | Random seed for reproducibility. `0` = random |
| `targetModelNsURIs` | String[] | - | Namespace URIs of target metamodels |
| `classConfigs` | ClassGenConfig[] | - | Per-class generation configurations |

### ClassGenConfig

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `contextClass` | String | required | Name of the target EClass (e.g. `Person`) |
| `instanceCount` | int | `10` | Number of instances to generate |
| `enabled` | boolean | `true` | Whether generation is active |
| `attributeGens` | AttributeGenConfig[] | - | Attribute generation rules |
| `referenceGens` | ReferenceGenConfig[] | - | Reference generation rules |

### AttributeGenConfig

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `featureName` | String | required | Name of the attribute in the EClass |
| `generatorKey` | String | required | Generator key (e.g. `faker.person.firstName`) or Datafaker expression (e.g. `#{Name.first_name}`) |
| `staticValue` | String | - | Fixed value (overrides generatorKey) |
| `template` | String | - | Template with `#{key}` placeholders |
| `unique` | boolean | `false` | Ensure unique values across all instances |

### ReferenceGenConfig

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `featureName` | String | required | Name of the reference in the EClass |
| `strategy` | ReferenceStrategy | `RANDOM` | How to assign targets |
| `targetClassFilter` | String | - | Restrict candidates to a specific class name |
| `minCount` | int | `0` | Minimum number of references |
| `maxCount` | int | `1` | Maximum number of references |

### ReferenceStrategy

| Strategy | Description |
|----------|-------------|
| `RANDOM` | Random selection from candidate pool |
| `ROUND_ROBIN` | Cyclic assignment across all instances |
| `FIRST` | Always assign the first candidate |
| `NONE` | Leave reference empty |

## Generator Keys

Generator keys follow the pattern `faker.<category>.<method>` and are mapped to Datafaker expressions. Examples:

| Generator Key | Datafaker Expression |
|---------------|---------------------|
| `faker.person.firstName` | `#{Name.first_name}` |
| `faker.person.lastName` | `#{Name.last_name}` |
| `faker.address.city` | `#{Address.city}` |
| `faker.address.zipCode` | `#{Address.zip_code}` |
| `faker.company.name` | `#{Company.name}` |
| `faker.internet.email` | `#{Internet.email_address}` |
| `faker.phone.number` | `#{PhoneNumber.phone_number}` |
| `faker.finance.iban` | `#{Finance.iban}` |
| `faker.book.title` | `#{Book.title}` |
| `faker.food.dish` | `#{Food.dish}` |
| `faker.job.title` | `#{Job.title}` |
| `faker.space.planet` | `#{Space.planet}` |

Supported categories: Person, Address, Company, Internet, Phone, Lorem, Number, Date, Color, Commerce, Finance, Book, Music, Food, Animal, Aviation, Medical, Currency, Country, Job, Demographic, File, Code, App, Weather, Space, Educator, Computer.

Direct Datafaker expressions are also supported as generator keys (e.g. `#{Address.city}`).

Unknown keys are converted by convention: `faker.superhero.name` becomes `#{Superhero.name}`.

## Generation Process

1. **Phase 1 — Instances & Attributes**: For each enabled `ClassGenConfig`, creates `instanceCount` EObject instances and fills attributes using Datafaker
2. **Phase 2 — References**: Resolves references between generated instances using the configured strategy

### Value Priority

For each attribute, the first matching rule applies:
1. `staticValue` — fixed value, used as-is
2. `template` — template with `#{key}` placeholders resolved via Datafaker
3. `generatorKey` — mapped to Datafaker expression and evaluated

## License

Eclipse Public License 2.0 (EPL-2.0)
