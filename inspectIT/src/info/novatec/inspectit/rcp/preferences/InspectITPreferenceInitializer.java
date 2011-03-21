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
	 * {@inheritDoc}
	 */
	@Override
	public void initializeDefaultPreferences() {
		List<CmrRepositoryDefinition> defaultCmrList = new ArrayList<CmrRepositoryDefinition>(1);
		CmrRepositoryDefinition defaultCmr = new CmrRepositoryDefinition(CmrRepositoryDefinition.DEFAULT_IP, CmrRepositoryDefinition.DEFAULT_PORT, CmrRepositoryDefinition.DEFAULT_NAME);
		defaultCmr.setDescription(CmrRepositoryDefinition.DEFAULT_DESCRIPTION);
		defaultCmrList.add(defaultCmr);
		PreferencesUtils.saveCmrRepositoryDefinitions(defaultCmrList, true);
	}

}
