package info.novatec.inspectit.rcp.editor.preferences;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.action.MenuAction;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId.LiveMode;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId.TimeResolution;
import info.novatec.inspectit.rcp.editor.preferences.control.IPreferenceControl;
import info.novatec.inspectit.rcp.handlers.MaximizeActiveViewHandler;
import info.novatec.inspectit.rcp.model.SensorTypeEnum;
import info.novatec.inspectit.rcp.util.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.internal.menus.CommandMessages;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

/**
 * This is the class where the preference panel is created.
 * 
 * @author Eduard Tudenhoefner
 * @author Patrice Bouillet
 * @author Stefan Siegl
 */
@SuppressWarnings("restriction")
public class FormPreferencePanel implements IPreferencePanel {

	/**
	 * ID of the preference panel.
	 */
	private String id;

	/**
	 * The used toolkit.
	 */
	private final FormToolkit toolkit;

	/**
	 * Callbacks which are containing the fire method which is executed whenever something is
	 * changed and updated.
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
	 * THe button for switching the stepping control.
	 */
	private Action switchSteppingControl;

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
		this.id = UUID.randomUUID().toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return id;
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
	 * {@inheritDoc}
	 */
	@Override
	public void bufferCleared() {
		fireEvent(new PreferenceEvent(PreferenceId.CLEAR_BUFFER));
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSteppingControlChecked(boolean checked) {
		if (null != switchSteppingControl) {
			switchSteppingControl.setChecked(checked);
		}
	}

	/**
	 * Creates the preference controls in the preference control panel.
	 * 
	 * @param parent
	 *            The parent {@link Composite} to which the controls will be added.
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
		switchPreferences = new SwitchPreferences("Additional options");
		MenuAction menuAction = new MenuAction();
		menuAction.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_TOOL));
		menuAction.setToolTipText("Preferences");

		// add the maximize to all forms, let eclipse hide it as declared in plugin.xml
		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(MaximizeActiveViewHandler.PREFERENCE_PANEL_ID_PARAMETER, id);
		CommandContributionItemParameter contributionParameters = new CommandContributionItemParameter(workbenchWindow, null, MaximizeActiveViewHandler.COMMAND_ID, params, InspectIT.getDefault()
				.getImageDescriptor(InspectITImages.IMG_WINDOW), null, null, null, null, getTooltipTextForMaximizeContributionItem(), SWT.CHECK, null, false);
		CommandContributionItem maximizeCommandContribution = new CommandContributionItem(contributionParameters);
		toolBarManager.add(maximizeCommandContribution);

		if (preferenceSet.contains(PreferenceId.HTTP_AGGREGATION_REQUESTMETHOD)) {
			toolBarManager.add(new Separator());
			toolBarManager.add(new SwitchHttpCategorizationRequestMethod("Include Request Method in Categorization"));
			toolBarManager.add(new Separator());
		}

		if (preferenceSet.contains(PreferenceId.INVOCATION_SUBVIEW_MODE)) {
			toolBarManager.add(new SwitchInvocationSubviewMode("Switch the tabbed views mode from/to aggregated/raw"));
		}

		if (preferenceSet.contains(PreferenceId.SAMPLINGRATE) || preferenceSet.contains(PreferenceId.TIMELINE)) {
			toolBarManager.add(switchPreferences);
		}

		if (preferenceSet.contains(PreferenceId.STEPPABLE_CONTROL)) {
			switchSteppingControl = new SwitchSteppingControl("Stepping control");
			toolBarManager.add(switchSteppingControl);
		}

		if (preferenceSet.contains(PreferenceId.LIVEMODE)) {
			toolBarManager.add(switchLiveMode);

			// Refresh rate
			MenuManager refreshMenuManager = new MenuManager("Refresh rate");
			refreshMenuManager.add(new SetRefreshRateAction("5 (s)", 5, true));
			refreshMenuManager.add(new SetRefreshRateAction("10 (s)", 10));
			refreshMenuManager.add(new SetRefreshRateAction("30 (s)", 30));
			refreshMenuManager.add(new SetRefreshRateAction("60 (s)", 60));
			menuAction.addContributionItem(refreshMenuManager);
		}
		if (preferenceSet.contains(PreferenceId.UPDATE)) {
			toolBarManager.add(new UpdateAction("Update"));
		}

		if (preferenceSet.contains(PreferenceId.ITEMCOUNT)) {
			MenuManager countMenuManager = new MenuManager("Item count to show");
			countMenuManager.add(new SetItemCountAction("10", 10));
			countMenuManager.add(new SetItemCountAction("20", 20));
			countMenuManager.add(new SetItemCountAction("50", 50));
			countMenuManager.add(new SetItemCountAction("100", 100));
			countMenuManager.add(new SetItemCountAction("200", 200));
			countMenuManager.add(new SetItemCountAction("500", 500));
			countMenuManager.add(new SetItemCountAction("All...", -1, true));
			menuAction.addContributionItem(countMenuManager);
		}
		if (preferenceSet.contains(PreferenceId.FILTERSENSORTYPE)) {
			MenuManager sensorTypeMenuManager = new MenuManager("Filter by SensorType");
			sensorTypeMenuManager.add(new FilterBySensorTypeAction("Timer", SensorTypeEnum.TIMER));
			sensorTypeMenuManager.add(new FilterBySensorTypeAction("Invocation Seq", SensorTypeEnum.INVOCATION_SEQUENCE));
			sensorTypeMenuManager.add(new FilterBySensorTypeAction("Exception", SensorTypeEnum.EXCEPTION_SENSOR));
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

		if (preferenceSet.contains(PreferenceId.TIME_RESOLUTION)) {
			MenuManager timeMenuManager = new MenuManager("Time Decimal Places");
			timeMenuManager.add(new SetTimeDecimalPlaces("0", 0, true));
			timeMenuManager.add(new SetTimeDecimalPlaces("1", 1));
			timeMenuManager.add(new SetTimeDecimalPlaces("2", 2));
			timeMenuManager.add(new SetTimeDecimalPlaces("3", 3));
			menuAction.addContributionItem(timeMenuManager);
		}

		// only add if there is really something in the menu
		if (menuAction.getSize() > 0) {
			toolBarManager.add(menuAction);
		}

		toolBarManager.update(true);
	}

	/**
	 * Due to the Eclipse bug this method will return the correct tool-tip text with correct binding
	 * sequence for the maximize active sub-view command.
	 * <p>
	 * <i>This method should be removed when Eclipse fixes the bug.</i>
	 * 
	 * @return Returns tool-tip text with key binding sequence.
	 */
	private String getTooltipTextForMaximizeContributionItem() {
		String tooltipText = "Maximize Active Sub-View";

		IBindingService bindingService = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
		TriggerSequence activeBinding = null;
		Binding[] allBindings = bindingService.getBindings();
		for (Binding b : allBindings) {
			ParameterizedCommand pCommand = b.getParameterizedCommand();
			if (null != pCommand) {
				String commandId = pCommand.getId();
				if (ObjectUtils.equals(commandId, MaximizeActiveViewHandler.COMMAND_ID)) {
					activeBinding = b.getTriggerSequence();
					break;
				}
			}
		}

		if (activeBinding != null && !activeBinding.isEmpty()) {
			String acceleratorText = activeBinding.format();
			if (acceleratorText != null && acceleratorText.length() != 0) {
				tooltipText = NLS.bind(CommandMessages.Tooltip_Accelerator, tooltipText, acceleratorText);
			}
		}

		return tooltipText;
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
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_PREFERENCES));
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
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_REFRESH));
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
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_LIVE_MODE));
			setChecked(LiveMode.ACTIVE_DEFAULT);
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
	 * Filters by the maximum number of elements shown.
	 * 
	 * @author Stefan Siegl
	 */
	private final class SetItemCountAction extends Action {
		/** the maximum number of elements shown. */
		private int limit;

		/**
		 * Constructor, setting checked to false.
		 * 
		 * @param text
		 *            the text
		 * @param limit
		 *            the maximum number of elements shown.
		 */
		public SetItemCountAction(String text, int limit) {
			this(text, limit, false);
		}

		/**
		 * Constructor.
		 * 
		 * @param text
		 *            the text
		 * @param limit
		 *            the maximum number of elements shown.
		 * @param isChecked
		 *            whether this option is set
		 */
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

	/**
	 * Filters by sensor type.
	 * 
	 * @author Stefan Siegl
	 */
	private final class FilterBySensorTypeAction extends Action {
		/** The sensor type. */
		private SensorTypeEnum sensorType;

		/**
		 * Constructor, setting checked to false.
		 * 
		 * @param text
		 *            the text
		 * @param sensorType
		 *            the sensor type
		 */
		public FilterBySensorTypeAction(String text, SensorTypeEnum sensorType) {
			this(text, sensorType, true);
		}

		/**
		 * Constructor.
		 * 
		 * @param text
		 *            the text
		 * @param sensorType
		 *            the sensor type
		 * @param isChecked
		 *            if this option is checked
		 */
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

	/**
	 * Filters by exclusive time.
	 * 
	 * @author Stefan Siegl
	 */
	private final class FilterByExclusiveTimeAction extends Action {
		/** the time. */
		private double time;

		/**
		 * Constructor, setting checked to false.
		 * 
		 * @param text
		 *            the text
		 * @param time
		 *            the time
		 */
		public FilterByExclusiveTimeAction(String text, double time) {
			this(text, time, false);
		}

		/**
		 * Constructor.
		 * 
		 * @param text
		 *            the text
		 * @param time
		 *            the time
		 * @param isChecked
		 *            if this option is checked
		 */
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

	/**
	 * Filters by total time.
	 * 
	 * @author Stefan Siegl
	 */
	private final class FilterByTotalTimeAction extends Action {
		/** the time. */
		private double time;

		/**
		 * Constructor, setting checked to false.
		 * 
		 * @param text
		 *            the text
		 * @param time
		 *            the time
		 */
		public FilterByTotalTimeAction(String text, double time) {
			this(text, time, false);
		}

		/**
		 * Constructor.
		 * 
		 * @param text
		 *            the text
		 * @param time
		 *            the time
		 * @param isChecked
		 *            if this option is checked
		 */
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
	 * Sets the automatic refresh rate.
	 * 
	 * @author Stefan Siegl
	 */
	private final class SetRefreshRateAction extends Action {
		/** refresh rate in ms. */
		private int rate;

		/**
		 * Constructor, setting checked to false.
		 * 
		 * @param text
		 *            the text
		 * @param rate
		 *            the refresh rate
		 */
		public SetRefreshRateAction(String text, int rate) {
			this(text, rate, false);
		}

		/**
		 * Constructor.
		 * 
		 * @param text
		 *            the text
		 * @param rate
		 *            the refresh rate
		 * @param isChecked
		 *            whether or not this option is active.
		 */
		public SetRefreshRateAction(String text, int rate, boolean isChecked) {
			super(text, Action.AS_RADIO_BUTTON);
			this.rate = rate;
			setChecked(isChecked);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			if (isChecked()) {
				Map<IPreferenceGroup, Object> refreshPreference = new HashMap<IPreferenceGroup, Object>();
				refreshPreference.put(LiveMode.REFRESH_RATE, rate);
				PreferenceEvent event = new PreferenceEvent(PreferenceId.LIVEMODE);
				event.setPreferenceMap(refreshPreference);
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

	/**
	 * Action for turning the stepping control off and on.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private final class SwitchSteppingControl extends Action {

		/**
		 * Default constructor.
		 * 
		 * @param text
		 *            the action's text, or <code>null</code> if there is no text
		 * @see Action
		 */
		public SwitchSteppingControl(String text) {
			super(text, AS_CHECK_BOX);
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_RIGHT_DOWN_ARROW));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			PreferenceEvent event = new PreferenceEvent(PreferenceId.STEPPABLE_CONTROL);
			Map<IPreferenceGroup, Object> steppablePreference = new HashMap<IPreferenceGroup, Object>();
			steppablePreference.put(PreferenceId.SteppableControl.BUTTON_STEPPABLE_CONTROL_ID, this.isChecked());
			event.setPreferenceMap(steppablePreference);
			fireEvent(event);
		}

	}

	/**
	 * Sets the decimal places.
	 * 
	 * @author Stefan Siegl
	 * 
	 */
	private final class SetTimeDecimalPlaces extends Action {
		/** The number of decimal places. */
		private int decimalPlaces;

		/**
		 * Default constructor.
		 * 
		 * @param text
		 *            the action's text, or <code>null</code> if there is no text
		 * @param decimalPlaces
		 *            the number of decimal places
		 * @see Action
		 */
		public SetTimeDecimalPlaces(String text, int decimalPlaces) {
			this(text, decimalPlaces, false);
		}

		/**
		 * Default constructor.
		 * 
		 * @param text
		 *            the action's text, or <code>null</code> if there is no text
		 * @param decimalPlaces
		 *            the number of decimal places
		 * @param isChecked
		 *            whether or not this option is enabled
		 * @see Action
		 */
		public SetTimeDecimalPlaces(String text, int decimalPlaces, boolean isChecked) {
			super(text, Action.AS_RADIO_BUTTON);
			this.decimalPlaces = decimalPlaces;
			setChecked(isChecked);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			if (isChecked()) {
				Map<IPreferenceGroup, Object> decimalPlacesPreference = new HashMap<IPreferenceGroup, Object>();
				decimalPlacesPreference.put(TimeResolution.TIME_DECIMAL_PLACES_ID, decimalPlaces);
				PreferenceEvent event = new PreferenceEvent(PreferenceId.TIME_RESOLUTION);
				event.setPreferenceMap(decimalPlacesPreference);
				fireEvent(event);
			}
		}
	}

	/**
	 * Option to switch between categorization based on request method or not.
	 * 
	 * @author Stefan Siegl
	 */
	private final class SwitchHttpCategorizationRequestMethod extends Action {

		/**
		 * Default Constructor.
		 * 
		 * @param text
		 *            the action's text, or <code>null</code> if there is no text
		 */
		public SwitchHttpCategorizationRequestMethod(String text) {
			super(text, AS_CHECK_BOX);
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_HTTP_AGGREGATION_REQUESTMESSAGE));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			PreferenceEvent event = new PreferenceEvent(PreferenceId.HTTP_AGGREGATION_REQUESTMETHOD);
			Map<IPreferenceGroup, Object> httpCategoriation = new HashMap<IPreferenceGroup, Object>();
			httpCategoriation.put(PreferenceId.HttpAggregationRequestMethod.BUTTON_HTTP_AGGREGATION_REQUESTMETHOD_ID, this.isChecked());
			event.setPreferenceMap(httpCategoriation);
			fireEvent(event);

			// perform a refresh
			update();
		}
	}

	/**
	 * Action for switching the mode of the invocation subviews from/to raw/aggregated.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private final class SwitchInvocationSubviewMode extends Action {

		/**
		 * Default constructor.
		 * 
		 * @param text
		 *            Text on the action.
		 */
		public SwitchInvocationSubviewMode(String text) {
			super(text, AS_CHECK_BOX);
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_HTTP_AGGREGATION_REQUESTMESSAGE));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			PreferenceEvent event = new PreferenceEvent(PreferenceId.INVOCATION_SUBVIEW_MODE);
			Map<IPreferenceGroup, Object> httpCategoriation = new HashMap<IPreferenceGroup, Object>();
			httpCategoriation.put(PreferenceId.InvocationSubviewMode.RAW, this.isChecked());
			event.setPreferenceMap(httpCategoriation);
			fireEvent(event);
		}
	}

}
