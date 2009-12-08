package info.novatec.novaspy.rcp.handlers;

import info.novatec.novaspy.rcp.editor.InputDefinition;
import info.novatec.novaspy.rcp.editor.root.FormRootEditor;
import info.novatec.novaspy.rcp.editor.root.RootEditorInput;
import info.novatec.novaspy.rcp.model.Component;
import info.novatec.novaspy.rcp.view.server.ServerView;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The open view handler which takes care of opening a view by retrieving the
 * {@link InputDefinition}.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class OpenViewHandler extends AbstractHandler {

	/**
	 * The constructor.
	 */
	public OpenViewHandler() {
	}

	/**
	 * {@inheritDoc}
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the view
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage page = window.getActivePage();
		ServerView view = (ServerView) page.findView(ServerView.ID);
		// Get the selection
		ISelection selection = view.getSite().getSelectionProvider().getSelection();
		if (selection != null && selection instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection) selection).getFirstElement();
			// If we had a selection lets open the editor
			if (obj != null) {
				Component component = (Component) obj;
				if (null != component.getInputDefinition()) {
					RootEditorInput input = new RootEditorInput(component.getInputDefinition());
					try {
						page.openEditor(input, FormRootEditor.ID);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}
}
