package info.novatec.inspectit.rcp.view.server;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.handlers.OpenViewHandler;
import info.novatec.inspectit.rcp.model.Component;
import info.novatec.inspectit.rcp.model.StorageTreeModelManager;
import info.novatec.inspectit.rcp.model.TreeModelManager;
import info.novatec.inspectit.rcp.repository.CmrRepositoryChangeListener;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.repository.RepositoryManager;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.nebula.widgets.pshelf.PShelf;
import org.eclipse.nebula.widgets.pshelf.PShelfItem;
import org.eclipse.nebula.widgets.pshelf.RedmondShelfRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

/**
 * This view displays the available servers / CMRs in a so called {@link PShelf} widget. Every
 * server contains a separate tree view which displays the information of this specific server.
 * 
 * @author Patrice Bouillet
 * @author Ivan Senic
 * 
 */
public class ServerView extends ViewPart implements CmrRepositoryChangeListener {

	/**
	 * The ID of this view.
	 */
	public static final String ID = "inspectit.serverview";

	/**
	 * The component used to display a separate item for every server.
	 */
	private PShelf pShelf;

	/**
	 * The label provider for all tree views.
	 */
	private ServerViewLabelProvider serverViewLabelProvider = new ServerViewLabelProvider();

	/**
	 * The sorting for all trees.
	 */
	private ServerViewComparator serverViewSorter = new ServerViewComparator();

	/**
	 * The double click listener used by all tree views.
	 */
	private TreeViewDoubleClickListener treeViewDoubleClickListener = new TreeViewDoubleClickListener();

	/**
	 * The toolkit used for the colors etc.
	 */
	private FormToolkit toolkit;

	/**
	 * List containing all created tree viewers.
	 */
	private List<TreeViewer> treeViewers = new ArrayList<TreeViewer>();

	/**
	 * Map of repositry definitions and selfs.
	 */
	private Map<RepositoryDefinition, CmrRepositoryShelfItem> repositoryShelfMap = new ConcurrentHashMap<RepositoryDefinition, CmrRepositoryShelfItem>();

	/**
	 * Map of online repositories and their statuses.
	 */
	private Map<CmrRepositoryDefinition, OnlineStatus> repositoryStatusMap = new ConcurrentHashMap<CmrRepositoryDefinition, CmrRepositoryDefinition.OnlineStatus>();

	/**
	 * Storage shelf.
	 */
	private PShelfItem storageShelf;

	/**
	 * Selection provider.
	 */
	private SelectionProviderIntermediate selectionProviderIntermediate = new SelectionProviderIntermediate();

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());

		getSite().setSelectionProvider(selectionProviderIntermediate);

		pShelf = new PShelf(parent, SWT.NONE);

		RedmondShelfRenderer redmondShelfRenderer = new RedmondShelfRenderer();
		pShelf.setRenderer(redmondShelfRenderer);

		Font font = new Font(parent.getDisplay(), "Arial", 12, SWT.BOLD | SWT.ITALIC);
		redmondShelfRenderer.setFont(font);
		redmondShelfRenderer.setSelectedFont(font);

		// create all items
		RepositoryManager repositoryManager = InspectIT.getDefault().getRepositoryManager();
		repositoryManager.addRepositoryChangeListener(this);
		for (RepositoryDefinition repositoryDefinition : repositoryManager.getRepositoryDefinitions()) {
			if (repositoryDefinition instanceof CmrRepositoryDefinition) {
				createServerItem(pShelf, (CmrRepositoryDefinition) repositoryDefinition, pShelf.getItems().length);
			}
		}

		// create the storage area
		createStorageArea(pShelf);

		// add a selection listener so we can set the correct selection provider
		pShelf.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				PShelfItem item = (PShelfItem) e.item;
				if (item.getBody().getChildren().length > 0) {
					Object child = item.getBody().getChildren()[0];
					if (child instanceof Tree) {
						Tree tree = (Tree) child;
						TreeViewer treeViewer = findTreeViewer(tree);
						selectionProviderIntermediate.setSelectionProviderDelegate(treeViewer);
					} else {
						selectionProviderIntermediate.setSelectionProviderDelegate(null);
					}
				}
			}
		});

	}

	/**
	 * Finds a tree viewer which was created for the server view in the list.
	 * 
	 * @param tree
	 *            The tree which should be searched for in the tree viewers.
	 * @return The found {@link TreeViewer}.
	 */
	private TreeViewer findTreeViewer(Tree tree) {
		for (TreeViewer treeViewer : treeViewers) {
			if (tree.equals(treeViewer.getTree())) {
				return treeViewer;
			}
		}

		return null;
	}

	/**
	 * Creates a new server item.
	 * 
	 * @param pShelf
	 *            The container for the item.
	 * @param repositoryDefinition
	 *            The repository definition for this item.
	 * @param index
	 *            Index of shelf.
	 */
	private void createServerItem(PShelf pShelf, final CmrRepositoryDefinition repositoryDefinition, int index) {
		CmrRepositoryShelfItem cmrRepositoryShelfItem = new CmrRepositoryShelfItem(pShelf, index, repositoryDefinition);
		repositoryShelfMap.put(repositoryDefinition, cmrRepositoryShelfItem);
		cmrRepositoryShelfItem.updateCmrRepresentation();
	}

	/**
	 * Deletes an already existing server item.
	 * 
	 * @param repositoryDefinition
	 *            The repository definition which is contained in one of the items.
	 */
	private void deleteServerItem(CmrRepositoryDefinition repositoryDefinition) {
		CmrRepositoryShelfItem shelfItem = repositoryShelfMap.remove(repositoryDefinition);
		repositoryStatusMap.remove(repositoryDefinition);
		if (null != shelfItem) {
			shelfItem.dispose();
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		pShelf.setFocus();
	}

	/**
	 * The double click listener of the server view.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private class TreeViewDoubleClickListener implements IDoubleClickListener {

		/**
		 * {@inheritDoc}
		 */
		public void doubleClick(DoubleClickEvent event) {
			TreeSelection selection = (TreeSelection) event.getSelection();
			Object element = selection.getFirstElement();
			if (null != element) {
				if (((Component) element).getInputDefinition() == null) {
					TreeViewer treeViewer = (TreeViewer) event.getViewer();
					TreePath path = selection.getPaths()[0];
					if (null != path) {
						boolean expanded = treeViewer.getExpandedState(path);
						if (expanded) {
							treeViewer.collapseToLevel(path, 1);
						} else {
							treeViewer.expandToLevel(path, 1);
						}
					}
				} else {
					IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
					ICommandService commandService = (ICommandService) getSite().getService(ICommandService.class);

					Command command = commandService.getCommand(OpenViewHandler.COMMAND);
					ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, new Event());
					IEvaluationContext context = (IEvaluationContext) executionEvent.getApplicationContext();
					context.addVariable(OpenViewHandler.INPUT, ((Component) element).getInputDefinition());

					try {
						command.executeWithChecks(executionEvent);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryAdded(RepositoryDefinition repositoryDefinition) {
		if (repositoryDefinition instanceof CmrRepositoryDefinition) {
			createServerItem(pShelf, (CmrRepositoryDefinition) repositoryDefinition, pShelf.getItems().length - 1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryRemoved(RepositoryDefinition repositoryDefinition) {
		if (repositoryDefinition instanceof CmrRepositoryDefinition) {
			deleteServerItem((CmrRepositoryDefinition) repositoryDefinition);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryOnlineStatusUpdated(CmrRepositoryDefinition repositoryDefinition, OnlineStatus oldStatus, OnlineStatus newStatus) {
		final CmrRepositoryShelfItem shelfItem = repositoryShelfMap.get(repositoryDefinition);
		if (null != shelfItem) {
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					shelfItem.updateStatusImage();
				}
			});
			OnlineStatus oldKnownStatus = repositoryStatusMap.get(repositoryDefinition);
			if (null == oldKnownStatus || (oldKnownStatus == OnlineStatus.OFFLINE && newStatus == OnlineStatus.ONLINE)) {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						shelfItem.updateCmrRepresentation();
					}
				});
			}
			if (newStatus == OnlineStatus.ONLINE || newStatus == OnlineStatus.OFFLINE) {
				repositoryStatusMap.put(repositoryDefinition, newStatus);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateRepository(final RepositoryDefinition repositoryDefinition) {
		if (repositoryDefinition instanceof StorageRepositoryDefinition) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					updateStorageRepository();
				}
			});
		} else if (repositoryDefinition instanceof CmrRepositoryDefinition) {
			final CmrRepositoryShelfItem shelfItem = repositoryShelfMap.get(repositoryDefinition);
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					shelfItem.updateStatusImage();
					if (((CmrRepositoryDefinition) repositoryDefinition).getOnlineStatus() == OnlineStatus.ONLINE) {
						shelfItem.updateCmrRepresentation();
					}
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateStorageRepository() {
		Control[] controls = storageShelf.getBody().getChildren();
		for (Control control : controls) {
			if (control instanceof Tree) {
				TreeViewer viewer = findTreeViewer((Tree) control);
				if (null != viewer) {
					treeViewers.remove(viewer);
					if (viewer.equals(selectionProviderIntermediate.getSelectionProviderDelegate())) {
						selectionProviderIntermediate.setSelectionProviderDelegate(null);
					}
				}
			}
			control.dispose();
		}
		updateStorageArea(storageShelf, (StorageRepositoryDefinition) storageShelf.getData());
		storageShelf.getBody().layout();
	}

	/**
	 * Returns the active repository definition, that is the active {@link PShelfItem} with the
	 * data.
	 * 
	 * @return The active repository definition.
	 */
	public RepositoryDefinition getActiveRepositoryDefinition() {
		return (RepositoryDefinition) pShelf.getSelection().getData();
	}

	/**
	 * Creates storage area.
	 * 
	 * @param pShelf
	 *            {@link PShelf}.
	 */
	private void createStorageArea(PShelf pShelf) {
		storageShelf = new PShelfItem(pShelf, SWT.NONE);

		RepositoryDefinition repositoryDefinition = new StorageRepositoryDefinition();

		// Check the version of the CMR
		storageShelf.setText("Storage Area");
		storageShelf.setImage(InspectIT.getDefault().getImage(InspectITConstants.IMG_DATABASE));
		storageShelf.getBody().setLayout(new FillLayout());
		storageShelf.setData(repositoryDefinition);

		updateStorageArea(storageShelf, repositoryDefinition);
	}

	/**
	 * Updates storage area.
	 * 
	 * @param item
	 *            Storage shelf.
	 * @param repositoryDefinition
	 *            {@link RepositoryDefinition}.
	 */
	private void updateStorageArea(PShelfItem item, RepositoryDefinition repositoryDefinition) {
		Tree tree = toolkit.createTree(item.getBody(), SWT.V_SCROLL | SWT.H_SCROLL);

		TreeViewer treeViewer = new TreeViewer(tree);
		treeViewer.setContentProvider(new ServerViewContentProvider());
		treeViewer.setLabelProvider(serverViewLabelProvider);
		treeViewer.setComparator(serverViewSorter);
		treeViewer.addDoubleClickListener(treeViewDoubleClickListener);
		treeViewer.setInput(new StorageTreeModelManager(repositoryDefinition));

		ColumnViewerToolTipSupport.enableFor(treeViewer, ToolTip.NO_RECREATE);

		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		getSite().registerContextMenu(ID + ".storagetree", menuManager, treeViewer);

		Control control = treeViewer.getControl();
		Menu menu = menuManager.createContextMenu(control);
		control.setMenu(menu);

		treeViewers.add(treeViewer);

		if (null == selectionProviderIntermediate.getSelectionProviderDelegate()) {
			selectionProviderIntermediate.setSelectionProviderDelegate(treeViewer);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		super.dispose();
		toolkit.dispose();
	}

	/**
	 * {@link PShelfItem} for displaying {@link CmrRepositoryDefinition}.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class CmrRepositoryShelfItem {

		/**
		 * Shelf.
		 */
		private PShelfItem item;

		/**
		 * Repository.
		 */
		private CmrRepositoryDefinition repositoryDefinition;

		/**
		 * Info label.
		 */
		private Label infoLabel;

		/**
		 * Default constructor.
		 * 
		 * @param shelf
		 *            {@link PShelf}
		 * @param index
		 *            Index.
		 * @param cmrRepositoryDefinition
		 *            {@link CmrRepositoryDefinition}.
		 */
		public CmrRepositoryShelfItem(PShelf shelf, int index, CmrRepositoryDefinition cmrRepositoryDefinition) {
			Assert.isNotNull(cmrRepositoryDefinition);
			this.repositoryDefinition = cmrRepositoryDefinition;
			this.item = new PShelfItem(shelf, SWT.NONE, index);
			this.item.setData(cmrRepositoryDefinition);
		}

		/**
		 * Updates only the online status image.
		 */
		public void updateStatusImage() {
			switch (repositoryDefinition.getOnlineStatus()) {
			case ONLINE:
				item.setImage(InspectIT.getDefault().getImage(InspectITConstants.IMG_SERVER_ONLINE));
				break;
			case OFFLINE:
				item.setImage(InspectIT.getDefault().getImage(InspectITConstants.IMG_SERVER_OFFLINE));
				break;
			case CHECKING:
				item.setImage(InspectIT.getDefault().getImage(InspectITConstants.IMG_SERVER_REFRESH));
			default:
				break;
			}
		}

		/**
		 * update the shelf based on the CMR online status.
		 */
		public void updateCmrRepresentation() {
			// dispose anything that was there before
			disposeControls();
			item.setText(repositoryDefinition.getIp() + " : " + repositoryDefinition.getPort() + " [v.  ? ]");
			item.getBody().setLayout(new FillLayout());
			updateStatusImage();
			switch (repositoryDefinition.getOnlineStatus()) {
			case ONLINE:
				if (null != infoLabel && !infoLabel.isDisposed()) {
					infoLabel.dispose();
				}
				item.setText(repositoryDefinition.getIp() + " : " + repositoryDefinition.getPort() + " [v. " + repositoryDefinition.getServerStatusService().getVersion() + " ]");

				Tree tree = toolkit.createTree(item.getBody(), SWT.V_SCROLL | SWT.H_SCROLL);
				final TreeViewer treeViewer = new TreeViewer(tree);
				treeViewer.setContentProvider(new ServerViewContentProvider());
				treeViewer.setLabelProvider(serverViewLabelProvider);
				treeViewer.setComparator(serverViewSorter);
				treeViewer.addDoubleClickListener(treeViewDoubleClickListener);
				treeViewer.setInput(new TreeModelManager(repositoryDefinition));
				treeViewer.expandToLevel(2);

				ColumnViewerToolTipSupport.enableFor(treeViewer, ToolTip.NO_RECREATE);

				treeViewers.add(treeViewer);

				if (null == selectionProviderIntermediate.getSelectionProviderDelegate()) {
					selectionProviderIntermediate.setSelectionProviderDelegate(treeViewer);
				}
				break;
			case OFFLINE:
				infoLabel = toolkit.createLabel(item.getBody(), "CMR is currently not online, please try again later!", SWT.WRAP | SWT.READ_ONLY);
				break;
			case CHECKING:
				infoLabel = toolkit.createLabel(item.getBody(), "Loading repository...", SWT.WRAP | SWT.READ_ONLY);
				break;
			default:
				break;
			}

			item.getBody().layout(true);
		}

		/**
		 * Dispose it self.
		 */
		public void dispose() {
			disposeControls();
			selectionProviderIntermediate.setSelectionProviderDelegate(null);
			item.dispose();
		}

		/**
		 * Dispose all controls.
		 */
		private void disposeControls() {
			for (Control control : item.getBody().getChildren()) {
				if (control instanceof Tree) {
					Tree tree = (Tree) item.getBody().getChildren()[0];
					TreeViewer treeViewer = findTreeViewer(tree);
					treeViewers.remove(treeViewer);
				}
				control.dispose();
			}
		}

	}

}