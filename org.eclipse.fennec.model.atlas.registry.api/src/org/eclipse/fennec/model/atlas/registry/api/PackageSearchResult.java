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

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;

/**
 * One page of a package search.
 *
 * @param hits      the metadata of the hits on this page that still resolve; a hit from an
 *                  ancestor scope is marked read-only
 * @param totalHits the number of hits across all pages, as the search index counts them
 */
public record PackageSearchResult(List<ObjectMetadata> hits, long totalHits) {
}
