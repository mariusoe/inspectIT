package rocks.inspectit.shared.cs.ci.anomaly.definition.anomaly;

/**
 * @author Marius Oehler
 *
 */
public class AnomalyDefinition {

	private int startCount;

	private int endCount;

	private boolean notifyOnWarning = true;

	private boolean notifyOnCritical = true;

	/**
	 * Gets {@link #notifyOnWarning}.
	 *
	 * @return {@link #notifyOnWarning}
	 */
	public boolean isNotifyOnWarning() {
		return this.notifyOnWarning;
	}

	/**
	 * Sets {@link #notifyOnWarning}.
	 *
	 * @param notifyOnWarning
	 *            New value for {@link #notifyOnWarning}
	 */
	public void setNotifyOnWarning(boolean notifyOnWarning) {
		this.notifyOnWarning = notifyOnWarning;
	}

	/**
	 * Gets {@link #notifyOnCritical}.
	 *
	 * @return {@link #notifyOnCritical}
	 */
	public boolean isNotifyOnCritical() {
		return this.notifyOnCritical;
	}

	/**
	 * Sets {@link #notifyOnCritical}.
	 *
	 * @param notifyOnCritical
	 *            New value for {@link #notifyOnCritical}
	 */
	public void setNotifyOnCritical(boolean notifyOnCritical) {
		this.notifyOnCritical = notifyOnCritical;
	}

	/**
	 * Gets {@link #startCount}.
	 *
	 * @return {@link #startCount}
	 */
	public int getStartCount() {
		return this.startCount;
	}

	/**
	 * Sets {@link #startCount}.
	 *
	 * @param startCount
	 *            New value for {@link #startCount}
	 */
	public void setStartCount(int startCount) {
		this.startCount = startCount;
	}

	/**
	 * Gets {@link #endCount}.
	 *
	 * @return {@link #endCount}
	 */
	public int getEndCount() {
		return this.endCount;
	}

	/**
	 * Sets {@link #endCount}.
	 *
	 * @param endCount
	 *            New value for {@link #endCount}
	 */
	public void setEndCount(int endCount) {
		this.endCount = endCount;
	}
}
