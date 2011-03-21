package info.novatec.inspectit.rcp.wizard.page;

import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.storage.label.composite.AbstractStorageLabelComposite;
import info.novatec.inspectit.rcp.storage.label.composite.impl.BooleanStorageLabelComposite;
import info.novatec.inspectit.rcp.storage.label.composite.impl.DateStorageLabelComposite;
import info.novatec.inspectit.rcp.storage.label.composite.impl.NumberStorageLabelComposite;
import info.novatec.inspectit.rcp.storage.label.composite.impl.StringStorageLabelComposite;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * Page for adding storage labels.
 * 
 * @author Ivan Senic
 * 
 */
public class AddStorageLabelWizardPage extends WizardPage {

	/**
	 * Default message.
	 */
	private String defaultMessage = "Define the new label type and its value";

	/**
	 * {@link StorageData} to add label to.
	 */
	private StorageData storageData;

	/**
	 * {@link CmrRepositoryDefinition} where data is located.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * List of available labels types.
	 */
	private List<AbstractStorageLabelType<?>> labelTypeList;

	/**
	 * Suggestion label list.
	 */
	private List<? extends AbstractStorageLabel<?>> suggestionLabelList = new ArrayList<AbstractStorageLabel<?>>();

	/**
	 * Storage label composite.
	 */
	private AbstractStorageLabelComposite storageLabelComposite;

	private Combo labelTypeSelection;
	private Button choseFromExisting;
	private Button createNewValue;
	private org.eclipse.swt.widgets.List existingValuesSelection;
	private Composite main;
	private Listener pageCompletionListener;

	/**
	 * Default constructor.
	 * 
	 * @param storageData
	 *            {@link StorageData}
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}
	 */
	public AddStorageLabelWizardPage(StorageData storageData, CmrRepositoryDefinition cmrRepositoryDefinition) {
		super("Add New Label");
		this.setTitle("Add New Label");
		defaultMessage += " for the storage '" + storageData.getName() + "'";
		this.setMessage(defaultMessage);
		this.storageData = storageData;
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		labelTypeList = cmrRepositoryDefinition.getStorageService().getAllLabelTypes();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(2, false));

		new Label(main, SWT.NONE).setText("Label type:");
		labelTypeSelection = new Combo(main, SWT.READ_ONLY);
		for (AbstractStorageLabelType<?> labelType : labelTypeList) {
			labelTypeSelection.add(TextFormatter.getLabelName(labelType));
		}
		labelTypeSelection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		labelTypeSelection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (labelTypeSelection.getSelectionIndex() != -1) {
					choseFromExisting.setEnabled(true);
					createNewValue.setEnabled(true);
					updateWizardPage();
				}
			}
		});

		choseFromExisting = new Button(main, SWT.RADIO);
		choseFromExisting.setText("Use already existing label value");
		choseFromExisting.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		choseFromExisting.setSelection(true);
		choseFromExisting.setEnabled(false);

		createNewValue = new Button(main, SWT.RADIO);
		createNewValue.setText("Create a new label value");
		createNewValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		createNewValue.setEnabled(false);

		Listener radioButtonsListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				updateWizardPage();
			}
		};
		choseFromExisting.addListener(SWT.Selection, radioButtonsListener);
		createNewValue.addListener(SWT.Selection, radioButtonsListener);

		pageCompletionListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				setPageComplete(isPageComplete());
			}
		};
		labelTypeSelection.addListener(SWT.Selection, pageCompletionListener);
		choseFromExisting.addListener(SWT.Selection, pageCompletionListener);
		createNewValue.addListener(SWT.Selection, pageCompletionListener);

		setControl(main);
	}

	/**
	 * Updates the controls on the page based on current selections.
	 */
	@SuppressWarnings("unchecked")
	private void updateWizardPage() {
		if (null != existingValuesSelection && !existingValuesSelection.isDisposed()) {
			existingValuesSelection.dispose();
		}
		if (null != storageLabelComposite && !storageLabelComposite.isDisposed()) {
			storageLabelComposite.dispose();
		}

		int index = labelTypeSelection.getSelectionIndex();
		AbstractStorageLabelType<?> selectedLabelType = labelTypeList.get(index);

		if (selectedLabelType.isOnePerStorage() && storageData.isLabelPresent(selectedLabelType)) {
			setMessage("Selected label type is one-per-storage. New label value will overwrite the old one.", IMessageProvider.WARNING);
		} else {
			setPageMessage();
		}

		if (!selectedLabelType.isValueReusable()) {
			choseFromExisting.setSelection(true);
			createNewValue.setSelection(false);
			createNewValue.setEnabled(false);
		} else {
			createNewValue.setEnabled(true);
		}

		if (choseFromExisting.getSelection()) {
			suggestionLabelList.clear();
			existingValuesSelection = new org.eclipse.swt.widgets.List(main, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
			existingValuesSelection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
				suggestionLabelList = cmrRepositoryDefinition.getStorageService().getLabelSuggestions(selectedLabelType);
				if (!suggestionLabelList.isEmpty()) {
					Collections.sort(suggestionLabelList);
					for (AbstractStorageLabel<?> existingLabel : suggestionLabelList) {
						existingValuesSelection.add(TextFormatter.getLabelValue(existingLabel, false));
					}
					existingValuesSelection.addListener(SWT.Selection, pageCompletionListener);
				} else {
					existingValuesSelection.add("No values exist on the repository");
					existingValuesSelection.setEnabled(false);
				}
			} else {
				existingValuesSelection.add("No values can be loaded from the repository");
				existingValuesSelection.setEnabled(false);
			}
		} else {
			if (selectedLabelType.getValueClass().equals(Boolean.class)) {
				storageLabelComposite = new BooleanStorageLabelComposite(main, SWT.NONE, (AbstractStorageLabelType<Boolean>) selectedLabelType);
			} else if (selectedLabelType.getValueClass().equals(Date.class)) {
				storageLabelComposite = new DateStorageLabelComposite(main, SWT.NONE, (AbstractStorageLabelType<Date>) selectedLabelType);
			} else if (selectedLabelType.getValueClass().equals(Number.class)) {
				storageLabelComposite = new NumberStorageLabelComposite(main, SWT.NONE, (AbstractStorageLabelType<Number>) selectedLabelType);
			} else if (selectedLabelType.getValueClass().equals(String.class)) {
				storageLabelComposite = new StringStorageLabelComposite(main, SWT.NONE, (AbstractStorageLabelType<String>) selectedLabelType);
			}

			storageLabelComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			storageLabelComposite.addListener(pageCompletionListener);
		}
		main.layout();
	}

	/**
	 * Sets the message based on the page selections.
	 */
	private void setPageMessage() {
		if (labelTypeSelection.getSelectionIndex() == -1) {
			setMessage("Label type must be selected", ERROR);
		} else if (choseFromExisting.getSelection() && null != existingValuesSelection && !existingValuesSelection.isDisposed() && existingValuesSelection.getSelectionIndex() == -1) {
			setMessage("No value for the label selected", ERROR);
		} else if (createNewValue.getSelection() && null != storageLabelComposite && !storageLabelComposite.isDisposed() && !storageLabelComposite.isInputValid()) {
			setMessage("No value for the label entered", ERROR);
		} else {
			setMessage(defaultMessage);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		if (labelTypeSelection.getSelectionIndex() == -1) {
			return false;
		}
		if (choseFromExisting.getSelection() && null != existingValuesSelection && !existingValuesSelection.isDisposed() && existingValuesSelection.getSelectionIndex() == -1) {
			return false;
		}
		if (createNewValue.getSelection() && null != storageLabelComposite && !storageLabelComposite.isDisposed() && !storageLabelComposite.isInputValid()) {
			return false;
		}
		return true;
	}

	/**
	 * @return Returns label to add to storage.
	 */
	public AbstractStorageLabel<?> getLabelToAdd() {
		if (choseFromExisting.getSelection()) {
			int index = existingValuesSelection.getSelectionIndex();
			if (index >= 0) {
				return suggestionLabelList.get(index);
			}
		} else if (createNewValue.getSelection()) {
			return storageLabelComposite.getStorageLabel();
		}
		return null;
	}

}
