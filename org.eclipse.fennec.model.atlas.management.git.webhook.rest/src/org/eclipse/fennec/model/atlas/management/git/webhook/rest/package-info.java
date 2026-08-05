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
/**
 * Provider-neutral git webhook ingest for the git storage backend.
 *
 * <p>Two Jakarta RS resources ({@code /github} and {@code /gitlab}) parse the
 * host-specific push payload into its concrete
 * {@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload}
 * subtype via the Fennec codec, then deliver the <em>neutral</em>
 * {@code WebhookPayload} onto the {@code TypedEventBus} keyed by a
 * repository+branch topic. Everything downstream (the git storage service, the
 * reconcile poll) consumes only the neutral type, so it never learns which host
 * a change came from.
 */
@org.osgi.annotation.bundle.Export
@org.eclipse.fennec.codec.rest.annotations.RequireCodecMessageBodyReaderWriter
@org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard
package org.eclipse.fennec.model.atlas.management.git.webhook.rest;
