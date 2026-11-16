/*
 * Copyright (c) 2012 - 2026 Data In Motion and others.
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
package org.eclipse.fennec.model.atlas.registry.impl;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.registry.api.RegistryAccessException;
import org.eclipse.fennec.model.atlas.registry.api.RegistryAccessException.Reason;
import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;
import org.eclipse.fennec.model.atlas.scope.api.ScopeRegistries;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.atlas.workflow.ScopeServiceCollector;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;

/**
 * How the reader and the writer find a scope, its schema registry and a package in it — one
 * place, so that both answer an unknown scope and a missing schema registry alike.
 */
final class ScopeLookup {

	private final ScopeServiceCollector scopeCollector;

	ScopeLookup(ScopeServiceCollector scopeCollector) {
		this.scopeCollector = scopeCollector;
	}

	/**
	 * @return the scope service of the named scope
	 * @throws RegistryAccessException {@link Reason#UNKNOWN_SCOPE} if there is none
	 */
	@SuppressWarnings("unchecked")
	WritableScopeService<EObject> scope(String scopeName) {
		WritableScopeService<?> scopeService = scopeCollector.getScopeServiceByScopeName(scopeName);
		if (scopeService == null) {
			throw new RegistryAccessException(Reason.UNKNOWN_SCOPE, "Scope [" + scopeName + "] not found.");
		}
		return (WritableScopeService<EObject>) scopeService;
	}

	/**
	 * The name of the scope's schema registry, resolved by its registry type rather than assumed
	 * to be {@code "schema"} (issue #179).
	 *
	 * @throws RegistryAccessException {@link Reason#NO_SCHEMA_REGISTRY} if the scope has none
	 */
	static String schemaRegistry(WritableScopeService<?> scopeService) {
		ScopeInfo scope = scopeService.getScopeInfo();
		return ScopeRegistries.schemaRegistryName(scope)
				.orElseThrow(() -> new RegistryAccessException(Reason.NO_SCHEMA_REGISTRY, String.format(
						"Scope [%s] has no schema registry.", scope == null ? "?" : scope.getName())));
	}

	/**
	 * The single package with the given nsURI in a stage, via the nsUri metadata property
	 * (hierarchy-aware, works for all storage backends). The create conflict check keeps the
	 * nsURI unique per (scope, registry, stage), so the lookup yields at most one element.
	 */
	static ObjectMetadata findByNsUri(WritableScopeService<?> scopeService, String registry, String stage,
			String nsUri) {
		List<ObjectMetadata> matches = scopeService.getMetadataByPropertyFromStageForRegistry(registry, stage,
				WorkflowConstants.NS_URI_METADATA_PROPERTY, nsUri);
		return matches.isEmpty() ? null : matches.get(0);
	}

	/** Final-stage variant of {@link #findByNsUri(WritableScopeService, String, String, String)}. */
	static ObjectMetadata findByNsUriInFinalStage(WritableScopeService<?> scopeService, String registry,
			String nsUri) {
		List<ObjectMetadata> matches = scopeService.getMetadataByPropertyFromFinalStageForRegistry(registry,
				WorkflowConstants.NS_URI_METADATA_PROPERTY, nsUri);
		return matches.isEmpty() ? null : matches.get(0);
	}
}
