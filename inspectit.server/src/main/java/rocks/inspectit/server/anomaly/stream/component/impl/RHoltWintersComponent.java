/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import java.util.LinkedList;
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
import rocks.inspectit.server.anomaly.stream.SwapCache;
import rocks.inspectit.server.anomaly.stream.SwapCache.InternalData;
import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
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

	private final SwapCache swapCache;

	private final RConnection rConnection;

	private final LinkedList<Double> historyQueue;

	private final LinkedList<ResidualContainer> residualHistory;

	private final int historyLimit;

	/**
	 * @param nextComponent
	 * @throws RserveException
	 */
	public RHoltWintersComponent(ISingleInputComponent<InvocationSequenceData> nextComponent, ScheduledExecutorService executor) throws RserveException {
		super(nextComponent);

		// init fields
		swapCache = new SwapCache(10000);
		historyQueue = new LinkedList<Double>();
		residualHistory = new LinkedList<ResidualContainer>();
		historyLimit = 720; // = 3600s at 5sec interval

		// connect to R
		rConnection = new RConnection("localhost");

		try {
			String rVersionString = rConnection.eval("R.version.string").asString();
			log.info("connected to R version: {}", rVersionString);

			// load library
			REXP loadResult = rConnection.eval("library(\"forecast\")");
			rConnection.eval("data <- c()");
			if (loadResult == null) {
				log.warn("forecast library could not be loaded..");
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
	protected EFlowControl processImpl(InvocationSequenceData item) {
		swapCache.push(item.getDuration());

		return EFlowControl.CONTINUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		swapCache.swap();

		if (swapCache.getInactive().getIndex().get() == 0) {
			return;
		}

		// calculate mean and reset cache
		InternalData data = swapCache.getInactive();

		// calculate mean of recent data
		double mean = StatisticUtils.mean(data.getData(), data.getIndex().get());
		historyQueue.add(mean);
		if (historyQueue.size() > historyLimit) {
			historyQueue.removeFirst();
		}

		// calculate residuals of recent data
		ResidualContainer container = new ResidualContainer();
		for (int i = 0; i < data.getIndex().get(); i++) {
			container.sum += Math.pow(data.getData()[i] - mean, 2);
		}
		container.count = data.getIndex().get();

		residualHistory.add(container);
		if (residualHistory.size() > historyLimit) {
			residualHistory.removeFirst();
		}

		// reset cache
		data.reset();

		long start = System.currentTimeMillis();
		double nextMean = forecastMean();
		System.out.println("duration: " + (System.currentTimeMillis() - start) + "ms");

		double stdDeviation = calculateStandardDeviation();

		double lowerConfidenceLevel = nextMean - 3 * stdDeviation;
		double upperConfidenceLevel = nextMean + 3 * stdDeviation;

		// store result in shared properties
		ConfidenceBand confidenceBand = new ConfidenceBand(nextMean, upperConfidenceLevel, lowerConfidenceLevel);
		SharedStreamProperties.setConfidenceBand(confidenceBand);

		// build influx point
		Builder builder = Point.measurement("R");
		builder.addField("mean", nextMean);
		builder.addField("lowerConfidenceLevel", lowerConfidenceLevel);
		builder.addField("upperConfidenceLevel", upperConfidenceLevel);

		SharedStreamProperties.getInfluxService().insert(builder.build());
	}

	/**
	 *
	 */
	private double calculateStandardDeviation() {
		double sum = 0D;
		int count = 0;
		for (ResidualContainer rContainer : residualHistory) {
			sum += rContainer.sum;
			count += rContainer.count;
		}

		double variance = sum / count;

		return Math.sqrt(variance);
	}

	/**
	 * @param data
	 *
	 */
	private double forecastMean() {
		if (historyQueue.isEmpty()) {
			return Double.NaN;
		}

		try {
			// load data into R
			double[] dataArray = AnomalyUtils.toDoubleArray(historyQueue);
			rConnection.assign("data", dataArray);
			// rConnection.eval("data <- c(data, newData)");
			// System.out.println("size in r: " + rConnection.eval("length(data)").asInteger());

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

	private class ResidualContainer {
		double sum = 0D;

		int count = 0;
	}
}
