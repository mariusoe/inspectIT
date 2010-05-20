package info.novatec.inspectit.rcp.wizard;

import info.novatec.inspectit.rcp.repository.service.storage.StorageNamingConstants;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ImportDataPage extends WizardPage {

	/**
	 * The folder of the export data.
	 */
	private Text file;

	protected ImportDataPage(String pageName) {
		super(pageName);
		setDescription("Please specify the inspectIT data to import");
	}

	@Override
	public void createControl(final Composite parent) {
		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NONE);
		// create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		int ncol = 4;
		gl.numColumns = ncol;
		composite.setLayout(gl);

		new Label(composite, SWT.NONE).setText("File:");
		file = new Text(composite, SWT.BORDER);
		file.setEnabled(false);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = ncol - 2;
		file.setLayoutData(gd);

		Button button = new Button(composite, SWT.PUSH);
		button.setText("Select");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(parent.getShell(), SWT.OPEN);
				String[] filterNames = new String[] { "inspectIT data" };
				String[] filterExtensions = new String[] { "*" + StorageNamingConstants.FILE_ENDING_DATA };
				String filterPath = "/";
				String platform = SWT.getPlatform();
				if (platform.equals("win32") || platform.equals("wpf")) {
					filterPath = "c:\\";
				}
				dialog.setFilterNames(filterNames);
				dialog.setFilterExtensions(filterExtensions);
				dialog.setFilterPath(filterPath);
				String selectedFile = dialog.open();
				if (selectedFile != null) {
					file.setText(selectedFile);
					update();
				}
			}
		});

		setControl(composite);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		String folderText = file.getText();
		return !"".equals(folderText);
	}

	/**
	 * Returns the file name.
	 * 
	 * @return the file name.
	 */
	public String getFileName() {
		return file.getText();
	}

	/**
	 * Updates the wizard buttons to make the finish button (in-)active.
	 */
	private void update() {
		getWizard().getContainer().updateButtons();
	}

}
