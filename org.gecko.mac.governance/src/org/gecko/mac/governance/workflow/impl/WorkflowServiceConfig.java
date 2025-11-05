/*
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
package org.gecko.mac.governance.workflow.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Configuration interface for EObjectWorkflowService factory instances.
 * 
 * This configuration allows multiple workflow service instances with different
 * storage backend configurations and workflow policies.
 */
@ObjectClassDefinition(
    name = "EObject Workflow Service Configuration",
    description = "Configuration for EObject workflow management with configurable storage providers"
)
public @interface WorkflowServiceConfig {
    
    /**
     * Unique identifier for this workflow instance.
     * Used for service registration and logging.
     * 
     * @return the workflow instance ID
     */
    @AttributeDefinition(
        name = "Workflow ID", 
        description = "Unique identifier for this workflow instance",
        required = true
    )
    String workflow_id() default "default";
    
    /**
     * OSGi filter for the draft storage service.
     * This allows selecting specific storage backends for draft objects.
     * 
     * Examples:
     * - "(storage.backend=file)" - Use file-based storage for drafts
     * - "(storage.backend=minio)" - Use MinIO storage for drafts
     * - "(&(storage.backend=file)(storage.workspace=/drafts))" - Specific file storage configuration
     * 
     * @return OSGi filter for draft storage service
     */
    @AttributeDefinition(
        name = "Draft Storage Filter",
        description = "OSGi filter to select the storage service for draft objects",
        required = true
    )
    String draft_storage_filter();
    
    /**
     * OSGi filter for the approved storage service.
     * This allows selecting different storage backends for approved objects.
     * 
     * Examples:
     * - "(storage.backend=minio)" - Use MinIO storage for approved objects
     * - "(storage.backend=git)" - Use Git-based storage for approved objects
     * - "(&(storage.backend=file)(storage.workspace=/approved))" - Specific file storage configuration
     * 
     * @return OSGi filter for approved storage service
     */
    @AttributeDefinition(
        name = "Approved Storage Filter",
        description = "OSGi filter to select the storage service for approved objects",
        required = true
    )
    String approved_storage_filter();
    
    /**
     * OSGi filter for the governance documentation storage service.
     * If not specified, governance documentation will not be stored separately.
     * 
     * Examples:
     * - "(storage.backend=file)" - Store governance docs in file storage
     * - "(storage.backend=minio)" - Store governance docs in MinIO
     * 
     * @return OSGi filter for governance documentation storage service
     */
    @AttributeDefinition(
        name = "Documentation Storage Filter",
        description = "OSGi filter to select the storage service for governance documentation (optional)",
        required = false
    )
    String documentation_storage_filter() default "";
    
    /**
     * Whether to archive drafts after approval instead of deleting them.
     * 
     * When true:
     * - Draft objects are marked as ARCHIVED after approval
     * - Original draft remains accessible for audit purposes
     * 
     * When false:
     * - Draft objects are deleted after successful approval
     * - Saves storage space but loses audit trail
     * 
     * @return true to archive drafts, false to delete them
     */
    @AttributeDefinition(
        name = "Archive Drafts on Approval",
        description = "Whether to archive drafts after approval instead of deleting them",
        type = AttributeType.BOOLEAN
    )
    boolean archive_drafts_on_approval() default true;
    
    /**
     * Timeout in milliseconds for transactional operations.
     * 
     * This timeout applies to:
     * - Object locking during transactional operations
     * - Copy operations between storage backends
     * - Rollback operations
     * 
     * @return timeout in milliseconds
     */
    @AttributeDefinition(
        name = "Transaction Timeout (ms)",
        description = "Timeout in milliseconds for transactional operations",
        type = AttributeType.LONG,
        min = "1000",
        max = "300000"
    )
    long transaction_timeout_ms() default 30000L;
    
    /**
     * Whether to enable automatic rollback on failure.
     * 
     * When true:
     * - Failed approval operations trigger automatic rollback
     * - Original object state is restored in draft storage
     * - Partial changes in approved storage are cleaned up
     * 
     * When false:
     * - Failed operations leave objects in inconsistent state
     * - Manual intervention required for cleanup
     * - Useful for debugging complex failure scenarios
     * 
     * @return true to enable automatic rollback
     */
    @AttributeDefinition(
        name = "Enable Auto Rollback",
        description = "Whether to enable automatic rollback on transactional failures",
        type = AttributeType.BOOLEAN
    )
    boolean enable_auto_rollback() default true;
    
    /**
     * Whether to require governance compliance checks before approval.
     * 
     * When true:
     * - Compliance checks are mandatory before object approval
     * - Approval fails if compliance service is unavailable
     * - Governance documentation is automatically generated
     * 
     * When false:
     * - Compliance checks are optional
     * - Objects can be approved without governance validation
     * - Useful for non-critical or internal objects
     * 
     * @return true to require compliance checks
     */
    @AttributeDefinition(
        name = "Require Compliance Checks",
        description = "Whether to require governance compliance checks before approval",
        type = AttributeType.BOOLEAN
    )
    boolean require_compliance_checks() default false;
    
    /**
     * Maximum number of concurrent approval operations.
     * 
     * Limits the number of simultaneous approval operations to prevent
     * resource exhaustion and ensure stable performance.
     * 
     * @return maximum concurrent approvals
     */
    @AttributeDefinition(
        name = "Max Concurrent Approvals",
        description = "Maximum number of concurrent approval operations",
        type = AttributeType.INTEGER,
        min = "1",
        max = "100"
    )
    int max_concurrent_approvals() default 10;
    
    /**
     * Whether to enable detailed operation logging.
     * 
     * When true:
     * - Detailed logs for all workflow operations
     * - Performance metrics and timing information
     * - Useful for debugging and monitoring
     * 
     * When false:
     * - Minimal logging for production performance
     * - Only errors and warnings are logged
     * 
     * @return true to enable detailed logging
     */
    @AttributeDefinition(
        name = "Enable Detailed Logging",
        description = "Whether to enable detailed operation logging for debugging",
        type = AttributeType.BOOLEAN
    )
    boolean enable_detailed_logging() default false;
    
    /**
     * Whether to automatically trigger compliance checks after draft upload.
     * 
     * When true:
     * - Compliance checks are automatically triggered when a draft is uploaded
     * - Governance documentation is generated for immediate review
     * - Streamlines the workflow for faster governance processing
     * 
     * When false:
     * - Compliance checks must be manually triggered
     * - Allows for draft refinement before compliance checking
     * - Useful for iterative development workflows
     * 
     * @return true to enable automatic compliance checks
     */
    @AttributeDefinition(
        name = "Enable Auto Compliance Check",
        description = "Whether to automatically trigger compliance checks after draft upload",
        type = AttributeType.BOOLEAN
    )
    boolean enable_auto_compliance_check() default true;
    
    /**
     * Whether to require approved governance documentation before object approval.
     * 
     * When true:
     * - Objects cannot be approved without approved governance documentation
     * - Approval process validates governance documentation status
     * - Enforces governance-gated workflow for compliance
     * 
     * When false:
     * - Objects can be approved without governance documentation
     * - Compliance checks are optional in the approval process
     * - Useful for non-regulated environments or internal tools
     * 
     * @return true to require approved governance documentation
     */
    @AttributeDefinition(
        name = "Require Approved Governance Documentation",
        description = "Whether to require approved governance documentation before object approval",
        type = AttributeType.BOOLEAN
    )
    boolean require_approved_governance_documentation() default true;
}