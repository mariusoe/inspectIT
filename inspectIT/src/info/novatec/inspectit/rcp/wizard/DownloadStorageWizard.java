package info.novatec.inspectit.rcp.wizard;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.provider.IStorageDataProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.view.impl.StorageManagerView;
import info.novatec.inspectit.rcp.wizard.page.StorageCompressionWizardPage;
import info.novatec.inspectit.storage.StorageData;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * Wizard for downloading storage.
 * 
 * @author Ivan Senic
 * 
 */
public class DownloadStorageWizard extends Wizard implements INewWizard {

	/**
	 * Storage to download.
	 */
	private StorageData storageData;

	/**
	 * Repository where storage is located.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Wizard page.
	 */
	private StorageCompressionWizardPage storageCompressionWizardPage;

	/**
	 * Default constructor.
	 * 
	 * @param storageData
	 *            Storage to download.
	 * @param cmrRepositoryDefinition
	 *            Repository where storage is located.
	 */
	public DownloadStorageWizard(StorageData storageData, CmrRepositoryDefinition cmrRepositoryDefinition) {
		Assert.isNotNull(storageData);
		Assert.isNotNull(cmrRepositoryDefinition);
		this.storageData = storageData;
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.setWindowTitle("Download Storage");
	}

	/**
	 * Secondary constructor.
	 * 
	 * @param storageDataProvider
	 *            {@link IStorageDataProvider} that holds the information about the storage and
	 *            repository.
	 */
	public DownloadStorageWizard(IStorageDataProvider storageDataProvider) {
		this(storageDataProvider.getStorageData(), storageDataProvider.getCmrRepositoryDefinition());
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
		String title = "Download Storage";
		String message = "Options for downloading the storage '" + storageData.getName() + "' (size: " + NumberFormatter.formatBytesToMBytes(storageData.getDiskSize()) + ")";
		storageCompressionWizardPage = new StorageCompressionWizardPage(storageData, title, message);
		addPage(storageCompressionWizardPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		final boolean compress = storageCompressionWizardPage.isCompressBefore();
		switch (cmrRepositoryDefinition.getOnlineStatus()) {
		case OFFLINE:
			// inform CMR is offline
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					InspectIT.getDefault().createInfoDialog("Could not download the storage. The CMR Repository is offline.", -1);
				}
			});
			break;
		case ONLINE:
		case CHECKING:
			Job downloadStorageJob = new Job("Download Storage") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					SubMonitor subMonitor = SubMonitor.convert(monitor);
					try {
						InspectIT.getDefault().getInspectITStorageManager().fullyDownloadStorage(storageData, cmrRepositoryDefinition, compress, subMonitor);
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								InspectIT.getDefault().createInfoDialog("Selected storage successfully fully downloaded.", -1);
								IViewPart storageManagerView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(StorageManagerView.VIEW_ID);
								if (storageManagerView instanceof StorageManagerView) {
									((StorageManagerView) storageManagerView).refreshWithoutCmrCall();
								}
							}
						});

					} catch (final Exception e) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								InspectIT.getDefault().createErrorDialog("Could not download the storage.", e, -1);
							}
						});
					}
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			downloadStorageJob.setUser(true);
			downloadStorageJob.setProperty(IProgressConstants.ICON_PROPERTY, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_STORAGE_DOWNLOADED));
			downloadStorageJob.schedule();
			break;
		default:
			break;
		}
		return true;
	}

}
