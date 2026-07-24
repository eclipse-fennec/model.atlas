/*
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.datagen.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests the Lucene-based fuzzy expression matching with various EClass name patterns.
 * Uses compound/creative class names to verify disambiguation works beyond simple cases.
 */
class ExpressionIndexTest {

	private static ExpressionIndex index;

	@BeforeAll
	static void setUp() {
		index = GeneratorKeyMapper.getExpressionIndex();
	}

	// --- Feature name "name" disambiguated by EClass context ---

	@Test
	void testNameOnCompanyPerson_isAmbiguous() {
		// "CompanyPerson" contains both "Company" and "Person" — genuinely ambiguous.
		// Both Company.name and Name.name are valid matches. The index picks based on
		// term overlap — "Company" directly matches the company category.
		String result = index.findExpression("name", "CompanyPerson");
		assertNotNull(result, "Should find a match for 'name' on CompanyPerson");
		assertTrue(result.equals("#{Company.name}") || result.equals("#{Name.name}"),
				"Ambiguous case should match either Company.name or Name.name, got: " + result);
	}

	@Test
	void testNameOnCompany() {
		String result = index.findExpression("name", "Company");
		assertNotNull(result);
		assertEquals("#{Company.name}", result);
	}

	@Test
	void testNameOnAnimalProfile() {
		String result = index.findExpression("name", "AnimalProfile");
		assertNotNull(result);
		assertEquals("#{Animal.name}", result);
	}

	@Test
	void testNameOnCountryInfo() {
		String result = index.findExpression("name", "CountryInfo");
		assertNotNull(result);
		assertEquals("#{Country.name}", result);
	}

	@Test
	void testNameOnCurrencyRecord() {
		String result = index.findExpression("name", "CurrencyRecord");
		assertNotNull(result);
		assertEquals("#{Currency.name}", result);
	}

	// --- Feature name "title" disambiguated by EClass context ---

	@Test
	void testTitleOnHREmployee() {
		// "HREmployee" — "HR" is an acronym not in the index, "Employee" isn't a Faker
		// category. Without clear context, any title-related expression is acceptable.
		String result = index.findExpression("title", "HREmployee");
		assertNotNull(result);
		assertTrue(result.contains(".title}"),
				"Should match a title expression, got: " + result);
	}

	@Test
	void testTitleOnJobApplicant() {
		// "JobApplicant" — "Job" directly matches the job category
		String result = index.findExpression("title", "JobApplicant");
		assertNotNull(result);
		assertEquals("#{Job.title}", result);
	}

	@Test
	void testTitleOnBookCatalogEntry() {
		String result = index.findExpression("title", "BookCatalogEntry");
		assertNotNull(result);
		assertEquals("#{Book.title}", result);
	}

	@Test
	void testTitleOnPerson() {
		// On a plain Person, "title" could be Name.title (Mr/Mrs) or Job.title
		// Name.title is more person-specific
		String result = index.findExpression("title", "Person");
		assertNotNull(result);
		assertEquals("#{Name.title}", result);
	}

	// --- Direct feature name matches (no ambiguity) ---

	@Test
	void testFirstNameOnCompanyPerson() {
		String result = index.findExpression("firstName", "CompanyPerson");
		assertNotNull(result);
		assertEquals("#{Name.first_name}", result);
	}

	@Test
	void testLastNameOnHREmployee() {
		String result = index.findExpression("lastName", "HREmployee");
		assertNotNull(result);
		assertEquals("#{Name.last_name}", result);
	}

	@Test
	void testCityOnShippingAddress() {
		String result = index.findExpression("city", "ShippingAddress");
		assertNotNull(result);
		assertEquals("#{Address.city}", result);
	}

	@Test
	void testZipCodeOnBillingAddress() {
		String result = index.findExpression("zipCode", "BillingAddress");
		assertNotNull(result);
		assertEquals("#{Address.zip_code}", result);
	}

	@Test
	void testEmailOnCustomerAccount() {
		String result = index.findExpression("email", "CustomerAccount");
		assertNotNull(result);
		assertEquals("#{Internet.email_address}", result);
	}

	@Test
	void testJobTitleOnPerson() {
		// The original bug scenario — "jobTitle" should find Job.title
		String result = index.findExpression("jobTitle", "Person");
		assertNotNull(result);
		assertEquals("#{Job.title}", result);
	}

	@Test
	void testJobTitleOnCompanyEmployee() {
		String result = index.findExpression("jobTitle", "CompanyEmployee");
		assertNotNull(result);
		assertEquals("#{Job.title}", result);
	}

	// --- Compound EClass names with clear category signal ---

	@Test
	void testInstrumentOnMusicEvent() {
		String result = index.findExpression("instrument", "MusicEvent");
		assertNotNull(result);
		assertEquals("#{Music.instrument}", result);
	}

	@Test
	void testPlanetOnSpaceExploration() {
		String result = index.findExpression("planet", "SpaceExploration");
		assertNotNull(result);
		assertEquals("#{Space.planet}", result);
	}

	@Test
	void testIngredientOnFoodRecipe() {
		String result = index.findExpression("ingredient", "FoodRecipe");
		assertNotNull(result);
		assertEquals("#{Food.ingredient}", result);
	}

	@Test
	void testIbanOnBankAccount() {
		String result = index.findExpression("iban", "BankAccount");
		assertNotNull(result);
		assertEquals("#{Finance.iban}", result);
	}

	// --- No match scenarios ---

	@Test
	void testCompletelyUnknownFeature() {
		String result = index.findExpression("xyzzyFooBarBaz", "SomeClass");
		assertNull(result, "Completely unknown feature should return null");
	}

	// --- splitCamelCase utility ---

	@Test
	void testSplitCamelCase() {
		assertEquals("job Title", ExpressionIndex.splitCamelCase("jobTitle"));
		assertEquals("first Name", ExpressionIndex.splitCamelCase("firstName"));
		assertEquals("Company Person", ExpressionIndex.splitCamelCase("CompanyPerson"));
		assertEquals("HR Employee", ExpressionIndex.splitCamelCase("HREmployee"));
		assertEquals("XML Parser", ExpressionIndex.splitCamelCase("XMLParser"));
		assertEquals("name", ExpressionIndex.splitCamelCase("name"));
	}
}
