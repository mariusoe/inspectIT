package rocks.inspectit.server.anomaly.metric.definition;

import java.util.Map;

/**
 * @author Marius Oehler
 *
 */
public class InfluxDBMetricDefinition extends AbstractMetricDefinition {

	public enum Function {
		MEAN, MIN, MAX, MEDIAN;
	};

	private String measurement;

	private Map<String, String> tagMap;

	private Function function;

	private String field;

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
	 * Gets {@link #tagMap}.
	 *
	 * @return {@link #tagMap}
	 */
	public Map<String, String> getTagMap() {
		return this.tagMap;
	}

	/**
	 * Sets {@link #tagMap}.
	 *
	 * @param tagMap
	 *            New value for {@link #tagMap}
	 */
	public void setTagMap(Map<String, String> tagMap) {
		this.tagMap = tagMap;
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
