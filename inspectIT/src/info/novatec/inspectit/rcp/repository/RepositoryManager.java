package info.novatec.inspectit.rcp.repository;

import info.novatec.inspectit.rcp.preferences.PreferencesUtils;
import info.novatec.inspectit.rcp.util.ListenerList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The repository manager stores all the repository definitions.
 * 
 * @author Patrice Bouillet
 * 
 */
public class RepositoryManager {

	/**
	 * The list containing the available {@link RepositoryDefinition} objects.
	 */
	private List<RepositoryDefinition> repositoryDefinitions = new ArrayList<RepositoryDefinition>();

	/**
	 * The list of listeners to be notified.
	 */
	private ListenerList<RepositoryChangeListener> repositoryChangeListeners = new ListenerList<RepositoryChangeListener>();

	/**
	 * Starts the repository manager (e.g. loads all the saved data).
	 */
	public void startup() {
		List<CmrRepositoryDefinition> savedCmrs = PreferencesUtils.getCmrRepositoryDefinitions();
		repositoryDefinitions.addAll(savedCmrs);
	}

	/**
	 * Adds a repository definition handled by this manager.
	 * 
	 * @param repositoryDefinition
	 *            The definition to add.
	 */
	public void addRepositoryDefinition(RepositoryDefinition repositoryDefinition) {
		repositoryDefinitions.add(repositoryDefinition);

		savePreference();

		for (RepositoryChangeListener repositoryChangeListener : repositoryChangeListeners) {
			repositoryChangeListener.repositoryAdded(repositoryDefinition);
		}
	}

	/**
	 * Removes a repository definition and notifies all registered listeners.
	 * 
	 * @param repositoryDefinition
	 *            The definition to remove.
	 */
	public void removeRepositoryDefinition(RepositoryDefinition repositoryDefinition) {
		repositoryDefinitions.remove(repositoryDefinition);

		savePreference();

		for (RepositoryChangeListener repositoryChangeListener : repositoryChangeListeners) {
			repositoryChangeListener.repositoryRemoved(repositoryDefinition);
		}
	}

	/**
	 * Notifies all listeners that a certain repository definition has been updated.
	 * 
	 * @param repositoryDefinition
	 *            The definition which was updated.
	 */
	public void updateRepositoryDefinition(RepositoryDefinition repositoryDefinition) {
		savePreference();

		for (RepositoryChangeListener repositoryChangeListener : repositoryChangeListeners) {
			repositoryChangeListener.updateRepository(repositoryDefinition);
		}
	}

	/**
	 * Notifies all listeners that the storage repository has been updated.
	 */
	public void updateStorageRepository() {
		for (RepositoryChangeListener repositoryChangeListener : repositoryChangeListeners) {
			repositoryChangeListener.updateStorageRepository();
		}
	}

	/**
	 * Returns all registered repository definitions handled by this manager. The list is
	 * unmodifiable.
	 * 
	 * @return The list of repository definitions.
	 */
	public List<RepositoryDefinition> getRepositoryDefinitions() {
		return Collections.unmodifiableList(repositoryDefinitions);
	}

	/**
	 * Adds a listener which notifies on certain events.
	 * 
	 * @param repositoryChangeListener
	 *            The listener to add.
	 */
	public void addRepositoryChangeListener(RepositoryChangeListener repositoryChangeListener) {
		repositoryChangeListeners.add(repositoryChangeListener);
	}

	/**
	 * Removes the listener.
	 * 
	 * @param repositoryChangeListener
	 *            The listener to remove.
	 */
	public void removeRepositoryChangeListener(RepositoryChangeListener repositoryChangeListener) {
		repositoryChangeListeners.remove(repositoryChangeListener);
	}

	/**
	 * Save the preferences to the backend store.
	 */
	private void savePreference() {
		List<CmrRepositoryDefinition> toSave = new ArrayList<CmrRepositoryDefinition>();
		for (RepositoryDefinition repositoryDefinition : repositoryDefinitions) {
			if (repositoryDefinition instanceof CmrRepositoryDefinition) {
				toSave.add((CmrRepositoryDefinition) repositoryDefinition);
			}
		}
		PreferencesUtils.saveCmrRepositoryDefinitions(toSave, false);
	}

}
