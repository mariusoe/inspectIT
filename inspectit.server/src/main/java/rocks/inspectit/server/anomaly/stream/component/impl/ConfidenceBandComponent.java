/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

import rocks.inspectit.server.anomaly.stream.ConfidenceBand;
import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.SwapCache;
import rocks.inspectit.server.anomaly.stream.SwapCache.InternalData;
import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.server.anomaly.utils.processor.impl.ExponentialSmoothing;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

public class ConfidenceBandComponent extends AbstractSingleStreamComponent<InvocationSequenceData> {

	private final SwapCache swapCache;

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

		double baselineDecayFactor = 0.2D;

		// DoubleExponentialSmoothing des = new DoubleExponentialSmoothing(1D, 0.75D);
		// DoubleExponentialSmoothing desSquared = new DoubleExponentialSmoothing(1D, 0.75D);

		ExponentialSmoothing des = new ExponentialSmoothing(0.2D);
		ExponentialSmoothing desSquared = new ExponentialSmoothing(0.2D);

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				update();
			} catch (Exception e) {
			}
		}

		private void update() {

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

				// hack
				stddev = SharedStreamProperties.getStandardDeviation();

				if (!Double.isNaN(stddev)) {
					// store result in shared properties
					ConfidenceBand confidenceBand = new ConfidenceBand(des.getValue(), des.getValue() + 3 * stddev, des.getValue() - 3 * stddev);
					SharedStreamProperties.setConfidenceBand(confidenceBand);

					// build influx point
					Builder builder = Point.measurement("R");
					builder.addField("mean", des.getValue());
					builder.addField("lowerConfidenceLevel", des.getValue() - 3 * stddev);
					builder.addField("upperConfidenceLevel", des.getValue() + 3 * stddev);

					SharedStreamProperties.getInfluxService().insert(builder.build());
				}
			} else {
			}

			// reset old cache data
			cacheData.reset();
		}
	}
}