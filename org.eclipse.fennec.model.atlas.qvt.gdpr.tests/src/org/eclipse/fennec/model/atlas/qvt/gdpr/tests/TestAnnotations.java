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
package org.eclipse.fennec.model.atlas.qvt.gdpr.tests;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.Scalar;
import org.osgi.test.common.annotation.Property.Type;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;

/**
 * The deployment these tests assert against: the three registries a transformation's GDPR report
 * needs, a scope over them, and the two stage actions wired in.
 * <p>
 * It is the jena deployment in miniature, and three lines carry the weight:
 * <ul>
 * <li><b>The schema registry declares {@code draft, approved, release} in that order.</b> That list
 * <em>is</em> the visibility ladder - the runtime chains one EPackage registry per stage, each
 * pointing at the next - and it is what the actions derive their search path from. Reorder it and
 * the tests about which stage a review is taken from change meaning.</li>
 * <li><b>{@code stageActionService.target} on the transformations and report registries.</b> That
 * reference defaults to {@code (scope=no-inject)}, so a registry that does not name an action binds
 * none and dispatches nothing at all - the action activates, logs its configuration, and never
 * fires.</li>
 * <li><b>The report registry's stages mirror the schema registry's.</b> A review is filed in the
 * stage of the model it reviewed, so the stage names have to line up for one to be found from the
 * other.</li>
 * </ul>
 */
@RequireEMF
@RequireConfigurationAdmin
public class TestAnnotations extends CommonTestAnnotations {

	public static final String SCOPE_NAME = "qvt-gdpr-test-scope";

	public static final String REPORT_REGISTRY = "gdpr";
	public static final String UNIT_REGISTRY = "transformations";

	public static final String REPORT_NS_URI = "https://org.eclipse/fennec/gdpr-report/1.0.0";
	public static final String COMPILED_NS_URI = "http://www.eclipse.org/fennec/m2x/compiled/1.0";

	public static final String PID_ANALYSER = "QvtGdprFlowStageAction";
	public static final String PID_FINDINGS = "QvtFlowFindingsStageAction";
	public static final String PID_REANALYSIS = "QvtGdprReanalysisStageAction";

	private static final String STAGES = "stages";
	private static final String TRANSITIONS = "workflow.transitions";
	private static final String STORAGE_MAPPINGS = "stage.storage.mappings";

	/** Everything the two actions need, wired the way the jena runtime wires it. */
	@RegistryConfiguration
	@StorageSetup
	@EPackageLuceneIndexSetup
	@SchemaRegistryServiceSetup
	@WithFactoryConfiguration(factoryPid = PID_REGISTRY_SERVICE, name = REPORT_REGISTRY, location = "?", properties = {
			@Property(key = "registry.name", value = REPORT_REGISTRY),
			@Property(key = "registry.type", value = "OTHER"),
			@Property(key = "root.eclass.uri", value = REPORT_NS_URI + "#//GdprReport"),
			@Property(key = "schemaPackage.target", value = "(emf.nsURI=" + REPORT_NS_URI + ")"),
			@Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
			@Property(key = "storageService.target", value = "(storage.type=file)"),
			@Property(key = "storageService.cardinality.minimum", value = "1", scalar = Scalar.Integer),
			// Without the target the action binds to nothing and no diagnostic is ever written.
			// Deliberately no minimum cardinality: the action needs the scope, the scope needs this
			// registry, and pinning the reference closes that ring into a deadlock.
			@Property(key = "stageActionService.target", value = "(|(component.name=" + PID_FINDINGS
					+ ")(component.name=" + PID_REANALYSIS + "))"),
			@Property(key = "registry.target", value = "(registry=main)"),
			@Property(key = STAGES, type = Type.Array, value = {
					"{ \"name\" : \"" + STAGE_DRAFT + "\", \"writable\" : true, \"final\": false}",
					"{ \"name\" : \"" + STAGE_APPROVED + "\", \"writable\" : true, \"final\": false}",
					"{ \"name\" : \"" + STAGE_RELEASE + "\", \"writable\" : true, \"final\": true}" }),
			@Property(key = TRANSITIONS, type = Type.Array, value = { STAGE_DRAFT + ":" + STAGE_APPROVED,
					STAGE_APPROVED + ":" + STAGE_RELEASE }),
			@Property(key = STORAGE_MAPPINGS, type = Type.Array, value = { STAGE_DRAFT + ":file",
					STAGE_APPROVED + ":file", STAGE_RELEASE + ":file" }) })
	@WithFactoryConfiguration(factoryPid = PID_REGISTRY_SERVICE, name = UNIT_REGISTRY, location = "?", properties = {
			@Property(key = "registry.name", value = UNIT_REGISTRY),
			@Property(key = "registry.type", value = "TRANSFORMATION"),
			@Property(key = "root.eclass.uri", value = COMPILED_NS_URI + "#//CompiledUnit"),
			@Property(key = "schemaPackage.target", value = "(emf.nsURI=" + COMPILED_NS_URI + ")"),
			@Property(key = "resourceSet.target", value = "(emf.name=compiled)"),
			@Property(key = "storageService.target", value = "(storage.type=file)"),
			@Property(key = "storageService.cardinality.minimum", value = "1", scalar = Scalar.Integer),
			@Property(key = "stageActionService.target", value = "(component.name=" + PID_ANALYSER + ")"),
			@Property(key = "registry.target", value = "(registry=main)"),
			@Property(key = STAGES, type = Type.Array, value = {
					"{ \"name\" : \"" + STAGE_DRAFT + "\", \"writable\" : true, \"final\": false}",
					"{ \"name\" : \"" + STAGE_APPROVED + "\", \"writable\" : true, \"final\": false}",
					"{ \"name\" : \"" + STAGE_RELEASE + "\", \"writable\" : true, \"final\": true}" }),
			@Property(key = TRANSITIONS, type = Type.Array, value = { STAGE_DRAFT + ":" + STAGE_APPROVED,
					STAGE_APPROVED + ":" + STAGE_RELEASE }),
			@Property(key = STORAGE_MAPPINGS, type = Type.Array, value = { STAGE_DRAFT + ":file",
					STAGE_APPROVED + ":file", STAGE_RELEASE + ":file" }) })
	@WithFactoryConfiguration(factoryPid = PID_ANALYSER, name = "analyser", location = "?", properties = {
			@Property(key = "report.registry", value = REPORT_REGISTRY),
			@Property(key = "trigger.stages", type = Type.Array, value = { STAGE_DRAFT, STAGE_APPROVED,
					STAGE_RELEASE }),
			@Property(key = "trigger.scopes", type = Type.Array, value = { SCOPE_NAME }),
			// No stage list: where a metamodel may be is derived from the schema registry's
			// declared stages, which is the ladder the runtime chained.
			@Property(key = "scope.target", value = "(atlas.scope=" + SCOPE_NAME + ")") })
	@WithFactoryConfiguration(factoryPid = PID_FINDINGS, name = "findings", location = "?", properties = {
			@Property(key = "target.registry", value = SCHEMA_REGISTRY_NAME),
			@Property(key = "report.stages", type = Type.Array, value = { STAGE_DRAFT, STAGE_APPROVED,
					STAGE_RELEASE }),
			@Property(key = "trigger.scopes", type = Type.Array, value = { SCOPE_NAME }),
			@Property(key = "scope.target", value = "(atlas.scope=" + SCOPE_NAME + ")") })
	@WithFactoryConfiguration(factoryPid = PID_REANALYSIS, name = "reanalysis", location = "?", properties = {
			@Property(key = "unit.registry", value = UNIT_REGISTRY),
			@Property(key = "report.stages", type = Type.Array, value = { STAGE_DRAFT, STAGE_APPROVED,
					STAGE_RELEASE }),
			@Property(key = "trigger.scopes", type = Type.Array, value = { SCOPE_NAME }),
			@Property(key = "scope.target", value = "(atlas.scope=" + SCOPE_NAME + ")") })
	@WithFactoryConfiguration(factoryPid = PID_SCOPE_SERVICE, name = SCOPE_NAME, location = "?", properties = {
			@Property(key = "atlas.scope", value = SCOPE_NAME),
			@Property(key = "scope.name", value = SCOPE_NAME),
			@Property(key = "registryService.target", value = "(|(registry.name=" + SCHEMA_REGISTRY_NAME
					+ ")(registry.name=" + REPORT_REGISTRY + ")(registry.name=" + UNIT_REGISTRY + "))"),
			@Property(key = "registryService.cardinality.minimum", value = "3", scalar = Scalar.Integer) })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface QvtGdprSetup {
	}

	public static final String PID_SCOPE_SERVICE = "ScopeService";
}
