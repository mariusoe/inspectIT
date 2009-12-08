package info.novatec.novaspy.rcp.action;

import info.novatec.novaspy.rcp.NovaSpy;
import info.novatec.novaspy.rcp.repository.RepositoryDefinition;
import info.novatec.novaspy.rcp.repository.RepositoryManager;
import info.novatec.novaspy.rcp.view.server.ServerView;

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
			boolean pressedOk = MessageDialog.openQuestion(window.getShell(), "Confirm", "Do you really want to remove the server:\n" + repositoryDefinition.getIp() + ":"
					+ repositoryDefinition.getPort());

			if (pressedOk) {
				// remove the server
				RepositoryManager repositoryManager = NovaSpy.getDefault().getRepositoryManager();
				repositoryManager.removeRepositoryDefinition(repositoryDefinition);
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
