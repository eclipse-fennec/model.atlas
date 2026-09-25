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
package org.eclipse.fennec.model.atlas.gdpr.history.tests;

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
 * The deployment these tests assert against, in one annotation: a registry holding the reviews, a
 * registry holding the derived documents, a scope over both, and the stage action wired between
 * them.
 * <p>
 * It is the jena deployment in miniature, and two of its lines are there because the live runtime
 * taught us they have to be:
 * <ul>
 * <li><b>{@code stageActionService.target} on the reports registry.</b> That reference defaults to
 * {@code (scope=no-inject)}, so a registry that does not name an action binds none and dispatches
 * nothing at all - the action activates, logs, and never fires.</li>
 * <li><b>The document EClass is declared {@code derived}.</b> A document lives in the stage its
 * reviews were carried out at, which may be the registry's final stage, and a final stage refuses
 * updates - so without that declaration the first document is accepted and every rebuild of it is
 * rejected.</li>
 * </ul>
 */
@RequireEMF
@RequireConfigurationAdmin
public class TestAnnotations extends CommonTestAnnotations {

	public static final String SCOPE_NAME = "gdpr-test-scope";

	public static final String REPORT_REGISTRY = "gdpr";
	public static final String DOCUMENT_REGISTRY = "gdprdoc";

	public static final String REPORT_NS_URI = "https://org.eclipse/fennec/gdpr-report/1.0.0";
	public static final String HISTORY_NS_URI = "https://org.eclipse/fennec/gdpr-report-history/1.0.0";

	public static final String PID_HISTORY_ACTION = "GDPRReportHistoryStageAction";

	/** Everything the document needs, wired the way the jena runtime wires it. */
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
			// Without the target the action binds to nothing and no document is ever written. There
			// is deliberately NO minimum cardinality: the action needs the scope service, the scope
			// needs this registry, and pinning the reference closes that ring into a deadlock where
			// none of the three activates. The registry therefore comes up without the action and
			// binds it when it appears - and because the action replays on startup, a report stored
			// in the meantime is picked up by that bind rather than lost.
			@Property(key = "stageActionService.target", value = "(component.name=" + PID_HISTORY_ACTION + ")"),
			@Property(key = "registry.target", value = "(registry=main)"),
			@Property(key = "stages", type = Type.Array, value = {
					"{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
					"{ \"name\" : \"release\", \"writable\" : true, \"final\": true}" }),
			@Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
			@Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:file", "release:file" }) })
	@WithFactoryConfiguration(factoryPid = PID_REGISTRY_SERVICE, name = DOCUMENT_REGISTRY, location = "?", properties = {
			@Property(key = "registry.name", value = DOCUMENT_REGISTRY),
			@Property(key = "registry.type", value = "OTHER"),
			@Property(key = "root.eclass.uri", value = HISTORY_NS_URI + "#//GdprReportHistory"),
			// Derived: the Atlas builds these documents itself, so the trusted service API may
			// rewrite one even in a final stage. A document lives in the stage its reviews were
			// carried out at, and that stage may well be the final one.
			@Property(key = "derived.eclass.uri", value = HISTORY_NS_URI + "#//GdprReportHistory"),
			@Property(key = "schemaPackage.target", value = "(emf.nsURI=" + HISTORY_NS_URI + ")"),
			@Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
			@Property(key = "storageService.target", value = "(storage.type=file)"),
			@Property(key = "storageService.cardinality.minimum", value = "1", scalar = Scalar.Integer),
			@Property(key = "registry.target", value = "(registry=main)"),
			@Property(key = "stages", type = Type.Array, value = {
					"{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
					"{ \"name\" : \"release\", \"writable\" : true, \"final\": true}" }),
			@Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
			@Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:file", "release:file" }) })
	@WithConfiguration(pid = PID_HISTORY_ACTION, location = "?", properties = {
			@Property(key = "reports.registry", value = REPORT_REGISTRY),
			@Property(key = "report.stages", type = Type.Array, value = { "draft", "release" }),
			@Property(key = "trigger.scopes", type = Type.Array, value = { SCOPE_NAME }),
			@Property(key = "scope.target", value = "(atlas.scope=" + SCOPE_NAME + ")"),
			@Property(key = "document.registry", value = DOCUMENT_REGISTRY) })
	@WithFactoryConfiguration(factoryPid = PID_SCOPE_SERVICE, name = SCOPE_NAME, location = "?", properties = {
			@Property(key = "atlas.scope", value = SCOPE_NAME),
			@Property(key = "scope.name", value = SCOPE_NAME),
			@Property(key = "registryService.target", value = "(|(registry.name=" + SCHEMA_REGISTRY_NAME
					+ ")(registry.name=" + REPORT_REGISTRY + ")(registry.name=" + DOCUMENT_REGISTRY + "))"),
			@Property(key = "registryService.cardinality.minimum", value = "3", scalar = Scalar.Integer) })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface GdprHistorySetup {
	}

	public static final String PID_SCOPE_SERVICE = "ScopeService";
}
