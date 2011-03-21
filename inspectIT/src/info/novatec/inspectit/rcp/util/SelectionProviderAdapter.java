package info.novatec.inspectit.rcp.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Simple class that can publish selection of the view to the site.
 * <p>
 * Class was created similar like the SelectionProviderAdapter class of Eclipse.
 * 
 * @author Ivan Senic
 * 
 */
public class SelectionProviderAdapter implements ISelectionProvider {

	/**
	 * List of listeners.
	 */
	private List<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();

	/**
	 * Current selection.
	 */
	private ISelection theSelection = StructuredSelection.EMPTY;

	/**
	 * {@inheritDoc}
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public ISelection getSelection() {
		return theSelection;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSelection(ISelection selection) {
		theSelection = selection;
		final SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
		for (final ISelectionChangedListener listener : listeners) {
			SafeRunner.run(new SafeRunnable() {
				public void run() {
					listener.selectionChanged(e);
				}
			});
		}
	}

}
