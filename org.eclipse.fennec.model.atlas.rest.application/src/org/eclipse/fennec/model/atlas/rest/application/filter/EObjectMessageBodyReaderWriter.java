/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.rest.application.filter;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.codec.rest.jakartas.internal.BaseJakartaCodecMessageBodyReaderWriter;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.model.atlas.rest.common.ModelAtlasRestConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.JakartarsWhiteboardConstants;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationSelect;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

/**
 * 
 * @author ilenia
 * @since May 19, 2026
 */
@Component(
		service = {MessageBodyReader.class, MessageBodyWriter.class},
		enabled = true,
		scope = ServiceScope.SINGLETON
	)
@JakartarsExtension
@JakartarsName("EObjectMessageBodyReaderWriter")
@JakartarsApplicationSelect("(|(emf=true)("+ JakartarsWhiteboardConstants.JAKARTA_RS_NAME + "=.default))")
@Provider
@Produces(MediaType.WILDCARD)
@Consumes(MediaType.WILDCARD)
public class EObjectMessageBodyReaderWriter<R extends EObject, W extends EObject> extends BaseJakartaCodecMessageBodyReaderWriter<R, W>{

	@Context
    private ContainerRequestContext requestContext;
	
	private ResourceSetFactory resourceSetFactory;
	
	@Activate() 
	public void activate(){
		resourceSetFactory = getResolvedResourceSet();
	}
	
	
s

	private ResourceSetFactory  getResolvedResourceSet() {
        return (ResourceSetFactory) requestContext.getProperty(ModelAtlasRestConstants.RESOLVED_RESOURCE_SET_FACTORY);
    }

	
}
