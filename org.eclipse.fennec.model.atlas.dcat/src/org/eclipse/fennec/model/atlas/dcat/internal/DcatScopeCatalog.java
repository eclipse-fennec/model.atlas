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

import java.util.logging.Logger;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.Designate;

/**
 * One scope's Catalog configuration, published as a service so every portal publisher sees it.
 *
 * <p>
 * Deliberately not per portal. A Catalog's ownership is a fact about the catalogue, not about which
 * of this atlas's publishers happens to be talking to it: if a scope's Catalog is somebody else's,
 * it is somebody else's for every publisher, and having to repeat that per portal is how one of
 * them ends up writing it.
 * </p>
 *
 * <p>
 * An unusable configuration still activates, and says so in {@link #settings()}. Refusing to
 * activate would remove the service, and the absence of a service is how the derived case is
 * expressed — so a broken {@code catalog.adopt} would silently become "write a Catalog under the
 * scope name", which is the opposite of what it asked for.
 * </p>
 */
@Component(name = DcatScopeCatalog.PID, //
        service = DcatScopeCatalog.class, //
        configurationPid = DcatScopeCatalog.PID, //
        configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = ScopeCatalogConfig.class, factory = true)
public class DcatScopeCatalog {

    /** ConfigAdmin factory PID. */
    static final String PID = "DcatScopeCatalog";

    private static final Logger LOGGER = Logger.getLogger(DcatScopeCatalog.class.getName());

    private volatile String scope;
    private volatile CatalogSettings settings;

    @Activate
    void activate(ScopeCatalogConfig config) {
        this.scope = config.scope();
        this.settings = CatalogSettings.of(config);
        if (scope == null || scope.isBlank()) {
            // Nothing to attach the configuration to; the publishers skip it by name.
            LOGGER.warning("A DcatScopeCatalog configuration has no `scope`, so it applies to nothing");
            return;
        }
        if (!settings.valid()) {
            LOGGER.warning(() -> "DcatScopeCatalog for scope " + scope + " is unusable and will refuse the scope: "
                    + settings.invalidReason());
        } else if (settings.adopt()) {
            LOGGER.info(() -> "Scope " + scope + " adopts Catalog " + settings.id()
                    + ": Datasets are linked into it, and it is never written from here");
        }
    }

    /** The scope this configuration is about. */
    String scope() {
        return scope;
    }

    /** What it says about that scope's Catalog. */
    CatalogSettings settings() {
        return settings;
    }
}
