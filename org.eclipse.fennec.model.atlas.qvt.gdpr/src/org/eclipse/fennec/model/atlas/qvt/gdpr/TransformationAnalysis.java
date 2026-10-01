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
package org.eclipse.fennec.model.atlas.qvt.gdpr;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.m2x.model.compiled.CompiledUnit;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.gdprReport.GDPRReportPackage;
import org.eclipse.fennec.model.gdprReport.GdprReport;
import org.eclipse.fennec.model.gdprReport.PackageSubject;

/**
 * Deriving a transformation's report and storing it, independently of what asked for it.
 *
 * <p>
 * Two things ask: a compiled unit arriving, and a review the unit rests on changing. They are
 * different events on different registries and they are different stage actions, but the work is
 * one piece - gather the reviews along the ladder, run the analysis, store the result - and it has
 * to be one piece, because a re-analysis that derived its report even slightly differently from the
 * first one would make a transformation's history document show a change that never happened.
 * </p>
 */
final class TransformationAnalysis {

	private static final Logger LOGGER = Logger.getLogger(TransformationAnalysis.class.getName());

	/** What the storage layer writes into {@code ActionContext.objectType()} for a report. */
	static final String REPORT_TYPE = EcoreUtil.getURI(GDPRReportPackage.Literals.GDPR_REPORT).toString();

	private final WritableScopeService<EObject> scope;
	private final String reportRegistry;

	TransformationAnalysis(WritableScopeService<EObject> scope, String reportRegistry) {
		this.scope = scope;
		this.reportRegistry = reportRegistry;
	}

	/**
	 * Derives the report of one compiled unit and stores it in the unit's own stage.
	 *
	 * @param unit  the compiled unit, with a manifest
	 * @param stage the stage the unit is in, which is where its report goes and where the search
	 *              for the metamodels' reviews starts
	 * @return the stored report
	 * @throws IllegalStateException if the report could not be stored
	 */
	GdprReport analyse(CompiledUnit unit, String stage) {
		GdprReport report = FlowAnalysis.analyse(unit, reviewsFor(stage), Instant.now());
		store(stage, report);
		LOGGER.info(() -> String.format("GDPR flow analysis of %s stored as %s/%s/%s/%s: %d flows, %d combinations.",
				unit.getManifest().getQualifiedName(), scope.getScopeName(), reportRegistry, stage,
				report.getReportId(), report.getEvaluation().size(), report.getCombinations().size()));
		return report;
	}

	/* ------------------------------------------------------------------ the reviews */

	/**
	 * The review of each metamodel revision, by fingerprint, as the stages this unit could read
	 * hold them now.
	 * <p>
	 * A scan per stage, reading each report: the subject's fingerprint lives in the content and not
	 * in the metadata, so there is nothing to index on. Both report actions of that registry read
	 * every report the same way.
	 *
	 * @see StageLadder for which stages those are
	 * @see StagedReviews for which review speaks for a revision when several do
	 */
	Map<String, GdprReport> reviewsFor(String stage) {
		List<String> stageOrder = StageLadder.visibleIn(scope.getScope(), reportRegistry, stage);
		List<StagedReviews.Candidate> candidates = new ArrayList<>();
		for (String each : stageOrder) {
			candidates.addAll(reviewsStoredIn(each));
		}
		return StagedReviews.resolve(stageOrder, candidates);
	}

	/**
	 * Every review of a metamodel stored in one stage.
	 * <p>
	 * <b>Only reviews of packages.</b> The registry holds transformation reports too - this bundle
	 * writes them - and reading one here would be the analysis trying to rest on its own output.
	 * The subject type says which is which; that an {@code m2x1:} fingerprint could never match a
	 * {@code packageEntry} anyway is true and is not what this relies on.
	 */
	private List<StagedReviews.Candidate> reviewsStoredIn(String stage) {
		List<StagedReviews.Candidate> candidates = new ArrayList<>();
		for (ObjectMetadata metadata : scope.listInStageForRegistry(reportRegistry, stage)) {
			GdprReport report = reportAt(stage, metadata.getObjectId());
			if (report == null || !(report.getSubject() instanceof PackageSubject subject)) {
				continue;
			}
			candidates.add(new StagedReviews.Candidate(stage, metadata.getObjectId(), generatedAt(report, metadata),
					subject.getSubjectFingerprint(), report));
		}
		return candidates;
	}

	/** One stored report, or {@code null} when it is gone or unreadable. */
	GdprReport reportAt(String stage, String objectId) {
		try {
			EObject content = scope.getContentFromStageForRegistry(reportRegistry, stage, objectId);
			return content instanceof GdprReport report ? report : null;
		} catch (RuntimeException goneOrUnreadable) {
			// Listed a moment ago and not there now, or not parseable: it cannot speak for a
			// revision either way, and failing the whole analysis over it would be worse.
			LOGGER.log(Level.FINE, goneOrUnreadable, () -> "Report " + objectId + " could not be read");
			return null;
		}
	}

	/** When a review happened: what it says, else when the Atlas last saw it. */
	static Instant generatedAt(GdprReport report, ObjectMetadata metadata) {
		String stated = report.getGeneratedAt();
		if (stated != null && !stated.isBlank()) {
			try {
				return Instant.parse(stated.trim());
			} catch (DateTimeParseException unreadable) {
				// Not a report to reject, one to order by the next best thing.
			}
		}
		Instant stored = metadata.getLastChangeTime() == null ? metadata.getUploadTime()
				: metadata.getLastChangeTime();
		return stored == null ? Instant.EPOCH : stored;
	}

	/* ------------------------------------------------------------------ storing */

	/**
	 * Writes the report into the stage the unit is in: an update where one of this analysis is
	 * already there, a new object otherwise.
	 * <p>
	 * The id covers the unit and every review the analysis rested on, so the same inputs rewrite the
	 * same document and a changed input writes another one - which is what lets the transformation's
	 * history document show a real change rather than a re-run.
	 */
	private void store(String stage, GdprReport report) {
		ObjectMetadata existing = scope.getMetadataFromStageForRegistry(reportRegistry, stage, report.getReportId());
		try {
			if (existing == null) {
				ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
				metadata.setObjectId(report.getReportId());
				metadata.setObjectName(report.getName());
				metadata.setUploadTime(Instant.now());
				metadata.setObjectType(REPORT_TYPE);
				scope.uploadToStageForRegistry(reportRegistry, stage, report, metadata).getValue();
			} else {
				scope.updateInStageForRegistry(reportRegistry, stage, report, report.getReportId(),
						existing.getVersion()).getValue();
			}
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause() == null ? e : e.getCause();
			throw new IllegalStateException("The GDPR flow report could not be stored: " + cause.getMessage(), cause);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while storing the GDPR flow report.", e);
		}
	}
}
