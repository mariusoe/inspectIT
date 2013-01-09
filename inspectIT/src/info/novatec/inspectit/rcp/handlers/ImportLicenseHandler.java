package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.cmr.service.ILicenseService;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The import license handler which takes care of opening a file select dialog and send the content
 * of the file as byte[] to LicenseService.
 * 
 * @author Patrice Bouillet
 * @author Dirk Maucher
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ImportLicenseHandler extends AbstractHandler {

	/**
	 * Command name.
	 */
	public static final String COMMAND = "info.novatec.inspectit.rcp.commands.importLicense";

	/**
	 * Input name.
	 */
	public static final String INPUT = COMMAND + ".input";

	/**
	 * {@inheritDoc}
	 */
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
					if (null != licenseFileInputStream) {
						licenseFileInputStream.close();
					}
				} catch (IOException e) { // NOPMD NOCHK
					// we do not care about this exception
				}
			}

			IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
			Object input = context.getVariable(INPUT);
			if (null == input) {
				showException(new RuntimeException("License importing can not continue, due to the wrong application context data."));
				return null;
			}
			RepositoryDefinition repositoryDefinition = (RepositoryDefinition) input;
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
