package info.novatec.inspectit.rcp.wizard;

import org.eclipse.jface.wizard.Wizard;

/**
 * Wizard used to export some data.
 * 
 * @author Patrice Bouillet
 * 
 */
public class ExportDataWizard extends Wizard {

	/**
	 * The page to enter some details about the export.
	 */
	private ExportDataPage exportStorageNamePage;

	/**
	 * The name of the file.
	 */
	private String fileName;

	public ExportDataWizard() {
		this.setWindowTitle("Export Storage Data Wizard");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {
		exportStorageNamePage = new ExportDataPage("Please specify the storage details");
		addPage(exportStorageNamePage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		this.fileName = exportStorageNamePage.getFileName();
		return true;
	}

	/**
	 * Returns the name of the file.
	 * 
	 * @return the name of the file.
	 */
	public String getFileName() {
		return fileName;
	}

}
