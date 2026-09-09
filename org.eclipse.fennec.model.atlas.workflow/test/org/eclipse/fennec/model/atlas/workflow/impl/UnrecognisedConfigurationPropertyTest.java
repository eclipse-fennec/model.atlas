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
package org.eclipse.fennec.model.atlas.workflow.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A configuration key the component does not know is dropped by Declarative
 * Services without a word, which is why {@code parent.scope} sat in six shipped
 * configurations while the component read {@code scope.parent} (issue #261).
 *
 * <p>
 * A dropped property is indistinguishable from one that was rejected, overridden
 * or never delivered, so both configurable components of this bundle say what they
 * ignored - and, where the key looks like a near miss, what they expected instead.
 * </p>
 *
 * @see <a href="https://github.com/eclipse-fennec/model.atlas/issues/261">issue
 *      #261</a>
 */
@DisplayName("Unrecognised configuration properties are reported")
public class UnrecognisedConfigurationPropertyTest {

    private static final String TEST_NS_URI = "http://example.org/test/1.0.0";

    /**
     * Held strongly and for the life of the class: {@code LogManager} keeps only a
     * weak reference to a logger, so one fetched per test can be collected before
     * the component's own static logger is initialised - and the handler would then
     * sit on an instance nobody logs to.
     */
    private static final Logger SCOPE_LOGGER = Logger.getLogger(ScopeServiceImpl.class.getName());

    private static final Logger REGISTRY_LOGGER = Logger.getLogger(RegistryServiceImpl.class.getName());

    private final List<LogRecord> scopeRecords = new ArrayList<>();
    private final List<LogRecord> registryRecords = new ArrayList<>();

    private Handler scopeHandler;
    private Handler registryHandler;
    private ResourceSet resourceSet;
    private EPackage testPackage;

    @BeforeEach
    void setUp() {
        scopeHandler = capture(SCOPE_LOGGER, scopeRecords);
        registryHandler = capture(REGISTRY_LOGGER, registryRecords);

        testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("test");
        testPackage.setNsPrefix("test");
        testPackage.setNsURI(TEST_NS_URI);
        EClass person = EcoreFactory.eINSTANCE.createEClass();
        person.setName("Person");
        testPackage.getEClassifiers().add(person);

        resourceSet = new ResourceSetImpl();
        Resource resource = new ResourceImpl(URI.createURI(TEST_NS_URI));
        resource.getContents().add(testPackage);
        resourceSet.getResources().add(resource);
    }

    @AfterEach
    void tearDown() {
        SCOPE_LOGGER.removeHandler(scopeHandler);
        REGISTRY_LOGGER.removeHandler(registryHandler);
    }

    @Test
    @DisplayName("the scope service names the key it ignored, and what it expected")
    void scopeServiceReportsTheKeyItIgnored() {
        scopeService().activate(props("scope.name", "jena", "parent.scope", "platform"));

        assertEquals(1, scopeRecords.size(), "exactly one property is unrecognised");
        String message = message(scopeRecords.get(0));
        assertEquals(Level.WARNING, scopeRecords.get(0).getLevel());
        assertTrue(message.contains("'parent.scope'"), () -> "the ignored key must be named: " + message);
        assertTrue(message.contains("'scope.parent'"), () -> "the expected key must be suggested: " + message);
        assertTrue(message.contains("jena"), () -> "the scope must be identifiable: " + message);
    }

    @Test
    @DisplayName("a configuration that only sets known keys stays quiet")
    void aKnownConfigurationIsQuiet() {
        scopeService().activate(props(
                "atlas.scope", "jena",
                "scope.name", "jena",
                "scope.description", "City Jena Scope",
                "scope.parent", "atlas",
                "registryService.target", "(registry.name=schema)"));

        assertEquals(List.of(), messages(scopeRecords), "a correct configuration must not be complained about");
    }

    @Test
    @DisplayName("the properties Declarative Services and the framework own are not the user's typos")
    void frameworkOwnedPropertiesAreNotReported() {
        scopeService().activate(props(
                "scope.name", "jena",
                "component.name", "ScopeService",
                "component.id", 7L,
                "service.pid", "ScopeService~jena",
                "service.factoryPid", "ScopeService",
                "service.ranking", 100,
                "objectClass", new String[] { "java.lang.Object" },
                ".private", "hidden",
                ":configurator:resource-version", 1,
                "registryService.cardinality.minimum", 1,
                "atlasSchemaRegistryService.target", "(registry.name=atlas-schema)"));

        assertEquals(List.of(), messages(scopeRecords), "framework-owned keys are not configuration mistakes");
    }

    @Test
    @DisplayName("the registry service reports its own unrecognised keys")
    void registryServiceReportsTheKeyItIgnored() {
        registryService().activate(props("registry.name", "schema", "delete.after.transitions", true));

        assertEquals(1, registryRecords.size(), "exactly one property is unrecognised");
        String message = message(registryRecords.get(0));
        assertTrue(message.contains("'delete.after.transitions'"),
                () -> "the ignored key must be named: " + message);
        assertTrue(message.contains("'delete.after.transition'"),
                () -> "the expected key must be suggested: " + message);
        assertTrue(message.contains("schema"), () -> "the registry must be identifiable: " + message);
    }

    @Test
    @DisplayName("every key of the shipped jena registry configuration is recognised")
    void theShippedRegistryConfigurationIsQuiet() {
        registryService().activate(props(
                "registry.type", "SCHEMA",
                "registry.name", "schema",
                "registry.description", "The schema registry to store EPackage objects",
                "stageActionService.target", "(component.name=EPackageStageActionService)",
                "stage.storage.mappings", new String[] { "draft:file" },
                "workflow.transitions", new String[] { "draft:approved" },
                "delete.after.transition", true,
                "storageService.target", "(storage.type=file)",
                "storageService.cardinality.minimum", 1,
                "resourceSet.target", "(emf.name=ecore)"));

        assertEquals(List.of(), messages(registryRecords), "the shipped configuration must not warn");
    }

    @Test
    @DisplayName("a key that resembles nothing is still reported, without a guess")
    void anUnrelatedKeyIsReportedWithoutASuggestion() {
        scopeService().activate(props("scope.name", "jena", "colour.of.the.bikeshed", "green"));

        String message = message(scopeRecords.get(0));
        assertTrue(message.contains("'colour.of.the.bikeshed'"), () -> "the ignored key must be named: " + message);
        assertTrue(!message.contains("Did you mean"), () -> "no plausible key to suggest: " + message);
    }

    private ScopeServiceImpl<EObject> scopeService() {
        ScopeServiceConfig config = mock(ScopeServiceConfig.class);
        when(config.scope_name()).thenReturn("jena");
        ScopeServiceImpl<EObject> scopeService = new ScopeServiceImpl<>(config);
        scopeRecords.clear();
        return scopeService;
    }

    private RegistryServiceImpl<EObject> registryService() {
        RegistryServiceConfig config = mock(RegistryServiceConfig.class);
        when(config.registry_name()).thenReturn("schema");
        when(config.registry_description()).thenReturn("");
        when(config.registry_type()).thenReturn("SCHEMA");
        when(config.workflow_transitions()).thenReturn(new String[] { "draft:release" });
        when(config.stage_storage_mappings()).thenReturn(new String[] { "draft:file", "release:file" });
        when(config.stages()).thenReturn(new String[] {
                "{\"name\": \"draft\", \"writable\": true, \"final\": false}",
                "{\"name\": \"release\", \"writable\": true, \"final\": true}" });
        when(config.root_eclass_uri()).thenReturn(new String[] { TEST_NS_URI + "#//Person" });
        when(config.derived_eclass_uri()).thenReturn(new String[0]);
        RegistryServiceImpl<EObject> registryService = new RegistryServiceImpl<>(List.of(), resourceSet,
                testPackage, config);
        // What construction complains about (no storage service is injected here) is not
        // what this test is about: only activation reports the configuration's own keys.
        registryRecords.clear();
        return registryService;
    }

    /**
     * The delivered component properties, which carry values of every type a
     * configuration may hold - {@code Map.of} would infer something narrower than
     * {@code Object} for a mixed list, and stops at ten pairs.
     */
    private static Map<String, Object> props(Object... keysAndValues) {
        Map<String, Object> properties = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            properties.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return properties;
    }

    private static Handler capture(Logger logger, List<LogRecord> into) {
        Handler handler = new Handler() {

            @Override
            public void publish(LogRecord record) {
                if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                    into.add(record);
                }
            }

            @Override
            public void flush() {
                // nothing buffered
            }

            @Override
            public void close() {
                // nothing to release
            }
        };
        logger.addHandler(handler);
        return handler;
    }

    private static String message(LogRecord record) {
        return record.getMessage() != null ? record.getMessage() : String.valueOf(record.getParameters());
    }

    private static List<String> messages(List<LogRecord> records) {
        return records.stream().map(UnrecognisedConfigurationPropertyTest::message).toList();
    }
}
