package org.eclipse.fennec.data.atlas.mapping.model.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.ColumnMapping;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingFactory;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping;
import org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGPackage;
import org.eclipse.fennec.persistence.eorm.Basic;
import org.eclipse.fennec.persistence.eorm.Entity;
import org.eclipse.fennec.persistence.eorm.EntityMappings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TableMappingConverterTest {

    private static final DGPackage DGE = DGPackage.eINSTANCE;
    private static final String DGE_NS = DGE.getNsURI();

    private TableMappingConverter converter;

    @BeforeEach
    void setUp() {
        converter = new TableMappingConverter();
    }

    @Test
    void unmappedClassesAreRemovedFromEorm() {
        JpaMappingConfig config = createConfig(DGE_NS,
                tableMapping(DGE_NS + "#//Person", "PERSON"),
                tableMapping(DGE_NS + "#//Address", "ADDRESS"));

        EntityMappings result = converter.toEntityMappings(DGE, config);

        List<String> names = result.getEntity().stream().map(Entity::getName).toList();
        assertEquals(2, names.size());
        assertTrue(names.contains("Person"));
        assertTrue(names.contains("Address"));
        assertFalse(names.contains("Company"));
    }

    @Test
    void allMappedClassesProduceEntities() {
        JpaMappingConfig config = createConfig(DGE_NS,
                tableMapping(DGE_NS + "#//Person", "PERSON"),
                tableMapping(DGE_NS + "#//Address", "ADDRESS"),
                tableMapping(DGE_NS + "#//Company", "COMPANY"));

        EntityMappings result = converter.toEntityMappings(DGE, config);

        List<String> names = result.getEntity().stream().map(Entity::getName).toList();
        assertEquals(3, names.size());
        assertTrue(names.contains("Person"));
        assertTrue(names.contains("Address"));
        assertTrue(names.contains("Company"));
    }

    @Test
    void tableNameIsOverridden() {
        JpaMappingConfig config = createConfig(DGE_NS,
                tableMapping(DGE_NS + "#//Person", "PERSONS_TABLE"));

        EntityMappings result = converter.toEntityMappings(DGE, config);

        assertEquals("PERSONS_TABLE", findEntity(result, "Person").getTable().getName());
    }

    @Test
    void schemaIsApplied() {
        TableMapping tm = tableMapping(DGE_NS + "#//Address", "ADDRESS");
        tm.setSchema("public");

        EntityMappings result = converter.toEntityMappings(DGE, createConfig(DGE_NS, tm));

        assertEquals("public", findEntity(result, "Address").getTable().getSchema());
    }

    @Test
    void columnNameIsOverridden() {
        TableMapping tm = tableMapping(DGE_NS + "#//Person", "PERSON");
        tm.getColumnMappings().add(columnMapping("firstName", "first_name", null, false));

        EntityMappings result = converter.toEntityMappings(DGE, createConfig(DGE_NS, tm));

        assertEquals("first_name", findBasic(findEntity(result, "Person"), "firstName").getColumn().getName());
    }

    @Test
    void columnTypeIsSetAsColumnDefinition() {
        TableMapping tm = tableMapping(DGE_NS + "#//Address", "ADDRESS");
        tm.getColumnMappings().add(columnMapping("zipCode", "zip_code", "VARCHAR(10)", false));

        EntityMappings result = converter.toEntityMappings(DGE, createConfig(DGE_NS, tm));

        assertEquals("VARCHAR(10)", findBasic(findEntity(result, "Address"), "zipCode").getColumn().getColumnDefinition());
    }

    @Test
    void nullableIsAppliedToColumn() {
        TableMapping tm = tableMapping(DGE_NS + "#//Person", "PERSON");
        tm.getColumnMappings().add(columnMapping("email", "email", null, false));

        EntityMappings result = converter.toEntityMappings(DGE, createConfig(DGE_NS, tm));

        assertFalse(findBasic(findEntity(result, "Person"), "email").getColumn().isNullable());
    }

    @Test
    void idAttributeIsMappedAsId() {
        var ecoreFactory = EcoreFactory.eINSTANCE;
        var pkg = ecoreFactory.createEPackage();
        pkg.setName("test");
        pkg.setNsPrefix("test");
        pkg.setNsURI("http://test/1.0");

        var eClass = ecoreFactory.createEClass();
        eClass.setName("Item");

        var idAttr = ecoreFactory.createEAttribute();
        idAttr.setName("id");
        idAttr.setEType(EcorePackage.Literals.ESTRING);
        idAttr.setID(true);
        eClass.getEStructuralFeatures().add(idAttr);

        var labelAttr = ecoreFactory.createEAttribute();
        labelAttr.setName("label");
        labelAttr.setEType(EcorePackage.Literals.ESTRING);
        eClass.getEStructuralFeatures().add(labelAttr);

        pkg.getEClassifiers().add(eClass);

        EntityMappings result = converter.toEntityMappings(pkg, createConfig("http://test/1.0",
                tableMapping("http://test/1.0#//Item", "ITEM")));

        Entity item = findEntity(result, "Item");
        assertEquals(1, item.getAttributes().getId().size());
        assertEquals("id", item.getAttributes().getId().get(0).getName());
        List<String> basicNames = item.getAttributes().getBasic().stream().map(Basic::getName).toList();
        assertEquals(2, basicNames.size());
        assertTrue(basicNames.contains("id"));
        assertTrue(basicNames.contains("label"));
    }

    // --- helpers ---

    private JpaMappingConfig createConfig(String nsUri, TableMapping... mappings) {
        JpaMappingConfig config = JPAMappingFactory.eINSTANCE.createJpaMappingConfig();
        config.setTargetModelNsUri(nsUri);
        for (TableMapping m : mappings) {
            config.getTableMappings().add(m);
        }
        return config;
    }

    private TableMapping tableMapping(String classUri, String tableName) {
        TableMapping tm = JPAMappingFactory.eINSTANCE.createTableMapping();
        tm.setClassName(classUri);
        tm.setTableName(tableName);
        return tm;
    }

    private ColumnMapping columnMapping(String featureName, String columnName, String columnType, boolean nullable) {
        ColumnMapping col = JPAMappingFactory.eINSTANCE.createColumnMapping();
        col.setFeatureName(featureName);
        col.setColumnName(columnName);
        if (columnType != null) {
            col.setColumnType(columnType);
        }
        col.setNullable(nullable);
        return col;
    }

    private Entity findEntity(EntityMappings mappings, String name) {
        return mappings.getEntity().stream()
                .filter(e -> name.equals(e.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No entity named: " + name));
    }

    private Basic findBasic(Entity entity, String featureName) {
        return entity.getAttributes().getBasic().stream()
                .filter(b -> featureName.equals(b.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No basic attribute named: " + featureName));
    }
}
