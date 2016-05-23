/**
 *
 */
package rocks.inspectit.server.anomaly.stream.object;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import rocks.inspectit.server.anomaly.forecast.IForecast;

/**
 * @author Marius Oehler
 *
 */
public class StreamContext {

	private final AtomicLong requestCount = new AtomicLong(0L);

	private ConfidenceBand confidenceBand;

	private double currentMean = Double.NaN;

	private final List<Double> dataHistory = new ArrayList<>();

	private long startTime;

	private boolean warmingUp = true;

	private boolean anomalyActive = false;

	private final SummaryStatistics standardDeviationStatistics = new SummaryStatistics();

	private IForecast forecaster;

	/**
	 * Gets {@link #forecaster}.
	 *
	 * @return {@link #forecaster}
	 */
	public IForecast getForecaster() {
		return forecaster;
	}

	/**
	 * Sets {@link #forecaster}.
	 *
	 * @param forecaster
	 *            New value for {@link #forecaster}
	 */
	public void setForecaster(IForecast forecaster) {
		this.forecaster = forecaster;
	}

	/**
	 * Gets {@link #standardDeviationStatistics}.
	 *
	 * @return {@link #standardDeviationStatistics}
	 */
	public SummaryStatistics getStandardDeviationStatistics() {
		return standardDeviationStatistics;
	}

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
	 * Gets {@link #warmingUp}.
	 *
	 * @return {@link #warmingUp}
	 */
	public boolean isWarmingUp() {
		return warmingUp;
	}

	/**
	 * Sets {@link #warmingUp}.
	 *
	 * @param warmUp
	 *            New value for {@link #warmingUp}
	 */
	public void setWarmingUp(boolean warmUp) {
		this.warmingUp = warmUp;
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
