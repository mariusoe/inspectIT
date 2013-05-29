package info.novatec.inspectit.rcp.editor.inputdefinition.extra;

import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.rcp.InspectIT;

import java.util.List;

import com.google.common.base.Objects;

/**
 * Input definition extra for the HTTP charting editors.
 * 
 * @author Ivan Senic
 * 
 */
public class HttpChartingInputDefinitionExtra implements IInputDefinitionExtra {

	/**
	 * List of templates that defines what will be included in charting.
	 */
	private List<HttpTimerData> templates;

	/**
	 * Defines if plotting should be based on the {@link InspectIT} tag value.
	 * 
	 * @see HttpTimerData#hasInspectItTaggingHeader()
	 */
	boolean plotByTagValue;

	/**
	 * Gets {@link #templates}.
	 * 
	 * @return {@link #templates}
	 */
	public List<HttpTimerData> getTemplates() {
		return templates;
	}

	/**
	 * Sets {@link #templates}.
	 * 
	 * @param templates
	 *            New value for {@link #templates}
	 */
	public void setTemplates(List<HttpTimerData> templates) {
		this.templates = templates;
	}

	/**
	 * Gets {@link #plotByTagValue}.
	 * 
	 * @return {@link #plotByTagValue}
	 */
	public boolean isPlotByTagValue() {
		return plotByTagValue;
	}

	/**
	 * Sets {@link #plotByTagValue}.
	 * 
	 * @param plotByTagValue
	 *            New value for {@link #plotByTagValue}
	 */
	public void setPlotByTagValue(boolean plotByTagValue) {
		this.plotByTagValue = plotByTagValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(templates, plotByTagValue);
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
		HttpChartingInputDefinitionExtra that = (HttpChartingInputDefinitionExtra) object;
		return Objects.equal(this.templates, that.templates) && Objects.equal(this.plotByTagValue, that.plotByTagValue);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("templates", templates).add("plotByTagValue", plotByTagValue).toString();
	}
}