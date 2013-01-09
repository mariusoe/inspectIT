package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.editor.tree.TreeSubView;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The handler to execute a details command on our tree sub views.
 * 
 * @author Patrice Bouillet
 * 
 */
public class TreeDetailsHandler extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		AbstractRootEditor rootEditor = (AbstractRootEditor) HandlerUtil.getActiveEditor(event);
		TreeSubView subView = (TreeSubView) rootEditor.getActiveSubView();

		subView.getTreeInputController().showDetails(subView.getTreeViewer().getTree().getShell(), selection.getFirstElement());

		return null;
	}
}
