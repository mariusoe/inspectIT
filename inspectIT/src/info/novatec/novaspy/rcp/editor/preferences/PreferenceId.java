package info.novatec.novaspy.rcp.editor.preferences;

import info.novatec.novaspy.rcp.editor.preferences.control.SamplingRateControl;
import info.novatec.novaspy.rcp.editor.preferences.control.TimeLineControl;

/**
 * The enumeration set for the unique preference group ids. By adding new
 * enumerations you should also create an inner public enumeration class which
 * contains the associated control ids.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public enum PreferenceId {

	/**
	 * The identifiers of the different control groups.
	 */
	TIMELINE, SAMPLINGRATE, LIVEMODE, UPDATE, ITEMCOUNT;

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
		BUTTON_LIVE_ID;
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

}
