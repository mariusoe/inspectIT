package info.novatec.inspectit.rcp.editor.preferences.control;

import info.novatec.inspectit.rcp.editor.preferences.IPreferenceGroup;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * This class creates a control group for the preference panel. It contains a time line where you
 * can select a time range for a historical view.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class TimeLineControl implements IPreferenceControl {

	/**
	 * The unique id of this preference control.
	 */
	private static final PreferenceId CONTROL_GROUP_ID = PreferenceId.TIMELINE;

	/**
	 * The map containing all configurable values by the slider for the day spinner.
	 */
	private Map<Integer, Integer> daysValueMap;

	/**
	 * The map containing all configurable values by the slider for the hour spinner.
	 */
	private Map<Integer, Integer> hoursValueMap;

	/**
	 * The map containing all configurable values by the slider for the minute spinner.
	 */
	private Map<Integer, Integer> minutesValueMap;

	/**
	 * The slider used for adjusting the three spinnners.
	 */
	private Scale slider = null;

	/**
	 * The spinner for day selection.
	 */
	private Spinner spinnerDays;

	/**
	 * The spinner for hour selection.
	 */
	private Spinner spinnerHours;

	/**
	 * The spinner for minute selection.
	 */
	private Spinner spinnerMinutes;

	/**
	 * The date/time selection.
	 */
	private CDateTime cDateTime;

	/**
	 * Indicates the time range start time.
	 */
	private GregorianCalendar toDate = new GregorianCalendar();

	/**
	 * Indicates the time range end time.
	 */
	private GregorianCalendar fromDate = new GregorianCalendar();

	/**
	 * Used for temporary saving the old value.
	 */
	private GregorianCalendar oldFromDate = new GregorianCalendar();

	/**
	 * Used for temporary saving the old value.
	 */
	private GregorianCalendar oldToDate = new GregorianCalendar();

	/**
	 * {@inheritDoc}
	 */
	public Composite createControls(Composite parent, FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR);
		section.setText("Timerange / Until");
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		Composite timeLineRow = toolkit.createComposite(section);
		section.setClient(timeLineRow);

		GridLayout timelineRowLayout = new GridLayout(4, false);
		timelineRowLayout.marginLeft = 15;
		timelineRowLayout.horizontalSpacing = 25;
		GridData firstGrid = new GridData(SWT.MAX, SWT.DEFAULT);
		firstGrid.grabExcessHorizontalSpace = true;
		timeLineRow.setLayout(timelineRowLayout);
		timeLineRow.setLayoutData(firstGrid);

		Composite innerComposite = new Composite(timeLineRow, SWT.NONE);
		GridLayout innerCompLayout = new GridLayout(7, true);
		innerCompLayout.marginLeft = 10;
		innerComposite.setLayout(innerCompLayout);
		innerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		final Map<Integer, Integer> mappingTable = createMappingTable();

		spinnerDays = new Spinner(innerComposite, SWT.HORIZONTAL | SWT.BORDER);
		spinnerDays.setMaximum(999);
		toolkit.adapt(spinnerDays, false, true);
		toolkit.createLabel(innerComposite, "Days", SWT.LEFT);

		spinnerDays.addSelectionListener(new SelectionAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (slider != null) {
					int value = spinnerDays.getSelection();
					if (daysValueMap.containsValue(value)) {
						slider.setSelection(getKeyFromValue(daysValueMap, value));
					}
				}
			}

		});

		spinnerHours = new Spinner(innerComposite, SWT.HORIZONTAL | SWT.BORDER);
		spinnerHours.setMaximum(23);
		spinnerHours.setSelection(1);
		toolkit.adapt(spinnerHours, false, true);
		spinnerHours.addSelectionListener(new SelectionAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (slider != null) {
					if (spinnerDays.getSelection() == 0) {
						int value = spinnerHours.getSelection();
						if (hoursValueMap.containsValue(value)) {
							slider.setSelection(getKeyFromValue(hoursValueMap, value));
						}
					}
				}
			}
		});

		toolkit.createLabel(innerComposite, "Hours", SWT.LEFT);
		spinnerMinutes = new Spinner(innerComposite, SWT.HORIZONTAL | SWT.BORDER);
		spinnerMinutes.setMaximum(59);
		toolkit.adapt(spinnerMinutes, false, true);
		toolkit.createLabel(innerComposite, "Minutes", SWT.LEFT);

		spinnerMinutes.addSelectionListener(new SelectionAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (slider != null) {
					if ((spinnerDays.getSelection() == 0) && (spinnerHours.getSelection() == 0)) {
						int value = spinnerMinutes.getSelection();
						if (minutesValueMap.containsValue(value)) {
							slider.setSelection(getKeyFromValue(minutesValueMap, value));
						}
					}
				}
			}

		});

		toolkit.createLabel(timeLineRow, "to Date:");
		cDateTime = new CDateTime(timeLineRow, CDT.BORDER | CDT.DROP_DOWN | SWT.RIGHT);
		toolkit.adapt(cDateTime, false, true);
		cDateTime.setFormat(CDT.DATE_SHORT | CDT.TIME_SHORT);
		cDateTime.setSelection(toDate.getTime());
		GridData cdtGrid = new GridData(150, 30);
		cdtGrid.grabExcessHorizontalSpace = true;
		cdtGrid.grabExcessVerticalSpace = true;
		cDateTime.setLayoutData(cdtGrid);
		cDateTime.setEnabled(true);

		toolkit.createLabel(timeLineRow, "", SWT.NONE);
		slider = new Scale(timeLineRow, SWT.HORIZONTAL);
		slider.setMinimum(0);
		slider.setMaximum(30);
		slider.setIncrement(1);
		slider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		slider.setSelection(5);

		slider.addSelectionListener(new SelectionAdapter() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void widgetSelected(SelectionEvent event) {
				int value = slider.getSelection();
				int minuteAreaSize = minutesValueMap.size();
				int hoursAreaSize = hoursValueMap.size();
				int hoursArea = hoursAreaSize + minuteAreaSize;
				int daysAreaSize = daysValueMap.size();
				int daysArea = daysAreaSize + hoursArea;

				if (value < minuteAreaSize) {
					spinnerMinutes.setSelection(mappingTable.get(value));
					spinnerHours.setSelection(0);
					spinnerDays.setSelection(0);
				} else if ((value >= minuteAreaSize) && (value < hoursArea)) {
					spinnerHours.setSelection(mappingTable.get(value));
					spinnerMinutes.setSelection(0);
					spinnerDays.setSelection(0);
				} else if ((value >= hoursArea) && (value < daysArea)) {
					spinnerDays.setSelection(mappingTable.get(value));
					spinnerMinutes.setSelection(0);
					spinnerHours.setSelection(0);
				}
			}
		});

		oldToDate.setTime(cDateTime.getSelection());
		oldFromDate.setTime(oldToDate.getTime());
		// default state is 10 minutes before the current time
		oldFromDate.add(GregorianCalendar.MINUTE, -10);

		return timeLineRow;
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<IPreferenceGroup, Object> eventFired() {
		Map<IPreferenceGroup, Object> preferenceControlMap = new HashMap<IPreferenceGroup, Object>();
		toDate.setTime(cDateTime.getSelection());
		fromDate.setTime(toDate.getTime());
		fromDate.add(GregorianCalendar.DAY_OF_WEEK, -(spinnerDays.getSelection()));
		fromDate.add(GregorianCalendar.HOUR_OF_DAY, -(spinnerHours.getSelection()));
		fromDate.add(GregorianCalendar.MINUTE, -(spinnerMinutes.getSelection()));

		if (oldToDate.getTime().getTime() != toDate.getTime().getTime()) {
			preferenceControlMap.put(PreferenceId.TimeLine.TO_DATE_ID, toDate.getTime());
			oldToDate.setTime(toDate.getTime());
		}
		if (oldFromDate.getTime().getTime() != fromDate.getTime().getTime()) {
			preferenceControlMap.put(PreferenceId.TimeLine.FROM_DATE_ID, fromDate.getTime());
			oldFromDate.setTime(fromDate.getTime());
		}

		return preferenceControlMap;
	}

	/**
	 * {@inheritDoc}
	 */
	public PreferenceId getControlGroupId() {
		return CONTROL_GROUP_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
	}

	/**
	 * Creates a mapping table for the slider and the three spinners.
	 * 
	 * @return the map containing the mapping table.
	 */
	private Map<Integer, Integer> createMappingTable() {
		List<Integer> daysValueList = new ArrayList<Integer>();
		Collections.addAll(daysValueList, 1, 2, 4, 8, 15, 30, 60, 90, 120, 150, 180, 210, 240, 270, 300, 330, 360);

		List<Integer> hoursValueList = new ArrayList<Integer>();
		Collections.addAll(hoursValueList, 1, 2, 4, 8, 11, 15, 18, 21, 23);

		List<Integer> minutesValueList = new ArrayList<Integer>();
		Collections.addAll(minutesValueList, 1, 12, 24, 36, 48);

		daysValueMap = new HashMap<Integer, Integer>();
		hoursValueMap = new HashMap<Integer, Integer>();
		minutesValueMap = new HashMap<Integer, Integer>();
		Map<Integer, Integer> mappingTable = new HashMap<Integer, Integer>();
		int counter = 0;

		for (Integer value : minutesValueList) {
			minutesValueMap.put(counter, value);
			++counter;
		}

		counter = minutesValueList.size();
		for (Integer value : hoursValueList) {
			hoursValueMap.put(counter, value);
			++counter;
		}

		counter = minutesValueList.size() + hoursValueList.size();
		for (Integer value : daysValueList) {
			daysValueMap.put(counter, value);
			++counter;
		}

		mappingTable.putAll(minutesValueMap);
		mappingTable.putAll(hoursValueMap);
		mappingTable.putAll(daysValueMap);

		return mappingTable;
	}

	/**
	 * Gets a key from a value.
	 * 
	 * @param map
	 *            The Map in which the search will be performed.
	 * @param value
	 *            the value to be searched for.
	 * @return the value.
	 */
	private Integer getKeyFromValue(Map<Integer, Integer> map, Integer value) {
		for (Map.Entry<Integer, Integer> entrySet : map.entrySet()) {
			if (entrySet.getValue().equals(value)) {
				return entrySet.getKey();
			}
		}
		return null;
	}
}
