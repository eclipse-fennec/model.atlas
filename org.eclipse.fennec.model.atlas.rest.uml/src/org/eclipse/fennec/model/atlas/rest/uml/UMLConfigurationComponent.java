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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.rest.uml;

import java.util.Hashtable;

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource.Factory;
import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.condition.Condition;

/**
 * 
 * @author ilenia
 * @since Nov 13, 2025
 */
@Component(immediate = true, name = "UMLConfigurationComponent")
public class UMLConfigurationComponent {

    private ServiceRegistration<?> ePackageConfiguratorRegistration = null;
    private ServiceRegistration<?> resourceFactoryRegistration = null;
    private ServiceRegistration<?> packageRegistration = null;
    private ServiceRegistration<?> eFactoryRegistration = null;
    private ServiceRegistration<?> conditionRegistration = null;

    @Activate
    public void activate(BundleContext ctx) {
        UMLPackage ePackage = UMLPackage.eINSTANCE;
        UMLEPackageConfigurator packageConfigurator = registerEPackageConfiguratorService(ePackage, ctx);
        registerResourceFactoryService(ctx);
        registerEPackageService(ePackage, packageConfigurator, ctx);
        registerEFactoryService(ePackage, packageConfigurator, ctx);
        registerConditionService(packageConfigurator, ctx);
    }

    @Deactivate
    public void deactivate() {
        ePackageConfiguratorRegistration.unregister();
        resourceFactoryRegistration.unregister();
        packageRegistration.unregister();
        eFactoryRegistration.unregister();
        conditionRegistration.unregister();
    }

    private void registerResourceFactoryService(BundleContext ctx) {
        // Must stay on the API-typed UMLResource.Factory.INSTANCE; the equivalent
        // UMLResourceFactoryImpl lives in an internal UML2 package with no API contract.
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(EMFNamespaces.EMF_CONFIGURATOR_NAME, UMLPackage.eNAME);
        properties.put(EMFNamespaces.EMF_MODEL_FILE_EXT, UMLResource.FILE_EXTENSION);
        properties.put(EMFNamespaces.EMF_MODEL_VERSION, "1.0");
        resourceFactoryRegistration = ctx.registerService(Factory.class.getName(), UMLResource.Factory.INSTANCE,
                properties);
    }

    private UMLEPackageConfigurator registerEPackageConfiguratorService(UMLPackage ePackage, BundleContext ctx) {
        UMLEPackageConfigurator packageConfigurator = new UMLEPackageConfigurator(ePackage);
        // register the EPackageConfigurator
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.putAll(packageConfigurator.getServiceProperties());
        ePackageConfiguratorRegistration = ctx.registerService(EPackageConfigurator.class, packageConfigurator,
                properties);

        return packageConfigurator;
    }

    private void registerEPackageService(UMLPackage ePackage, UMLEPackageConfigurator packageConfigurator,
            BundleContext ctx) {
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.putAll(packageConfigurator.getServiceProperties());
        String[] serviceClasses = new String[] { UMLPackage.class.getName(), EPackage.class.getName() };
        packageRegistration = ctx.registerService(serviceClasses, ePackage, properties);
    }

    private void registerEFactoryService(UMLPackage ePackage, UMLEPackageConfigurator packageConfigurator,
            BundleContext ctx) {
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.putAll(packageConfigurator.getServiceProperties());
        String[] serviceClasses = new String[] { UMLFactory.class.getName(), EFactory.class.getName() };
        eFactoryRegistration = ctx.registerService(serviceClasses, ePackage.getUMLFactory(), properties);
    }

    private void registerConditionService(UMLEPackageConfigurator packageConfigurator, BundleContext ctx) {
        // register the EPackage
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.putAll(packageConfigurator.getServiceProperties());
        properties.put(Condition.CONDITION_ID, UMLPackage.eNS_URI);
        conditionRegistration = ctx.registerService(Condition.class, Condition.INSTANCE, properties);
    }

}
