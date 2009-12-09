package info.novatec.inspectit.rcp;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	// Actions - important to allocate these only in makeActions, and then use
	// them
	// in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.
	private IWorkbenchAction exitAction;
	private IWorkbenchAction aboutAction;
	private IWorkbenchAction prefAction;
	private IWorkbenchAction saveAction;
	private IWorkbenchAction cutAction;
	private IWorkbenchAction copyAction;
	private IWorkbenchAction pasteAction;
	private IWorkbenchAction deleteAction;
	private IWorkbenchAction selectAllAction;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void makeActions(final IWorkbenchWindow window) {
		// Creates the actions and registers them.
		// Registering is needed to ensure that key bindings work.
		// The corresponding commands keybindings are defined in the plugin.xml
		// file.
		// Registering also provides automatic disposal of the actions when
		// the window is closed.

		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);
		aboutAction = ActionFactory.ABOUT.create(window);
		register(aboutAction);
		prefAction = ActionFactory.PREFERENCES.create(window);
		register(prefAction);
		saveAction = ActionFactory.SAVE.create(window);
		register(saveAction);
		cutAction = ActionFactory.CUT.create(window);
		register(cutAction);
		copyAction = ActionFactory.COPY.create(window);
		register(copyAction);
		pasteAction = ActionFactory.PASTE.create(window);
		register(pasteAction);
		deleteAction = ActionFactory.DELETE.create(window);
		register(deleteAction);
		selectAllAction = ActionFactory.SELECT_ALL.create(window);
		register(selectAllAction);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
		MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
		menuBar.add(fileMenu);

		// MenuManager editMenu = new MenuManager("&Edit",
		// IWorkbenchActionConstants.M_EDIT);
		// menuBar.add(editMenu);

		MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
		menuBar.add(helpMenu);

		// fileMenu.add(saveAction);
		// fileMenu.add(new Separator());
		// fileMenu.add(prefAction);
		// fileMenu.add(new Separator());
		fileMenu.add(exitAction);
		// editMenu.add(cutAction);
		// editMenu.add(copyAction);
		// editMenu.add(pasteAction);
		// editMenu.add(new Separator());
		// editMenu.add(deleteAction);
		// editMenu.add(new Separator());
		// editMenu.add(selectAllAction);
		helpMenu.add(aboutAction);

	}

}
