package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.provider.IStorageDataProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.storage.StorageData;

import org.eclipse.core.runtime.Assert;

/**
 * Leaf used for displaying the storages in the storage tree.
 *
 * @author Ivan Senic
 *
 */
public class StorageLeaf extends Leaf implements IStorageDataProvider {

	/**
	 * {@link StorageData}.
	 */
	private StorageData storageData;

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Default constructor.
	 *
	 * @param storageData
	 *            {@link StorageData}
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 */
	public StorageLeaf(StorageData storageData, CmrRepositoryDefinition cmrRepositoryDefinition) {
		super();
		Assert.isNotNull(storageData);
		Assert.isNotNull(cmrRepositoryDefinition);
		this.storageData = storageData;
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.setName(storageData.getName());
		this.setImageDescriptor(ImageFormatter.getImageDescriptorForStorageLeaf(storageData));
		this.setTooltip(storageData.getName());
	}

	/**
	 * {@inheritDoc}
	 */
	public StorageData getStorageData() {
		return storageData;
	}

	/**
	 * {@inheritDoc}
	 */
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		return cmrRepositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cmrRepositoryDefinition == null) ? 0 : cmrRepositoryDefinition.hashCode());
		result = prime * result + ((storageData == null) ? 0 : storageData.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		StorageLeaf other = (StorageLeaf) obj;
		if (cmrRepositoryDefinition == null) {
			if (other.cmrRepositoryDefinition != null) {
				return false;
			}
		} else if (!cmrRepositoryDefinition.equals(other.cmrRepositoryDefinition)) {
			return false;
		}
		if (storageData == null) {
			if (other.storageData != null) {
				return false;
			}
		} else if (!storageData.equals(other.storageData)) {
			return false;
		}
		return true;
	}

}
