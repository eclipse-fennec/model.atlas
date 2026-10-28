/**
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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.asset.impl;

import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.action.api.ActionContext;
import org.eclipse.fennec.model.atlas.action.api.StageActionService;
import org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.ModelAtlasAssetFactory;
import org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.SchemaAsset;
import org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.Promises;

import inventory.InventoryFactory;
import inventory.SourceKind;
import inventory.SourceRef;


/**
 * 
 * @author ilenia
 * @since Oct 26, 2026
 */
@Component(name = "AssetStageActionService", service = StageActionService.class, 
configurationPid = AssetStageActionService.PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = AssetStageActionService.Config.class)
public class AssetStageActionService implements StageActionService {

	
	private static final Logger LOGGER = Logger.getLogger(AssetStageActionService.class.getName());
    private static final String EPACKAGE_TYPE = EcoreUtil.getURI(EcorePackage.Literals.EPACKAGE).toString();

	static final String PID = "AssetStageActionService";
	
	private final WritableScopeService<EObject> scopeService;
	
	private final Set<String> supportedObjTypes;
	private final Set<String> triggerStages;
	private final String inventoryRegistry;
	private final String inventoryStage;
	private final String baseUri;
	
	@ObjectClassDefinition(name = "Asset Stage Action Service Configuration")
	public @interface Config {
		
		@AttributeDefinition(name = "Triggers", description = "The scope:registry:stage tuples that should trigger this action. A part that is blank or * matches anything. Only the stage part is honoured so far: the scope and the registry an action answers for are decided by the registry that binds it")
		String[] triggers() default {};
		
		@AttributeDefinition(name = "Supported Object Types", description = "The array of supported object types for this action")
		String[] supported_object_types() default {};
		
		@AttributeDefinition(name = "Inventory Registry", description = "The registry where the inventory assets should be written to")
		String inventory_registry();
		
		@AttributeDefinition(name = "Inventory Stage", description =  "The stage of the inventory registry where the assets should be written to")
		String inventory_stage();
		
		@AttributeDefinition(name = "Base URI", description = "Prefix for the source URI of an asset, e.g. http://localhost:8080/atlas/rest. Left unset, the source URI is written relative to the REST application, which keeps a stored asset correct when the atlas is reachable under another address than the one it was written under. Set it when the inventory is handed out to someone who cannot know that address", required = false)
		String base_uri() default "";
	}
	
	@Activate
	public AssetStageActionService(@Reference(name = "scopeService") WritableScopeService<EObject> scopeService, Config config) {
		this.supportedObjTypes = toSet(config.supported_object_types());
		this.triggerStages = toTriggerStages(config.triggers());
		this.scopeService = scopeService;
		this.inventoryRegistry = config.inventory_registry();
		this.inventoryStage = config.inventory_stage();
		this.baseUri = trimTrailingSlash(config.base_uri());
	}
	

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.action.api.StageActionService#supportsObjectType(java.lang.String)
	 */
	@Override
	public boolean supportsObjectType(String objectType) {
		// an immutable set throws on contains(null), and an object whose type the storage
		// never resolved reaches here with none
		return objectType != null && supportedObjTypes.contains(objectType);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.action.api.StageActionService#getTriggerStages()
	 */
	@Override
	public Set<String> getTriggerStages() {
		return triggerStages;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.action.api.StageActionService#getTriggerEvents()
	 */
	@Override
	public Set<ActionEvent> getTriggerEvents() {
		return Set.of(ActionEvent.ENTER, ActionEvent.UPDATE, ActionEvent.EXIT);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.action.api.StageActionService#onEnter(org.eclipse.fennec.model.atlas.action.api.ActionContext)
	 */
	@Override
	public Promise<Void> onEnter(ActionContext ctx) {
		return updateAsset(ctx);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.action.api.StageActionService#onUpdate(org.eclipse.fennec.model.atlas.action.api.ActionContext)
	 */
	@Override
	public Promise<Void> onUpdate(ActionContext ctx) {
		return updateAsset(ctx);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.action.api.StageActionService#onExit(org.eclipse.fennec.model.atlas.action.api.ActionContext)
	 */
	@Override
	public Promise<Void> onExit(ActionContext ctx) {
		if (fromInventory(ctx)) {
			return Promises.resolved(null);
		}
		String objectId = assetId(ctx);
		return resolveRemoval(scopeService.deleteFromStageForRegistry(inventoryRegistry, inventoryStage, objectId),
				objectId);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.action.api.StageActionService#requiresReplayOnStartup()
	 */
	@Override
	public boolean requiresReplayOnStartup() {
		return false;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.action.api.StageActionService#requiresReplayOnShutdown()
	 */
	@Override
	public boolean requiresReplayOnShutdown() {
		return false;
	}
	
	private static Set<String> toSet(String[] values) {
		if (values == null) {
			return Set.of();
		}
		Set<String> set = new LinkedHashSet<>();
		for (String value : values) {
			if (!blank(value)) {
				set.add(value.trim());
			}
		}
		return Set.copyOf(set);
	}


	private static boolean blank(String value) {
		return value == null || value.isBlank();
	}

	/**
	 * The stages named by the configured {@code scope:registry:stage} triggers.
	 *
	 * <p>
	 * An empty set is what the workflow reads as "every stage", so that is what no trigger
	 * at all returns, and also what a trigger that leaves its stage part open returns: a
	 * set of the stages the other triggers name would be narrower than what was configured.
	 * </p>
	 *
	 * @param triggers the configured tuples, or {@code null}
	 * @return the trigger stages, empty for every stage
	 * @throws IllegalArgumentException for an entry that is no tuple
	 */
	private static Set<String> toTriggerStages(String[] triggers) {
		if (triggers == null) {
			return Set.of();
		}
		Set<String> stages = new LinkedHashSet<>();
		for (String trigger : triggers) {
			if (blank(trigger)) {
				continue;
			}
			String[] parts = trigger.split(":", -1);
			if (parts.length != 3) {
				throw new IllegalArgumentException(
						String.format("triggers entry '%s' is no scope:registry:stage tuple", trigger));
			}
			String stage = parts[2].trim();
			if (stage.isEmpty() || "*".equals(stage)) {
				return Set.of();
			}
			stages.add(stage);
		}
		return Set.copyOf(stages);
	}
	
	private Promise<Void> updateAsset(ActionContext ctx) {
		if (fromInventory(ctx)) {
			return Promises.resolved(null);
		}
		String objectId = assetId(ctx);
		StageableAsset stageableAsset = null;
		if(EPACKAGE_TYPE.equals(ctx.objectType())) {
			stageableAsset = createSchemaAsset(ctx, objectId);
		} else {
			stageableAsset = createStageableAsset(ctx, objectId, ctx.objectType());
		}
		stageableAsset.getSources().add(createSourceRef(ctx));

		ObjectMetadata existing = scopeService.getMetadataFromStageForRegistry(inventoryRegistry, inventoryStage, objectId);
		// The lookup falls back to the parent scope's final stage, and an asset found there
		// is not one this stage can update: updateInStage reads the local metadata and would
		// work on null. Only an asset this very scope holds counts as present.
		boolean present = existing != null && scopeService.getScopeName().equals(existing.getScope());

		Promise<ObjectMetadata> written;
		String outcome;
		if (present) {
			written = scopeService.updateInStageForRegistry(inventoryRegistry, inventoryStage, stageableAsset, objectId,
					versionOf(ctx));
			outcome = "updated";
		} else {
			ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
			metadata.setObjectId(objectId);
			metadata.setUploadTime(Instant.now());
			metadata.setVersion(versionOf(ctx));
			// the type the asset actually is, derived the way the storage layer derives it,
			// so a SchemaAsset stays findable as one
			metadata.setObjectType(EcoreUtil.getURI(stageableAsset.eClass()).toString());
			written = scopeService.uploadToStageForRegistry(inventoryRegistry, inventoryStage, stageableAsset, metadata);
			outcome = "created";
		}
		return resolve(written, objectId, outcome);
	}

	/**
	 * The id the asset of one registry object is stored under.
	 *
	 * <p>
	 * Every asset lands in one stage of one registry, and the inventory scope need not be
	 * the scope the object came from, so the id carries the whole address the asset is
	 * about: an objectId is unique per stage, not across stages (issue #211), and not
	 * across registries or scopes either. No {@code #} in the separator - EMF reads one in
	 * an ID attribute back as a cross-document href.
	 * </p>
	 */
	private static String assetId(ActionContext ctx) {
		return String.join("_", "asset", ctx.scope(), ctx.registry(), ctx.stage(), ctx.objectId());
	}

	/**
	 * Whether the event is about the inventory this action writes into.
	 *
	 * <p>
	 * Writing an asset raises ENTER or UPDATE on the inventory registry in turn, and
	 * nothing in the configuration stops that registry from binding this action - the
	 * action may well sit in the scope it writes to. An event from there is ignored here
	 * rather than left to the object types a configuration happens to list.
	 * </p>
	 */
	private boolean fromInventory(ActionContext ctx) {
		return inventoryRegistry.equals(ctx.registry()) && scopeService.getScopeName().equals(ctx.scope());
	}

	/**
	 * Which revision of the source object the asset reflects. A fingerprint names one
	 * exactly; an object that carries none has nothing better to say than that this is the
	 * current one.
	 */
	private static String versionOf(ActionContext ctx) {
		String fingerprint = ctx.fingerprint();
		return fingerprint == null ? "1.0" : fingerprint;
	}

	/**
	 * The write's outcome, as this action's own promise.
	 *
	 * <p>
	 * A failure is returned rather than thrown. The workflow catches only an
	 * {@code InvocationTargetException} from the promise it waits on, so an exception
	 * thrown by the callback itself escapes the dispatch and fails the upload of the
	 * object the event was about. The inventory is a side effect of that upload: what a
	 * failure here should cost is a diagnostic on the object and whatever the registry's
	 * action chain says about {@code onFailure}, not the upload.
	 * </p>
	 */
	private Promise<Void> resolve(Promise<ObjectMetadata> written, String objectId, String outcome) {
		try {
			written.getValue();
			LOGGER.info(() -> String.format("Asset '%s' %s in %s/%s/%s.", objectId, outcome,
					scopeService.getScopeName(), inventoryRegistry, inventoryStage));
			return Promises.resolved(null);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause() == null ? e : e.getCause();
			return Promises.failed(new IllegalStateException(
					String.format("The Asset '%s' could not be written: %s", objectId,
							cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage()),
					cause));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return Promises.failed(new IllegalStateException(
					String.format("Interrupted while writing the Asset '%s'.", objectId), e));
		}
	}

	/** Same waiting and the same failure as a write, for the call that removes instead. */
	private Promise<Void> resolveRemoval(Promise<Boolean> removed, String objectId) {
		try {
			Boolean gone = removed.getValue();
			LOGGER.info(() -> String.format("Asset '%s' %s in %s/%s/%s.", objectId,
					Boolean.TRUE.equals(gone) ? "removed" : "was not there to remove",
					scopeService.getScopeName(), inventoryRegistry, inventoryStage));
			return Promises.resolved(null);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause() == null ? e : e.getCause();
			return Promises.failed(new IllegalStateException(
					String.format("The Asset '%s' could not be removed: %s", objectId,
							cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage()),
					cause));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return Promises.failed(new IllegalStateException(
					String.format("Interrupted while removing the Asset '%s'.", objectId), e));
		}
	}

	/**
	 * Where the object the asset is about can be read.
	 *
	 * <p>
	 * The URI addresses the object through the REST API, as
	 * {@code {scope}/registries/{registry}/stages/{stage}/content?objectId=...} - the endpoint
	 * that reads the object. The path that ends in the objectId is a POST/PUT address and
	 * answers no GET, so it would hand a reader an address it cannot read. Without a configured
	 * base URI it stays relative to the REST application: an asset outlives the runtime that
	 * wrote it, and an atlas that comes back up behind another host or port would otherwise
	 * hand out an address that never resolves. A reader inside the atlas knows the base; one
	 * outside it is served by a configuration that states the address the atlas is published
	 * under.
	 * </p>
	 *
	 * <p>
	 * The revision is a revision of this reference, not of the object it points at: it is
	 * derived from what the reference says - its kind, its format and its URI - so it
	 * changes when the object becomes reachable somewhere else, and stays put while only
	 * the object behind the URI changes. That the object changed is what a fingerprint
	 * says, and a fingerprint is on the asset, not here.
	 * </p>
	 */
	private SourceRef createSourceRef(ActionContext ctx) {
		SourceRef sourceRef = InventoryFactory.eINSTANCE.createSourceRef();
		sourceRef.setKind(EPACKAGE_TYPE.equals(ctx.objectType()) ? SourceKind.EPACKAGE : SourceKind.OTHER);
		sourceRef.setName(ctx.objectId());
		sourceRef.setUri(objectUri(ctx));
		sourceRef.setSourceRevision(sourceRevision(sourceRef));
		return sourceRef;
	}

	/**
	 * The revision of a source reference: a digest of what the reference states, so that two
	 * references that say the same thing carry the same revision and any change to what they
	 * say gives a new one. Derived rather than counted, because the action writes the asset
	 * whole and has no previous revision in hand to count up from.
	 *
	 * <p>
	 * The scheme prefix is {@code sr1:} and not the {@code fp1:} of a model fingerprint -
	 * this hashes a reference, and the two must not be read as the same kind of value.
	 * </p>
	 */
	private static String sourceRevision(SourceRef sourceRef) {
		String stated = String.join("\n", sourceRef.getKind().getName(),
				sourceRef.getFormat() == null ? "" : sourceRef.getFormat(), sourceRef.getUri());
		StringBuilder revision = new StringBuilder("sr1:");
		for (byte b : sha256(stated)) {
			revision.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
		}
		return revision.toString();
	}

	private static byte[] sha256(String value) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 is required of every Java runtime", e);
		}
	}

	/**
	 * The object's address, relative to the REST application unless a base URI is configured.
	 * Relative means relative: no leading slash, which would address the host's root rather
	 * than the application the atlas is deployed under.
	 */
	private String objectUri(ActionContext ctx) {
		String path = String.join("/", segment(ctx.scope()), "registries", segment(ctx.registry()), "stages",
				segment(ctx.stage()), "content");
		return (baseUri.isEmpty() ? path : baseUri + "/" + path) + "?objectId="
				+ URLEncoder.encode(ctx.objectId(), StandardCharsets.UTF_8);
	}

	/**
	 * One path segment, percent-encoded. {@link URLEncoder} writes a space as {@code +},
	 * which is a space only in a query string - where the objectId sits - and not in a path.
	 */
	private static String segment(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
	}

	private static String trimTrailingSlash(String uri) {
		if (blank(uri)) {
			return "";
		}
		String trimmed = uri.trim();
		return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
	}

	/** A metadata value as a string, or {@code null} when it is absent or is something else. */
	private static String text(Object value) {
		return value instanceof String s ? s : null;
	}

   private StageableAsset createStageableAsset(ActionContext ctx, String assetId, String assetType) {
	   StageableAsset stageableAsset = ModelAtlasAssetFactory.eINSTANCE.createStageableAsset();
		stageableAsset.setId(assetId);
		stageableAsset.setObjectId(ctx.objectId());
		stageableAsset.setAssetType(assetType);
		stageableAsset.setScope(ctx.scope());
		stageableAsset.setStage(ctx.stage());
		stageableAsset.setRegistry(ctx.registry());
		return stageableAsset;
   }

   private SchemaAsset createSchemaAsset(ActionContext ctx, String assetId) {
	   SchemaAsset schemaAsset = ModelAtlasAssetFactory.eINSTANCE.createSchemaAsset();
		schemaAsset.setId(assetId);
		schemaAsset.setObjectId(ctx.objectId());
		schemaAsset.setAssetType(ctx.objectType());
		schemaAsset.setFingerprint(ctx.fingerprint());
		schemaAsset.setScope(ctx.scope());
		schemaAsset.setStage(ctx.stage());
		schemaAsset.setRegistry(ctx.registry());
		// no display name: the workflow puts only the fingerprint and the nsUri into the
		// event's metadata, so there is nothing here to read one from
		schemaAsset.setNsURI(text(ctx.metadata().get(WorkflowConstants.NS_URI_METADATA_PROPERTY)));
		return schemaAsset;
   }
}
