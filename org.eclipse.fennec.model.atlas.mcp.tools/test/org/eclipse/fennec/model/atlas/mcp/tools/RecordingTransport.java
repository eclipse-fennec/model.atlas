/*
 * ******************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Data In Motion Consulting - initial implementation
 * ******************************************************************
 */
package org.eclipse.fennec.model.atlas.mcp.tools;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An {@link AtlasTransport} that answers with a canned status and records what it
 * was asked to send. Everything above the transport is testable this way, which
 * is why the seam exists.
 *
 * @author ilenia
 * @since Aug 27, 2026
 */
class RecordingTransport implements AtlasTransport {

	private final AtlasTransport.Result postResult;
	private AtlasTransport.Result getResult = new AtlasTransport.Result(200, "");

	String path;
	Map<String, String> query = new LinkedHashMap<>();
	String contentType;
	String body;
	final List<String> gets = new ArrayList<>();

	RecordingTransport(int status) {
		this(status, "");
	}

	RecordingTransport(int status, String body) {
		this.postResult = new AtlasTransport.Result(status, body);
	}

	/** Sets what a stage probe answers; 400 stands for a stage the server does not have. */
	RecordingTransport withStageStatus(int status) {
		this.getResult = new AtlasTransport.Result(status, "");
		return this;
	}

	@Override
	public AtlasTransport.Result post(String path, Map<String, String> query, String contentType, String body) {
		this.path = path;
		this.query = new LinkedHashMap<>(query);
		this.contentType = contentType;
		this.body = body;
		return postResult;
	}

	@Override
	public AtlasTransport.Result get(String path) {
		gets.add(path);
		return getResult;
	}
}
