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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.MountableFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test fixture that serves a real, read-only git repository over the anonymous
 * {@code git://} protocol from a throw-away container, mirroring how the git
 * storage backend talks to GitHub/GitLab in production.
 *
 * <p>Why {@code git://} and not {@code http://}: {@code org.gecko.jgit}'s
 * {@code GitServiceImpl} only treats a URL as <em>remote</em> (and therefore
 * clones/fetches it) when it starts with {@code git}, {@code https} or {@code ssh};
 * a plain {@code http://} URL falls into its local-{@code File} branch and fails.
 * The anonymous git-daemon protocol needs no TLS and no credentials, which is
 * exactly right for our read-only backend, so we use it here. Since gecko.jgit
 * moved to the Apache MINA sshd backend and only installs the SSH session factory
 * when a {@code privateKey} is configured, the real {@code GitServiceImpl} can be
 * driven over this URL directly (no test stand-in needed anymore).
 *
 * <h3>Layout of the served repo</h3>
 * <ul>
 *   <li>branch {@code main}: {@link #PERSON_ECORE} (the {@code person} EPackage with
 *       a single {@code Person.name} attribute) + a {@code README.md}.</li>
 *   <li>branch {@code release}: the same {@link #PERSON_ECORE} path but with an extra
 *       {@code Person.email} attribute, so the two branches (= stages) carry different
 *       content at the same repo path — the D9 collision case — and different commits.</li>
 * </ul>
 *
 * <p>The fixture repo is built on the host with the {@code git} CLI and copied into
 * the container at creation time. {@link #commitOnBranch(String, String, String)}
 * adds a further commit to the <em>served</em> repo at runtime (via
 * {@code git} inside the container), which the webhook/poll tests use to simulate an
 * external push.
 */
public final class GitTestRepository implements AutoCloseable {

	/** Repo name as served by the daemon; the objectId repo-paths are relative to this. */
	public static final String REPO_NAME = "testrepo.git";

	public static final String BRANCH_MAIN = "main";
	public static final String BRANCH_RELEASE = "release";

	/** Repo-relative path of the person schema present on both branches. */
	public static final String PERSON_ECORE = "models/person.ecore";

	/** nsURI of the {@code person} EPackage shipped in {@link #PERSON_ECORE}. */
	public static final String PERSON_NS_URI = "http://example.org/person/1.0";

	/** Repo-relative path of a {@code person} <em>instance</em> present on {@code main}. */
	public static final String ALICE_XMI = "instances/alice.xmi";

	/** A {@code Person} instance (of {@link #PERSON_ECORE}); resolvable only when person is registered. */
	public static final String ALICE_XMI_CONTENT = ""
			+ "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<person:Person xmlns:person=\"" + PERSON_NS_URI + "\" name=\"Alice\"/>\n";

	/**
	 * The {@code person} schema with an EXTRA {@code email} attribute — pushed over
	 * {@link #PERSON_ECORE} at runtime to simulate a schema <em>reload</em> (same nsURI, new
	 * commit, changed content) for the referential-integrity tests.
	 */
	public static final String PERSON_ECORE_WITH_EMAIL = ""
			+ "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<ecore:EPackage xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\""
			+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
			+ " xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\""
			+ " name=\"person\" nsURI=\"" + PERSON_NS_URI + "\" nsPrefix=\"person\">\n"
			+ "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Person\">\n"
			+ "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\""
			+ " eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
			+ "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"email\""
			+ " eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
			+ "  </eClassifiers>\n"
			+ "</ecore:EPackage>\n";

	// --- cross-ecore referential-integrity fixtures (product -> category) ---

	public static final String CATEGORY_NS_URI = "http://example.org/category/1.0";
	public static final String PRODUCT_NS_URI = "http://example.org/product/1.0";
	public static final String CATEGORY_ECORE = "models/category.ecore";
	public static final String PRODUCT_ECORE = "models/product.ecore";

	/** Package {@code category} with EClass {@code Category} (attribute {@code name}). */
	public static final String CATEGORY_ECORE_CONTENT = ""
			+ "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<ecore:EPackage xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\""
			+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
			+ " xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\""
			+ " name=\"category\" nsURI=\"" + CATEGORY_NS_URI + "\" nsPrefix=\"category\">\n"
			+ "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Category\">\n"
			+ "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\""
			+ " eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
			+ "  </eClassifiers>\n"
			+ "</ecore:EPackage>\n";

	/** {@code category} reloaded with an EXTRA {@code code} attribute (same nsURI, new commit). */
	public static final String CATEGORY_ECORE_CONTENT_V2 = ""
			+ "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<ecore:EPackage xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\""
			+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
			+ " xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\""
			+ " name=\"category\" nsURI=\"" + CATEGORY_NS_URI + "\" nsPrefix=\"category\">\n"
			+ "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Category\">\n"
			+ "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\""
			+ " eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
			+ "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"code\""
			+ " eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
			+ "  </eClassifiers>\n"
			+ "</ecore:EPackage>\n";

	/**
	 * Package {@code product} whose EClass {@code Product} has an EReference {@code category}
	 * whose type is {@code Category} in {@link #CATEGORY_NS_URI} — a <em>cross-ecore</em>
	 * reference that only resolves when the {@code category} package is registered.
	 */
	public static final String PRODUCT_ECORE_CONTENT = ""
			+ "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<ecore:EPackage xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\""
			+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
			+ " xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\""
			+ " name=\"product\" nsURI=\"" + PRODUCT_NS_URI + "\" nsPrefix=\"product\">\n"
			+ "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Product\">\n"
			+ "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"title\""
			+ " eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
			+ "    <eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"category\""
			+ " eType=\"ecore:EClass " + CATEGORY_NS_URI + "#//Category\"/>\n"
			+ "  </eClassifiers>\n"
			+ "</ecore:EPackage>\n";

	/** Repo-relative path used by the webhook/poll tests to push a NEW schema at runtime. */
	public static final String ANIMAL_ECORE = "models/animal.ecore";

	/** A self-contained {@code animal} EPackage the webhook/poll tests push onto a branch. */
	public static final String ANIMAL_ECORE_CONTENT = ""
			+ "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<ecore:EPackage xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\""
			+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
			+ " xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\""
			+ " name=\"animal\" nsURI=\"http://example.org/animal/1.0\" nsPrefix=\"animal\">\n"
			+ "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Animal\">\n"
			+ "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"species\""
			+ " eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
			+ "  </eClassifiers>\n"
			+ "</ecore:EPackage>\n";

	private static final String PERSON_ECORE_MAIN = ""
			+ "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<ecore:EPackage xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\""
			+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
			+ " xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\""
			+ " name=\"person\" nsURI=\"" + PERSON_NS_URI + "\" nsPrefix=\"person\">\n"
			+ "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Person\">\n"
			+ "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\""
			+ " eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
			+ "  </eClassifiers>\n"
			+ "</ecore:EPackage>\n";

	private static final String PERSON_ECORE_RELEASE = ""
			+ "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<ecore:EPackage xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\""
			+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
			+ " xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\""
			+ " name=\"person\" nsURI=\"" + PERSON_NS_URI + "\" nsPrefix=\"person\">\n"
			+ "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Person\">\n"
			+ "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\""
			+ " eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
			+ "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"email\""
			+ " eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
			+ "  </eClassifiers>\n"
			+ "</ecore:EPackage>\n";

	private static final int GIT_DAEMON_PORT = 9418;
	private static final String CONTAINER_REPOS = "/srv/repos";

	private static final Logger LOG = LoggerFactory.getLogger(GitTestRepository.class);

	private final Path bareRepo;
	private final GenericContainer<?> container;

	/**
	 * Builds the fixture repo under {@code root} and prepares (but does not start) the
	 * serving container.
	 *
	 * @param root a writable working directory (e.g. a JUnit {@code @TempDir})
	 */
	@SuppressWarnings("resource")
	public GitTestRepository(Path root) throws Exception {
		LOG.info("[git-it] building fixture repo under {}", root);
		this.bareRepo = buildFixtureRepo(root);
		LOG.info("[git-it] fixture bare repo ready at {}", bareRepo);
		LOG.info("[git-it] preparing git-daemon container image (alpine + git-daemon)...");
		this.container = new GenericContainer<>(new ImageFromDockerfile()
				.withDockerfileFromBuilder(b -> b
						.from("alpine:3.20")
						// git-daemon is a SEPARATE alpine package; the base `git` package does
						// not ship the `git daemon` subcommand.
						.run("apk add --no-cache git git-daemon")
						.run("git config --system --add safe.directory '*'")
						.run("mkdir -p " + CONTAINER_REPOS)
						.expose(GIT_DAEMON_PORT)
						.build()))
				.withCopyFileToContainer(MountableFile.forHostPath(bareRepo), CONTAINER_REPOS + "/" + REPO_NAME)
				.withExposedPorts(GIT_DAEMON_PORT)
				.withCommand("git", "daemon", "--reuseaddr", "--verbose", "--export-all",
						"--base-path=" + CONTAINER_REPOS, CONTAINER_REPOS)
				// Stream the daemon's own stdout/stderr into the test log, and fail fast
				// (instead of hanging) if it never starts listening.
				.withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("git-daemon")))
				.waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(60)));
	}

	public void start() {
		LOG.info("[git-it] starting git-daemon container (first run builds the image, may take a while)...");
		container.start();
		LOG.info("[git-it] container started; serving {} (mapped port {})", gitUrl(),
				container.getMappedPort(GIT_DAEMON_PORT));
	}

	/** The {@code git://host:port/testrepo.git} URL the daemon serves (valid after {@link #start()}). */
	public String gitUrl() {
		return "git://" + container.getHost() + ":" + container.getMappedPort(GIT_DAEMON_PORT) + "/" + REPO_NAME;
	}

	/**
	 * Adds a commit to the <em>served</em> repo on {@code branch}, writing {@code content}
	 * to {@code repoPath}, and returns the new commit SHA. Used to simulate an external
	 * push between fetches (webhook / reconcile-poll tests). Operates directly on the bare
	 * repo inside the container via a transient work tree, so the daemon serves the new tip
	 * on the next fetch.
	 */
	public String commitOnBranch(String branch, String repoPath, String content) throws Exception {
		String repo = CONTAINER_REPOS + "/" + REPO_NAME;
		String wt = "/tmp/wt-" + branch;
		// Fresh work tree checked out at the branch tip, write the file, commit, done.
		execOk("sh", "-c", "rm -rf " + wt + " && git --git-dir=" + repo + " worktree add -f " + wt + " " + branch);
		String rel = wt + "/" + repoPath;
		execOk("sh", "-c", "mkdir -p \"$(dirname " + rel + ")\" && cat > " + rel + " <<'EOF'\n" + content + "\nEOF");
		execOk("sh", "-c", "cd " + wt + " && git config user.email t@t.t && git config user.name t"
				+ " && git add -A && git commit -q -m 'runtime push to " + branch + "'");
		// Prune the work tree so only the bare repo (which the daemon serves) remains.
		execOk("sh", "-c", "git --git-dir=" + repo + " worktree remove --force " + wt);
		ExecResult sha = container.execInContainer("git", "--git-dir=" + repo, "rev-parse", branch);
		return sha.getStdout().trim();
	}

	/**
	 * Removes {@code repoPath} from {@code branch} in the <em>served</em> repo and commits,
	 * returning the new commit SHA. Simulates an external push that deletes a file (schema
	 * removal — D8-3). Operates on the bare repo via a transient work tree.
	 */
	public String removeOnBranch(String branch, String repoPath) throws Exception {
		String repo = CONTAINER_REPOS + "/" + REPO_NAME;
		String wt = "/tmp/wt-rm-" + branch;
		execOk("sh", "-c", "rm -rf " + wt + " && git --git-dir=" + repo + " worktree add -f " + wt + " " + branch);
		execOk("sh", "-c", "cd " + wt + " && git rm -q " + repoPath + " && git config user.email t@t.t"
				+ " && git config user.name t && git commit -q -m 'runtime remove " + repoPath + " from " + branch + "'");
		execOk("sh", "-c", "git --git-dir=" + repo + " worktree remove --force " + wt);
		ExecResult sha = container.execInContainer("git", "--git-dir=" + repo, "rev-parse", branch);
		return sha.getStdout().trim();
	}

	private void execOk(String... cmd) throws Exception {
		ExecResult r = container.execInContainer(cmd);
		if (r.getExitCode() != 0) {
			throw new IllegalStateException("Command failed (" + r.getExitCode() + "): " + String.join(" ", cmd)
					+ "\nstdout: " + r.getStdout() + "\nstderr: " + r.getStderr());
		}
	}

	@Override
	public void close() {
		container.stop();
	}

	// --- fixture construction (host side) -----------------------------------

	/**
	 * Creates a work repo with {@code main} + {@code release} branches carrying the
	 * fixtures, then produces a bare clone (with both branches) that the daemon serves.
	 *
	 * @return the path of the bare repo to copy into the container
	 */
	private static Path buildFixtureRepo(Path root) throws Exception {
		Path work = root.resolve("work");
		Files.createDirectories(work);

		git(work, "init", "-q", "-b", BRANCH_MAIN);
		git(work, "config", "user.email", "t@t.t");
		git(work, "config", "user.name", "t");

		writeFile(work, PERSON_ECORE, PERSON_ECORE_MAIN);
		writeFile(work, ALICE_XMI, ALICE_XMI_CONTENT);
		writeFile(work, "README.md", "main\n");
		git(work, "add", "-A");
		git(work, "commit", "-q", "-m", "main commit");

		git(work, "branch", BRANCH_RELEASE);
		git(work, "checkout", "-q", BRANCH_RELEASE);
		writeFile(work, PERSON_ECORE, PERSON_ECORE_RELEASE);
		writeFile(work, "README.md", "release\n");
		git(work, "add", "-A");
		git(work, "commit", "-q", "-m", "release commit");
		git(work, "checkout", "-q", BRANCH_MAIN);

		// Deliberately NOT `git clone --bare`: a local clone copies/hardlinks the object
		// directory file by file, which has proven flaky on CI runners (it died with
		// "failed to copy file to '.../objects/xx/...': No such file or directory").
		// An init-plus-push moves the objects through git's normal, atomic transfer instead.
		Path bare = root.resolve(REPO_NAME);
		Files.createDirectories(bare);
		git(bare, "init", "-q", "--bare", "-b", BRANCH_MAIN);
		git(work, "push", "-q", bare.toString(), BRANCH_MAIN, BRANCH_RELEASE);
		return bare;
	}

	private static void writeFile(Path base, String relPath, String content) throws IOException {
		Path p = base.resolve(relPath);
		Files.createDirectories(p.getParent());
		Files.writeString(p, content, StandardCharsets.UTF_8);
	}

	private static void git(Path dir, String... args) throws Exception {
		List<String> cmd = new ArrayList<>();
		cmd.add("git");
		for (String a : args) {
			cmd.add(a);
		}
		Process p = new ProcessBuilder(cmd)
				.directory(dir.toFile())
				.redirectErrorStream(true)
				.start();
		String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		int code = p.waitFor();
		if (code != 0) {
			throw new IllegalStateException("git " + String.join(" ", args) + " failed (" + code + "): " + out);
		}
	}
}
