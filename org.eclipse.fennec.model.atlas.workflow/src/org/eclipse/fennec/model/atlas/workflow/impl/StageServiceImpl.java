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
package org.eclipse.fennec.model.atlas.workflow.impl;

import org.eclipse.fennec.model.atlas.wf.workflowapi.StageService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.Designate;

/**
 * 
 * @author ilenia
 * @since Jan 13, 2026
 */
@Component(name = "StageService", configurationPid = "StageService", configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = StageServiceConfig.class)
public class StageServiceImpl implements StageService {

	private StageServiceConfig config;

	@Activate
	public StageServiceImpl(StageServiceConfig config) {
		this.config = config;		
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.StageService#getStageName()
	 */
	@Override
	public String getStageName() {
		return config.stage_name();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.StageService#isWritable()
	 */
	@Override
	public boolean isWritable() {
		return config.stage_writable();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.StageService#isFinalStage()
	 */
	@Override
	public boolean isFinalStage() {
		return config.stage_final();
	}

}
