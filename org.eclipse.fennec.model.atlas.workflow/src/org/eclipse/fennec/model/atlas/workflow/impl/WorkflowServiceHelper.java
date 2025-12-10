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
package org.eclipse.fennec.model.atlas.workflow.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery;
import org.osgi.util.promise.Promise;

/**
 * 
 * @author ilenia
 * @since Dec 9, 2025
 */
public class WorkflowServiceHelper {
	
	private static final ManagementFactory managementFactory = ManagementFactory.eINSTANCE;
	
	public static void requireTrue(boolean value, String message) {
		if(value) return;
		throw new IllegalStateException(message);
	}
	
	public static boolean areStagesSubsequent(WorkflowServiceConfig config, String fromStage, String toStage) {
		int fromIndex = List.of(config.stages()).indexOf(fromStage);
		int toIndex = List.of(config.stages()).indexOf(toStage);
		return (toIndex - fromIndex) == 1;
	}
	
	public static boolean isStageAllowed(WorkflowServiceConfig config, String stage) {
		return List.of(config.stages()).contains(stage);
	}
	
	public static boolean isStageWritable(WorkflowServiceConfig config, String stage) {
		return List.of(config.writable_stages()).contains(stage);
	}
	
	public static boolean isRegistryAllowed(WorkflowServiceConfig config, String registry) {
		return List.of(config.registries()).contains(registry);
	}

	/**
	 * Helper method to unwrap Promise results with proper exception handling
	 */
	public static <R> R getPromiseValue(Promise<R> promise) {
		try {
			return promise.getValue();
		} catch (InvocationTargetException | InterruptedException e) {
			throw new RuntimeException("Promise execution failed", e);
		}
	}


	public static ObjectQuery createQuery(Map<EStructuralFeature, Object> queryValueMap) {
		ObjectQuery query = managementFactory.createObjectQuery();
		queryValueMap.forEach((k,v) -> {
			query.eSet(k, v);
		});
		return query;
	}
}
