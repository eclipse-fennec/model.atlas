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
package org.eclipse.fennec.model.atlas.validation.rest.tests;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.datagen.example.model.dge.Company;
import org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGFactory;
import org.eclipse.fennec.model.atlas.validation.model.cocl.BatchValidationRequest;
import org.eclipse.fennec.model.atlas.validation.model.cocl.COCLFactory;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole;
import org.eclipse.fennec.model.atlas.validation.model.cocl.Severity;
import org.eclipse.fennec.model.atlas.validation.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.model.atlas.validation.rest.tests.helper.TestAnnotations;
import org.eclipse.fennec.model.atlas.validation.rest.tests.helper.TestAnnotations.JenaScopeServiceSetup;
import org.eclipse.fennec.model.atlas.validation.rest.tests.helper.TestHelper;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;

/**
 * Integration tests for ObjectBatchValidationResource REST endpoints.
 *
 * <p>
 * Uses a real "jena" scope backed by a "cocl" registry with a writable
 * "release" stage. Each test that needs an OclConstraintSet uploads one via
 * the ScopeService before issuing REST calls.
 * </p>
 *
 * @author ilenia
 * @since Apr 2026
 */
@RequireEMF
@RequireJakartarsWhiteboard
@RequireConfigurationAdmin
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@SuppressWarnings({"rawtypes", "unchecked"})
public class ObjectBatchValidationResourceTest {

	private static final String BASE_URL = "http://localhost:8185/rest";
	private static final String DGE_COMPANY_CLASS_URI = "https://dg.de/1.0#//Company";

	@InjectService(filter = "(emf.name=workflowapi)")
	ResourceSet resourceSet;

	// Tracks the per-(scope, stage) ResourceSet published once the registry chain
	// for the jena/release scope is up. cardinality 0 so injection does not block
	// before the scope configuration has been applied; the test awaits it via
	// waitForService in ensureResourceAvailability.
	@InjectService(cardinality = 0, filter = "(&(scope.name=jena)(stage.name=release))")
	ServiceAware<ResourceSet> jenaReleaseResourceSet;

	@InjectService
	ClientBuilder clientBuilder;

	@TempDir
	Path tempDir;

	private Client restClient;

	@BeforeEach
	public void setup() throws Exception {
		System.setProperty(TestAnnotations.PROP_TEMP_DIR, tempDir.toString());
		restClient = clientBuilder.build();
		TestHelper.ensureXMIFactory(resourceSet);
	}

	@AfterEach
	public void teardown() throws Exception {
		if (nonNull(restClient)) {
			restClient.close();
			restClient = null;
		}
	}

	// ========== validate Tests ==========

	@Test
	//@Disabled
	public void testValidate_NoObjects_Returns400() throws Exception {
		BatchValidationRequest request = COCLFactory.eINSTANCE.createBatchValidationRequest();
		request.setCoclId("any-id");

		Response response = postValidateRequest(request);

		assertEquals(400, response.getStatus(), "Should return 400 when no objects are provided");
	}

	@Test
	//@Disabled
	public void testValidate_NoCoclId_Returns400() throws Exception {
		BatchValidationRequest request = COCLFactory.eINSTANCE.createBatchValidationRequest();
		request.getValidationObjects().add(DGFactory.eINSTANCE.createCompany());

		Response response = postValidateRequest(request);

		assertEquals(400, response.getStatus(), "Should return 400 when coclId is missing");
	}

	@Test
	//@Disabled
	@JenaScopeServiceSetup
	public void testValidate_UnknownCoclId_Returns400(
			@InjectBundleContext BundleContext context,
			@InjectService(filter = "(scope.name=jena)", timeout = 10000) ScopeService jenaScope)
			throws Exception {
		ensureResourceAvailability(context);

		BatchValidationRequest request = COCLFactory.eINSTANCE.createBatchValidationRequest();
		request.getValidationObjects().add(DGFactory.eINSTANCE.createCompany());
		request.setCoclId("non-existent-id");

		Response response = postValidateRequest(request);

		assertEquals(400, response.getStatus(), "Should return 400 when coclId is not found");
	}

	@Test
	//@Disabled
	@JenaScopeServiceSetup
	public void testValidate_ValidBatch_Returns200(
			@InjectBundleContext BundleContext context,
			@InjectService(filter = "(scope.name=jena)", timeout = 10000) ScopeService jenaScope)
			throws Exception {
		ensureResourceAvailability(context);
		String coclId = "validation-set-001";
		saveValidationConstraintSet(jenaScope, coclId);

		Company acme = DGFactory.eINSTANCE.createCompany();
		acme.setName("Acme");

		BatchValidationRequest request = COCLFactory.eINSTANCE.createBatchValidationRequest();
		request.getValidationObjects().add(acme);
		request.setCoclId(coclId);

		Response response = postValidateRequest(request);

		assertEquals(200, response.getStatus(), "Should return 200 for a valid batch");
		String body = response.readEntity(String.class);
		assertNotNull(body, "Response body should not be null");
		assertTrue(body.contains("diagnostics"), "Response should contain diagnostics");
	}

	@Test
	//@Disabled
	@JenaScopeServiceSetup
	public void testValidate_FilterConstraintWrongRole_Returns400(
			@InjectBundleContext BundleContext context,
			@InjectService(filter = "(scope.name=jena)", timeout = 10000) ScopeService jenaScope)
			throws Exception {
		ensureResourceAvailability(context);
		String coclId = "validation-set-002";
		saveValidationConstraintSet(jenaScope, coclId);

		OclConstraint badFilter = COCLFactory.eINSTANCE.createOclConstraint();
		badFilter.setRole(OclRole.VALIDATION);

		BatchValidationRequest request = COCLFactory.eINSTANCE.createBatchValidationRequest();
		request.getValidationObjects().add(DGFactory.eINSTANCE.createCompany());
		request.setCoclId(coclId);
		request.setFilterConstraint(badFilter);

		Response response = postValidateRequest(request);

		assertEquals(400, response.getStatus(), "Should return 400 when the filter constraint has wrong role");
	}

	@Test
	//@Disabled
	@JenaScopeServiceSetup
	public void testValidate_WithFilter_FiltersObjects_Returns200(
			@InjectBundleContext BundleContext context,
			@InjectService(filter = "(scope.name=jena)", timeout = 10000) ScopeService jenaScope)
			throws Exception {
		ensureResourceAvailability(context);
		String coclId = "validation-set-003";
		saveValidationConstraintSet(jenaScope, coclId);

		Company withAddress = DGFactory.eINSTANCE.createCompany();
		withAddress.setName("HasAddress");
		withAddress.setAddress(DGFactory.eINSTANCE.createAddress());

		Company withoutAddress = DGFactory.eINSTANCE.createCompany();
		withoutAddress.setName("NoAddress");

		OclConstraint filterConstraint = filterConstraint("self.address <> null");

		BatchValidationRequest request = COCLFactory.eINSTANCE.createBatchValidationRequest();
		request.getValidationObjects().add(withAddress);
		request.getValidationObjects().add(withoutAddress);
		request.setCoclId(coclId);
		request.setFilterConstraint(filterConstraint);

		Response response = postValidateRequest(request);

		assertEquals(200, response.getStatus(), "Should return 200 when some objects are filtered");
	}

	// ========== filter Tests ==========

	@Test
	//@Disabled
	public void testFilter_NoObjects_Returns400() throws Exception {
		BatchValidationRequest request = COCLFactory.eINSTANCE.createBatchValidationRequest();
		request.setCoclId("any-id");

		Response response = postFilterRequest(request);

		assertEquals(400, response.getStatus(), "Should return 400 when no objects are provided");
	}

	@Test
	//@Disabled
	public void testFilter_NoCoclId_Returns400() throws Exception {
		BatchValidationRequest request = COCLFactory.eINSTANCE.createBatchValidationRequest();
		request.getValidationObjects().add(DGFactory.eINSTANCE.createCompany());

		Response response = postFilterRequest(request);

		assertEquals(400, response.getStatus(), "Should return 400 when coclId is missing");
	}

	@Test
	//@Disabled
	@JenaScopeServiceSetup
	public void testFilter_UnknownCoclId_Returns400(
			@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);

		BatchValidationRequest request = COCLFactory.eINSTANCE.createBatchValidationRequest();
		request.getValidationObjects().add(DGFactory.eINSTANCE.createCompany());
		request.setCoclId("non-existent-id");

		Response response = postFilterRequest(request);

		assertEquals(400, response.getStatus(), "Should return 400 when coclId is not found");
	}

	@Test
	//@Disabled
	@JenaScopeServiceSetup
	public void testFilter_NoFilterConstraints_Returns204(
			@InjectBundleContext BundleContext context,
			@InjectService(filter = "(scope.name=jena)", timeout = 10000) ScopeService jenaScope)
			throws Exception {
		ensureResourceAvailability(context);
		String coclId = "filter-set-no-filter";
		saveValidationConstraintSet(jenaScope, coclId);

		BatchValidationRequest request = COCLFactory.eINSTANCE.createBatchValidationRequest();
		request.getValidationObjects().add(DGFactory.eINSTANCE.createCompany());
		request.setCoclId(coclId);

		Response response = postFilterRequest(request);

		assertEquals(204, response.getStatus(), "Should return 204 when constraintSet has no REFERENCE_FILTER constraints");
	}

	@Test
	//@Disabled
	@JenaScopeServiceSetup
	public void testFilter_AllRetained_Returns204(
			@InjectBundleContext BundleContext context,
			@InjectService(filter = "(scope.name=jena)", timeout = 10000) ScopeService jenaScope)
			throws Exception {
		ensureResourceAvailability(context);
		String coclId = "filter-set-all-pass";
		saveFilterConstraintSet(jenaScope, coclId, "self.address <> null");

		Company acme = DGFactory.eINSTANCE.createCompany();
		acme.setName("Acme");
		acme.setAddress(DGFactory.eINSTANCE.createAddress());

		BatchValidationRequest request = COCLFactory.eINSTANCE.createBatchValidationRequest();
		request.getValidationObjects().add(acme);
		request.setCoclId(coclId);

		Response response = postFilterRequest(request);

		assertEquals(204, response.getStatus(), "Should return 204 when all objects are retained after filtering");
	}

	@Test
	//@Disabled
	@JenaScopeServiceSetup
	public void testFilter_SomeFiltered_Returns200(
			@InjectBundleContext BundleContext context,
			@InjectService(filter = "(scope.name=jena)", timeout = 10000) ScopeService jenaScope)
			throws Exception {
		ensureResourceAvailability(context);
		String coclId = "filter-set-some-removed";
		saveFilterConstraintSet(jenaScope, coclId, "self.address <> null");

		Company withAddress = DGFactory.eINSTANCE.createCompany();
		withAddress.setName("HasAddress");
		withAddress.setAddress(DGFactory.eINSTANCE.createAddress());

		Company withoutAddress = DGFactory.eINSTANCE.createCompany();
		withoutAddress.setName("NoAddress");

		BatchValidationRequest request = COCLFactory.eINSTANCE.createBatchValidationRequest();
		request.getValidationObjects().add(withAddress);
		request.getValidationObjects().add(withoutAddress);
		request.setCoclId(coclId);

		Response response = postFilterRequest(request);

		assertEquals(200, response.getStatus(), "Should return 200 when some objects are filtered out");
		String body = response.readEntity(String.class);
		assertNotNull(body, "Response body should not be null");
		assertTrue(body.contains("results"), "Response should contain results");
	}

	// ---- helpers ----

	private void ensureResourceAvailability(BundleContext context) throws InterruptedException {
		ResourceAware resourceAware = ResourceAware.create(context, "ObjectBatchValidationResource");
		boolean resourceReady = resourceAware.waitForResource(15, TimeUnit.SECONDS);
		assertTrue(resourceReady, "ObjectBatchValidationResource should be registered within 15 seconds.");
		// The JAX-RS resource being registered does not guarantee the scope/stage
		// ResourceSet is published yet. The codec's request-scoped @Context
		// ResourceSet is resolved by ScopedResourceSetProvider from the
		// (scope.name, stage.name) ResourceSet service tracked by
		// ResourceSetCollector; if it is not yet available the provider answers
		// 400. Wait for it so requests don't race the registry chain coming up.
		assertNotNull(jenaReleaseResourceSet.waitForService(TimeUnit.SECONDS.toMillis(15)),
				"ResourceSet for scope 'jena' / stage 'release' should be available within 15 seconds.");
	}

	private void saveValidationConstraintSet(ScopeService<EObject> jenaScope, String id) throws Exception {
		OclConstraintSet constraintSet = COCLFactory.eINSTANCE.createOclConstraintSet();
		constraintSet.setName(id);

		OclConstraint validationConstraint = COCLFactory.eINSTANCE.createOclConstraint();
		validationConstraint.setName("CompanyNameNotNull");
		validationConstraint.setContextClass(DGE_COMPANY_CLASS_URI);
		validationConstraint.setExpression("self.name <> null");
		validationConstraint.setRole(OclRole.VALIDATION);
		validationConstraint.setSeverity(Severity.ERROR);
		validationConstraint.setActive(true);
		constraintSet.getConstraints().add(validationConstraint);

		uploadConstraintSet(jenaScope, id, constraintSet);
	}

	private void saveFilterConstraintSet(ScopeService jenaScope, String id, String filterExpression)
			throws Exception {
		OclConstraintSet constraintSet = COCLFactory.eINSTANCE.createOclConstraintSet();
		constraintSet.setName(id);

		OclConstraint filterConstraint = COCLFactory.eINSTANCE.createOclConstraint();
		filterConstraint.setName("CompanyFilter");
		filterConstraint.setContextClass(DGE_COMPANY_CLASS_URI);
		filterConstraint.setExpression(filterExpression);
		filterConstraint.setRole(OclRole.REFERENCE_FILTER);
		filterConstraint.setSeverity(Severity.INFO);
		filterConstraint.setActive(true);
		constraintSet.getConstraints().add(filterConstraint);

		// Also include a VALIDATION constraint so validate tests pass the "no active VALIDATION" guard
		OclConstraint validationConstraint = COCLFactory.eINSTANCE.createOclConstraint();
		validationConstraint.setName("CompanyNameNotNull");
		validationConstraint.setContextClass(DGE_COMPANY_CLASS_URI);
		validationConstraint.setExpression("self.name <> null");
		validationConstraint.setRole(OclRole.VALIDATION);
		validationConstraint.setSeverity(Severity.ERROR);
		validationConstraint.setActive(true);
		constraintSet.getConstraints().add(validationConstraint);

		uploadConstraintSet(jenaScope, id, constraintSet);
	}

	private void uploadConstraintSet(ScopeService<EObject> jenaScope, String id, OclConstraintSet constraintSet)
			throws Exception {
		TestHelper.uploadConstraintSet(jenaScope, id, constraintSet);
	}

	private OclConstraint filterConstraint(String expression) {
		OclConstraint constraint = COCLFactory.eINSTANCE.createOclConstraint();
		constraint.setName("filter");
		constraint.setContextClass(DGE_COMPANY_CLASS_URI);
		constraint.setExpression(expression);
		constraint.setRole(OclRole.REFERENCE_FILTER);
		constraint.setSeverity(Severity.INFO);
		constraint.setActive(true);
		return constraint;
	}

	private Response postValidateRequest(BatchValidationRequest request) throws Exception {
		String xmiContent = TestHelper.serializeToXMI(request, resourceSet);
		return restClient.target(BASE_URL).path("jena/release/validate/batch")
				.request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
	}

	private Response postFilterRequest(BatchValidationRequest request) throws Exception {
		String xmiContent = TestHelper.serializeToXMI(request, resourceSet);
		return restClient.target(BASE_URL).path("jena/release/validate/batch/filter")
				.request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
	}
}
