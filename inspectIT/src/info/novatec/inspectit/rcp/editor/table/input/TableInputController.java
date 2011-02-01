package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.root.SubViewClassificationController;
import info.novatec.inspectit.rcp.editor.table.TableSubView;
import info.novatec.inspectit.rcp.editor.table.TableViewerComparator;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Shell;

/**
 * The interface for all table input controller.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface TableInputController extends SubViewClassificationController {

	/**
	 * Sets the input definition of this controller.
	 * 
	 * @param inputDefinition
	 *            The input definition.
	 */
	void setInputDefinition(InputDefinition inputDefinition);

	/**
	 * Creates the columns in the given table viewer.
	 * 
	 * @param tableViewer
	 *            The table viewer.
	 */
	void createColumns(TableViewer tableViewer);

	/**
	 * Generates and returns the input for the table. Returning
	 * <code>null</code> is possible and indicates most of the time that there
	 * is no default list or object to display in the table. For some
	 * {@link DefaultData} objects, the method {@link #canOpenInput(List)}
	 * should return true so that the input object is set by the
	 * {@link TableSubView}.
	 * 
	 * @return The table input or <code>null</code> if nothing to display for
	 *         default.
	 */
	Object getTableInput();

	/**
	 * Returns the content provider for the {@link TableViewer}.
	 * 
	 * @return The content provider.
	 * @see IContentProvider
	 */
	IContentProvider getContentProvider();

	/**
	 * Returns the label provider for the {@link TableViewer}.
	 * 
	 * @return The label provider
	 * @see IBaseLabelProvider
	 */
	IBaseLabelProvider getLabelProvider();

	/**
	 * Returns the comparator for the {@link TableViewer}. Can be
	 * <code>null</code> to indicate that no sorting of the elements should be
	 * done.
	 * 
	 * @return The table viewer comparator.
	 */
	TableViewerComparator<? extends DefaultData> getComparator();

	/**
	 * Sets the limit of the displayed elements in the table.
	 * 
	 * @param limit
	 *            The limit value.
	 */
	void setLimit(int limit);

	/**
	 * Refreshes the current data and updates the table input if new items are
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
	 * (which is object from the table model).
	 * 
	 * @param object
	 *            The object to create the string from.
	 * @return The created human readable string.
	 */
	Object getReadableString(Object object);

	/**
	 * Show some details in a pop-up to the user about the selected element.
	 */
	void showDetails(Shell parent, Object element);

	/**
	 * Defines if a selection can show some details or not.
	 * 
	 * @return <code>true</code> if shome details can be shown.
	 */
	boolean canShowDetails();

	/**
	 * Disposes the table input.
	 */
	void dispose();

}
