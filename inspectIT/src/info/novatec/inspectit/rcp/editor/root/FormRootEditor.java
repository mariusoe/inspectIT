package info.novatec.inspectit.rcp.editor.root;

import info.novatec.inspectit.rcp.editor.preferences.FormPreferencePanel;
import info.novatec.inspectit.rcp.editor.preferences.IPreferencePanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * An implementation of a root editor which uses a form to create a nicer view.
 * 
 * @author Patrice Bouillet
 * 
 */
public class FormRootEditor extends AbstractRootEditor {

	/**
	 * The identifier of the {@link FormRootEditor}.
	 */
	public static final String ID = "inspectit.editor.formrooteditor";

	/**
	 * The form toolkit which defines the colors etc.
	 */
	private FormToolkit toolkit;

	/**
	 * The form of the view.
	 */
	private Form form;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createView(Composite parent) {
		// create the toolkit
		this.toolkit = new FormToolkit(parent.getDisplay());

		// create the preference panel with the callback
		IPreferencePanel preferencePanel = new FormPreferencePanel(toolkit);
		// set the preference panel
		setPreferencePanel(preferencePanel);

		// create the form
		form = toolkit.createForm(parent);
		form.setImage(getInputDefinition().getImageDescriptor().createImage());
		form.setText(getInputDefinition().getHeaderText());
		form.setMessage(getInputDefinition().getHeaderDescription());
		form.getBody().setLayout(new GridLayout());
		// decorate the heading to make it look better
		toolkit.decorateFormHeading(form);
		// create an preference area if the subviews are requesting it
		preferencePanel.createPartControl(form.getBody(), getSubView().getPreferenceIds(), form.getToolBarManager());

		// go further with creating the subview(s)
		getSubView().createPartControl(form.getBody(), toolkit);
		getSubView().getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		super.dispose();
		// manually dispose the image of the form
		form.getImage().dispose();
		// dispose of the toolkit
		toolkit.dispose();
	}

	/**
	 * @return the form
	 */
	public Form getForm() {
		return form;
	}
	
	

}
