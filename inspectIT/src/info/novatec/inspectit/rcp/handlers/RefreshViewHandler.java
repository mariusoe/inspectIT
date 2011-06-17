package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.editor.ISubView;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Refresh view handler that refreash the current active sub-view.
 * 
 * @author Ivan Senic
 * 
 */
public class RefreshViewHandler extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
		if (activeEditor instanceof AbstractRootEditor) {
			ISubView subView = ((AbstractRootEditor) activeEditor).getSubView();
			subView.doRefresh();
		}
		return null;
	}

}
