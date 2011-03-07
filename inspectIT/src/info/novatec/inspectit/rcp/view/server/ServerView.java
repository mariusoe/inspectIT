package info.novatec.inspectit.rcp.view.server;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.handlers.OpenViewHandler;
import info.novatec.inspectit.rcp.model.Component;
import info.novatec.inspectit.rcp.model.StorageTreeModelManager;
import info.novatec.inspectit.rcp.model.TreeModelManager;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.RepositoryChangeListener;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.repository.RepositoryManager;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinition;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
 * 
 */
public class ServerView extends ViewPart implements RepositoryChangeListener {

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
		for (RepositoryDefinition repositoryDefinition : repositoryManager.getRepositoryDefinitions()) {
			createServerItem(pShelf, repositoryDefinition, pShelf.getItems().length);
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

		repositoryManager.addRepositoryChangeListener(this);
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
	 */
	private void createServerItem(PShelf pShelf, final RepositoryDefinition repositoryDefinition, int index) {
		final PShelfItem item = new PShelfItem(pShelf, SWT.NONE, index);

		UpdateRepositoryJob updateRepositoryJob = new UpdateRepositoryJob(item, repositoryDefinition);
		updateRepositoryJob.schedule();
	}

	/**
	 * Deletes an already existing server item.
	 * 
	 * @param shelf
	 *            The container for the item.
	 * @param repositoryDefinition
	 *            The repository definition which is contained in one of the items.
	 */
	private void deleteServerItem(PShelf shelf, RepositoryDefinition repositoryDefinition) {
		PShelfItem[] items = shelf.getItems();

		// search for the corresponding item which holds this repo definition.
		for (PShelfItem shelfItem : items) {
			if (shelfItem.getData().equals(repositoryDefinition)) {
				if (shelfItem.getBody().getChildren().length > 0) {
					if (shelfItem.getBody().getChildren()[0] instanceof Tree) {
						Tree tree = (Tree) shelfItem.getBody().getChildren()[0];
						TreeViewer treeViewer = findTreeViewer(tree);
						treeViewers.remove(treeViewer);
					}
					selectionProviderIntermediate.setSelectionProviderDelegate(null);
				}
				shelfItem.dispose();
				break;
			}
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
		createServerItem(pShelf, repositoryDefinition, pShelf.getItems().length - 1);
	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryRemoved(RepositoryDefinition repositoryDefinition) {
		deleteServerItem(pShelf, repositoryDefinition);
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateRepository(RepositoryDefinition repositoryDefinition) {
		PShelfItem[] items = pShelf.getItems();

		// search for the corresponding item which holds this repository
		// definition.
		for (final PShelfItem shelfItem : items) {
			if (shelfItem.getData().equals(repositoryDefinition)) {
				Control[] controls = shelfItem.getBody().getChildren();
				for (Control control : controls) {
					if (control instanceof Tree) {
						TreeViewer viewer = findTreeViewer((Tree) control);
						if (null != viewer) {
							treeViewers.remove(viewer);
						}
					}
					control.dispose();
				}

				// always clear the selection provider
				selectionProviderIntermediate.setSelectionProviderDelegate(null);

				if (repositoryDefinition instanceof CmrRepositoryDefinition) {
					UpdateRepositoryJob updateRepositoryJob = new UpdateRepositoryJob(shelfItem, repositoryDefinition);
					updateRepositoryJob.schedule();

				} else if (repositoryDefinition instanceof StorageRepositoryDefinition) {
					updateStorageArea(shelfItem, repositoryDefinition);
				}
				shelfItem.getBody().layout();
				break;
			}
		}
	}

	@Override
	public void updateStorageRepository() {
		PShelfItem[] items = pShelf.getItems();

		// search for the corresponding item which holds this repository
		// definition.
		for (PShelfItem shelfItem : items) {
			if (shelfItem.getData() instanceof StorageRepositoryDefinition) {
				Control[] controls = shelfItem.getBody().getChildren();
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
				updateStorageArea(shelfItem, (StorageRepositoryDefinition) shelfItem.getData());
				shelfItem.getBody().layout();
				break;
			}
		}
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

	private void createStorageArea(PShelf pShelf) {
		PShelfItem item = new PShelfItem(pShelf, SWT.NONE);

		RepositoryDefinition repositoryDefinition = new StorageRepositoryDefinition();

		// Check the version of the CMR
		item.setText("Storage Area");
		item.setImage(InspectIT.getDefault().getImage(InspectITConstants.IMG_DATABASE));
		item.getBody().setLayout(new FillLayout());
		item.setData(repositoryDefinition);

		updateStorageArea(item, repositoryDefinition);
	}

	private void updateStorageArea(PShelfItem item, RepositoryDefinition repositoryDefinition) {
		Tree tree = toolkit.createTree(item.getBody(), SWT.V_SCROLL | SWT.H_SCROLL);
		final TreeViewer treeViewer = new TreeViewer(tree);
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
	 * Updates the repository in one {@link PShelfItem}.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class UpdateRepositoryJob extends Job {

		/**
		 * Shelf.
		 */
		private PShelfItem item;

		/**
		 * Repository.
		 */
		private RepositoryDefinition repositoryDefinition;

		/**
		 * Info label.
		 */
		private Label infoLabel;

		/**
		 * Constructor with repository to update and shelf.
		 * 
		 * @param item
		 *            Shelf that needs to be update.
		 * @param repositoryDefinition
		 *            Repository that needs to be displayed in a shelf.
		 */
		public UpdateRepositoryJob(PShelfItem item, RepositoryDefinition repositoryDefinition) {
			super("Update Repository Job");
			this.item = item;
			this.repositoryDefinition = repositoryDefinition;
			initializeUpdate();
		}

		/**
		 * Set up the loading message.
		 */
		private void initializeUpdate() {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					item.setText(repositoryDefinition.getIp() + " : " + repositoryDefinition.getPort() + " [v.  ? ]");
					item.setImage(InspectIT.getDefault().getImage(InspectITConstants.IMG_SERVER_REFRESH));
					item.getBody().setLayout(new FillLayout());
					item.setData(repositoryDefinition);

					infoLabel = toolkit.createLabel(item.getBody(), "Loading repository...", SWT.WRAP | SWT.READ_ONLY);
					item.getBody().layout();
				}
			});

		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Performs the update of repository.
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (repositoryDefinition.getServerStatusService().isOnline()) {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						infoLabel.dispose();
						item.setImage(InspectIT.getDefault().getImage(InspectITConstants.IMG_SERVER_ONLINE));
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
						item.getBody().layout();
					}
				});
			} else {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						item.setImage(InspectIT.getDefault().getImage(InspectITConstants.IMG_SERVER_OFFLINE));

						infoLabel.setText("CMR is currently not online, please try again later!");
						item.getBody().layout();
					}
				});

			}
			return Status.OK_STATUS;
		}

	}

}