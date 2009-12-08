package info.novatec.novaspy.rcp.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

public class NovaSpyPreferenceInitializer extends AbstractPreferenceInitializer {
	
	public NovaSpyPreferenceInitializer() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = new DefaultScope().getNode("info.novatec.novaspy.rcp");
		node.put("server_host_1", "localhost");
		node.putInt("server_port_1", 8080);
	}

}
