package info.novatec.inspectit.rcp.editor.tree.input;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.tree.TreeSubView;
import info.novatec.inspectit.rcp.editor.tree.TreeViewerComparator;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * The interface for all tree input controller.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface TreeInputController {

	/**
	 * Sets the input definition of this controller.
	 * 
	 * @param inputDefinition
	 *            The input definition.
	 */
	void setInputDefinition(InputDefinition inputDefinition);

	/**
	 * Creates the columns in the given tree viewer.
	 * 
	 * @param treeViewer
	 *            The tree viewer.
	 */
	void createColumns(TreeViewer treeViewer);

	/**
	 * Generates and returns the input for the tree. Returning <code>null</code>
	 * is possible and indicates most of the time that there is no default list
	 * or object to display in the table. For some {@link DefaultData} objects,
	 * the method {@link #canOpenInput(List)} should return true so that the
	 * input object is set by the {@link TreeSubView}.
	 * 
	 * @return The tree input or <code>null</code> if nothing to display for
	 *         default.
	 */
	Object getTreeInput();

	/**
	 * Returns the content provider for the {@link TreeViewer}.
	 * 
	 * @return The content provider.
	 * @see IContentProvider
	 */
	IContentProvider getContentProvider();

	/**
	 * Returns the label provider for the {@link TreeViewer}.
	 * 
	 * @return The label provider
	 * @see IBaseLabelProvider
	 */
	IBaseLabelProvider getLabelProvider();

	/**
	 * Returns the comparator for the {@link TreeViewer}. Can be
	 * <code>null</code> to indicate that no sorting of the elements should be
	 * done.
	 * 
	 * @return The tree viewer comparator.
	 */
	TreeViewerComparator<? extends DefaultData> getComparator();

	/**
	 * Refreshes the current data and updates the tree input if new items are
	 * available.
	 * 
	 * @param monitor
	 *            The progress monitor.
	 */
	void doRefresh(IProgressMonitor monitor);

	/**
	 * This method will be called when a double click event is executed.
	 * 
	 * @param event
	 *            The event object.
	 */
	void doubleClick(DoubleClickEvent event);

	/**
	 * Returns <code>true</code> if the controller can open the input which
	 * consists of one or several {@link DefaultData} objects.
	 * 
	 * @param data
	 *            The data which is checked if the controller can open it.
	 * @return Returns <code>true</code> if the controller can open the input.
	 */
	boolean canOpenInput(List<? extends DefaultData> data);

	/**
	 * Returns all needed preference IDs.
	 * 
	 * @return A {@link Set} containing all {@link PreferenceId}. Returning
	 *         <code>null</code> is not permitted here. At least a
	 *         {@link Collections#EMPTY_SET} should be returned.
	 */
	Set<PreferenceId> getPreferenceIds();

	/**
	 * This method is called whenever something is changed in one of the
	 * preferences.
	 * 
	 * @param preferenceEvent
	 *            The event object containing the changed objects.
	 */
	void preferenceEventFired(PreferenceEvent preferenceEvent);

	/**
	 * This method creates a human readable string out of the given object
	 * (which is object from the tree model).
	 * 
	 * @param object
	 *            The object to create the string from.
	 * @return The created human readable string.
	 */
	String getReadableString(Object object);

	/**
	 * Returns an optional filter for this tree.
	 * 
	 * @return the filter array.
	 */
	ViewerFilter[] getFilters();

	/**
	 * Disposes the tree input.
	 */
	void dispose();

}
