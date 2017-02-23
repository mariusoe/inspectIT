package rocks.inspectit.shared.cs.ci.anomaly.definition.anomaly;

/**
 * @author Marius Oehler
 *
 */
public class AnomalyDefinition {

	private int startCount;

	private int endCount;

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
