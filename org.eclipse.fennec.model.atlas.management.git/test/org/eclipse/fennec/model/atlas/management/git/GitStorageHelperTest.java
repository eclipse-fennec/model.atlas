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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
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
		assertEquals(sha256Hex(PERSON_ECORE), md.getContentHash(), "contentHash = SHA-256 of the blob bytes");
		assertEquals("http://example.org/person", md.getProperties().get("nsUri"),
				"schema metadata carries the nsUri property (like the schema upload path)");
		assertNotNull(md.getFingerprint(), "schema metadata carries the model fingerprint (F4 producer)");
		assertTrue(md.getFingerprint().startsWith("fp1:"),
				"fingerprint uses the current scheme tag, was: " + md.getFingerprint());
	}

	private static String sha256Hex(String content) throws Exception {
		return HexFormat.of().formatHex(
				MessageDigest.getInstance("SHA-256").digest(content.getBytes(StandardCharsets.UTF_8)));
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
		try(GitStorageHelper gsh = new GitStorageHelper(resourceSet, List.of(gs), "jena",
				Map.of(EPACKAGE_TYPE, "schema", EOBJECT_TYPE, "object"), registry, null)) {
			ArgumentCaptor<ObjectMetadata> captor = ArgumentCaptor.forClass(ObjectMetadata.class);
			verify(registry).updateCache(captor.capture());
			ObjectMetadata md = captor.getValue();
			assertEquals("jena/main/" + WIDGET_PATH, md.getObjectId());
			assertEquals("object", md.getRegistry(), "instance falls through exact->EObject catch-all");
			assertEquals(WIDGET_TYPE, md.getObjectType(), "objectType = the instance's own eClass URI");
		}

		
	}

	@Test
	void instance_skipped_whenNoCatchAllConfigured() throws Exception {
		GitService gs = instanceGitService();
		// No EObject entry: an instance whose exact type is unmapped is ignored,
		// never routed to a hardcoded default.
		try(GitStorageHelper gsh = new GitStorageHelper(resourceSet, List.of(gs), "jena", Map.of(EPACKAGE_TYPE, "schema"), registry, null)) {
			verify(registry, never()).updateCache(any());
		}
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

		try(GitStorageHelper h = new GitStorageHelper(resourceSet, List.of(widgetGitService()), "jena",
				Map.of(EOBJECT_TYPE, "object"), registry, collector)) {
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

	// --- G7: reconcile (webhook / poll resync) ------------------------------

	@Test
	void reconcile_tipUnchanged_isNoOp() throws Exception {
		// getFiles keeps returning the same COMMIT tree -> nothing to do.
		GitStorageHelper h = helper(Map.of(EPACKAGE_TYPE, "schema"));
		verify(registry, times(1)).updateCache(any()); // construction derive

		assertFalse(h.reconcile("main"), "unchanged tip -> false");
		verify(registry, never()).removeFromCache(any());
		verify(registry, times(1)).updateCache(any()); // still just the construction derive
	}

	@Test
	void reconcile_tipMoved_evictsAndRederivesWithNewVersion() throws Exception {
		TreeResult tree1 = new TreeResult(COMMIT, List.of(ECORE_PATH, "README.md"));
		TreeResult tree2 = new TreeResult("c2", List.of(ECORE_PATH, "README.md"));
		when(gitService.getFiles()).thenReturn(tree1, tree2);
		when(gitService.readFile(eq("c2"), eq(ECORE_PATH)))
				.thenAnswer(inv -> new ByteArrayInputStream(PERSON_ECORE.getBytes(StandardCharsets.UTF_8)));

		GitStorageHelper h = helper(Map.of(EPACKAGE_TYPE, "schema"));

		assertTrue(h.reconcile("main"), "moved tip -> true");
		verify(registry).removeFromCache("jena/main/" + ECORE_PATH);

		ArgumentCaptor<ObjectMetadata> captor = ArgumentCaptor.forClass(ObjectMetadata.class);
		verify(registry, times(2)).updateCache(captor.capture());
		assertEquals(COMMIT, captor.getAllValues().get(0).getVersion());
		assertEquals("c2", captor.getAllValues().get(1).getVersion(), "re-derived at the new tip commit");
		// still present, now versioned at the new tip
		assertEquals(List.of("jena/main/" + ECORE_PATH), h.listObjectIds("jena", "schema", "main"));
	}

	@Test
	void reconcile_tipMoved_firesOnReconciledListener() throws Exception {
		TreeResult tree1 = new TreeResult(COMMIT, List.of(ECORE_PATH, "README.md"));
		TreeResult tree2 = new TreeResult("c2", List.of(ECORE_PATH, "README.md"));
		when(gitService.getFiles()).thenReturn(tree1, tree2);
		when(gitService.readFile(eq("c2"), eq(ECORE_PATH)))
				.thenAnswer(inv -> new ByteArrayInputStream(PERSON_ECORE.getBytes(StandardCharsets.UTF_8)));

		GitStorageHelper h = helper(Map.of(EPACKAGE_TYPE, "schema"));
		int[] fired = { 0 };
		List<List<String>> removedSeen = new ArrayList<>();
		h.setOnReconciled((stage, removed) -> {
			fired[0]++;
			removedSeen.add(removed);
		});

		assertTrue(h.reconcile("main"), "moved tip -> true");
		assertEquals(1, fired[0], "the reconcile listener fires once when the branch tip moves (D8-D resync)");
		assertEquals(List.of(List.of()), removedSeen, "no schema removed -> empty removed list (change, not removal)");
	}

	@Test
	void reconcile_tipUnchanged_doesNotFireListener() throws Exception {
		GitStorageHelper h = helper(Map.of(EPACKAGE_TYPE, "schema"));
		int[] fired = { 0 };
		h.setOnReconciled((stage, removed) -> fired[0]++);

		assertFalse(h.reconcile("main"), "unchanged tip -> false");
		assertEquals(0, fired[0], "the reconcile listener must not fire when nothing changed");
	}

	@Test
	void reconcile_removedSchema_reportedToListenerForExit() throws Exception {
		// person.ecore present at COMMIT, gone at c2 -> its (qualified) objectId is a removed schema.
		TreeResult tree1 = new TreeResult(COMMIT, List.of(ECORE_PATH));
		TreeResult tree2 = new TreeResult("c2", List.of("README.md"));
		when(gitService.getFiles()).thenReturn(tree1, tree2);

		GitStorageHelper h = helper(Map.of(EPACKAGE_TYPE, "schema"));
		List<String> removedSeen = new ArrayList<>();
		h.setOnReconciled((stage, removed) -> removedSeen.addAll(removed));

		assertTrue(h.reconcile("main"), "moved tip -> true");
		assertEquals(List.of("jena/main/" + ECORE_PATH), removedSeen,
				"the removed schema's qualified objectId is reported so the resync can drive EXIT");
	}

	@Test
	void reconcile_removedFile_isEvictedAndNotRederived() throws Exception {
		TreeResult tree1 = new TreeResult(COMMIT, List.of(ECORE_PATH));
		TreeResult tree2 = new TreeResult("c2", List.of("README.md")); // person.ecore deleted
		when(gitService.getFiles()).thenReturn(tree1, tree2);

		GitStorageHelper h = helper(Map.of(EPACKAGE_TYPE, "schema"));
		assertEquals(1, h.listObjectIds("jena", "schema", "main").size());

		assertTrue(h.reconcile("main"));
		verify(registry).removeFromCache("jena/main/" + ECORE_PATH);
		assertTrue(h.listObjectIds("jena", "schema", "main").isEmpty(), "removed file gone from listing");
		verify(registry, times(1)).updateCache(any()); // only the construction derive; nothing re-derived
	}

	@Test
	void reconcile_unknownBranch_returnsFalse() throws Exception {
		GitStorageHelper h = helper(Map.of(EPACKAGE_TYPE, "schema"));
		assertFalse(h.reconcile("no-such-branch"));
		verify(registry, never()).removeFromCache(any());
	}

	@Test
	void reconcileAll_reconcilesOnlyMovedBranches() throws Exception {
		// main moves cMain1 -> cMain2; release stays at cRel.
		GitService main = mock(GitService.class);
		when(main.getBranch()).thenReturn("main");
		when(main.getFiles()).thenReturn(
				new TreeResult("cMain1", List.of(ECORE_PATH)),
				new TreeResult("cMain2", List.of(ECORE_PATH)));
		when(main.readFile(anyString(), eq(ECORE_PATH)))
				.thenAnswer(inv -> new ByteArrayInputStream(PERSON_ECORE.getBytes(StandardCharsets.UTF_8)));

		GitService release = mock(GitService.class);
		when(release.getBranch()).thenReturn("release");
		when(release.getFiles()).thenReturn(new TreeResult("cRel", List.of(ECORE_PATH)));
		when(release.readFile(anyString(), eq(ECORE_PATH)))
				.thenAnswer(inv -> new ByteArrayInputStream(PERSON_ECORE.getBytes(StandardCharsets.UTF_8)));

		try(GitStorageHelper h = new GitStorageHelper(resourceSet, List.of(main, release), "jena",
				Map.of(EPACKAGE_TYPE, "schema"), registry, null)) {
			verify(registry, times(2)).updateCache(any()); // one per branch at construction

			h.reconcileAll();

			// only main moved -> only its entry evicted + re-derived
			verify(registry).removeFromCache("jena/main/" + ECORE_PATH);
			verify(registry, never()).removeFromCache("jena/release/" + ECORE_PATH);

			ArgumentCaptor<ObjectMetadata> captor = ArgumentCaptor.forClass(ObjectMetadata.class);
			verify(registry, times(3)).updateCache(captor.capture()); // +1 main re-derive
			assertEquals("cMain2", captor.getAllValues().get(2).getVersion());
			assertEquals(List.of("jena/main/" + ECORE_PATH), h.listObjectIds("jena", "schema", "main"));
			assertEquals(List.of("jena/release/" + ECORE_PATH), h.listObjectIds("jena", "schema", "release"));
		}
		
	}
}
