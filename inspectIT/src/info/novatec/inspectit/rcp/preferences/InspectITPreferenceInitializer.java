package info.novatec.inspectit.rcp.preferences;

import info.novatec.inspectit.rcp.InspectIT;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Initializes the default preferences.
 * 
 * @author Patrice Bouillet
 * 
 */
public class InspectITPreferenceInitializer extends AbstractPreferenceInitializer {

	public InspectITPreferenceInitializer() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = new ConfigurationScope().getNode(InspectIT.ID);
		node.put("server_host_1", "localhost");
		node.putInt("server_port_1", 8080);
		try {
			node.flush();
		} catch (BackingStoreException e) {
			InspectIT.getDefault().createErrorDialog("Could not save default server settings!", e, -1);
		}
	}

}
