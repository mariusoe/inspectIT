package info.novatec.inspectit.rcp.wizard.page;

import info.novatec.inspectit.storage.IStorageData;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Page for the selecting if storage should be compressed before downloading/exporting etc..
 * 
 * @author Ivan Senic
 * 
 */
public class StorageCompressionWizardPage extends WizardPage {

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
	public StorageCompressionWizardPage(IStorageData storageData, String title, String message) {
		super(title);
		Assert.isNotNull(storageData);
		this.setTitle(title);
		this.setMessage(message);
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
		compress.setText("Yes - suggested if downloading from Internet or slow network");
		compress.setSelection(true);

		Button dontCompress = new Button(main, SWT.RADIO);
		dontCompress.setText("No - suggested if downloading from fast local network");
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
