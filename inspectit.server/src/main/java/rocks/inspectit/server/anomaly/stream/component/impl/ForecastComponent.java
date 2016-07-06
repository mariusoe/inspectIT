/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import rocks.inspectit.server.anomaly.forecast.ForecastFactory;
import rocks.inspectit.server.anomaly.forecast.HoltWintersForecast;
import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.object.StreamContext;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.server.anomaly.utils.StatisticUtils;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
public class ForecastComponent extends AbstractSingleStreamComponent<InvocationSequenceData> implements Runnable {

	/**
	 * Logger for the class.
	 */
	@Log
	private Logger log;

	private Queue<StreamObject<InvocationSequenceData>> intervalQueue = new ConcurrentLinkedQueue<StreamObject<InvocationSequenceData>>();

	@Value("${anomaly.settings.confidenceBandUpdateInterval}")
	private long updateInterval;

	@Autowired
	private SharedStreamProperties streamProperties;

	@Autowired
	private ForecastFactory forecastFactory;

	private int holtWintersRecalculation = 10;

	/**
	 * The season length.
	 */
	@Value("${anomaly.settings.forecast.seasonLength}")
	private long seasonLength;

	/**
	 * {@link ExecutorService} for sending keep alive messages.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	private ScheduledExecutorService executorService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(StreamObject<InvocationSequenceData> item) {
		intervalQueue.add(item);

		return EFlowControl.CONTINUE;
	}

	public void start() {
		executorService.scheduleAtFixedRate(this, updateInterval, updateInterval, TimeUnit.SECONDS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		Queue<StreamObject<InvocationSequenceData>> queue = intervalQueue;
		intervalQueue = new ConcurrentLinkedQueue<StreamObject<InvocationSequenceData>>();

		if (queue.isEmpty()) {
			return;
		}

		Map<String, ArrayList<StreamObject<InvocationSequenceData>>> invocationStreamMap = new HashMap<>();

		for (StreamObject<InvocationSequenceData> iso : queue) {
			if (!invocationStreamMap.containsKey(iso.getContext().getBusinessTransaction())) {
				invocationStreamMap.put(iso.getContext().getBusinessTransaction(), new ArrayList<StreamObject<InvocationSequenceData>>());
			}

			invocationStreamMap.get(iso.getContext().getBusinessTransaction()).add(iso);
		}

		for (Entry<String, ArrayList<StreamObject<InvocationSequenceData>>> entry : invocationStreamMap.entrySet()) {
			try {
				calcualate(entry.getKey(), entry.getValue());
			} catch (Exception e) {
				log.error("Error during forecasting..", e);
			}
		}
	}

	/**
	 * @param businessTransaction
	 * @param dataList
	 */
	private void calcualate(String businessTransaction, ArrayList<StreamObject<InvocationSequenceData>> dataList) {
		String logPrefix = "[" + businessTransaction + "] ";

		StreamContext context = streamProperties.getStreamContext(businessTransaction);

		double[] valueArray = new double[dataList.size()];
		for (int i = 0; i < dataList.size(); i++) {
			valueArray[i] = dataList.get(i).getData().getDuration();
		}

		double mean = StatisticUtils.mean(valueArray);
		context.getDataHistory().add(mean);

		double forecastValue;

		if (context.getDataHistory().size() <= 2 * seasonLength) {
			if (context.getDoubleExponentialSmoothing() == null) {
				if (log.isInfoEnabled()) {
					log.info(logPrefix + "Using double exponential smoothing forecasting until enough data has been collected for HoltWinters.");
				}
				context.setDoubleExponentialSmoothing(forecastFactory.createDoubleExponentialSmoothing());
			}

			context.getDoubleExponentialSmoothing().push(mean);
			forecastValue = context.getDoubleExponentialSmoothing().forecast();
		} else {
			if (context.getHoltWintersForecast() == null) {
				if (log.isInfoEnabled()) {
					log.info(logPrefix + "Switching forecasting alogirthm to HoltWinters.");
				}

				updateHoltWintersForecastInstance(context);
			} else if (holtWintersRecalculation > 0 && context.getHoltWintersForecast().getSeasonIndex() == 0) {
				if (log.isInfoEnabled()) {
					log.info(logPrefix + "Recalculate parameter for HoltWinters forecasting.");
				}

				updateHoltWintersForecastInstance(context);

				holtWintersRecalculation--;
			}

			HoltWintersForecast holtWinters = context.getHoltWintersForecast();

			// add new value and forecast next
			holtWinters.fit(mean);
			forecastValue = holtWinters.forecast();
		}

		context.setCurrentMean(forecastValue);
	}

	/**
	 * Finding the best parameter based on the current data and creates a new
	 * {@link HoltWintersForecast} instance using the calculated parameter.
	 *
	 * @param context
	 *            the current stream context
	 */
	private void updateHoltWintersForecastInstance(StreamContext context) {
		// find best parameter
		Double[] historyData = context.getDataHistory().toArray(new Double[0]);
		double[] parameter = bruteForceParameterDetermination(ArrayUtils.toPrimitive(historyData));

		if (log.isInfoEnabled()) {
			log.info("[{}] |-Found parameter for HoltWinters: alpha={}, beta={}, gamma={} producing a RMSE of {}", context.getBusinessTransaction(), parameter[1], parameter[2], parameter[3],
					parameter[0]);
		}

		// create the instance and set the parameter
		HoltWintersForecast holtWinters = forecastFactory.createHoltWinters();
		holtWinters.setSmoothingFactor(parameter[1]);
		holtWinters.setTrendSmoothingFactor(parameter[2]);
		holtWinters.setSeasonalSmoothingFactor(parameter[3]);

		holtWinters.train(ArrayUtils.toPrimitive(historyData));

		context.setHoltWintersForecast(holtWinters);
	}

	/**
	 * Tries to find the best parameter for the HoltWinter algorithms to fit the given data. This
	 * approach is a work around. A better solution would be to use an algorithm like LBFGS.
	 *
	 * @param data
	 *            the data
	 * @return array containing the minimal RSME, smoothing factor, trend smoothing factor and
	 *         seasonal smoothing factor
	 */
	private double[] bruteForceParameterDetermination(double[] data) {
		double stepSize = 0.05D;

		double minRMSE = Double.MAX_VALUE;
		double alpha = 0;
		double beta = 0;
		double gamma = 0;

		for (double x = 0; x <= 1; x += stepSize) {
			for (double y = 0; y <= 1; y += stepSize) {
				for (double z = 0; z <= 1; z += stepSize) {
					HoltWintersForecast holtWinters = forecastFactory.createHoltWinters();
					holtWinters.setSmoothingFactor(x);
					holtWinters.setTrendSmoothingFactor(y);
					holtWinters.setSeasonalSmoothingFactor(z);

					holtWinters.train(data);

					if (holtWinters.getRootMeanSquaredError() < minRMSE) {
						minRMSE = holtWinters.getRootMeanSquaredError();

						alpha = x;
						beta = y;
						gamma = z;
					}
				}
			}
		}

		return new double[] { minRMSE, alpha, beta, gamma };
	}
}
