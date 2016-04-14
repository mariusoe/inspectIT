/**
 *
 */
package info.novatec.inspectit.cmr.anomaly.utils.processor.impl;

import info.novatec.inspectit.cmr.anomaly.utils.AnomalyUtils;
import info.novatec.inspectit.cmr.anomaly.utils.QueryHelper;
import info.novatec.inspectit.cmr.anomaly.utils.processor.IStatisticProcessor;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the holt-winters approach.
 *
 * @author Marius Oehler
 *
 */
public class HoltWintersSmoothing implements IStatisticProcessor {

	/**
	 * Logger for the class.
	 */
	private final Logger log = LoggerFactory.getLogger(HoltWintersSmoothing.class);

	/**
	 * The time constant.
	 */
	private final double timeConstant;

	/**
	 * The trend smoothing factor.
	 */
	private final double trendSmoothingFactor;

	/**
	 * The trend smoothing factor.
	 */
	private final double seasonSmoothingFactor;

	/**
	 * The length (milliseconds) of a season interval.
	 */
	private final long seasonLength;

	/**
	 * The time of the last push.
	 */
	private long lastTime;

	/**
	 * The current value.
	 */
	private double currentValue;

	/**
	 * The current trend value.
	 */
	private double currentTrend;

	/**
	 * The current season value.
	 */
	private double currentSeason;

	private boolean initialized = false;

	private final int EXISTING_SEASONS = 3;

	private final int CHUNK_SIZE = 5000;

	private final long samplingRate;

	private QueryHelper queryHelper;

	private final double[] seasonalCache;

	/**
	 * @param timeConstant
	 * @param trendSmoothingFactor
	 * @param seasonSmoothingFactor
	 * @param timeUnit
	 * @param queryHelper
	 */
	public HoltWintersSmoothing(double timeConstant, double trendSmoothingFactor, double seasonSmoothingFactor, long seasonLength, long samplingRate, TimeUnit timeUnit) {
		this.timeConstant = timeConstant;
		this.trendSmoothingFactor = trendSmoothingFactor;
		this.seasonSmoothingFactor = seasonSmoothingFactor;
		this.samplingRate = timeUnit.toMillis(samplingRate);
		this.seasonLength = seasonLength;

		seasonalCache = new double[(int) seasonLength];
	}

	/**
	 * Sets {@link #queryHelper}.
	 *
	 * @param queryHelper
	 *            New value for {@link #queryHelper}
	 */
	public void setQueryHelper(QueryHelper queryHelper) {
		this.queryHelper = queryHelper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void push(long time, double value) {
		long deltaTime = time - lastTime;
		double smoothingFactor = AnomalyUtils.calculateSmoothingFactor(timeConstant, deltaTime);

		if (!initialized) {

			currentValue = value;
			currentTrend = b0(time);

			double x = x(time);

			double a1 = a(time, 1);

			for (int i = 1; i <= seasonLength; i++) {
				seasonalCache[i - 1] = c(time, i);
			}

			currentSeason = seasonalCache[(int) ((time % (samplingRate * seasonLength)) / samplingRate)];

			initialized = true;
		} else {
			// currentTrend = b_t-1
			// currentValue = s_t-1

			int seasonIndex = (int) ((time % (samplingRate * seasonLength)) / samplingRate);
			currentSeason = seasonalCache[seasonIndex];

			double nextValue = smoothingFactor * (value * currentSeason) + (1 - smoothingFactor) * (currentValue + currentTrend);

			currentTrend = trendSmoothingFactor * (nextValue - currentValue) + (1 - trendSmoothingFactor) * currentTrend;

			seasonalCache[seasonIndex] = seasonSmoothingFactor * (value / nextValue) + (1 - seasonSmoothingFactor) * seasonalCache[seasonIndex];

			currentValue = nextValue;

		}

		lastTime = time;
	}

	private double b0(long time) {
		double sum = 0;

		long seasonZero = time - time % seasonLength * samplingRate - EXISTING_SEASONS * (seasonLength * samplingRate);

		for (int i = 1; i <= seasonLength; i++) {
			sum += (x(seasonZero + seasonLength * samplingRate + i * samplingRate) - x(seasonZero + i * samplingRate)) / EXISTING_SEASONS;
		}
		return sum / seasonLength;
	}

	private double c(long time, int i) {
		double sum = 0;

		for (int j = 1; j <= EXISTING_SEASONS; j++) {
			long timeAligned = time - time % seasonLength * samplingRate - j * (seasonLength * samplingRate);
			timeAligned += i * samplingRate; // da x() nach hinten mittelt

			sum += x(timeAligned) / a(time, j);
		}

		return sum / EXISTING_SEASONS;
	}

	/**
	 *
	 * @param j
	 *            wieviele season zurück. 0=aktuell. 1=vorherige
	 * @return
	 */
	private double a(long time, int j) {
		long seasonAligned = time - time % seasonLength * samplingRate - j * (seasonLength * samplingRate);
		return queryHelper.queryDouble("MEAN(\"duration\")", "invocation_sequences", "time >" + seasonAligned + "ms AND time < " + (seasonAligned + seasonLength * samplingRate) + "ms");
	}

	private double x(long t) {
		long timeAligned = t - t % samplingRate;
		return queryHelper.queryDouble("MEAN(\"duration\")", "invocation_sequences", "time <" + timeAligned + "ms AND time > " + (timeAligned - samplingRate) + "ms");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getValue() {
		return currentValue;
	}

	/**
	 * Gets {@link #currentSeason}.
	 *
	 * @return {@link #currentSeason}
	 */
	public double getCurrentSeason() {
		return currentSeason;
	}

}
