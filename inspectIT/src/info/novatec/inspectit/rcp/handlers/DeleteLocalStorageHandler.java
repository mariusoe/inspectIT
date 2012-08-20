package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.provider.ILocalStorageDataProvider;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinition;
import info.novatec.inspectit.rcp.util.ObjectUtils;
import info.novatec.inspectit.storage.LocalStorageData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for deleting the local storage.
 * 
 * @author Ivan Senic
 * 
 */
public class DeleteLocalStorageHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StructuredSelection structuredSelection = (StructuredSelection) HandlerUtil.getCurrentSelection(event);
		List<LocalStorageData> localStoragesToDelete = new ArrayList<LocalStorageData>();
		for (Iterator<?> it = structuredSelection.iterator(); it.hasNext();) {
			LocalStorageData localStorageData = ((ILocalStorageDataProvider) it.next()).getLocalStorageData();
			if (localStorageData.isFullyDownloaded()) {
				localStoragesToDelete.add(localStorageData);
			}
		}

		if (!localStoragesToDelete.isEmpty()) {
			StringBuffer confirmText = new StringBuffer(100);
			boolean plural = localStoragesToDelete.size() > 1;
			if (!plural) {
				confirmText.append("Are you sure you want to delete the locally downloaded data for the selected storage? ");
			} else {
				confirmText.append("Are you sure you want to  delete the locally downloaded data for the " + localStoragesToDelete.size() + " selected storages? ");
			}

			IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			List<IEditorPart> openedEditors = new ArrayList<IEditorPart>();
			IEditorReference[] editorReferences = activePage.getEditorReferences();
			for (IEditorReference editorReference : editorReferences) {
				IEditorPart editor = editorReference.getEditor(false);
				if (editor instanceof AbstractRootEditor) {
					RepositoryDefinition repositoryDefinition = ((AbstractRootEditor) editor).getInputDefinition().getRepositoryDefinition();
					if (isStorageForRepository(repositoryDefinition, localStoragesToDelete)) {
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

			if (SWT.OK == confirmDelete.open()) {
				for (LocalStorageData localStorageData : localStoragesToDelete) {
					try {
						InspectIT.getDefault().getInspectITStorageManager().deleteLocalStorageData(localStorageData);
					} catch (IOException e) {
						InspectIT.getDefault().createErrorDialog("There was an exception trying to delete local storage data.", e, -1);
						return null;
					}
				}

				// close editors
				for (IEditorPart editor : openedEditors) {
					activePage.closeEditor(editor, false);
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
	 * @param localStoragesToDelete
	 *            Storages that were deleted.
	 * @return True if repository fits to the deleted storages.
	 */
	private boolean isStorageForRepository(RepositoryDefinition repositoryDefinition, List<LocalStorageData> localStoragesToDelete) {
		if (repositoryDefinition instanceof StorageRepositoryDefinition) {
			StorageRepositoryDefinition storageRepositoryDefinition = (StorageRepositoryDefinition) repositoryDefinition;
			for (LocalStorageData localStorageData : localStoragesToDelete) {
				if (ObjectUtils.equals(storageRepositoryDefinition.getLocalStorageData().getId(), localStorageData.getId())) {
					return true;
				}
			}
		}
		return false;
	}

}
