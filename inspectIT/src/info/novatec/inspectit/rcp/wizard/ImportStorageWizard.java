package info.novatec.inspectit.rcp.wizard;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.storage.InspectITStorageManager;
import info.novatec.inspectit.rcp.util.ObjectUtils;
import info.novatec.inspectit.rcp.view.impl.StorageManagerView;
import info.novatec.inspectit.rcp.wizard.page.ImportStorageInfoPage;
import info.novatec.inspectit.rcp.wizard.page.ImportStorageSelectPage;
import info.novatec.inspectit.storage.IStorageData;
import info.novatec.inspectit.storage.StorageException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * Wizard for importing the storages.
 * 
 * @author Ivan Senic
 * 
 */
public class ImportStorageWizard extends Wizard implements INewWizard {

	/**
	 * {@link ImportStorageSelectPage}.
	 */
	private ImportStorageSelectPage importStorageSelectPage;

	/**
	 * {@link ImportStorageInfoPage}.
	 */
	private ImportStorageInfoPage importStorageInfoPage;

	/**
	 * Default constructor.
	 */
	public ImportStorageWizard() {
		this.setWindowTitle("Import Storage");
	}

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
		importStorageSelectPage = new ImportStorageSelectPage();
		addPage(importStorageSelectPage);
		importStorageInfoPage = new ImportStorageInfoPage();
		addPage(importStorageInfoPage);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (ObjectUtils.equals(page, importStorageSelectPage)) {
			importStorageInfoPage.setFileName(importStorageSelectPage.getFileName());
			importStorageInfoPage.setImportLocally(importStorageSelectPage.isImportLocally());
			importStorageInfoPage.setCmrRepositoryDefinition(importStorageSelectPage.getCmrRepositoryDefinition());
			importStorageInfoPage.update();
		}
		return super.getNextPage(page);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		if (ObjectUtils.equals(page, importStorageInfoPage)) {
			importStorageInfoPage.reset();
		}
		return super.getPreviousPage(page);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canFinish() {
		if (getContainer().getCurrentPage().equals(importStorageSelectPage)) {
			return false;
		} else {
			if (!importStorageSelectPage.isPageComplete()) {
				return false;
			}
			if (!importStorageInfoPage.isPageComplete()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		final String fileName = importStorageSelectPage.getFileName();
		final CmrRepositoryDefinition cmrRepositoryDefinition = importStorageSelectPage.getCmrRepositoryDefinition();
		boolean importLocally = importStorageSelectPage.isImportLocally();
		if (importLocally) {
			Job importStorageJob = new Job("Import Storage") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Importing data..", IProgressMonitor.UNKNOWN);
					try {
						InspectIT.getDefault().getInspectITStorageManager().unzipStorageData(fileName);
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
								IViewPart storageManagerView = activePage.findView(StorageManagerView.VIEW_ID);
								if (storageManagerView instanceof StorageManagerView) {
									((StorageManagerView) storageManagerView).refreshWithoutCmrCall();
								}
								InspectIT.getDefault().createInfoDialog("Storage successfully imported.", -1);
							}
						});
					} catch (final Exception e) {
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								InspectIT.getDefault().createErrorDialog("Exception occurred trying to import the storage via file.", e, -1);
							}
						});
					}
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			importStorageJob.setUser(true);
			importStorageJob.schedule();
		} else {
			Job importStorageJob = new Job("Import Storage") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Uploading data..", IProgressMonitor.UNKNOWN);
					InspectITStorageManager storageManager = InspectIT.getDefault().getInspectITStorageManager();
					try {
						storageManager.uploadZippedStorage(fileName, cmrRepositoryDefinition);
					} catch (final Exception e) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								InspectIT.getDefault().createErrorDialog("Storage data was not successfully uploaded to the CMR. Import failed.", e, -1);
							}
						});
						return Status.CANCEL_STATUS;
					}
					monitor.beginTask("Unpacking data..", IProgressMonitor.UNKNOWN);
					try {
						IStorageData storageData = storageManager.getStorageDataFromZip(fileName);
						cmrRepositoryDefinition.getStorageService().unpackUploadedStorage(storageData);
					} catch (final StorageException e) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								InspectIT.getDefault().createErrorDialog("Storage data was not successfully unpacked on the CMR. Import failed.", e, -1);
							}
						});
						return Status.CANCEL_STATUS;
					}

					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
							IViewPart storageManagerView = activePage.findView(StorageManagerView.VIEW_ID);
							if (storageManagerView instanceof StorageManagerView) {
								((StorageManagerView) storageManagerView).refresh(cmrRepositoryDefinition);
							}
							InspectIT.getDefault().createInfoDialog("Storage data was successfully imported to the CMR.", -1);
						}
					});
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			importStorageJob.setUser(true);
			importStorageJob.schedule();
		}

		return true;
	}
}
