package info.novatec.inspectit.rcp.wizard.page;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * Wizard page for selecting the agents.
 * 
 * @author Ivan Senic
 * 
 */
public class SelectAgentsWizardPage extends WizardPage {

	/**
	 * Default wizard message.
	 */
	private static final String DEFAULT_MESSAGE = "Selected Agent(s)";

	/**
	 * List of available agents on the server.
	 */
	private List<? extends PlatformIdent> agentList;

	/**
	 * Agent selection table.
	 */
	private Table agentSelection;

	/**
	 * Main composite.
	 */
	private Composite main;

	/**
	 * If any agent should be used.
	 */
	private Button allAgents;

	/**
	 * If specific agents should be used.
	 */
	private Button specificAgents;

	/**
	 * Cmr to get Agents from.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Default constructor.
	 */
	public SelectAgentsWizardPage() {
		this(DEFAULT_MESSAGE);
	}

	/**
	 * This constructor sets the wizard page message.
	 * 
	 * @param message
	 *            Wizard page message.
	 */
	public SelectAgentsWizardPage(String message) {
		super("Select Agent(s)");
		this.setTitle("Select Agent(s)");
		this.setMessage(message);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		main = new Composite(parent, SWT.NONE);
		setControl(main);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		if (null != allAgents && !allAgents.isDisposed() && allAgents.getSelection()) {
			return true;
		} else {
			boolean agentSelected = false;
			if (null != agentSelection && !agentSelection.isDisposed()) {
				for (TableItem tableItem : agentSelection.getItems()) {
					if (tableItem.getChecked()) {
						agentSelected = true;
						break;
					}
				}
			}
			if (!agentSelected) {
				return false;
			}
			return true;
		}
	}

	/**
	 * Returns if all agents should be used.
	 * 
	 * @return Returns if all agents should be used.
	 */
	public boolean isAllAgents() {
		return allAgents.getSelection();
	}

	/**
	 * @return Returns list of Agent IDs to be involved in copy to buffer request.
	 */
	public List<Long> getSelectedAgents() {
		if (allAgents.getSelection()) {
			List<Long> returnList = new ArrayList<Long>();
			for (PlatformIdent agent : agentList) {
				returnList.add(agent.getId());
			}
			return returnList;
		} else {
			int index = 0;
			List<Long> returnList = new ArrayList<Long>();
			for (TableItem tableItem : agentSelection.getItems()) {
				if (tableItem.getChecked()) {
					returnList.add(agentList.get(index).getId());
				}
				index++;
			}
			return returnList;
		}
	}

	/**
	 * Sets the repository. Needed to be called before the page is displayed to the user.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 */
	public void setCmrRepositoryDefinition(final CmrRepositoryDefinition cmrRepositoryDefinition) {
		if (!ObjectUtils.equals(cmrRepositoryDefinition, this.cmrRepositoryDefinition)) {
			this.cmrRepositoryDefinition = cmrRepositoryDefinition;
			for (Control control : main.getChildren()) {
				control.dispose();
			}

			if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
				agentList = cmrRepositoryDefinition.getGlobalDataAccessService().getConnectedAgents();
				main.setLayout(new GridLayout(1, false));

				allAgents = new Button(main, SWT.RADIO);
				allAgents.setText("All agent(s)");
				allAgents.setSelection(true);

				specificAgents = new Button(main, SWT.RADIO);
				specificAgents.setText("Select specific Agent(s)");

				agentSelection = new Table(main, SWT.CHECK | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
				for (PlatformIdent platformIdent : agentList) {
					new TableItem(agentSelection, SWT.NONE).setText(platformIdent.getAgentName() + " [v. " + platformIdent.getVersion() + "]");
				}
				agentSelection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				agentSelection.setEnabled(false);

				Listener pageCompletedListener = new Listener() {

					@Override
					public void handleEvent(Event event) {
						setPageComplete(isPageComplete());
					}
				};
				agentSelection.addListener(SWT.Selection, pageCompletedListener);
				allAgents.addListener(SWT.Selection, pageCompletedListener);
				specificAgents.addListener(SWT.Selection, pageCompletedListener);

				Listener agentsSelectionListener = new Listener() {

					@Override
					public void handleEvent(Event event) {
						if (allAgents.getSelection()) {
							agentSelection.setEnabled(false);
						} else {
							agentSelection.setEnabled(true);
						}
					}
				};
				allAgents.addListener(SWT.Selection, agentsSelectionListener);
				specificAgents.addListener(SWT.Selection, agentsSelectionListener);

			} else {
				main.setLayout(new GridLayout(2, false));

				new Label(main, SWT.NONE).setImage(Display.getDefault().getSystemImage(SWT.ERROR));
				Label text = new Label(main, SWT.WRAP);
				text.setText("Selected repository is currently offline. Action can not be performed.");
			}
			main.layout();
		}
	}

}
