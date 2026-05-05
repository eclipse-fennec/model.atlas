package org.eclipse.fennec.data.atlas.mapping.model.converter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.ColumnMapping;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinType;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping;
import org.eclipse.fennec.persistence.eorm.Attributes;
import org.eclipse.fennec.persistence.eorm.Base;
import org.eclipse.fennec.persistence.eorm.BaseColumn;
import org.eclipse.fennec.persistence.eorm.BaseRef;
import org.eclipse.fennec.persistence.eorm.EORMFactory;
import org.eclipse.fennec.persistence.eorm.Entity;
import org.eclipse.fennec.persistence.eorm.EntityMappings;
import org.eclipse.fennec.persistence.eorm.ForeignKey;
import org.eclipse.fennec.persistence.eorm.JoinColumn;
import org.eclipse.fennec.persistence.eorm.ManyToOne;
import org.eclipse.fennec.persistence.eorm.OneToMany;
import org.eclipse.fennec.persistence.eorm.OneToOne;
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

        if (entity.getAttributes() == null) {
            return;
        }

        EClass eClass = (EClass) ePackage.getEClassifier(entity.getName());

        if (!tableMapping.getColumnMappings().isEmpty()) {
            Map<String, ColumnMapping> colsByFeatureName = tableMapping.getColumnMappings().stream()
                    .collect(Collectors.toMap(ColumnMapping::getFeatureName, Function.identity()));
            applyColumnMappings(eClass, entity.getAttributes(), colsByFeatureName);
        }

        if (!tableMapping.getJoinMappings().isEmpty()) {
            applyJoinMappings(entity.getAttributes(), tableMapping.getJoinMappings());
        }
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

    private void applyJoinMappings(Attributes attributes, List<JoinMapping> joinMappings) {
        Map<String, BaseRef> refsByName = buildRefMap(attributes);
        for (JoinMapping jm : joinMappings) {
            BaseRef ref = refsByName.get(jm.getReferenceName());
            if (ref == null) {
                continue;
            }
            if (jm.getJoinType() == JoinType.FOREIGN_KEY) {
                String col = jm.getJoinColumn();
                if (col != null && !col.isBlank()) {
                    overrideJoinColumn(ref, col);
                }
            }
            if (!jm.getCascadeType().isEmpty()) {
                applyCascade(ref, jm.getCascadeType());
            }
        }
    }

    private Map<String, BaseRef> buildRefMap(Attributes attributes) {
        Map<String, BaseRef> map = new LinkedHashMap<>();
        attributes.getManyToOne().forEach(r -> map.put(r.getName(), r));
        attributes.getOneToMany().forEach(r -> map.put(r.getName(), r));
        attributes.getOneToOne().forEach(r -> map.put(r.getName(), r));
        attributes.getManyToMany().forEach(r -> map.put(r.getName(), r));
        return map;
    }

    private void overrideJoinColumn(BaseRef ref, String columnName) {
        List<JoinColumn> list = joinColumnListOf(ref);
        if (list == null) {
            return;
        }
        // If the auto-generated mapping used a JoinTable, switch it to FK strategy.
        if (ref.getJoinTable() != null) {
            ref.setJoinTable(null);
        }
        if (list.isEmpty()) {
            JoinColumn jc = EORMFactory.eINSTANCE.createJoinColumn();
            jc.setName(columnName);
            list.add(jc);
        } else {
            list.get(0).setName(columnName);
        }
        // ReferenceConfigurator.processOneToOne() reads fk.getName() (not the JoinColumn list)
        // to build the EclipseLink mapping. The auto-generated name is uppercase (e.g. EMPLOYEE_ID),
        // so we must keep the ForeignKey name in sync with the override.
        if (ref.getForeignKey() != null) {
            ref.getForeignKey().setName(columnName);
        } else {
            ForeignKey fk = EORMFactory.eINSTANCE.createForeignKey();
            fk.setName(columnName);
            ref.setForeignKey(fk);
        }
    }

    private List<JoinColumn> joinColumnListOf(BaseRef ref) {
        if (ref instanceof ManyToOne r) return r.getJoinColumn();
        if (ref instanceof OneToMany r) return r.getJoinColumn();
        if (ref instanceof OneToOne r)  return r.getJoinColumn();
        return null;
    }

    private void applyCascade(BaseRef ref,
            List<org.eclipse.fennec.data.atlas.mapping.model.jpamapping.CascadeType> cascadeTypes) {
        org.eclipse.fennec.persistence.eorm.CascadeType cascade = EORMFactory.eINSTANCE.createCascadeType();
        for (org.eclipse.fennec.data.atlas.mapping.model.jpamapping.CascadeType ct : cascadeTypes) {
            switch (ct) {
                case ALL     -> cascade.setCascadeAll(EORMFactory.eINSTANCE.createEmptyType());
                case PERSIST -> cascade.setCascadePersist(EORMFactory.eINSTANCE.createEmptyType());
                case MERGE   -> cascade.setCascadeMerge(EORMFactory.eINSTANCE.createEmptyType());
                case REMOVE  -> cascade.setCascadeRemove(EORMFactory.eINSTANCE.createEmptyType());
                case REFRESH -> cascade.setCascadeRefresh(EORMFactory.eINSTANCE.createEmptyType());
                case DETACH  -> cascade.setCascadeDetach(EORMFactory.eINSTANCE.createEmptyType());
            }
        }
        ref.setCascade(cascade);
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
