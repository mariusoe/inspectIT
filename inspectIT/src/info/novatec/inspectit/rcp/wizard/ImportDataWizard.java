package info.novatec.inspectit.rcp.wizard;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.repository.service.storage.StorageNamingConstants;
import info.novatec.inspectit.rcp.util.ZipUtil;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class ImportDataWizard extends Wizard implements IImportWizard {

	private ImportDataPage importDataPage;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {
		super.addPages();
		importDataPage = new ImportDataPage("Import data");
		addPage(importDataPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		final String fileName = importDataPage.getFileName();
		try {
			getContainer().run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Importing data...", IProgressMonitor.UNKNOWN);
					File dir = new File(StorageNamingConstants.DEFAULT_STORAGE_DIRECTORY);
					if (!dir.exists()) {
						dir.mkdirs();
					}
					File file = new File(fileName);
					String folder = file.getName().split("\\.")[0];
					ZipUtil.unzipFile(fileName, StorageNamingConstants.DEFAULT_STORAGE_DIRECTORY + folder);

					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							InspectIT.getDefault().getRepositoryManager().updateStorageRepository();
						}
					});

					monitor.done();
				}
			});
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("Something went wrong while trying to import some data", e, -1);
			e.printStackTrace();
		}

		return true;
	}
}
