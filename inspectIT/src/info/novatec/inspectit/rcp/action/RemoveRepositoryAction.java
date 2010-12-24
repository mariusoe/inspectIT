package info.novatec.inspectit.rcp.action;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.repository.RepositoryManager;
import info.novatec.inspectit.rcp.view.server.ServerView;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;

public class RemoveRepositoryAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	/**
	 * {@inheritDoc}
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	/**
	 * {@inheritDoc}
	 */
	public void run(IAction action) {
		try {
			ServerView view = (ServerView) window.getActivePage().showView(ServerView.ID);
			RepositoryDefinition repositoryDefinition = view.getActiveRepositoryDefinition();
			if (repositoryDefinition instanceof CmrRepositoryDefinition) {
				boolean pressedOk = MessageDialog.openQuestion(window.getShell(), "Confirm",
						"Do you really want to remove the server:\n" + repositoryDefinition.getIp() + ":" + repositoryDefinition.getPort());

				if (pressedOk) {
					// remove the server
					RepositoryManager repositoryManager = InspectIT.getDefault().getRepositoryManager();
					repositoryManager.removeRepositoryDefinition(repositoryDefinition);
				}
			}
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
	}

}
