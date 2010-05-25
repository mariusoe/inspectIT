package info.novatec.inspectit.rcp.wizard;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;

public class NewRepositoryDefinitionWizard extends Wizard {

	private NewRepositoryDefinitionPage newRepositoryDefinitionPage;

	public NewRepositoryDefinitionWizard() {
		this.setWindowTitle("New Repository Definition Wizard");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {
		newRepositoryDefinitionPage = new NewRepositoryDefinitionPage("Repository Definition Wizard");
		addPage(newRepositoryDefinitionPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		String ip = newRepositoryDefinitionPage.getIp();
		int port = Integer.parseInt(newRepositoryDefinitionPage.getPort());

		RepositoryDefinition repositoryDefinition = new CmrRepositoryDefinition(ip, port);
		InspectIT.getDefault().getRepositoryManager().addRepositoryDefinition(repositoryDefinition);

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Image getDefaultPageImage() {
		return InspectIT.getDefault().getImage(InspectITConstants.IMG_SERVER_ADD);
	}

}
