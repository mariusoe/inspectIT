package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.provider.IStorageDataProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinition;
import info.novatec.inspectit.rcp.util.ObjectUtils;
import info.novatec.inspectit.storage.StorageException;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.type.impl.ExploredByLabelType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * Handler for deleting a Storage. Handler will inform the user about the users who have mounted the
 * storage that is about to be deleted.
 * 
 * @author Ivan Senic
 * 
 */
public class DeleteStorageHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection) {
			Iterator<?> it = ((StructuredSelection) selection).iterator();
			final List<IStorageDataProvider> storagesToDelete = new ArrayList<IStorageDataProvider>();
			Set<AbstractStorageLabel<String>> exploredBySet = new HashSet<AbstractStorageLabel<String>>();
			boolean confirmed = false;
			while (it.hasNext()) {
				Object nextObject = it.next();
				if (nextObject instanceof IStorageDataProvider) {
					storagesToDelete.add((IStorageDataProvider) nextObject);
					exploredBySet.addAll(((IStorageDataProvider) nextObject).getStorageData().getLabels(new ExploredByLabelType()));
				}
			}

			if (!storagesToDelete.isEmpty()) {
				StringBuffer confirmText = new StringBuffer();
				final boolean plural = storagesToDelete.size() > 1;
				if (!plural) {
					confirmText.append("Are you sure you want to delete the selected storage? ");
					if (!exploredBySet.isEmpty()) {
						confirmText.append("Note that the storage was (and still could be) explored by following users: ");
					}
				} else {
					confirmText.append("Are you sure you want to  delete the " + storagesToDelete.size() + " selected storages? ");
					if (!exploredBySet.isEmpty()) {
						confirmText.append("Note that the storages were (and still could be) explored by following users: ");
					}
				}
				if (!exploredBySet.isEmpty()) {
					for (AbstractStorageLabel<String> exploredByLabel : exploredBySet) {
						confirmText.append("\n * " + exploredByLabel.getValue());
					}
				}

				final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				final List<IEditorPart> openedEditors = new ArrayList<IEditorPart>();
				IEditorReference[] editorReferences = activePage.getEditorReferences();
				for (IEditorReference editorReference : editorReferences) {
					IEditorPart editor = editorReference.getEditor(false);
					if (editor instanceof AbstractRootEditor) {
						RepositoryDefinition repositoryDefinition = ((AbstractRootEditor) editor).getInputDefinition().getRepositoryDefinition();
						if (isStorageForRepository(repositoryDefinition, storagesToDelete)) {
							openedEditors.add(editor);
						}
					}
				}
				if (!openedEditors.isEmpty() && !plural) {
					confirmText.append("\n\nNote that all opened editors displaying the data from selected storage will be closed");
				} else if (!openedEditors.isEmpty() && plural) {
					confirmText.append("\n\nNote that all opened editors displaying the data from selected storages will be closed");
				}

				MessageBox confirmDelete = new MessageBox(HandlerUtil.getActiveShell(event), SWT.OK | SWT.CANCEL | SWT.ICON_QUESTION);
				confirmDelete.setText("Confirm Delete");
				confirmDelete.setMessage(confirmText.toString());
				confirmed = SWT.OK == confirmDelete.open();

				if (confirmed) {
					final boolean confirmedFinal = confirmed;
					Job deleteStorageJob = new Job("Delete Storage Job") {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							final Set<CmrRepositoryDefinition> involvedCmrSet = new HashSet<CmrRepositoryDefinition>();
							for (final IStorageDataProvider storageDataProvider : storagesToDelete) {
								if (storageDataProvider.getCmrRepositoryDefinition().getOnlineStatus() != OnlineStatus.OFFLINE) {
									involvedCmrSet.add(storageDataProvider.getCmrRepositoryDefinition());
									try {
										storageDataProvider.getCmrRepositoryDefinition().getStorageService().deleteStorage(storageDataProvider.getStorageData());
										InspectIT.getDefault().getInspectITStorageManager().storageRemotelyDeleted(storageDataProvider.getStorageData());
									} catch (final StorageException e) {
										Display.getDefault().asyncExec(new Runnable() {
											@Override
											public void run() {
												String name = storageDataProvider.getStorageData().getName();
												InspectIT.getDefault().createErrorDialog("Storage '" + name + "' could not be successfully deleted from CMR.", e, -1);
											}
										});
									} catch (final Exception e) {
										Display.getDefault().asyncExec(new Runnable() {
											@Override
											public void run() {
												String name = storageDataProvider.getStorageData().getName();
												InspectIT.getDefault().createErrorDialog("Local data for storage '" + name + "' was not cleared successfully.", e, -1);
											}
										});
									}
								} else {
									Display.getDefault().asyncExec(new Runnable() {
										@Override
										public void run() {
											String name = storageDataProvider.getStorageData().getName();
											InspectIT.getDefault().createInfoDialog("Storage '" + name + "' can not be deleted, because CMR where it is located is offline.", -1);
										}
									});
								}
							}

							if (confirmedFinal) {
								Display.getDefault().asyncExec(new Runnable() {
									@Override
									public void run() {
										// close editors
										for (IEditorPart editor : openedEditors) {
											activePage.closeEditor(editor, false);
										}
									}

								});
							}
							return Status.OK_STATUS;
						}
					};
					deleteStorageJob.setUser(true);
					deleteStorageJob.setProperty(IProgressConstants.ICON_PROPERTY, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_DELETE));
					deleteStorageJob.schedule();
				}
			}
		}
		return null;
	}

	/**
	 * Returns if the given repository is StorageRepositoryDefinition and is bounded to one of
	 * deleted storages.
	 * 
	 * @param repositoryDefinition
	 *            Repository.
	 * @param storagesToDelete
	 *            Storages that were deleted.
	 * @return True if repository fits to the deleted storages.
	 */
	private boolean isStorageForRepository(RepositoryDefinition repositoryDefinition, List<IStorageDataProvider> storagesToDelete) {
		if (repositoryDefinition instanceof StorageRepositoryDefinition) {
			StorageRepositoryDefinition storageRepositoryDefinition = (StorageRepositoryDefinition) repositoryDefinition;
			for (IStorageDataProvider storageDataProvider : storagesToDelete) {
				if (!storageRepositoryDefinition.getLocalStorageData().isFullyDownloaded()
						&& ObjectUtils.equals(storageRepositoryDefinition.getLocalStorageData().getId(), storageDataProvider.getStorageData().getId())) {
					return true;
				}
			}
		}
		return false;
	}

}
