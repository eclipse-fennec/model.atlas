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

import java.util.List;
import java.util.Optional;

/**
 * The result of a batch write: all of its items were applied, or none.
 *
 * @param applied whether the batch was applied; {@code false} means at least one item was refused
 *                and nothing of the batch remains written
 * @param items   one result per item, in the order of the batch
 */
public record BatchResult(boolean applied, List<BatchItemResult> items) {

	/**
	 * @param applied whether the batch was applied
	 * @param items   one result per item
	 */
	public BatchResult {
		items = List.copyOf(items);
	}

	/**
	 * @return the first refused item, if the batch was not applied
	 */
	public Optional<BatchItemResult> firstRefusal() {
		return items.stream().filter(BatchItemResult::isRefused).findFirst();
	}
}
