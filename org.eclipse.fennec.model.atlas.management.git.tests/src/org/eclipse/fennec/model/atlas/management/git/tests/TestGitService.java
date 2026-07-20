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
package org.eclipse.fennec.model.atlas.management.git.tests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.gecko.jgit.api.GitService;
import org.gecko.jgit.api.TreeResult;

/**
 * Test-only {@link GitService} used by the git-backend OSGi ITs instead of the real
 * {@code org.gecko.jgit.GitServiceImpl}.
 *
 * <h3>Why this exists (gecko.jgit limitation)</h3>
 * <p>{@code GitServiceImpl.activate()} <em>unconditionally</em> installs a
 * {@code TransportConfigCallback} that casts every jgit {@code Transport} to
 * {@code SshTransport} to attach an SSH private key. That makes it usable only with
 * SSH scp-like remotes ({@code git@host:path}); over {@code git://} the transport is
 * {@code TransportGitAnon} and over {@code https://} it is {@code TransportHttp}, so
 * both fail with a {@code ClassCastException}. SSH scp-like URLs carry no port, and
 * {@code ssh://host:port/…} is rejected by gecko's {@code isRemote} check (it only
 * accepts {@code git}/{@code https} prefixes), so the real impl cannot be driven from
 * a Testcontainers-mapped random port. Whether that SSH-only restriction is intended
 * is an open question for the team (see {@code PLAN.md} G8); until it is decided this
 * class stands in for {@code GitServiceImpl}.
 *
 * <p>It does exactly what {@code GitServiceImpl} does — fetch all heads from the
 * remote into a local repository and read trees/blobs without a working-tree checkout —
 * but with <b>no transport callback</b>, so the anonymous {@code git://} protocol
 * served by the test container works. Everything downstream ({@code GitStorageHelper},
 * {@code GitURIHandler}, reconcile, the webhook/poll paths) is the production code and
 * is fully exercised.
 */
public final class TestGitService implements GitService {

	private final String gitUrl;
	private final String branch;
	private final Repository repository;

	/**
	 * @param gitUrl   the {@code git://…} URL of the served repo
	 * @param branch   the branch (= stage) this service represents
	 * @param gitDir   a fresh directory to hold this service's bare mirror
	 */
	public TestGitService(String gitUrl, String branch, Path gitDir) throws Exception {
		this.gitUrl = gitUrl;
		this.branch = branch;
		this.repository = new FileRepositoryBuilder().setGitDir(gitDir.toFile()).build();
		this.repository.create(true); // bare mirror
		fetch();
	}

	@Override
	public void fetch() {
		try (Git git = Git.wrap(repository)) {
			git.fetch()
					.setRemote(gitUrl)
					.setRefSpecs(new RefSpec("+refs/heads/*:refs/heads/*"))
					.call();
		} catch (Exception e) {
			throw new IllegalStateException("Test git fetch failed for " + gitUrl, e);
		}
	}

	@Override
	public TreeResult getFiles() {
		return getFiles(branch);
	}

	@Override
	public TreeResult getFiles(String branchName) {
		try {
			ObjectId commitId = repository.resolve("refs/heads/" + branchName);
			if (commitId == null) {
				return new TreeResult(null, new ArrayList<>());
			}
			List<String> files = new ArrayList<>();
			try (RevWalk revWalk = new RevWalk(repository); TreeWalk treeWalk = new TreeWalk(repository)) {
				RevCommit commit = revWalk.parseCommit(commitId);
				treeWalk.addTree(commit.getTree());
				treeWalk.setRecursive(true);
				while (treeWalk.next()) {
					files.add(treeWalk.getPathString());
				}
			}
			return new TreeResult(commitId.name(), files);
		} catch (Exception e) {
			throw new IllegalStateException("Test git getFiles failed for branch " + branchName, e);
		}
	}

	@Override
	public InputStream readFile(String commit, String path) {
		try {
			ObjectId commitId = repository.resolve(commit);
			if (commitId == null) {
				return null;
			}
			try (RevWalk revWalk = new RevWalk(repository)) {
				RevCommit revCommit = revWalk.parseCommit(commitId);
				try (TreeWalk treeWalk = TreeWalk.forPath(repository, path, revCommit.getTree())) {
					if (treeWalk == null) {
						return null;
					}
					ObjectLoader loader = repository.open(treeWalk.getObjectId(0));
					return new ByteArrayInputStream(loader.getBytes());
				}
			}
		} catch (Exception e) {
			throw new IllegalStateException("Test git readFile failed for " + commit + ":" + path, e);
		}
	}

	@Override
	public InputStream readLatestFile(String path) {
		return readFile(branch, path);
	}

	@Override
	public void loadFile(String commit, String path, OutputStream out) {
		try (InputStream in = readFile(commit, path)) {
			if (in != null) {
				in.transferTo(out);
			}
		} catch (Exception e) {
			throw new IllegalStateException("Test git loadFile failed for " + commit + ":" + path, e);
		}
	}

	@Override
	public void loadLatestFile(String path, OutputStream out) {
		loadFile(branch, path, out);
	}

	@Override
	public List<String> getBranches() {
		try {
			List<String> branches = new ArrayList<>();
			for (Ref ref : repository.getRefDatabase().getRefsByPrefix("refs/heads/")) {
				branches.add(ref.getName().substring("refs/heads/".length()));
			}
			return branches;
		} catch (Exception e) {
			throw new IllegalStateException("Test git getBranches failed", e);
		}
	}

	@Override
	public Iterable<RevCommit> getLog() {
		try (Git git = Git.wrap(repository)) {
			ObjectId commitId = repository.resolve("refs/heads/" + branch);
			return git.log().add(commitId).call();
		} catch (Exception e) {
			throw new IllegalStateException("Test git getLog failed", e);
		}
	}

	@Override
	public String getBranch() {
		return branch;
	}

	@Override
	public String getGitUrl() {
		return gitUrl;
	}

	@Override
	public String getRef() {
		return "refs/heads/" + branch;
	}
}
