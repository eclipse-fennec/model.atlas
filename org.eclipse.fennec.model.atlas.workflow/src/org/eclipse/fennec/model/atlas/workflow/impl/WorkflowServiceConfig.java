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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.workflow.impl;

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

	@AttributeDefinition(
			name = "Workflow ID", 
			description = "Unique identifier for this workflow instance"
			)
	String workflow_id();

	@AttributeDefinition(
			name = "Workflow Type", 
			description = "Type of this workflow. To help discriminating between package workflow and object storage workflow",
			required = false
			)
	String type() default "schemaWorkflow";

	@AttributeDefinition(
			name = "Description",
			required = false, 
			description = "The scope description")
	String description();    

	@AttributeDefinition(
			name = "Scope", 
			description = "Unique identifier for the scope this workflow should handle",
			required = true
			)
	String scope();	

	@AttributeDefinition(
			name = "Parent Scope",
			required = false, 
			description = "The parent scope", 
			defaultValue = "atlas")
	String parent_scope() default "atlas";	

	@AttributeDefinition(
			name = "Parent Workflow Service Target Filter",
			required = false, 
			description = "The parent workflow service target filter, to be able to delegate to the parent service when an object is not found in the current scope", 
			defaultValue = "atlas")
	String parentWorkflowService_target() default "(&(scope=atlas)(type=schemaWorkflow))";		

	@AttributeDefinition(
			name = "Delete After Transition",
			required = false, 
			description = "Whether or not to delete an object from the source stage if a transition to another target stage has been completed", 
			defaultValue = "false")
	boolean delete_after_transition() default false;	

	@AttributeDefinition(
			name = "Workflow Stages",
			required = false, 
			description = "The stages this workflow supports",
			defaultValue = {"draft", "approved", "release"}
			)
	String[] stages() default {"draft", "approved", "release"};

	@AttributeDefinition(
			name = "Workflow Final Stafe",
			required = false, 
			description = "The final stage for this workflow",
			defaultValue = "release"
			)
	String final_stage() default "release";

	@AttributeDefinition(
			name = "Workflow Writable Stages",
			required = false, 
			description = "The stages that can be writable for this workflow",
			defaultValue = {"draft", "approved", "release"}
			)
	String[] writable_stages() default {"draft", "approved", "release"};

	@AttributeDefinition(
			name = "Workflow Registries",
			required = false, 
			description = "The registries that are handled by this workflow",
			defaultValue = {"schema"}
			)
	String[] registries() default {"schema"};

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




}