package info.novatec.inspectit.rcp.wizard;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.storage.InspectITStorageManager;
import info.novatec.inspectit.rcp.view.impl.StorageManagerView;
import info.novatec.inspectit.rcp.wizard.page.ExportStorageWizardPage;
import info.novatec.inspectit.storage.IStorageData;
import info.novatec.inspectit.storage.LocalStorageData;
import info.novatec.inspectit.storage.StorageData;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * Wizard for exporting the storage.
 * 
 * @author Ivan Senic
 * 
 */
public class ExportStorageWizard extends Wizard implements INewWizard {

	/**
	 * Storage to export.
	 */
	private IStorageData storageData;

	/**
	 * Cmr repository definition.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * {@link ExportStorageWizardPage}.
	 */
	private ExportStorageWizardPage exportStorageWizardPage;

	/**
	 * Default constructor.
	 */
	protected ExportStorageWizard() {
		this.setWindowTitle("Export Storage");
	}

	/**
	 * Default constructor.
	 * 
	 * @param localStorageData
	 *            Storage to export.
	 */
	public ExportStorageWizard(LocalStorageData localStorageData) {
		this();
		this.storageData = localStorageData;
	}

	/**
	 * Default constructor.
	 * 
	 * @param storageData
	 *            Storage to export.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} where storage is located.
	 */
	public ExportStorageWizard(StorageData storageData, CmrRepositoryDefinition cmrRepositoryDefinition) {
		this();
		this.storageData = storageData;
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
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
		exportStorageWizardPage = new ExportStorageWizardPage(storageData);
		addPage(exportStorageWizardPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		final InspectITStorageManager storageManager = InspectIT.getDefault().getInspectITStorageManager();
		final String fileName = exportStorageWizardPage.getFileName();
		LocalStorageData localStorageData = null;
		if (storageData instanceof LocalStorageData) {
			localStorageData = (LocalStorageData) storageData;
		} else if (storageData instanceof StorageData) {
			localStorageData = storageManager.getLocalDataForStorage((StorageData) storageData);
		}

		if (null != localStorageData && localStorageData.isFullyDownloaded()) {
			final LocalStorageData finalLocalStorageData = localStorageData;
			Job exportStorageJob = new Job("Export Storage") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Exporting data..", IProgressMonitor.UNKNOWN);
					try {
						storageManager.zipStorageData(finalLocalStorageData, fileName);
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								IViewPart storageManagerView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(StorageManagerView.VIEW_ID);
								if (storageManagerView instanceof StorageManagerView) {
									((StorageManagerView) storageManagerView).refreshWithoutCmrCall();
								}
								InspectIT.getDefault().createInfoDialog("The storage was exported successfully.", -1);
							}
						});
					} catch (final Exception e) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								InspectIT.getDefault().createErrorDialog("Exception occurred trying to export storage.", e, -1);
							}
						});
					}
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			exportStorageJob.setUser(true);
			exportStorageJob.schedule();
		} else {
			if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
				Job downloadAndExportStorageJob = new Job("Download And Export Storage") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask("Exporting data..", IProgressMonitor.UNKNOWN);
						try {
							storageManager.zipStorageData((StorageData) storageData, cmrRepositoryDefinition, fileName);
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									InspectIT.getDefault().createInfoDialog("The storage was downloaded and exported successfully.", -1);

								}
							});
						} catch (final Exception e) {
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									InspectIT.getDefault().createErrorDialog("Exception occurred trying to export storage.", e, -1);
								}
							});
						}
						monitor.done();
						return Status.OK_STATUS;

					}
				};
				downloadAndExportStorageJob.setUser(true);
				downloadAndExportStorageJob.schedule();
			} else {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						InspectIT.getDefault().createInfoDialog("The storage could not be downloaded, the CMR Repository is offline. Export will be aborted.", -1);
					}
				});
			}
		}

		return true;
	}
}
