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
package org.eclipse.fennec.model.atlas.rest.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestAnnotations;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestHelper;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.Scalar;
import org.osgi.test.common.annotation.Property.Type;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

/**
 * {@code /{scopeName}/schema} is a URL literal, not the name of a registry. A deployment may
 * call its schema registry whatever it likes — here {@code models} — and the endpoint has to
 * serve it all the same (issue #179).
 *
 * <p>
 * Before the fix both requests below answered {@code 400}: <em>Registry [schema] is not
 * available for scope [renamed-schema-scope]</em>.
 * </p>
 *
 * @see <a href="https://github.com/eclipse-fennec/model.atlas/issues/179">issue #179</a>
 */
public class RenamedSchemaRegistryTest extends AbstractRestTest {

	private static final String SCOPE = "renamed-schema-scope";

	/** The scope's schema registry, deliberately not called "schema". */
	private static final String SCHEMA_REGISTRY = "models";

	private static final String NS_URI = "http://test.example.com/renamed/1.0";

	private static final String PACKAGE_NAME = "RenamedSchema";

	@InjectService(cardinality = 0, filter = "(scope.name=" + SCOPE + ")")
	ServiceAware<ScopeService> renamedScopeService;

	@InjectService(cardinality = 0, filter = "(&(scope.name=" + SCOPE + ")(stage.name="
			+ CommonTestAnnotations.STAGE_RELEASE + "))")
	ServiceAware<ResourceSet> renamedScopeResourceSet;

	@WithFactoryConfiguration(factoryPid = CommonTestAnnotations.PID_REGISTRY_SERVICE, name = SCHEMA_REGISTRY, location = "?", properties = {
			@Property(key = "registry.name", value = SCHEMA_REGISTRY),
			@Property(key = "registry.type", value = "SCHEMA"),
			@Property(key = "schema.uri", value = "http://www.eclipse.org/emf/2002/Ecore"),
			@Property(key = "root.eclass.uri", value = "http://www.eclipse.org/emf/2002/Ecore#//EPackage"),
			@Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
			@Property(key = "storageService.target", value = "(storage.type=file)"),
			@Property(key = "registry.target", value = "(registry=main)"),
			@Property(key = "stages", type = Type.Array, value = {
					"{ \"name\" : \"" + CommonTestAnnotations.STAGE_DRAFT + "\", \"writable\" : true, \"final\": false}",
					"{ \"name\" : \"" + CommonTestAnnotations.STAGE_APPROVED + "\", \"writable\" : true, \"final\": false}",
					"{ \"name\" : \"" + CommonTestAnnotations.STAGE_RELEASE + "\", \"writable\" : true, \"final\": true}", }),
			@Property(key = "workflow.transitions", type = Type.Array, value = {
					CommonTestAnnotations.STAGE_DRAFT + ":" + CommonTestAnnotations.STAGE_APPROVED,
					CommonTestAnnotations.STAGE_APPROVED + ":" + CommonTestAnnotations.STAGE_RELEASE }),
			@Property(key = "stage.storage.mappings", type = Type.Array, value = {
					CommonTestAnnotations.STAGE_DRAFT + ":file", CommonTestAnnotations.STAGE_APPROVED + ":file",
					CommonTestAnnotations.STAGE_RELEASE + ":file" }) })
	@Retention(RetentionPolicy.RUNTIME)
	@interface RenamedSchemaRegistrySetup {
	}

	@CommonTestAnnotations.EPackageLuceneIndexSetup
	@CommonTestAnnotations.StorageSetup
	@RenamedSchemaRegistrySetup
	@WithFactoryConfiguration(factoryPid = TestAnnotations.PID_SCOPE_SERVICE, name = SCOPE, location = "?", properties = {
			@Property(key = "atlas.scope", value = SCOPE), @Property(key = "scope.name", value = SCOPE),
			@Property(key = "registryService.target", value = "(registry.name=" + SCHEMA_REGISTRY + ")"),
			@Property(key = "registryService.cardinality.minimum", value = "1", scalar = Scalar.Integer) })
	@Retention(RetentionPolicy.RUNTIME)
	@interface RenamedSchemaScopeSetup {
	}

	@Test
	@RenamedSchemaScopeSetup
	public void listsPackagesThroughTheSchemaPath(@InjectBundleContext BundleContext context) throws Exception {
		awaitScope(context);

		Response response = schemaTarget().path("all").request().get();

		response.bufferEntity();
		assertTrue(
				response.getStatus() == Response.Status.OK.getStatusCode()
						|| response.getStatus() == Response.Status.NO_CONTENT.getStatusCode(),
				"/schema/all must be served by the scope's SCHEMA registry '" + SCHEMA_REGISTRY + "' | status: "
						+ response.getStatus() + " | body: " + response.readEntity(String.class));
	}

	@Test
	@RenamedSchemaScopeSetup
	public void createsAndListsAPackageThroughTheSchemaPath(@InjectBundleContext BundleContext context)
			throws Exception {
		awaitScope(context);

		EPackage testPackage = TestHelper.createTestEPackage(NS_URI, PACKAGE_NAME, PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response created = schemaStageTarget(CommonTestAnnotations.STAGE_DRAFT).queryParam("nsUri", NS_URI)
				.queryParam("name", PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
		assertStatus(Response.Status.CREATED.getStatusCode(), created,
				"uploading a package must reach the scope's SCHEMA registry, whatever its name");
		created.bufferEntity();
		assertTrue(created.readEntity(String.class).contains("registry=\"" + SCHEMA_REGISTRY + "\""),
				"the metadata must record the registry the package actually went to");

		Response listed = schemaStageTarget(CommonTestAnnotations.STAGE_DRAFT).request("application/xmi").get();
		assertStatus(Response.Status.OK.getStatusCode(), listed, "the draft stage must list the uploaded package");
		// The nsUri property travels as a serialized value in XMI, so the listing is checked
		// on the metadata the container spells out plainly.
		String body = listed.readEntity(String.class);
		assertTrue(body.contains("objectName=\"" + PACKAGE_NAME + "\""),
				"the draft listing must contain the uploaded package | body: " + body);
	}

	/** {@code /renamed-schema-scope/schema} */
	private WebTarget schemaTarget() {
		return scopeTarget(SCOPE).path("schema");
	}

	/** {@code /renamed-schema-scope/schema/stages/{stage}} */
	private WebTarget schemaStageTarget(String stage) {
		return schemaTarget().path("stages").path(stage);
	}

	private void awaitScope(BundleContext context) throws InterruptedException {
		ResourceAware resourceAware = ResourceAware.create(context, getResourceName());
		assertTrue(resourceAware.waitForResource(15, TimeUnit.SECONDS),
				getResourceName() + " should be registered within 15 seconds");
		assertNotNull(renamedScopeService.waitForService(TimeUnit.SECONDS.toMillis(15)),
				"ScopeService for '" + SCOPE + "' should be available within 15 seconds");
		assertNotNull(renamedScopeResourceSet.waitForService(TimeUnit.SECONDS.toMillis(15)),
				"ResourceSet for scope '" + SCOPE + "' / stage 'release' should be available within 15 seconds");
	}

	@Override
	String getResourceName() {
		return "SchemaPackagesResource";
	}
}
