package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.provider.IStorageDataProvider;
import info.novatec.inspectit.rcp.wizard.DownloadStorageWizard;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for downloading the complete storage.
 * 
 * @author Ivan Senic
 * 
 */
public class DownloadStorageHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection) {
			Object selected = ((StructuredSelection) selection).getFirstElement();
			if (selected instanceof IStorageDataProvider) {
				IStorageDataProvider storageDataProvider = (IStorageDataProvider) selected;
				WizardDialog wizardDialog = new WizardDialog(HandlerUtil.getActiveShell(event), new DownloadStorageWizard(storageDataProvider));
				wizardDialog.open();
			}

		}
		return null;
	}
}
