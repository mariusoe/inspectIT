package info.novatec.inspectit.rcp.editor.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.eclipse.ui.progress.PendingUpdateAdapter;

/**
 * This tree viewer works in conjunction with the {@link DeferredTreeContentManager} so that the
 * expand function will work.
 * 
 * @author Patrice Bouillet
 * 
 */
public class DeferredTreeViewer extends TreeViewer {

	/**
	 * Maps the parent widgets to the level so that we know how deep we want to go.
	 */
	private Map<Widget, Integer> parentWidgets = Collections.synchronizedMap(new HashMap<Widget, Integer>());

	/**
	 * List of the elements that need to be expanded.
	 */
	private List<Object> objectsToBeExpanded = Collections.synchronizedList(new ArrayList<Object>());

	/**
	 * Object to be selected.
	 */
	private AtomicReference<Object> objectToSelect = new AtomicReference<Object>();

	/**
	 * Creates a tree viewer on a newly-created tree control under the given parent. The tree
	 * control is created using the SWT style bits <code>MULTI, H_SCROLL, V_SCROLL,</code> and
	 * <code>BORDER</code>. The viewer has no input, no content provider, a default label provider,
	 * no sorter, and no filters.
	 * 
	 * @param parent
	 *            the parent control
	 */
	public DeferredTreeViewer(Composite parent) {
		super(parent);
	}

	/**
	 * Creates a tree viewer on the given tree control. The viewer has no input, no content
	 * provider, a default label provider, no sorter, and no filters.
	 * 
	 * @param tree
	 *            the tree control
	 */
	public DeferredTreeViewer(Tree tree) {
		super(tree);
	}

	/**
	 * Creates a tree viewer on a newly-created tree control under the given parent. The tree
	 * control is created using the given SWT style bits. The viewer has no input, no content
	 * provider, a default label provider, no sorter, and no filters.
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
		// we have to activate our own filters first, stupid eclipse
		// implementation which has got two different paths of applying filters
		// ...
		ViewerFilter[] filters = getFilters();
		for (int i = 0; i < filters.length; i++) {
			ViewerFilter filter = filters[i];
			childElements = filter.filter(this, parentElement, childElements);
		}

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

		if (objectsToBeExpanded != null && !objectsToBeExpanded.isEmpty()) {
			// iterate over all child elements
			for (Object object : childElements) {
				// is object in List of objects that need to be expanded?
				if (objectsToBeExpanded.contains(object)) {
					// then expand it
					if (!getExpandedState(object)) {
						super.expandToLevel(object, 1);
					}
				}
			}
		}

		// if there is object to be selected, we will selected if its parent is expanded
		while (true) {
			Object objToSelect = objectToSelect.get();
			if (objToSelect != null && (!isRootElement(objToSelect) || getExpandedState(getParentElement(objToSelect)))) {
				List<Object> selectionList = new ArrayList<Object>();
				Widget w = internalGetWidgetToSelect(objToSelect);
				if (w != null) {
					if (objectToSelect.compareAndSet(objToSelect, null)) {
						selectionList.add(w);
						setSelection(selectionList);
						break;
					}
				} else {
					break;
				}
			} else {
				break;
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

		// when the widget is actually expanding, we have to remove its data from the list of object
		// that
		// needs to be expanded, if the data of the widget is found in the list
		Object data = widget.getData();
		if (data != null && objectsToBeExpanded.contains(data)) {
			objectsToBeExpanded.remove(data);
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

	/**
	 * Expands all ancestors of the given element or tree path so that the given element becomes
	 * visible in this viewer's tree control, and then expands the subtree rooted at the given
	 * element to the given level. The element will be then selected.
	 * 
	 * @param elementOrTreePath
	 *            the element
	 * @param level
	 *            non-negative level, or <code>ALL_LEVELS</code> to expand all levels of the tree
	 */
	public void expandToObjectAndSelect(Object elementOrTreePath, int level) {
		if (checkBusy()) {
			return;
		}
		Object parent = getParentElement(elementOrTreePath);
		// check if the element is already visible, or if it is root
		if ((parent != null && getExpandedState(parent)) || isRootElement(elementOrTreePath)) {
			// then only set selection
			Widget w = internalGetWidgetToSelect(elementOrTreePath);
			if (null != w) {
				List<Object> selectionList = new ArrayList<Object>();
				selectionList.add(w);
				setSelection(selectionList);
			}
			// and overwrite any earlier set selection object
			objectToSelect.set(null);
		} else {
			// get all the objects that need to be expanded so that object is visible
			objectToSelect.set(elementOrTreePath);
			List<Object> objectsToExpand = createObjectList(parent, new ArrayList<Object>());
			objectsToBeExpanded.addAll(objectsToExpand);
			Widget w = internalExpand(elementOrTreePath, true);
			if (w != null) {
				internalExpandToLevel(w, level);
			}
		}
	}

	/**
	 * Constructs the list of elements that need to be expanded, so that object supplied can be
	 * visible.
	 * 
	 * @param object
	 *            Object that expansion should reach.
	 * @param objectList
	 *            List where the results are stored.
	 * @return List of objects for expansion.
	 */
	private List<Object> createObjectList(Object object, List<Object> objectList) {
		if (!isRootElement(object)) {	
			if (!getExpandedState(object)) {
				if (objectList == null) {
					objectList = new ArrayList<Object>();
				}
				objectList.add(object);
				createObjectList(getParentElement(object), objectList);
			}
		}
		return objectList;
	}

	/**
	 * Checks if the given element is one of the root object in the input list of the tree viewer.
	 * 
	 * @param element
	 *            Element to check.
	 * @return True if the element is one of the root objects.
	 */
	@SuppressWarnings("unchecked")
	private boolean isRootElement(Object element) {
		Object input = getRoot();
		if (input != null && input instanceof List) {
			return ((List<Object>) input).contains(element);
		}
		return false;
	}
}
