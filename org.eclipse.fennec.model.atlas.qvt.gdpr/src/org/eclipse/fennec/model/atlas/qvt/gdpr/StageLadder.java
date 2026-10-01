/**
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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.qvt.gdpr;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.fennec.model.atlas.scope.api.RegistryInfo;
import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;
import org.eclipse.fennec.model.atlas.scope.api.ScopeRegistries;
import org.eclipse.fennec.model.atlas.scope.api.StageInfo;

/**
 * Which stages a metamodel of a transformation may be in, seen from the stage the transformation
 * itself is in.
 *
 * <p>
 * This is not a policy and it is not configured: it is a reading of the ladder the runtime already
 * built. {@code SchemaRegistryChainConfigurator} creates one EPackage registry per stage of the
 * schema registry and chains them in declared order - <em>stage[i] points at stage[i+1]</em>, and
 * the last points at the parent scope. A unit compiled in stage {@code S} therefore resolved its
 * metamodels from {@code S} and from every stage declared after it, and from nowhere else.
 * </p>
 *
 * <pre>
 * declared: [draft, approved, release]
 *
 * draft    → approved → release → parent scope
 * approved → release  → parent scope
 * release  → parent scope
 * </pre>
 *
 * <p>
 * So visibility runs one way, and that direction is the point: work in progress may rest on
 * released models, released work must never rest on drafts. Deriving the search path from the same
 * list the chain is built from is what keeps the two from drifting - a configured list would be a
 * second statement of the same fact, and the failure of a second statement is that it can be wrong.
 * A search path wider than the ladder would let a draft review speak for a released transformation;
 * one narrower would report a metamodel as unreviewed when its review is exactly where the compiler
 * found the model.
 * </p>
 */
final class StageLadder {

	private StageLadder() {
	}

	/**
	 * The stages to look in, nearest first.
	 *
	 * @param scope the scope, whose schema registry declares the ladder; {@code null} answers with
	 *              the stage alone
	 * @param stage the stage the transformation - or the report about it - is in
	 * @return {@code stage} and every schema stage declared after it, in that order. Never empty
	 */
	static List<String> visibleFrom(ScopeInfo scope, String stage) {
		List<String> declared = schemaStagesOf(scope);
		int position = declared.indexOf(stage);
		if (position >= 0) {
			return List.copyOf(declared.subList(position, declared.size()));
		}
		if (declared.isEmpty()) {
			// No schema registry in this scope, so there is no ladder to climb and the only place
			// a model could be is where the event is.
			return List.of(stage);
		}
		// A stage no schema registry declares - the configurator serves it too, chaining it onto
		// the tail of the schema chain, so that tail is exactly one stage long.
		return List.of(stage, declared.get(declared.size() - 1));
	}

	/**
	 * The same, narrowed to the stages one registry actually has.
	 * <p>
	 * The ladder is a property of the <em>schema</em> registry, but it is searched against others -
	 * the registry holding the reviews, the registry holding the models - and a registry declares
	 * its own stages. Asking one for a stage it does not have is not an empty answer but an
	 * {@code IllegalArgumentException}, so a deployment whose report registry declares fewer stages
	 * than its schema registry would fail every event rather than simply find less.
	 *
	 * @param scope        the scope; {@code null} answers with the stage alone
	 * @param registryName the registry the stages will be asked of; unknown to the scope means no
	 *                     narrowing, because guessing it has nothing would silently find nothing
	 * @param stage        the stage the event came from
	 * @return the visible stages that {@code registryName} has, nearest first. May be empty, which
	 *         says that registry holds nothing anywhere the transformation could see
	 */
	static List<String> visibleIn(ScopeInfo scope, String registryName, String stage) {
		return narrow(visibleFrom(scope, stage), scope, registryName);
	}

	/**
	 * The other direction: every stage from which {@code stage} is visible, including itself.
	 * <p>
	 * It answers "who was allowed to read this?", which is what a change to something has to ask
	 * before it can know who is now out of date. A review landing in {@code approved} makes stale
	 * the transformations in {@code draft} and in {@code approved}, and none in {@code release} -
	 * the exact inverse of the ladder, and deliberately computed <em>from</em>
	 * {@link #visibleFrom} rather than restated, because a second statement of one rule is a second
	 * thing that can be wrong.
	 *
	 * @param scope the scope; {@code null} answers with the stage alone
	 * @param stage the stage something changed in
	 * @return the stages that can see it, in declared order. Never empty
	 */
	static List<String> stagesSeeing(ScopeInfo scope, String stage) {
		List<String> candidates = allStagesOf(scope);
		if (candidates.isEmpty()) {
			return List.of(stage);
		}
		List<String> seeing = new ArrayList<>();
		for (String candidate : candidates) {
			if (visibleFrom(scope, candidate).contains(stage)) {
				seeing.add(candidate);
			}
		}
		return seeing.isEmpty() ? List.of(stage) : List.copyOf(seeing);
	}

	/** {@link #stagesSeeing}, narrowed to the stages one registry has. */
	static List<String> stagesSeeingIn(ScopeInfo scope, String registryName, String stage) {
		return narrow(stagesSeeing(scope, stage), scope, registryName);
	}

	private static List<String> narrow(List<String> stages, ScopeInfo scope, String registryName) {
		List<String> declared = stagesOf(scope, registryName);
		if (declared.isEmpty()) {
			return stages;
		}
		return stages.stream().filter(declared::contains).toList();
	}

	/**
	 * Every stage any registry of the scope declares: the schema registry's ladder first, in
	 * declared order, then the ones only other registries have, sorted so two runs agree.
	 */
	private static List<String> allStagesOf(ScopeInfo scope) {
		if (scope == null) {
			return List.of();
		}
		List<String> all = new ArrayList<>(schemaStagesOf(scope));
		List<String> others = new ArrayList<>();
		for (RegistryInfo registry : scope.getRegistries()) {
			for (String name : namesOf(registry)) {
				if (!all.contains(name) && !others.contains(name)) {
					others.add(name);
				}
			}
		}
		others.sort(null);
		all.addAll(others);
		return all;
	}

	/** The stages of the scope's schema registry, in declared order: the ladder itself. */
	private static List<String> schemaStagesOf(ScopeInfo scope) {
		return namesOf(scope == null ? null : ScopeRegistries.schemaRegistry(scope).orElse(null));
	}

	/** The stages one named registry of the scope declares, in declared order. */
	private static List<String> stagesOf(ScopeInfo scope, String registryName) {
		if (scope == null || registryName == null) {
			return List.of();
		}
		return namesOf(scope.getRegistries().stream().filter(r -> registryName.equals(r.getName())).findFirst()
				.orElse(null));
	}

	private static List<String> namesOf(RegistryInfo registry) {
		if (registry == null) {
			return List.of();
		}
		List<String> names = new ArrayList<>();
		for (StageInfo stage : registry.getStages()) {
			if (stage.getName() != null && !stage.getName().isBlank()) {
				names.add(stage.getName());
			}
		}
		return names;
	}
}
