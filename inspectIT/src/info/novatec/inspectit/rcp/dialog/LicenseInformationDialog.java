package info.novatec.inspectit.rcp.dialog;

import info.novatec.inspectit.communication.data.cmr.LicenseInfoData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.handlers.ImportLicenseHandler;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Dialog for displaying license information.
 * 
 * @author Ivan Senic
 * 
 */
public class LicenseInformationDialog extends TitleAreaDialog {

	/**
	 * Repository definition.
	 */
	private CmrRepositoryDefinition repositoryDefinition;

	/**
	 * Id for import license button.
	 */
	private static final int IMPORT_LICENCE_BUTTON_ID = -1;

	/**
	 * Id for get license button.
	 */
	private static final int GET_LICENCE_BUTTON_ID = -2;

	/**
	 * Composite that is created when dialog is created.
	 */
	private Composite dialogAreaComposite;

	/**
	 * Sub composite where all the widgets are. This composite can be disposed and re created.
	 */
	private Composite mainComposite;

	/**
	 * Last loaded/impored {@link LicenseInfoData}.
	 */
	private LicenseInfoData licenseInfoData;

	private Label validFrom;

	private Label validUntil;

	private Label maxAgents;

	private FormText holder;

	/**
	 * Constructor.
	 * 
	 * @param parentShell
	 *            the parent shell, or <code>null</code> to create a top-level shell
	 * @param repositoryDefinition
	 *            repository definition to display information for
	 */
	public LicenseInformationDialog(Shell parentShell, CmrRepositoryDefinition repositoryDefinition) {
		super(parentShell);
		this.repositoryDefinition = repositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create() {
		super.create();
		this.setTitle("License Information");
		this.setMessage("Current license info for the " + repositoryDefinition.getName() + " (" + repositoryDefinition.getIp() + ":" + repositoryDefinition.getPort() + ")",
				IMessageProvider.INFORMATION);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		mainComposite = new Composite(parent, SWT.NONE);

		mainComposite.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.minimumWidth = 400;
		gd.minimumHeight = 150;
		mainComposite.setLayoutData(gd);

		Label label = new Label(mainComposite, SWT.NONE);
		label.setText("License valid from:");
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		validFrom = new Label(mainComposite, SWT.WRAP);
		validFrom.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		label = new Label(mainComposite, SWT.NONE);
		label.setText("License valid until:");
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		validUntil = new Label(mainComposite, SWT.WRAP);
		validUntil.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		label = new Label(mainComposite, SWT.NONE);
		label.setText("Max agents allowed:");
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		maxAgents = new Label(mainComposite, SWT.WRAP);
		maxAgents.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		label = new Label(mainComposite, SWT.NONE);
		label.setText("License holder:");
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		holder = new FormText(mainComposite, SWT.WRAP | SWT.NO_FOCUS);
		holder.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		LicenseInfoData licenseInfoData = repositoryDefinition.getLicenseService().getLicenseInfoData();
		displayInformation(licenseInfoData);

		mainComposite.setFocus();
		return mainComposite;
	}

	/**
	 * Creates the widgets.
	 * 
	 * @param licenceInfoData
	 *            License information.
	 */
	private void displayInformation(LicenseInfoData licenceInfoData) {
		if (null != licenceInfoData) {
			validFrom.setText(DateFormat.getDateInstance().format(licenceInfoData.getNotBefore()));
		} else {
			validFrom.setText("-");
		}

		if (null != licenceInfoData) {
			validUntil.setText(DateFormat.getDateInstance().format(licenceInfoData.getNotAfter()));
		} else {
			validUntil.setText("-");
		}

		if (null != licenceInfoData) {
			maxAgents.setText(licenceInfoData.getMaximumAgents() + " agent(s)");
		} else {
			maxAgents.setText("-");
		}

		if (null != licenceInfoData) {
			String holderText = licenceInfoData.getHolder();
			String text = "<form><p>" + StringUtils.replace(holderText, ",", "<br/>") + "</p></form>";
			holder.setText(text, true, false);
		} else {
			holder.setText("-", false, false);
		}

		mainComposite.layout(true, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, GET_LICENCE_BUTTON_ID, "Get license", false);
		createButton(parent, IMPORT_LICENCE_BUTTON_ID, "Import license", false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, true);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Licence Information");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IMPORT_LICENCE_BUTTON_ID) {
			try {
				IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
				ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

				Command command = commandService.getCommand(ImportLicenseHandler.COMMAND);
				ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, new Event());
				IEvaluationContext context = (IEvaluationContext) executionEvent.getApplicationContext();
				context.addVariable(ImportLicenseHandler.INPUT, repositoryDefinition);
				command.executeWithChecks(executionEvent);
			} catch (Exception e) {
				InspectIT.getDefault().createErrorDialog(e.getMessage(), e, -1);
			}
			refreshData();
		} else if (buttonId == GET_LICENCE_BUTTON_ID) {
			IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
			try {
				IWebBrowser browser = browserSupport.createBrowser(null);
				URL url = new URL("http://www.inspectit.eu");
				browser.openURL(url);
			} catch (PartInitException e) {
				InspectIT.getDefault().createErrorDialog(e.getMessage(), e, -1);
			} catch (MalformedURLException e) {
				InspectIT.getDefault().createErrorDialog(e.getMessage(), e, -1);
			}
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * 
	 * @return Last loaded {@link LicenseInfoData}. This method returns valid results only if the
	 *         dialog returned with the {@link SWT#OK} value.
	 */
	public LicenseInfoData getLicenseInfoData() {
		return licenseInfoData;
	}

	/**
	 * Refreshes the data on the dialog, with new data from the CMR.
	 */
	private void refreshData() {
		licenseInfoData = repositoryDefinition.getLicenseService().getLicenseInfoData();
		displayInformation(licenseInfoData);
	}

}
