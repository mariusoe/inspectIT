/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.object.StreamContext;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.server.anomaly.utils.AnomalyUtils;
import rocks.inspectit.server.anomaly.utils.StatisticUtils;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
public class RHoltWintersComponent extends AbstractSingleStreamComponent<InvocationSequenceData> implements Runnable {

	/**
	 * Logger for the class.
	 */
	@Log
	private Logger log;

	private RConnection rConnection;

	private final Map<String, LinkedList<Double>> historyListMap = new HashMap<>();

	private Queue<StreamObject<InvocationSequenceData>> intervalQueue = new ConcurrentLinkedQueue<StreamObject<InvocationSequenceData>>();

	@Value("${anomaly.settings.confidenceBandHistorySize}")
	private int historyLimit;

	@Value("${anomaly.settings.confidenceBandUpdateInterval}")
	private long updateInterval;

	@Autowired
	private SharedStreamProperties streamProperties;

	/**
	 * {@link ExecutorService} for sending keep alive messages.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	private ScheduledExecutorService executorService;

	public void start() {
		// connect to R
		try {
			rConnection = new RConnection("localhost");

			String rVersionString = rConnection.eval("R.version.string").asString();
			log.info("||-Connected to Rserve [{}]", rVersionString);

			// load library
			REXP loadResult = rConnection.eval("library(\"forecast\")");
			rConnection.eval("data <- c()");
			if (loadResult == null) {
				log.warn("||-forecast library could not be loaded..");
			}

			executorService.scheduleAtFixedRate(this, updateInterval, updateInterval, TimeUnit.SECONDS);
		} catch (RserveException e) {
			if (log.isErrorEnabled()) {
				log.error("Cannot connect to Rserve.", e);
			}
		} catch (REXPMismatchException e) {
			if (log.isErrorEnabled()) {
				log.error("R error.", e);
			}
		}
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
			calcualate(entry.getKey(), entry.getValue());
		}
	}

	private void calcualate(String businessTransaction, List<StreamObject<InvocationSequenceData>> invocationList) {
		double[] durationArray = new double[invocationList.size()];

		for (int i = 0; i < invocationList.size(); i++) {
			durationArray[i] = invocationList.get(i).getData().getDuration();
		}

		// calculate mean of recent data
		double mean = StatisticUtils.mean(durationArray);

		if (!historyListMap.containsKey(businessTransaction)) {
			historyListMap.put(businessTransaction, new LinkedList<Double>());
		}
		LinkedList<Double> historyList = historyListMap.get(businessTransaction);
		historyList.add(mean);
		if (historyList.size() > historyLimit) {
			historyList.removeFirst();
		}

		double nextMean = Math.max(0D, forecastMean(historyList));

		StreamContext context = streamProperties.getStreamContext(businessTransaction);

		if (!Double.isNaN(nextMean)) {
			context.setCurrentMean(nextMean);
		}
	}

	/**
	 * @param data
	 *
	 */
	private double forecastMean(List<Double> dataList) {
		if (dataList.size() <= 5) {
			return Double.NaN;
		}

		try {
			// load data into R
			double[] dataArray = AnomalyUtils.toDoubleArray(dataList);
			rConnection.assign("data", dataArray);

			// forecast
			REXP result = rConnection.eval("try(fcast <- holt(data, h=1, level=c(95,99.7)), silent=TRUE)");
			if (result.inherits("try-error")) {
				System.err.println("Error: " + result.asString());
			}

			return rConnection.eval("fcast$mean[1]").asDouble();
		} catch (REngineException | REXPMismatchException | RuntimeException e) {
			log.error("error while forecasting. ", e);
			return Double.NaN;
		}
	}
}
