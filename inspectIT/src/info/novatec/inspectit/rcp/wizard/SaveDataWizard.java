package info.novatec.inspectit.rcp.wizard;

import org.eclipse.jface.wizard.Wizard;

public class SaveDataWizard extends Wizard {
	
	private SaveDataPage storageNamePage;
	
	private String storageName;
	
	public SaveDataWizard() {
		this.setWindowTitle("Enter Storage Name Wizard");
	}
	
	@Override
	public void addPages() {
		storageNamePage = new SaveDataPage("Enter a name for the storage data");
		addPage(storageNamePage);
	}

	@Override
	public boolean performFinish() {
		this.storageName = storageNamePage.getStorageName();
		return true;
	}
	
	public String getStorageName() {
		return storageName;
	}

}
