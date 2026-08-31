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
package org.eclipse.fennec.model.atlas.dcat.internal;

/**
 * What retirement does to what was published.
 *
 * <p>
 * An operator's choice rather than ours, because the right answer depends on what the catalogue
 * is for: a discovery portal wants a retired model gone, an archive wants it kept. The default is
 * the reversible one.
 * </p>
 *
 * <p>
 * Public because {@code DcatPublisherConfig} is realized by a JDK dynamic proxy, which cannot read
 * a package-private attribute type — the symptom is an {@code IllegalAccessError} at activation,
 * not a compile error. The package is private to the bundle, so this is not exported API.
 * </p>
 */
public enum UnpublishMode {

    /**
     * Leave everything; the portal keeps advertising it. Defensible for an archive, and the only
     * mode under which this publisher never removes anything.
     */
    NONE,

    /**
     * Remove the membership links and keep the resource: it stops being discoverable through the
     * catalogue without anything being deleted. The default, because it is the only mode that
     * cannot destroy what a portal-side editor added.
     */
    UNLINK,

    /**
     * Drop our memberships, then delete the resource with
     * {@code DeleteMode#SINGLE} — which refuses while any <em>other</em> referrer remains. That
     * refusal is the point: our links are ours to drop, a foreign one is not.
     */
    DELETE,

    /**
     * Delete with {@code DeleteMode#CASCADE}: the portal unlinks every referrer first and reports
     * what it rewrote. Removes links this publisher did not create.
     */
    CASCADE
}
