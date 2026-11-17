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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageLuceneIndex.SearchHit;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageLuceneIndex.SearchResult;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageLuceneIndex;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageSearchQuery;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.registry.api.ObjectAddress;
import org.eclipse.fennec.model.atlas.registry.api.PackageAddress;
import org.eclipse.fennec.model.atlas.registry.api.PackageSearch;
import org.eclipse.fennec.model.atlas.registry.api.PackageSearchResult;
import org.eclipse.fennec.model.atlas.registry.api.RegistryApiFactory;
import org.eclipse.fennec.model.atlas.registry.api.RegistryReader;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.atlas.workflow.ScopeServiceCollector;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * The {@link RegistryReader} on top of the workflow's scope services and the package search
 * index (issue #386).
 */
@Component(service = RegistryReader.class)
public class RegistryReaderImpl implements RegistryReader {

	private final ScopeLookup lookup = new ScopeLookup();
	private final SearchIndex searchIndex = new SearchIndex();

	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	void bindScopeCollector(ScopeServiceCollector scopeCollector) {
		lookup.bind(scopeCollector);
	}

	void unbindScopeCollector(ScopeServiceCollector scopeCollector) {
		lookup.unbind(scopeCollector);
	}

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC,
			policyOption = ReferencePolicyOption.GREEDY)
	void bindEPackageIndex(EPackageLuceneIndex index) {
		searchIndex.bind(index);
	}

	void unbindEPackageIndex(EPackageLuceneIndex index) {
		searchIndex.unbind(index);
	}

	@Override
	public String schemaRegistry(String scope) {
		return ScopeLookup.schemaRegistry(lookup.scope(scope));
	}

	@Override
	public List<ObjectMetadata> listAll(String scope, String registry) {
		return lookup.scope(scope).listAllForRegistry(registry);
	}

	@Override
	public List<ObjectMetadata> listFinal(String scope, String registry) {
		return lookup.scope(scope).listInFinalStageForRegistry(registry);
	}

	@Override
	public List<ObjectMetadata> list(String scope, String registry, String stage) {
		return lookup.scope(scope).listInStageForRegistry(registry, stage);
	}

	@Override
	public List<ObjectMetadata> listByName(String scope, String registry, String stage, String namePattern) {
		return lookup.scope(scope).listInStageForRegistryByName(registry, stage, namePattern);
	}

	@Override
	public Optional<ObjectMetadata> metadata(ObjectAddress address) {
		return Optional.ofNullable(lookup.scope(address.getScope()).getMetadataFromStageForRegistry(address.getRegistry(),
				address.getStage(), address.getObjectId()));
	}

	@Override
	public Optional<ObjectMetadata> finalMetadata(String scope, String registry, String objectId) {
		return Optional.ofNullable(lookup.scope(scope).getMetadataFromFinalStageForRegistry(registry, objectId));
	}

	@Override
	public Optional<EObject> content(ObjectAddress address) {
		return Optional.ofNullable(lookup.scope(address.getScope()).getContentFromStageForRegistry(address.getRegistry(),
				address.getStage(), address.getObjectId()));
	}

	@Override
	public Optional<EObject> finalContent(String scope, String registry, String objectId) {
		return lookup.scope(scope).get(registry, objectId);
	}

	@Override
	public Optional<ObjectMetadata> packageMetadata(PackageAddress address) {
		WritableScopeService<EObject> scopeService = lookup.scope(address.getScope());
		return Optional.ofNullable(ScopeLookup.findByNsUri(scopeService, ScopeLookup.schemaRegistry(scopeService),
				address.getStage(), address.getNsUri()));
	}

	@Override
	public Optional<ObjectMetadata> finalPackageMetadata(String scope, String nsUri) {
		WritableScopeService<EObject> scopeService = lookup.scope(scope);
		return Optional.ofNullable(ScopeLookup.findByNsUriInFinalStage(scopeService,
				ScopeLookup.schemaRegistry(scopeService), nsUri));
	}

	@Override
	public PackageSearchResult searchPackages(String scope, PackageSearch search) {
		EPackageSearchQuery query = EPackageSearchQuery.create()
				.scopes(scopeChain(scope))
				.stage(search.getStage())
				.nsUri(search.getNsUri())
				.nsUriExact(search.getNsUriExact())
				.name(search.getName())
				.nsPrefix(search.getPrefix())
				.classifier(search.getClassifier())
				.featureName(search.getFeatureName())
				.featureType(search.getFeatureType())
				.featureNameTypePair(search.getFeatureNameTypePair())
				.limit(search.getLimit())
				.offset(search.getOffset())
				.build();
		EPackageLuceneIndex index = searchIndex.get()
				.orElseThrow(() -> new IllegalStateException("The package search index is not available"));
		SearchResult result = index.search(query);
		PackageSearchResult page = RegistryApiFactory.eINSTANCE.createPackageSearchResult();
		page.getHits().addAll(resolve(result.hits(), scope));
		page.setTotalHits(result.totalHits());
		return page;
	}

	/** The scope and its ancestors, nearest first. */
	private Set<String> scopeChain(String scopeName) {
		Set<String> chain = new LinkedHashSet<>();
		String current = scopeName;
		while (current != null && chain.add(current)) {
			Scope scope = lookup.collector().getScopeByName(current);
			current = scope != null ? scope.getParentScope() : null;
		}
		return chain;
	}

	/**
	 * The metadata behind each hit, read from the scope and stage the index recorded. The index is
	 * a derived view, so a hit that no longer resolves — its scope or object gone — is dropped. A
	 * hit from an ancestor scope is read-only from where the search was made.
	 */
	private List<ObjectMetadata> resolve(List<SearchHit> hits, String scopeName) {
		List<ObjectMetadata> resolved = new ArrayList<>(hits.size());
		for (SearchHit hit : hits) {
			WritableScopeService<?> scopeService = lookup.collector().getScopeServiceByScopeName(hit.scope());
			if (scopeService == null) {
				continue;
			}
			ObjectMetadata metadata = scopeService.getMetadataFromStageForRegistry(hit.registry(), hit.stage(),
					hit.objectId());
			if (metadata != null) {
				if (!scopeName.equals(metadata.getScope())) {
					metadata.setIsReadOnly(true);
				}
				resolved.add(metadata);
			}
		}
		return resolved;
	}
}
