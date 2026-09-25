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
package org.eclipse.fennec.model.atlas.rest.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.action.api.GateContext;
import org.eclipse.fennec.model.atlas.action.api.GateDiagnostic;
import org.eclipse.fennec.model.atlas.action.api.GateVerdict;
import org.eclipse.fennec.model.atlas.action.api.StageGate;
import org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person;
import org.eclipse.fennec.model.atlas.rest.model.RestFactory;
import org.eclipse.fennec.model.atlas.rest.model.StageTransitionRequest;
import org.eclipse.fennec.model.atlas.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestAnnotations;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestHelper;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.Scalar;
import org.osgi.test.common.annotation.Property.Type;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.Promises;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

/**
 * REST contract of a stage gate's refusal (issue #295): a refused transition and a refused
 * delete answer {@code 409 Conflict} whose body is the object's metadata from its current
 * stage, with the gate's findings as {@code diagnostics}; {@code ?force=true} deletes past
 * the veto. The object registry of this test has its own gate, registered by the test.
 */
@SuppressWarnings("rawtypes")
public class GateRefusalRestTest extends AbstractRestTest {

	private static final String SCOPE = "gate-scope";
	private static final String REGISTRY = "gated-persons";
	private static final String GATE_PROPERTY = "atlas.test.gate";
	private static final String GATE_ID = "rest-gate-it";
	private static final String OBJECT_ID = "gated-person";
	private static final String TRANSITION_CODE = "no-go";
	private static final String DELETE_CODE = "still-needed";

	@InjectService(cardinality = 0, filter = "(scope.name=" + SCOPE + ")")
	ServiceAware<ScopeService> gateScopeService;

	@InjectService(cardinality = 0, filter = "(&(scope.name=" + SCOPE + ")(stage.name="
			+ CommonTestAnnotations.STAGE_RELEASE + "))")
	ServiceAware<ResourceSet> gateScopeResourceSet;

	private final List<ServiceRegistration<StageGate>> registrations = new CopyOnWriteArrayList<>();

	@AfterEach
	void unregisterGates() {
		registrations.forEach(ServiceRegistration::unregister);
		registrations.clear();
	}

	/** An object registry like the standard one, but guarded by the gate this test registers. */
	@WithFactoryConfiguration(factoryPid = CommonTestAnnotations.PID_REGISTRY_SERVICE, name = REGISTRY, location = "?", properties = {
			@Property(key = "registry.name", value = REGISTRY),
			@Property(key = "registry.type", value = "OBJECT"),
			@Property(key = "root.eclass.uri", value = "https://dg.de/1.0#//Person"),
			@Property(key = "schemaPackage.target", value = "(emf.nsURI=https://dg.de/1.0)"),
			@Property(key = "resourceSet.target", value = "(emf.name=dge)"),
			@Property(key = "storageService.target", value = "(storage.type=file)"),
			@Property(key = "registry.target", value = "(registry=main)"),
			@Property(key = "stageGate.target", value = "(" + GATE_PROPERTY + "=" + GATE_ID + ")"),
			@Property(key = "stageGate.cardinality.minimum", scalar = Scalar.Integer, value = "1"),
			@Property(key = "stages", type = Type.Array, value = {
					"{ \"name\" : \"" + CommonTestAnnotations.STAGE_DRAFT + "\", \"writable\" : true, \"final\": false}",
					"{ \"name\" : \"" + CommonTestAnnotations.STAGE_APPROVED + "\", \"writable\" : true, \"final\": false}",
					"{ \"name\" : \"" + CommonTestAnnotations.STAGE_RELEASE + "\", \"writable\" : true, \"final\": true}" }),
			@Property(key = "workflow.transitions", type = Type.Array, value = {
					CommonTestAnnotations.STAGE_DRAFT + ":" + CommonTestAnnotations.STAGE_APPROVED,
					CommonTestAnnotations.STAGE_APPROVED + ":" + CommonTestAnnotations.STAGE_RELEASE }),
			@Property(key = "stage.storage.mappings", type = Type.Array, value = {
					CommonTestAnnotations.STAGE_DRAFT + ":file", CommonTestAnnotations.STAGE_APPROVED + ":file",
					CommonTestAnnotations.STAGE_RELEASE + ":file" }) })
	@Retention(RetentionPolicy.RUNTIME)
	@interface GatedRegistrySetup {
	}

	@CommonTestAnnotations.EPackageLuceneIndexSetup
	@CommonTestAnnotations.StorageSetup
	@CommonTestAnnotations.SchemaRegistryServiceSetup
	@GatedRegistrySetup
	@WithFactoryConfiguration(factoryPid = TestAnnotations.PID_SCOPE_SERVICE, name = SCOPE, location = "?", properties = {
			@Property(key = "atlas.scope", value = SCOPE), @Property(key = "scope.name", value = SCOPE),
			@Property(key = "registryService.target", value = "(|(registry.name="
					+ CommonTestAnnotations.SCHEMA_REGISTRY_NAME + ")(registry.name=" + REGISTRY + "))"),
			@Property(key = "registryService.cardinality.minimum", value = "2", scalar = Scalar.Integer) })
	@Retention(RetentionPolicy.RUNTIME)
	@interface GatedScopeSetup {
	}

	@Test
	@GatedScopeSetup
	public void refusedTransitionAnswers409WithTheSourceMetadata(@InjectBundleContext BundleContext context)
			throws Exception {
		registerGate(context);
		awaitScope(context);
		upload();

		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId(OBJECT_ID);
		transition.setTargetStage(CommonTestAnnotations.STAGE_APPROVED);
		Response response = stageTarget(CommonTestAnnotations.STAGE_DRAFT).path("actions").path("transition")
				.request("application/json")
				.post(Entity.entity(TestHelper.serializeToXMI(transition, resourceSet), "application/xmi"));

		response.bufferEntity();
		String body = response.readEntity(String.class);
		assertEquals(409, response.getStatus(), "a gate's refusal is a conflict | body: " + body);
		assertTrue(body.contains("\"objectId\"") && body.contains(OBJECT_ID),
				"the body is the object's metadata, got: " + body);
		assertTrue(body.contains("\"diagnostics\"") && body.contains(TRANSITION_CODE),
				"the metadata carries the gate's finding, got: " + body);
		assertTrue(body.contains(GATE_ID), "recorded under the gate's producer, got: " + body);
		assertFalse(body.contains("\"code\" : \"409\"") || body.contains("\"code\":\"409\""),
				"not the plain error document, got: " + body);

		// what the response said is what the object now carries
		Response metadata = stageTarget(CommonTestAnnotations.STAGE_DRAFT).queryParam("objectId", OBJECT_ID)
				.request("application/json").get();
		metadata.bufferEntity();
		String stored = metadata.readEntity(String.class);
		assertEquals(200, metadata.getStatus(), "the object stayed in draft | body: " + stored);
		assertTrue(stored.contains(TRANSITION_CODE), "the veto is recorded on the object, got: " + stored);
		Response promoted = stageTarget(CommonTestAnnotations.STAGE_APPROVED).queryParam("objectId", OBJECT_ID)
				.request("application/json").get();
		assertTrue(promoted.getStatus() == 204 || promoted.getStatus() == 404,
				"nothing entered the target stage, status: " + promoted.getStatus());
	}

	@Test
	@GatedScopeSetup
	public void refusedDeleteAnswers409AndForceDeletesAnyway(@InjectBundleContext BundleContext context)
			throws Exception {
		registerGate(context);
		awaitScope(context);
		upload();

		Response refused = stageTarget(CommonTestAnnotations.STAGE_DRAFT).queryParam("objectId", OBJECT_ID)
				.request("application/json").delete();
		refused.bufferEntity();
		String body = refused.readEntity(String.class);
		assertEquals(409, refused.getStatus(), "a gate's refusal is a conflict | body: " + body);
		assertTrue(body.contains("\"objectId\"") && body.contains(OBJECT_ID),
				"the body is the object's metadata, got: " + body);
		assertTrue(body.contains("\"diagnostics\"") && body.contains(DELETE_CODE),
				"the metadata carries the gate's finding, got: " + body);

		Response still = stageTarget(CommonTestAnnotations.STAGE_DRAFT).queryParam("objectId", OBJECT_ID)
				.request("application/json").get();
		assertEquals(200, still.getStatus(), "the object stays");

		Response forced = stageTarget(CommonTestAnnotations.STAGE_DRAFT).queryParam("objectId", OBJECT_ID)
				.queryParam("force", "true").request("application/json").delete();
		assertStatus(200, forced, "force overrides the veto");

		Response gone = stageTarget(CommonTestAnnotations.STAGE_DRAFT).queryParam("objectId", OBJECT_ID)
				.request("application/json").get();
		assertTrue(gone.getStatus() == 204 || gone.getStatus() == 404, "the object is gone, status: " + gone.getStatus());
	}

	private void registerGate(BundleContext context) {
		StageGate gate = new StageGate() {
			@Override
			public boolean supportsObjectType(String objectType) {
				return true;
			}

			@Override
			public String producer() {
				return GATE_ID;
			}

			@Override
			public Promise<GateVerdict> beforeTransition(GateContext ctx) {
				return Promises.resolved(GateVerdict.refuse("the test gate says no",
						List.of(GateDiagnostic.error(TRANSITION_CODE, "the test gate says no").at("//person"))));
			}

			@Override
			public Promise<GateVerdict> beforeDelete(GateContext ctx) {
				return Promises.resolved(GateVerdict.refuse("the test gate still needs it",
						List.of(GateDiagnostic.error(DELETE_CODE, "the test gate still needs it"))));
			}
		};
		Dictionary<String, Object> props = new Hashtable<>();
		props.put(GATE_PROPERTY, GATE_ID);
		registrations.add(context.registerService(StageGate.class, gate, props));
	}

	private void awaitScope(BundleContext context) throws InterruptedException {
		ResourceAware resourceAware = ResourceAware.create(context, getResourceName());
		assertTrue(resourceAware.waitForResource(15, TimeUnit.SECONDS),
				getResourceName() + " should be registered within 15 seconds");
		assertNotNull(gateScopeService.waitForService(TimeUnit.SECONDS.toMillis(15)),
				"ScopeService for '" + SCOPE + "' should be available within 15 seconds");
		assertNotNull(gateScopeResourceSet.waitForService(TimeUnit.SECONDS.toMillis(15)),
				"ResourceSet for scope '" + SCOPE + "' / stage 'release' should be available within 15 seconds");
	}

	private void upload() throws Exception {
		Person person = TestHelper.createTestObject();
		String xmi = TestHelper.serializeToXMI(person, resourceSet);
		Response response = stageTarget(CommonTestAnnotations.STAGE_DRAFT).path(OBJECT_ID)
				.queryParam("name", "GatedPerson").queryParam("mediaType", "application/xml")
				.request("application/xmi").post(Entity.entity(xmi, "application/xmi"));
		assertStatus(201, response, "the upload is not gated");
	}

	/** /{SCOPE}/registries/{REGISTRY}/stages/{stage} */
	private WebTarget stageTarget(String stage) {
		return scopeTarget(SCOPE).path("registries").path(REGISTRY).path("stages").path(stage);
	}

	@Override
	String getResourceName() {
		return "ObjectRegistryResource";
	}
}
