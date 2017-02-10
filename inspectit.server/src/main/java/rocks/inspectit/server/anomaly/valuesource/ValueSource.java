package rocks.inspectit.server.anomaly.valuesource;

/**
 * @author Marius Oehler
 *
 */
public abstract class ValueSource {

	private long aggregationWindowLength;

	public abstract double getValue();

	public abstract double[] getValues(int count);

	/**
	 * Gets {@link #aggregationWindowLength}.
	 *
	 * @return {@link #aggregationWindowLength}
	 */
	protected long getAggregationWindowLength() {
		return this.aggregationWindowLength;
	}

	/**
	 * Sets {@link #aggregationWindowLength}.
	 *
	 * @param aggregationWindowLength
	 *            New value for {@link #aggregationWindowLength}
	 */
	public void setAggregationWindowLength(long aggregationWindowLength) {
		this.aggregationWindowLength = aggregationWindowLength;
	}
}
