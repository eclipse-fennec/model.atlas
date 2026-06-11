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
package org.eclipse.fennec.data.atlas.jpa.rest;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.codec.rest.jakartas.spi.ResourceSetProvider;
import org.eclipse.fennec.data.atlas.jpa.watcher.api.WatcherConstants;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.persistence.eorm.EntityMappings;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.propertytypes.ServiceRanking;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.persistence.EntityManagerFactory;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

/**
 * Validates the {@code rootFolderName}-scoped JPA request (resolving the
 * matching {@code EntityManagerFactory}, {@code EntityMappings} and EMF
 * {@code EClassifier}), stashes the resolved EntityManager/EntityMappings on
 * the request, and supplies the per-request {@link ResourceSet} consumed by
 * the codec message-body handlers.
 *
 * <p>The {@link ResourceSet} is exposed through the codec's
 * {@link ResourceSetProvider} SPI rather than the old request-property +
 * {@code ResourceSetFactory} mechanism. The codec's
 * {@code CodecResourceSetFeature} binds the highest-ranked
 * {@link ResourceSetProvider}; the {@link ServiceRanking} below makes this
 * folder-scoped provider win over the codec default.
 *
 * @author ilenia
 * @since May 18, 2026
 */
@Component(service = { ContainerRequestFilter.class, ResourceSetProvider.class })
@JakartarsExtension
@JakartarsName("JpaDataResourceFilter")
@ServiceRanking(100) // Win over the codec's DefaultResourceSetProvider
public class JpaDataResourceFilter implements ContainerRequestFilter, ResourceSetProvider {

	private static final Logger LOGGER = Logger.getLogger(JpaDataResourceFilter.class.getName());
	private Map<String, ResourceSetFactory> folderToResrouceSetFactoryMap = new ConcurrentHashMap<>();
	private Map<String, EntityManagerFactory> folderToEntityManagerFactoryMap = new ConcurrentHashMap<>();
	private Map<String, EntityMappings> folderToEntityMappingsMap = new ConcurrentHashMap<>();
	
	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY)
	void bindResourceSetFactory(ResourceSetFactory resourceSetFactory, Map<String, Object> properties) {
		if(properties.containsKey(WatcherConstants.KEY_JPA_ROOT_FOLDER)) {
			folderToResrouceSetFactoryMap.put((String) properties.get(WatcherConstants.KEY_JPA_ROOT_FOLDER), resourceSetFactory);
		} else {
			LOGGER.warning("Cannot bind ResourceSetFactory without jpa.root.folder property");
		}
	}

	void unbindResourceSetFactory(ResourceSetFactory resourceSetFactory, Map<String, Object> properties) {
		if(properties.containsKey(WatcherConstants.KEY_JPA_ROOT_FOLDER)) {
			folderToResrouceSetFactoryMap.remove((String) properties.get(WatcherConstants.KEY_JPA_ROOT_FOLDER));
		} else {
			LOGGER.warning("Cannot unbind ResourceSetFactory without jpa.root.folder property");
		}	
	}
		
	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY)
	void bindEntityManagerFactory(EntityManagerFactory entityManagerFactory, Map<String, Object> properties) {
		if(properties.containsKey("osgi.unit.name")) {
			folderToEntityManagerFactoryMap.put((String) properties.get("osgi.unit.name"), entityManagerFactory);
		} else {
			LOGGER.warning("Cannot bind EntityManagerFactory without osgi.unit.name property");
		}
	}

	void unbindEntityManagerFactory(EntityManagerFactory entityManagerFactory, Map<String, Object> properties) {
		if(properties.containsKey("osgi.unit.name")) {
			folderToEntityManagerFactoryMap.remove((String) properties.get("osgi.unit.name"));
		} else {
			LOGGER.warning("Cannot unbind EntityManagerFactory without osgi.unit.name property");
		}	
	}
	
	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY)
	void bindEntityMappings(EntityMappings entityMappings, Map<String, Object> properties) {
		if(properties.containsKey(WatcherConstants.KEY_JPA_ROOT_FOLDER)) {
			folderToEntityMappingsMap.put((String) properties.get(WatcherConstants.KEY_JPA_ROOT_FOLDER), entityMappings);
		} else {
			LOGGER.warning("Cannot bind EntityMappings without jpa.root.folder property");
		}
	}

	void unbindEntityMappings(EntityMappings entityMappings, Map<String, Object> properties) {
		if(properties.containsKey(WatcherConstants.KEY_JPA_ROOT_FOLDER)) {
			folderToEntityMappingsMap.remove((String) properties.get(WatcherConstants.KEY_JPA_ROOT_FOLDER));
		} else {
			LOGGER.warning("Cannot unbind EntityMappings without jpa.root.folder property");
		}	
	}

	/* 
	 * (non-Javadoc)
	 * @see jakarta.ws.rs.container.ContainerRequestFilter#filter(jakarta.ws.rs.container.ContainerRequestContext)
	 */
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		MultivaluedMap<String, String> pathParams = requestContext.getUriInfo().getPathParameters();
		String rootFolderName = pathParams.getFirst("rootFolderName");
		if(!folderToResrouceSetFactoryMap.containsKey(rootFolderName)) {
			throw new WebApplicationException(
					Response.status(Response.Status.BAD_REQUEST)
							.entity(String.format("ResourceSetFactory for Root Folder [%s] not found.",
									rootFolderName))
							.build());
		}
		if(!folderToEntityManagerFactoryMap.containsKey(rootFolderName)) {
			throw new WebApplicationException(
					Response.status(Response.Status.BAD_REQUEST)
							.entity(String.format("EntityManagerFactory for Root Folder [%s] not found.",
									rootFolderName))
							.build());
		}
		if(!folderToEntityMappingsMap.containsKey(rootFolderName)) {
			throw new WebApplicationException(
					Response.status(Response.Status.BAD_REQUEST)
							.entity(String.format("EntityMappings for Root Folder [%s] not found.",
									rootFolderName))
							.build());
		}
		EntityMappings entityMappings = folderToEntityMappingsMap.get(rootFolderName);
		MultivaluedMap<String, String> queryParams = requestContext.getUriInfo().getQueryParameters();
		if(queryParams.containsKey("ePackageUri")) {
			String ePackageUri = queryParams.getFirst("ePackageUri");
			if(!ePackageUri.equals(entityMappings.getPackage())) {
				throw new WebApplicationException(
						Response.status(Response.Status.BAD_REQUEST)
								.entity(String.format("The provided ePackageUri [%s] does not match the ePackageUri [%s] supported by the EntityMappings.",
										ePackageUri, entityMappings.getPackage()))
								.build());
			}
		}
		ResourceSetFactory resourceSetFactory = folderToResrouceSetFactoryMap.get(rootFolderName);
		EPackage ePackage = resourceSetFactory.createResourceSet().getPackageRegistry().getEPackage(entityMappings.getPackage());
		if(ePackage == null) {
			throw new WebApplicationException(
					Response.status(Response.Status.BAD_REQUEST)
							.entity(String.format("No EPackage [%s] found in ResourceSet Package Registry.",
								entityMappings.getPackage()))
							.build());
		}
		String className = pathParams.getFirst("eClassName");
		if(ePackage.getEClassifier(className) == null) {
			throw new WebApplicationException(
					Response.status(Response.Status.BAD_REQUEST)
							.entity(String.format("No EClassifier [%s] found in EPckage [%s].",
									className, entityMappings.getPackage()))
							.build());
		}
		requestContext.setProperty("entity.manager.factory", folderToEntityManagerFactoryMap.get(rootFolderName));
		requestContext.setProperty("entity.mappings", entityMappings);
	}

	/**
	 * Supplies the per-request {@link ResourceSet} for the codec message-body
	 * handlers, created from the {@link ResourceSetFactory} registered for the
	 * request's {@code rootFolderName} path parameter. The request filter has
	 * already validated the folder by the time the codec resolves the
	 * {@link ResourceSet}, so an unknown folder here means the request never
	 * reached a body handler; we fail defensively with 400 in that case.
	 */
	@Override
	public ResourceSet getResourceSet(ContainerRequestContext requestContext) {
		MultivaluedMap<String, String> pathParams = requestContext.getUriInfo().getPathParameters();
		String rootFolderName = pathParams.getFirst("rootFolderName");
		ResourceSetFactory resourceSetFactory = folderToResrouceSetFactoryMap.get(rootFolderName);
		if (resourceSetFactory == null) {
			throw new WebApplicationException(
					Response.status(Response.Status.BAD_REQUEST)
							.entity(String.format("ResourceSetFactory for Root Folder [%s] not found.",
									rootFolderName))
							.build());
		}
		return resourceSetFactory.createResourceSet();
	}

}
