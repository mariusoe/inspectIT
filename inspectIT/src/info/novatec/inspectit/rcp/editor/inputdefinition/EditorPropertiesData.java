package info.novatec.inspectit.rcp.editor.inputdefinition;

import org.eclipse.jface.resource.ImageDescriptor;

import com.google.common.base.Objects;

/**
 * Class that defines the editor properties like title, image, description.
 * 
 * @author Ivan Senic
 * 
 */
public class EditorPropertiesData {

	/**
	 * The name of the view part.
	 */
	private String partName = "Editor title";

	/**
	 * The tooltip is shown when hovering over the view part tab.
	 */
	private String partTooltip = "Editor tooltip";

	/**
	 * The imageDescriptor descriptor of the view.
	 */
	private ImageDescriptor imageDescriptor;

	/**
	 * String used by the {@link Form} currently to display the text on it.
	 */
	private String headerText = "Form title";

	/**
	 * String used by the {@link Form} currently to display the message on it.
	 */
	private String headerDescription = "Form description";

	/**
	 * Gets {@link #partName}.
	 * 
	 * @return {@link #partName}
	 */
	public String getPartName() {
		return partName;
	}

	/**
	 * Sets {@link #partName}.
	 * 
	 * @param partName
	 *            New value for {@link #partName}
	 */
	public void setPartName(String partName) {
		this.partName = partName;
	}

	/**
	 * Gets {@link #partTooltip}.
	 * 
	 * @return {@link #partTooltip}
	 */
	public String getPartTooltip() {
		return partTooltip;
	}

	/**
	 * Sets {@link #partTooltip}.
	 * 
	 * @param partTooltip
	 *            New value for {@link #partTooltip}
	 */
	public void setPartTooltip(String partTooltip) {
		this.partTooltip = partTooltip;
	}

	/**
	 * Gets {@link #imageDescriptor}.
	 * 
	 * @return {@link #imageDescriptor}
	 */
	public ImageDescriptor getImageDescriptor() {
		return imageDescriptor;
	}

	/**
	 * Sets {@link #imageDescriptor}.
	 * 
	 * @param imageDescriptor
	 *            New value for {@link #imageDescriptor}
	 */
	public void setImageDescriptor(ImageDescriptor imageDescriptor) {
		this.imageDescriptor = imageDescriptor;
	}

	/**
	 * Gets {@link #headerText}.
	 * 
	 * @return {@link #headerText}
	 */
	public String getHeaderText() {
		return headerText;
	}

	/**
	 * Sets {@link #headerText}.
	 * 
	 * @param headerText
	 *            New value for {@link #headerText}
	 */
	public void setHeaderText(String headerText) {
		this.headerText = headerText;
	}

	/**
	 * Gets {@link #headerDescription}.
	 * 
	 * @return {@link #headerDescription}
	 */
	public String getHeaderDescription() {
		return headerDescription;
	}

	/**
	 * Sets {@link #headerDescription}.
	 * 
	 * @param headerDescription
	 *            New value for {@link #headerDescription}
	 */
	public void setHeaderDescription(String headerDescription) {
		this.headerDescription = headerDescription;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(partName, partTooltip, imageDescriptor, headerText, headerDescription);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		}
		EditorPropertiesData that = (EditorPropertiesData) object;
		return Objects.equal(this.partName, that.partName)
				&& Objects.equal(this.partTooltip, that.partTooltip)
				&& Objects.equal(this.imageDescriptor, that.imageDescriptor)
				&& Objects.equal(this.headerText, that.headerText)
				&& Objects.equal(this.headerDescription, that.headerDescription);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("partName", partName)
				.add("partTooltip", partTooltip)
				.add("imageDescriptor", imageDescriptor)
				.add("headerText", headerText)
				.add("headerDescription", headerDescription)
				.toString();
	}

}