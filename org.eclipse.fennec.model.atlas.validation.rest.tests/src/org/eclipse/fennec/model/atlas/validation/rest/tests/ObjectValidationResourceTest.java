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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.datagen.example.model.dge.Address;
import org.eclipse.fennec.model.atlas.datagen.example.model.dge.Company;
import org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGFactory;
import org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGPackage;
import org.eclipse.fennec.model.atlas.validation.model.cocl.COCLFactory;
import org.eclipse.fennec.model.atlas.validation.model.cocl.DerivedValidationRequest;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OperationReturnType;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OperationValidationRequest;
import org.eclipse.fennec.model.atlas.validation.model.cocl.Severity;
import org.eclipse.fennec.model.atlas.validation.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.model.atlas.validation.rest.tests.helper.TestAnnotations;
import org.eclipse.fennec.model.atlas.validation.rest.tests.helper.TestAnnotations.JenaScopeServiceSetup;
import org.eclipse.fennec.model.atlas.validation.rest.tests.helper.TestHelper;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.gecko.emf.rest.annotations.RequireEMFMessageBodyReaderWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;

/**
 * Integration tests for ObjectValidationResource REST endpoints.
 *
 * <p>
 * Uses a real "jena" scope backed by a "cocl" registry. Tests that require an
 * OclConstraintSet upload one via the ScopeService before issuing REST calls.
 * </p>
 *
 * @author ilenia
 * @since Mar 17, 2026
 */
@RequireEMF
@RequireEMFMessageBodyReaderWriter
@RequireJakartarsWhiteboard
@RequireConfigurationAdmin
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@SuppressWarnings({"rawtypes"})
public class ObjectValidationResourceTest {

	private static final String BASE_URL = "http://localhost:8185/rest";
	private static final String DGE_COMPANY_CLASS_URI = "https://dg.de/1.0#//Company";

	@InjectService(filter = "(emf.name=workflowapi)")
	ResourceSet resourceSet;

	@InjectService
	ClientBuilder clientBuilder;

	@TempDir
	Path tempDir;

	private Client restClient;
	private final List<Resource> tempResources = new ArrayList<>();
	private final List<String> tempPackageUris = new ArrayList<>();

	@BeforeEach
	public void setup() throws Exception {
		System.setProperty(TestAnnotations.PROP_TEMP_DIR, tempDir.toString());
		restClient = clientBuilder.build();
		TestHelper.ensureXMIFactory(resourceSet);
	}

	@AfterEach
	public void teardown() throws Exception {
		tempResources.forEach(r -> resourceSet.getResources().remove(r));
		tempResources.clear();
		tempPackageUris.forEach(uri -> {
			EPackage.Registry.INSTANCE.remove(uri);
			resourceSet.getPackageRegistry().remove(uri);
		});
		tempPackageUris.clear();

		if (nonNull(restClient)) {
			restClient.close();
			restClient = null;
		}
	}

	// ========== Validation Tests ==========

	@Test
	//@Disabled
	public void testValidate_Success() throws Exception {
		EPackage validPackage = TestHelper.createTestEPackage("http://test.com/valid/1.0", "ValidPackage", "vp");
		String xmiContent = TestHelper.serializeToXMI(validPackage, resourceSet);

		Response response = restClient.target(BASE_URL).path("jena/release/validate").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
		assertNotNull(response.readEntity(String.class), "Should return diagnostic content");
	}

	@Test
	//@Disabled
	public void testValidate_ResponseContainsDiagnosticInfo() throws Exception {
		EPackage validPackage = TestHelper.createTestEPackage("http://test.com/diag/1.0", "DiagPackage", "dp");
		String xmiContent = TestHelper.serializeToXMI(validPackage, resourceSet);

		Response response = restClient.target(BASE_URL).path("jena/release/validate").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return diagnostic content");
		assertTrue(responseContent.contains("type") || responseContent.contains("message"),
				"Response should contain diagnostic information");
	}

	// ========== MediaType Tests ==========

	@Test
	//@Disabled
	public void testValidate_UnsupportedMediaType() throws Exception {
		EPackage validPackage = TestHelper.createTestEPackage("http://test.com/unsup/1.0", "UnsupPackage", "up");
		String xmiContent = TestHelper.serializeToXMI(validPackage, resourceSet);

		Response response = restClient.target(BASE_URL).path("jena/release/validate")
				.queryParam("mediaType", "application/unsupported").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(415, response.getStatus(), "Should return HTTP 415 Unsupported Media Type");
	}

	@Test
	//@Disabled
	public void testValidate_WithSupportedMediaTypeQueryParam() throws Exception {
		EPackage validPackage = TestHelper.createTestEPackage("http://test.com/mt/1.0", "MtPackage", "mt");
		String xmiContent = TestHelper.serializeToXMI(validPackage, resourceSet);

		Response response = restClient.target(BASE_URL).path("jena/release/validate")
				.queryParam("mediaType", "application/xml").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
		assertEquals("application/xml", response.getHeaderString("Content-Type"),
				"Content-Type header should be set to mediaType query parameter value");
	}

	@Test
	//@Disabled
	public void testValidate_RejectsUnsupportedAcceptHeader() throws Exception {
		EPackage validPackage = TestHelper.createTestEPackage("http://test.com/def/1.0", "DefPackage", "def");
		String xmiContent = TestHelper.serializeToXMI(validPackage, resourceSet);

		Response response = restClient.target(BASE_URL).path("jena/release/validate").request("text/plain")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(406, response.getStatus(), "Should return HTTP 406 Not Acceptable for unsupported Accept header");
	}

	// ========== ValidateByOclId Tests ==========

	@Test
	//@Disabled
	@JenaScopeServiceSetup
	public void testValidateByOclId_UnknownOclId_Returns400(
			@InjectBundleContext BundleContext context,
			@InjectService(filter = "(scope.name=jena)", timeout = 10000) ScopeService jenaScope)
			throws Exception {
		ensureResourceAvailability(context);

		Company company = DGFactory.eINSTANCE.createCompany();
		company.setName("Acme");
		String xmiContent = TestHelper.serializeToXMI(company, resourceSet);

		Response response = restClient.target(BASE_URL).path("jena/release/validate/non-existent-id")
				.request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(400, response.getStatus(), "Should return 400 when the OCL id is not found");
	}

	@Test
	//@Disabled
	@JenaScopeServiceSetup
	public void testValidateByOclId_ConstraintSetNotApplicable_Returns400(
			@InjectBundleContext BundleContext context,
			@InjectService(filter = "(scope.name=jena)", timeout = 10000) ScopeService jenaScope)
			throws Exception {
		ensureResourceAvailability(context);
		String coclId = "validate-ocl-not-applicable";
		TestHelper.uploadConstraintSet(jenaScope, coclId, buildValidationCoclSetForWrongModel(coclId));

		Company company = DGFactory.eINSTANCE.createCompany();
		company.setName("Acme");
		String xmiContent = TestHelper.serializeToXMI(company, resourceSet);

		Response response = restClient.target(BASE_URL).path("jena/release/validate/" + coclId)
				.request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(400, response.getStatus(), "Should return 400 when the constraint set is not applicable to the object's model");
	}

	@Test
	//@Disabled
	@JenaScopeServiceSetup
	public void testValidateByOclId_ValidObjectPassesConstraint_Returns200(
			@InjectBundleContext BundleContext context,
			@InjectService(filter = "(scope.name=jena)", timeout = 10000) ScopeService jenaScope)
			throws Exception {
		ensureResourceAvailability(context);
		String coclId = "validate-ocl-pass";
		TestHelper.uploadConstraintSet(jenaScope, coclId, buildValidationCoclSet(coclId));

		Company company = DGFactory.eINSTANCE.createCompany();
		company.setName("Acme");
		String xmiContent = TestHelper.serializeToXMI(company, resourceSet);

		Response response = restClient.target(BASE_URL).path("jena/release/validate/" + coclId)
				.request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(200, response.getStatus(), "Should return 200 when the object passes all constraints");
		assertNotNull(response.readEntity(String.class), "Response body should not be null");
	}

	@Test
	//@Disabled
	@JenaScopeServiceSetup
	public void testValidateByOclId_ObjectFailsConstraint_Returns200WithDiagnostics(
			@InjectBundleContext BundleContext context,
			@InjectService(filter = "(scope.name=jena)", timeout = 10000) ScopeService jenaScope)
			throws Exception {
		ensureResourceAvailability(context);
		String coclId = "validate-ocl-fail";
		TestHelper.uploadConstraintSet(jenaScope, coclId, buildValidationCoclSet(coclId));

		Company company = DGFactory.eINSTANCE.createCompany();
		// name is null → constraint "self.name <> null" fails
		String xmiContent = TestHelper.serializeToXMI(company, resourceSet);

		Response response = restClient.target(BASE_URL).path("jena/release/validate/" + coclId)
				.request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(200, response.getStatus(), "Should return 200 even when constraints fail (errors are in diagnostics)");
		String body = response.readEntity(String.class);
		assertNotNull(body, "Response body should not be null");
		assertTrue(body.contains("diagnostics"), "Response should contain diagnostics");
	}

	// ========== Compute Tests (no C-OCL) ==========

	@Test
	//@Disabled
	public void testCompute_NoValidationObjects() throws Exception {
		OperationValidationRequest request = buildComputeRequest(null, null);

		Response response = postComputeRequest(request);

		assertEquals(400, response.getStatus(), "Should return 400 when no validation objects are provided");
	}

	@Test
	//@Disabled
	public void testCompute_TooManyValidationObjects() throws Exception {
		EOperation op = companyOperation("getTotalEmployees");
		OperationValidationRequest request = buildComputeRequest(DGFactory.eINSTANCE.createCompany(), op);
		request.getValidationObjects().add(DGFactory.eINSTANCE.createCompany());

		Response response = postComputeRequest(request);

		assertEquals(400, response.getStatus(), "Should return 400 when more than one validation object is provided");
	}

	@Test
	//@Disabled
	public void testCompute_OperationNotFoundInEClass() throws Exception {
		EOperation unknownOp = EcorePackage.eINSTANCE.getEClass().getEOperations().get(0);
		OperationValidationRequest request = buildComputeRequest(DGFactory.eINSTANCE.createCompany(), unknownOp);

		Response response = postComputeRequest(request);

		assertEquals(400, response.getStatus(), "Should return 400 when the operation name is not found in the object EClass");
	}

	@Test
	//@Disabled
	public void testCompute_WrongReturnType() throws Exception {
		EOperation op = mismatchOperation("getTotalEmployees", EcorePackage.Literals.ESTRING);
		OperationValidationRequest request = buildComputeRequest(DGFactory.eINSTANCE.createCompany(), op);

		Response response = postComputeRequest(request);

		assertEquals(400, response.getStatus(), "Should return 400 when the operation return type does not match");
	}

	@Test
	//@Disabled
	public void testCompute_WrongParamCount() throws Exception {
		EParameter p1 = param("namePrefix", EcorePackage.Literals.ESTRING);
		EParameter p2 = param("extra", EcorePackage.Literals.ESTRING);
		EOperation op = mismatchOperation("findEmployeesByNamePrefix", EcorePackage.Literals.ESTRING, p1, p2);
		OperationValidationRequest request = buildComputeRequest(DGFactory.eINSTANCE.createCompany(), op);

		Response response = postComputeRequest(request);
		String entity = response.readEntity(String.class);
		System.out.println(entity);
		assertEquals(400, response.getStatus(), "Should return 400 when the operation parameter count does not match");
	}

	@Test
	//@Disabled
	public void testCompute_WrongParamType() throws Exception {
		EParameter p = param("namePrefix", EcorePackage.Literals.EINT);
		EOperation op = mismatchOperation("findEmployeesByNamePrefix", EcorePackage.Literals.ESTRING, p);
		OperationValidationRequest request = buildComputeRequest(DGFactory.eINSTANCE.createCompany(), op);

		Response response = postComputeRequest(request);

		assertEquals(400, response.getStatus(), "Should return 400 when an operation parameter type does not match");
	}

	@Test
	//@Disabled
	public void testCompute_ValidOperation_Returns200() throws Exception {
		EOperation op = companyOperation("getTotalEmployees");
		OperationValidationRequest request = buildComputeRequest(DGFactory.eINSTANCE.createCompany(), op);

		Response response = postComputeRequest(request);

		assertEquals(200, response.getStatus(), "Should return 200 when the operation is valid and invocation succeeds");
		assertNotNull(response.readEntity(String.class), "Response body should not be null");
	}

	@Test
	//@Disabled
	public void testCompute_ValidOperationWithParameters_Returns200() throws Exception {
		EOperation op = companyOperation("findEmployeesByNamePrefix");
		OperationValidationRequest request = buildComputeRequest(DGFactory.eINSTANCE.createCompany(), op);

		OperationRequestParameter nameParam = COCLFactory.eINSTANCE.createOperationRequestParameter();
		nameParam.setParameter(op.getEParameters().get(0));
		nameParam.setJavaValue("A");
		request.getParameters().add(nameParam);

		Response response = postComputeRequest(request);

		assertEquals(200, response.getStatus(), "Should return 200 when the operation with parameters is valid and invocation succeeds");
		assertNotNull(response.readEntity(String.class), "Response body should not be null");
	}

	// ========== Compute with C-OCL Tests ==========

	@Test
	//@Disabled
	@JenaScopeServiceSetup
	public void testComputeWithCocl_MissingOperationName_Returns400(
			@InjectBundleContext BundleContext context,
			@InjectService(filter = "(scope.name=jena)", timeout = 10000) ScopeService jenaScope)
			throws Exception {
		ensureResourceAvailability(context);
		String coclId = "compute-ocl-missing-name";
		TestHelper.uploadConstraintSet(jenaScope, coclId, buildOperationCoclSet(coclId));

		OperationValidationRequest request = COCLFactory.eINSTANCE.createOperationValidationRequest();
		request.getValidationObjects().add(DGFactory.eINSTANCE.createCompany());
		request.setCoclId(coclId);
		// operationName intentionally omitted

		Response response = postComputeRequest(request);

		assertEquals(400, response.getStatus(), "Should return 400 when coclId is set but operationName is blank");
	}

	@Test
	//@Disabled
	@JenaScopeServiceSetup
	public void testComputeWithCocl_NoMatchingOperationConstraint_Returns400(
			@InjectBundleContext BundleContext context,
			@InjectService(filter = "(scope.name=jena)", timeout = 10000) ScopeService jenaScope)
			throws Exception {
		ensureResourceAvailability(context);
		String coclId = "compute-ocl-no-match";
		TestHelper.uploadConstraintSet(jenaScope, coclId, buildOperationCoclSet(coclId));

		OperationValidationRequest request = COCLFactory.eINSTANCE.createOperationValidationRequest();
		request.getValidationObjects().add(DGFactory.eINSTANCE.createCompany());
		request.setCoclId(coclId);
		request.setOperationName("unknownOperation");

		Response response = postComputeRequest(request);

		assertEquals(400, response.getStatus(), "Should return 400 when no OPERATION constraint matches the requested name");
	}

	@Test
	//@Disabled
	@JenaScopeServiceSetup
	public void testComputeWithCocl_ValidOperation_Returns200(
			@InjectBundleContext BundleContext context,
			@InjectService(filter = "(scope.name=jena)", timeout = 10000) ScopeService jenaScope)
			throws Exception {
		ensureResourceAvailability(context);
		String coclId = "compute-ocl-valid";
		TestHelper.uploadConstraintSet(jenaScope, coclId, buildOperationCoclSet(coclId));

		OperationValidationRequest request = COCLFactory.eINSTANCE.createOperationValidationRequest();
		request.getValidationObjects().add(DGFactory.eINSTANCE.createCompany());
		request.setCoclId(coclId);
		request.setOperationName("getTotalEmployees");

		Response response = postComputeRequest(request);

		assertEquals(200, response.getStatus(), "Should return 200 when OPERATION constraint is found and evaluated");
		assertNotNull(response.readEntity(String.class), "Response body should not be null");
	}

	// ========== Derive Tests (no C-OCL) ==========

	@Test
	//@Disabled
	public void testDerive_NoValidationObjects() throws Exception {
		DerivedValidationRequest request = buildDeriveRequest(null);
		Response response = postDeriveRequest(request);
		assertEquals(400, response.getStatus(), "Should return 400 when no validation objects are provided");
	}

	@Test
	//@Disabled
	public void testDerive_TooManyValidationObjects() throws Exception {
		EStructuralFeature nameFeature = DGPackage.eINSTANCE.getCompany().getEStructuralFeature("name");
		DerivedValidationRequest request = buildDeriveRequest(DGFactory.eINSTANCE.createCompany(), nameFeature);
		request.getValidationObjects().add(DGFactory.eINSTANCE.createCompany());
		Response response = postDeriveRequest(request);
		assertEquals(400, response.getStatus(), "Should return 400 when more than one validation object is provided");
	}

	@Test
	//@Disabled
	public void testDerive_FeatureNotInEClass() throws Exception {
		EStructuralFeature personFeature = DGPackage.eINSTANCE.getPerson().getEStructuralFeature("firstName");
		DerivedValidationRequest request = buildDeriveRequest(DGFactory.eINSTANCE.createCompany(), personFeature);
		Response response = postDeriveRequest(request);
		assertEquals(400, response.getStatus(), "Should return 400 when the feature does not belong to the object EClass");
	}

	@Test
	//@Disabled
	public void testDerive_SimpleFeature_Returns200() throws Exception {
		EStructuralFeature nameFeature = DGPackage.eINSTANCE.getCompany().getEStructuralFeature("name");
		DerivedValidationRequest request = buildDeriveRequest(DGFactory.eINSTANCE.createCompany(), nameFeature);
		Response response = postDeriveRequest(request);
		assertEquals(200, response.getStatus(), "Should return 200 for a valid EDataType feature");
		assertNotNull(response.readEntity(String.class), "Response body should not be null");
	}

	@Test
	//@Disabled
	public void testDerive_EObjectFeature_Returns200() throws Exception {
		Company company = DGFactory.eINSTANCE.createCompany();
		Address address = DGFactory.eINSTANCE.createAddress();
		company.setAddress(address);
		EStructuralFeature addressFeature = DGPackage.eINSTANCE.getCompany().getEStructuralFeature("address");
		DerivedValidationRequest request = buildDeriveRequest(company, addressFeature);
		Response response = postDeriveRequest(request);
		assertEquals(200, response.getStatus(), "Should return 200 for a valid EClass feature");
		assertNotNull(response.readEntity(String.class), "Response body should not be null");
	}

	@Test
	//@Disabled
	public void testDerive_ManyEObjectFeature_Returns200() throws Exception {
		Company company = DGFactory.eINSTANCE.createCompany();
		EStructuralFeature employeesFeature = DGPackage.eINSTANCE.getCompany().getEStructuralFeature("employees");
		DerivedValidationRequest request = buildDeriveRequest(company, employeesFeature);
		Response response = postDeriveRequest(request);
		assertEquals(200, response.getStatus(), "Should return 200 for a many-valued EClass feature");
		assertNotNull(response.readEntity(String.class), "Response body should not be null");
	}

	@Test
	//@Disabled
	public void testDerive_ManyEDataTypeFeature_Returns200() throws Exception {
		Company company = DGFactory.eINSTANCE.createCompany();
		EStructuralFeature employeesNamesFeature = DGPackage.eINSTANCE.getCompany().getEStructuralFeature("employeesNames");
		DerivedValidationRequest request = buildDeriveRequest(company, employeesNamesFeature);
		Response response = postDeriveRequest(request);
		assertEquals(200, response.getStatus(), "Should return 200 for a many-valued EDataType feature");
		assertNotNull(response.readEntity(String.class), "Response body should not be null");
	}

	// ========== Derive with C-OCL Tests ==========

	@Test
	//@Disabled
	@JenaScopeServiceSetup
	public void testDeriveWithCocl_NoMatchingDerivedConstraint_Returns200WithWarn(
			@InjectBundleContext BundleContext context,
			@InjectService(filter = "(scope.name=jena)", timeout = 10000) ScopeService jenaScope)
			throws Exception {
		ensureResourceAvailability(context);
		String coclId = "derive-ocl-no-match";
		TestHelper.uploadConstraintSet(jenaScope, coclId, buildEmptyDerivedCoclSet(coclId));

		EStructuralFeature nameFeature = DGPackage.eINSTANCE.getCompany().getEStructuralFeature("name");
		DerivedValidationRequest request = buildDeriveRequest(DGFactory.eINSTANCE.createCompany(), nameFeature);

		Response response = postDeriveRequest(request, coclId);

		assertEquals(200, response.getStatus(), "Should return 200 with WARN when no DERIVED constraint is found for the feature");
		String body = response.readEntity(String.class);
		assertNotNull(body, "Response body should not be null");
		assertTrue(body.contains("diagnostics"), "Response should contain diagnostics");
	}

	@Test
	//@Disabled
	@JenaScopeServiceSetup
	public void testDeriveWithCocl_ValidDerivedConstraint_Returns200(
			@InjectBundleContext BundleContext context,
			@InjectService(filter = "(scope.name=jena)", timeout = 10000) ScopeService jenaScope)
			throws Exception {
		ensureResourceAvailability(context);
		String coclId = "derive-ocl-valid";
		TestHelper.uploadConstraintSet(jenaScope, coclId, buildDerivedCoclSet(coclId, "name", "self.name"));

		Company company = DGFactory.eINSTANCE.createCompany();
		company.setName("Acme");
		EStructuralFeature nameFeature = DGPackage.eINSTANCE.getCompany().getEStructuralFeature("name");
		DerivedValidationRequest request = buildDeriveRequest(company, nameFeature);

		Response response = postDeriveRequest(request, coclId);

		assertEquals(200, response.getStatus(), "Should return 200 when DERIVED constraint is found and evaluated");
		assertNotNull(response.readEntity(String.class), "Response body should not be null");
	}

	// ---- helpers ----

	private void ensureResourceAvailability(BundleContext context) throws InterruptedException {
		ResourceAware resourceAware = ResourceAware.create(context, "ObjectValidationResource");
		boolean resourceReady = resourceAware.waitForResource(15, TimeUnit.SECONDS);
		assertTrue(resourceReady, "ObjectValidationResource should be registered within 15 seconds.");
	}

	// ---- constraint set builders ----

	private OclConstraintSet buildValidationCoclSet(String id) {
		OclConstraintSet set = COCLFactory.eINSTANCE.createOclConstraintSet();
		set.setName(id);
		OclConstraint constraint = COCLFactory.eINSTANCE.createOclConstraint();
		constraint.setName("CompanyNameNotNull");
		constraint.setContextClass(DGE_COMPANY_CLASS_URI);
		constraint.setExpression("self.name <> null");
		constraint.setRole(OclRole.VALIDATION);
		constraint.setSeverity(Severity.ERROR);
		constraint.setActive(true);
		set.getConstraints().add(constraint);
		return set;
	}

	private OclConstraintSet buildValidationCoclSetForWrongModel(String id) {
		OclConstraintSet set = buildValidationCoclSet(id);
		set.getTargetModelNsURIs().add("http://wrong.model/1.0");
		return set;
	}

	private OclConstraintSet buildOperationCoclSet(String id) {
		OclConstraintSet set = COCLFactory.eINSTANCE.createOclConstraintSet();
		set.setName(id);
		OclConstraint constraint = COCLFactory.eINSTANCE.createOclConstraint();
		constraint.setName("GetTotalEmployees");
		constraint.setContextClass(DGE_COMPANY_CLASS_URI);
		constraint.setExpression("self.employees->size()");
		constraint.setRole(OclRole.OPERATION);
		constraint.setOperationName("getTotalEmployees");
		constraint.setOperationReturnType(OperationReturnType.JAVA_OBJECT);
		constraint.setSeverity(Severity.INFO);
		constraint.setActive(true);
		set.getConstraints().add(constraint);
		return set;
	}

	private OclConstraintSet buildEmptyDerivedCoclSet(String id) {
		OclConstraintSet set = COCLFactory.eINSTANCE.createOclConstraintSet();
		set.setName(id);
		return set;
	}

	private OclConstraintSet buildDerivedCoclSet(String id, String featureName, String expression) {
		OclConstraintSet set = COCLFactory.eINSTANCE.createOclConstraintSet();
		set.setName(id);
		OclConstraint constraint = COCLFactory.eINSTANCE.createOclConstraint();
		constraint.setName("DeriveFeature");
		constraint.setContextClass(DGE_COMPANY_CLASS_URI);
		constraint.setExpression(expression);
		constraint.setRole(OclRole.DERIVED);
		constraint.setFeatureName(featureName);
		constraint.setSeverity(Severity.INFO);
		constraint.setActive(true);
		set.getConstraints().add(constraint);
		return set;
	}

	// ---- compute helpers ----

	private EOperation companyOperation(String name) {
		return DGPackage.eINSTANCE.getCompany().getEOperations().stream()
				.filter(o -> name.equals(o.getName()))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Operation not found: " + name));
	}

	private EParameter param(String name, EClassifier type) {
		EParameter p = EcoreFactory.eINSTANCE.createEParameter();
		p.setName(name);
		p.setEType(type);
		return p;
	}

	/**
	 * Creates an EOperation with the given name and signature backed by a
	 * temporary resource so it can be serialized as a cross-reference in XMI.
	 * The resource is registered for cleanup in teardown.
	 */
	private EOperation mismatchOperation(String name, EClassifier returnType, EParameter... params) {
		String nsUri = "http://test.eclipse.fennec/mismatch/" + name + "/" + System.nanoTime();
		EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setNsURI(nsUri);
		pkg.setName("mismatch");
		pkg.setNsPrefix("mm");
		EClass cls = EcoreFactory.eINSTANCE.createEClass();
		cls.setName("Mismatch");
		pkg.getEClassifiers().add(cls);
		EOperation op = EcoreFactory.eINSTANCE.createEOperation();
		op.setName(name);
		op.setEType(returnType);
		for (EParameter p : params) op.getEParameters().add(p);
		cls.getEOperations().add(op);
		Resource r = new XMIResourceImpl(URI.createURI(nsUri));
		r.getContents().add(pkg);
		resourceSet.getResources().add(r);
		EPackage.Registry.INSTANCE.put(nsUri, pkg);
		resourceSet.getPackageRegistry().put(nsUri, pkg);
		tempResources.add(r);
		tempPackageUris.add(nsUri);
		return op;
	}

	private OperationValidationRequest buildComputeRequest(EObject validationObject, EOperation operation) {
		OperationValidationRequest request = COCLFactory.eINSTANCE.createOperationValidationRequest();
		request.setOperation(operation);
		if (validationObject != null) {
			request.getValidationObjects().add(validationObject);
		}
		return request;
	}

	private Response postComputeRequest(OperationValidationRequest request) throws Exception {
		String xmiContent = TestHelper.serializeToXMI(request, resourceSet);
		return restClient.target(BASE_URL).path("jena/release/validate/compute")
				.request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
	}

	// ---- derive helpers ----

	private DerivedValidationRequest buildDeriveRequest(EObject validationObject, EStructuralFeature... features) {
		DerivedValidationRequest request = COCLFactory.eINSTANCE.createDerivedValidationRequest();
		if (validationObject != null) {
			request.getValidationObjects().add(validationObject);
		}
		for (EStructuralFeature feature : features) {
			request.getDerivedFeature().add(feature);
		}
		return request;
	}

	private Response postDeriveRequest(DerivedValidationRequest request) throws Exception {
		String xmiContent = TestHelper.serializeToXMI(request, resourceSet);
		System.out.println(xmiContent);
		return restClient.target(BASE_URL).path("jena/release/validate/derive")
				.request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
	}

	private Response postDeriveRequest(DerivedValidationRequest request, String oclId) throws Exception {
		String xmiContent = TestHelper.serializeToXMI(request, resourceSet);
		return restClient.target(BASE_URL).path("jena/release/validate/derive")
				.queryParam("oclId", oclId)
				.request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
	}
}
