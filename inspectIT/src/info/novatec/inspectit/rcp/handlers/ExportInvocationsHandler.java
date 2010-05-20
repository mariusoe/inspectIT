package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.service.IInvocationDataAccessService;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.repository.service.cmr.InvocationDataAccessService;
import info.novatec.inspectit.rcp.repository.service.storage.StorageNamingConstants;
import info.novatec.inspectit.rcp.util.ZipUtil;
import info.novatec.inspectit.rcp.wizard.ExportDataWizard;

import java.io.File;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The handler to export invocation sequences.
 * 
 * @author Patrice Bouillet
 * 
 */
public class ExportInvocationsHandler extends AbstractStorageHandler {

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		AbstractRootEditor editor = (AbstractRootEditor) HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getActiveEditor();
		RepositoryDefinition repositoryDefinition = editor.getInputDefinition().getRepositoryDefinition();

		// First, we need to execute our wizard where the user has to enter a
		// name
		ExportDataWizard wizard = new ExportDataWizard();
		WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
		dialog.setBlockOnOpen(true);

		if (WizardDialog.OK == dialog.open()) {
			IInvocationDataAccessService dataAccessService = repositoryDefinition.getInvocationDataAccessService();

			// create the temp storage directory
			File file = new File(wizard.getFileName());
			String folder = file.getName().split("\\.")[0];
			File dir = new File(StorageNamingConstants.DEFAULT_TEMP_DIRECTORY + folder);
			if (!dir.exists()) {
				dir.mkdirs();
			} else {
				// should never exist
				InspectIT.getDefault().createErrorDialog("Tried to create a tmp dir but failed: " + dir.getAbsolutePath(), null, -1);
				throw new ExecutionException("Tried to create a tmp dir but failed: " + dir.getAbsolutePath());
			}

			// save the invocation sequences
			saveInvocationSequences((InvocationDataAccessService) dataAccessService, dir, selection.iterator());

			// inefficient, should be replaced by just getting the correct
			// PlatformIdent object to save
			List<PlatformIdent> platformIdents = repositoryDefinition.getGlobalDataAccessService().getConnectedAgents();
			Long platformId = Long.valueOf(((InvocationSequenceData) selection.getFirstElement()).getPlatformIdent());

			for (PlatformIdent platformIdent : platformIdents) {
				if (platformIdent.getId().equals(platformId)) {
					savePlatform(dir, platformIdent);
					break;
				}
			}

			ZipUtil.zipFolder(dir.getAbsolutePath(), wizard.getFileName());

			deleteDirectory(dir);
		}

		return null;
	}

}
