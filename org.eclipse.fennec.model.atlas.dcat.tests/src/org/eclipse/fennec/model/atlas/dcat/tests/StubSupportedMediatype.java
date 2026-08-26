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
package org.eclipse.fennec.model.atlas.dcat.tests;

import java.util.List;

import org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype;
import org.osgi.service.component.annotations.Component;

/**
 * What this test runtime claims it can serve. The publisher intersects its configured allowlist
 * with this, so without it no Distribution is ever advertised — which is the point: a portal must
 * not advertise a format the server would answer 415 for.
 */
@Component(service = SupportedMediatype.class)
public class StubSupportedMediatype implements SupportedMediatype {

    @Override
    public List<String> getSupportedMediaTypes() {
        return List.of("application/xmi", "application/json");
    }
}
