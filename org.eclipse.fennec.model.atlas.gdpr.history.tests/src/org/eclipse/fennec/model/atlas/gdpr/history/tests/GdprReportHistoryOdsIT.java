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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.gdprReportHistory.GdprReportHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * The document has to render as a spreadsheet a human can read: one sheet per containment list,
 * with the values that equal their feature default still in the cells.
 * <p>
 * <b>Why this is an OSGi test.</b> {@code OdsResourceFactoryComponent} is Private-Package in
 * {@code org.eclipse.fennec.codec.ods} and reaches the world only as a {@code Resource.Factory}
 * service, so no test bundle can construct it. The exported {@code OdsFormatProvider} could be
 * driven by hand, but it bypasses the codec resource that applies the save options - and the save
 * options are the thing that has to be pinned: without them the endpoint returns a document with one
 * sheet and none of its content.
 */
@ExtendWith(TempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("GDPR review document - ODS rendering")
public class GdprReportHistoryOdsIT {

	private static final String SCOPE_FILTER = "(atlas.scope=" + TestAnnotations.SCOPE_NAME + ")";
	private static final String ODS_FILTER = "(emf.fileExtension=ods)";

	/** The two options the content endpoint has to carry; see §3.5 and R1b of the plan. */
	private static final Map<Object, Object> SHEETS_AND_DEFAULTS = Map.of( //
			"codec.tabular.referenceMode", "SQL_TABLES", //
			"codec.serializeDefault", Boolean.TRUE);

	private static final Pattern SHEET_NAME = Pattern.compile("table:name=\"([^\"]+)\"");

	@Test
	@TestAnnotations.GdprHistorySetup
	@DisplayName("a two-revision document renders as one sheet per list, defaults included")
	public void documentRendersAsSheets(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> scopeAware,
			@InjectService(cardinality = 0, timeout = 30000, filter = ODS_FILTER) //
			ServiceAware<Resource.Factory> odsAware) throws Exception {

		GdprReportHistory document = twoRevisionDocument(scopeAware);
		Resource.Factory ods = odsAware.waitForService(30000);
		assertNotNull(ods, "the ODS codec has to be in the runtime for the document to be downloadable");

		String content = new String(contentXml(render(ods, document, SHEETS_AND_DEFAULTS)), StandardCharsets.UTF_8);

		assertEquals(List.of("GdprReportHistory", "ReportRevision", "EvaluationRow", "ChangeRow"), sheets(content),
				"one sheet per containment list, in containment order");
		assertTrue(content.contains("SPECIAL_CATEGORY"), "the raised category belongs in the change sheet");
		assertTrue(content.contains("UNCHANGED"),
				"a value equal to its feature default still has to appear - that is codec.serializeDefault");
		assertTrue(content.contains("AI_AGENT") && content.contains("HUMAN"),
				"both authors have to be readable in the revisions sheet");
	}

	@Test
	@TestAnnotations.GdprHistorySetup
	@DisplayName("without the save options the document loses its content")
	public void withoutTheOptionsTheSheetsAreGone(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> scopeAware,
			@InjectService(cardinality = 0, timeout = 30000, filter = ODS_FILTER) //
			ServiceAware<Resource.Factory> odsAware) throws Exception {

		// Not a curiosity: this is why the two options are mandatory rather than cosmetic. If a
		// future codec default makes this test fail, the endpoint's documentation is what changes.
		GdprReportHistory document = twoRevisionDocument(scopeAware);
		Resource.Factory ods = odsAware.waitForService(30000);

		String content = new String(contentXml(render(ods, document, Map.of())), StandardCharsets.UTF_8);

		assertEquals(List.of("GdprReportHistory"), sheets(content),
				"the default reference mode drops the containment lists, so the document is empty");
	}

	/* ------------------------------------------------------------------ fixtures */

	private static GdprReportHistory twoRevisionDocument(ServiceAware<WritableScopeService> scopeAware)
			throws Exception {
		@SuppressWarnings("unchecked")
		WritableScopeService<EObject> scope = scopeAware.waitForService(30000);
		assertNotNull(scope, "the test scope service must come up");
		Documents.store(scope, Reports.agentReview());
		Documents.awaitRevisions(scope, 1);
		Documents.store(scope, Reports.humanCorrection());
		return Documents.awaitRevisions(scope, 2);
	}

	/** Saves the document through the real ODS factory and hands back the bytes. */
	private static byte[] render(Resource.Factory factory, GdprReportHistory document, Map<Object, Object> options)
			throws Exception {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ods", factory);
		Resource resource = resourceSet.createResource(URI.createURI("gdpr-history.ods"));
		// A copy, so saving does not detach the document from the resource it was loaded into.
		resource.getContents().add(org.eclipse.emf.ecore.util.EcoreUtil.copy(document));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		resource.save(out, new LinkedHashMap<>(options));
		return out.toByteArray();
	}

	/** The sheet names, in document order, read out of the ODS's content.xml. */
	private static List<String> sheets(String contentXml) {
		List<String> names = new ArrayList<>();
		Matcher matcher = SHEET_NAME.matcher(contentXml);
		while (matcher.find()) {
			names.add(matcher.group(1));
		}
		return names;
	}

	/** An ODS is a ZIP; its content.xml is the sheet layout. */
	private static byte[] contentXml(byte[] odsBytes) throws Exception {
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(odsBytes))) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if ("content.xml".equals(entry.getName())) {
					return zip.readAllBytes();
				}
			}
		}
		return new byte[0];
	}
}
