package info.novatec.novaspy.rcp.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewRepositoryDefinitionPage extends WizardPage {

	private Text ip;

	private Text port;

	protected NewRepositoryDefinitionPage(String pageName) {
		super(pageName);
		setDescription("Please enter IP and Port for the definition of a new CMR");
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl(Composite parent) {
		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NONE);
		// create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		int ncol = 4;
		gl.numColumns = ncol;
		composite.setLayout(gl);

		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.widthHint = 25;

		new Label(composite, SWT.NONE).setText("IP:");
		ip = new Text(composite, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = ncol - 1;
		ip.setLayoutData(gd);

		new Label(composite, SWT.NONE).setText("Port:");
		port = new Text(composite, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = ncol - 1;
		port.setLayoutData(gd);

		setControl(composite);
	}

	public String getIp() {
		return ip.getText();
	}

	public String getPort() {
		return port.getText();
	}

}
