package info.novatec.inspectit.rcp.view.impl;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.tree.DeferredTreeViewer;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.model.TreeModelManager;
import info.novatec.inspectit.rcp.repository.CmrRepositoryChangeListener;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.CmrRepositoryManager.UpdateRepositoryJob;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinition;
import info.novatec.inspectit.rcp.storage.listener.StorageChangeListener;
import info.novatec.inspectit.rcp.util.ObjectUtils;
import info.novatec.inspectit.rcp.util.SelectionProviderAdapter;
import info.novatec.inspectit.rcp.view.IRefreshableView;
import info.novatec.inspectit.rcp.view.listener.TreeViewDoubleClickListener;
import info.novatec.inspectit.rcp.view.tree.TreeContentProvider;
import info.novatec.inspectit.rcp.view.tree.TreeLabelProvider;
import info.novatec.inspectit.rcp.view.tree.TreeViewerComparator;
import info.novatec.inspectit.storage.IStorageData;
import info.novatec.inspectit.storage.LocalStorageData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

/**
 * Data explorer view show one Agent from a given {@link RepositoryDefinition}. Other agents can be
 * selected via view menu.
 * 
 * @author Ivan Senic
 * 
 */
public class DataExplorerView extends ViewPart implements CmrRepositoryChangeListener, StorageChangeListener, IRefreshableView {

	/**
	 * ID of the refresh contribution item needed for setting the visibility.
	 */
	private static final String REFRESH_CONTRIBUTION_ITEM = "info.novatec.inspectit.rcp.view.dataExplorer.refresh";

	/**
	 * ID of the refresh contribution item needed for setting the visibility.
	 */
	private static final String CLEAR_BUFFER_CONTRIBUTION_ITEM = "info.novatec.inspectit.rcp.view.dataExplorer.clearBuffer";

	/**
	 * ID of this view.
	 */
	public static final String VIEW_ID = "info.novatec.inspectit.rcp.view.dataExplorer";

	/**
	 * Displayed repository.
	 */
	private RepositoryDefinition displayedRepositoryDefinition;

	/**
	 * Displayed agent.
	 */
	private PlatformIdent displayedAgent;

	/**
	 * Available agents for displaying.
	 */
	private List<? extends PlatformIdent> availableAgents;

	/**
	 * Cashed statuses of CMR repository definitions.
	 */
	private ConcurrentHashMap<CmrRepositoryDefinition, OnlineStatus> cachedOnlineStatus = new ConcurrentHashMap<CmrRepositoryDefinition, OnlineStatus>();

	/**
	 * Listener for tree double clicks.
	 */
	private final TreeViewDoubleClickListener treeViewDoubleClickListener = new TreeViewDoubleClickListener();

	/**
	 * Toolkit used for the view components.
	 */
	private FormToolkit toolkit;

	/**
	 * Main form for display of the repository.
	 */
	private Form mainForm;

	/**
	 * Tree in the form for the agents representation.
	 */
	private DeferredTreeViewer treeViewer;

	/**
	 * Composite used for message displaying.
	 */
	private Composite messageComposite;

	/**
	 * Collapse action.
	 */
	private CollapseAction collapseAction;

	/**
	 * Adapter to publlich the selection to the Site.
	 */
	private SelectionProviderAdapter selectionProviderAdapter = new SelectionProviderAdapter();

	/**
	 * Combo where agents are displayed.
	 */
	private Combo agentsCombo;

	/**
	 * Toolbar manager for the view.
	 */
	private IToolBarManager toolBarManager;

	/**
	 * Map of the cached expanded objects in the agent tree per agent.
	 */
	private Map<PlatformIdent, List<Object>> expandedElementsPerAgent = new ConcurrentHashMap<PlatformIdent, List<Object>>();

	/**
	 * Default constructor.
	 */
	public DataExplorerView() {
		InspectIT.getDefault().getCmrRepositoryManager().addCmrRepositoryChangeListener(this);
		InspectIT.getDefault().getInspectITStorageManager().addStorageChangeListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent) {
		createViewToolbar();

		toolkit = new FormToolkit(parent.getDisplay());
		mainForm = toolkit.createForm(parent);
		mainForm.getBody().setLayout(new GridLayout(1, true));
		createHeadClient();
		toolkit.decorateFormHeading(mainForm);

		Tree tree = toolkit.createTree(mainForm.getBody(), SWT.V_SCROLL | SWT.H_SCROLL);
		treeViewer = new DeferredTreeViewer(tree);
		treeViewer.setContentProvider(new TreeContentProvider());
		treeViewer.setLabelProvider(new TreeLabelProvider());
		treeViewer.setComparator(new TreeViewerComparator());
		treeViewer.addDoubleClickListener(treeViewDoubleClickListener);
		ColumnViewerToolTipSupport.enableFor(treeViewer, ToolTip.NO_RECREATE);

		updateFormTitle();
		updateFormBody();
		updateAgentsCombo();

		getSite().setSelectionProvider(selectionProviderAdapter);
	}

	/**
	 * Show the given repository on the view. If the selected agent is not provided, the arbitrary
	 * agent will be shown.
	 * 
	 * @param repositoryDefinition
	 *            Repository definition to display.
	 * @param agent
	 *            Agent to select. Can be null. If the repository does not
	 */
	public void showRepository(final RepositoryDefinition repositoryDefinition, final PlatformIdent agent) {
		displayedRepositoryDefinition = repositoryDefinition;
		updateAvailableAgents(repositoryDefinition);
		selectAgentForDisplay(agent);

		StructuredSelection ss = new StructuredSelection(repositoryDefinition);
		selectionProviderAdapter.setSelection(ss);

		performUpdate();
	}

	/**
	 * Selects the provided agent for display, if it is in the {@link #availableAgents} list. If
	 * not, a arbitrary agent will be selected if any is available.
	 * 
	 * @param agent
	 *            Hint for agent selection.
	 */
	private void selectAgentForDisplay(PlatformIdent agent) {
		if (null != displayedAgent) {
			cacheExpandedObjects(displayedAgent);
		}
		if (null != agent && null != availableAgents && availableAgents.contains(agent)) {
			displayedAgent = agent;
		} else if (null != availableAgents && !availableAgents.isEmpty()) {
			displayedAgent = availableAgents.iterator().next();
		} else {
			displayedAgent = null;
		}
	}

	/**
	 * Caches the current expanded objects in the tree viewer with the given platform ident. Note
	 * that this method will filter out the elements given by
	 * {@link org.eclipse.jface.viewers.TreeViewer#getExpandedElements()}, so that only the last
	 * expanded element in the tree is saved.
	 * 
	 * @param platformIdent
	 *            {@link PlatformIdent} to cache elements for.
	 */
	private void cacheExpandedObjects(final PlatformIdent platformIdent) {
		Object[] allExpanded = treeViewer.getExpandedElements();
		if (allExpanded.length > 0) {
			Set<Object> parents = new HashSet<Object>();
			for (Object expanded : allExpanded) {
				Object parent = ((ITreeContentProvider) treeViewer.getContentProvider()).getParent(expanded);
				while (parent != null) {
					parents.add(parent);
					parent = ((ITreeContentProvider) treeViewer.getContentProvider()).getParent(parent);
				}
			}
			List<Object> expandedList = new ArrayList<Object>(Arrays.asList(allExpanded));
			expandedList.removeAll(parents);
			expandedElementsPerAgent.put(platformIdent, expandedList);
		} else {
			expandedElementsPerAgent.put(platformIdent, Collections.emptyList());
		}
	}

	/**
	 * Updates the list of available agents.
	 * 
	 * @param repositoryDefinition
	 *            {@link RepositoryDefinition}.
	 */
	private void updateAvailableAgents(RepositoryDefinition repositoryDefinition) {
		if (repositoryDefinition instanceof CmrRepositoryDefinition) {
			CmrRepositoryDefinition cmrRepositoryDefinition = (CmrRepositoryDefinition) repositoryDefinition;
			if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
				availableAgents = cmrRepositoryDefinition.getGlobalDataAccessService().getConnectedAgents();
			} else {
				availableAgents = null;
			}
		} else if (repositoryDefinition instanceof StorageRepositoryDefinition) {
			StorageRepositoryDefinition storageRepositoryDefinition = (StorageRepositoryDefinition) repositoryDefinition;
			if (storageRepositoryDefinition.getLocalStorageData().isFullyDownloaded() || storageRepositoryDefinition.getCmrRepositoryDefinition().getOnlineStatus() != OnlineStatus.OFFLINE) {
				availableAgents = storageRepositoryDefinition.getGlobalDataAccessService().getConnectedAgents();
			} else {
				availableAgents = null;
			}
		} else {
			availableAgents = null;
		}
	}

	/**
	 * Creates view toolbar.
	 */
	private void createViewToolbar() {
		toolBarManager = getViewSite().getActionBars().getToolBarManager();
		collapseAction = new CollapseAction();
		toolBarManager.add(collapseAction);
	}

	/**
	 * Creates the head client that holds the agents in combo box.
	 */
	private void createHeadClient() {
		Composite headClient = new Composite(mainForm.getHead(), SWT.NONE);
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		headClient.setLayout(gl);

		Label agentImg = new Label(headClient, SWT.NONE);
		agentImg.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_AGENT));

		agentsCombo = new Combo(headClient, SWT.READ_ONLY | SWT.BORDER | SWT.DROP_DOWN);
		agentsCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		agentsCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selected = agentsCombo.getSelectionIndex();
				if (selected < availableAgents.size()) {
					PlatformIdent platformIdent = availableAgents.get(selected);
					if (!ObjectUtils.equals(displayedAgent, platformIdent)) {
						selectAgentForDisplay(platformIdent);
						performUpdate();
					}
				}
			}
		});

		mainForm.setHeadClient(headClient);
	}

	/**
	 * Updates the combo menu with agents.
	 */
	private void updateAgentsCombo() {
		agentsCombo.removeAll();
		if (null != availableAgents && !availableAgents.isEmpty()) {
			agentsCombo.setEnabled(true);
			int i = 0;
			int selectedIndex = -1;
			for (PlatformIdent platformIdent : availableAgents) {
				agentsCombo.add(TextFormatter.getAgentDescription(platformIdent));
				if (ObjectUtils.equals(platformIdent, displayedAgent)) {
					selectedIndex = i;
				}
				i++;
			}
			if (-1 != selectedIndex) {
				agentsCombo.select(selectedIndex);
			}
		} else {
			agentsCombo.setEnabled(false);
		}
		mainForm.getHead().layout();
	}

	/**
	 * Updates the form title.
	 */
	private void updateFormTitle() {
		if (null != displayedRepositoryDefinition) {
			if (displayedRepositoryDefinition instanceof CmrRepositoryDefinition) {
				CmrRepositoryDefinition cmrRepositoryDefinition = (CmrRepositoryDefinition) displayedRepositoryDefinition;
				mainForm.setImage(ImageFormatter.getCmrRepositoryImage(cmrRepositoryDefinition, true));
				mainForm.setText(cmrRepositoryDefinition.getName());
				mainForm.setToolTipText(getCmrRepositoryDescription(cmrRepositoryDefinition));
			} else if (displayedRepositoryDefinition instanceof StorageRepositoryDefinition) {
				StorageRepositoryDefinition storageRepositoryDefinition = (StorageRepositoryDefinition) displayedRepositoryDefinition;
				mainForm.setImage(ImageFormatter.getStorageRepositoryImage(storageRepositoryDefinition));
				mainForm.setText(storageRepositoryDefinition.getName());
				mainForm.setToolTipText(getStorageDescirption(storageRepositoryDefinition));
			}
			mainForm.setMessage(null);
		} else {
			mainForm.setImage(null);
			mainForm.setText("No repository loaded");
			mainForm.setMessage("Repositories can be loaded from Repository or Storage Manager", IMessageProvider.WARNING);
			mainForm.setToolTipText(null);
		}
	}

	/**
	 * Updates the tree input and refreshes the tree.
	 */

	private void updateFormBody() {
		clearFormBody();
		if (null != displayedRepositoryDefinition && null != displayedAgent) {
			TreeModelManager treeModelManager = null;
			treeModelManager = new TreeModelManager(displayedRepositoryDefinition, displayedAgent);
			if (null != treeModelManager && null != displayedAgent) {
				treeViewer.setInput(treeModelManager);
				treeViewer.getTree().setVisible(true);
				treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			} else {
				displayMessage("Repository is currently unavailable.", Display.getDefault().getSystemImage(SWT.ICON_ERROR));
			}
		} else if (null != displayedRepositoryDefinition && null == displayedAgent) {
			if (null == availableAgents) {
				displayMessage("No agent could be loaded on selected repository.", Display.getDefault().getSystemImage(SWT.ICON_WARNING));
			} else {
				displayMessage("This repository is empty.", Display.getDefault().getSystemImage(SWT.ICON_INFORMATION));
			}
		}

		mainForm.getBody().layout();
	}

	/**
	 * Updates view tool-bar.
	 */
	private void updateViewToolbar() {
		collapseAction.updateEnabledState();
		toolBarManager.find(REFRESH_CONTRIBUTION_ITEM).setVisible(displayedRepositoryDefinition instanceof CmrRepositoryDefinition);
		toolBarManager.find(CLEAR_BUFFER_CONTRIBUTION_ITEM).setVisible(
				displayedRepositoryDefinition instanceof CmrRepositoryDefinition && !OnlineStatus.OFFLINE.equals(((CmrRepositoryDefinition) displayedRepositoryDefinition).getOnlineStatus()));
		toolBarManager.update(true);
	}

	/**
	 * Clears the look of the form.
	 */
	private void clearFormBody() {
		if (messageComposite != null && !messageComposite.isDisposed()) {
			messageComposite.dispose();
		}
		treeViewer.setInput(null);
		treeViewer.getTree().setVisible(false);
		treeViewer.getTree().setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
	}

	/**
	 * Updates the form.
	 */
	public void performUpdate() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				mainForm.setBusy(true);
				updateFormTitle();
				updateFormBody();
				updateAgentsCombo();
				updateViewToolbar();
				if (null != displayedAgent) {
					List<Object> expandedObjects = expandedElementsPerAgent.get(displayedAgent);
					if (null != expandedObjects) {
						for (Object object : expandedObjects) {
							treeViewer.expandObject(object, 1);
						}
					}
				}
				mainForm.setBusy(false);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFocus() {
		treeViewer.getTree().setFocus();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refresh() {
		if (displayedRepositoryDefinition instanceof CmrRepositoryDefinition) {
			if (null != displayedAgent) {
				cacheExpandedObjects(displayedAgent);
			}
			final UpdateRepositoryJob job = InspectIT.getDefault().getCmrRepositoryManager().forceCmrRepositoryOnlineStatusUpdate((CmrRepositoryDefinition) displayedRepositoryDefinition);
			job.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					updateAvailableAgents(displayedRepositoryDefinition);
					if (null != availableAgents && !availableAgents.isEmpty() && null != displayedAgent) {
						boolean found = false;
						for (PlatformIdent platformIdent : availableAgents) {
							if (platformIdent.getId().longValue() == displayedAgent.getId()) {
								displayedAgent = platformIdent;
								found = true;
								break;
							}
						}
						if (!found) {
							displayedAgent = availableAgents.get(0);
						}
					} else if (null != availableAgents && !availableAgents.isEmpty() && null == displayedAgent) {
						displayedAgent = availableAgents.get(0);
					} else {
						displayedAgent = null;
					}
					performUpdate();
					job.removeJobChangeListener(this);
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canRefresh() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryOnlineStatusUpdated(CmrRepositoryDefinition repositoryDefinition, OnlineStatus oldStatus, OnlineStatus newStatus) {
		if (newStatus != OnlineStatus.CHECKING) {
			if (ObjectUtils.equals(displayedRepositoryDefinition, repositoryDefinition)) {
				OnlineStatus cachedStatus = cachedOnlineStatus.get(repositoryDefinition);
				if (cachedStatus == OnlineStatus.OFFLINE && newStatus == OnlineStatus.ONLINE) {
					updateAvailableAgents(displayedRepositoryDefinition);
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							mainForm.setBusy(true);
							updateFormTitle();
							updateFormBody();
							updateAgentsCombo();
							updateViewToolbar();
							mainForm.setBusy(false);
						}
					});
				} else if (cachedStatus == OnlineStatus.ONLINE && newStatus == OnlineStatus.OFFLINE) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							mainForm.setBusy(true);
							updateFormTitle();
							updateAgentsCombo();
							updateViewToolbar();
							mainForm.setBusy(false);
						}
					});
				}
			}
			cachedOnlineStatus.put(repositoryDefinition, newStatus);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryDataUpdated(CmrRepositoryDefinition cmrRepositoryDefinition) {
		if (ObjectUtils.equals(cmrRepositoryDefinition, displayedRepositoryDefinition)) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					mainForm.setBusy(true);
					updateFormTitle();
					mainForm.setBusy(false);
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryAdded(CmrRepositoryDefinition cmrRepositoryDefinition) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryRemoved(CmrRepositoryDefinition cmrRepositoryDefinition) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void storageDataUpdated(IStorageData storageData) {
		if (displayedRepositoryDefinition instanceof StorageRepositoryDefinition) {
			final StorageRepositoryDefinition storageRepositoryDefinition = (StorageRepositoryDefinition) displayedRepositoryDefinition;
			if (ObjectUtils.equals(storageData.getId(), storageRepositoryDefinition.getLocalStorageData().getId())) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						updateFormTitle();
					}
				});
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void storageRemotelyDeleted(IStorageData storageData) {
		if (displayedRepositoryDefinition instanceof StorageRepositoryDefinition) {
			final StorageRepositoryDefinition storageRepositoryDefinition = (StorageRepositoryDefinition) displayedRepositoryDefinition;
			if (!storageRepositoryDefinition.getLocalStorageData().isFullyDownloaded() && ObjectUtils.equals(storageData.getId(), storageRepositoryDefinition.getLocalStorageData().getId())) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						clearFormBody();
						agentsCombo.removeAll();
						agentsCombo.setEnabled(false);
						displayMessage("Selected storage was remotely deleted and is not available anymore.", Display.getDefault().getSystemImage(SWT.ICON_WARNING));
					}
				});
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void storageLocallyDeleted(IStorageData storageData) {
		if (displayedRepositoryDefinition instanceof StorageRepositoryDefinition) {
			final StorageRepositoryDefinition storageRepositoryDefinition = (StorageRepositoryDefinition) displayedRepositoryDefinition;
			if (ObjectUtils.equals(storageData.getId(), storageRepositoryDefinition.getLocalStorageData().getId())) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						clearFormBody();
						agentsCombo.removeAll();
						agentsCombo.setEnabled(false);
						displayMessage("Selected storage was locally deleted and is not available anymore.", Display.getDefault().getSystemImage(SWT.ICON_WARNING));
					}
				});
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		InspectIT.getDefault().getCmrRepositoryManager().removeCmrRepositoryChangeListener(this);
		InspectIT.getDefault().getInspectITStorageManager().removeStorageChangeListener(this);
		super.dispose();
	}

	/**
	 * Displays the message on the provided composite.
	 * 
	 * @param text
	 *            Text of message.
	 * @param image
	 *            Image to show.
	 */
	private void displayMessage(String text, Image image) {
		if (null == messageComposite || messageComposite.isDisposed()) {
			messageComposite = toolkit.createComposite(mainForm.getBody());
		} else {
			for (Control c : messageComposite.getChildren()) {
				if (!c.isDisposed()) {
					c.dispose();
				}
			}
		}
		messageComposite.setLayout(new GridLayout(2, false));
		messageComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		toolkit.createLabel(messageComposite, null).setImage(image);
		toolkit.createLabel(messageComposite, text, SWT.WRAP).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
	}

	/**
	 * Returns storage description for title box.
	 * 
	 * @param storageRepositoryDefinition
	 *            {@link StorageRepositoryDefinition}
	 * @return Description for title box.
	 */
	private String getStorageDescirption(StorageRepositoryDefinition storageRepositoryDefinition) {
		LocalStorageData localStorageData = storageRepositoryDefinition.getLocalStorageData();
		if (localStorageData.isFullyDownloaded()) {
			return "Storage Repositry - Accessible offline";
		} else {
			return "Storage Repositry - Accessible via CMR repository";
		}
	}

	/**
	 * Description of the {@link CmrRepositoryDefinition}.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @return Description in form http://ip:port
	 */
	private String getCmrRepositoryDescription(CmrRepositoryDefinition cmrRepositoryDefinition) {
		return "Central Management Repository @ http://" + cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort();
	}

	/**
	 * Action that collapses all agents.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class CollapseAction extends Action {

		/**
		 * Default constructor.
		 */
		public CollapseAction() {
			super();
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_COLLAPSE));
			setToolTipText("Collapse All");
			updateEnabledState();
		}

		/**
		 * Updates the enabled state of action based on the currently selected
		 * {@link CmrRepositoryDefinition}.
		 */
		public final void updateEnabledState() {
			if (null != treeViewer && treeViewer.getInput() != null) {
				setEnabled(true);
			} else {
				setEnabled(false);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			treeViewer.setExpandedElements(new Object[0]);
			treeViewer.refresh();
		}

	}

}
