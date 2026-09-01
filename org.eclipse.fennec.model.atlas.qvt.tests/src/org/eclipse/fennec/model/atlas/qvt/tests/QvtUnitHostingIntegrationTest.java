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
package org.eclipse.fennec.model.atlas.qvt.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.m2x.model.compiled.CompiledFactory;
import org.eclipse.fennec.m2x.model.compiled.CompiledPackage;
import org.eclipse.fennec.m2x.model.compiled.CompiledUnit;
import org.eclipse.fennec.m2x.model.compiled.SourceUnit;
import org.eclipse.fennec.m2x.qvto.api.BasicQvtoModelExtent;
import org.eclipse.fennec.m2x.qvto.api.QvtoConfiguration;
import org.eclipse.fennec.m2x.qvto.api.QvtoEngine;
import org.eclipse.fennec.m2x.qvto.api.QvtoExecutionContext;
import org.eclipse.fennec.m2x.qvto.api.QvtoUnitResolver;
import org.eclipse.fennec.m2x.qvto.engine.QvtoEngines;
import org.eclipse.fennec.m2x.unit.api.PreparedContext;
import org.eclipse.fennec.m2x.unit.api.Unit;
import org.eclipse.fennec.m2x.unit.api.UnitKey;
import org.eclipse.fennec.m2x.unit.api.UnitKind;
import org.eclipse.fennec.m2x.unit.prepare.UnitPreparer;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.qvt.AtlasUnitStore;
import org.eclipse.fennec.model.atlas.qvt.QvtUnits;
import org.eclipse.fennec.model.atlas.qvt.diagnostics.CompileStatus;
import org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.Scalar;
import org.osgi.test.common.annotation.Property.TemplateArgument;
import org.osgi.test.common.annotation.Property.Type;
import org.osgi.test.common.annotation.Property.ValueSource;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Integration tests for the QVT unit hosting (issue #239): compile-on-upload
 * behaviour, diagnostics, library dependencies with the transitive recompile
 * cascade, and the round trip from the Atlas store to a prepared execution on
 * a plain consumer engine.
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(QvtUnitHostingIntegrationTest.TempDirPropertyExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@WithFactoryConfiguration(factoryPid = "LuceneEObjectRegistryService", name = "shared-registry", location = "?", properties = {
        @Property(key = "registry.workspace.folder", value = "%s/shared-registry", templateArguments = {
                @TemplateArgument(source = ValueSource.SystemProperty, value = CommonTestAnnotations.PROP_TEMP_DIR) }),
        @Property(key = "registry", value = "main") })
@WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "file-storage", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/file-storage", templateArguments = {
                @TemplateArgument(source = ValueSource.SystemProperty, value = CommonTestAnnotations.PROP_TEMP_DIR) }),
        @Property(key = "storage.type", value = "file"),
        @Property(key = "registry.target", value = "(registry=main)") })
@WithFactoryConfiguration(factoryPid = "QvtStageActionService", name = "qvt-action", location = "?", properties = {
        @Property(key = "trigger.stages", scalar = Scalar.String, type = Type.Array, value = { "draft", "release" }) })
@WithFactoryConfiguration(factoryPid = "RegistryService", name = QvtUnitHostingIntegrationTest.REGISTRY, location = "?", properties = {
        @Property(key = "registry.name", value = QvtUnitHostingIntegrationTest.REGISTRY),
        @Property(key = "registry.type", value = "TRANSFORMATION"),
        @Property(key = "root.eclass.uri", scalar = Scalar.String, type = Type.Array, value = {
                "http://www.eclipse.org/fennec/m2x/compiled/1.0#//CompiledUnit",
                "http://www.eclipse.org/fennec/m2x/compiled/1.0#//SourceUnit",
                "http://eclipse.org/fennec/model/atlas/qvt/diagnostics/1.0.0#//SourceDiagnostics" }),
        @Property(key = "resourceSet.target", value = "(&(emf.name=compiled)(emf.name=diagnostics))"),
        @Property(key = "storageService.target", value = "(storage.type=file)"),
        @Property(key = "stageActionService.target", value = "(component.name=QvtStageActionService)"),
        @Property(key = "stageActionService.cardinality.minimum", scalar = Scalar.Integer, value = "1"),
        @Property(key = "stages", type = Type.Array, value = {
                "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}" }),
        @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
        @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:file", "release:file" }) })
@DisplayName("QVT unit hosting integration tests")
public class QvtUnitHostingIntegrationTest {

    static final String REGISTRY = "transformations";
    static final String SCOPE = "qvt-test-scope";
    static final String DRAFT = "draft";

    private static final String SOURCE_UNIT_TYPE = EcoreUtil.getURI(CompiledPackage.Literals.SOURCE_UNIT).toString();

    public static class TempDirPropertyExtension implements BeforeAllCallback, AfterAllCallback {

        @Override
        public void beforeAll(ExtensionContext context) throws Exception {
            System.setProperty(CommonTestAnnotations.PROP_TEMP_DIR,
                    Files.createTempDirectory("qvt-unit-hosting-test-").toString());
        }

        @Override
        public void afterAll(ExtensionContext context) {
            System.clearProperty(CommonTestAnnotations.PROP_TEMP_DIR);
        }
    }

    // --- fixtures -------------------------------------------------------

    private static final String RENAME = """
            modeltype ECORE uses ecore('http://www.eclipse.org/emf/2002/Ecore');
            transformation Rename(inout m : ECORE) {
                main() {
                    m.objectsOfType(EPackage)->forEach(p) {
                        p.name := p.name.toUpperCase();
                    };
                }
            }
            """;

    private static final String CASE_LIB = """
            library text.Case {
                helper shout(s : String) : String {
                    return s.toUpperCase() + '!';
                }
            }
            """;

    private static final String CASE_LIB_CHANGED = """
            library text.Case {
                helper shout(s : String) : String {
                    return s.toUpperCase() + '!!';
                }
            }
            """;

    private static final String ANNOUNCE = """
            modeltype ECORE uses ecore('http://www.eclipse.org/emf/2002/Ecore');
            import text.Case;
            transformation Announce(inout m : ECORE) {
                main() {
                    m.objectsOfType(EPackage)->forEach(p) {
                        p.name := shout(p.name);
                    };
                }
            }
            """;

    private static final String BROKEN = """
            modeltype ECORE uses ecore('http://www.eclipse.org/emf/2002/Ecore');
            transformation Broken(inout m : ECORE) {
                main() {
                    this is no qvt at all
            """;

    // --- helpers --------------------------------------------------------

    private static ObjectMetadata uploadSource(RegistryService<EObject> registry, String stage, String objectId,
            String qualifiedName, String source) throws Exception {
        SourceUnit document = CompiledFactory.eINSTANCE.createSourceUnit();
        document.setLanguage(QvtUnits.LANGUAGE_QVTO);
        document.setQualifiedName(qualifiedName);
        document.setUri("atlas:/" + qualifiedName + ".qvto");
        document.setSource(source);
        ObjectMetadata existing = registry.getMetadataFromStage(SCOPE, stage, objectId);
        if (existing != null) {
            return registry.updateInStage(SCOPE, stage, document, objectId, existing.getVersion()).getValue();
        }
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(objectId);
        metadata.setObjectName(qualifiedName);
        metadata.setObjectType(SOURCE_UNIT_TYPE);
        metadata.setUploadTime(Instant.now());
        return registry.uploadToStage(SCOPE, stage, document, metadata).getValue();
    }

    private static SourceDiagnostics diagnosticsOf(RegistryService<EObject> registry, String stage,
            String qualifiedName) {
        EObject content = registry.getContentFromStage(SCOPE, stage,
                QvtUnits.diagnosticsObjectId(QvtUnits.LANGUAGE_QVTO, qualifiedName));
        assertNotNull(content, "expected a diagnostics document for " + qualifiedName);
        assertTrue(content instanceof SourceDiagnostics, "expected SourceDiagnostics, got " + content.eClass());
        return (SourceDiagnostics) content;
    }

    private static RegistryService<EObject> registry(ServiceAware<RegistryService> aware) throws Exception {
        @SuppressWarnings("unchecked")
        RegistryService<EObject> registry = aware.waitForService(15000);
        assertNotNull(registry, "the transformations RegistryService never appeared");
        return registry;
    }

    // --- tests ----------------------------------------------------------

    @Test
    @DisplayName("An invalid source stays stored as draft together with its diagnostics")
    void invalidSourceKeptWithDiagnostics(
            @InjectService(cardinality = 0, timeout = 15000, filter = "(registry.name=" + REGISTRY + ")") //
            ServiceAware<RegistryService> aware) throws Exception {
        RegistryService<EObject> registry = registry(aware);
        uploadSource(registry, DRAFT, "Broken", "Broken", BROKEN);

        EObject stored = registry.getContentFromStage(SCOPE, DRAFT, "Broken");
        assertTrue(stored instanceof SourceUnit, "the invalid source must stay stored");
        assertEquals(BROKEN, ((SourceUnit) stored).getSource());

        SourceDiagnostics diagnostics = diagnosticsOf(registry, DRAFT, "Broken");
        assertEquals(CompileStatus.INVALID, diagnostics.getCompileStatus());
        assertFalse(diagnostics.getEntries().isEmpty(), "the findings must be recorded");
        assertTrue(diagnostics.getEntries().stream().anyMatch(entry -> entry.getLine() > 0),
                "at least one finding carries a real position");

        AtlasUnitStore store = new AtlasUnitStore(registry, SCOPE, DRAFT);
        assertTrue(store.versions(QvtUnits.LANGUAGE_QVTO, "Broken", UnitKind.COMPILED).isEmpty(),
                "no unit is produced for an invalid source");
    }

    @Test
    @DisplayName("A startable root transformation compiles to a draft-stage unit")
    void startableRootCompilesIntoDraft(
            @InjectService(cardinality = 0, timeout = 15000, filter = "(registry.name=" + REGISTRY + ")") //
            ServiceAware<RegistryService> aware) throws Exception {
        RegistryService<EObject> registry = registry(aware);
        uploadSource(registry, DRAFT, "Rename", "Rename", RENAME);

        AtlasUnitStore store = new AtlasUnitStore(registry, SCOPE, DRAFT);
        List<UnitKey> versions = store.versions(QvtUnits.LANGUAGE_QVTO, "Rename", UnitKind.COMPILED);
        assertEquals(1, versions.size(), "exactly one compiled unit for one upload");

        Optional<Unit> unit = store.get(versions.get(0));
        assertTrue(unit.isPresent());
        CompiledUnit document = ((Unit.Packaged) unit.get()).document();
        assertEquals("Rename", document.getManifest().getQualifiedName());
        assertEquals(QvtUnits.LANGUAGE_QVTO, document.getManifest().getLanguage());
        assertNotNull(document.getManifest().getUnitFingerprint());

        SourceDiagnostics diagnostics = diagnosticsOf(registry, DRAFT, "Rename");
        assertEquals(CompileStatus.OK, diagnostics.getCompileStatus());
        assertEquals(versions.get(0).fingerprint().orElseThrow(), diagnostics.getUnitFingerprint(),
                "the diagnostics name the unit a consumer pins");
    }

    @Test
    @DisplayName("A library is stored as a dependency and marked LIBRARY")
    void libraryStoredAsDependency(
            @InjectService(cardinality = 0, timeout = 15000, filter = "(registry.name=" + REGISTRY + ")") //
            ServiceAware<RegistryService> aware) throws Exception {
        RegistryService<EObject> registry = registry(aware);
        uploadSource(registry, DRAFT, "text.Case", "text.Case", CASE_LIB);

        SourceDiagnostics diagnostics = diagnosticsOf(registry, DRAFT, "text.Case");
        assertEquals(CompileStatus.LIBRARY, diagnostics.getCompileStatus());

        AtlasUnitStore store = new AtlasUnitStore(registry, SCOPE, DRAFT);
        assertFalse(store.versions(QvtUnits.LANGUAGE_QVTO, "text.Case", UnitKind.COMPILED).isEmpty(),
                "the library's compiled form is stored for consumers' prepare");
        assertFalse(store.versions(QvtUnits.LANGUAGE_QVTO, "text.Case", UnitKind.SOURCE).isEmpty(),
                "the working-copy source is visible through the store");
    }

    @Test
    @DisplayName("A changed library recompiles its dependents; pinned versions stay resolvable")
    void changedLibraryRecompilesDependents(
            @InjectService(cardinality = 0, timeout = 15000, filter = "(registry.name=" + REGISTRY + ")") //
            ServiceAware<RegistryService> aware) throws Exception {
        RegistryService<EObject> registry = registry(aware);
        uploadSource(registry, DRAFT, "text.Case", "text.Case", CASE_LIB);
        uploadSource(registry, DRAFT, "Announce", "Announce", ANNOUNCE);

        AtlasUnitStore store = new AtlasUnitStore(registry, SCOPE, DRAFT);
        List<UnitKey> before = store.versions(QvtUnits.LANGUAGE_QVTO, "Announce", UnitKind.COMPILED);
        assertEquals(1, before.size());
        String pinnedBefore = before.get(0).fingerprint().orElseThrow();

        uploadSource(registry, DRAFT, "text.Case", "text.Case", CASE_LIB_CHANGED);

        List<UnitKey> after = store.versions(QvtUnits.LANGUAGE_QVTO, "Announce", UnitKind.COMPILED);
        assertEquals(2, after.size(), "the dependent recompiled against the changed library");
        String pinnedAfter = after.get(0).fingerprint().orElseThrow();
        assertFalse(pinnedAfter.equals(pinnedBefore),
                "under pin the unit fingerprint folds in the dependency's fingerprint");
        assertTrue(store.get(UnitKey.pinned(QvtUnits.LANGUAGE_QVTO, "Announce", UnitKind.COMPILED, pinnedBefore))
                .isPresent(), "the previously pinned version stays resolvable");
    }

    @Test
    @DisplayName("A library change cascades transitively to the fixpoint (A <- B <- C)")
    void cascadeReachesTransitiveDependents(
            @InjectService(cardinality = 0, timeout = 15000, filter = "(registry.name=" + REGISTRY + ")") //
            ServiceAware<RegistryService> aware) throws Exception {
        RegistryService<EObject> registry = registry(aware);
        String libA = """
                library chain.A {
                    helper base(s : String) : String {
                        return s + '-a';
                    }
                }
                """;
        String libAChanged = """
                library chain.A {
                    helper base(s : String) : String {
                        return s + '-A';
                    }
                }
                """;
        String libB = """
                import chain.A;
                library chain.B {
                    helper wrap(s : String) : String {
                        return base(s) + '-b';
                    }
                }
                """;
        String trafoC = """
                modeltype ECORE uses ecore('http://www.eclipse.org/emf/2002/Ecore');
                import chain.B;
                transformation chain.C(inout m : ECORE) {
                    main() {
                        m.objectsOfType(EPackage)->forEach(p) {
                            p.name := wrap(p.name);
                        };
                    }
                }
                """;
        uploadSource(registry, DRAFT, "chain.A", "chain.A", libA);
        uploadSource(registry, DRAFT, "chain.B", "chain.B", libB);
        uploadSource(registry, DRAFT, "chain.C", "chain.C", trafoC);

        AtlasUnitStore store = new AtlasUnitStore(registry, SCOPE, DRAFT);
        assertEquals(1, store.versions(QvtUnits.LANGUAGE_QVTO, "chain.C", UnitKind.COMPILED).size());

        uploadSource(registry, DRAFT, "chain.A", "chain.A", libAChanged);

        assertEquals(2, store.versions(QvtUnits.LANGUAGE_QVTO, "chain.B", UnitKind.COMPILED).size(),
                "the direct dependent recompiled");
        assertEquals(2, store.versions(QvtUnits.LANGUAGE_QVTO, "chain.C", UnitKind.COMPILED).size(),
                "the transitive dependent recompiled too");
    }

    @Test
    @DisplayName("Round trip: fetched by name + fingerprint, prepared and executed on a plain consumer engine")
    void roundTripPrepareExecute(
            @InjectService(cardinality = 0, timeout = 15000, filter = "(registry.name=" + REGISTRY + ")") //
            ServiceAware<RegistryService> aware) throws Exception {
        RegistryService<EObject> registry = registry(aware);
        uploadSource(registry, DRAFT, "Rename", "Rename", RENAME);

        AtlasUnitStore store = new AtlasUnitStore(registry, SCOPE, DRAFT);
        UnitKey newest = store.versions(QvtUnits.LANGUAGE_QVTO, "Rename", UnitKind.COMPILED).get(0);
        UnitKey pinned = UnitKey.pinned(QvtUnits.LANGUAGE_QVTO, "Rename", UnitKind.COMPILED,
                newest.fingerprint().orElseThrow());

        // the consumer side: an engine that knows no resolver, fed by prepare only
        QvtoUnitResolver forbidden = name -> {
            throw new AssertionError("a resolver was asked for '" + name + "' after prepare");
        };
        QvtoEngine runner = QvtoEngines.create(QvtoConfiguration.builder()
                .addUnitResolver(forbidden).unitResolverEnabled(true).build());
        PreparedContext prepared = UnitPreparer.withDefaults(store, runner.unitBinder()).prepare(pinned);

        EPackage input = EcoreFactory.eINSTANCE.createEPackage();
        input.setName("shelf");
        input.setNsURI("http://example.org/qvt-test/shelf");
        runner.execute(prepared, "Rename", QvtoExecutionContext.of(new BasicQvtoModelExtent(input)));
        assertEquals("SHELF", input.getName(), "the fetched unit executed");
    }
}
