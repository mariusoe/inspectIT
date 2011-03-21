package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.view.impl.StorageManagerView;
import info.novatec.inspectit.rcp.wizard.CreateStorageWizard;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for creating a new storage.
 * 
 * @author Ivan Senic
 * 
 */
public class CreateStorageHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		CreateStorageWizard csw = new CreateStorageWizard();
		WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), csw);
		dialog.open();
		if (dialog.getReturnCode() == WizardDialog.OK) {
			IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(StorageManagerView.VIEW_ID);
			if (viewPart instanceof StorageManagerView) {
				((StorageManagerView) viewPart).refresh();
			}
		} 
		return null;
	}

}
