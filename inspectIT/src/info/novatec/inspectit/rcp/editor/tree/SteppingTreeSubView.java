package info.novatec.inspectit.rcp.editor.tree;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.editor.preferences.IPreferenceGroup;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.tree.input.SteppingTreeInputController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * View that enables locating the element in the tree via {@link SteppingControl}.
 * 
 * @author Ivan Senic
 * 
 */
public class SteppingTreeSubView extends TreeSubView {

	/**
	 * Main composite for this view. It holds the {@link TreeViewer} and additionally
	 * {@link SteppingControl} if necessary.
	 */
	private Composite subComposite;

	/**
	 * Stepping control.
	 */
	private SteppingControl steppingControl;

	/**
	 * Input controller for this view.
	 */
	private SteppingTreeInputController steppingTreeInputController;

	/**
	 * Default constructor.
	 * 
	 * @param treeInputController
	 *            Stepping tree input controller.
	 * @see TreeSubView#TreeSubView(info.novatec.inspectit.rcp.editor.tree.input.TreeInputController)
	 */
	public SteppingTreeSubView(SteppingTreeInputController treeInputController) {
		super(treeInputController);

		this.steppingTreeInputController = treeInputController;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		subComposite = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		subComposite.setLayout(layout);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		subComposite.setLayoutData(gd);

		super.createPartControl(subComposite, toolkit);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		getTreeViewer().getTree().setLayoutData(gd);

		if (steppingControl == null) {
			steppingControl = new SteppingControl(subComposite, toolkit, steppingTreeInputController.getSteppingObjectList());
		}
		steppingControl.showControl();

		// the focus has to be passed to the subComposite, because it can not register it
		getTreeViewer().getTree().addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				subComposite.notifyListeners(SWT.FocusIn, null);
			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Control getControl() {
		return subComposite;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDataInput(List<? extends DefaultData> data) {
		super.setDataInput(data);
		steppingControl.inputChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		subComposite.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		super.preferenceEventFired(preferenceEvent);
		switch (preferenceEvent.getPreferenceId()) {
		case STEPPABLE_CONTROL:
			Map<IPreferenceGroup, Object> preferenceMap = preferenceEvent.getPreferenceMap();
			Object isChecked = preferenceMap.get(PreferenceId.SteppableControl.BUTTON_STEPPABLE_CONTROL_ID);
			if (isChecked instanceof Boolean) {
				Boolean makeControlVisible = (Boolean) isChecked;
				if (makeControlVisible) {
					steppingControl.showControl();
				} else {
					steppingControl.hideControl();
				}
			}
			break;
		case CLEAR_BUFFER:
			steppingControl.inputChanged();
			break;
		default:
			break;
		}
	}

	/**
	 * Tries to expand the tree viewer to the wanted occurrence of wanted element. If the wanted
	 * occurrence is not reachable, nothing is done. Otherwise the tree is expanded and element
	 * selected.
	 * 
	 * @param template
	 *            Element to reach.
	 * @param occurance
	 *            Wanted occurrence in the tree.
	 */
	private void expandToObject(Object template, int occurance) {
		Object realElement = steppingTreeInputController.getElement(template, occurance);
		if (null != realElement) {
			((DeferredTreeViewer) getTreeViewer()).expandToObjectAndSelect(realElement, 0);
		}
	}

	/**
	 * Is input set for this sub view.
	 * 
	 * @return True is input is not null or if it is not empty. Otherwise false.
	 */
	@SuppressWarnings("unchecked")
	private boolean isInputSet() {
		List<Object> input = (List<Object>) getTreeViewer().getInput();
		if (input == null || input.isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * Stepping control class.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class SteppingControl {

		/**
		 * Composite where stepping control will be created.
		 */
		private Composite parent;

		/**
		 * Toolkit.
		 */
		private FormToolkit toolkit;

		/**
		 * List of objects that are able to be located in the tree.
		 */
		private List<Object> steppableObjects;

		/**
		 * List of objects that are able currently in the combo.
		 */
		private List<Object> objectsInCombo;

		/**
		 * Main composite of stepping control.
		 */
		private Composite mainComposite;

		/**
		 * Combo for object selection.
		 */
		private Combo objectSelection;

		/**
		 * Next button.
		 */
		private Button next;

		/**
		 * Previous button.
		 */
		private Button previous;

		/**
		 * Information label.
		 */
		private Label info;

		/**
		 * Flag for defining is the control show or not.
		 */
		private boolean controlShown = false;

		/**
		 * The currently selected object that is to be found in the tree.
		 */
		private Object selectedObject;

		/**
		 * Current displayed occurrence of selected object.
		 */
		private int occurrence;

		/**
		 * Total occurrence of the selected object that could be reached.
		 */
		private int totalOccurrences;

		/**
		 * Default constructor.
		 * 
		 * @param parent
		 *            Composite where stepping control will be created.
		 * @param toolkit
		 *            Toolkit.
		 * @param objectList
		 *            List of objects that are able to be located in the tree.
		 */
		public SteppingControl(Composite parent, FormToolkit toolkit, List<Object> objectList) {
			super();
			this.parent = parent;
			this.toolkit = toolkit;
			this.steppableObjects = objectList;
		}

		/**
		 * Creates stepping control.
		 * 
		 * @param parent
		 *            Composite where stepping control will be created.
		 * @param toolkit
		 *            Toolkit.
		 */
		private void createPartControl(Composite parent, FormToolkit toolkit) {
			mainComposite = toolkit.createComposite(parent);
			GridLayout layout = new GridLayout(7, false);
			mainComposite.setLayout(layout);
			mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

			toolkit.createLabel(mainComposite, "Object to locate:");

			objectSelection = new Combo(mainComposite, SWT.SIMPLE | SWT.DROP_DOWN | SWT.READ_ONLY);
			GridData gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = GridData.FILL;
			gd.minimumWidth = 200;
			objectSelection.setLayoutData(gd);

			previous = toolkit.createButton(mainComposite, "Previous", SWT.PUSH | SWT.NO_BACKGROUND);
			previous.setEnabled(false);
			previous.setImage(InspectIT.getDefault().getImage(InspectITConstants.IMG_PREVIOUS));

			next = toolkit.createButton(mainComposite, "Next", SWT.PUSH | SWT.NO_BACKGROUND);
			next.setEnabled(false);
			next.setImage(InspectIT.getDefault().getImage(InspectITConstants.IMG_NEXT));

			info = toolkit.createLabel(mainComposite, "No invocation loaded");

			// added additional composite to the right, so that minimizing and maximizing the window
			// can look better
			Composite helpComposite = toolkit.createComposite(mainComposite);
			gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = GridData.FILL;
			gd.minimumWidth = 0;
			gd.heightHint = 0;
			gd.widthHint = 0;
			helpComposite.setLayoutData(gd);
			

			objectSelection.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event event) {
					int selectionIndex = objectSelection.getSelectionIndex();
					if (selectionIndex != -1) {
						Object selObject = objectsInCombo.get(selectionIndex);
						selectedObject = selObject;
						if (isInputSet()) {
							occurrence = 0;
							totalOccurrences = steppingTreeInputController.countOccurrences(selectedObject);
							expandToObject(selectedObject, ++occurrence);
							if (!(totalOccurrences > occurrence)) {
								next.setEnabled(false);
							} else {
								next.setEnabled(true);
							}
							if (!(occurrence > 1)) {
								previous.setEnabled(false);
							} else {
								previous.setEnabled(true);
							}
							updateInfoBox();
						}
					}
				}
			});

			next.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					expandToObject(selectedObject, ++occurrence);
					if (!(totalOccurrences > occurrence)) {
						next.setEnabled(false);
					}
					if (!(occurrence > 1)) {
						previous.setEnabled(false);
					} else {
						previous.setEnabled(true);
					}
					updateInfoBox();
				}
			});

			previous.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					expandToObject(selectedObject, --occurrence);
					next.setEnabled(true);
					if (!(occurrence > 1)) {
						previous.setEnabled(false);
					}
					updateInfoBox();
				}
			});
			controlShown = true;
		}

		/**
		 * 
		 * 
		 * @param object
		 *            One of the objects that are to be located in the tree.
		 * @return Returns the string to be inserted into the combo box for supplied object.
		 */
		private String getTextualString(Object object) {
			String representation = steppingTreeInputController.getElementTextualRepresentation(object);
			// Assure that string is not too long
			if (representation.length() > 120) {
				return representation.substring(0, 118) + "..";
			}
			return representation;
		}

		/**
		 * Hides stepping control.
		 */
		public void hideControl() {
			if (controlShown) {
				mainComposite.dispose();
				subComposite.layout();
				controlShown = false;
			}
		}

		/**
		 * Shows stepping control.
		 */
		public void showControl() {
			if (!controlShown) {
				createPartControl(parent, toolkit);
				subComposite.layout();
				controlShown = true;
				inputChanged();
			}
		}

		/**
		 * Resets stepping control.
		 */
		public void inputChanged() {
			if (controlShown) {
				if (isInputSet()) {
					objectsInCombo = createObjectsForComboList();
					objectSelection.removeAll();
					for (Object object : objectsInCombo) {
						objectSelection.add(getTextualString(object));
					}
					objectSelection.pack(true);
					if (null != selectedObject && objectsInCombo.contains(selectedObject)) {
						objectSelection.select(objectsInCombo.indexOf(selectedObject));
					} else {
						objectSelection.select(0);
					}
				} else {
					objectSelection.removeAll();
					next.setEnabled(false);
					previous.setEnabled(false);
					updateInfoBox();
				}
				mainComposite.layout();
			}
		}

		/**
		 * Updates the text in the info box based on the current status of the stepping control.
		 */
		private void updateInfoBox() {
			if (controlShown) {
				if (isInputSet()) {
					if (objectSelection.getSelectionIndex() != -1) {
						if (occurrence == 0 && totalOccurrences != 0) {
							String msg = "Found " + totalOccurrences + " occurrence";
							if (totalOccurrences > 1) {
								msg += "s";
							}
							info.setText(msg);
						} else if (occurrence != 0) {
							info.setText(occurrence + "/" + totalOccurrences);
						} else {
							info.setText("No occurrences found");
						}
					}
				} else {
					info.setText("No invocation loaded");
				}
				mainComposite.layout();
			}
		}

		/**
		 * Creates the list of objects that will be inserted to combo, thus only objects that are
		 * locate-able in the invocation.
		 * 
		 * @return List of objects.
		 */
		private List<Object> createObjectsForComboList() {
			List<Object> list = new ArrayList<Object>();
			for (Object object : steppableObjects) {
				if (steppingTreeInputController.isElementOccurrenceReachable(object, 1)) {
					list.add(object);
				}
			}
			return list;
		}

	}
}
