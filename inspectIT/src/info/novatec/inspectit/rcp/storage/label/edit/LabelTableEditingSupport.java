package info.novatec.inspectit.rcp.storage.label.edit;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.util.ObjectUtils;
import info.novatec.inspectit.rcp.view.impl.StorageManagerView;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageException;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.BooleanStorageLabel;
import info.novatec.inspectit.storage.label.DateStorageLabel;
import info.novatec.inspectit.storage.label.NumberStorageLabel;
import info.novatec.inspectit.storage.label.StringStorageLabel;

import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

/**
 * Editing support for the tables where labels are displayed. This editing support class returns the
 * different {@link CellEditor} implementations based on the label types.
 * 
 * @author Ivan Senic
 * 
 */
public class LabelTableEditingSupport extends EditingSupport {

	/**
	 * {@link StorageData} needed for updating.
	 */
	private StorageData storageData;

	/**
	 * {@link CmrRepositoryDefinition} needed for updating.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Table where editing is done.
	 */
	private Table table;

	/**
	 * {@link TextCellEditor} used when labels are {@link StringStorageLabel} and
	 * {@link NumberStorageLabel}.
	 */
	private TextCellEditor textEditor;

	/**
	 * {@link ComboBoxCellEditor} used when label is {@link BooleanStorageLabel}.
	 */
	private ComboBoxCellEditor comboBoxEditor;

	/**
	 * {@link DateDialogCellEditor} used when label is {@link DateStorageLabel}.
	 */
	private DateDialogCellEditor dateDialogCellEditor;

	/**
	 * Default constructor.
	 * 
	 * @param viewer
	 *            TableViewer.
	 * @param storageData
	 *            {@link StorageData} needed for updating.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} needed for updating.
	 */
	public LabelTableEditingSupport(TableViewer viewer, StorageData storageData, CmrRepositoryDefinition cmrRepositoryDefinition) {
		super(viewer);
		this.table = viewer.getTable();
		this.storageData = storageData;
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.textEditor = new TextCellEditor(table);
		this.comboBoxEditor = new ComboBoxCellEditor(table, new String[] { "Yes", "No" });
		this.dateDialogCellEditor = new DateDialogCellEditor(table);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected CellEditor getCellEditor(Object element) {
		AbstractStorageLabel<?> storageLabel = (AbstractStorageLabel<?>) element;
		if (storageLabel instanceof DateStorageLabel) {
			return dateDialogCellEditor;
		} else if (storageLabel instanceof BooleanStorageLabel) {
			return comboBoxEditor;
		}
		return textEditor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean canEdit(Object element) {
		AbstractStorageLabel<?> storageLabel = (AbstractStorageLabel<?>) element;
		return storageLabel.getStorageLabelType().isEditable();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object getValue(Object element) {
		AbstractStorageLabel<?> storageLabel = (AbstractStorageLabel<?>) element;
		if (storageLabel instanceof DateStorageLabel) {
			return ((DateStorageLabel) storageLabel).getValue();
		} else if (storageLabel instanceof BooleanStorageLabel) {
			if (((BooleanStorageLabel) storageLabel).getValue().booleanValue()) {
				return 0;
			} else {
				return 1;
			}
		} else {
			return TextFormatter.getLabelValue(storageLabel, false);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setValue(Object element, Object value) {
		AbstractStorageLabel<?> storageLabel = (AbstractStorageLabel<?>) element;
		if (isValueChanged(storageLabel, value)) {
			if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
				try {
					cmrRepositoryDefinition.getStorageService().removeLabelFromStorage(storageData, storageLabel);
					setNewValue(storageLabel, value);
					storageLabel.setId(0);
					cmrRepositoryDefinition.getStorageService().addLabelToStorage(storageData, storageLabel, true);
					refreshStorageManagerView();
				} catch (StorageException e) {
					InspectIT.getDefault().createErrorDialog("Label value can not be updated.", e, -1);
				}
				getViewer().refresh();
			} else {
				InspectIT.getDefault().createInfoDialog("Label value can not be updated, CMR repository is offline", -1);
			}
		}
	}

	/**
	 * Refreshes the {@link StorageManagerView}.
	 */
	private void refreshStorageManagerView() {
		IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(StorageManagerView.VIEW_ID);
		if (viewPart instanceof StorageManagerView) {
			((StorageManagerView) viewPart).refresh(cmrRepositoryDefinition);
		}
	}

	/**
	 * Sets new value for the {@link AbstractStorageLabel}.
	 * 
	 * @param storageLabel
	 *            Storage label.
	 * @param value
	 *            Value returned by the {@link CellEditor}.
	 */
	private void setNewValue(AbstractStorageLabel<?> storageLabel, Object value) {
		if (storageLabel instanceof BooleanStorageLabel) {
			BooleanStorageLabel booleanStorageLabel = (BooleanStorageLabel) storageLabel;
			int intValue = ((Integer) value).intValue();
			booleanStorageLabel.setValue(intValue == 0);
		} else if (storageLabel instanceof StringStorageLabel) {
			StringStorageLabel stringStorageLabel = (StringStorageLabel) storageLabel;
			stringStorageLabel.setValue((String) value);
		} else if (storageLabel instanceof DateStorageLabel) {
			DateStorageLabel dateStorageLabel = (DateStorageLabel) storageLabel;
			dateStorageLabel.setValue((Date) value);
		} else if (storageLabel instanceof NumberStorageLabel) {
			NumberStorageLabel numberStorageLabel = (NumberStorageLabel) storageLabel;
			String stringValue = (String) value;
			Number newNumber = null;
			if (stringValue.indexOf('.') != -1) {
				newNumber = Double.parseDouble(stringValue);
			} else {
				newNumber = Integer.parseInt(stringValue);
			}
			numberStorageLabel.setValue(newNumber);
		}
	}

	/**
	 * Returns if the value of the storage label is changed. This depends from the type of the
	 * storage label as well as the type of object returned by the cell editors.
	 * 
	 * @param storageLabel
	 *            Storage label to check.
	 * @param value
	 *            New value entered via editing support.
	 * @return Returns if the value of the storage label is changed.
	 */
	private boolean isValueChanged(AbstractStorageLabel<?> storageLabel, Object value) {
		if (storageLabel instanceof DateStorageLabel) {
			return !ObjectUtils.equals(storageLabel.getValue(), value);
		} else if (storageLabel instanceof BooleanStorageLabel) {
			BooleanStorageLabel booleanStorageLabel = (BooleanStorageLabel) storageLabel;
			int intValue = ((Integer) value).intValue();
			return booleanStorageLabel.getBooleanValue().booleanValue() ^ intValue == 0;
		} else if (storageLabel instanceof StringStorageLabel) {
			return !ObjectUtils.equals(storageLabel.getValue(), value);
		} else if (storageLabel instanceof NumberStorageLabel) {
			String stringValue = (String) value;
			Number newNumber = null;
			if (stringValue.indexOf('.') != -1) {
				newNumber = Double.parseDouble(stringValue);
			} else {
				newNumber = Integer.parseInt(stringValue);
			}
			return !ObjectUtils.equals(storageLabel.getValue(), newNumber);
		}
		return false;
	}

	/**
	 * {@link DialogCellEditor} that will be used when {@link DateStorageLabel} should be edited.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class DateDialogCellEditor extends DialogCellEditor {

		/**
		 * Default constructor.
		 * 
		 * @param table
		 *            Table.
		 */
		public DateDialogCellEditor(Table table) {
			super(table);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object openDialogBox(Control cellEditorWindow) {
			Date date = (Date) getValue();
			DateDialog dateDialog = new DateDialog(cellEditorWindow.getShell(), date);
			dateDialog.open();
			if (dateDialog.getReturnCode() == Dialog.OK) {
				return dateDialog.getDate();
			} else {
				return null;
			}
		}

	}

	/**
	 * Dialog that is displayed when the date should be edited.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class DateDialog extends Dialog {

		/**
		 * {@link CDateTime} for selecting the date.
		 */
		private CDateTime cDateTime;

		/**
		 * Date selected.
		 */
		private Date date;

		/**
		 * Default constructor.
		 * 
		 * @param parentShell
		 *            Shell.
		 * @param date
		 *            Initially set date in the dialog.
		 */
		public DateDialog(Shell parentShell, Date date) {
			super(parentShell);
			this.date = date;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Edit Date");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite main = new Composite(parent, SWT.NONE);
			main.setLayout(new GridLayout(1, false));
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			main.setLayoutData(gd);

			cDateTime = new CDateTime(main, CDT.BORDER | CDT.DROP_DOWN | CDT.TAB_FIELDS);
			cDateTime.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			cDateTime.setSelection(date);

			return main;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void buttonPressed(int buttonId) {
			if (buttonId == IDialogConstants.OK_ID) {
				date = cDateTime.getSelection();
			}
			super.buttonPressed(buttonId);
		}

		/**
		 * Gets {@link #date}.
		 * 
		 * @return {@link #date}
		 */
		public Date getDate() {
			return date;
		}

	}

}
