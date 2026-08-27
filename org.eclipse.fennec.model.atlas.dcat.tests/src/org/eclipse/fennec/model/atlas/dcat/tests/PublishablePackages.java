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

import java.util.Hashtable;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Registers {@link EPackage} services carrying exactly the properties
 * {@code DynamicEPackageRegistrationService} publishes, so the publisher's tracker filter is
 * exercised for real.
 *
 * <p>
 * Standing in for the registration service rather than driving it keeps this IT about the
 * publisher: that the flag reaches those properties in the first place is O13, and is covered by
 * unit tests that capture the actual {@code registerService} call.
 * </p>
 */
final class PublishablePackages {

    /** The stage in {@link StubScopeService}'s registry that is marked final. */
    static final String FINAL_STAGE = "release";

    private PublishablePackages() {
    }

    /**
     * @param context     the test bundle's context
     * @param nsUri       the namespace URI
     * @param stage       the stage to claim
     * @param fingerprint an {@code fp1:} fingerprint, or {@code null}
     * @param dcat        the publication flag, as the registration service would project it
     * @return the registration, to be unregistered by the caller
     */
    static ServiceRegistration<?> register(BundleContext context, String nsUri, String stage, String fingerprint,
            boolean dcat) {
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setName("Person");
        ePackage.setNsURI(nsUri);
        ePackage.setNsPrefix("person");

        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put("emf.nsURI", nsUri);
        properties.put("emf.name", ePackage.getName());
        properties.put("emf.model.scope", StubScopeService.SCOPE);
        properties.put("atlas.stage", stage);
        properties.put("emf.version", "1.1.0");
        if (fingerprint != null) {
            properties.put("emf.fingerprint", fingerprint);
        }
        properties.put("dynamic.registration", Boolean.TRUE);
        properties.put("dcat", Boolean.valueOf(dcat));
        return context.registerService(EPackage.class, ePackage, properties);
    }
}
