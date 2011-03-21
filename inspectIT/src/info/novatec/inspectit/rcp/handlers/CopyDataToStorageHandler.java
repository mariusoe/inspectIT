package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.wizard.CopyDataToStorageWizard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for copying data to storage.
 * 
 * @author Ivan Senic
 * 
 */
public class CopyDataToStorageHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		AbstractRootEditor rootEditor = (AbstractRootEditor) HandlerUtil.getActiveEditor(event);
		RepositoryDefinition repositoryDefinition = rootEditor.getInputDefinition().getRepositoryDefinition();
		StructuredSelection selection = (StructuredSelection) HandlerUtil.getCurrentSelection(event);

		List<DefaultData> copyDataList = new ArrayList<DefaultData>(selection.size());
		for (Iterator<?> it = selection.iterator(); it.hasNext();) {
			Object nextObject = it.next();
			if (nextObject instanceof DefaultData) {
				copyDataList.add((DefaultData) nextObject);
			}
		}

		if (!copyDataList.isEmpty() && repositoryDefinition instanceof CmrRepositoryDefinition) {
			CopyDataToStorageWizard wizard = new CopyDataToStorageWizard((CmrRepositoryDefinition) repositoryDefinition, copyDataList);
			WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
			dialog.open();
		}

		return null;
	}

}
