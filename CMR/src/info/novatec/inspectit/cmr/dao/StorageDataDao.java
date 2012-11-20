package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.SystemInformationData;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

import java.util.Collection;
import java.util.List;

/**
 * Storage data dao interface.
 * 
 * @author Ivan Senic
 * 
 */
public interface StorageDataDao {

	/**
	 * Saves a label to the database if the same label does not exists.
	 * 
	 * @param label
	 *            Label to save.
	 * @return True if label was saved, false otherwise.
	 */
	boolean saveLabel(AbstractStorageLabel<?> label);

	/**
	 * Removes a label.
	 * 
	 * @param label
	 *            Label to remove.
	 */
	void removeLabel(AbstractStorageLabel<?> label);

	/**
	 * Removes a collection of labels.
	 * 
	 * @param labels
	 *            Labels.
	 */
	void removeLabels(Collection<AbstractStorageLabel<?>> labels);

	/**
	 * Returns all labels registered on the CMR.
	 * 
	 * @return Returns all labels registered on the CMR.
	 */
	List<AbstractStorageLabel<?>> getAllLabels();

	/**
	 * Returns all labels of specified type registered on the CMR.
	 * 
	 * @param <E>
	 *            Type of label.
	 * @param labelType
	 *            Label type.
	 * @return Returns all labels of specified type registered on the CMR.
	 */
	<E> List<AbstractStorageLabel<E>> getAllLabelsForType(AbstractStorageLabelType<E> labelType);

	/**
	 * Saves the {@link AbstractStorageLabelType} to the database. The abel will be saved only if
	 * the {@link AbstractStorageLabelType#isMultiType()} is true or no instances of the label type
	 * are already saved.
	 * 
	 * @param labelType
	 *            Label type to save.
	 */
	void saveLabelType(AbstractStorageLabelType<?> labelType);

	/**
	 * Removes label type from database.
	 * 
	 * @param labelType
	 *            Label type to remove.
	 * @throws Exception
	 *             If there are still labels of this type existing in the database.
	 */
	void removeLabelType(AbstractStorageLabelType<?> labelType) throws Exception;

	/**
	 * Returns all instances of desired label type.
	 * 
	 * @param <E>
	 *            Label value type.
	 * @param labelTypeClass
	 *            Label type class.
	 * @return List of all instances.
	 */
	<E extends AbstractStorageLabelType<?>> List<E> getLabelTypes(Class<E> labelTypeClass);

	/**
	 * Returns all label types.
	 * 
	 * @return Returns all label types.
	 */
	List<AbstractStorageLabelType<?>> getAllLabelTypes();

	/**
	 * Returns all the data that is indexed in the indexing tree for a specific platform ident. Not
	 * that is possible that some data is contained two times in the return list, ones as a object
	 * in the list, ones as a part of invocation that is in the list.
	 * 
	 * @param platformId
	 *            Id of agent.
	 * @return List of {@link DefaultData} objects.
	 */
	List<DefaultData> getAllDefaultDataForAgent(long platformId);

	/**
	 * Returns the fresh data from the buffer from the given template list. This data is later used
	 * to be copied to storage.
	 * 
	 * @param copyDataList
	 *            Template list.
	 * @return Data to be store in storage.
	 */
	List<DefaultData> getDataFromCopyTemplateList(List<DefaultData> copyDataList);

	/**
	 * Returns the last {@link SystemInformationData} for every agent provided in the list.
	 * 
	 * @param agentIds
	 *            Collection of agent IDs.
	 * @return List of {@link SystemInformationData}.
	 */
	List<SystemInformationData> getSystemInformationData(Collection<Long> agentIds);

}