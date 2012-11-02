package info.novatec.inspectit.rcp.wizard;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.util.ObjectUtils;
import info.novatec.inspectit.rcp.view.impl.StorageManagerView;
import info.novatec.inspectit.rcp.wizard.page.AddStorageLabelWizardPage;
import info.novatec.inspectit.rcp.wizard.page.DefineDataProcessorsWizardPage;
import info.novatec.inspectit.rcp.wizard.page.DefineNewStorageWizzardPage;
import info.novatec.inspectit.rcp.wizard.page.NewOrExistsingStorageWizardPage;
import info.novatec.inspectit.rcp.wizard.page.SelectExistingStorageWizardPage;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageException;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;

import java.util.Collection;
import java.util.List;

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
 * Wizard for copying the selected data to one storage.
 * 
 * @author Ivan Senic
 * 
 */
public class CopyDataToStorageWizard extends Wizard implements INewWizard {

	/**
	 * List of data to be copied.
	 */
	private List<DefaultData> copyDataList;

	/**
	 * CMR for the action.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Page for selecting if new or existing storage page should be used.
	 */
	private NewOrExistsingStorageWizardPage newOrExistsingStorageWizardPage;

	/**
	 * Page for new storage.
	 */
	private DefineNewStorageWizzardPage defineNewStorageWizzardPage;

	/**
	 * Page for selecting the existing storage.
	 */
	private SelectExistingStorageWizardPage selectExistingStorageWizardPage;

	/**
	 * Selection of data to be saved.
	 */
	private DefineDataProcessorsWizardPage defineDataProcessorsWizardPage;

	/**
	 * Add label wizard page.
	 */
	private AddStorageLabelWizardPage addLabelWizardPage;

	/**
	 * Default constructor.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to execute action on.
	 * @param copyDataList
	 *            List of data to be copied.
	 */
	public CopyDataToStorageWizard(CmrRepositoryDefinition cmrRepositoryDefinition, List<DefaultData> copyDataList) {
		this.copyDataList = copyDataList;
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.setWindowTitle("Save Data to Storage Wizard");
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
		newOrExistsingStorageWizardPage = new NewOrExistsingStorageWizardPage();
		addPage(newOrExistsingStorageWizardPage);
		defineNewStorageWizzardPage = new DefineNewStorageWizzardPage(cmrRepositoryDefinition);
		addPage(defineNewStorageWizzardPage);
		selectExistingStorageWizardPage = new SelectExistingStorageWizardPage(cmrRepositoryDefinition, false);
		addPage(selectExistingStorageWizardPage);
		defineDataProcessorsWizardPage = new DefineDataProcessorsWizardPage(DefineDataProcessorsWizardPage.ONLY_INVOCATIONS | DefineDataProcessorsWizardPage.EXTRACT_INVOCATIONS);
		addPage(defineDataProcessorsWizardPage);
		addLabelWizardPage = new AddStorageLabelWizardPage(cmrRepositoryDefinition);
		addPage(addLabelWizardPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		StorageData storageData = null;
		if (newOrExistsingStorageWizardPage.useNewStorage()) {
			storageData = defineNewStorageWizzardPage.getStorageData();
		} else {
			storageData = selectExistingStorageWizardPage.getSelectedStorageData();
		}

		final Collection<AbstractDataProcessor> processors = defineDataProcessorsWizardPage.getProcessorList();
		final StorageData finalStorageData = storageData;
		if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
			Job copyDataJob = new Job("Copy Data to Buffer") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						StorageData updatedStorageData = cmrRepositoryDefinition.getStorageService().copyDataToStorage(finalStorageData, copyDataList, processors);
						List<AbstractStorageLabel<?>> labels = addLabelWizardPage.getLabelsToAdd();
						if (!labels.isEmpty()) {
							cmrRepositoryDefinition.getStorageService().addLabelsToStorage(updatedStorageData, labels, true);
						}
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
								IViewPart storageManagerView = activePage.findView(StorageManagerView.VIEW_ID);
								if (storageManagerView instanceof StorageManagerView) {
									((StorageManagerView) storageManagerView).refresh(cmrRepositoryDefinition);
								}
							}
						});
					} catch (final StorageException e) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								InspectIT.getDefault().createErrorDialog("Copy data to buffer failed.", e, -1);
							}
						});
					}
					return Status.OK_STATUS;
				}
			};
			copyDataJob.setUser(true);
			copyDataJob.schedule();
			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (ObjectUtils.equals(page, newOrExistsingStorageWizardPage)) {
			if (newOrExistsingStorageWizardPage.useNewStorage()) {
				return defineNewStorageWizzardPage;
			} else {
				return selectExistingStorageWizardPage;
			}
		} else if (ObjectUtils.equals(page, defineNewStorageWizzardPage)) {
			addLabelWizardPage.setStorageData(defineNewStorageWizzardPage.getStorageData());
			return defineDataProcessorsWizardPage;
		} else if (ObjectUtils.equals(page, selectExistingStorageWizardPage)) {
			addLabelWizardPage.setStorageData(selectExistingStorageWizardPage.getSelectedStorageData());
			return defineDataProcessorsWizardPage;
		} else {
			return super.getNextPage(page);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		if (ObjectUtils.equals(page, defineNewStorageWizzardPage) || ObjectUtils.equals(page, selectExistingStorageWizardPage)) {
			return newOrExistsingStorageWizardPage;
		} else if (ObjectUtils.equals(page, defineDataProcessorsWizardPage)) {
			if (newOrExistsingStorageWizardPage.useNewStorage()) {
				return defineNewStorageWizzardPage;
			} else {
				return selectExistingStorageWizardPage;
			}
		} else {
			return super.getPreviousPage(page);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canFinish() {
		if (!newOrExistsingStorageWizardPage.isPageComplete()) {
			return false;
		} else if (newOrExistsingStorageWizardPage.useNewStorage() && !defineNewStorageWizzardPage.isPageComplete()) {
			return false;
		} else if (!newOrExistsingStorageWizardPage.useNewStorage() && !selectExistingStorageWizardPage.isPageComplete()) {
			return false;
		} else if (!defineDataProcessorsWizardPage.isPageComplete()) {
			return false;
		}
		return true;
	}
}
