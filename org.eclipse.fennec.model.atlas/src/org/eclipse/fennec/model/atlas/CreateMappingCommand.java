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
package org.eclipse.fennec.model.atlas;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.annotations.GogoCommand;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.persistence.eorm.EntityMappings;
import org.eclipse.fennec.persistence.orm.processor.MappingProcessor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * 
 * @author ungei
 * @since 19 Feb 2025
 */
@Component(service = CreateMappingCommand.class, scope = ServiceScope.PROTOTYPE)
@GogoCommand(scope = "mdo", function = "createMapping")
public class CreateMappingCommand {
	
	private static Path BASE_PATH = null;
	
	static void setBasePath(Path basePath) {
		BASE_PATH = basePath;
	}

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	ResourceSet set;
	
	public void help() {
		System.out.println("");
	}
	
	@Descriptor("Creates a  JPA mapping from the given ecore file. The mapping will be stored next to the ecore file.\n"
			+ "Usage: createMapping <ecoreFileLocation> <persistanceUnitName>")
	public void createMapping(
			@Descriptor("the location of the ecore file. Can be absolut or relative to the configured data folder") 
			String ecoreUri, 
			@Descriptor("the name of the persitence unit. Will become the name of the file with the mapping as well.")
			String unitName) {
		
		Path absolutePath = null;
		if(BASE_PATH != null) {
			absolutePath = Path.of(BASE_PATH.toString(), ecoreUri).toAbsolutePath();
		} else {
			absolutePath = Path.of(ecoreUri).toAbsolutePath();
		}
		if(!absolutePath.toFile().exists()) {
			System.err.println("File " + absolutePath.toString() + " does not exist!");
			return;
		}
		
		String ecoreUriToUse = absolutePath.toUri().toString();
		if(!ecoreUriToUse.startsWith("file://")) {
			ecoreUriToUse = "file://" + ecoreUri;
		}
		URI ecoreURI = URI.createURI(ecoreUriToUse);
		Resource resource = set.createResource(ecoreURI);
		try {
			resource.load(Map.of());
		} catch (IOException e) {
			System.err.println("Unnable to load " + ecoreUri + ": " + e.getMessage());
			e.printStackTrace();
			return;
		}
		EObject eObject = resource.getContents().get(0);
		if(eObject == null) {
			System.err.println("The given file seems to be empty");
			return;
		}
		
		if(!(eObject instanceof EPackage)){
			System.err.println("Aehm, the content of the file should be of type EPackage but it actually is: " + eObject.eClass().getName());
			return;
		}
		
		EPackage ePackage = (EPackage) eObject;
		MappingProcessor mappingProcessor = MappingProcessor.create(ePackage);
		mappingProcessor.process();
		EntityMappings target = mappingProcessor.getTarget();
		target.setName(unitName);
		
		String basePath = absolutePath.getParent().toUri().toString();
		
		Resource mappingResource = set.createResource(URI.createURI(basePath + unitName + ".xmi"));
		mappingResource.getContents().add(target);
		try {
			mappingResource.save(Map.of());
		} catch (IOException e) {
			System.err.println("Unable to save the resulting mapping to " + resource.getURI().toString() + " : " + e.getMessage());
			e.printStackTrace();
		}
		System.out.println("saved MappingFile to " + mappingResource.getURI().toString());
	}
	
}
