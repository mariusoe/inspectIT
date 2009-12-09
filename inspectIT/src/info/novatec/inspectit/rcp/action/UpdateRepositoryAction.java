package info.novatec.inspectit.rcp.action;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.repository.RepositoryManager;
import info.novatec.inspectit.rcp.view.server.ServerView;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;


public class UpdateRepositoryAction implements IWorkbenchWindowActionDelegate {

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
			RepositoryManager repositoryManager = InspectIT.getDefault().getRepositoryManager();
			repositoryManager.updateRepositoryDefinition(repositoryDefinition);
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
