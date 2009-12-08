package info.novatec.novaspy.rcp.handlers;

import info.novatec.novaspy.rcp.editor.root.AbstractRootEditor;
import info.novatec.novaspy.rcp.editor.table.TableSubView;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The handler to execute a copy command on our table sub views.
 * 
 * @author Patrice Bouillet
 * 
 */
public class TableCopyHandler extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		AbstractRootEditor rootEditor = (AbstractRootEditor) HandlerUtil.getActiveEditor(event);
		TableSubView subView = (TableSubView) rootEditor.getActiveSubView();

		StringBuilder sb = new StringBuilder();
		for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			sb.append(subView.getTableInputController().getReadableString(object));
			sb.append(System.getProperty("line.separator"));
		}

		TextTransfer textTransfer = TextTransfer.getInstance();
		Clipboard cb = new Clipboard(HandlerUtil.getActiveShell(event).getDisplay());
		cb.setContents(new Object[] { sb.toString() }, new Transfer[] { textTransfer });

		return null;
	}
}
