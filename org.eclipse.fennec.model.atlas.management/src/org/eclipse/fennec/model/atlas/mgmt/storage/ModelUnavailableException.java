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
package org.eclipse.fennec.model.atlas.mgmt.storage;

import java.io.IOException;

/**
 * Thrown when an instance cannot be read because the EPackage its content references is not
 * registered. This is a general storage condition, not specific to any one backend: any backend
 * that stores instances separately from their models can end up with an instance whose model has
 * been removed or is not (yet) available (e.g. a git push that removed a schema on a branch while
 * the instance file remains — D8-3 — or a deleted {@code .ecore} in the file backend).
 *
 * <p>It is a clean, typed "model unavailable" signal so callers (and the REST layer) can surface a
 * meaningful state instead of an opaque parse failure / 500. It is thrown centrally from
 * {@link AbstractStorageHelper#loadEObject}, so every backend benefits uniformly.
 *
 * <p>Extends {@link IOException} so it flows through the storage read path
 * ({@code loadEObject} declares {@code throws IOException}), keeping the missing
 * {@link #getNsURI() nsURI} and the object's coordinates available for diagnostics and for
 * mapping to an HTTP response.
 */
public class ModelUnavailableException extends IOException {

	private static final long serialVersionUID = 1L;

	private final String scope;
	private final String stage;
	private final String objectId;
	private final String nsURI;

	public ModelUnavailableException(String scope, String stage, String objectId, String nsURI, Throwable cause) {
		super("Model unavailable for object " + objectId + " (scope=" + scope + ", stage=" + stage + ")"
				+ (nsURI != null ? " — required package is not registered: " + nsURI : "")
				+ " (the schema was likely removed)", cause);
		this.scope = scope;
		this.stage = stage;
		this.objectId = objectId;
		this.nsURI = nsURI;
	}

	public String getScope() {
		return scope;
	}

	public String getStage() {
		return stage;
	}

	public String getObjectId() {
		return objectId;
	}

	/** The nsURI of the missing package, or {@code null} if it could not be determined. */
	public String getNsURI() {
		return nsURI;
	}
}
