package info.novatec.inspectit.rcp.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

public class InspectITPreferenceInitializer extends AbstractPreferenceInitializer {
	
	public InspectITPreferenceInitializer() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = new DefaultScope().getNode("info.novatec.inspectit.rcp");
		node.put("server_host_1", "localhost");
		node.putInt("server_port_1", 8080);
	}

}
