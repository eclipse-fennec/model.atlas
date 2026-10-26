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
package org.eclipse.fennec.model.atlas.asset.api;

import org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset;
import org.osgi.annotation.versioning.ProviderType;

import inventory.AssetRelation;

/**
 * 
 * @see ProviderType
 * @since 1.0
 */
@ProviderType
public interface AssetService {

	StageableAsset saveAsset(StageableAsset asset);
	
	StageableAsset getAssetById(String assetId);
	
	boolean deleteAsset(String assetId);
	
	boolean addAssetRelation(AssetRelation assetRelation, String assetId);

}
