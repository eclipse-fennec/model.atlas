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

import org.eclipse.fennec.model.atlas.datagen.DataGenService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceStrategy;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import net.datafaker.Faker;

/**
 * OSGi DS component that generates EObject instances based on a {@link DataGenConfig}
 * and target EPackages. Uses Datafaker expressions for attribute value generation.
 * Prototype-scoped so each consumer gets its own instance.
 */
@Component(scope = ServiceScope.PROTOTYPE)
public class DataGenServiceImpl implements DataGenService {

	/**
	 * Generates EObject instances based on the given configuration and target packages.
	 *
	 * @param config the data generation configuration
	 * @param targetPackages the EPackages containing the target EClasses
	 * @return map of EClass name to list of generated EObject instances
	 */
	public Map<String, List<EObject>> generate(DataGenConfig config, List<EPackage> targetPackages) {
		Locale locale = Locale.forLanguageTag(config.getLocale() != null ? config.getLocale() : "de");
		Random random = config.getSeed() != 0 ? new Random(config.getSeed()) : new Random();
		Faker faker = new Faker(locale, random);

		Map<String, EClass> classLookup = buildClassLookup(targetPackages);
		Map<String, List<EObject>> result = new HashMap<>();

		// Phase 1: Generate all instances with attributes
		for (ClassGenConfig classConfig : config.getClassConfigs()) {
			if (!classConfig.isEnabled()) {
				continue;
			}
			String className = classConfig.getContextClass();
			EClass eClass = classLookup.get(className);
			if (eClass == null) {
				throw new IllegalArgumentException("EClass not found: " + className);
			}

			List<EObject> instances = new ArrayList<>();
			EPackage ePackage = eClass.getEPackage();
			Map<String, Set<String>> uniqueValues = new HashMap<>();

			for (int i = 0; i < classConfig.getInstanceCount(); i++) {
				EObject instance = ePackage.getEFactoryInstance().create(eClass);
				fillAttributes(instance, eClass, classConfig, faker, i, uniqueValues);
				instances.add(instance);
			}
			result.put(className, instances);
		}

		// Phase 2: Resolve references (all instances exist now)
		for (ClassGenConfig classConfig : config.getClassConfigs()) {
			if (!classConfig.isEnabled()) {
				continue;
			}
			String className = classConfig.getContextClass();
			List<EObject> instances = result.get(className);
			EClass eClass = classLookup.get(className);
			if (instances == null || eClass == null) {
				continue;
			}
			AtomicInteger roundRobinCounter = new AtomicInteger(0);
			for (EObject instance : instances) {
				fillReferences(instance, eClass, classConfig, result, random, roundRobinCounter);
			}
		}

		return result;
	}

	/**
	 * Convenience method that returns all generated instances as a flat list.
	 */
	public List<EObject> generateFlat(DataGenConfig config, List<EPackage> targetPackages) {
		Map<String, List<EObject>> result = generate(config, targetPackages);
		List<EObject> flat = new ArrayList<>();
		result.values().forEach(flat::addAll);
		return flat;
	}

	private void fillAttributes(EObject instance, EClass eClass, ClassGenConfig classConfig,
			Faker faker, int index, Map<String, Set<String>> uniqueValues) {
		for (AttributeGenConfig attrConfig : classConfig.getAttributeGens()) {
			EStructuralFeature feature = eClass.getEStructuralFeature(attrConfig.getFeatureName());
			if (!(feature instanceof EAttribute eAttr)) {
				continue;
			}
			Set<String> usedForFeature = attrConfig.isUnique()
					? uniqueValues.computeIfAbsent(attrConfig.getFeatureName(), k -> new HashSet<>())
					: null;
			Object value = generateAttributeValue(eAttr, attrConfig, eClass, faker, index, usedForFeature);
			if (value != null) {
				instance.eSet(eAttr, value);
			}
		}
	}

	private Object generateAttributeValue(EAttribute eAttr, AttributeGenConfig attrConfig,
			EClass eClass, Faker faker, int index, Set<String> usedForFeature) {
		// Static value takes precedence
		if (attrConfig.getStaticValue() != null && !attrConfig.getStaticValue().isBlank()) {
			return convertToType(attrConfig.getStaticValue(), eAttr.getEAttributeType());
		}

		// Template with placeholders
		if (attrConfig.getTemplate() != null && !attrConfig.getTemplate().isBlank()) {
			String resolved = resolveTemplate(attrConfig.getTemplate(), faker);
			return convertToType(resolved, eAttr.getEAttributeType());
		}

		// Generator key -> Datafaker expression
		String expression = resolveExpression(attrConfig, eAttr, eClass);
		int maxAttempts = attrConfig.isUnique() ? 1000 : 1;

		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			String rawValue = faker.expression(expression);
			if (usedForFeature == null || usedForFeature.add(rawValue)) {
				return convertToType(rawValue, eAttr.getEAttributeType());
			}
		}
		throw new IllegalStateException("Could not generate unique value for "
				+ attrConfig.getFeatureName() + " after 1000 attempts");
	}

	/**
	 * Resolves the Datafaker expression for an attribute config.
	 * If a generatorKey is set, uses direct mapping. Otherwise falls back
	 * to Lucene fuzzy search using the feature name and EClass context.
	 */
	private String resolveExpression(AttributeGenConfig attrConfig, EAttribute eAttr, EClass eClass) {
		String generatorKey = attrConfig.getGeneratorKey();
		if (generatorKey != null && !generatorKey.isBlank()) {
			return GeneratorKeyMapper.toExpression(generatorKey);
		}
		// No generatorKey set — try Lucene fuzzy match using feature name + EClass context
		String fuzzyMatch = GeneratorKeyMapper.resolveByFeature(eAttr.getName(), eClass.getName());
		if (fuzzyMatch != null) {
			return fuzzyMatch;
		}
		throw new IllegalArgumentException("No generatorKey configured and no fuzzy match found for feature '"
				+ eAttr.getName() + "' on EClass '" + eClass.getName() + "'");
	}

	/**
	 * Resolves a template string by replacing #{key} placeholders with Datafaker expressions.
	 * Placeholders can be either generatorKeys (e.g. #{faker.person.firstName})
	 * or direct Datafaker expressions (e.g. #{Name.first_name}).
	 */
	String resolveTemplate(String template, Faker faker) {
		StringBuilder sb = new StringBuilder();
		int pos = 0;
		while (pos < template.length()) {
			int start = template.indexOf("#{", pos);
			if (start < 0) {
				sb.append(template, pos, template.length());
				break;
			}
			sb.append(template, pos, start);
			int end = template.indexOf("}", start);
			if (end < 0) {
				sb.append(template, start, template.length());
				break;
			}
			String key = template.substring(start + 2, end);
			// If it looks like a generatorKey (starts with faker.), map it
			String expression;
			if (key.startsWith("faker.")) {
				expression = GeneratorKeyMapper.toExpression(key);
			} else {
				// Already a Datafaker expression key
				expression = "#{" + key + "}";
			}
			sb.append(faker.expression(expression));
			pos = end + 1;
		}
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	private void fillReferences(EObject instance, EClass eClass, ClassGenConfig classConfig,
			Map<String, List<EObject>> allInstances, Random random, AtomicInteger roundRobinCounter) {
		for (ReferenceGenConfig refConfig : classConfig.getReferenceGens()) {
			EStructuralFeature feature = eClass.getEStructuralFeature(refConfig.getFeatureName());
			if (!(feature instanceof EReference eRef)) {
				continue;
			}
			if (refConfig.getStrategy() == ReferenceStrategy.NONE) {
				continue;
			}

			// Find candidate instances
			List<EObject> candidates = findCandidates(eRef, refConfig, allInstances);
			if (candidates.isEmpty()) {
				continue;
			}

			int count = determineReferenceCount(refConfig, candidates.size(), random);
			List<EObject> selected = selectTargets(candidates, count, refConfig.getStrategy(),
					random, roundRobinCounter);

			if (eRef.isMany()) {
				List<EObject> refList = (List<EObject>) instance.eGet(eRef);
				refList.addAll(selected);
			} else if (!selected.isEmpty()) {
				instance.eSet(eRef, selected.get(0));
			}
		}
	}

	private List<EObject> findCandidates(EReference eRef, ReferenceGenConfig refConfig,
			Map<String, List<EObject>> allInstances) {
		EClass targetType = eRef.getEReferenceType();
		List<EObject> candidates = new ArrayList<>();

		for (Map.Entry<String, List<EObject>> entry : allInstances.entrySet()) {
			// Apply class filter if specified
			if (refConfig.getTargetClassFilter() != null && !refConfig.getTargetClassFilter().isBlank()) {
				if (!entry.getKey().equals(refConfig.getTargetClassFilter())) {
					continue;
				}
			}
			for (EObject obj : entry.getValue()) {
				if (targetType.isSuperTypeOf(obj.eClass())) {
					candidates.add(obj);
				}
			}
		}
		return candidates;
	}

	private int determineReferenceCount(ReferenceGenConfig refConfig, int availableCount, Random random) {
		int min = Math.max(0, refConfig.getMinCount());
		int max = Math.min(availableCount, Math.max(min, refConfig.getMaxCount()));
		if (min == max) {
			return min;
		}
		return min + random.nextInt(max - min + 1);
	}

	private List<EObject> selectTargets(List<EObject> candidates, int count,
			ReferenceStrategy strategy, Random random, AtomicInteger roundRobinCounter) {
		List<EObject> selected = new ArrayList<>();
		for (int i = 0; i < count && i < candidates.size(); i++) {
			switch (strategy) {
			case RANDOM:
				EObject randomPick = candidates.get(random.nextInt(candidates.size()));
				if (!selected.contains(randomPick)) {
					selected.add(randomPick);
				}
				break;
			case ROUND_ROBIN:
				int idx = roundRobinCounter.getAndIncrement() % candidates.size();
				selected.add(candidates.get(idx));
				break;
			case FIRST:
				if (selected.isEmpty()) {
					selected.add(candidates.get(0));
				}
				return selected;
			default:
				break;
			}
		}
		return selected;
	}

	/**
	 * Converts a string value to the appropriate Java type based on the EDataType.
	 */
	static Object convertToType(String value, EDataType eDataType) {
		if (value == null) {
			return null;
		}
		return EcoreUtil.createFromString(eDataType, value);
	}

	private Map<String, EClass> buildClassLookup(List<EPackage> packages) {
		Map<String, EClass> lookup = new HashMap<>();
		for (EPackage pkg : packages) {
			buildClassLookupRecursive(pkg, pkg, "", lookup);
		}
		return lookup;
	}

	private void buildClassLookupRecursive(EPackage rootPkg, EPackage currentPkg,
			String pathPrefix, Map<String, EClass> lookup) {
		for (EClassifier classifier : currentPkg.getEClassifiers()) {
			if (classifier instanceof EClass eClass) {
				String uri = rootPkg.getNsURI() + "#//" + pathPrefix + eClass.getName();
				lookup.put(uri, eClass);
			}
		}
		for (EPackage sub : currentPkg.getESubpackages()) {
			buildClassLookupRecursive(rootPkg, sub,
					pathPrefix + sub.getName() + "/", lookup);
		}
	}
}
