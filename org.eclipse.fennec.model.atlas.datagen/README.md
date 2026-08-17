# DataGen Service

OSGi service for generating fake EMF EObject instances based on a `DataGenConfig` and target EPackages. Uses [Datafaker](https://www.datafaker.net/) expressions for realistic attribute value generation.

## Overview

The DataGen Service takes a configuration model (`DataGenConfig`) that describes which EClasses to instantiate, how many instances to create, and how to populate attributes and references. It generates dynamic EObject instances at runtime without requiring generated Java code for the target models.

## Architecture

```
DataGenService (Interface)
  └── DataGenServiceImpl (@Component, scope=PROTOTYPE)
        └── GeneratorKeyMapper (internal)
              └── ExpressionIndex (Lucene fuzzy search)
```

- **`DataGenService`** — exported API interface
- **`DataGenServiceImpl`** — prototype-scoped OSGi DS component
- **`GeneratorKeyMapper`** — maps `faker.*` keys to Datafaker `#{...}` expressions
- **`ExpressionIndex`** — in-memory Lucene index for fuzzy expression matching by feature name

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
| `customGenerators` | CustomGeneratorDef[] | - | Config-local generator definitions (see below) |

### CustomGeneratorDef

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `key` | String | required | The key an `generatorKey` or a `#{key}` template placeholder refers to |
| `label` | String | required | Display name of the generator |
| `expression` | String | required | Datafaker expression, e.g. `Dr. #{Name.first_name}` |
| `category` | String | - | Optional grouping for UIs |

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
| `generatorKey` | String | optional | Generator key (e.g. `faker.person.firstName`) or Datafaker expression (e.g. `#{Name.first_name}`). If omitted, the feature name is used for automatic fuzzy matching (see below). |
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

## Expression Resolution

When a `generatorKey` is provided, it is resolved through the explicit mapping or convention-based fallback as described above. When no `generatorKey` is set, the service uses **Lucene-based fuzzy matching** to automatically find the best Datafaker expression.

### Resolution Order

1. **`generatorKey` set** — the config's own `customGenerators` first, then the direct map lookup, then the convention-based fallback (`faker.category.method` → `#{Category.method}`)
2. **`generatorKey` not set** — Lucene fuzzy search using the `featureName` and the containing EClass name as context
3. **No match found** — error with a descriptive message

### Custom Generators

`customGenerators` lets a config define its own generator keys without touching `GeneratorKeyMapper`. Each definition maps a `key` to a Datafaker `expression`; the expression may combine several placeholders and literal text.

```xml
<customGenerators key="custom.saluted" label="Salutation + first name"
                  expression="Dr. #{Name.first_name}"/>
```

The key is then usable both as a `generatorKey` and as a `#{...}` template placeholder:

```xml
<attributeGens featureName="firstName" generatorKey="custom.saluted"/>
<attributeGens featureName="label" template="#{custom.saluted}, #{Address.city}"/>
```

Custom generators are resolved **before** the built-in mapping, so a definition may also override a built-in key such as `faker.person.firstName`. A definition without a `key` or without an `expression` is rejected with an `IllegalArgumentException`; a repeated key is replaced by the later definition.

### Automatic Fuzzy Matching

The `ExpressionIndex` builds an in-memory Lucene index from all known generator key mappings at startup. When no `generatorKey` is configured for an attribute, the feature name is used as the primary search term and the EClass name provides disambiguation context.

**How it works:**
- The feature name (e.g. `jobTitle`) is split into terms (`job`, `title`) and matched via `FuzzyQuery` (MUST)
- The EClass name (e.g. `CompanyEmployee`) is split into terms (`Company`, `Employee`) and used as boost (SHOULD)
- The best-scoring Datafaker expression is returned

**Examples:**

| EClass | Feature Name | Resolved Expression | Why |
|--------|-------------|-------------------|-----|
| `Person` | `firstName` | `#{Name.first_name}` | "first" + "name" match directly |
| `Person` | `jobTitle` | `#{Job.title}` | "job" + "title" match Job.title |
| `ShippingAddress` | `city` | `#{Address.city}` | "city" matches, "Address" in class name boosts |
| `AnimalProfile` | `name` | `#{Animal.name}` | "Animal" in class name disambiguates |
| `Company` | `name` | `#{Company.name}` | "Company" matches company category |
| `BookCatalogEntry` | `title` | `#{Book.title}` | "Book" in class name disambiguates |
| `JobApplicant` | `title` | `#{Job.title}` | "Job" in class name disambiguates |
| `SpaceExploration` | `planet` | `#{Space.planet}` | "Space" + "planet" match directly |

**Limitations:** For genuinely ambiguous cases (e.g. feature `name` on EClass `CompanyPerson`, or `title` without a clear category signal in the class name), the fuzzy match may not pick the intended expression. In these cases, use an explicit `generatorKey` for deterministic results.

## Generation Process

1. **Phase 1 — Instances & Attributes**: For each enabled `ClassGenConfig`, creates `instanceCount` EObject instances and fills attributes using Datafaker
2. **Phase 2 — References**: Resolves references between generated instances using the configured strategy

### Value Priority

For each attribute, the first matching rule applies:
1. `staticValue` — fixed value, used as-is
2. `template` — template with `#{key}` placeholders resolved via custom generators, then Datafaker
3. `generatorKey` — resolved via custom generators, then the built-in mapping, and evaluated
4. **Fuzzy fallback** — feature name + EClass name resolved via Lucene index

## License

Eclipse Public License 2.0 (EPL-2.0)
