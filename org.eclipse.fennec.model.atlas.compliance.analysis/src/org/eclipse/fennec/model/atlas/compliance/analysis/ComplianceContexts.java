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
package org.eclipse.fennec.model.atlas.compliance.analysis;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.compliance.analysis.RequirementCitations.Citation;
import org.eclipse.fennec.model.atlas.compliance.analysis.RequirementCitations.Outcome;
import org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.eclipse.fennec.model.compliance.context.ComplianceContext;

/**
 * The contexts an analyser cites against, fetched from a registry once each and kept.
 *
 * <p>
 * A {@code ComplianceContext} <em>contains</em> its corpora, so fetching one pulls everything it
 * holds: 1.3 MB for {@code gdpr}, 6.7 MB for {@code bsi-grundschutz-pp}. An analyser raising
 * findings asks for the same two or three contexts over and over, so the fetch happens once per
 * context and the answer is kept for as long as this object lives. That is the only thing in this
 * area with a cost worth thinking about, and it is a memoisation rather than an index: nothing is
 * searched, nothing is written, and what comes back is the stored object.
 * </p>
 *
 * <p>
 * <b>Not a component, on purpose.</b> An analyser already binds a scope service to read its
 * reviews and write its report, and already has a configuration to name its registries. Giving
 * this one of its own would add a PID to every deployment and a service whose target, left unset,
 * binds nothing and resolves nothing while the analyser carries on reporting as though the context
 * simply had no such requirement. An analyser constructs one of these in its activate and names
 * the registry in its own configuration, where a reader of that configuration can see it.
 * </p>
 *
 * <p>
 * A miss is not cached. A context absent from the registry is a deployment fault, and the next
 * lookup asking again is what lets seeding it fix the deployment without a restart.
 * </p>
 */
public final class ComplianceContexts {

	/** Where compliance contexts live by convention, matching the seeded deployments. */
	public static final String DEFAULT_REGISTRY = "compliance-context";

	private final ReadableScopeService<EObject> scope;
	private final String registry;
	private final String stage;
	private final Map<String, RequirementCitations> cached = new ConcurrentHashMap<>();

	/**
	 * @param scope    the scope to read from, never {@code null}
	 * @param registry the registry holding the contexts; blank takes {@link #DEFAULT_REGISTRY}
	 * @param stage    the stage to read them from; blank takes the scope's own view, which is the
	 *                 ordinary case - a context is reference data and sits at one stage for
	 *                 everyone
	 */
	public ComplianceContexts(ReadableScopeService<EObject> scope, String registry, String stage) {
		this.scope = Objects.requireNonNull(scope, "scope");
		this.registry = blankToNull(registry) == null ? DEFAULT_REGISTRY : registry;
		this.stage = blankToNull(stage);
	}

	/**
	 * The evidence for one requirement of one context.
	 *
	 * @param contextId     the context, as a rule catalogue names it
	 * @param requirementId the requirement, as a rule catalogue names it
	 * @param language      the language to quote in, or {@code null} for the cited corpus's own
	 * @return the answer, never {@code null}; {@link Outcome#UNKNOWN_CONTEXT} when the registry
	 *         holds no such context, which is a fault in the deployment and not a finding
	 */
	public Citation resolve(String contextId, String requirementId, String language) {
		return citations(contextId)
				.map(citations -> citations.resolve(requirementId, language))
				.orElseGet(() -> new Citation(Outcome.UNKNOWN_CONTEXT, contextId, null, requirementId, language, null,
						List.of()));
	}

	/**
	 * One context's resolver, fetched on first use.
	 *
	 * @param contextId the context id
	 * @return the resolver, or empty when the registry holds no such context
	 */
	public Optional<RequirementCitations> citations(String contextId) {
		if (blankToNull(contextId) == null) {
			return Optional.empty();
		}
		RequirementCitations known = cached.get(contextId);
		if (known != null) {
			return Optional.of(known);
		}
		Optional<ComplianceContext> context = fetch(contextId);
		if (context.isEmpty()) {
			return Optional.empty();
		}
		// Two threads racing here each build a resolver over the same stored context and one of
		// them is discarded. That is cheaper than holding a lock across a multi-megabyte fetch,
		// and the two are equivalent - read-only views of the same object.
		RequirementCitations built = new RequirementCitations(context.get());
		RequirementCitations winner = cached.putIfAbsent(contextId, built);
		return Optional.of(winner == null ? built : winner);
	}

	/** Whether a context has already been fetched, which is what a test asserting "once" asks. */
	public boolean isCached(String contextId) {
		return cached.containsKey(contextId);
	}

	private Optional<ComplianceContext> fetch(String contextId) {
		ReadableRegistryView<EObject> view = stage == null ? scope.registryView(registry)
				: scope.registryView(registry, stage);
		return view.get(contextId).filter(ComplianceContext.class::isInstance).map(ComplianceContext.class::cast);
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value;
	}
}
