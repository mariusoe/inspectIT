/**
 *
 */
package rocks.inspectit.server.anomaly.stream.object;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import rocks.inspectit.server.anomaly.forecast.DoubleExponentialSmoothing;
import rocks.inspectit.server.anomaly.forecast.HoltWintersForecast;
import rocks.inspectit.server.anomaly.stream.ConfidenceBand;

/**
 * @author Marius Oehler
 *
 */
public class StreamContext {

	private final AtomicLong requestCount = new AtomicLong(0L);

	private ConfidenceBand confidenceBand;

	private double standardDeviation;

	private double currentMean;

	private final List<Double> dataHistory = new ArrayList<>();

	private DoubleExponentialSmoothing doubleExponentialSmoothing;

	private HoltWintersForecast holtWintersForecast;

	private long startTime;

	private boolean warmUp = true;

	private boolean anomalyActive = false;

	/**
	 * Gets {@link #anomalyActive}.
	 *
	 * @return {@link #anomalyActive}
	 */
	public boolean isAnomalyActive() {
		return anomalyActive;
	}

	/**
	 * Sets {@link #anomalyActive}.
	 *
	 * @param anomalyActive
	 *            New value for {@link #anomalyActive}
	 */
	public void setAnomalyActive(boolean anomalyActive) {
		this.anomalyActive = anomalyActive;
	}

	/**
	 * Gets {@link #warmUp}.
	 *
	 * @return {@link #warmUp}
	 */
	public boolean isWarmUp() {
		return warmUp;
	}

	/**
	 * Sets {@link #warmUp}.
	 *
	 * @param warmUp
	 *            New value for {@link #warmUp}
	 */
	public void setWarmUp(boolean warmUp) {
		this.warmUp = warmUp;
	}

	/**
	 * Gets {@link #startTime}.
	 *
	 * @return {@link #startTime}
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * Sets {@link #startTime}.
	 *
	 * @param startTime
	 *            New value for {@link #startTime}
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * Gets {@link #holtWintersForecast}.
	 *
	 * @return {@link #holtWintersForecast}
	 */
	public HoltWintersForecast getHoltWintersForecast() {
		return holtWintersForecast;
	}

	/**
	 * Sets {@link #holtWintersForecast}.
	 *
	 * @param holtWintersForecast
	 *            New value for {@link #holtWintersForecast}
	 */
	public void setHoltWintersForecast(HoltWintersForecast holtWintersForecast) {
		this.holtWintersForecast = holtWintersForecast;
	}

	/**
	 * Gets {@link #doubleExponentialSmoothing}.
	 *
	 * @return {@link #doubleExponentialSmoothing}
	 */
	public DoubleExponentialSmoothing getDoubleExponentialSmoothing() {
		return doubleExponentialSmoothing;
	}

	/**
	 * Sets {@link #doubleExponentialSmoothing}.
	 *
	 * @param doubleExponentialSmoothing
	 *            New value for {@link #doubleExponentialSmoothing}
	 */
	public void setDoubleExponentialSmoothing(DoubleExponentialSmoothing doubleExponentialSmoothing) {
		this.doubleExponentialSmoothing = doubleExponentialSmoothing;
	}

	/**
	 * Gets {@link #dataHistory}.
	 *
	 * @return {@link #dataHistory}
	 */
	public List<Double> getDataHistory() {
		return dataHistory;
	}

	/**
	 * Gets {@link #currentMean}.
	 *
	 * @return {@link #currentMean}
	 */
	public double getCurrentMean() {
		return currentMean;
	}

	/**
	 * Sets {@link #currentMean}.
	 *
	 * @param currentMean
	 *            New value for {@link #currentMean}
	 */
	public void setCurrentMean(double currentMean) {
		this.currentMean = currentMean;
	}

	/**
	 * Gets {@link #confidenceBand}.
	 *
	 * @return {@link #confidenceBand}
	 */
	public ConfidenceBand getConfidenceBand() {
		return confidenceBand;
	}

	/**
	 * Sets {@link #confidenceBand}.
	 *
	 * @param confidenceBand
	 *            New value for {@link #confidenceBand}
	 */
	public void setConfidenceBand(ConfidenceBand confidenceBand) {
		this.confidenceBand = confidenceBand;
	}

	/**
	 * Gets {@link #standardDeviation}.
	 *
	 * @return {@link #standardDeviation}
	 */
	public double getStandardDeviation() {
		return standardDeviation;
	}

	/**
	 * Sets {@link #standardDeviation}.
	 *
	 * @param standardDeviation
	 *            New value for {@link #standardDeviation}
	 */
	public void setStandardDeviation(double standardDeviation) {
		this.standardDeviation = standardDeviation;
	}

	public void incrementRequestCount() {
		requestCount.incrementAndGet();
	}

	/**
	 * Gets {@link #requestCount}.
	 *
	 * @return {@link #requestCount}
	 */
	public long getRequestCount() {
		return requestCount.get();
	}

	private String businessTransaction;

	/**
	 * Sets {@link #businessTransaction}.
	 *
	 * @param businessTransaction
	 *            New value for {@link #businessTransaction}
	 */
	public void setBusinessTransaction(String businessTransaction) {
		this.businessTransaction = businessTransaction;
	}

	/**
	 * Gets {@link #businessTransaction}.
	 *
	 * @return {@link #businessTransaction}
	 */
	public String getBusinessTransaction() {
		return businessTransaction;
	}

}
