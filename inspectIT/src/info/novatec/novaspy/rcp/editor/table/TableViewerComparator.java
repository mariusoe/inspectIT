package info.novatec.novaspy.rcp.editor.table;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * This class is used to provide an easy comparator used for the sorting of
 * table viewers.
 * 
 * @param <E>
 *            Defines the objects which are compared by this implementation.
 * @author Patrice Bouillet
 * 
 */
public abstract class TableViewerComparator<E> extends ViewerComparator {

	/**
	 * The available sort states.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private enum SortState {
		/** State that won't sort. */
		NONE(SWT.NONE),
		/** State that sorts upwards. */
		UP(SWT.UP),
		/** State that sorts downwards. */
		DOWN(SWT.DOWN);

		/**
		 * The swt direction.
		 */
		private int swtDirection;

		/**
		 * Constructor to accept the swt direction.
		 * 
		 * @param swtDirection
		 *            The swt direction.
		 */
		private SortState(int swtDirection) {
			this.swtDirection = swtDirection;
		}
	}

	/**
	 * Defines the current column as an enumeration.
	 */
	private Enum<?> enumSortColumn;

	/**
	 * Default sort state.
	 */
	private SortState sortState = SortState.UP;

	/**
	 * Adds a column to this comparator so it can be used to sort by.
	 * 
	 * @param column
	 *            The {@link TreeColumn} implementation.
	 * @param id
	 *            The id of the {@link TreeColumn} (user-defined).
	 */
	public final void addColumn(final TableColumn column, final Enum<?> id) {
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toggleSortColumn(id);

				Table table = column.getParent();
				table.setSortColumn(column);
				table.setSortDirection(sortState.swtDirection);
			}
		});
	}

	/**
	 * Toggles the sorting of the column.
	 * 
	 * @param id
	 *            The enumeration id.
	 */
	private void toggleSortColumn(Enum<?> id) {
		if (enumSortColumn == id) {
			switch (sortState) {
			case NONE:
				sortState = SortState.UP;
				break;
			case UP:
				sortState = SortState.DOWN;
				break;
			case DOWN:
				sortState = SortState.NONE;
				break;
			default:
				break;
			}
		} else {
			enumSortColumn = id;
			sortState = SortState.UP;
		}
	}

	/**
	 * Returns the column id enumeration on which we are currently sorting.
	 * 
	 * @return The enumeration sorting column.
	 */
	protected final Enum<?> getEnumSortColumn() {
		return enumSortColumn;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public final int compare(Viewer viewer, Object o1, Object o2) {
		if (null == enumSortColumn) {
			return 0;
		}

		// just return 0 if we don't want to sort
		if (SortState.NONE.equals(sortState)) {
			return 0;
		}

		E e1 = (E) o1;
		E e2 = (E) o2;

		int result = compareElements(viewer, e1, e2);

		if (SortState.DOWN.equals(sortState)) {
			result = -result;
		}

		return result;
	}

	/**
	 * Implemented by all extension classes. This performs the actual
	 * comparison. Clients should NOT care about the reversing of the sorting,
	 * this is done by the abstract class.
	 * <p>
	 * This method is called by the {@link #compare(Viewer, Object, Object)}
	 * method. Thus NEVER call super.compare(...) in it.
	 * 
	 * @param viewer
	 *            The viewer.
	 * @param element1
	 *            The first element to compare.
	 * @param element2
	 *            The second element to compare.
	 * @return A negative number if the first element is less than the second
	 *         element; the value <code>0</code> if the first element is equal
	 *         to the second element; and a positive number if the first element
	 *         is greater than the second element
	 */
	protected abstract int compareElements(Viewer viewer, E element1, E element2);

}
