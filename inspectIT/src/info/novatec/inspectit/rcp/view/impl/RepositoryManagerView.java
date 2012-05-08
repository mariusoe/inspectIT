package info.novatec.inspectit.rcp.view.impl;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.form.CmrRepositoryPropertyForm;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.handlers.ShowRepositoryHandler;
import info.novatec.inspectit.rcp.model.AgentLeaf;
import info.novatec.inspectit.rcp.model.Component;
import info.novatec.inspectit.rcp.model.DeferredAgentsComposite;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryChangeListener;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.CmrRepositoryManager;
import info.novatec.inspectit.rcp.repository.CmrRepositoryManager.UpdateRepositoryJob;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.util.ObjectUtils;
import info.novatec.inspectit.rcp.view.IRefreshableView;
import info.novatec.inspectit.rcp.view.tree.TreeContentProvider;
import info.novatec.inspectit.rcp.view.tree.TreeLabelProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

/**
 * Repository manager view where user can work with repositories, check agents, and give input for
 * the data explorer view.
 * 
 * @author Ivan Senic
 * 
 */
public class RepositoryManagerView extends ViewPart implements IRefreshableView, CmrRepositoryChangeListener {

	/**
	 * ID of this view.
	 */
	public static final String VIEW_ID = "info.novatec.inspectit.rcp.view.repositoryManager";

	/**
	 * ID for tree menu.
	 */
	private static final String MENU_ID = "info.novatec.inspectit.rcp.view.repositoryManager.repositoryTree";

	/**
	 * {@link CmrRepositoryManager}.
	 */
	private CmrRepositoryManager cmrRepositoryManager;

	/**
	 * Input list.
	 */
	private List<DeferredAgentsComposite> inputList = new ArrayList<DeferredAgentsComposite>();

	/**
	 * Online statuses map.
	 */
	private Map<CmrRepositoryDefinition, OnlineStatus> cachedStatusMap = new ConcurrentHashMap<CmrRepositoryDefinition, OnlineStatus>();

	/**
	 * Toolkit.
	 */
	private FormToolkit toolkit;

	/**
	 * Form for the view.
	 */
	private Form mainForm;

	/**
	 * Tree Viewer.
	 */
	private TreeViewer treeViewer;

	/**
	 * Composite for displaying the messages.
	 */
	private Composite messageComposite;

	/**
	 * CMR property form.
	 */
	private CmrRepositoryPropertyForm cmrPropertyForm;

	/**
	 * Views main composite.
	 */
	private Composite mainComposite;

	/**
	 * Boolean for layout of view.
	 */
	private boolean verticaLayout = true;

	/**
	 * Last selected repository, so that the selection can be maintained after the view is
	 * refreshed.
	 */
	private DeferredAgentsComposite lastSelectedRepository = null;

	/**
	 * Default constructor.
	 */
	public RepositoryManagerView() {
		cmrRepositoryManager = InspectIT.getDefault().getCmrRepositoryManager();
		cmrRepositoryManager.addCmrRepositoryChangeListener(this);
		createInputList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent) {
		createViewToolbar();

		toolkit = new FormToolkit(parent.getDisplay());

		mainComposite = toolkit.createComposite(parent);
		GridLayout mainLayout = new GridLayout(1, true);
		mainLayout.marginWidth = 0;
		mainLayout.marginHeight = 0;
		mainComposite.setLayout(mainLayout);

		mainForm = toolkit.createForm(mainComposite);
		mainForm.getBody().setLayout(new GridLayout(1, true));
		mainForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		toolkit.decorateFormHeading(mainForm);

		Tree tree = toolkit.createTree(mainForm.getBody(), SWT.V_SCROLL | SWT.H_SCROLL);
		treeViewer = new TreeViewer(tree);
		treeViewer.setContentProvider(new TreeContentProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Object[]) {
					return (Object[]) inputElement;
				}
				if (inputElement instanceof Collection) {
					return ((Collection<Object>) inputElement).toArray();
				}
				return new Object[0];
			}
		});
		treeViewer.setLabelProvider(new TreeLabelProvider() {
			@Override
			public Image getImage(Object element) {
				if (element instanceof DeferredAgentsComposite) {
					CmrRepositoryDefinition cmrRepositoryDefinition = (CmrRepositoryDefinition) ((DeferredAgentsComposite) element).getRepositoryDefinition();
					return ImageFormatter.getCmrRepositoryImage(cmrRepositoryDefinition, true);
				}
				return super.getImage(element);
			}
		});
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection structuredSelection = (StructuredSelection) event.getSelection();
				if (structuredSelection.getFirstElement() instanceof DeferredAgentsComposite) {
					lastSelectedRepository = (DeferredAgentsComposite) structuredSelection.getFirstElement();
				}
			}
		});
		treeViewer.addDoubleClickListener(new RepositoryManagerDoubleClickListener());
		ColumnViewerToolTipSupport.enableFor(treeViewer, ToolTip.NO_RECREATE);
		treeViewer.setInput(inputList);

		cmrPropertyForm = new CmrRepositoryPropertyForm(mainComposite, toolkit);
		cmrPropertyForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		treeViewer.addSelectionChangedListener(cmrPropertyForm);

		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		getSite().registerContextMenu(MENU_ID, menuManager, treeViewer);
		Control control = treeViewer.getControl();
		Menu menu = menuManager.createContextMenu(control);
		control.setMenu(menu);

		mainComposite.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				int width = mainComposite.getBounds().width;
				int height = mainComposite.getBounds().height;

				GridLayout gd = null;
				if (width > height && verticaLayout) {
					verticaLayout = false;
					gd = (new GridLayout(2, true));
				} else if (width < height && !verticaLayout) {
					verticaLayout = true;
					gd = new GridLayout(1, true);
				}

				if (null != gd) {
					gd.marginHeight = 0;
					gd.marginWidth = 0;
					mainComposite.setLayout(gd);
					mainComposite.layout();
				}
			}
		});

		updateFormBody();

		getSite().setSelectionProvider(treeViewer);
	}

	/**
	 * Creates the view tool-bar.
	 */
	private void createViewToolbar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		toolBarManager.add(new ShowPropertiesAction());
		toolBarManager.add(new Separator());
	}

	/**
	 * Updates the repository map.
	 */
	private void createInputList() {
		inputList.clear();
		List<CmrRepositoryDefinition> repositories = cmrRepositoryManager.getCmrRepositoryDefinitions();
		for (CmrRepositoryDefinition cmrRepositoryDefinition : repositories) {
			inputList.add(new DeferredAgentsComposite(cmrRepositoryDefinition));
			OnlineStatus onlineStatus = cmrRepositoryDefinition.getOnlineStatus();
			cachedStatusMap.put(cmrRepositoryDefinition, onlineStatus);
		}
	}

	/**
	 * Updates body.
	 */
	private void updateFormBody() {
		clearFormBody();
		if (!inputList.isEmpty()) {
			treeViewer.getTree().setVisible(true);
			treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			treeViewer.setInput(inputList);
			treeViewer.expandAll();
			if (null != lastSelectedRepository && inputList.contains(lastSelectedRepository)) {
				StructuredSelection ss = new StructuredSelection(lastSelectedRepository);
				treeViewer.setSelection(ss, true);
			}
		} else {
			displayMessage("No CMR repositopry present. Please add the CMR repository via 'Add CMR repository' action.", Display.getDefault().getSystemImage(SWT.ICON_INFORMATION));
		}
		mainForm.getBody().layout();
	}

	/**
	 * Clears the look of the forms body.
	 */
	private void clearFormBody() {
		if (messageComposite != null && !messageComposite.isDisposed()) {
			messageComposite.dispose();
		}
		treeViewer.setInput(Collections.emptyList());
		treeViewer.getTree().setVisible(false);
		treeViewer.getTree().setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
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
	public void repositoryOnlineStatusUpdated(final CmrRepositoryDefinition repositoryDefinition, OnlineStatus oldStatus, OnlineStatus newStatus) {
		if (newStatus != OnlineStatus.CHECKING) {
			OnlineStatus cachedStatus = cachedStatusMap.get(repositoryDefinition);
			if (null != cachedStatus) {
				if (cachedStatus != newStatus) {
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							mainForm.setBusy(true);
							boolean update = false;
							for (DeferredAgentsComposite composite : inputList) {
								if (ObjectUtils.equals(composite.getRepositoryDefinition(), repositoryDefinition)) {
									treeViewer.refresh(composite, true);
									treeViewer.expandAll();
									if (ObjectUtils.equals(composite, lastSelectedRepository)) {
										if (null != lastSelectedRepository && inputList.contains(lastSelectedRepository)) {
											treeViewer.setSelection(StructuredSelection.EMPTY);
											StructuredSelection ss = new StructuredSelection(lastSelectedRepository);
											treeViewer.setSelection(ss, true);
											update = true;
										}
									}
								}
							}
							mainForm.setBusy(false);
							if (update) {
								cmrPropertyForm.refresh();
							}
						}
					});
				}
			}
			cachedStatusMap.put(repositoryDefinition, newStatus);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryDataUpdated(final CmrRepositoryDefinition cmrRepositoryDefinition) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				mainForm.setBusy(true);
				for (DeferredAgentsComposite composite : inputList) {
					if (ObjectUtils.equals(composite.getRepositoryDefinition(), cmrRepositoryDefinition)) {
						treeViewer.refresh(composite);
						break;
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
	public void repositoryAdded(CmrRepositoryDefinition cmrRepositoryDefinition) {
		final DeferredAgentsComposite newComposite = new DeferredAgentsComposite(cmrRepositoryDefinition);
		inputList.add(newComposite);
		OnlineStatus onlineStatus = cmrRepositoryDefinition.getOnlineStatus();
		cachedStatusMap.put(cmrRepositoryDefinition, onlineStatus);

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				mainForm.setBusy(true);
				if (inputList.size() > 1) {
					treeViewer.refresh();
					if (null != lastSelectedRepository && inputList.contains(lastSelectedRepository)) {
						StructuredSelection ss = new StructuredSelection(lastSelectedRepository);
						treeViewer.setSelection(ss, true);
					}
				}
				else {
					updateFormBody();
				}
				mainForm.setBusy(false);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryRemoved(CmrRepositoryDefinition cmrRepositoryDefinition) {
		DeferredAgentsComposite toRemove = null;
		for (DeferredAgentsComposite composite : inputList) {
			if (ObjectUtils.equals(composite.getRepositoryDefinition(), cmrRepositoryDefinition)) {
				toRemove = composite;
				break;
			}
		}
		if (null != toRemove) {
			inputList.remove(toRemove);
		}
		cachedStatusMap.remove(cmrRepositoryDefinition);

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (inputList.isEmpty()) {
					updateFormBody();
				} else {
					treeViewer.refresh();
				}
			}
		});
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Refreshes all repositories.
	 */
	public void refresh() {
		Collection<UpdateRepositoryJob> jobs = cmrRepositoryManager.forceAllCmrRepositoriesOnlineStatusUpdate();
		for (final UpdateRepositoryJob updateRepositoryJob : jobs) {
			updateRepositoryJob.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					CmrRepositoryDefinition cmrRepositoryDefinition = updateRepositoryJob.getCmrRepositoryDefinition();
					DeferredAgentsComposite toUpdate = null;
					for (DeferredAgentsComposite composite : inputList) {
						if (ObjectUtils.equals(composite.getRepositoryDefinition(), cmrRepositoryDefinition)) {
							toUpdate = composite;
							break;
						}
					}
					if (null != toUpdate) {
						final DeferredAgentsComposite finalToUpdate = toUpdate;
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								treeViewer.refresh(finalToUpdate, true);
								if (ObjectUtils.equals(finalToUpdate, lastSelectedRepository)) {
									treeViewer.setSelection(StructuredSelection.EMPTY);
									StructuredSelection ss = new StructuredSelection(finalToUpdate);
									treeViewer.setSelection(ss, true);
									cmrPropertyForm.refresh();
								}
							}
						});
					}
					updateRepositoryJob.removeJobChangeListener(this);
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canRefresh() {
		return !inputList.isEmpty() || !cmrRepositoryManager.getCmrRepositoryDefinitions().isEmpty();
	}

	/**
	 * Show or hides properties.
	 * 
	 * @param show
	 *            Should properties be shown.
	 */
	public void setShowProperties(boolean show) {
		if (show) {
			CmrRepositoryDefinition cmrRepositoryDefinition = null;
			StructuredSelection selection = (StructuredSelection) treeViewer.getSelection();
			if (!selection.isEmpty()) {
				if (selection.getFirstElement() instanceof ICmrRepositoryProvider) {
					cmrRepositoryDefinition = ((ICmrRepositoryProvider) selection.getFirstElement()).getCmrRepositoryDefinition();
				}
			}

			cmrPropertyForm = new CmrRepositoryPropertyForm(mainComposite, toolkit, cmrRepositoryDefinition);
			cmrPropertyForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			treeViewer.addSelectionChangedListener(cmrPropertyForm);
			mainComposite.layout();
		} else {
			if (null != cmrPropertyForm && !cmrPropertyForm.isDisposed()) {
				treeViewer.removeSelectionChangedListener(cmrPropertyForm);
				cmrPropertyForm.dispose();
				cmrPropertyForm = null;
			}
			mainComposite.layout();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		cmrRepositoryManager.removeCmrRepositoryChangeListener(this);
		super.dispose();
	}

	/**
	 * Action for show hide properties.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class ShowPropertiesAction extends Action {

		/**
		 * Default constructor.
		 */
		public ShowPropertiesAction() {
			super(null, AS_CHECK_BOX);
			setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_PROPERTIES));
			setChecked(true);
			setToolTipText("Hide Properties");
		}

		/**
		 * {@inheritDoc}
		 */
		public void run() {
			if (isChecked()) {
				setShowProperties(true);
				setToolTipText("Hide Properties");
			} else {
				setShowProperties(false);
				setToolTipText("Show Properties");
			}
		};
	}

	/**
	 * Double click listener for the view.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class RepositoryManagerDoubleClickListener implements IDoubleClickListener {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void doubleClick(final DoubleClickEvent event) {
			UIJob openDataExplorerJob = new UIJob("Opening Data Explorer..") {

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					process();
					return Status.OK_STATUS;
				}
			};
			openDataExplorerJob.setUser(true);
			openDataExplorerJob.schedule();
		}

		/**
		 * Processes the double-click.
		 * 
		 */
		private void process() {
			RepositoryDefinition repositoryDefinition = null;
			PlatformIdent platformIdent = null;

			StructuredSelection selection = (StructuredSelection) treeViewer.getSelection();
			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof DeferredAgentsComposite) {
				repositoryDefinition = ((DeferredAgentsComposite) firstElement).getRepositoryDefinition();
			} else if (firstElement instanceof AgentLeaf) {
				platformIdent = ((AgentLeaf) firstElement).getPlatformIdent();
				Component parent = ((AgentLeaf) firstElement).getParent();
				while (null != parent) {
					if (parent instanceof DeferredAgentsComposite) {
						repositoryDefinition = ((DeferredAgentsComposite) parent).getRepositoryDefinition();
						break;
					}
					parent = parent.getParent();
				}
			}

			if (null != repositoryDefinition) {
				IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
				ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

				Command command = commandService.getCommand(ShowRepositoryHandler.COMMAND);
				ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, new Event());
				IEvaluationContext context = (IEvaluationContext) executionEvent.getApplicationContext();
				context.addVariable(ShowRepositoryHandler.REPOSITORY_DEFINITION, repositoryDefinition);
				if (null != platformIdent) {
					context.addVariable(ShowRepositoryHandler.AGENT, platformIdent);
				}

				try {
					command.executeWithChecks(executionEvent);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

}
