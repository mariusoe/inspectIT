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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.server.anomaly.stream.ConfidenceBand;
import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.StreamStatistics;
import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.server.anomaly.stream.object.InvocationStreamObject;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.server.anomaly.utils.AnomalyUtils;
import rocks.inspectit.server.anomaly.utils.StatisticUtils;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class RHoltWintersComponent extends AbstractSingleStreamComponent<InvocationSequenceData> implements Runnable {

	/**
	 * Logger for the class.
	 */
	private final Logger log = LoggerFactory.getLogger(RHoltWintersComponent.class);

	private final RConnection rConnection;

	private final Map<String, LinkedList<Double>> historyListMap;

	private final int historyLimit;

	private Queue<InvocationStreamObject> intervalQueue;

	/**
	 * @param nextComponent
	 * @throws RserveException
	 */
	public RHoltWintersComponent(ISingleInputComponent<InvocationSequenceData> nextComponent, ScheduledExecutorService executor) throws RserveException {
		super(nextComponent);

		// init fields
		historyListMap = new HashMap<>();
		historyLimit = 60; // 720=1h | 60=5m
		intervalQueue = new ConcurrentLinkedQueue<InvocationStreamObject>();

		// connect to R
		rConnection = new RConnection("localhost");

		try {
			String rVersionString = rConnection.eval("R.version.string").asString();
			log.info("||-Connected to Rserve [{}]", rVersionString);

			// load library
			REXP loadResult = rConnection.eval("library(\"forecast\")");
			rConnection.eval("data <- c()");
			if (loadResult == null) {
				log.warn("||-forecast library could not be loaded..");
			}
		} catch (REXPMismatchException e) {
			log.error(e.getMessage());
		}

		// schedule component
		executor.scheduleAtFixedRate(this, 5, 5, TimeUnit.SECONDS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(StreamObject<InvocationSequenceData> item) {
		intervalQueue.add((InvocationStreamObject) item);

		return EFlowControl.CONTINUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		Queue<InvocationStreamObject> queue = intervalQueue;
		intervalQueue = new ConcurrentLinkedQueue<InvocationStreamObject>();

		if (queue.isEmpty()) {
			return;
		}

		Map<String, ArrayList<InvocationStreamObject>> invocationStreamMap = new HashMap<>();

		for (InvocationStreamObject iso : queue) {
			if (!invocationStreamMap.containsKey(iso.getBusinessTransaction())) {
				invocationStreamMap.put(iso.getBusinessTransaction(), new ArrayList<InvocationStreamObject>());
			}

			invocationStreamMap.get(iso.getBusinessTransaction()).add(iso);
		}

		for (Entry<String, ArrayList<InvocationStreamObject>> entry : invocationStreamMap.entrySet()) {
			calcualate(entry.getKey(), entry.getValue());
		}
	}

	private void calcualate(String businessTransaction, List<InvocationStreamObject> invocationList) {
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

		long start = System.currentTimeMillis();
		double nextMean = Math.max(0D, forecastMean(historyList));

		StreamStatistics streamStatistics = SharedStreamProperties.getStreamStatistic(businessTransaction);

		if (!Double.isNaN(nextMean)) {
			System.out.println("duration: " + (System.currentTimeMillis() - start) + "ms");

			double stdDeviation = streamStatistics.getStandardDeviation();

			double lowerConfidenceLevel = Math.max(0D, nextMean - 3 * stdDeviation);
			double upperConfidenceLevel = nextMean + 3 * stdDeviation;

			// store result in shared properties
			ConfidenceBand confidenceBand = new ConfidenceBand(nextMean, upperConfidenceLevel, lowerConfidenceLevel);
			streamStatistics.setConfidenceBand(confidenceBand);

			// build influx point
			Builder builder = Point.measurement("statistics");
			builder.addField("mean", nextMean);
			builder.addField("lowerConfidenceLevel", lowerConfidenceLevel);
			builder.addField("upperConfidenceLevel", upperConfidenceLevel);
			builder.tag("businessTransaction", businessTransaction);

			SharedStreamProperties.getInfluxService().insert(builder.build());
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
