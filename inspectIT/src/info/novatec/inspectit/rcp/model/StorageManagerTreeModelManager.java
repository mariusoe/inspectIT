package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tree model manager for storage manager view.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageManagerTreeModelManager {

	/**
	 * Storage and repository map.
	 */
	private Map<StorageData, CmrRepositoryDefinition> storageRespositoryMap;

	/**
	 * Label type for grouping.
	 */
	private AbstractStorageLabelType<?> storageLabelType;

	/**
	 * @param storageRespositoryMap
	 *            map of {@link StorageData} objects and repositories where they are located.
	 * @param storageLabelType
	 *            {@link AbstractStorageLabelType} to define the label ordering. It can be null,
	 *            then Storages will be ordered by repository.
	 */
	public StorageManagerTreeModelManager(Map<StorageData, CmrRepositoryDefinition> storageRespositoryMap, AbstractStorageLabelType<?> storageLabelType) {
		super();
		this.storageRespositoryMap = storageRespositoryMap;
		this.storageLabelType = storageLabelType;
	}

	/**
	 * Returns objects divided either by the provided label class, or by
	 * {@link CmrRepositoryDefinition} they are located to.
	 * 
	 * @return Returns objects divided either by the provided label class, or by
	 *         {@link CmrRepositoryDefinition} they are located to.
	 */
	public Object[] getRootObjects() {
		if (null == storageRespositoryMap || storageRespositoryMap.isEmpty()) {
			return new Object[0];
		}

		if (null != storageLabelType) {
			Composite unknown = new Composite();
			unknown.setName("Unknown");
			unknown.setImage(ImageFormatter.getImageForLabel(storageLabelType));
			boolean addUnknow = false;
			Map<Object, Composite> map = new HashMap<Object, Composite>();
			for (Map.Entry<StorageData, CmrRepositoryDefinition> entry : storageRespositoryMap.entrySet()) {
				List<? extends AbstractStorageLabel<?>> labelList = entry.getKey().getLabels(storageLabelType);
				if (labelList != null && !labelList.isEmpty()) {
					for (AbstractStorageLabel<?> label : labelList) {
						Composite c = map.get(TextFormatter.getLabelValue(label, true));
						if (c == null) {
							c = new Composite();
							c.setName(TextFormatter.getLabelName(label) + ": " + TextFormatter.getLabelValue(label, true));
							c.setImage(ImageFormatter.getImageForLabel(storageLabelType));
							map.put(TextFormatter.getLabelValue(label, true), c);
						}
						StorageLeaf storageLeaf = new StorageLeaf(entry.getKey(), entry.getValue());
						storageLeaf.setParent(c);
						c.addChild(storageLeaf);
					}
				} else {
					unknown.addChild(new StorageLeaf(entry.getKey(), entry.getValue()));
					addUnknow = true;
				}
			}
			ArrayList<Composite> returnList = new ArrayList<Composite>();
			returnList.addAll(map.values());
			if (addUnknow) {
				returnList.add(unknown);
			}
			return returnList.toArray(new Composite[returnList.size()]);
		} else {
			Map<CmrRepositoryDefinition, Composite> map = new HashMap<CmrRepositoryDefinition, Composite>();
			for (Map.Entry<StorageData, CmrRepositoryDefinition> entry : storageRespositoryMap.entrySet()) {
				CmrRepositoryDefinition cmrRepositoryDefinition = entry.getValue();
				Composite c = map.get(cmrRepositoryDefinition);
				if (c == null) {
					c = new Composite();
					c.setName(cmrRepositoryDefinition.getName());
					c.setImage(InspectIT.getDefault().getImage(InspectITConstants.IMG_SERVER_ONLINE_SMALL));
					map.put(cmrRepositoryDefinition, c);
				}
				StorageLeaf storageLeaf = new StorageLeaf(entry.getKey(), entry.getValue());
				storageLeaf.setParent(c);
				c.addChild(storageLeaf);
			}
			return map.values().toArray(new Composite[map.values().size()]);
		}
	}
}
