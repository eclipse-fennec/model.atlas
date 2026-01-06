package org.eclipse.fennec.model.atlas.schema.registry.api;

import org.eclipse.emf.ecore.EClass;
import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface SchemaRegistryService {

	String getRegistryName();
	
	String getSchemaUri();
	
	EClass getRootEClass();
	
	boolean isCompatible(EClass eClass);

}
