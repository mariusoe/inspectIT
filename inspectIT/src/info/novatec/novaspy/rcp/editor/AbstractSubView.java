package info.novatec.novaspy.rcp.editor;

import info.novatec.novaspy.rcp.editor.root.AbstractRootEditor;

import org.eclipse.core.runtime.Assert;

/**
 * Common abstract class for all sub-views.
 * 
 * @author Patrice Bouillet
 * 
 */
public abstract class AbstractSubView implements ISubView {

	/**
	 * The root editor.
	 */
	private AbstractRootEditor rootEditor;

	/**
	 * {@inheritDoc}
	 */
	public void setRootEditor(AbstractRootEditor rootEditor) {
		Assert.isNotNull(rootEditor);

		this.rootEditor = rootEditor;
	}

	/**
	 * {@inheritDoc}
	 */
	public AbstractRootEditor getRootEditor() {
		Assert.isNotNull(rootEditor);

		return rootEditor;
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
	}

}
