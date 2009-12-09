package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.cmr.service.ILicenseService;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.view.server.ServerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The import license handler which takes care of opening a file select dialog
 * and send the content of the file as byte[] to LicenseService.
 * 
 * @author Patrice Bouillet
 * @author Dirk Maucher
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ImportLicenseHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		FileDialog fileDialog = new FileDialog(HandlerUtil.getActiveShell(event), SWT.OPEN);
		fileDialog.setFilterExtensions(new String[] { "*.lic" });
		String licenseFileName = fileDialog.open();

		if (null != licenseFileName) {
			byte[] licenseFileContent = null;
			File licenseFile = new File(licenseFileName);
			FileInputStream licenseFileInputStream = null;

			try {
				licenseFileInputStream = new FileInputStream(licenseFile);
				licenseFileContent = new byte[(int) licenseFile.length()];
				licenseFileInputStream.read(licenseFileContent);
			} catch (FileNotFoundException e) {
				showException(e);
				return null;
			} catch (IOException e) {
				showException(e);
				return null;
			} finally {
				try {
					licenseFileInputStream.close();
				} catch (IOException e) {
					// we do not care about this exception
				}
			}

			ServerView serverView = (ServerView) HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().findView(ServerView.ID);
			RepositoryDefinition repositoryDefinition = serverView.getActiveRepositoryDefinition();
			ILicenseService licenseService = repositoryDefinition.getLicenseService();

			try {
				licenseService.receiveLicenseContent(licenseFileContent);
				InspectIT.getDefault().createInfoDialog("Successfully imported new license", -1);
			} catch (Exception e) {
				showException(e);
			}

		}

		return null;
	}

	/**
	 * Shows an exception to the user.
	 * 
	 * @param e
	 *            The exception.
	 */
	private void showException(Exception e) {
		InspectIT.getDefault().createErrorDialog(e.getMessage(), e, -1);
	}

}
