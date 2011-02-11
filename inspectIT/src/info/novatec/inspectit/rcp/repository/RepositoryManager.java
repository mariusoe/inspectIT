package info.novatec.inspectit.rcp.repository;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.util.ListenerList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * The repository manager stores all the repository definitions.
 * 
 * @author Patrice Bouillet
 * 
 */
public class RepositoryManager {

	/**
	 * The preference value for the host.
	 */
	private static final String HOST = "server_host_";

	/**
	 * The preference value for the port.
	 */
	private static final String PORT = "server_port_";

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
		IPreferencesService service = Platform.getPreferencesService();
		Preferences configurationNode = new ConfigurationScope().getNode(InspectIT.ID);
		Preferences defaultNode = new DefaultScope().getNode(InspectIT.ID);
		Preferences[] nodes = new Preferences[] { configurationNode, defaultNode };

		// load existing definitions
		for (int i = 1; i < Integer.MAX_VALUE; i++) {
			String ip = service.get(HOST + i, null, nodes);
			int port = Integer.parseInt(service.get(PORT + i, "8182", nodes));
			if (null != ip) {
				repositoryDefinitions.add(new CmrRepositoryDefinition(ip, port));
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
		IEclipsePreferences node = new ConfigurationScope().getNode(InspectIT.ID);
		// first, remove all existing preferences
		for (int i = 1; i < Integer.MAX_VALUE; i++) {
			String ip = node.get(HOST + i, null);
			if (null != ip) {
				node.remove(HOST + i);
				node.remove(PORT + i);
			} else {
				break;
			}
		}
		// second, add the details again
		int id = 1;
		for (RepositoryDefinition repositoryDefinition : repositoryDefinitions) {
			node.put(HOST + id, repositoryDefinition.getIp());
			node.putInt(PORT + id, repositoryDefinition.getPort());
			id++;
		}
		// last, flush/save the settings
		try {
			node.flush();
		} catch (BackingStoreException e) {
			InspectIT.getDefault().createErrorDialog("Could not save the preferences to the backing store!", e, -1);
		}
	}

}
