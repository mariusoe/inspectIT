package info.novatec.novaspy.rcp.view.server;

import info.novatec.novaspy.rcp.model.Composite;
import info.novatec.novaspy.rcp.model.TreeModelManager;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.progress.DeferredTreeContentManager;

/**
 * The content provider for the tree viewer used for every single available CMR.
 * 
 * @author Patrice Bouillet
 * 
 */
public class ServerViewContentProvider implements ITreeContentProvider {

	/**
	 * The manager is used to access the deferred objects.
	 */
	private DeferredTreeContentManager manager;

	/**
	 * {@inheritDoc}
	 */
	public Object[] getChildren(Object parentElement) {
		if (manager.isDeferredAdapter(parentElement)) {
			Object[] children = manager.getChildren(parentElement);

			return children;
		} else {
			// direct access to the children
			Composite composite = (Composite) parentElement;
			return composite.getChildren().toArray();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getParent(Object element) {
		if (element instanceof Composite) {
			Composite composite = (Composite) element;
			return composite.getParent();
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasChildren(Object element) {
		if (null == element) {
			return false;
		}

		if (manager.isDeferredAdapter(element)) {
			return manager.mayHaveChildren(element);
		}

		if (element instanceof Composite) {
			Composite composite = (Composite) element;
			return composite.hasChildren();
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getElements(Object inputElement) {
		TreeModelManager treeModelManager = (TreeModelManager) inputElement;
		return treeModelManager.getRootElements();
	}

	/**
	 * {@inheritDoc}
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		manager = new DeferredTreeContentManager((AbstractTreeViewer) viewer);
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
	}

}
