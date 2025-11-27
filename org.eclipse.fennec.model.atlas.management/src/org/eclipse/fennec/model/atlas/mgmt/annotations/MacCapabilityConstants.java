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
package org.eclipse.fennec.model.atlas.mgmt.annotations;

/**
 * Constants for OSGi capabilities and requirements in the Model Atlas Cloud ecosystem.
 * 
 * <p>This interface centralizes all namespace and capability names used across
 * the MAC projects to ensure consistency and avoid magic strings.</p>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
public interface MacCapabilityConstants {
    
    // === NAMESPACES ===
    
    /**
     * Namespace for Model Atlas Cloud management capabilities.
     * Used for storage services, registry services, and core management functionality.
     */
    String NAMESPACE_MAC_MANAGEMENT = "mac.management";
    
    /**
     * Namespace for Model Atlas Cloud governance capabilities.
     * Used for workflow services, compliance checking, and governance processes.
     */
    String NAMESPACE_MAC_GOVERNANCE = "mac.governance";
    
    /**
     * Namespace for Model Atlas Cloud AI capabilities.
     * Used for AI model generation, chat completion, and intelligent services.
     */
    String NAMESPACE_MAC_AI = "mac.ai";
    
    /**
     * Namespace for Model Atlas Cloud IoT capabilities.
     * Used for sensor integration, MQTT handling, and IoT data processing.
     */
    String NAMESPACE_MAC_IOT = "mac.iot";
    
    // === MANAGEMENT CAPABILITIES ===
    
    /**
     * Capability name for EObject storage services.
     * Provided by file storage, MinIO storage, and other storage implementations.
     */
    String CAP_EOBJECT_STORAGE = "EObjectStorage";
    
    /**
     * Capability name for EObject registry services.
     * Provided by Lucene registry and other registry implementations.
     */
    String CAP_EOBJECT_REGISTRY = "EObjectRegistry";
    
    /**
     * Capability name for file-based storage services.
     * Provided by file storage implementations with Lucene indexing.
     */
    String CAP_FILE_STORAGE = "FileStorage";
    
    /**
     * Capability name for cloud storage services.
     * Provided by MinIO and other cloud storage implementations.
     */
    String CAP_CLOUD_STORAGE = "CloudStorage";
    
    // === GOVERNANCE CAPABILITIES ===
    
    /**
     * Capability name for EObject workflow services.
     * Provided by workflow implementations handling draft→approval→release cycles.
     */
    String CAP_EOBJECT_WORKFLOW = "EObjectWorkflow";
    
    /**
     * Capability name for governance compliance services.
     * Provided by compliance checkers supporting multiple policy frameworks.
     */
    String CAP_GOVERNANCE_COMPLIANCE = "GovernanceCompliance";
    
    /**
     * Capability name for multi-policy compliance checking.
     * Provided by services supporting EU AI Act, GDPR, ISO27001, KRITIS, etc.
     */
    String CAP_MULTI_POLICY_COMPLIANCE = "MultiPolicyCompliance";
    
    // === AI CAPABILITIES ===
    
    /**
     * Capability name for AI model generation services.
     * Provided by services that generate EMF models from unknown data structures.
     */
    String CAP_AI_MODEL_GENERATION = "AIModelGeneration";
    
    /**
     * Capability name for chat completion services.
     * Provided by OpenAI, Gemini, and other chat completion implementations.
     */
    String CAP_CHAT_COMPLETION = "ChatCompletion";
    
    /**
     * Capability name for JSON fingerprinting services.
     * Provided by services that generate structural fingerprints for deduplication.
     */
    String CAP_JSON_FINGERPRINTING = "JsonFingerprinting";
    
    // === IOT CAPABILITIES ===
    
    /**
     * Capability name for MQTT integration services.
     * Provided by MQTT-to-SensiNact bridges and message handlers.
     */
    String CAP_MQTT_INTEGRATION = "MqttIntegration";
    
    /**
     * Capability name for SensiNact integration services.
     * Provided by sensor mapping and digital twin services.
     */
    String CAP_SENSINACT_INTEGRATION = "SensiNactIntegration";
    
    /**
     * Capability name for LoRaWAN protocol support.
     * Provided by services handling LoRaWAN uplink message processing.
     */
    String CAP_LORAWAN_PROTOCOL = "LoRaWanProtocol";
    
    // === VERSION CONSTANTS ===
    
    /**
     * Current version for MAC management capabilities.
     */
    String VERSION_MANAGEMENT = "1.0";
    
    /**
     * Current version for MAC governance capabilities.
     */
    String VERSION_GOVERNANCE = "1.0";
    
    /**
     * Current version for MAC AI capabilities.
     */
    String VERSION_AI = "1.0";
    
    /**
     * Current version for MAC IoT capabilities.
     */
    String VERSION_IOT = "1.0";
}