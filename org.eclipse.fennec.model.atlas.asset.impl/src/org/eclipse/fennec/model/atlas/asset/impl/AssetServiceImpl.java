package org.eclipse.fennec.model.atlas.asset.impl;

import java.io.IOException;
import java.util.logging.Logger;

import org.eclipse.fennec.model.atlas.asset.api.AssetService;
import org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.ModelAtlasAssetPackage;
import org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset;
import org.eclipse.fennec.persistence.repository.api.Repository;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import inventory.AssetRelation;

@Component(name="ModelAtlasAssetService")
public class AssetServiceImpl implements AssetService{
	
	private static final Logger LOGGER = Logger.getLogger(AssetServiceImpl.class.getName());
	
	@Reference(target = "(scope=no-inject)")
	private Repository repository;


	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.asset.api.AssetService#saveAsset(org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset)
	 */
	@Override
	public StageableAsset saveAsset(StageableAsset asset) {
		try {
			repository.save(asset);
		} catch (IOException e) {
			LOGGER.severe(String.format("IOException while saving StageableAsset with id %s: %s", asset.getId(), e.getMessage()));
			return null;
		}
		return asset;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.asset.api.AssetService#getAssetById(java.lang.String)
	 */
	@Override
	public StageableAsset getAssetById(String assetId) {
		try {
			StageableAsset asset = (StageableAsset) repository.getEObject(ModelAtlasAssetPackage.Literals.STAGEABLE_ASSET, assetId);
			return asset;
		} catch (IOException e) {
			LOGGER.severe(String.format("IOException while retrieving StageableAsset with id %s: %s", assetId, e.getMessage()));
			return null;
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.asset.api.AssetService#deleteAsset(java.lang.String)
	 */
	@Override
	public boolean deleteAsset(String assetId) {
//		TODO: When an Asset is deleted we should also delete all the AssetRelation that has that Asset as target inside other assets
		
		StageableAsset asset = getAssetById(assetId);
		if(asset == null) {
			LOGGER.severe(String.format("Cannot delete asset %s because asset retrieval failed", assetId));
			return false;
		}
		try {
			repository.delete(asset);
		} catch (IOException e) {
			LOGGER.severe(String.format("Cannot delete asset %s because asset deletion failed with %s", assetId, e.getMessage()));
			return false;
		}
		return true;		
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.asset.api.AssetService#addAssetRelation(inventory.AssetRelation, java.lang.String)
	 */
	@Override
	public boolean addAssetRelation(AssetRelation assetRelation, String assetId) {
		StageableAsset asset = getAssetById(assetId);
		if(asset == null) {
			LOGGER.severe(String.format("Cannot add AssetRelation to asset %s because asset retrieval failed", assetId));
			return false;
		}
		asset.getRelations().add(assetRelation);
		asset = saveAsset(asset);
		if(asset == null) {
			LOGGER.severe(String.format("Cannot add AssetRelation to asset %s because asset save operation failed", assetId));
			return false;
		}
		return true;
	}
}
