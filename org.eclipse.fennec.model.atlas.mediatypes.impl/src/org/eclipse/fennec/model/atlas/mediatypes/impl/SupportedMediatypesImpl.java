/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
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
package org.eclipse.fennec.model.atlas.mediatypes.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype;
import org.osgi.annotation.bundle.Capability;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * <p>
 * Registers its own service rather than letting DS do it, so that it can refresh the service
 * properties when the list changes. The list is derived from the bound {@code ResourceSet} and grows
 * as codecs register content types, and a component has no way to modify service properties DS
 * registered on its behalf — only the holder of the {@code ServiceRegistration} can. Without this, a
 * consumer had no way to learn that the list had changed: the service never rebinds, so there was no
 * event to react to, and anything that read the list once was stale for as long as it ran.
 * </p>
 */
// The service is registered manually below, so DS does not generate its osgi.service capability.
// Declare it explicitly, or resolving against this bundle stops satisfying consumers' service
// requirements — which the production runtime has, since it resolves with `-resolve.effective:
// active`.
@Capability(namespace = "osgi.service", attribute = {
        "objectClass:List<String>=\"org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype\"",
        "uses:=org.eclipse.fennec.model.atlas.mediatypes.api" })
@Component(immediate = true, service = {})
public class SupportedMediatypesImpl implements SupportedMediatype {

    private List<String> mediaTypes = new CopyOnWriteArrayList<>();

    private volatile ServiceRegistration<SupportedMediatype> registration;

    @Reference(updated = "bindResourceSet", policyOption = ReferencePolicyOption.GREEDY)
    void bindResourceSet(ResourceSet set) {
        synchronized (mediaTypes) {

            mediaTypes.clear();
            set.getResourceFactoryRegistry().getContentTypeToFactoryMap().keySet().stream()
                    .filter(s -> s.startsWith("application/") || s.startsWith("text/")).forEach(mediaTypes::add);
            mediaTypes.add("application/xmi");
            mediaTypes.add("application/uml");
            mediaTypes.add("application/schema+xml");
        }
        // Outside the block, and only once registered: this fires a synchronous MODIFIED event, so
        // consumers run their updated methods on this thread and must see the finished list. The
        // first call arrives before activate, where the registration is made with the same
        // properties.
        publishProperties();
    }

    @Activate
    void activate(BundleContext context) {
        registration = context.registerService(SupportedMediatype.class, this, properties());
    }

    @Deactivate
    void deactivate() {
        ServiceRegistration<SupportedMediatype> current = registration;
        registration = null;
        if (current != null) {
            current.unregister();
        }
    }

    private void publishProperties() {
        ServiceRegistration<SupportedMediatype> current = registration;
        if (current != null) {
            current.setProperties(properties());
        }
    }

    private Hashtable<String, Object> properties() {
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put(SupportedMediatype.MEDIATYPES_PROPERTY, mediaTypes.toArray(String[]::new));
        return properties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype#
     * getSupportedMediaTypes()
     */
    @Override
    public List<String> getSupportedMediaTypes() {
        return new ArrayList<>(mediaTypes);
    }

}
