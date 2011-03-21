package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.dialog.EditRepositoryDataDialog;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.view.impl.RepositoryManagerView;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Rename the CMR name and description handler.
 * 
 * @author Ivan Senic
 * 
 */
public class EditCmrRepositoryHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		CmrRepositoryDefinition cmrRepositoryDefinition = null;
		Object selectedElement = ((StructuredSelection) HandlerUtil.getCurrentSelection(event)).getFirstElement();
		if (selectedElement instanceof ICmrRepositoryProvider) {
			cmrRepositoryDefinition = ((ICmrRepositoryProvider) selectedElement).getCmrRepositoryDefinition();
		} else {
			return null;
		}

		EditRepositoryDataDialog editDialog = new EditRepositoryDataDialog(HandlerUtil.getActiveShell(event), cmrRepositoryDefinition.getName(), cmrRepositoryDefinition.getDescription());
		editDialog.open();
		if (editDialog.getReturnCode() == EditRepositoryDataDialog.OK) {
			cmrRepositoryDefinition.setName(editDialog.getName());
			cmrRepositoryDefinition.setDescription(editDialog.getDescription());
			InspectIT.getDefault().getCmrRepositoryManager().updateCmrRepositoryDefinitionData(cmrRepositoryDefinition);
			IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(RepositoryManagerView.VIEW_ID);
			if (viewPart instanceof RepositoryManagerView) {
				((RepositoryManagerView) viewPart).refresh();
			}
		}

		return null;
	}

}
