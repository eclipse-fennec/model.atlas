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
 *      Mark Hoffmann - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.rest.tests.helper;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jakartars.runtime.JakartarsServiceRuntime;
import org.osgi.service.jakartars.runtime.dto.ApplicationDTO;
import org.osgi.service.jakartars.runtime.dto.ResourceDTO;
import org.osgi.service.jakartars.runtime.dto.RuntimeDTO;
import org.osgi.util.tracker.ServiceTracker;
/**
 * Utility class for monitoring Jakarta REST resource registration and removal in the OSGi runtime.
 * 
 * <p>This class provides a mechanism to track when JAX-RS resources are registered or removed 
 * from the Jakarta REST service runtime, which is essential for integration testing where you need 
 * to ensure resources are available before making REST calls.</p>
 * 
 * <h3>Use Case</h3>
 * <p>In OSGi integration tests, REST resources are registered asynchronously with the Jakarta 
 * REST runtime. Without proper synchronization, test methods may execute before the resource 
 * is fully available, leading to intermittent test failures with HTTP 404 errors.</p>
 * 
 * <h3>Key Features</h3>
 * <ul>
 * <li><strong>Resource Registration Monitoring</strong> - Tracks when a specific resource becomes available</li>
 * <li><strong>Resource Removal Monitoring</strong> - Tracks when a resource is unregistered</li>
 * <li><strong>Timeout Support</strong> - Allows waiting with configurable timeouts</li>
 * <li><strong>Thread-Safe</strong> - Uses CountDownLatch for reliable concurrent synchronization</li>
 * <li><strong>Automatic Cleanup</strong> - ServiceTracker automatically closed after wait operations</li>
 * </ul>
 * 
 * <h3>Implementation Details</h3>
 * <p>The class uses the OSGi {@link JakartarsServiceRuntime} to monitor the runtime DTO and track
 * resource registration events. It examines the {@code defaultApplication} and searches through
 * {@code resourceDTOs} to find resources by name.</p>
 * 
 * <h3>Usage Example</h3>
 * <pre>{@code
 * @Test
 * public void testRestResource() throws Exception {
 *     // Wait for resource to be registered before making REST calls
 *     ResourceAware resourceAware = ResourceAware.create(bundleContext, "governance-documentation");
 *     assertTrue(resourceAware.waitForResource(10, TimeUnit.SECONDS), 
 *                "REST resource should be registered within 10 seconds");
 *     
 *     // Now safe to make REST calls - resource is guaranteed to be available
 *     Response response = restClient.target("http://localhost:8185/rest/governance/documentation/statistics")
 *                                   .request()
 *                                   .get();
 *     assertEquals(200, response.getStatus());
 * }
 * }</pre>
 * 
 * <h3>Testing Lifecycle</h3>
 * <p>This utility is particularly valuable for:</p>
 * <ul>
 * <li><strong>Service Activation Testing</strong> - Verify resources become available after bundle start</li>
 * <li><strong>Service Deactivation Testing</strong> - Verify resources are properly removed on bundle stop</li>
 * <li><strong>Configuration Changes</strong> - Monitor resource lifecycle during configuration updates</li>
 * <li><strong>Integration Test Reliability</strong> - Eliminate race conditions in REST endpoint testing</li>
 * </ul>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 * @see JakartarsServiceRuntime
 * @see ResourceDTO
 * @see ServiceTracker
 */
public class ResourceAware {

  /** Service tracker for monitoring JakartarsServiceRuntime availability and changes */
  private final ServiceTracker<JakartarsServiceRuntime, JakartarsServiceRuntime> runtimeTracker;
  
  /** Latch that counts down when the target resource is registered */
  private final CountDownLatch addLatch;
  
  /** Latch that counts down when the target resource is removed */
  private final CountDownLatch removeLatch;

  /**
   * Factory method to create a ResourceAware instance for monitoring a specific resource.
   * 
   * <p>This is the preferred way to create ResourceAware instances as it validates input
   * parameters and provides a clean API for the caller.</p>
   * 
   * @param ctx the OSGi bundle context for service tracking (must not be null)
   * @param resourceName the name of the JAX-RS resource to monitor (must not be null)
   * @return a new ResourceAware instance configured to monitor the specified resource
   * @throws AssertionError if ctx or resourceName is null
   */
  public static ResourceAware create(BundleContext ctx, String resourceName) {
    assertNotNull(ctx, "BundleContext must not be null");
    assertNotNull(resourceName, "Resource name must not be null");
    return new ResourceAware(ctx, resourceName);
  }

  /**
   * Private constructor that sets up the service tracker and countdown latches.
   * 
   * <p>This constructor initializes the ServiceTracker to monitor JakartarsServiceRuntime
   * and sets up CountDownLatches for synchronization. The service tracker immediately
   * begins monitoring for resource registration/removal events.</p>
   * 
   * @param ctx the OSGi bundle context for service tracking
   * @param resourceName the name of the JAX-RS resource to monitor
   */
  private ResourceAware(BundleContext ctx, String resourceName) {
    addLatch = new CountDownLatch(1);
    removeLatch = new CountDownLatch(1);
    runtimeTracker = new ServiceTracker<JakartarsServiceRuntime, JakartarsServiceRuntime>(ctx, JakartarsServiceRuntime.class, null) {

      /**
       * Called when a JakartarsServiceRuntime service is registered.
       * Checks if our target resource is available in the new runtime.
       */
      @Override
      public JakartarsServiceRuntime addingService(ServiceReference<JakartarsServiceRuntime> reference) {
        JakartarsServiceRuntime runtime = super.addingService(reference);
        checkForResourceRegistration(runtime, false);
        return runtime;
      }

      /**
       * Called when a JakartarsServiceRuntime service is modified.
       * Re-checks if our target resource is available in the updated runtime.
       */
      @Override
      public void modifiedService(ServiceReference<JakartarsServiceRuntime> reference, JakartarsServiceRuntime service) {
        super.modifiedService(reference, service);
        checkForResourceRegistration(service, false);
      }

      /**
       * Called when a JakartarsServiceRuntime service is removed.
       * Checks if our target resource has been removed from the runtime.
       */
      @Override
      public void removedService(ServiceReference<JakartarsServiceRuntime> reference, JakartarsServiceRuntime service) {
        super.removedService(reference, service);
        checkForResourceRegistration(service, true);
      }

      /**
       * Core method that examines the Jakarta REST runtime DTO to determine
       * if our target resource is present or absent.
       * 
       * <p>This method traverses the runtime DTO hierarchy:</p>
       * <ol>
       * <li>Gets the RuntimeDTO from the service</li>
       * <li>Accesses the defaultApplication</li>
       * <li>Iterates through resourceDTOs</li>
       * <li>Compares resource names to find our target</li>
       * </ol>
       * 
       * @param runtime the Jakarta REST service runtime to examine
       * @param removal {@code true} if checking for resource removal,
       *                {@code false} if checking for resource registration
       */
      private void checkForResourceRegistration(JakartarsServiceRuntime runtime, boolean removal) {
        if (runtime != null) {
          RuntimeDTO runtimeDTO = runtime.getRuntimeDTO();
          if (runtimeDTO != null) {
            boolean found = false;
            
            // First check default application
            if (runtimeDTO.defaultApplication != null && runtimeDTO.defaultApplication.resourceDTOs != null) {
              for (ResourceDTO resourceDTO : runtimeDTO.defaultApplication.resourceDTOs) {
                if (resourceName.equals(resourceDTO.name)) {
                  found = true;
                  if (!removal) {
                    addLatch.countDown();
                    return;
                  }
                }
              }
            }
            
            // If not found in default application, check all named applications
            if (!found && runtimeDTO.applicationDTOs != null) {
              for (ApplicationDTO appDTO : runtimeDTO.applicationDTOs) {
                if (appDTO.resourceDTOs != null) {
                  for (ResourceDTO resourceDTO : appDTO.resourceDTOs) {
                    if (resourceName.equals(resourceDTO.name)) {
                      found = true;
                      if (!removal) {
                        addLatch.countDown();
                        return;
                      }
                    }
                  }
                }
              }
            }
            
            // Handle removal case or not found case
            if (removal && !found) {
              removeLatch.countDown();
              return;
            } else if (!removal && found) {
              addLatch.countDown();
              return;
            }
          }
        }
      }
    };

    runtimeTracker.open(true);
  }

  /**
   * Waits for the monitored resource to be registered in the Jakarta REST runtime.
   * 
   * <p>This method blocks until either:</p>
   * <ul>
   * <li>The target resource is found in the runtime DTO (returns {@code true})</li>
   * <li>The specified timeout expires (returns {@code false})</li>
   * <li>The current thread is interrupted (throws {@code InterruptedException})</li>
   * </ul>
   * 
   * <p><strong>Important:</strong> This method automatically closes the ServiceTracker
   * after completion, regardless of the outcome. Each ResourceAware instance is designed
   * for single-use operation.</p>
   * 
   * @param value the maximum time to wait
   * @param unit the time unit of the {@code value} argument
   * @return {@code true} if the resource was registered within the timeout,
   *         {@code false} if the timeout elapsed before resource registration
   * @throws InterruptedException if the current thread is interrupted while waiting
   */
  public boolean waitForResource(long value, TimeUnit unit) throws InterruptedException {
    try {
      return addLatch.await(value, unit);
    } finally {
      runtimeTracker.close();
    }
  }

  /**
   * Waits for the monitored resource to be removed from the Jakarta REST runtime.
   * 
   * <p>This method blocks until either:</p>
   * <ul>
   * <li>The target resource is no longer found in the runtime DTO (returns {@code true})</li>
   * <li>The specified timeout expires (returns {@code false})</li>
   * <li>The current thread is interrupted (throws {@code InterruptedException})</li>
   * </ul>
   * 
   * <p><strong>Important:</strong> This method automatically closes the ServiceTracker
   * after completion, regardless of the outcome. Each ResourceAware instance is designed
   * for single-use operation.</p>
   * 
   * @param value the maximum time to wait
   * @param unit the time unit of the {@code value} argument
   * @return {@code true} if the resource was removed within the timeout,
   *         {@code false} if the timeout elapsed before resource removal
   * @throws InterruptedException if the current thread is interrupted while waiting
   */
  public boolean waitForResourceRemoval(long value, TimeUnit unit) throws InterruptedException {
    try {
      return removeLatch.await(value, unit);
    } finally {
      runtimeTracker.close();
    }
  }
}