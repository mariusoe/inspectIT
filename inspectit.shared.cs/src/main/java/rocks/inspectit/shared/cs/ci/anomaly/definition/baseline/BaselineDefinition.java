package rocks.inspectit.shared.cs.ci.anomaly.definition.baseline;

import rocks.inspectit.shared.cs.ci.anomaly.definition.AbstractDefinition;

/**
 * @author Marius Oehler
 *
 */
public abstract class BaselineDefinition extends AbstractDefinition {

	private boolean excludeCriticalData;

	private boolean excludeWarningData;

	/**
	 * Gets {@link #excludeWarningData}.
	 *
	 * @return {@link #excludeWarningData}
	 */
	public boolean isExcludeWarningData() {
		return this.excludeWarningData;
	}

	/**
	 * Sets {@link #excludeWarningData}.
	 *
	 * @param excludeWarningData
	 *            New value for {@link #excludeWarningData}
	 */
	public void setExcludeWarningData(boolean excludeWarningData) {
		this.excludeWarningData = excludeWarningData;
	}

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
