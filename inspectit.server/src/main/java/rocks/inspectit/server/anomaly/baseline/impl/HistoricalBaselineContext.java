package rocks.inspectit.server.anomaly.baseline.impl;

import java.util.Arrays;

import rocks.inspectit.server.anomaly.baseline.AbstractBaselineContext;
import rocks.inspectit.shared.cs.ci.anomaly.definition.baseline.HistoricalBaselineDefinition;

public class HistoricalBaselineContext extends AbstractBaselineContext<HistoricalBaselineDefinition> {

	private double[] valueStore;

	private int currentIndex = 0;

	private double currentValue = Double.NaN;

	private double trendValue = Double.NaN;

	/**
	 * Gets {@link #valueStore}.
	 *
	 * @return {@link #valueStore}
	 */
	public double[] getValueStore() {
		return this.valueStore;
	}

	/**
	 * Sets {@link #valueStore}.
	 *
	 * @param valueStore
	 *            New value for {@link #valueStore}
	 */
	public void setValueStore(double[] valueStore) {
		this.valueStore = valueStore;
	}

	/**
	 * Gets {@link #currentIndex}.
	 *
	 * @return {@link #currentIndex}
	 */
	public int getCurrentIndex() {
		return this.currentIndex;
	}

	/**
	 * Sets {@link #currentIndex}.
	 *
	 * @param currentIndex
	 *            New value for {@link #currentIndex}
	 */
	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}

	/**
	 * Gets {@link #currentValue}.
	 *
	 * @return {@link #currentValue}
	 */
	public double getCurrentValue() {
		return this.currentValue;
	}

	/**
	 * Sets {@link #currentValue}.
	 *
	 * @param currentValue
	 *            New value for {@link #currentValue}
	 */
	public void setCurrentValue(double currentValue) {
		this.currentValue = currentValue;
	}

	/**
	 * Gets {@link #trendValue}.
	 *
	 * @return {@link #trendValue}
	 */
	public double getTrendValue() {
		return this.trendValue;
	}

	/**
	 * Sets {@link #trendValue}.
	 *
	 * @param trendValue
	 *            New value for {@link #trendValue}
	 */
	public void setTrendValue(double trendValue) {
		this.trendValue = trendValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
		valueStore = new double[getDefinition().getSeasonLength()];
		Arrays.fill(valueStore, Double.NaN);
	}
}