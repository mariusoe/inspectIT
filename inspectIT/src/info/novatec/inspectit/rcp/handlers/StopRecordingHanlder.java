package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.view.impl.RepositoryManagerView;
import info.novatec.inspectit.rcp.view.impl.StorageManagerView;
import info.novatec.inspectit.storage.StorageException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * Stops recording.
 * 
 * @author Ivan Senic
 * 
 */
public class StopRecordingHanlder extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		CmrRepositoryDefinition cmrRepositoryDefinition = null;
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection) {
			if (((StructuredSelection) selection).getFirstElement() instanceof ICmrRepositoryProvider) {
				cmrRepositoryDefinition = ((ICmrRepositoryProvider) ((StructuredSelection) selection).getFirstElement()).getCmrRepositoryDefinition();
			}
		}
		if (null != cmrRepositoryDefinition) {
			if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
				boolean isRecordingActive = cmrRepositoryDefinition.getStorageService().isRecordingOn();
				if (isRecordingActive) {
					try {
						final CmrRepositoryDefinition finalCmrRepositoryDefinition = cmrRepositoryDefinition;
						Job stopRecordingJob = new Job("Stop Recording") {
							@Override
							protected IStatus run(IProgressMonitor monitor) {
								try {
									finalCmrRepositoryDefinition.getStorageService().stopRecording();
									Display.getDefault().asyncExec(new Runnable() {
										@Override
										public void run() {
											IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
											IViewPart repositoryManagerView = activePage.findView(RepositoryManagerView.VIEW_ID);
											if (repositoryManagerView instanceof RepositoryManagerView) {
												((RepositoryManagerView) repositoryManagerView).refresh();
											}
											IViewPart storageManagerView = activePage.findView(StorageManagerView.VIEW_ID);
											if (storageManagerView instanceof StorageManagerView) {
												((StorageManagerView) storageManagerView).refresh(finalCmrRepositoryDefinition);
											}
										}
									});
								} catch (final StorageException e) {
									Display.getDefault().asyncExec(new Runnable() {
										@Override
										public void run() {
											InspectIT.getDefault().createErrorDialog("Stoping the recording failed", e, -1);
										}
									});
								}
								return Status.OK_STATUS;
							}
						};
						stopRecordingJob.setUser(true);
						stopRecordingJob.setProperty(IProgressConstants.ICON_PROPERTY, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_RECORD_STOP));
						stopRecordingJob.schedule();
					} catch (Exception e) {
						InspectIT.getDefault().createErrorDialog("Execution Error", e, -1);
					}
				}
			} else {
				InspectIT.getDefault().createErrorDialog("Recording can not be stoped, because the repository is currenlty offline.", null, -1);
			}
		}
		return null;
	}

}
