package info.novatec.inspectit.rcp.preferences;

import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

/**
 * Initializes the default preferences.
 * 
 * @author Patrice Bouillet
 * 
 */
public class InspectITPreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 * Default CMR ip address.
	 */
	private static final String DEFAULT_IP = "localhost";

	/**
	 * Default CMR port.
	 */
	private static final int DEFAULT_PORT = 8182;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initializeDefaultPreferences() {
		List<CmrRepositoryDefinition> defaultCmrList = new ArrayList<CmrRepositoryDefinition>(1);
		defaultCmrList.add(new CmrRepositoryDefinition(DEFAULT_IP, DEFAULT_PORT));
		PreferencesUtils.saveCmrRepositoryDefinitions(defaultCmrList, true);
	}

}
