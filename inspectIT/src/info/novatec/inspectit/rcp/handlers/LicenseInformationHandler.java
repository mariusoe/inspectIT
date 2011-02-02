package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.dialog.LicenseInformationDialog;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.view.server.ServerView;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for opneing the licence information dialog.
 * 
 * @author Ivan Senic
 * 
 */
public class LicenseInformationHandler extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ServerView serverView = (ServerView) HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().findView(ServerView.ID);
		RepositoryDefinition repositoryDefinition = serverView.getActiveRepositoryDefinition();
		Shell shell = HandlerUtil.getActiveShell(event);
		if (repositoryDefinition instanceof CmrRepositoryDefinition) {
			LicenseInformationDialog licenseDialog = new LicenseInformationDialog(shell, (CmrRepositoryDefinition) repositoryDefinition);
			licenseDialog.open();
		} else {
			MessageDialog.openInformation(shell, "License Information", "Please first select a CMR repository in the left navigation menu.");
		}
		return null;
	}

}
