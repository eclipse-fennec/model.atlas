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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.fennec.model.atlas.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestAnnotations;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.workflow.ResourceSetCollector;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentServiceObjects;
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
 * Issue #333: uploading an event.atlas {@code ProviderMapping} XMI answers an error
 * without any description.
 *
 * <p>
 * The test replays the issue's request: the event.atlas mapping model and the domain
 * model the mapping points into are uploaded to the schema registry, then the issue's
 * XMI is uploaded to an object registry that accepts any {@code EObject} - under the
 * object id {@code Mapping WaterQuality.xmi} and with {@code override=true}, like
 * {@code POST .../stages/draft/Mapping%20WaterQuality.xmi?name=Mapping+WaterQuality&override=true}.
 * The object was stored, but the {@code Location} header of the answer carried the
 * object id unescaped ({@code ?objectId=Mapping WaterQuality.xmi}); Jersey cannot parse
 * that header and the client got a {@code 500} without a description. The answer's
 * {@code Location} has to lead to the object's metadata.
 * </p>
 *
 * <p>
 * The mapping itself is no special case for the Atlas, although {@code ResourceMapping}
 * extends Ecore's {@code EAttribute} and the instance cross-references the domain
 * package and Ecore's data types: every reference is validated - loaded through the
 * server's (scope, stage) ResourceSet, the one the REST layer reads an upload with,
 * and again after reading the stored mapping back - and the EMF {@link Diagnostician}
 * has to accept the instance.
 * </p>
 *
 * <p>
 * The domain model of the issue ({@code http://data-in-motion.biz/waterparc/domain})
 * is not public; {@code waterparc-domain.ecore} is reduced to what the mapping
 * references.
 * </p>
 */
public class MappingXmiUploadRestTest extends AbstractRestTest {

	private static final String SCOPE = "mapping-scope";
	private static final String SCHEMA_REGISTRY = "mapping-schemas";
	private static final String REGISTRY = "mappings";
	/** The object id of the issue's request: a blank and a dot, as a file name would have. */
	private static final String OBJECT_ID = "Mapping WaterQuality.xmi";
	private static final String OBJECT_NAME = "Mapping WaterQuality";

	private static final String MAPPING_NS_URI = "https://fennec.eclipse.org/event.atlas/mapping/1.0";
	private static final String DOMAIN_NS_URI = "http://data-in-motion.biz/waterparc/domain";

	private static final String TEST_DATA = "/test-data/issue333/";

	@InjectService(cardinality = 0, filter = "(scope.name=" + SCOPE + ")")
	ServiceAware<ScopeService> mappingScopeService;

	@InjectService(cardinality = 0, filter = "(&(scope.name=" + SCOPE + ")(stage.name="
			+ CommonTestAnnotations.STAGE_RELEASE + "))")
	ServiceAware<ResourceSet> mappingScopeResourceSet;

	@InjectService(cardinality = 0)
	ServiceAware<ResourceSetCollector> resourceSetCollector;

	/** Each {@code resources} entry in document order: its name, the domain attribute it maps, its Ecore type. */
	private static final List<String[]> RESOURCES = List.of(
			new String[] { "ph", "ph", "EDouble" },
			new String[] { "freeChlorine", "free_chlorine", "EDouble" },
			new String[] { "redox", "redox", "EDouble" },
			new String[] { "status", "status", "EString" },
			new String[] { "area", "area_id", "EString" });

	/**
	 * The scope's schema registry. Unlike the shared test schema registry it runs the
	 * {@code EPackageStageActionService}, as the runtime configurations do: only then does
	 * an uploaded schema get registered for its (scope, stage), which an instance upload
	 * depends on.
	 */
	@WithFactoryConfiguration(factoryPid = "EPackageStageActionService", name = "mapping-stage-action", location = "?", properties = {
			@Property(key = "storageService.target", value = "(storage.type=file)"),
			@Property(key = "trigger.stages", scalar = Scalar.String, type = Type.Array, value = {
					CommonTestAnnotations.STAGE_DRAFT, CommonTestAnnotations.STAGE_APPROVED,
					CommonTestAnnotations.STAGE_RELEASE }) })
	@WithFactoryConfiguration(factoryPid = CommonTestAnnotations.PID_REGISTRY_SERVICE, name = SCHEMA_REGISTRY, location = "?", properties = {
			@Property(key = "registry.name", value = SCHEMA_REGISTRY),
			@Property(key = "registry.type", value = "SCHEMA"),
			@Property(key = "root.eclass.uri", value = "http://www.eclipse.org/emf/2002/Ecore#//EPackage"),
			@Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
			@Property(key = "storageService.target", value = "(storage.type=file)"),
			@Property(key = "stageActionService.target", value = "(component.name=EPackageStageActionService)"),
			@Property(key = "stageActionService.cardinality.minimum", value = "1", scalar = Scalar.Integer),
			@Property(key = "registry.target", value = "(registry=main)"),
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
	@interface MappingSchemaRegistrySetup {
	}

	/** An object registry taking instances of any uploaded schema, like a mapping store does. */
	@WithFactoryConfiguration(factoryPid = CommonTestAnnotations.PID_REGISTRY_SERVICE, name = REGISTRY, location = "?", properties = {
			@Property(key = "registry.name", value = REGISTRY),
			@Property(key = "registry.type", value = "OTHER"),
			@Property(key = "root.eclass.uri", value = "http://www.eclipse.org/emf/2002/Ecore#//EObject"),
			@Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
			@Property(key = "storageService.target", value = "(storage.type=file)"),
			@Property(key = "registry.target", value = "(registry=main)"),
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
	@interface MappingRegistrySetup {
	}

	@CommonTestAnnotations.EPackageLuceneIndexSetup
	@CommonTestAnnotations.StorageSetup
	@MappingSchemaRegistrySetup
	@MappingRegistrySetup
	@WithFactoryConfiguration(factoryPid = TestAnnotations.PID_SCOPE_SERVICE, name = SCOPE, location = "?", properties = {
			@Property(key = "atlas.scope", value = SCOPE), @Property(key = "scope.name", value = SCOPE),
			@Property(key = "registryService.target", value = "(|(registry.name="
					+ SCHEMA_REGISTRY + ")(registry.name=" + REGISTRY + "))"),
			@Property(key = "registryService.cardinality.minimum", value = "2", scalar = Scalar.Integer) })
	@Retention(RetentionPolicy.RUNTIME)
	@interface MappingScopeSetup {
	}

	@Test
	@MappingScopeSetup
	public void providerMappingXmiUploads(@InjectBundleContext BundleContext context) throws Exception {
		awaitScope(context);
		uploadSchema("waterparc-domain.ecore", DOMAIN_NS_URI, "domain");
		uploadSchema("event-atlas-mapping.ecore", MAPPING_NS_URI, "mapping");

		// the issue's request: creates the object ...
		Response created = uploadMapping();
		assertStatus(201, created, "The mapping XMI of issue #333 must upload");
		String metadata = created.readEntity(String.class);
		assertTrue(metadata.contains(OBJECT_ID), "The response is the stored object's metadata, got: " + metadata);
		assertLocationNamesTheObject(created);

		// ... and the same request again replaces it (override=true takes the update branch)
		Response replaced = uploadMapping();
		assertStatus(200, replaced, "Uploading the mapping again with override=true replaces it");
		assertLocationNamesTheObject(replaced);

		Response content = objectStageTarget(CommonTestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", OBJECT_ID).request("application/xmi").get();
		assertStatus(200, content, "The uploaded mapping must read back");
		String xmi = content.readEntity(String.class);
		assertNotNull(xmi);
		assertTrue(xmi.contains("ProviderMapping") && xmi.contains("mid=\"water-quality\""),
				"The read back content is the uploaded mapping, got: " + xmi);

		verifyReferences("the read back mapping", xmi);
	}

	/** {@code POST .../stages/draft/Mapping%20WaterQuality.xmi?name=Mapping+WaterQuality&override=true} */
	private Response uploadMapping() throws IOException {
		return objectStageTarget(CommonTestAnnotations.STAGE_DRAFT).path(OBJECT_ID)
				.queryParam("name", OBJECT_NAME).queryParam("override", "true").request("application/json")
				.post(Entity.entity(testData("waterpark-water-quality.xmi"), "application/xmi"));
	}

	/** The {@code Location} of an upload answer is a valid URI that leads to the object's metadata. */
	private void assertLocationNamesTheObject(Response response) {
		String location = response.getHeaderString("Location");
		assertNotNull(location, "An upload answers with the object's Location");
		assertTrue(location.startsWith(BASE_URL + "/"), "The Location is absolute below the REST base, was: " + location);
		Response metadata = restClient.target(location).request("application/json").get();
		assertStatus(200, metadata, "The Location '" + location + "' must lead to the object");
		String body = metadata.readEntity(String.class);
		assertTrue(body.contains(OBJECT_ID), "The Location '" + location + "' names the object, got: " + body);
	}

	/**
	 * The issue's XMI as the server reads it: through the (scope, draft) ResourceSet the
	 * uploaded schemas are registered in. Independent of storing it, so a failure here
	 * points at the resolution against the registered packages.
	 */
	@Test
	@MappingScopeSetup
	public void providerMappingReferencesResolveAgainstTheUploadedSchemas(@InjectBundleContext BundleContext context)
			throws Exception {
		awaitScope(context);
		uploadSchema("waterparc-domain.ecore", DOMAIN_NS_URI, "domain");
		uploadSchema("event-atlas-mapping.ecore", MAPPING_NS_URI, "mapping");

		verifyReferences("the uploaded mapping", testData("waterpark-water-quality.xmi"));
	}

	/**
	 * Loads {@code xmi} through the (scope, draft) chain ResourceSet and validates every
	 * reference of the mapping, and the mapping as a whole.
	 */
	private void verifyReferences(String what, String xmi) throws Exception {
		ResourceSetCollector collector = resourceSetCollector.waitForService(TimeUnit.SECONDS.toMillis(15));
		assertNotNull(collector, "The ResourceSetCollector should be available");
		ComponentServiceObjects<ResourceSet> lease = collector.getResourceSetObjects(SCOPE,
				CommonTestAnnotations.STAGE_DRAFT);
		assertNotNull(lease, "The (" + SCOPE + ", draft) chain ResourceSet must exist");
		ResourceSet chainResourceSet = lease.getService();
		Resource resource = new XMIResourceImpl(URI.createURI("temp/mapping.xmi"));
		try {
			chainResourceSet.getResources().add(resource);
			resource.load(new ByteArrayInputStream(xmi.getBytes(StandardCharsets.UTF_8)), Map.of());
			assertTrue(resource.getErrors().isEmpty(), what + " loads without errors: " + resource.getErrors());
			assertEquals(1, resource.getContents().size(), what + " has one root");
			EObject mapping = resource.getContents().get(0);
			assertEquals("ProviderMapping", mapping.eClass().getName(), what + " is a ProviderMapping");
			assertEquals(MAPPING_NS_URI, mapping.eClass().getEPackage().getNsURI());

			EPackage domain = chainResourceSet.getPackageRegistry().getEPackage(DOMAIN_NS_URI);
			assertNotNull(domain, "The uploaded domain package must be registered for (" + SCOPE + ", draft)");
			EClass waterQuality = (EClass) domain.getEClassifier("WaterQuality");
			assertNotNull(waterQuality, "The domain package has WaterQuality");

			// nothing may stay a proxy
			Map<EObject, Collection<EStructuralFeature.Setting>> unresolved = EcoreUtil.UnresolvedProxyCrossReferencer
					.find(resource);
			assertTrue(unresolved.isEmpty(), what + " has unresolved references: " + describe(unresolved));

			// ProviderMapping
			assertSame(waterQuality, single(mapping, "providerClasses"), what + ": providerClasses");
			EObject name = (EObject) mapping.eGet(feature(mapping, "name"));
			assertNotNull(name, what + ": the provider name mapping");
			assertSame(waterQuality.getEStructuralFeature("sensor_id"), single(name, "featurePath"),
					what + ": name/featurePath");
			EObject timestamp = (EObject) mapping.eGet(feature(mapping, "timestamp"));
			assertNotNull(timestamp, what + ": the provider timestamp mapping");

			// ServiceMapping and its ResourceMappings
			List<EObject> services = list(mapping, "services");
			assertEquals(1, services.size(), what + ": one service");
			List<EObject> resources = list(services.get(0), "resources");
			assertEquals(RESOURCES.size(), resources.size(), what + ": the resources of the service");
			for (int i = 0; i < RESOURCES.size(); i++) {
				String[] expected = RESOURCES.get(i);
				EObject resourceMapping = resources.get(i);
				String label = what + ": resource '" + expected[0] + "'";
				assertEquals(expected[0], resourceMapping.eGet(feature(resourceMapping, "name")), label + " name");
				assertSame(EcorePackage.eINSTANCE.getEClassifier(expected[2]),
						resourceMapping.eGet(feature(resourceMapping, "eType")), label + " eType");
				assertSame(waterQuality.getEStructuralFeature(expected[1]), single(resourceMapping, "valueFeature"),
						label + " valueFeature");
				assertSame(timestamp, resourceMapping.eGet(feature(resourceMapping, "timestamp")),
						label + " timestamp is the provider's timestamp mapping");
			}

			// AdminMapping
			EObject admin = (EObject) mapping.eGet(feature(mapping, "admin"));
			assertNotNull(admin, what + ": the admin mapping");
			assertSame(domain, admin.eGet(feature(admin, "providerPackage")), what + ": admin/providerPackage");

			Diagnostic diagnostic = Diagnostician.INSTANCE.validate(mapping);
			assertEquals(Diagnostic.OK, diagnostic.getSeverity(), what + " validates: " + describe(diagnostic));
		} finally {
			resource.unload();
			chainResourceSet.getResources().remove(resource);
			lease.ungetService(chainResourceSet);
		}
	}

	private static EStructuralFeature feature(EObject object, String name) {
		EStructuralFeature feature = object.eClass().getEStructuralFeature(name);
		assertNotNull(feature, object.eClass().getName() + " has no feature '" + name + "'");
		return feature;
	}

	@SuppressWarnings("unchecked")
	private static List<EObject> list(EObject object, String name) {
		return (List<EObject>) object.eGet(feature(object, name));
	}

	private static EObject single(EObject object, String name) {
		List<EObject> values = list(object, name);
		assertEquals(1, values.size(), object.eClass().getName() + "." + name + " holds one value: " + values);
		EObject value = values.get(0);
		assertFalse(value.eIsProxy(), object.eClass().getName() + "." + name + " is resolved: " + value);
		return value;
	}

	private static String describe(Map<EObject, Collection<EStructuralFeature.Setting>> unresolved) {
		StringBuilder sb = new StringBuilder();
		unresolved.forEach((proxy, settings) -> settings.forEach(setting -> sb.append("\n  ")
				.append(setting.getEObject().eClass().getName()).append('.')
				.append(setting.getEStructuralFeature().getName()).append(" -> ")
				.append(EcoreUtil.getURI(proxy))));
		return sb.toString();
	}

	private static String describe(Diagnostic diagnostic) {
		StringBuilder sb = new StringBuilder(diagnostic.getMessage());
		for (Diagnostic child : diagnostic.getChildren()) {
			sb.append("\n  ").append(child.getMessage());
		}
		return sb.toString();
	}

	private void uploadSchema(String file, String nsUri, String name) throws IOException {
		Response response = scopeTarget(SCOPE).path("schema").path("stages").path(CommonTestAnnotations.STAGE_DRAFT)
				.queryParam("nsUri", nsUri).queryParam("name", name).request("application/json")
				.post(Entity.entity(testData(file), "application/xmi"));
		assertStatus(201, response, "Schema " + nsUri + " must upload");
	}

	private String testData(String file) throws IOException {
		try (InputStream in = getClass().getResourceAsStream(TEST_DATA + file)) {
			assertNotNull(in, "Test data " + TEST_DATA + file + " must be part of the test bundle");
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private void awaitScope(BundleContext context) throws InterruptedException {
		ResourceAware resourceAware = ResourceAware.create(context, getResourceName());
		assertTrue(resourceAware.waitForResource(15, TimeUnit.SECONDS),
				getResourceName() + " should be registered within 15 seconds");
		assertNotNull(mappingScopeService.waitForService(TimeUnit.SECONDS.toMillis(15)),
				"ScopeService for '" + SCOPE + "' should be available within 15 seconds");
		assertNotNull(mappingScopeResourceSet.waitForService(TimeUnit.SECONDS.toMillis(15)),
				"ResourceSet for scope '" + SCOPE + "' / stage 'release' should be available within 15 seconds");
	}

	/** /{SCOPE}/registries/{REGISTRY}/stages/{stage} */
	private WebTarget objectStageTarget(String stage) {
		return scopeTarget(SCOPE).path("registries").path(REGISTRY).path("stages").path(stage);
	}

	@Override
	String getResourceName() {
		return "ObjectRegistryResource";
	}
}
