/*
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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.registry.api;

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.registry.api.RegistryAccessException.Reason;

/**
 * What a batch did with one of its items.
 *
 * <p>
 * Exactly one of three cases holds: the item was written ({@code outcome} set), it was refused
 * ({@code refusal} and {@code message} set), or it was not applied because another item of the
 * batch was refused ({@code outcome} and {@code refusal} both {@code null}; whatever it had
 * written was undone).
 * </p>
 *
 * @param id       the item's identifier: the objectId of an instance, the nsURI of a package; may be
 *                 {@code null} for an instance that has none
 * @param outcome  what was written, or {@code null}
 * @param metadata the stored metadata of a written item, or {@code null}
 * @param refusal  why the item was refused, or {@code null}
 * @param message  the refusal's message, or {@code null}
 */
public record BatchItemResult(String id, WriteOutcome outcome, ObjectMetadata metadata, Reason refusal,
		String message) {

	/**
	 * @param id     the item's identifier
	 * @param result what writing it did
	 * @return a written item
	 */
	public static BatchItemResult written(String id, WriteResult result) {
		return new BatchItemResult(id, result.outcome(), result.metadata(), null, null);
	}

	/**
	 * @param id      the item's identifier
	 * @param refusal why it was refused
	 * @return a refused item
	 */
	public static BatchItemResult refused(String id, RegistryAccessException refusal) {
		return new BatchItemResult(id, null, null, refusal.reason(), refusal.getMessage());
	}

	/**
	 * @param id the item's identifier
	 * @return an item that is not applied because another item was refused
	 */
	public static BatchItemResult notApplied(String id) {
		return new BatchItemResult(id, null, null, null, null);
	}

	/**
	 * @return whether this item was refused
	 */
	public boolean isRefused() {
		return refusal != null;
	}
}
