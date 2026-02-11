/**
 * Copyright (c) 2012 - 2025 Kentyou and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Kentyou - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.healthcheck;

import java.util.List;

import org.apache.felix.hc.api.FormattingResultLog;
import org.apache.felix.hc.api.HealthCheck;
import org.apache.felix.hc.api.Result;
import org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Health check that verifies media type codecs are available.
 *
 * @since 1.0
 */
@Component(service = HealthCheck.class, property = {
	HealthCheck.NAME + "=Media Types",
	HealthCheck.TAGS + "=atlas,readiness"
})
public class MediaTypesHealthCheck implements HealthCheck {

	@Reference
	private SupportedMediatype supportedMediatype;

	@Override
	public Result execute() {
		FormattingResultLog log = new FormattingResultLog();

		if (supportedMediatype == null) {
			log.critical("SupportedMediatype service not available");
		} else {
			List<String> types = supportedMediatype.getSupportedMediaTypes();
			if (types != null && !types.isEmpty()) {
				log.info("Supporting {} media types", types.size());
			} else {
				log.warn("No media types available");
			}
		}

		return new Result(log);
	}
}
