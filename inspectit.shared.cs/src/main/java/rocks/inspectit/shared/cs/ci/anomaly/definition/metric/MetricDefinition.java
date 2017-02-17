package rocks.inspectit.shared.cs.ci.anomaly.definition.metric;

import rocks.inspectit.shared.cs.ci.anomaly.definition.AbstractDefinition;

/**
 * @author Marius Oehler
 *
 */
public abstract class MetricDefinition extends AbstractDefinition {

	private boolean isAggregated;

	/**
	 * Gets {@link #isAggregated}.
	 *
	 * @return {@link #isAggregated}
	 */
	public boolean isAggregated() {
		return this.isAggregated;
	}

	/**
	 * Sets {@link #isAggregated}.
	 *
	 * @param isAggregated
	 *            New value for {@link #isAggregated}
	 */
	public void setAggregated(boolean isAggregated) {
		this.isAggregated = isAggregated;
	}

}
