package info.novatec.inspectit.rcp.editor.preferences;

import info.novatec.inspectit.rcp.editor.preferences.control.SamplingRateControl;
import info.novatec.inspectit.rcp.editor.preferences.control.TimeLineControl;

/**
 * The enumeration set for the unique preference group ids. By adding new enumerations you should
 * also create an inner public enumeration class which contains the associated control ids.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public enum PreferenceId {

	/**
	 * The identifiers of the different control groups.
	 */
	TIMELINE, SAMPLINGRATE, LIVEMODE, UPDATE, ITEMCOUNT, FILTERSENSORTYPE, INVOCFILTEREXCLUSIVETIME, INVOCFILTERTOTALTIME;

	/**
	 * Inner enumeration for TIMELINE.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	public enum TimeLine implements IPreferenceGroup {
		/**
		 * The identifiers of the elements in the {@link TimeLineControl}.
		 */
		FROM_DATE_ID, TO_DATE_ID;
	}

	/**
	 * Inner enumeration for SAMPLINGRATE.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	public enum SamplingRate implements IPreferenceGroup {
		/**
		 * The identifiers of the elements in the {@link SamplingRateControl} .
		 */
		SLIDER_ID, DIVIDER_ID, TIMEFRAME_DIVIDER_ID;
	}

	/**
	 * Inner enumeration for LIVEMODE.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	public enum LiveMode implements IPreferenceGroup {
		/**
		 * The identifier for the live button.
		 */
		BUTTON_LIVE_ID, REFRESH_RATE;
	}

	/**
	 * Inner enumeration for ITEMCOUNT.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	public enum ItemCount implements IPreferenceGroup {
		/**
		 * The identifier for the item count.
		 */
		COUNT_SELECTION_ID;
	}

	/**
	 * Inner enumeration for the SENSORTYPESELECTION.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	public enum SensorTypeSelection implements IPreferenceGroup {
		/**
		 * The identifier for the sensor type selections.-
		 */
		SENSOR_TYPE_SELECTION_ID;
	}

	/**
	 * Inner enumeration for the INVOCEXCLUSIVETIMESELECTION.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	public enum InvocExclusiveTimeSelection implements IPreferenceGroup {
		/**
		 * The identifier for the time selection.
		 */
		TIME_SELECTION_ID;
	}

	/**
	 * Inner enumeration for the INVOCTOTALTIMESELECTION.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	public enum InvocTotalTimeSelection implements IPreferenceGroup {
		/**
		 * The identifier for the time selection.
		 */
		TIME_SELECTION_ID;
	}

}
