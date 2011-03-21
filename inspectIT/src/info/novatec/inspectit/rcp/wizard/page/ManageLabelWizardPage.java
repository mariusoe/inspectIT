package info.novatec.inspectit.rcp.wizard.page;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.storage.label.composite.AbstractStorageLabelComposite;
import info.novatec.inspectit.rcp.storage.label.composite.impl.BooleanStorageLabelComposite;
import info.novatec.inspectit.rcp.storage.label.composite.impl.DateStorageLabelComposite;
import info.novatec.inspectit.rcp.storage.label.composite.impl.NumberStorageLabelComposite;
import info.novatec.inspectit.rcp.storage.label.composite.impl.StringStorageLabelComposite;
import info.novatec.inspectit.rcp.util.ObjectUtils;
import info.novatec.inspectit.storage.StorageException;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.type.AbstractCustomStorageLabelType;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;
import info.novatec.inspectit.storage.label.type.impl.CustomBooleanLabelType;
import info.novatec.inspectit.storage.label.type.impl.CustomDateLabelType;
import info.novatec.inspectit.storage.label.type.impl.CustomNumberLabelType;
import info.novatec.inspectit.storage.label.type.impl.CustomStringLabelType;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * Manage label page.
 * 
 * @author Ivan Senic
 * 
 */
public class ManageLabelWizardPage extends WizardPage {

	/**
	 * Default message.
	 */
	private static final String DEFAULT_MESSAGE = "Add and remove labels that can be used later for labeling storages";

	/**
	 * CMR repository definition.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Available remote label list.
	 */
	private List<AbstractStorageLabel<?>> labelList = new ArrayList<AbstractStorageLabel<?>>();

	/**
	 * Available remote label type list.
	 */
	private List<AbstractStorageLabelType<?>> labelTypeList = new ArrayList<AbstractStorageLabelType<?>>();

	/**
	 * Table viewer for labels.
	 */
	private TableViewer labelsTableViewer;

	/**
	 * Remove label button.
	 */
	private Button removeLabels;

	/**
	 * Add label button.
	 */
	private Button createLabel;

	/**
	 * {@link TableViewer} for label types.
	 */
	private TableViewer labelTypeTableViewer;

	/**
	 * Create label type button.
	 */
	private Button createLabelType;

	/**
	 * Remove label types button.
	 */
	private Button removeLabelType;

	/**
	 * Should storage be refreshed at the end of wizard.
	 */
	private boolean shouldRefreshStorages = false;

	/**
	 * Default constructor.
	 * 
	 * @param cmrRepositoryDefinition
	 *            Cmr to manage labels for.
	 */
	public ManageLabelWizardPage(CmrRepositoryDefinition cmrRepositoryDefinition) {
		super("Manage Labels");
		this.setTitle("Manage Labels");
		this.setMessage(DEFAULT_MESSAGE);
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		if (null != cmrRepositoryDefinition) {
			this.setMessage("Label management for repository '" + cmrRepositoryDefinition.getName() + "' (" + cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort() + ")");
		}
		labelTypeList.addAll(cmrRepositoryDefinition.getStorageService().getAllLabelTypes());
		labelList.addAll(cmrRepositoryDefinition.getStorageService().getAllLabels());
		Iterator<AbstractStorageLabel<?>> it = labelList.iterator();
		while (it.hasNext()) {
			if (!it.next().getStorageLabelType().isValueReusable()) {
				it.remove();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);

		// label type - upper composite
		Composite upperComposite = new Composite(sashForm, SWT.NONE);
		upperComposite.setLayout(new GridLayout(2, false));

		Label labelTypeInfo = new Label(upperComposite, SWT.BOLD);
		labelTypeInfo.setText("Existing label types");
		labelTypeInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

		Table labelTypeTable = new Table(upperComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
		labelTypeTable.setHeaderVisible(true);
		labelTypeTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));

		TableColumn column = new TableColumn(labelTypeTable, SWT.NONE);
		column.setText("Name");
		column.setMoveable(false);
		column.setResizable(true);
		column.setWidth(140);

		column = new TableColumn(labelTypeTable, SWT.NONE);
		column.setText("Value type");
		column.setMoveable(false);
		column.setResizable(true);
		column.setWidth(100);

		column = new TableColumn(labelTypeTable, SWT.NONE);
		column.setText("One per storage");
		column.setMoveable(false);
		column.setResizable(true);
		column.setWidth(100);

		labelTypeTableViewer = new TableViewer(labelTypeTable);
		labelTypeTableViewer.setContentProvider(new ArrayContentProvider());
		labelTypeTableViewer.setLabelProvider(new StyledCellIndexLabelProvider() {
			@Override
			protected StyledString getStyledText(Object element, int index) {
				if (element instanceof AbstractStorageLabelType) {
					AbstractStorageLabelType<?> labelType = (AbstractStorageLabelType<?>) element;
					switch (index) {
					case 0:
						return new StyledString(TextFormatter.getLabelName(labelType));
					case 1:
						return new StyledString(TextFormatter.getLabelValueType(labelType));
					case 2:
						if (labelType.isOnePerStorage()) {
							return new StyledString("Yes");
						} else {
							return new StyledString("No");
						}
					default:
					}
				}
				return null;
			}

			@Override
			protected Image getColumnImage(Object element, int index) {
				if (index == 0 && element instanceof AbstractStorageLabelType) {
					ImageDescriptor id = ImageFormatter.getImageDescriptorForLabel((AbstractStorageLabelType<?>) element);
					if (null != id) {
						return id.createImage();
					}
				}
				return null;
			}
		});
		labelTypeTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				manageLabelTypeSlection();
			}
		});
		labelTypeTableViewer.setInput(labelTypeList);

		createLabelType = new Button(upperComposite, SWT.PUSH);
		createLabelType.setText("Create");
		createLabelType.setToolTipText("Create New Label Type");
		createLabelType.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		createLabelType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CreateLabelTypeDialog createDialog = new CreateLabelTypeDialog(getShell());
				createDialog.open();
				if (createDialog.getReturnCode() == Dialog.OK) {
					AbstractStorageLabelType<?> createdType = createDialog.getCreatedLabelType();
					if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
						cmrRepositoryDefinition.getStorageService().saveLabelType(createdType);
						refreshLabelTypes();
					} else {
						InspectIT.getDefault().createInfoDialog("Can not create label type, CMR repository is offline.", -1);
					}
				}
			}
		});

		removeLabelType = new Button(upperComposite, SWT.PUSH);
		removeLabelType.setText("Remove");
		removeLabelType.setToolTipText("Remove Label Type");
		removeLabelType.setEnabled(false);
		removeLabelType.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		removeLabelType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AbstractStorageLabelType<?> typeToRemove = (AbstractStorageLabelType<?>) ((StructuredSelection) labelTypeTableViewer.getSelection()).getFirstElement();
				// check that there are no existing labels
				boolean canDelete = true;
				for (AbstractStorageLabel<?> label : labelList) {
					if (ObjectUtils.equals(label.getStorageLabelType(), typeToRemove)) {
						canDelete = false;
						break;
					}
				}

				if (canDelete && cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
					try {
						cmrRepositoryDefinition.getStorageService().removeLabelType(typeToRemove);
						refreshLabelTypes();
					} catch (StorageException exception) {
						InspectIT.getDefault().createErrorDialog("Exception occured on the server while removing the label type.", exception, -1);
					}
				} else if (canDelete) {
					InspectIT.getDefault().createInfoDialog("Can not remove label type, CMR repository is offline.", -1);
				} else {
					InspectIT.getDefault().createInfoDialog("Can not remove label type, there are still labels of this type existing. Please remove all labels first.", -1);
				}
			}
		});

		// labels - lower composite
		Composite lowerComposite = new Composite(sashForm, SWT.NONE);
		lowerComposite.setLayout(new GridLayout(2, false));

		Table table = new Table(lowerComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));

		column = new TableColumn(table, SWT.NONE);
		column.setText("Label");
		column.setMoveable(false);
		column.setResizable(true);
		column.setWidth(140);

		column = new TableColumn(table, SWT.NONE);
		column.setText("Value");
		column.setMoveable(false);
		column.setResizable(true);
		column.setWidth(100);

		labelsTableViewer = new TableViewer(table);
		labelsTableViewer.setContentProvider(new ArrayContentProvider());
		labelsTableViewer.setLabelProvider(new StyledCellIndexLabelProvider() {
			@Override
			protected StyledString getStyledText(Object element, int index) {
				if (element instanceof AbstractStorageLabel) {
					AbstractStorageLabel<?> label = (AbstractStorageLabel<?>) element;
					switch (index) {
					case 0:
						return new StyledString(TextFormatter.getLabelName(label));
					case 1:
						return new StyledString(TextFormatter.getLabelValue(label, false));
					default:
					}
				}
				return null;
			}

			@Override
			protected Image getColumnImage(Object element, int index) {
				if (index == 0 && element instanceof AbstractStorageLabel) {
					ImageDescriptor id = ImageFormatter.getImageDescriptorForLabel(((AbstractStorageLabel<?>) element).getStorageLabelType());
					if (null != id) {
						return id.createImage();
					}
				}
				return null;
			}
		});
		labelsTableViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof AbstractStorageLabel && e2 instanceof AbstractStorageLabel) {
					return ((AbstractStorageLabel<?>) e1).compareTo((AbstractStorageLabel<?>) e2);
				}
				return super.compare(viewer, e1, e2);
			}
		});
		labelsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (labelsTableViewer.getSelection().isEmpty()) {
					removeLabels.setEnabled(false);
				} else {
					removeLabels.setEnabled(true);
				}
			}
		});

		createLabel = new Button(lowerComposite, SWT.PUSH);
		createLabel.setText("Create");
		createLabel.setToolTipText("Create New Label");
		createLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		createLabel.setEnabled(false);
		createLabel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AbstractStorageLabelType<?> suggestedLabelType = (AbstractStorageLabelType<?>) ((StructuredSelection) labelTypeTableViewer.getSelection()).getFirstElement();
				CreateLabelDialog createLabelDialog = new CreateLabelDialog(getShell(), labelTypeList, suggestedLabelType);
				createLabelDialog.open();
				if (createLabelDialog.getReturnCode() == Dialog.OK) {
					AbstractStorageLabel<?> createdLabel = createLabelDialog.getCreatedLabel();
					if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
						cmrRepositoryDefinition.getStorageService().saveLabelToCmr(createdLabel);
						refreshLabels();
					} else {
						InspectIT.getDefault().createInfoDialog("Can not create label, CMR repository is offline.", -1);
					}
				}
			}
		});

		removeLabels = new Button(lowerComposite, SWT.PUSH);
		removeLabels.setText("Remove");
		removeLabels.setToolTipText("Remove Label(s)");
		removeLabels.setEnabled(false);

		final Menu removeButtonMenu = new Menu(removeLabels);

		MenuItem removeOnlyFromCmrMenuItem = new MenuItem(removeButtonMenu, SWT.NONE);
		removeOnlyFromCmrMenuItem.setText("Remove Only From CMR");
		removeOnlyFromCmrMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSelectedLabels(false);
			}
		});

		MenuItem removeAllMenuItem = new MenuItem(removeButtonMenu, SWT.NONE);
		removeAllMenuItem.setText("Remove From CMR And All Storages");
		removeAllMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSelectedLabels(true);
				shouldRefreshStorages = true;
			}
		});

		removeLabels.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		removeLabels.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeButtonMenu.setVisible(true);
			}
		});

		sashForm.setWeights(new int[] { 1, 1 });
		setControl(sashForm);
	}

	/**
	 * Removes selected labels.
	 * 
	 * @param removeAlsoFromStorages
	 *            Should labels be also removed from storages.
	 */
	private void removeSelectedLabels(boolean removeAlsoFromStorages) {
		StructuredSelection structuredSelection = ((StructuredSelection) labelsTableViewer.getSelection());
		List<AbstractStorageLabel<?>> labelsToRemove = new ArrayList<AbstractStorageLabel<?>>(structuredSelection.size());
		for (Iterator<?> iterator = structuredSelection.iterator(); iterator.hasNext();) {
			AbstractStorageLabel<?> abstractStorageLabel = (AbstractStorageLabel<?>) iterator.next();
			labelsToRemove.add(abstractStorageLabel);
		}

		if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
			try {
				cmrRepositoryDefinition.getStorageService().removeLabelsFromCmr(labelsToRemove, removeAlsoFromStorages);
				refreshLabels();
			} catch (StorageException exception) {
				InspectIT.getDefault().createErrorDialog("Exception throw trying to remove label(s)", exception, -1);
			}

		} else {
			InspectIT.getDefault().createInfoDialog("Can not remove label(s), CMR repository is offline.", -1);
		}
	}

	/**
	 * Refreshes the list of label types and updates the tables and selection.
	 */
	private void refreshLabelTypes() {
		AbstractStorageLabelType<?> selectedLabelType = null;
		if (!labelTypeTableViewer.getSelection().isEmpty()) {
			selectedLabelType = (AbstractStorageLabelType<?>) ((StructuredSelection) labelTypeTableViewer.getSelection()).getFirstElement();
		}
		labelTypeList.clear();
		labelTypeList.addAll(cmrRepositoryDefinition.getStorageService().getAllLabelTypes());

		labelTypeTableViewer.refresh();

		if (null != selectedLabelType) {
			labelTypeTableViewer.setSelection(new StructuredSelection(selectedLabelType), true);
		}
	}

	/**
	 * Refreshes the list of the labels and updates the tables and selection.
	 */
	private void refreshLabels() {
		labelList.clear();
		labelList.addAll(cmrRepositoryDefinition.getStorageService().getAllLabels());
		manageLabelTypeSlection();
	}

	/**
	 * Manages the label type selection.
	 */
	private void manageLabelTypeSlection() {
		if (!labelTypeTableViewer.getSelection().isEmpty()) {
			removeLabelType.setEnabled(true);
			createLabel.setEnabled(true);
			AbstractStorageLabelType<?> labelType = (AbstractStorageLabelType<?>) ((StructuredSelection) labelTypeTableViewer.getSelection()).getFirstElement();
			List<AbstractStorageLabel<?>> inputForLabelTable = new ArrayList<AbstractStorageLabel<?>>();
			for (AbstractStorageLabel<?> label : labelList) {
				if (ObjectUtils.equals(label.getStorageLabelType(), labelType)) {
					inputForLabelTable.add(label);
				}
			}
			labelsTableViewer.setInput(inputForLabelTable);
			labelsTableViewer.refresh();
		} else {
			removeLabelType.setEnabled(true);
			createLabel.setEnabled(true);
			labelsTableViewer.setInput(null);
			labelsTableViewer.refresh();
		}
	}

	/**
	 * Gets {@link #shouldRefreshStorages}.
	 * 
	 * @return {@link #shouldRefreshStorages}
	 */
	public boolean isShouldRefreshStorages() {
		return shouldRefreshStorages;
	}

	/**
	 * Create label type dialog.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class CreateLabelTypeDialog extends Dialog {

		/**
		 * Available type list.
		 */
		private final AbstractCustomStorageLabelType<?>[] availableTypes = new AbstractCustomStorageLabelType<?>[] {
				new CustomBooleanLabelType(),
				new CustomDateLabelType(),
				new CustomNumberLabelType(),
				new CustomStringLabelType()
		};

		/**
		 * Reference to the created label type.
		 */
		private AbstractStorageLabelType<?> createdLabelType;

		/** Widgets. */
		private Button okButton;
		private Composite main;
		private Text name;
		private Combo valueTypeSelection;
		private Button yesButton;
		private Button noButton;

		/**
		 * Default constructor.
		 * 
		 * @param parentShell
		 *            Shell
		 */
		public CreateLabelTypeDialog(Shell parentShell) {
			super(parentShell);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Create Label Type");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
			okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
			okButton.setEnabled(false);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void buttonPressed(int buttonId) {
			if (buttonId == IDialogConstants.OK_ID) {
				createdLabelType = ensureLabelType();
			}
			super.buttonPressed(buttonId);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Control createDialogArea(Composite parent) {
			main = new Composite(parent, SWT.NONE);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.minimumWidth = 350;
			main.setLayoutData(gd);
			main.setLayout(new GridLayout(3, false));

			new Label(main, SWT.NONE).setText("Name:");
			name = new Text(main, SWT.BORDER);
			name.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

			new Label(main, SWT.NONE).setText("Value type:");
			valueTypeSelection = new Combo(main, SWT.DROP_DOWN | SWT.READ_ONLY);
			valueTypeSelection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			for (AbstractStorageLabelType<?> labelType : availableTypes) {
				valueTypeSelection.add(TextFormatter.getLabelValueType(labelType));
			}

			new Label(main, SWT.NONE).setText("One per storage:");
			yesButton = new Button(main, SWT.RADIO);
			yesButton.setText("Yes");
			yesButton.setSelection(true);

			noButton = new Button(main, SWT.RADIO);
			noButton.setText("No");

			final Listener listener = new Listener() {
				@Override
				public void handleEvent(Event event) {
					okButton.setEnabled(isInputValid());
				}

			};
			name.addListener(SWT.Modify, listener);
			valueTypeSelection.addListener(SWT.Selection, listener);
			yesButton.addListener(SWT.Selection, listener);
			noButton.addListener(SWT.Selection, listener);

			return main;
		}

		/**
		 * @return Returns if the input of the dialog is valid.
		 */
		private boolean isInputValid() {
			if (name.getText().trim().isEmpty()) {
				return false;
			}
			if (valueTypeSelection.getSelectionIndex() == -1) {
				return false;
			}
			return true;
		}

		/**
		 * Ensures that the label type is correctly created.
		 * 
		 * @return {@link AbstractStorageLabelType}
		 */
		private AbstractStorageLabelType<?> ensureLabelType() {
			AbstractCustomStorageLabelType<?> labelType = availableTypes[valueTypeSelection.getSelectionIndex()];
			labelType.setName(name.getText().trim());
			labelType.setOnePerStorage(yesButton.getSelection());
			return labelType;
		}

		/**
		 * Gets {@link #createdLabelType}.
		 * 
		 * @return {@link #createdLabelType}
		 */
		public AbstractStorageLabelType<?> getCreatedLabelType() {
			return createdLabelType;
		}

	}

	/**
	 * Create label dialog.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class CreateLabelDialog extends Dialog {

		/**
		 * List of label types that can be created.
		 */
		private List<AbstractStorageLabelType<?>> labelTypes;

		/**
		 * Suggested label type that will be initially selected.
		 */
		private AbstractStorageLabelType<?> suggestedType;

		/**
		 * Label type selection Combo.
		 */
		private Combo typeSelection;

		/**
		 * Storage label composite.
		 */
		private AbstractStorageLabelComposite storageLabelComposite;

		/**
		 * OK Button.
		 */
		private Button okButton;

		/**
		 * Main composite.
		 */
		private Composite main;

		/**
		 * Created label reference.
		 */
		private AbstractStorageLabel<?> createdLabel;

		/**
		 * Default constructor.
		 * 
		 * @param parentShell
		 *            Shell.
		 * @param labelTypes
		 *            List of labels that can be created.
		 * @param suggestedType
		 *            Suggested type that will initially be selected in the combo box.
		 */
		public CreateLabelDialog(Shell parentShell, List<AbstractStorageLabelType<?>> labelTypes, AbstractStorageLabelType<?> suggestedType) {
			super(parentShell);
			this.labelTypes = labelTypes;
			this.suggestedType = suggestedType;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Create Label");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
			okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
			okButton.setEnabled(isInputValid());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void buttonPressed(int buttonId) {
			if (buttonId == IDialogConstants.OK_ID) {
				createdLabel = storageLabelComposite.getStorageLabel();
			}
			super.buttonPressed(buttonId);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Control createDialogArea(Composite parent) {
			main = new Composite(parent, SWT.NONE);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.minimumWidth = 350;
			main.setLayoutData(gd);
			main.setLayout(new GridLayout(2, false));

			new Label(main, SWT.NONE).setText("Label type:");
			typeSelection = new Combo(main, SWT.DROP_DOWN | SWT.READ_ONLY);
			int index = -1;
			int i = 0;
			for (AbstractStorageLabelType<?> labelType : labelTypes) {
				typeSelection.add(TextFormatter.getLabelName(labelType));
				if (ObjectUtils.equals(labelType, suggestedType)) {
					index = i;
				}
				i++;
			}
			typeSelection.select(index);
			typeSelection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			final Listener listener = new Listener() {
				@Override
				public void handleEvent(Event event) {
					okButton.setEnabled(isInputValid());
				}
			};
			typeSelection.addListener(SWT.Selection, listener);
			typeSelection.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateStorageLabelComposite();
				}
			});

			new Label(main, SWT.NONE).setText("Label value:");
			updateStorageLabelComposite();

			return main;
		}

		/**
		 * 
		 * @return Returns created label.
		 */
		public AbstractStorageLabel<?> getCreatedLabel() {
			return createdLabel;
		}

		/**
		 * Updates the storage label composite.
		 */
		@SuppressWarnings("unchecked")
		private void updateStorageLabelComposite() {
			if (null != storageLabelComposite && !storageLabelComposite.isDisposed()) {
				storageLabelComposite.dispose();
			}

			AbstractStorageLabelType<?> selectedLabelType = labelTypes.get(typeSelection.getSelectionIndex());
			if (selectedLabelType.getValueClass().equals(Boolean.class)) {
				storageLabelComposite = new BooleanStorageLabelComposite(main, SWT.NONE, (AbstractStorageLabelType<Boolean>) selectedLabelType, false);
			} else if (selectedLabelType.getValueClass().equals(Date.class)) {
				storageLabelComposite = new DateStorageLabelComposite(main, SWT.NONE, (AbstractStorageLabelType<Date>) selectedLabelType, false);
			} else if (selectedLabelType.getValueClass().equals(Number.class)) {
				storageLabelComposite = new NumberStorageLabelComposite(main, SWT.NONE, (AbstractStorageLabelType<Number>) selectedLabelType, false);
			} else if (selectedLabelType.getValueClass().equals(String.class)) {
				storageLabelComposite = new StringStorageLabelComposite(main, SWT.NONE, (AbstractStorageLabelType<String>) selectedLabelType, false);
			}

			final Listener listener = new Listener() {
				@Override
				public void handleEvent(Event event) {
					okButton.setEnabled(isInputValid());
				}
			};
			storageLabelComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			storageLabelComposite.addListener(listener);
			main.layout();
		}

		/**
		 * 
		 * @return If dialog input is valid.
		 */
		private boolean isInputValid() {
			if (typeSelection.getSelectionIndex() == -1) {
				return false;
			}
			if (null == storageLabelComposite || !storageLabelComposite.isInputValid()) {
				return false;
			}
			return true;
		}
	}

}
