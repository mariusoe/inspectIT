package info.novatec.novaspy.rcp.repository;

import info.novatec.novaspy.rcp.NovaSpy;
import info.novatec.novaspy.rcp.util.ListenerList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Preferences;

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
		Preferences preferences = NovaSpy.getDefault().getPluginPreferences();

		for (int i = 1; i < Integer.MAX_VALUE; i++) {
			String ip = preferences.getString("server_host_" + i);
			int port = preferences.getInt("server_port_" + i);
			if (!"".equals(ip)) {
				addRepositoryDefinition(new RepositoryDefinition(ip, port));
			} else {
				break;
			}
		}
	}

	/**
	 * Adds a repository definition handled by this manager.
	 * 
	 * @param repositoryDefinition
	 *            The definition to add.
	 */
	public void addRepositoryDefinition(RepositoryDefinition repositoryDefinition) {
		repositoryDefinitions.add(repositoryDefinition);

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

		for (RepositoryChangeListener repositoryChangeListener : repositoryChangeListeners) {
			repositoryChangeListener.repositoryRemoved(repositoryDefinition);
		}
	}

	/**
	 * Notifies all listeners that a certain repository definition has been
	 * updated.
	 * 
	 * @param repositoryDefinition
	 *            The definition which was updated.
	 */
	public void updateRepositoryDefinition(RepositoryDefinition repositoryDefinition) {
		for (RepositoryChangeListener repositoryChangeListener : repositoryChangeListeners) {
			repositoryChangeListener.updateRepository(repositoryDefinition);
		}
	}

	/**
	 * Returns all registered repository definitions handled by this manager.
	 * The list is unmodifiable.
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
	 * Persist the repository list.
	 */
	public void shutdown() {
		Preferences preferences = NovaSpy.getDefault().getPluginPreferences();

		// remove all old saved server definitions
		for (int i = 1; i < Integer.MAX_VALUE; i++) {
			String ip = preferences.getString("server_host_" + i);
			if (!"".equals(ip)) {
				preferences.setValue("server_host_" + i, "");
				preferences.setValue("server_port_" + i, 0);
			} else {
				break;
			}
		}

		int i = 1;
		for (RepositoryDefinition repositoryDefinition : repositoryDefinitions) {
			preferences.setValue("server_host_" + i, repositoryDefinition.getIp());
			preferences.setValue("server_port_" + i, repositoryDefinition.getPort());
			i++;
		}
	}

}
