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
package org.eclipse.fennec.model.atlas.gdpr.diagnostics.tests;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.Scalar;
import org.osgi.test.common.annotation.Property.Type;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;

/**
 * The deployment these tests assert against: a schema registry holding the reviewed packages, a
 * registry holding the reviews, a scope over both, and the stage action wired between them.
 * <p>
 * It is the jena deployment in miniature. Two lines carry the weight:
 * <ul>
 * <li><b>{@code stageActionService.target} on the report registry.</b> That reference defaults to
 * {@code (scope=no-inject)}, so a registry that does not name the action binds none and dispatches
 * nothing at all - the action activates, logs its configuration, and never fires.</li>
 * <li><b>The report registry's stages mirror the schema registry's.</b> That is the invariant the
 * action's addressing rests on: it writes to the stage the report is in, so a report about an
 * {@code approved} package has to be in {@code approved} too.</li>
 * </ul>
 */
@RequireEMF
@RequireConfigurationAdmin
public class TestAnnotations extends CommonTestAnnotations {

	public static final String SCOPE_NAME = "gdpr-diagnostics-test-scope";

	public static final String REPORT_REGISTRY = "gdpr";

	public static final String REPORT_NS_URI = "https://org.eclipse/fennec/gdpr-report/1.0.0";

	public static final String PID_DIAGNOSTICS_ACTION = "GDPRMetadataDiagnosticsStageAction";

	/** Everything the action needs, wired the way the jena runtime wires it. */
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
			// There is deliberately NO minimum cardinality: the action needs the scope service,
			// the scope needs this registry, and pinning the reference closes that ring into a
			// deadlock where none of the three activates.
			@Property(key = "stageActionService.target", value = "(component.name=" + PID_DIAGNOSTICS_ACTION + ")"),
			@Property(key = "registry.target", value = "(registry=main)"),
			// The same stages the schema registry has, because a review is about the stage it was
			// carried out at and the action writes to that same stage.
			@Property(key = "stages", type = Type.Array, value = {
					"{ \"name\" : \"" + STAGE_DRAFT + "\", \"writable\" : true, \"final\": false}",
					"{ \"name\" : \"" + STAGE_APPROVED + "\", \"writable\" : true, \"final\": false}",
					"{ \"name\" : \"" + STAGE_RELEASE + "\", \"writable\" : true, \"final\": true}" }),
			@Property(key = "workflow.transitions", type = Type.Array, value = { STAGE_DRAFT + ":" + STAGE_APPROVED,
					STAGE_APPROVED + ":" + STAGE_RELEASE }),
			@Property(key = "stage.storage.mappings", type = Type.Array, value = { STAGE_DRAFT + ":file",
					STAGE_APPROVED + ":file", STAGE_RELEASE + ":file" }) })
	@WithConfiguration(pid = PID_DIAGNOSTICS_ACTION, location = "?", properties = {
			@Property(key = "target.registry", value = SCHEMA_REGISTRY_NAME),
			@Property(key = "report.stages", type = Type.Array, value = { STAGE_DRAFT, STAGE_APPROVED,
					STAGE_RELEASE }),
			@Property(key = "trigger.scopes", type = Type.Array, value = { SCOPE_NAME }),
			@Property(key = "scope.target", value = "(atlas.scope=" + SCOPE_NAME + ")") })
	@WithFactoryConfiguration(factoryPid = PID_SCOPE_SERVICE, name = SCOPE_NAME, location = "?", properties = {
			@Property(key = "atlas.scope", value = SCOPE_NAME),
			@Property(key = "scope.name", value = SCOPE_NAME),
			@Property(key = "registryService.target", value = "(|(registry.name=" + SCHEMA_REGISTRY_NAME
					+ ")(registry.name=" + REPORT_REGISTRY + "))"),
			@Property(key = "registryService.cardinality.minimum", value = "2", scalar = Scalar.Integer) })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface GdprDiagnosticsSetup {
	}

	public static final String PID_SCOPE_SERVICE = "ScopeService";
}
