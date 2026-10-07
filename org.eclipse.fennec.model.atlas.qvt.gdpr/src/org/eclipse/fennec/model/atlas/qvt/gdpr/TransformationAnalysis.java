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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.m2x.model.compiled.CompiledUnit;
import org.eclipse.fennec.m2x.model.compiled.PackageEntry;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.compliance.report.ReportPackage;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.PackageSubject;
import org.eclipse.fennec.model.compliance.report.TransformationSubject;
import org.osgi.util.promise.Promise;

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

	/**
	 * The producer the unanalysable-transformation statement is written under.
	 * <p>
	 * <b>It must equal the one the review-findings mapper uses.</b> A producer's roots are replaced
	 * as a set, so writing this is what withdraws the findings of the last analysis that succeeded,
	 * and a successful analysis writes over this one in turn - the two are the same statement about
	 * the same object, in its two outcomes. The literal is repeated rather than shared because the
	 * bundle that owns the mapper is Private-Package and a deployment may run either bundle without
	 * the other; the string is the contract between them.
	 */
	private static final String REVIEW_PRODUCER = "gdpr.review";

	/** Code of the root saying this transformation could not be analysed at all. */
	private static final String CODE_NO_ANALYSIS = "gdpr.no-review";

	/** Code of a child naming one metamodel the analysis would have rested on and could not. */
	private static final String CODE_UNREVIEWED_SOURCE = "gdpr.unreviewed-source";

	/** The coarse classification the whole GDPR family writes under. */
	private static final String DIAGNOSTIC_CATEGORY = "compliance";

	/** What the storage layer writes into {@code ActionContext.objectType()} for a report. */
	static final String REPORT_TYPE = EcoreUtil.getURI(ReportPackage.Literals.COMPLIANCE_REPORT).toString();

	private final WritableScopeService<EObject> scope;
	private final String reportRegistry;

	TransformationAnalysis(WritableScopeService<EObject> scope, String reportRegistry) {
		this.scope = scope;
		this.reportRegistry = reportRegistry;
	}

	/**
	 * Derives the report of one compiled unit and stores it in the unit's own stage - or, where a
	 * metamodel it rests on has no review, derives nothing and says so on the unit.
	 * <p>
	 * <b>Why an unreviewed metamodel stops the whole report.</b> A transformation is not reviewed,
	 * it is derived: every category this analysis asserts was asserted by a metamodel review first,
	 * and every citation was carried over from one. A metamodel with no review contributes no
	 * classification, so the flows through it are still fact while nothing says what travels along
	 * them - and a report that is silent about them reads exactly like one that found them clean. A
	 * partial analysis of a transformation reads as an analysis, and the finding it is missing may
	 * be the one that mattered.
	 * <p>
	 * So the gap is stated where a person looks for it, as an {@code ERROR} on the compiled unit
	 * under the same producer a derived report writes under. Writing it is also what withdraws the
	 * findings of the last analysis that did succeed, because a producer's roots are replaced as a
	 * set - and the report of that analysis is deleted here, which is what takes its findings off
	 * the metamodels too.
	 *
	 * @param unit         the compiled unit, with a manifest
	 * @param stage        the stage the unit is in, which is where its report goes and where the
	 *                     search for the metamodels' reviews starts
	 * @param unitRegistry the registry the unit itself is in, for the diagnostic
	 * @param unitObjectId the unit's object id, for the diagnostic
	 * @return the stored report, or {@code null} when a metamodel has no review
	 * @throws IllegalStateException if the report could not be stored
	 */
	ComplianceReport analyse(CompiledUnit unit, String stage, String unitRegistry, String unitObjectId) {
		Map<String, ComplianceReport> reviews = reviewsFor(stage);
		List<PackageEntry> unreviewed = unreviewed(unit, reviews);
		if (!unreviewed.isEmpty()) {
			withdraw(stage, unit);
			resolve(scope.updateDiagnosticsInStageForRegistry(unitRegistry, stage, unitObjectId,
					REVIEW_PRODUCER, incomplete(unit, unreviewed)));
			LOGGER.warning(() -> String.format(
					"No GDPR flow analysis of %s: %d of the %d metamodels it was compiled against have no review "
							+ "(%s). The unit is recorded as unanalysable rather than as clean.",
					unit.getManifest().getQualifiedName(), unreviewed.size(),
					unit.getManifest().getPackageEntry().size(),
					unreviewed.stream().map(PackageEntry::getNsURI).collect(Collectors.joining(", "))));
			return null;
		}
		ComplianceReport report = FlowAnalysis.analyse(unit, reviews, Instant.now());
		store(stage, report);
		LOGGER.info(() -> String.format("GDPR flow analysis of %s stored as %s/%s/%s/%s: %d flows, %d combinations.",
				unit.getManifest().getQualifiedName(), scope.getScopeName(), reportRegistry, stage,
				report.getReportId(), report.getEvaluations().size(), report.getCombinations().size()));
		return report;
	}

	/**
	 * The manifest entries no review speaks for, in manifest order.
	 * <p>
	 * Keyed on the fingerprint and not on the nsURI: a review of another revision of the same
	 * metamodel says nothing about the revision this unit was compiled against, and treating it as
	 * if it did is how a stale classification becomes a current one. A package listed twice - a
	 * model declared {@code inout} - is reported once.
	 */
	private static List<PackageEntry> unreviewed(CompiledUnit unit, Map<String, ComplianceReport> reviews) {
		List<PackageEntry> missing = new ArrayList<>();
		Set<String> seen = new LinkedHashSet<>();
		for (PackageEntry entry : unit.getManifest().getPackageEntry()) {
			if (reviews.get(entry.getFingerprint()) == null && seen.add(entry.getNsURI())) {
				missing.add(entry);
			}
		}
		return missing;
	}

	/**
	 * What is written on the unit instead of a report: one {@code ERROR} root, with a child naming
	 * each metamodel that has no review of the revision this unit was compiled against.
	 * <p>
	 * {@code ERROR} is not an exception to the rule that a review never produces one. That rule
	 * exists because {@code ERROR} means the check did not run rather than that the model is bad -
	 * and here the check genuinely did not run.
	 */
	private static List<Diagnostic> incomplete(CompiledUnit unit, List<PackageEntry> unreviewed) {
		Diagnostic root = ManagementFactory.eINSTANCE.createDiagnostic();
		root.setCode(CODE_NO_ANALYSIS);
		root.setSeverity(DiagnosticSeverity.ERROR);
		root.setCategory(DIAGNOSTIC_CATEGORY);
		root.setSource(unit.getManifest().getUnitFingerprint());
		root.setMessage(String.format(
				"No GDPR analysis of this transformation: %d of the %d metamodels it was compiled against have no "
						+ "review of the revision it was compiled against, so nothing classifies the data that "
						+ "travels through them. The analysis is impossible, not clean.",
				unreviewed.size(), unit.getManifest().getPackageEntry().size()));
		for (PackageEntry entry : unreviewed) {
			Diagnostic child = ManagementFactory.eINSTANCE.createDiagnostic();
			child.setCode(CODE_UNREVIEWED_SOURCE);
			child.setTarget(entry.getNsURI());
			child.setSeverity(DiagnosticSeverity.ERROR);
			child.setCategory(DIAGNOSTIC_CATEGORY);
			child.setSource(unit.getManifest().getUnitFingerprint());
			child.setMessage(String.format(
					"%s has no GDPR review of the revision this transformation was compiled against (%s).",
					entry.getNsURI(), entry.getFingerprint()));
			root.getChildren().add(child);
		}
		return List.of(root);
	}

	/**
	 * Deletes the report an earlier analysis of this unit stored, if there is one.
	 * <p>
	 * It cannot be addressed by id: the id covers the reviews the analysis rested on, and one of
	 * those has just gone - so the id that analysis minted is no longer derivable. It is found by
	 * what it is about instead, which is the unit's own fingerprint on its subject.
	 * <p>
	 * Deleting it is also what takes the transformation's findings off the metamodels: the action
	 * that wrote them there answers a report's deletion by clearing them.
	 */
	private void withdraw(String stage, CompiledUnit unit) {
		String fingerprint = unit.getManifest().getUnitFingerprint();
		for (ObjectMetadata metadata : scope.listInStageForRegistry(reportRegistry, stage)) {
			ComplianceReport stored = reportAt(stage, metadata.getObjectId());
			if (stored == null || !(stored.getSubject() instanceof TransformationSubject subject)
					|| !Objects.equals(fingerprint, subject.getSubjectFingerprint())) {
				continue;
			}
			try {
				scope.deleteFromStageForRegistry(reportRegistry, stage, metadata.getObjectId()).getValue();
				LOGGER.info(() -> String.format(
						"Withdrew the GDPR flow report %s of %s: a metamodel it rested on is no longer reviewed.",
						metadata.getObjectId(), unit.getManifest().getQualifiedName()));
			} catch (InvocationTargetException e) {
				Throwable cause = e.getCause() == null ? e : e.getCause();
				throw new IllegalStateException(
						"A GDPR flow report that no longer holds could not be withdrawn: " + cause.getMessage(), cause);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException("Interrupted while withdrawing a GDPR flow report.", e);
			}
		}
	}

	private static void resolve(Promise<ObjectMetadata> written) {
		try {
			written.getValue();
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause() == null ? e : e.getCause();
			throw new IllegalStateException(
					"The unanalysable-transformation diagnostic could not be written: " + cause.getMessage(), cause);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while writing the unanalysable-transformation diagnostic.", e);
		}
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
	Map<String, ComplianceReport> reviewsFor(String stage) {
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
			ComplianceReport report = reportAt(stage, metadata.getObjectId());
			if (report == null || !(report.getSubject() instanceof PackageSubject subject)) {
				continue;
			}
			candidates.add(new StagedReviews.Candidate(stage, metadata.getObjectId(), generatedAt(report, metadata),
					subject.getSubjectFingerprint(), report));
		}
		return candidates;
	}

	/** One stored report, or {@code null} when it is gone or unreadable. */
	ComplianceReport reportAt(String stage, String objectId) {
		try {
			EObject content = scope.getContentFromStageForRegistry(reportRegistry, stage, objectId);
			return content instanceof ComplianceReport report ? report : null;
		} catch (RuntimeException goneOrUnreadable) {
			// Listed a moment ago and not there now, or not parseable: it cannot speak for a
			// revision either way, and failing the whole analysis over it would be worse.
			LOGGER.log(Level.FINE, goneOrUnreadable, () -> "Report " + objectId + " could not be read");
			return null;
		}
	}

	/** When a review happened: what it says, else when the Atlas last saw it. */
	static Instant generatedAt(ComplianceReport report, ObjectMetadata metadata) {
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
	private void store(String stage, ComplianceReport report) {
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
