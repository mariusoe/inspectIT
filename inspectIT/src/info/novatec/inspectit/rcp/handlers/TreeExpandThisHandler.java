package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.editor.tree.TreeSubView;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler to expand the selected items.
 * 
 * @author Patrice Bouillet
 * 
 */
public class TreeExpandThisHandler extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		AbstractRootEditor rootEditor = (AbstractRootEditor) HandlerUtil.getActiveEditor(event);
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		TreeSubView treeSubView = (TreeSubView) rootEditor.getActiveSubView();

		for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			treeSubView.getTreeViewer().expandToLevel(object, TreeViewer.ALL_LEVELS);
		}

		return null;
	}

}
