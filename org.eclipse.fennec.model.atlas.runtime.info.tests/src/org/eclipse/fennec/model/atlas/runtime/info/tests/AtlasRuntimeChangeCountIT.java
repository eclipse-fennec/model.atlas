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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.runtime.info.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.model.atlas.mediatypes.api.MediatypeContributor;
import org.eclipse.fennec.model.atlas.runtime.info.AtlasRuntime;
import org.eclipse.fennec.model.atlas.runtime.info.AtlasRuntimeDTO;
import org.eclipse.fennec.model.atlas.runtime.info.MediaTypeDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * That a change actually reaches a watcher.
 *
 * <p>
 * The unit tests prove what a snapshot says about a given state. What they cannot prove is the part
 * that only exists in a framework: that the count moves when something the service observes arrives,
 * that the moved count is published onto the service registration, and that the framework delivers
 * it as a property change. That is the whole subscription protocol, and it is three separate things
 * that can each silently not happen.
 * </p>
 *
 * <p>
 * Updates are coalesced onto a daemon thread at most once every 200 ms, so these tests wait for the
 * number rather than reading it straight after the change. A coalescing window may delay an update;
 * it may never drop one, and waiting is how that gets asserted.
 * </p>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class AtlasRuntimeChangeCountIT {

    /** Generous next to the 200 ms coalescing window: this asserts arrival, not latency. */
    private static final long TIMEOUT_MILLIS = 10_000;

    @InjectBundleContext
    BundleContext context;

    private ServiceRegistration<MediatypeContributor> contributor;

    private ServiceRegistration<EPackage> ePackage;

    @AfterEach
    public void unregisterContributor() {
        if (contributor != null) {
            contributor.unregister();
            contributor = null;
        }
        if (ePackage != null) {
            ePackage.unregister();
            ePackage = null;
        }
    }

    private ServiceReference<AtlasRuntime> reference() throws InvalidSyntaxException {
        return context.getServiceReferences(AtlasRuntime.class, null).iterator().next();
    }

    private long publishedCount() throws InvalidSyntaxException {
        Object value = reference().getProperty(Constants.SERVICE_CHANGECOUNT);
        return value instanceof Long count ? count.longValue() : -1L;
    }

    private void awaitCountAbove(long previous) throws Exception {
        long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
        while (System.currentTimeMillis() < deadline) {
            if (publishedCount() > previous) {
                return;
            }
            Thread.sleep(50);
        }
        throw new AssertionError("the change count never moved past " + previous + " within " + TIMEOUT_MILLIS
                + "ms; a coalescing window may delay an update, it may never drop one");
    }

    @Test
    @DisplayName("a service the runtime observes arriving moves the published change count")
    void trackedArrivalMovesTheCount() throws Exception {
        long before = publishedCount();
        assertTrue(before >= 0, "the property must be there before anything changes");

        contributor = register("application/vnd.test+xml");

        awaitCountAbove(before);
    }

    @Test
    @DisplayName("the framework delivers the moved count to a listener as a property change")
    void aWatcherIsToldAboutTheChange() throws Exception {
        CountDownLatch modified = new CountDownLatch(1);
        AtomicLong seen = new AtomicLong(-1);
        ServiceListener listener = (ServiceEvent event) -> {
            if (event.getType() == ServiceEvent.MODIFIED) {
                Object value = event.getServiceReference().getProperty(Constants.SERVICE_CHANGECOUNT);
                if (value instanceof Long count) {
                    seen.set(count.longValue());
                    modified.countDown();
                }
            }
        };
        context.addServiceListener(listener, "(objectClass=" + AtlasRuntime.class.getName() + ")");
        try {
            contributor = register("application/vnd.told+xml");

            assertTrue(modified.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS),
                    "a property change is what a dynamic reference's updated callback is built on; "
                            + "without it a watcher binds and is never told anything");
            assertTrue(seen.get() >= 0);
        } finally {
            context.removeServiceListener(listener);
        }
    }

    @Test
    @DisplayName("the snapshot names the contributor that announced a media type, and forgets it when it goes")
    void contributorAppearsAndDisappears() throws Exception {
        long before = publishedCount();
        contributor = register("application/vnd.origin+xml");
        awaitCountAbove(before);

        AtlasRuntimeDTO withContributor = context.getService(reference()).snapshot();
        MediaTypeDTO entry = withContributor.mediaTypes.stream()
                .filter(type -> "application/vnd.origin+xml".equals(type.mediaType)).findFirst().orElseThrow();
        assertEquals("CONTRIBUTOR", entry.origin, "a declared type is not a codec's");
        assertFalse(entry.universal, "announced for the whole REST layer, produced only where a writer exists");
        assertTrue(entry.serviceId > 0, "the entry names who announced it");

        long beforeRemoval = publishedCount();
        contributor.unregister();
        contributor = null;
        awaitCountAbove(beforeRemoval);

        AtlasRuntimeDTO without = context.getService(reference()).snapshot();
        assertTrue(without.mediaTypes.stream().noneMatch(type -> "application/vnd.origin+xml".equals(type.mediaType)),
                "a withdrawn contributor's types go with it");
    }

    @Test
    @DisplayName("an EPackage is observed through its reference and never asked for")
    void ePackagesAreObservedWithoutBeingDereferenced() throws Exception {
        AtomicInteger dereferenced = new AtomicInteger();
        ServiceFactory<EPackage> factory = new ServiceFactory<>() {

            @Override
            public EPackage getService(Bundle bundle, ServiceRegistration<EPackage> registration) {
                dereferenced.incrementAndGet();
                return EcorePackage.eINSTANCE;
            }

            @Override
            public void ungetService(Bundle bundle, ServiceRegistration<EPackage> registration, EPackage service) {
                // nothing to release: the instance is the singleton Ecore package
            }
        };
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("emf.nsURI", "http://example.org/untouched");
        properties.put("emf.name", "untouched");

        long before = publishedCount();
        ePackage = context.registerService(EPackage.class, factory, properties);

        awaitCountAbove(before);

        // The runtime binds EPackages as ServiceReferences, and everything it reports about one
        // comes off the service properties. Asking for the object would activate the component
        // behind it, which turns a snapshot — taken on every telemetry collection — into something
        // that starts things up.
        assertEquals(0, dereferenced.get(), "a snapshot observes packages, it does not activate them");

        long beforeRemoval = publishedCount();
        ePackage.unregister();
        ePackage = null;
        awaitCountAbove(beforeRemoval);

        assertEquals(0, dereferenced.get(), "nor on the way out");
    }

    private ServiceRegistration<MediatypeContributor> register(String mediaType) {
        MediatypeContributor declared = () -> List.of(mediaType);
        return context.registerService(MediatypeContributor.class, declared, null);
    }
}
