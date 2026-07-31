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
package org.eclipse.fennec.model.atlas.workflow.impl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.workflow.ActionContext;
import org.eclipse.fennec.model.atlas.workflow.RegistryResync;
import org.eclipse.fennec.model.atlas.workflow.StageActionService;
import org.eclipse.fennec.model.atlas.workflow.StageActionService.ActionEvent;
import org.eclipse.fennec.model.atlas.workflow.StageActionService.ExitReason;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.typedevent.TypedEventConstants;
import org.osgi.service.typedevent.UntypedEventHandler;
import org.osgi.service.typedevent.annotations.RequireTypedEvent;

/**
 * Workflow-side handler for the {@link RegistryResync} event (D8 changes D + D8-3).
 *
 * <p>When a storage backend signals that a scope's content changed out-of-band (a git push
 * picked up by webhook/poll), this handler reconciles registration for that scope:
 * <ul>
 *   <li><b>ENTER (add/change):</b> calls {@link RegistryService#activate(String)} on every
 *       registry, reusing the cold-start replay path so schemas a push added or changed are
 *       (re)registered as OSGi services (visible via REST and to atlas clients).</li>
 *   <li><b>EXIT (removal, D8-3):</b> for each {@link RegistryResync#KEY_REMOVED_OBJECT_IDS
 *       removed} schema objectId it dispatches an EXIT to every {@link StageActionService}
 *       that handles EPackages, so the removed schema's EPackage is unregistered. This mirrors
 *       {@code RegistryServiceImpl.dispatch(EXIT, …)} but is driven from the event rather than
 *       a write operation (which git, being read-only, never performs).</li>
 * </ul>
 *
 * <p>The handler is decoupled from the publisher through the event bus, so it can reference
 * {@link RegistryService}/{@link StageActionService} (which in turn reference the storage
 * services) without the storage → dispatcher activation cycle that blocks a backend from
 * driving dispatch directly.
 */
@RequireTypedEvent
@Component(service = UntypedEventHandler.class, property = TypedEventConstants.TYPED_EVENT_TOPICS + "="
		+ RegistryResync.TOPIC)
public class RegistryResyncHandler implements UntypedEventHandler {

	private static final Logger LOGGER = Logger.getLogger(RegistryResyncHandler.class.getName());

	private static final String EPACKAGE_TYPE = EcoreUtil.getURI(EcorePackage.Literals.EPACKAGE).toString();

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	@SuppressWarnings("rawtypes")
	private volatile List<RegistryService> registryServices;

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	private volatile List<StageActionService> stageActionServices;

	@Override
	public void notifyUntyped(String topic, Map<String, Object> event) {
		Object rawScope = event.get(RegistryResync.KEY_SCOPE);
		if (!(rawScope instanceof String scope) || scope.isBlank()) {
			LOGGER.warning(() -> "Ignoring registry resync event without a valid scope: " + event);
			return;
		}
		replayEnter(scope, event);
		unregisterRemoved(scope, event);
	}

	/** ENTER replay for add/change: re-register everything currently present in the scope. */
	private void replayEnter(String scope, Map<String, Object> event) {
		@SuppressWarnings("rawtypes")
		List<RegistryService> services = registryServices;
		if (services == null || services.isEmpty()) {
			return;
		}
		LOGGER.info(() -> "Registry resync (ENTER) for scope " + scope + "; replaying " + services.size()
				+ " registr(y/ies)");
		for (@SuppressWarnings("rawtypes")
		RegistryService rs : services) {
			try {
				rs.activate(scope);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING,
						"Registry resync replay failed for scope " + scope + " on " + rs.getClass().getSimpleName(), e);
			}
		}
	}

	/** EXIT for removed schemas (D8-3): unregister the EPackage of each removed objectId. */
	private void unregisterRemoved(String scope, Map<String, Object> event) {
		String[] removed = toStringArray(event.get(RegistryResync.KEY_REMOVED_OBJECT_IDS));
		if (removed.length == 0) {
			return;
		}
		Object rawStage = event.get(RegistryResync.KEY_STAGE);
		if (!(rawStage instanceof String stage) || stage.isBlank()) {
			LOGGER.warning(() -> "Registry resync has removed objectIds but no stage; skipping EXIT: " + event);
			return;
		}
		List<StageActionService> actions = stageActionServices;
		if (actions == null || actions.isEmpty()) {
			return;
		}
		for (String objectId : removed) {
			ActionContext ctx = new ActionContext(scope, "", objectId, EPACKAGE_TYPE, stage, null, null,
					ExitReason.DELETED, "system", Instant.now(), "git resync: schema removed", false, Map.of());
			dispatchExit(ctx, actions);
		}
		LOGGER.info(() -> "Registry resync (EXIT) for scope " + scope + " stage " + stage + ": unregistered "
				+ removed.length + " removed schema(s)");
	}

	/** Mirrors {@code RegistryServiceImpl.dispatch(EXIT, ctx)} for a single removed schema. */
	private void dispatchExit(ActionContext ctx, List<StageActionService> actions) {
		for (StageActionService sas : actions) {
			if (!sas.supportsObjectType(ctx.objectType())) {
				continue;
			}
			Set<String> triggerStages = sas.getTriggerStages();
			if (!triggerStages.isEmpty() && !triggerStages.contains(ctx.stage())) {
				continue;
			}
			Set<ActionEvent> triggerEvents = sas.getTriggerEvents();
			if (!triggerEvents.isEmpty() && !triggerEvents.contains(ActionEvent.EXIT)) {
				continue;
			}
			try {
				sas.onExit(ctx).onFailure(t -> LOGGER.log(Level.WARNING,
						"Resync EXIT failed for " + ctx.objectId() + " on " + sas.getClass().getSimpleName(), t));
			} catch (Exception e) {
				LOGGER.log(Level.WARNING,
						"Resync EXIT threw for " + ctx.objectId() + " on " + sas.getClass().getSimpleName(), e);
			}
		}
	}

	private static String[] toStringArray(Object value) {
		if (value instanceof String[] arr) {
			return arr;
		}
		if (value instanceof List<?> list) {
			return list.stream().filter(String.class::isInstance).map(String.class::cast).toArray(String[]::new);
		}
		return new String[0];
	}
}
