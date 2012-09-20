package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.communication.data.cmr.CmrStatusData;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.view.impl.RepositoryManagerView;
import info.novatec.inspectit.rcp.view.impl.StorageManagerView;
import info.novatec.inspectit.rcp.wizard.StartRecordingWizard;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Starts recording.
 * 
 * @author Ivan Senic
 * 
 */
public class StartRecordingHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// try to get the CMR where recording should start.
		CmrRepositoryDefinition cmrRepositoryDefinition = null;
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection) {
			if (((StructuredSelection) selection).getFirstElement() instanceof ICmrRepositoryProvider) {
				cmrRepositoryDefinition = ((ICmrRepositoryProvider) ((StructuredSelection) selection).getFirstElement()).getCmrRepositoryDefinition();
			}
		}

		// check if the writing state is OK
		try {
			CmrStatusData cmrStatusData = cmrRepositoryDefinition.getCmrManagementService().getCmrStatusData();
			if (cmrStatusData.isWarnSpaceLeftActive()) {
				String leftSpace = NumberFormatter.humanReadableByteCount(cmrStatusData.getStorageDataSpaceLeft());
				if (!MessageDialog.openQuestion(HandlerUtil.getActiveShell(event), "Confirm",
						"For selected CMR there is an active warning about insufficient storage space left. Only " + leftSpace
								+ " are left on the target server, are you sure you want to continue?")) {
					return null;
				}
			}
		} catch (Exception e) {
			// ignore because if we can not get the info. we will still respond to user action
		}

		// open wizard
		StartRecordingWizard startRecordingWizard = new StartRecordingWizard(cmrRepositoryDefinition);
		WizardDialog wizardDialog = new WizardDialog(HandlerUtil.getActiveShell(event), startRecordingWizard);
		wizardDialog.open();

		// if recording has been started refresh the repository and storage manager view
		if (wizardDialog.getReturnCode() == WizardDialog.OK) {
			IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart repositoryManagerView = activePage.findView(RepositoryManagerView.VIEW_ID);
			if (repositoryManagerView instanceof RepositoryManagerView) {
				((RepositoryManagerView) repositoryManagerView).refresh();
			}
			IViewPart storageManagerView = activePage.findView(StorageManagerView.VIEW_ID);
			if (storageManagerView instanceof StorageManagerView) {
				if (null != cmrRepositoryDefinition) {
					((StorageManagerView) storageManagerView).refresh(cmrRepositoryDefinition);
				} else {
					((StorageManagerView) storageManagerView).refresh();
				}
			}
		}
		return null;
	}

}
