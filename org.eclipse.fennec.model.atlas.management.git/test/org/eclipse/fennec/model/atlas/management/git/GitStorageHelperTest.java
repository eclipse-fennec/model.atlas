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
package org.eclipse.fennec.model.atlas.management.git;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.workflow.ResourceSetCollector;
import org.gecko.jgit.api.GitService;
import org.gecko.jgit.api.TreeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.osgi.service.component.ComponentServiceObjects;

/**
 * Unit tests for {@link GitStorageHelper}. A Mockito {@link GitService} feeds a
 * canned tree + blob; a real {@link ResourceSet} exercises the derived-metadata
 * path and, via {@code loadEObject}, the {@link GitURIHandler} end-to-end.
 */
class GitStorageHelperTest {

	private static final String COMMIT = "c1";
	private static final String ECORE_PATH = "models/person.ecore";
	private static final String EPACKAGE_TYPE = EcoreUtil.getURI(EcorePackage.Literals.EPACKAGE).toString();
	private static final String EOBJECT_TYPE = EcoreUtil.getURI(EcorePackage.Literals.EOBJECT).toString();

	private static final String WIDGET_NS = "http://example.org/widget";
	private static final String WIDGET_PATH = "data/w1.xmi";
	private static final String WIDGET_TYPE = WIDGET_NS + "#//Widget";
	private static final String WIDGET_XMI =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<widget:Widget xmlns:widget=\"" + WIDGET_NS + "\"/>\n";

	private static final String PERSON_ECORE = """
			<?xml version="1.0" encoding="UTF-8"?>
			<ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
			    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			    xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
			    name="person" nsURI="http://example.org/person" nsPrefix="person">
			  <eClassifiers xsi:type="ecore:EClass" name="Person"/>
			</ecore:EPackage>
			""";

	private ResourceSet resourceSet;
	private GitService gitService;
	private EObjectRegistryService<EObject> registry;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setup() {
		resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);

		gitService = mock(GitService.class);
		when(gitService.getBranch()).thenReturn("main");
		when(gitService.getFiles()).thenReturn(new TreeResult(COMMIT, List.of(ECORE_PATH, "README.md")));
		when(gitService.readFile(eq(COMMIT), eq(ECORE_PATH)))
				.thenAnswer(inv -> new ByteArrayInputStream(PERSON_ECORE.getBytes(StandardCharsets.UTF_8)));

		registry = mock(EObjectRegistryService.class);
	}

	private GitStorageHelper helper(Map<String, String> typeToRegistry) {
		// null collector -> reads fall back to the management resourceSet
		return new GitStorageHelper(resourceSet, List.of(gitService), "jena", typeToRegistry, registry, null);
	}

	@Test
	void deriveMetadata_stampsAllReplayFields_andPrimesCache() throws Exception {
		helper(Map.of(EPACKAGE_TYPE, "schema"));

		ArgumentCaptor<ObjectMetadata> captor = ArgumentCaptor.forClass(ObjectMetadata.class);
		verify(registry).updateCache(captor.capture());
		ObjectMetadata md = captor.getValue();
		assertEquals("jena/main/" + ECORE_PATH, md.getObjectId(), "objectId = scope/stage/repoPath (D9)");
		assertEquals("jena", md.getScope());
		assertEquals("schema", md.getRegistry());
		assertEquals("main", md.getStage(), "stage = branch");
		assertEquals(EPACKAGE_TYPE, md.getObjectType());
		assertEquals(COMMIT, md.getVersion(), "version = commit id");
	}

	@Test
	void unrecognizedExtension_isIgnored() throws Exception {
		// README.md has no registered factory -> never derived, readFile never called for it.
		helper(Map.of(EPACKAGE_TYPE, "schema"));
		verify(gitService, never()).readFile(eq(COMMIT), eq("README.md"));
	}

	@Test
	void unmappedType_isIgnored() throws Exception {
		GitStorageHelper h = helper(Map.of()); // empty type->registry map
		verify(registry, never()).updateCache(any());
		assertTrue(h.listObjectIds("jena", "schema", "main").isEmpty());
	}

	@Test
	void listObjectIds_filtersByStageAndRegistry() throws Exception {
		GitStorageHelper h = helper(Map.of(EPACKAGE_TYPE, "schema"));
		assertEquals(List.of("jena/main/" + ECORE_PATH), h.listObjectIds("jena", "schema", "main"));
		assertTrue(h.listObjectIds("jena", "other-registry", "main").isEmpty());
		assertTrue(h.listObjectIds("jena", "schema", "no-such-stage").isEmpty());
	}

	@Test
	void storageExists_and_findObjectPath() throws Exception {
		GitStorageHelper h = helper(Map.of(EPACKAGE_TYPE, "schema"));
		// storageExists takes the repo path; findObjectPath takes the qualified objectId
		// and returns the (stripped) repo path.
		assertTrue(h.storageExists("jena", "schema", "main", ECORE_PATH));
		assertFalse(h.storageExists("jena", "schema", "main", "models/missing.ecore"));
		assertEquals(ECORE_PATH, h.findObjectPath("jena", "schema", "main", "jena/main/" + ECORE_PATH));
		assertNull(h.findObjectPath("jena", "schema", "main", "jena/main/models/missing.ecore"));
	}

	@Test
	void loadEObject_resolvesThroughGitUriHandler() throws Exception {
		GitStorageHelper h = helper(Map.of(EPACKAGE_TYPE, "schema"));
		EObject loaded = h.loadEObject("jena", "schema", "main", "jena/main/" + ECORE_PATH);
		assertInstanceOf(EPackage.class, loaded);
		assertEquals("http://example.org/person", ((EPackage) loaded).getNsURI());
	}

	@Test
	void loadMetadata_servesDerivedCopy() throws Exception {
		GitStorageHelper h = helper(Map.of(EPACKAGE_TYPE, "schema"));
		ObjectMetadata md = h.loadMetadata("jena", "schema", "main", "jena/main/" + ECORE_PATH);
		assertEquals("jena/main/" + ECORE_PATH, md.getObjectId());
		assertEquals("main", md.getStage());
		assertNull(h.loadMetadata("jena", "schema", "main", "jena/main/models/missing.ecore"));
	}

	@Test
	void writeMethods_throwReadOnly() throws Exception {
		GitStorageHelper h = helper(Map.of(EPACKAGE_TYPE, "schema"));
		assertThrows(UnsupportedOperationException.class,
				() -> h.deleteObject("jena", "schema", "main", ECORE_PATH));
		assertThrows(UnsupportedOperationException.class, () -> h.persistResource(ECORE_PATH, null));
	}

	// --- EObject catch-all routing for instances ----------------------------

	@Test
	void instance_routedToObjectRegistry_viaConfiguredEObjectCatchAll() throws Exception {
		GitService gs = instanceGitService();
		// EPackage -> schema (exact), EObject -> object (explicit catch-all).
		// null collector: parses against the management resourceSet, which here has the Widget package.
		new GitStorageHelper(resourceSet, List.of(gs), "jena",
				Map.of(EPACKAGE_TYPE, "schema", EOBJECT_TYPE, "object"), registry, null);

		ArgumentCaptor<ObjectMetadata> captor = ArgumentCaptor.forClass(ObjectMetadata.class);
		verify(registry).updateCache(captor.capture());
		ObjectMetadata md = captor.getValue();
		assertEquals("jena/main/" + WIDGET_PATH, md.getObjectId());
		assertEquals("object", md.getRegistry(), "instance falls through exact->EObject catch-all");
		assertEquals(WIDGET_TYPE, md.getObjectType(), "objectType = the instance's own eClass URI");
	}

	@Test
	void instance_skipped_whenNoCatchAllConfigured() throws Exception {
		GitService gs = instanceGitService();
		// No EObject entry: an instance whose exact type is unmapped is ignored,
		// never routed to a hardcoded default.
		new GitStorageHelper(resourceSet, List.of(gs), "jena", Map.of(EPACKAGE_TYPE, "schema"), registry, null);
		verify(registry, never()).updateCache(any());
	}

	/**
	 * A GitService serving a single Widget instance file, with the Widget package
	 * and an XMI factory registered on the shared management resource set so it parses.
	 */
	private GitService instanceGitService() {
		registerWidgetPackage(resourceSet);
		return widgetGitService();
	}

	/** Registers the Widget package (resource URI = nsURI) + XMI factory on {@code rs}. */
	private void registerWidgetPackage(ResourceSet rs) {
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		EPackage widget = EcoreFactory.eINSTANCE.createEPackage();
		widget.setName("widget");
		widget.setNsURI(WIDGET_NS);
		widget.setNsPrefix("widget");
		EClass widgetClass = EcoreFactory.eINSTANCE.createEClass();
		widgetClass.setName("Widget");
		widget.getEClassifiers().add(widgetClass);
		// Give the package a resource URI = nsURI, as registered packages have in
		// production, so EcoreUtil.getURI(eClass) yields "nsURI#//Widget".
		ResourceImpl pkgResource = new ResourceImpl(URI.createURI(WIDGET_NS));
		pkgResource.getContents().add(widget);
		rs.getResources().add(pkgResource);
		rs.getPackageRegistry().put(WIDGET_NS, widget);
	}

	private GitService widgetGitService() {
		GitService gs = mock(GitService.class);
		when(gs.getBranch()).thenReturn("main");
		when(gs.getFiles()).thenReturn(new TreeResult(COMMIT, List.of(WIDGET_PATH)));
		when(gs.readFile(eq(COMMIT), eq(WIDGET_PATH)))
				.thenAnswer(inv -> new ByteArrayInputStream(WIDGET_XMI.getBytes(StandardCharsets.UTF_8)));
		return gs;
	}

	// --- G5: leased per-stage ResourceSet + rederive timing -----------------

	@Test
	@SuppressWarnings("unchecked")
	void instance_derivedViaLeasedPerStageResourceSet_onceAvailable() throws Exception {
		// Management RS recognizes .xmi (so the file isn't filtered out) but does NOT
		// have the Widget package -> the instance can only be parsed via a leased
		// per-(scope,stage) ResourceSet that does.
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi",
				new XMIResourceFactoryImpl());

		ResourceSet stageRs = new ResourceSetImpl();
		registerWidgetPackage(stageRs);
		ComponentServiceObjects<ResourceSet> cso = mock(ComponentServiceObjects.class);
		when(cso.getService()).thenReturn(stageRs);

		ResourceSetCollector collector = mock(ResourceSetCollector.class);
		when(collector.getResourceSetObjects("jena", "main")).thenReturn(null); // not available yet

		GitStorageHelper h = new GitStorageHelper(resourceSet, List.of(widgetGitService()), "jena",
				Map.of(EOBJECT_TYPE, "object"), registry, collector);

		// Cold start: no per-stage RS, management RS lacks the package -> skipped.
		verify(registry, never()).updateCache(any());

		// The per-stage ResourceSet (with the Widget package) becomes available.
		when(collector.getResourceSetObjects("jena", "main")).thenReturn(cso);
		h.rederive();

		ArgumentCaptor<ObjectMetadata> captor = ArgumentCaptor.forClass(ObjectMetadata.class);
		verify(registry).updateCache(captor.capture());
		assertEquals("jena/main/" + WIDGET_PATH, captor.getValue().getObjectId());
		assertEquals("object", captor.getValue().getRegistry());
		assertEquals(WIDGET_TYPE, captor.getValue().getObjectType());
		verify(cso).ungetService(stageRs); // lease returned
	}
}
