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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class GeneratorKeyMapperTest {

	@Test
	void testKnownKeys() {
		assertEquals("#{Name.first_name}", GeneratorKeyMapper.toExpression("faker.person.firstName"));
		assertEquals("#{Name.last_name}", GeneratorKeyMapper.toExpression("faker.person.lastName"));
		assertEquals("#{Address.city}", GeneratorKeyMapper.toExpression("faker.address.city"));
		assertEquals("#{Company.name}", GeneratorKeyMapper.toExpression("faker.company.name"));
		assertEquals("#{Internet.email_address}", GeneratorKeyMapper.toExpression("faker.internet.email"));
		assertEquals("#{PhoneNumber.phone_number}", GeneratorKeyMapper.toExpression("faker.phone.number"));
	}

	@Test
	void testPassthroughExpression() {
		assertEquals("#{Name.first_name}", GeneratorKeyMapper.toExpression("#{Name.first_name}"));
		assertEquals("#{Custom.something}", GeneratorKeyMapper.toExpression("#{Custom.something}"));
	}

	@Test
	void testNewCategories() {
		// Finance
		assertEquals("#{Finance.credit_card}", GeneratorKeyMapper.toExpression("faker.finance.creditCard"));
		assertEquals("#{Finance.iban}", GeneratorKeyMapper.toExpression("faker.finance.iban"));
		// Book
		assertEquals("#{Book.title}", GeneratorKeyMapper.toExpression("faker.book.title"));
		// Food
		assertEquals("#{Food.ingredient}", GeneratorKeyMapper.toExpression("faker.food.ingredient"));
		// Job
		assertEquals("#{Job.title}", GeneratorKeyMapper.toExpression("faker.job.title"));
		assertEquals("#{Job.title}", GeneratorKeyMapper.toExpression("faker.person.jobTitle"));
		// Country
		assertEquals("#{Country.name}", GeneratorKeyMapper.toExpression("faker.country.name"));
		// Space
		assertEquals("#{Space.planet}", GeneratorKeyMapper.toExpression("faker.space.planet"));
		// Code
		assertEquals("#{Code.isbn13}", GeneratorKeyMapper.toExpression("faker.code.isbn13"));
		// Computer
		assertEquals("#{Computer.operating_system}", GeneratorKeyMapper.toExpression("faker.computer.operatingSystem"));
	}

	@Test
	void testConventionFallback() {
		// Unknown key -> convention-based conversion (keys NOT in the map)
		assertEquals("#{Superhero.name}", GeneratorKeyMapper.toExpression("faker.superhero.name"));
		assertEquals("#{Pokemon.name}", GeneratorKeyMapper.toExpression("faker.pokemon.name"));
	}

	@Test
	void testConventionCamelCaseToSnakeCase() {
		assertEquals("#{Some.camel_case_method}", GeneratorKeyMapper.convertByConvention("faker.some.camelCaseMethod"));
	}

	@Test
	void testNullAndBlank() {
		assertThrows(IllegalArgumentException.class, () -> GeneratorKeyMapper.toExpression(null));
		assertThrows(IllegalArgumentException.class, () -> GeneratorKeyMapper.toExpression(""));
		assertThrows(IllegalArgumentException.class, () -> GeneratorKeyMapper.toExpression("   "));
	}
}
