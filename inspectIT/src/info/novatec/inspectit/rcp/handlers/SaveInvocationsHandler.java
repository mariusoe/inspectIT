package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.service.IInvocationDataAccessService;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.repository.service.storage.StorageNamingConstants;
import info.novatec.inspectit.rcp.wizard.SaveDataWizard;

import java.io.File;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The handler to save invocation sequences.
 * 
 * @author Patrice Bouillet
 * 
 */
public class SaveInvocationsHandler extends AbstractStorageHandler {

	@SuppressWarnings("unchecked")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Display display = HandlerUtil.getActiveShell(event).getDisplay();
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		AbstractRootEditor editor = (AbstractRootEditor) HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getActiveEditor();
		RepositoryDefinition repositoryDefinition = editor.getInputDefinition().getRepositoryDefinition();

		// First, we need to execute our wizard where the user has to enter a
		// name
		SaveDataWizard wizard = new SaveDataWizard();
		WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
		dialog.setBlockOnOpen(true);

		if (WizardDialog.OK == dialog.open()) {
			IInvocationDataAccessService dataAccessService = repositoryDefinition.getInvocationDataAccessService();

			// create the storage directory if it does not exist
			File dir = new File(StorageNamingConstants.DEFAULT_STORAGE_DIRECTORY + wizard.getStorageName());
			if (!dir.exists()) {
				boolean created = dir.mkdirs();
				if (!created) {
					// something unexpected happened and the directory couldn't be created --> abort
					InspectIT.getDefault().createErrorDialog("The following temp directory could not be created: " + dir.getAbsolutePath(), null, -1);
					throw new ExecutionException("The following temp directory could not be created: " + dir.getAbsolutePath());
				}
			}

			// first, save the invocation sequences
			saveInvocationSequences(dataAccessService, dir, selection.iterator());

			// inefficient, should be replaced by just getting the correct
			// PlatformIdent object to save
			List<PlatformIdent> platformIdents = (List<PlatformIdent>) repositoryDefinition.getGlobalDataAccessService().getConnectedAgents();
			Long platformId = Long.valueOf(((InvocationSequenceData) selection.getFirstElement()).getPlatformIdent());

			for (PlatformIdent platformIdent : platformIdents) {
				if (platformIdent.getId().equals(platformId)) {
					savePlatform(dir, platformIdent);
					break;
				}
			}

			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					InspectIT.getDefault().getRepositoryManager().updateStorageRepository();
				}
			});
		}

		return null;
	}

}
