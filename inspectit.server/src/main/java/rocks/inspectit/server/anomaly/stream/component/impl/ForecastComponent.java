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
					log.info("Using double exponential smoothing forecasting until enough data has been collected for HoltWinters.");
				}
				context.setDoubleExponentialSmoothing(forecastFactory.createDoubleExponentialSmoothing());
			}

			context.getDoubleExponentialSmoothing().push(mean);
			forecastValue = context.getDoubleExponentialSmoothing().forecast();
		} else {
			if (context.getHoltWintersForecast() == null) {
				if (log.isInfoEnabled()) {
					log.info("Switching forecasting alogirthm to HoltWinters.");
				}
				context.setHoltWintersForecast(forecastFactory.createHoltWinters());
			}

			HoltWintersForecast holtWinters = context.getHoltWintersForecast();

			forecastValue = holtWinters.forecast(ArrayUtils.toPrimitive(context.getDataHistory().toArray(new Double[0])));
		}

		context.setCurrentMean(forecastValue);
	}
}
