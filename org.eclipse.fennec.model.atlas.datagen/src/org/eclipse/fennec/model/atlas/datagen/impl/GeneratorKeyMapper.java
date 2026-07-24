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

import java.util.Map;

/**
 * Maps datagen generatorKey format (e.g. "faker.person.firstName")
 * to Datafaker expression format (e.g. "#{Name.first_name}").
 *
 * Resolution order:
 * 1. Direct expression pass-through (starts with "#{")
 * 2. Explicit map lookup by generatorKey
 * 3. Lucene fuzzy search using featureName + EClass context
 * 4. Convention-based fallback
 */
public class GeneratorKeyMapper {

	private static final Map<String, String> KEY_TO_EXPRESSION = Map.ofEntries(
			// Person / Name (Datafaker class: Name)
			Map.entry("faker.person.firstName", "#{Name.first_name}"),
			Map.entry("faker.person.lastName", "#{Name.last_name}"),
			Map.entry("faker.person.fullName", "#{Name.name}"),
			Map.entry("faker.person.prefix", "#{Name.prefix}"),
			Map.entry("faker.person.suffix", "#{Name.suffix}"),
			Map.entry("faker.person.title", "#{Name.title}"),
			Map.entry("faker.person.username", "#{Name.username}"),
			Map.entry("faker.person.jobTitle", "#{Job.title}"),
			// Address
			Map.entry("faker.address.street", "#{Address.street_address}"),
			Map.entry("faker.address.streetName", "#{Address.street_name}"),
			Map.entry("faker.address.city", "#{Address.city}"),
			Map.entry("faker.address.cityName", "#{Address.city_name}"),
			Map.entry("faker.address.zipCode", "#{Address.zip_code}"),
			Map.entry("faker.address.postcode", "#{Address.zip_code}"),
			Map.entry("faker.address.country", "#{Address.country}"),
			Map.entry("faker.address.countryCode", "#{Address.country_code}"),
			Map.entry("faker.address.state", "#{Address.state}"),
			Map.entry("faker.address.stateAbbr", "#{Address.state_abbr}"),
			Map.entry("faker.address.buildingNumber", "#{Address.building_number}"),
			Map.entry("faker.address.fullAddress", "#{Address.full_address}"),
			Map.entry("faker.address.latitude", "#{Address.latitude}"),
			Map.entry("faker.address.longitude", "#{Address.longitude}"),
			// Company
			Map.entry("faker.company.name", "#{Company.name}"),
			Map.entry("faker.company.industry", "#{Company.industry}"),
			Map.entry("faker.company.buzzword", "#{Company.buzzword}"),
			Map.entry("faker.company.catchPhrase", "#{Company.catch_phrase}"),
			Map.entry("faker.company.bs", "#{Company.bs}"),
			Map.entry("faker.company.url", "#{Company.url}"),
			Map.entry("faker.company.logo", "#{Company.logo}"),
			// Internet
			Map.entry("faker.internet.email", "#{Internet.email_address}"),
			Map.entry("faker.internet.safeEmail", "#{Internet.safe_email_address}"),
			Map.entry("faker.internet.url", "#{Internet.url}"),
			Map.entry("faker.internet.domainName", "#{Internet.domain_name}"),
			Map.entry("faker.internet.domainSuffix", "#{Internet.domain_suffix}"),
			Map.entry("faker.internet.ipAddress", "#{Internet.ip_v4_address}"),
			Map.entry("faker.internet.ipV6Address", "#{Internet.ip_v6_address}"),
			Map.entry("faker.internet.macAddress", "#{Internet.mac_address}"),
			Map.entry("faker.internet.username", "#{Internet.username}"),
			Map.entry("faker.internet.password", "#{Internet.password}"),
			Map.entry("faker.internet.uuid", "#{Internet.uuid}"),
			Map.entry("faker.internet.userAgent", "#{Internet.user_agent}"),
			// Phone (Datafaker class: PhoneNumber)
			Map.entry("faker.phone.number", "#{PhoneNumber.phone_number}"),
			Map.entry("faker.phone.cellPhone", "#{PhoneNumber.cell_phone}"),
			// Lorem
			Map.entry("faker.lorem.word", "#{Lorem.word}"),
			Map.entry("faker.lorem.sentence", "#{Lorem.sentence}"),
			Map.entry("faker.lorem.paragraph", "#{Lorem.paragraph}"),
			Map.entry("faker.lorem.characters", "#{Lorem.characters}"),
			// Number
			Map.entry("faker.number.digit", "#{Number.digit}"),
			Map.entry("faker.number.randomDigit", "#{Number.random_digit}"),
			// Date (Datafaker class: DateAndTime)
			Map.entry("faker.date.past", "#{DateAndTime.past}"),
			Map.entry("faker.date.future", "#{DateAndTime.future}"),
			Map.entry("faker.date.birthday", "#{DateAndTime.birthday}"),
			// Color
			Map.entry("faker.color.name", "#{Color.name}"),
			Map.entry("faker.color.hex", "#{Color.hex}"),
			// Commerce
			Map.entry("faker.commerce.productName", "#{Commerce.product_name}"),
			Map.entry("faker.commerce.price", "#{Commerce.price}"),
			Map.entry("faker.commerce.department", "#{Commerce.department}"),
			Map.entry("faker.commerce.material", "#{Commerce.material}"),
			Map.entry("faker.commerce.promotionCode", "#{Commerce.promotion_code}"),
			// Finance
			Map.entry("faker.finance.creditCard", "#{Finance.credit_card}"),
			Map.entry("faker.finance.iban", "#{Finance.iban}"),
			Map.entry("faker.finance.bic", "#{Finance.bic}"),
			// Book
			Map.entry("faker.book.title", "#{Book.title}"),
			Map.entry("faker.book.author", "#{Book.author}"),
			Map.entry("faker.book.publisher", "#{Book.publisher}"),
			Map.entry("faker.book.genre", "#{Book.genre}"),
			// Music
			Map.entry("faker.music.instrument", "#{Music.instrument}"),
			Map.entry("faker.music.genre", "#{Music.genre}"),
			// Food
			Map.entry("faker.food.ingredient", "#{Food.ingredient}"),
			Map.entry("faker.food.dish", "#{Food.dish}"),
			Map.entry("faker.food.fruit", "#{Food.fruit}"),
			Map.entry("faker.food.vegetable", "#{Food.vegetable}"),
			Map.entry("faker.food.spice", "#{Food.spice}"),
			// Animal
			Map.entry("faker.animal.name", "#{Animal.name}"),
			Map.entry("faker.animal.species", "#{Animal.species}"),
			Map.entry("faker.animal.genus", "#{Animal.genus}"),
			// Aviation
			Map.entry("faker.aviation.airport", "#{Aviation.airport}"),
			Map.entry("faker.aviation.aircraft", "#{Aviation.aircraft}"),
			Map.entry("faker.aviation.iata", "#{Aviation.IATA}"),
			// Medical
			Map.entry("faker.medical.medicineName", "#{Medical.medicine_name}"),
			Map.entry("faker.medical.diseaseName", "#{Medical.disease_name}"),
			Map.entry("faker.medical.hospitalName", "#{Medical.hospital_name}"),
			// Currency
			Map.entry("faker.currency.name", "#{Currency.name}"),
			Map.entry("faker.currency.code", "#{Currency.code}"),
			// Country
			Map.entry("faker.country.name", "#{Country.name}"),
			Map.entry("faker.country.capital", "#{Country.capital}"),
			Map.entry("faker.country.flag", "#{Country.flag}"),
			Map.entry("faker.country.code2", "#{Country.country_code2}"),
			Map.entry("faker.country.code3", "#{Country.country_code3}"),
			// Job
			Map.entry("faker.job.title", "#{Job.title}"),
			Map.entry("faker.job.position", "#{Job.position}"),
			Map.entry("faker.job.field", "#{Job.field}"),
			Map.entry("faker.job.keySkill", "#{Job.key_skill}"),
			// Demographic
			Map.entry("faker.demographic.race", "#{Demographic.race}"),
			Map.entry("faker.demographic.sex", "#{Demographic.sex}"),
			Map.entry("faker.demographic.maritalStatus", "#{Demographic.marital_status}"),
			// File
			Map.entry("faker.file.fileName", "#{File.file_name}"),
			Map.entry("faker.file.extension", "#{File.extension}"),
			Map.entry("faker.file.mimeType", "#{File.mime_type}"),
			// Code (barcodes, ISBNs)
			Map.entry("faker.code.isbn10", "#{Code.isbn10}"),
			Map.entry("faker.code.isbn13", "#{Code.isbn13}"),
			Map.entry("faker.code.ean8", "#{Code.ean8}"),
			Map.entry("faker.code.ean13", "#{Code.ean13}"),
			// App
			Map.entry("faker.app.name", "#{App.name}"),
			Map.entry("faker.app.version", "#{App.version}"),
			Map.entry("faker.app.author", "#{App.author}"),
			// Weather
			Map.entry("faker.weather.description", "#{Weather.description}"),
			Map.entry("faker.weather.temperatureCelsius", "#{Weather.temperature_celsius}"),
			// Space
			Map.entry("faker.space.planet", "#{Space.planet}"),
			Map.entry("faker.space.galaxy", "#{Space.galaxy}"),
			Map.entry("faker.space.constellation", "#{Space.constellation}"),
			Map.entry("faker.space.star", "#{Space.star}"),
			Map.entry("faker.space.nebula", "#{Space.nebula}"),
			// Education
			Map.entry("faker.educator.university", "#{Educator.university}"),
			Map.entry("faker.educator.course", "#{Educator.course}"),
			Map.entry("faker.educator.campus", "#{Educator.campus}"),
			// Device / Computer
			Map.entry("faker.computer.platform", "#{Computer.platform}"),
			Map.entry("faker.computer.operatingSystem", "#{Computer.operating_system}"),
			Map.entry("faker.computer.type", "#{Computer.type}")
	);

	private static final ExpressionIndex EXPRESSION_INDEX = new ExpressionIndex(KEY_TO_EXPRESSION);

	/**
	 * Maps a generatorKey to a Datafaker expression.
	 * If the key starts with "#{", it is already an expression and returned as-is.
	 * If no mapping is found, the key is converted to expression format by convention.
	 *
	 * @param generatorKey the generator key (e.g. "faker.person.firstName")
	 * @return the Datafaker expression (e.g. "#{Name.first_name}")
	 */
	public static String toExpression(String generatorKey) {
		if (generatorKey == null || generatorKey.isBlank()) {
			throw new IllegalArgumentException("generatorKey must not be null or blank");
		}
		// Already an expression
		if (generatorKey.startsWith("#{")) {
			return generatorKey;
		}
		// Known mapping
		String expression = KEY_TO_EXPRESSION.get(generatorKey);
		if (expression != null) {
			return expression;
		}
		// Convention-based fallback: faker.category.method -> #{Category.method}
		return convertByConvention(generatorKey);
	}

	/**
	 * Resolves a Datafaker expression using the feature name and EClass context.
	 * Used as fallback when no explicit generatorKey is configured.
	 *
	 * Resolution order:
	 * 1. Lucene fuzzy search with featureName + eClassName context
	 * 2. Returns null if no match found (caller should handle)
	 *
	 * @param featureName the EMF attribute name (e.g. "jobTitle", "firstName")
	 * @param eClassName the containing EClass name (e.g. "Person", "CompanyPerson")
	 * @return the matched Datafaker expression, or null if no match
	 */
	public static String resolveByFeature(String featureName, String eClassName) {
		return EXPRESSION_INDEX.findExpression(featureName, eClassName);
	}

	/**
	 * Returns the expression index (for testing).
	 */
	static ExpressionIndex getExpressionIndex() {
		return EXPRESSION_INDEX;
	}

	/**
	 * Converts a dot-separated key to Datafaker expression format.
	 * Example: "faker.address.cityName" -> "#{Address.city_name}"
	 */
	static String convertByConvention(String key) {
		String working = key;
		if (working.startsWith("faker.")) {
			working = working.substring("faker.".length());
		}
		int dot = working.indexOf('.');
		if (dot < 0) {
			return "#{" + capitalize(working) + "}";
		}
		String category = capitalize(working.substring(0, dot));
		String method = toSnakeCase(working.substring(dot + 1));
		return "#{" + category + "." + method + "}";
	}

	private static String capitalize(String s) {
		if (s.isEmpty()) return s;
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	private static String toSnakeCase(String camelCase) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < camelCase.length(); i++) {
			char c = camelCase.charAt(i);
			if (Character.isUpperCase(c) && i > 0) {
				sb.append('_');
			}
			sb.append(Character.toLowerCase(c));
		}
		return sb.toString();
	}
}
