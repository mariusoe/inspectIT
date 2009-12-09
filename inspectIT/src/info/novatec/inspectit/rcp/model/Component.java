package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.rcp.editor.InputDefinition;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * A component can be used in any tree based views.
 * 
 * @author Patrice Bouillet
 * 
 */
public abstract class Component {

	/**
	 * The name of the component.
	 */
	private String name;

	/**
	 * The tooltip of the component, if there is any.
	 */
	private String tooltip = "";

	/**
	 * The image descriptor of the component, if there is any.
	 */
	private ImageDescriptor imageDescriptor;

	/**
	 * The parent component.
	 */
	private Component parent;

	/**
	 * The input definition if there is one. This is used to create the
	 * appropriate view.
	 */
	private InputDefinition inputDefinition;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	public String getTooltip() {
		return tooltip;
	}

	public ImageDescriptor getImageDescriptor() {
		return imageDescriptor;
	}

	public void setImageDescriptor(ImageDescriptor imageDescriptor) {
		this.imageDescriptor = imageDescriptor;
	}

	public Component getParent() {
		return parent;
	}

	public void setParent(Component parent) {
		this.parent = parent;
	}

	public void setInputDefinition(InputDefinition inputDefinition) {
		this.inputDefinition = inputDefinition;
	}

	public InputDefinition getInputDefinition() {
		return inputDefinition;
	}

}
