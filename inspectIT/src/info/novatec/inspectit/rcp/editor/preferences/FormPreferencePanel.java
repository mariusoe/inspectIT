package info.novatec.inspectit.rcp.editor.preferences;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.control.IPreferenceControl;
import info.novatec.inspectit.rcp.model.SensorTypeEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * This is the class where the preference panel is created.
 * 
 * @author Eduard Tudenhoefner
 * @author Patrice Bouillet
 * 
 */
public class FormPreferencePanel implements IPreferencePanel {

	/**
	 * The used toolkit.
	 */
	private final FormToolkit toolkit;

	/**
	 * Callbacks which are containing the fire method which is executed whenever
	 * something is changed and updated.
	 */
	private List<PreferenceEventCallback> callbacks = new ArrayList<PreferenceEventCallback>();

	/**
	 * The button for live mode switching.
	 */
	private Action switchLiveMode;

	/**
	 * The button for switching the preferences.
	 */
	private Action switchPreferences;

	/**
	 * The list of created preference controls.
	 */
	private List<IPreferenceControl> preferenceControlList = new ArrayList<IPreferenceControl>();

	/**
	 * The created section.
	 */
	private Section section;

	/**
	 * The constructor which needs a {@link ViewController} reference.
	 * 
	 * @param toolkit
	 *            The Form toolkit which defines the used colors.
	 */
	public FormPreferencePanel(FormToolkit toolkit) {
		Assert.isNotNull(toolkit);

		this.toolkit = toolkit;
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerCallback(PreferenceEventCallback callback) {
		Assert.isNotNull(callback);

		callbacks.add(callback);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeCallback(PreferenceEventCallback callback) {
		Assert.isNotNull(callback);

		callbacks.remove(callback);
	}

	/**
	 * Fires the event for all registered callbacks.
	 * 
	 * @param event
	 *            The event to fire.
	 */
	private void fireEvent(PreferenceEvent event) {
		for (PreferenceEventCallback callback : callbacks) {
			callback.eventFired(event);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent, Set<PreferenceId> preferenceSet, IToolBarManager toolBarManager) {
		section = toolkit.createSection(parent, Section.NO_TITLE);
		section.setText("Preferences");
		section.setLayout(new GridLayout(1, false));
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		section.setVisible(false);

		Composite innerComposite = toolkit.createComposite(section);
		innerComposite.setLayout(new GridLayout(1, false));
		innerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// only add buttons and some controls if the set is not empty
		if ((null != preferenceSet) && !preferenceSet.isEmpty()) {
			if (null != toolBarManager) {
				createButtons(preferenceSet, toolBarManager);
			}
			createPreferenceControls(innerComposite, preferenceSet);
		}

		section.setClient(innerComposite);
		section.setExpanded(false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setVisible(boolean visible) {
		section.setVisible(visible);
		section.setExpanded(visible);
	}

	/**
	 * {@inheritDoc}
	 */
	public void disableLiveMode() {
		if (switchLiveMode.isChecked()) {
			switchLiveMode.setChecked(false);
			// switchPreferences.setEnabled(!switchPreferences.isEnabled());

			createLiveModeEvent();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void update() {
		if (switchPreferences.isChecked()) {
			for (IPreferenceControl preferenceControl : preferenceControlList) {
				PreferenceEvent event = new PreferenceEvent(preferenceControl.getControlGroupId());
				event.setPreferenceMap(preferenceControl.eventFired());
				fireEvent(event);
			}
		}

		fireEvent(new PreferenceEvent(PreferenceId.UPDATE));
	}

	/**
	 * Creates the preference controls in the preference control panel.
	 * 
	 * @param parent
	 *            The parent {@link Composite} to which the controls will be
	 *            added.
	 * @param preferenceSet
	 *            The set containing the preference IDs.
	 */
	private void createPreferenceControls(Composite parent, Set<PreferenceId> preferenceSet) {
		for (PreferenceId preferenceIdEnum : preferenceSet) {
			IPreferenceControl preferenceControl = PreferenceControlFactory.createPreferenceControls(parent, toolkit, preferenceIdEnum);
			if (null != preferenceControl) {
				preferenceControlList.add(preferenceControl);
			}
		}
	}

	/**
	 * Creates the buttons for this panel.
	 * 
	 * @param preferenceSet
	 *            the list containing the preference ids.
	 * @param toolBarManager
	 *            The tool bar manager.
	 */
	private void createButtons(Set<PreferenceId> preferenceSet, IToolBarManager toolBarManager) {
		switchLiveMode = new SwitchLiveMode("Live");
		switchPreferences = new SwitchPreferences("Preferences");

		if (preferenceSet.contains(PreferenceId.SAMPLINGRATE) || preferenceSet.contains(PreferenceId.TIMELINE)) {
			toolBarManager.add(switchPreferences);
		}
		if (preferenceSet.contains(PreferenceId.LIVEMODE)) {
			toolBarManager.add(switchLiveMode);
		}
		if (preferenceSet.contains(PreferenceId.UPDATE)) {
			toolBarManager.add(new UpdateAction("Update"));
		}

		MenuAction menuAction = new MenuAction();
		if (preferenceSet.contains(PreferenceId.ITEMCOUNT)) {
			MenuManager countMenuManager = new MenuManager("Item count to show");
			countMenuManager.add(new SetItemCountAction("10", 10, true));
			countMenuManager.add(new SetItemCountAction("20", 20));
			countMenuManager.add(new SetItemCountAction("50", 50));
			countMenuManager.add(new SetItemCountAction("100", 100));
			countMenuManager.add(new SetItemCountAction("200", 200));
			countMenuManager.add(new SetItemCountAction("500", 500));
			countMenuManager.add(new SetItemCountAction("All...", -1));
			menuAction.addContributionItem(countMenuManager);
		}
		if (preferenceSet.contains(PreferenceId.FILTERSENSORTYPE)) {
			MenuManager sensorTypeMenuManager = new MenuManager("Filter by SensorType");
			sensorTypeMenuManager.add(new FilterBySensorTypeAction("Timer", SensorTypeEnum.TIMER));
			sensorTypeMenuManager.add(new FilterBySensorTypeAction("Invocation Seq", SensorTypeEnum.INVOCATION_SEQUENCE));
			sensorTypeMenuManager.add(new FilterBySensorTypeAction("Exception", SensorTypeEnum.EXCEPTION_TRACER));
			sensorTypeMenuManager.add(new Separator());
			sensorTypeMenuManager.add(new FilterBySensorTypeAction("JDBC Statement", SensorTypeEnum.JDBC_STATEMENT));
			sensorTypeMenuManager.add(new FilterBySensorTypeAction("JDBC Prep Statement", SensorTypeEnum.JDBC_PREPARED_STATEMENT));
			sensorTypeMenuManager.add(new FilterBySensorTypeAction("JDBC Connection", SensorTypeEnum.JDBC_CONNECTION, false));
			sensorTypeMenuManager.add(new FilterBySensorTypeAction("JDBC Prep Parameter", SensorTypeEnum.JDBC_PREPARED_STATEMENT_PARAMETER, false));
			menuAction.addContributionItem(sensorTypeMenuManager);
		}
		if (preferenceSet.contains(PreferenceId.INVOCFILTEREXCLUSIVETIME)) {
			MenuManager timeMenuManager = new MenuManager("Filter Details by Exclusive Time");
			timeMenuManager.add(new FilterByExclusiveTimeAction("No filter", Double.NaN, true));
			// timeMenuManager.add(new Separator());
			timeMenuManager.add(new FilterByExclusiveTimeAction("0.1 ms", 0.1));
			timeMenuManager.add(new FilterByExclusiveTimeAction("0.2 ms", 0.2));
			timeMenuManager.add(new FilterByExclusiveTimeAction("0.5 ms", 0.5));
			// timeMenuManager.add(new Separator());
			timeMenuManager.add(new FilterByExclusiveTimeAction("1 ms", 1.0));
			timeMenuManager.add(new FilterByExclusiveTimeAction("2 ms", 2.0));
			timeMenuManager.add(new FilterByExclusiveTimeAction("5 ms", 5.0));
			timeMenuManager.add(new FilterByExclusiveTimeAction("10 ms", 10.0));
			// timeMenuManager.add(new Separator());
			timeMenuManager.add(new FilterByExclusiveTimeAction("50 ms", 50.0));
			timeMenuManager.add(new FilterByExclusiveTimeAction("100 ms", 100.0));
			timeMenuManager.add(new FilterByExclusiveTimeAction("200 ms", 200.0));
			timeMenuManager.add(new FilterByExclusiveTimeAction("500 ms", 500.0));
			// timeMenuManager.add(new Separator());
			timeMenuManager.add(new FilterByExclusiveTimeAction("1 s", 1000.0));
			timeMenuManager.add(new FilterByExclusiveTimeAction("1.5 s", 1500.0));
			timeMenuManager.add(new FilterByExclusiveTimeAction("2 s", 2000.0));
			timeMenuManager.add(new FilterByExclusiveTimeAction("5 s", 5000.0));
			menuAction.addContributionItem(timeMenuManager);
		}
		if (preferenceSet.contains(PreferenceId.INVOCFILTERTOTALTIME)) {
			MenuManager timeMenuManager = new MenuManager("Filter Details by Total Time");
			timeMenuManager.add(new FilterByTotalTimeAction("No filter", Double.NaN, true));
			// timeMenuManager.add(new Separator());
			timeMenuManager.add(new FilterByTotalTimeAction("0.1 ms", 0.1));
			timeMenuManager.add(new FilterByTotalTimeAction("0.2 ms", 0.2));
			timeMenuManager.add(new FilterByTotalTimeAction("0.5 ms", 0.5));
			// timeMenuManager.add(new Separator());
			timeMenuManager.add(new FilterByTotalTimeAction("1 ms", 1.0));
			timeMenuManager.add(new FilterByTotalTimeAction("2 ms", 2.0));
			timeMenuManager.add(new FilterByTotalTimeAction("5 ms", 5.0));
			timeMenuManager.add(new FilterByTotalTimeAction("10 ms", 10.0));
			// timeMenuManager.add(new Separator());
			timeMenuManager.add(new FilterByTotalTimeAction("50 ms", 50.0));
			timeMenuManager.add(new FilterByTotalTimeAction("100 ms", 100.0));
			timeMenuManager.add(new FilterByTotalTimeAction("200 ms", 200.0));
			timeMenuManager.add(new FilterByTotalTimeAction("500 ms", 500.0));
			// timeMenuManager.add(new Separator());
			timeMenuManager.add(new FilterByTotalTimeAction("1 s", 1000.0));
			timeMenuManager.add(new FilterByTotalTimeAction("1.5 s", 1500.0));
			timeMenuManager.add(new FilterByTotalTimeAction("2 s", 2000.0));
			timeMenuManager.add(new FilterByTotalTimeAction("5 s", 5000.0));
			menuAction.addContributionItem(timeMenuManager);
		}

		// only add if there is really something in the menu
		if (menuAction.getSize() > 0) {
			toolBarManager.add(menuAction);
		}

		toolBarManager.update(true);
	}

	/**
	 * Switches the Preference View on/off.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private final class SwitchPreferences extends Action {
		/**
		 * Switches the preferences.
		 * 
		 * @param text
		 *            The text.
		 */
		private SwitchPreferences(String text) {
			super(text, AS_CHECK_BOX);
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_PREFERENCES));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			FormPreferencePanel.this.setVisible(isChecked());
		}
	}

	/**
	 * Updates the Preferences.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private final class UpdateAction extends Action {
		/**
		 * Updates an action.
		 * 
		 * @param text
		 *            The text.
		 */
		private UpdateAction(String text) {
			super(text);
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_REFRESH));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			FormPreferencePanel.this.update();
		}
	}

	/**
	 * Switches the live mode.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private final class SwitchLiveMode extends Action {

		/**
		 * Switches the Live Mode.
		 * 
		 * @param text
		 *            The text.
		 */
		public SwitchLiveMode(String text) {
			super(text);
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_LIVE_MODE));
			setChecked(false);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			createLiveModeEvent();
		}
	}

	/**
	 * Action to add a menu to the preference view.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private final class MenuAction extends Action implements IMenuCreator {

		/**
		 * The menu manager.
		 */
		private final MenuManager menuManager;

		/**
		 * Creates a new menu.
		 */
		public MenuAction() {
			super("", Action.AS_DROP_DOWN_MENU);
			menuManager = new MenuManager();
			setMenuCreator(this);
		}

		/**
		 * Adds an action to this menu.
		 * 
		 * @param action
		 *            The action to add.
		 */
		public void addMenuAction(IAction action) {
			menuManager.add(action);
		}

		/**
		 * Adds a contribution item to this manager, like a sub-menu ...
		 * 
		 * @param contributionItem
		 *            THe contribution item to add.
		 */
		public void addContributionItem(IContributionItem contributionItem) {
			menuManager.add(contributionItem);
		}

		/**
		 * @see MenuManager#getSize()
		 * @return the number of contributions in this manager.
		 */
		public int getSize() {
			return menuManager.getSize();
		}

		/**
		 * {@inheritDoc}
		 */
		public Menu getMenu(Control parent) {
			return menuManager.createContextMenu(parent);
		}

		/**
		 * {@inheritDoc}
		 */
		public Menu getMenu(Menu parent) {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public void dispose() {
			menuManager.dispose();
		}

	}

	private final class SetItemCountAction extends Action {
		private int limit;

		public SetItemCountAction(String text, int limit) {
			this(text, limit, false);
		}

		public SetItemCountAction(String text, int limit, boolean isChecked) {
			super(text, Action.AS_RADIO_BUTTON);
			this.limit = limit;
			setChecked(isChecked);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			if (isChecked()) {
				Map<IPreferenceGroup, Object> countPreference = new HashMap<IPreferenceGroup, Object>();
				countPreference.put(PreferenceId.ItemCount.COUNT_SELECTION_ID, limit);
				PreferenceEvent event = new PreferenceEvent(PreferenceId.ITEMCOUNT);
				event.setPreferenceMap(countPreference);
				fireEvent(event);
			}
		}
	}

	private final class FilterBySensorTypeAction extends Action {
		private SensorTypeEnum sensorType;

		public FilterBySensorTypeAction(String text, SensorTypeEnum sensorType) {
			this(text, sensorType, true);
		}

		public FilterBySensorTypeAction(String text, SensorTypeEnum sensorType, boolean isChecked) {
			super(text, Action.AS_CHECK_BOX);
			this.sensorType = sensorType;
			setChecked(isChecked);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			Map<IPreferenceGroup, Object> sensorTypePreference = new HashMap<IPreferenceGroup, Object>();
			sensorTypePreference.put(PreferenceId.SensorTypeSelection.SENSOR_TYPE_SELECTION_ID, sensorType);
			PreferenceEvent event = new PreferenceEvent(PreferenceId.FILTERSENSORTYPE);
			event.setPreferenceMap(sensorTypePreference);
			fireEvent(event);
		}
	}

	private final class FilterByExclusiveTimeAction extends Action {
		private double time;

		public FilterByExclusiveTimeAction(String text, double time) {
			this(text, time, false);
		}

		public FilterByExclusiveTimeAction(String text, double time, boolean isChecked) {
			super(text, Action.AS_RADIO_BUTTON);
			this.time = time;
			setChecked(isChecked);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			if (isChecked()) {
				Map<IPreferenceGroup, Object> sensorTypePreference = new HashMap<IPreferenceGroup, Object>();
				sensorTypePreference.put(PreferenceId.InvocExclusiveTimeSelection.TIME_SELECTION_ID, new Double(time));
				PreferenceEvent event = new PreferenceEvent(PreferenceId.INVOCFILTEREXCLUSIVETIME);
				event.setPreferenceMap(sensorTypePreference);
				fireEvent(event);
			}
		}
	}

	private final class FilterByTotalTimeAction extends Action {
		private double time;

		public FilterByTotalTimeAction(String text, double time) {
			this(text, time, false);
		}

		public FilterByTotalTimeAction(String text, double time, boolean isChecked) {
			super(text, Action.AS_RADIO_BUTTON);
			this.time = time;
			setChecked(isChecked);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			if (isChecked()) {
				Map<IPreferenceGroup, Object> sensorTypePreference = new HashMap<IPreferenceGroup, Object>();
				sensorTypePreference.put(PreferenceId.InvocTotalTimeSelection.TIME_SELECTION_ID, new Double(time));
				PreferenceEvent event = new PreferenceEvent(PreferenceId.INVOCFILTERTOTALTIME);
				event.setPreferenceMap(sensorTypePreference);
				fireEvent(event);
			}
		}
	}

	/**
	 * Creates and fires a new live mode event.
	 */
	private void createLiveModeEvent() {
		Map<IPreferenceGroup, Object> livePreference = new HashMap<IPreferenceGroup, Object>();
		livePreference.put(PreferenceId.LiveMode.BUTTON_LIVE_ID, switchLiveMode.isChecked());
		PreferenceEvent event = new PreferenceEvent(PreferenceId.LIVEMODE);
		event.setPreferenceMap(livePreference);
		fireEvent(event);
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
		for (IPreferenceControl preferenceControl : preferenceControlList) {
			preferenceControl.dispose();
		}

		switchLiveMode = null;
	}

}
