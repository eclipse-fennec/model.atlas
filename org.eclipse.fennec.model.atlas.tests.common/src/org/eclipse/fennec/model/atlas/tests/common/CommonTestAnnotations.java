package org.eclipse.fennec.model.atlas.tests.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.TemplateArgument;
import org.osgi.test.common.annotation.Property.Type;
import org.osgi.test.common.annotation.Property.ValueSource;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;

public class CommonTestAnnotations {
	
	public static final String PROP_TEMP_DIR = "tempDir";

	public static final String PID_SHARED_REGISTRY = "LuceneEObjectRegistryService";
	
	public static final String PID_FILE_STORAGE = "FileObjectStorage";
	
	public static final String PID_EPACKAGE_INDEX_SERVICE = "EPackageLuceneIndex";
	
	public static final String PID_REGISTRY_SERVICE = "RegistryService";
	
	public static final String FILE_STORAGE_FOLDER = "file-storage";
	
	public static final String SCHEMA_REGISTRY_NAME = "schema";
	
	public static final String STAGE_DRAFT = "draft";

	public static final String STAGE_APPROVED = "approved";

	public static final String STAGE_RELEASE = "release";

	@WithFactoryConfiguration(factoryPid = PID_SHARED_REGISTRY, name = "shared-registry", location = "?", properties = {
			@Property(key = "registry.workspace.folder", value = "%s/shared-registry", templateArguments = {
					@TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR) }),
			@Property(key = "registry", value = "main"),
			@Property(key = "storage.backend.tracking", value = "true"),
			@Property(key = "initial.index.capacity", value = "1000"),
			@Property(key = "enable.debug.logging", value = "true") })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface RegistryConfiguration {
	}

	@RegistryConfiguration
	@WithFactoryConfiguration(factoryPid = PID_FILE_STORAGE, name = "file-storage", location = "?", properties = {
			@Property(key = "workspace.folder", value = "%s/" + FILE_STORAGE_FOLDER, templateArguments = {
					@TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR) }),
			@Property(key = "storage.type", value = "file"),
			@Property(key = "registry.target", value = "(registry=main)")})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface StorageSetup {
	}
	
	@WithFactoryConfiguration(factoryPid = PID_EPACKAGE_INDEX_SERVICE, name = "epackage-index", location = "?", properties = {
			@Property(key = "index.folder", value = "%s/epackage-index", templateArguments = {
					@TemplateArgument(source = ValueSource.SystemProperty, value = PROP_TEMP_DIR) })})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface EPackageLuceneIndexSetup{
	}
	
	@EPackageLuceneIndexSetup
	@StorageSetup
	@WithFactoryConfiguration(factoryPid = PID_REGISTRY_SERVICE, name = SCHEMA_REGISTRY_NAME, location = "?", properties = {
			@Property(key = "registry.name", value = SCHEMA_REGISTRY_NAME),
			@Property(key = "registry.type", value = "SCHEMA"),
			@Property(key = "schema.uri", value = "http://www.eclipse.org/emf/2002/Ecore"),
			@Property(key = "root.eclass.uri", value = "http://www.eclipse.org/emf/2002/Ecore#//EPackage"),
			@Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
			@Property(key = "storageService.target", value = "(storage.type=file)" ),
			@Property(key = "registry.target", value = "(registry=main)"),
			@Property(key = "stages", type = Type.Array, value = {
					"{ \"name\" : \"" + STAGE_DRAFT    + "\", \"writable\" : true, \"final\": false}",
					"{ \"name\" : \"" + STAGE_APPROVED + "\", \"writable\" : true, \"final\": false}",                                                                                                                                                                                
					"{ \"name\" : \"" + STAGE_RELEASE  + "\", \"writable\" : true, \"final\": true}",
			}),                                                                                                                                                                                                                                                               
			@Property(key = "workflow.transitions", type = Type.Array,                                                                                                                                                                                                            
			value = { STAGE_DRAFT + ":" + STAGE_APPROVED, STAGE_APPROVED + ":" + STAGE_RELEASE }),
			@Property(key = "stage.storage.mappings", type = Type.Array, value = { STAGE_DRAFT +":file", STAGE_APPROVED+":file",
			STAGE_RELEASE+":file" })})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface SchemaRegistryServiceSetup {
	}
}
