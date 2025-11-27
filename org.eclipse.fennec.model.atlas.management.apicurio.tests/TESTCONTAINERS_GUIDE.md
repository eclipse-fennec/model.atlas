# Testcontainers Integration Testing Guide

This guide explains how to use Testcontainers in OSGi integration tests within the Fennec Model Atlas project.

## Overview

Testcontainers is a Java library that provides lightweight, disposable instances of Docker containers for integration testing. This project uses OSGified testcontainers dependencies from `org.eclipse.daanse.tooling.testcontainers.core` to ensure compatibility with the OSGi runtime.

## Prerequisites

- Docker installed and running on your system
- Java 21+
- Gradle build environment
- OSGi Test Support libraries (OSGi Testing Framework)

## Dependencies

### Test Bundle Configuration

Add the following to your `test.bndrun` file:

```properties
-runbundles: \
    org.eclipse.daanse.tooling.testcontainers.core;version='[0.0.1,0.0.2)',\
    com.sun.jna;version='[5.18.1,5.18.2)',\
    org.apache.commons.commons-compress;version='[1.28.0,1.28.1)',\
    org.apache.commons.commons-io;version='[2.20.0,2.20.1)',\
    org.apache.commons.lang3;version='[3.20.0,3.20.1)',\
    slf4j.api;version='[2.0.11,2.0.12)',\
    ch.qos.logback.classic;version='[1.5.3,1.5.4)',\
    ch.qos.logback.core;version='[1.5.3,1.5.4)'
```

The OSGified testcontainers bundle includes all necessary dependencies for running Docker containers from OSGi tests.

## Basic Testcontainer Setup

### 1. Import Required Classes

```java
import org.testcontainers.containers.GenericContainer;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.config.InjectConfiguration;
```

### 2. Create a Container in Your Test

Use a try-with-resources block to ensure proper container lifecycle management:

```java
@Test
public void testWithContainer() throws Exception {
    try (GenericContainer<?> container = new GenericContainer<>("image:tag")) {
        // Configure container
        container.withEnv("ENV_VAR", "value")
                 .withExposedPorts(8080);

        // Start container
        container.start();

        // Get mapped port (Docker assigns random host ports)
        int mappedPort = container.getMappedPort(8080);

        // Use the container in your test
        String serviceUrl = String.format("http://localhost:%d", mappedPort);

        // Perform test operations...

        // Container automatically stops when leaving try block
    }
}
```

### 3. Configure OSGi Services with Container Details

After starting the container, update your OSGi service configuration with the container's connection details:

```java
@Test
public void testWithOSGiService(
        @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(
            factoryPid = "MyServicePid",
            name = "test",
            location = "?")) Configuration configuration) throws Exception {

    try (GenericContainer<?> container = new GenericContainer<>("image:tag")) {
        container.withExposedPorts(8080);
        container.start();

        int mappedPort = container.getMappedPort(8080);

        // Update OSGi configuration
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("service.url", String.format("http://localhost:%d", mappedPort));
        configuration.update(props);

        // Wait for service to activate
        Thread.sleep(1000);

        // Continue with test...
    }
}
```

## Best Practices

### 1. Container Lifecycle Management

Always use try-with-resources to ensure containers are properly stopped:

```java
try (GenericContainer<?> container = new GenericContainer<>("image:tag")) {
    container.start();
    // Test code
} // Container automatically stopped and removed
```

### 2. Wait for Service Initialization

After updating OSGi configuration, allow time for the service to initialize:

```java
configuration.update(serviceProperties);
Thread.sleep(3000); // Adjust based on service startup time

EObjectStorageService service = serviceAware.waitForService(5000L);
assertNotNull(service, "Service should be available");
```

### 3. Use Specific Image Tags

Avoid `latest` tag in production tests. Use specific versions for reproducibility:

```java
// Good
new GenericContainer<>("apicurio/apicurio-registry:2.4.12.Final")

// Acceptable for development
new GenericContainer<>("apicurio/apicurio-registry:latest-release")

// Avoid in CI/CD
new GenericContainer<>("apicurio/apicurio-registry:latest")
```

### 4. Configure Container Environment

Set environment variables to control container behavior:

```java
container.withEnv("FEATURE_ENABLED", "true")
         .withEnv("LOG_LEVEL", "DEBUG");
```

### 5. Expose Required Ports

Explicitly expose ports that your test needs to access:

```java
container.withExposedPorts(8080, 8081);
int httpPort = container.getMappedPort(8080);
int adminPort = container.getMappedPort(8081);
```

## Advanced Patterns

### Reusable Test Annotations

Create custom annotations to reduce boilerplate in tests:

```java
@WithFactoryConfiguration(
    factoryPid = "MyServicePid",
    name = "shared-config",
    location = "?",
    properties = {
        @Property(key = "workspace.folder", value = "%s/workspace",
                  templateArguments = {
                      @TemplateArgument(source = ValueSource.SystemProperty,
                                       value = "tempDir")
                  })
    })
@Retention(RetentionPolicy.RUNTIME)
public @interface MyServiceConfiguration {}
```

Then use in tests:

```java
@Test
@MyServiceConfiguration
public void testWithAnnotation(...) {
    // Test code
}
```

### Shared Registry Services

For tests requiring a shared registry, use a factory configuration:

```java
@RegistryConfiguration
@Test
public void testWithRegistry(
        @InjectService(cardinality = 0, filter = "(registry.type=shared)")
        ServiceAware<EObjectRegistryService> registryAware) throws Exception {

    EObjectRegistryService registry = registryAware.waitForService(5000L);
    // Use registry in test
}
```

## Common Patterns in Apicurio Tests

The Apicurio tests demonstrate several key patterns:

1. **Container Configuration**: Environment variables and port mappings
2. **OSGi Service Configuration**: Dynamic configuration via ConfigAdmin
3. **Service Availability Checking**: Using `ServiceAware.waitForService()`
4. **Async Operations**: Using OSGi Promises for async results
5. **Cleanup**: Proper resource cleanup in try-with-resources blocks

See `EObjectApicurioStorageServiceTest.java` for complete examples.

## Troubleshooting

### Container Fails to Start

- Ensure Docker is running: `docker ps`
- Check Docker daemon logs
- Verify image is available: `docker pull image:tag`

### Port Conflicts

- Testcontainers uses random host ports automatically
- Always use `container.getMappedPort()` instead of hardcoded ports

### OSGi Service Not Available

- Increase wait timeout: `serviceAware.waitForService(10000L)`
- Check service filter syntax
- Verify all required bundles are in `-runbundles`

### Slow Test Execution

- Container startup can take 5-10 seconds
- Consider reusing containers across tests (advanced pattern)
- Use specific image tags to avoid pulling latest

## References

- [Testcontainers Documentation](https://www.testcontainers.org/)
- [OSGi Testing Support](https://github.com/osgi/osgi-test)
- OSGified Testcontainers: `org.eclipse.daanse.tooling.testcontainers.core`
