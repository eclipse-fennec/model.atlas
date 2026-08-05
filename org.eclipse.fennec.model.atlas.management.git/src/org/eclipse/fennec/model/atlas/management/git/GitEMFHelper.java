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
package org.eclipse.fennec.model.atlas.management.git;

import org.eclipse.emf.common.util.URI;

/**
 * Helpers for the {@code git://{commitId}/{path}} URI scheme used by the git
 * storage backend.
 *
 * <p>Lifted from {@code de.jena.mdo.git.epackage.registry.GitEMFHelper} in the
 * jena-MDO reference.
 */
public class GitEMFHelper {

	private GitEMFHelper() {
		// utility
	}

	/**
	 * Extracts the repository-relative file path (including extension) from a
	 * {@code git://{commitId}/{path}} URI. The commit id is carried in the URI
	 * host, so the path is everything after it.
	 *
	 * @param uri a {@code git://} URI
	 * @return the repository-relative path, without a leading slash
	 */
	public static String getGitFilePath(URI uri) {
		URI filePart = uri.deresolve(uri.trimSegments(uri.segmentCount()).trimFragment().trimFileExtension());
		String file = filePart.toString();
		if (file.startsWith("/")) {
			file = file.substring(1);
		}
		return file;
	}

	/**
	 * Builds a {@code git://{commitId}/{path}} URI for a repository-relative path
	 * at a specific commit.
	 *
	 * @param commitId the commit SHA (URI host)
	 * @param path the repository-relative path, with extension
	 * @return the git URI
	 */
	public static URI createGitURI(String commitId, String path) {
		String normalized = path.startsWith("/") ? path.substring(1) : path;
		return URI.createURI("git://" + commitId + "/" + normalized);
	}
}
