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
//package org.eclipse.fennec.data.atlas.jpa.tests;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//import java.util.Dictionary;
//import java.util.Hashtable;
//
//import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig;
//import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingFactory;
//import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig;
//import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.SqlDialect;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.osgi.framework.BundleContext;
//import org.osgi.framework.ServiceRegistration;
//import org.osgi.service.cm.Configuration;
//import org.osgi.service.cm.ConfigurationAdmin;
//import org.osgi.test.common.annotation.InjectBundleContext;
//import org.osgi.test.common.annotation.InjectService;
//import org.osgi.test.junit5.cm.ConfigurationExtension;
//import org.osgi.test.junit5.context.BundleContextExtension;
//import org.osgi.test.junit5.service.ServiceExtension;
//
//@ExtendWith(BundleContextExtension.class)
//@ExtendWith(ServiceExtension.class)
//@ExtendWith(ConfigurationExtension.class)
//public class DataSourceConfigHandlerTests {
//
//    private static final String H2_FACTORY_PID = "daanse.jdbc.datasource.h2.DataSource";
//
//    private ServiceRegistration<JpaMappingConfig> currentRegistration;
//
//    @AfterEach
//    void cleanUp() {
//        if (currentRegistration != null) {
//            try {
//                currentRegistration.unregister();
//            } catch (IllegalStateException e) {
//                // already unregistered in the test
//            }
//            currentRegistration = null;
//        }
//    }
//
//    @Test
//    public void testH2DataSource_createdWhenJpaMappingConfigRegistered(
//            @InjectBundleContext BundleContext ctx,
//            @InjectService ConfigurationAdmin configAdmin) throws Exception {
//        currentRegistration = registerService(ctx, createH2Config("ds-create-test"));
//        assertNotNull(waitForConfiguration(configAdmin, "ds-create-test", 5000));
//    }
//
//    @Test
//    public void testH2DataSource_hasCorrectProperties(
//            @InjectBundleContext BundleContext ctx,
//            @InjectService ConfigurationAdmin configAdmin) throws Exception {
//        currentRegistration = registerService(ctx, createH2Config("ds-props-test"));
//        Configuration cfg = waitForConfiguration(configAdmin, "ds-props-test", 15_000);
//        assertNotNull(cfg);
//        Dictionary<String, Object> props = cfg.getProperties();
//        assertEquals("mem:testdb", props.get("identifier"));
//        assertEquals("sa", props.get("username"));
//        assertEquals("DB_PASSWORD", props.get(".password"));
//    }
//
//    @Test
//    public void testNonH2JpaMappingConfig_noDataSourceCreated(
//            @InjectBundleContext BundleContext ctx,
//            @InjectService ConfigurationAdmin configAdmin) throws Exception {
//        currentRegistration = registerService(ctx, createPostgresConfig("ds-postgres-test"));
//        Thread.sleep(2000);
//        assertNull(configAdmin.listConfigurations(
//                "(service.pid=" + H2_FACTORY_PID + "~ds-postgres-test)"));
//    }
//
//    @Test
//    public void testH2DataSource_deletedWhenJpaMappingConfigUnregistered(
//            @InjectBundleContext BundleContext ctx,
//            @InjectService ConfigurationAdmin configAdmin) throws Exception {
//        currentRegistration = registerService(ctx, createH2Config("ds-delete-test"));
//        assertNotNull(waitForConfiguration(configAdmin, "ds-delete-test", 5000));
//        currentRegistration.unregister();
//        currentRegistration = null;
//        assertTrue(waitForNoConfiguration(configAdmin, "ds-delete-test", 5000));
//    }
//
//    @Test
//    public void testH2DataSource_recreatedWhenMappingNameChanges(
//            @InjectBundleContext BundleContext ctx,
//            @InjectService ConfigurationAdmin configAdmin) throws Exception {
//    	
//        String PROP_NAME = "jpamapping.name";
//        String PROP_UNIT_NAME = "unitName";
//
//    	Dictionary<String, Object> properties = new Hashtable<>();
//    	properties.put(PROP_NAME, "ds-rename-old");
//    	properties.put(PROP_UNIT_NAME, "testUnitName");
//    	    	
//        JpaMappingConfig config = createH2Config("ds-rename-old");
//        
//        currentRegistration = registerService(ctx, config, properties);
//        assertNotNull(waitForConfiguration(configAdmin, "ds-rename-old", 5000));
//
//        currentRegistration.unregister();
//
//        properties.put(PROP_NAME, "ds-rename-new");
//        config.setName("ds-rename-new");
//        currentRegistration = registerService(ctx, config, properties);
//
//        assertTrue(waitForNoConfiguration(configAdmin, "ds-rename-old", 15_000));
//        assertNotNull(waitForConfiguration(configAdmin, "ds-rename-new", 5000));
//    }
//
//    private JpaMappingConfig createH2Config(String name) {
//        JpaMappingConfig config = JPAMappingFactory.eINSTANCE.createJpaMappingConfig();
//        config.setName(name);
//        DataSourceConfig ds = JPAMappingFactory.eINSTANCE.createDataSourceConfig();
//        ds.setDialect(SqlDialect.H2);
//        ds.setJdbcUrl("jdbc:h2:mem:testdb");
//        ds.setUsername("sa");
//        ds.setPasswordRef("DB_PASSWORD");
//        config.setDataSource(ds);
//        return config;
//    }
//
//    private JpaMappingConfig createPostgresConfig(String name) {
//        JpaMappingConfig config = JPAMappingFactory.eINSTANCE.createJpaMappingConfig();
//        config.setName(name);
//        DataSourceConfig ds = JPAMappingFactory.eINSTANCE.createDataSourceConfig();
//        ds.setDialect(SqlDialect.POSTGRES);
//        ds.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
//        ds.setUsername("user");
//        ds.setPasswordRef("PG_PASSWORD");
//        config.setDataSource(ds);
//        return config;
//    }
//
//    private ServiceRegistration<JpaMappingConfig> registerService(BundleContext ctx, JpaMappingConfig config) {
//        return ctx.registerService(JpaMappingConfig.class, config, new Hashtable<>());
//    }
//    
//    private ServiceRegistration<JpaMappingConfig> registerService(BundleContext ctx, JpaMappingConfig config, Dictionary<String, Object> properties) {
//        return ctx.registerService(JpaMappingConfig.class, config, properties);
//    }
//
//    private Configuration waitForConfiguration(ConfigurationAdmin ca, String name, long timeoutMs) throws Exception {
//        String filter = "(service.pid=" + H2_FACTORY_PID + "~" + name + ")";
//        long deadline = System.currentTimeMillis() + timeoutMs;
//        while (System.currentTimeMillis() < deadline) {
//            Configuration[] cfgs = ca.listConfigurations(filter);
//            if (cfgs != null && cfgs.length > 0) {
//                return cfgs[0];
//            }
//            Thread.sleep(100);
//        }
//        return null;
//    }
//
//    private boolean waitForNoConfiguration(ConfigurationAdmin ca, String name, long timeoutMs) throws Exception {
//        String filter = "(service.pid=" + H2_FACTORY_PID + "~" + name + ")";
//        long deadline = System.currentTimeMillis() + timeoutMs;
//        while (System.currentTimeMillis() < deadline) {
//            if (ca.listConfigurations(filter) == null) {
//                return true;
//            }
//            Thread.sleep(100);
//        }
//        return ca.listConfigurations(filter) == null;
//    }
//}
