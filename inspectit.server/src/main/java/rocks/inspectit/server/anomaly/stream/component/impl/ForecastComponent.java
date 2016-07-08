/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import rocks.inspectit.server.anomaly.forecast.ForecastFactory;
import rocks.inspectit.server.anomaly.forecast.IForecast;
import rocks.inspectit.server.anomaly.forecast.impl.DoubleExponentialSmoothing;
import rocks.inspectit.server.anomaly.forecast.impl.HoltWintersForecast;
import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.object.StreamContext;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.server.anomaly.stream.utils.StreamUtils;
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

	/**
	 * The season length.
	 */
	@Value("${anomaly.settings.forecast.seasonalDuration}")
	private long seasonalDuration;

	@Value("#{${anomaly.settings.forecast.seasonalDuration} * 3600 / ${anomaly.settings.confidenceBandUpdateInterval}}")
	private int seasonalLength;

	@Autowired
	private SharedStreamProperties streamProperties;

	@Autowired
	private ForecastFactory forecastFactory;

	private int holtWintersRecalculation = 10;

	/**
	 * {@link ExecutorService} for sending keep alive messages.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	private ScheduledExecutorService executorService;

	/**
	 * @param businessTransaction
	 * @param dataList
	 */
	private void calcualate(String businessTransaction, List<StreamObject<InvocationSequenceData>> dataList) {
		String logPrefix = "[" + businessTransaction + "] ";

		StreamContext context = streamProperties.getStreamContext(businessTransaction);

		double[] valueArray = new double[dataList.size()];
		for (int i = 0; i < dataList.size(); i++) {
			valueArray[i] = dataList.get(i).getData().getDuration();
		}

		double mean = StatisticUtils.mean(valueArray);
		context.getDataHistory().add(mean);

		if (context.isWarmUp()) {
			return;
		}

		if (context.getDataHistory().size() <= 2 * seasonalLength) {
			if (context.getForecaster() == null) {
				if (log.isInfoEnabled()) {
					log.info(logPrefix + "Using double exponential smoothing forecasting until enough data has been collected for HoltWinters.");
				}

				initDoubleExponentialSmoothing(context);
			}
		} else {
			if (context.getForecaster() == null || !(context.getForecaster() instanceof HoltWintersForecast)) {
				if (log.isInfoEnabled()) {
					log.info(logPrefix + "Switching forecasting alogirthm to HoltWinters.");
				}

				updateHoltWintersForecast(context);
			} else if (holtWintersRecalculation > 0 && ((HoltWintersForecast) context.getForecaster()).getSeasonIndex() == 0) {
				if (log.isInfoEnabled()) {
					log.info(logPrefix + "Recalculate parameter for HoltWinters forecasting.");
				}

				updateHoltWintersForecast(context);

				holtWintersRecalculation--;
			}
		}

		IForecast forecaster = context.getForecaster();
		if (forecaster != null) {
			forecaster.fit(mean);
			double forecastedValue = forecaster.forecast();
			context.setCurrentMean(forecastedValue);
		}
	}

	private void initDoubleExponentialSmoothing(StreamContext context) {
		DoubleExponentialSmoothing exponentialSmoothing = forecastFactory.createDoubleExponentialSmoothing();

		double[] primitiveHistoryData = ArrayUtils.toPrimitive(context.getDataHistory().toArray(new Double[0]));

		DescriptiveStatistics statistics = new DescriptiveStatistics(primitiveHistoryData);
		double median = new Median().evaluate(primitiveHistoryData);

		for (double dataElement : primitiveHistoryData) {
			if (Math.abs(dataElement - statistics.getMean()) < 3 * statistics.getStandardDeviation()) {
				exponentialSmoothing.fit(dataElement);
			} else {
				exponentialSmoothing.fit(median);
			}
		}

		context.setForecaster(exponentialSmoothing);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(StreamObject<InvocationSequenceData> item) {
		intervalQueue.add(item);

		return EFlowControl.CONTINUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		// replace current queue with new queue
		Queue<StreamObject<InvocationSequenceData>> queue = intervalQueue;
		intervalQueue = new ConcurrentLinkedQueue<StreamObject<InvocationSequenceData>>();

		if (queue.isEmpty()) {
			return;
		}

		// split by business transaction
		Map<String, List<StreamObject<InvocationSequenceData>>> streamMap = StreamUtils.mapByBusinessTransaction(queue);

		for (Entry<String, List<StreamObject<InvocationSequenceData>>> entry : streamMap.entrySet()) {
			try {
				calcualate(entry.getKey(), entry.getValue());
			} catch (Exception e) {
				if (log.isErrorEnabled()) {
					log.error("Error during forecasting..", e);
				}
			}
		}
	}

	public void start() {
		executorService.scheduleAtFixedRate(this, updateInterval, updateInterval, TimeUnit.SECONDS);
	}

	/**
	 * Finding the best parameter based on the current data and creates a new
	 * {@link HoltWintersForecast} instance using the calculated parameter.
	 *
	 * @param context
	 *            the current stream context
	 */
	private void updateHoltWintersForecast(StreamContext context) {
		// find best parameter
		Double[] historyData = context.getDataHistory().toArray(new Double[0]);
		double[] parameter = HoltWintersForecast.bruteForceParameterDetermination(forecastFactory, ArrayUtils.toPrimitive(historyData));

		if (log.isInfoEnabled()) {
			log.info("[{}] |-New HoltWinters parameter: alpha={}, beta={}, gamma={} producing a RMSE of {}", context.getBusinessTransaction(), parameter[1], parameter[2], parameter[3], parameter[0]);
		}

		// create the instance and set the parameter
		HoltWintersForecast holtWinters = forecastFactory.createHoltWinters();
		holtWinters.setSmoothingFactor(parameter[1]);
		holtWinters.setTrendSmoothingFactor(parameter[2]);
		holtWinters.setSeasonalSmoothingFactor(parameter[3]);

		holtWinters.train(ArrayUtils.toPrimitive(historyData));

		context.setForecaster(holtWinters);
	}
}
