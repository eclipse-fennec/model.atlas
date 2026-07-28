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
package org.eclipse.fennec.model.atlas.management.git.webhook.rest;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.ws.rs.NameBinding;

/**
 * Name-binding that scopes the {@link GithubWebhookSignatureFilter} to the
 * GitHub webhook resource method only.
 *
 * <p>The filter buffers and validates the raw request body (GitHub HMAC) and
 * must therefore never run against unrelated resources in the shared Jakarta
 * RS whiteboard. Binding it by name keeps it confined to the {@code /github}
 * POST endpoint.
 *
 * @author Data In Motion
 * @since 1.0
 */
@NameBinding
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
public @interface VerifyGithubWebhookSignature {
}
