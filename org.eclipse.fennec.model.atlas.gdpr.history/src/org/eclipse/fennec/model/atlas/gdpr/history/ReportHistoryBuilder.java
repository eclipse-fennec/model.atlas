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
package org.eclipse.fennec.model.atlas.gdpr.history;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.fennec.model.gdprReport.ClassifierEvaluation;
import org.eclipse.fennec.model.gdprReport.ConfidenceType;
import org.eclipse.fennec.model.gdprReport.Evaluation;
import org.eclipse.fennec.model.gdprReport.Evidence;
import org.eclipse.fennec.model.gdprReport.FeatureEvaluation;
import org.eclipse.fennec.model.gdprReport.Finding;
import org.eclipse.fennec.model.gdprReport.GDPRReportPackage;
import org.eclipse.fennec.model.gdprReport.GdprReport;
import org.eclipse.fennec.model.gdprReport.GdprReportOrigin;
import org.eclipse.fennec.model.gdprReport.LegalCorpusRef;
import org.eclipse.fennec.model.gdprReport.PackageSubject;
import org.eclipse.fennec.model.gdprReport.RelevanceLevelType;
import org.eclipse.fennec.model.gdprReport.Subject;
import org.eclipse.fennec.model.gdprReport.TransformationSubject;
import org.eclipse.fennec.model.gdprReportHistory.ChangeKind;
import org.eclipse.fennec.model.gdprReportHistory.ChangeRow;
import org.eclipse.fennec.model.gdprReportHistory.EvaluationRow;
import org.eclipse.fennec.model.gdprReportHistory.GDPRReportHistoryFactory;
import org.eclipse.fennec.model.gdprReportHistory.GdprReportHistory;
import org.eclipse.fennec.model.gdprReportHistory.ReportRevision;
import org.eclipse.fennec.model.gdprReportHistory.RevisionOrigin;

/**
 * Builds the derived {@link GdprReportHistory} of one subject from the reviews stored for it.
 * <p>
 * <b>Everything GDPR-specific in this bundle is here.</b> The stage action around it only decides
 * when to run and where to put the result.
 * <p>
 * <b>It rebuilds, it does not append.</b> Give it every report there is and it produces the whole
 * document. A replayed event therefore cannot duplicate a revision, and a report that was written
 * while nothing was listening is picked up by the next rebuild rather than being missing forever.
 * Nothing here reads the previous history object.
 * <p>
 * <b>One row per evaluated classifier or feature, not per finding.</b> A feature with two findings
 * becomes one row whose category cell holds both, because the document is read as "one line per
 * thing in my model". The consequence is worth knowing: a second finding appearing on a feature
 * shows up as a widened category cell rather than as a new row. Keying rows by finding instead would
 * turn the headline case - a category raised from {@code PERSONAL_DATA} to {@code SPECIAL_CATEGORY}
 * - into a removal plus an addition, which is exactly what the change sheet exists to avoid.
 * <p>
 * <b>Matching is by id, and never by {@code Finding.id}.</b> {@code ClassifierEvaluation.id} and
 * {@code FeatureEvaluation.id} are documented as stable across reruns - the classifier name and
 * {@code classifier.feature}. {@code Finding.id} is documented as "e.g. F-001", unique within one
 * report and assigned per run, so pairing two revisions on it would confidently report a change to a
 * finding that did not change. {@code uriFragment} is the fallback when an id is missing, being
 * derived from the model rather than from an agent following an instruction.
 */
public class ReportHistoryBuilder {

	/** Report ids end in this, as {@code BatchGDPRCheckService.reportId()} writes them. */
	private static final DateTimeFormatter ID_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
			.withZone(ZoneOffset.UTC);

	private static final String CATEGORY = "category";
	private static final String RELEVANCE_LEVEL = "relevanceLevel";
	private static final String CONFIDENCE = "confidence";
	private static final String RATIONALE = "rationale";
	private static final String RECOMMENDATION = "recommendation";
	private static final String PURPOSE = "purpose";
	private static final String EVIDENCE = "evidence";

	private static final Logger LOGGER = Logger.getLogger(ReportHistoryBuilder.class.getName());

	private final GDPRReportHistoryFactory factory = GDPRReportHistoryFactory.eINSTANCE;

	/**
	 * Builds the document.
	 *
	 * @param reports   every stored review of the subject <b>in one language</b>, in any order; an
	 *                  empty list yields a history with no revisions rather than {@code null}
	 * @param rebuiltAt when this rebuild happened, required
	 * @return the history, never {@code null}
	 */
	public GdprReportHistory build(List<StoredReport> reports, Instant rebuiltAt) {
		Objects.requireNonNull(reports, "reports");
		Objects.requireNonNull(rebuiltAt, "rebuiltAt");

		List<StoredReport> ordered = new ArrayList<>(reports);
		ordered.sort(Comparator.comparing(ReportHistoryBuilder::orderingKey)
				.thenComparing(StoredReport::objectId));

		GdprReportHistory history = factory.createGdprReportHistory();
		history.setRebuiltAt(rebuiltAt.toString());
		history.setRevisionCount(ordered.size());
		describeSubject(history, ordered);
		describeLanguage(history, ordered);

		Map<RowKey, EvaluationRow> previous = Map.of();
		int revisionNumber = 0;
		for (StoredReport stored : ordered) {
			revisionNumber++;

			Map<RowKey, EvaluationRow> current = flatten(stored.report(), revisionNumber);
			List<ChangeRow> changes = diff(previous, current, revisionNumber, stored);

			history.getEvaluations().addAll(current.values());
			history.getChanges().addAll(changes);
			history.getRevisions().add(revision(stored, revisionNumber, changes.size()));

			previous = current;
		}
		return history;
	}

	/* ------------------------------------------------------------------ the subject */

	private void describeSubject(GdprReportHistory history, List<StoredReport> ordered) {
		// The newest report describes the subject: an older one may predate a rename, and the
		// fingerprint is the same for all of them anyway while a document covers one revision.
		for (int i = ordered.size() - 1; i >= 0; i--) {
			Subject subject = ordered.get(i).report().getSubject();
			if (subject == null) {
				continue;
			}
			// The document names its subject the way that kind of subject is named: a package by
			// its EPackage name, a transformation by the unit it was compiled from. The namespace
			// URI is no longer a field of the document - a transformation has none - so a package
			// that carries no name falls back to it through historyName rather than losing it.
			if (subject instanceof PackageSubject packageSubject) {
				history.setSubjectName(packageSubject.getName());
			} else if (subject instanceof TransformationSubject transformation) {
				history.setSubjectName(transformation.getQualifiedName());
			}
			history.setSubjectFingerprint(subject.getSubjectFingerprint());
			history.setName(historyName(subject));
			return;
		}
	}

	/**
	 * The language the reviews were carried out in, taken from the corpus they quote.
	 * <p>
	 * <b>A document covers one language.</b> A review quotes one consolidation of one language
	 * version from start to seal, so revisions in two languages are not successive revisions of one
	 * review: diffing them would report every rationale and recommendation as changed on each
	 * switch, which is noise in the one sheet that exists to be read. The caller groups; this only
	 * records what it was given and says so when the grouping did not hold.
	 *
	 * @param history the document being built
	 * @param ordered the reviews, oldest first
	 */
	private static void describeLanguage(GdprReportHistory history, List<StoredReport> ordered) {
		Set<String> languages = new TreeSet<>();
		for (StoredReport stored : ordered) {
			LegalCorpusRef corpus = stored.report().getCorpus();
			String language = corpus == null ? null : corpus.getLanguage();
			if (blankToNull(language) != null) {
				languages.add(language.trim().toUpperCase(Locale.ROOT));
			}
		}
		if (languages.size() > 1) {
			LOGGER.log(Level.WARNING, () -> String.format(
					"Reviews in %d languages (%s) were built into one document; its change sheet compares a "
							+ "revision in one language against a revision in another and cannot be read. Group the "
							+ "reports by corpus language and build one document per language.",
					languages.size(), String.join(", ", languages)));
		}
		history.setLanguage(languages.isEmpty() ? null : languages.iterator().next());
	}

	/**
	 * Only a {@link PackageSubject} has a namespace to fall back on; a transformation is named by
	 * the unit it was compiled from. A subject of some later kind leaves the document unnamed rather
	 * than named after the wrong thing.
	 */
	private static String historyName(Subject subject) {
		String name = null;
		if (subject instanceof PackageSubject packageSubject) {
			name = blankToNull(packageSubject.getName()) == null ? packageSubject.getNsURI()
					: packageSubject.getName();
		} else if (subject instanceof TransformationSubject transformation) {
			name = blankToNull(transformation.getQualifiedName());
		}
		return name == null ? "GDPR review history" : "GDPR review history of " + name;
	}

	/* ------------------------------------------------------------------ sheet 1 */

	private ReportRevision revision(StoredReport stored, int revisionNumber, int changeCount) {
		GdprReport report = stored.report();
		ReportRevision revision = factory.createReportRevision();
		revision.setRevisionNumber(revisionNumber);
		revision.setReportId(stored.objectId());
		revision.setGeneratedAt(report.getGeneratedAt());
		revision.setGeneratedBy(blankToNull(stored.changedBy()) == null ? report.getGeneratedBy() : stored.changedBy());
		revision.setOrigin(origin(stored));
		warnOnReportIdMismatch(stored);
		revision.setChangeCount(changeCount);
		revision.setFindingCount(countFindings(report));

		Subject subject = report.getSubject();
		if (subject != null) {
			revision.setModelFingerprint(subject.getSubjectFingerprint());
		}
		LegalCorpusRef corpus = report.getCorpus();
		if (corpus != null) {
			revision.setCorpusCelex(corpus.getCelex());
			revision.setCorpusConsolidatedDate(corpus.getConsolidatedDate());
		}
		return revision;
	}

	/**
	 * Every finding the report holds, whatever kind of evaluation carries it. It is deliberately not
	 * limited to the evaluations {@link #flatten} turns into rows: the number answers "how much did
	 * this review find", and a report whose findings sit on flows has found them all the same.
	 */
	private static int countFindings(GdprReport report) {
		int count = report.getCombinations().size();
		for (Evaluation evaluation : report.getEvaluation()) {
			count += evaluation.getFindings().size();
			if (evaluation instanceof ClassifierEvaluation classifier) {
				for (FeatureEvaluation feature : classifier.getFeatureEvaluation()) {
					count += feature.getFindings().size();
				}
			}
		}
		return count;
	}

	/* ------------------------------------------------------------------ sheet 2 */

	/**
	 * Flattens one report into rows, keyed so that two revisions can be paired. A
	 * {@link LinkedHashMap} because the sheet should read in the order the report was written.
	 */
	private Map<RowKey, EvaluationRow> flatten(GdprReport report, int revisionNumber) {
		Map<RowKey, EvaluationRow> rows = new LinkedHashMap<>();
		warnOnUnrowedEvaluations(report, revisionNumber);
		for (ClassifierEvaluation classifier : classifiersOf(report)) {
			String classifierId = identify(classifier.getId(), classifier.getUriFragment(), classifier.getName());
			if (classifierId == null) {
				// Nothing to key it by, so it could not be compared against anything in another
				// revision. Dropping it silently would be worse, but so would inventing a key.
				continue;
			}
			if (!classifier.getFindings().isEmpty()) {
				RowKey key = new RowKey(classifierId, "");
				rows.put(key, classifierRow(classifier, classifierId, revisionNumber));
			}
			for (FeatureEvaluation feature : classifier.getFeatureEvaluation()) {
				String featureId = identify(feature.getId(), feature.getUriFragment(), feature.getName());
				if (featureId == null) {
					continue;
				}
				RowKey key = new RowKey(classifierId, featureId);
				rows.put(key, featureRow(classifier, classifierId, feature, featureId, revisionNumber));
			}
		}
		return rows;
	}

	/**
	 * The classifier evaluations of a report, in order. A report of a transformation carries
	 * {@code FlowEvaluation}s here instead, and an {@link EvaluationRow} has nowhere to put a source
	 * and a target feature, so those are left out of the sheet rather than flattened into a shape that
	 * does not fit them.
	 */
	private static List<ClassifierEvaluation> classifiersOf(GdprReport report) {
		return report.getEvaluation().stream().filter(ClassifierEvaluation.class::isInstance)
				.map(ClassifierEvaluation.class::cast).collect(Collectors.toList());
	}

	/**
	 * Says so when a revision holds evaluations the sheet cannot show. Silence would read as a review
	 * that found nothing, which is the one thing this document must never imply.
	 */
	private static void warnOnUnrowedEvaluations(GdprReport report, int revisionNumber) {
		long unrowed = report.getEvaluation().stream().filter(e -> !(e instanceof ClassifierEvaluation)).count();
		if (unrowed > 0) {
			LOGGER.log(Level.WARNING, () -> String.format(
					"Revision %d holds %d evaluation(s) that are not classifier evaluations; they are counted in "
							+ "findingCount but have no row in the evaluation sheet, which only carries classifiers "
							+ "and features.",
					revisionNumber, unrowed));
		}
	}

	private EvaluationRow classifierRow(ClassifierEvaluation classifier, String classifierId, int revisionNumber) {
		EvaluationRow row = factory.createEvaluationRow();
		row.setRevisionNumber(revisionNumber);
		row.setClassifierId(classifierId);
		row.setClassifierName(classifier.getName());
		row.setClassifierUriFragment(classifier.getUriFragment());
		merge(row, classifier.getFindings(), null);
		return row;
	}

	private EvaluationRow featureRow(ClassifierEvaluation classifier, String classifierId, FeatureEvaluation feature,
			String featureId, int revisionNumber) {
		EvaluationRow row = factory.createEvaluationRow();
		row.setRevisionNumber(revisionNumber);
		row.setClassifierId(classifierId);
		row.setClassifierName(classifier.getName());
		row.setClassifierUriFragment(classifier.getUriFragment());
		row.setFeatureId(featureId);
		row.setFeatureName(feature.getName());
		row.setFeatureUriFragment(feature.getUriFragment());
		row.setTypeName(feature.getTypeName());
		row.setPurpose(feature.getPurpose());
		merge(row, feature.getFindings(), feature.getRelevanceLevel());
		return row;
	}

	/**
	 * Folds the findings of one classifier or feature into the cells of one row. With no findings
	 * the row still exists - it was examined and nothing was found, which is a statement worth
	 * keeping - and carries the evaluation's own relevance.
	 */
	private void merge(EvaluationRow row, List<Finding> findings, RelevanceLevelType ownRelevance) {
		Set<String> categories = new TreeSet<>();
		Set<String> citations = new TreeSet<>();
		List<String> rationales = new ArrayList<>();
		List<String> recommendations = new ArrayList<>();
		RelevanceLevelType relevance = ownRelevance;
		ConfidenceType confidence = null;

		for (Finding finding : findings) {
			if (finding.getCategory() != null) {
				categories.add(finding.getCategory().getName());
			}
			relevance = highest(relevance, finding.getRelevanceLevel());
			confidence = mostCautious(confidence, finding.getConfidence());
			addIfPresent(rationales, finding.getRationale());
			addIfPresent(recommendations, finding.getRecommendation());
			for (Evidence evidence : finding.getEvidence()) {
				addIfPresent(citations, evidence.getCitationId());
			}
		}

		row.setCategory(join(categories));
		row.setRelevanceLevel(relevance == null ? null : relevance.getName());
		row.setConfidence(confidence == null ? null : confidence.getName());
		row.setRationale(join(rationales));
		row.setRecommendation(join(recommendations));
		row.setCitations(join(citations));
	}

	/* ------------------------------------------------------------------ sheet 3 */

	/**
	 * Compares one revision with the one before it, setting the change kind on the current rows and
	 * producing a change row per field that actually differs.
	 * <p>
	 * A row that disappeared leaves no row in this revision - the sheet states what a revision said,
	 * and it said nothing about that feature - so its removal is recorded only here.
	 */
	private List<ChangeRow> diff(Map<RowKey, EvaluationRow> previous, Map<RowKey, EvaluationRow> current,
			int revisionNumber, StoredReport stored) {
		List<ChangeRow> changes = new ArrayList<>();
		if (previous.isEmpty()) {
			current.values().forEach(row -> row.setChangeKind(ChangeKind.UNCHANGED));
			return changes;
		}

		for (Map.Entry<RowKey, EvaluationRow> entry : current.entrySet()) {
			EvaluationRow before = previous.get(entry.getKey());
			EvaluationRow after = entry.getValue();
			if (before == null) {
				after.setChangeKind(ChangeKind.ADDED);
				changes.add(change(entry.getKey(), revisionNumber, stored, "", ChangeKind.ADDED, null,
						after.getCategory()));
				continue;
			}
			List<ChangeRow> fieldChanges = compareFields(entry.getKey(), before, after, revisionNumber, stored);
			after.setChangeKind(fieldChanges.isEmpty() ? ChangeKind.UNCHANGED : ChangeKind.MODIFIED);
			changes.addAll(fieldChanges);
		}

		for (Map.Entry<RowKey, EvaluationRow> entry : previous.entrySet()) {
			if (!current.containsKey(entry.getKey())) {
				changes.add(change(entry.getKey(), revisionNumber, stored, "", ChangeKind.REMOVED,
						entry.getValue().getCategory(), null));
			}
		}
		return changes;
	}

	private List<ChangeRow> compareFields(RowKey key, EvaluationRow before, EvaluationRow after, int revisionNumber,
			StoredReport stored) {
		List<ChangeRow> changes = new ArrayList<>();
		compare(changes, key, revisionNumber, stored, CATEGORY, before.getCategory(), after.getCategory());
		compare(changes, key, revisionNumber, stored, RELEVANCE_LEVEL, before.getRelevanceLevel(),
				after.getRelevanceLevel());
		compare(changes, key, revisionNumber, stored, CONFIDENCE, before.getConfidence(), after.getConfidence());
		compare(changes, key, revisionNumber, stored, RATIONALE, before.getRationale(), after.getRationale());
		compare(changes, key, revisionNumber, stored, RECOMMENDATION, before.getRecommendation(),
				after.getRecommendation());
		// The purpose is the one cell a person is expected to fill by hand, so the revision in
		// which it appeared, and who wrote it, is the most quoted line this document has.
		comparePurpose(changes, key, revisionNumber, stored, before.getPurpose(), after.getPurpose());
		// Citations are compared one by one rather than as a joined cell: a citation that quietly
		// disappeared between revisions is the single most important thing this document surfaces,
		// and it must not be buried in a before/after pair of long strings.
		compareCitations(changes, key, revisionNumber, stored, before.getCitations(), after.getCitations());
		return changes;
	}

	/**
	 * A purpose that appears where there was none is an addition, not a modification: nobody
	 * changed their mind, somebody answered a question that was open.
	 */
	private void comparePurpose(List<ChangeRow> changes, RowKey key, int revisionNumber, StoredReport stored,
			String before, String after) {
		String was = blankToNull(before);
		String now = blankToNull(after);
		if (Objects.equals(was, now)) {
			return;
		}
		ChangeKind kind = was == null ? ChangeKind.ADDED : now == null ? ChangeKind.REMOVED : ChangeKind.MODIFIED;
		changes.add(change(key, revisionNumber, stored, PURPOSE, kind, was, now));
	}

	private void compareCitations(List<ChangeRow> changes, RowKey key, int revisionNumber, StoredReport stored,
			String before, String after) {
		Set<String> gone = split(before);
		Set<String> arrived = split(after);
		Set<String> removed = new LinkedHashSet<>(gone);
		removed.removeAll(arrived);
		Set<String> added = new LinkedHashSet<>(arrived);
		added.removeAll(gone);
		removed.forEach(c -> changes.add(change(key, revisionNumber, stored, EVIDENCE, ChangeKind.REMOVED, c, null)));
		added.forEach(c -> changes.add(change(key, revisionNumber, stored, EVIDENCE, ChangeKind.ADDED, null, c)));
	}

	private void compare(List<ChangeRow> changes, RowKey key, int revisionNumber, StoredReport stored, String field,
			String before, String after) {
		if (!Objects.equals(blankToNull(before), blankToNull(after))) {
			changes.add(change(key, revisionNumber, stored, field, ChangeKind.MODIFIED, before, after));
		}
	}

	private ChangeRow change(RowKey key, int revisionNumber, StoredReport stored, String field, ChangeKind kind,
			String oldValue, String newValue) {
		ChangeRow change = factory.createChangeRow();
		change.setRevisionNumber(revisionNumber);
		change.setChangedAt(stored.report().getGeneratedAt());
		change.setChangedBy(blankToNull(stored.changedBy()) == null ? stored.report().getGeneratedBy()
				: stored.changedBy());
		change.setClassifierId(key.classifierId());
		change.setFeatureId(key.featureId());
		change.setField(field);
		change.setChangeKind(kind);
		change.setOldValue(oldValue);
		change.setNewValue(newValue);
		return change;
	}

	/* ------------------------------------------------------------------ ordering */

	/**
	 * When a revision happened. {@code generatedAt} first; failing that the timestamp the report id
	 * ends in, which {@code BatchGDPRCheckService} puts there for exactly this reason; failing that
	 * the epoch, so an unorderable report sorts first and is still in the document.
	 */
	private static Instant orderingKey(StoredReport stored) {
		String generatedAt = blankToNull(stored.report().getGeneratedAt());
		if (generatedAt != null) {
			try {
				return Instant.parse(generatedAt);
			} catch (DateTimeParseException e) {
				// Fall through to the id: a report whose timestamp cannot be read is not a report
				// to reject, it is one to order by the next best thing.
			}
		}
		Instant fromId = timestampIn(stored.objectId());
		return fromId == null ? Instant.EPOCH : fromId;
	}

	private static Instant timestampIn(String objectId) {
		int dash = objectId.length() - "yyyyMMdd-HHmmss".length();
		if (dash < 0) {
			return null;
		}
		try {
			return LocalDateTime.parse(objectId.substring(dash), ID_STAMP).toInstant(ZoneOffset.UTC);
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	/* ------------------------------------------------------------------ provenance */

	/**
	 * Where the revision came from. The report says so itself; the value the caller passed is only
	 * the answer for a report written before the model carried the field.
	 * <p>
	 * <b>Read through {@code eIsSet} rather than the getter.</b> An unset EMF enum reads as its
	 * first literal, and a report that never stated an origin would otherwise be reported as
	 * whatever sits at value 0 - a guess in the one column that must not guess.
	 */
	private static RevisionOrigin origin(StoredReport stored) {
		GdprReport report = stored.report();
		if (!report.eIsSet(GDPRReportPackage.Literals.GDPR_REPORT__ORIGIN)) {
			return stored.origin();
		}
		GdprReportOrigin stated = report.getOrigin();
		if (stated == null) {
			return stored.origin();
		}
		return switch (stated) {
		case AI_AGENT -> RevisionOrigin.AI_AGENT;
		case HUMAN -> RevisionOrigin.HUMAN;
		case UNKNOWN -> RevisionOrigin.UNKNOWN;
		case STATIC_ANALYSIS -> RevisionOrigin.STATIC_ANALYSIS;
		};
	}

	/**
	 * The object id is what addresses the report, so it is what the document cites. An id in the
	 * content that disagrees is a claim about a different report and worth saying out loud, because
	 * it means something upstream stored a report under an id it does not think it has.
	 */
	private static void warnOnReportIdMismatch(StoredReport stored) {
		String claimed = blankToNull(stored.report().getReportId());
		if (claimed != null && !claimed.equals(stored.objectId())) {
			LOGGER.log(Level.WARNING,
					() -> String.format("GDPR report stored as '%s' calls itself '%s'. The document cites the "
							+ "id it is stored under.", stored.objectId(), claimed));
		}
	}

	/* ------------------------------------------------------------------ small helpers */

	private static String identify(String id, String uriFragment, String name) {
		String identity = blankToNull(id);
		if (identity != null) {
			return identity;
		}
		identity = blankToNull(uriFragment);
		return identity != null ? identity : blankToNull(name);
	}

	private static RelevanceLevelType highest(RelevanceLevelType one, RelevanceLevelType other) {
		if (one == null) {
			return other;
		}
		if (other == null) {
			return one;
		}
		return one.getValue() >= other.getValue() ? one : other;
	}

	/**
	 * The least confident of the two. A row that folds a confident finding together with one that
	 * needs a purpose confirmed is not a confident row.
	 * <p>
	 * <b>Not by enum value.</b> {@code ConfidenceType} runs LOW, MEDIUM, HIGH,
	 * REQUIRES_PURPOSE_CONFIRMATION, so its ordinal is a declaration order and not a scale:
	 * REQUIRES_PURPOSE_CONFIRMATION sorts highest while meaning the least settled of all. Comparing
	 * on {@code getValue()} would quietly report a merged row as HIGH when one of its findings is
	 * waiting for a human to confirm what the field is for.
	 */
	private static ConfidenceType mostCautious(ConfidenceType one, ConfidenceType other) {
		if (one == null) {
			return other;
		}
		if (other == null) {
			return one;
		}
		return cautiousness(one) <= cautiousness(other) ? one : other;
	}

	/** Least settled first, so the smaller rank is the more cautious answer. */
	private static int cautiousness(ConfidenceType confidence) {
		return switch (confidence) {
		case REQUIRES_PURPOSE_CONFIRMATION -> 0;
		case LOW -> 1;
		case MEDIUM -> 2;
		case HIGH -> 3;
		};
	}

	private static void addIfPresent(java.util.Collection<String> target, String value) {
		String present = blankToNull(value);
		if (present != null) {
			target.add(present);
		}
	}

	private static String join(java.util.Collection<String> values) {
		return values.isEmpty() ? null : values.stream().collect(Collectors.joining(", "));
	}

	private static Set<String> split(String joined) {
		if (blankToNull(joined) == null) {
			return Set.of();
		}
		return java.util.Arrays.stream(joined.split(",")).map(String::trim).filter(s -> !s.isEmpty())
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value;
	}

	/**
	 * What two revisions are paired on: the classifier, and the feature within it or the empty
	 * string for a finding about the classifier itself.
	 */
	private record RowKey(String classifierId, String featureId) {
	}
}
