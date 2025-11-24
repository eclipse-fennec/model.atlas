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
package org.eclipse.fennec.model.atlas.model.documentation.provider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationOptions;
import org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

/**
 * 
 * @author ilenia
 * @since Oct 20, 2022
 */
@Component(name = "ModelDocumentationProvider", service = ModelDocumentationProvider.class, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = ModelDocumentationProviderConfig.class)
public class ModelDocumentationProvider {

	@Reference(target = "(component.name=EcoreToHtmlComponent)")
	EcoreToDocumentationService ecoreToHtmlComponent;

	@Reference(target = "(component.name=EcoreToMarkdownComponent)")
	EcoreToDocumentationService ecoreToMdComponent;

	ModelDocumentationProviderConfig config;

	@Activate
	public void activate(ModelDocumentationProviderConfig config) throws ConfigurationException {
		this.config = config;
	}

	public ModelDocumentationProviderConfig getConfig() {
		return config;
	}

	public boolean hasEPackageChanged(EPackage ePackage) {
		Path hashCodeFilePath = Paths.get(config.output_root_folder(), config.output_package_hash_code_folder(), 
				ePackage.getName().concat(ModelDocumentationConstants.TXT_FILE_EXTENSION));
		if(!Files.exists(hashCodeFilePath)) {
			return true;
		}
		try (BufferedReader br = new BufferedReader(new FileReader(hashCodeFilePath.toFile()));) {
			String oldHashCode = br.readLine();
			if(!oldHashCode.equals(String.valueOf(ePackage.hashCode()))) {
				return true; 
			}
		} catch(IOException e) {
			e.printStackTrace();
			return true;
		}
		return false;
	}

	public Map<String, String> generateAllPackageDocumentation(EPackage ePackage) {
		System.out.println("Generating Package Docs for " + ePackage.getName());
		generateAllMarkdownDocumentation(ePackage);
		generateAllHtmlDocumentation(ePackage);
		updateHashCodeFile(ePackage);
		Map<String, String> documentationFileMap = createDocumentationFileMap(ePackage);
		return documentationFileMap;
	}


	public Map<String, String> generateAllClassesDocumentation(EPackage ePackage) {
		Map<String, String> classDocFileMap = new HashMap<>();
		List<EClass> eClasses = ePackage.getEClassifiers().stream()
				.filter(c -> c instanceof EClass).map(c -> (EClass) c).collect(Collectors.toList());
		eClasses.stream().forEach(c -> {
			generateAllMarkdownDocumentation(c);
			generateAllHtmlDocumentation(c);
			classDocFileMap.putAll(createDocumentationFileMap(c));
		});
		return classDocFileMap;
	}
	

	public void generateAllHtmlDocumentation(EPackage ePackage) {
		generateDocumentation(ePackage, EcoreToDocumentationOptions.ONLY_HTML_CLASS_OVERVIEW);
		generateDocumentation(ePackage, EcoreToDocumentationOptions.HTML_WITH_MERMAID_CLASS_DIAGRAM);
	}

	public void generateAllHtmlDocumentation(EClass eClass) {
		generateDocumentation(eClass, EcoreToDocumentationOptions.ONLY_HTML_CLASS_OVERVIEW);
		generateDocumentation(eClass, EcoreToDocumentationOptions.HTML_WITH_MERMAID_CLASS_DIAGRAM);
	}
	
	public OutputStream generateDocumentation(EPackage ePackage, EcoreToDocumentationOptions option) {
		OutputStream os = null;
		try {
			switch(option) {
			case HTML_WITH_MERMAID_CLASS_DIAGRAM: case HTML_WITH_PLANTUML_CLASS_DIAGRAM: case ONLY_HTML_CLASS_OVERVIEW:
				return ecoreToHtmlComponent.doGenerateDocumentation(ePackage, option, config.output_root_folder());		
			case MARKDOWN_WITH_MERMAID_CLASS_DIAGRAM: case MARKDOWN_WITH_PLANTUML_CLASS_DIAGRAM: case ONLY_MARKDOWN_CLASS_OVERVIEW:
				return ecoreToMdComponent.doGenerateDocumentation(ePackage, option, config.output_root_folder());
			default:
				return os;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return os;
	}

	public OutputStream generateDocumentation(EClass eClass, EcoreToDocumentationOptions option) {
		OutputStream os = null;
		try {
			switch(option) {
			case HTML_WITH_MERMAID_CLASS_DIAGRAM: case HTML_WITH_PLANTUML_CLASS_DIAGRAM: case ONLY_HTML_CLASS_OVERVIEW:
				return ecoreToHtmlComponent.doGenerateDocumentation(eClass, option, config.output_root_folder());		
			case MARKDOWN_WITH_MERMAID_CLASS_DIAGRAM: case MARKDOWN_WITH_PLANTUML_CLASS_DIAGRAM: case ONLY_MARKDOWN_CLASS_OVERVIEW:
				return ecoreToMdComponent.doGenerateDocumentation(eClass, option, config.output_root_folder());
			default:
				return os;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return os;		
	}
	
	public OutputStream retrieveDocumentation(String docFilePath, boolean generateIfNotFound, EPackage ePackage, EcoreToDocumentationOptions docOption) {
		OutputStream os = retrieveDocumentationFile(docFilePath);
		if(os == null && generateIfNotFound) {
			os = generateDocumentation(ePackage, docOption);
		}
		return os;
	}
	
	public OutputStream retrieveDocumentation(String docFilePath, boolean generateIfNotFound, EClass eClass, EcoreToDocumentationOptions docOption) {
		OutputStream os = retrieveDocumentationFile(docFilePath);
		if(os == null && generateIfNotFound) {
			os = generateDocumentation(eClass, docOption);
		}
		return os;
	}
	
	private void generateAllMarkdownDocumentation(EPackage ePackage) {
		generateDocumentation(ePackage, EcoreToDocumentationOptions.ONLY_MARKDOWN_CLASS_OVERVIEW);
		generateDocumentation(ePackage, EcoreToDocumentationOptions.MARKDOWN_WITH_MERMAID_CLASS_DIAGRAM);
		generateDocumentation(ePackage, EcoreToDocumentationOptions.MARKDOWN_WITH_PLANTUML_CLASS_DIAGRAM);

	}

	private void generateAllMarkdownDocumentation(EClass eClass) {
		generateDocumentation(eClass, EcoreToDocumentationOptions.ONLY_MARKDOWN_CLASS_OVERVIEW);
		generateDocumentation(eClass, EcoreToDocumentationOptions.MARKDOWN_WITH_MERMAID_CLASS_DIAGRAM);
		generateDocumentation(eClass, EcoreToDocumentationOptions.MARKDOWN_WITH_PLANTUML_CLASS_DIAGRAM);
	}
	
	private OutputStream retrieveDocumentationFile(String docFilePath) {
		File docFile = new File(docFilePath);
		try {
			if(docFile.exists()) {
				try(InputStream is = new FileInputStream(docFile); OutputStream os = new ByteArrayOutputStream();) {
					os.write(is.readAllBytes());			
					return os;
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;		
	}

	private void updateHashCodeFile(EPackage ePackage) {
		Path hashCodeFilePath = Paths.get(config.output_root_folder(), config.output_package_hash_code_folder(), 
				ePackage.getName().concat(ModelDocumentationConstants.TXT_FILE_EXTENSION));
		File hashCodeFile = hashCodeFilePath.toFile();
		try {
			if(!hashCodeFile.exists()) {
				Files.createDirectories(hashCodeFilePath.getParent());
				Files.createFile(hashCodeFilePath);
			}
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(hashCodeFile))) {
				System.out.println("Hash code " + ePackage.hashCode());
				bw.write(String.valueOf(ePackage.hashCode()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}



	private Map<String, String> createDocumentationFileMap(EPackage ePackage) {
		Map<String, String> documentationFileMap = new HashMap<>();
		documentationFileMap.put(ModelDocumentationConstants.PROPERTY_MD_FILE, 
				Paths.get(config.output_root_folder(), config.output_md_folder(), 
						ePackage.getName().concat(ModelDocumentationConstants.MD_FILE_EXTENSION)).toString());

		documentationFileMap.put(ModelDocumentationConstants.PROPERTY_MD_MERMAID_FILE, 
				Paths.get(config.output_root_folder(), config.output_md_mermaid_folder(), 
						ePackage.getName().concat(ModelDocumentationConstants.MD_FILE_EXTENSION)).toString());

		documentationFileMap.put(ModelDocumentationConstants.PROPERTY_MD_PLANTUML_FILE, 
				Paths.get(config.output_root_folder(), config.output_md_plantuml_folder(), 
						ePackage.getName().concat(ModelDocumentationConstants.MD_FILE_EXTENSION)).toString());

		documentationFileMap.put(ModelDocumentationConstants.PROPERTY_HTML_FILE, 
				Paths.get(config.output_root_folder(), config.output_html_folder(), 
						ePackage.getName().concat(ModelDocumentationConstants.HTML_FILE_EXTENSION)).toString());

		documentationFileMap.put(ModelDocumentationConstants.PROPERTY_HTML_MERMAID_FILE, 
				Paths.get(config.output_root_folder(), config.output_html_mermaid_folder(), 
						ePackage.getName().concat(ModelDocumentationConstants.HTML_FILE_EXTENSION)).toString());

		documentationFileMap.put(ModelDocumentationConstants.PROPERTY_HASH_CODE_FILE, 
				Paths.get(config.output_root_folder(), config.output_package_hash_code_folder(), 
						ePackage.getName().concat(ModelDocumentationConstants.TXT_FILE_EXTENSION)).toString());
		return documentationFileMap;
	}

	private Map<String, String> createDocumentationFileMap(EClass eClass) {
		Map<String, String> documentationFileMap = new HashMap<>();
		documentationFileMap.put(ModelDocumentationConstants.PROPERTY_MD_FILE.concat(".").concat(eClass.getName()), 
				Paths.get(config.output_root_folder(), config.output_md_folder(), 
						eClass.getEPackage().getName().concat("_").concat(eClass.getName()).concat(ModelDocumentationConstants.MD_FILE_EXTENSION)).toString());

		documentationFileMap.put(ModelDocumentationConstants.PROPERTY_MD_MERMAID_FILE.concat(".").concat(eClass.getName()), 
				Paths.get(config.output_root_folder(), config.output_md_mermaid_folder(), 
						eClass.getEPackage().getName().concat("_").concat(eClass.getName()).concat(ModelDocumentationConstants.MD_FILE_EXTENSION)).toString());

		documentationFileMap.put(ModelDocumentationConstants.PROPERTY_MD_PLANTUML_FILE.concat(".").concat(eClass.getName()), 
				Paths.get(config.output_root_folder(), config.output_md_plantuml_folder(), 
						eClass.getEPackage().getName().concat("_").concat(eClass.getName()).concat(ModelDocumentationConstants.MD_FILE_EXTENSION)).toString());

		documentationFileMap.put(ModelDocumentationConstants.PROPERTY_HTML_FILE.concat(".").concat(eClass.getName()), 
				Paths.get(config.output_root_folder(), config.output_html_folder(), 
						eClass.getEPackage().getName().concat("_").concat(eClass.getName()).concat(ModelDocumentationConstants.HTML_FILE_EXTENSION)).toString());

		documentationFileMap.put(ModelDocumentationConstants.PROPERTY_HTML_MERMAID_FILE.concat(".").concat(eClass.getName()), 
				Paths.get(config.output_root_folder(), config.output_html_mermaid_folder(), 
						eClass.getEPackage().getName().concat("_").concat(eClass.getName()).concat(ModelDocumentationConstants.HTML_FILE_EXTENSION)).toString());
		return documentationFileMap;
	}
}
