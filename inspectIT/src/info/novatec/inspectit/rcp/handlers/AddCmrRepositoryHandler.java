package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.dialog.AddCmrRepositoryDefinitionDialog;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryManager;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Add CMR repository handler.
 * 
 * @author Ivan Senic
 * 
 */
public class AddCmrRepositoryHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		AddCmrRepositoryDefinitionDialog dialog = new AddCmrRepositoryDefinitionDialog(HandlerUtil.getActiveShell(event));
		dialog.open();
		if (dialog.getReturnCode() == Window.OK) {
			CmrRepositoryDefinition cmrRepositoryDefinition = dialog.getCmrRepositoryDefinition();
			CmrRepositoryManager cmrRepositoryManager = InspectIT.getDefault().getCmrRepositoryManager();
			if (!cmrRepositoryManager.getCmrRepositoryDefinitions().contains(cmrRepositoryDefinition)) {
				cmrRepositoryManager.addCmrRepositoryDefinition(cmrRepositoryDefinition);
			} else {
				InspectIT.getDefault().createInfoDialog("Central management repository (CMR) with given IP and port already exists.", -1);
			}
		}
		return null;
	}

}
