package rocks.inspectit.server.anomaly.definition.threshold;

import rocks.inspectit.server.anomaly.definition.AbstractDefinition;

/**
 * @author Marius Oehler
 *
 */
public abstract class ThresholdDefinition extends AbstractDefinition {

	private boolean excludeCriticalData;

	/**
	 * Gets {@link #excludeCriticalData}.
	 *
	 * @return {@link #excludeCriticalData}
	 */
	public boolean isExcludeCriticalData() {
		return this.excludeCriticalData;
	}

	/**
	 * Sets {@link #excludeCriticalData}.
	 *
	 * @param excludeCriticalData
	 *            New value for {@link #excludeCriticalData}
	 */
	public void setExcludeCriticalData(boolean excludeCriticalData) {
		this.excludeCriticalData = excludeCriticalData;
	}

}
