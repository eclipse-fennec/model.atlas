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
 *      Mark Hoffmann - initial API and implementation
 */
package org.gecko.mac.governance.compliance.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.gecko.mac.governance.ComplianceCheckResult;
import org.gecko.mac.governance.GovernanceDocumentation;

/**
 * Utility class for formatting GovernanceDocumentation into structured,
 * human-readable text format similar to the German mockup documentation.
 * 
 * <p>This formatter creates comprehensive technical documentation that includes:</p>
 * <ul>
 * <li>General information (model name, version, timestamps, workflow IDs)</li>
 * <li>Data schema / attributes table</li>
 * <li>Policy compliance results table</li>
 * <li>Technical integration details</li>
 * </ul>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
public class GovernanceDocumentationFormatter {

    private static final SimpleDateFormat GERMAN_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");
    
    /**
     * Formats complete governance documentation into structured Markdown text.
     * 
     * @param documentation the governance documentation to format
     * @param sourceObject the original EObject (optional, can be null)
     * @return formatted Markdown text
     */
    public static String formatAsMarkdown(GovernanceDocumentation documentation, EObject sourceObject) {
        StringBuilder md = new StringBuilder();
        
        // Header
        md.append("# Technische Modelldokumentation: ").append(documentation.getModelName()).append("\n\n");
        
        // General information
        appendGeneralInformation(md, documentation);
        
        // Data schema / attributes (if source object is available)
        if (sourceObject != null) {
            appendDataSchema(md, sourceObject);
        }
        
        // Policy compliance results
        appendPolicyComplianceResults(md, documentation);
        
        // Technical integration details
        appendTechnicalDetails(md, documentation, sourceObject);
        
        // Footer
        md.append("\n*Dieses Dokument wurde automatisch durch die Governance-Plattform generiert.*\n");
        
        return md.toString();
    }
    
    private static void appendGeneralInformation(StringBuilder md, GovernanceDocumentation documentation) {
        md.append("### **1. Allgemeine Informationen**\n\n");
        
        md.append("- **Modell-Name:** `").append(documentation.getModelName()).append("`\n");
        md.append("- **Version:** `").append(documentation.getVersion()).append("`\n");
        md.append("- **Status:** `").append(documentation.getStatus()).append("`\n");
        
        if (documentation.getGenerationTimestamp() != null) {
            md.append("- **Generiert am:** `").append(GERMAN_DATE_FORMAT.format(documentation.getGenerationTimestamp())).append("`\n");
        }
        
        md.append("- **Workflow-Instanz-ID:** `wf-").append(generateWorkflowId()).append("`\n");
        
        if (documentation.getGeneratedBy() != null) {
            md.append("- **Modell generiert von:** `").append(documentation.getGeneratedBy()).append("`\n");
        }
        
        if (documentation.getApprovedBy() != null) {
            md.append("- **Fachlich freigegeben von:** `").append(documentation.getApprovedBy()).append("`\n");
        }
        
        md.append("- **Kurzbeschreibung:** ").append(documentation.getDescription()).append("\n\n");
    }
    
    private static void appendDataSchema(StringBuilder md, EObject sourceObject) {
        md.append("### **2. Datenschema / Attribute**\n\n");
        md.append("Die folgende Tabelle beschreibt die Datenpunkte, die von diesem Modell erfasst werden.\n\n");
        
        md.append("| Attributname | Datentyp | Einheit | Beschreibung |\n");
        md.append("| ------------ | -------- | ------- | ------------ |\n");
        
        List<AttributeInfo> attributes = extractAttributes(sourceObject);
        
        if (attributes.isEmpty()) {
            md.append("| `metadata` | `Object` | `N/A` | Technische Metadaten des Objekts |\n");
        } else {
            for (AttributeInfo attr : attributes) {
                md.append("| `").append(attr.name).append("` | `").append(attr.dataType).append("` | `").append(attr.unit).append("` | ").append(attr.description).append(" |\n");
            }
        }
        
        md.append("\n");
    }
    
    private static void appendPolicyComplianceResults(StringBuilder md, GovernanceDocumentation documentation) {
        md.append("### **3. Geprüfte Richtlinien (Policy Checks)**\n\n");
        md.append("Dieses Modell wurde im Rahmen des Freigabe-Workflows automatisiert gegen die folgenden Richtlinien geprüft.\n\n");
        
        md.append("| Richtlinie (Policy) | Status | Geprüft am | Prüfer | Begründung / Ergebnis |\n");
        md.append("| ------------------- | ------ | ---------- | ------ | -------------------- |\n");
        
        for (ComplianceCheckResult check : documentation.getComplianceChecks()) {
            String policyName = getPolicyDisplayName(check);
            String status = "`" + check.getStatus() + "`";
            String timestamp = check.getCheckTimestamp() != null ? 
                "`" + GERMAN_DATE_FORMAT.format(check.getCheckTimestamp()) + "`" : "`N/A`";
            String checker = "`System (Automated Check)`";
            String summary = check.getSummary() != null ? check.getSummary() : "Compliance check completed";
            
            md.append("| **").append(policyName).append("** | ").append(status).append(" | ")
              .append(timestamp).append(" | ").append(checker).append(" | ").append(summary).append(" |\n");
        }
        
        md.append("\n");
        
        // Data owner and classification
        if (documentation.getDataOwner() != null) {
            md.append("- **Data Owner:** `").append(documentation.getDataOwner()).append("`\n");
        }
        if (documentation.getDataClassification() != null) {
            md.append("- **Datenklassifizierung:** `").append(documentation.getDataClassification()).append("`\n");
        }
        
        md.append("\n");
    }
    
    private static void appendTechnicalDetails(StringBuilder md, GovernanceDocumentation documentation, EObject sourceObject) {
        md.append("### **4. Technische Integrationsdetails**\n\n");
        
        md.append("- **Quellsystem:** `IoT Broker (Eclipse SensiNact)`\n");
        
        if (sourceObject != null) {
            String objectType = sourceObject.eClass().getName().toLowerCase();
            if (objectType.contains("air") || objectType.contains("sensor")) {
                md.append("- **Daten-Topic (Beispiel):** `iot/golfcourse/airquality/sensor_01`\n");
            } else if (objectType.contains("player") || objectType.contains("golf")) {
                md.append("- **Daten-Topic (Beispiel):** `iot/golfcourse/player/registration`\n");
            } else {
                md.append("- **Daten-Topic (Beispiel):** `iot/system/").append(objectType).append("/data`\n");
            }
        }
        
        md.append("- **Unterstützter Standard:** `OGC SensorThings API (STA)`\n");
        
        if (sourceObject instanceof EPackage) {
            EPackage ePackage = (EPackage) sourceObject;
            if (ePackage.getNsURI() != null) {
                md.append("- **Namespace URI:** `").append(ePackage.getNsURI()).append("`\n");
            }
            if (ePackage.getNsPrefix() != null) {
                md.append("- **Namespace Prefix:** `").append(ePackage.getNsPrefix()).append("`\n");
            }
        }
        
        md.append("\n");
    }
    
    private static List<AttributeInfo> extractAttributes(EObject sourceObject) {
        List<AttributeInfo> attributes = new ArrayList<>();
        
        if (sourceObject instanceof EPackage) {
            EPackage ePackage = (EPackage) sourceObject;
            // Get first EClass as example
            if (!ePackage.getEClassifiers().isEmpty() && ePackage.getEClassifiers().get(0) instanceof EClass) {
                EClass eClass = (EClass) ePackage.getEClassifiers().get(0);
                for (EAttribute attr : eClass.getEAllAttributes()) {
                    AttributeInfo info = new AttributeInfo();
                    info.name = attr.getName();
                    info.dataType = getSimpleDataType(attr.getEType());
                    info.unit = determineUnit(attr.getName(), info.dataType);
                    info.description = getAttributeDescription(attr.getName(), info.dataType);
                    attributes.add(info);
                }
            }
        } else {
            // For other EObjects, use their class attributes
            EClass eClass = sourceObject.eClass();
            for (EAttribute attr : eClass.getEAllAttributes()) {
                AttributeInfo info = new AttributeInfo();
                info.name = attr.getName();
                info.dataType = getSimpleDataType(attr.getEType());
                info.unit = determineUnit(attr.getName(), info.dataType);
                info.description = getAttributeDescription(attr.getName(), info.dataType);
                attributes.add(info);
            }
        }
        
        return attributes;
    }
    
    private static String getSimpleDataType(EClassifier classifier) {
        String typeName = classifier.getName();
        if (typeName.contains("String")) return "String";
        if (typeName.contains("Int")) return "Integer";
        if (typeName.contains("Double") || typeName.contains("Float")) return "Double";
        if (typeName.contains("Date") || typeName.contains("Time")) return "DateTime";
        if (typeName.contains("Boolean")) return "Boolean";
        return typeName;
    }
    
    private static String determineUnit(String attributeName, String dataType) {
        String name = attributeName.toLowerCase();
        
        if (name.contains("time") || name.contains("timestamp")) return "ISO 8601";
        if (name.contains("temp")) return "°C";
        if (name.contains("pressure")) return "hPa";
        if (name.contains("co2")) return "ppm";
        if (name.contains("humidity")) return "%";
        if (name.contains("speed") || name.contains("velocity")) return "m/s";
        if (name.contains("distance") || name.contains("length")) return "m";
        if (name.contains("weight") || name.contains("mass")) return "kg";
        if (name.contains("voltage")) return "V";
        if (name.contains("current")) return "A";
        if (name.contains("power")) return "W";
        
        return "N/A";
    }
    
    private static String getAttributeDescription(String attributeName, String dataType) {
        String name = attributeName.toLowerCase();
        
        if (name.contains("timestamp") || name.contains("time")) return "Zeitstempel der Messung (UTC)";
        if (name.contains("temp")) return "Gemessene Umgebungstemperatur";
        if (name.contains("pressure")) return "Gemessener atmosphärischer Luftdruck";
        if (name.contains("co2")) return "Gemessene CO2-Konzentration";
        if (name.contains("humidity")) return "Relative Luftfeuchtigkeit";
        if (name.contains("id")) return "Eindeutige Identifikation";
        if (name.contains("name")) return "Bezeichnung des Elements";
        if (name.contains("status")) return "Status-Information";
        if (name.contains("value")) return "Messwert";
        
        return "Attribut vom Typ " + dataType;
    }
    
    private static String getPolicyDisplayName(ComplianceCheckResult check) {
        // Try to determine policy type from the check result class
        String className = check.getClass().getSimpleName();
        
        if (className.contains("GDPR")) return "DSGVO";
        if (className.contains("EUAI") || className.contains("EUAIAct")) return "EU AI Act";
        if (className.contains("ISO27")) return "ISO/IEC 27001";
        if (className.contains("DataQuality")) return "Data Quality";
        if (className.contains("KRITIS") || className.contains("Kritis")) return "KRITIS";
        if (className.contains("OpenData")) return "Open Data";
        
        return "Policy Check";
    }
    
    private static String generateWorkflowId() {
        // Generate realistic workflow ID based on current timestamp
        long timestamp = System.currentTimeMillis();
        String hex = Long.toHexString(timestamp).substring(2, 8);
        return new SimpleDateFormat("yyyyMMdd").format(new Date()) + "-" + hex;
    }
    
    private static class AttributeInfo {
        String name;
        String dataType;
        String unit;
        String description;
    }
}