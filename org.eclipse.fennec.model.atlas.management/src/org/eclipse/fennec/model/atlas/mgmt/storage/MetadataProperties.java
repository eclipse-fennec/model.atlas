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

import java.io.ByteArrayInputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.util.HexFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;

/**
 * Reads metadata properties that were written before they were text (issue #354).
 * <p>
 * {@code properties} was {@code String -> EJavaObject}, and EMF converts an {@code EJavaObject} to
 * a string by Java-serializing the value and hex-encoding the bytes. Every property in every
 * metadata file written until now is therefore an {@code ACED0005...} blob. The attribute is an
 * {@code EString} now, so loading such a file yields the hex itself - and an {@code nsUri} that
 * reads as hex is an {@code nsUri} the schema registry cannot find a package by. This turns those
 * values back into what they said, once, as the metadata is loaded.
 * <p>
 * <b>What it will decode.</b> A String, a Boolean, or one of the boxed numbers, and nothing else.
 * That is not a convenience: the old form is read with an {@code ObjectInputStream}, and metadata
 * files are exactly the input that may have come from outside the server - the reason the issue
 * calls the old representation a deserialization risk and not only an unreadable one. The filter
 * is what makes reading it once safe, and it rejects by class rather than by what the bytes claim.
 * Anything it refuses is left as the text it already is, which is no worse than not decoding.
 */
public final class MetadataProperties {

	private static final Logger LOGGER = Logger.getLogger(MetadataProperties.class.getName());

	/** The first four bytes of a Java serialization stream, hex-encoded as EMF wrote them. */
	private static final String STREAM_MAGIC = "ACED0005";

	/**
	 * The only classes a legacy value may deserialize to. {@code java.lang.*} alone would still
	 * admit far too much, so these are named one by one.
	 */
	private static final ObjectInputFilter LEGACY_VALUES = ObjectInputFilter.Config.createFilter(
			"java.lang.String;java.lang.Boolean;java.lang.Integer;java.lang.Long;java.lang.Short;"
					+ "java.lang.Byte;java.lang.Double;java.lang.Float;java.lang.Character;!*");

	private MetadataProperties() {
	}

	/**
	 * Replaces every property value still in the old serialized form with what it said.
	 * <p>
	 * In place and on the loaded metadata, so that a caller which goes on to store it writes the
	 * readable form back - the migration happens by being read and written, with no pass over the
	 * storage and no release note telling anybody to run one.
	 *
	 * @param metadata the freshly loaded metadata, or {@code null}
	 * @return the same instance, for chaining
	 */
	public static ObjectMetadata decodeLegacyValues(ObjectMetadata metadata) {
		if (metadata == null || metadata.getProperties() == null) {
			return metadata;
		}
		for (Map.Entry<String, String> entry : metadata.getProperties()) {
			String decoded = decode(entry.getKey(), entry.getValue());
			if (decoded != null) {
				entry.setValue(decoded);
			}
		}
		return metadata;
	}

	/**
	 * What one stored value said, or {@code null} when it is not in the old form or cannot be read
	 * as one.
	 *
	 * @param key   the property key, for the log line when a value is refused
	 * @param value the stored value; may be {@code null}
	 * @return the decoded value, or {@code null} to leave the stored one alone
	 */
	static String decode(String key, String value) {
		if (value == null || value.length() <= STREAM_MAGIC.length() || !value.startsWith(STREAM_MAGIC)) {
			return null;
		}
		byte[] bytes;
		try {
			bytes = HexFormat.of().parseHex(value);
		} catch (IllegalArgumentException notHex) {
			// It began with those eight characters and is not hex at all, so it is text that
			// happens to start that way.
			return null;
		}
		try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
			in.setObjectInputFilter(LEGACY_VALUES);
			return String.valueOf(in.readObject());
		} catch (Exception refused) {
			LOGGER.log(Level.WARNING, refused, () -> String.format(
					"The metadata property '%s' looks like a value stored in the form this Atlas used before "
							+ "properties became text, but it could not be read as one of the types that form was "
							+ "ever used for. It is left as it is.",
					key));
			return null;
		}
	}
}
