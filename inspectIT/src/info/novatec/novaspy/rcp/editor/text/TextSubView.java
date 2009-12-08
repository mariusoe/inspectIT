package info.novatec.novaspy.rcp.editor.text;

import info.novatec.novaspy.communication.DefaultData;
import info.novatec.novaspy.rcp.editor.AbstractSubView;
import info.novatec.novaspy.rcp.editor.preferences.PreferenceId;
import info.novatec.novaspy.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.novaspy.rcp.editor.text.input.TextInputController;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * This class is for text views.
 * 
 * @author Eduard Tudenhoefner
 * @author Patrice Bouillet
 * 
 */
public class TextSubView extends AbstractSubView {

	/**
	 * The {@link Composite}.
	 */
	private Composite composite;

	/**
	 * The {@link TextInputController}.
	 */
	private TextInputController textInputController;

	/**
	 * The constructor accepting one parameter.
	 * 
	 * @param textInputController
	 *            An instance of the {@link TextInputController}.
	 */
	public TextSubView(TextInputController textInputController) {
		this.textInputController = textInputController;
	}

	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		textInputController.setInputDefinition(getRootEditor().getInputDefinition());

		composite = toolkit.createComposite(parent);
		composite.setLayout(new GridLayout(1, false));
		textInputController.createPartControl(composite, toolkit);
	}

	/**
	 * {@inheritDoc}
	 */
	public void doRefresh() {
		Assert.isNotNull(textInputController);

		textInputController.doRefresh();
	}

	/**
	 * {@inheritDoc}
	 */
	public Control getControl() {
		return composite;
	}

	/**
	 * {@inheritDoc}
	 */
	public ISelectionProvider getSelectionProvider() {
		return null;
	}

	/**
	 * Returns an instance of a {@link TextInputController}.
	 * 
	 * @return An instance of a {@link TextInputController}.
	 */
	public TextInputController getTextInputController() {
		return textInputController;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDataInput(List<? extends DefaultData> data) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PreferenceId> getPreferenceIds() {
		return Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		textInputController.dispose();
	}

}
