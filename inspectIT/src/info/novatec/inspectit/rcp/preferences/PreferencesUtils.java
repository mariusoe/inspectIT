package info.novatec.inspectit.rcp.preferences;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.preferences.valueproviders.PreferenceValueProviderFactory;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * Utility for using preferences stores.
 * 
 * @author Ivan Senic
 * 
 */
public final class PreferencesUtils {

	/**
	 * Private constructor.
	 */
	private PreferencesUtils() {
	}

	/**
	 * Saves a string value to the preference store.
	 * 
	 * @param name
	 *            Name of the preference.
	 * @param value
	 *            Value to be saved.
	 * @param isDefault
	 *            If this is true, the setting will be saved as a default preference. Not that the
	 *            default preferences are not saved to disk, and have to be entered manually. If it
	 *            is false, preference will be saved in the configuration scope.
	 */
	public static void saveStringValue(String name, String value, boolean isDefault) {
		ScopedPreferenceStore preferenceStore = InspectIT.getDefault().getPreferenceStore();
		if (isDefault) {
			preferenceStore.setDefault(name, value);
		} else {
			preferenceStore.setValue(name, value);
		}
		try {
			preferenceStore.save();
		} catch (IOException e) {
			InspectIT.getDefault().createErrorDialog("Error occured trying to save setting with name '" + name + "' to preference store.", e, -1);
		}
	}

	/**
	 * Save given repository definitions to the preference store.
	 * 
	 * @param repositoryDefinitions
	 *            {@link CmrRepositoryDefinition} to save.
	 * @param isDefault
	 *            Is it a default setting.
	 */
	public static void saveCmrRepositoryDefinitions(List<CmrRepositoryDefinition> repositoryDefinitions, boolean isDefault) {
		String valueToSave = null;
		try {
			valueToSave = PreferenceValueProviderFactory.getValueForObject(PreferencesConstants.CMR_REPOSITORY_DEFINITIONS, repositoryDefinitions);
			if (null != valueToSave) {
				saveStringValue(PreferencesConstants.CMR_REPOSITORY_DEFINITIONS, valueToSave, isDefault);
			}
		} catch (PreferenceException e) {
			InspectIT.getDefault().createErrorDialog("Error trying to save CMR repositories to the preference store", e, -1);
		}
	}

	/**
	 * Returns the list of {@link CmrRepositoryDefinition} that exists in the preference store.
	 * 
	 * @return he list of {@link CmrRepositoryDefinition} that exists in the preference store.
	 */
	public static List<CmrRepositoryDefinition> getCmrRepositoryDefinitions() {
		ScopedPreferenceStore preferenceStore = InspectIT.getDefault().getPreferenceStore();
		String values = preferenceStore.getString(PreferencesConstants.CMR_REPOSITORY_DEFINITIONS);
		List<CmrRepositoryDefinition> returnList = null;
		try {
			returnList = PreferenceValueProviderFactory.getObjectFromValue(PreferencesConstants.CMR_REPOSITORY_DEFINITIONS, values);
			return returnList;
		} catch (PreferenceException e) {
			InspectIT.getDefault().createErrorDialog("Error trying to load CMR repositories from the preference store", e, -1);
			return Collections.emptyList();
		}
	}

}
