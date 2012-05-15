package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;

import org.eclipse.swt.graphics.Image;

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
	 * The image of the component, if there is any.
	 */
	private Image image;

	/**
	 * The parent component.
	 */
	private Component parent;

	/**
	 * The input definition if there is one. This is used to create the appropriate view.
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

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
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
