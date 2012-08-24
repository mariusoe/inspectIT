package info.novatec.inspectit.rcp;

import info.novatec.inspectit.rcp.action.MenuAction;
import info.novatec.inspectit.rcp.wizard.AddCmrRepositoryWizard;
import info.novatec.inspectit.rcp.wizard.CreateStorageWizard;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * Our own {@link ActionBarAdvisor}.
 * 
 * @author Ivan Senic
 * 
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	/**
	 * Id for the menu action we manually create.
	 */
	private static final String MENU_ACTION_ADD = "info.novatec.inspectit.menu.action.add";

	/**
	 * Default constructor.
	 * 
	 * @param configurer
	 *            {@link IActionBarConfigurer}.
	 * @see ActionBarAdvisor#ActionBarAdvisor(IActionBarConfigurer)
	 */
	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void fillCoolBar(ICoolBarManager coolBar) {
		IAction addMenuAction = getAction(MENU_ACTION_ADD);
		ToolBarManager toolBarManager = new ToolBarManager();
		toolBarManager.add(addMenuAction);
		toolBarManager.add(getAction(IWorkbenchCommandConstants.FILE_IMPORT));

		coolBar.add(toolBarManager);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void makeActions(IWorkbenchWindow window) {
		super.makeActions(window);

		MenuAction addMenuAction = new MenuAction();
		addMenuAction.setId(MENU_ACTION_ADD);
		addMenuAction.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_ADD));
		addMenuAction.addContributionItem(new ActionContributionItem(new AddRepositoryAction(window)));
		addMenuAction.addContributionItem(new ActionContributionItem(new AddStorageAction(window)));
		register(addMenuAction);

		IAction importAction = ActionFactory.IMPORT.create(window);
		importAction.setId(IWorkbenchCommandConstants.FILE_IMPORT);
		register(importAction);
	}

	/**
	 * Add repository action.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class AddRepositoryAction extends Action {

		/**
		 * {@link ISelectionProvider} for opening the wizard dialog.
		 */
		private IShellProvider shellProvider;

		/**
		 * Default constructor.
		 * 
		 * @param shellProvider
		 *            {@link ISelectionProvider} for opening the wizard dialog.
		 */
		public AddRepositoryAction(IShellProvider shellProvider) {
			this.shellProvider = shellProvider;
			this.setText("Add Central Management Repository (CMR)");
			this.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_SERVER_ONLINE_SMALL));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			AddCmrRepositoryWizard addCmrRepositoryWizard = new AddCmrRepositoryWizard();
			WizardDialog wizardDialog = new WizardDialog(shellProvider.getShell(), addCmrRepositoryWizard);
			wizardDialog.open();
		}
	}

	/**
	 * Add repository action.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class AddStorageAction extends Action {

		/**
		 * {@link ISelectionProvider} for opening the wizard dialog.
		 */
		private IShellProvider shellProvider;

		/**
		 * Default constructor.
		 * 
		 * @param shellProvider
		 *            {@link ISelectionProvider} for opening the wizard dialog.
		 */
		public AddStorageAction(IShellProvider shellProvider) {
			this.shellProvider = shellProvider;
			this.setText("Create Storage");
			this.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_STOARGE_NEW));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			CreateStorageWizard createStorageWizard = new CreateStorageWizard();
			WizardDialog wizardDialog = new WizardDialog(shellProvider.getShell(), createStorageWizard);
			wizardDialog.open();
		}
	}

}
