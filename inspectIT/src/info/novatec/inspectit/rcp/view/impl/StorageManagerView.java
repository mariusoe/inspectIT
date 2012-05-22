package info.novatec.inspectit.rcp.view.impl;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.action.MenuAction;
import info.novatec.inspectit.rcp.filter.FilterComposite;
import info.novatec.inspectit.rcp.form.StorageDataPropertyForm;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.handlers.CloseAndShowStorageHandler;
import info.novatec.inspectit.rcp.handlers.ShowRepositoryHandler;
import info.novatec.inspectit.rcp.model.Component;
import info.novatec.inspectit.rcp.model.StorageLeaf;
import info.novatec.inspectit.rcp.model.StorageManagerTreeModelManager;
import info.novatec.inspectit.rcp.provider.IStorageDataProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryChangeListener;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.CmrRepositoryManager;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.storage.InspectITStorageManager;
import info.novatec.inspectit.rcp.util.ObjectUtils;
import info.novatec.inspectit.rcp.view.IRefreshableView;
import info.novatec.inspectit.rcp.view.tree.StorageManagerTreeContentProvider;
import info.novatec.inspectit.rcp.view.tree.StorageManagerTreeLabelProvider;
import info.novatec.inspectit.storage.LocalStorageData;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageData.StorageState;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
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
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

/**
 * 
 * @author Ivan Senic
 * 
 */
public class StorageManagerView extends ViewPart implements CmrRepositoryChangeListener, IRefreshableView {
	/**
	 * View id.
	 */
	public static final String VIEW_ID = "info.novatec.inspectit.rcp.view.storageManager";

	/**
	 * Menu id.
	 */
	public static final String MENU_ID = "info.novatec.inspectit.rcp.view.storageManager.storageTree";

	/**
	 * {@link CmrRepositoryManager}.
	 */
	private CmrRepositoryManager cmrRepositoryManager;

	/**
	 * Map of storages and their repositories.
	 */
	private Map<StorageData, CmrRepositoryDefinition> storageRespositoryMap = new ConcurrentHashMap<StorageData, CmrRepositoryDefinition>();

	/**
	 * Cashed statuses of CMR repository definitions.
	 */
	private ConcurrentHashMap<CmrRepositoryDefinition, OnlineStatus> cachedOnlineStatus = new ConcurrentHashMap<CmrRepositoryDefinition, OnlineStatus>();

	/**
	 * Toolkit for decorations.
	 */
	private FormToolkit toolkit;

	/**
	 * Main form.
	 */
	private Form mainForm;

	/**
	 * Tree Viewer.
	 */
	private TreeViewer treeViewer;

	/**
	 * Filter for the tree.
	 */
	private TreeFilter treeFilter = new TreeFilter();

	/**
	 * Composite for message displaying.
	 */
	private Composite cmrMessageComposite;

	/**
	 * Label type that storages are ordered by.
	 */
	private AbstractStorageLabelType<?> orderingLabelType = null;

	/**
	 * Menu manager for filter repositories actions. Needed because it must be updated when the
	 * storages are added/removed.
	 */
	private MenuManager filterByRepositoryMenu;

	/**
	 * Menu manager for grouping storage by label. Needed because it must be updated when the
	 * storages are added/removed.
	 */
	private MenuManager groupByLabelMenu;

	/**
	 * Storage property form.
	 */
	private StorageDataPropertyForm storagePropertyForm;

	/**
	 * Last selected leaf.
	 */
	private StorageLeaf lastSelectedLeaf = null;

	/**
	 * Boolean for layout of view.
	 */
	private boolean verticaLayout = true;

	/**
	 * Views main composite.
	 */
	private Composite mainComposite;

	/**
	 * Upper composite where filter box and storage tree is located.
	 */
	private Composite upperComposite;

	/**
	 * Filter storages composite that will be displayed at top of view.
	 */
	private FilterStorageComposite filterStorageComposite;

	/**
	 * Default constructor.
	 */
	public StorageManagerView() {
		cmrRepositoryManager = InspectIT.getDefault().getCmrRepositoryManager();
		cmrRepositoryManager.addCmrRepositoryChangeListener(this);
		updateStorageList();
	}

	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		createViewToolbar();

		mainComposite = toolkit.createComposite(parent);
		GridLayout mainLayout = new GridLayout(1, true);
		mainLayout.marginWidth = 0;
		mainLayout.marginHeight = 0;
		mainComposite.setLayout(mainLayout);

		upperComposite = toolkit.createComposite(mainComposite);
		GridLayout upperLayout = new GridLayout(1, true);
		upperLayout.marginWidth = 0;
		upperLayout.marginHeight = 0;
		upperComposite.setLayout(upperLayout);
		upperComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// filter composite
		filterStorageComposite = new FilterStorageComposite(upperComposite, SWT.NONE);
		filterStorageComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		mainForm = toolkit.createForm(upperComposite);
		mainForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainForm.getBody().setLayout(new GridLayout(1, true));

		Tree tree = toolkit.createTree(mainForm.getBody(), SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
		treeViewer = new TreeViewer(tree);
		treeViewer.setContentProvider(new StorageManagerTreeContentProvider());
		treeViewer.setLabelProvider(new StorageManagerTreeLabelProvider());
		// treeViewer.setComparator(new ServerViewComparator());
		treeViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof Component && e2 instanceof Component) {
					return ((Component) e1).getName().compareTo(((Component) e2).getName());
				}
				return super.compare(viewer, e1, e2);
			}
		});
		treeViewer.addFilter(treeFilter);
		treeViewer.addFilter(filterStorageComposite.getFilter());
		treeViewer.getTree().setVisible(false);
		ColumnViewerToolTipSupport.enableFor(treeViewer, ToolTip.NO_RECREATE);

		storagePropertyForm = new StorageDataPropertyForm(mainComposite, toolkit);
		storagePropertyForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		treeViewer.addSelectionChangedListener(storagePropertyForm);

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection structuredSelection = (StructuredSelection) event.getSelection();
				if (structuredSelection.getFirstElement() instanceof StorageLeaf) {
					lastSelectedLeaf = (StorageLeaf) structuredSelection.getFirstElement();
				}
			}
		});

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateViewToolbar();
			}
		});

		treeViewer.addDoubleClickListener(new DoubleClickListener());

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
					gd = new GridLayout(2, true);
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
		updateViewToolbar();

		getSite().setSelectionProvider(treeViewer);
	}

	/**
	 * Creates the view tool-bar.
	 */
	private void createViewToolbar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		toolBarManager.add(new ShowPropertiesAction());

		MenuAction filterMenuAction = new MenuAction();
		filterMenuAction.setText("Group and Filter");
		filterMenuAction.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_FILTER));

		groupByLabelMenu = new MenuManager("Group Storages By");
		filterMenuAction.addContributionItem(groupByLabelMenu);

		filterByRepositoryMenu = new MenuManager("Filter By Repository");
		filterMenuAction.addContributionItem(filterByRepositoryMenu);

		MenuManager filterByStateMenu = new MenuManager("Filter By Storage State");
		filterByStateMenu.add(new FilterStatesAction("Writable", StorageState.OPENED));
		filterByStateMenu.add(new FilterStatesAction("Recording", StorageState.RECORDING));
		filterByStateMenu.add(new FilterStatesAction("Readable", StorageState.CLOSED));
		filterMenuAction.addContributionItem(filterByStateMenu);

		toolBarManager.add(filterMenuAction);
		toolBarManager.add(new Separator());

	}

	/**
	 * Updates the storage list for all {@link CmrRepositoryDefinition}.
	 */
	private void updateStorageList() {
		storageRespositoryMap.clear();
		for (CmrRepositoryDefinition cmrRepositoryDefinition : cmrRepositoryManager.getCmrRepositoryDefinitions()) {
			boolean canUpdate = false;
			if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {
				canUpdate = true;
			} else {
				OnlineStatus cachedStatus = cachedOnlineStatus.get(cmrRepositoryDefinition);
				if (OnlineStatus.ONLINE.equals(cachedStatus)) {
					canUpdate = true;
				}
			}
			if (canUpdate) {
				try {
					List<StorageData> storages = cmrRepositoryDefinition.getStorageService().getExistingStorages();
					for (StorageData storage : storages) {
						storageRespositoryMap.put(storage, cmrRepositoryDefinition);
					}
				} catch (Exception e) {
					continue;
				}
			}
		}
	}

	/**
	 * Updates the storage list only for provided {@link CmrRepositoryDefinition}.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}
	 * @param removeOnly
	 *            If set to true, no storages will be loaded from the CMR.
	 */
	private void updateStorageList(CmrRepositoryDefinition cmrRepositoryDefinition, boolean removeOnly) {
		while (storageRespositoryMap.values().remove(cmrRepositoryDefinition)) {
			continue;
		}
		if (!removeOnly) {
			boolean canUpdate = false;
			if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {
				canUpdate = true;
			} else {
				OnlineStatus cachedStatus = cachedOnlineStatus.get(cmrRepositoryDefinition);
				if (OnlineStatus.ONLINE.equals(cachedStatus)) {
					canUpdate = true;
				}
			}
			if (canUpdate) {
				List<StorageData> storages = cmrRepositoryDefinition.getStorageService().getExistingStorages();
				for (StorageData storage : storages) {
					storageRespositoryMap.put(storage, cmrRepositoryDefinition);
				}
			}
		}
	}

	/**
	 * Updates the form body.
	 */
	private void updateFormBody() {
		clearFormBody();
		if (!storageRespositoryMap.isEmpty()) {
			treeViewer.getTree().setVisible(true);
			treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			treeViewer.setInput(new StorageManagerTreeModelManager(storageRespositoryMap, orderingLabelType));
			treeViewer.expandToLevel(TreeViewer.ALL_LEVELS);
			if (null != lastSelectedLeaf && storageRespositoryMap.keySet().contains(lastSelectedLeaf.getStorageData())) {
				StructuredSelection ss = new StructuredSelection(lastSelectedLeaf);
				treeViewer.setSelection(ss, true);
			}
			filterStorageComposite.setEnabled(true);
		} else {
			displayMessage("No storage information available on currently available CMR repositories.", Display.getDefault().getSystemImage(SWT.ICON_INFORMATION));
			filterStorageComposite.setEnabled(false);
		}
		mainForm.getBody().layout();
	}

	/**
	 * Clears the look of the forms body.
	 */
	private void clearFormBody() {
		if (cmrMessageComposite != null && !cmrMessageComposite.isDisposed()) {
			cmrMessageComposite.dispose();
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
		if (null == cmrMessageComposite || cmrMessageComposite.isDisposed()) {
			cmrMessageComposite = toolkit.createComposite(mainForm.getBody());
		} else {
			for (Control c : cmrMessageComposite.getChildren()) {
				if (!c.isDisposed()) {
					c.dispose();
				}
			}
		}
		cmrMessageComposite.setLayout(new GridLayout(2, false));
		cmrMessageComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		toolkit.createLabel(cmrMessageComposite, null).setImage(image);
		toolkit.createLabel(cmrMessageComposite, text, SWT.WRAP).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
	}

	/**
	 * Updates the view tool-bar.
	 */
	private void updateViewToolbar() {
		filterByRepositoryMenu.removeAll();
		for (CmrRepositoryDefinition cmrRepositoryDefinition : cmrRepositoryManager.getCmrRepositoryDefinitions()) {
			filterByRepositoryMenu.add(new FilterRepositoriesAction(cmrRepositoryDefinition));
		}
		filterByRepositoryMenu.getParent().update(false);

		Set<AbstractStorageLabelType<?>> availableLabelTypes = new HashSet<AbstractStorageLabelType<?>>();
		for (StorageData storageData : storageRespositoryMap.keySet()) {
			for (AbstractStorageLabel<?> label : storageData.getLabelList()) {
				availableLabelTypes.add(label.getStorageLabelType());
			}
		}
		groupByLabelMenu.removeAll();
		groupByLabelMenu.add(new LabelOrderAction("CMR Repository", InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_SERVER_ONLINE_SMALL), null, null == orderingLabelType));
		for (AbstractStorageLabelType<?> labelType : availableLabelTypes) {
			groupByLabelMenu.add(new LabelOrderAction(TextFormatter.getLabelName(labelType), ImageFormatter.getImageDescriptorForLabel(labelType), labelType, ObjectUtils.equals(labelType,
					orderingLabelType)));
		}
	}

	/**
	 * Performs update.
	 * 
	 * @param updateStorageList
	 *            If the update should go to the CMRs for an updated storage list.
	 */
	private void performUpdate(final boolean updateStorageList) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				mainForm.setBusy(true);
				if (updateStorageList) {
					updateStorageList();
				}
				updateFormBody();
				updateViewToolbar();
				mainForm.setBusy(false);
				mainForm.layout();
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
	public void repositoryOnlineStatusUpdated(CmrRepositoryDefinition repositoryDefinition, OnlineStatus oldStatus, OnlineStatus newStatus) {
		if (newStatus == OnlineStatus.ONLINE) {
			OnlineStatus cachedStatus = cachedOnlineStatus.get(repositoryDefinition);
			if (null == cachedStatus || OnlineStatus.OFFLINE.equals(cachedStatus) || OnlineStatus.UNKNOWN.equals(cachedStatus)) {
				updateStorageList(repositoryDefinition, false);
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						updateFormBody();
					}
				});
			}
			cachedOnlineStatus.put(repositoryDefinition, newStatus);
		} else if (newStatus == OnlineStatus.OFFLINE) {
			OnlineStatus cachedStatus = cachedOnlineStatus.get(repositoryDefinition);
			if (null == cachedStatus || OnlineStatus.ONLINE.equals(cachedStatus)) {
				updateStorageList(repositoryDefinition, true);
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						updateFormBody();
					}
				});
			}
			cachedOnlineStatus.put(repositoryDefinition, newStatus);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryAdded(CmrRepositoryDefinition cmrRepositoryDefinition) {
		cachedOnlineStatus.put(cmrRepositoryDefinition, cmrRepositoryDefinition.getOnlineStatus());
		updateStorageList(cmrRepositoryDefinition, false);
		performUpdate(false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryRemoved(CmrRepositoryDefinition cmrRepositoryDefinition) {
		cachedOnlineStatus.remove(cmrRepositoryDefinition);
		updateStorageList(cmrRepositoryDefinition, true);
		performUpdate(false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void repositoryDataUpdated(CmrRepositoryDefinition cmrRepositoryDefinition) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				updateFormBody();
				updateViewToolbar();
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public void refresh() {
		performUpdate(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canRefresh() {
		return !storageRespositoryMap.isEmpty() || !cmrRepositoryManager.getCmrRepositoryDefinitions().isEmpty();
	}

	/**
	 * Refreshes the view, only by refreshing the storages on the given repository.
	 * 
	 * @param cmrRepositoryDefinition
	 *            Repository to update storages for.
	 */
	public void refresh(CmrRepositoryDefinition cmrRepositoryDefinition) {
		updateStorageList(cmrRepositoryDefinition, false);
		performUpdate(false);
	}

	/**
	 * Show or hides properties.
	 * 
	 * @param show
	 *            Should properties be shown.
	 */
	public void setShowProperties(boolean show) {
		if (show) {
			StorageLeaf storageLeaf = null;
			StructuredSelection selection = (StructuredSelection) treeViewer.getSelection();
			if (!selection.isEmpty()) {
				if (selection.getFirstElement() instanceof StorageLeaf) {
					storageLeaf = ((StorageLeaf) selection.getFirstElement());
				}
			}

			storagePropertyForm = new StorageDataPropertyForm(mainComposite, toolkit, storageLeaf);
			storagePropertyForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			treeViewer.addSelectionChangedListener(storagePropertyForm);
			mainComposite.layout();
			setTitleToolTip("Hide Properties");
		} else {
			if (null != storagePropertyForm && !storagePropertyForm.isDisposed()) {
				treeViewer.removeSelectionChangedListener(storagePropertyForm);
				storagePropertyForm.dispose();
				storagePropertyForm = null;
			}
			mainComposite.layout();
			setTitleToolTip("Show Properties");
		}
	}

	/**
	 * Performs update of the view, without getting data from CMR.
	 */
	public void refreshWithoutCmrCall() {
		performUpdate(false);
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
	 * Filter for the tree.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class TreeFilter extends ViewerFilter {

		/**
		 * Set of excluded repositories.
		 */
		private Set<CmrRepositoryDefinition> filteredRespositories = new HashSet<CmrRepositoryDefinition>();

		/**
		 * Set of excluded states.
		 */
		private Set<StorageState> filteredStates = new HashSet<StorageState>();

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof StorageLeaf) {
				StorageLeaf storageLeaf = (StorageLeaf) element;
				if (filteredRespositories.contains(storageLeaf.getCmrRepositoryDefinition())) {
					return false;
				}
				if (filteredStates.contains(storageLeaf.getStorageData().getState())) {
					return false;
				}
			}
			return true;
		}

		/**
		 * @return the filteredRespositories
		 */
		public Set<CmrRepositoryDefinition> getFilteredRespositories() {
			return filteredRespositories;
		}

		/**
		 * @return the filteredStates
		 */
		public Set<StorageState> getFilteredStates() {
			return filteredStates;
		}

	}

	/**
	 * Action for selecting the grouping of storages.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class LabelOrderAction extends Action {

		/**
		 * Label type to group.
		 */
		private AbstractStorageLabelType<?> labelType;

		/**
		 * Constructor.
		 * 
		 * @param name
		 *            Name of action.
		 * @param imgDescriptor
		 *            {@link ImageDescriptor}.
		 * @param labelType
		 *            Label type to represent. Null for default settings.
		 * @param isChecked
		 *            Should be checked.
		 */
		public LabelOrderAction(String name, ImageDescriptor imgDescriptor, AbstractStorageLabelType<?> labelType, boolean isChecked) {
			super(name, Action.AS_RADIO_BUTTON);
			this.labelType = labelType;
			setChecked(isChecked);
			setImageDescriptor(imgDescriptor);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			if (isChecked()) {
				orderingLabelType = labelType;
				updateFormBody();
			}
		}
	}

	/**
	 * Filter by storage repository action.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class FilterRepositoriesAction extends Action {

		/**
		 * Cmr to exclude/include.
		 */
		private CmrRepositoryDefinition cmrRepositoryDefinition;

		/**
		 * @param cmrRepositoryDefinition
		 *            Cmr to exclude/include.
		 */
		public FilterRepositoriesAction(CmrRepositoryDefinition cmrRepositoryDefinition) {
			super();
			this.cmrRepositoryDefinition = cmrRepositoryDefinition;
			setText(cmrRepositoryDefinition.getName());
			setChecked(!treeFilter.getFilteredRespositories().contains(cmrRepositoryDefinition));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			if (isChecked()) {
				treeFilter.getFilteredRespositories().remove(cmrRepositoryDefinition);
			} else {
				treeFilter.getFilteredRespositories().add(cmrRepositoryDefinition);
			}
			treeViewer.refresh();
			treeViewer.expandToLevel(TreeViewer.ALL_LEVELS);
			if (null != lastSelectedLeaf && storageRespositoryMap.keySet().contains(lastSelectedLeaf.getStorageData())) {
				StructuredSelection ss = new StructuredSelection(lastSelectedLeaf);
				treeViewer.setSelection(ss, true);
			}
		}

	}

	/**
	 * Filter by storage state action.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class FilterStatesAction extends Action {

		/**
		 * Storage state to exclude/include.
		 */
		private StorageState state;

		/**
		 * 
		 * @param text
		 *            Action text.
		 * @param state
		 *            Storage state to exclude/include.
		 */
		public FilterStatesAction(String text, StorageState state) {
			super();
			this.state = state;
			setText(text);
			setChecked(!treeFilter.getFilteredStates().contains(state));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			if (isChecked()) {
				treeFilter.getFilteredStates().remove(state);
			} else {
				treeFilter.getFilteredStates().add(state);
			}
			treeViewer.refresh();
			treeViewer.expandToLevel(TreeViewer.ALL_LEVELS);
			if (null != lastSelectedLeaf && storageRespositoryMap.keySet().contains(lastSelectedLeaf.getStorageData())) {
				StructuredSelection ss = new StructuredSelection(lastSelectedLeaf);
				treeViewer.setSelection(ss, true);
			}
		}

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
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private final class FilterStorageComposite extends FilterComposite {

		/**
		 * String to be filtered.
		 */
		private String filterString = "";

		/**
		 * Filter.
		 */
		private ViewerFilter filter = new ViewerFilter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (Objects.equals("", filterString)) {
					return true;
				} else {
					if (element instanceof IStorageDataProvider) {
						return select(((IStorageDataProvider) element).getStorageData());
					}
					return true;
				}
			}

			/**
			 * Does a filter select on {@link StorageData}.
			 * 
			 * @param storageData
			 *            {@link StorageData}
			 * @return True if data in {@link StorageData} fits the filter string.
			 */
			private boolean select(StorageData storageData) {
				if (StringUtils.containsIgnoreCase(storageData.getName(), filterString)) {
					return true;
				}
				if (StringUtils.containsIgnoreCase(storageData.getDescription(), filterString)) {
					return true;
				}
				if (StringUtils.containsIgnoreCase(storageData.getState().toString(), filterString)) {
					return true;
				}
				for (AbstractStorageLabel<?> label : storageData.getLabelList()) {
					if (StringUtils.containsIgnoreCase(TextFormatter.getLabelValue(label, false), filterString)) {
						return true;
					}
				}
				return false;
			}
		};

		/**
		 * Default constructor.
		 * 
		 * @param parent
		 *            A widget which will be the parent of the new instance (cannot be null).
		 * @param style
		 *            The style of widget to construct.
		 * @see Composite#Composite(Composite, int)
		 */
		public FilterStorageComposite(Composite parent, int style) {
			super(parent, style, "Filter storages");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void executeCancel() {
			this.filterString = "";
			treeViewer.refresh();
			treeViewer.expandToLevel(TreeViewer.ALL_LEVELS);
			if (null != lastSelectedLeaf && storageRespositoryMap.keySet().contains(lastSelectedLeaf.getStorageData())) {
				StructuredSelection ss = new StructuredSelection(lastSelectedLeaf);
				treeViewer.setSelection(ss, true);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void executeFilter(String filterString) {
			this.filterString = filterString;
			treeViewer.refresh();
			treeViewer.expandToLevel(TreeViewer.ALL_LEVELS);
			if (null != lastSelectedLeaf && storageRespositoryMap.keySet().contains(lastSelectedLeaf.getStorageData())) {
				StructuredSelection ss = new StructuredSelection(lastSelectedLeaf);
				treeViewer.setSelection(ss, true);
			}
		}

		/**
		 * Gets {@link #filter}.
		 * 
		 * @return {@link #filter}
		 */
		public ViewerFilter getFilter() {
			return filter;
		}

	}

	/**
	 * Double click listener, that opens the data explorer.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class DoubleClickListener implements IDoubleClickListener {

		/**
		 * {@inheritDoc}
		 */
		public void doubleClick(final DoubleClickEvent event) {
			UIJob openDataExplorerJob = new UIJob("Opening Data Explorer..") {

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					process(event);
					return Status.OK_STATUS;
				}
			};
			openDataExplorerJob.setUser(true);
			openDataExplorerJob.schedule();
		}

		/**
		 * Processes the double-click.
		 * 
		 * @param event
		 *            Event that denotes the click.
		 */
		private void process(DoubleClickEvent event) {
			StructuredSelection selection = (StructuredSelection) treeViewer.getSelection();
			if (!selection.isEmpty() && selection.getFirstElement() instanceof StorageLeaf) {
				StorageLeaf storageLeaf = (StorageLeaf) selection.getFirstElement();
				InspectITStorageManager storageManager = InspectIT.getDefault().getInspectITStorageManager();
				RepositoryDefinition repositoryDefinition = null;
				if (storageManager.isStorageMounted(storageLeaf.getStorageData())) {
					LocalStorageData localStorageData = storageManager.getLocalDataForStorage(storageLeaf.getStorageData());
					try {
						repositoryDefinition = storageManager.getStorageRepositoryDefinition(localStorageData);
					} catch (Exception e) {
						repositoryDefinition = null;
					}
				} else if (storageLeaf.getStorageData().getState() == StorageState.CLOSED) {
					try {
						storageManager.mountStorage(storageLeaf.getStorageData(), storageLeaf.getCmrRepositoryDefinition(), false);
						LocalStorageData localStorageData = storageManager.getLocalDataForStorage(storageLeaf.getStorageData());
						repositoryDefinition = storageManager.getStorageRepositoryDefinition(localStorageData);
					} catch (Exception e1) {
						repositoryDefinition = null;
						InspectIT.getDefault().createErrorDialog("Can not open storage.", e1, -1);
					}
				} else {
					String dialogMessage = "Storages that are in writable mode can not be explored. Do you want to finalize selected storage first and then open it?";
					MessageDialog dialog = new MessageDialog(getSite().getShell(), "Opening Writable Storage", null, dialogMessage, MessageDialog.QUESTION, new String[] { "Yes", "No" }, 0);
					if (0 == dialog.open()) {
						treeViewer.setSelection(treeViewer.getSelection());
						IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
						ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

						Command command = commandService.getCommand(CloseAndShowStorageHandler.COMMAND);
						ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, new Event());
						IEvaluationContext context = (IEvaluationContext) executionEvent.getApplicationContext();
						context.addVariable(CloseAndShowStorageHandler.STORAGE_DATA_PROVIDER, storageLeaf);
						context.addVariable(ISources.ACTIVE_SITE_NAME, getSite());
						try {
							command.executeWithChecks(executionEvent);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
					return;
				}

				if (null != repositoryDefinition) {
					IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
					ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

					Command command = commandService.getCommand(ShowRepositoryHandler.COMMAND);
					ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, new Event());
					IEvaluationContext context = (IEvaluationContext) executionEvent.getApplicationContext();
					context.addVariable(ShowRepositoryHandler.REPOSITORY_DEFINITION, repositoryDefinition);

					try {
						command.executeWithChecks(executionEvent);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			} else {
				TreeSelection treeSelection = (TreeSelection) selection;
				TreePath path = treeSelection.getPaths()[0];
				if (null != path) {
					boolean expanded = treeViewer.getExpandedState(path);
					if (expanded) {
						treeViewer.collapseToLevel(path, 1);
					} else {
						treeViewer.expandToLevel(path, 1);
					}
				}
			}
		}
	}

}
