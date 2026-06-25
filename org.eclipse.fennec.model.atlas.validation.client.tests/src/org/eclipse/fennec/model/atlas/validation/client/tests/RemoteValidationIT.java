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
package org.eclipse.fennec.model.atlas.validation.client.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.model.atlas.datagen.example.model.dge.Company;
import org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGFactory;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.eclipse.fennec.model.atlas.validation.ValidationService;
import org.eclipse.fennec.model.atlas.validation.model.cocl.COCLFactory;
import org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole;
import org.eclipse.fennec.model.atlas.validation.model.cocl.Severity;
import org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * P5-5 — acceptance: the validation service runs <em>unchanged</em> against a <b>remote</b>
 * Atlas backend exposed by the client, exactly as it does in-process. This is the proof of
 * Goal 1 (contract-identical surface): only the OSGi wiring differs.
 * <p>
 * In-process the validation flow is covered by {@code ObjectValidationResourceTest}
 * (validation + the in-process {@code ScopeService}). Here the <em>same</em>
 * {@code ValidationServiceImpl} is exercised, but the {@code ReadableScopeService} it
 * resolves through the {@code ReadableScopeCollector} is the <b>remote</b> P5-4 publication
 * of {@code rest.client.osgi} (pointed at a live jena container) — no validation-bundle
 * change, only configuration.
 * <p>
 * Flow:
 * <ol>
 * <li>start the {@code jena} image (Testcontainers), with the cocl registry from
 * {@code docker/dockercompose/configs/jena.json};</li>
 * <li>drive the {@code rest.client.osgi} ConfigAdmin factory at the container →
 * {@code RemoteScopeServicePublisher} (P5-4) publishes a
 * {@code ReadableScopeService(atlas.scope=jena, atlas.remote=true)} the collector binds;</li>
 * <li>seed an {@code OclConstraintSet} into {@code jena/cocl} at the final stage through the
 * writable REST endpoint ({@code ObjectRegistryResource.createObject}) — the remote client is
 * read-only, so the object must exist on the server;</li>
 * <li>call {@code ValidationService.validateWithOcl(company, coclId, "jena", rs)}; the constraint
 * set is fetched from the <em>remote</em> scope (P5-0 content endpoint) and applied.</li>
 * </ol>
 * Skipped automatically when Docker or the local {@code jena-snapshot} image is absent (so a
 * normal build stays green); build the image and run {@code testOSGi} locally to exercise it.
 * Mirrors the container setup of {@code AtlasClientOsgiIT} / {@code JenaAtlasClientIT}.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class RemoteValidationIT {

	/** Factory PID of the OSGi Atlas client component (see {@code AtlasClientComponent.PID}). */
	private static final String PID = "org.eclipse.fennec.model.atlas.rest.client";

	private static final String IMAGE = "eclipsefennec/model.atlas:jena-snapshot";
	private static final int HTTP_PORT = 8080;
	private static final String JENA_SCOPE = "jena";
	private static final String COCL_REGISTRY = "cocl";
	/** jena's final (and writable) stage, per jena.json. */
	private static final String FINAL_STAGE = "release";
	private static final String DGE_COMPANY_CLASS_URI = "https://dg.de/1.0#//Company";

	private static final String CONFIG_LOAD_DIR = "/opt/modelatlas/runtime/load";
	private static final String STORAGE_ROOT = "/opt/modelatlas/runtime/data";
	private static final long SERVICE_WAIT_MS = 15_000L;

	private static GenericContainer<?> atlas;
	private static URI baseUri;

	@BeforeAll
	static void startAtlas() {
		assumeTrue(DockerClientFactory.instance().isDockerAvailable(),
				"Docker not available — skipping remote validation acceptance test");
		assumeTrue(atlasImageAvailableLocally(),
				"Image " + IMAGE + " not present locally — skipping (build it to run this IT)");
		atlas = new GenericContainer<>(IMAGE).withExposedPorts(HTTP_PORT)
				.withEnv("STORAGE_ROOT", STORAGE_ROOT)
				.withEnv("ATLAS_HTTP_PORT", String.valueOf(HTTP_PORT))
				.withFileSystemBind(resolveConfigsDir(), CONFIG_LOAD_DIR, BindMode.READ_ONLY)
				.waitingFor(Wait.forHttp("/atlas/rest/scopes").forPort(HTTP_PORT).forStatusCode(200)
						.forResponsePredicate(body -> body.contains(JENA_SCOPE)))
				.withStartupTimeout(Duration.ofMinutes(2));
		atlas.start();
		baseUri = URI.create("http://" + atlas.getHost() + ":" + atlas.getMappedPort(HTTP_PORT) + "/atlas/rest");
	}

	@AfterAll
	static void stopAtlas() {
		if (atlas != null) {
			atlas.stop();
			atlas.close();
		}
	}

	@Test
	public void validationRunsUnchangedAgainstRemoteScope(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID, name = "remote",
					location = "?")) Configuration configuration,
			@InjectService(cardinality = 0,
					filter = "(&(atlas.scope=jena)(atlas.remote=true))") ServiceAware<ReadableScopeService> remoteScope,
			@InjectService ServiceAware<ResourceSetFactory> resourceSetFactories,
			@InjectService ServiceAware<ValidationService> validationServices) throws Exception {

		// 1. Activate the remote client → P5-4 publishes ReadableScopeService(atlas.scope=jena).
		//    LAZY: no EPackage prefetch needed; scope publication is independent of the mode.
		Hashtable<String, Object> props = new Hashtable<>();
		props.put("base.uri", baseUri.toString());
		props.put("mode", "LAZY");
		props.put("scope.allow.list", new String[] { JENA_SCOPE });
		configuration.update(props);

		// 2. The remote scope publication appears (and is thus bound by the collector).
		assertNotNull(remoteScope.waitForService(SERVICE_WAIT_MS),
				"P5-4 should publish a remote ReadableScopeService for the jena scope");

		ResourceSetFactory rsf = resourceSetFactories.waitForService(SERVICE_WAIT_MS);
		assertNotNull(rsf, "an EMF ResourceSetFactory must be present");
		ResourceSet resourceSet = rsf.createResourceSet();

		ValidationService validation = validationServices.waitForService(SERVICE_WAIT_MS);
		assertNotNull(validation, "the ValidationService must be present");

		// 3. Seed an OclConstraintSet into jena/cocl at the final (writable) stage via REST.
		String coclId = "validate-company-name";
		int status = uploadObject(COCL_REGISTRY, FINAL_STAGE, coclId, toXmi(buildValidationCoclSet(coclId), resourceSet));
		assertTrue(status == 200 || status == 201, "constraint-set upload should succeed, was HTTP " + status);

		// 4a. A valid Company passes — the constraint set was fetched from the REMOTE scope and applied.
		Company valid = DGFactory.eINSTANCE.createCompany();
		valid.setName("Acme");
		ValidationResponse okResponse = validation.validateWithOcl(valid, coclId, JENA_SCOPE, resourceSet);
		assertNotNull(okResponse, "validation against the remote scope must produce a response");
		assertTrue(!hasConstraintFailure(okResponse, "self.name <> null"),
				"a Company with a name must satisfy the remotely-fetched constraint");

		// 4b. An invalid Company fails the same remotely-fetched constraint.
		Company invalid = DGFactory.eINSTANCE.createCompany(); // name == null
		ValidationResponse failResponse = validation.validateWithOcl(invalid, coclId, JENA_SCOPE, resourceSet);
		assertTrue(hasConstraintFailure(failResponse, "self.name <> null"),
				"a Company without a name must fail the remotely-fetched constraint");

		// 4c. An unknown id proves the lookup really hits the remote scope (nothing found → 400-equivalent).
		assertThrows(IllegalArgumentException.class,
				() -> validation.validateWithOcl(valid, "no-such-cocl", JENA_SCOPE, resourceSet),
				"an unknown C-OCL id must fail to resolve against the remote scope");
	}

	// ---- fixtures (same scenario as the in-process ObjectValidationResourceTest) ----

	private static OclConstraintSet buildValidationCoclSet(String id) {
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

	/** Serialize an OclConstraintSet to plain EMF XMI — the wire the writable endpoint accepts. */
	private static String toXmi(OclConstraintSet set, ResourceSet resourceSet) throws IOException {
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().putIfAbsent(
				Resource.Factory.Registry.DEFAULT_EXTENSION,
				new org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl());
		Resource resource = resourceSet
				.createResource(org.eclipse.emf.common.util.URI.createURI("mem://constraint-set.xmi"));
		resource.getContents().add(set);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		resource.save(out, null);
		resource.getContents().clear(); // leave the resourceSet clean for validation
		resourceSet.getResources().remove(resource);
		return out.toString(StandardCharsets.UTF_8);
	}

	/**
	 * POST an object to {@code /{jena}/registries/{registry}/stages/{stage}/{objectId}}
	 * ({@code ObjectRegistryResource.createObject}). The endpoint is declared {@code @POST @PUT},
	 * but Jersey registers only the first HTTP-method designator, so PUT 405s — POST it is.
	 */
	private static int uploadObject(String registry, String stage, String objectId, String xmi) throws Exception {
		URI target = URI.create(baseUri + "/" + JENA_SCOPE + "/registries/" + registry + "/stages/" + stage + "/"
				+ objectId + "?version=1.0&override=true");
		HttpResponse<String> response = HttpClient.newHttpClient().send(
				HttpRequest.newBuilder(target).header("Content-Type", "application/xmi")
						.POST(HttpRequest.BodyPublishers.ofString(xmi, StandardCharsets.UTF_8)).build(),
				HttpResponse.BodyHandlers.ofString());
		return response.statusCode();
	}

	/** Whether any diagnostic (recursively) is an ERROR carrying the failed constraint's expression. */
	private static boolean hasConstraintFailure(ValidationResponse response, String expression) {
		return containsFailure(response.getDiagnostics(), expression);
	}

	private static boolean containsFailure(List<Diagnostic> diagnostics, String expression) {
		for (Diagnostic diagnostic : diagnostics) {
			boolean self = Severity.ERROR.equals(diagnostic.getType()) && diagnostic.getMessage() != null
					&& diagnostic.getMessage().contains(expression);
			if (self || containsFailure(diagnostic.getChildren(), expression)) {
				return true;
			}
		}
		return false;
	}

	// ---- container helpers (mirror AtlasClientOsgiIT) ----

	private static boolean atlasImageAvailableLocally() {
		try {
			Process probe = new ProcessBuilder("docker", "image", "inspect", IMAGE)
					.redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD)
					.start();
			if (!probe.waitFor(20, TimeUnit.SECONDS)) {
				probe.destroyForcibly();
				return false;
			}
			return probe.exitValue() == 0;
		} catch (IOException unavailable) {
			return false;
		} catch (InterruptedException interrupted) {
			Thread.currentThread().interrupt();
			return false;
		}
	}

	private static String resolveConfigsDir() {
		Path dir = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
		for (Path p = dir; p != null; p = p.getParent()) {
			Path candidate = p.resolve("docker/dockercompose/configs");
			if (Files.isDirectory(candidate)) {
				return candidate.toString();
			}
		}
		throw new IllegalStateException("Could not locate docker/dockercompose/configs starting from " + dir);
	}
}
