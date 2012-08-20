package info.novatec.inspectit.rcp.wizard.page;

import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.storage.StorageData;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Page for the selecting if data to be downloaded should be compressed before.
 * 
 * @author Ivan Senic
 * 
 */
public class DownloadStorageWizardPage extends WizardPage {

	/**
	 * If compression should be used.
	 */
	private Button compress;

	/**
	 * Default constructor.
	 * 
	 * @param storageData
	 *            Storage to download.
	 */
	public DownloadStorageWizardPage(StorageData storageData) {
		super("Download Storage");
		Assert.isNotNull(storageData);
		this.setTitle("Download Storage");
		this.setMessage("Options for downloading the storage '" + storageData.getName() + "' (size: " + NumberFormatter.formatBytesToMBytes(storageData.getDiskSize()) + ")");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(1, false));

		new Label(main, SWT.NONE).setText("Compress the data before download:");

		compress = new Button(main, SWT.RADIO);
		compress.setText("Yes - suggested when downloading from Internet or slow network");
		compress.setSelection(true);

		Button dontCompress = new Button(main, SWT.RADIO);
		dontCompress.setText("No - suggested when downloading from fast local network");
		dontCompress.setSelection(false);

		setControl(main);
	}

	/**
	 * @return If user selected to compress data before download.
	 */
	public boolean isCompressBefore() {
		return compress.getSelection();
	}
}
