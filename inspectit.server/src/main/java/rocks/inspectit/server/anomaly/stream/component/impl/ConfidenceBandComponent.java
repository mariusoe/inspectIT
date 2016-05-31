/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.SwapCache;
import rocks.inspectit.server.anomaly.stream.SwapCache.InternalData;
import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.server.anomaly.utils.processor.impl.DoubleExponentialSmoothing;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class ConfidenceBandComponent extends AbstractSingleStreamComponent<InvocationSequenceData> {

	private final SwapCache swapCache;

	private final double movingAverage = Double.NaN;

	private final double movingAverageSquared = Double.NaN;

	private final ConfidenceBandUpdater baselineUpdater;

	/**
	 * @param nextComponent
	 */
	public ConfidenceBandComponent(ISingleInputComponent<InvocationSequenceData> nextComponent, int cacheSize, ScheduledExecutorService executorService) {
		super(nextComponent);

		baselineUpdater = new ConfidenceBandUpdater();
		swapCache = new SwapCache(cacheSize);

		executorService.scheduleAtFixedRate(baselineUpdater, 5000, 5000, TimeUnit.MILLISECONDS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(InvocationSequenceData item) {
		swapCache.push(item.getDuration());

		return EFlowControl.CONTINUE;
	}

	class ConfidenceBandUpdater implements Runnable {

		/**
		 * Logger for the class.
		 */
		private final Logger log = LoggerFactory.getLogger(ConfidenceBandUpdater.class);

		double baselineDecayFactor = 0.2D;

		DoubleExponentialSmoothing des = new DoubleExponentialSmoothing(1D, 0.75D);
		DoubleExponentialSmoothing desSquared = new DoubleExponentialSmoothing(1D, 0.75D);

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

				des.push(System.currentTimeMillis(), intervalMean);
				desSquared.push(System.currentTimeMillis(), intervalMean * intervalMean);

				// calculate and set new baseline
				// if (Double.isNaN(movingAverage) || Double.isNaN(movingAverageSquared)) {
				// movingAverage = intervalMean;
				// movingAverageSquared = intervalMean * intervalMean;
				// } else {
				// movingAverage =
				// AnomalyUtils.calculateExponentialMovingAverage(baselineDecayFactor,
				// movingAverage, intervalMean);
				// movingAverageSquared =
				// AnomalyUtils.calculateExponentialMovingAverage(baselineDecayFactor,
				// movingAverageSquared, Math.pow(intervalMean, 2));
				// }

				// double stddev = Math.sqrt(movingAverageSquared - Math.pow(movingAverage, 2));

				double stddev = Math.sqrt(desSquared.getValue() - Math.pow(des.getValue(), 2));
				if (Double.isNaN(stddev)) {
					stddev = 0D;
				}

				// SharedStreamProperties.setConfidenceBandUpper(movingAverage + 3 * stddev);
				// SharedStreamProperties.setConfidenceBandLower(movingAverage - 3 * stddev);
				SharedStreamProperties.setConfidenceBandUpper(des.getValue() + 3 * stddev);
				SharedStreamProperties.setConfidenceBandLower(des.getValue() - 3 * stddev);
				SharedStreamProperties.setStandardDeviation(stddev);

				SharedStreamProperties.getInfluxService()
						.insert(Point.measurement("status").addField("threshold_upper", SharedStreamProperties.getConfidenceBandUpper())
								.addField("threshold_lower", SharedStreamProperties.getConfidenceBandLower())
								/*
								 * .addField("movingAverage", movingAverage)
								 * .addField("movingAverageSquared", movingAverageSquared)
								 */.addField("stddev", stddev).addField("des", des.getValue()).build());
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
