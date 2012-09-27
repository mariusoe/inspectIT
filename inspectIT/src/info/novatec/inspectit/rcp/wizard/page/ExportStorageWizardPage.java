package info.novatec.inspectit.rcp.wizard.page;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.storage.IStorageData;
import info.novatec.inspectit.storage.LocalStorageData;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageFileExtensions;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Page for exporting storage.
 * 
 * @author Ivan Senic
 * 
 */
public class ExportStorageWizardPage extends WizardPage {

	/**
	 * Default message for wizard page.
	 */
	private static final String DEFAULT_MESSAGE = "Select where to export storage";

	/**
	 * Storage to export.
	 */
	private IStorageData storageData;

	/**
	 * Text box where selected file will be displayed.
	 */
	private Text fileText;

	/**
	 * Default constructor.
	 * 
	 * @param storageData
	 *            Storage to export.
	 */
	public ExportStorageWizardPage(IStorageData storageData) {
		super("Export Storage");
		this.setTitle("Export Storage");
		this.setMessage(DEFAULT_MESSAGE);
		this.storageData = storageData;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(3, false));

		new Label(main, SWT.NONE).setText("File:");

		fileText = new Text(main, SWT.READ_ONLY | SWT.BORDER);
		fileText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Button select = new Button(main, SWT.PUSH);
		select.setText("Select");
		select.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
				fileDialog.setOverwrite(true);
				fileDialog.setText("Select File for Export");
				fileDialog.setFilterExtensions(new String[] { "*" + StorageFileExtensions.ZIP_STORAGE_EXT });
				fileDialog.setFileName(storageData.getName());
				String file = fileDialog.open();
				if (null != file) {
					fileText.setText(file);
				}
				setPageComplete(isPageComplete());
				if (fileText.getText().isEmpty()) {
					setMessage("No file selected", ERROR);
				} else {
					setMessage(DEFAULT_MESSAGE);
				}
			}
		});
		select.forceFocus();

		Group group = new Group(main, SWT.NONE);
		group.setText("Storage Info");
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 10;
		gl.marginWidth = 10;
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		group.setLayout(gl);

		new Label(group, SWT.NONE).setText("Name:");
		new Label(group, SWT.NONE).setText(storageData.getName());

		new Label(group, SWT.NONE).setText("Description:");
		String desc = "";
		if (null != storageData.getDescription()) {
			desc = storageData.getDescription();
		}
		new Label(group, SWT.NONE).setText(desc);

		new Label(group, SWT.NONE).setText("Size on disk:");
		new Label(group, SWT.NONE).setText(NumberFormatter.formatBytesToMBytes(storageData.getDiskSize()));

		new Label(group, SWT.NONE).setText("Data downloaded:");
		Label downloaded = new Label(group, SWT.NONE);
		LocalStorageData localStorageData = null;
		if (storageData instanceof LocalStorageData) {
			localStorageData = (LocalStorageData) storageData;
		} else if (storageData instanceof StorageData) {
			localStorageData = InspectIT.getDefault().getInspectITStorageManager().getLocalDataForStorage((StorageData) storageData);
		}
		boolean notDownloaded = (null == localStorageData || !localStorageData.isFullyDownloaded());

		if (notDownloaded) {
			downloaded.setText("No");
		} else {
			downloaded.setText("Yes");
		}

		if (notDownloaded) {
			Composite infoComposite = new Composite(main, SWT.NONE);
			infoComposite.setLayout(new GridLayout(2, false));
			infoComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
			new Label(infoComposite, SWT.NONE).setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO));
			new Label(infoComposite, SWT.WRAP).setText("The not downloaded storage will have to be downloaded prior to export operation.");
		}

		setControl(main);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		return !fileText.getText().isEmpty();
	}

	/**
	 * @return Returns the selected file name.
	 */
	public String getFileName() {
		return fileText.getText();
	}
}
