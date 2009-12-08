package info.novatec.novaspy.rcp.editor.text.input;

import info.novatec.novaspy.rcp.editor.InputDefinition;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * The controller for all text inputs.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface TextInputController {

	/**
	 * Sets the input definition of this controller.
	 * 
	 * @param inputDefinition
	 *            The input definition.
	 */
	void setInputDefinition(InputDefinition inputDefinition);

	/**
	 * Returns an object containing the composite with the whole input.
	 * 
	 * @param parent
	 *            The parent used to draw the elements to.
	 * @param toolkit
	 *            The form toolkit.
	 */
	void createPartControl(Composite parent, FormToolkit toolkit);

	/**
	 * The do refresh method is called at least one time to fill the labels with
	 * some initial data. It depends on several settings if this method is
	 * called repeatedly.
	 */
	void doRefresh();

	/**
	 * Disposes this view / editor.
	 */
	void dispose();

}
