package info.novatec.inspectit.rcp.action;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.root.IRootEditor;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.view.server.ServerView;

import java.util.Collections;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;

/**
 * Action for clear buffer.
 * 
 * @author Ivan Senic
 * 
 */
public class ClearRepositoryBufferAction implements IWorkbenchWindowActionDelegate {

	/**
	 * Workbench window.
	 */
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
	@Override
	public void run(IAction action) {
		try {
			ServerView view = (ServerView) window.getActivePage().showView(ServerView.ID);
			RepositoryDefinition repositoryDefinition = view.getActiveRepositoryDefinition();
			if (repositoryDefinition instanceof CmrRepositoryDefinition) {
				boolean isSure = MessageDialog.openConfirm(null, "Empty buffer",
						"Are you sure that you want to completely delete all the data in the buffer on repository " + repositoryDefinition.getIp() + ":" + repositoryDefinition.getPort() + "?");
				if (isSure) {
					repositoryDefinition.getBufferService().clearBuffer();
					IEditorReference[] editors = window.getActivePage().getEditorReferences();
					for (IEditorReference editor : editors) {
						IRootEditor rootEditor = (IRootEditor) editor.getEditor(false);
						if (null != rootEditor.getPreferencePanel()) {
							if (rootEditor.getSubView().getPreferenceIds().contains(PreferenceId.CLEAR_BUFFER)) {
								InputDefinition inputDefinition = rootEditor.getInputDefinition();
								if (inputDefinition.getRepositoryDefinition().equals(repositoryDefinition)) {
									rootEditor.getSubView().setDataInput(Collections.<DefaultData>emptyList());
								}
							}
						}
					}
				}
			}
		} catch (PartInitException e) {
			e.printStackTrace();
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
	}

}
