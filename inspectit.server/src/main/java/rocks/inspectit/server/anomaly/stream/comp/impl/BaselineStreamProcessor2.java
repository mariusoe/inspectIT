/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp.impl;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.SwapCache;
import rocks.inspectit.server.anomaly.stream.SwapCache.InternalData;
import rocks.inspectit.server.anomaly.stream.comp.AbstractResultProcessor;
import rocks.inspectit.server.anomaly.stream.comp.ForkStreamProcessor;
import rocks.inspectit.server.anomaly.stream.comp.IStreamProcessor;
import rocks.inspectit.server.anomaly.utils.AnomalyUtils;
import rocks.inspectit.server.tsdb.InfluxDBService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class BaselineStreamProcessor2 extends ForkStreamProcessor<InvocationSequenceData> {

	/**
	 * Logger for the class.
	 */
	private final Logger log = LoggerFactory.getLogger(BaselineStreamProcessor2.class);

	private final SwapCache swapCache;

	private double movingAverage = 0;
	private double movingAverageSquared = 0;
	private double stddev = 0;

	private final BaselineUpdater baselineUpdater;

	private final InfluxDBService influx;

	/**
	 * The result processor.
	 */
	private final AbstractResultProcessor<InvocationSequenceData> resultProcessor;

	/**
	 *
	 */
	public BaselineStreamProcessor2(IStreamProcessor<InvocationSequenceData> nextProcessorA, IStreamProcessor<InvocationSequenceData> nextProcessorB) {
		super(nextProcessorA, nextProcessorB);
	}

	/**
	 * @param influx
	 * @param executorService
	 *
	 */
	public BaselineStreamProcessor2(InfluxDBService influx, int cacheSize, AbstractResultProcessor<InvocationSequenceData> resultProcessor, ScheduledExecutorService executorService) {
		this.influx = influx;
		this.resultProcessor = resultProcessor;
		swapCache = new SwapCache(cacheSize);

		baselineUpdater = new BaselineUpdater();
		executorService.scheduleAtFixedRate(baselineUpdater, 5000, 5000, TimeUnit.MILLISECONDS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processImpl(InvocationSequenceData item) {
		// store duration
		swapCache.push(item.getDuration());

		if (item.getDuration() > SharedStreamProperties.getThreeSigmaThreshold()) {

			resultProcessor.problem(item);

		} else {
			resultProcessor.okay(item);
		}
	}

	/**
	 * Gets {@link #swapCache}.
	 *
	 * @return {@link #swapCache}
	 */
	public SwapCache getSwapCache() {
		return swapCache;
	}

	class BaselineUpdater implements Runnable {

		/**
		 * Logger for the class.
		 */
		private final Logger log = LoggerFactory.getLogger(BaselineUpdater.class);

		double baselineDecayFactor = 0.1D;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				update();
			} catch (Exception e) {
				if (log.isErrorEnabled()) {
					log.error("error during baseline update", e);
				}
			}
		}

		private void update() {
			if (log.isDebugEnabled()) {
				log.debug("update baseline..");
			}

			// swap cache
			swapCache.swap();

			InternalData cacheData = swapCache.getInactive();

			// calculate new baseline
			int currentIndex = cacheData.getIndex().get();
			if (currentIndex > 0) {
				double intervalMean = 0;

				for (int i = 0; i < currentIndex; i++) {
					intervalMean += cacheData.getData()[i];
				}
				// index = amount data
				intervalMean /= currentIndex;

				// calculate and set new baseline
				if (Double.isNaN(movingAverage) || Double.isNaN(movingAverageSquared)) {
					movingAverage = intervalMean;
					movingAverageSquared = intervalMean * intervalMean;
				} else {
					movingAverage = AnomalyUtils.calculateExponentialMovingAverage(baselineDecayFactor, movingAverage, intervalMean);
					movingAverageSquared = AnomalyUtils.calculateExponentialMovingAverage(baselineDecayFactor, movingAverageSquared, intervalMean * intervalMean);
				}

				stddev = Math.sqrt(movingAverageSquared - movingAverage * movingAverage);

				SharedStreamProperties.setThreeSigmaThreshold(movingAverage + 3 * stddev);

				// System.out.println(movingAverage);

				influx.insert(Point.measurement("status").addField("threshold", SharedStreamProperties.getThreeSigmaThreshold()).addField("movingAverage", movingAverage)
						.addField("movingAverageSquared", movingAverageSquared).addField("stddev", stddev).build());
			} else {
				if (log.isDebugEnabled()) {
					log.debug("no data to calculate new baseline");
				}
			}

			// reset old cache data
			cacheData.reset();
		}
	}
}
