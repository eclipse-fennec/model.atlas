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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.runtime.info.internal;

import java.util.List;

/**
 * One media type contributor, reduced to what the snapshot needs.
 *
 * @param serviceId  the {@code service.id} of the contributor
 * @param mediaTypes what it declares it can serve
 */
record TrackedContributor(long serviceId, List<String> mediaTypes) {
}
