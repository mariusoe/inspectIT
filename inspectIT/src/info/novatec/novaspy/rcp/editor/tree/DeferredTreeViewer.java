package info.novatec.novaspy.rcp.editor.tree;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.eclipse.ui.progress.PendingUpdateAdapter;

/**
 * This tree viewer works in conjunction with the
 * {@link DeferredTreeContentManager} so that the expand function will work.
 * 
 * @author Patrice Bouillet
 * 
 */
public class DeferredTreeViewer extends TreeViewer {

	/**
	 * Maps the parent widgets to the level so that we know how deep we want to
	 * go.
	 */
	private Map<Widget, Integer> parentWidgets = Collections.synchronizedMap(new HashMap<Widget, Integer>());

	/**
	 * Creates a tree viewer on a newly-created tree control under the given
	 * parent. The tree control is created using the SWT style bits
	 * <code>MULTI, H_SCROLL, V_SCROLL,</code> and <code>BORDER</code>. The
	 * viewer has no input, no content provider, a default label provider, no
	 * sorter, and no filters.
	 * 
	 * @param parent
	 *            the parent control
	 */
	public DeferredTreeViewer(Composite parent) {
		super(parent);
	}

	/**
	 * Creates a tree viewer on the given tree control. The viewer has no input,
	 * no content provider, a default label provider, no sorter, and no filters.
	 * 
	 * @param tree
	 *            the tree control
	 */
	public DeferredTreeViewer(Tree tree) {
		super(tree);
	}

	/**
	 * Creates a tree viewer on a newly-created tree control under the given
	 * parent. The tree control is created using the given SWT style bits. The
	 * viewer has no input, no content provider, a default label provider, no
	 * sorter, and no filters.
	 * 
	 * @param parent
	 *            the parent control
	 * @param style
	 *            the SWT style bits used to create the tree.
	 */
	public DeferredTreeViewer(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalAdd(Widget widget, Object parentElement, Object[] childElements) {
		super.internalAdd(widget, parentElement, childElements);

		// check if we are currently in the process of expanding the child
		// elements
		if (parentWidgets.containsKey(widget)) {
			// iterate over all child elements
			for (Object object : childElements) {
				// is it expandable
				if (super.isExpandable(object)) {
					// get the level
					Integer level = parentWidgets.get(widget);
					if (level == TreeViewer.ALL_LEVELS) {
						super.expandToLevel(object, TreeViewer.ALL_LEVELS);
					} else {
						super.expandToLevel(object, level - 1);
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalExpandToLevel(Widget widget, int level) {
		if (level > 1 || TreeViewer.ALL_LEVELS == level) {
			// we want to open more than one level, have to take care of that.
			Object data = widget.getData();
			if (!(data instanceof PendingUpdateAdapter)) {
				// just care about our own widgets
				parentWidgets.put(widget, Integer.valueOf(level));
			}
		}

		super.internalExpandToLevel(widget, level);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalRemove(Object[] elementsOrPaths) {
		// we want to remove the parent of the PendingUpdateAdapter items from
		// our Map
		if (1 == elementsOrPaths.length) {
			Object object = elementsOrPaths[0];
			if (object instanceof PendingUpdateAdapter) {
				Widget[] widgets = findItems(object);
				if (null != widgets && widgets.length > 0) {
					Widget widget = widgets[0];
					Widget parentWidget = getParentItem((Item) widget);
					if (parentWidgets.containsKey(parentWidget)) {
						parentWidgets.remove(parentWidget);
					}
				}
			}
		}

		super.internalRemove(elementsOrPaths);
	}

}
