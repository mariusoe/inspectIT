package info.novatec.novaspy.rcp.editor;

import info.novatec.novaspy.communication.DefaultData;
import info.novatec.novaspy.rcp.editor.preferences.PreferenceId;
import info.novatec.novaspy.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.novaspy.rcp.editor.root.AbstractRootEditor;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Interface used by all sub-views which are creating the final view.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface ISubView {

	/**
	 * Sets the root editor for this sub view. This is needed for event handling
	 * purposes or the access to the preference area.
	 * 
	 * @param rootEditor
	 *            The root editor.
	 */
	void setRootEditor(AbstractRootEditor rootEditor);

	/**
	 * Returns the root editor.
	 * 
	 * @return The root editor.
	 */
	AbstractRootEditor getRootEditor();

	/**
	 * Creates the part control of this view.
	 * 
	 * @param parent
	 *            The parent used to draw the elements to.
	 * @param toolkit
	 *            The form toolkit which is used for defining the colors of the
	 *            widgets. Can be <code>null</code> to indicate that there is no
	 *            toolkit.
	 */
	void createPartControl(Composite parent, FormToolkit toolkit);

	/**
	 * A sub-view should return all preference IDs itself is in need of and the
	 * ones of the children (it is a sub-view containing other views).
	 * 
	 * @return A {@link Set} containing all {@link PreferenceId}. Returning
	 *         <code>null</code> is not permitted here. At least a
	 *         {@link Collections#EMPTY_SET} should be returned.
	 */
	Set<PreferenceId> getPreferenceIds();

	/**
	 * Every sub-view contains some logic to retrieve the data on its own. This
	 * method invokes the refresh process which should update the view.
	 * <p>
	 * For some views, it is possible that they do not show or do anything for
	 * default.
	 */
	void doRefresh();

	/**
	 * This method is called whenever something is changed in one of the
	 * preferences.
	 * 
	 * @param preferenceEvent
	 *            The event object containing the changed objects.
	 */
	void preferenceEventFired(PreferenceEvent preferenceEvent);

	/**
	 * This will set the data input of the view. Every view can initialize
	 * itself with some data (like live data from the server). This is only
	 * needed if some specific needs to be displayed.
	 * 
	 * @param data
	 *            The list of {@link DefaultData} objects.
	 */
	void setDataInput(List<? extends DefaultData> data);

	/**
	 * Returns the control class of this view controller.
	 * 
	 * @return The {@link Control} class.
	 */
	Control getControl();

	/**
	 * Returns the selection provider for this view.
	 * 
	 * @return The selection provider.
	 */
	ISelectionProvider getSelectionProvider();

	/**
	 * Disposes this sub-view.
	 */
	void dispose();

}
