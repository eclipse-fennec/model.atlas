package org.eclipse.fennec.data.atlas.mapping.model.converter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.ColumnMapping;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping;
import org.eclipse.fennec.persistence.eorm.Attributes;
import org.eclipse.fennec.persistence.eorm.Base;
import org.eclipse.fennec.persistence.eorm.BaseColumn;
import org.eclipse.fennec.persistence.eorm.Entity;
import org.eclipse.fennec.persistence.eorm.EntityMappings;
import org.eclipse.fennec.persistence.orm.EntityMapper;

public class TableMappingConverter {

    public EntityMappings toEntityMappings(EPackage ePackage, JpaMappingConfig config) {
        EntityMappings mappings = new EntityMapper().createMappingsFromEPackage(ePackage);

        Map<String, TableMapping> byClassUri = config.getTableMappings().stream()
                .collect(Collectors.toMap(TableMapping::getClassName, Function.identity()));

        mappings.getEntity().removeIf(entity -> !byClassUri.containsKey(classUri(ePackage, entity)));

        for (Entity entity : mappings.getEntity()) {
            applyTableMapping(ePackage, entity, byClassUri.get(classUri(ePackage, entity)));
        }

        return mappings;
    }

    private void applyTableMapping(EPackage ePackage, Entity entity, TableMapping tableMapping) {
        if (entity.getTable() != null) {
            if (tableMapping.getTableName() != null && !tableMapping.getTableName().isBlank()) {
                entity.getTable().setName(tableMapping.getTableName());
            }
            if (tableMapping.getSchema() != null && !tableMapping.getSchema().isBlank()) {
                entity.getTable().setSchema(tableMapping.getSchema());
            }
        }

        if (tableMapping.getColumnMappings().isEmpty() || entity.getAttributes() == null) {
            return;
        }

        EClass eClass = (EClass) ePackage.getEClassifier(entity.getName());
        Map<String, ColumnMapping> colsByFeatureName = tableMapping.getColumnMappings().stream()
                .collect(Collectors.toMap(ColumnMapping::getFeatureName, Function.identity()));

        applyColumnMappings(eClass, entity.getAttributes(), colsByFeatureName);
    }

    private void applyColumnMappings(EClass eClass, Attributes attributes, Map<String, ColumnMapping> colsByFeatureName) {
        for (Base attribute : allAttributes(attributes)) {
            ColumnMapping col = colsByFeatureName.get(attribute.getName());
            if (col == null) {
                continue;
            }
            EAttribute eAttribute = (EAttribute) eClass.getEStructuralFeature(attribute.getName());
            BaseColumn column = attribute.getColumn();
            if (eAttribute == null || column == null) {
                continue;
            }
            if (col.getColumnName() != null && !col.getColumnName().isBlank()) {
                column.setName(col.getColumnName());
            }
            if (col.getColumnType() != null && !col.getColumnType().isBlank()) {
                column.setColumnDefinition(col.getColumnType());
            }
            column.setNullable(col.isNullable());
        }
    }

    private Iterable<Base> allAttributes(Attributes attributes) {
        var all = new java.util.ArrayList<Base>();
        all.addAll(attributes.getId());
        all.addAll(attributes.getBasic());
        return all;
    }

    private String classUri(EPackage ePackage, Entity entity) {
        return ePackage.getNsURI() + "#//" + entity.getName();
    }
}
