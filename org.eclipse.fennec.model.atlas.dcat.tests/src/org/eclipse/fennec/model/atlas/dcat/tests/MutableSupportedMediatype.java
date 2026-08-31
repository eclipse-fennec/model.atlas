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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

/**
 * A {@link SupportedMediatype} whose list a test can change, refreshing its service properties the
 * way the real implementation does when a codec registers a content type.
 *
 * <p>
 * Registered at a ranking above the fixed stub, so it takes over the publisher's greedy reference
 * for the duration of one test and hands it back on unregister.
 * </p>
 */
final class MutableSupportedMediatype implements SupportedMediatype, AutoCloseable {

    private final List<String> mediaTypes = new CopyOnWriteArrayList<>();
    private final ServiceRegistration<SupportedMediatype> registration;

    private MutableSupportedMediatype(BundleContext context, List<String> initial) {
        mediaTypes.addAll(initial);
        Hashtable<String, Object> properties = properties();
        // Above StubSupportedMediatype, whose list is fixed; the publisher's reference is greedy, so
        // this becomes the one it uses.
        properties.put(Constants.SERVICE_RANKING, Integer.valueOf(100));
        registration = context.registerService(SupportedMediatype.class, this, properties);
    }

    static MutableSupportedMediatype register(BundleContext context, List<String> initial) {
        return new MutableSupportedMediatype(context, initial);
    }

    /**
     * Reports a new list and refreshes the service properties, which is the only notification a
     * consumer gets — the service itself never rebinds.
     */
    void report(List<String> types) {
        mediaTypes.clear();
        mediaTypes.addAll(types);
        Hashtable<String, Object> properties = properties();
        properties.put(Constants.SERVICE_RANKING, Integer.valueOf(100));
        registration.setProperties(properties);
    }

    @Override
    public List<String> getSupportedMediaTypes() {
        return List.copyOf(mediaTypes);
    }

    private Hashtable<String, Object> properties() {
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put(SupportedMediatype.MEDIATYPES_PROPERTY, mediaTypes.toArray(String[]::new));
        return properties;
    }

    @Override
    public void close() {
        registration.unregister();
    }
}
