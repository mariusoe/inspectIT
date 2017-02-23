package rocks.inspectit.shared.cs.ci.anomaly.definition.metric;

import rocks.inspectit.shared.cs.ci.anomaly.definition.AbstractDefinition;

/**
 * @author Marius Oehler
 *
 */
public abstract class MetricDefinition extends AbstractDefinition {

	private double defaultValue = Double.NaN;

	/**
	 * Gets {@link #defaultValue}.
	 * 
	 * @return {@link #defaultValue}
	 */
	public double getDefaultValue() {
		return this.defaultValue;
	}

	/**
	 * Sets {@link #defaultValue}.
	 * 
	 * @param defaultValue
	 *            New value for {@link #defaultValue}
	 */
	public void setDefaultValue(double defaultValue) {
		this.defaultValue = defaultValue;
	}
}
