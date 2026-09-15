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
package org.eclipse.fennec.model.atlas.publisher;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Hands a registered {@code EPackage} — a metamodel, not an instance — to a
 * model.atlas schema stage.
 * <p>
 * The caller names a namespace URI and gets back a receipt. It never handles the
 * XMI, never learns the server address, and cannot choose the scope, the stage or
 * whether an existing entry is replaced: those are resolved when the service is
 * configured. A model inferred at runtime can therefore leave the process without
 * its serialization passing through whoever asked for the publication.
 * <p>
 * Which namespaces may be published at all is a configured allow-list, so a
 * caller holding this service still cannot publish an arbitrary package.
 * <p>
 * <b>Deploying this bundle and configuring a publisher is the authorization
 * decision.</b> A runtime with no {@code ModelAtlasPublisher} configuration
 * cannot publish a schema from code at all.
 *
 * @author ilenia
 * @since Aug 27, 2026
 */
@ProviderType
public interface PackagePublisher {

	/**
	 * The outcome of one publication, as the caller sees it. Carries no upstream
	 * body and no server address.
	 *
	 * @param outcome         {@code created} or {@code updated}
	 * @param nsURI           the published namespace URI
	 * @param packageName     the EPackage's name
	 * @param scope           the scope it went to
	 * @param stage           the stage it went to
	 * @param classifierCount how many classifiers the published package holds
	 * @param byteSize        the size of the serialized document
	 */
	record Receipt(
			String outcome,
			String nsURI,
			String packageName,
			String scope,
			String stage,
			int classifierCount,
			int byteSize) {
	}

	/**
	 * Publishes the registered package carrying the given namespace URI.
	 *
	 * @param nsURI the namespace URI of a registered package, which must be allow-listed
	 * @return the receipt of a successful publication
	 * @throws PublishException with a caller-facing message for every failure
	 */
	Receipt publish(String nsURI);
}
