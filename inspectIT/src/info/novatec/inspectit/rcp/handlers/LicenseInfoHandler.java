package info.novatec.inspectit.rcp.handlers;
import info.novatec.inspectit.rcp.dialog.LicenseInformationDialog;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for openning the licence information dialog.
 * 
 * @author Ivan Senic
 * 
 */
public class LicenseInfoHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection) {
			Object selectedObject = ((StructuredSelection) selection).getFirstElement();
			if (selectedObject instanceof ICmrRepositoryProvider) {
				CmrRepositoryDefinition cmrRepositoryDefinition = ((ICmrRepositoryProvider) selectedObject).getCmrRepositoryDefinition();
				LicenseInformationDialog licenseInformationDialog = new LicenseInformationDialog(HandlerUtil.getActiveShell(event), cmrRepositoryDefinition);
				licenseInformationDialog.open();
			}
		}

		return null;
	}

}
