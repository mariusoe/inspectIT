package info.novatec.inspectit.rcp.view.server;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.model.TreeModelManager;
import info.novatec.inspectit.rcp.repository.RepositoryChangeListener;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.repository.RepositoryManager;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

/**
 * This view displays the available servers / CMRs in a so called {@link PShelf}
 * widget. Every server contains a separate tree view which displays the
 * information of this specific server.
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

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());

		pShelf = new PShelf(parent, SWT.NONE);

		RedmondShelfRenderer redmondShelfRenderer = new RedmondShelfRenderer();
		pShelf.setRenderer(redmondShelfRenderer);

		Font font = new Font(parent.getDisplay(), "Arial", 12, SWT.BOLD | SWT.ITALIC);
		redmondShelfRenderer.setFont(font);
		redmondShelfRenderer.setSelectedFont(font);

		// create all items
		RepositoryManager repositoryManager = InspectIT.getDefault().getRepositoryManager();
		for (RepositoryDefinition repositoryDefinition : repositoryManager.getRepositoryDefinitions()) {
			createServerItem(pShelf, repositoryDefinition);
		}

		// add a selection listener so we can set the correct selection provider
		pShelf.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				PShelfItem item = (PShelfItem) e.item;
				if (item.getBody().getChildren().length > 0) {
					Tree tree = (Tree) item.getBody().getChildren()[0];
					TreeViewer treeViewer = findTreeViewer(tree);
					getSite().setSelectionProvider(treeViewer);
				} else {
					getSite().setSelectionProvider(null);
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
	private void createServerItem(PShelf pShelf, RepositoryDefinition repositoryDefinition) {
		PShelfItem item = new PShelfItem(pShelf, SWT.NONE);
		item.setText(repositoryDefinition.getIp() + " : " + repositoryDefinition.getPort());
		item.getBody().setLayout(new FillLayout());
		item.setData(repositoryDefinition);

		updateServerItem(item, repositoryDefinition);
	}

	/**
	 * Updates a specific server item.
	 * 
	 * @param item
	 *            The item to update.
	 * @param repositoryDefinition
	 *            The repository definition which is needed to update the
	 *            information
	 */
	private void updateServerItem(PShelfItem item, RepositoryDefinition repositoryDefinition) {
		if (repositoryDefinition.getServerStatusService().isOnline()) {
			item.setImage(InspectIT.getDefault().getImage(InspectITConstants.IMG_SERVER_ONLINE));

			Tree tree = toolkit.createTree(item.getBody(), SWT.V_SCROLL | SWT.H_SCROLL);
			TreeViewer treeViewer = new TreeViewer(tree);
			treeViewer.setContentProvider(new ServerViewContentProvider());
			treeViewer.setLabelProvider(serverViewLabelProvider);
			treeViewer.setComparator(serverViewSorter);
			treeViewer.addDoubleClickListener(treeViewDoubleClickListener);
			treeViewer.setInput(new TreeModelManager(repositoryDefinition));
			treeViewer.expandToLevel(2);

			ColumnViewerToolTipSupport.enableFor(treeViewer, ToolTip.NO_RECREATE);

			treeViewers.add(treeViewer);

			if (null == getSite().getSelectionProvider()) {
				getSite().setSelectionProvider(treeViewer);
			}
		} else {
			item.setImage(InspectIT.getDefault().getImage(InspectITConstants.IMG_SERVER_OFFLINE));

			toolkit.createLabel(item.getBody(), "CMR is currently not online, please try again later!", SWT.WRAP | SWT.READ_ONLY);
		}
		item.getBody().layout();
	}

	/**
	 * Deletes an already existing server item.
	 * 
	 * @param shelf
	 *            The container for the item.
	 * @param repositoryDefinition
	 *            The repository definition which is contained in one of the
	 *            items.
	 */
	private void deleteServerItem(PShelf shelf, RepositoryDefinition repositoryDefinition) {
		PShelfItem[] items = shelf.getItems();

		// search for the corresponding item which holds this repo definition.
		for (PShelfItem shelfItem : items) {
			if (shelfItem.getData().equals(repositoryDefinition)) {
				shelfItem.dispose();
				if (shelfItem.getBody().getChildren().length > 0) {
					Tree tree = (Tree) shelfItem.getBody().getChildren()[0];
					TreeViewer treeViewer = findTreeViewer(tree);
					treeViewers.remove(treeViewer);
					getSite().setSelectionProvider(null);
				}
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
	 *The double click listener of the server view.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private class TreeViewDoubleClickListener implements IDoubleClickListener {

		/**
		 * {@inheritDoc}
		 */
		public void doubleClick(DoubleClickEvent event) {
			IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
			try {
				handlerService.executeCommand("info.novatec.inspectit.rcp.commands.openView", null);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void repositoryAdded(RepositoryDefinition repositoryDefinition) {
		createServerItem(pShelf, repositoryDefinition);
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
		for (PShelfItem shelfItem : items) {
			if (shelfItem.getData().equals(repositoryDefinition)) {
				Control[] controls = shelfItem.getBody().getChildren();
				for (Control control : controls) {
					if (control instanceof Tree) {
						TreeViewer viewer = findTreeViewer((Tree) control);
						if (null != viewer) {
							treeViewers.remove(viewer);
							if (viewer.equals(getSite().getSelectionProvider())) {
								getSite().setSelectionProvider(null);
							}
						}
					}
					control.dispose();
				}
				updateServerItem(shelfItem, repositoryDefinition);
				shelfItem.getBody().layout();
				break;
			}
		}
	}

	/**
	 * Returns the active repository definition, that is the active
	 * {@link PShelfItem} with the data.
	 * 
	 * @return The active repository definition.
	 */
	public RepositoryDefinition getActiveRepositoryDefinition() {
		return (RepositoryDefinition) pShelf.getSelection().getData();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		super.dispose();
		toolkit.dispose();
	}

}