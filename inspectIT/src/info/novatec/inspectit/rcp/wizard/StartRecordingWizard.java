package info.novatec.inspectit.rcp.wizard;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.provider.IStorageDataProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.util.ObjectUtils;
import info.novatec.inspectit.rcp.wizard.page.DefineDataProcessorsWizardPage;
import info.novatec.inspectit.rcp.wizard.page.DefineNewStorageWizzardPage;
import info.novatec.inspectit.rcp.wizard.page.DefineTimelineWizardPage;
import info.novatec.inspectit.rcp.wizard.page.NewOrExistsingStorageWizardPage;
import info.novatec.inspectit.rcp.wizard.page.SelectAgentsWizardPage;
import info.novatec.inspectit.rcp.wizard.page.SelectExistingStorageWizardPage;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageException;
import info.novatec.inspectit.storage.recording.RecordingProperties;

import java.util.Date;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard for starting a recording.
 *
 * @author Ivan Senic
 *
 */
public class StartRecordingWizard extends Wizard implements INewWizard {

	/**
	 * {@link NewOrExistsingStorageWizardPage}.
	 */
	private NewOrExistsingStorageWizardPage newOrExistsingStorageWizardPage;

	/**
	 * Define data page.
	 */
	private DefineDataProcessorsWizardPage defineDataPage;

	/**
	 * Recording storage selection page.
	 */
	private SelectExistingStorageWizardPage selectStorageWizardPage;

	/**
	 * New storage page.
	 */
	private DefineNewStorageWizzardPage defineNewStorageWizzardPage;

	/**
	 * Select agents wizard page.
	 */
	private SelectAgentsWizardPage selectAgentsWizardPage;

	/**
	 * Time-line wizard page.
	 */
	private DefineTimelineWizardPage timelineWizardPage;

	/**
	 * Initially selected CMR.
	 */
	private CmrRepositoryDefinition selectedCmr;

	/**
	 * Public constructor.
	 */
	public StartRecordingWizard() {
		super();
		this.setWindowTitle("Start Recording Wizard");
	}

	/**
	 * This constructor will extract the {@link CmrRepositoryDefinition} out of
	 * {@link IStorageDataProvider}.
	 *
	 * @param storageDataProvider
	 *            {@link IStorageDataProvider}.
	 */
	public StartRecordingWizard(IStorageDataProvider storageDataProvider) {
		this();
		this.selectedCmr = storageDataProvider.getCmrRepositoryDefinition();
	}

	/**
	 * This constructor gets the selected {@link CmrRepositoryDefinition}.
	 *
	 * @param selectedCmr
	 *            Selected {@link CmrRepositoryDefinition}.
	 */
	public StartRecordingWizard(CmrRepositoryDefinition selectedCmr) {
		this();
		this.selectedCmr = selectedCmr;
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
		defineNewStorageWizzardPage = new DefineNewStorageWizzardPage(selectedCmr);
		addPage(defineNewStorageWizzardPage);
		selectStorageWizardPage = new SelectExistingStorageWizardPage(selectedCmr);
		addPage(selectStorageWizardPage);
		selectAgentsWizardPage = new SelectAgentsWizardPage("Select Agent(s) that should participate in recording");
		addPage(selectAgentsWizardPage);
		defineDataPage = new DefineDataProcessorsWizardPage(DefineDataProcessorsWizardPage.BUFFER_DATA | DefineDataProcessorsWizardPage.SYSTEM_DATA
				| DefineDataProcessorsWizardPage.EXTRACT_INVOCATIONS);
		addPage(defineDataPage);
		timelineWizardPage = new DefineTimelineWizardPage("Limit Recording", "Optionally select how long recording should last", DefineTimelineWizardPage.FUTURE);
		addPage(timelineWizardPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		StorageData storageData;
		CmrRepositoryDefinition cmrRepositoryDefinition;

		if (newOrExistsingStorageWizardPage.useNewStorage()) {
			storageData = defineNewStorageWizzardPage.getStorageData();
			cmrRepositoryDefinition = defineNewStorageWizzardPage.getSelectedRepository();
		} else {
			storageData = selectStorageWizardPage.getSelectedStorageData();
			cmrRepositoryDefinition = selectStorageWizardPage.getSelectedRepository();
		}

		if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
			RecordingProperties recordingProperties = new RecordingProperties();
			recordingProperties.setRecordingDataProcessors(defineDataPage.getProcessorList());
			if (timelineWizardPage.isTimerframeUsed()) {
				Date recordEndDate = timelineWizardPage.getToDate();
				recordingProperties.setRecordEndDate(recordEndDate);
			}
			boolean isRecordingActive = cmrRepositoryDefinition.getStorageService().isRecordingOn();
			if (!isRecordingActive) {
				try {
					cmrRepositoryDefinition.getStorageService().startRecording(storageData, recordingProperties);
				} catch (StorageException e) {
					InspectIT.getDefault().createErrorDialog("Recording did not start.", e, -1);
					return false;
				}
			}
		} else {
			InspectIT.getDefault().createErrorDialog("Recording did not start. Selected CMR repository is currently not available.", -1);
			return false;
		}
		return true;
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
				return selectStorageWizardPage;
			}
		} else if (ObjectUtils.equals(page, defineNewStorageWizzardPage)) {
			selectAgentsWizardPage.setCmrRepositoryDefinition(defineNewStorageWizzardPage.getSelectedRepository());
			return selectAgentsWizardPage;
		} else if (ObjectUtils.equals(page, selectStorageWizardPage)) {
			selectAgentsWizardPage.setCmrRepositoryDefinition(selectStorageWizardPage.getSelectedRepository());
			return selectAgentsWizardPage;
		} else {
			return super.getNextPage(page);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		if (ObjectUtils.equals(page, defineNewStorageWizzardPage) || ObjectUtils.equals(page, selectStorageWizardPage)) {
			return newOrExistsingStorageWizardPage;
		} else if (ObjectUtils.equals(page, selectAgentsWizardPage)) {
			if (newOrExistsingStorageWizardPage.useNewStorage()) {
				return defineNewStorageWizzardPage;
			} else {
				return selectStorageWizardPage;
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
		} else if (!newOrExistsingStorageWizardPage.useNewStorage() && !selectStorageWizardPage.isPageComplete()) {
			return false;
		} else if (!selectAgentsWizardPage.isPageComplete()) {
			return false;
		} else if (!defineDataPage.isPageComplete()) {
			return false;
		} else if (!timelineWizardPage.isPageComplete()) {
			return false;
		}
		return true;
	}
}