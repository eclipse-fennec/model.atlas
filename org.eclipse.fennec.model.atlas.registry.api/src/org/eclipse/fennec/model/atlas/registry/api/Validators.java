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
import org.eclipse.fennec.model.atlas.mgmt.storage.MetadataHash;

/**
 * The validators a {@link Precondition} is checked against: hashes the registry keeps in an
 * object's metadata. Front ends derive their own validators from them (the REST layer its ETags),
 * so that what a client was handed can be turned back into a precondition.
 */
public final class Validators {

	private Validators() {
	}

	/**
	 * The validator of an object.
	 *
	 * <ul>
	 * <li>{@link PreconditionTarget#CONTENT}: {@link ObjectMetadata#getContentHash()}.</li>
	 * <li>{@link PreconditionTarget#METADATA}: {@link ObjectMetadata#getMetadataHash()}; for
	 * metadata stored before that hash existed, the hash its fields give.</li>
	 * </ul>
	 *
	 * @param metadata the object's metadata, may be {@code null}
	 * @param target   which validator; {@code null} means {@link PreconditionTarget#CONTENT}
	 * @return the validator, or {@code null} when none can be derived
	 */
	public static String validator(ObjectMetadata metadata, PreconditionTarget target) {
		if (metadata == null) {
			return null;
		}
		return target == PreconditionTarget.METADATA ? MetadataHash.of(metadata) : metadata.getContentHash();
	}
}
