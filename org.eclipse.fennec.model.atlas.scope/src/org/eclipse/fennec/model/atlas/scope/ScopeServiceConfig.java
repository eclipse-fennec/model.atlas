package org.eclipse.fennec.model.atlas.scope;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition
public @interface ScopeServiceConfig {

	@AttributeDefinition(cardinality = 1, required = true, description = "The scope name")
	String name();
	
	@AttributeDefinition(required = false, description = "The scope description")
	String description();
	
	@AttributeDefinition(required = false, description = "The parent scope", defaultValue = "atlas")
	String parent_scope() default "atlas";	
	

}
