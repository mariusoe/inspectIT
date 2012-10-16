package info.novatec.inspectit.rcp.preferences;

import info.novatec.inspectit.rcp.model.SensorTypeEnum;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.util.ArrayList;
import java.util.EnumSet;
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
		// CMR list
		List<CmrRepositoryDefinition> defaultCmrList = new ArrayList<CmrRepositoryDefinition>(1);
		CmrRepositoryDefinition defaultCmr = new CmrRepositoryDefinition(CmrRepositoryDefinition.DEFAULT_IP, CmrRepositoryDefinition.DEFAULT_PORT, CmrRepositoryDefinition.DEFAULT_NAME);
		defaultCmr.setDescription(CmrRepositoryDefinition.DEFAULT_DESCRIPTION);
		defaultCmrList.add(defaultCmr);
		PreferencesUtils.saveCmrRepositoryDefinitions(defaultCmrList, true);

		// Editor defaults
		PreferencesUtils.saveIntValue(PreferencesConstants.DECIMAL_PLACES, 0, true);
		PreferencesUtils.saveLongValue(PreferencesConstants.REFRESH_RATE, 5000L, true);
		PreferencesUtils.saveIntValue(PreferencesConstants.ITEMS_COUNT_TO_SHOW, -1, true);
		PreferencesUtils.saveDoubleValue(PreferencesConstants.INVOCATION_FILTER_EXCLUSIVE_TIME, Double.NaN, true);
		PreferencesUtils.saveDoubleValue(PreferencesConstants.INVOCATION_FILTER_TOTAL_TIME, Double.NaN, true);
		PreferencesUtils.saveObject(PreferencesConstants.INVOCATION_FILTER_SENSOR_TYPES,
				EnumSet.of(SensorTypeEnum.TIMER, SensorTypeEnum.INVOCATION_SEQUENCE, SensorTypeEnum.EXCEPTION_SENSOR, SensorTypeEnum.JDBC_STATEMENT, SensorTypeEnum.JDBC_PREPARED_STATEMENT), true);
	}

}
