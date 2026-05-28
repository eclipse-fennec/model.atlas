///**
// * Copyright (c) 2012 - 2026 Data In Motion and others.
// * All rights reserved.
// *
// * This program and the accompanying materials are made
// * available under the terms of the Eclipse Public License 2.0
// * which is available at https://www.eclipse.org/legal/epl-2.0/
// *
// * SPDX-License-Identifier: EPL-2.0
// *
// * Contributors:
// *     Data In Motion - initial API and implementation
// */
//package org.eclipse.fennec.data.atlas.jpa.watcher;
//
//import java.io.IOException;
//import java.lang.System.Logger;
//import java.lang.System.Logger.Level;
//import java.util.Dictionary;
//import java.util.Hashtable;
//
//import org.eclipse.emf.ecore.EPackage;
//import org.eclipse.fennec.data.atlas.mapping.model.converter.TableMappingConverter;
//import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig;
//import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
//import org.eclipse.fennec.persistence.eorm.EntityMappings;
//import org.osgi.framework.BundleContext;
//import org.osgi.framework.Constants;
//import org.osgi.framework.Filter;
//import org.osgi.framework.InvalidSyntaxException;
//import org.osgi.framework.ServiceReference;
//import org.osgi.service.cm.Configuration;
//import org.osgi.service.cm.ConfigurationAdmin;
//import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
//import org.osgi.service.component.annotations.Activate;
//import org.osgi.service.component.annotations.Component;
//import org.osgi.service.component.annotations.ConfigurationPolicy;
//import org.osgi.service.component.annotations.Deactivate;
//import org.osgi.service.component.annotations.Reference;
//import org.osgi.service.component.annotations.ServiceScope;
//import org.osgi.service.metatype.annotations.AttributeDefinition;
//import org.osgi.service.metatype.annotations.Designate;
//import org.osgi.service.metatype.annotations.ObjectClassDefinition;
//import org.osgi.util.tracker.ServiceTracker;
//import org.osgi.util.tracker.ServiceTrackerCustomizer;
//
///**
// * Bridges the file-watcher pipeline to the JPA persistence unit.
// *
// * <p>DS activates this component once the {@link JpaMappingConfig} service for
// * the configured unit is registered. It then opens a {@link ServiceTracker}
// * for the {@link EPackage} whose nsURI matches
// * {@link JpaMappingConfig#getTargetModelNsUri()}. When the EPackage arrives,
// * it generates an {@link EntityMappings} via {@link TableMappingConverter},
// * registers it as an OSGi service, and creates a
// * {@code fennec.jpa.EMPersistenceUnit} factory configuration so that
// * EclipseLink can start serving JPA queries over the loaded data.
// *
// * <p>When the {@link JpaMappingConfig} is modified (e.g. a column renamed),
// * the persistence unit is torn down and rebuilt so EclipseLink recreates
// * the schema with the new layout.
// *
// * <p>Everything is torn down cleanly when the EPackage disappears or this
// * component is deactivated.
// */
//@RequireConfigurationAdmin
//@Designate(factory = true, ocd = JpaPersistenceUnitConfigurator.Config.class)
//@Component(name = JpaPersistenceUnitConfigurator.PID, configurationPolicy = ConfigurationPolicy.REQUIRE,
//        scope = ServiceScope.SINGLETON)
//public class JpaPersistenceUnitConfigurator {
//
//    private static final Logger LOG = System.getLogger(JpaPersistenceUnitConfigurator.class.getName());
//
//    public static final String PID = "JpaPersistenceUnitConfigurator";
//
//    static final String PROP_EORM_MAPPING_NAME = "fennec.jpa.orm.mapping.name";
//
//    @ObjectClassDefinition
//    public @interface Config {
//        @AttributeDefinition(name = "Unit name", description = "Persistence unit name — must match JpaMappingConfig.name")
//        String unitName();
//    }
//
////    @Reference(name = "jpaMappingConfig", target = "(scope=no-inject)")
////    private JpaMappingConfig jpaMappingConfig;
//    
//    @Reference(name = "entityMappings", target = "(scope=no-inject)")
//    EntityMappings entityMappings;
//
//    @Reference
//    private ConfigurationAdmin configAdmin;
//
//    private String unitName;
////    private BundleContext bundleContext;
//    private ServiceTracker<EPackage, EPackage> ePackageTracker;
////    private ServiceRegistration<EntityMappings> entityMappingsReg;
//    private Configuration emPersistenceUnitConfig;
//
//    @Activate
//    void activate(BundleContext ctx, Config config) {
////        bundleContext = ctx;
//        unitName = config.unitName();
//        openEPackageTracker(ctx);
//    }
//
//    @Deactivate
//    void deactivate() {
//        if (ePackageTracker != null) {
//            ePackageTracker.close();
//            ePackageTracker = null;
//        }
//        teardownPersistence();
//    }
//
//    private void openEPackageTracker(BundleContext ctx) {
//        String nsUri = entityMappings.getPackage();
//        if (nsUri == null || nsUri.isBlank()) {
//            LOG.log(Level.WARNING,
//                    "EntityMappings ''{0}'' has no targetModelNsUri — skipping persistence unit setup", unitName);
//            return;
//        }
//        try {
//            Filter filter = ctx.createFilter(
//                    "(&(" + Constants.OBJECTCLASS + "=" + EPackage.class.getName() + ")"
//                    + "(" + EMFNamespaces.EMF_MODEL_NSURI + "=" + nsUri + "))");
//            ePackageTracker = new ServiceTracker<>(ctx, filter, new ServiceTrackerCustomizer<>() {
//                @Override
//                public EPackage addingService(ServiceReference<EPackage> ref) {
//                    EPackage ePackage = ctx.getService(ref);
//                    if (ePackage != null) {
//                        setupPersistence(ePackage);
//                    }
//                    return ePackage;
//                }
//
//                @Override
//                public void modifiedService(ServiceReference<EPackage> ref, EPackage ePackage) {
//                }
//
//                @Override
//                public void removedService(ServiceReference<EPackage> ref, EPackage ePackage) {
//                    teardownPersistence();
//                    ctx.ungetService(ref);
//                }
//            });
//            ePackageTracker.open(true);
//        } catch (InvalidSyntaxException e) {
//            LOG.log(Level.ERROR, "Invalid EPackage filter for nsURI " + nsUri, e);
//        }
//    }
//
//    private void setupPersistence(EPackage ePackage) {
//        try {
////            EntityMappings mappings = new TableMappingConverter().toEntityMappings(ePackage, jpaMappingConfig);
////
////            Dictionary<String, Object> mappingProps = new Hashtable<>();
////            mappingProps.put(PROP_EORM_MAPPING_NAME, unitName);
////            entityMappingsReg = bundleContext.registerService(EntityMappings.class, mappings, mappingProps);
////            System.out.println("Registered EntityMappings for unitName " + unitName);
//            
//            emPersistenceUnitConfig = configAdmin.getFactoryConfiguration(
//                    "fennec.jpa.EMPersistenceUnit", unitName, "?");
//            Dictionary<String, Object> puProps = new Hashtable<>();
//            puProps.put("fennec.jpa.persistenceUnitName", unitName);
//            puProps.put("fennec.jpa.dataSource.target", "(unitName=" + unitName + ")");
//            puProps.put("fennec.jpa.mapping.target", "(" + PROP_EORM_MAPPING_NAME + "=" + unitName + ")");
//            emPersistenceUnitConfig.update(puProps);
//            System.out.println("Registered fennec.jpa.EMPersistenceUnit for unitName " + unitName);
//            LOG.log(Level.INFO, "JPA persistence unit ''{0}'' configured", unitName);
//        } catch (IOException e) {
//            LOG.log(Level.ERROR, "Failed to create EMPersistenceUnit config for unit " + unitName, e);
//        } catch (Exception e) {
//            LOG.log(Level.ERROR, "Failed to generate EntityMappings for unit " + unitName, e);
//        }
//    }
//
//    private void teardownPersistence() {
////        if (entityMappingsReg != null) {
////            try {
////                entityMappingsReg.unregister();
////                System.out.println("Unregistered EntityMappings for unitName " + unitName);
////            } catch (IllegalStateException ignored) {
////            }
////            entityMappingsReg = null;
////        }
//        if (emPersistenceUnitConfig != null) {
//            try {
//                emPersistenceUnitConfig.delete();
//                System.out.println("Unregistered fennec.jpa.EMPersistenceUnit for unitName " + unitName);
//            } catch (IOException ignored) {
//            }
//            emPersistenceUnitConfig = null;
//        }
//    }
//}
