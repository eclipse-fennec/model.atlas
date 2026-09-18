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
package org.eclipse.fennec.model.atlas.rest.client.api;

/**
 * One package change, named precisely enough to act on (#276).
 * <p>
 * Drift used to be reported as a bare nsURI, which is not an identity: several versions of one
 * nsURI can be live at once, one per stage. A holder of the {@code release} version could not tell
 * that only {@code draft} had moved, so it evicted a still-valid package to be safe.
 *
 * @param scope       the scope the change was observed in
 * @param stage       the stage the change happened at, or {@code null} when the server did not say
 * @param nsUri       the namespace URI that changed
 * @param fingerprint the model version now at that location, or {@code null} when it is gone — or
 *                    when the server reports no version detail (an Atlas older than #276)
 *
 * @author Data In Motion
 */
public record PackageDrift(String scope, String stage, String nsUri, String fingerprint) {

	/**
	 * Whether this change concerns a holder located at {@code atScope}/{@code atStage}.
	 * <p>
	 * A stage-free holder (a {@code null} {@code atStage}) takes every change for its scope: it
	 * tracks whatever the scope's final stage resolves to, and the server reports final-stage
	 * moves like any other. A holder that knows its stage takes only changes at that stage — and,
	 * where the server reported no stage at all, takes the change rather than risk missing one.
	 *
	 * @param atScope the holder's scope, or {@code null} to match any
	 * @param atStage the holder's stage, or {@code null} for a stage-free holder
	 * @return whether the holder should react
	 */
	public boolean concerns(String atScope, String atStage) {
		if (atScope != null && scope != null && !atScope.equals(scope)) {
			return false;
		}
		if (atStage == null || stage == null) {
			return true;
		}
		return atStage.equals(stage);
	}
}
