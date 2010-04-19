package info.novatec.inspectit.rcp.editor.tree;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.AbstractSubView;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.root.FormRootEditor;
import info.novatec.inspectit.rcp.editor.tree.input.TreeInputController;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Sub-view which is used to create a tree.
 * 
 * @author Patrice Bouillet
 * 
 */
public class TreeSubView extends AbstractSubView {

	/**
	 * The referenced input controller.
	 */
	private final TreeInputController treeInputController;

	/**
	 * The created tree viewer.
	 */
	private TreeViewer treeViewer;

	/**
	 * Default constructor which needs a tree input controller to create all the
	 * content etc.
	 * 
	 * @param treeInputController
	 *            The tree input controller.
	 */
	public TreeSubView(TreeInputController treeInputController) {
		Assert.isNotNull(treeInputController);

		this.treeInputController = treeInputController;
	}

	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		treeInputController.setInputDefinition(getRootEditor().getInputDefinition());

		Tree tree = toolkit.createTree(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		tree.setHeaderVisible(true);

		treeViewer = new DeferredTreeViewer(tree);
		treeInputController.createColumns(treeViewer);
		treeViewer.setUseHashlookup(true);
		treeViewer.setContentProvider(treeInputController.getContentProvider());
		treeViewer.setLabelProvider(treeInputController.getLabelProvider());
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				treeInputController.doubleClick(event);
			}
		});
		treeViewer.setComparator(treeInputController.getComparator());
		if (null != treeViewer.getComparator()) {
			TreeColumn[] treeColumns = treeViewer.getTree().getColumns();
			for (TreeColumn treeColumn : treeColumns) {
				treeColumn.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						treeViewer.refresh();
					}
				});
			}
		}
		if (null != treeInputController.getFilters()) {
			treeViewer.setFilters(treeInputController.getFilters());
		}

		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		getRootEditor().getSite().registerContextMenu(FormRootEditor.ID + ".treesubview", menuManager, treeViewer);

		Control control = treeViewer.getControl();
		Menu menu = menuManager.createContextMenu(control);
		control.setMenu(menu);

		Object input = treeInputController.getTreeInput();
		treeViewer.setInput(input);
	}

	/**
	 * {@inheritDoc}
	 */
	public void doRefresh() {
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDataInput(List<? extends DefaultData> data) {
		if (treeInputController.canOpenInput(data)) {
			treeViewer.setInput(data);
			treeViewer.expandToLevel(2);
			treeViewer.refresh();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Control getControl() {
		return treeViewer.getControl();
	}

	/**
	 * {@inheritDoc}
	 */
	public ISelectionProvider getSelectionProvider() {
		return treeViewer;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PreferenceId> getPreferenceIds() {
		return treeInputController.getPreferenceIds();
	}

	/**
	 * {@inheritDoc}
	 */
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		treeInputController.preferenceEventFired(preferenceEvent);
		
		if (PreferenceId.FILTERSENSORTYPE.equals(preferenceEvent.getPreferenceId())) {
			// we have to reapply the filter if there is one
			if (null != treeInputController.getFilters()) {
				treeViewer.setFilters(treeInputController.getFilters());
			}
		}
	}

	/**
	 * Returns the tree viewer.
	 * 
	 * @return The tree viewer.
	 */
	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	/**
	 * Returns the tree input controller.
	 * 
	 * @return The tree input controller.
	 */
	public TreeInputController getTreeInputController() {
		return treeInputController;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		treeInputController.dispose();
	}

}
