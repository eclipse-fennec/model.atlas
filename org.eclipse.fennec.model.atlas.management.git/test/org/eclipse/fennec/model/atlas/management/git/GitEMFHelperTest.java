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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@code git://{commitId}/{path}} URI helpers.
 */
class GitEMFHelperTest {

	@Test
	void roundTrip_nestedPathWithExtension() {
		URI uri = GitEMFHelper.createGitURI("abc123", "models/person.ecore");
		assertEquals("git://abc123/models/person.ecore", uri.toString());
		assertEquals("abc123", uri.host());
		assertEquals("models/person.ecore", GitEMFHelper.getGitFilePath(uri));
	}

	@Test
	void getGitFilePath_topLevelFile() {
		URI uri = URI.createURI("git://c1/person.xmi");
		assertEquals("person.xmi", GitEMFHelper.getGitFilePath(uri));
	}

	@Test
	void createGitURI_stripsLeadingSlash() {
		assertEquals("git://c1/a/b.ecore", GitEMFHelper.createGitURI("c1", "/a/b.ecore").toString());
	}
}
