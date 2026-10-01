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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.eclipse.fennec.model.atlas.scope.api.RegistryInfo;
import org.eclipse.fennec.model.atlas.scope.api.RegistryType;
import org.eclipse.fennec.model.atlas.scope.api.ScopeApiFactory;
import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;
import org.eclipse.fennec.model.atlas.scope.api.StageInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Where a transformation's metamodels may be, seen from the stage the transformation is in.
 * <p>
 * It has to be the same set the compiler could resolve them from, which is what
 * {@code SchemaRegistryChainConfigurator} builds: one EPackage registry per stage of the schema
 * registry, chained in declared order, stage[i] pointing at stage[i+1] and the last at the parent
 * scope. Wider than that would let a draft review speak for a released transformation; narrower
 * would report a metamodel as unreviewed when its review is exactly where the compiler found the
 * model.
 */
@DisplayName("The stages a transformation's metamodels may be in")
public class StageLadderTest {

	private static final ScopeApiFactory SCOPE = ScopeApiFactory.eINSTANCE;

	@Test
	@DisplayName("an early stage sees itself and everything declared after it")
	public void visibilityRunsForward() {
		ScopeInfo scope = scope("draft", "approved", "release");

		assertEquals(List.of("draft", "approved", "release"), StageLadder.visibleFrom(scope, "draft"),
				"work in progress may rest on released models");
		assertEquals(List.of("approved", "release"), StageLadder.visibleFrom(scope, "approved"));
	}

	@Test
	@DisplayName("the final stage sees only itself, so released work never rests on a draft")
	public void theFinalStageSeesOnlyItself() {
		assertEquals(List.of("release"), StageLadder.visibleFrom(scope("draft", "approved", "release"), "release"),
				"the chain's last stage points at the parent scope, never back down its own ladder");
	}

	@Test
	@DisplayName("the stage names are the deployment's own, in the order it declared them")
	public void nothingIsHardcoded() {
		// A scope may call its stages anything and order them any way; the ladder is whatever the
		// schema registry declared, which is what the chain was built from.
		ScopeInfo scope = scope("wip", "blessed", "bananas");

		assertEquals(List.of("blessed", "bananas"), StageLadder.visibleFrom(scope, "blessed"));
		assertEquals(List.of("bananas"), StageLadder.visibleFrom(scope, "bananas"));
	}

	@Test
	@DisplayName("a stage no schema registry declares chains onto the tail of the ladder")
	public void anInstanceOnlyStageSeesTheLastSchemaStage() {
		// The configurator serves such a stage too, parented on the last schema stage - so that is
		// the whole of what it can see beyond itself.
		assertEquals(List.of("tenant", "release"),
				StageLadder.visibleFrom(scope("draft", "approved", "release"), "tenant"));
	}

	@Test
	@DisplayName("a scope with no schema registry has no ladder to climb")
	public void withoutASchemaRegistryThereIsOnlyTheOwnStage() {
		ScopeInfo scope = SCOPE.createScopeInfo();
		scope.setName("instances-only");
		assertEquals(List.of("draft"), StageLadder.visibleFrom(scope, "draft"));
		assertEquals(List.of("draft"), StageLadder.visibleFrom(null, "draft"),
				"and a scope that is not there at all is not a reason to throw");
	}

	@Test
	@DisplayName("the inverse: who could have read a stage is the prefix up to it")
	public void stagesSeeingIsTheInverseOfTheLadder() {
		ScopeInfo scope = scope("draft", "approved", "release");

		// A review landing in approved makes stale the transformations in draft and in approved,
		// and none in release - which is exactly the set of stages that can see approved.
		assertEquals(List.of("draft", "approved"), StageLadder.stagesSeeing(scope, "approved"));
		assertEquals(List.of("draft"), StageLadder.stagesSeeing(scope, "draft"),
				"only draft can see draft, which is why a draft review never reaches released work");
		assertEquals(List.of("draft", "approved", "release"), StageLadder.stagesSeeing(scope, "release"),
				"every stage can see the last one");
	}

	@Test
	@DisplayName("the two directions agree with each other by construction")
	public void theInverseIsConsistentWithTheLadder() {
		ScopeInfo scope = scope("draft", "approved", "release");
		List<String> all = List.of("draft", "approved", "release");

		for (String seen : all) {
			for (String from : all) {
				assertEquals(StageLadder.visibleFrom(scope, from).contains(seen),
						StageLadder.stagesSeeing(scope, seen).contains(from),
						from + " sees " + seen + " must mean " + seen + " is seen from " + from);
			}
		}
	}

	@Test
	@DisplayName("a stage only another registry declares is one that can see the ladder's tail")
	public void anInstanceOnlyStageIsFoundByTheInverse() {
		ScopeInfo scope = scope("draft", "approved", "release");
		addStage(scope, "sandbox");

		// sandbox chains onto the last schema stage, so a change in release reaches it too.
		assertEquals(List.of("draft", "approved", "release", "sandbox"),
				StageLadder.stagesSeeing(scope, "release"));
		assertEquals(List.of("sandbox"), StageLadder.stagesSeeing(scope, "sandbox"));
		assertEquals(List.of("draft", "approved"), StageLadder.stagesSeeing(scope, "approved"),
				"and a change in approved does not reach it, because it cannot see approved");
	}

	@Test
	@DisplayName("a search is narrowed to the stages the registry being asked actually has")
	public void theSearchIsNarrowedToTheRegistrysOwnStages() {
		ScopeInfo scope = scope("draft", "approved", "release");
		RegistryInfo reports = SCOPE.createRegistryInfo();
		reports.setName("gdpr");
		reports.setType(RegistryType.OTHER);
		for (String name : List.of("draft", "release")) {
			StageInfo stage = SCOPE.createStageInfo();
			stage.setName(name);
			reports.getStages().add(stage);
		}
		scope.getRegistries().add(reports);

		// Asking a registry for a stage it does not declare is an error, not an empty answer, so a
		// report registry with fewer stages has to find less rather than fail.
		assertEquals(List.of("draft", "release"), StageLadder.visibleIn(scope, "gdpr", "draft"));
		assertEquals(List.of("draft"), StageLadder.stagesSeeingIn(scope, "gdpr", "approved"));
		assertEquals(List.of("draft", "approved", "release"), StageLadder.visibleFrom(scope, "draft"),
				"the ladder itself is untouched; only the search over one registry is narrowed");
	}

	private static void addStage(ScopeInfo scope, String name) {
		RegistryInfo other = SCOPE.createRegistryInfo();
		other.setName("transformations");
		other.setType(RegistryType.TRANSFORMATION);
		StageInfo stage = SCOPE.createStageInfo();
		stage.setName(name);
		other.getStages().add(stage);
		scope.getRegistries().add(other);
	}

	private static ScopeInfo scope(String... stages) {
		ScopeInfo scope = SCOPE.createScopeInfo();
		scope.setName("jena");
		RegistryInfo schema = SCOPE.createRegistryInfo();
		schema.setName("schema");
		schema.setType(RegistryType.SCHEMA);
		for (String name : stages) {
			StageInfo stage = SCOPE.createStageInfo();
			stage.setName(name);
			schema.getStages().add(stage);
		}
		scope.getRegistries().add(schema);
		return scope;
	}
}
