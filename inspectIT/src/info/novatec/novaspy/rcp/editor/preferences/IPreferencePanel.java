package info.novatec.novaspy.rcp.editor.preferences;

import java.util.Set;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;

/**
 * The interface for all preference panels.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public interface IPreferencePanel {

	/**
	 * Creates the part control of this view.
	 * 
	 * @param parent
	 *            The parent used to draw the elements to.
	 * @param preferenceSet
	 *            The set containing the preference IDs which are used to show
	 *            the correct options.
	 * @param toolBarManager
	 *            The toolbar manager is needed if buttons are going to be
	 *            displayed. Otherwise it can be <code>null</code>.
	 * 
	 */
	void createPartControl(Composite parent, Set<PreferenceId> preferenceSet, IToolBarManager toolBarManager);

	/**
	 * Registers a callback at this preference panel.
	 * 
	 * @param callback
	 *            The callback to register.
	 */
	void registerCallback(PreferenceEventCallback callback);

	/**
	 * Removes a callback from the preference panel.
	 * 
	 * @param callback
	 *            The callback to remove.
	 */
	void removeCallback(PreferenceEventCallback callback);

	/**
	 * Sets the visibility of the preference panel to show/hide.
	 * 
	 * @param visible
	 *            The visibility state.
	 */
	void setVisible(boolean visible);

	/**
	 * This method is called when an option is changed and should be applied to
	 * all the contained views.
	 */
	void update();

	/**
	 * Disables the live mode in the preference panel.
	 */
	void disableLiveMode();

	/**
	 * Disposes this view / editor.
	 */
	void dispose();

}
