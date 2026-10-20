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

/**
 * Which end of an analysed artefact a rule's statement is about, and therefore which model it is
 * written onto.
 *
 * <p>
 * An artefact's report is about the artefact. But the person maintaining a metamodel it reads or
 * writes will never open that report, and half of what it says is directly about their model - so
 * those halves are written there too, and deciding which half is which is the whole point of this
 * enum.
 * </p>
 *
 * <p>
 * The ends are named from the data's point of view, not the artefact's, which is what lets one
 * enum serve artefacts that are not transformations: {@link #SOURCE} is a model a classified value
 * is read from, {@link #TARGET} one it reaches. A persistence mapping's target is a table, a
 * resource mapping's is a published resource; in both cases the statement for the reader of the
 * metamodel is the same kind of statement.
 * </p>
 */
public enum Reach {

	/** Read by the artefact: a statement about a field whose data goes elsewhere. */
	SOURCE,

	/** Written by the artefact: a statement about a field that receives data. */
	TARGET,

	/** Both ends have something to do about it, and what they would do differs. */
	BOTH;

	public boolean touchesSource() {
		return this != TARGET;
	}

	public boolean touchesTarget() {
		return this != SOURCE;
	}
}
