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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;

/**
 * The {@link ObjectMetadata#getMetadataHash() metadata hash}: a token of the metadata state a
 * client can observe. It changes with the content hash, the version, the status, the last change
 * time and the diagnostics, and with nothing else, so that a metadata-only change (a stage
 * transition, a diagnostics edit) is visible while the content hash stays as it is.
 */
public final class MetadataHash {

	private MetadataHash() {
	}

	/**
	 * Computes the hash and stores it in the metadata.
	 *
	 * @param metadata the metadata about to be stored or handed out
	 * @return {@code metadata}
	 */
	public static ObjectMetadata update(ObjectMetadata metadata) {
		metadata.setMetadataHash(compute(metadata));
		return metadata;
	}

	/**
	 * The metadata hash of an object: the stored one, or, for metadata stored before the hash
	 * existed, the one its fields give. Such metadata gets the stored hash with its next write.
	 *
	 * @param metadata the metadata, may be {@code null}
	 * @return the hash, or {@code null} for {@code null} metadata or metadata without any of the
	 *         fields the hash covers
	 */
	public static String of(ObjectMetadata metadata) {
		if (metadata == null) {
			return null;
		}
		String stored = metadata.getMetadataHash();
		return stored != null ? stored : compute(metadata);
	}

	/**
	 * Computes the hash from the fields it covers, ignoring a stored one.
	 *
	 * @param metadata the metadata
	 * @return the hash, or {@code null} when none of the covered fields is set
	 */
	public static String compute(ObjectMetadata metadata) {
		String contentHash = metadata.getContentHash();
		String version = metadata.getVersion();
		Object status = metadata.getStatus();
		Object lastChange = metadata.getLastChangeTime();
		String diagnostics = diagnostics(metadata);
		if (contentHash == null && version == null && status == null && lastChange == null && diagnostics.isEmpty()) {
			return null;
		}
		String raw = contentHash + "|" + version + "|" + status + "|" + lastChange + "|" + diagnostics;
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException e) {
			// SHA-256 is a required JCE algorithm; if it is somehow absent, fall back to the raw form.
			return raw;
		}
	}

	/**
	 * The diagnostics' share of the hash (issue #292). A diagnostics write deliberately leaves
	 * {@code lastChangeTime} alone, so without this a client would not notice that the object's
	 * findings had changed. Folds in, per diagnostic and depth first, what a client would show: id,
	 * severity, status, version and message. Empty when the object has no diagnostics, so objects
	 * without any keep the hash they had before.
	 */
	private static String diagnostics(ObjectMetadata metadata) {
		StringBuilder raw = new StringBuilder();
		appendDiagnostics(raw, metadata.getDiagnostics());
		return raw.toString();
	}

	private static void appendDiagnostics(StringBuilder raw, List<Diagnostic> diagnostics) {
		for (Diagnostic diagnostic : diagnostics) {
			raw.append(diagnostic.getId()).append(',').append(diagnostic.getSeverity()).append(',')
					.append(diagnostic.getStatus()).append(',').append(diagnostic.getVersion()).append(',')
					.append(diagnostic.getMessage()).append(';');
			appendDiagnostics(raw, diagnostic.getChildren());
		}
	}
}
