package info.novatec.inspectit.rcp.wizard;

import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.wizard.page.ManageLabelWizardPage;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Manage Labels wizard.
 * 
 * @author Ivan Senic
 * 
 */
public class ManageLabelWizard extends Wizard implements INewWizard {

	/**
	 * CMR to manage labels for.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Page.
	 */
	private ManageLabelWizardPage manageLabelsPage;

	/**
	 * Default constructor.
	 * 
	 * @param cmrRepositoryDefinition
	 *            Repository to manage lables for.
	 */
	public ManageLabelWizard(CmrRepositoryDefinition cmrRepositoryDefinition) {
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {
		manageLabelsPage = new ManageLabelWizardPage(cmrRepositoryDefinition);
		addPage(manageLabelsPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		return true;
	}

	/**
	 * Gets {@link #shouldRefreshStorages}.
	 * 
	 * @return {@link #shouldRefreshStorages}
	 */
	public boolean isShouldRefreshStorages() {
		return manageLabelsPage.isShouldRefreshStorages();
	}

}
