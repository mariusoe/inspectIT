package info.novatec.inspectit.rcp.wizard.page;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard page for definition of the new {@link CmrRepositoryDefinition}.
 * 
 * @author Ivan Senic
 * 
 */
public class DefineCmrWizardPage extends WizardPage {

	/**
	 * Default page message.
	 */
	private static final String DEFAULT_MESSAGE = "Define the information for the new CMR Repository";

	/**
	 * Name tex box.
	 */
	private Text nameBox;

	/**
	 * IP tex box.
	 */
	private Text ipBox;

	/**
	 * Port tex box.
	 */
	private Text portBox;

	/**
	 * Description box.
	 */
	private Text descriptionBox;

	/**
	 * List of existing repositories to check if the same one already exists.
	 */
	private List<CmrRepositoryDefinition> existingRepositories;

	/**
	 * Default constructor.
	 */
	public DefineCmrWizardPage() {
		super("Add New CMR Repository");
		this.setTitle("Add New CMR Repository");
		this.setMessage(DEFAULT_MESSAGE);
		this.existingRepositories = InspectIT.getDefault().getCmrRepositoryManager().getCmrRepositoryDefinitions();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		final Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(4, false));

		Label nameLabel = new Label(main, SWT.LEFT);
		nameLabel.setText("Server name:");
		nameBox = new Text(main, SWT.BORDER);
		nameBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

		Label ipLabel = new Label(main, SWT.LEFT);
		ipLabel.setText("IP Address:");
		ipBox = new Text(main, SWT.BORDER);
		ipBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		ipBox.setText(CmrRepositoryDefinition.DEFAULT_IP);

		Label portLabel = new Label(main, SWT.LEFT);
		portLabel.setText("Port:");
		portBox = new Text(main, SWT.BORDER | SWT.RIGHT);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		portBox.setLayoutData(gd);
		portBox.setText(String.valueOf(CmrRepositoryDefinition.DEFAULT_PORT));
		portBox.setTextLimit(5);
		portBox.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (portBox.getText().length() > String.valueOf(CmrRepositoryDefinition.DEFAULT_PORT).length()) {
					main.layout();
				}
			}
		});

		Label descLabel = new Label(main, SWT.LEFT);
		descLabel.setText("Description:");
		descLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		descriptionBox = new Text(main, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		descriptionBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));

		Listener pageCompletionListener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				setPageComplete(isPageComplete());
				setPageMessage();
			}
		};

		nameBox.addListener(SWT.Modify, pageCompletionListener);
		ipBox.addListener(SWT.Modify, pageCompletionListener);
		portBox.addListener(SWT.Modify, pageCompletionListener);

		setControl(main);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canFlipToNextPage() {
		return isPageComplete();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		if (nameBox.getText().isEmpty()) {
			return false;
		}
		if (ipBox.getText().trim().isEmpty()) {
			return false;
		}
		if (portBox.getText().trim().isEmpty()) {
			return false;
		} else {
			try {
				Integer.parseInt(portBox.getText().trim());
			} catch (NumberFormatException e) {
				return false;
			}
		}

		String ip = ipBox.getText().trim();
		int port = Integer.parseInt(portBox.getText().trim());
		for (CmrRepositoryDefinition cmrRepositoryDefinition : existingRepositories) {
			if (Objects.equals(ip, cmrRepositoryDefinition.getIp()) && port == cmrRepositoryDefinition.getPort()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return Returns the defined {@link CmrRepositoryDefinition}.
	 */
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		CmrRepositoryDefinition cmrRepositoryDefinition = new CmrRepositoryDefinition(ipBox.getText().trim(), Integer.parseInt(portBox.getText()), nameBox.getText().trim());
		if (!descriptionBox.getText().trim().isEmpty()) {
			cmrRepositoryDefinition.setDescription(descriptionBox.getText().trim());
		} else {
			cmrRepositoryDefinition.setDescription("");
		}
		return cmrRepositoryDefinition;
	}

	/**
	 * Sets the message based on the page selections.
	 */
	private void setPageMessage() {
		if (nameBox.getText().isEmpty()) {
			setMessage("No value for the CMR name entered", ERROR);
			return;
		}
		if (ipBox.getText().trim().isEmpty()) {
			setMessage("No value for the CMR IP address entered", ERROR);
			return;
		}
		if (portBox.getText().trim().isEmpty()) {
			setMessage("No value for the CMR port entered", ERROR);
			return;
		} else {
			try {
				Integer.parseInt(portBox.getText().trim());
			} catch (NumberFormatException e) {
				setMessage("The port is not in a valid number format", ERROR);
				return;
			}
		}

		String ip = ipBox.getText().trim();
		int port = Integer.parseInt(portBox.getText().trim());
		for (CmrRepositoryDefinition cmrRepositoryDefinition : existingRepositories) {
			if (Objects.equals(ip, cmrRepositoryDefinition.getIp()) && port == cmrRepositoryDefinition.getPort()) {
				setMessage("The repository with given IP address and port already exists", ERROR);
				return;
			}
		}

		setMessage(DEFAULT_MESSAGE);
	}

}
