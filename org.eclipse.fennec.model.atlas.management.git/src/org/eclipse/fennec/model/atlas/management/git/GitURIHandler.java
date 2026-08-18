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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.impl.URIHandlerImpl;
import org.eclipse.fennec.jgit.api.GitService;

/**
 * EMF {@link org.eclipse.emf.ecore.resource.URIHandler} that resolves
 * {@code git://{commitId}/{path}} URIs by streaming a blob straight out of the
 * local clone maintained by a {@link GitService} — no working-tree checkout.
 *
 * <p>Adapted from {@code de.jena.mdo.git.epackage.registry.GitURIHandler} in the
 * jena-MDO reference. The reference bound a single {@code GitService}; here the
 * git storage backend may serve several branches (= stages) of one repo, each
 * with its own {@code GitService}, so this handler routes to the right service
 * by the commit id carried in the URI host. Because a commit id is globally
 * unique within the repo, the mapping is unambiguous regardless of whether the
 * per-branch clones share object stores.
 *
 * <p>Read-only: {@link #createOutputStream(URI, Map)} always throws, so an attempt
 * to {@code Resource.save()} a {@code git://} URI fails cleanly and visibly (git
 * content is written externally on the git host, not through this backend).
 */
public class GitURIHandler extends URIHandlerImpl {

	private static final String READ_ONLY_MESSAGE =
			"Git storage is read-only; writes happen externally on the git host";

	private final Map<String, GitService> commitToService;

	/**
	 * @param commitToService live view of commit id → owning {@link GitService};
	 *        the git storage helper keeps it populated as branches are refreshed.
	 */
	public GitURIHandler(Map<String, GitService> commitToService) {
		this.commitToService = commitToService;
	}

	@Override
	public boolean canHandle(URI uri) {
		return "git".equals(uri.scheme());
	}

	@Override
	public InputStream createInputStream(URI uri, Map<?, ?> options) throws IOException {
		String commitId = uri.host();
		GitService gitService = commitToService.get(commitId);
		if (gitService == null) {
			throw new IOException("No git service known for commit " + commitId + " (uri " + uri + ")");
		}
		String file = GitEMFHelper.getGitFilePath(uri);
		return gitService.readFile(commitId, file);
	}

	/**
	 * Read-only backend: writing a {@code git://} URI is never supported. Throwing here
	 * makes {@code Resource.save()} fail immediately with a clear read-only signal instead
	 * of the opaque failure produced by the default {@link URIHandlerImpl} write path.
	 */
	@Override
	public OutputStream createOutputStream(URI uri, Map<?, ?> options) throws IOException {
		throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
	}
}
