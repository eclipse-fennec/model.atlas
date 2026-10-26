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

import java.util.Set;

import org.eclipse.fennec.model.atlas.action.api.ActionContext;
import org.eclipse.fennec.model.atlas.action.api.Promise;
import org.eclipse.fennec.model.atlas.action.api.StageActionService;

/**
 * 
 * @author ilenia
 * @since Oct 26, 2026
 */
public class AssetStageActionService implements StageActionService {

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.action.api.StageActionService#supportsObjectType(java.lang.String)
	 */
	@Override
	public boolean supportsObjectType(String objectType) {
		// TODO Auto-generated method stub
		return false;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.action.api.StageActionService#getTriggerStages()
	 */
	@Override
	public Set<String> getTriggerStages() {
		// TODO Auto-generated method stub
		return null;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.action.api.StageActionService#getTriggerEvents()
	 */
	@Override
	public Set<ActionEvent> getTriggerEvents() {
		// TODO Auto-generated method stub
		return null;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.action.api.StageActionService#onEnter(org.eclipse.fennec.model.atlas.action.api.ActionContext)
	 */
	@Override
	public Promise<Void> onEnter(ActionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.action.api.StageActionService#onUpdate(org.eclipse.fennec.model.atlas.action.api.ActionContext)
	 */
	@Override
	public Promise<Void> onUpdate(ActionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.action.api.StageActionService#onExit(org.eclipse.fennec.model.atlas.action.api.ActionContext)
	 */
	@Override
	public Promise<Void> onExit(ActionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.action.api.StageActionService#requiresReplayOnStartup()
	 */
	@Override
	public boolean requiresReplayOnStartup() {
		// TODO Auto-generated method stub
		return false;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.action.api.StageActionService#requiresReplayOnShutdown()
	 */
	@Override
	public boolean requiresReplayOnShutdown() {
		// TODO Auto-generated method stub
		return false;
	}

}
