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

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageLuceneIndex;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.registry.RegistryAddress;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractEObjectStorageService;
import org.eclipse.fennec.model.atlas.registry.api.BatchItemResult;
import org.eclipse.fennec.model.atlas.registry.api.BatchResult;
import org.eclipse.fennec.model.atlas.registry.api.ConflictPolicy;
import org.eclipse.fennec.model.atlas.registry.api.ObjectAddress;
import org.eclipse.fennec.model.atlas.registry.api.ObjectWrite;
import org.eclipse.fennec.model.atlas.registry.api.PackageAddress;
import org.eclipse.fennec.model.atlas.registry.api.PackageWrite;
import org.eclipse.fennec.model.atlas.registry.api.Precondition;
import org.eclipse.fennec.model.atlas.registry.api.RegistryAccessException;
import org.eclipse.fennec.model.atlas.registry.api.RegistryAccessException.Reason;
import org.eclipse.fennec.model.atlas.registry.api.RegistryWriter;
import org.eclipse.fennec.model.atlas.registry.api.WriteOutcome;
import org.eclipse.fennec.model.atlas.registry.api.WriteResult;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.atlas.workflow.RegistryServiceCollector;
import org.eclipse.fennec.model.atlas.workflow.ScopeServiceCollector;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.util.promise.Promise;

/**
 * The {@link RegistryWriter} on top of the workflow's scope and registry services (issue #386).
 *
 * <p>
 * The rules and their order are the ones the REST resources applied before they delegated here,
 * kept message for message so that clients see no difference.
 * </p>
 */
@Component(service = RegistryWriter.class)
public class RegistryWriterImpl implements RegistryWriter {

	private static final Logger LOGGER = Logger.getLogger(RegistryWriterImpl.class.getName());

	/** The objectType a stored EPackage records. */
	private static final String EPACKAGE_TYPE = EcoreUtil.getURI(EcorePackage.Literals.EPACKAGE).toString();

	private final ScopeLookup lookup;
	private final RegistryServiceCollector registryCollector;
	private final ManagementFactory mgmtFactory;
	private final SearchIndex searchIndex = new SearchIndex();

	@Activate
	public RegistryWriterImpl(@Reference ScopeServiceCollector scopeCollector,
			@Reference RegistryServiceCollector registryCollector, @Reference ManagementFactory mgmtFactory) {
		this.lookup = new ScopeLookup(scopeCollector);
		this.registryCollector = registryCollector;
		this.mgmtFactory = mgmtFactory;
	}

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC,
			policyOption = ReferencePolicyOption.GREEDY)
	void bindEPackageIndex(EPackageLuceneIndex index) {
		searchIndex.bind(index);
	}

	void unbindEPackageIndex(EPackageLuceneIndex index) {
		searchIndex.unbind(index);
	}

	// ======================
	// Instances
	// ======================

	@Override
	public WriteResult createObject(ObjectWrite write) {
		ObjectAddress address = write.address();
		EObject object = write.content();
		ObjectMetadata existing = checkCreateObject(write);
		WritableScopeService<EObject> scopeService = scope(address.scope());
		if (existing != null) {
			if (policy(write.onConflict()) == ConflictPolicy.SKIP) {
				return WriteResult.of(WriteOutcome.SKIPPED, existing);
			}
			ObjectMetadata metadata = await(scopeService.updateInStageForRegistry(address.registry(),
					address.stage(), object, address.objectId(), write.version()));
			return WriteResult.of(WriteOutcome.UPDATED, metadata);
		}
		ObjectMetadata metadata = mgmtFactory.createObjectMetadata();
		metadata.setObjectId(address.objectId());
		metadata.setObjectName(write.name());
		metadata.setUploadTime(Instant.now());
		metadata.setStage(address.stage());
		metadata.setScope(address.scope());
		metadata.setRegistry(address.registry());
		metadata.setVersion(write.version());
		metadata.setObjectType(EcoreUtil.getURI(object.eClass()).toString());

		metadata = await(scopeService.uploadToStageForRegistry(address.registry(), address.stage(), object, metadata));
		return WriteResult.of(WriteOutcome.CREATED, metadata);
	}

	@Override
	public WriteResult updateObject(ObjectWrite write) {
		ObjectAddress address = write.address();
		EObject object = write.content();
		requireClientType(address.registry(), object);
		WritableScopeService<EObject> scopeService = scope(address.scope());

		ObjectMetadata existing = scopeService.getMetadataFromStageForRegistry(address.registry(), address.stage(),
				address.objectId());
		if (existing == null) {
			return WriteResult.of(WriteOutcome.NOT_FOUND);
		}
		// e.g. retrieved from a parent scope's final stage
		if (existing.isIsReadOnly()) {
			throw readOnly("Object", address.objectId());
		}
		requirePrecondition(write.precondition(), existing);
		if (unchanged(object, existing)) {
			return WriteResult.of(WriteOutcome.UNCHANGED, existing);
		}
		ObjectMetadata metadata = await(scopeService.updateInStageForRegistry(address.registry(), address.stage(),
				object, address.objectId(), write.version()));
		return WriteResult.of(WriteOutcome.UPDATED, metadata);
	}

	@Override
	public WriteResult deleteObject(ObjectAddress address, boolean force, Precondition precondition) {
		WritableScopeService<EObject> scopeService = scope(address.scope());
		ObjectMetadata existing = scopeService.getMetadataFromStageForRegistry(address.registry(), address.stage(),
				address.objectId());
		if (existing == null) {
			return WriteResult.of(WriteOutcome.NOT_FOUND);
		}
		if (existing.isIsReadOnly()) {
			throw readOnly("Object", address.objectId());
		}
		requirePrecondition(precondition, existing);
		boolean deleted = await(scopeService.deleteFromStageForRegistry(address.registry(), address.stage(),
				address.objectId(), force));
		if (!deleted) {
			throw new IllegalStateException(
					String.format("Object %s deletion failed but causes are unknown", address.objectId()));
		}
		// a package stored by id rather than by nsURI is still in the search index
		if (EPACKAGE_TYPE.equals(existing.getObjectType())) {
			searchIndex.remove(RegistryAddress.of(existing));
		}
		return WriteResult.of(WriteOutcome.DELETED);
	}

	@Override
	public WriteResult transitionObject(ObjectAddress address, String targetStage, boolean overwrite,
			Precondition precondition) {
		WritableScopeService<EObject> scopeService = scope(address.scope());
		ObjectMetadata existing = scopeService.getMetadataFromStageForRegistry(address.registry(), address.stage(),
				address.objectId());
		if (existing == null) {
			// not in the source stage — already in the target is a retried transition
			ObjectMetadata inTarget = scopeService.getMetadataFromStageForRegistry(address.registry(), targetStage,
					address.objectId());
			return inTarget == null ? WriteResult.of(WriteOutcome.NOT_FOUND)
					: WriteResult.of(WriteOutcome.ALREADY_IN_TARGET, inTarget);
		}
		if (existing.isIsReadOnly()) {
			throw readOnly("Object", address.objectId());
		}
		requirePrecondition(precondition, existing);
		ObjectMetadata metadata = scopeService.transitionToStageForRegistry(address.registry(), address.objectId(),
				address.stage(), targetStage, overwrite);
		return WriteResult.of(WriteOutcome.TRANSITIONED, metadata);
	}

	// ======================
	// Packages
	// ======================

	@Override
	public WriteResult createPackage(PackageWrite write) {
		return createPackage(write, checkCreatePackage(write));
	}

	private WriteResult createPackage(PackageWrite write, PackagePlan plan) {
		WritableScopeService<EObject> scopeService = plan.scopeService();
		EPackage ePackage = write.content();
		String nsUri = plan.nsUri();
		String version = plan.version();
		String registry = plan.registry();

		ObjectMetadata existing = plan.existing();
		if (existing != null) {
			if (policy(write.onConflict()) == ConflictPolicy.SKIP) {
				return WriteResult.of(WriteOutcome.SKIPPED, existing);
			}
			ObjectMetadata metadata = await(scopeService.updateInStageForRegistry(registry, write.stage(),
					ePackage, existing.getObjectId(), version));
			// An absent dcat leaves the stored flag alone: an overwrite that says nothing about
			// publication must not unpublish the model. Only an explicit value moves it.
			if (write.dcat() != null) {
				metadata = await(scopeService.updatePropertiesInStageForRegistry(registry, write.stage(),
						existing.getObjectId(), Map.of(WorkflowConstants.DCAT_PUBLISH_METADATA_PROPERTY,
								Boolean.toString(write.dcat()))));
			}
			searchIndex.index(metadata, ePackage);
			return WriteResult.of(WriteOutcome.UPDATED, metadata);
		}
		// The objectId is a random UUID assigned once at upload and stable across stage
		// transitions (it is the lifecycle audit trail); the nsURI is carried only by the nsUri
		// property.
		ObjectMetadata metadata = mgmtFactory.createObjectMetadata();
		metadata.setObjectId(UUID.randomUUID().toString());
		metadata.setObjectName(write.name() == null ? ePackage.getName() : write.name());
		metadata.setUploadTime(Instant.now());
		metadata.setStage(write.stage());
		metadata.setScope(write.scope());
		metadata.setRegistry(registry);
		metadata.setVersion(version);
		metadata.setObjectType(EcoreUtil.getURI(ePackage.eClass()).toString());
		metadata.getProperties().put(WorkflowConstants.NS_URI_METADATA_PROPERTY, nsUri);
		// "true"/"false", not a Boolean: a property is text, so that it can be read and edited in
		// the stored metadata and by a client that is not Java (issue #354). Absent means false.
		metadata.getProperties().put(WorkflowConstants.DCAT_PUBLISH_METADATA_PROPERTY,
				Boolean.toString(Boolean.TRUE.equals(write.dcat())));

		metadata = await(scopeService.uploadToStageForRegistry(registry, write.stage(), ePackage, metadata));
		searchIndex.index(metadata, ePackage);
		return WriteResult.of(WriteOutcome.CREATED, metadata);
	}

	@Override
	public WriteResult updatePackage(PackageWrite write) {
		WritableScopeService<EObject> scopeService = scope(write.scope());
		EPackage ePackage = write.content();
		String nsUri = validateAndResolveNsUri(write.nsUri(), ePackage);
		String version = PackageVersions.resolve(ePackage, write.version(), nsUri, write.lenientVersion());
		String registry = packageRegistry(scopeService, write.registry(), ePackage);

		ObjectMetadata existing = findByNsUri(scopeService, registry, write.stage(), nsUri);
		if (existing == null) {
			return WriteResult.of(WriteOutcome.NOT_FOUND);
		}
		// e.g. retrieved from a parent scope's final stage
		if (existing.isIsReadOnly()) {
			throw readOnly("Schema", nsUri);
		}
		requirePrecondition(write.precondition(), existing);
		if (unchanged(ePackage, existing)) {
			return WriteResult.of(WriteOutcome.UNCHANGED, existing);
		}

		EcoreUtil.resolveAll(ePackage);
		if (ePackage.eResource() != null) {
			ePackage.eResource().setURI(URI.createURI(ePackage.getNsURI()));
		}
		ObjectMetadata metadata = await(scopeService.updateInStageForRegistry(registry, write.stage(), ePackage,
				existing.getObjectId(), version));
		searchIndex.index(metadata, ePackage);
		return WriteResult.of(WriteOutcome.UPDATED, metadata);
	}

	@Override
	public WriteResult updatePackageProperties(PackageAddress address, Map<String, String> properties,
			Precondition precondition) {
		WritableScopeService<EObject> scopeService = scope(address.scope());
		requireNsUri(address.nsUri());
		String registry = schemaRegistry(scopeService);

		ObjectMetadata existing = findByNsUri(scopeService, registry, address.stage(), address.nsUri());
		if (existing == null) {
			return WriteResult.of(WriteOutcome.NOT_FOUND);
		}
		// An inherited package's record belongs to the scope that defines it. Editing it through a
		// child scope would write to the parent's metadata under a name that does not say so,
		// which is worse than refusing.
		if (existing.getScope() != null && !existing.getScope().equals(address.scope())) {
			throw new RegistryAccessException(Reason.INHERITED,
					String.format("Schema %s is inherited from scope %s; edit its metadata there", address.nsUri(),
							existing.getScope()));
		}
		if (existing.isIsReadOnly()) {
			throw readOnly("Schema", address.nsUri());
		}
		requirePrecondition(precondition, existing);
		ObjectMetadata metadata = await(scopeService.updatePropertiesInStageForRegistry(registry, address.stage(),
				existing.getObjectId(), properties));
		return metadata == null ? WriteResult.of(WriteOutcome.NOT_FOUND)
				: WriteResult.of(WriteOutcome.UPDATED, metadata);
	}

	@Override
	public WriteResult deletePackage(PackageAddress address, boolean force, Precondition precondition) {
		WritableScopeService<EObject> scopeService = scope(address.scope());
		requireNsUri(address.nsUri());
		String registry = schemaRegistry(scopeService);

		ObjectMetadata existing = findByNsUri(scopeService, registry, address.stage(), address.nsUri());
		if (existing == null) {
			return WriteResult.of(WriteOutcome.NOT_FOUND);
		}
		if (existing.isIsReadOnly()) {
			throw readOnly("Schema", address.nsUri());
		}
		requirePrecondition(precondition, existing);
		boolean deleted = await(
				scopeService.deleteFromStageForRegistry(registry, address.stage(), existing.getObjectId(), force));
		if (!deleted) {
			throw new IllegalStateException(
					String.format("Schema %s deletion failed but causes are unknown", address.nsUri()));
		}
		// Only this stage's copy is gone: the same id may be held by other stages of this
		// registry, and those entries must stay searchable (issue #252)
		searchIndex.remove(RegistryAddress.of(existing));
		return WriteResult.of(WriteOutcome.DELETED);
	}

	@Override
	public WriteResult transitionPackage(String scope, String stage, String idOrNsUri, String targetStage,
			boolean overwrite, Precondition precondition) {
		WritableScopeService<EObject> scopeService = scope(scope);
		if (idOrNsUri == null || idOrNsUri.isBlank()) {
			throw new RegistryAccessException(Reason.INVALID, "Transition request must carry an objectId");
		}
		String registry = schemaRegistry(scopeService);
		ObjectMetadata existing = resolvePackage(scopeService, registry, stage, idOrNsUri);
		if (existing == null) {
			// not in the source stage — already in the target is a retried transition
			ObjectMetadata inTarget = resolvePackage(scopeService, registry, targetStage, idOrNsUri);
			return inTarget == null ? WriteResult.of(WriteOutcome.NOT_FOUND)
					: WriteResult.of(WriteOutcome.ALREADY_IN_TARGET, inTarget);
		}
		if (existing.isIsReadOnly()) {
			throw readOnly("Schema", idOrNsUri);
		}
		requirePrecondition(precondition, existing);
		ObjectMetadata metadata = scopeService.transitionToStageForRegistry(registry, existing.getObjectId(), stage,
				targetStage, overwrite);
		reindexAfterTransition(scopeService, registry, metadata, stage, targetStage);
		return WriteResult.of(WriteOutcome.TRANSITIONED, metadata);
	}

	// ======================
	// Batches
	// ======================

	@Override
	public BatchResult writeObjects(List<ObjectWrite> writes) {
		return runBatch(writes.stream().<BatchItem> map(ObjectItem::new).toList());
	}

	@Override
	public BatchResult writePackages(List<PackageWrite> writes) {
		return runBatch(writes.stream().<BatchItem> map(PackageItem::new).toList());
	}

	/**
	 * All items or none: every item is checked before the first one is written, and once writing
	 * has started, a refusal undoes what the batch wrote so far, newest first.
	 */
	private BatchResult runBatch(List<BatchItem> items) {
		List<BatchItemResult> results = new ArrayList<>(items.size());
		Set<String> keys = new HashSet<>();
		boolean refused = false;
		for (BatchItem item : items) {
			try {
				item.check();
				if (!keys.add(item.key())) {
					throw new RegistryAccessException(Reason.INVALID,
							String.format("%s occurs more than once in the batch", item.id()));
				}
				results.add(BatchItemResult.notApplied(item.id()));
			} catch (RegistryAccessException e) {
				refused = true;
				results.add(BatchItemResult.refused(item.id(), e));
			}
		}
		if (refused) {
			return new BatchResult(false, results);
		}

		Deque<Runnable> undo = new ArrayDeque<>();
		for (int i = 0; i < items.size(); i++) {
			BatchItem item = items.get(i);
			try {
				results.set(i, BatchItemResult.written(item.id(), item.write(undo)));
			} catch (RegistryAccessException e) {
				undo(undo);
				List<BatchItemResult> notApplied = new ArrayList<>(items.size());
				for (BatchItem other : items) {
					notApplied.add(BatchItemResult.notApplied(other.id()));
				}
				notApplied.set(i, BatchItemResult.refused(item.id(), e));
				return new BatchResult(false, notApplied);
			} catch (RuntimeException | Error e) {
				undo(undo);
				throw e;
			}
		}
		return new BatchResult(true, results);
	}

	/** Runs the undo steps, newest first. A step that fails is logged; the others still run. */
	private static void undo(Deque<Runnable> undo) {
		while (!undo.isEmpty()) {
			try {
				undo.pop().run();
			} catch (RuntimeException e) {
				LOGGER.log(Level.WARNING, "Could not undo a write of a refused batch", e);
			}
		}
	}

	/** One item of a batch. */
	private interface BatchItem {

		/** The identifier reported for the item. */
		String id();

		/** What makes two items of a batch the same target. */
		String key();

		/** Applies the rules the single write applies, without writing anything. */
		void check();

		/** Writes the item and registers how to undo that write. */
		WriteResult write(Deque<Runnable> undo);
	}

	private final class ObjectItem implements BatchItem {

		private final ObjectWrite write;

		ObjectItem(ObjectWrite write) {
			this.write = write;
		}

		@Override
		public String id() {
			return write.address().objectId();
		}

		@Override
		public String key() {
			return write.address().toString();
		}

		@Override
		public void check() {
			if (id() == null || id().isBlank()) {
				throw new RegistryAccessException(Reason.INVALID, String.format("Object of type %s has no object id",
						EcoreUtil.getURI(write.content().eClass())));
			}
			checkCreateObject(write);
		}

		@Override
		public WriteResult write(Deque<Runnable> undo) {
			ObjectAddress address = write.address();
			WritableScopeService<EObject> scopeService = scope(address.scope());
			ObjectMetadata existing = scopeService.getMetadataFromStageForRegistry(address.registry(),
					address.stage(), address.objectId());
			boolean overwrite = existing != null && policy(write.onConflict()) == ConflictPolicy.OVERWRITE;
			if (overwrite && unchanged(write.content(), existing)) {
				return WriteResult.of(WriteOutcome.UNCHANGED, existing);
			}
			Runnable restore = overwrite ? restoreObject(scopeService, address, existing) : null;
			WriteResult result = createObject(write);
			if (result.outcome() == WriteOutcome.CREATED) {
				undo.push(() -> deleteObject(address, true, null));
			} else if (result.outcome() == WriteOutcome.UPDATED) {
				undo.push(restore);
			}
			return result;
		}
	}

	private final class PackageItem implements BatchItem {

		private final PackageWrite write;

		PackageItem(PackageWrite write) {
			this.write = write;
		}

		@Override
		public String id() {
			return write.nsUri() != null ? write.nsUri() : write.content().getNsURI();
		}

		@Override
		public String key() {
			return String.join("|", write.scope(), String.valueOf(write.registry()), write.stage(), id());
		}

		@Override
		public void check() {
			checkCreatePackage(write);
		}

		@Override
		public WriteResult write(Deque<Runnable> undo) {
			PackagePlan plan = checkCreatePackage(write);
			ObjectMetadata existing = plan.existing();
			boolean overwrite = existing != null && policy(write.onConflict()) == ConflictPolicy.OVERWRITE;
			if (overwrite && unchanged(write.content(), existing)) {
				return WriteResult.of(WriteOutcome.UNCHANGED, existing);
			}
			Runnable restore = overwrite ? restorePackage(plan, write) : null;
			WriteResult result = createPackage(write, plan);
			if (result.outcome() == WriteOutcome.CREATED) {
				ObjectAddress address = new ObjectAddress(write.scope(), plan.registry(), write.stage(),
						result.metadata().getObjectId());
				undo.push(() -> deleteObject(address, true, null));
			} else if (result.outcome() == WriteOutcome.UPDATED) {
				undo.push(restore);
			}
			return result;
		}
	}

	/** Puts the content an instance has now back, should the batch replacing it be undone. */
	private static Runnable restoreObject(WritableScopeService<EObject> scopeService, ObjectAddress address,
			ObjectMetadata existing) {
		EObject current = scopeService.getContentFromStageForRegistry(address.registry(), address.stage(),
				address.objectId());
		if (current == null) {
			return () -> LOGGER.log(Level.WARNING, "Cannot restore object {0}: its previous content was not readable",
					address.objectId());
		}
		EObject snapshot = EcoreUtil.copy(current);
		String version = existing.getVersion();
		return () -> await(scopeService.updateInStageForRegistry(address.registry(), address.stage(), snapshot,
				address.objectId(), version));
	}

	/**
	 * Puts the content, version and publication flag a package has now back, should the batch
	 * replacing it be undone.
	 */
	private Runnable restorePackage(PackagePlan plan, PackageWrite write) {
		ObjectMetadata existing = plan.existing();
		String objectId = existing.getObjectId();
		Object current = plan.scopeService().getContentFromStageForRegistry(plan.registry(), write.stage(), objectId);
		if (!(current instanceof EPackage currentPackage)) {
			return () -> LOGGER.log(Level.WARNING, "Cannot restore package {0}: its previous content was not readable",
					plan.nsUri());
		}
		EPackage snapshot = EcoreUtil.copy(currentPackage);
		String version = existing.getVersion();
		String dcat = existing.getProperties().get(WorkflowConstants.DCAT_PUBLISH_METADATA_PROPERTY);
		return () -> {
			ObjectMetadata metadata = await(plan.scopeService().updateInStageForRegistry(plan.registry(),
					write.stage(), snapshot, objectId, version));
			if (write.dcat() != null) {
				metadata = await(plan.scopeService().updatePropertiesInStageForRegistry(plan.registry(),
						write.stage(), objectId, Map.of(WorkflowConstants.DCAT_PUBLISH_METADATA_PROPERTY,
								dcat == null ? Boolean.FALSE.toString() : dcat)));
			}
			searchIndex.index(metadata, snapshot);
		};
	}

	// ======================
	// Rules
	// ======================

	/**
	 * The rules {@link #createObject(ObjectWrite)} applies before it writes.
	 *
	 * @return the object already at the address, or {@code null}
	 */
	private ObjectMetadata checkCreateObject(ObjectWrite write) {
		ObjectAddress address = write.address();
		requireClientType(address.registry(), write.content());
		WritableScopeService<EObject> scopeService = scope(address.scope());

		ObjectMetadata existing = scopeService.getMetadataFromStageForRegistry(address.registry(), address.stage(),
				address.objectId());
		if (existing != null) {
			switch (policy(write.onConflict())) {
			case SKIP:
				break;
			case FAIL:
				throw new RegistryAccessException(Reason.ALREADY_EXISTS, String.format(
						"Object %s already exists and override flag is false. Cannot update object.",
						address.objectId()));
			case OVERWRITE:
			default:
				if (existing.isIsReadOnly()) {
					throw new RegistryAccessException(Reason.READ_ONLY,
							String.format("Object %s is read-only. Cannot update it.", address.objectId()));
				}
				requirePrecondition(write.precondition(), existing);
			}
		}
		return existing;
	}

	/** Where and how {@link #createPackage(PackageWrite)} writes a package. */
	private record PackagePlan(WritableScopeService<EObject> scopeService, String nsUri, String version,
			String registry, ObjectMetadata existing) {
	}

	/** The rules {@link #createPackage(PackageWrite)} applies before it writes. */
	private PackagePlan checkCreatePackage(PackageWrite write) {
		WritableScopeService<EObject> scopeService = scope(write.scope());
		EPackage ePackage = write.content();
		String nsUri = validateAndResolveNsUri(write.nsUri(), ePackage);
		String version = PackageVersions.resolve(ePackage, write.version(), nsUri, write.lenientVersion());
		String registry = packageRegistry(scopeService, write.registry(), ePackage);

		ObjectMetadata existing = findByNsUri(scopeService, registry, write.stage(), nsUri);
		if (existing != null) {
			switch (policy(write.onConflict())) {
			case SKIP:
				break;
			case FAIL:
				throw new RegistryAccessException(Reason.ALREADY_EXISTS, String.format(
						"Schema %s already exists and overwrite flag is false. Cannot update object.", nsUri));
			case OVERWRITE:
			default:
				if (existing.isIsReadOnly()) {
					throw new RegistryAccessException(Reason.READ_ONLY,
							String.format("Schema %s is read-only. Cannot update it.", nsUri));
				}
				requirePrecondition(write.precondition(), existing);
			}
		}
		return new PackagePlan(scopeService, nsUri, version, registry, existing);
	}

	/**
	 * The type gate a client is held to: derived content is written by the Atlas only, and
	 * everything else must be a root type of the registry. The registry service enforces the
	 * latter itself as well; checking here answers it before anything is looked up.
	 */
	private void requireClientType(String registryName, EObject object) {
		RegistryService<?> registryService = registryCollector.getRegistryServiceByRegistryName(registryName);
		if (registryService == null) {
			throw new RegistryAccessException(Reason.UNKNOWN_REGISTRY,
					"Unknown or unconfigured registry: " + registryName);
		}
		if (registryService.isDerivedEClass(object.eClass())) {
			throw new RegistryAccessException(Reason.DERIVED_CONTENT,
					String.format("Object type %s is derived content of registry %s and is written by the Atlas only",
							EcoreUtil.getURI(object.eClass()), registryName));
		}
		if (!registryService.isEClassCompatibleWithRegistry(object.eClass())) {
			throw new RegistryAccessException(Reason.INCOMPATIBLE_TYPE,
					String.format("Object type %s not compatible with registry %s (expects %s)",
							EcoreUtil.getURI(object.eClass()), registryName, acceptedRootEClasses(registryService)));
		}
	}

	private static void requirePrecondition(Precondition precondition, ObjectMetadata existing) {
		if (!Precondition.orNone(precondition).test(existing)) {
			throw new RegistryAccessException(Reason.PRECONDITION_FAILED,
					"Resource has been modified. ETag mismatch.");
		}
	}

	private static RegistryAccessException readOnly(String kind, String id) {
		return new RegistryAccessException(Reason.READ_ONLY, String.format("%s %s is in read-only state", kind, id));
	}

	/** Content-aware skip: content equal to what is stored is not written again. */
	private static boolean unchanged(EObject content, ObjectMetadata existing) {
		String newContentHash = AbstractEObjectStorageService.computeContentHash(content);
		return newContentHash != null && newContentHash.equals(existing.getContentHash());
	}

	private static ConflictPolicy policy(ConflictPolicy policy) {
		return policy == null ? ConflictPolicy.FAIL : policy;
	}

	private static void requireNsUri(String nsUri) {
		if (nsUri == null || nsUri.isBlank()) {
			throw new RegistryAccessException(Reason.INVALID, "Query parameter nsUri is required");
		}
	}

	/**
	 * The nsURI a package is stored under: the package's own, which an nsURI the caller passed
	 * along must match.
	 */
	private static String validateAndResolveNsUri(String nsUri, EPackage ePackage) {
		String packageNsUri = ePackage.getNsURI();
		if (packageNsUri == null || packageNsUri.isBlank()) {
			throw new RegistryAccessException(Reason.INVALID, "EPackage must have a non-empty nsURI set");
		}
		if (nsUri == null || nsUri.isBlank()) {
			return packageNsUri;
		}
		String decoded = URI.decode(nsUri);
		if (!decoded.equals(packageNsUri)) {
			throw new RegistryAccessException(Reason.INVALID, String
					.format("Query parameter nsUri '%s' does not match EPackage nsURI '%s'", decoded, packageNsUri));
		}
		return decoded;
	}

	// ======================
	// Lookups
	// ======================

	private WritableScopeService<EObject> scope(String scopeName) {
		return lookup.scope(scopeName);
	}

	private static String schemaRegistry(WritableScopeService<?> scopeService) {
		return ScopeLookup.schemaRegistry(scopeService);
	}

	/**
	 * The registry a package goes to: the scope's schema registry unless the caller names
	 * another, which then has to accept the package like any client content.
	 */
	private String packageRegistry(WritableScopeService<?> scopeService, String registry, EPackage ePackage) {
		if (registry == null) {
			return schemaRegistry(scopeService);
		}
		requireClientType(registry, ePackage);
		return registry;
	}

	private static ObjectMetadata findByNsUri(WritableScopeService<?> scopeService, String registry, String stage,
			String nsUri) {
		return ScopeLookup.findByNsUri(scopeService, registry, stage, nsUri);
	}

	/**
	 * A package by the identifier a transition request carries: first as a real objectId, then —
	 * for clients that still send the raw nsURI (the objectId used to be derived from it) — via
	 * the nsUri property.
	 */
	private static ObjectMetadata resolvePackage(WritableScopeService<?> scopeService, String registry, String stage,
			String idOrNsUri) {
		ObjectMetadata metadata = scopeService.getMetadataFromStageForRegistry(registry, stage, idOrNsUri);
		return metadata != null ? metadata : findByNsUri(scopeService, registry, stage, idOrNsUri);
	}

	private static String acceptedRootEClasses(RegistryService<?> registryService) {
		return registryService.getRootEClasses().stream().map(EcoreUtil::getURI).map(Object::toString)
				.collect(Collectors.joining(", "));
	}

	// ======================
	// Search index
	// ======================

	/**
	 * Brings the search index in line with a transition that has just happened. The index doc
	 * records the stage a hit must be resolved against, so both ends need attention: the target
	 * stage gains a doc for the copy it now holds, and the source stage keeps or loses its own doc
	 * depending on whether the registry deleted the source copy ({@code delete.after.transition}).
	 * An id is unique per stage, not across stages (issue #211), so the two docs coexist for a
	 * copying transition.
	 *
	 * <p>
	 * A transition that succeeded must not be reported as failed because the search index could
	 * not be updated: the index is a derived view, so a failure here is logged and the target
	 * entry removed, leaving the package absent from search rather than present under the wrong
	 * stage.
	 * </p>
	 */
	private void reindexAfterTransition(WritableScopeService<?> scopeService, String registry, ObjectMetadata metadata,
			String sourceStage, String targetStage) {
		if (!searchIndex.isAvailable()) {
			return;
		}
		String objectId = metadata.getObjectId();
		try {
			Object content = scopeService.getContentFromStageForRegistry(registry, targetStage, objectId);
			if (content instanceof EPackage ePackage) {
				searchIndex.index(metadata, ePackage);
			} else {
				LOGGER.log(Level.WARNING,
						"Could not re-index object {0} after its transition to stage {1}: no EPackage content in the target stage. Removing its stale index entry.",
						new Object[] { objectId, targetStage });
				searchIndex.remove(addressIn(metadata, targetStage));
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, e, () -> "Could not re-index object " + objectId
					+ " after its transition to stage " + targetStage + "; removing its stale index entry");
			searchIndex.remove(addressIn(metadata, targetStage));
		}
		dropSourceEntryIfMoved(scopeService, registry, metadata, sourceStage);
	}

	/**
	 * Drops the source stage's index entry when the transition was a move rather than a copy.
	 * Whether the source copy survives is the registry's configuration, not the caller's, so it is
	 * read back from the stage itself. A failure to look it up leaves the entry in place: a search
	 * hit that no longer resolves is dropped from the results, while removing a document of a
	 * package that is still there would hide it for good.
	 */
	private void dropSourceEntryIfMoved(WritableScopeService<?> scopeService, String registry, ObjectMetadata metadata,
			String sourceStage) {
		String objectId = metadata.getObjectId();
		try {
			if (scopeService.getMetadataFromStageForRegistry(registry, sourceStage, objectId) == null) {
				searchIndex.remove(addressIn(metadata, sourceStage));
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, e, () -> "Could not check whether object " + objectId
					+ " still lives in stage " + sourceStage + "; leaving its index entry in place");
		}
	}

	/** The address the given object has in the given stage of its registry. */
	private static RegistryAddress addressIn(ObjectMetadata metadata, String stage) {
		return new RegistryAddress(metadata.getScope(), metadata.getRegistry(), stage, metadata.getObjectId());
	}

	// ======================
	// Promises
	// ======================

	/**
	 * Waits for a workflow write. Its failure is rethrown as what it is — a stage policy refusal,
	 * a gate refusal, an invalid stage — rather than wrapped in the promise's
	 * {@link InvocationTargetException}, so callers can tell a refusal from a fault.
	 */
	private static <T> T await(Promise<T> promise) {
		try {
			return promise.getValue();
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause instanceof RuntimeException runtime) {
				throw runtime;
			}
			if (cause instanceof Error error) {
				throw error;
			}
			throw new IllegalStateException("A registry write failed", cause == null ? e : cause);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RegistryAccessException(Reason.INTERRUPTED, "The request was interrupted", e);
		}
	}
}
