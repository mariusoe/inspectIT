package rocks.inspectit.shared.cs.ci.anomaly.definition.metric;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marius Oehler
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "anomaly-influx-metric")
public class InfluxDBMetricDefinition extends MetricDefinition {

	public enum Function {
		MEAN, MEDIAN, COUNT, SUM, MAX, MIN;
	};

	@XmlAttribute(name = "measurement")
	private String measurement;

	@XmlElementWrapper(name = "tags")
	private Map<String, String> tags = new HashMap<>();

	@XmlAttribute(name = "function")
	private Function function;

	@XmlAttribute(name = "field")
	private String field;

	@XmlAttribute(name = "opperateOnAggregation")
	private boolean opperateOnAggregation = false;

	/**
	 * Gets {@link #opperateOnAggregation}.
	 *
	 * @return {@link #opperateOnAggregation}
	 */
	public boolean isOpperateOnAggregation() {
		return this.opperateOnAggregation;
	}

	/**
	 * Sets {@link #opperateOnAggregation}.
	 *
	 * @param opperateOnAggregation
	 *            New value for {@link #opperateOnAggregation}
	 */
	public void setOpperateOnAggregation(boolean opperateOnAggregation) {
		this.opperateOnAggregation = opperateOnAggregation;
	}

	/**
	 * Gets {@link #field}.
	 *
	 * @return {@link #field}
	 */
	public String getField() {
		return this.field;
	}

	/**
	 * Sets {@link #field}.
	 *
	 * @param field
	 *            New value for {@link #field}
	 */
	public void setField(String field) {
		this.field = field;
	}

	/**
	 * Gets {@link #measurement}.
	 *
	 * @return {@link #measurement}
	 */
	public String getMeasurement() {
		return this.measurement;
	}

	/**
	 * Sets {@link #measurement}.
	 *
	 * @param measurement
	 *            New value for {@link #measurement}
	 */
	public void setMeasurement(String measurement) {
		this.measurement = measurement;
	}

	/**
	 * Gets {@link #tags}.
	 *
	 * @return {@link #tags}
	 */
	public Map<String, String> getTags() {
		return this.tags;
	}

	/**
	 * Gets {@link #function}.
	 *
	 * @return {@link #function}
	 */
	public Function getFunction() {
		return this.function;
	}

	/**
	 * Sets {@link #function}.
	 *
	 * @param function
	 *            New value for {@link #function}
	 */
	public void setFunction(Function function) {
		this.function = function;
	}

}
