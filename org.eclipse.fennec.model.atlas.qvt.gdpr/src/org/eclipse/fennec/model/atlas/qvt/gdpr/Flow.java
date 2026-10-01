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

import org.eclipse.fennec.model.gdprReport.FlowKind;

/**
 * One path along which a compiled transformation moves a value: one source feature reaching one
 * target feature, in one mapping.
 * <p>
 * It is a fact read off the compiled unit and says nothing about data protection - the
 * classification comes from the metamodels' reviews, and the rules are applied to the two
 * together. A flow is recorded even when nothing classifies either end, because a flow the
 * analyser could not interpret has to be visible rather than absent.
 *
 * @param mapping        the mapping or helper the assignment sits in. A compiled unit has no line
 *                       numbers, so this is the coarsest address a developer can act on
 * @param sourceNsURI    nsURI of the metamodel the source feature belongs to, or {@code null} when
 *                       no feature was read at all
 * @param sourceFeature  EMF fragment of the feature that is read, {@code //Patient/diagnosis}. It
 *                       matches the {@code uriFragment} of a {@code FeatureEvaluation} in that
 *                       metamodel's own review, which is how a finding inherits its category and
 *                       its evidence
 * @param targetNsURI    nsURI of the metamodel the target feature belongs to
 * @param targetFeature  EMF fragment of the feature that is written, {@code //Contact/comment}
 * @param targetTypeName  name of the target feature's type, {@code EString} and the like, or
 *                       {@code null} when the target could not be resolved. Whether a
 *                       classification can survive in a field turns on it
 * @param kind           how the value travels
 * @param targetResolved whether the target feature was found on the mapping's result type. The
 *                       target is matched <em>by name</em> (see {@link QvtFlowExtractor}), so this
 *                       records whether that name matched anything; an unresolved target makes the
 *                       flow {@link FlowKind#OPAQUE}
 */
record Flow(String mapping, String sourceNsURI, String sourceFeature, String targetNsURI,
		String targetFeature, String targetTypeName, FlowKind kind, boolean targetResolved) {

	/** {@code <nsURI>#<fragment>} of the source feature, the key a review is joined on. */
	String sourceKey() {
		return sourceFeature == null ? null : sourceNsURI + "#" + sourceFeature;
	}

	/** Which target field the flow ends in, independently of where it came from. */
	String targetKey() {
		return targetNsURI + "#" + targetFeature;
	}
}
