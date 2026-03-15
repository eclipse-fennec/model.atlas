package org.eclipse.fennec.model.atlas.datagen.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenFactory;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataGenServiceTest {

	private DataGenServiceImpl service;
	private EPackage testPackage;
	private EClass personClass;
	private EClass addressClass;

	@BeforeEach
	void setUp() {
		service = new DataGenServiceImpl();
		testPackage = createTestPackage();
		personClass = (EClass) testPackage.getEClassifier("Person");
		addressClass = (EClass) testPackage.getEClassifier("Address");
	}

	@Test
	void testGenerateSimpleInstances() {
		DataGenConfig config = createSimpleConfig();

		Map<String, List<EObject>> result = service.generate(config, List.of(testPackage));

		assertTrue(result.containsKey("Person"));
		assertEquals(5, result.get("Person").size());

		EObject firstPerson = result.get("Person").get(0);
		assertNotNull(firstPerson.eGet(personClass.getEStructuralFeature("firstName")));
		assertNotNull(firstPerson.eGet(personClass.getEStructuralFeature("lastName")));
	}

	@Test
	void testReproducibleWithSeed() {
		DataGenConfig config = createSimpleConfig();
		config.setSeed(42);

		Map<String, List<EObject>> result1 = service.generate(config, List.of(testPackage));
		Map<String, List<EObject>> result2 = service.generate(config, List.of(testPackage));

		EObject person1 = result1.get("Person").get(0);
		EObject person2 = result2.get("Person").get(0);

		EAttribute firstNameAttr = (EAttribute) personClass.getEStructuralFeature("firstName");
		assertEquals(person1.eGet(firstNameAttr), person2.eGet(firstNameAttr));
	}

	@Test
	void testStaticValue() {
		DataGenConfig config = DatagenFactory.eINSTANCE.createDataGenConfig();
		config.setName("test");
		config.setLocale("de");

		ClassGenConfig classConfig = DatagenFactory.eINSTANCE.createClassGenConfig();
		classConfig.setContextClass("Person");
		classConfig.setInstanceCount(3);

		AttributeGenConfig attrConfig = DatagenFactory.eINSTANCE.createAttributeGenConfig();
		attrConfig.setFeatureName("firstName");
		attrConfig.setGeneratorKey("faker.person.firstName");
		attrConfig.setStaticValue("FixedName");
		classConfig.getAttributeGens().add(attrConfig);

		config.getClassConfigs().add(classConfig);

		Map<String, List<EObject>> result = service.generate(config, List.of(testPackage));

		for (EObject person : result.get("Person")) {
			assertEquals("FixedName", person.eGet(personClass.getEStructuralFeature("firstName")));
		}
	}

	@Test
	void testTemplate() {
		DataGenConfig config = DatagenFactory.eINSTANCE.createDataGenConfig();
		config.setName("test");
		config.setLocale("en");
		config.setSeed(42);

		ClassGenConfig classConfig = DatagenFactory.eINSTANCE.createClassGenConfig();
		classConfig.setContextClass("Person");
		classConfig.setInstanceCount(1);

		AttributeGenConfig attrConfig = DatagenFactory.eINSTANCE.createAttributeGenConfig();
		attrConfig.setFeatureName("firstName");
		attrConfig.setGeneratorKey("faker.person.firstName");
		attrConfig.setTemplate("#{Name.first_name} #{Name.last_name}");
		classConfig.getAttributeGens().add(attrConfig);

		config.getClassConfigs().add(classConfig);

		Map<String, List<EObject>> result = service.generate(config, List.of(testPackage));
		String value = (String) result.get("Person").get(0).eGet(personClass.getEStructuralFeature("firstName"));
		assertNotNull(value);
		assertTrue(value.contains(" "), "Template should produce a value with a space: " + value);
	}

	@Test
	void testUniqueValues() {
		DataGenConfig config = DatagenFactory.eINSTANCE.createDataGenConfig();
		config.setName("test");
		config.setLocale("en");
		config.setSeed(123);

		ClassGenConfig classConfig = DatagenFactory.eINSTANCE.createClassGenConfig();
		classConfig.setContextClass("Person");
		classConfig.setInstanceCount(10);

		AttributeGenConfig attrConfig = DatagenFactory.eINSTANCE.createAttributeGenConfig();
		attrConfig.setFeatureName("firstName");
		attrConfig.setGeneratorKey("faker.person.firstName");
		attrConfig.setUnique(true);
		classConfig.getAttributeGens().add(attrConfig);

		config.getClassConfigs().add(classConfig);

		Map<String, List<EObject>> result = service.generate(config, List.of(testPackage));
		Set<Object> values = new HashSet<>();
		for (EObject person : result.get("Person")) {
			Object val = person.eGet(personClass.getEStructuralFeature("firstName"));
			assertTrue(values.add(val), "Duplicate value found: " + val);
		}
	}

	@Test
	void testReferenceRandom() {
		DataGenConfig config = createConfigWithReferences(ReferenceStrategy.RANDOM);

		Map<String, List<EObject>> result = service.generate(config, List.of(testPackage));

		assertEquals(3, result.get("Person").size());
		assertEquals(5, result.get("Address").size());

		EReference addressRef = (EReference) personClass.getEStructuralFeature("address");
		for (EObject person : result.get("Person")) {
			EObject addr = (EObject) person.eGet(addressRef);
			assertNotNull(addr, "Person should have an address assigned");
		}
	}

	@Test
	void testReferenceNone() {
		DataGenConfig config = createConfigWithReferences(ReferenceStrategy.NONE);

		Map<String, List<EObject>> result = service.generate(config, List.of(testPackage));

		EReference addressRef = (EReference) personClass.getEStructuralFeature("address");
		for (EObject person : result.get("Person")) {
			Object addr = person.eGet(addressRef);
			assertTrue(addr == null, "Person should not have an address with NONE strategy");
		}
	}

	@Test
	void testDisabledClassConfig() {
		DataGenConfig config = createSimpleConfig();
		config.getClassConfigs().get(0).setEnabled(false);

		Map<String, List<EObject>> result = service.generate(config, List.of(testPackage));

		assertFalse(result.containsKey("Person"));
	}

	@Test
	void testUnknownClassThrows() {
		DataGenConfig config = DatagenFactory.eINSTANCE.createDataGenConfig();
		config.setName("test");

		ClassGenConfig classConfig = DatagenFactory.eINSTANCE.createClassGenConfig();
		classConfig.setContextClass("NonExistentClass");
		classConfig.setInstanceCount(1);
		config.getClassConfigs().add(classConfig);

		assertThrows(IllegalArgumentException.class,
				() -> service.generate(config, List.of(testPackage)));
	}

	@Test
	void testGenerateFlat() {
		DataGenConfig config = createConfigWithReferences(ReferenceStrategy.NONE);

		List<EObject> flat = service.generateFlat(config, List.of(testPackage));

		assertEquals(8, flat.size()); // 3 Person + 5 Address
	}

	@Test
	void testDirectDatafakerExpression() {
		DataGenConfig config = DatagenFactory.eINSTANCE.createDataGenConfig();
		config.setName("test");
		config.setLocale("en");
		config.setSeed(42);

		ClassGenConfig classConfig = DatagenFactory.eINSTANCE.createClassGenConfig();
		classConfig.setContextClass("Address");
		classConfig.setInstanceCount(1);

		AttributeGenConfig attrConfig = DatagenFactory.eINSTANCE.createAttributeGenConfig();
		attrConfig.setFeatureName("city");
		attrConfig.setGeneratorKey("#{Address.city}");
		classConfig.getAttributeGens().add(attrConfig);

		config.getClassConfigs().add(classConfig);

		Map<String, List<EObject>> result = service.generate(config, List.of(testPackage));
		String city = (String) result.get("Address").get(0).eGet(addressClass.getEStructuralFeature("city"));
		assertNotNull(city);
		assertFalse(city.isBlank());
	}

	@Test
	void testReferenceRoundRobin() {
		DataGenConfig config = createConfigWithReferences(ReferenceStrategy.ROUND_ROBIN);

		Map<String, List<EObject>> result = service.generate(config, List.of(testPackage));

		EReference addressRef = (EReference) personClass.getEStructuralFeature("address");
		List<EObject> addresses = result.get("Address");
		List<EObject> persons = result.get("Person");

		// Round-robin should cycle through addresses: person0->addr0, person1->addr1, person2->addr2
		for (int i = 0; i < persons.size(); i++) {
			EObject addr = (EObject) persons.get(i).eGet(addressRef);
			assertNotNull(addr, "Person " + i + " should have an address");
			assertEquals(addresses.get(i % addresses.size()), addr,
					"Round-robin should assign address " + (i % addresses.size()) + " to person " + i);
		}
	}

	@Test
	void testReferenceFirst() {
		DataGenConfig config = createConfigWithReferences(ReferenceStrategy.FIRST);

		Map<String, List<EObject>> result = service.generate(config, List.of(testPackage));

		EReference addressRef = (EReference) personClass.getEStructuralFeature("address");
		EObject firstAddress = result.get("Address").get(0);

		for (EObject person : result.get("Person")) {
			EObject addr = (EObject) person.eGet(addressRef);
			assertNotNull(addr, "Person should have an address with FIRST strategy");
			assertEquals(firstAddress, addr, "FIRST strategy should always assign the first address");
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	void testManyReference() {
		DataGenConfig config = DatagenFactory.eINSTANCE.createDataGenConfig();
		config.setName("many-ref-test");
		config.setLocale("en");
		config.setSeed(42);

		ClassGenConfig addressConfig = DatagenFactory.eINSTANCE.createClassGenConfig();
		addressConfig.setContextClass("Address");
		addressConfig.setInstanceCount(5);
		AttributeGenConfig cityGen = DatagenFactory.eINSTANCE.createAttributeGenConfig();
		cityGen.setFeatureName("city");
		cityGen.setGeneratorKey("faker.address.city");
		addressConfig.getAttributeGens().add(cityGen);
		config.getClassConfigs().add(addressConfig);

		ClassGenConfig personConfig = DatagenFactory.eINSTANCE.createClassGenConfig();
		personConfig.setContextClass("Person");
		personConfig.setInstanceCount(2);
		AttributeGenConfig nameGen = DatagenFactory.eINSTANCE.createAttributeGenConfig();
		nameGen.setFeatureName("firstName");
		nameGen.setGeneratorKey("faker.person.firstName");
		personConfig.getAttributeGens().add(nameGen);

		ReferenceGenConfig refConfig = DatagenFactory.eINSTANCE.createReferenceGenConfig();
		refConfig.setFeatureName("addresses"); // many reference
		refConfig.setStrategy(ReferenceStrategy.RANDOM);
		refConfig.setMinCount(2);
		refConfig.setMaxCount(3);
		personConfig.getReferenceGens().add(refConfig);

		config.getClassConfigs().add(personConfig);

		Map<String, List<EObject>> result = service.generate(config, List.of(testPackage));

		EReference addressesRef = (EReference) personClass.getEStructuralFeature("addresses");
		for (EObject person : result.get("Person")) {
			List<EObject> addrs = (List<EObject>) person.eGet(addressesRef);
			assertTrue(addrs.size() >= 2 && addrs.size() <= 3,
					"Many-ref should have 2-3 addresses, got " + addrs.size());
		}
	}

	@Test
	void testTargetClassFilter() {
		DataGenConfig config = DatagenFactory.eINSTANCE.createDataGenConfig();
		config.setName("filter-test");
		config.setLocale("en");
		config.setSeed(42);

		ClassGenConfig addressConfig = DatagenFactory.eINSTANCE.createClassGenConfig();
		addressConfig.setContextClass("Address");
		addressConfig.setInstanceCount(3);
		AttributeGenConfig cityGen = DatagenFactory.eINSTANCE.createAttributeGenConfig();
		cityGen.setFeatureName("city");
		cityGen.setGeneratorKey("faker.address.city");
		addressConfig.getAttributeGens().add(cityGen);
		config.getClassConfigs().add(addressConfig);

		ClassGenConfig personConfig = DatagenFactory.eINSTANCE.createClassGenConfig();
		personConfig.setContextClass("Person");
		personConfig.setInstanceCount(2);

		ReferenceGenConfig refConfig = DatagenFactory.eINSTANCE.createReferenceGenConfig();
		refConfig.setFeatureName("address");
		refConfig.setStrategy(ReferenceStrategy.RANDOM);
		refConfig.setTargetClassFilter("Address");
		refConfig.setMinCount(1);
		refConfig.setMaxCount(1);
		personConfig.getReferenceGens().add(refConfig);

		config.getClassConfigs().add(personConfig);

		Map<String, List<EObject>> result = service.generate(config, List.of(testPackage));

		EReference addressRef = (EReference) personClass.getEStructuralFeature("address");
		for (EObject person : result.get("Person")) {
			EObject addr = (EObject) person.eGet(addressRef);
			assertNotNull(addr, "Should find address with matching filter");
		}
	}

	@Test
	void testTargetClassFilterNoMatch() {
		DataGenConfig config = DatagenFactory.eINSTANCE.createDataGenConfig();
		config.setName("filter-nomatch-test");
		config.setLocale("en");
		config.setSeed(42);

		ClassGenConfig addressConfig = DatagenFactory.eINSTANCE.createClassGenConfig();
		addressConfig.setContextClass("Address");
		addressConfig.setInstanceCount(3);
		config.getClassConfigs().add(addressConfig);

		ClassGenConfig personConfig = DatagenFactory.eINSTANCE.createClassGenConfig();
		personConfig.setContextClass("Person");
		personConfig.setInstanceCount(2);

		ReferenceGenConfig refConfig = DatagenFactory.eINSTANCE.createReferenceGenConfig();
		refConfig.setFeatureName("address");
		refConfig.setStrategy(ReferenceStrategy.RANDOM);
		refConfig.setTargetClassFilter("NonExistent");
		refConfig.setMinCount(1);
		refConfig.setMaxCount(1);
		personConfig.getReferenceGens().add(refConfig);

		config.getClassConfigs().add(personConfig);

		Map<String, List<EObject>> result = service.generate(config, List.of(testPackage));

		EReference addressRef = (EReference) personClass.getEStructuralFeature("address");
		for (EObject person : result.get("Person")) {
			Object addr = person.eGet(addressRef);
			assertTrue(addr == null, "Non-matching filter should leave reference empty");
		}
	}

	@Test
	void testUniqueValuesAcrossInstances() {
		DataGenConfig config = DatagenFactory.eINSTANCE.createDataGenConfig();
		config.setName("test");
		config.setLocale("en");
		config.setSeed(123);

		ClassGenConfig classConfig = DatagenFactory.eINSTANCE.createClassGenConfig();
		classConfig.setContextClass("Person");
		classConfig.setInstanceCount(20);

		AttributeGenConfig attrConfig = DatagenFactory.eINSTANCE.createAttributeGenConfig();
		attrConfig.setFeatureName("firstName");
		attrConfig.setGeneratorKey("faker.person.firstName");
		attrConfig.setUnique(true);
		classConfig.getAttributeGens().add(attrConfig);

		config.getClassConfigs().add(classConfig);

		Map<String, List<EObject>> result = service.generate(config, List.of(testPackage));

		Set<Object> allValues = new HashSet<>();
		for (EObject person : result.get("Person")) {
			Object val = person.eGet(personClass.getEStructuralFeature("firstName"));
			assertTrue(allValues.add(val), "Duplicate value across instances: " + val);
		}
		assertEquals(20, allValues.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	void testManyReferenceRoundRobin() {
		DataGenConfig config = DatagenFactory.eINSTANCE.createDataGenConfig();
		config.setName("many-rr-test");
		config.setLocale("en");
		config.setSeed(42);

		ClassGenConfig addressConfig = DatagenFactory.eINSTANCE.createClassGenConfig();
		addressConfig.setContextClass("Address");
		addressConfig.setInstanceCount(4);
		config.getClassConfigs().add(addressConfig);

		ClassGenConfig personConfig = DatagenFactory.eINSTANCE.createClassGenConfig();
		personConfig.setContextClass("Person");
		personConfig.setInstanceCount(2);

		ReferenceGenConfig refConfig = DatagenFactory.eINSTANCE.createReferenceGenConfig();
		refConfig.setFeatureName("addresses");
		refConfig.setStrategy(ReferenceStrategy.ROUND_ROBIN);
		refConfig.setMinCount(2);
		refConfig.setMaxCount(2);
		personConfig.getReferenceGens().add(refConfig);

		config.getClassConfigs().add(personConfig);

		Map<String, List<EObject>> result = service.generate(config, List.of(testPackage));

		List<EObject> addresses = result.get("Address");
		List<EObject> persons = result.get("Person");

		// Person 0 should get addr[0], addr[1]; Person 1 should get addr[2], addr[3]
		List<EObject> p0Addrs = (List<EObject>) persons.get(0).eGet(personClass.getEStructuralFeature("addresses"));
		List<EObject> p1Addrs = (List<EObject>) persons.get(1).eGet(personClass.getEStructuralFeature("addresses"));

		assertEquals(2, p0Addrs.size());
		assertEquals(2, p1Addrs.size());
		assertEquals(addresses.get(0), p0Addrs.get(0));
		assertEquals(addresses.get(1), p0Addrs.get(1));
		assertEquals(addresses.get(2), p1Addrs.get(0));
		assertEquals(addresses.get(3), p1Addrs.get(1));
	}

	// --- Helper methods ---

	private DataGenConfig createSimpleConfig() {
		DataGenConfig config = DatagenFactory.eINSTANCE.createDataGenConfig();
		config.setName("test-config");
		config.setLocale("de");
		config.setSeed(42);

		ClassGenConfig classConfig = DatagenFactory.eINSTANCE.createClassGenConfig();
		classConfig.setContextClass("Person");
		classConfig.setInstanceCount(5);

		AttributeGenConfig firstNameGen = DatagenFactory.eINSTANCE.createAttributeGenConfig();
		firstNameGen.setFeatureName("firstName");
		firstNameGen.setGeneratorKey("faker.person.firstName");
		classConfig.getAttributeGens().add(firstNameGen);

		AttributeGenConfig lastNameGen = DatagenFactory.eINSTANCE.createAttributeGenConfig();
		lastNameGen.setFeatureName("lastName");
		lastNameGen.setGeneratorKey("faker.person.lastName");
		classConfig.getAttributeGens().add(lastNameGen);

		config.getClassConfigs().add(classConfig);
		return config;
	}

	private DataGenConfig createConfigWithReferences(ReferenceStrategy strategy) {
		DataGenConfig config = DatagenFactory.eINSTANCE.createDataGenConfig();
		config.setName("ref-test");
		config.setLocale("en");
		config.setSeed(42);

		// Address class config
		ClassGenConfig addressConfig = DatagenFactory.eINSTANCE.createClassGenConfig();
		addressConfig.setContextClass("Address");
		addressConfig.setInstanceCount(5);

		AttributeGenConfig cityGen = DatagenFactory.eINSTANCE.createAttributeGenConfig();
		cityGen.setFeatureName("city");
		cityGen.setGeneratorKey("faker.address.city");
		addressConfig.getAttributeGens().add(cityGen);

		config.getClassConfigs().add(addressConfig);

		// Person class config with reference to Address
		ClassGenConfig personConfig = DatagenFactory.eINSTANCE.createClassGenConfig();
		personConfig.setContextClass("Person");
		personConfig.setInstanceCount(3);

		AttributeGenConfig nameGen = DatagenFactory.eINSTANCE.createAttributeGenConfig();
		nameGen.setFeatureName("firstName");
		nameGen.setGeneratorKey("faker.person.firstName");
		personConfig.getAttributeGens().add(nameGen);

		ReferenceGenConfig refConfig = DatagenFactory.eINSTANCE.createReferenceGenConfig();
		refConfig.setFeatureName("address");
		refConfig.setStrategy(strategy);
		refConfig.setMinCount(1);
		refConfig.setMaxCount(1);
		personConfig.getReferenceGens().add(refConfig);

		config.getClassConfigs().add(personConfig);
		return config;
	}

	/**
	 * Creates a test EPackage with Person and Address classes.
	 */
	private EPackage createTestPackage() {
		EcoreFactory ecoreFactory = EcoreFactory.eINSTANCE;

		EPackage pkg = ecoreFactory.createEPackage();
		pkg.setName("testmodel");
		pkg.setNsPrefix("test");
		pkg.setNsURI("http://test.example.com/testmodel/1.0");

		// Address class
		EClass address = ecoreFactory.createEClass();
		address.setName("Address");

		EAttribute city = ecoreFactory.createEAttribute();
		city.setName("city");
		city.setEType(EcorePackage.Literals.ESTRING);
		address.getEStructuralFeatures().add(city);

		EAttribute zipCode = ecoreFactory.createEAttribute();
		zipCode.setName("zipCode");
		zipCode.setEType(EcorePackage.Literals.ESTRING);
		address.getEStructuralFeatures().add(zipCode);

		pkg.getEClassifiers().add(address);

		// Person class
		EClass person = ecoreFactory.createEClass();
		person.setName("Person");

		EAttribute firstName = ecoreFactory.createEAttribute();
		firstName.setName("firstName");
		firstName.setEType(EcorePackage.Literals.ESTRING);
		person.getEStructuralFeatures().add(firstName);

		EAttribute lastName = ecoreFactory.createEAttribute();
		lastName.setName("lastName");
		lastName.setEType(EcorePackage.Literals.ESTRING);
		person.getEStructuralFeatures().add(lastName);

		EAttribute age = ecoreFactory.createEAttribute();
		age.setName("age");
		age.setEType(EcorePackage.Literals.EINT);
		person.getEStructuralFeatures().add(age);

		EReference addressRef = ecoreFactory.createEReference();
		addressRef.setName("address");
		addressRef.setEType(address);
		person.getEStructuralFeatures().add(addressRef);

		EReference addressesRef = ecoreFactory.createEReference();
		addressesRef.setName("addresses");
		addressesRef.setEType(address);
		addressesRef.setUpperBound(-1); // many reference
		person.getEStructuralFeatures().add(addressesRef);

		pkg.getEClassifiers().add(person);

		return pkg;
	}
}
